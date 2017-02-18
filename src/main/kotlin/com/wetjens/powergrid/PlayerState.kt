package com.wetjens.powergrid

class PlayerState(val balance: Int = 50,
                  val powerPlants: List<PowerPlant> = emptyList()) {

    fun pay(amount: Int): PlayerState {
        amount <= balance || throw IllegalArgumentException("balance too low")

        return PlayerState(balance - amount, powerPlants)
    }

    fun addPowerPlant(powerPlant: PowerPlant, replaces: PowerPlant?): PlayerState {
        val newPowerPlants = when (replaces) {
            null -> powerPlants + powerPlant
            else -> (powerPlants - replaces) + powerPlant
        }.sortedBy(PowerPlant::cost)

        return PlayerState(balance, newPowerPlants)
    }

}