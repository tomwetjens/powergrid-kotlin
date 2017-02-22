package com.wetjens.powergrid

import com.wetjens.powergrid.resource.Resource
import com.wetjens.powergrid.resource.ResourceMarkets
import com.wetjens.powergrid.resource.ResourceType

/**
 * Buy resources phase in a game of Power Grid.
 */
data class BuyResourcesPhase(val buyingPlayers: List<Player>,
                             val currentBuyingPlayer: Player = buyingPlayers.first(),
                             val resourceMarkets: ResourceMarkets) : Phase {

    override val currentPlayer: Player
        get() = currentBuyingPlayer

    val nextBuyingPlayer: Player by lazy {
        val nextIndex = (buyingPlayers.indexOf(currentBuyingPlayer) + 1) % buyingPlayers.size
        buyingPlayers[nextIndex]
    }

    val completed: Boolean = buyingPlayers.isEmpty()

    fun buy(resourceType: ResourceType, amount: Int): BuyResourcesPhase {
        return copy(resourceMarkets = resourceMarkets - Resource(resourceType, amount))
    }

    fun pass(): BuyResourcesPhase {
        return copy(buyingPlayers - currentBuyingPlayer, currentBuyingPlayer = nextBuyingPlayer)
    }
}

