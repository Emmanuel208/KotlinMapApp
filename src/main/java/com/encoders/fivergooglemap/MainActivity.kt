package com.encoders.fivergooglemap

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModelProvider
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.toolbox.JsonArrayRequest
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.encoders.driver.CLientViewModel.ClientViewModel
import com.encoders.fivergooglemap.base.BaseActivity
import com.encoders.fivergooglemap.databinding.ActivityMainBinding
import com.encoders.fivergooglemap.models.AllPlaces
import com.encoders.fivergooglemap.models.AllPlacesItem
import com.encoders.fivergooglemap.models.Places_Types
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.ln
import kotlin.math.roundToInt


class MainActivity : BaseActivity(), OnMapReadyCallback, GoogleApiClient.ConnectionCallbacks,
    GoogleApiClient.OnConnectionFailedListener,
    LocationListener {
    override fun onBackPressed() {
        super.onBackPressed()
        finishAffinity()
    }

    private var service: LocationManager? = null
    private var enabled: Boolean? = null
    private var mLocationRequest: LocationRequest? = null
    private var mGoogleApiClient: GoogleApiClient? = null
    private var mLastLocation: Location? = null
    private var mCurrLocationMarker: Marker? = null
    private lateinit var mMap: GoogleMap
    private var REQUEST_LOCATION_CODE = 101
    private lateinit var PICKUP_LATITUDE: String
    private lateinit var PICKUP_LONGITUDE: String
    private val AUTOCOMPLETE_REQUEST_CODE = 1
    private lateinit var allPlaces: AllPlaces
    private lateinit var clientViewModel: ClientViewModel
    private lateinit var places_list: MutableList<String>
    private lateinit var allPlacesItem: MutableList<AllPlacesItem>
    private lateinit var places_types: MutableList<Places_Types>
    private var PLACE_TYPE: String = ""
    private var LOAD_PLACES_OR_NOT: Boolean = false

    var ALL_PLACES_URL =
        "https://gist.githubusercontent.com/saravanabalagi/541a511eb71c366e0bf3eecbee2dab0a/raw/bb1529d2e5b71fd06760cb030d6e15d6d56c34b3/places.json"

    var PLACES_TYPES =
        "https://gist.githubusercontent.com/saravanabalagi/541a511eb71c366e0bf3eecbee2dab0a/raw/bb1529d2e5b71fd06760cb030d6e15d6d56c34b3/place_types.json"

    override fun onLocationChanged(location: Location?) {
        mLastLocation = location
        if (mCurrLocationMarker != null) {
            mCurrLocationMarker!!.remove()
        }

        var latLng = LatLng(location!!.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 12f))
        mMap.addMarker(
            MarkerOptions()
                .position(latLng)
                .title("Current Location")
        )
        mGoogleApiClient!!.disconnect()
        PICKUP_LATITUDE = latLng.latitude.toString()
        PICKUP_LONGITUDE = latLng.longitude.toString()

        val circle: Circle = mMap.addCircle(
            CircleOptions()
                .center(LatLng(latLng.latitude, latLng.longitude))
                .radius(5000.0)
                .strokeColor(Color.BLUE)
                .strokeWidth(2F)
                .fillColor(resources.getColor(R.color.blue))
        )

        PLACES_TYPES()

    }

    override fun onConnected(p0: Bundle?) {
        mLocationRequest = LocationRequest()
        mLocationRequest!!.interval = 1000
        mLocationRequest!!.fastestInterval = 1000
        mLocationRequest!!.priority = LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY

        // Check if enabled and if not send user to the GPS settings
        if (!enabled!!) {
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(intent);
        }
        // Check if permission is granted or not
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient,
                mLocationRequest,
                this
            )
        }
    }

    override fun onConnectionSuspended(p0: Int) {
    }

    override fun onConnectionFailed(p0: ConnectionResult) {
    }

    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        service = this.getSystemService(LOCATION_SERVICE) as LocationManager
        enabled = service!!.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val mapFragment = supportFragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
        mapFragment.retainInstance = true
        clientViewModel = ViewModelProvider(this)[ClientViewModel::class.java]

        binding.searchPlace.setOnClickListener {
            if (Check_Internet_Connection(this)) {
                CoroutineScope(Dispatchers.Main).launch {
                    Show_Selected_Places()
                }
            } else {
                No_Internet_Connection()
            }
        }
    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL

        //Initialize Google Play Services
        if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
            ) {
                //Location Permission already granted
                buildGoogleApiClient()
                mMap.isMyLocationEnabled = true
            } else {
                //Request Location Permission
                checkLocationPermission()
            }
        } else {
            buildGoogleApiClient()
            mMap.isMyLocationEnabled = true
        }

