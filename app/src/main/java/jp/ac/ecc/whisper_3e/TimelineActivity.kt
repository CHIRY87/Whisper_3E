package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class TimelineActivity : OverflowMenuActivity() {

    private lateinit var timelineRecycle: RecyclerView
    private var loginUserId: String? = null
    private val whisperList = mutableListOf<WhisperRowData>()
    private lateinit var adapter: WhisperAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline)

        timelineRecycle = findViewById(R.id.timelineRecycle)
        timelineRecycle.layoutManager = LinearLayoutManager(this)

        loginUserId = GlobalData.loginUserId

        if (loginUserId.isNullOrEmpty()) {
            Toast.makeText(this, "ユーザーIDが無効です。再度ログインしてください。", Toast.LENGTH_SHORT).show()
            finish()  // finish activity if no valid login user
            return
        }

        adapter = WhisperAdapter(whisperList, this)
        timelineRecycle.adapter = adapter

        fetchTimeline()
    }

    private fun fetchTimeline() {
        val url = "https://10.108.1.194/timeline" // Replace with your actual API
        val client = OkHttpClient()

        val requestBody = FormBody.Builder()
            .add("userId", loginUserId!!)
            .build()

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@TimelineActivity, "通信に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        showError("サーバーエラー: ${response.code}")
                        return
                    }

                    val json = JSONObject(response.body.string())
                    if (json.getBoolean("error")) {
                        showError(json.getString("message"))
                        return
                    }

                    val whispers = json.getJSONArray("whispers")
                    parseWhispers(whispers)
                }
            }
        })
    }

    private fun parseWhispers(jsonArray: JSONArray) {
        whisperList.clear()
        for (i in 0 until jsonArray.length()) {
            val obj = jsonArray.getJSONObject(i)
            val whisper = WhisperRowData(
                whisperId = obj.getString("whisperId"),
                userId = obj.getString("userId"),
                userName = obj.getString("userName"),
                whisperText = obj.getString("whisperText"),
                userImage = obj.getString("userIconPath"),
                isLiked = obj.getBoolean("isLiked")
            )
            whisperList.add(whisper)
        }

        runOnUiThread {
            adapter.notifyDataSetChanged()
        }
    }

    private fun showError(message: String) {
        runOnUiThread {
            Toast.makeText(this@TimelineActivity, message, Toast.LENGTH_SHORT).show()
        }
    }
}
