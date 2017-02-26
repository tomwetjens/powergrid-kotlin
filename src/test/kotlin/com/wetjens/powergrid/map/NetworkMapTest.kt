package com.wetjens.powergrid.map

import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import org.junit.Test
import kotlin.test.assertTrue

class NetworkMapTest {

    @Test
    fun isReachable() {
        val map = RestrictedNetworkMapTest::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        val flensburg = map.cities.find { city -> city.name == "Flensburg" }!!
        val muenchen = map.cities.find { city -> city.name == "München" }!!

        assertTrue(flensburg.isReachable(muenchen))
    }

}