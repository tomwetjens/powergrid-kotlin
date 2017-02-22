package com.wetjens.powergrid

import org.junit.Test
import kotlin.test.assertEquals


class PlayerStateTest {

    val coal4 = PowerPlant(cost = 1, consumes = setOf(ResourceType.COAL), requires = 4, powers = 1)
    val coal2 = PowerPlant(cost = 1, consumes = setOf(ResourceType.COAL), requires = 2, powers = 1)
    val oil1 = PowerPlant(cost = 1, consumes = setOf(ResourceType.OIL), requires = 1, powers = 1)
    val oilCoal2 = PowerPlant(cost = 1, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 1)

    @Test
    fun storageCapacity() {
        val playerState = PlayerState(powerPlants = listOf(coal2, coal4, oil1))
        assertEquals(12, playerState.storageCapacity[ResourceType.COAL])
        assertEquals(2, playerState.storageCapacity[ResourceType.OIL])
    }

    @Test
    fun storageCapacityHybrid() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2),
                resources = mapOf(Pair(ResourceType.COAL, 2), Pair(ResourceType.OIL, 1)))
        assertEquals(8, playerState.storageCapacity[ResourceType.COAL])
        assertEquals(6, playerState.storageCapacity[ResourceType.OIL])
    }

    @Test
    fun storageHybridEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2),
                resources = mapOf(Pair(ResourceType.COAL, 4), Pair(ResourceType.OIL, 2)))
        assertEquals(4, playerState.storage[coal2]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oil1]!![ResourceType.OIL])
        assertEquals(0, playerState.storage[oilCoal2]!![ResourceType.COAL] ?: 0)
        assertEquals(0, playerState.storage[oilCoal2]!![ResourceType.OIL] ?: 0)
    }

    @Test
    fun storageHybridMixed() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2),
                resources = mapOf(Pair(ResourceType.COAL, 6), Pair(ResourceType.OIL, 4)))
        assertEquals(4, playerState.storage[coal2]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oil1]!![ResourceType.OIL])
        assertEquals(2, playerState.storage[oilCoal2]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oilCoal2]!![ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2),
                resources = mapOf(Pair(ResourceType.COAL, 4), Pair(ResourceType.OIL, 2)))
        assertEquals(4, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(4, playerState.storageAvailable[ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridMixed() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2),
                resources = mapOf(Pair(ResourceType.COAL, 5), Pair(ResourceType.OIL, 3)))
        assertEquals(2, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(2, playerState.storageAvailable[ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridAllEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2, oil1, oilCoal2))
        assertEquals(8, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(6, playerState.storageAvailable[ResourceType.OIL])
    }

}