package com.wetjens.powergrid.powerplant

import java.util.*

/**
 * Market with power plant offerings.
 */
data class PowerPlantMarket private constructor(
        /**
         * The deck from which replacements are drawn.
         */
        val deck: PowerPlantDeck,

        /**
         * The actual offering.
         */
        val actual: List<PowerPlant>,

        /**
         * The future offering.
         */
        val future: List<PowerPlant>) {

    /**
     * Initializes a default market with a shuffled deck for the given number of players,
     * with the actual offering filled `[3,4,5,6]` and the future offering filled `[7,8,9,10]`.
     */
    constructor(random: Random, numberOfPlayers: Int) : this(PowerPlantDeck(random, numberOfPlayers))

    private constructor(deck: PowerPlantDeck) : this(deck,
            deck.powerPlants.values.take(4),
            deck.powerPlants.values.take(8).takeLast(4))

    /**
     * Takes a power plant from the actual offering and replaces it with one from the deck.
     */
    operator fun minus(powerPlant: PowerPlant): PowerPlantMarket {
        powerPlant in actual || throw IllegalArgumentException("$powerPlant not in actual")

        return removeAndReplace(powerPlant)
    }

    private fun removeAndReplace(powerPlant: PowerPlant): PowerPlantMarket {
        val replacement = deck.onTop
        val newActualAndFuture = (actual + future - powerPlant + replacement)
                .filterNotNull()
                .sortedBy(PowerPlant::cost)

        val newActual = newActualAndFuture.take(Math.min(actual.size, newActualAndFuture.size))
        val newFuture = newActualAndFuture.takeLast(newActualAndFuture.size - newActual.size)

        val newDeck = -deck

        return copy(deck = newDeck, actual = newActual, future = newFuture)
    }

    /**
     * Moves everything in the future offering to the actual offering.
     */
    fun onlyActual(): PowerPlantMarket {
        return copy(actual = actual + future, future = emptyList())
    }

    /**
     * Removes all power plants from the actual offering that have a cost lower than or equal to the given cost,
     * and replaces them with new power plants from the deck (that have a cost higher than the given cost).
     */
    fun removeLowerOrEqual(cost: Int): PowerPlantMarket {
        val lower = actual.firstOrNull { powerPlant -> powerPlant.cost <= cost }
        if (lower != null) {
            return minus(lower).removeLowerOrEqual(cost)
        } else {
            return this
        }
    }

    /**
     * Removes the highest power plant from the future offering, putting it back in the deck under the pile,
     * and replaces it with a new power plant drawn from the deck.
     */
    fun removeHighestFuture(): PowerPlantMarket {
        val highest = future.lastOrNull()
        if (highest != null) {
            val replaced = removeAndReplace(highest)
            return replaced.copy(deck = replaced.deck.plus(highest))
        } else {
            return this
        }
    }

}