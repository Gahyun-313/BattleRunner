package com.example.battlerunner.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.naver.maps.geometry.LatLng
import com.naver.maps.map.CameraUpdate
import com.naver.maps.map.MapView
import com.naver.maps.map.NaverMap
import com.naver.maps.map.OnMapReadyCallback
import com.naver.maps.map.overlay.Marker
import com.naver.maps.map.util.FusedLocationSource
import com.naver.maps.map.LocationTrackingMode

class HomeGoalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mapView: MapView
    private lateinit var naverMap: NaverMap
    private lateinit var locationSource: FusedLocationSource

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1000
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_goal)

        // Initialize location source before MapView
        locationSource = FusedLocationSource(this, LOCATION_PERMISSION_REQUEST_CODE)

        // Initialize MapView
        mapView = findViewById(R.id.naverMapView)
        mapView.onCreate(savedInstanceState)
        mapView.getMapAsync(this)

        setupUI()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.confirmBtn)?.setOnClickListener {
            val distanceInput = findViewById<EditText>(R.id.distanceInput)
            val distance = distanceInput?.text.toString().toIntOrNull()
            if (distance != null) {
                // Handle valid distance input
                handleDistanceInput(distance)
            }
        }

        findViewById<ImageButton>(R.id.closeBtn)?.setOnClickListener {
            finish()
        }
    }

    private fun handleDistanceInput(distance: Int) {
        // Implement your distance handling logic here
    }

    override fun onMapReady(map: NaverMap) {
        naverMap = map

        try {
            // Set up location source
            naverMap.locationSource = locationSource

            // Enable location tracking
            naverMap.locationTrackingMode = LocationTrackingMode.Follow


            // Configure UI settings
            naverMap.uiSettings.apply {
                isLocationButtonEnabled = true
                isZoomControlEnabled = true
                isCompassEnabled = true
            }
            /*
            // Set initial camera position (Seoul)
            val seoulLatLng = LatLng(37.5665, 126.9780)
            naverMap.moveCamera(CameraUpdate.scrollTo(seoulLatLng))
            naverMap.moveCamera(CameraUpdate.zoomTo(15.0))*/

            val marker = Marker()
            naverMap.addOnLocationChangeListener { location ->
                val currentLatLng = LatLng(location.latitude, location.longitude)
                marker.position = currentLatLng
                marker.map = naverMap // Update marker's position on the map
                naverMap.moveCamera(CameraUpdate.scrollTo(currentLatLng)) // Move camera to current location
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "Error initializing the map.", Toast.LENGTH_SHORT).show()
        }
    }


    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (locationSource.onRequestPermissionsResult(requestCode, permissions, grantResults)) {
            if (!locationSource.isActivated) {
                naverMap.locationTrackingMode = LocationTrackingMode.None
                Toast.makeText(this, "Location permissions denied.", Toast.LENGTH_SHORT).show()
            }
            return
        }
    }

    // Lifecycle methods
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }

    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
    }

    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }
}