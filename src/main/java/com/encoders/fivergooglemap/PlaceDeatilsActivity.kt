package com.encoders.fivergooglemap

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.encoders.fivergooglemap.databinding.ActivityPlaceDeatilsBinding

class PlaceDeatilsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityPlaceDeatilsBinding
    private lateinit var LATITUDE: String
    private lateinit var LONGITUDE: String
    private lateinit var PLACE_ID: String
    private lateinit var PLACE_NAME: String
    private lateinit var GAELIC_NAME: String
    private lateinit var PLACE_TYPE: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPlaceDeatilsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        LATITUDE = intent.getStringExtra("LATITUDE")!!
        LONGITUDE = intent.getStringExtra("LONGITUDE")!!
        PLACE_ID = intent.getStringExtra("PLACE_ID")!!
        PLACE_NAME = intent.getStringExtra("PLACE_NAME")!!
        GAELIC_NAME = intent.getStringExtra("GAELIC_NAME")!!
        PLACE_TYPE = intent.getStringExtra("PLACE_TYPE")!!
        binding.latitude.text = LATITUDE
        binding.longitude.text = LONGITUDE
        binding.placeId.text = PLACE_ID
        binding.placeName.text = PLACE_NAME
        binding.gaelicName.text = GAELIC_NAME
        binding.type.text = PLACE_TYPE

        binding.back.setOnClickListener {
            finish()
        }
    }

    override fun onBackPressed() {
        super.onBackPressed()
        finish()
    }
}