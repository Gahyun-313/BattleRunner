package com.example.battlerunner

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.MapFragment

class BattleFragment : Fragment(R.layout.fragment_battle) {

    // ViewBinding 변수 선언
    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!

    private var isRunning = false  // 타이머 실행 여부
    private var startTime: Long = 0  // 타이머 시작 시간
    private var elapsedTime: Long = 0  // 경과 시간 저장
    private val handler = Handler(Looper.getMainLooper())  // 타이머 업데이트를 위한 핸들러

    // 타이머를 주기적으로 업데이트하기 위한 Runnable
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                elapsedTime = System.currentTimeMillis() - startTime
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60))

                // 경과된 시간을 시:분:초 형식으로 표시
                binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                handler.postDelayed(this, 1000)  // 1초마다 업데이트
            }
        }
    }

    // 프래그먼트가 생성되었을 때 호출되는 메서드
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    // 초기화 (inflate)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용한 inflate
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    // 뷰가 생성된 후 호출되는 메서드 (버튼 리스너 설정)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 시작 버튼 클릭 리스너
        binding.startBtn.setOnClickListener {
            startTimer()
        }

        // 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            stopTimer()
        }

        // Battle 종료 버튼 클릭 시
        // 종료 버튼 클릭 리스너
        binding.BattlefinishBtn.setOnClickListener {
            val intent = Intent(requireActivity(), BattleEndActivity::class.java)
            intent.putExtra("elapsedTime", elapsedTime)  // 경과 시간 전달
            startActivity(intent)
        }


        // MapFragment 추가
        val mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commit()
    }

    // 타이머 시작 함수
    private fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis() - elapsedTime  // 타이머 시작
            isRunning = true
            handler.post(timerRunnable)  // 타이머 동작 시작
        }
    }

    // 타이머 종료 함수
    private fun stopTimer() {
        if (isRunning) {
            isRunning = false
            handler.removeCallbacks(timerRunnable)  // 타이머 중지
        }
    }

    // 뷰가 파괴될 때 호출되는 메서드 (메모리 누수 방지)
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
        handler.removeCallbacks(timerRunnable)
    }
}
