package com.wetjens.powerline

import com.wetjens.powerline.powerplant.PowerPlant

/**
 * Auction that is held during the auction phase for a power plant.
 */
data class Auction(val biddingPlayers: List<Player>,
                   val currentBiddingPlayer: Player = biddingPlayers.first(),
                   val powerPlant: PowerPlant,
                   val replaces: PowerPlant?,
                   val currentBid: Int) {

    val nextBiddingPlayer: Player by lazy {
        val nextIndex = (biddingPlayers.indexOf(currentBiddingPlayer) + 1) % biddingPlayers.size
        biddingPlayers[nextIndex]
    }

    /**
     * Returns whether the auction is still open for bidding, or it's closed and can take no more bids.
     */
    val closed: Boolean = biddingPlayers.size == 1

}