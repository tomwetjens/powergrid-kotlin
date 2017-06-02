package com.wetjens.powergrid

import com.wetjens.powergrid.resource.Resource
import com.wetjens.powergrid.resource.ResourceType

data class BuyResourcesAction(val resourceType: ResourceType,
                         val amount: Int) : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<BuyResourcesPhase> { buyResourcesPhase ->
            val cost = powerGrid.resourceMarkets[resourceType].calculateCost(amount)

            val playerState = powerGrid.playerStates[buyResourcesPhase.currentBuyingPlayer]!!
            playerState.balance >= cost || throw IllegalArgumentException("balance too low")

            val newPlayerState = playerState
                    .pay(cost)
                    .addResource(resourceType, amount)

            return powerGrid.copy(
                    resourceMarkets = powerGrid.resourceMarkets - Resource(resourceType, amount),
                    playerStates = powerGrid.playerStates + Pair(buyResourcesPhase.currentBuyingPlayer, newPlayerState))
        }
    }

}