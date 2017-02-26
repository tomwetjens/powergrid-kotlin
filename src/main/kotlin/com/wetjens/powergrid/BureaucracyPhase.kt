package com.wetjens.powergrid

/**
 * Bureaucracy phase in a game of Power Grid.
 */
data class BureaucracyPhase(private val powerGrid: PowerGrid,
                            private val nextPhase: (PowerGrid) -> PowerGrid,
                            val players: List<Player>,
                            override val currentPlayer: Player = players.first()) : Phase {

    val nextPlayer: Player by lazy {
        val nextIndex = (players.indexOf(currentPlayer) + 1) % players.size
        players[nextIndex]
    }


}

