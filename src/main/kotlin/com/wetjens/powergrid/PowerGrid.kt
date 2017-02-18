package com.wetjens.powergrid

import java.util.*

class PowerGrid private constructor(
        val step: Int = 1,
        val round: Int = 1,
        val phase: Phase,
        val playerOrder: List<Player>,
        val playerStates: Map<Player, PlayerState> = playerOrder.associate { player -> Pair(player, PlayerState()) },
        val powerPlantMarket: PowerPlantMarket) {

    /**
     * Initializes a game of Power Grid.
     */
    constructor(random: Random, players: List<Player>) : this(
            playerOrder = players.shuffle(random),
            powerPlantMarket = PowerPlantMarket(random, players.size))

    private constructor(playerOrder: List<Player>, powerPlantMarket: PowerPlantMarket) : this(
            phase = AuctionPhase(auctioningPlayers = playerOrder),
            playerOrder = playerOrder,
            powerPlantMarket = powerPlantMarket)

    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces: PowerPlant?): PowerGrid {
        if (phase is AuctionPhase) {
            val newPowerPlantMarket = powerPlantMarket.take(powerPlant)

            val playerState = playerStates[phase.currentAuctioningPlayer]!!
            playerState.balance >= initialBid || throw IllegalArgumentException("balance too low")

            return if (phase.auctioningPlayers.size == 1) {
                // last player to auction, just buy it
                val newPlayerStates = completePowerPlantPurchase(phase.currentAuctioningPlayer, powerPlant, initialBid, replaces)

                val newPlayerOrder = playerOrder.reversed()
                PowerGrid(step, round, BuyResourcesPhase(newPlayerOrder), playerOrder, newPlayerStates, newPowerPlantMarket)
            } else {
                PowerGrid(step, round, phase.startAuction(powerPlant, initialBid, replaces), playerOrder, playerStates, newPowerPlantMarket)
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun passAuction(): PowerGrid {
        if (phase is AuctionPhase) {
            round > 1 || throw IllegalStateException("cannot pass in first round")

            val newAuctionPhase = phase.passAuction()

            return when (newAuctionPhase.completed) {
                false -> PowerGrid(step, round, newAuctionPhase, playerOrder, playerStates, powerPlantMarket)
                else -> {
                    val newPlayerOrder = playerOrder.reversed()
                    PowerGrid(step, round, BuyResourcesPhase(newPlayerOrder), newPlayerOrder, playerStates, powerPlantMarket)
                }
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun raise(bid: Int): PowerGrid {
        if (phase is AuctionPhase) {
            val playerState = playerStates[phase.auction.currentBiddingPlayer]!!
            playerState.balance >= bid || throw IllegalArgumentException("balance too low")

            return PowerGrid(step, round, phase.raise(bid), playerOrder, playerStates, powerPlantMarket)
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun fold(): PowerGrid {
        if (phase is AuctionPhase) {
            val newAuctionPhase = phase.fold()

            var newPlayerStates = playerStates

            if (phase.auction.biddingPlayers.size == 2) {
                // last player folded

                val winningPlayer = phase.auction.nextBiddingPlayer
                newPlayerStates = completePowerPlantPurchase(winningPlayer, phase.auction.powerPlant, phase.auction.currentBid, phase.auction.replaces)
            }

            return when (newAuctionPhase.completed) {
                false -> PowerGrid(step, round, newAuctionPhase, playerOrder, newPlayerStates, powerPlantMarket)
                else -> {
                    val newPlayerOrder = playerOrder.reversed()
                    PowerGrid(step, round, BuyResourcesPhase(newPlayerOrder), newPlayerOrder, newPlayerStates, powerPlantMarket)
                }
            }
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    private fun completePowerPlantPurchase(player: Player, powerPlant: PowerPlant, bid: Int, replaces:PowerPlant?): Map<Player, PlayerState> {
        val playerState = playerStates[player]
        val newPlayerState = playerState!!.pay(bid).addPowerPlant(powerPlant, replaces)

        return playerStates + Pair(player, newPlayerState)
    }

}
