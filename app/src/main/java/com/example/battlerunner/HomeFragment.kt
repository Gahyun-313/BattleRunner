package com.example.battlerunner

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.example.battlerunner.databinding.FragmentHomeBinding

class HomeFragment : Fragment(R.layout.fragment_home) {

    // ViewBinding 변수 선언
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private var param1: String? = null
    private var param2: String? = null

    //MainActivity에서 넘어올 때 초기화
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            //param1 = it.getString(ARG_PARAM1)
            //param2 = it.getString(ARG_PARAM2)
        }
    }

    //초기화(inflate)
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // ViewBinding을 사용해 layout을 인플레이트
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // 종료 버튼 클릭
        binding.finishBtn.setOnClickListener {
            // 개인 러닝 종료 팝업 띄우기 -> PersonalEndActivity 호출
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java)
            startActivity(intent)  // 여기에서 바로 시작
        }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null  // 메모리 누수 방지
    }

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            HomeFragment().apply {
                arguments = Bundle().apply {
                    //putString(ARG_PARAM1, param1)
                    //putString(ARG_PARAM2, param2)
                }
            }
    }
}
