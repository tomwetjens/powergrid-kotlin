package com.wetjens.powergrid.map

/**
 * Area on a [NetworkMap] that contains cities.
 */
interface Area {

    val name: String
    val cities: Set<City>

}