package com.wetjens.powergrid.resource

data class ResourceMarket(val spaces: List<Space>) {

    companion object Factory {
        fun default() = ResourceMarket((1..8).map { n -> Space(capacity = 3, cost = n) })

        fun uranium() = ResourceMarket((1..8).map { n -> Space(capacity = 1, cost = n) }
                + (10..16 step 2).map({ n -> Space(capacity = 1, cost = n) }))
    }

    val capacity: Int by lazy {
        spaces.fold(0, { sum, space -> sum + space.capacity })
    }

    val available: Int by lazy {
        spaces.fold(0, { sum, space -> sum + space.available })
    }

    fun calculateCost(amount: Int): Int {
        val (amountToTake, cost) = spaces.fold(Pair(amount, 0), { state, space ->
            val (amountToTake, cost) = state
            when (amountToTake) {
                0 -> state
                else -> {
                    val take = Math.min(space.available, amountToTake)
                    Pair(amountToTake - take, cost + take * space.cost)
                }
            }
        })

        amountToTake == 0 || throw IllegalArgumentException("not enough available")

        return cost
    }

    operator fun minus(amount: Int): ResourceMarket {
        var amountRemaining = amount

        val newSpaces = spaces.map { space ->
            val amountCanRemoved = Math.min(space.available, amountRemaining)
            amountRemaining -= amountCanRemoved
            space - amountCanRemoved
        }

        amountRemaining == 0 || throw IllegalArgumentException("not enough available")

        return copy(spaces = newSpaces)
    }

    operator fun plus(amount: Int): ResourceMarket {
        var amountRemaining = amount

        val newSpaces = spaces.reversed().map { space ->
            val amountCanAdd = Math.min(space.capacity - space.available, amountRemaining)
            amountRemaining -= amountCanAdd
            space + amountCanAdd
        }

        amountRemaining == 0 || throw IllegalArgumentException("exceeds capacity")

        return copy(spaces = newSpaces.reversed())
    }

    data class Space(val capacity: Int,
                     val cost: Int,
                     val available: Int = 0) {

        operator fun minus(amount: Int): Space = copy(available = Math.max(0, available - amount))

        operator fun plus(amount: Int): Space = copy(available = Math.min(capacity, available + amount))

    }
}

