package com.encoders.fivergooglemap.models

data class AllPlacesItem(
    val gaelic_name: String,
    val id: String,
    val latitude: Double,
    val location: String,
    val longitude: Double,
    val name: String,
    val place_type_id: String
)