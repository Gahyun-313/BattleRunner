package com.example.battlerunner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTime: Long = 0
    private val handler = Handler(Looper.getMainLooper())

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap
    private val pathPoints = mutableListOf<LatLng>()

    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60))

                binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                handler.postDelayed(this, 1000)
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Log.d("BattleFragment", "BattleFragment has been created")

        val userName = arguments?.getString("userName") ?: ""
        binding.title.text = "$userName 님과의 배틀"

        // 처음에는 정지 버튼을 숨김
        binding.finishBtn.visibility = View.GONE

        // 시작 버튼 클릭 리스너
        binding.startBtn.setOnClickListener {
            startTimer()
            toggleButtonVisibility(isRunning = true)
        }

        // 정지 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            stopTimer()
            toggleButtonVisibility(isRunning = false)
        }


        // BattlefinishBtn 클릭 리스너
        binding.BattlefinishBtn.setOnClickListener {
            val intent = Intent(requireActivity(), BattleEndActivity::class.java)
            intent.putExtra("elapsedTime", elapsedTime)
            intent.putExtra("userName", binding.title.text.toString())
            stopTimerAndReset()  // 타이머 멈추고 초기화
            toggleButtonVisibility(isRunning = false) // 시작 버튼이 보이도록 설정
            startActivity(intent)
        }

        binding.searchBtn.setOnClickListener {
            val matchingFragment = MatchingFragment()
            parentFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, matchingFragment)
                .addToBackStack(null)
                .commit()
        }

        val supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
        supportMapFragment?.getMapAsync(this)
    }

    private fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime
            isRunning = true
            handler.post(timerRunnable)
        }
    }

    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(timerRunnable)
        }
    }
    // 타이머를 멈추고 초기화하는 메서드
    private fun stopTimerAndReset() {
        stopTimer()  // 타이머 중지
        elapsedTime = 0  // 경과 시간 초기화
        binding.todayTime.text = "00:00:00"  // 시간 표시 초기화
    }

    private fun toggleButtonVisibility(isRunning: Boolean) {
        if (isRunning) {
            binding.startBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.VISIBLE
        } else {
            binding.startBtn.visibility = View.VISIBLE
            binding.finishBtn.visibility = View.GONE
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (hasLocationPermission()) {
            try {
                googleMap.isMyLocationEnabled = true
            } catch (e: SecurityException) {
                e.printStackTrace()
            }
        } else {
            requestLocationPermission()
        }
        startLocationUpdates()
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED || ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    private fun startLocationUpdates() {
        val locationRequest = LocationRequest.create().apply {
            interval = 2000
            fastestInterval = 1000
            priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        }
        if (hasLocationPermission()) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, Looper.getMainLooper())
        } else {
            requestLocationPermission()
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
        googleMap.addPolyline(
            PolylineOptions().addAll(pathPoints).color(android.graphics.Color.BLUE).width(5f)
        )
        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18f))
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        handler.removeCallbacks(timerRunnable)
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}