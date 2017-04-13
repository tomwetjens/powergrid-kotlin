package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant

/**
 * Starts a new auction for the current player that is up for auction (if no auction is already in progress),
 * for a given power plant with an initial bid.
 *
 * @param initialBid Must be equal to or greather than the cost of the chosen power plant.
 * @param replaces If player reached max number of power plants that can be owned, this power plant must be replaced with the new power plant.
 */
data class StartAuctionAction(val powerPlant: PowerPlant,
                         val initialBid: Int,
                         val replaces: PowerPlant? = null) : AuctionAction {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<AuctionPhase> { auctionPhase ->
            auctionPhase.currentAuction == null || throw IllegalStateException("auction in progress")

            checkBid(powerGrid, auctionPhase.currentAuctioningPlayer, initialBid, replaces)

            val newPowerPlantMarket = (powerGrid.powerPlantMarket - powerPlant)
                    .removeLowerOrEqual(powerGrid.numberOfCitiesConnectedByLeadingPlayer)

            initialBid >= powerPlant.cost || throw IllegalArgumentException("bid must be >= ${powerPlant.cost}")

            val biddingPlayers = powerGrid.players.filter({ player -> auctionPhase.auctioningPlayers.contains(player) })
            // get next player clockwise from current auctioning player
            val biddingPlayer = biddingPlayers[(biddingPlayers.indexOf(auctionPhase.currentAuctioningPlayer) + 1) % biddingPlayers.size]

            val newAuction = Auction(
                    biddingPlayers = biddingPlayers,
                    currentBiddingPlayer = biddingPlayer,
                    powerPlant = powerPlant,
                    replaces = replaces,
                    currentBid = initialBid)

            val newAuctionPhase = when (auctionPhase.auctioningPlayers.size) {
                1 -> auctionPhase.copy(auctioningPlayers = emptyList(), closedAuctions = auctionPhase.closedAuctions + newAuction)
                else -> auctionPhase.copy(currentAuction = newAuction)
            }

            return if (newAuctionPhase.completed) {
                // last player to auction, just buy it
                val newPlayerStates = completePowerPlantPurchase(powerGrid, auctionPhase.currentAuctioningPlayer, powerPlant, initialBid, replaces)


                powerGrid.copy(
                        playerStates = newPlayerStates,
                        powerPlantMarket = newPowerPlantMarket).dispatch(FinishAuctionPhaseAction())
            } else {
                powerGrid.copy(phase = newAuctionPhase, powerPlantMarket = newPowerPlantMarket)
            }
        }
    }

}