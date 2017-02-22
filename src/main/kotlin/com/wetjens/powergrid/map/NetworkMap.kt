package com.wetjens.powergrid.map

/**
 * Map on which [PowerGrid] can be played.
 */
interface NetworkMap {

    val areas: Set<Area>
    val cities: Set<City>

}