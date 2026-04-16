package com.example.lingo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView

class MainActivity : ComponentActivity() {

    private var showingFront = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        val startQuizBtn = findViewById<View>(R.id.startQuizBtn)
        val frontWord = findViewById<TextView>(R.id.frontWord)
        val backWord = findViewById<TextView>(R.id.backWord)
        val frontSentence = findViewById<TextView>(R.id.frontSentence)
        val backSentence = findViewById<TextView>(R.id.backSentence)

        startQuizBtn.setOnClickListener {
            val intent = Intent(this, QuizActivity::class.java)
            intent.putExtra(QuizActivity.EXTRA_FLASHCARD_WORD, frontWord.text.toString())
            intent.putExtra(QuizActivity.EXTRA_FLASHCARD_TRANSLATION, backWord.text.toString())
            intent.putExtra(QuizActivity.EXTRA_FLASHCARD_SENTENCE, frontSentence.text.toString())
            intent.putExtra(QuizActivity.EXTRA_TRANSLATED_SENTENCE, backSentence.text.toString())
            startActivity(intent)
        }

        val flashcard = findViewById<CardView>(R.id.flashcard)
        val flashcardFront = findViewById<View>(R.id.flashcard_front)
        val flashcardBack = findViewById<View>(R.id.flashcardBack)

        flashcard.setOnClickListener {
            showingFront = !showingFront
            if (showingFront) {
                flashcardFront.visibility = View.VISIBLE
                flashcardBack.visibility = View.GONE
            } else {
                flashcardFront.visibility = View.GONE
                flashcardBack.visibility = View.VISIBLE
            }
        }
    }
}
