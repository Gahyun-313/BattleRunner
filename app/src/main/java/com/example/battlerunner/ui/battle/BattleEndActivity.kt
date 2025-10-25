package com.example.battlerunner.ui.battle

import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.GlobalApplication
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.ActivityBattleEndBinding
import com.example.battlerunner.ui.main.MainActivity
import com.example.battlerunner.ui.shared.MapFragment
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Locale

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding

    // 지도 표시용 MapFragment
    private var mapFragment: MapFragment = MapFragment()

    // BattleViewModel 가져오기
    private val battleViewModel: BattleViewModel by lazy {
        (application as GlobalApplication).battleViewModel
    }

    // 현재 배틀의 ID (Intent로 전달받음)
    private var battleId: Long = -1L

    // DBHelper (로컬 로그인 정보 조회용)
    private lateinit var dbHelper: DBHelper

    // 배틀 데이터 저장 디렉 생성 (내부 저장소에 battle_records 폴더를 생성하고 경로 반환)
    private fun getBattleStorageDir(): File {
        val dir = File(filesDir, "battle_records")
        if (!dir.exists()) dir.mkdir() // 폴더 없으면 생성
        return dir
    }

    /**
     * 배틀 결과 지도 스냅샷 저장
     * @param bitmap 지도 캡처 이미지
     * @param opponentName 상대방 이름 (파일명에 포함)
     */
    private fun saveBattleData(bitmap: Bitmap, opponentName: String) {
        // 파일명: 날짜_상대이름.png
        val dateKey = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
            .format(System.currentTimeMillis())
        val dir = getBattleStorageDir()
        val safeOpp = opponentName.replace(Regex("[\\\\/:*?\"<>|\\s]+"), "_")
        val imageFile = File(dir, "${dateKey}_${safeOpp}.png")

        // 이미지지 저장
        FileOutputStream(imageFile).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        Log.d("BattleEndActivity", "이미지 저장 성공: ${imageFile.absolutePath}")
    }

    /**
     * 액티비티 진입 시 실행되는 생명주기 콜백
     * - ViewBinding 설정
     * - 배틀 ID, 유저 ID 확인
     * - 참가자 정보 로드 후 UI 세팅
     */
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // DBHelper 초기화 및 배틀 ID 확인
        dbHelper = DBHelper.getInstance(this)
        battleId = intent.getLongExtra("battleId", -1L)
        if (battleId <= 0L) {
            Toast.makeText(this, "배틀 ID가 없습니다.", Toast.LENGTH_SHORT).show()
            finish(); return
        }

        // 내 ID 가져오기
        val myId = dbHelper.getUserId()?.toString() ?: ""

        // ViewModel에 참가자 정보가 없는 경우 서버에서 새로 요청
        if (battleViewModel.myUserId.value.isNullOrEmpty() ||
            battleViewModel.opponentId.value.isNullOrEmpty()) {
            battleViewModel.loadBattleParticipants(battleId, myId) {
                setupUiAndMap() // 참가자 정보 로딩 후 UI 초기화
            }
        } else {
            setupUiAndMap()
        }
    }

    // 지도 초기화, 소유권 동기화, 승패 계산, 결과 출력 등
    private fun setupUiAndMap() {
        // 상대 이름으로 타이틀 설정
        val oppName = battleViewModel.opponentName.value ?: "상대"
        binding.title.text = "$oppName님과의 배틀 결과"

        // MapFragment 연결
        supportFragmentManager.beginTransaction()
            .replace(R.id.mapContainer, mapFragment)
            .commitNow()

        // MapFragment 준비 후 그리드 표시
        mapFragment.setOnMapReadyCallback {
            // 내 위치 활성화 및 이동
            mapFragment.enableMyLocation()
            mapFragment.moveToCurrentLocationImmediate()

            // 서버에서 소유권 동기화
            battleViewModel.fetchGridOwnership(battleId) { success ->
                if (success) {
                    // 소유권 정보로 그리드 색상 표시
                    mapFragment.drawGrid(battleViewModel.gridPolygons.value ?: emptyList(), battleViewModel.ownershipMap)

                    // 소유권 데이터 기반으로 점령 칸 수 계산
                    val myId = battleViewModel.myUserId.value
                    val oppId = battleViewModel.opponentId.value
                    var myCells = 0
                    var oppCells = 0
                    var neutralCells = 0

                    battleViewModel.ownershipMap.values.forEach { owner ->
                        when (owner) {
                            myId -> myCells++
                            oppId -> oppCells++
                            else -> neutralCells++
                        }
                    }

                    // 승패 결과 문구 생성
                    val myName = battleViewModel.myName.value ?: "나"
                    val resultText = when {
                        myCells > oppCells ->
                            "$myName님의 승리!\n(내 영역 ${myCells}칸 / 상대 ${oppCells}칸)"
                        myCells < oppCells ->
                            "$myName님의 패배...\n(내 영역 ${myCells}칸 / 상대 ${oppCells}칸)"
                        else ->
                            "무승부!\n(내 영역 ${myCells}칸 / 상대 ${oppCells}칸 / 중립 ${neutralCells})"
                    }

                    // 결과 텍스트 표시
                    binding.result.text = resultText
                } else {
                    Toast.makeText(this, "소유권 동기화 실패", Toast.LENGTH_SHORT).show()
                }
            }
        }

        /**
         * 창닫기 버튼 클릭 리스너
         *
         * 지도 스냅샷 저장 -> 마이페이지에서 기록 열람 위함
         * MainActivity로 돌아가 MatchingFragment 실행
         */
        binding.closeBtn.setOnClickListener {
            mapFragment.takeMapSnapshot { bitmap ->
                val opp = battleViewModel.opponentName.value ?: "상대"
                if (bitmap != null) {
                    saveBattleData(bitmap, opp) // 지도 이미지 저장
                    Log.d("BattleEndActivity", "배틀 데이터 저장 완료")
                } else {
                    Log.e("BattleEndActivity", "스냅샷 생성 실패")
                }

                // MainActivity로 복귀 (기존 인스턴스 재활용)
                val intent = Intent(this, MainActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                    putExtra("showMatchingFragment", true)
                }

                // ViewModel 초기화 및 이동
                battleViewModel.clearGrid()
                startActivity(intent)
                finish()
            }
        }
    }
}
