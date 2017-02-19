package com.wetjens.powergrid

import java.util.*

/**
 * Deck from which power plants can be drawn.
 */
data class PowerPlantDeck private constructor(
        /**
         * Set of power plants that the deck is based on.
         */
        val powerPlants: SortedMap<Int, PowerPlant>,

        private val deck: List<PowerPlant>) {

    private object Constants {
        val defaultPowerPlants = listOf(
                PowerPlant(cost = 4, consumes = setOf(ResourceType.COAL), requires = 2, powers = 1),
                PowerPlant(cost = 8, consumes = setOf(ResourceType.COAL), requires = 3, powers = 2),
                PowerPlant(cost = 10, consumes = setOf(ResourceType.COAL), requires = 2, powers = 2),
                PowerPlant(cost = 15, consumes = setOf(ResourceType.COAL), requires = 2, powers = 3),
                PowerPlant(cost = 20, consumes = setOf(ResourceType.COAL), requires = 3, powers = 5),
                PowerPlant(cost = 25, consumes = setOf(ResourceType.COAL), requires = 2, powers = 5),
                PowerPlant(cost = 31, consumes = setOf(ResourceType.COAL), requires = 3, powers = 6),
                PowerPlant(cost = 36, consumes = setOf(ResourceType.COAL), requires = 3, powers = 7),
                PowerPlant(cost = 42, consumes = setOf(ResourceType.COAL), requires = 2, powers = 6),

                PowerPlant(cost = 6, consumes = setOf(ResourceType.BIO_MASS), requires = 1, powers = 1),
                PowerPlant(cost = 14, consumes = setOf(ResourceType.BIO_MASS), requires = 2, powers = 2),
                PowerPlant(cost = 19, consumes = setOf(ResourceType.BIO_MASS), requires = 2, powers = 3),
                PowerPlant(cost = 24, consumes = setOf(ResourceType.BIO_MASS), requires = 2, powers = 4),
                PowerPlant(cost = 30, consumes = setOf(ResourceType.BIO_MASS), requires = 3, powers = 6),
                PowerPlant(cost = 38, consumes = setOf(ResourceType.BIO_MASS), requires = 3, powers = 7),

                PowerPlant(cost = 11, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 2),
                PowerPlant(cost = 17, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 2),
                PowerPlant(cost = 23, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 3),
                PowerPlant(cost = 28, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 4),
                PowerPlant(cost = 34, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 5),
                PowerPlant(cost = 39, consumes = setOf(ResourceType.URANIUM), requires = 1, powers = 6),

                PowerPlant(cost = 3, consumes = setOf(ResourceType.OIL), requires = 2, powers = 1),
                PowerPlant(cost = 7, consumes = setOf(ResourceType.OIL), requires = 3, powers = 2),
                PowerPlant(cost = 9, consumes = setOf(ResourceType.OIL), requires = 1, powers = 1),
                PowerPlant(cost = 16, consumes = setOf(ResourceType.OIL), requires = 2, powers = 3),
                PowerPlant(cost = 26, consumes = setOf(ResourceType.OIL), requires = 2, powers = 5),
                PowerPlant(cost = 32, consumes = setOf(ResourceType.OIL), requires = 3, powers = 6),
                PowerPlant(cost = 35, consumes = setOf(ResourceType.OIL), requires = 1, powers = 5),
                PowerPlant(cost = 40, consumes = setOf(ResourceType.OIL), requires = 2, powers = 6),

                PowerPlant(cost = 5, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 1),
                PowerPlant(cost = 12, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 2),
                PowerPlant(cost = 21, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 4),
                PowerPlant(cost = 29, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 1, powers = 3),
                PowerPlant(cost = 96, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 3, powers = 7),

                PowerPlant(cost = 13, consumes = emptySet(), requires = 0, powers = 1),
                PowerPlant(cost = 18, consumes = emptySet(), requires = 0, powers = 2),
                PowerPlant(cost = 22, consumes = emptySet(), requires = 0, powers = 2),
                PowerPlant(cost = 27, consumes = emptySet(), requires = 0, powers = 3),
                PowerPlant(cost = 33, consumes = emptySet(), requires = 0, powers = 4),
                PowerPlant(cost = 37, consumes = emptySet(), requires = 0, powers = 4),
                PowerPlant(cost = 44, consumes = emptySet(), requires = 0, powers = 5),

                PowerPlant(cost = 50, consumes = emptySet(), requires = 0, powers = 6)
        ).associate { powerPlant -> Pair(powerPlant.cost, powerPlant) }.toSortedMap()
    }

    /**
     * Initializes a shuffled deck for the default set of power plants and the given number of players,
     * with `3-10` removed and `13` on top.
     */
    constructor(random: Random, numberOfPlayers: Int) : this(Constants.defaultPowerPlants,
            listOf(Constants.defaultPowerPlants[13]!!) + Constants.defaultPowerPlants.filterNot { entry ->
                val (cost) = entry
                cost in 3..10 || cost == 13
            }.values.toList()
                    .shuffle(random)
                    .drop(when (numberOfPlayers) {
                        in 2..3 -> 8
                        4 -> 4
                        else -> 0
                    }))

    /**
     * Number of power plants remaining that can be drawn.
     */
    val remaining: Int
        get() = deck.size

    /**
     * Power plant that is on top and can be drawn.
     */
    val onTop: PowerPlant?
        get() = deck.firstOrNull()

    /**
     * Draws the power plant that is on top.
     */
    operator fun unaryMinus(): PowerPlantDeck {
        return copy(deck = deck.drop(1))
    }

}