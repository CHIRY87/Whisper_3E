package jp.ac.ecc.whisper_3e

import WhisperAdapter
import android.content.Intent
import android.os.Bundle
import android.util.Log
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
            finish()
            return
        }

//        adapter = WhisperAdapter(whisperList, this) tam thoi thay doi bang cau lenh phia duoi sua ngay 16/6
//        adapter = WhisperAdapter(whisperList, this@TimelineActivity) { whisper ->
//            val intent = Intent(this@TimelineActivity, UserInfoActivity::class.java).apply {
//                putExtra("userId", whisper.userId)
//            }
//            startActivity(intent)
//        }
        adapter = WhisperAdapter(
            whisperList,
            this@TimelineActivity,
            onUserImageClick = { whisper ->
                val intent = Intent(this@TimelineActivity, UserInfoActivity::class.java).apply {
                    putExtra("USER_ID", whisper.userId)
                }
                startActivity(intent)
            },
            onGoodClick = { whisper, position ->  // <-- đây là điểm sửa
                // Xử lý khi nhấn ngôi sao ở vị trí 'position'

                // Ví dụ: hiện thông báo
                Toast.makeText(this@TimelineActivity, "${whisper.userName} のささやきをお気に入りにしました", Toast.LENGTH_SHORT).show()

                // Bạn có thể gọi API toggle like ở đây
                // Sau khi thành công, cập nhật whisperList[position], rồi gọi adapter.notifyItemChanged(position)
            }
        )



        timelineRecycle.adapter = adapter

        fetchTimeline()
    }

    private fun fetchTimeline() {
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/timelineInfo.php"
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

                    val responseBody = response.body?.string()
                    Log.d("DEBUG_JSON", "Response: $responseBody")

                    try {
                        val json = JSONObject(responseBody ?: "")

                        val isError = json.optBoolean("error", false)
                        if (isError) {
                            val message = json.optString("message", "エラーが発生しました。")
                            showError(message)
                            return
                        }

                        val whispers = json.optJSONArray("whispers")
                        if (whispers != null) {
                            parseWhispers(whispers)
                        } else {
                            showError("ささやきデータが取得できませんでした。")
                        }
                    } catch (e: Exception) {
                        showError("JSONの解析中にエラーが発生しました。")
                        Log.e("JSON_ERROR", "Parsing error: ${e.message}", e)
                    }
                }
            }
        })
    }

private fun parseWhispers(jsonArray: JSONArray) {
    whisperList.clear()
    for (i in 0 until jsonArray.length()) {
        val obj = jsonArray.getJSONObject(i)

        val whisper = WhisperRowData(
            whisperNo = obj.optInt("whisperNo"),
            userId = obj.optString("userId"),
            userName = obj.optString("userName"),
            content = obj.optString("content"),
            postDate = obj.optString("postDate", ""),
            iconPath = obj.optString("iconPath", ""), // hoặc "" nếu không có
            goodCount = obj.optInt("goodCnt", 0),
            commentCount = obj.optInt("commentCnt", 0),
            goodFlg = obj.optInt("goodFlg", 0) == 1 // ✅ trả về true nếu là 1, false nếu là 0
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
