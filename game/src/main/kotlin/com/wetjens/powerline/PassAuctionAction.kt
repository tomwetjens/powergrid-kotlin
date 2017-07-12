package com.wetjens.powerline

/**
 * Passes to start (if no auction is in progress) for the current player that is up for auction.
 */
class PassAuctionAction : AuctionAction {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            powerGrid.round > 1 || throw IllegalStateException("cannot pass in first round")

            auctionPhase.currentAuction == null || throw IllegalStateException("auction in progress")

            val newAuctioningPlayers = auctionPhase.auctioningPlayers - auctionPhase.currentAuctioningPlayer

            val newAuctionPhase = auctionPhase.copy(
                    auctioningPlayers = newAuctioningPlayers,
                    currentAuctioningPlayer = auctionPhase.nextAuctioningPlayer,
                    currentAuction = null)

            val newPowerGrid = powerGrid.copy(phase = newAuctionPhase)

            return when (newAuctionPhase.completed) {
                false -> newPowerGrid
                else -> newPowerGrid.dispatch(FinishAuctionPhaseAction())
            }
        }
    }

}