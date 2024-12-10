package com.example.battlerunner.ui.shared

import android.graphics.Bitmap
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.FragmentMapBinding
import com.example.battlerunner.utils.LocationUtils
import com.example.battlerunner.utils.MapUtils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Polygon
import com.google.android.gms.maps.model.PolygonOptions

class MapFragment : Fragment(), OnMapReadyCallback {

    private var _binding: FragmentMapBinding? = null // 뷰 바인딩을 위한 변수
    private val binding get() = _binding!! // 바인딩 객체 접근용
    lateinit var googleMap: GoogleMap // 구글 맵 객체
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치 제공자 클라이언트
    private var onMapReadyCallback: (() -> Unit)? = null
    private lateinit var dbHelper: DBHelper
    lateinit var userId: String

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 위치 제공자 초기화
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // DBHelper 싱글턴 인스턴스 초기화
        dbHelper = DBHelper.getInstance(requireContext())
        userId = dbHelper.getUserId().toString() // 사용자 ID

        // 지도 프래그먼트 설정 및 콜백
        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer)
                as? SupportMapFragment ?: SupportMapFragment.newInstance().also {
            childFragmentManager.beginTransaction().replace(R.id.mapFragmentContainer, it).commitNow()
        }
        mapFragment.getMapAsync(this) // Map 준비가 완료되면 onMapReady 호출

        // custom 내 위치 버튼 리스너
        binding.customLocationButton.setOnClickListener {
            enableMyLocation()
            moveToCurrentLocationImmediate()
        }
    }

    // 구글 맵이 준비되었을 때 호출되는 메서드
    override fun onMapReady(map: GoogleMap) {
        googleMap = map // 구글 맵 초기화

        if (!isAdded) return  // Fragment가 Activity에 연결되었는지 확인

        // TODO: false로 설정해 기본 버튼을 숨기고 customBtn 활성화 해야 하나, customBtn 미작동 이슈로 임시 사용함
        googleMap.uiSettings.isMyLocationButtonEnabled = true // 기본 내 위치 버튼

        if (LocationUtils.hasLocationPermission(requireContext())) {
            enableMyLocation() // 내 위치 활성화
            moveToCurrentLocationImmediate() // 내 위치로 이동
        } else {
            // 기본 위치 = 명지대 5공학관
            val defaultLocation = LatLng(37.222101, 127.187709)
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 15f))

            // 위치 권한 요청
            LocationUtils.requestLocationPermission(this)
        }
        onMapReadyCallback?.invoke() // 콜백 호출
    }

    // 내 위치 활성화 메서드
    fun enableMyLocation() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // 권한 확인
            try {
                googleMap.isMyLocationEnabled = true // 내 위치 표시 활성화
            } catch (e: SecurityException) {
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e)
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한이 없으면 요청
        }
    }

    // 현재 위치로 카메라를 이동하는 메서드
    fun moveToCurrentLocationImmediate() {
        if (LocationUtils.hasLocationPermission(requireContext())) { // 권한을 먼저 확인
            try {
                fusedLocationClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, null)
                    .addOnSuccessListener { location: Location? ->
                        if (location != null) {
                            val currentLatLng = LatLng(location.latitude, location.longitude)
                            googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f))
                        } else {
                            Toast.makeText(requireContext(), "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
                        }
                    }
            } catch (e: SecurityException) {
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // BattleEndActivity에서 그리드 복원
    fun drawGrid(polygons: List<Polygon>, ownershipMap: MutableMap<Int, String>) {

        polygons.forEach { polygon ->

            val polygonId = polygon.tag?.toString()?.toIntOrNull() // 안전하게 변환
            val ownerId = ownershipMap[polygonId] // 소유자 확인

            val fillColor = when (ownerId) {
                // Todo: 아이디 하드코딩!!!!!!!!!!!!!!!!!!!
                "gu20313@naver.com" -> Color.BLUE
                "gus20313@gmail.com" -> Color.RED
                else -> Color.argb(10, 0, 0, 0)
            }

            Log.d("MapFragment", "Polygon ID: ${polygonId}, Owner: $ownerId, userId: $userId")

            // 새로운 Polygon 추가
            googleMap.addPolygon(
                PolygonOptions()
                    .addAll(polygon.points)
                    .strokeWidth(0.5f)
                    .strokeColor(Color.GRAY)
                    .fillColor(fillColor)
            )
        }
    }

    // 지도를 이미지로 저장하는 메서드
    fun takeMapSnapshot(callback: (Bitmap?) -> Unit) {
        googleMap?.snapshot { snapshot ->
            callback(snapshot)
        }
    }

    // 경로 제거 메서드
    fun clearMapPath() {
        if (::googleMap.isInitialized) {
            googleMap.clear() // 지도에 그려진 모든 오버레이(Polyline 포함)를 제거
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        MapUtils.stopLocationUpdates(fusedLocationClient)
        _binding = null
    }

    // moveToCurrentLocation을 안전하게 호출하기 위한 콜백 설정 메서드
    fun setOnMapReadyCallback(callback: () -> Unit) {
        onMapReadyCallback = callback
    }

    // 경로를 그리는 메서드
    fun drawPath(pathPoints: List<LatLng>) {
        if (::googleMap.isInitialized && pathPoints.isNotEmpty()) {
            googleMap.clear() // 기존 경로 제거
            val polylineOptions = MapUtils.createPolylineOptions(pathPoints)
            googleMap.addPolyline(polylineOptions) // 새로운 경로 그리기

        }
    }



}
