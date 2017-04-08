package com.wetjens.powergrid

/**
 * Phase when game of Power Grid has ended.
 */
data class EndedPhase(val winner: Player,
                      override val currentPlayer: Player = winner) : Phase {

}

