package com.wetjens.collections

import com.wetjens.collections.combinations
import org.junit.Test

import org.junit.Assert.*

class CombinationsTest {

    @Test
    fun combinations() {
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

    @Test
    fun combinations_Max() {
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

    @Test
    fun combinations_Empty() {
        assertEquals(emptySet<Int>(), emptySet<Int>().combinations(1, 3))
    }

}