//HomeGoalActivity
package com.example.battlerunner.ui.home

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.location.LocationManager
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
import org.json.JSONException
import org.json.JSONObject

class HomeGoalActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var distanceInput: EditText
    private lateinit var confirmBtn: Button
    private lateinit var closeBtn: ImageButton
    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient

    private var currentLatitude: Double? = null
    private var currentLongitude: Double? = null

    private fun checkLocationEnabled() {
        val locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) &&
            !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
            Toast.makeText(this, "위치 서비스가 비활성화되어 있습니다. 활성화해 주세요.", Toast.LENGTH_SHORT).show()
        }
    }

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
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), LOCATION_PERMISSION_REQUEST_CODE)
            return
        }

        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                val currentLocation = LatLng(location.latitude, location.longitude)
                currentLatitude = location.latitude
                currentLongitude = location.longitude
                googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                googleMap.addMarker(MarkerOptions().position(currentLocation).title("현재 위치"))
            } else {
                // 위치 데이터가 없을 경우 새 위치를 요청
                requestNewLocationData()
            }
        }
    }

    private fun requestNewLocationData() {
        val locationRequest = com.google.android.gms.location.LocationRequest.create().apply {
            interval = 10000
            fastestInterval = 5000
            priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            fusedLocationClient.requestLocationUpdates(locationRequest, object : com.google.android.gms.location.LocationCallback() {
                override fun onLocationResult(locationResult: com.google.android.gms.location.LocationResult) {
                    val location = locationResult.lastLocation
                    if (location != null) {
                        val currentLocation = LatLng(location.latitude, location.longitude)
                        currentLatitude = location.latitude
                        currentLongitude = location.longitude
                        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentLocation, 15f))
                        googleMap.addMarker(MarkerOptions().position(currentLocation).title("현재 위치"))
                    }
                }
            }, null)
        }
    }




    // ChatGPT API로 추천 경로를 가져오는 함수
    private fun fetchRecommendedRoute(distance: Int) {
        if (currentLatitude == null || currentLongitude == null) {
            Toast.makeText(this, "현재 위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val json = JSONObject().apply {
                    put("model", "gpt-3.5-turbo")
                    put("messages", JSONArray().apply {
                        put(JSONObject().apply {
                            put("role", "user")
                            put("content", "현재 위치 (${currentLatitude}, ${currentLongitude})에서 ${distance}km 이내의 추천 경로를 JSON 형태로 좌표 리스트로 제공해 주세요.")
                        })
                    })
                    put("max_tokens", 100)
                    put("temperature", 0.7)
                }

                val apiKey = "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"
                val client = OkHttpClient()
                val requestBody = RequestBody.create("application/json".toMediaTypeOrNull(), json.toString())
                val request = Request.Builder()
                    .url("https://api.openai.com/v1/chat/completions")
                    .addHeader("Authorization", "Bearer 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")
                    .post(requestBody)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                println("Response Code: ${response.code}")
                println("Response Data: $responseData")



                if (response.isSuccessful && responseData != null) {
                    val routeCoordinates = parseRouteCoordinates(responseData)
                    withContext(Dispatchers.Main) {
                        displayRouteOnMap(routeCoordinates)
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        val errorMessage = when (response.code) {
                            400 -> "잘못된 요청입니다."
                            401 -> "API 키가 유효하지 않습니다."
                            500 -> "서버 오류입니다. 잠시 후 다시 시도해주세요."
                            else -> "추천 경로를 가져올 수 없습니다. 코드: ${response.code}"
                        }
                        Toast.makeText(this@HomeGoalActivity, errorMessage, Toast.LENGTH_SHORT).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeGoalActivity, "오류가 발생했습니다: ${e.message}", Toast.LENGTH_SHORT).show()
                }
                e.printStackTrace()
            }
        }
    }








    private fun parseRouteCoordinates(responseData: String): List<LatLng> {
        val routeCoordinates = mutableListOf<LatLng>()

        try {
            val jsonObject = JSONObject(responseData)
            val choices = jsonObject.getJSONArray("choices")
            val content = choices.getJSONObject(0).getJSONObject("message").getString("content")

            // content를 JSONObject로 변환
            val contentJson = JSONObject(content)

            // route 또는 routes 키 확인
            if (contentJson.has("route")) {
                // route 키를 사용
                val routeArray = contentJson.getJSONArray("route")
                for (i in 0 until routeArray.length()) {
                    val point = routeArray.getJSONObject(i)
                    val lat = point.getDouble("latitude")
                    val lng = point.getDouble("longitude")
                    routeCoordinates.add(LatLng(lat, lng))
                }
            } else if (contentJson.has("routes")) {
                // routes 키를 사용
                val routesArray = contentJson.getJSONArray("routes")

                // routes 배열 내부 확인
                for (i in 0 until routesArray.length()) {
                    val element = routesArray.get(i)

                    // 배열 안에 바로 좌표 객체가 있는 경우
                    if (element is JSONObject && element.has("latitude") && element.has("longitude")) {
                        val lat = element.getDouble("latitude")
                        val lng = element.getDouble("longitude")
                        routeCoordinates.add(LatLng(lat, lng))
                    }
                    // 배열 안에 coordinates 배열이 있는 경우
                    else if (element is JSONObject && element.has("coordinates")) {
                        val coordinatesArray = element.getJSONArray("coordinates")
                        for (j in 0 until coordinatesArray.length()) {
                            val point = coordinatesArray.getJSONObject(j)
                            val lat = point.getDouble("latitude")
                            val lng = point.getDouble("longitude")
                            routeCoordinates.add(LatLng(lat, lng))
                        }
                    }
                }
            } else {
                throw JSONException("route 또는 routes 키를 찾을 수 없습니다.")
            }
        } catch (e: Exception) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(this@HomeGoalActivity, "추천 경로 데이터를 처리하는 중 오류가 발생했습니다.", Toast.LENGTH_SHORT).show()
            }
        }

        return routeCoordinates
    }






    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                getCurrentLocation()
            } else {
                Toast.makeText(this, "위치 권한이 필요합니다.", Toast.LENGTH_SHORT).show()
            }
        }
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
