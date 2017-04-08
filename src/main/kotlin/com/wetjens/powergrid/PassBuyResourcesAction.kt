package com.wetjens.powergrid

class PassBuyResourcesAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<BuyResourcesPhase> { buyResourcesPhase ->
            return when (buyResourcesPhase.buyingPlayers.size) {
                1 -> powerGrid.dispatch(StartBuildPhaseAction())
                else -> powerGrid.copy(phase = buyResourcesPhase.copy(
                        buyingPlayers = buyResourcesPhase.buyingPlayers - buyResourcesPhase.currentBuyingPlayer,
                        currentBuyingPlayer = buyResourcesPhase.nextBuyingPlayer))
            }
        }
    }

}