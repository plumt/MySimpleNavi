package com.yun.mysimplenavi.util

import com.google.android.gms.maps.model.LatLng

object PolyUtil {


    fun isLocationOnPath(
        point: LatLng,
        polyline: List<LatLng>,
        geodesic: Boolean,
        tolerance: Double
    ): Boolean {
        return isLocationOnEdgeOrPath(point, polyline, false, geodesic, tolerance)
    }

    fun isLocationOnEdge(point: LatLng, polygon: List<LatLng>, geodesic: Boolean, tolerance: Double): Boolean {
        return isLocationOnEdgeOrPath(point, polygon, true, geodesic, tolerance)
    }

    private fun isLocationOnEdgeOrPath(
        point: LatLng,
        poly: List<LatLng>,
        closed: Boolean,
        geodesic: Boolean,
        toleranceEarth: Double
    ): Boolean {
        val size = poly.size
        if (size == 0) {
            return false
        }
        val tolerance: Double = toleranceEarth / 6371009.0
        val havTolerance: Double = hav(tolerance)
        val lat3 = Math.toRadians(point.latitude)
        val lng3 = Math.toRadians(point.longitude)
        val prev = poly[if (closed) size - 1 else 0]
        var lat1 = Math.toRadians(prev.latitude)
        var lng1 = Math.toRadians(prev.longitude)
        if (geodesic) {
            for (point2 in poly) {
                val lat2 = Math.toRadians(point2.latitude)
                val lng2 = Math.toRadians(point2.longitude)
                if (isOnSegmentGC(lat1, lng1, lat2, lng2, lat3, lng3, havTolerance)) {
                    return true
                }
                lat1 = lat2
                lng1 = lng2
            }
        } else { // We project the points to mercator space, where the Rhumb segment is a straight line,
            // and compute the geodesic distance between point3 and the closest point on the
            // segment. This method is an approximation, because it uses "closest" in mercator
            // space which is not "closest" on the sphere -- but the error is small because
            // "tolerance" is small.
            val minAcceptable = lat3 - tolerance
            val maxAcceptable = lat3 + tolerance
            var y1: Double = mercator(lat1)
            val y3: Double = mercator(lat3)
            val xTry = DoubleArray(3)
            for (point2 in poly) {
                val lat2 = Math.toRadians(point2.latitude)
                val y2: Double = mercator(lat2)
                val lng2 = Math.toRadians(point2.longitude)
                if (Math.max(lat1, lat2) >= minAcceptable && Math.min(
                        lat1,
                        lat2
                    ) <= maxAcceptable
                ) { // We offset longitudes by -lng1; the implicit x1 is 0.
                    val x2: Double = wrap(lng2 - lng1, -Math.PI, Math.PI)
                    val x3Base: Double = wrap(lng3 - lng1, -Math.PI, Math.PI)
                    xTry[0] = x3Base
                    // Also explore wrapping of x3Base around the world in both directions.
                    xTry[1] = x3Base + 2 * Math.PI
                    xTry[2] = x3Base - 2 * Math.PI
                    for (x3 in xTry) {
                        val dy = y2 - y1
                        val len2 = x2 * x2 + dy * dy
                        val t: Double =
                            if (len2 <= 0) 0.0 else clamp(
                                (x3 * x2 + (y3 - y1) * dy) / len2,
                                0.0,
                                1.0
                            )
                        val xClosest = t * x2
                        val yClosest = y1 + t * dy
                        val latClosest: Double = inverseMercator(yClosest)
                        val havDist: Double = havDistance(lat3, latClosest, x3 - xClosest)
                        if (havDist < havTolerance) {
                            return true
                        }
                    }
                }
                lat1 = lat2
                lng1 = lng2
                y1 = y2
            }
        }
        return false
    }

