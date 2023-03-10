package com.yun.mysimplenavi.common.constants

class GpsConstants {

    companion object {

        const val WAY_OFF_ROUTE_MAX_COUNT = 3
    }

    object Distance {
        const val WAY_POINT_DISTANCE_CAR = 30.0
        const val WAY_POINT_DISTANCE_FOOT = 15.0
        const val WAY_OFF_DISTANCE = 50.0
    }

    object WayPoint {
        const val RIGHT = "right"
        const val SLIGHT_RIGHT = "slight right"
        const val LEFT = "left"
        const val SLIGHT_LEFT = "slight left"
        const val STRAIGHT = "straight"
        const val U_TURN = "uturn"
        const val DEPART = "depart"
        const val ARRIVE = "arrive"
    }


}