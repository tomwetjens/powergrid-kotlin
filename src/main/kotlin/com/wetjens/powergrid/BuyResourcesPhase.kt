package com.wetjens.powergrid

import com.wetjens.powergrid.resource.Resource
import com.wetjens.powergrid.resource.ResourceType

/**
 * Buy resources phase in a game of Power Grid.
 */
data class BuyResourcesPhase(
        private val nextPhase: (PowerGrid) -> PowerGrid,
        val buyingPlayers: List<Player>,
        val currentBuyingPlayer: Player = buyingPlayers.first()) : Phase {

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
            1 -> nextPhase(powerGrid)
            else -> powerGrid.copy(phase = copy(buyingPlayers = buyingPlayers - currentBuyingPlayer, currentBuyingPlayer = nextBuyingPlayer))
        }
    }
}

