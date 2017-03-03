package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.Resource
import com.wetjens.powergrid.resource.ResourceType

/**
 * Bureaucracy phase in a game of Power Grid.
 */
data class BureaucracyPhase(private val powerGrid: PowerGrid,
                            private val nextPhase: (PowerGrid) -> PowerGrid,
                            val players: List<Player>,
                            override val currentPlayer: Player = players.first()) : Phase {

    companion object Factory {

        val payments = listOf(10, 22, 33, 44, 54, 64, 73, 82, 90, 98, 105, 112, 118, 124, 129, 134, 138, 142, 145, 148, 150)

        val newResources = mapOf(
                Pair(ResourceType.COAL, listOf(
                        listOf(3, 4, 3), // 2 players
                        listOf(4, 5, 3), // 3 players
                        listOf(5, 6, 4), // etc.
                        listOf(5, 7, 5),
                        listOf(7, 9, 6)
                )),
                Pair(ResourceType.OIL, listOf(
                        listOf(2, 2, 4), // 2 players
                        listOf(2, 3, 4), // 3 players
                        listOf(3, 4, 5), // etc.
                        listOf(4, 5, 6),
                        listOf(5, 6, 7)
                )),
                Pair(ResourceType.BIO_MASS, listOf(
                        listOf(1, 2, 3), // 2 players
                        listOf(1, 2, 3), // 3 players
                        listOf(2, 3, 4), // etc.
                        listOf(3, 3, 5),
                        listOf(3, 5, 6)
                )),
                Pair(ResourceType.URANIUM, listOf(
                        listOf(1, 1, 1), // 2 players
                        listOf(1, 1, 1), // 3 players
                        listOf(1, 2, 2), // etc.
                        listOf(2, 3, 2),
                        listOf(2, 3, 3)
                ))
        )

        fun start(powerGrid: PowerGrid,
                  nextPhase: (PowerGrid) -> PowerGrid,
                  players: List<Player> = powerGrid.playerOrder): PowerGrid {

            val playersThatCanPower = players
                    .filter { player ->
                        val playerState = powerGrid.playerStates[player]!!

                        // has enough resources to run any power plant?
                        playerState.powerPlants.any { powerPlant ->
                            val available = playerState.storage[powerPlant]!!
                                    .filterKeys { type -> powerPlant.consumes.contains(type) }
                                    .values
                                    .fold(0, { sum, stored -> sum + stored })

                            powerPlant.requires <= available
                        }
                    }
                    .filter { player ->
                        // has any connected cities?
                        powerGrid.cityStates.values.any { cityState -> cityState.connectedBy.contains(player) }
                    }

            var newPlayerStates = powerGrid.playerStates

            // give all players that cannot power anyway already the minimum payment
            (players - playersThatCanPower).forEach { player ->
                val playerState = powerGrid.playerStates[player]!!
                val newPlayerState = playerState.earn(payments[0])

                newPlayerStates += Pair(player, newPlayerState)
            }

            val newPowerGrid = powerGrid.copy(playerStates = newPlayerStates)

            // go into bureaucracy phase with players that could produce
            return when (playersThatCanPower.isEmpty()) {
                true -> nextPhase(finish(newPowerGrid))
                false -> newPowerGrid.copy(
                        phase = BureaucracyPhase(
                                powerGrid = powerGrid,
                                players = playersThatCanPower,
                                nextPhase = nextPhase))
            }
        }

        private fun finish(powerGrid: PowerGrid): PowerGrid {
            var newPowerGrid = addNewResources(powerGrid)

            newPowerGrid = newPowerGrid.copy(
                    powerPlantMarket = newPowerGrid.powerPlantMarket.removeHighestFuture())

            return newPowerGrid
        }

        private fun addNewResources(powerGrid: PowerGrid): PowerGrid {
            var newResourceMarkets = powerGrid.resourceMarkets

            newResources.forEach { entry ->
                val (type, perNumberOfPlayers) = entry

                val perStep = perNumberOfPlayers[powerGrid.players.size - 2]
                val amount = perStep[powerGrid.step - 1]

                val amountToAdd = Math.min(newResourceMarkets[type].capacity - newResourceMarkets[type].available, amount)

                newResourceMarkets += Resource(type, amountToAdd)
            }

            return powerGrid.copy(resourceMarkets = newResourceMarkets)
        }
    }

    val nextPlayer: Player by lazy {
        val nextIndex = (players.indexOf(currentPlayer) + 1) % players.size
        players[nextIndex]
    }

    fun producePower(powerPlants: Set<PowerPlant>, resources: Map<ResourceType, Int>): PowerGrid {
        val playerState = powerGrid.playerStates[currentPlayer]!!

        // player must have the power plants
        playerState.powerPlants.containsAll(powerPlants) || throw IllegalArgumentException("player does not have power plants")

        // player must have the resources
        resources.all { entry ->
            val (type, amount) = entry

            amount <= (playerState.resources[type] ?: 0) || throw IllegalArgumentException("player does not have $amount $type")
        }

        hasEnoughResourcesToRun(powerPlants, resources) || throw IllegalArgumentException("not enough resources")

        val produced = powerPlants.fold(0, { sum, powerPlant -> sum + powerPlant.powers })
        val connected = powerGrid.cityStates.values.fold(0, { sum, cs -> sum + cs.connectedBy.filter { cb -> cb == currentPlayer }.size })

        val powers = Math.min(connected, produced)
        val payment = payments[powers]

        var newPlayerState = playerState.earn(payment)

        resources.forEach { resource ->
            val (type, amount) = resource
            newPlayerState = newPlayerState.removeResource(type, amount)
        }


        val newPowerGrid = powerGrid.copy(
                playerStates = powerGrid.playerStates + Pair(currentPlayer, newPlayerState))

        return when (players.size) {
            1 -> nextPhase(finish(newPowerGrid))
            else -> newPowerGrid.copy(
                    phase = copy(
                            powerGrid = newPowerGrid,
                            players = players - currentPlayer,
                            currentPlayer = nextPlayer))
        }
    }

    private fun hasEnoughResourcesToRun(powerPlants: Set<PowerPlant>, resources: Map<ResourceType, Int>): Boolean {
        // must be enough resources to run the power plants
        // start with all power plants still requiring their defined amount of resources
        val stillRequires = mutableMapOf<PowerPlant, Int>()
        powerPlants.associateTo(stillRequires, { powerPlant -> Pair(powerPlant, powerPlant.requires) })

        // spread resources across power plants to optimize
        resources.forEach { resource ->
            var (type, remaining) = resource

            val powerPlantsThatConsume = powerPlants.filter { powerPlant -> powerPlant.consumes.contains(type) }

            // try to fill the non-hybrid power plants first
            val powerPlantsNonHybridFirst = powerPlantsThatConsume.sortedBy { powerPlant -> powerPlant.consumes.size }

            powerPlantsNonHybridFirst.forEach { powerPlant ->
                val requires = stillRequires[powerPlant]!!

                val amount = Math.min(requires, remaining)
                stillRequires[powerPlant] = requires - amount
                remaining -= amount
            }

            remaining == 0 || throw IllegalArgumentException("too many resources")
        }

        return stillRequires.values.all { requires -> requires == 0 }
    }


}

