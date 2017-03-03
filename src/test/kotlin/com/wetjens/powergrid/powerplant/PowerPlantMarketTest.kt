package com.wetjens.powergrid.powerplant

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class PowerPlantMarketTest {

    val random: Random = Random(0)

    @Test
    fun initialMarket() {
        val market = PowerPlantMarket(random, 2)

        assertEquals(listOf(3, 4, 5, 6), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), market.future.map(PowerPlant::cost))
    }

    @Test
    fun take() {
        var market = PowerPlantMarket(random, 2)

        market -= market.actual[1]
        assertEquals(listOf(3, 5, 6, 7), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(8, 9, 10, 13), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 6, 7, 8), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(9, 10, 13, 20), market.future.map(PowerPlant::cost))

        // empty out deck
        (1..24).forEach { market -= market.actual[1] }
        assertEquals(0, market.deck.remaining)

        assertEquals(listOf(3, 11, 39, 40), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(42, 44, 50, 96), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 39, 40, 42), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(44, 50, 96), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 40, 42, 44), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(50, 96), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 42, 44, 50), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(96), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 44, 50, 96), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 50, 96), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3, 96), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        market -= market.actual[1]
        assertEquals(listOf(3), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        market -= market.actual[0]
        assertEquals(emptyList(), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))
    }

    @Test
    fun onlyActual() {
        var market = PowerPlantMarket(random, 2)

        assertEquals(listOf(3, 4, 5, 6), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), market.future.map(PowerPlant::cost))

        market = market.onlyActual()

        assertEquals(listOf(3, 4, 5, 6, 7, 8, 9, 10), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        market -= market.actual[0]
        assertEquals(listOf(4, 5, 6, 7, 8, 9, 10, 13), market.actual.map(PowerPlant::cost))
        assertEquals(emptyList(), market.future.map(PowerPlant::cost))

        // etc.
    }

    @Test
    fun removeLowerOrEqual() {
        var market = PowerPlantMarket(random, 2)

        assertEquals(listOf(3, 4, 5, 6), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), market.future.map(PowerPlant::cost))

        market = market.removeLowerOrEqual(6)

        assertEquals(listOf(7, 8, 9, 10), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(13, 19, 20, 22), market.future.map(PowerPlant::cost))

        market = market.removeLowerOrEqual(15)

        assertEquals(listOf(19, 20, 22, 23), market.actual.map(PowerPlant::cost))
        assertEquals(listOf(37, 39, 44, 96), market.future.map(PowerPlant::cost))
    }

}