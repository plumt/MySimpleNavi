package com.yun.mysimplenavi.util

object Util {

    fun convertSeconds(seconds: Int): Triple<Int, Int, Int> {
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        val hours = minutes / 60
        val remainingMinutes = minutes % 60
        return Triple(hours, remainingMinutes, remainingSeconds)
    }

    fun formatDistance(distanceInMeters: Double): String {
        return if (distanceInMeters < 1000) {
            "${distanceInMeters.toInt()}m"
        } else {
            "${"%.2f".format(distanceInMeters / 1000)}km"
        }
    }
}