//SharedViewModel
package com.example.battlerunner.ui.shared

import android.os.CountDownTimer
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SharedViewModel : ViewModel() {
    private var _elapsedTimeForHome = MutableLiveData<Long>() // HomeFragment 전용 시간
    val elapsedTimeForHome: LiveData<Long> get() = _elapsedTimeForHome

    private var _elapsedTime = MutableLiveData<Long>() // BattleFragment에서도 사용
    val elapsedTime: LiveData<Long> get() = _elapsedTime

    private var timer: CountDownTimer? = null
    private var accumulatedTime: Long = 0L // 누적 시간 저장

    private val _isRunning = MutableLiveData<Boolean>(false)
    val isRunning: LiveData<Boolean> get() = _isRunning

    // 타이머 시작 메서드
    fun startTimer() {
        if (_isRunning.value == false) {
            _isRunning.value = true
            timer = object : CountDownTimer(Long.MAX_VALUE, 1000) {
                override fun onTick(millisUntilFinished: Long) {
                    accumulatedTime += 1000 // 누적 시간을 증가
                    _elapsedTime.value = accumulatedTime // BattleFragment에서 사용
                    _elapsedTimeForHome.value = accumulatedTime // HomeFragment에서 사용
                }

                override fun onFinish() {
                    _isRunning.value = false
                }
            }.start()
        }
    }

    // 타이머 정지 메서드 (누적 시간은 유지)
    fun stopTimer() {
        timer?.cancel()
        _isRunning.value = false
        // accumulatedTime 유지하여 BattleFragment에서 이어갈 수 있도록 함
    }

    // 타이머 및 누적 시간 초기화 메서드
    fun resetTimer() {
        _elapsedTimeForHome.value = 0L
        _elapsedTime.value = 0L
        accumulatedTime = 0L
        _isRunning.value = false
    }
}