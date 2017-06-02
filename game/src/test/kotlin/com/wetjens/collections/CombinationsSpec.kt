package com.wetjens.collections

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals

object CombinationsSpec : Spek({

    it("should give unique set of combinations for a given set of numbers") {
        val set = setOf(1, 2, 3, 4)

        assertEquals(setOf(
                setOf(1),
                setOf(1, 2),
                setOf(1, 2, 3),
                setOf(1, 2, 3, 4),
                setOf(1, 2, 4),
                setOf(1, 3),
                setOf(1, 3, 4),
                setOf(1, 4),
                setOf(2),
                setOf(2, 1),
                setOf(2, 1, 4),
                setOf(2, 3),
                setOf(2, 3, 4),
                setOf(2, 4),
                setOf(3),
                setOf(3, 4),
                setOf(4)
        ), set.combinations(1, 4))
    }

    it("should give unique set of combinations between min and max size for a given set of numbers") {
        val set = setOf(1, 2, 3, 4)

        assertEquals(setOf(
                setOf(1),
                setOf(1, 2),
                setOf(1, 2, 3),
                setOf(1, 2, 4),
                setOf(1, 3),
                setOf(1, 3, 4),
                setOf(1, 4),
                setOf(2),
                setOf(2, 1),
                setOf(2, 1, 4),
                setOf(2, 3),
                setOf(2, 3, 4),
                setOf(2, 4),
                setOf(3),
                setOf(3, 4),
                setOf(4)
        ), set.combinations(1, 3))
    }

    it("should give empty set of combinations for a given empty set") {
        assertEquals(emptySet<Set<Int>>(), emptySet<Int>().combinations(1, 3))
    }

})