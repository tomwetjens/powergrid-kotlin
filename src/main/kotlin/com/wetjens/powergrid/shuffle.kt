package com.wetjens.powergrid

import java.util.*

/**
 * Returns a list containing the elements of a list in random order.
 *
 * @param random the source of random
 * @return list containing the elements of the original list in random order.
 */
fun <T> List<T>.shuffle(random: Random): List<T> {
    if (size == 1) {
        return this
    }

    val mutableList = toMutableList()

    for (i in size downTo 2) {
        mutableList.swap(i-1, random.nextInt(i))
    }

    return mutableList.toList()
}