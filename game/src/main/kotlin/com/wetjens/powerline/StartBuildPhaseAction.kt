package com.wetjens.powerline

class StartBuildPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.copy(phase = BuildPhase(buildingPlayers = powerGrid.playerOrder.reversed()))
    }
}