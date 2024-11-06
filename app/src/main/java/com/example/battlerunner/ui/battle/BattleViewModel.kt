package com.example.battlerunner

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import android.os.Handler
import android.os.Looper
import com.google.android.gms.maps.model.LatLng

class BattleViewModel : ViewModel() {
    private val _elapsedTime = MutableLiveData<Long>().apply { value = 0L }
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private val _pathPoints = MutableLiveData<MutableList<LatLng>>().apply { value = mutableListOf() }
    val pathPoints: LiveData<MutableList<LatLng>> get() = _pathPoints

    private val _userName = MutableLiveData<String>()
    val userName: LiveData<String> get() = _userName


    private var isRunning = false
    private var startTime: Long = 0
    private var elapsedTimeValue: Long = 0

    fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTimeValue
            isRunning = true
            updateTimer()
        }
    }

    fun stopTimer() {
        isRunning = false
    }

    private fun updateTimer() {
        if (isRunning) {
            elapsedTimeValue = System.currentTimeMillis() - startTime
            _elapsedTime.postValue(elapsedTimeValue)
            Handler(Looper.getMainLooper()).postDelayed({ updateTimer() }, 1000)
        }
    }

    fun addPathPoint(point: LatLng) {
        _pathPoints.value?.apply {
            add(point)
            _pathPoints.postValue(this)
        }
    }

    fun setUserName(name: String) {
        if (_userName.value != name) {
            _userName.postValue(name)
        }
    }



    fun resetTimer() {
        stopTimer()
        elapsedTimeValue = 0
        _elapsedTime.postValue(0)
    }
}
