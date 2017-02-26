package com.wetjens.powergrid

data class CityState(val connectedBy: List<Player> = emptyList()) {

    fun connect(player: Player): CityState {
        return copy(connectedBy = connectedBy + player)
    }

}