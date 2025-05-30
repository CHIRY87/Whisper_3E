package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class WhisperActivity : OverflowMenuActivity() {

    private lateinit var whisperText: TextView
    private lateinit var whisperEdit: EditText
    private lateinit var whisperButton: Button
    private lateinit var cancelButton: Button

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        whisperText = findViewById(R.id.whisperText)
        whisperEdit = findViewById(R.id.whisperEdit)
        whisperButton = findViewById(R.id.whisperButton)
        cancelButton = findViewById(R.id.cancelButton)

        val sharedPref = getSharedPreferences("MyPrefs", MODE_PRIVATE)
        val loggedInUserId = sharedPref.getString("userId", null)

        whisperButton.setOnClickListener {
            val whisperContent = whisperEdit.text.toString().trim()

            if (whisperContent.isEmpty()) {
                Toast.makeText(this, "ささやく内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("ログインID", loggedInUserId.toString())
            if (loggedInUserId.isNullOrEmpty()) {
                Toast.makeText(this, "ユーザーIDが取得できません。ログインし直してください。", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            sendWhisperRequest(loggedInUserId, whisperContent)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun sendWhisperRequest(userId: String, whisperContent: String) {
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/test/whisperAdd.php"

        val json = JSONObject().apply {
            put("userId", userId)
            put("content", whisperContent)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(
                        this@WhisperActivity,
                        "通信エラーが発生しました: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        try {
                            val jsonResponse = JSONObject(responseBody)
                            val result = jsonResponse.optString("result", "")

                            when (result) {
                                "success" -> {
                                    Toast.makeText(
                                        this@WhisperActivity,
                                        "ささやきが登録されました",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    whisperEdit.text.clear()

                                    startActivity(
                                        Intent(
                                            this@WhisperActivity,
                                            TimelineActivity::class.java
                                        )
                                    )
                                    finish()
                                }
                                "error" -> {
                                    val errMsg = jsonResponse.optString("errMsg", "エラーが発生しました")
                                    Toast.makeText(this@WhisperActivity, errMsg, Toast.LENGTH_SHORT).show()
                                }
                                else -> {
                                    Toast.makeText(
                                        this@WhisperActivity,
                                        "予期しないレスポンスを受信しました",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                }
                            }
                        } catch (e: Exception) {
                            Toast.makeText(
                                this@WhisperActivity,
                                "レスポンスの解析中にエラーが発生しました。",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    } else {
                        Toast.makeText(
                            this@WhisperActivity,
                            "通信エラーが発生しました",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
                }
            }
        })
    }
}
