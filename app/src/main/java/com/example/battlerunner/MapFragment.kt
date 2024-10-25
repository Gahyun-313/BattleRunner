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

// MapFragment 클래스 선언 및 OnMapReadyCallback 인터페이스 구현
class MapFragment : Fragment(), OnMapReadyCallback {

    // 위치 권한 요청 코드를 상수로 정의
    companion object {
        const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }

    // FragmentMapBinding 변수를 선언 (뷰 바인딩)
    private var _binding: FragmentMapBinding? = null
    // 바인딩을 안전하게 사용하기 위해 직접 접근이 아닌 getter를 사용
    private val binding get() = _binding!!
    // GoogleMap 객체를 늦게 초기화할 수 있도록 lateinit으로 선언
    private lateinit var googleMap: GoogleMap
    // FusedLocationProviderClient를 사용하여 위치 정보를 가져오는 변수 선언
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    // 프래그먼트의 뷰를 생성할 때 호출되는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // FragmentMapBinding을 사용해 뷰를 inflate하고 반환
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드, 주요 초기화 작업 수행
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // FusedLocationProviderClient 초기화, 현재 위치를 가져오기 위해 사용
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // SupportMapFragment를 사용하여 Google Map 프래그먼트를 로드
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        // Google Map이 준비되면 OnMapReadyCallback의 onMapReady를 호출
        mapFragment.getMapAsync(this)

        // 현재 위치로 이동하는 버튼 클릭 리스너 설정
        binding.btnCurrentLocation.setOnClickListener {
            // googleMap이 초기화되었는지 확인 후, 현재 위치로 이동
            if (::googleMap.isInitialized) {
                moveToCurrentLocation()
            } else {
                // 지도가 아직 준비되지 않았을 경우 토스트 메시지 출력
                Toast.makeText(requireContext(), "지도가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Google Map이 준비되었을 때 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        // GoogleMap 객체 초기화
        googleMap = map

        // 위치 권한이 있는지 확인
        if (hasLocationPermission()) {
            // 권한이 있으면 내 위치 사용 활성화
            enableMyLocation()
        } else {
            // 권한이 없으면 요청
            requestLocationPermission()
        }

        // 기본 위치를 명지대학교 5공학관으로 설정
        val seoul = LatLng(37.222101, 127.187709)
        // 카메라를 기본 위치로 이동 및 줌 레벨 설정
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(seoul, 12f))
    }

    // 위치 권한이 있는지 확인하는 메서드
    private fun hasLocationPermission(): Boolean {
        // ACCESS_FINE_LOCATION 및 ACCESS_COARSE_LOCATION 권한을 체크하여 반환
        return ActivityCompat.checkSelfPermission(
            requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED ||
                ActivityCompat.checkSelfPermission(
                    requireContext(), Manifest.permission.ACCESS_COARSE_LOCATION
                ) == PackageManager.PERMISSION_GRANTED
    }

    // 내 위치를 지도에 표시하는 메서드
    private fun enableMyLocation() {
        try {
            // 위치 권한이 있으면 내 위치 표시를 활성화
            if (hasLocationPermission()) {
                googleMap.isMyLocationEnabled = true
            }
        } catch (e: SecurityException) {
            // 예외 발생 시 로그와 토스트 메시지 출력
            Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
        }
    }

    // 위치 권한을 요청하는 메서드
    private fun requestLocationPermission() {
        // ACCESS_FINE_LOCATION 및 ACCESS_COARSE_LOCATION 권한을 요청
        ActivityCompat.requestPermissions(
            requireActivity(),
            arrayOf(Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION),
            LOCATION_PERMISSION_REQUEST_CODE
        )
    }

    // 현재 위치로 카메라를 이동하는 메서드
    private fun moveToCurrentLocation() {
        // 위치 권한이 있는지 확인
        if (hasLocationPermission()) {
            try {
                // 마지막 위치 정보를 가져와서 성공 시 카메라를 해당 위치로 이동
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        // 현재 위치 좌표를 LatLng 객체로 생성
                        val currentLatLng = LatLng(location.latitude, location.longitude)
                        // 애니메이션을 사용해 카메라를 현재 위치로 이동
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                    } else {
                        // 위치 정보를 가져오지 못했을 때 로그와 토스트 메시지 출력
                        Log.e("MapFragment", "현재 위치를 가져올 수 없습니다.")
                        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: SecurityException) {
                // 예외 발생 시 로그와 토스트 메시지 출력
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            // 권한이 없을 경우 요청
            requestLocationPermission()
        }
    }

    // 프래그먼트가 파괴될 때 바인딩 객체 해제
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
