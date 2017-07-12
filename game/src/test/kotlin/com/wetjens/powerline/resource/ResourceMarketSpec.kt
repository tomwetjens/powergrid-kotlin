package com.wetjens.powerline.resource

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals

object ResourceMarketSpec : Spek({

    it("should calculate cost") {
        val resourceMarket = ResourceMarket.default() + 24

        assertEquals(24, resourceMarket.available)
        assertEquals(24, resourceMarket.capacity)

        assertEquals(1, resourceMarket.calculateCost(1))
        assertEquals(3, resourceMarket.calculateCost(3))
        assertEquals(5, resourceMarket.calculateCost(4))
        assertEquals(9, resourceMarket.calculateCost(6))
        assertEquals(108, resourceMarket.calculateCost(24))
    }

    it("should calculate cost for uranium") {
        val resourceMarket = ResourceMarket.uranium() + 12

        assertEquals(1, resourceMarket.calculateCost(1))
        assertEquals(3, resourceMarket.calculateCost(2))
        assertEquals(10, resourceMarket.calculateCost(4))
        assertEquals(36, resourceMarket.calculateCost(8))
        assertEquals(46, resourceMarket.calculateCost(9))
        assertEquals(88, resourceMarket.calculateCost(12))
    }

    it("should remove amount from market") {
        var resourceMarket = ResourceMarket.default() + 24

        resourceMarket -= 1
        assertEquals(23, resourceMarket.available)
        assertEquals(listOf(2, 3, 3, 3, 3, 3, 3, 3), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 3
        assertEquals(20, resourceMarket.available)
        assertEquals(listOf(0, 2, 3, 3, 3, 3, 3, 3), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 4
        assertEquals(16, resourceMarket.available)
        assertEquals(listOf(0, 0, 1, 3, 3, 3, 3, 3), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 6
        assertEquals(10, resourceMarket.available)
        assertEquals(listOf(0, 0, 0, 0, 1, 3, 3, 3), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 10
        assertEquals(0, resourceMarket.available)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0), resourceMarket.spaces.map(ResourceMarket.Space::available))
    }

    it("should remove amount from market uranium") {
        var resourceMarket = ResourceMarket.uranium() + 12

        resourceMarket -= 1
        assertEquals(11, resourceMarket.available)
        assertEquals(listOf(0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 3
        assertEquals(8, resourceMarket.available)
        assertEquals(listOf(0, 0, 0, 0, 1, 1, 1, 1, 1, 1, 1, 1), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 4
        assertEquals(4, resourceMarket.available)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0, 1, 1, 1, 1), resourceMarket.spaces.map(ResourceMarket.Space::available))

        resourceMarket -= 4
        assertEquals(0, resourceMarket.available)
        assertEquals(listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0), resourceMarket.spaces.map(ResourceMarket.Space::available))
    }

})