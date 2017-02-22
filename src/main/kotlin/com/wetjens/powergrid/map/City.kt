package com.wetjens.powergrid.map

/**
 * City on a [NetworkMap] that can be connected by a player.
 */
interface City {

    val name: String
    val area: Area
    val connections: Set<Connection>

}