package com.wetjens.powergrid.powerplant

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals

object PowerPlantDeckSpec : Spek({

    it("should initialize deck for 2 players") {
        val deck = PowerPlantDeck(Random(0), 2)

        assertEquals(26, deck.remaining)
        assertEquals(13, deck.onTop!!.cost)
    }

    it("should initialize deck for 3 players") {
        val deck = PowerPlantDeck(Random(0), 3)

        assertEquals(26, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    it("should initialize deck for 4 players") {
        val deck = PowerPlantDeck(Random(0), 4)

        assertEquals(30, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    it("should initialize deck for 5 players") {
        val deck = PowerPlantDeck(Random(0), 5)

        assertEquals(34, deck.remaining)
        assertEquals(13, deck.onTop?.cost)
    }

    it("should draw card from deck") {
        var deck = PowerPlantDeck(Random(0), 2)
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

    it("should put card back under deck") {
        var deck = PowerPlantDeck(Random(0), 2)

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
        (1..25).forEach { deck = -deck }
        assertEquals(1, deck.remaining)
        assertEquals(powerPlant, deck.onTop!!)
    }

})