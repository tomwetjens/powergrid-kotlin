package com.wetjens.powergrid.map.yaml

import com.fasterxml.jackson.annotation.JsonIgnore
import com.wetjens.powergrid.map.NetworkMap
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

/**
 * [NetworkMap] that can be loaded from a YAML file.
 */
class YamlNetworkMap : NetworkMap {

    // Need to be var because deserialized from YAML
    override var areas: Set<YamlArea> = mutableSetOf()

    @get:JsonIgnore
    override var cities: Set<YamlCity> = mutableSetOf()

    @get:JsonIgnore
    var connections: List<YamlConnection> = mutableListOf()

    companion object Factory {

        /**
         * Loads a [NetworkMap] from a YAML file.
         */
        fun load(inputStream: InputStream): NetworkMap {
            val map = Yaml().loadAs<YamlNetworkMap>(inputStream, YamlNetworkMap::class.java)

            map.connections.forEach { connection ->
                connection.from.connections.add(connection)
                connection.to.connections.add(connection.inverse)
            }

            map.cities.forEach { city ->
                city.area.cities.add(city)
            }

            return map
        }
    }
}
