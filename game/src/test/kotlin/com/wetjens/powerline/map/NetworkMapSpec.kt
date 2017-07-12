package com.wetjens.powerline.map

import com.wetjens.powerline.map.yaml.YamlNetworkMap
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals
import kotlin.test.assertTrue

object NetworkMapSpec : Spek({

    val map = RestrictedNetworkMapSpec::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val flensburg = map.cities.find { city -> city.name == "Flensburg" }!!
    val muenchen = map.cities.find { city -> city.name == "München" }!!
    val duisburg = map.cities.find { city -> city.name == "Duisburg" }!!
    val essen = map.cities.find { city -> city.name == "Essen" }!!

    it("should check if city is reachable from other city") {
        assertTrue(flensburg.isReachable(muenchen))
    }

    it("should calculate shortest path between cities") {
        assertEquals(112, map.shortestPath(flensburg, muenchen).cost)
        assertEquals(0, map.shortestPath(duisburg, essen).cost)
    }
})