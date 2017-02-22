package com.wetjens.powergrid

import org.junit.Assert.*
import org.junit.Test

class NetworkMapTest {

    @Test
    fun load() {
        val map = NetworkMapTest::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> NetworkMap.load(inputStream) }

        assertEquals(42, map.cities.size)
        assertEquals(81, map.connections.size)
    }

}