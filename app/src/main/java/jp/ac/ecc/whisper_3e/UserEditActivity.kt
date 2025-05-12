package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.android.volley.Request
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import org.json.JSONObject

open class UserEditActivity : OverflowMenuActivity() {

    private lateinit var userNameEdit: EditText
    private lateinit var profileEdit: EditText
    private lateinit var userImage: ImageView
    private lateinit var userIdText: TextView
    private lateinit var changeButton: Button
    private lateinit var cancelButton: Button

    // 画面生成時（onCreate処理）
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        // ２－１．画面デザインで定義したオブジェクトを変数として宣言する
        userNameEdit = findViewById(R.id.userNameEdit)
        profileEdit = findViewById(R.id.profileEdit)
        userImage = findViewById(R.id.userImage)
        userIdText = findViewById(R.id.userIdText)
        changeButton = findViewById(R.id.changeButton)
        cancelButton = findViewById(R.id.cancelButton)

        // ２－２．グローバル変数のログインユーザーIDを取得
        val userId = intent.getStringExtra("USER_ID")

        // ２－３．ユーザ情報取得APIをリクエストしてログインユーザのユーザ情報取得処理を行う
        fetchUserInfo(userId)

        // ２－４．changeButtonのクリックイベントリスナーを作成する
        changeButton.setOnClickListener {
            val userName = userNameEdit.text.toString()
            val profile = profileEdit.text.toString()
            // ユーザ変更処理APIをリクエストして入力したユーザ情報の更新処理を行う
            updateUserInfo(userId, userName, profile)
        }

        // ２－５．cancelButtonのクリックイベントリスナーを作成する
        cancelButton.setOnClickListener {
            // 自分の画面を閉じる
            finish()
        }
    }

    // ２－３．ユーザ情報取得APIをリクエストしてログインユーザのユーザ情報取得処理を行う
    private fun fetchUserInfo(userId: String?) {
        val url = "https://example.com/api/user/$userId"  // ここでURLを実際のものに置き換えます

        val jsonRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    if (response.getBoolean("error")) {
                        // ２－３－１－１．JSONデータがエラーの場合、受け取ったエラーメッセージをトースト表示して処理を終了させる
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    } else {
                        // ２－３－１－２．取得したデータを各オブジェクトにセットする
                        userIdText.text = "User ID: ${response.getString("id")}"
                        userNameEdit.setText(response.getString("userName"))
                        profileEdit.setText(response.getString("profile"))
                        // 画像が必要であれば、ここに画像を更新するロジックを追加します
                    }
                } catch (e: Exception) {
                    // ２－３－２．リクエストが失敗した時（エラーメッセージをトースト表示）
                    Toast.makeText(this, "データ処理エラー", Toast.LENGTH_SHORT).show()
                }
            },
            {
                // ２－３－２－１．エラーメッセージをトースト表示する
                Toast.makeText(this, "リクエストが失敗しました", Toast.LENGTH_SHORT).show()
            })

        val queue = Volley.newRequestQueue(this)
        queue.add(jsonRequest)
    }

    // ２－４－１．ユーザ変更処理APIをリクエストして入力したユーザ情報の更新処理を行う
    private fun updateUserInfo(userId: String?, userName: String, profile: String) {
        val url = "https://"  // ここでURL

        val jsonBody = JSONObject().apply {
            put("id", userId)
            put("userName", userName)
            put("profile", profile)
        }

        val jsonRequest = JsonObjectRequest(
            Request.Method.POST, url, jsonBody,
            { response ->
                try {
                    if (response.getBoolean("error")) {
                        // ２－４－１－１．JSONデータがエラーの場合、受け取ったエラーメッセージをトースト表示して処理を終了させる
                        Toast.makeText(this, response.getString("message"), Toast.LENGTH_SHORT).show()
                    } else {
                        // ２－４－１－１－２．ユーザ情報画面に遷移する
                        val intent = Intent(this, UserInfoActivity::class.java)
                        intent.putExtra("USER_ID", userId)
                        startActivity(intent)

                        // ２－４－１－１－３．自分の画面を閉じる
                        finish()
                    }
                } catch (e: Exception) {
                    // ２－４－１－２．リクエストが失敗した時（エラーメッセージをトースト表示）
                    Toast.makeText(this, "データ処理エラー", Toast.LENGTH_SHORT).show()
                }
            },
            {
                // ２－４－１－２－１．エラーメッセージをトースト表示する
                Toast.makeText(this, "リクエストが失敗しました", Toast.LENGTH_SHORT).show()
            })

        val queue = Volley.newRequestQueue(this)
        queue.add(jsonRequest)
    }
}
