package com.wetjens.powergrid

data class CityState(val connectedBy: Set<Player> = emptySet()) {

    fun connect(player: Player): CityState {
        !connectedBy.contains(player) || throw IllegalStateException("player already connected city")

        return copy(connectedBy = connectedBy + player)
    }

}