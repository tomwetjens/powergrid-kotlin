package com.wetjens.powergrid.map

/**
 * Map on which [PowerGrid] can be played.
 */
interface NetworkMap {

    val areas: Set<Area>
    val cities: Set<City>

    /**
     * Calculates the shortest path (lowest cost) between the given source and target city.
     */
    fun shortestPath(source: City, target: City): Path {
        val unvisited = cities.toMutableSet()

        val dist: MutableMap<City, Int> = mutableMapOf()
        val prev: MutableMap<City, Connection> = mutableMapOf()

        dist[source] = 0

        while (unvisited.isNotEmpty()) {
            val current = unvisited.sortedBy { city -> dist[city] ?: Int.MAX_VALUE }.first()

            if (current == target) {
                // found
                val path: MutableList<Connection> = mutableListOf()

                var from = current
                while (from != source) {
                    val connection = prev[from]!!
                    path.add(connection)
                    from = connection.from
                }

                return Path(path.reversed())
            }

            unvisited -= current

            current.connections.forEach { connection ->
                val alt = dist[current] ?: Int.MAX_VALUE + connection.cost
                if (alt < dist[connection.to] ?: Int.MAX_VALUE) {
                    dist[connection.to] = alt
                    prev[connection.to] = connection
                }
            }
        }

        throw IllegalStateException("no path from $source to $target")
    }

    fun restrict(areas: Set<Area>): NetworkMap {
        return RestrictedNetworkMap(this, areas)
    }

}