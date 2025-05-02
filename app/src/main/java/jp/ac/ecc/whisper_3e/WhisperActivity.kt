package jp.ac.ecc.whisper_3e

class WhisperActivity {
}
package com.example.whisperapp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.whisperapp.api.WhisperApi
import com.example.whisperapp.model.GlobalData
import com.example.whisperapp.model.WhisperResponse
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class WhisperActivity : OverFlowMenuActivity() {

    private lateinit var whisperEdit: EditText
    private lateinit var whisperButton: Button
    private lateinit var cancelButton: Button
    private var loginUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_whisper)

        // 2-1. Khai báo view
        whisperEdit = findViewById(R.id.whisperEdit)
        whisperButton = findViewById(R.id.whisperButton)
        cancelButton = findViewById(R.id.cancelButton)

        // 2-2. Lấy ID người dùng đăng nhập
        loginUserId = GlobalData.loginUserId

        // 2-3. Xử lý nút whisper
        whisperButton.setOnClickListener {
            val content = whisperEdit.text.toString().trim()
            if (content.isEmpty()) {
                Toast.makeText(this, "ささやく内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // 2-3-2. Gửi API đăng ký whisper
            WhisperApi.service.postWhisper(loginUserId ?: "", content)
                .enqueue(object : Callback<WhisperResponse> {
                    override fun onResponse(
                        call: Call<WhisperResponse>,
                        response: Response<WhisperResponse>
                    ) {
                        if (response.isSuccessful && response.body() != null) {
                            val body = response.body()!!
                            if (body.error != null) {
                                Toast.makeText(this@WhisperActivity, body.error, Toast.LENGTH_SHORT).show()
                            } else {
                                // 2-3-2-1-2. Chuyển sang màn hình timeline
                                startActivity(Intent(this@WhisperActivity, TimelineActivity::class.java))
                                // 2-3-2-1-3. Đóng màn hình hiện tại
                                finish()
                            }
                        } else {
                            Toast.makeText(this@WhisperActivity, "エラーが発生しました", Toast.LENGTH_SHORT).show()
                        }
                    }

                    override fun onFailure(call: Call<WhisperResponse>, t: Throwable) {
                        Toast.makeText(this@WhisperActivity, "通信に失敗しました", Toast.LENGTH_SHORT).show()
                    }
                })
        }

        // 2-4. Xử lý nút cancel
        cancelButton.setOnClickListener {
            finish()
        }
    }
}
