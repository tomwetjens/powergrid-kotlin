package com.wetjens.powergrid.server

import com.wetjens.powergrid.map.NetworkMap
import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/map")
class MapController {

    val map = StateController::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    @RequestMapping
    fun getMap(): Mono<NetworkMap> {
        return Mono.just(map)
    }

}