package com.example.tetrisdeneme

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity


class ResultActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_result)

        val scoreTextView = findViewById<TextView>(R.id.scoreTextView)
        val highScoreTextView = findViewById<TextView>(R.id.highScoreTextView)
        val restartButton = findViewById<TextView>(R.id.againButton)

        val score = intent.getIntExtra("score", 0)
        scoreTextView.text = "Puan: $score"

        val settings = getSharedPreferences("GAME_DATA", MODE_PRIVATE)
        var highScore = settings.getInt("HIGH_SCORE", 0)

        if (score > highScore) {
            highScoreTextView.text = "En yüksek puan: $score"
            val editor = settings.edit()
            editor.putInt("HIGH_SCORE", score)
            editor.apply()
        }
        else {
            highScoreTextView.text = "En yüksek puan: $highScore"
        }

        restartButton.setOnClickListener {
        val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }

    }
}