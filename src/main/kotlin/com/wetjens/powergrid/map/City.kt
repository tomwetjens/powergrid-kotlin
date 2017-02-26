package com.wetjens.powergrid.map

/**
 * City on a [NetworkMap] that can be connected by a player.
 */
interface City {

    val name: String
    val area: Area
    val connections: Set<Connection>

    fun isReachable(other: City, visited: Set<City> = emptySet()): Boolean {
        val direct = connections.any { connection -> connection.to == other }

        return direct || connections
                .filter { connection -> !visited.contains(connection.to) }
                .any { connection ->
                    connection.to.isReachable(other, visited + this)
                }
    }

}