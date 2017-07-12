package com.wetjens.powerline

class FinishBuildPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        val goingToStep2 = powerGrid.step == 1 && powerGrid.numberOfCitiesConnectedByLeadingPlayer >= powerGrid.step2StartsOnNumberOfCities

        return (if (goingToStep2) {
            val newPowerPlantMarket = powerGrid.powerPlantMarket
                    .removeLowestAndReplace()
                    .removeLowerOrEqual(powerGrid.numberOfCitiesConnectedByLeadingPlayer)

            powerGrid.copy(step = 2, powerPlantMarket = newPowerPlantMarket)
        } else {
            val goingToStep3 = powerGrid.step == 2 && powerGrid.powerPlantMarket.future.isEmpty()

            if (goingToStep3) {
                val newPowerPlantMarket = powerGrid.powerPlantMarket
                        .removeLowestWithoutReplacement()

                powerGrid.copy(step = 3, powerPlantMarket = newPowerPlantMarket)
            } else {
                powerGrid
            }
        }).dispatch(StartBureaucracyPhaseAction())
    }
}