//        mMap.setOnCameraChangeListener { cameraPosition ->
//            val latLng = cameraPosition.target
//            Log.e("Latitude : ", latLng.latitude.toString() + "")
//            Log.e("Longitude : ", latLng.longitude.toString() + "")
//            PICKUP_LATITUDE = latLng.latitude.toString()
//            PICKUP_LONGITUDE = latLng.longitude.toString()
//            try {
//
//            } catch (e: Exception) {
//                e.printStackTrace()
//            }
//        }

    }

    @Synchronized
    fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(this)
            .addConnectionCallbacks(this)
            .addOnConnectionFailedListener(this)
            .addApi(LocationServices.API)
            .build()

        mGoogleApiClient!!.connect()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_LOCATION_CODE -> {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // permission was granted, yay! Do the location-related task you need to do.
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient()
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    // permission denied, boo! Disable the functionality that depends on this permission.
                    Toast.makeText(this, "permission denied", Toast.LENGTH_LONG).show()
                }
                return
            }
        }
    }

    private fun checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                AlertDialog.Builder(this)
                    .setTitle("Location Permission Needed")
                    .setMessage("This app needs the Location permission, please accept to use location functionality")
                    .setPositiveButton("OK", DialogInterface.OnClickListener { dialog, which ->
                        ActivityCompat.requestPermissions(
                            this,
                            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                            REQUEST_LOCATION_CODE
                        )
                    })
                    .create()
                    .show()

            } else ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                REQUEST_LOCATION_CODE
            )
        }
    }

    private fun AllPlaces() {
        Show_Loader(this)
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val request = JsonArrayRequest(Request.Method.GET, ALL_PLACES_URL, null, { response ->
            Hide_Loader()
            try {
                mMap.clear()
                allPlacesItem = mutableListOf()
                for (place in 0 until response.length()) {
                    val jsonObject: JSONObject? = response.getJSONObject(place)
                    mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    jsonObject!!.getString("latitude").toDouble(),
                                    jsonObject!!.getString("longitude").toDouble()
                                )
                            )
                            .title(jsonObject!!.getString("name"))
                    )

                    allPlacesItem.add(
                        AllPlacesItem(
                            jsonObject!!.getString("gaelic_name"),
                            jsonObject!!.getInt("id").toString(),
                            jsonObject!!.getDouble("latitude"),
                            jsonObject!!.getString("location"),
                            jsonObject!!.getDouble("longitude"),
                            jsonObject!!.getString("name"),
                            jsonObject!!.getInt("place_type_id").toString(),

                            )
                    )
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { error ->
            Toast.makeText(this@MainActivity, error.message.toString(), Toast.LENGTH_LONG)
                .show()
        })

        queue.add(request)
    }

    private fun PLACES_TYPES() {
        Show_Loader(this)
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val request = JsonArrayRequest(Request.Method.GET, PLACES_TYPES, null, { response ->
            Hide_Loader()
            try {
                places_list = mutableListOf()
                places_types = mutableListOf()
                places_list.add("All Places")
                for (place in 0 until response.length()) {
                    val jsonObject = response.getJSONObject(place)
                    places_list.add(jsonObject.getString("name"))

                    places_types.add(
                        Places_Types(
                            jsonObject.getInt("id"),
                            jsonObject.getString("name"),
                            jsonObject.getString("updated_at"),
                            jsonObject.getString("created_at")
                        )
                    )

                }
                mMap.setOnInfoWindowClickListener {
                    val lat = mMap.cameraPosition.target.latitude
                    val lng = mMap.cameraPosition.target.longitude

                    for (selected_place in allPlacesItem) {
                        if (selected_place.latitude.roundToInt() == lat.roundToInt() && selected_place.longitude.roundToInt() == lng.roundToInt()) {

                            startActivity(
                                Intent(this, PlaceDeatilsActivity::class.java)
                                    .putExtra(
                                        "LATITUDE",
                                        mMap.cameraPosition.target.latitude.toString()
                                    )
                                    .putExtra(
                                        "LATITUDE",
                                        mMap.cameraPosition.target.latitude.toString()
                                    )
                                    .putExtra(
                                        "LONGITUDE",
                                        mMap.cameraPosition.target.longitude.toString()
                                    )
                                    .putExtra("PLACE_ID", selected_place.place_type_id.toString())
                                    .putExtra("PLACE_NAME", selected_place.name.toString())
                                    .putExtra("GAELIC_NAME", selected_place.gaelic_name.toString())
                                    .putExtra("PLACE_TYPE", PLACE_TYPE)

                            )
                        }
                    }

                }
                Places_Dropdown()
                AllPlaces()

            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { error ->
            Toast.makeText(this@MainActivity, error.message.toString(), Toast.LENGTH_LONG)
                .show()
        })

        queue.add(request)
    }


    fun Places_Dropdown() {
        var adapter = ArrayAdapter(
            this, R.layout.spinner_parent_subject_selected, places_list
        )
        adapter?.setDropDownViewResource(R.layout.spinner_parent_info_dropdown_item)
        binding.placesDropdown.adapter = adapter
        binding.placesDropdown.onItemSelectedListener =
            object : AdapterView.OnItemSelectedListener {
                override fun onItemSelected(
                    parent: AdapterView<*>, view: View, position: Int, id: Long
                ) {
                    if (position != 0) {
                        LOAD_PLACES_OR_NOT = true
                        Show_Selected_Places(places_types[position + 1].id)
                        PLACE_TYPE = places_types[position + 1].name
                    } else {
                        if (LOAD_PLACES_OR_NOT) {
                            LOAD_PLACES_OR_NOT = false
                            PLACES_TYPES()

                        }

                    }

                }

                override fun onNothingSelected(parent: AdapterView<*>) {

                }
            }

    }


    fun Show_Selected_Places(place_id: Int) {
        if (mMap != null) {
            mMap.clear()

            for (show_place_ares in allPlacesItem) {
                if (show_place_ares.place_type_id == place_id.toString()) {
                    mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    show_place_ares.latitude,
                                    show_place_ares.longitude
                                )
                            )
                            .title(show_place_ares.name)
                    )
                }
            }
        }

        mMap.setOnInfoWindowClickListener {
            val lat = mMap.cameraPosition.target.latitude
            val lng = mMap.cameraPosition.target.longitude

            for (selected_place in allPlacesItem) {

                if (selected_place.latitude.roundToInt() == lat.roundToInt() && selected_place.longitude.roundToInt() == lng.roundToInt()) {
                    startActivity(
                        Intent(this, PlaceDeatilsActivity::class.java)
                            .putExtra("LATITUDE", mMap.cameraPosition.target.latitude.toString())
                            .putExtra("LATITUDE", mMap.cameraPosition.target.latitude.toString())
                            .putExtra("LONGITUDE", mMap.cameraPosition.target.longitude.toString())
                            .putExtra("PLACE_ID", selected_place.place_type_id.toString())
                            .putExtra("PLACE_NAME", selected_place.name.toString())
                            .putExtra("GAELIC_NAME", selected_place.gaelic_name.toString())
                            .putExtra("PLACE_TYPE", PLACE_TYPE)

                    )
                }
            }

        }

    }


    fun Show_Selected_Places() {


        var PLACES_NEAR_TO_ME =
            "https://maps.googleapis.com/maps/api/place/nearbysearch/json?location=$PICKUP_LATITUDE,$PICKUP_LONGITUDE&radius=${binding.radius.text.toString()}&type=$PLACE_TYPE&keyword=$PLACE_TYPE&key=AIzaSyCu--XPvjl6QGD3NgqnuiYVw4naYBZwJZg"

        Show_Loader(this)
        val queue: RequestQueue = Volley.newRequestQueue(applicationContext)
        val request = JsonObjectRequest(Request.Method.GET, PLACES_NEAR_TO_ME, null, { response ->
            Hide_Loader()
            try {

                val status = response.getString("status")
                if (status == "OK") {
                    mMap.clear()

                    mMap.addMarker(
                        MarkerOptions()
                            .position(
                                LatLng(
                                    PICKUP_LATITUDE.toDouble(),
                                    PICKUP_LONGITUDE.toDouble()
                                )
                            )
                            .title("Current Location")
                    )
                    mGoogleApiClient!!.disconnect()
                    val circle: Circle = mMap.addCircle(
                        CircleOptions()
                            .center(LatLng(PICKUP_LATITUDE.toDouble(), PICKUP_LONGITUDE.toDouble()))
                            .radius(binding.radius.text.toString().toDouble())
                            .strokeColor(Color.BLUE)
                            .strokeWidth(2F)
                            .fillColor(resources.getColor(R.color.blue))
                    )


                    val jsonArray: JSONArray = response.getJSONArray("results")
                    allPlacesItem = mutableListOf()
                    for (place in 0 until jsonArray.length()) {
                        val jsonObject = jsonArray.getJSONObject(place)
                        val geomatery: JSONObject = jsonObject.getJSONObject("geometry")

                        val location: JSONObject = geomatery.getJSONObject("location")

                        mMap.addMarker(
                            MarkerOptions()
                                .position(
                                    LatLng(
                                        location.getString("lat").toDouble(),
                                        location.getString("lng").toDouble()
                                    )
                                )
                                .title(jsonObject.getString("name"))
                        )

                        allPlacesItem.add(
                            AllPlacesItem(
                                jsonObject!!.getString("name"),
                                jsonObject!!.getString("place_id"),
                                location.getString("lat").toDouble(),
                                jsonObject!!.getString("name"),
                                location.getString("lng").toDouble(),
                                jsonObject!!.getString("name"),
                                jsonObject!!.getString("place_id"),

                                )
                        )


                    }


                    mMap.setOnInfoWindowClickListener {

                        val lat = mMap.cameraPosition.target.latitude
                        val lng = mMap.cameraPosition.target.longitude

//                        binding.latLng.text = lat.roundToInt().toString() + " , " + lng.roundToInt().toString()+"\n"
                        for (selected_place in allPlacesItem) {
//                            binding.latLng.append(selected_place.latitude.roundToInt().toString()+" , "+selected_place.longitude.roundToInt().toString()+"\n")
                            if (  it.title == selected_place.name) {
                                startActivity(
                                    Intent(this, PlaceDeatilsActivity::class.java)
                                        .putExtra(
                                            "LATITUDE",
                                            mMap.cameraPosition.target.latitude.toString()
                                        )
                                        .putExtra(
                                            "LATITUDE",
                                            mMap.cameraPosition.target.latitude.toString()
                                        )
                                        .putExtra(
                                            "LONGITUDE",
                                            mMap.cameraPosition.target.longitude.toString()
                                        )
                                        .putExtra(
                                            "PLACE_ID",
                                            selected_place.place_type_id.toString()
                                        )
                                        .putExtra("PLACE_NAME", selected_place.name.toString())
                                        .putExtra(
                                            "GAELIC_NAME",
                                            selected_place.gaelic_name.toString()
                                        )
                                        .putExtra("PLACE_TYPE", PLACE_TYPE)

                                )
                            }
                        }

                    }

                } else {
                    Normal_Alert(getString(R.string.no_places_found), this)
                }


            } catch (e: Exception) {
                e.printStackTrace()
            }

        }, { error ->
            Toast.makeText(this@MainActivity, error.message.toString(), Toast.LENGTH_LONG)
                .show()
        })

        queue.add(request)


    }


}