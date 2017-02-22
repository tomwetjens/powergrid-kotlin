package com.wetjens.powergrid.map.yaml

import com.wetjens.powergrid.map.Area

class YamlArea : Area {

    override var name: String = ""
    override var cities: MutableSet<YamlCity> = mutableSetOf()

    override fun toString(): String {
        return "$name"
    }

}