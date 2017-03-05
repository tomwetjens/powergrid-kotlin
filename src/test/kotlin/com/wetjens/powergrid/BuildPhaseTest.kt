package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.resource.ResourceType
import org.junit.Test
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class BuildPhaseTest {

    val random: Random = Random(0)

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")
    val player4 = Player(name = "Player 4")
    val player5 = Player(name = "Player 5")
    val player6 = Player(name = "Player 6")

    val players = listOf(player1, player2, player3)

    val map = PowerGridTest::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val duesseldorf = map.cities.find { city -> city.name == "Düsseldorf" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!
    val muenster = map.cities.find { city -> city.name == "Münster" }!!

    @Test
    fun buildConnectStartingCity() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player3, powerGrid.currentPlayer)

        powerGrid = powerGrid.connectCity(duesseldorf)
                .passConnectCity()

        assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
        assertEquals(37, powerGrid.playerStates[player3]!!.balance)

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player2, powerGrid.currentPlayer)

        powerGrid = powerGrid.connectCity(essen)
                .passConnectCity()

        assertEquals(setOf(player2), powerGrid.cityStates[essen]!!.connectedBy)
        assertEquals(36, powerGrid.playerStates[player2]!!.balance)

        powerGrid = powerGrid.connectCity(muenster)

        assertEquals(setOf(player1), powerGrid.cityStates[muenster]!!.connectedBy)
        assertEquals(35, powerGrid.playerStates[player1]!!.balance)
    }

    @Test
    fun buildCannotConnectCityAlreadyConnectedInSameStep() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player3, powerGrid.currentPlayer)

        powerGrid = powerGrid.connectCity(duesseldorf)
                .passConnectCity()

        assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
        assertEquals(37, powerGrid.playerStates[player3]!!.balance)

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player2, powerGrid.currentPlayer)

        try {
            powerGrid.connectCity(duesseldorf)
            fail("should throw because city reached max connections")
        } catch (e: IllegalStateException) {
            assertEquals("reached max connections", e.message)
        }
    }

    @Test
    fun buildConnectCityMustBeConnectedToAlreadyConnectedCities() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player3, powerGrid.currentPlayer)
        assertEquals(47, powerGrid.playerStates[player3]!!.balance)

        powerGrid = powerGrid
                .connectCity(duesseldorf)
                .connectCity(muenster)

        assertEquals(19, powerGrid.playerStates[player3]!!.balance)
    }

    @Test
    fun buildConnectMultipleCities() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player3, powerGrid.currentPlayer)
        assertEquals(47, powerGrid.playerStates[player3]!!.balance)

        powerGrid = powerGrid.connectCity(duesseldorf)
                .connectCity(essen)
                .connectCity(muenster)
                .passConnectCity()

        assertEquals(setOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
        assertEquals(setOf(player3), powerGrid.cityStates[essen]!!.connectedBy)
        assertEquals(setOf(player3), powerGrid.cityStates[muenster]!!.connectedBy)
        assertEquals(9, powerGrid.playerStates[player3]!!.balance)

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player2, powerGrid.currentPlayer)
    }

    @Test
    fun step2AfterPlayerConnects7thCity() {
        var powerGrid = PowerGrid(random = random, players = players, map = map).copy(playerStates = mapOf(
                Pair(player1, PlayerState(balance = 9999)),
                Pair(player2, PlayerState(balance = 9999)),
                Pair(player3, PlayerState(balance = 9999))
        ))

        val cities = map.cities.toList()

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[1], 4)
                .passBid()
                .startAuction(powerGrid.powerPlantMarket.actual[2], 5)
                .buyResources(ResourceType.OIL, 2)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        // player connects 7 cities
        (0..6).forEach { i -> powerGrid = powerGrid.connectCity(cities[i]) }

        // should still be in step 1
        assertEquals(1, powerGrid.step)

        powerGrid = powerGrid.passConnectCity()
        // other player connects 9 cities
        (7..15).forEach { i -> powerGrid = powerGrid.connectCity(cities[i]) }

        // should still be in step 1
        assertEquals(1, powerGrid.step)

        powerGrid = powerGrid.passConnectCity()
                .passConnectCity()

        // should be in step 2
        assertEquals(2, powerGrid.step)
        assertTrue(powerGrid.phase is BureaucracyPhase)

        // lowest power plant should be removed and also the all power plants lower than 9
        assertEquals(listOf(10, 11, 13, 21), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(19, powerGrid.powerPlantMarket.deck.remaining)
    }

    @Test
    fun step2AfterPlayerConnects6thCityWhen6Players() {
        var powerGrid = PowerGrid(
                random = random,
                players = listOf(player1, player2, player3, player4, player5, player6),
                map = map.restrict(setOf(
                        map.areas.find { area -> area.name == "NE" }!!,
                        map.areas.find { area -> area.name == "NW" }!!,
                        map.areas.find { area -> area.name == "W" }!!,
                        map.areas.find { area -> area.name == "SE" }!!,
                        map.areas.find { area -> area.name == "SW" }!!
                ))).copy(
                playerStates = mapOf(
                        Pair(player1, PlayerState(balance = 9999)),
                        Pair(player2, PlayerState(balance = 9999)),
                        Pair(player3, PlayerState(balance = 9999)),
                        Pair(player4, PlayerState(balance = 9999)),
                        Pair(player5, PlayerState(balance = 9999)),
                        Pair(player6, PlayerState(balance = 9999))
                ))

        val cities = map.cities.toList()

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
                .passBid()
                .passBid()
                .passBid()
                .passBid()
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4)
                .passBid()
                .passBid()
                .passBid()
                .passBid()
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 5)
                .passBid()
                .passBid()
                .passBid()
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 6)
                .passBid()
                .passBid()
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 7)
                .passBid()

        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 8)
                .buyResources(ResourceType.OIL, 2)
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()
                .passBuyResources()

        // player connects 6 cities
        (0..5).forEach { i -> powerGrid = powerGrid.connectCity(cities[i]) }

        // should still be in step 1
        assertEquals(1, powerGrid.step)

        powerGrid = powerGrid.passConnectCity()
                .passConnectCity()
                .passConnectCity()
                .passConnectCity()
                .passConnectCity()
                .passConnectCity()

        // should be in step 2
        assertEquals(2, powerGrid.step)
        assertTrue(powerGrid.phase is BureaucracyPhase)

        // lowest power plant should be removed
        assertEquals(listOf(10, 13, 16, 22), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(27, powerGrid.powerPlantMarket.deck.remaining)
    }

    @Test
    fun step2AfterPlayerConnects6thCityWhen2Players() {
        var powerGrid = PowerGrid(
                random = random,
                players = listOf(player1, player2),
                map = map.restrict(setOf(
                        map.areas.find { area -> area.name == "NE" }!!,
                        map.areas.find { area -> area.name == "NW" }!!,
                        map.areas.find { area -> area.name == "W" }!!
                ))).copy(
                playerStates = mapOf(
                        Pair(player1, PlayerState(balance = 9999)),
                        Pair(player2, PlayerState(balance = 9999))
                ))

        val cities = map.cities.toList()

        powerGrid = powerGrid
                .startAuction(powerGrid.powerPlantMarket.actual[0], 3)
                .passBid()
        powerGrid = powerGrid.startAuction(powerGrid.powerPlantMarket.actual[0], 4)
                .buyResources(ResourceType.OIL, 2)
                .passBuyResources()
                .passBuyResources()

        // player connects 10 cities
        (0..9).forEach { i -> powerGrid = powerGrid.connectCity(cities[i]) }

        // should still be in step 1
        assertEquals(1, powerGrid.step)

        powerGrid = powerGrid.passConnectCity()
                .passConnectCity()

        // should be in step 2
        assertEquals(2, powerGrid.step)
        assertTrue(powerGrid.phase is BureaucracyPhase)

        // lowest power plant should be removed
        assertEquals(listOf(12, 13, 14, 20), powerGrid.powerPlantMarket.actual.map(PowerPlant::cost))
        assertEquals(18, powerGrid.powerPlantMarket.deck.remaining)
    }
}