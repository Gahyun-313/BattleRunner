package com.example.battlerunner

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.battlerunner.databinding.ActivityBattleEndBinding

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // exitBtn 클릭 리스너 설정
        binding.closeBtn.setOnClickListener {
            val intent = Intent()
            setResult(RESULT_OK, intent)
            this.finish()
        }
    }
}