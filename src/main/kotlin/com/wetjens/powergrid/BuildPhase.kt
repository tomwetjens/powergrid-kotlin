package com.wetjens.powergrid

import com.wetjens.powergrid.map.City

/**
 * Build phase in a game of Power Grid.
 */
data class BuildPhase(private val powerGrid: PowerGrid,
                      private val nextPhase: (PowerGrid) -> PowerGrid,
                      val buildingPlayers: List<Player>,
                      val currentBuildingPlayer: Player = buildingPlayers.first()) : Phase {

    override val currentPlayer: Player
        get() = currentBuildingPlayer

    val nextBuildingPlayer: Player by lazy {
        val nextIndex = (buildingPlayers.indexOf(currentBuildingPlayer) + 1) % buildingPlayers.size
        buildingPlayers[nextIndex]
    }

    fun connectCity(city: City): PowerGrid {
        powerGrid.map.cities.contains(city) || throw IllegalArgumentException("city not playable")

        val playerState = powerGrid.playerStates[currentBuildingPlayer]!!
        val cityState = powerGrid.cityStates[city]!!

        cityState.connectedBy.size < powerGrid.step || throw IllegalStateException("reached max connections")

        val cityCost = when (cityState.connectedBy.size) {
            0 -> 10
            1 -> 15
            else -> 20
        }

        val alreadyConnectedCities = powerGrid.cityStates.filterValues { cs -> cs.connectedBy.contains(currentBuildingPlayer) }.keys

        val connectionCost = when (alreadyConnectedCities.isEmpty()) {
            true -> 0
            else -> {
                // find the cheapest path from any of the already connected cities
                val cheapestPath = alreadyConnectedCities
                        .map { source -> powerGrid.map.shortestPath(source, city) }
                        .reduce({ cheapest, path -> if (path.cost < cheapest.cost) path else cheapest })

                cheapestPath.cost
            }
        }

        val cost = cityCost + connectionCost
        cost <= playerState.balance || throw IllegalStateException("balance too low")

        val newPlayerState = playerState.pay(cost)
        val newCityState = cityState.connect(currentBuildingPlayer)

        val newPowerPlantMarket = powerGrid.powerPlantMarket.removeLowerOrEqual(powerGrid.leadingPlayerNumberOfCitiesConnected)

        val newPowerGrid = powerGrid.copy(
                cityStates = powerGrid.cityStates + Pair(city, newCityState),
                playerStates = powerGrid.playerStates + Pair(currentBuildingPlayer, newPlayerState),
                powerPlantMarket = newPowerPlantMarket)

        return newPowerGrid.copy(phase = copy(powerGrid = newPowerGrid))
    }

    fun passConnectCity(): PowerGrid {
        return when (buildingPlayers.size) {
            1 -> finish()
            else -> {
                val newBuildPhase = copy(
                        buildingPlayers = buildingPlayers - currentBuildingPlayer,
                        currentBuildingPlayer = nextBuildingPlayer)

                powerGrid.copy(phase = newBuildPhase)
            }
        }
    }

    private fun finish(): PowerGrid {
        return nextPhase(when (powerGrid.step == 1 && powerGrid.leadingPlayerNumberOfCitiesConnected >= 7) {
            true -> {
                val newPowerPlantMarket = (powerGrid.powerPlantMarket - powerGrid.powerPlantMarket.actual[0])
                        .removeLowerOrEqual(powerGrid.leadingPlayerNumberOfCitiesConnected)

                powerGrid.copy(step = 2, powerPlantMarket = newPowerPlantMarket)
            }
            false -> powerGrid
        })
    }

}

