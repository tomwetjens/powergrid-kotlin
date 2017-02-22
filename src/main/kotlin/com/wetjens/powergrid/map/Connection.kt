package com.wetjens.powergrid.map

interface Connection {

    val from: City
    val to: City
    val cost: Int

}