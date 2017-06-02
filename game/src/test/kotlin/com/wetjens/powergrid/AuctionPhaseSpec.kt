package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

object AuctionPhaseSpec : Spek({

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    val map = AuctionPhaseSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    describe("PowerGrid") {

        it("should start auction phase with leading player") {
            val powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)
        }

        /**
         * The player may choose one power plant from the actual market and makes a bid to purchase it.
         */
        it("should only auction power plants in the actual market") {
            val powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            val powerPlant = powerGrid.powerPlantMarket.future[0]
            try {
                powerGrid.dispatch(StartAuctionAction(powerPlant, 5))
                fail()
            } catch (e: IllegalArgumentException) {
                assertEquals("$powerPlant not in actual", e.message)
            }
        }

        /**
         * The number of the power plant is the lowest bid allowed, but the player may start with a higher bid.
         */
        it("should only accept initial bid equal or higher than cost") {
            val powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            try {
                powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 2))
                fail()
            } catch (e: IllegalArgumentException) {
                assertEquals("bid must be >= 3", e.message)
            }
        }

        /**
         * Players immediately draw a new card to replace the power plant sold and place it in the market.
         */
        it("should immediately replace power plant after starting auction") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            val powerPlant3 = powerGrid.powerPlantMarket.actual[0]
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerPlant3, 3))

            assertEquals(listOf(4, 5, 6, 7), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(listOf(8, 9, 10, 13), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
        }

        /**
         * Player chooses a power plant from the actual market and makes an initial bid.
         *
         * Continuing in clockwise order, the other players can make higher bids or pass.
         * If a player passes, he may not re-enter the current auction.
         *
         * Players keep bidding or passing until only one player remains.
         * He pays his bid to the bank and takes the power plant.
         */
        it("should perform bidding in clockwise order") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)

            val powerPlant3 = powerGrid.powerPlantMarket.actual[0]
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerPlant3, 3))

            assertEquals(player1, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(RaiseAction(4))

            assertEquals(player2, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(RaiseAction(5))

            assertEquals(player3, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(RaiseAction(6))

            assertEquals(player1, powerGrid.currentPlayer)
            // player3 buys it
            powerGrid = powerGrid.dispatch(PassBidAction()).dispatch(PassBidAction())
            assertEquals(44, powerGrid.playerStates[player3]!!.balance)
            assertEquals(listOf(powerPlant3), powerGrid.playerStates[player3]!!.powerPlants)

            // then second best player is up
            val powerPlant4 = powerGrid.powerPlantMarket.actual[0]
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerPlant4, 4))

            assertEquals(player1, powerGrid.currentPlayer)
            // player2 buys it
            powerGrid = powerGrid.dispatch(PassBidAction())
            assertEquals(46, powerGrid.playerStates[player2]!!.balance)
            assertEquals(listOf(powerPlant4), powerGrid.playerStates[player2]!!.powerPlants)

            // then last player
            assertEquals(player1, powerGrid.currentPlayer)
            val powerPlant5 = powerGrid.powerPlantMarket.actual[0]
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerPlant5, 5))
            assertEquals(45, powerGrid.playerStates[player1]!!.balance)
            assertEquals(listOf(powerPlant5), powerGrid.playerStates[player1]!!.powerPlants)
        }

        it("should not start auction if balance is too low") {
            val powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            try {
                powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 51))
                fail()
            } catch (e: IllegalArgumentException) {
                assertEquals("balance too low", e.message)
            }
        }

        it("should not accept bid if balance too low") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))

            try {
                powerGrid.dispatch(RaiseAction(51))
                fail()
            } catch (e: IllegalArgumentException) {
                assertEquals("balance too low", e.message)
            }
        }

        /**
         * A player that buys a power plant cannot bid in another auction in the same round.
         */
        it("should not allow bidding by player that already bought as power plant in the same round") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))

            assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
            assertEquals(player1, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(PassBidAction())

            assertEquals(player2, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(PassBidAction())

            // player3 has bought it

            // start new auction
            assertEquals(player2, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 4))

            // then player3 should not be bidding anymore

            assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
            assertEquals(player1, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(PassBidAction())

            assertFalse((powerGrid.phase as AuctionPhase).auctionInProgress)
            assertEquals(player1, powerGrid.currentPlayer)

            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 5))
            assertTrue(powerGrid.phase is BuyResourcesPhase)
        }

        /**
         * In the first round every player must buy a power plant.
         */
        it("should force player to start auction in the first round") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)
            try {
                powerGrid.dispatch(PassAuctionAction())
                fail("should not be allowed to pass in first round")
            } catch (e: IllegalStateException) {
                assertEquals("cannot pass in first round", e.message)
            }

            // buy something and move auction to next player
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
            powerGrid = powerGrid.dispatch(PassBidAction())
            powerGrid = powerGrid.dispatch(PassBidAction())

            // next player should also not be allowed to pass

            assertEquals(player2, powerGrid.currentPlayer)
            try {
                powerGrid.dispatch(PassAuctionAction())
                fail("should not be allowed to pass in first round")
            } catch (e: IllegalStateException) {
                assertEquals("cannot pass in first round", e.message)
            }
        }

        /**
         * If it is a player's turn to choose a power plant, but he does not want to buy a power plant, he can pass.
         * If he does so, he cannot bid on other power plants in later auction in this round and, thus, will not get a new power plant this round.
         */
        it("should not allow player to bid after passing in the same round") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map).copy(round = 2)

            assertEquals(player3, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(PassAuctionAction())

            // start new auction
            assertEquals(player2, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))

            // then player3 should not be allowed bidding

            assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
            assertEquals(player1, powerGrid.currentPlayer)
            powerGrid = powerGrid.dispatch(PassBidAction())

            assertFalse((powerGrid.phase as AuctionPhase).auctionInProgress)
            assertEquals(player1, powerGrid.currentPlayer)
        }

        it("should allow next player to start auction after auctioning player wins bid") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)
            // leading player buys
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())

            // then second best player should be able to pick
            assertTrue(powerGrid.phase is AuctionPhase)
            assertEquals(player2, powerGrid.currentPlayer)

            // second best player buys
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 4))
                    .dispatch(PassBidAction())

            // then last player should be able to pick
            assertTrue(powerGrid.phase is AuctionPhase)
            assertEquals(player1, powerGrid.currentPlayer)
        }

        it("should allow current player to start again auction after not winning bid") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(player3, powerGrid.currentPlayer)
            // other player buys it
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(RaiseAction(4))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())

            // then player should again pick
            assertTrue(powerGrid.phase is AuctionPhase)
            assertEquals(player3, powerGrid.currentPlayer)

            // now buys it
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 4))
                    .dispatch(PassBidAction())

            // then last player should be able to pick
            assertTrue(powerGrid.phase is AuctionPhase)
            assertEquals(player2, powerGrid.currentPlayer)
        }

        /**
         * Because of the random player order at the beginning of the game,
         * the player order must be re-determined after auctioning the power plants before going to the next phase.
         */
        it("should redetermine player order after first round") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            assertEquals(listOf(player3, player2, player1), powerGrid.playerOrder)

            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[1], 4))
                    .dispatch(PassBidAction())
                    .dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[2], 5))

            assertEquals(listOf(player1, player2, player3), powerGrid.playerOrder)

            assertTrue(powerGrid.phase is BuyResourcesPhase)
            assertEquals(listOf(player3, player2, player1), (powerGrid.phase as BuyResourcesPhase).buyingPlayers)
        }

        it("should not allow player to start auction without replacing one of 3 power plants") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            // given a player has 3 power plants
            powerGrid = powerGrid.copy(playerStates = mapOf(
                    Pair(player1, PlayerState(powerPlants = emptyList())),
                    Pair(player2, PlayerState(powerPlants = emptyList())),
                    Pair(player3, PlayerState(powerPlants = listOf(
                            powerGrid.powerPlantMarket.deck.powerPlants[26]!!,
                            powerGrid.powerPlantMarket.deck.powerPlants[27]!!,
                            powerGrid.powerPlantMarket.deck.powerPlants[28]!!
                    )))
            ))


            // then he must indicate one that must be replaced
            try {
                powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))
                fail("must replace a power plant")
            } catch (e: IllegalArgumentException) {
                // expected
                assertEquals("must replace a power plant", e.message)
            }

            // indicate that first should be replaced
            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3, powerGrid.playerStates[player3]!!.powerPlants[0]))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())

            assertEquals(listOf(3, 27, 28), powerGrid.playerStates[player3]!!.powerPlants.map(PowerPlant::cost))
        }

        it("should not allow player to raise bid without replacing one of 3 power plants") {
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)

            // given a player has 3 power plants
            powerGrid = powerGrid.copy(playerStates = mapOf(
                    Pair(player1, PlayerState(powerPlants = listOf(
                            powerGrid.powerPlantMarket.deck.powerPlants[26]!!,
                            powerGrid.powerPlantMarket.deck.powerPlants[27]!!,
                            powerGrid.powerPlantMarket.deck.powerPlants[28]!!
                    ))),
                    Pair(player2, PlayerState(powerPlants = emptyList())),
                    Pair(player3, PlayerState(powerPlants = emptyList()))
            ))

            powerGrid = powerGrid.dispatch(StartAuctionAction(powerGrid.powerPlantMarket.actual[0], 3))

            assertEquals(player1, powerGrid.currentPlayer)

            // then he must indicate one that must be replaced
            try {
                powerGrid.dispatch(RaiseAction(4))
                fail("must replace a power plant")
            } catch (e: IllegalArgumentException) {
                // expected
                assertEquals("must replace a power plant", e.message)
            }

            // indicate that first should be replaced
            powerGrid = powerGrid.dispatch(RaiseAction(4, powerGrid.playerStates[player1]!!.powerPlants[0]))
                    .dispatch(PassBidAction())
                    .dispatch(PassBidAction())

            assertEquals(listOf(3, 27, 28), powerGrid.playerStates[player1]!!.powerPlants.map(PowerPlant::cost))
        }

        it("should remove cheapest power plant when all players pass auction") {
            // given game in round >1
            var powerGrid = PowerGrid(random = Random(0), players = players, map = map)
                    .copy(round = 2)
            // but unrealistic, but okay, just to prove this rule
            assertEquals(listOf(3, 4, 5, 6), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(listOf(7, 8, 9, 10), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))

            // when all players pass auction
            powerGrid = powerGrid
                    .dispatch(PassAuctionAction())
                    .dispatch(PassAuctionAction())
                    .dispatch(PassAuctionAction())

            // then lowest power plant must be removed from market
            assertEquals(listOf(4, 5, 6, 7), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
            assertEquals(listOf(8, 9, 10, 13), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
        }
    }
})