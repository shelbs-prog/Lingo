package com.example.lingo

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.activity.ComponentActivity
import androidx.activity.enableEdgeToEdge
import androidx.cardview.widget.CardView
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL


class MainActivity : ComponentActivity() {

    private var showingFront = true

    private lateinit var progressCompleted: TextView
    private lateinit var progressLastScore: TextView
    private lateinit var progressStreak: TextView


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)


        // Connecting Kotlin variables to the XML views
        val startQuizBtn = findViewById<MaterialButton>(R.id.startQuizBtn)
        val aiBtn = findViewById<MaterialButton>(R.id.generateExampleBtn)
        val aiCard = findViewById<MaterialCardView>(R.id.aiExampleCard)
        val aiStatus = findViewById<TextView>(R.id.aiStatusText)
        val aiSpanish = findViewById<TextView>(R.id.aiSpanishText)
        val aiEnglish = findViewById<TextView>(R.id.aiEnglishText)

        val frontWord = findViewById<TextView>(R.id.frontWord)
        val backWord = findViewById<TextView>(R.id.backWord)
        val frontSentence = findViewById<TextView>(R.id.frontSentence)
        val backSentence = findViewById<TextView>(R.id.backSentence)

        val flashcard = findViewById<CardView>(R.id.flashcard)
        val flashcardFront = findViewById<View>(R.id.flashcard_front)
        val flashcardBack = findViewById<View>(R.id.flashcardBack)

        progressCompleted = findViewById(R.id.progressCompleted)
        progressLastScore = findViewById(R.id.progressLastScore)
        progressStreak = findViewById(R.id.progressStreak)
        updateProgress()



        startQuizBtn.setOnClickListener {
            val quizIntent = Intent(this, QuizActivity::class.java)
            quizIntent.putExtra(QuizActivity.EXTRA_FLASHCARD_WORD, frontWord.text.toString())
            quizIntent.putExtra(QuizActivity.EXTRA_FLASHCARD_TRANSLATION, backWord.text.toString())
            quizIntent.putExtra(
                QuizActivity.EXTRA_FLASHCARD_SENTENCE,
                frontSentence.text.toString()
            )
            quizIntent.putExtra(
                QuizActivity.EXTRA_TRANSLATED_SENTENCE,
                backSentence.text.toString()
            )
            startActivity(quizIntent)
        }

        // Sends the AI request when the AI button has been clicked

        aiBtn.setOnClickListener {
            val word = frontWord.text.toString()
            val translation = backWord.text.toString()

            aiCard.visibility = View.VISIBLE
            aiStatus.visibility = View.VISIBLE
            aiStatus.text = "Generating an example..."
            aiSpanish.text = ""
            aiEnglish.text = ""
            aiBtn.isEnabled = false

            // Error checking to ensure API key is correct

            if (BuildConfig.OPENAI_API_KEY.isBlank()) {
                aiStatus.text = "Add OPENAI_API_KEY to local.properties to use the AI feature."
                aiBtn.isEnabled = true
                return@setOnClickListener
            }

            // Thread allows for increased app stability.
            Thread {

                // Connects to OpenAI's endpoint

                try {
                    val url = URL("https://api.openai.com/v1/responses")
                    val connection = url.openConnection() as HttpURLConnection

                    // Sending data to the server

                    connection.requestMethod = "POST"


                    // API Authorization
                    connection.setRequestProperty(
                        "Authorization",
                        "Bearer ${BuildConfig.OPENAI_API_KEY}"
                    )

                    // Notifies the service of the incoming format
                    connection.setRequestProperty("Content-Type", "application/json")
                    connection.doOutput = true

                    // Giving instruction to the AI
                    val prompt = """
                        Create one very short beginner Spanish sentence using the word "$word".
                        The English meaning of "$word" is "$translation".
                        Keep the vocabulary simple for a new learner.
                        Respond in exactly this format:
                        Spanish: <sentence>
                        English: <translation>
                    """.trimIndent()


                    //  Creates the JSON object for travel

                    val body = JSONObject()

                    // Adds the Model and INPUT into the JSON
                    // Think of this as

                    // {"model": "gpt-5-mini",
                    // "input": "Create one very short beginner spanish sentence."}

                    body.put("model", BuildConfig.OPENAI_MODEL)
                    body.put("input", prompt)

                    // This is the part that actually sends the JSON and proceeds into it's conversion

                    OutputStreamWriter(connection.outputStream).use { writer ->
                        writer.write(body.toString())
                        writer.flush()
                    }

                    // This gets the HTTP status code
                    // Think of {"200 = success"} {"401 = unauth / bad key"} {"429 = rate limit"}

                    val responseCode = connection.responseCode

                    // Chooses which stream to read from

                    val stream =
                        if (responseCode in 200..299) connection.inputStream else connection.errorStream

                    // Reads line by line to return the final full string

                    val responseText = BufferedReader(InputStreamReader(stream)).use { reader ->
                        val text = StringBuilder()
                        var line: String?
                        while (reader.readLine().also { line = it } != null) {
                            text.append(line)
                        }
                        text.toString()
                    }


                    // Error handling

                    if (responseCode !in 200..299) {
                        runOnUiThread {
                            aiStatus.text =
                                "The AI request failed. Check your API key and internet connection."
                            aiBtn.isEnabled = true
                        }
                        return@Thread
                    }


                    // This line allows for Kotlin inspection
                    val json = JSONObject(responseText)
                    var outputText = ""


                    // Checks if OPENAI - Gave us the result directly in output_text

                    if (json.has("output_text")) {
                        outputText = json.getString("output_text")
                    } else {

                        // Allows for deep inspection

                        val outputArray = json.optJSONArray("output") ?: JSONArray()
                        for (i in 0 until outputArray.length()) {
                            val outputItem = outputArray.optJSONObject(i) ?: continue
                            val contentArray = outputItem.optJSONArray("content") ?: continue
                            for (j in 0 until contentArray.length()) {
                                val contentItem = contentArray.optJSONObject(j) ?: continue
                                val text = contentItem.optString("text")
                                if (text.isNotBlank()) {
                                    outputText = text
                                }
                            }
                        }
                    }

                    // Breaks AI Response into lines one at a time

                    val lines = outputText.lines()
                    var spanishLine = ""
                    var englishLine = ""

                    // Allows for spanish text extraction

                    for (line in lines) {
                        val trimmedLine = line.trim()
                        if (trimmedLine.startsWith("Spanish:", ignoreCase = true)) {
                            spanishLine = trimmedLine.substringAfter(":").trim()
                        }
                        if (trimmedLine.startsWith("English:", ignoreCase = true)) {
                            englishLine = trimmedLine.substringAfter(":").trim()
                        }
                    }

                    // Everything in this block updates the screen with the new information
                    runOnUiThread {

                        // Checks to make sure both pieces are found successfully

                        if (spanishLine.isNotBlank() && englishLine.isNotBlank()) {
                            aiStatus.text = "Here is a new AI-generated sentence:"
                            aiSpanish.text = "Spanish: $spanishLine"
                            aiEnglish.text = "English: $englishLine"
                        } else {
                            aiStatus.text =
                                "The AI response came back, but it was not in the expected format."
                            aiSpanish.text = ""
                            aiEnglish.text = ""
                        }
                        aiBtn.isEnabled = true
                    }
                } catch (e: Exception) {
                    runOnUiThread {
                        aiStatus.text = "Something went wrong while generating the example."
                        aiSpanish.text = ""
                        aiEnglish.text = ""
                        aiBtn.isEnabled = true
                    }
                }
            }.start()
        }

        // Allows the switching between front and back

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

    private fun updateProgress() {
        val prefs = getSharedPreferences("progress", MODE_PRIVATE)

        val quizzesCompleted = prefs.getInt("quizzesCompleted", 0)
        val lastScore = prefs.getInt("lastScore", 0)
        val lastTotalQuestions = prefs.getInt("lastTotalQuestions", 0)
        val streakDays = prefs.getInt("streakDays", 0)

        progressCompleted.text = "Quizzes completed: $quizzesCompleted"

        progressLastScore.text =
            if (quizzesCompleted == 0 || lastTotalQuestions == 0) {
                "Last score: No quiz yet"
            } else {
                val percent = (lastScore * 100) / lastTotalQuestions
                "Last score: $percent%"
            }

        progressStreak.text =
            "Current Streak: $streakDays day" + if (streakDays == 1) "" else "s"

    }

    override fun onResume() {
        super.onResume()
        updateProgress()
    }
}
