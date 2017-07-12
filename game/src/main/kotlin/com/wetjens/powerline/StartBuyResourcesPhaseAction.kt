package com.wetjens.powerline

class StartBuyResourcesPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.copy(phase = BuyResourcesPhase(buyingPlayers = powerGrid.playerOrder.reversed()))
    }

}