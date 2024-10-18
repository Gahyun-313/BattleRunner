package com.example.battlerunner

import com.example.battlerunner.MapFragment
import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.os.Handler
import android.os.Looper
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    // ViewBinding 변수 선언
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    // 타이머 관련 변수
    private var startTime: Long = 0  // 타이머 시작 시간
    private var elapsedTime: Long = 0  // 경과 시간
    private var handler: Handler = Handler(Looper.getMainLooper())  // UI 업데이트를 위한 Handler
    private var isRunning = false  // 타이머 실행 여부 확인

    // 타이머를 주기적으로 업데이트하기 위한 Runnable
    private val timerRunnable = object : Runnable {
        override fun run() {
            if (isRunning) {
                // 경과 시간 계산 (밀리초 단위)
                elapsedTime = System.currentTimeMillis() - startTime

                // 시간, 분, 초 계산
                val seconds = (elapsedTime / 1000) % 60
                val minutes = (elapsedTime / (1000 * 60)) % 60
                val hours = (elapsedTime / (1000 * 60 * 60))

                // 시간, 분, 초 포맷팅하여 화면에 표시
                binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)

                // 1초마다 업데이트
                handler.postDelayed(this, 1000)
            }
        }
    }

    // 프래그먼트가 처음 생성될 때 View를 초기화하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용해 레이아웃을 인플레이트
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    // UI와 상호작용하는 부분을 설정하는 메서드
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 시작 버튼 클릭 리스너
        binding.startBtn.setOnClickListener {
            startTimer()  // 타이머 시작
        }

        // 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            stopTimerAndMoveToNextScreen()  // 타이머 멈추고 다음 화면으로 이동
        }

        // MapFragment 추가
        val mapFragment = MapFragment()
        childFragmentManager.beginTransaction()
            .replace(R.id.fragmentContainer, mapFragment)
            .commit()
    }

    // 타이머 시작 메서드
    private fun startTimer() {
        if (!isRunning) {
            startTime = System.currentTimeMillis()  // 현재 시간 저장
            isRunning = true  // 타이머 실행 상태로 변경
            handler.post(timerRunnable)  // 타이머 실행
        }
    }

    // 타이머 멈추고 다음 화면으로 이동하는 메서드
    private fun stopTimerAndMoveToNextScreen() {
        if (isRunning) {
            isRunning = false  // 타이머 멈춤
            handler.removeCallbacks(timerRunnable)  // 타이머 업데이트 중지

            // 경과 시간을 Intent로 다음 화면에 전달
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java)
            intent.putExtra("elapsedTime", elapsedTime)  // 경과 시간 전달
            startActivity(intent)
        }
    }

    // 프래그먼트가 파괴될 때 호출되는 메서드
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
        handler.removeCallbacks(timerRunnable)  // 타이머 콜백 제거
    }

    // 프래그먼트를 새 인스턴스로 생성할 때 사용
    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    // param1과 param2를 저장하는 부분 (현재 사용하지 않음)
                    // putString(ARG_PARAM1, param1)
                    // putString(ARG_PARAM2, param2)
                }
            }
    }
}
