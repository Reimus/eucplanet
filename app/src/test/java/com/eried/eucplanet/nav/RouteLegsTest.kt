package com.eried.eucplanet.nav

import com.eried.eucplanet.data.model.GeoPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class RouteLegsTest {

    // A square loop, about 1.1 km a side at this latitude, that ends where it began.
    private val a = GeoPoint(60.0, 10.0)
    private val b = GeoPoint(60.01, 10.0)
    private val c = GeoPoint(60.01, 10.02)
    private val d = GeoPoint(60.0, 10.02)

    private fun line(vararg pts: GeoPoint): List<GeoPoint> =
        pts.toList().zipWithNext { p, q -> (0..9).map { t -> GeoPoint(p.lat + (q.lat - p.lat) * t / 10, p.lng + (q.lng - p.lng) * t / 10) } }
            .flatten() + pts.last()

    @Test fun `legs follow the path and add up to it`() {
        val path = line(a, b, c, d, a)
        val legs = RouteLegs.metres(listOf(a, b, c, d, a), path)
        assertEquals(4, legs.size)
        assertEquals(GeoMath.polylineLengthM(path), legs.sum(), 1.0)
        assertEquals(GeoMath.distanceM(a, b), legs[0], 5.0)
        assertEquals(GeoMath.distanceM(b, c), legs[1], 5.0)
    }

    @Test fun `a loop's last stop is not found back at its start`() {
        val path = line(a, b, c, d, a)
        val legs = RouteLegs.metres(listOf(a, c, a), path)
        // Half the loop each way, not the whole loop and then nothing.
        assertEquals(legs[0], legs[1], 5.0)
    }

    @Test fun `a path that starts at the rider does not count the ride to the first stop`() {
        val rider = GeoPoint(59.99, 10.0)
        val path = line(rider, a, b, c)
        val legs = RouteLegs.metres(listOf(a, b, c), path)
        assertEquals(GeoMath.distanceM(a, b), legs[0], 5.0)
        assertEquals(GeoMath.distanceM(b, c), legs[1], 5.0)
    }

    @Test fun `without a path the legs are straight lines`() {
        val legs = RouteLegs.metres(listOf(a, b, c), emptyList())
        assertEquals(listOf(GeoMath.distanceM(a, b), GeoMath.distanceM(b, c)), legs)
    }
}
