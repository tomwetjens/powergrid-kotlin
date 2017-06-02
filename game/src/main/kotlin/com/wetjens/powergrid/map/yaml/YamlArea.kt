package com.wetjens.powergrid.map.yaml

import com.wetjens.powergrid.map.Area

class YamlArea : Area {

    // Need to be var because deserialized from YAML
    override var name: String = ""
    override var cities: MutableSet<YamlCity> = mutableSetOf()

    override fun toString(): String {
        return "$name"
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true

        return when (other) {
            is Area -> name == other.name
            else -> false
        }
    }

    override fun hashCode(): Int {
        return name.hashCode()
    }
}