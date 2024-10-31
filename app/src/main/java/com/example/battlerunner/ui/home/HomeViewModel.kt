// HomeViewModel.kt
package com.example.battlerunner.ui.home

import android.location.Location
import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.model.LatLng

class HomeViewModel : ViewModel() {

    private var _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private var _pathPoints = MutableLiveData<List<LatLng>>(emptyList())
    val pathPoints: LiveData<List<LatLng>> get() = _pathPoints

    private var timer: CountDownTimer? = null
    private var isRunning = false

    fun startTimer() {
        if (!isRunning) {
            isRunning = true
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    _elapsedTime.value = (_elapsedTime.value ?: 0) + 1000
                }

                override fun onFinish() {
                    isRunning = false
                }
            }.start()
        }
    }

    fun stopTimer() {
        timer?.cancel()
        isRunning = false
    }

    fun addPathPoint(location: LatLng) {
        val updatedPoints = _pathPoints.value?.toMutableList() ?: mutableListOf()
        updatedPoints.add(location)
        _pathPoints.value = updatedPoints
    }
}
