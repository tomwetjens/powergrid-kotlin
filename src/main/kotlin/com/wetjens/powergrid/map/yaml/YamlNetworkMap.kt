package com.wetjens.powergrid.map.yaml

import com.wetjens.powergrid.map.NetworkMap
import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class YamlNetworkMap : NetworkMap {

    // Need to be var because deserialized from YAML
    override var areas: Set<YamlArea> = mutableSetOf()
    override var cities: Set<YamlCity> = mutableSetOf()

    var connections: List<YamlConnection> = mutableListOf()

    companion object Factory {

        fun load(inputStream: InputStream): YamlNetworkMap {
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
