package com.wetjens.powerline

import com.wetjens.powerline.resource.ResourceType

/**
 * Bureaucracy phase in a game of Power Grid.
 */
data class BureaucracyPhase(val players: List<Player>,
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
    }

    val nextPlayer: Player by lazy {
        val nextIndex = (players.indexOf(currentPlayer) + 1) % players.size
        players[nextIndex]
    }

}

