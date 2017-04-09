package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant

/**
 * Auction phase in a game of Power Grid.
 *
 * Initially all players are participating in auctions and each may start one auction.
 *
 * The player that starts an auction determines the initial bid. Other players may participate in the auction by raising the current bid or by folding.
 * When all but one player have folded during an auction, the auction is closed and the next player may start an auction.
 *
 * If a player passes to start an auction, he cannot participate in any subsequent auctions started by other players.
 */
data class AuctionPhase(val biddingOrder: List<Player>,
                        val auctioningPlayers: List<Player>,
                        val currentAuctioningPlayer: Player = auctioningPlayers.first(),
                        val closedAuctions: List<Auction> = emptyList(),
                        val currentAuction: Auction? = null) : Phase {

    override val currentPlayer: Player
        get() = currentAuction?.currentBiddingPlayer ?: currentAuctioningPlayer

    val nextAuctioningPlayer: Player by lazy {
        val nextIndex = (auctioningPlayers.indexOf(currentAuctioningPlayer) + 1) % auctioningPlayers.size
        auctioningPlayers[nextIndex]
    }

    val completed: Boolean = auctioningPlayers.isEmpty()

    val auctionInProgress: Boolean = currentAuction != null

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
}