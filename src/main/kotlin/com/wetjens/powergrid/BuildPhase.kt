package com.wetjens.powergrid

import com.wetjens.powergrid.map.NetworkMap

/**
 * Build phase in a game of Power Grid.
 */
data class BuildPhase(val buildingPlayers: List<Player>,
                      val currentBuildingPlayer: Player = buildingPlayers.first(),
                      val map: NetworkMap) : Phase {

    override val currentPlayer: Player
        get() = currentBuildingPlayer

    val nextBuildingPlayer: Player by lazy {
        val nextIndex = (buildingPlayers.indexOf(currentBuildingPlayer) + 1) % buildingPlayers.size
        buildingPlayers[nextIndex]
    }

}

