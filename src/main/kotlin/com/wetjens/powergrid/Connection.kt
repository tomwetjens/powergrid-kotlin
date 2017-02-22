package com.wetjens.powergrid

class Connection() {

    var from: City? = null
    var to: City? = null
    var cost: Int? = null

    constructor(from: City?, to: City?, cost: Int?) : this() {
        this.from = from
        this.to = to
        this.cost = cost
    }

    val inverse: Connection by lazy {
        Connection(from = to, to = from, cost = cost)
    }

    override fun toString(): String {
        return "$from->($cost)->$to"
    }
}