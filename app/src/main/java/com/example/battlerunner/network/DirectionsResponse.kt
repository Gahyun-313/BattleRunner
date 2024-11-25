package com.example.battlerunner.network

data class DirectionsResponse(
    val code: Int,
    val message: String,
    val route: Route?
)

data class Route(
    val traoptimal: List<TraOptimal>
)

data class TraOptimal(
    val summary: Summary,
    val path: List<List<Double>>
)

data class Summary(
    val start: Location,
    val goal: Location,
    val distance: Int,
    val duration: Int
)

data class Location(
    val lat: Double,
    val lng: Double
)