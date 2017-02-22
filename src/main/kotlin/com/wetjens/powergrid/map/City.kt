package com.wetjens.powergrid.map

interface City {

    val name: String
    val area: Area
    val connections: Set<Connection>

}