package com.wetjens.powergrid

import com.wetjens.powergrid.map.City

data class ConnectCityAction(val city: City) : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<BuildPhase> { buildPhase ->
            powerGrid.map.cities.contains(city) || throw IllegalArgumentException("city $city not playable")

            val playerState = powerGrid.playerStates[buildPhase.currentBuildingPlayer]!!
            val cityState = powerGrid.cityStates[city]!!

            cityState.connectedBy.size < powerGrid.step || throw IllegalStateException("reached max connections")

            val cityCost = when (cityState.connectedBy.size) {
                0 -> 10
                1 -> 15
                else -> 20
            }

            val alreadyConnectedCities = powerGrid.cityStates.filterValues { cs -> cs.connectedBy.contains(buildPhase.currentBuildingPlayer) }.keys

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
            val newCityState = cityState.connect(buildPhase.currentBuildingPlayer)

            val newPowerPlantMarket = powerGrid.powerPlantMarket
                    .removeLowerOrEqual(powerGrid.numberOfCitiesConnectedByLeadingPlayer)

            val newPowerGrid = powerGrid.copy(
                    cityStates = powerGrid.cityStates + Pair(city, newCityState),
                    playerStates = powerGrid.playerStates + Pair(buildPhase.currentBuildingPlayer, newPlayerState),
                    powerPlantMarket = newPowerPlantMarket)

            val numberOfConnectedCities = alreadyConnectedCities.size + 1

            return if (numberOfConnectedCities >= powerGrid.gameEndsOnNumberOfCities)
                EndedPhase.start(newPowerGrid) else newPowerGrid
        }
    }
}