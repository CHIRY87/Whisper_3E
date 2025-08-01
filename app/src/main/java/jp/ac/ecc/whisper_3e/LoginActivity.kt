package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class LoginActivity : AppCompatActivity() {

    // 1-1. 画面デザインで定義したオブジェクトを変数として宣言する
    private lateinit var loginText: TextView
    private lateinit var userIdEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var createButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // Viewの初期化
        loginText = findViewById(R.id.loginText)
        userIdEdit = findViewById(R.id.userIdEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        loginButton = findViewById(R.id.loginButton)
        createButton = findViewById(R.id.createButton)

        // 1-2. loginButtonのクリックイベントリスナーを作成
        loginButton.setOnClickListener {
            val userId = userIdEdit.text.toString().trim()
            val password = passwordEdit.text.toString().trim()

            // 1-2-1. 入力項目が空白の時、エラーメッセージを表示
            if (userId.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "ユーザーIDとパスワードを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 1-2-2. ログイン認証APIをリクエスト
            loginRequest(userId, password)
        }

        // 1-3. createButtonのクリックイベントリスナーを作成
        createButton.setOnClickListener {
            // 1-3-1. ユーザ作成画面に遷移
            startActivity(Intent(this, CreateUserActivity::class.java))
        }
    }

    // ログイン認証APIの呼び出し処理
    private fun loginRequest(userId: String, password: String) {
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("userId", userId)
            put("password", password)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/loginAuth.php")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            // 1-2-2-2. リクエストが失敗した時
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@LoginActivity, "サーバーへの接続に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // 1-2-2-1. レスポンスが正常に返ってきた時
            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()

                runOnUiThread {
                    if (!response.isSuccessful || responseBody == null) {
                        Toast.makeText(this@LoginActivity, "サーバーからの応答が不正です。", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    try {
                        val jsonResponse = JSONObject(responseBody)
                        val result = jsonResponse.optString("result")

                        if (result == "success") {
                            // 1-2-3-1-2. グローバル変数にuserIdを保存
                            GlobalData.loginUserId = userId


                            // SharedPreferences に保存
                            getSharedPreferences("MyPrefs", MODE_PRIVATE)
                                .edit()
                                .putString("userId", userId)
                                .apply()
                            val errMsg = jsonResponse.optString("errMsg", "ログインに失敗しました。")

                            Toast.makeText(this@LoginActivity, "ログインに成功しました。", Toast.LENGTH_SHORT).show()

                            // 1-2-3-1-3. タイムライン画面に遷移
                            startActivity(Intent(this@LoginActivity, TimelineActivity::class.java))

                            // 1-2-3-1-4. この画面を閉じる
                            finish()

                        } else {
                            // 1-2-3-1-1. エラー時のメッセージ表示
                            val errMsg = jsonResponse.optString("errMsg", "ログインに失敗しました。")
                            Toast.makeText(this@LoginActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        Toast.makeText(this@LoginActivity, "レスポンスの解析中にエラーが発生しました。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
