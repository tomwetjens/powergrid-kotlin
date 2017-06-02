package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant

interface AuctionAction : Action {

    fun checkBid(powerGrid: PowerGrid, player: Player, bid: Int, replaces: PowerPlant?) {
        val playerState = powerGrid.playerStates[player]!!

        playerState.powerPlants.size < powerGrid.maxOwnedPowerPlants || replaces != null || throw IllegalArgumentException("must replace a power plant")

        playerState.balance >= bid || throw IllegalArgumentException("balance too low")
    }

    fun completePowerPlantPurchase(powerGrid: PowerGrid, player: Player, powerPlant: PowerPlant, bid: Int, replaces: PowerPlant?): Map<Player, PlayerState> {
        val playerState = powerGrid.playerStates[player]
        val newPlayerState = playerState!!.pay(bid).addPowerPlant(powerPlant, replaces)

        return powerGrid.playerStates + Pair(player, newPlayerState)
    }

}