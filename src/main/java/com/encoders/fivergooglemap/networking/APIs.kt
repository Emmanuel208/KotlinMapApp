package com.encoders.myapplication.Networking

class APIs {
    companion object {
        const val BASE_URL = "https://gist.githubusercontent.com/"


        const val GOOGLEMAP_BASE_URL = "https://maps.googleapis.com/"
        const val MAP_DOMAIN = "maps/api/directions/json"

        const val CALCULATE_DISTANCE = "$GOOGLEMAP_BASE_URL$MAP_DOMAIN"

        const val APP_PLACES = "$BASE_URL/saravanabalagi/541a511eb71c366e0bf3eecbee2dab0a/raw/bb1529d2e5b71fd06760cb030d6e15d6d56c34b3/places.json"
    }
}
