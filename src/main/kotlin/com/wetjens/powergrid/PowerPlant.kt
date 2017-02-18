package com.wetjens.powergrid

data class PowerPlant(
        val cost: Int,
        val consumes: Set<ResourceType>,
        val requires: Int,
        val powers: Int)

