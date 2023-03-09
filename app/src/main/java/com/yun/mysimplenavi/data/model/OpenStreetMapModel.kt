package com.yun.mysimplenavi.data.model

class OpenStreetMapModel {
    data class RS(
        val code: String?,
        val routes: ArrayList<Route>?
    ){
        data class Route(
            val legs: ArrayList<Leg>?,
            val distance: Double?,
            val duration: Double?,
        ){
            data class Leg(
                val steps: ArrayList<Step>?
            ){
                data class Step(
                    val maneuver: Maneuver?,
                    val distance: Double?,
                    val intersections: ArrayList<Intersections>?
                ){
                    data class Maneuver(
                        val bearing_after: Int,
                        val type: String,
                        val modifier: String,
                        val location: ArrayList<Double>
                    )

                    data class Intersections(
                        val location: ArrayList<Double>
                    )
                }
            }
        }
    }
}