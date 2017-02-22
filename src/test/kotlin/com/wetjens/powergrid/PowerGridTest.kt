package com.wetjens.powergrid

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class PowerGridTest {

    val random: Random = Random(0)

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    @Test
    fun preparationFirstStep() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(1, powerGrid.step)
    }

    @Test
    fun preparationFirstRound() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(1, powerGrid.round)
    }

    @Test
    fun preparationInitialBalance() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(50, powerGrid.playerStates[player1]!!.balance)
        assertEquals(50, powerGrid.playerStates[player2]!!.balance)
    }

    @Test
    fun preparationInitialRandomPlayerOrder() {
        var powerGrid = PowerGrid(random, listOf(player1, player2, player3))

        assertEquals(listOf(player3, player2, player1), powerGrid.playerOrder)
    }

    @Test
    fun preparationInitialPowerPlantMarket() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(listOf(3, 4, 5, 6), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
    }

    @Test
    fun preparationInitialDeck() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(13, powerGrid.powerPlantMarket.deck.onTop?.cost)
    }

    @Test
    fun preparationStartInAuctionPhase() {
        var powerGrid = PowerGrid(random, players)

        assertTrue(powerGrid.phase is AuctionPhase)
    }

    @Test
    fun preparationStartWithFirstPlayer() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
    }

    @Test
    fun preparationInitialResourceMarkets() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(24, powerGrid.resourceMarkets[ResourceType.COAL].capacity)
        assertEquals(24, powerGrid.resourceMarkets[ResourceType.COAL].available)

        assertEquals(24, powerGrid.resourceMarkets[ResourceType.OIL].capacity)
        assertEquals(18, powerGrid.resourceMarkets[ResourceType.OIL].available)

        assertEquals(24, powerGrid.resourceMarkets[ResourceType.BIO_MASS].capacity)
        assertEquals(6, powerGrid.resourceMarkets[ResourceType.BIO_MASS].available)

        assertEquals(12, powerGrid.resourceMarkets[ResourceType.URANIUM].capacity)
        assertEquals(2, powerGrid.resourceMarkets[ResourceType.URANIUM].available)
    }

    @Test
    fun auctionLeadingPlayerShouldStartAuctionPhase() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
    }

    /**
     * The player may choose one power plant from the actual market and makes a bid to purchase it.
     */
    @Test(expected = IllegalArgumentException::class)
    fun auctionOnlyActualMarket() {
        val powerGrid = PowerGrid(random, players)

        val powerPlant = powerGrid.powerPlantMarket.future[0]
        try {
            powerGrid.startAuction(powerPlant, 5)
        } catch (e: IllegalArgumentException) {
            assertEquals("$powerPlant not in actual", e.message)
            throw e
        }
    }

    /**
     * The number of the power plant is the lowest bid allowed, but the player may start with a higher bid.
     */
    @Test(expected = IllegalArgumentException::class)
    fun auctionInitialBidMustBeAtLeastCost() {
        val powerGrid = PowerGrid(random, players)

        try {
            powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 2)
        } catch (e: IllegalArgumentException) {
            assertEquals("bid too low", e.message)
            throw e
        }
    }

    /**
     * Players immediately draw a new card to replace the power plant sold and place it in the market.
     */
    @Test
    fun auctionImmediatelyReplacePowerPlantAfterStartingAuction() {
        var powerGrid = PowerGrid(random, players)

        val powerPlant3 = powerGrid.powerPlantMarket.actual[0]
        powerGrid = powerGrid.startAuction(powerPlant3, 3)

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
    @Test
    fun auctionBiddingInClockwiseOrder() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)

        val powerPlant3 = powerGrid.powerPlantMarket.actual[0]
        powerGrid = powerGrid.startAuction(powerPlant3, 3)

        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.raise(4)

        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.raise(5)

        assertEquals(player3, powerGrid.currentPlayer)
        powerGrid = powerGrid.raise(6)

        assertEquals(player1, powerGrid.currentPlayer)
        // player3 buys it
        powerGrid = powerGrid.passBid().passBid()
        assertEquals(44, powerGrid.playerStates[player3]!!.balance)
        assertEquals(listOf(powerPlant3), powerGrid.playerStates[player3]!!.powerPlants)

        // then second best player is up
        val powerPlant4 = powerGrid.powerPlantMarket.actual[0]
        powerGrid = powerGrid.startAuction(powerPlant4, 4)

        assertEquals(player1, powerGrid.currentPlayer)
        // player2 buys it
        powerGrid = powerGrid.passBid()
        assertEquals(46, powerGrid.playerStates[player2]!!.balance)
        assertEquals(listOf(powerPlant4), powerGrid.playerStates[player2]!!.powerPlants)

        // then last player
        assertEquals(player1, powerGrid.currentPlayer)
        val powerPlant5 = powerGrid.powerPlantMarket.actual[0]
        powerGrid = powerGrid.startAuction(powerPlant5, 5)
        assertEquals(45, powerGrid.playerStates[player1]!!.balance)
        assertEquals(listOf(powerPlant5), powerGrid.playerStates[player1]!!.powerPlants)
    }

    @Test(expected = IllegalArgumentException::class)
    fun auctionCannotStartAuctionIfBalanceTooLow() {
        val powerGrid = PowerGrid(random, players)

        try {
            powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 51)
        } catch (e: IllegalArgumentException) {
            assertEquals("balance too low", e.message)
            throw e
        }
    }

    @Test(expected = IllegalArgumentException::class)
    fun auctionCannotBidIfBalanceTooLow() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)

        try {
            powerGrid.raise(51)
        } catch (e: IllegalArgumentException) {
            assertEquals("balance too low", e.message)
            throw e
        }
    }

    /**
     * A player that buys a power plant cannot bid in another auction in the same round.
     */
    @Test
    fun auctionPlayerThatBuysCannotBidAgainInSameRound() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)

        assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.passBid()

        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.passBid()

        // player3 has bought it

        // start new auction
        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4)

        // then player3 should not be bidding anymore

        assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.passBid()

        assertFalse((powerGrid.phase as AuctionPhase).auctionInProgress)
        assertEquals(player1, powerGrid.currentPlayer)

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 5)
        assertTrue(powerGrid.phase is BuyResourcesPhase)
    }

    /**
     * In the first round every player must buy a power plant.
     */
    @Test
    fun auctionCannotPassAuctionInFirstRound() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
        try {
            powerGrid.passAuction()
            fail("should not be allowed to pass in first round")
        } catch (e: IllegalStateException) {
            assertEquals("cannot pass in first round", e.message)
        }

        // buy something and move auction to next player
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)
        powerGrid = powerGrid.passBid()
        powerGrid = powerGrid.passBid()

        // next player should also not be allowed to pass

        assertEquals(player2, powerGrid.currentPlayer)
        try {
            powerGrid.passAuction()
            fail("should not be allowed to pass in first round")
        } catch (e: IllegalStateException) {
            assertEquals("cannot pass in first round", e.message)
        }
    }

    /**
     * If it is a player's turn to choose a power plant, but he does not want to buy a power plant, he can pass.
     * If he does so, he cannot bid on other power plants in later auction in this round and, thus, will not get a new power plant this round.
     */
    @Test
    fun auctionPlayerThatPassesAuctionCannotBidInSameRound() {
        var powerGrid = PowerGrid(random, players).copy(round = 2)

        assertEquals(player3, powerGrid.currentPlayer)
        powerGrid = powerGrid.passAuction()

        // start new auction
        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)

        // then player3 should not be allowed bidding

        assertTrue((powerGrid.phase as AuctionPhase).auctionInProgress)
        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.passBid()

        assertFalse((powerGrid.phase as AuctionPhase).auctionInProgress)
        assertEquals(player1, powerGrid.currentPlayer)
    }

    @Test
    fun auctionAuctioningPlayerWinningBidNextPlayerBecomesAuctioningPlayer() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
        // leading player buys
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()

        // then second best player should be able to pick
        assertTrue(powerGrid.phase is AuctionPhase)
        assertEquals(player2, powerGrid.currentPlayer)

        // second best player buys
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4)
                .passBid()

        // then last player should be able to pick
        assertTrue(powerGrid.phase is AuctionPhase)
        assertEquals(player1, powerGrid.currentPlayer)
    }

    @Test
    fun auctionAuctioningPlayerNotWinningBidRemainsAuctioningPlayer() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(player3, powerGrid.currentPlayer)
        // other player buys it
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .raise(4)
                .passBid()
                .passBid()

        // then player should again pick
        assertTrue(powerGrid.phase is AuctionPhase)
        assertEquals(player3, powerGrid.currentPlayer)

        // now buys it
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4)
                .passBid()

        // then last player should be able to pick
        assertTrue(powerGrid.phase is AuctionPhase)
        assertEquals(player2, powerGrid.currentPlayer)
    }

    /**
     * Because of the random player order at the beginning of the game,
     * the player order must be re-determined after auctioning the power plants before going to the next phase.
     */
    @Test
    fun auctionRedeterminePlayerOrderFirstRound() {
        var powerGrid = PowerGrid(random, players)

        assertEquals(listOf(player3, player2, player1), powerGrid.playerOrder)

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)

        assertEquals(listOf(player1, player2, player3), powerGrid.playerOrder)

        assertTrue(powerGrid.phase is BuyResourcesPhase)
        assertEquals(listOf(player3, player2, player1), (powerGrid.phase as BuyResourcesPhase).buyingPlayers)
    }

    @Test
    fun auctionStartAuctionCanHaveMaxThreePowerPlants() {
        var powerGrid = PowerGrid(random, players)

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
            powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)
            fail("must replace a power plant")
        } catch (e: IllegalArgumentException) {
            // expected
            assertEquals("must replace a power plant", e.message)
        }

        // indicate that first should be replaced
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3, powerGrid.playerStates[player3]!!.powerPlants[0])
                .passBid()
                .passBid()

        assertEquals(listOf(3, 27, 28), powerGrid.playerStates[player3]!!.powerPlants.map(PowerPlant::cost))
    }

    @Test
    fun auctionBidCanHaveMaxThreePowerPlants() {
        var powerGrid = PowerGrid(random, players)

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

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3)

        assertEquals(player1, powerGrid.currentPlayer)

        // then he must indicate one that must be replaced
        try {
            powerGrid.raise(4)
            fail("must replace a power plant")
        } catch (e: IllegalArgumentException) {
            // expected
            assertEquals("must replace a power plant", e.message)
        }

        // indicate that first should be replaced
        powerGrid = powerGrid.raise(4, powerGrid.playerStates[player1]!!.powerPlants[0])
                .passBid()
                .passBid()

        assertEquals(listOf(3, 27, 28), powerGrid.playerStates[player1]!!.powerPlants.map(PowerPlant::cost))
    }

    @Test
    fun auctionAllPlayersPassAuctionThenRemoveCheapestPowerPlant() {
        // given game in round >1
        var powerGrid = PowerGrid(random, players)
                .copy(round = 2)
        // but unrealistic, but okay, just to prove this rule
        assertEquals(listOf(3, 4, 5, 6), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))

        // when all players pass auction
        powerGrid = powerGrid
                .passAuction()
                .passAuction()
                .passAuction()

        // then lowest power plant must be removed from market
        assertEquals(listOf(4, 5, 6, 7), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(listOf(8, 9, 10, 13), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
    }

    @Test
    fun buyResourcesStartInReversePlayerOrder() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)

        assertTrue(powerGrid.phase is BuyResourcesPhase)
        assertEquals(listOf(player3, player2, player1), (powerGrid.phase as BuyResourcesPhase).buyingPlayers)
    }

    @Test
    fun buyResources() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // 2 oil
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // 2 coal
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // 2 coal,oil

        assertEquals(player3, powerGrid.currentPlayer)
        powerGrid = powerGrid.buyResources(ResourceType.OIL, 4).passBuyResources()

        assertEquals(14, powerGrid.resourceMarkets[ResourceType.OIL].available)
        assertEquals(34, powerGrid.playerStates[player3]!!.balance)

        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.buyResources(ResourceType.COAL, 4).passBuyResources()

        assertEquals(20, powerGrid.resourceMarkets[ResourceType.COAL].available)
        assertEquals(41, powerGrid.playerStates[player2]!!.balance)

        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.buyResources(ResourceType.COAL, 4).passBuyResources()

        assertEquals(16, powerGrid.resourceMarkets[ResourceType.COAL].available)
        assertEquals(35, powerGrid.playerStates[player1]!!.balance)
    }

    @Test
    fun buyResourcesBalanceTooLow() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // 2 oil
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // 2 coal
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // 2 coal,oil

        assertEquals(player3, powerGrid.currentPlayer)
        assertEquals(47, powerGrid.playerStates[player3]!!.balance)

        // lose some money
        powerGrid = powerGrid.copy(playerStates = powerGrid.playerStates + Pair(player3, powerGrid.playerStates[player3]!!.pay(27)))
        assertEquals(20, powerGrid.playerStates[player3]!!.balance)

        try {
            powerGrid.buyResources(ResourceType.OIL, 6) // costs 21
            fail("must throw because balance too low")
        } catch (e: IllegalArgumentException) {
            // expected
            assertEquals("balance too low", e.message)
        }
    }

    @Test
    fun buyResourcesNotAvailable() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // 2 oil
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // 2 coal
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // 2 coal,oil

        assertEquals(2, powerGrid.resourceMarkets[ResourceType.URANIUM].available)

        try {
            powerGrid.buyResources(ResourceType.URANIUM, 3)
            fail("must throw because not enough available")
        } catch (e: IllegalArgumentException) {
            // expected
            assertEquals("not enough available", e.message)
        }
    }

    @Test
    fun buyResourcesMaxStorageExceeded() {
        var powerGrid = PowerGrid(random, players)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // 2 oil
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // 2 coal
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // 2 coal,oil

        // should be able to store twice the amount it requires
        powerGrid = powerGrid.buyResources(ResourceType.OIL, 4)

        try {
            powerGrid.buyResources(ResourceType.OIL, 1)
            fail("must throw because max storage exceeded")
        } catch (e: IllegalArgumentException) {
            // expected
            assertEquals("max storage exceeded", e.message)
        }
    }
}