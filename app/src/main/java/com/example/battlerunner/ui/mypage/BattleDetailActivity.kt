package com.example.battlerunner.ui.mypage

import android.app.AlertDialog
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.data.local.DBHelper
import com.example.battlerunner.databinding.ActivityBattleDetailBinding
import java.io.File

class BattleDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Intent로 받은 배틀 데이터 정보
        val battleDate = intent.getStringExtra("battleDate") ?: return
        val opponentName = intent.getStringExtra("opponentName") ?: return

        // DBHelper에서 배틀 데이터를 가져오기
        val dbHelper = DBHelper.getInstance(this)
        val battleRecord = dbHelper.getBattleRecord(battleDate)

        if (battleRecord != null) {
            showBattleDetails(battleRecord.imagePath, battleDate, opponentName)
        } else {
            showErrorDialog("배틀 기록을 불러올 수 없습니다.")
        }
    }

    private fun showBattleDetails(imagePath: String, date: String, opponentName: String) {
        // 이미지 로드
        val bitmap = BitmapFactory.decodeFile(imagePath)
        if (bitmap != null) {
            binding.battleImage.setImageBitmap(bitmap)
        } else {
            binding.battleImage.setImageResource(R.drawable.placeholder)
        }

        // 텍스트 설정
        binding.battleDate.text = "날짜: $date"
        binding.opponentName.text = "상대 이름: $opponentName"
    }

    private fun showErrorDialog(message: String) {
        AlertDialog.Builder(this)
            .setTitle("Error")
            .setMessage(message)
            .setPositiveButton("확인") { _, _ -> finish() }
            .show()
    }
}
