package com.wetjens.powergrid

import com.wetjens.powergrid.map.City

/**
 * Build phase in a game of Power Grid.
 */
data class BuildPhase(
        val buildingPlayers: List<Player>,
        val currentBuildingPlayer: Player = buildingPlayers.first()) : Phase {

    companion object Factory {

        fun start(powerGrid: PowerGrid): PowerGrid {
            return powerGrid.copy(phase = BuildPhase(buildingPlayers = powerGrid.playerOrder.reversed()))
        }
    }

    override val currentPlayer: Player
        get() = currentBuildingPlayer

    val nextBuildingPlayer: Player by lazy {
        val nextIndex = (buildingPlayers.indexOf(currentBuildingPlayer) + 1) % buildingPlayers.size
        buildingPlayers[nextIndex]
    }

    fun connectCity(powerGrid: PowerGrid, city: City): PowerGrid {
        powerGrid.map.cities.contains(city) || throw IllegalArgumentException("city $city not playable")

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

        val newPowerPlantMarket = powerGrid.powerPlantMarket.removeLowerOrEqual(powerGrid.numberOfCitiesConnectedByLeadingPlayer)

        return powerGrid.copy(
                cityStates = powerGrid.cityStates + Pair(city, newCityState),
                playerStates = powerGrid.playerStates + Pair(currentBuildingPlayer, newPlayerState),
                powerPlantMarket = newPowerPlantMarket)
    }

    fun passConnectCity(powerGrid: PowerGrid): PowerGrid {
        return when (buildingPlayers.size) {
            1 -> finish(powerGrid)
            else -> {
                val newBuildPhase = copy(
                        buildingPlayers = buildingPlayers - currentBuildingPlayer,
                        currentBuildingPlayer = nextBuildingPlayer)

                powerGrid.copy(phase = newBuildPhase)
            }
        }
    }

    private fun finish(powerGrid: PowerGrid): PowerGrid {
        return BureaucracyPhase.start(when (powerGrid.step == 1
                && powerGrid.numberOfCitiesConnectedByLeadingPlayer >= powerGrid.step2StartsOnNumberOfCities) {
            true -> {
                val newPowerPlantMarket = (powerGrid.powerPlantMarket - powerGrid.powerPlantMarket.actual[0])
                        .removeLowerOrEqual(powerGrid.numberOfCitiesConnectedByLeadingPlayer)

                powerGrid.copy(step = 2, powerPlantMarket = newPowerPlantMarket)
            }
            false -> powerGrid
        })
    }

}

