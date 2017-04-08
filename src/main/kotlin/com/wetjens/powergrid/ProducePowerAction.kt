package com.wetjens.powergrid

import com.wetjens.powergrid.powerplant.PowerPlant
import com.wetjens.powergrid.powerplant.enoughResources
import com.wetjens.powergrid.resource.ResourceType

data class ProducePowerAction(val powerPlants: Set<PowerPlant>,
                              val resources: Map<ResourceType, Int>) : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        return powerGrid.applyWithPhase<BureaucracyPhase> { bureaucracyPhase ->
            val playerState = powerGrid.playerStates[bureaucracyPhase.currentPlayer]!!

            // player must have the power plants
            playerState.powerPlants.containsAll(powerPlants) || throw IllegalArgumentException("player does not have power plants")

            // player must have the resources
            resources.all { entry ->
                val (type, amount) = entry

                amount <= (playerState.resources[type] ?: 0) || throw IllegalArgumentException("player does not have $amount $type")
            }

            powerPlants.enoughResources(resources) || throw IllegalArgumentException("not enough resources")

            val produced = powerPlants.map(PowerPlant::powers).sum()
            val connected = powerGrid.numberOfConnectedCities(bureaucracyPhase.currentPlayer)

            val powers = Math.min(connected, produced)
            val payment = BureaucracyPhase.payments[Math.min(powers, BureaucracyPhase.payments.size - 1)]

            var newPlayerState = playerState.earn(payment)

            resources.forEach { resource ->
                val (type, amount) = resource
                newPlayerState = newPlayerState.removeResource(type, amount)
            }


            val newPowerGrid = powerGrid.copy(
                    playerStates = powerGrid.playerStates + Pair(bureaucracyPhase.currentPlayer, newPlayerState))

            return when (bureaucracyPhase.players.size) {
                1 -> newPowerGrid.dispatch(FinishBureaucracyPhaseAction())
                else -> newPowerGrid.copy(
                        phase = bureaucracyPhase.copy(
                                players = bureaucracyPhase.players - bureaucracyPhase.currentPlayer,
                                currentPlayer = bureaucracyPhase.nextPlayer))
            }
        }
    }
}