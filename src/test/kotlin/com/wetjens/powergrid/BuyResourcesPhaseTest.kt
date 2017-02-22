package com.wetjens.powergrid

import com.wetjens.powergrid.resource.ResourceMarkets
import com.wetjens.powergrid.resource.ResourceType
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuyResourcesPhaseTest {

    @Test
    fun test() {
        val player1 = Player(name = "Player 1")
        val player2 = Player(name = "Player 2")

        val resourceMarkets = ResourceMarkets()

        var phase = BuyResourcesPhase(buyingPlayers = listOf(player1, player2), resourceMarkets = resourceMarkets)

        assertEquals(player1, phase.currentBuyingPlayer)

        phase = phase.buy(ResourceType.COAL, 2)
        assertEquals(22, phase.resourceMarkets[ResourceType.COAL].available)
        assertEquals(player1, phase.currentBuyingPlayer)

        phase = phase.pass()
        assertEquals(player2, phase.currentBuyingPlayer)

        phase = phase.buy(ResourceType.COAL, 2)
        assertEquals(20, phase.resourceMarkets[ResourceType.COAL].available)
        phase = phase.buy(ResourceType.OIL, 2)
        assertEquals(16, phase.resourceMarkets[ResourceType.OIL].available)

        phase = phase.pass()
        assertTrue(phase.completed)
    }

}