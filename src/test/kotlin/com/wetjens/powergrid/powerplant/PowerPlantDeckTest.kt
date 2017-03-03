package com.wetjens.powergrid.powerplant

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals

class PowerPlantDeckTest {

    val random: Random = Random(0)

    @Test
    fun initialDeck2Players() {
        val deck = PowerPlantDeck(random, 2)

        assertEquals(26, deck.remaining)
        assertEquals(13, deck.onTop!!.cost)
    }

    @Test
    fun initialDeck3Players() {
        val deck = PowerPlantDeck(random, 3)

        assertEquals(26, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    @Test
    fun initialDeck4Players() {
        val deck = PowerPlantDeck(random, 4)

        assertEquals(30, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    @Test
    fun initialDeck5Players() {
        val deck = PowerPlantDeck(random, 5)

        assertEquals(34, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    @Test
    fun draw() {
        var deck = PowerPlantDeck(random, 2)
        assertEquals(13, deck.onTop?.cost)

        deck = -deck
        assertEquals(20, deck.onTop?.cost)

        deck = -deck
        assertEquals(22, deck.onTop?.cost)

        deck = -deck
        assertEquals(19, deck.onTop?.cost)

        deck = -deck
        assertEquals(39, deck.onTop?.cost)

        deck = -deck
        assertEquals(12, deck.onTop?.cost)

        // etc.
    }

    @Test
    fun plus() {
        var deck = PowerPlantDeck(random, 2)

        assertEquals(26, deck.remaining)
        assertEquals(13, deck.onTop?.cost)

        val powerPlant = deck.onTop!!

        deck = -deck

        assertEquals(25, deck.remaining)
        assertEquals(20, deck.onTop?.cost)

        deck += powerPlant

        assertEquals(26, deck.remaining)
        assertEquals(20, deck.onTop?.cost)

        // check that it is put under the pile
        (1..25).forEach { deck = -deck}
        assertEquals(1, deck.remaining)
        assertEquals(powerPlant, deck.onTop!!)
    }

}