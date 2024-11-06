// SharedViewModel.kt
package com.example.battlerunner.ui.shared

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private var _elapsedTime = MutableLiveData<Long>()
    val elapsedTime: LiveData<Long> get() = _elapsedTime

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

    fun resetTimer() {
        _elapsedTime.value = 0L
    }
}
