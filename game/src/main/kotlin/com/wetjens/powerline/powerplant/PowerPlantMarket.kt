package com.wetjens.powerline.powerplant

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
        val future: List<PowerPlant>,

        private val numberOfDrawsUntilOnlyActual: Int) {

    /**
     * Initializes a default market with a shuffled deck for the given number of players,
     * with the actual offering filled `[3,4,5,6]` and the future offering filled `[7,8,9,10]`.
     */
    constructor(random: Random, numberOfPlayers: Int) : this(PowerPlantDeck(random, numberOfPlayers))

    private constructor(deck: PowerPlantDeck) : this(deck = deck,
            actual = deck.powerPlants.values.take(4),
            future = deck.powerPlants.values.take(8).takeLast(4),
            numberOfDrawsUntilOnlyActual = deck.remaining + 1)

    /**
     * Takes a power plant from the actual offering and replaces it with one from the deck.
     */
    operator fun minus(powerPlant: PowerPlant): PowerPlantMarket {
        powerPlant in actual || throw IllegalArgumentException("$powerPlant not in actual")

        return removeAndReplace(powerPlant)
    }

    private fun removeAndReplace(powerPlant: PowerPlant): PowerPlantMarket {
        val replacement = when (numberOfDrawsUntilOnlyActual) {
            1 -> null
            else -> deck.onTop
        }

        return removeAndReplace(powerPlant, replacement)
    }

    private fun removeAndReplace(powerPlant: PowerPlant, replacement: PowerPlant?): PowerPlantMarket {
        val newActualAndFuture = (actual + future - powerPlant + replacement)
                .filterNotNull()
                .sortedBy(PowerPlant::cost)

        val newActual = newActualAndFuture.take(Math.min(actual.size, newActualAndFuture.size))
        val newFuture = newActualAndFuture.takeLast(newActualAndFuture.size - newActual.size)

        val newMarket = copy(
                actual = newActual,
                future = newFuture,
                numberOfDrawsUntilOnlyActual = Math.max(0, numberOfDrawsUntilOnlyActual - 1))

        return when (numberOfDrawsUntilOnlyActual) {
            1 -> newMarket.onlyActual()
            else -> newMarket.copy(deck = -deck)
        }
    }

    /**
     * Moves everything in the future offering to the actual offering.
     */
    fun onlyActual(): PowerPlantMarket {
        return copy(
                deck = deck.shuffle(),
                actual = actual + future,
                future = emptyList(),
                numberOfDrawsUntilOnlyActual = 0)
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

    /**
     * Removes lowest power plant from actual offering and replaces it with a new power plant drawn from the deck.
     */
    fun removeLowestAndReplace(): PowerPlantMarket {
        return this - actual[0]
    }

    fun removeLowestWithoutReplacement(): PowerPlantMarket {
        return removeAndReplace(actual[0], null)
    }

}