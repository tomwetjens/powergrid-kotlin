package com.wetjens.powergrid

import com.wetjens.powergrid.map.City
import com.wetjens.powergrid.map.Connection

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
                val possibleFromCities = alreadyConnectedCities.filter { c -> c.hasConnectionTo(city) }
                possibleFromCities.isNotEmpty() || throw IllegalArgumentException("not reachable")

                // pick the cheapest connection
                val connection = possibleFromCities
                        .flatMap(City::connections)
                        .sortedBy(Connection::cost)
                        .first()

                connection.cost
            }
        }

        val cost = cityCost + connectionCost
        cost <= playerState.balance || throw IllegalStateException("balance too low")

        val newPlayerState = playerState.pay(cost)
        val newCityState = cityState.connect(currentBuildingPlayer)

        val newPowerGrid = powerGrid.copy(
                cityStates = powerGrid.cityStates + Pair(city, newCityState),
                playerStates = powerGrid.playerStates + Pair(currentBuildingPlayer, newPlayerState))

        return newPowerGrid.copy(phase = copy(powerGrid = newPowerGrid))
    }

    fun passConnectCity(): PowerGrid {
        return when (buildingPlayers.size) {
            1 -> nextPhase(powerGrid)
            else -> {
                val newBuildPhase = copy(
                        buildingPlayers = buildingPlayers - currentBuildingPlayer,
                        currentBuildingPlayer = nextBuildingPlayer)

                powerGrid.copy(phase = newBuildPhase)
            }
        }
    }

}

