package com.example.mvvmkotlinexample.retrofit


import com.encoders.fivergooglemap.models.AllPlaces
import com.encoders.myapplication.Networking.APIs.Companion.APP_PLACES
import retrofit2.Call
import retrofit2.http.*

interface ApiInterface {


    @GET(APP_PLACES)
    fun APP_PLACES(
    ): Call<AllPlaces>


}