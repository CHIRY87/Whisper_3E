package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class LoginActivity : AppCompatActivity() {

    private lateinit var loginText: TextView
    private lateinit var userIdEdit: EditText
    private lateinit var passwordEdit: EditText
    private lateinit var loginButton: Button
    private lateinit var createButton: Button

    companion object {
        var loginUserId: String? = null // グローバル変数
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // １－１．オブジェクトを変数にバインド
        loginText = findViewById(R.id.loginText)
        userIdEdit = findViewById(R.id.userIdEdit)
        passwordEdit = findViewById(R.id.passwordEdit)
        loginButton = findViewById(R.id.loginButton)
        createButton = findViewById(R.id.createButton)

        // １－２．Loginボタンのクリックリスナー
        loginButton.setOnClickListener {
            val userId = userIdEdit.text.toString()
            val password = passwordEdit.text.toString()

            // １－２－１．空白チェック
            if (userId.isBlank() || password.isBlank()) {
                Toast.makeText(this, "ユーザーIDとパスワードを入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // １－２－２．ログイン認証APIをリクエスト
            loginRequest(userId, password)
        }

        // １－３．Create Userボタンのクリックリスナー
        createButton.setOnClickListener {
            val intent = Intent(this, CreateUserActivity::class.java) // 遷移先は仮に CreateUserActivity
            startActivity(intent)
        }
    }

    private fun loginRequest(userId: String, password: String) {
        // ★ 本来はここでAPIリクエストします（今回は仮実装）

        // --- 仮でログイン成功処理を書く ---
        val isSuccess = true // 本来はサーバのレスポンスに応じて判定
        val errorMessage = "ログイン失敗" // 本来はサーバから取得

        if (isSuccess) {
            // １－２－３－１－２．ユーザIDをグローバル変数に保存
            loginUserId = userId

            // １－２－３－１－３．タイムライン画面に遷移
//            val intent = Intent(this, TimelineActivity::class.java) // TimelineActivityに遷移
//            startActivity(intent)

            // １－２－３－１－４．自分の画面を閉じる
            finish()
        } else {
            // エラーの場合
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}