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

class BuildPhaseTest {

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

        assertEquals(listOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
        assertEquals(37, powerGrid.playerStates[player3]!!.balance)

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player2, powerGrid.currentPlayer)

        powerGrid = powerGrid.connectCity(essen)
                .passConnectCity()

        assertEquals(listOf(player2), powerGrid.cityStates[essen]!!.connectedBy)
        assertEquals(36, powerGrid.playerStates[player2]!!.balance)

        powerGrid = powerGrid.connectCity(muenster)

        assertEquals(listOf(player1), powerGrid.cityStates[muenster]!!.connectedBy)
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

        assertEquals(listOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
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

        assertEquals(listOf(player3), powerGrid.cityStates[duesseldorf]!!.connectedBy)
        assertEquals(listOf(player3), powerGrid.cityStates[essen]!!.connectedBy)
        assertEquals(listOf(player3), powerGrid.cityStates[muenster]!!.connectedBy)
        assertEquals(9, powerGrid.playerStates[player3]!!.balance)

        assertTrue(powerGrid.phase is BuildPhase)
        assertEquals(player2, powerGrid.currentPlayer)
    }
}