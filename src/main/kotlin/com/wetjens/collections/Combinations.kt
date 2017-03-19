package com.wetjens.collections

/**
 * Returns a set of unique combinations of elements in the collection.
 *
 * @param min Minimum number of elements in each combination.
 * @param max Maximum number of elements in each combination.
 * @return unique combinations of elements in the collection that match the given size constraints.
 */
fun <T> Collection<T>.combinations(min: Int, max: Int): Set<Set<T>> {
    min > 0 || throw IllegalArgumentException("min must be greater than 0")
    min <= max || throw IllegalArgumentException("min must less than or equal to max")

    return combinations().filter { c -> c.size in min..max }.toSet()
}

/**
 * Returns a set of all unique combinations of elements in the collection.
 *
 * @return all unique combinations of elements in the collection.
 */
fun <T> Collection<T>.combinations(): Set<Set<T>> {
    return when (size) {
        1 -> setOf(this.toSet())
        else -> flatMap { elem ->
            val cs = minus(elem).combinations()
            (cs + cs.map { c -> setOf(elem) + c }).toSet()
        }.toSet()
    }
}