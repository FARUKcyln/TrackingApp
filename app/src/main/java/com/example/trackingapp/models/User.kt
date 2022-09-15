package com.example.trackingapp.models

import com.google.android.gms.maps.model.BitmapDescriptor

data class User(
    var fullName: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null
)