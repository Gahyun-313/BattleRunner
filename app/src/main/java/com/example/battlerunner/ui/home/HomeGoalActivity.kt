// HomeGoalActivity
package com.example.battlerunner.ui.home

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.example.battlerunner.R
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class HomeGoalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var distanceInput: EditText
    private lateinit var confirmBtn: Button
    private lateinit var closeBtn: ImageButton
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_home_goal)

        distanceInput = findViewById(R.id.distanceInput)
        confirmBtn = findViewById(R.id.confirmBtn)
        closeBtn = findViewById(R.id.closeBtn)

        closeBtn.setOnClickListener {
            finish()
        }

        confirmBtn.setOnClickListener {
            val distance = distanceInput.text.toString().toIntOrNull()
            if (distance != null && distance > 0) {
                fetchRecommendedRoute(distance)
            } else {
                Toast.makeText(this, "올바른 거리를 입력해 주세요.", Toast.LENGTH_SHORT).show()
            }
        }

        val mapFragment = supportFragmentManager.findFragmentById(R.id.mapFragmentContainer) as SupportMapFragment
        mapFragment.getMapAsync(this)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        getCurrentLocation()
    }

    private fun getCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                currentLatitude = location.latitude
                currentLongitude = location.longitude

                val currentLocation = LatLng(currentLatitude!!, currentLongitude!!)
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("현재 위치"))
            } else {
                Toast.makeText(this, "현재 위치를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
            }
        }
    }


    // ChatGPT API로 추천 경로를 가져오는 함수
    private fun fetchRecommendedRoute(distance: Int) {
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "추천 경로를 ${distance}km 거리로 알려주세요.")
                        })
                    })
                }

                val client = OkHttpClient()
                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer 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")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                // 로그 추가 - HTTP 상태 코드와 메시지
                println("Response Code: ${response.code}")
                println("Response Message: ${response.message}")
                println("Response Data: $responseData")


                if (response.isSuccessful && responseData != null) {
                    val routeCoordinates = parseRouteCoordinates(responseData,distance)
                    withContext(Dispatchers.Main) {
                        displayRouteOnMap(routeCoordinates)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(this@HomeGoalActivity, "추천 경로를 가져올 수 없습니다. 코드: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeGoalActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace() // 예외 스택 추적 로그 추가
            }
        }
    }


    // JSON 응답에서 경로 좌표를 추출하는 함수 (예시)
    private fun parseRouteCoordinates(responseData: String, distance: Int): List<LatLng> {
        val routeCoordinates = mutableListOf<LatLng>()
        try {
            // ChatGPT의 응답을 JSON으로 파싱
            val jsonObject = JSONObject(responseData)
            val choices = jsonObject.getJSONArray("choices")
            val content = choices.getJSONObject(0).getJSONObject("message").getString("content").trim()

            // 응답의 content에서 좌표 정보를 추출
            val regex = Regex("Latitude: (\\d+\\.\\d+), Longitude: (\\d+\\.\\d+)")
            val matches = regex.findAll(content)

            // 모든 매칭된 좌표를 routeCoordinates에 추가
            for (match in matches) {
                val (lat, lng) = match.destructured
                routeCoordinates.add(LatLng(lat.toDouble(), lng.toDouble()))
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "경로 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
        }

        // 기본 좌표를 반환 (경로가 없을 경우 대비)
        if (routeCoordinates.isEmpty() && currentLatitude != null && currentLongitude != null) {
            val randomBearing = Random.nextDouble(0.0, 360.0) // 0부터 360도 사이의 랜덤 각도
            routeCoordinates.add(LatLng(currentLatitude!!, currentLongitude!!)) // 현재 위치
            routeCoordinates.add(calculateDestination(currentLatitude!!, currentLongitude!!, distance.toDouble(), randomBearing)) // 랜덤 방향 사용
        }


        return routeCoordinates
    }

    private fun calculateDestination(lat: Double, lng: Double, distanceKm: Double, bearing: Double): LatLng {
        val earthRadiusKm = 6371.0
        val distanceRad = distanceKm / earthRadiusKm

        val latRad = Math.toRadians(lat)
        val lngRad = Math.toRadians(lng)
        val bearingRad = Math.toRadians(bearing)

        val newLatRad = Math.asin(
            Math.sin(latRad) * Math.cos(distanceRad) +
                    Math.cos(latRad) * Math.sin(distanceRad) * Math.cos(bearingRad)
        )
        val newLngRad = lngRad + Math.atan2(
            Math.sin(bearingRad) * Math.sin(distanceRad) * Math.cos(latRad),
            Math.cos(distanceRad) - Math.sin(latRad) * Math.sin(newLatRad)
        )

        return LatLng(Math.toDegrees(newLatRad), Math.toDegrees(newLngRad))
    }



    // 지도에 추천 경로를 표시하는 함수
    private fun displayRouteOnMap(routeCoordinates: List<LatLng>) {
        googleMap.clear()
        if (routeCoordinates.isNotEmpty()) {
            val startPoint = routeCoordinates.first()
            val endPoint = routeCoordinates.last()

            googleMap.addMarker(MarkerOptions().position(startPoint).title("출발"))
            googleMap.addMarker(MarkerOptions().position(endPoint).title("도착"))
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 13f))

            for (i in 0 until routeCoordinates.size - 1) {
                val segmentStart = routeCoordinates[i]
                val segmentEnd = routeCoordinates[i + 1]
                googleMap.addPolyline(
                    PolylineOptions().add(segmentStart, segmentEnd).width(5f)
                )
            }

            Toast.makeText(this, "${routeCoordinates.size}개의 추천 경로가 표시되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "추천 경로를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}
