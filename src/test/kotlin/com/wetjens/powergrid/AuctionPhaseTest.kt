package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.junit.Test
import kotlin.test.assertEquals

class AuctionPhaseTest {

    val powerPlant = PowerPlant(cost = 4, consumes = setOf(ResourceType.COAL), requires = 2, powers = 1)

    @Test
    fun test() {
        val player1 = Player(name = "Player 1")
        val player2 = Player(name = "Player 2")

        var auctionPhase = AuctionPhase(
                biddingOrder = listOf(player1, player2),
                auctioningPlayers = listOf(player1, player2))

        assertEquals(listOf(player1, player2), auctionPhase.auctioningPlayers)
        assertEquals(player1, auctionPhase.currentAuctioningPlayer)
        assertEquals(false, auctionPhase.auctionInProgress)

        auctionPhase = auctionPhase.startAuction(powerPlant, 4, null)

        assertEquals(powerPlant, auctionPhase.auction.powerPlant)
        assertEquals(4, auctionPhase.auction.currentBid)
        assertEquals(player2, auctionPhase.auction.currentBiddingPlayer)
        assertEquals(player1, auctionPhase.currentAuctioningPlayer)

        auctionPhase = auctionPhase.raise(5)

        assertEquals(5, auctionPhase.auction.currentBid)
        assertEquals(player1, auctionPhase.currentAuctioningPlayer)
        assertEquals(player1, auctionPhase.auction.currentBiddingPlayer)

        auctionPhase = auctionPhase.raise(6)

        assertEquals(6, auctionPhase.auction.currentBid)
        assertEquals(player1, auctionPhase.currentAuctioningPlayer)
        assertEquals(player2, auctionPhase.auction.currentBiddingPlayer)

        auctionPhase = auctionPhase.passBid()

        assertEquals(false, auctionPhase.auctionInProgress)
        assertEquals(player2, auctionPhase.currentAuctioningPlayer)
    }

    @Test
    fun testPassAuction() {
        val player1 = Player(name = "Player 1")
        val player2 = Player(name = "Player 2")

        var auctionPhase = AuctionPhase(
                biddingOrder = listOf(player1, player2),
                auctioningPlayers = listOf(player1, player2))

        assertEquals(listOf(player1, player2), auctionPhase.auctioningPlayers)
        assertEquals(false, auctionPhase.auctionInProgress)
        assertEquals(player1, auctionPhase.currentAuctioningPlayer)

        auctionPhase = auctionPhase.passAuction()

        assertEquals(listOf(player2), auctionPhase.auctioningPlayers)
        assertEquals(player2, auctionPhase.currentAuctioningPlayer)
        assertEquals(false, auctionPhase.auctionInProgress)
    }

}