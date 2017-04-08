package com.wetjens.powergrid

import com.wetjens.powergrid.resource.Resource

class FinishBureaucracyPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        var newPowerGrid = addNewResources(powerGrid)

        var newPowerPlantMarket = when (newPowerGrid.step) {
            3 -> newPowerGrid.powerPlantMarket.removeLowestAndReplace()
            else -> newPowerGrid.powerPlantMarket.removeHighestFuture()
        }

        val goingToStep3 = powerGrid.step == 2 && newPowerPlantMarket.future.isEmpty()

        if (goingToStep3) {
            newPowerPlantMarket = newPowerPlantMarket.removeLowestWithoutReplacement()
        }

        newPowerGrid = newPowerGrid.copy(powerPlantMarket = newPowerPlantMarket)

        return newPowerGrid.dispatch(StartAuctionPhaseAction())
    }

    private fun addNewResources(powerGrid: PowerGrid): PowerGrid {
        var newResourceMarkets = powerGrid.resourceMarkets

        BureaucracyPhase.newResources.forEach { entry ->
            val (type, perNumberOfPlayers) = entry

            val perStep = perNumberOfPlayers[powerGrid.players.size - 2]
            val amount = perStep[powerGrid.step - 1]

            val amountToAdd = Math.min(newResourceMarkets[type].capacity - newResourceMarkets[type].available, amount)

            newResourceMarkets += Resource(type, amountToAdd)
        }

        return powerGrid.copy(resourceMarkets = newResourceMarkets)
    }

}