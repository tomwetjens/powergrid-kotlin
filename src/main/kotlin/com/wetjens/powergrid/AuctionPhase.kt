package com.wetjens.powergrid

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
class AuctionPhase(
        val auctioningPlayers: List<Player>,
        val currentAuctioningPlayer: Player = auctioningPlayers.first(),
        private val currentAuction: Auction? = null) : Phase {

    val nextAuctioningPlayer: Player by lazy {
        val nextIndex = (auctioningPlayers.indexOf(currentAuctioningPlayer) + 1) % auctioningPlayers.size
        auctioningPlayers[nextIndex]
    }

    val completed: Boolean = auctioningPlayers.isEmpty()

    val auctionInProgress: Boolean = currentAuction != null

    val auction: Auction by lazy {
        currentAuction ?: throw IllegalStateException("no auction in progress")
    }

    /**
     * Starts a new auction for the current player that is up for auction (if no auction is already in progress),
     * for a given power plant with an initial bid.
     *
     * @param bid must be at minimum the cost of the power plant
     */
    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces:PowerPlant?): AuctionPhase {
        currentAuction == null || throw IllegalStateException("auction in progress")
        auctioningPlayers.size > 1 || throw IllegalStateException("not enough players for auction")

        initialBid >= powerPlant.cost || throw IllegalArgumentException("bid too low")

        return AuctionPhase(auctioningPlayers, currentAuctioningPlayer, Auction(auctioningPlayers, nextAuctioningPlayer, powerPlant, replaces, initialBid))
    }

    /**
     * Passes to start (if no auction is in progress) for the current player that is up for auction.
     */
    fun passAuction(): AuctionPhase {
        currentAuction == null || throw IllegalStateException("auction in progress")

        val newAuctioningPlayers = auctioningPlayers - currentAuctioningPlayer
        return AuctionPhase(newAuctioningPlayers, nextAuctioningPlayer, null)
    }

    /**
     * Raises the bid for the current player that is bidding in the auction.
     */
    fun raise(bid: Int): AuctionPhase {
        return AuctionPhase(auctioningPlayers, currentAuctioningPlayer, auction.raise(bid))
    }

    /**
     * Folds the current player that is bidding in the auction.
     */
    fun fold(): AuctionPhase {
        val newAuction = auction.fold()

        return when (newAuction.closed) {
            true -> AuctionPhase(auctioningPlayers - currentAuctioningPlayer, nextAuctioningPlayer, null)
            else -> AuctionPhase(auctioningPlayers, currentAuctioningPlayer, newAuction)
        }
    }

    /**
     * Auction that is held during the auction phase for a power plant.
     */
    class Auction(val biddingPlayers: List<Player>,
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

        /**
         * Raises the bid for the current player that is bidding in the auction.
         */
        fun raise(bid: Int): Auction {
            bid > currentBid || throw IllegalArgumentException("bid too low")

            return Auction(biddingPlayers, nextBiddingPlayer, powerPlant, replaces, bid)
        }

        /**
         * Auction that is held during the auction phase for a power plant.
         */
        fun fold(): Auction {
            biddingPlayers.size > 1 || throw IllegalStateException("no other players bidding anymore")

            return Auction(biddingPlayers - currentBiddingPlayer, nextBiddingPlayer, powerPlant, replaces, currentBid)
        }
    }
}