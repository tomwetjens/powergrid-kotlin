package com.wetjens.powergrid

class FinishAuctionPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            var newPowerGrid = when (powerGrid.round) {
                1 -> powerGrid.redeterminePlayerOrder()
                else -> powerGrid
            }

            if (auctionPhase.closedAuctions.isEmpty()) {
                // if no power plants are sold in phase, then throw out lowest and replace
                newPowerGrid = newPowerGrid.copy(powerPlantMarket = powerGrid.powerPlantMarket.removeLowestAndReplace())
            }

            val goingToStep3 = powerGrid.step == 2 && powerGrid.powerPlantMarket.future.isEmpty()

            if (goingToStep3) {
                newPowerGrid = newPowerGrid.copy(
                        step = 3,
                        powerPlantMarket = newPowerGrid.powerPlantMarket.removeLowestWithoutReplacement())
            }

            return newPowerGrid.dispatch(StartBuyResourcesPhaseAction())
        }
    }

}