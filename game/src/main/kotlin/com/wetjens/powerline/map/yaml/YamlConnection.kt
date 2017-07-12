package com.wetjens.powerline.map.yaml

import com.wetjens.powerline.map.Connection

class YamlConnection() : Connection {

    private object Constants {
        val emptyCity = YamlCity()
    }

    // Need to be var because deserialized from YAML
    override var from: YamlCity = Constants.emptyCity

    override var to: YamlCity = Constants.emptyCity

    override var cost: Int = 0

    constructor(from: YamlCity, to: YamlCity, cost: Int) : this() {
        this.from = from
        this.to = to
        this.cost = cost
    }

    val inverse: YamlConnection by lazy {
        YamlConnection(from = to, to = from, cost = cost)
    }

    override fun toString(): String {
        return "$from->($cost)->$to"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return when (other) {
            is Connection -> to == other.to && from == other.from && cost == other.cost
            else -> false
        }
    }

    override fun hashCode(): Int {
        var result = from.hashCode()
        result = 31 * result + to.hashCode()
        result = 31 * result + cost
        return result
    }

}