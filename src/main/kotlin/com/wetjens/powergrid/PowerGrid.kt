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
        val players: List<Player>,
        val playerOrder: List<Player>,
        val phase: Phase = AuctionPhase(biddingOrder = players, auctioningPlayers = playerOrder, nextPhase = PowerGrid::goToBuyResourcesPhase),
        val playerStates: Map<Player, PlayerState> = players.associate { player -> Pair(player, PlayerState()) },
        val maxOwnedPowerPlants: Int = when (players.size) {
            2 -> 4
            else -> 3
        },
        val gameEndsOnNumberOfCities: Int = when (players.size) {
            2 -> 21
            5 -> 15
            6 -> 14
            else -> 17
        },
        val powerPlantMarket: PowerPlantMarket,
        val resourceMarkets: ResourceMarkets = ResourceMarkets()) {

    init {
        players.size <= 6 || throw IllegalArgumentException("too many players")

        val playAreas = when (players.size) {
            2 -> 3
            6 -> 5
            else -> 6
        }

        map.areas.size == playAreas || throw IllegalArgumentException("must play $playAreas areas")
    }

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

    val currentPlayer: Player
        get() = phase.currentPlayer

    val leadingPlayerNumberOfCitiesConnected: Int by lazy {
        playerStates.keys
                .map({ player ->
                    cityStates.values
                            .fold(0, { sum, cs ->
                                sum + cs.connectedBy.filter { cb -> cb == player }.size
                            })
                })
                .reduce(Math::max)
    }

    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces: PowerPlant? = null): PowerGrid {
        if (phase is AuctionPhase) {
            return phase.startAuction(this, powerPlant, initialBid, replaces)
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun passAuction(): PowerGrid {
        if (phase is AuctionPhase) {
            return phase.passAuction(this)
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun raise(bid: Int, replaces: PowerPlant? = null): PowerGrid {
        if (phase is AuctionPhase) {
            return phase.raise(this, bid, replaces)
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun passBid(): PowerGrid {
        if (phase is AuctionPhase) {
            return phase.passBid(this)
        } else {
            throw IllegalStateException("not in auction phase")
        }
    }

    fun buyResources(type: ResourceType, amount: Int): PowerGrid {
        if (phase is BuyResourcesPhase) {
            return phase.buy(this, type, amount)
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
    }

    fun passBuyResources(): PowerGrid {
        if (phase is BuyResourcesPhase) {
            return phase.pass(this)
        } else {
            throw IllegalStateException("not in buy resources phase")
        }
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

    fun redeterminePlayerOrder(): PowerGrid {
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

    private fun goToAuctionPhase(): PowerGrid {
        return redeterminePlayerOrder().copy(
                phase = AuctionPhase(
                        biddingOrder = players,
                        auctioningPlayers = playerOrder,
                        nextPhase = PowerGrid::goToBuyResourcesPhase))
    }

    private fun goToBuyResourcesPhase(): PowerGrid {
        return copy(phase = BuyResourcesPhase(
                buyingPlayers = playerOrder.reversed(),
                nextPhase = PowerGrid::goToBuildPhase))
    }

    private fun goToBuildPhase(): PowerGrid {
        return copy(phase = BuildPhase(powerGrid = this,
                buildingPlayers = playerOrder.reversed(),
                nextPhase = PowerGrid::goToBureaucracyPhase))
    }

    private fun goToBureaucracyPhase(): PowerGrid {
        return BureaucracyPhase.start(powerGrid = this, nextPhase = PowerGrid::goToAuctionPhase)
    }
}
