package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.resource.ResourceType
import junit.framework.Assert.assertEquals
import org.junit.Test
import java.util.*

class EndedPhaseTest {

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
    fun winnerMostCitiesConnectedAndPowered() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        val endedPhase = powerGrid.copy(
                cityStates = powerGrid.cityStates +
                        Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                        Pair(essen, CityState(connectedBy = setOf(player2))) +
                        Pair(muenster, CityState(connectedBy = setOf(player3))),
                playerStates = powerGrid.playerStates +
                        Pair(player1, PlayerState(
                                powerPlants = listOf(powerGrid.powerPlantMarket.deck.powerPlants[4]!!),
                                resources = mapOf(Pair(ResourceType.COAL, 2))))).dispatch(EndAction()).phase as EndedPhase

        assertEquals(player1, endedPhase.winner)
    }

    @Test
    fun winnerMostCitiesPoweredButNotConnected() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        val endedPhase = powerGrid.copy(
                cityStates = powerGrid.cityStates +
                        Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                        Pair(essen, CityState(connectedBy = setOf(player2))) +
                        Pair(muenster, CityState(connectedBy = setOf(player2))),
                playerStates = powerGrid.playerStates +
                        Pair(player1, PlayerState(
                                powerPlants = listOf(powerGrid.powerPlantMarket.deck.powerPlants[4]!!),
                                resources = mapOf(Pair(ResourceType.COAL, 2))))).dispatch(EndAction()).phase as EndedPhase

        assertEquals(player1, endedPhase.winner)
    }

    @Test
    fun winnerBalance() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        val endedPhase = powerGrid.copy(
                playerStates = powerGrid.playerStates +
                        Pair(player1, PlayerState(balance = 50)) +
                        Pair(player2, PlayerState(balance = 49)) +
                        Pair(player3, PlayerState(balance = 48))).dispatch(EndAction()).phase as EndedPhase

        assertEquals(player1, endedPhase.winner)
    }

    @Test
    fun winnerMostCitiesConnected() {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        val endedPhase = powerGrid.copy(
                cityStates = powerGrid.cityStates +
                        Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                        Pair(essen, CityState(connectedBy = setOf(player1))) +
                        Pair(muenster, CityState(connectedBy = setOf(player2)))).dispatch(EndAction()).phase as EndedPhase

        assertEquals(player1, endedPhase.winner)
    }

}