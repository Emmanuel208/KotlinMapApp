package com.encoders.driver.CLientViewModel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.encoders.fivergooglemap.models.AllPlaces
import com.encoders.fivergooglemap.models.AllPlacesItem
import com.encoders.fivergooglemap.repository.AppRepository
import okhttp3.MultipartBody
import okhttp3.RequestBody

class ClientViewModel : ViewModel() {

    var allPlaces: MutableLiveData<AllPlaces>? = null


    fun clear_data() {
        AppRepository.clear_data()
    }


    fun AllPlaces(

    ): LiveData<AllPlaces>? {
        allPlaces = AppRepository.AllPlaces(

        )
        return allPlaces
    }


}