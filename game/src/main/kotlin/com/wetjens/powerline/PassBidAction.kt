package com.wetjens.powerline

/**
 * Folds the current player that is bidding in the auction.
 */
class PassBidAction : AuctionAction {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            val auction = auctionPhase.currentAuction!!

            auction.biddingPlayers.size > 1 || throw IllegalStateException("no other players bidding anymore")

            val newAuction = auction.copy(
                    biddingPlayers = auction.biddingPlayers - auction.currentBiddingPlayer,
                    currentBiddingPlayer = auction.nextBiddingPlayer)

            val newAuctionPhase = when (newAuction.closed) {
                true -> auctionPhase.copy(auctioningPlayers = auctionPhase.auctioningPlayers - auction.nextBiddingPlayer,
                        currentAuctioningPlayer = when (auction.nextBiddingPlayer) {
                            auctionPhase.currentAuctioningPlayer -> auctionPhase.nextAuctioningPlayer
                            else -> auctionPhase.currentAuctioningPlayer
                        },
                        closedAuctions = auctionPhase.closedAuctions + auction,
                        currentAuction = null)
                else -> auctionPhase.copy(currentAuction = newAuction)
            }

            var newPlayerStates = powerGrid.playerStates

            if (auction.biddingPlayers.size == 2) {
                // last player folded

                val winningPlayer = auction.nextBiddingPlayer
                newPlayerStates = completePowerPlantPurchase(powerGrid, winningPlayer, auction.powerPlant, auction.currentBid, auction.replaces)
            }

            return when (newAuctionPhase.completed) {
                false -> powerGrid.copy(phase = newAuctionPhase, playerStates = newPlayerStates)
                true -> powerGrid.copy(playerStates = newPlayerStates).dispatch(FinishAuctionPhaseAction())
            }
        }
    }

}