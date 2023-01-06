package com.encoders.fivergooglemap.repository

import android.util.Log
import androidx.lifecycle.MutableLiveData
import com.encoders.fivergooglemap.models.AllPlaces
import com.encoders.fivergooglemap.models.AllPlacesItem
import com.example.mvvmkotlinexample.retrofit.RetrofitClient
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

object AppRepository {

    var allPlaces = MutableLiveData<AllPlaces>()

    fun clear_data() {
        allPlaces = MutableLiveData()

    }


    fun AllPlaces(
    ): MutableLiveData<AllPlaces> {

        val call = RetrofitClient.apiInterface_Header.APP_PLACES(
        )

        call.enqueue(object : Callback<AllPlaces> {
            override fun onFailure(call: Call<AllPlaces>, t: Throwable) {
                // TODO("Not yet implemented")
                Log.v("DEBUG : ", t.message.toString())
            }

            override fun onResponse(
                call: Call<AllPlaces>,
                response: Response<AllPlaces>
            ) {
                // TODO("Not yet implemented")
                Log.v("DEBUG : ", response.body().toString())

                val data = response.body()

                allPlaces.value = data!!
            }
        })

        return allPlaces
    }


}