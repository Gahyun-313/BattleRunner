package com.example.battlerunner.ui.home

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.shared.MapFragment
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.LocationUtils.requestLocationPermission
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class HomeFragment : Fragment(R.layout.fragment_home), OnMapReadyCallback {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private var mapFragment = MapFragment()
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private var cameraPosition: CameraPosition? = null
    private var isDrawing: Boolean = false


    private val viewModel by lazy {
        ViewModelProvider(requireActivity()).get(HomeViewModel::class.java)
    }

    private val pathPoints = mutableListOf<LatLng>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

        mapFragment.setOnMapReadyCallback {
            cameraPosition?.let {
                mapFragment.googleMap.moveCamera(CameraUpdateFactory.newCameraPosition(it))
            } ?: mapFragment.moveToCurrentLocationImmediate()
        }

        // ViewModel의 경과 시간 관찰하여 UI 업데이트
        viewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        // ViewModel의 거리 관찰하여 UI 업데이트
        viewModel.distance.observe(viewLifecycleOwner) { totalDistance ->
            binding.todayDistance.text = String.format("%.2f m", totalDistance)
        }

        binding.startBtn.setOnClickListener {
            if (LocationUtils.hasLocationPermission(requireContext())) {
                MapUtils.startLocationUpdates(requireContext(), fusedLocationClient, viewModel)
                viewModel.startTimer() // ViewModel에서 타이머 시작
            } else {
                LocationUtils.requestLocationPermission(this)
            }
        }

        binding.finishBtn.setOnClickListener {
            viewModel.stopTimer() // ViewModel에서 타이머 중지
            MapUtils.stopLocationUpdates(fusedLocationClient)
        }

        observePathUpdates()
    }

    private fun observePathUpdates() {
        viewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (isDrawing && ::googleMap.isInitialized) { // googleMap 초기화 여부 확인
                mapFragment.drawPath(pathPoints)
            } else {
                Log.e("HomeFragment", "googleMap is not initialized or not in drawing mode")
            }
        }
    }

    private fun updateMapPath(pathPoints: List<LatLng>) {
        googleMap.clear()
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(5f)
        )
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            LocationUtils.requestLocationPermission(this)
        }
        startLocationUpdates()
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (LocationUtils.hasLocationPermission(requireContext())) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        }
    }

    private val locationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            locationResult.locations.forEach { location ->
                updateLocationUI(location)
            }
        }
    }

    private fun updateLocationUI(location: Location) {
        val latLng = LatLng(location.latitude, location.longitude)
        pathPoints.add(latLng)
        viewModel.addPathPoint(latLng)
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(Color.BLUE).width(5f)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        viewModel.stopTimer() // 타이머 정지
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
