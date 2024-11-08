//BattleFragment.kt
package com.example.battlerunner

import android.Manifest
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.battlerunner.databinding.FragmentBattleBinding
import com.example.battlerunner.ui.shared.SharedViewModel
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationResult
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.PolylineOptions

class BattleFragment : Fragment(R.layout.fragment_battle), OnMapReadyCallback {

    private var _binding: FragmentBattleBinding? = null
    private val binding get() = _binding!!
    private lateinit var battleViewModel: BattleViewModel
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var googleMap: GoogleMap

    private val sharedViewModel: SharedViewModel by lazy {
        ViewModelProvider(requireActivity()).get(SharedViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBattleBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        battleViewModel = ViewModelProvider(requireActivity()).get(BattleViewModel::class.java)
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireActivity())

        // 초기 버튼 상태 설정: 시작 버튼만 보이도록
        binding.startBtn.visibility = View.VISIBLE
        binding.stopBtn.visibility = View.GONE
        binding.finishBtn.visibility = View.GONE


        val userName = arguments?.getString("userName") ?: battleViewModel.userName.value ?: ""
        binding.title.text = "$userName 님과의 배틀"
        battleViewModel.setUserName(userName)

        battleViewModel.userName.observe(viewLifecycleOwner) { name ->
            binding.appliedUserName.text = name
        }

        sharedViewModel.elapsedTime.observe(viewLifecycleOwner) { elapsedTime ->
            val seconds = (elapsedTime / 1000) % 60
            val minutes = (elapsedTime / (1000 * 60)) % 60
            val hours = (elapsedTime / (1000 * 60 * 60))
            binding.todayTime.text = String.format("%02d:%02d:%02d", hours, minutes, seconds)
        }

        sharedViewModel.hasStarted.observe(viewLifecycleOwner) { hasStarted ->
            if (hasStarted) {
                if (sharedViewModel.isRunning.value == true) {
                    binding.startBtn.visibility = View.GONE
                    binding.stopBtn.visibility = View.VISIBLE
                    binding.finishBtn.visibility = View.VISIBLE
                } else {
                    binding.startBtn.visibility = View.VISIBLE
                    binding.stopBtn.visibility = View.GONE
                    binding.finishBtn.visibility = View.GONE
                }
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }
        sharedViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (!isRunning) {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
                binding.finishBtn.visibility = View.GONE
            }
        }

        battleViewModel.pathPoints.observe(viewLifecycleOwner) { pathPoints ->
            if (::googleMap.isInitialized) {
                googleMap.clear()
                googleMap.addPolyline(
                    PolylineOptions().addAll(pathPoints).color(android.graphics.Color.BLUE).width(5f)
                )
            } else {
                Log.e("BattleFragment", "GoogleMap is not initialized yet")
            }
        }


        binding.startBtn.setOnClickListener {
            sharedViewModel.startTimer()

            // 버튼 상태 변경: 시작 버튼 숨기고 정지 버튼 보이기
            binding.startBtn.visibility = View.GONE
            binding.stopBtn.visibility = View.VISIBLE
            binding.finishBtn.visibility = View.VISIBLE
        }

        // 정지 버튼 클릭 리스너
        binding.stopBtn.setOnClickListener {
            sharedViewModel.stopTimer() // 타이머 정지

            // 버튼 상태 변경: 정지 버튼 숨기고 시작 버튼 보이기
            binding.startBtn.visibility = View.VISIBLE
            binding.stopBtn.visibility = View.GONE
            binding.finishBtn.visibility = View.GONE
        }

        // 종료 버튼 클릭 리스너
        binding.finishBtn.setOnClickListener {
            sharedViewModel.stopTimer() // 타이머 정지
            val intent = Intent(requireActivity(), PersonalEndActivity::class.java).apply {
                putExtra("elapsedTime", sharedViewModel.elapsedTime.value ?: 0L)
                putExtra("userName", binding.title.text.toString())
            }
            startActivityForResult(intent, REQUEST_CODE_PERSONAL_END)
        }





        binding.BattlefinishBtn.setOnClickListener {
            sharedViewModel.stopTimer()
            val intent = Intent(requireActivity(), BattleEndActivity::class.java).apply {
                putExtra("elapsedTime", sharedViewModel.elapsedTime.value ?: 0L)
                putExtra("userName", binding.title.text.toString())
            }
            startActivity(intent)
        }
        sharedViewModel.isRunning.observe(viewLifecycleOwner) { isRunning ->
            if (isRunning) {
                binding.startBtn.visibility = View.GONE
                binding.stopBtn.visibility = View.VISIBLE
            } else {
                binding.startBtn.visibility = View.VISIBLE
                binding.stopBtn.visibility = View.GONE
            }
        }

    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQUEST_CODE_PERSONAL_END && resultCode == Activity.RESULT_OK) {
            sharedViewModel.resetTimer() // 타이머 초기화
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(), Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                1
            )
        }
    }

    override fun onResume() {
        super.onResume()
        if (!::googleMap.isInitialized) {
            val supportMapFragment = childFragmentManager.findFragmentById(R.id.mapFragmentContainer) as? SupportMapFragment
            supportMapFragment?.getMapAsync(this)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        private const val REQUEST_CODE_PERSONAL_END = 1001
    }
}