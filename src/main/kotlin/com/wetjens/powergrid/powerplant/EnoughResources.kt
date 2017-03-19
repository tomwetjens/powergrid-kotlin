package com.wetjens.powergrid.powerplant

import com.wetjens.powergrid.resource.ResourceType

/**
 * Calculates if the given resources are enough to run the set of power plants.
 *
 * @param resources resources
 * @return whether the given resources match the amounts required to run all of the power plants
 */
fun Collection<PowerPlant>.enoughResources(resources: Map<ResourceType, Int>): Boolean {
    // must be enough resources to run the power plants
    // start with all power plants still requiring their defined amount of resources
    val stillRequires = mutableMapOf<PowerPlant, Int>()
    associateTo(stillRequires, { powerPlant -> Pair(powerPlant, powerPlant.requires) })

    // spread resources across power plants to optimize
    resources.forEach { resource ->
        var (type, remaining) = resource

        val powerPlantsThatConsume = filter { powerPlant -> powerPlant.consumes.contains(type) }

        // try to fill the non-hybrid power plants first
        val powerPlantsNonHybridFirst = powerPlantsThatConsume.sortedBy { powerPlant -> powerPlant.consumes.size }

        powerPlantsNonHybridFirst.forEach { powerPlant ->
            val requires = stillRequires[powerPlant]!!

            val amount = Math.min(requires, remaining)
            stillRequires[powerPlant] = requires - amount
            remaining -= amount
        }
    }

    return stillRequires.values.all { requires -> requires == 0 }
}