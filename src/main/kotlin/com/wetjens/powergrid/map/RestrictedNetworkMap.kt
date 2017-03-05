package com.wetjens.powergrid.map

/**
 * Wraps a base map and restricts the areas that can be played on.
 * The resulting map can be used just like a normal map; it will 'see' only the playable areas, cities and connections.
 */
class RestrictedNetworkMap(
        base: NetworkMap,
        areas: Set<Area>) : NetworkMap by base {

    private class PlayableArea(val base: Area) : Area {

        val playableCities: MutableSet<PlayableCity> = mutableSetOf()

        override val name: String = base.name

        override val cities: Set<City> = playableCities

        override fun toString(): String = base.toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return when (other) {
                is PlayableArea -> other.base == base
                is Area -> other == base
                else -> false
            }
        }

        override fun hashCode(): Int {
            return base.hashCode()
        }

    }

    private class PlayableCity(val base: City,
                               override val area: PlayableArea) : City {

        val playableConnections: MutableSet<PlayableConnection> = mutableSetOf()

        override val name: String = base.name

        override val connections: Set<Connection> = playableConnections

        override fun toString(): String = base.toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return when (other) {
                is PlayableCity -> other.base == base
                is City -> other == base
                else -> false
            }
        }

        override fun hashCode(): Int {
            return base.hashCode()
        }
    }

    private class PlayableConnection(val base: Connection,
                                     override val from: PlayableCity,
                                     override val to: PlayableCity) : Connection {

        override val cost: Int = base.cost

        override fun toString(): String = base.toString()

        override fun equals(other: Any?): Boolean {
            if (this === other) return true

            return when (other) {
                is PlayableConnection -> other.base == base
                is Area -> other == base
                else -> false
            }
        }

        override fun hashCode(): Int {
            return base.hashCode()
        }
    }

    private val playableAreas: Set<PlayableArea>
    private val playableCities: Set<PlayableCity>

    init {
        base.areas.containsAll(areas) || throw IllegalArgumentException("base map must contain areas")

        val areasByBase = areas.associate { area -> Pair(area, PlayableArea(area)) }
        playableAreas = areasByBase.values.toSet()

        val citiesByBase = areas
                .flatMap(Area::cities)
                .associate({ city ->
                    val playableArea = areasByBase[city.area]!!
                    val playableCity = PlayableCity(city, playableArea)

                    playableArea.playableCities.add(playableCity)

                    Pair(city, playableCity)
                })

        // wrap the connections as well since they must not point to an non-playable city
        citiesByBase.values.forEach({ playableCity -> restrictConnections(playableCity, citiesByBase) })
        playableCities = citiesByBase.values.toSet()

        // check that areas are connected
        playableAreas.all({ playableArea -> isReachable(playableArea, playableAreas - playableArea) })
                || throw IllegalArgumentException("all areas must be reachable")
    }

    override val areas: Set<Area>
        get() = playableAreas

    override val cities: Set<City>
        get() = playableCities

    private fun isReachable(area: Area, otherAreas: Set<Area>): Boolean {
        return otherAreas.all { otherArea -> area.isReachable(otherArea) }
    }

    private fun restrictConnections(city: PlayableCity, otherCities: Map<City, PlayableCity>) {
        city.base.connections
                .filter({ connection ->
                    otherCities.containsKey(connection.to)
                })
                .mapTo(city.playableConnections, { connection ->
                    PlayableConnection(connection, city, otherCities[connection.to]!!)
                })
    }

}