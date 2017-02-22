package com.wetjens.powergrid

import org.yaml.snakeyaml.Yaml
import java.io.InputStream

class NetworkMap {

    var areas: List<Area> = mutableListOf()
    var cities: List<City> = mutableListOf()
    var connections: List<Connection> = mutableListOf()

    companion object Factory {

        fun load(inputStream: InputStream): NetworkMap {
            val map = Yaml().loadAs<NetworkMap>(inputStream, NetworkMap::class.java)

            map.connections.forEach { connection ->
                connection.from!!.connections.add(connection)
                connection.to!!.connections.add(connection.inverse)
            }

            return map
        }
    }
}
