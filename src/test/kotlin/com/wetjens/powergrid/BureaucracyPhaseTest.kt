package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import kotlin.test.fail

class BureaucracyPhaseTest {

    val random: Random = Random(0)

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    val map = PowerGridTest::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val duesseldorf = map.cities.find { city -> city.name == "Düsseldorf" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!
    val muenster = map.cities.find { city -> city.name == "Münster" }!!

    @Test
    fun bureaucracyNoPlayersCanPower() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // player3
                .passBid() // player2
                .passBid() // player1
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // player2
                .passBid() // player 1
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // player1
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()
                .passConnectCity()
                .passConnectCity()
                .passConnectCity()

        assertTrue(powerGrid.phase is AuctionPhase)
        assertEquals(player1, powerGrid.currentPlayer)

        assertEquals(57, powerGrid.playerStates[player3]!!.balance)
        assertEquals(56, powerGrid.playerStates[player2]!!.balance)
        assertEquals(55, powerGrid.playerStates[player1]!!.balance)
    }

    @Test
    fun bureaucracyProduce() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // player3
                .passBid() // player2
                .passBid() // player1
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // player2
                .passBid() // player 1
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // player1
                .buyResources(ResourceType.OIL, 2) // player3
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player2
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player1
                .passBuyResources()
                .connectCity(duesseldorf) // player3
                .passConnectCity()
                .connectCity(essen) // player2
                .passConnectCity()
                .connectCity(muenster) // player1
                .passConnectCity()

        assertTrue(powerGrid.phase is BureaucracyPhase)
        assertEquals(31, powerGrid.playerStates[player3]!!.balance)
        assertEquals(34, powerGrid.playerStates[player2]!!.balance)
        assertEquals(32, powerGrid.playerStates[player1]!!.balance)

        assertEquals(20, powerGrid.resourceMarkets[ResourceType.COAL].available)
        assertEquals(16, powerGrid.resourceMarkets[ResourceType.OIL].available)

        assertEquals(player1, powerGrid.currentPlayer)
        powerGrid = powerGrid.producePower(
                setOf(powerGrid.playerStates[player1]!!.powerPlants[0]),
                mapOf(Pair(ResourceType.COAL, 2)))

        assertEquals(54, powerGrid.playerStates[player1]!!.balance)
        assertEquals(0, powerGrid.playerStates[player1]!!.resources[ResourceType.COAL] ?: 0)

        assertEquals(player2, powerGrid.currentPlayer)
        powerGrid = powerGrid.producePower(
                setOf(powerGrid.playerStates[player2]!!.powerPlants[0]),
                mapOf(Pair(ResourceType.COAL, 2)))

        assertEquals(56, powerGrid.playerStates[player2]!!.balance)
        assertEquals(0, powerGrid.playerStates[player2]!!.resources[ResourceType.COAL] ?: 0)

        assertEquals(player3, powerGrid.currentPlayer)
        powerGrid = powerGrid.producePower(
                setOf(powerGrid.playerStates[player3]!!.powerPlants[0]),
                mapOf(Pair(ResourceType.OIL, 2)))

        assertEquals(53, powerGrid.playerStates[player3]!!.balance)
        assertEquals(0, powerGrid.playerStates[player3]!!.resources[ResourceType.OIL] ?: 0)

        assertTrue(powerGrid.phase is AuctionPhase)
    }

    @Test
    fun bureaucracyNewResourcesAddedToMarket() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // player3
                .passBid() // player2
                .passBid() // player1
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // player2
                .passBid() // player 1
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // player1
                .buyResources(ResourceType.OIL, 2) // player3
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player2
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player1
                .passBuyResources()
                .connectCity(duesseldorf) // player3
                .passConnectCity()
                .connectCity(essen) // player2
                .passConnectCity()
                .connectCity(muenster) // player1
                .passConnectCity()

        powerGrid = powerGrid
                .producePower(
                        setOf(powerGrid.playerStates[player1]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.COAL, 2)))
                .producePower(
                        setOf(powerGrid.playerStates[player2]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.COAL, 2)))
                .producePower(
                        setOf(powerGrid.playerStates[player3]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.OIL, 2)))

        assertTrue(powerGrid.phase is AuctionPhase)

        // new resources placed into market
        assertEquals(24, powerGrid.resourceMarkets[ResourceType.COAL].available)
        assertEquals(18, powerGrid.resourceMarkets[ResourceType.OIL].available)
    }

    @Test
    fun bureaucracyPassProduce() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // player3
                .passBid() // player2
                .passBid() // player1
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // player2
                .passBid() // player 1
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // player1
                .buyResources(ResourceType.OIL, 2) // player3
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player2
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player1
                .passBuyResources()
                .connectCity(duesseldorf) // player3
                .passConnectCity()
                .connectCity(essen) // player2
                .passConnectCity()
                .connectCity(muenster) // player1
                .passConnectCity()

        assertTrue(powerGrid.phase is BureaucracyPhase)
        assertEquals(31, powerGrid.playerStates[player3]!!.balance)
        assertEquals(34, powerGrid.playerStates[player2]!!.balance)
        assertEquals(32, powerGrid.playerStates[player1]!!.balance)

        powerGrid = powerGrid.producePower(emptySet(), emptyMap())
        powerGrid = powerGrid.producePower(emptySet(), emptyMap())
        powerGrid = powerGrid.producePower(emptySet(), emptyMap())

        assertEquals(42, powerGrid.playerStates[player1]!!.balance)
        assertEquals(44, powerGrid.playerStates[player2]!!.balance)
        assertEquals(41, powerGrid.playerStates[player3]!!.balance)

        assertEquals(2, powerGrid.playerStates[player1]!!.resources[ResourceType.COAL] ?: 0)
        assertEquals(2, powerGrid.playerStates[player2]!!.resources[ResourceType.COAL] ?: 0)
        assertEquals(2, powerGrid.playerStates[player3]!!.resources[ResourceType.OIL] ?: 0)
    }

    @Test
    fun bureaucracyRemoveHighestFuture() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3) // player3
                .passBid() // player2
                .passBid() // player1
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4) // player2
                .passBid() // player 1
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5) // player1
                .buyResources(ResourceType.OIL, 2) // player3
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player2
                .passBuyResources()
                .buyResources(ResourceType.COAL, 2) // player1
                .passBuyResources()
                .connectCity(duesseldorf) // player3
                .passConnectCity()
                .connectCity(essen) // player2
                .passConnectCity()
                .connectCity(muenster) // player1
                .passConnectCity()

        powerGrid = powerGrid
                .producePower(
                        setOf(powerGrid.playerStates[player1]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.COAL, 2)))
                .producePower(
                        setOf(powerGrid.playerStates[player2]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.COAL, 2)))
                .producePower(
                        setOf(powerGrid.playerStates[player3]!!.powerPlants[0]),
                        mapOf(Pair(ResourceType.OIL, 2)))

        assertTrue(powerGrid.phase is AuctionPhase)

        // highest power plant from future is removed and added under the pile
        assertEquals(listOf(6, 7, 8, 9), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(listOf(10, 11, 13, 26), powerGrid.powerPlantMarket.future.map(PowerPlant::cost))
        assertEquals(23, powerGrid.powerPlantMarket.deck.remaining)
    }
}