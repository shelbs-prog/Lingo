package com.example.lingo

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import com.google.android.material.button.MaterialButton

class QuizActivity : ComponentActivity() {

    private lateinit var quizHeader: TextView
    private lateinit var questionLabel: TextView
    private lateinit var questionText: TextView
    private lateinit var resultText: TextView
    private lateinit var nextBtn: MaterialButton
    private lateinit var optionButtons: List<MaterialButton>

    private var currentQuestionIndex = 0
    private var score = 0
    private var answered = false

    private lateinit var questions: List<String>
    private lateinit var choices: List<List<String>>
    private lateinit var correctAnswers: List<String>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_quiz)

        quizHeader = findViewById(R.id.quizHeader)
        questionLabel = findViewById(R.id.questionLabel)
        questionText = findViewById(R.id.questionText)
        resultText = findViewById(R.id.resultText)
        nextBtn = findViewById(R.id.nextBtn)
        optionButtons = listOf(
            findViewById(R.id.option1),
            findViewById(R.id.option2),
            findViewById(R.id.option3),
            findViewById(R.id.option4)
        )

        createQuizData()
        showQuestion()

        nextBtn.setOnClickListener {
            if (!answered) {
                resultText.text = "Pick an answer first."
                resultText.setTextColor(0xFFC62828.toInt())
                return@setOnClickListener
            }

            currentQuestionIndex++

            if (currentQuestionIndex < questions.size) {
                showQuestion()
            } else {
                showFinalScore()
            }
        }
    }

    private fun createQuizData() {
        val word = intent.getStringExtra(EXTRA_FLASHCARD_WORD) ?: "Hola"
        val translation = intent.getStringExtra(EXTRA_FLASHCARD_TRANSLATION) ?: "Hello"

        questions = listOf(
            "What does \"$word\" mean in English?",
            "When would you use \"$word\"?",
            "Which answer matches \"$word\" best?",
            "If someone says \"$word\", what are they doing?",
            "Which word is the English meaning of \"$word\"?"
        )

        choices = listOf(
            listOf("Hello", "Goodbye", "Thanks", "Please"),
            listOf("When greeting someone", "When going to sleep", "When saying sorry", "When saying thank you"),
            listOf("Hello", "Water", "Book", "School"),
            listOf("Greeting someone", "Asking for food", "Saying goodbye", "Talking about time"),
            listOf(translation, "Chair", "Window", "Pencil")
        )

        correctAnswers = listOf(
            "Hello",
            "When greeting someone",
            "Hello",
            "Greeting someone",
            translation
        )
    }

    private fun showQuestion() {
        answered = false
        quizHeader.text = "Quick Quiz"
        questionLabel.text = "Question ${currentQuestionIndex + 1} of ${questions.size}"
        questionText.text = questions[currentQuestionIndex]
        resultText.text = ""
        nextBtn.text = if (currentQuestionIndex == questions.lastIndex) "Show Score" else "Next Question"

        for (i in optionButtons.indices) {
            val button = optionButtons[i]
            button.text = choices[currentQuestionIndex][i]
            button.isEnabled = true
            resetButtonStyle(button)
            button.setOnClickListener {
                checkAnswer(button.text.toString(), button)
            }
        }
    }

    private fun checkAnswer(selectedAnswer: String, selectedButton: MaterialButton) {
        if (answered) return

        answered = true
        val correctAnswer = correctAnswers[currentQuestionIndex]

        for (button in optionButtons) {
            button.isEnabled = false
        }

        if (selectedAnswer == correctAnswer) {
            score++
            resultText.text = "Correct!"
            resultText.setTextColor(0xFF2E7D32.toInt())
            selectedButton.setBackgroundColor(0xFFE8F5E9.toInt())
            selectedButton.strokeColor = ColorStateList.valueOf(0xFF66BB6A.toInt())
            selectedButton.setTextColor(0xFF2E7D32.toInt())
        } else {
            resultText.text = "Incorrect. The correct answer is \"$correctAnswer\"."
            resultText.setTextColor(0xFFC62828.toInt())
            selectedButton.setBackgroundColor(0xFFFFEBEE.toInt())
            selectedButton.strokeColor = ColorStateList.valueOf(0xFFEF5350.toInt())
            selectedButton.setTextColor(0xFFC62828.toInt())

            for (button in optionButtons) {
                if (button.text.toString() == correctAnswer) {
                    button.setBackgroundColor(0xFFE8F5E9.toInt())
                    button.strokeColor = ColorStateList.valueOf(0xFF66BB6A.toInt())
                    button.setTextColor(0xFF2E7D32.toInt())
                }
            }
        }
    }

    private fun showFinalScore() {
        quizHeader.text = "Quiz Complete"
        questionLabel.text = "Final Score"
        questionText.text = "You got $score out of ${questions.size} correct."
        resultText.text = "Tap Finish to go back."
        resultText.setTextColor(0xFF666666.toInt())

        for (button in optionButtons) {
            button.isEnabled = false
            button.text = ""
            button.alpha = 0f
        }

        nextBtn.text = "Finish"
        nextBtn.setOnClickListener {
            finish()
        }
    }

    private fun resetButtonStyle(button: MaterialButton) {
        button.alpha = 1f
        button.setBackgroundColor(0xFFFFFFFF.toInt())
        button.strokeColor = ColorStateList.valueOf(0xFFD7C8F5.toInt())
        button.setTextColor(0xFF4B2E83.toInt())
    }

    companion object {
        const val EXTRA_FLASHCARD_WORD = "extra_flashcard_word"
        const val EXTRA_FLASHCARD_TRANSLATION = "extra_flashcard_translation"
        const val EXTRA_FLASHCARD_SENTENCE = "extra_flashcard_sentence"
        const val EXTRA_TRANSLATED_SENTENCE = "extra_translated_sentence"
    }
}
