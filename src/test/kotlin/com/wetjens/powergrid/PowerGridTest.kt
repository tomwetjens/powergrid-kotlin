package com.wetjens.powergrid

import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class PowerGridTest {

    val random: Random = Random(0)

    @Test
    fun test() {
        val player1 = Player(name = "Player 1")
        val player2 = Player(name = "Player 2")

        var powerGrid = PowerGrid(random, listOf(player1, player2))

        assertEquals(1, powerGrid.step)
        assertEquals(1, powerGrid.round)

        assertEquals(player1, powerGrid.playerOrder[0])
        assertEquals(player2, powerGrid.playerOrder[1])

        assertEquals(50, powerGrid.playerStates[player1]!!.balance)
        assertEquals(50, powerGrid.playerStates[player2]!!.balance)

        assertTrue(powerGrid.phase is AuctionPhase)

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 3, null)
        assertEquals(listOf(4, 5, 6, 7), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))

        powerGrid = powerGrid.fold()
        assertEquals(47, powerGrid.playerStates[player1]!!.balance)
        assertEquals(listOf(3), powerGrid.playerStates[player1]!!.powerPlants.map(PowerPlant::cost))

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4, null)
        assertEquals(listOf(5, 6, 7, 8), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(46, powerGrid.playerStates[player2]!!.balance)
        assertEquals(listOf(4), powerGrid.playerStates[player2]!!.powerPlants.map(PowerPlant::cost))

        assertTrue(powerGrid.phase is BuyResourcesPhase)


    }

}