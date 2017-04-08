package com.wetjens.powergrid

/**
 * Buy resources phase in a game of Power Grid.
 */
data class BuyResourcesPhase(
        val buyingPlayers: List<Player>,
        val currentBuyingPlayer: Player = buyingPlayers.first()) : Phase {

    override val currentPlayer: Player
        get() = currentBuyingPlayer

    val nextBuyingPlayer: Player by lazy {
        val nextIndex = (buyingPlayers.indexOf(currentBuyingPlayer) + 1) % buyingPlayers.size
        buyingPlayers[nextIndex]
    }
}

