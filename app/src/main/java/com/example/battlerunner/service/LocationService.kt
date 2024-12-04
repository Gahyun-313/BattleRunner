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
import androidx.lifecycle.LifecycleService
import androidx.lifecycle.Observer
import com.example.battlerunner.R
import com.example.battlerunner.ui.home.HomeViewModel
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.utils.LocationUtils
import com.google.android.gms.location.*

// 러닝 중일 때, 앱이 백그라운드에서 돌게 함

class LocationService : LifecycleService() {

    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback

    // 알림 채널 ID
    private val channelId = "running_service_channel"

    // HomeViewModel 인스턴스 가져오기
    private val homeViewModel: HomeViewModel by lazy {
        (application as GlobalApplication).homeViewModel
    }

    // Foreground 서비스가 시작될 때 호출
    override fun onCreate() {
        super.onCreate()
        initializeNotificationChannel() // 알림 채널 생성
        startForegroundService() // Foreground 서비스 시작
        initializeLocationUpdates() // 위치 업데이트 초기화
    }

    // 알림 채널 초기화
    private fun initializeNotificationChannel() {
        val channelName = "Running Tracker"
        val notificationManager = getSystemService(NotificationManager::class.java)

        val channel = NotificationChannel(
            channelId,
            channelName,
            NotificationManager.IMPORTANCE_LOW // 낮은 중요도
        )
        notificationManager.createNotificationChannel(channel)
    }

    // Foreground 서비스 설정
    private fun startForegroundService() {
        // 초기 알림 생성
        val notification = createNotification("러닝 경로가 추적 중입니다.", "시간: 00:00:00\n거리: 0.00 m")
        startForeground(1, notification)

        // Foreground 알림 동적 업데이트 설정
        observeViewModelForNotificationUpdates()
    }

    // 알림 생성 메서드
    private fun createNotification(title: String, content: String): Notification {
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle(title) // 알림 제목
            .setContentText(content) // 알림 내용
            .setSmallIcon(R.drawable.logo1) // 아이콘
            .setPriority(NotificationCompat.PRIORITY_LOW) // 알림 우선순위
            .build()
    }

    // ViewModel 데이터를 관찰하여 알림 업데이트
    private fun observeViewModelForNotificationUpdates() {
        val notificationManager = getSystemService(NotificationManager::class.java)
        homeViewModel.elapsedTime.observe(this, Observer { elapsedTime ->
            val distance = homeViewModel.distance.value ?: 0f

            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            val distanceString = String.format("%.2f m", distance)

            // 알림 내용 업데이트
            val updatedNotification = createNotification("러닝 중", "시간: $timeString\n거리: $distanceString")

            // 알림 갱신
            notificationManager.notify(1, updatedNotification)
        })

        homeViewModel.distance.observe(this, Observer { distance ->
            val elapsedTime = homeViewModel.elapsedTime.value ?: 0L

            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            val timeString = String.format("%02d:%02d:%02d", hours, minutes, seconds)
            val distanceString = String.format("%.2f m", distance)

            // 알림 내용 업데이트
            val updatedNotification = createNotification("러닝 중", "시간: $timeString\n거리: $distanceString")

            // 알림 갱신
            notificationManager.notify(1, updatedNotification)
        })
    }

    // 위치 업데이트 초기화
    private fun initializeLocationUpdates() {
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult) {
                for (location in locationResult.locations) {
                    Log.d("LocationService", "Location: ${location.latitude}, ${location.longitude}")
                }
            }
        }

        val locationRequest = LocationRequest.Builder(Priority.PRIORITY_HIGH_ACCURACY, 1000L)
            .setMinUpdateIntervalMillis(500L)
            .build()

        try {
            if (LocationUtils.hasLocationPermission(this)) {
                fusedLocationClient.requestLocationUpdates(
                    locationRequest,
                    locationCallback,
                    Looper.getMainLooper()
                )
            } else {
                Log.w("LocationService", "Location permissions not granted. Cannot request updates.")
                stopSelf()
            }
        } catch (e: SecurityException) {
            Log.e("LocationService", "SecurityException: ${e.message}")
            stopSelf()
        }
    }

    // 서비스 종료
    override fun onDestroy() {
        super.onDestroy()
        if (::locationCallback.isInitialized) {
            fusedLocationClient.removeLocationUpdates(locationCallback)
        }
        stopForeground(true)
    }

//    override fun onBind(intent: Intent?): IBinder? {
//        return null
//    }
}
