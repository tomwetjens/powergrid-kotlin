package com.wetjens.powergrid

import com.wetjens.powergrid.resource.Resource
import com.wetjens.powergrid.resource.ResourceType

/**
 * Buy resources phase in a game of Power Grid.
 */
data class BuyResourcesPhase(
        val buyingPlayers: List<Player>,
        val currentBuyingPlayer: Player = buyingPlayers.first()) : Phase {

    companion object Factory {

        fun start(powerGrid: PowerGrid): PowerGrid {
            return powerGrid.copy(phase = BuyResourcesPhase(buyingPlayers = powerGrid.playerOrder.reversed()))
        }
    }

    override val currentPlayer: Player
        get() = currentBuyingPlayer

    val nextBuyingPlayer: Player by lazy {
        val nextIndex = (buyingPlayers.indexOf(currentBuyingPlayer) + 1) % buyingPlayers.size
        buyingPlayers[nextIndex]
    }

    fun buy(powerGrid: PowerGrid, resourceType: ResourceType, amount: Int): PowerGrid {
        val cost = powerGrid.resourceMarkets[resourceType].calculateCost(amount)

        val playerState = powerGrid.playerStates[currentBuyingPlayer]!!
        playerState.balance >= cost || throw IllegalArgumentException("balance too low")

        val newPlayerState = playerState
                .pay(cost)
                .addResource(resourceType, amount)

        return powerGrid.copy(
                resourceMarkets = powerGrid.resourceMarkets - Resource(resourceType, amount),
                playerStates = powerGrid.playerStates + Pair(currentBuyingPlayer, newPlayerState))
    }

    fun pass(powerGrid: PowerGrid): PowerGrid {
        return when (buyingPlayers.size) {
            1 -> BuildPhase.start(powerGrid)
            else -> powerGrid.copy(phase = copy(buyingPlayers = buyingPlayers - currentBuyingPlayer, currentBuyingPlayer = nextBuyingPlayer))
        }
    }
}

