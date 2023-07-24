package com.example.tetrisdeneme

import android.app.AlertDialog
import android.app.Dialog
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.GridView
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import java.util.*

import kotlinx.coroutines.*

class MainActivity : AppCompatActivity() {

    companion object {
         var gameIsRunning = false
    }
    private var gameIsFinished = false

    private var gameCoroutine: Job? = null
    private val numColumns = 8
    private val numRows = 10
    private lateinit var gridView: GridView
    private lateinit var adapter: GridAdapter
    private lateinit var wordTextView: TextView
    private lateinit var buttonCheck: ImageButton
    private lateinit var buttonDelete: ImageButton
    private lateinit var pauseButton: ImageButton
    private lateinit var restartButton: ImageButton
    private lateinit var continueButton: ImageButton
    private lateinit var pointsTextView: TextView
    private var gameSpeed: Long = 5000 // Başlangıç hızı 5 saniye

    private var wrongAttempt = 0
    private val random = Random()

    private var score = 0
    private var tempscore = 0

    private val words = hashSetOf<String>()
    private val wordList = mutableListOf<String>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        gridView = findViewById(R.id.gridview)
        adapter = GridAdapter(this, numColumns, numRows)
        gridView.adapter = adapter

        wordTextView = findViewById(R.id.word_textview)
        buttonCheck = findViewById(R.id.btnCheck)
        buttonDelete = findViewById(R.id.btnDelete)
        pointsTextView = findViewById(R.id.pointsTextView)
        pauseButton = findViewById(R.id.pauseButton)
        restartButton = findViewById(R.id.restrartButton)
        continueButton = findViewById(R.id.continueButton)

        // dosyadan kelime okuma
        wordList.addAll(loadWordsFromAssets(this, "turkce_kelime_listesi.txt"))
        words.addAll(wordList)

        // silme butonu dinleyicisi
        buttonDelete.setOnClickListener {
                var word = wordTextView.text.toString()
                if (word.isNotEmpty()) {
                    tempscore -= adapter.letterScores[word.last().toString()] ?: 0
                    word = word.substring(0, word.length - 1)
                    wordTextView.text = word

                    val lastPosition = adapter.selectedLetters.size - 1
                    val lastDeletedPosition = adapter.selectedLetters[lastPosition]

                    val deletedTextView = gridView.getChildAt(lastDeletedPosition) as TextView
                    deletedTextView.alpha = 1.0f

                    adapter.removeLastLetter()
                }
        }


        // kontrol butonu dinleyicisi
        buttonCheck.setOnClickListener {
            val word = wordTextView.text.toString()
            if (word.isNotEmpty()) {
                wordTextView.text = ""
//                val isWordInSet = words.contains(word.lowercase(Locale.ROOT))
                val isWordInSet = words.contains(word.lowercase(Locale.forLanguageTag("tr-TR")))

                Log.d("isWordInSet", isWordInSet.toString())
                Log.d("word", word.lowercase())

                if (isWordInSet) {
                    score += tempscore
                    pointsTextView.text = "Puan: $score"
                    gameSpeed = calculateGameSpeed (score)
                    Log.d("gameSpeed", gameSpeed.toString())
                    tempscore = 0
                    adapter.removeItemFromGrid()
                    adapter.clearSelectedLetters()
                    wrongAttempt = 0

                } else {
                    tempscore = 0
                    adapter.clearSelectedLetters()
                    wrongAttempt++

                }
            }
            clearOpacities()
        }

        restartButton.setOnClickListener {
            if (gameIsFinished) {
                showAlertDialog("Yeni oyun başlatmak ister misiniz?", View.OnClickListener {
                    clearGame()
                    startGame()

                })
            }
            else {
                showAlertDialog("Oyun bitmedi, yeniden başlatmak ister misiniz?", View.OnClickListener {
                    clearGame()
                    startGame()
                })
            }

        }

        pauseButton.setOnClickListener {
            pauseButton.visibility = View.INVISIBLE
            continueButton.visibility = View.VISIBLE
            if(!gameIsFinished){
                gameIsRunning = !gameIsRunning
            }
        }
        continueButton.setOnClickListener{
            pauseButton.visibility = View.VISIBLE
            continueButton.visibility = View.INVISIBLE
            if(!gameIsFinished){
                gameIsRunning = !gameIsRunning
            }
        }

