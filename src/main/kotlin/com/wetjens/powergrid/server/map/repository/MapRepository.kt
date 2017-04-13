package com.wetjens.powergrid.server.map

import com.wetjens.powergrid.map.NetworkMap
import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import com.wetjens.powergrid.server.StateController
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Repository
class MapRepository {

    val germany = StateController::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    fun getMaps(): Mono<List<String>> {
        return Mono.just(listOf("germany"))
    }

    fun getMap(name:String): Mono<NetworkMap> {
        return if (name == "germany") Mono.just(germany) else Mono.empty()
    }

}