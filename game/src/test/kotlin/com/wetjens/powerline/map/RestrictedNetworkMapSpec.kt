package com.wetjens.powerline.map

import com.wetjens.powerline.map.yaml.YamlNetworkMap
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.test.fail

object RestrictedNetworkMapSpec : Spek({

    it("should restrict areas") {
        val base = RestrictedNetworkMapSpec::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        val ne = base.areas.find { area -> area.name == "NE" }!!
        val nw = base.areas.find { area -> area.name == "NW" }!!

        val map = RestrictedNetworkMap(base, setOf(ne, nw))

        assertEquals(2, map.areas.size)
        assertEquals(14, map.cities.size)

        // city in one playable area
        val magdeburg = map.cities.find { city -> city.name == "Magdeburg" }!!
        // city in other playable area
        val hannover = map.cities.find { city -> city.name == "Hannover" }!!

        // they should still be connected
        assertTrue(magdeburg.connections.any { connection -> connection.to == hannover })

        // should not be connected any more to cities outside playable area
        assertEquals(3, magdeburg.connections.size)
    }

    it("should not allow unreachable areas") {
        val base = RestrictedNetworkMapSpec::class.java.getResourceAsStream("/maps/germany.yaml")
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

    it("should not allow unreachable area clusters") {
        val base = RestrictedNetworkMapSpec::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        val ne = base.areas.find { area -> area.name == "NE" }!!
        val nw = base.areas.find { area -> area.name == "NW" }!!

        val se = base.areas.find { area -> area.name == "SE" }!!
        val sw = base.areas.find { area -> area.name == "SW" }!!

        try {
            RestrictedNetworkMap(base, setOf(ne, nw, sw, se))
            fail("should throw because not all areas are reachable")
        } catch (e: IllegalArgumentException) {
            assertEquals("all areas must be reachable", e.message)
        }
    }

})