package com.example.battlerunner.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.Looper
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.battlerunner.R
import com.example.battlerunner.utils.LocationUtils
import com.google.android.gms.location.*

class LocationService : Service() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // Foreground 서비스가 시작될 때 호출
    override fun onCreate() {
        super.onCreate()
        initializeNotificationChannel() // 알림 채널 생성
        startForegroundService() // Foreground 서비스 시작
        initializeLocationUpdates() // 위치 업데이트 초기화
    }

    // 알림 채널 초기화
    private fun initializeNotificationChannel() {
        val channelId = "running_service_channel" // 알림 채널 ID
        val channelName = "Running Tracker" // 알림 채널 이름
        val notificationManager = getSystemService(NotificationManager::class.java)

        // 알림 채널 생성
        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_DEFAULT // 높은 중요도

            //NotificationManager.IMPORTANCE_LOW // 낮은 중요도 (소음 없음)
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Foreground 서비스 설정
    private fun startForegroundService() {
        val channelId = "running_service_channel" // 알림 채널 ID

        // 상태바에 표시될 알림 설정
        val notification: Notification = NotificationCompat.Builder(this, channelId)
            .setContentTitle("러닝 중") // 알림 제목
            .setContentText("러닝 경로가 추적 중입니다.") // 알림 내용
            .setSmallIcon(R.drawable.logo1) // 테스트용 기본 아이콘
            .setPriority(NotificationCompat.PRIORITY_LOW) // 알림 우선순위
            .build()

        // 알림 생성 완료 로그
        Log.d("LocationService", "알림 생성 완료.")

        // Foreground 서비스 시작
        startForeground(1, notification)

        // Foreground 서비스 시작 로그
        Log.d("LocationService", "Foreground 서비스가 시작되었습니다.")
    }

    // 위치 업데이트 초기화
    private fun initializeLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    // 위치 업데이트 처리
                    Log.d("LocationService", "Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()

        try {
            // 권한이 승인되었는지 확인
            if (LocationUtils.hasLocationPermission(this)) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Log.w("LocationService", "Location permissions not granted. Cannot request updates.")
                stopSelf() // 권한 없으면 서비스 종료
            }
        } catch (e: SecurityException) {
            // 권한 부족으로 발생하는 예외 처리
            Log.e("LocationService", "SecurityException: ${e.message}")
            stopSelf() // 예외 발생 시 서비스 종료
        }
    }

    // 서비스 종료
    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback) // 위치 업데이트 중지
        }
        stopForeground(true) // Foreground 서비스를 종료
    }

    override fun onBind(intent: Intent?): IBinder? {
        // 이 서비스는 바인딩되지 않으며, Foreground 서비스로만 동작
        return null
    }
}
