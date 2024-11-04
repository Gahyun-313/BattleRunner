// HomeViewModel.kt
package com.example.battlerunner.ui.home

import android.location.Location
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class HomeViewModel : ViewModel() {

    // 경과 시간을 저장하는 LiveData
    private var _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    // 경로의 위치 좌표 리스트를 저장하는 LiveData
    private var _pathPoints = MutableLiveData<List<LatLng>>(emptyList())
    val pathPoints: LiveData<List<LatLng>> get() = _pathPoints

    // 총 러닝 거리를 저장하는 LiveData
    private var _distance = MutableLiveData<Float>(0f) // 초기 값은 0으로 설정
    val distance: LiveData<Float> get() = _distance

    private var timer: CountDownTimer? = null // 타이머 객체
    private var isRunning = false // 러닝 시작 여부를 나타내는 변수
    private var lastLocation: LatLng? = null // 이전 위치를 저장하는 변수

    // 타이머 시작 메서드
    fun startTimer() {
        if (!isRunning) { // 타이머가 이미 실행 중이 아닌 경우에만 시작
            isRunning = true
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = (_elapsedTime.value ?: 0) + 1000 // 1초마다 경과 시간 증가
                }

                override fun onFinish() {
                    isRunning = false // 타이머 종료 시 실행 상태를 false로 설정
                }
            }.start()
        }
    }

    // 타이머 중지 메서드
    fun stopTimer() {
        timer?.cancel() // 타이머 취소
        isRunning = false // 실행 상태를 false로 설정
    }

    // 새로운 위치를 추가하고 거리를 계산하는 메서드
    fun addPathPoint(location: LatLng) {
        if (!isRunning) return // 정지 상태에서는 업데이트하지 않음

        // 이전 위치가 있는 경우, 현재 위치와의 거리를 계산하여 누적
        lastLocation?.let {
            val results = FloatArray(1) // 거리 결과를 저장할 배열
            Location.distanceBetween(
                it.latitude, it.longitude, // 이전 위치 좌표
                location.latitude, location.longitude, // 현재 위치 좌표
                results // 결과 배열에 거리 값 저장
            )
            _distance.value = (_distance.value ?: 0f) + results[0] // 누적 거리 업데이트
        }
        // 현재 위치를 이전 위치로 설정하여 다음 위치 추가 시 사용할 수 있게 함
        lastLocation = location

        // 경로 리스트에 현재 위치를 추가하고 LiveData 업데이트
        val updatedPoints = _pathPoints.value?.toMutableList() ?: mutableListOf()
        updatedPoints.add(location)
        _pathPoints.value = updatedPoints
    }
}

