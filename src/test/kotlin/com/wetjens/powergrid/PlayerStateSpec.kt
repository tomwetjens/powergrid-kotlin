package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals


object PlayerStateSpec : Spek({

    val coal3P2 = PowerPlant(cost = 8, consumes = setOf(ResourceType.COAL), requires = 3, powers = 2)
    val coal2P1 = PowerPlant(cost = 4, consumes = setOf(ResourceType.COAL), requires = 2, powers = 1)
    val oil1P1 = PowerPlant(cost = 9, consumes = setOf(ResourceType.OIL), requires = 1, powers = 1)
    val oilCoal2P4 = PowerPlant(cost = 21, consumes = setOf(ResourceType.COAL, ResourceType.OIL), requires = 2, powers = 4)
    val oil2P3 = PowerPlant(cost = 16, consumes = setOf(ResourceType.OIL), requires = 2, powers = 3)

    describe("PlayerState") {
        given("player has power plants") {
            val playerState = PlayerState(powerPlants = listOf(coal2P1, coal3P2, oil1P1))

            it("should correctly calculate storage capacity") {
                assertEquals(10, playerState.storageCapacity[ResourceType.COAL])
                assertEquals(2, playerState.storageCapacity[ResourceType.OIL])
            }
        }

        given("player has hybrid power plant") {
            val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4))

            it("should correctly calculate storage capacity") {
                assertEquals(8, playerState.storageCapacity[ResourceType.COAL])
                assertEquals(6, playerState.storageCapacity[ResourceType.OIL])
            }

            it("should calculate available storage correctly") {
                assertEquals(8, playerState.storageAvailable[ResourceType.COAL])
                assertEquals(6, playerState.storageAvailable[ResourceType.OIL])
            }
        }

        given("player has hybrid power plant and resources") {
            val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                    resources = mapOf(Pair(ResourceType.COAL, 4), Pair(ResourceType.OIL, 2)))

            it("should store resources on non-hybrid first") {
                assertEquals(4, playerState.storage[coal2P1]!![ResourceType.COAL])
                assertEquals(2, playerState.storage[oil1P1]!![ResourceType.OIL])
                assertEquals(0, playerState.storage[oilCoal2P4]!![ResourceType.COAL] ?: 0)
                assertEquals(0, playerState.storage[oilCoal2P4]!![ResourceType.OIL] ?: 0)
            }

            it("should calculate available storage correctly") {
                assertEquals(4, playerState.storageAvailable[ResourceType.COAL])
                assertEquals(4, playerState.storageAvailable[ResourceType.OIL])
            }
        }

        given("player has hybrid power plant and resources stored on it") {
            val playerState = PlayerState(powerPlants = listOf(coal2P1, oil1P1, oilCoal2P4),
                    resources = mapOf(Pair(ResourceType.COAL, 6), Pair(ResourceType.OIL, 4)))

            it("should store mixed resources on hybrid power plant") {
                assertEquals(4, playerState.storage[coal2P1]!![ResourceType.COAL])
                assertEquals(2, playerState.storage[oil1P1]!![ResourceType.OIL])
                assertEquals(2, playerState.storage[oilCoal2P4]!![ResourceType.COAL])
                assertEquals(2, playerState.storage[oilCoal2P4]!![ResourceType.OIL])
            }

            it("should calculate available storage correctly") {
                assertEquals(0, playerState.storageAvailable[ResourceType.COAL])
                assertEquals(0, playerState.storageAvailable[ResourceType.OIL])
            }
        }

        given("player has power plants and enough resources") {
            val playerState = PlayerState(
                    powerPlants = listOf(coal2P1, oilCoal2P4, oil2P3),
                    resources = mapOf(Pair(ResourceType.COAL, 2), Pair(ResourceType.OIL, 2)))

            it("should calculate number of cities that player can supply correctly") {
                assertEquals(7, playerState.numberOfCitiesCanSupply)
            }
        }
    }
})