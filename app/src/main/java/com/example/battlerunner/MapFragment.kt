package com.example.battlerunner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentMapBinding
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import android.location.Location
import android.util.Log
import android.widget.Toast

class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    private var _binding: FragmentMapBinding? = null
    private val binding get() = _binding!!
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // SupportMapFragment 사용
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // 현재 위치로 이동하는 버튼 클릭 리스너
        binding.btnCurrentLocation.setOnClickListener {
            if (::googleMap.isInitialized) {
                moveToCurrentLocation()
            } else {
                Toast.makeText(requireContext(), "지도가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // 위치 권한 확인
        if (hasLocationPermission()) {
            enableMyLocation()
        } else {
            requestLocationPermission()
        }

        // 기본 위치 (서울) 설정
        val seoul = LatLng(37.5665, 126.9780)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))
    }

    private fun hasLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    private fun enableMyLocation() {
        try {
            if (hasLocationPermission()) {
                googleMap.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun requestLocationPermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 현재 위치로 카메라 이동
    private fun moveToCurrentLocation() {
        if (hasLocationPermission()) {
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    } else {
                        Log.e("MapFragment", "현재 위치를 가져올 수 없습니다.")
                        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            requestLocationPermission()  // 권한 요청
        }
    }

    // 생명주기 메서드들 (onDestroyView만 필요)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
