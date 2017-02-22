package com.wetjens.powergrid

class City {

    var name: String? = null
    var connections: MutableList<Connection> = mutableListOf()

    override fun toString(): String {
        return "$name"
    }
}