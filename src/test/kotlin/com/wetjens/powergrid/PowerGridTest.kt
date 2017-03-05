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

}