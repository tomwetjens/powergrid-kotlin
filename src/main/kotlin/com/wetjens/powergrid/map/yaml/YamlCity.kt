package com.wetjens.powergrid.map.yaml

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wetjens.powergrid.map.City

class YamlCity : City {

    private object Constants {
        val emptyArea = YamlArea()
    }

    // Need to be var because deserialized from YAML
    override var name: String = ""

    @get:JsonIgnore
    override var area: YamlArea = Constants.emptyArea

    override var connections: MutableSet<YamlConnection> = mutableSetOf()

    override fun toString(): String {
        return "$name"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return when (other) {
            is City -> name == other.name
            else -> false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }

}