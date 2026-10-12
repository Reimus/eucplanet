package com.eried.eucplanet.nav

import com.eried.eucplanet.data.model.GeoPoint

/**
 * How far each leg of a route is, from one stop to the next, along the path
 * the rider will actually take.
 */
object RouteLegs {
    /**
     * One distance per leg (stops.size - 1 of them), in metres. Each stop is
     * looked for only past the previous one, so a loop that ends where it began
     * does not find its last stop at the start. The last leg runs to the end of
     * the path. Without a path, the legs are straight lines.
     */
    fun metres(stops: List<GeoPoint>, path: List<GeoPoint>): List<Double> {
        if (stops.size < 2) return emptyList()
        if (path.size < 2) return stops.zipWithNext { a, b -> GeoMath.distanceM(a, b) }
        val total = GeoMath.polylineLengthM(path)
        val legs = ArrayList<Double>(stops.size - 1)
        var from = 0
        var fromAlong = 0.0
        var prevAlong = 0.0
        for (k in 1 until stops.size - 1) {
            val rest = path.subList(from, path.size)
            val hit = GeoMath.nearestOnPolyline(stops[k], rest)
            val along = if (hit == null) prevAlong else fromAlong + hit.alongM
            legs += (along - prevAlong).coerceAtLeast(0.0)
            prevAlong = along
            if (hit != null) {
                for (i in from until from + hit.segmentIndex) fromAlong += GeoMath.distanceM(path[i], path[i + 1])
                from += hit.segmentIndex
            }
        }
        legs += (total - prevAlong).coerceAtLeast(0.0)
        return legs
    }
}
