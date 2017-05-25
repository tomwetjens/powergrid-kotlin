package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object PowerGridSpec : Spek({

    val random: Random = Random(0)

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    val map = PowerGridSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    describe("PowerGrid") {
        val powerGrid = PowerGrid(random, players, map)

        it("should start in step 1") {
            assertEquals(1, powerGrid.step)
        }

        it("should start in round 1") {
            assertEquals(1, powerGrid.round)
        }

        it("should give players initial balance") {
            players.forEach { player -> assertEquals(50, powerGrid.playerStates[player]!!.balance) }
        }

        it("should start with random player order") {
            assertEquals(listOf(player3, player2, player1), powerGrid.playerOrder)
        }

        it("should start with initial power plant market") {
            assertEquals(listOf(3, 4, 5, 6), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(listOf(7, 8, 9, 10), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
        }

        it("should start with initial deck") {
            assertEquals(13, powerGrid.powerPlantMarket.deck.onTop?.cost)
        }

        it("should start in auction phase") {
            assertTrue(powerGrid.phase is AuctionPhase)
        }

        it("should start with first player") {
            assertEquals(player3, powerGrid.currentPlayer)
        }

        it("should start with initial resource markets") {
            assertEquals(24, powerGrid.resourceMarkets[ResourceType.COAL].capacity)
            assertEquals(24, powerGrid.resourceMarkets[ResourceType.COAL].available)

            assertEquals(24, powerGrid.resourceMarkets[ResourceType.OIL].capacity)
            assertEquals(18, powerGrid.resourceMarkets[ResourceType.OIL].available)

            assertEquals(24, powerGrid.resourceMarkets[ResourceType.BIO_MASS].capacity)
            assertEquals(6, powerGrid.resourceMarkets[ResourceType.BIO_MASS].available)

            assertEquals(12, powerGrid.resourceMarkets[ResourceType.URANIUM].capacity)
            assertEquals(2, powerGrid.resourceMarkets[ResourceType.URANIUM].available)
        }
    }
})