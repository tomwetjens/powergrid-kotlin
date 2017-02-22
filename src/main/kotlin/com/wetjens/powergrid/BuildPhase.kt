package com.wetjens.powergrid

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

