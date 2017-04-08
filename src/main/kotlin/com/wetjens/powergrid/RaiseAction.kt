package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant

/**
 * Raises the bid for the current player that is bidding in the auction.
 */
data class RaiseAction(val bid: Int,
                  val replaces: PowerPlant? = null) : AuctionAction {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            checkBid(powerGrid, auctionPhase.auction.currentBiddingPlayer, bid, replaces)

            bid > auctionPhase.auction.currentBid || throw IllegalArgumentException("bid too low")

            val newAuction = auctionPhase.auction.copy(
                    currentBiddingPlayer = auctionPhase.auction.nextBiddingPlayer,
                    currentBid = bid,
                    replaces = replaces)

            return powerGrid.copy(phase = auctionPhase.copy(currentAuction = newAuction))
        }
    }

}