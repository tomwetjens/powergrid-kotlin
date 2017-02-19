package com.wetjens.powergrid

import java.util.*

data class PlayerState(val balance: Int = 50,
                       val powerPlants: List<PowerPlant> = emptyList(),
                       val numberOfHouses: Int = 0): Comparable<PlayerState> {

    fun pay(amount: Int): PlayerState {
        amount <= balance || throw IllegalArgumentException("balance too low")

        return copy(balance = balance - amount)
    }

    fun addPowerPlant(powerPlant: PowerPlant, replaces: PowerPlant?): PlayerState {
        val newPowerPlants = when (replaces) {
            null -> powerPlants + powerPlant
            else -> (powerPlants - replaces) + powerPlant
        }.sortedBy(PowerPlant::cost)

        return copy(powerPlants = newPowerPlants)
    }

    val highestPowerPlant: PowerPlant? = powerPlants.lastOrNull()

    override
    operator fun compareTo(other: PlayerState): Int {
        return Comparator.comparing(PlayerState::numberOfHouses)
                .thenComparing({ playerState -> playerState.highestPowerPlant?.cost ?: throw IllegalStateException("no power plant")})
                .compare(this, other)
    }

}