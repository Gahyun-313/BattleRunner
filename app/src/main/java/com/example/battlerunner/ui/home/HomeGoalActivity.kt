package com.example.battlerunner.ui.home

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

class HomeGoalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var distanceInput: EditText
    private lateinit var confirmBtn: Button
    private lateinit var closeBtn: ImageButton
    private lateinit var googleMap: GoogleMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_goal) // activity_home_goal 레이아웃 확인

        distanceInput = findViewById(R.id.distanceInput)
        confirmBtn = findViewById(R.id.confirmBtn)
        closeBtn = findViewById(R.id.closeBtn)

        // 'X' 버튼을 눌러 액티비티 종료
        closeBtn.setOnClickListener {
            finish() // HomeFragment로 돌아감
        }

        // '확인' 버튼 클릭 시 추천 경로를 표시
        confirmBtn.setOnClickListener {
            val distance = distanceInput.text.toString().toIntOrNull()
            if (distance != null && distance > 0) {
                showRecommendedRoute(distance)
            } else {
                Toast.makeText(this, "올바른 거리를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        // Google Maps 초기화
        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)

    }
    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        val defaultLocation = LatLng(37.5665, 126.9780) // 서울 시청 좌표 (예시)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(defaultLocation, 12f))
    }

    // 거리(km)에 따라 추천 경로를 지도에 표시하는 함수
    private fun showRecommendedRoute(distance: Int) {
        // 임의의 추천 경로를 지도에 표시 (여기서 ChatGPT API 호출로 경로를 받아올 수 있음)
        val startPoint = LatLng(37.5665, 126.9780) // 서울 시청 좌표
        val endPoint = LatLng(37.5765, 126.9780) // 임의의 종점 좌표

        googleMap.clear()
        googleMap.addMarker(MarkerOptions().position(startPoint).title("출발"))
        googleMap.addMarker(MarkerOptions().position(endPoint).title("도착"))
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 13f))

        // 거리와 함께 추천 경로 표시
        Toast.makeText(this, "$distance km 추천 경로가 표시되었습니다.", Toast.LENGTH_SHORT).show()
    }
}
