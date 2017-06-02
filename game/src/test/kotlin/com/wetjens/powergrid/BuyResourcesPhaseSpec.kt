package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.resource.ResourceType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

object BuyResourcesPhaseSpec : Spek({

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    val map = BuyResourcesPhaseSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    describe("PowerGrid") {
        var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

        beforeEachTest {

            powerGrid = PowerGrid(random = Random(0), players = players, map = map)
        }

        given("buy resources phase started") {
            beforeEachTest {
                powerGrid = powerGrid
                        .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                        .dispatch(PassBidAction())
                        .dispatch(PassBidAction())
                        .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                        .dispatch(PassBidAction())
                        .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))

                assertEquals(listOf(player1, player2, player3), powerGrid.playerOrder)
                assertTrue(powerGrid.phase is BuyResourcesPhase)
            }

            it("should start in reverse order") {
                assertEquals(listOf(player3, player2, player1), (powerGrid.phase as BuyResourcesPhase).buyingPlayers)
            }

            it("should buy resources for players in order and go to build phase") {
                assertEquals(player3, powerGrid.currentPlayer)
                powerGrid = powerGrid.dispatch(BuyResourcesAction(ResourceType.OIL, 4)).dispatch(PassBuyResourcesAction())

                assertEquals(14, powerGrid.resourceMarkets[ResourceType.OIL].available)
                assertEquals(34, powerGrid.playerStates[player3]!!.balance)

                assertEquals(player2, powerGrid.currentPlayer)
                powerGrid = powerGrid.dispatch(BuyResourcesAction(ResourceType.COAL, 4)).dispatch(PassBuyResourcesAction())

                assertEquals(20, powerGrid.resourceMarkets[ResourceType.COAL].available)
                assertEquals(41, powerGrid.playerStates[player2]!!.balance)

                assertEquals(player1, powerGrid.currentPlayer)
                powerGrid = powerGrid.dispatch(BuyResourcesAction(ResourceType.COAL, 4)).dispatch(PassBuyResourcesAction())

                assertEquals(16, powerGrid.resourceMarkets[ResourceType.COAL].available)
                assertEquals(35, powerGrid.playerStates[player1]!!.balance)

                assertTrue(powerGrid.phase is BuildPhase)
            }

            it("should check if balance is too low") {
                assertEquals(player3, powerGrid.currentPlayer)
                assertEquals(47, powerGrid.playerStates[player3]!!.balance)

                // lose some money
                powerGrid = powerGrid.copy(playerStates = powerGrid.playerStates + Pair(player3, powerGrid.playerStates[player3]!!.pay(27)))
                assertEquals(20, powerGrid.playerStates[player3]!!.balance)

                try {
                    powerGrid.dispatch(BuyResourcesAction(ResourceType.OIL, 6)) // costs 21
                    fail("must throw because balance too low")
                } catch (e: IllegalArgumentException) {
                    // expected
                    assertEquals("balance too low", e.message)
                }
            }

            it("should check if resources are available") {
                assertEquals(2, powerGrid.resourceMarkets[ResourceType.URANIUM].available)

                try {
                    powerGrid.dispatch(BuyResourcesAction(ResourceType.URANIUM, 3))
                    fail("must throw because not enough available")
                } catch (e: IllegalArgumentException) {
                    // expected
                    assertEquals("not enough available", e.message)
                }
            }

            it("should check if storage available") {
                // should be able to store twice the amount it requires
                powerGrid = powerGrid.dispatch(BuyResourcesAction(ResourceType.OIL, 4))

                try {
                    powerGrid.dispatch(BuyResourcesAction(ResourceType.OIL, 1))
                    fail("must throw because max storage exceeded")
                } catch (e: IllegalArgumentException) {
                    // expected
                    assertEquals("max storage exceeded", e.message)
                }
            }
        }
    }
})