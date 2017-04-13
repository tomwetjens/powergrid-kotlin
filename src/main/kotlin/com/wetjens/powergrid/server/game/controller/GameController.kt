package com.wetjens.powergrid.server

import com.wetjens.powergrid.Player
import com.wetjens.powergrid.PowerGrid
import com.wetjens.powergrid.map.yaml.YamlNetworkMap
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono
import java.util.*

@RestController
@RequestMapping("/state")
class StateController {

    val map = StateController::class.java.getResourceAsStream("/maps/germany.yaml")
            .use { inputStream -> YamlNetworkMap.load(inputStream) }

    val state = PowerGrid(Random(), listOf(Player("Player 1"), Player("Player 2"), Player("Player 3")), map)

    @RequestMapping
    fun getState(): Mono<PowerGrid> {
        return Mono.just(state)
    }

}