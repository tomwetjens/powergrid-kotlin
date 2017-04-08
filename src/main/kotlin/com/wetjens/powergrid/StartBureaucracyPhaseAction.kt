package com.wetjens.powergrid

class StartBureaucracyPhaseAction : Action {

    override fun apply(powerGrid: PowerGrid): PowerGrid {
        val playersThatCanPower = powerGrid.players
                .filter { player ->
                    val playerState = powerGrid.playerStates[player]!!

                    // has enough resources to run any power plant?
                    playerState.powerPlants.any { powerPlant ->
                        val available = playerState.storage[powerPlant]!!
                                .filterKeys { type -> powerPlant.consumes.contains(type) }
                                .values
                                .sum()

                        powerPlant.requires <= available
                    }
                }
                .filter { player ->
                    // has any connected cities?
                    powerGrid.cityStates.values.any { cityState -> cityState.connectedBy.contains(player) }
                }

        var newPlayerStates = powerGrid.playerStates

        // give all players that cannot power anyway already the minimum payment
        (powerGrid.players - playersThatCanPower).forEach { player ->
            val playerState = powerGrid.playerStates[player]!!
            val newPlayerState = playerState.earn(BureaucracyPhase.payments[0])

            newPlayerStates += Pair(player, newPlayerState)
        }

        val newPowerGrid = powerGrid.copy(playerStates = newPlayerStates)

        // go into bureaucracy phase with players that could produce
        return when (playersThatCanPower.isEmpty()) {
            true -> newPowerGrid.dispatch(FinishBureaucracyPhaseAction())
            false -> newPowerGrid.copy(
                    phase = BureaucracyPhase(players = playersThatCanPower))
        }
    }

}