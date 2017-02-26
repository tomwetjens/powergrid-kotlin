package com.wetjens.powergrid.map

/**
 * Area on a [NetworkMap] that contains cities.
 */
interface Area {

    val name: String
    val cities: Set<City>

    fun isReachable(other: Area): Boolean {
        return cities.any { city -> other.cities.any { otherCity -> city.isReachable(otherCity) } }
    }

}