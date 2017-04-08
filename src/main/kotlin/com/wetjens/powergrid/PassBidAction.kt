package com.wetjens.powergrid

/**
 * Folds the current player that is bidding in the auction.
 */
class PassBidAction : AuctionAction {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            auctionPhase.auction.biddingPlayers.size > 1 || throw IllegalStateException("no other players bidding anymore")

            val newAuction = auctionPhase.auction.copy(
                    biddingPlayers = auctionPhase.auction.biddingPlayers - auctionPhase.auction.currentBiddingPlayer,
                    currentBiddingPlayer = auctionPhase.auction.nextBiddingPlayer)

            val newAuctionPhase = when (newAuction.closed) {
                true -> auctionPhase.copy(auctioningPlayers = auctionPhase.auctioningPlayers - auctionPhase.auction.nextBiddingPlayer,
                        currentAuctioningPlayer = when (auctionPhase.auction.nextBiddingPlayer) {
                            auctionPhase.currentAuctioningPlayer -> auctionPhase.nextAuctioningPlayer
                            else -> auctionPhase.currentAuctioningPlayer
                        },
                        closedAuctions = auctionPhase.closedAuctions + auctionPhase.auction,
                        currentAuction = null)
                else -> auctionPhase.copy(currentAuction = newAuction)
            }

            var newPlayerStates = powerGrid.playerStates

            if (auctionPhase.auction.biddingPlayers.size == 2) {
                // last player folded

                val winningPlayer = auctionPhase.auction.nextBiddingPlayer
                newPlayerStates = completePowerPlantPurchase(powerGrid, winningPlayer, auctionPhase.auction.powerPlant, auctionPhase.auction.currentBid, auctionPhase.auction.replaces)
            }

            return when (newAuctionPhase.completed) {
                false -> powerGrid.copy(phase = newAuctionPhase, playerStates = newPlayerStates)
                true -> powerGrid.copy(playerStates = newPlayerStates).dispatch(FinishAuctionPhaseAction())
            }
        }
    }

}