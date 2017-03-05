package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
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

    val map = PowerGridTest::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    @Test
    fun preparationFirstStep() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(1, powerGrid.step)
    }

    @Test
    fun preparationFirstRound() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(1, powerGrid.round)
    }

    @Test
    fun preparationInitialBalance() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(50, powerGrid.playerStates[player1]!!.balance)
        assertEquals(50, powerGrid.playerStates[player2]!!.balance)
    }

    @Test
    fun preparationInitialRandomPlayerOrder() {
        var powerGrid = PowerGrid(random = random, players = listOf(player1, player2, player3), map = map)

        assertEquals(listOf(player3, player2, player1), powerGrid.playerOrder)
    }

    @Test
    fun preparationInitialPowerPlantMarket() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(listOf(3, 4, 5, 6), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(listOf(7, 8, 9, 10), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
    }

    @Test
    fun preparationInitialDeck() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(13, powerGrid.powerPlantMarket.deck.onTop?.cost)
    }

    @Test
    fun preparationStartInAuctionPhase() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertTrue(powerGrid.phase is AuctionPhase)
    }

    @Test
    fun preparationStartWithFirstPlayer() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        assertEquals(player3, powerGrid.currentPlayer)
    }

    @Test
    fun preparationInitialResourceMarkets() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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
    fun buyResourcesStartInReversePlayerOrder() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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

        assertTrue(powerGrid.phase is BuildPhase)
    }

    @Test
    fun buyResourcesBalanceTooLow() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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
        var powerGrid = PowerGrid(random = random, players = players, map = map)

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