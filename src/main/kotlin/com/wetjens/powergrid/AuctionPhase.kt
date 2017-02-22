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
data class AuctionPhase(
        val biddingOrder: List<Player>,
        val auctioningPlayers: List<Player>,
        val currentAuctioningPlayer: Player = auctioningPlayers.first(),
        val closedAuctions: List<Auction> = emptyList(),
        private val currentAuction: Auction? = null) : Phase {

    override val currentPlayer: Player
        get() = when (auctionInProgress) {
            true -> auction.currentBiddingPlayer
            else -> currentAuctioningPlayer
        }

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
    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces: PowerPlant?): AuctionPhase {
        currentAuction == null || throw IllegalStateException("auction in progress")

        initialBid >= powerPlant.cost || throw IllegalArgumentException("bid too low")

        val biddingPlayers = biddingOrder.filter({ player -> auctioningPlayers.contains(player) })
        // get next player clockwise from current auctioning player
        val biddingPlayer = biddingPlayers[(biddingPlayers.indexOf(currentAuctioningPlayer) + 1) % biddingPlayers.size]

        val newAuction = Auction(
                biddingPlayers = biddingPlayers,
                currentBiddingPlayer = biddingPlayer,
                powerPlant = powerPlant,
                replaces = replaces,
                currentBid = initialBid)

        return when (auctioningPlayers.size) {
            1 -> copy(auctioningPlayers = emptyList(), closedAuctions = closedAuctions + newAuction)
            else -> copy(currentAuction = newAuction)
        }
    }

    /**
     * Passes to start (if no auction is in progress) for the current player that is up for auction.
     */
    fun passAuction(): AuctionPhase {
        currentAuction == null || throw IllegalStateException("auction in progress")

        val newAuctioningPlayers = auctioningPlayers - currentAuctioningPlayer
        return copy(auctioningPlayers = newAuctioningPlayers, currentAuctioningPlayer = nextAuctioningPlayer, currentAuction = null)
    }

    /**
     * Raises the bid for the current player that is bidding in the auction.
     */
    fun raise(bid: Int, replaces: PowerPlant? = null): AuctionPhase {
        return copy(currentAuction = auction.raise(bid, replaces))
    }

    /**
     * Folds the current player that is bidding in the auction.
     */
    fun passBid(): AuctionPhase {
        val newAuction = auction.pass()

        return when (newAuction.closed) {
            true -> copy(auctioningPlayers = auctioningPlayers - auction.nextBiddingPlayer,
                    currentAuctioningPlayer = when (auction.nextBiddingPlayer) {
                        currentAuctioningPlayer -> nextAuctioningPlayer
                        else -> currentAuctioningPlayer
                    },
                    closedAuctions = closedAuctions + auction,
                    currentAuction = null)
            else -> copy(currentAuction = newAuction)
        }
    }

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

        /**
         * Raises the bid for the current player that is bidding in the auction.
         */
        fun raise(bid: Int, replaces: PowerPlant? = null): Auction {
            bid > currentBid || throw IllegalArgumentException("bid too low")

            return copy(currentBiddingPlayer = nextBiddingPlayer, currentBid = bid, replaces = replaces)
        }

        /**
         * Auction that is held during the auction phase for a power plant.
         */
        fun pass(): Auction {
            biddingPlayers.size > 1 || throw IllegalStateException("no other players bidding anymore")

            return copy(biddingPlayers = biddingPlayers - currentBiddingPlayer, currentBiddingPlayer = nextBiddingPlayer)
        }
    }
}