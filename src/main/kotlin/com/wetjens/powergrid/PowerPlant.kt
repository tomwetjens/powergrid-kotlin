package com.wetjens.powergrid

data class PowerPlant(
        val cost: Int,
        val consumes: Set<ResourceType>,
        val requires: Int,
        val capacity: Int = requires * 2,
        val powers: Int)

