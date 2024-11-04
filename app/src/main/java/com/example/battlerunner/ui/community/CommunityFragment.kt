package com.example.battlerunner.ui.community

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.R
import com.example.battlerunner.databinding.FragmentCommunityBinding
import com.example.battlerunner.databinding.FragmentHomeBinding
import com.example.battlerunner.ui.home.HomeViewModel
import com.google.android.gms.maps.GoogleMap

class CommunityFragment : Fragment(R.layout.fragment_community) {

    private var _binding: FragmentCommunityBinding? = null
    private val binding get() = _binding!!

    // 프래그먼트의 뷰를 생성하는 메서드
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCommunityBinding.inflate(inflater, container, false)
        return binding.root
    }
}