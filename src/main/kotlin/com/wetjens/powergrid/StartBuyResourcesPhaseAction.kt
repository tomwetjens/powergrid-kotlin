package com.wetjens.powergrid

class StartBuyResourcesPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.copy(phase = BuyResourcesPhase(buyingPlayers = powerGrid.playerOrder.reversed()))
    }

}