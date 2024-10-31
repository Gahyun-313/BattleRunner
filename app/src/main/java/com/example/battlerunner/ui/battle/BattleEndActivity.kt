package com.example.battlerunner.ui.battle

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.battlerunner.R
import com.example.battlerunner.databinding.ActivityBattleEndBinding

class BattleEndActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBattleEndBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBattleEndBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}