package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.junit.Test
import kotlin.test.assertEquals


class PlayerStateTest {

    val coal3P2 = PowerPlant(cost = 8, consumes = setOf(ResourceType.COAL), requires = 3, powers = 2)
    val coal2P1 = PowerPlant(cost = 4, consumes = setOf(ResourceType.COAL), requires = 2, powers = 1)
    val oil1P1 = PowerPlant(cost = 9, consumes = setOf(ResourceType.OIL), requires = 1, powers = 1)
    val oilCoal2P4 = PowerPlant(cost = 21, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 4)
    val oil2P3 = PowerPlant(cost = 16, consumes = setOf(ResourceType.OIL), requires = 2, powers = 3)

    @Test
    fun storageCapacity() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, coal3P2, oil1P1))
        assertEquals(10, playerState.storageCapacity[ResourceType.COAL])
        assertEquals(2, playerState.storageCapacity[ResourceType.OIL])
    }

    @Test
    fun storageCapacityHybrid() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                resources = mapOf(Pair(ResourceType.COAL, 2), Pair(ResourceType.OIL, 1)))
        assertEquals(8, playerState.storageCapacity[ResourceType.COAL])
        assertEquals(6, playerState.storageCapacity[ResourceType.OIL])
    }

    @Test
    fun storageHybridEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                resources = mapOf(Pair(ResourceType.COAL, 4), Pair(ResourceType.OIL, 2)))
        assertEquals(4, playerState.storage[coal2P1]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oil1P1]!![ResourceType.OIL])
        assertEquals(0, playerState.storage[oilCoal2P4]!![ResourceType.COAL] ?: 0)
        assertEquals(0, playerState.storage[oilCoal2P4]!![ResourceType.OIL] ?: 0)
    }

    @Test
    fun storageHybridMixed() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                resources = mapOf(Pair(ResourceType.COAL, 6), Pair(ResourceType.OIL, 4)))
        assertEquals(4, playerState.storage[coal2P1]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oil1P1]!![ResourceType.OIL])
        assertEquals(2, playerState.storage[oilCoal2P4]!![ResourceType.COAL])
        assertEquals(2, playerState.storage[oilCoal2P4]!![ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                resources = mapOf(Pair(ResourceType.COAL, 4), Pair(ResourceType.OIL, 2)))
        assertEquals(4, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(4, playerState.storageAvailable[ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridMixed() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                resources = mapOf(Pair(ResourceType.COAL, 5), Pair(ResourceType.OIL, 3)))
        assertEquals(2, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(2, playerState.storageAvailable[ResourceType.OIL])
    }

    @Test
    fun storageAvailableHybridAllEmpty() {
        val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4))
        assertEquals(8, playerState.storageAvailable[ResourceType.COAL])
        assertEquals(6, playerState.storageAvailable[ResourceType.OIL])
    }

    @Test
    fun optimizePoweredCities() {
        val playerState = PlayerState(
                powerPlants = listOf(coal2P1, oilCoal2P4, oil2P3),
                resources = mapOf(Pair(ResourceType.COAL, 2), Pair(ResourceType.OIL, 2)))

        assertEquals(7, playerState.numberOfCitiesCanSupply)
    }

}