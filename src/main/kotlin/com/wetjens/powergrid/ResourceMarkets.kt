package com.wetjens.powergrid

data class ResourceMarkets(val markets: Map<ResourceType, ResourceMarket> = mapOf(
        Pair(ResourceType.COAL, ResourceMarket.default() + 24),
        Pair(ResourceType.OIL, ResourceMarket.default() + 18),
        Pair(ResourceType.BIO_MASS, ResourceMarket.default() + 6),
        Pair(ResourceType.URANIUM, ResourceMarket.uranium() + 2))) {

    operator fun get(type: ResourceType): ResourceMarket {
        val market = markets[type]
        market != null || throw IllegalArgumentException("no market for $type")
        return market!!
    }

    operator fun minus(resource: Resource): ResourceMarkets {
        return copy(markets = markets + Pair(resource.type, this[resource.type] - resource.amount))
    }

    operator fun plus(resource: Resource): ResourceMarkets {
        return copy(markets = markets + Pair(resource.type, this[resource.type] + resource.amount))
    }

}