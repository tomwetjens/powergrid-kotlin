package com.wetjens.powerline

class PassConnectCityAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<BuildPhase> { buildPhase ->
            when (buildPhase.buildingPlayers.size) {
                1 -> powerGrid.dispatch(FinishBuildPhaseAction())
                else -> {
                    val newBuildPhase = buildPhase.copy(
                            buildingPlayers = buildPhase.buildingPlayers - buildPhase.currentBuildingPlayer,
                            currentBuildingPlayer = buildPhase.nextBuildingPlayer)

                    powerGrid.copy(phase = newBuildPhase)
                }
            }
        }
    }
}