package com.wetjens.powerline.map.yaml

import com.wetjens.powerline.map.City

class YamlCity : City {

    private object Constants {
        val emptyArea = YamlArea()
    }

    // Need to be var because deserialized from YAML
    override var name: String = ""

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