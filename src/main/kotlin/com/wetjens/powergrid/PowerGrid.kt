package com.wetjens.powergrid

import java.util.*

data class PowerGrid constructor(
        val step: Int = 1,
        val round: Int = 1,
        val phase: Phase,
        val players: List<Player>,
        val playerOrder: List<Player>,
        val playerStates: Map<Player, PlayerState> = players.associate { player -> Pair(player, PlayerState()) },
        val maxOwnedPowerPlants: Int = 3,
        val powerPlantMarket: PowerPlantMarket,
        val resourceMarkets: ResourceMarkets) {

    /**
     * Initializes a game of Power Grid.
     */
    constructor(random: Random, players: List<Player>) : this(
            players = players,
            playerOrder = players.shuffle(random),
            powerPlantMarket = PowerPlantMarket(random, players.size))

    private constructor(players: List<Player>, playerOrder: List<Player>, powerPlantMarket: PowerPlantMarket) : this(
            phase = AuctionPhase(biddingOrder = players, auctioningPlayers = playerOrder),
            players = players,
            playerOrder = playerOrder,
            powerPlantMarket = powerPlantMarket,
            resourceMarkets = ResourceMarkets())

    val currentPlayer: Player
        get() = phase.currentPlayer

    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces: PowerPlant? = null): PowerGrid {
        if (phase is AuctionPhase) {
            checkBid(phase.currentAuctioningPlayer, initialBid, replaces)

            val newPowerPlantMarket = powerPlantMarket - powerPlant

            val newAuctionPhase = phase.startAuction(powerPlant, initialBid, replaces)

            return if (newAuctionPhase.completed) {
                // last player to auction, just buy it
                val newPlayerStates = completePowerPlantPurchase(phase.currentAuctioningPlayer, powerPlant, initialBid, replaces)

                val newPowerGrid = copy(playerStates = newPlayerStates, powerPlantMarket = newPowerPlantMarket)

                when (round) {
                    1 -> newPowerGrid.redeterminePlayerOrder()
                    else -> newPowerGrid
                }.goToBuyResourcesPhase()
            } else {
                copy(phase = newAuctionPhase, powerPlantMarket = newPowerPlantMarket)
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    private fun checkBid(player: Player, bid: Int, replaces: PowerPlant?) {
        val playerState = playerStates[player]!!

        playerState.powerPlants.size < maxOwnedPowerPlants || replaces != null || throw IllegalArgumentException("must replace a power plant")

        playerState.balance >= bid || throw IllegalArgumentException("balance too low")
    }

    private fun goToBuyResourcesPhase(): PowerGrid {
        return copy(phase = BuyResourcesPhase(buyingPlayers = playerOrder.reversed(), resourceMarkets = resourceMarkets))
    }

    private fun redeterminePlayerOrder(): PowerGrid {
        return copy(playerOrder = playerOrder.sortedBy({ player -> playerStates[player]!! }).reversed())
    }

    fun passAuction(): PowerGrid {
        if (phase is AuctionPhase) {
            round > 1 || throw IllegalStateException("cannot pass in first round")

            val newAuctionPhase = phase.passAuction()

            val newPowerGrid = copy(phase = newAuctionPhase)

            return when (newAuctionPhase.completed) {
                false -> newPowerGrid
                else -> {
                    when (newAuctionPhase.closedAuctions.isEmpty()) {
                    // if no power plants are sold in phase, then throw out lowest and replace
                        true -> newPowerGrid.copy(powerPlantMarket = powerPlantMarket - powerPlantMarket.actual[0])
                        false -> newPowerGrid
                    }.goToBuyResourcesPhase()
                }
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun raise(bid: Int, replaces: PowerPlant? = null): PowerGrid {
        if (phase is AuctionPhase) {
            checkBid(phase.auction.currentBiddingPlayer, bid, replaces)

            return copy(phase = phase.raise(bid, replaces))
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun passBid(): PowerGrid {
        if (phase is AuctionPhase) {
            val newAuctionPhase = phase.passBid()

            var newPlayerStates = playerStates

            if (phase.auction.biddingPlayers.size == 2) {
                // last player folded

                val winningPlayer = phase.auction.nextBiddingPlayer
                newPlayerStates = completePowerPlantPurchase(winningPlayer, phase.auction.powerPlant, phase.auction.currentBid, phase.auction.replaces)
            }

            return when (newAuctionPhase.completed) {
                false -> copy(phase = newAuctionPhase, playerStates = newPlayerStates)
                else -> {
                    copy(phase = BuyResourcesPhase(buyingPlayers = playerOrder.reversed(), resourceMarkets = resourceMarkets),
                            playerStates = newPlayerStates)
                }
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    private fun completePowerPlantPurchase(player: Player, powerPlant: PowerPlant, bid: Int, replaces: PowerPlant?): Map<Player, PlayerState> {
        val playerState = playerStates[player]
        val newPlayerState = playerState!!.pay(bid).addPowerPlant(powerPlant, replaces)

        return playerStates + Pair(player, newPlayerState)
    }

    fun buyResources(type: ResourceType, amount: Int): PowerGrid {
        if (phase is BuyResourcesPhase) {
            val cost = resourceMarkets[type].calculateCost(amount)

            val playerState = playerStates[phase.currentBuyingPlayer]!!
            playerState.balance >= cost || throw IllegalArgumentException("balance too low")

            val newBuyResourcesPhase = phase.buy(type, amount)

            return copy(phase = newBuyResourcesPhase,
                    resourceMarkets = newBuyResourcesPhase.resourceMarkets,
                    playerStates = playerStates + Pair(phase.currentBuyingPlayer, playerState.pay(cost)))
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
    }

    fun passBuyResources():PowerGrid {
        if (phase is BuyResourcesPhase) {
            return when (phase.buyingPlayers.size) {
                1 -> goToBuildingPhase()
                else -> copy(phase = phase.pass())
            }
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
    }

    private fun goToBuildingPhase(): PowerGrid {
        // TODO
        return copy()
    }

}
