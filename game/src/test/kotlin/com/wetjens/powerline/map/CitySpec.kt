package com.wetjens.powerline.map

import org.jetbrains.spek.api.Spek
import org.jetbrains.spek.api.dsl.it
import kotlin.test.assertTrue

object CitySpec : Spek({

    class TestArea(override val name: String,
                   override val cities: MutableSet<City> = mutableSetOf()) : Area

    class TestCity(override val name: String,
                   override val area: Area,
                   override val connections: MutableSet<Connection> = mutableSetOf()) : City {
        override fun toString(): String {
            return "$name"
        }
    }

    class TestConnection(override val from: City,
                         override val to: City,
                         override val cost: Int = 1) : Connection {
        override fun toString(): String {
            return "$from$to"
        }
    }

    val area = TestArea("1")

    val a = TestCity("A", area)
    val b = TestCity("B", area)
    val c = TestCity("C", area)
    val d = TestCity("D", area)
    val e = TestCity("E", area)

    //      B ---- D ---- E
    //     / \  /
    //   A --- C

    val ab = TestConnection(a, b)
    val ac = TestConnection(a, c)

    val ba = TestConnection(b, a)
    val bc = TestConnection(b, c)
    val bd = TestConnection(b, d)

    val ca = TestConnection(c, a)
    val cb = TestConnection(c, b)
    val cd = TestConnection(c, d)

    val db = TestConnection(d, b)
    val dc = TestConnection(d, c)
    val de = TestConnection(d, e)

    val ed = TestConnection(e, d)

    it("should check if city is reachable") {
        a.connections.add(ab)
        a.connections.add(ac)

        b.connections.add(ba)
        b.connections.add(bc)

        c.connections.add(cb)
        c.connections.add(ca)

        c.connections.add(cd)
        d.connections.add(dc)

        d.connections.add(db)
        b.connections.add(bd)

        d.connections.add(de)
        e.connections.add(ed)

        assertTrue(a.isReachable(e))
    }

})