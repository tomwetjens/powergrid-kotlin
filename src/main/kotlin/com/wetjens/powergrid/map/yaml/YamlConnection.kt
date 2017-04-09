package com.wetjens.powergrid.map.yaml

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonProperty
import com.wetjens.powergrid.map.Connection

class YamlConnection() : Connection {

    private object Constants {
        val emptyCity = YamlCity()
    }

    // Need to be var because deserialized from YAML
    @get:JsonIgnore
    override var from: YamlCity = Constants.emptyCity

    @get:JsonIgnoreProperties("connections")
    override var to: YamlCity = Constants.emptyCity

    override var cost: Int = 0

    constructor(from: YamlCity, to: YamlCity, cost: Int) : this() {
        this.from = from
        this.to = to
        this.cost = cost
    }

    @get:JsonIgnore
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