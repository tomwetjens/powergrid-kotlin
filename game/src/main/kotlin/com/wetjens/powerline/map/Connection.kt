package com.wetjens.powerline.map

/**
 * Connection between two [City]s on a [NetworkMap].
 */
interface Connection {

    val from: City
    val to: City
    val cost: Int

}