package com.wetjens.powerline

import com.wetjens.powerline.map.yaml.YamlNetworkMap
import com.wetjens.powerline.powerplant.PowerPlant
import com.wetjens.powerline.resource.ResourceType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

object BuildPhaseSpec : Spek({

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")
    val player4 = Player(name = "Player 4")
    val player5 = Player(name = "Player 5")
    val player6 = Player(name = "Player 6")

    val players = listOf(player1, player2, player3)

    val map = BuildPhaseSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val duesseldorf = map.cities.find { city -> city.name == "Düsseldorf" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!
    val muenster = map.cities.find { city -> city.name == "Münster" }!!

    describe("PowerGrid") {
        it("should connect starting city") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player3, powerGrid.currentPlayer)

            powerGrid = powerGrid.dispatch(ConnectCityAction((duesseldorf)))
                    .dispatch(PassConnectCityAction())

            assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
            assertEquals(37, powerGrid.playerStates[player3]!!.balance)

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player2, powerGrid.currentPlayer)

            powerGrid = powerGrid.dispatch(ConnectCityAction((essen)))
                    .dispatch(PassConnectCityAction())

            assertEquals(setOf(player2), powerGrid.cityStates[essen]!!.connectedBy)
            assertEquals(36, powerGrid.playerStates[player2]!!.balance)

            powerGrid = powerGrid.dispatch(ConnectCityAction((muenster)))

            assertEquals(setOf(player1), powerGrid.cityStates[muenster]!!.connectedBy)
            assertEquals(35, powerGrid.playerStates[player1]!!.balance)
        }

        it("should not connect city connected in same step") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player3, powerGrid.currentPlayer)

            powerGrid = powerGrid.dispatch(ConnectCityAction((duesseldorf)))
                    .dispatch(PassConnectCityAction())

            assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
            assertEquals(37, powerGrid.playerStates[player3]!!.balance)

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player2, powerGrid.currentPlayer)

            try {
                powerGrid.dispatch(ConnectCityAction((duesseldorf)))
                fail("should throw because city reached max connections")
            } catch (e: IllegalStateException) {
                assertEquals("reached max connections", e.message)
            }
        }

        it("should only connect city that is connected to existing network") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player3, powerGrid.currentPlayer)
            assertEquals(47, powerGrid.playerStates[player3]!!.balance)

            powerGrid = powerGrid
                    .dispatch(ConnectCityAction((duesseldorf)))
                    .dispatch(ConnectCityAction((muenster)))

            assertEquals(19, powerGrid.playerStates[player3]!!.balance)
        }

        it("should connect multiple cities") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player3, powerGrid.currentPlayer)
            assertEquals(47, powerGrid.playerStates[player3]!!.balance)

            powerGrid = powerGrid.dispatch(ConnectCityAction((duesseldorf)))
                    .dispatch(ConnectCityAction((essen)))
                    .dispatch(ConnectCityAction((muenster)))
                    .dispatch(PassConnectCityAction())

            assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
            assertEquals(setOf(player3), powerGrid.cityStates[essen]!!.connectedBy)
            assertEquals(setOf(player3), powerGrid.cityStates[muenster]!!.connectedBy)
            assertEquals(9, powerGrid.playerStates[player3]!!.balance)

            assertTrue(powerGrid.phase is BuildPhase)
            assertEquals(player2, powerGrid.currentPlayer)
        }

        it("should go to step 2 when player connects 7th city") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map).copy(playerStates = mapOf(
                    Pair(player1, PlayerState(balance = 9999)),
                    Pair(player2, PlayerState(balance = 9999)),
                    Pair(player3, PlayerState(balance = 9999))
            ))

            val cities = map.cities.toList()

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))
                    .dispatch(BuyResourcesAction(ResourceType.OIL, 2))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            // player connects 7 cities
            (0..6).forEach { i -> powerGrid = powerGrid.dispatch(ConnectCityAction((cities[i]))) }

            // should still be in step 1
            assertEquals(1, powerGrid.step)

            powerGrid = powerGrid.dispatch(PassConnectCityAction())
            // other player connects 9 cities
            (7..15).forEach { i -> powerGrid = powerGrid.dispatch(ConnectCityAction((cities[i]))) }

            // should still be in step 1
            assertEquals(1, powerGrid.step)

            powerGrid = powerGrid.dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())

            // should be in step 2
            assertEquals(2, powerGrid.step)
            assertTrue(powerGrid.phase is BureaucracyPhase)

            // lowest power plant should be removed and also the all power plants lower than 9
            assertEquals(listOf(10, 11, 13, 21), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(19, powerGrid.powerPlantMarket.deck.remaining)
        }

        it("should go to step 2 when player connects 6th city when 6 players") {
            var powerGrid = PowerGrid(
                    random = Random(0),
                    players = listOf(player1, player2, player3, player4, player5, player6),
                    map = map.restrict(setOf(
                            map.areas.find { area -> area.name == "NE" }!!,
                            map.areas.find { area -> area.name == "NW" }!!,
                            map.areas.find { area -> area.name == "W" }!!,
                            map.areas.find { area -> area.name == "SE" }!!,
                            map.areas.find { area -> area.name == "SW" }!!
                    ))).copy(
                    playerStates = mapOf(
                            Pair(player1, PlayerState(balance = 9999)),
                            Pair(player2, PlayerState(balance = 9999)),
                            Pair(player3, PlayerState(balance = 9999)),
                            Pair(player4, PlayerState(balance = 9999)),
                            Pair(player5, PlayerState(balance = 9999)),
                            Pair(player6, PlayerState(balance = 9999))
                    ))

            val cities = map.cities.toList()

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 4))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 5))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 6))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 7))
                    .dispatch(PassBidAction())

            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 8))
                    .dispatch(BuyResourcesAction(ResourceType.OIL, 2))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            // player connects 6 cities
            (0..5).forEach { i -> powerGrid = powerGrid.dispatch(ConnectCityAction((cities[i]))) }

            // should still be in step 1
            assertEquals(1, powerGrid.step)

            powerGrid = powerGrid.dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())

            // should be in step 2
            assertEquals(2, powerGrid.step)
            assertTrue(powerGrid.phase is BureaucracyPhase)

            // lowest power plant should be removed
            assertEquals(listOf(10, 13, 16, 22), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(27, powerGrid.powerPlantMarket.deck.remaining)
        }

        it("should go to step 2 when player connects 6th city when 2 players") {
            var powerGrid = PowerGrid(
                    random = Random(0),
                    players = listOf(player1, player2),
                    map = map.restrict(setOf(
                            map.areas.find { area -> area.name == "NE" }!!,
                            map.areas.find { area -> area.name == "NW" }!!,
                            map.areas.find { area -> area.name == "W" }!!
                    ))).copy(
                    playerStates = mapOf(
                            Pair(player1, PlayerState(balance = 9999)),
                            Pair(player2, PlayerState(balance = 9999))
                    ))

            val cities = map.cities.toList()

            powerGrid = powerGrid
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 4))
                    .dispatch(BuyResourcesAction(ResourceType.OIL, 2))
                    .dispatch(PassBuyResourcesAction())
                    .dispatch(PassBuyResourcesAction())

            // player connects 10 cities
            (0..9).forEach { i -> powerGrid = powerGrid.dispatch(ConnectCityAction((cities[i]))) }

            // should still be in step 1
            assertEquals(1, powerGrid.step)

            powerGrid = powerGrid.dispatch(PassConnectCityAction())
                    .dispatch(PassConnectCityAction())

            // should be in step 2
            assertEquals(2, powerGrid.step)
            assertTrue(powerGrid.phase is BureaucracyPhase)

            // lowest power plant should be removed
            assertEquals(listOf(12, 13, 14, 20), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(18, powerGrid.powerPlantMarket.deck.remaining)
        }
    }
})