package com.wetjens.powergrid.map.yaml

import com.wetjens.powergrid.map.City
import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals

object YamlNetworkMapSpec : Spek({

    it("should load from yaml file") {
        val map = YamlNetworkMapSpec::class.java.getResourceAsStream("/maps/germany.yaml")
                .use { inputStream -> YamlNetworkMap.load(inputStream) }

        assertEquals(6, map.areas.size)
        map.areas.forEach { area -> assertEquals(7, area.cities.size) }

        assertEquals(42, map.cities.size)
        assertEquals(162, map.cities.flatMap(City::connections).size)
    }

})