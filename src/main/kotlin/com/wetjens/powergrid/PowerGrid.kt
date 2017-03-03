package com.wetjens.powergrid

import com.wetjens.powergrid.map.City
import com.wetjens.powergrid.map.NetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.powerplant.PowerPlantMarket
import com.wetjens.powergrid.resource.ResourceMarkets
import com.wetjens.powergrid.resource.ResourceType
import java.util.*
import kotlin.comparisons.compareBy

data class PowerGrid constructor(
        val map: NetworkMap,
        val cityStates: Map<City, CityState> = map.cities.associate { city -> Pair(city, CityState()) },
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
    constructor(random: Random,
                players: List<Player>,
                map: NetworkMap) : this(
            map = map,
            players = players,
            playerOrder = players.shuffle(random),
            powerPlantMarket = PowerPlantMarket(random, players.size))

    private constructor(map: NetworkMap,
                        players: List<Player>,
                        playerOrder: List<Player>,
                        powerPlantMarket: PowerPlantMarket) : this(
            map = map,
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
        val newPlayerOrder = players.sortedWith(compareBy(
                { player -> numberOfConnectedCities(player) },
                { player ->
                    playerStates[player]!!.highestPowerPlant?.cost ?: throw IllegalStateException("no power plant")
                })).reversed()

        return copy(playerOrder = newPlayerOrder)
    }

    private fun numberOfConnectedCities(player: Player): Int {
        val numberConnected: Int = cityStates.values
                .map { cityState ->
                    cityState.connectedBy.filter { cb ->
                        cb == player
                    }.size
                }
                .reduce { sum, size -> sum + size }

        return numberConnected
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

            val newPlayerState = playerState
                    .pay(cost)
                    .addResource(type, amount)

            val newBuyResourcesPhase = phase.buy(type, amount)

            return copy(phase = newBuyResourcesPhase,
                    resourceMarkets = newBuyResourcesPhase.resourceMarkets,
                    playerStates = playerStates + Pair(phase.currentBuyingPlayer, newPlayerState))
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
    }

    fun passBuyResources(): PowerGrid {
        if (phase is BuyResourcesPhase) {
            return when (phase.buyingPlayers.size) {
                1 -> goToBuildPhase()
                else -> copy(phase = phase.pass())
            }
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
    }

    private fun goToBureaucracyPhase(): PowerGrid {
        return BureaucracyPhase.start(powerGrid = this, nextPhase = PowerGrid::goToAuctionPhase)
    }

    private fun goToAuctionPhase(): PowerGrid {
        return redeterminePlayerOrder()
                .copy(phase = AuctionPhase(biddingOrder = players, auctioningPlayers = playerOrder))
    }

    private fun goToBuildPhase(): PowerGrid {
        return copy(phase = BuildPhase(powerGrid = this,
                buildingPlayers = playerOrder.reversed(),
                nextPhase = PowerGrid::goToBureaucracyPhase))
    }

    fun connectCity(city: City): PowerGrid {
        if (phase is BuildPhase) {
            return phase.connectCity(city)
        } else {
            throw IllegalStateException("not in build phase")
        }
    }

    fun passConnectCity(): PowerGrid {
        if (phase is BuildPhase) {
            return phase.passConnectCity()
        } else {
            throw IllegalStateException("not in build phase")
        }
    }

    fun producePower(powerPlants: Set<PowerPlant>, resources: Map<ResourceType, Int>): PowerGrid {
        if (phase is BureaucracyPhase) {
            return phase.producePower(powerPlants, resources)
        } else {
            throw IllegalStateException("not in bureaucracy phase")
        }
    }

}
