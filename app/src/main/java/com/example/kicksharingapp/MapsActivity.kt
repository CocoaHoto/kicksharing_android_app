package com.example.kicksharingapp

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.content.res.Resources
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import com.example.kicksharingapp.databinding.ActivityMapsBinding
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.ValueEventListener
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var fusedLocationProviderClient: FusedLocationProviderClient
    private lateinit var locationRequest: LocationRequest
    private lateinit var userLocationMarker : Marker;
    private lateinit var popUpFrameLayout: FrameLayout
    private lateinit var scooterInteractionFrameLayout: FrameLayout
    private lateinit var avatarTextView: ImageView
    private lateinit var auth: FirebaseAuth;
    private lateinit var dataBase: DatabaseReference

    private val ACCESS_LOCATION_REQUEST_CODE = 10001;

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        init()
    }

    private fun init() {
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.place_holder, PopupFragment.newInstance())
            .commit()

        supportFragmentManager
            .beginTransaction()
            .replace(R.id.place_holder_2, ScooterInteraction.newInstance())
            .commit()

        avatarTextView = findViewById(R.id.imageView)
        popUpFrameLayout = findViewById(R.id.place_holder)
        scooterInteractionFrameLayout = findViewById((R.id.place_holder_2))

        popUpFrameLayout.visibility = View.GONE

        avatarTextView.setOnClickListener {
            if(popUpFrameLayout.isVisible) {
                popUpFrameLayout.visibility = View.GONE
            }
            else {
                popUpFrameLayout.visibility = View.VISIBLE
            }
        }

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this)
        locationRequest = LocationRequest.create()
        locationRequest.interval = 500
        locationRequest.fastestInterval = 500
        locationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY

        auth = Firebase.auth
        dataBase = Firebase.database("https://kicksharingapp-default-rtdb.europe-west1.firebasedatabase.app").reference
    }

    override fun onStart() {
        super.onStart()
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startLocationUpdates();
        } else {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_LOCATION_REQUEST_CODE)
        }
    }

    override fun onStop() {
        super.onStop()
        stopLocationUpdates()
    }

    private fun changeMapStyle(googleMap: GoogleMap){
        try {
            val success = googleMap.setMapStyle(
                MapStyleOptions.loadRawResourceStyle(
                    this, R.raw.style_json
                )
            )
            if (!success) {
                Log.e("Error", "Style parsing failed.")
            }
        } catch (e: Resources.NotFoundException) {
            Log.e("Error", "Can't find style.", e)
        }
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        //Ставим карту по умолчанию
        mMap.mapType = GoogleMap.MAP_TYPE_NORMAL;
        //ставим слушатель нажатии на карту, что все окна закрывались при нажатии на карту
        mMap.setOnMapClickListener {
            popUpFrameLayout.visibility =View.GONE
        }
        //изменяем стиль карты
        changeMapStyle(mMap)
        //добавляем самокаты на карту
        addScootersOnMap()
    }


    private fun addScootersOnMap(){
        val scooterRef = dataBase.child("Scooter")

        val scooterDataListener = object : ValueEventListener {
            @SuppressLint("UseCompatLoadingForDrawables")
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                for(ds in dataSnapshot.children)
                {
                    val scooterDS = ds.getValue(Scooter::class.java)
                    if (scooterDS != null) {
                        val latlng = scooterDS.latitude?.let { scooterDS.longitude?.let { it1 ->
                            LatLng(it,
                                it1
                            )
                        } }
                        val markerOptions = MarkerOptions()
                        if (latlng != null) {
                            markerOptions.position(latlng)
                        }
                        val bitmapdraw = resources.getDrawable(R.drawable.scooter) as BitmapDrawable
                        val b = bitmapdraw.bitmap
                        val smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false)
                        markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))

                        mMap.addMarker(markerOptions)
                    }
                }
            }

            override fun onCancelled(databaseError: DatabaseError) {
                Log.w("TAG", "loadPost:onCancelled", databaseError.toException())
            }
        }
        scooterRef.addValueEventListener(scooterDataListener)
    }

    //Функция обратного вызова (Callback)
    var locationCallback: LocationCallback = object : LocationCallback() {
        //Вызывается даннаня функция
        override fun onLocationResult(locationResult: LocationResult) {
            super.onLocationResult(locationResult)
            Log.d("debug", "onLocationResult: " + locationResult.lastLocation)
            if(this@MapsActivity::mMap.isInitialized){
                //Ставится маркер на место, где находится пользователь
                setUserLocationMarker(locationResult.lastLocation)
            }
        }
    }

    @SuppressLint("MissingPermission")
    //Начинается обновление положения пользователя
    private fun startLocationUpdates() {
        fusedLocationProviderClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
    }

    //останавливается обновление положения пользователя
    private fun stopLocationUpdates() {
        fusedLocationProviderClient.removeLocationUpdates(locationCallback)
    }

    @SuppressLint("UseCompatLoadingForDrawables")
    private fun setUserLocationMarker(lastLocation: Location) {
        val lating = LatLng(lastLocation.latitude, lastLocation.longitude)
        if (this::userLocationMarker.isInitialized) {
            userLocationMarker.position = lating
        }
        else {
            val markerOptions = MarkerOptions()
            markerOptions.position(lating)
            val bitmapdraw = resources.getDrawable(R.drawable.standing_man) as BitmapDrawable
            val b = bitmapdraw.bitmap
            val smallMarker = Bitmap.createScaledBitmap(b, 150, 150, false)
            markerOptions.icon(BitmapDescriptorFactory.fromBitmap(smallMarker))
            userLocationMarker = mMap.addMarker(markerOptions)!!
            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(lating, 17f))

        }
    }
}