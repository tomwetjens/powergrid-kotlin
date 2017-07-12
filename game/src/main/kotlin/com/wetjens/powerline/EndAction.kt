package com.wetjens.powerline

import kotlin.comparisons.compareBy

/**
 * Determines the winning player and ends the game.
 *
 * The winning player is:
 * - the player that has connected the most cities and can supply power to these cities;
 * - or the player that has highest balance;
 * - or the player that has connected the most cities;
 */
class EndAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        val winner = powerGrid.players.sortedWith(compareBy(
                { player ->
                    val numberOfConnectedCities = powerGrid.numberOfConnectedCities(player)
                    val citiesThatCanPower = powerGrid.playerStates[player]!!.numberOfCitiesCanSupply
                    Math.min(numberOfConnectedCities, citiesThatCanPower)
                },
                { player -> powerGrid.playerStates[player]!!.balance },
                { player -> powerGrid.numberOfConnectedCities(player) }
        )).reversed().first()

        return powerGrid.copy(phase = EndedPhase(winner = winner))
    }
}