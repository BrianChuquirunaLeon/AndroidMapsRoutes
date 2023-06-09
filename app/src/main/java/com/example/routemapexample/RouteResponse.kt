package com.example.routemapexample

import com.google.gson.annotations.SerializedName

data class RouteResponse (
    @SerializedName("features") val features:List<FeatureRoute>
    )

data class FeatureRoute (
    @SerializedName("geometry") val geometry:Geometry
        )

data class Geometry (
    @SerializedName("coordinates") val coordinates:List<List<Double>>
    )




