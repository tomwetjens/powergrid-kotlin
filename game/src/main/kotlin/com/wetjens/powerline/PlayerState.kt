package com.wetjens.powerline

import com.wetjens.collections.combinations
import com.wetjens.powerline.powerplant.PowerPlant
import com.wetjens.powerline.powerplant.enoughResources
import com.wetjens.powerline.resource.ResourceType

data class PlayerState(val balance: Int = 50,
                       val powerPlants: List<PowerPlant> = emptyList(),
                       val resources: Map<ResourceType, Int> = emptyMap()) {

    fun pay(amount: Int): PlayerState {
        amount <= balance || throw IllegalArgumentException("balance too low")

        return copy(balance = balance - amount)
    }

    fun addPowerPlant(powerPlant: PowerPlant, replaces: PowerPlant?): PlayerState {
        val newPowerPlants = when (replaces) {
            null -> powerPlants + powerPlant
            else -> (powerPlants - replaces) + powerPlant
        }.sortedBy(PowerPlant::cost)

        return copy(powerPlants = newPowerPlants)
    }

    val highestPowerPlant: PowerPlant? = powerPlants.lastOrNull()

    /**
     * Amount of resources that could be stored theoretically on all the power plants combined.
     */
    val storageCapacity: Map<ResourceType, Int> by lazy {
        ResourceType.values().associate { type ->
            Pair(type, powerPlants.filter { powerPlant -> powerPlant.consumes.contains(type) }
                    .fold(0, { sum, powerPlant -> sum + powerPlant.capacity }))
        }
    }

    /**
     * Amount of resources that could be added to the storage theoretically on all power plants combined.
     */
    val storageAvailable: Map<ResourceType, Int> by lazy {
        ResourceType.values().associate { type ->
            Pair(type, powerPlantsByResourceType[type]?.fold(0, { available, powerPlant ->
                available + (powerPlant.capacity - storage[powerPlant]!!.values.fold(0, { sum, amount -> sum + amount }))
            }) ?: 0)
        }
    }

    private val powerPlantsByResourceType: Map<ResourceType, List<PowerPlant>> by lazy {
        // group power plants by what they consume, sorted by number of different types they consume (so non-hybrid first)
        powerPlants.flatMap { powerPlant ->
            powerPlant.consumes.map { type ->
                Pair(type, powerPlant)
            }
        }.groupBy({ pair -> pair.first }, { pair -> pair.second })
    }

    /**
     * Resources that are stored in the power plants.
     */
    val storage: Map<PowerPlant, Map<ResourceType, Int>> by lazy {
        // start with all power plants empty
        val storage = mutableMapOf<PowerPlant, MutableMap<ResourceType, Int>>()
        powerPlants.associateTo(storage, { powerPlant -> Pair(powerPlant, mutableMapOf()) })

        // spread resources across power plants to optimize storage
        resources.forEach { resource ->
            var (type, remaining) = resource

            val powerPlantsThatCouldStore = powerPlantsByResourceType[type] ?: throw IllegalStateException("no power plants that could store $type")

            // try to fill the non-hybrid power plants first
            val powerPlantsNonHybridFirst = powerPlantsThatCouldStore.sortedBy { powerPlant -> powerPlant.consumes.size }

            var powerPlantIndex = 0
            while (remaining > 0) {
                powerPlantIndex < powerPlantsNonHybridFirst.size || throw IllegalStateException("no power plants left to store $remaining $type")
                val powerPlant = powerPlantsNonHybridFirst[powerPlantIndex++]

                val current = storage[powerPlant]!![type] ?: 0

                val available = powerPlant.capacity - current
                val amount = Math.min(available, remaining)

                storage[powerPlant]!![type] = current + amount
                remaining -= amount
            }
        }

        storage
    }

    fun addResource(type: ResourceType, amount: Int): PlayerState {
        amount <= storageAvailable[type]!! || throw IllegalArgumentException("max storage exceeded")

        return copy(resources = resources + Pair(type, resources[type] ?: 0 + amount))
    }

    fun earn(amount: Int): PlayerState {
        amount > 0 || throw IllegalArgumentException("amount too low")

        return copy(balance = balance + amount)
    }

    fun removeResource(type: ResourceType, amount: Int): PlayerState {
        amount <= resources[type] ?: 0 || throw IllegalArgumentException("not enough resources")

        return copy(resources = resources + Pair(type, resources[type]!! - amount))
    }

    /**
     * Number of cities that, given the power plants and resources, this player can supply power to.
     */
    val numberOfCitiesCanSupply: Int by lazy {
        powerPlants
                .combinations()
                .filter { combination -> combination.enoughResources(resources) }
                .map { combination -> combination.map(PowerPlant::powers).sum() }
                .max() ?: 0
    }

}