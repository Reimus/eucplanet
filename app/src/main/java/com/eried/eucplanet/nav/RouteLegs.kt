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
     * does not find its last stop at the start. The path may begin before the
     * first stop (at the rider); that stretch is not a leg. Without a path, the
     * legs are straight lines.
     */
    fun metres(stops: List<GeoPoint>, path: List<GeoPoint>): List<Double> {
        if (stops.size < 2) return emptyList()
        if (path.size < 2) return stops.zipWithNext { a, b -> GeoMath.distanceM(a, b) }
        val total = GeoMath.polylineLengthM(path)
        val along = ArrayList<Double>(stops.size)
        var from = 0
        var fromAlong = 0.0
        for (k in stops.indices) {
            val hit = GeoMath.nearestOnPolyline(stops[k], path.subList(from, path.size))
            val prev = along.lastOrNull() ?: 0.0
            along += when {
                // The last stop is where the path ends.
                k == stops.lastIndex && k > 0 -> total
                hit == null -> prev
                else -> (fromAlong + hit.alongM).coerceAtLeast(prev)
            }
            if (hit != null) {
                for (i in from until from + hit.segmentIndex) fromAlong += GeoMath.distanceM(path[i], path[i + 1])
                from += hit.segmentIndex
            }
        }
        return along.zipWithNext { a, b -> (b - a).coerceAtLeast(0.0) }
    }
}
