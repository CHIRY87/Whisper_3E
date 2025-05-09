package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class WhisperActivity : OverflowMenuActivity() {

    private lateinit var whisperText: TextView
    private lateinit var whisperEdit: EditText
    private lateinit var whisperButton: Button
    private lateinit var cancelButton: Button

    private val client = OkHttpClient() // OkHttpクライアント

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        // 画面上のUI要素を取得
        whisperText = findViewById(R.id.whisperText)
        whisperEdit = findViewById(R.id.whisperEdit)
        whisperButton = findViewById(R.id.whisperButton)
        cancelButton = findViewById(R.id.cancelButton)

        // ログインユーザーID（OverflowMenuActivityから継承されたグローバル変数を使用）
        val loggedInUserId = loginUserId

        // Whisperボタンのクリックイベント
        whisperButton.setOnClickListener {
            val whisperContent = whisperEdit.text.toString()

            // 入力チェック
            if (whisperContent.isBlank()) {
                Toast.makeText(this, "ささやく内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ささやき登録処理
            sendWhisperRequest(loggedInUserId, whisperContent)
        }

        // キャンセルボタンのクリックイベント
        cancelButton.setOnClickListener {
            finish()  // 自分の画面を閉じる
        }
    }

    // ささやき登録処理を行う関数
    private fun sendWhisperRequest(userId: String, whisperContent: String) {
        val url = "https://your.api.endpoint/whisper"  // 実際のAPIエンドポイントに置き換えてください

        // POSTリクエストの作成
        val formBody = FormBody.Builder()
            .add("userId", userId)
            .add("whisper", whisperContent)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(formBody)
            .build()

        // APIリクエストを実行
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody != null) {
                        // レスポンスをJSON形式で解析
                        val jsonResponse = JSONObject(responseBody)
                        val success = jsonResponse.getBoolean("success")

                        runOnUiThread {
                            if (success) {
                                Toast.makeText(this@WhisperActivity, "ささやきが登録されました", Toast.LENGTH_SHORT).show()
                                // タイムライン画面に遷移
                                startActivity(Intent(this@WhisperActivity, TimelineActivity::class.java))
                                finish()
                            } else {
                                val errorMessage = jsonResponse.getString("message")
                                Toast.makeText(this@WhisperActivity, errorMessage, Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // APIリクエストが失敗した場合
                    runOnUiThread {
                        Toast.makeText(this@WhisperActivity, "通信エラーが発生しました", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                // リクエストが失敗した場合
                runOnUiThread {
                    Toast.makeText(this@WhisperActivity, "通信エラーが発生しました", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }
}
