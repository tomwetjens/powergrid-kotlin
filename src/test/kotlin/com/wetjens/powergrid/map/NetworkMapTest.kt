package com.wetjens.powergrid.map

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class NetworkMapTest {

    val map = RestrictedNetworkMapTest::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val flensburg = map.cities.find { city -> city.name == "Flensburg" }!!
    val muenchen = map.cities.find { city -> city.name == "München" }!!
    val duisburg = map.cities.find { city -> city.name == "Duisburg" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!

    @Test
    fun isReachable() {
        assertTrue(flensburg.isReachable(muenchen))
    }

    @Test
    fun shortestPath() {
        assertEquals(112, map.shortestPath(flensburg, muenchen).cost)
        assertEquals(0, map.shortestPath(duisburg, essen).cost)
    }
}