        startGame()

    }
    private fun clearGame() {
        gameIsFinished = false
        gameIsRunning = false
        gameCoroutine?.cancel()
        wordTextView.text = ""
        pointsTextView.text = "Puan: 0"
        adapter.clearSelectedLetters()
        score = 0
        gameSpeed = 5000
        wrongAttempt = 0
        pauseButton.visibility = View.VISIBLE
        continueButton.visibility = View.INVISIBLE

        clearOpacities()
    }

    private fun clearOpacities() {
        for (i in 0 until gridView.childCount) {
            val textView = gridView.getChildAt(i) as? TextView
            if (textView != null && textView.alpha == 0.5f) {
                textView.alpha = 1.0f
            }
        }
    }

    private fun showAlertDialog(title: String, positiveClickListener: View.OnClickListener) {
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle(title)
            .setNegativeButton("Hayır", null)
            .setPositiveButton("Evet") { _,_ ->
                positiveClickListener.onClick(null)
                gameIsRunning = true
            }
            .setOnDismissListener {
                gameIsRunning = true
            }
            .show()
        gameIsRunning = false
    }


    //   kelimeleri dosyadan okuma
    private fun loadWordsFromAssets(context: Context, fileName: String): List<String> {
        val wordList = mutableListOf<String>()
        context.assets.open(fileName).bufferedReader().useLines { lines ->
            lines.forEach {
                wordList.add(it)
            }
        }
        return wordList
    }

    fun setAnsWord(letter: String) {
        val currentAnsWord = wordTextView.text.toString()
        val updatedAnsWord = currentAnsWord + letter
        wordTextView.text = updatedAnsWord

        tempscore += adapter.letterScores[letter] ?: 0
        Log.d("tempscore", tempscore.toString())

    }

    private fun initGrid() {

        for (col in 0 until numColumns) {
            for (row in 0 until numRows) {
                adapter.setGridItem(col, row, "")
            }
        }
        for (col in 0 until numColumns) {
            for (row in numRows - 3 until numRows) {
                val letter = getRandomLetter()
                Log.d("letter", letter)
                adapter.setGridItem(col, row, letter)
            }
        }
    }

    private fun startGame() {

        initGrid()
        gameIsRunning = true

        gameCoroutine = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                delay(gameSpeed)
                if (gameIsRunning and (wrongAttempt >= 3)) {
                    dropRowBox()
                    wrongAttempt = 0
                }
                else if (gameIsRunning){
                    dropBox()
                }
                if (gameIsFinished) {
                    break
                }
            }
        }
    }

    private fun calculateGameSpeed(points: Int): Long {
        return when (points) {
            in 0..5 -> 5000
            in 5..10 -> 4000
            in 10..15 -> 3000
            in 15..20 -> 2000
            else -> 1000
        }
    }

    private fun getRandomLetter(): String {
        val mostUsedLetters = listOf("A", "E", "I", "İ", "N", "R", "S", "T")
        val otherVowels = listOf("O", "Ö", "U", "Ü")
        val otherSilentLetters = listOf("B", "C", "Ç", "D", "F", "G", "Ğ", "H", "J", "K", "L", "M", "P", "Ş", "V", "Y", "Z")

        val letter = when (random.nextInt(10)) {
            in 0..3 -> mostUsedLetters.random()        // %40
            in 4..5 -> otherVowels.random()            // %20
            else -> otherSilentLetters.random()              // %40
        }
        Log.d("letter", letter)
        return letter
    }

    private fun dropRowBox() {
        for (col in 0 until numColumns) {
            val letter = getRandomLetter()
            var row = numRows - 1
            while (row >= 0 && !adapter.getGridItem(col, row).isEmpty()) {
                row--
            }
            if (row >= 0) {
                adapter.setGridItem(col, row, letter)
            }
        }
        wrongAttempt = 0
        checkGameIsFinished()
    }

    private fun dropBox() {
        val column = random.nextInt(numColumns)
        var row = 0
        while (row < numRows - 1 && adapter.getGridItem(column, row + 1).isEmpty()) {
            row++
        }
        val letter = getRandomLetter()
        adapter.setGridItem(column, row, letter)
        checkGameIsFinished()

    }
    private fun checkGameIsFinished() {
        // son sütunun dolu olup olmama kontrolü
        for (col in 0 until numColumns) {
            if (!adapter.getGridItem(col, 0).isEmpty()) {
                gameIsFinished = true
                gameIsRunning = false
//                showAlertDialog("Oyun bitti, yeniden başlatmak ister misiniz?", View.OnClickListener {
//                    clearGame()
//                    startGame()
//                })
                val intent = Intent(this, ResultActivity::class.java)
                intent.putExtra("score", score)
                startActivity(intent)
            }
        }
        Log.d("gameIsFinished", gameIsFinished.toString())
        Log.d("gameIsFinished", gameIsRunning.toString())
    }


}
