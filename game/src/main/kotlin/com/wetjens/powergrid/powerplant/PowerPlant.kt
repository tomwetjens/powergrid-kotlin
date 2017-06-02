package com.wetjens.powergrid.powerplant

import com.wetjens.powergrid.resource.ResourceType

data class PowerPlant(
        val cost: Int,
        val consumes: Set<ResourceType>,
        val requires: Int,
        val capacity: Int = requires * 2,
        val powers: Int) {

    override fun toString(): String {
        return "$cost"
    }
}
