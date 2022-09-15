package com.example.trackingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.Window
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.trackingapp.adapter.UserListAdapter
import com.example.trackingapp.databinding.ActivityMapsBinding
import com.example.trackingapp.models.User
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.*
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.CancellationToken
import com.google.android.gms.tasks.CancellationTokenSource
import com.google.android.gms.tasks.OnTokenCanceledListener
import com.google.firebase.database.*


class MapsActivity : AppCompatActivity(), OnMapReadyCallback,
    LocationSource.OnLocationChangedListener {

    private lateinit var mMap: GoogleMap
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var binding: ActivityMapsBinding
    private lateinit var userId: String

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE)
        window.setFlags(
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
            WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        )

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        if (!checkPermission()) {
            requestPermission()
        }

        userId = "-NBqcS-bigWl4u_TtubU"


        val database = FirebaseDatabase.getInstance()
        database.reference.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                mMap.clear()
                placeUsersOnMap()
            }

            override fun onCancelled(error: DatabaseError) {
                TODO("Not yet implemented")
            }

        })

        // initialize fused location client
        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)

        getCurrentLocation()

    }

    @SuppressLint("MissingPermission")
    private fun getCurrentLocation() {
        if (checkPermission()) {
            fusedLocationProviderClient.getCurrentLocation(
                Priority.PRIORITY_HIGH_ACCURACY,
                object : CancellationToken() {
                    override fun onCanceledRequested(p0: OnTokenCanceledListener) =
                        CancellationTokenSource().token

                    override fun isCancellationRequested() = false
                })
                .addOnSuccessListener { location: Location? ->
                    if (location == null)
                        Toast.makeText(this, "Cannot get location.", Toast.LENGTH_SHORT).show()
                    else {
                        println(location.toString())
                        updateMyCurrentLocation(location)
                    }
                }
        }
    }

    private fun moveMapCameraToCurrentLocation(location: Location) {
        val myLocation = LatLng(location.latitude, location.longitude)
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(myLocation, 6f))
    }

    private fun updateMyCurrentLocation(location: Location) {
        val database = FirebaseDatabase.getInstance().getReference("Users").child(userId)
        database.child("latitude").setValue(location.latitude)
        database.child("longitude").setValue(location.longitude)
    }

    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_FINE_LOCATION
        )
        val result1 = ContextCompat.checkSelfPermission(
            applicationContext,
            Manifest.permission.ACCESS_COARSE_LOCATION
        )
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ),
            1
        )
    }


    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
    }

    private fun placeUsersOnMap() {
        val database = FirebaseDatabase.getInstance().getReference("Users")
        database.get().addOnSuccessListener {
            val users = mutableListOf<User>()
            it.children.forEach { result ->
                users.add(
                    User(
                        result.child("fullName").value as String?,
                        result.child("latitude").value as Double?,
                        result.child("longitude").value as Double?
                    )
                )
            }

            var avgLat: Double = 0.0
            var avgLng: Double = 0.0

            for (user in users) {
                avgLat += user.latitude!!
                avgLng += user.longitude!!
                val userLocation = user.latitude?.let { lat ->
                    user.longitude?.let { lng ->
                        LatLng(
                            lat,
                            lng
                        )
                    }
                }
                userLocation?.let { it1 ->
                    MarkerOptions().position(it1).title(user.fullName)
                }
                    ?.let { it2 -> mMap.addMarker(it2) }
            }

            if (users.size != 0) {
                avgLng /= users.size
                avgLat /= users.size
            }

            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(avgLat,avgLng), 7f))
            //mMap.setLatLngBoundsForCameraTarget(bound.build())
            val userListAdapter = UserListAdapter(this, users as ArrayList<User>, mMap)
            binding.recListView.layoutManager = LinearLayoutManager(this)
            binding.recListView.adapter = userListAdapter

        }.addOnFailureListener {
            Toast.makeText(this, "Failed", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onLocationChanged(p0: Location) {
        updateMyCurrentLocation(p0)
    }
}