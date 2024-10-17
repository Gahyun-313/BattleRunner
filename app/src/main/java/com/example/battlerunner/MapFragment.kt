import android.os.Bundle  // 안드로이드 번들 패키지 불러오기
import android.view.LayoutInflater  // LayoutInflater 클래스 불러오기
import android.view.View  // View 클래스 불러오기
import android.view.ViewGroup  // ViewGroup 클래스 불러오기
import androidx.fragment.app.Fragment  // Fragment 클래스 불러오기
import com.example.battlerunner.databinding.FragmentMapBinding

import com.google.android.gms.maps.*  // Google Maps 패키지 불러오기
import com.google.android.gms.maps.model.LatLng  // LatLng 클래스 불러오기 (위치 데이터 클래스)
import com.google.android.gms.maps.model.Marker  // Marker 클래스 불러오기
import com.google.android.gms.maps.model.MarkerOptions  // MarkerOptions 클래스 불러오기

// MapFragment 클래스 정의, Fragment와 OnMapReadyCallback을 구현
internal class MapFragment : Fragment(), OnMapReadyCallback {

    companion object {  // 동반 객체 정의
        const val TAG = "MapFragment"  // 로그 태그 상수 정의
    }

    private var _binding: FragmentMapBinding? = null  // 바인딩을 저장할 변수 (nullable)
    private val binding get() = _binding!!  // _binding이 null이 아님을 보장하기 위한 non-null 타입 반환

    private lateinit var mapView: MapView  // MapView 객체를 저장할 변수 선언
    private lateinit var googleMap: GoogleMap  // GoogleMap 객체를 저장할 변수 선언
    private var currentMarker: Marker? = null  // 현재 위치의 Marker 객체를 저장할 nullable 변수

    // Fragment의 뷰를 생성하는 메서드, 레이아웃을 인플레이트
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMapBinding.inflate(inflater, container, false)  // 레이아웃 인플레이트 및 바인딩 초기화
        return binding.root  // 바인딩의 루트 뷰 반환
    }

    // 뷰가 생성된 후 호출되는 메서드, MapView 초기화
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mapView = binding.mapView  // MapView 바인딩
        mapView.onCreate(savedInstanceState)  // MapView 생명주기 관리 - onCreate 호출
        mapView.getMapAsync(this)  // 지도 준비 콜백 등록 (OnMapReadyCallback)
    }

    /**
     * onMapReady()
     * 지도 준비가 완료되었을 때 호출
     * @param googleMap GoogleMap 객체 (준비된 지도)
     */
    override fun onMapReady(googleMap: GoogleMap) {
        this.googleMap = googleMap  // GoogleMap 객체 할당

        currentMarker = setupMarker(LatLngEntity(37.5562, 126.9724))  // 마커 설정 (기본 위치: 서울역)
        currentMarker?.showInfoWindow()  // 마커 정보 창 표시
    }

    /**
     * setupMarker()
     * 선택한 위치에 마커 설정
     * @param locationLatLngEntity 위치 데이터 객체
     * @return 생성된 마커
     */
    private fun setupMarker(locationLatLngEntity: LatLngEntity): Marker? {
        val positionLatLng = LatLng(locationLatLngEntity.latitude!!, locationLatLngEntity.longitude!!)  // 위치 데이터 LatLng로 변환
        val markerOption = MarkerOptions().apply {
            position(positionLatLng)  // 마커 위치 설정
            title("위치")  // 마커 제목 설정
            snippet("서울역 위치")  // 마커 설명 설정
        }

        googleMap.mapType = GoogleMap.MAP_TYPE_NORMAL  // 지도 유형 설정 (일반 지도)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(positionLatLng, 15f))  // 카메라 위치 이동 및 줌 설정
        googleMap.animateCamera(CameraUpdateFactory.zoomTo(15f))  // 카메라 애니메이션 줌 설정
        return googleMap.addMarker(markerOption)  // 마커 추가 및 반환
    }

    // 프래그먼트가 시작될 때 MapView의 onStart 호출
    override fun onStart() {
        super.onStart()
        mapView.onStart()
    }

    // 프래그먼트가 재개될 때 MapView의 onResume 호출
    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    // 프래그먼트가 일시 중지될 때 MapView의 onPause 호출
    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    // 프래그먼트가 중지될 때 MapView의 onStop 호출
    override fun onStop() {
        super.onStop()
        mapView.onStop()
    }

    // 메모리가 부족할 때 MapView의 onLowMemory 호출
    override fun onLowMemory() {
        super.onLowMemory()
        mapView.onLowMemory()
    }

    // 프래그먼트 뷰가 소멸될 때 MapView의 onDestroy 호출 및 바인딩 해제
    override fun onDestroyView() {
        mapView.onDestroy()
        super.onDestroyView()
        _binding = null  // 바인딩 객체 해제
    }

    /**
     * LatLngEntity 데이터 클래스
     *
     * @property latitude 위도 (예: 37.5562)
     * @property longitude 경도 (예: 126.9724)
     */
    data class LatLngEntity(
        var latitude: Double?,  // 위도 변수 (nullable Double)
        var longitude: Double?  // 경도 변수 (nullable Double)
    )
}