    private fun isOnSegmentGC(
        lat1: Double,
        lng1: Double,
        lat2: Double,
        lng2: Double,
        lat3: Double,
        lng3: Double,
        havTolerance: Double
    ): Boolean {
        val havDist13: Double = havDistance(lat1, lat3, lng1 - lng3)
        if (havDist13 <= havTolerance) {
            return true
        }
        val havDist23: Double = havDistance(lat2, lat3, lng2 - lng3)
        if (havDist23 <= havTolerance) {
            return true
        }
        val sinBearing = sinDeltaBearing(lat1, lng1, lat2, lng2, lat3, lng3)
        val sinDist13: Double = sinFromHav(havDist13)
        val havCrossTrack: Double = havFromSin(sinDist13 * sinBearing)
        if (havCrossTrack > havTolerance) {
            return false
        }
        val havDist12: Double = havDistance(lat1, lat2, lng1 - lng2)
        val term = havDist12 + havCrossTrack * (1 - 2 * havDist12)
        if (havDist13 > term || havDist23 > term) {
            return false
        }
        if (havDist12 < 0.74) {
            return true
        }
        val cosCrossTrack = 1 - 2 * havCrossTrack
        val havAlongTrack13 = (havDist13 - havCrossTrack) / cosCrossTrack
        val havAlongTrack23 = (havDist23 - havCrossTrack) / cosCrossTrack
        val sinSumAlongTrack: Double = sinSumFromHav(havAlongTrack13, havAlongTrack23)
        return sinSumAlongTrack > 0 // Compare with half-circle == PI using sign of sin().
    }

    fun hav(x: Double): Double {
        val sinHalf = Math.sin(x * 0.5)
        return sinHalf * sinHalf
    }

    fun mercator(lat: Double): Double {
        return Math.log(Math.tan(lat * 0.5 + Math.PI / 4))
    }

    // Returns sin(arcHav(x) + arcHav(y)).
    fun sinSumFromHav(x: Double, y: Double): Double {
        val a = Math.sqrt(x * (1 - x))
        val b = Math.sqrt(y * (1 - y))
        return 2 * (a + b - 2 * (a * y + b * x))
    }

    /**
     * Returns hav() of distance from (lat1, lng1) to (lat2, lng2) on the unit sphere.
     */
    fun havDistance(lat1: Double, lat2: Double, dLng: Double): Double {
        return hav(lat1 - lat2) + hav(dLng) * Math.cos(lat1) * Math.cos(lat2)
    }

    fun wrap(n: Double, min: Double, max: Double): Double {
        return if (n >= min && n < max) n else mod(n - min, max - min) + min
    }

    /**
     * Restrict x to the range [low, high].
     */
    fun clamp(x: Double, low: Double, high: Double): Double {
        return if (x < low) low else if (x > high) high else x
    }

    /**
     * Returns latitude from mercator Y.
     */
    fun inverseMercator(y: Double): Double {
        return 2 * Math.atan(Math.exp(y)) - Math.PI / 2
    }

    /**
     * Returns sin(initial bearing from (lat1,lng1) to (lat3,lng3) minus initial bearing
     * from (lat1, lng1) to (lat2,lng2)).
     */
    private fun sinDeltaBearing(lat1: Double, lng1: Double, lat2: Double, lng2: Double, lat3: Double, lng3: Double): Double {
        val sinLat1 = Math.sin(lat1)
        val cosLat2 = Math.cos(lat2)
        val cosLat3 = Math.cos(lat3)
        val lat31 = lat3 - lat1
        val lng31 = lng3 - lng1
        val lat21 = lat2 - lat1
        val lng21 = lng2 - lng1
        val a = Math.sin(lng31) * cosLat3
        val c = Math.sin(lng21) * cosLat2
        val b: Double = Math.sin(lat31) + 2 * sinLat1 * cosLat3 * hav(lng31)
        val d: Double = Math.sin(lat21) + 2 * sinLat1 * cosLat2 * hav(lng21)
        val denom = (a * a + b * b) * (c * c + d * d)
        return if (denom <= 0) 1.0 else (a * d - b * c) / Math.sqrt(denom)
    }

    // Given h==hav(x), returns sin(abs(x)).
    fun sinFromHav(h: Double): Double {
        return 2 * Math.sqrt(h * (1 - h))
    }

    // Returns hav(asin(x)).
    fun havFromSin(x: Double): Double {
        val x2 = x * x
        return x2 / (1 + Math.sqrt(1 - x2)) * .5
    }

    /**
     * Returns the non-negative remainder of x / m.
     * @param x The operand.
     * @param m The modulus.
     */
    fun mod(x: Double, m: Double): Double {
        return (x % m + m) % m
    }
}