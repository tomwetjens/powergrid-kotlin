package com.wetjens.powerline.resource

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertEquals

object ResourceMarketsSpec : Spek({

    it("should initialize markets for different resource types") {
        val resourceMarkets = ResourceMarkets()

        assertEquals(24, resourceMarkets[ResourceType.COAL].available)
        assertEquals(18, resourceMarkets[ResourceType.OIL].available)
        assertEquals(6, resourceMarkets[ResourceType.BIO_MASS].available)
        assertEquals(2, resourceMarkets[ResourceType.URANIUM].available)
    }

})