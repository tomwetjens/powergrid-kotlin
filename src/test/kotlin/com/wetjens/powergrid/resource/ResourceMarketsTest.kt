package com.wetjens.powergrid.resource

import org.junit.Test
import kotlin.test.assertEquals

class ResourceMarketsTest {

    @Test
    fun test() {
        val resourceMarkets = ResourceMarkets()

        assertEquals(24, resourceMarkets[ResourceType.COAL].available)
        assertEquals(18, resourceMarkets[ResourceType.OIL].available)
        assertEquals(6, resourceMarkets[ResourceType.BIO_MASS].available)
        assertEquals(2, resourceMarkets[ResourceType.URANIUM].available)
    }

}