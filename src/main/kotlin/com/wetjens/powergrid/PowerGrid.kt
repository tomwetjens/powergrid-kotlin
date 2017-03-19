package com.wetjens.powergrid

import com.wetjens.collections.shuffle
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
        val phase: Phase = AuctionPhase(biddingOrder = players, auctioningPlayers = playerOrder),
        val playerStates: Map<Player, PlayerState> = players.associate { player -> Pair(player, PlayerState()) },
        val maxOwnedPowerPlants: Int = when (players.size) {
            2 -> 4
            else -> 3
        },
        val step2StartsOnNumberOfCities: Int = when (players.size) {
            2 -> 10
            6 -> 6
            else -> 7
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

    /**
     * Number of cities connected by the leading player.
     */
    val numberOfCitiesConnectedByLeadingPlayer: Int by lazy {
        playerStates.keys.map({ player -> numberOfConnectedCities(player) }).reduce(Math::max)
    }

    /**
     * Starts a new auction for the current player that is up for auction (if no auction is already in progress), for a given power plant with an initial bid.
     *
     * @param initialBid Must be equal to or greather than the cost of the chosen power plant.
     * @param replaces If player reached max number of power plants that can be owned, this power plant must be replaced with the new power plant.
     */
    fun startAuction(powerPlant: PowerPlant, initialBid: Int, replaces: PowerPlant? = null): PowerGrid {
        return inPhase<AuctionPhase>().startAuction(this, powerPlant, initialBid, replaces)
    }

    fun passAuction(): PowerGrid {
        return inPhase<AuctionPhase>().passAuction(this)
    }

    fun raise(bid: Int, replaces: PowerPlant? = null): PowerGrid {
        return inPhase<AuctionPhase>().raise(this, bid, replaces)
    }

    fun passBid(): PowerGrid {
        return inPhase<AuctionPhase>().passBid(this)
    }

    fun buyResources(type: ResourceType, amount: Int): PowerGrid {
        return inPhase<BuyResourcesPhase>().buy(this, type, amount)
    }

    fun passBuyResources(): PowerGrid {
        return inPhase<BuyResourcesPhase>().pass(this)
    }

    fun connectCity(city: City): PowerGrid {
        return inPhase<BuildPhase>().connectCity(this, city)
    }

    fun passConnectCity(): PowerGrid {
        return inPhase<BuildPhase>().passConnectCity(this)
    }

    fun producePower(powerPlants: Set<PowerPlant>, resources: Map<ResourceType, Int>): PowerGrid {
        return inPhase<BureaucracyPhase>().producePower(this, powerPlants, resources)
    }

    /**
     * Redetermines the player order where the first player is the player that has the highest number of cities connected,
     * then the player that has the highest power plant.
     */
    fun redeterminePlayerOrder(): PowerGrid {
        val newPlayerOrder = players.sortedWith(compareBy(
                { player -> numberOfConnectedCities(player) },
                { player ->
                    playerStates[player]!!.highestPowerPlant?.cost ?: throw IllegalStateException("no power plant")
                })).reversed()

        return copy(playerOrder = newPlayerOrder)
    }

    /**
     * Returns the number of cities that are connected by the given player.
     */
    fun numberOfConnectedCities(player: Player): Int {
        return cityStates.values.filter { cityState -> cityState.connectedBy.contains(player) }.size
    }

    private inline fun <reified T : Phase> inPhase(): T {
        return phase as? T ?: throw IllegalStateException("unexpected phase $phase")
    }

}
