package com.example.battlerunner // 패키지 선언

import android.location.Location // 위치 정보를 사용하기 위해 Location 클래스 임포트
import android.os.Bundle // 번들을 사용하기 위해 임포트
import android.util.Log // 로그를 사용하기 위해 임포트
import android.view.LayoutInflater // 레이아웃 인플레이터 임포트
import android.view.View // 뷰 클래스 임포트
import android.view.ViewGroup // 뷰 그룹 임포트
import android.widget.Toast // 토스트 메시지 임포트
import androidx.fragment.app.Fragment // 프래그먼트를 사용하기 위해 임포트
import com.example.battlerunner.databinding.FragmentMapBinding // 바인딩 임포트
import com.example.battlerunner.utils.LocationUtils // 위치 권한 확인을 위한 LocationUtils 임포트
import com.google.android.gms.location.FusedLocationProviderClient // 위치 제공자 임포트
import com.google.android.gms.location.LocationServices // 위치 서비스 임포트
import com.google.android.gms.maps.CameraUpdateFactory // 카메라 이동을 위한 팩토리 임포트
import com.google.android.gms.maps.GoogleMap // 구글맵 객체 임포트
import com.google.android.gms.maps.OnMapReadyCallback // 맵 준비 콜백 인터페이스 임포트
import com.google.android.gms.maps.SupportMapFragment // 지원맵 프래그먼트 임포트
import com.google.android.gms.maps.model.LatLng // 좌표 객체 임포트

class MapFragment : Fragment(), OnMapReadyCallback { // MapFragment 클래스 선언 및 OnMapReadyCallback 구현

    private var _binding: FragmentMapBinding? = null // 뷰 바인딩을 위한 변수 선언
    private val binding get() = _binding!! // 바인딩 객체 접근을 위한 getter 설정
    private lateinit var googleMap: GoogleMap // GoogleMap 객체 초기화 설정
    private lateinit var fusedLocationClient: FusedLocationProviderClient // 위치 제공자 초기화 설정

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View { // onCreateView 메서드 시작
        _binding = FragmentMapBinding.inflate(inflater, container, false) // 바인딩 초기화
        return binding.root // 바인딩된 뷰 반환
    }

    // 뷰가 생성된 후 호출되는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) { // onViewCreated 메서드 시작
        super.onViewCreated(view, savedInstanceState) // 부모 클래스의 onViewCreated 호출

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity()) // FusedLocationProviderClient 초기화

        val mapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment // 지도 프래그먼트 가져오기
        mapFragment.getMapAsync(this) // 맵 준비되면 콜백 호출

        binding.btnCurrentLocation.setOnClickListener { // 현재 위치 버튼 클릭 리스너 설정
            if (::googleMap.isInitialized) { // googleMap이 초기화되었는지 확인
                moveToCurrentLocation() // 초기화되었다면 현재 위치로 이동
            } else {
                Toast.makeText(requireContext(), "지도가 준비되지 않았습니다.", Toast.LENGTH_SHORT).show() // 지도 준비 안됨 메시지 출력
            }
        }
    }

    override fun onMapReady(map: GoogleMap) { // 맵 준비 시 호출되는 메서드
        googleMap = map // GoogleMap 객체 초기화

        if (LocationUtils.hasLocationPermission(requireContext())) { // 위치 권한 확인
            enableMyLocation() // 위치 권한이 있다면 내 위치 사용 활성화
        } else {
            LocationUtils.requestLocationPermission(this) // 권한 없으면 요청
        }

        val defaultLocation = LatLng(37.222101, 127.187709) // 기본 위치 설정 (명지대학교 5공학관)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f)) // 기본 위치로 카메라 이동
    }

    private fun enableMyLocation() { // 내 위치를 지도에 표시하는 메서드
        try {
            if (LocationUtils.hasLocationPermission(requireContext())) { // 위치 권한이 있는지 확인
                googleMap.isMyLocationEnabled = true // 권한이 있다면 내 위치 표시 활성화
            }
        } catch (e: SecurityException) { // 예외 처리
            Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e) // 로그 출력
            Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show() // 사용자에게 권한 필요 알림
        }
    }

    private fun moveToCurrentLocation() { // 현재 위치로 이동하는 메서드
        if (LocationUtils.hasLocationPermission(requireContext())) { // 위치 권한 확인
            try {
                fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? -> // 마지막 위치 가져오기
                    if (location != null) { // 위치가 존재하는 경우
                        val currentLatLng = LatLng(location.latitude, location.longitude) // 현재 위치 좌표 설정
                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLatLng, 15f)) // 카메라를 현재 위치로 이동
                    } else {
                        Log.e("MapFragment", "현재 위치를 가져올 수 없습니다.") // 위치 정보 없음 로그 출력
                        Toast.makeText(requireContext(), "현재 위치를 가져올 수 없습니다.", Toast.LENGTH_SHORT).show() // 사용자에게 알림
                    }
                }
            } catch (e: SecurityException) { // 예외 처리
                Log.e("MapFragment", "권한 없이 위치 서비스를 사용하려고 시도했습니다.", e) // 예외 로그 출력
                Toast.makeText(requireContext(), "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show() // 사용자에게 권한 필요 알림
            }
        } else {
            LocationUtils.requestLocationPermission(this) // 권한 없을 경우 요청
        }
    }

    override fun onDestroyView() { // 뷰 파괴 시 호출되는 메서드
        super.onDestroyView() // 부모 클래스의 onDestroyView 호출
        _binding = null // 메모리 누수 방지를 위해 바인딩 해제
    }
}
