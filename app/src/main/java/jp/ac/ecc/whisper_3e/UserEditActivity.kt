package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.util.Log
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class UserEditActivity : AppCompatActivity() {

    private lateinit var userNameEdit: EditText
    private lateinit var profileEdit: EditText
    private lateinit var userImage: ImageView
    private lateinit var userIdText: TextView
    private lateinit var changeButton: Button
    private lateinit var cancelButton: Button

    private var userId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_edit)

        userNameEdit = findViewById(R.id.userNameEdit)
        profileEdit = findViewById(R.id.profileEdit)
        userImage = findViewById(R.id.userImage)
        userIdText = findViewById(R.id.userIdText)
        changeButton = findViewById(R.id.changeButton)
        cancelButton = findViewById(R.id.cancelButton)

        userId = intent.getStringExtra("USER_ID")
        if (userId.isNullOrEmpty()) {
            userId = GlobalData.loginUserId
        }

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "ユーザーIDが取得できません", Toast.LENGTH_SHORT).show()
            finish()
            return
        }

        userIdText.text = userId
        loadUserInfo(userId!!)

        changeButton.setOnClickListener {
            val userName = userNameEdit.text.toString().trim()
            val profile = profileEdit.text.toString().trim()

            if (userName.isEmpty()) {
                Toast.makeText(this, "ユーザー名を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateUserInfo(userId!!, userName, profile)
        }

        cancelButton.setOnClickListener {
            finish()
        }
    }

    private fun loadUserInfo(userId: String) {
        val client = OkHttpClient()
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userInfo.php"

        val json = JSONObject().apply {
            put("userId", userId)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity, "ユーザー情報の取得に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()

                runOnUiThread {
                    if (!response.isSuccessful || bodyString == null) {
                        Toast.makeText(this@UserEditActivity, "ユーザー情報の応答が不正です。", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    Log.d("SERVER_RESPONSE", bodyString)

                    try {
                        val jsonResponse = JSONObject(bodyString)
                        val result = jsonResponse.optString("result")

                        if (result == "success") {
                            val userName = jsonResponse.optString("userName", "")
                            val profile = jsonResponse.optString("profile", "")
                            val imageUrl = jsonResponse.optString("imageUrl", "")

                            userNameEdit.setText(userName)
                            profileEdit.setText(profile)

                            if (imageUrl.isNotEmpty()) {
                                Glide.with(this@UserEditActivity)
                                    .load(imageUrl)
                                    .into(userImage)
                            }
                        } else {
                            val errMsg = jsonResponse.optString("errMsg", "ユーザー情報の取得に失敗しました。")
                            Toast.makeText(this@UserEditActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@UserEditActivity, "レスポンスの解析中にエラーが発生しました。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun updateUserInfo(userId: String, userName: String, profile: String) {
        val client = OkHttpClient()
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userUpd.php"

        val json = JSONObject().apply {
            put("userId", userId)
            put("userName", userName)
            put("profile", profile)
        }
        val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserEditActivity, "更新に失敗しました: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val bodyString = response.body?.string()

                runOnUiThread {
                    if (!response.isSuccessful || bodyString == null) {
                        Toast.makeText(this@UserEditActivity, "サーバーからの応答が不正です。", Toast.LENGTH_SHORT).show()
                        return@runOnUiThread
                    }

                    Log.d("UPDATE_RESPONSE", bodyString)

                    try {
                        val jsonResponse = JSONObject(bodyString)
                        val result = jsonResponse.optString("result")

                        if (result == "success") {
                            Toast.makeText(this@UserEditActivity, "ユーザー情報を更新しました。", Toast.LENGTH_SHORT).show()
                            setResult(RESULT_OK)
                            finish()
                        } else {
                            val errMsg = jsonResponse.optString("errMsg", "更新に失敗しました。")
                            Toast.makeText(this@UserEditActivity, errMsg, Toast.LENGTH_SHORT).show()
                        }

                    } catch (e: Exception) {
                        e.printStackTrace()
                        Toast.makeText(this@UserEditActivity, "レスポンスの解析中にエラーが発生しました。", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
