package com.wetjens.powergrid

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.resource.ResourceType
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.describe
import org.jetbrains.spek.api.dsl.given
import org.jetbrains.spek.api.dsl.it
import java.util.*
import kotlin.test.assertEquals

object EndedPhaseSpec : Spek({

    val random: Random = Random(0)

    val player1 = Player(name = "Player 1")
    val player2 = Player(name = "Player 2")
    val player3 = Player(name = "Player 3")

    val players = listOf(player1, player2, player3)

    val map = EndedPhaseSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val duesseldorf = map.cities.find { city -> city.name == "Düsseldorf" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!
    val muenster = map.cities.find { city -> city.name == "Münster" }!!

    describe("PowerGrid") {
        var powerGrid = PowerGrid(random = random, players = players, map = map)

        given("ended and player has most cities connected and powered") {
            powerGrid = powerGrid.copy(
                    cityStates = powerGrid.cityStates +
                            Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                            Pair(essen, CityState(connectedBy = setOf(player2))) +
                            Pair(muenster, CityState(connectedBy = setOf(player3))),
                    playerStates = powerGrid.playerStates +
                            Pair(player1, PlayerState(
                                    powerPlants = listOf(powerGrid.powerPlantMarket.deck.powerPlants[4]!!),
                                    resources = mapOf(Pair(ResourceType.COAL, 2))))).dispatch(EndAction())
            it("should return winner") {
                assertEquals(player1, (powerGrid.phase as EndedPhase).winner)
            }
        }

        given("ended and player powers most cities") {
            powerGrid = powerGrid.copy(
                    cityStates = powerGrid.cityStates +
                            Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                            Pair(essen, CityState(connectedBy = setOf(player2))) +
                            Pair(muenster, CityState(connectedBy = setOf(player2))),
                    playerStates = powerGrid.playerStates +
                            Pair(player1, PlayerState(
                                    powerPlants = listOf(powerGrid.powerPlantMarket.deck.powerPlants[4]!!),
                                    resources = mapOf(Pair(ResourceType.COAL, 2))))).dispatch(EndAction())

            it("should return winner") {
                assertEquals(player1, (powerGrid.phase as EndedPhase).winner)
            }
        }

        given("ended and player has highest balance") {
            powerGrid = powerGrid.copy(
                    playerStates = powerGrid.playerStates +
                            Pair(player1, PlayerState(balance = 50)) +
                            Pair(player2, PlayerState(balance = 49)) +
                            Pair(player3, PlayerState(balance = 48))).dispatch(EndAction())

            it("should return winner") {
                assertEquals(player1, (powerGrid.phase as EndedPhase).winner)
            }
        }

        given("ended and player has most cities connected but none powered") {
            powerGrid = powerGrid.copy(
                    cityStates = powerGrid.cityStates +
                            Pair(duesseldorf, CityState(connectedBy = setOf(player1))) +
                            Pair(essen, CityState(connectedBy = setOf(player1))) +
                            Pair(muenster, CityState(connectedBy = setOf(player2)))).dispatch(EndAction())

            it("should return winner") {
                assertEquals(player1, (powerGrid.phase as EndedPhase).winner)
            }
        }
    }
})