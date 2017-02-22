package com.wetjens.powergrid.map

/**
 * Wraps a base map and restricts the areas that can be played on.
 * The resulting map can be used just like a normal map; it will 'see' only the playable areas, cities and connections.
 */
class RestrictedNetworkMap(
        base: NetworkMap,
        private val playableAreas: Set<Area>) : NetworkMap by base {

    private class PlayableCity(val base: City) : City by base {

        var playableConnections: MutableSet<PlayableConnection> = mutableSetOf()

        override val connections: Set<Connection>
            get() = playableConnections
    }

    private class PlayableConnection(override val from: PlayableCity,
                                     override val to: PlayableCity,
                                     override val cost: Int) : Connection

    init {
        base.areas.containsAll(playableAreas) || throw IllegalArgumentException("base map must contain areas")

        // check that areas are connected
        playableAreas.all({ area -> isReachable(area, playableAreas - area) }) || throw IllegalArgumentException("all areas must be reachable")
    }

    override val areas: Set<Area>
        get() = playableAreas

    override val cities: Set<City> by lazy {
        val cities = playableAreas
                .flatMap(Area::cities)
                .associate({ city -> Pair(city, PlayableCity(city)) })

        // wrap the connections as well since they must not point to an non-playable city
        cities.values.forEach({ city -> restrictConnections(city, cities) })

        cities.values.toSet()
    }

    private fun isReachable(area: Area, otherAreas: Set<Area>): Boolean {
        return area.cities.any { city ->
            otherAreas.any { otherArea ->
                otherArea.cities.any { otherCity ->
                    city.connections.any { connection ->
                        connection.to == otherCity
                    }
                }
            }
        }
    }

    private fun restrictConnections(city: PlayableCity, otherCities: Map<City, PlayableCity>) {
        city.base.connections
                .filter({ connection ->
                    playableAreas.any { playableArea -> playableArea.cities.contains(connection.to) }
                })
                .mapTo(city.playableConnections, { connection ->
                    PlayableConnection(city, otherCities[connection.to]!!, connection.cost)
                })
    }

}