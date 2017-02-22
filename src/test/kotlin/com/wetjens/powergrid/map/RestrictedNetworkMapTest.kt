package com.wetjens.powergrid.map

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

class RestrictedNetworkMapTest {

    @Test
    fun areas() {
        val base = RestrictedNetworkMapTest::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        val ne = base.areas.find { area -> area.name == "NE" }!!
        val nw = base.areas.find { area -> area.name == "NW" }!!

        val map = RestrictedNetworkMap(base, setOf(ne, nw))

        assertEquals(setOf(ne, nw), map.areas)
        assertEquals(14, map.cities.size)

        // city in one playable area
        val magdeburg = map.cities.find { city -> city.name == "Magdeburg" }!!
        // city in other playable area
        val hannover = map.cities.find { city -> city.name == "Hannover" }!!

        // they should still be connected
        assertTrue(magdeburg.connections.any { connection -> connection.to == hannover })

        // should not be connected any more to city outside playable area
        assertEquals(3, magdeburg.connections.size)
    }

    @Test
    fun areasUnreachable() {
        val base = RestrictedNetworkMapTest::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        val ne = base.areas.find { area -> area.name == "NE" }!!
        val se = base.areas.find { area -> area.name == "SE" }!!

        try {
            RestrictedNetworkMap(base, setOf(ne, se))
            fail("should throw because not all areas are reachable")
        } catch (e: IllegalArgumentException) {
            assertEquals("all areas must be reachable", e.message)
        }
    }

}