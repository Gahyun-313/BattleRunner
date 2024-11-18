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
                fetchChatGPTRouteRecommendation(distance) // ChatGPT 호출
                //fetchAIRecommendedRoute(distance)
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



    private fun fetchChatGPTRouteRecommendation(distance: Int) {
        val openAiApiKey = "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" // 올바른 API 키
        val url = "https://api.openai.com/v1/chat/completions"

        val requestBody = JSONObject().apply {
            put("model", "gpt-3.5-turbo")
            put("messages", JSONArray().apply {
                put(JSONObject().apply {
                    put("role", "system")
                    put("content", "당신은 경로 추천을 담당하는 AI입니다.")
                })
                put(JSONObject().apply {
                    put("role", "user")
                    put("content", "사용자가 현재 위치에서 $distance km 거리를 걷고 싶어합니다. 추천 경로를 생성해주세요.")
                })
            })
            put("max_tokens", 100)
            put("temperature", 0.7)
        }

        CoroutineScope(Dispatchers.IO).launch {
            try {
                val client = OkHttpClient()
                val body = RequestBody.create("application/json".toMediaTypeOrNull(), requestBody.toString())
                val request = Request.Builder()
                    .url(url)
                    .addHeader("Authorization", "Bearer $openAiApiKey")
                    .post(body)
                    .build()

                val response = client.newCall(request).execute()
                val responseData = response.body?.string()

                if (response.isSuccessful && responseData != null) {
                    try {
                        val jsonObject = JSONObject(responseData)
                        val choices = jsonObject.getJSONArray("choices")
                        val content = choices.getJSONObject(0).getJSONObject("message").getString("content").trim()

                        withContext(Dispatchers.Main) {
                            Toast.makeText(this@HomeGoalActivity, "추천 경로: $content", Toast.LENGTH_LONG).show()
                        }
                    } catch (e: JSONException) {
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HomeGoalActivity,
                                "JSON 파싱 오류: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                    }
                } else {
                    withContext(Dispatchers.Main) {
                        Toast.makeText(
                            this@HomeGoalActivity,
                            "AI 경로 추천 실패: ${response.code}\nResponse Body: $responseData",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    Toast.makeText(this@HomeGoalActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }



    /*
        // ChatGPT API로 추천 경로를 가져오는 함수
        private fun fetchAIRecommendedRoute(distance: Int) {
            if (currentLatitude == null || currentLongitude == null) {
                Toast.makeText(this, "현재 위치를 확인할 수 없습니다.", Toast.LENGTH_SHORT).show()
                return
            }

            val originLat = currentLatitude!!
            val originLng = currentLongitude!!
            val apiKey = "AIzaSyC21ZOMbiLf9LxoZ4V4dBqd-Nddfd-dlIc" // Google Maps API 키를 입력하세요.

            // 목적지 좌표 계산 (테스트용, 원하는 방식으로 변경 가능)
            val destinationLat = originLat + 0.01
            val destinationLng = originLng + 0.01

            val url = "https://maps.googleapis.com/maps/api/directions/json?" +
                    "origin=$originLat,$originLng&destination=$destinationLat,$destinationLng&mode=walking&key=$apiKey"

            println("Directions API URL: $url") // 요청 URL 디버깅 로그

            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()
                    val request = Request.Builder().url(url).build()
                    val response = client.newCall(request).execute()
                    val responseData = response.body?.string()

                    println("API Response Data: $responseData") // 응답 데이터 출력


                    if (response.isSuccessful && responseData != null) {
                        val jsonObject = JSONObject(responseData)
                        val status = jsonObject.getString("status")
                        println("API Response Status: $status") // 상태 출력
                        if (status == "OK") {
                            val routeCoordinates = parseDirectionsApiResponse(responseData)
                            withContext(Dispatchers.Main) {
                                displayRouteOnMap(routeCoordinates)
                            }
                        } else {
                            val errorMessage = jsonObject.optString("error_message", "알 수 없는 오류")
                            println("Error Message: $errorMessage") // 에러 메시지 출력
                            withContext(Dispatchers.Main) {
                                Toast.makeText(
                                    this@HomeGoalActivity,
                                    "경로를 찾을 수 없습니다. 상태: $status, 오류: $errorMessage",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                        }
                    } else {
                        println("Network Error: ${response.code}") // HTTP 상태 코드 출력
                        withContext(Dispatchers.Main) {
                            Toast.makeText(
                                this@HomeGoalActivity,
                                "네트워크 오류: ${response.code}",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                } catch (e: Exception) {
                    withContext(Dispatchers.Main) {
                        println("API Request Error: ${e.message}") // 에러 로그
                        Toast.makeText(this@HomeGoalActivity, "오류 발생: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }*/

    private fun parseDirectionsApiResponse(responseData: String): List<LatLng> {
        val routeCoordinates = mutableListOf<LatLng>()
        try {
            val jsonObject = JSONObject(responseData)
            val routes = jsonObject.getJSONArray("routes")
            println("Routes found: ${routes.length()}") // 경로 수 출력
            if (routes.length() > 0) {
                val firstRoute = routes.getJSONObject(0)
                val overviewPolyline = firstRoute.getJSONObject("overview_polyline")
                val points = overviewPolyline.getString("points")
                println("Polyline points: $points") // Polyline 데이터 출력
                routeCoordinates.addAll(decodePolyline(points))
            } else {
                println("Routes 배열이 비어 있습니다.")
            }
        } catch (e: JSONException) {
            println("JSON Parsing Error: ${e.message}") // JSON 파싱 오류 출력
            e.printStackTrace()
        }
        return routeCoordinates
    }




    /*private fun parseDirectionsApiResponse(responseData: String): List<LatLng> {
        val routeCoordinates = mutableListOf<LatLng>()

        try {
            val jsonObject = JSONObject(responseData)
            val routes = jsonObject.getJSONArray("routes")
            if (routes.length() > 0) {
                val firstRoute = routes.getJSONObject(0)
                val overviewPolyline = firstRoute.getJSONObject("overview_polyline")
                val points = overviewPolyline.getString("points")

                // Polyline 데이터를 LatLng 리스트로 변환
                routeCoordinates.addAll(decodePolyline(points))
            }
        } catch (e: JSONException) {
            e.printStackTrace()
        }

        return routeCoordinates
    }*/

    private fun decodePolyline(encoded: String): List<LatLng> {
        val poly = ArrayList<LatLng>()
        var index = 0
        val len = encoded.length
        var lat = 0
        var lng = 0

        while (index < len) {
            var b: Int
            var shift = 0
            var result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlat = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lat += dlat

            shift = 0
            result = 0
            do {
                b = encoded[index++].code - 63
                result = result or (b and 0x1f shl shift)
                shift += 5
            } while (b >= 0x20)
            val dlng = if (result and 1 != 0) (result shr 1).inv() else result shr 1
            lng += dlng

            val p = LatLng(lat / 1E5, lng / 1E5)
            poly.add(p)
        }

        return poly
    }






    private fun validateAndFixJson(responseData: String): String? {
        return if (responseData.trim().endsWith("}")) {
            responseData // JSON이 올바르게 닫혀 있음
        } else {
            null // 잘린 데이터로 간주
        }
    }




    private fun parseRouteCoordinates(responseData: String): List<LatLng> {
        val routeCoordinates = mutableListOf<LatLng>()

        try {
            // JSON 응답 파싱
            val jsonObject = JSONObject(responseData)
            val choices = jsonObject.getJSONArray("choices")
            val content = choices.getJSONObject(0).getJSONObject("message").getString("content")

            // content를 JSON Object로 변환
            val contentJson = JSONObject(content)
            val routesArray = contentJson.getJSONArray("routes")

            // 첫 번째 route 처리
            if (routesArray.length() > 0) {
                val firstRoute = routesArray.getJSONObject(0)

                // "coordinates" 키에 좌표 배열이 존재
                if (firstRoute.has("coordinates")) {
                    val coordinatesArray = firstRoute.getJSONArray("coordinates")

                    // Coordinates 추가
                    for (i in 0 until coordinatesArray.length()) {
                        val coordinateArray = coordinatesArray.getJSONArray(i)
                        val lat = coordinateArray.getDouble(0) // 첫 번째 값: 위도
                        val lng = coordinateArray.getDouble(1) // 두 번째 값: 경도
                        routeCoordinates.add(LatLng(lat, lng))
                    }
                } else {
                    throw JSONException("coordinates 데이터를 찾을 수 없습니다.")
                }
            } else {
                throw JSONException("routes 배열이 비어 있습니다.")
            }
        } catch (e: JSONException) {
            e.printStackTrace()
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(
                    this@HomeGoalActivity,
                    "추천 경로 데이터를 불러올 수 없습니다: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
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
            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(startPoint, 15f))

            val polylineOptions = PolylineOptions().addAll(routeCoordinates).width(10f)
            googleMap.addPolyline(polylineOptions)

            // 디버깅 로그 추가
            println("Polyline coordinates: $routeCoordinates")
            Toast.makeText(this, "경로가 표시되었습니다.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(this, "경로 데이터를 찾을 수 없습니다.", Toast.LENGTH_SHORT).show()
        }
    }




    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1
    }
}