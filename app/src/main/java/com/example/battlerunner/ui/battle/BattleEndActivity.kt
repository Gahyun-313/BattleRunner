package com.example.battlerunner.ui.battle

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.ActivityBattleEndBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import com.google.android.gms.maps.model.Polygon
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding
    private var mapFragment: MapFragment = MapFragment()

    // BattleViewModel 가져오기
    private val battleViewModel: BattleViewModel by lazy {
        (application as GlobalApplication).battleViewModel
    }

    // 배틀 데이터 저장 디렉토리 생성 함수
    private fun getBattleStorageDir(): File {
        val dir = File(filesDir, "battle_records")
        if (!dir.exists()) dir.mkdir()
        return dir
    }

    // 배틀 데이터 저장 메서드
    private fun saveBattleData(bitmap: Bitmap, opponentName: String) {
        val dateKey = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault()).format(System.currentTimeMillis())
        val dir = getBattleStorageDir()
        val imageFile = File(dir, "$dateKey.png")

        try {
            // 이미지 저장
            FileOutputStream(imageFile).use {
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
            }
            Log.d("BattleEndActivity", "이미지 저장 성공: ${imageFile.absolutePath}")

            // 데이터베이스에 저장
            val dbHelper = DBHelper.getInstance(this)
            val success = dbHelper.insertBattleRecord(
                endDate = dateKey,
                imagePath = imageFile.absolutePath,
                //TODO 실제 데이터로 대체
                elapsedTime = 0,
                distance = 0f
            )
            if (success) {
                Log.d("BattleEndActivity", "데이터베이스 저장 성공")
            } else {
                Log.e("BattleEndActivity", "데이터베이스 저장 실패")
            }
        } catch (e: Exception) {
            Log.e("BattleEndActivity", "이미지 저장 실패", e)
        }
    }


    @SuppressLint("DefaultLocale", "SetTextI18n")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val battleId = intent.getLongExtra("battleId", -1) // 배틀 Id 받아옴

        // MapFragment 초기화 및 설정
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapFragmentContainer, mapFragment)
            .commitNow()

//        val oppositeName = intent.getStringExtra("oppositeName")
//        val userName = intent.getStringExtra("userName")
//        val userId = intent.getStringExtra("userId")

        // 문자 설정
        binding.title.text = "김가현님과의 배틀 결과" // "{$oppositeName}님과의 배틀 결과"
        // TODO: 승리, 패배 문구 설정!!!!!!!!!!!!!!!!!
        binding.result.text = "sh kim님의 승리"   // "{$userName}님의 승리/패배"

        // MapFragment 준비 후 그리드 표시
        mapFragment.setOnMapReadyCallback {
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()

            battleViewModel.fetchGridOwnership(battleId) { success ->
                if (success) {
                    mapFragment.drawGrid(battleViewModel.gridPolygons.value ?: emptyList(), battleViewModel.ownershipMap)
                }
            }
        }

        // 창닫기 버튼 클릭 리스너
        binding.closeBtn.setOnClickListener {
            mapFragment.takeMapSnapshot { bitmap ->
                if (bitmap != null) {
                    saveBattleData(bitmap, "김가현")
                    Log.d("BattleEndActivity", "배틀 데이터 저장 완료")
                } else {
                    Log.e("BattleEndActivity", "스냅샷 생성 실패 또는 상대 이름 없음")
                }

                // MainActivity -> MatchingFragment 표시하도록 요청
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP // 기존 MainActivity 인스턴스 재사용
                    putExtra("showMatchingFragment", true) // MatchingFragment 표시 요청
                }
                battleViewModel.clearGrid()
                startActivity(intent)
                finish() // Activity 종료
            }
        }
    }
}