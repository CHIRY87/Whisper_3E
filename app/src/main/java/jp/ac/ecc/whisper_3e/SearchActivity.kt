package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class SearchActivity : OverflowMenuActivity() {
    private lateinit var searchEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var whisperRadio: RadioButton
    private lateinit var recyclerView: RecyclerView
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)

        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)
        recyclerView = findViewById(R.id.searchRecycle)
        recyclerView.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            val query = searchEdit.text.toString().trim()
            if (query.isEmpty()) {
                Toast.makeText(this, "検索内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val section = when (radioGroup.checkedRadioButtonId) {
                R.id.userRadio -> "1"
                R.id.whisperRadio -> "2"
                else -> ""
            }

            if (section.isEmpty()) {
                Toast.makeText(this, "検索区分を選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val json = JSONObject().apply {
                put("section", section)
                put("string", query)
            }

            val requestBody = json.toString()
                .toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())

            val request = Request.Builder()
                .url("https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/search.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "通信エラー: ${e.message}", Toast.LENGTH_SHORT).show()
                        searchButton.isEnabled = true
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val bodyString = response.body?.string()
                        if (bodyString.isNullOrEmpty()) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "サーバー応答が空です", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        if (bodyString.trim().startsWith("<!DOCTYPE") || bodyString.trim().startsWith("<html")) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "サーバーから不正な応答がありました。", Toast.LENGTH_LONG).show()
                            }
                            return
                        }

                        try {
                            val json = JSONObject(bodyString)

                            if (json.has("error")) {
                                val errorMsg = json.getString("error")
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                                return
                            }

                            if (section == "1") {
                                val userList = json.optJSONArray("userList") ?: JSONArray()
                                val users = mutableListOf<UserRowData>()
                                for (i in 0 until userList.length()) {
                                    val user = userList.getJSONObject(i)
                                    users.add(
                                        UserRowData(
                                            userId = user.optString("userId", ""),
                                            userName = user.optString("userName", "No username"),
                                            profile = user.optString("profile", ""),           // you may need to check your JSON for this
                                            userFollowFlg = user.optInt("userFollowFlg", 0) == 1,
                                            followCount = user.optInt("followCount", 0),
                                            followerCount = user.optInt("followerCount", 0),
                                            whisperList = emptyList(),                          // or fetch if available
                                            goodList = emptyList()                              // or fetch if available
                                        )
                                    )
                                }
                            } else if (section == "2") {
                                val whisperList = json.optJSONArray("whisperList") ?: JSONArray()
                                val whisperDataList = mutableListOf<WhisperRowData>()
                                for (i in 0 until whisperList.length()) {
                                    val whisper = whisperList.getJSONObject(i)
                                    whisperDataList.add(
                                        WhisperRowData(
                                            whisperNo = whisper.optInt("whisperNo", 0),
                                            userId = whisper.optString("userId", ""),
                                            userName = whisper.optString("userName", "No username"),
                                            postDate = whisper.optString("postDate", ""),
                                            content = whisper.optString("content", "No whisper"),
                                            goodFlg = whisper.optInt("goodFlg", 0) == 1
                                        )
                                    )
                                }
                                runOnUiThread {
                                    recyclerView.adapter = WhisperAdapter(whisperDataList, this@SearchActivity)
                                    recyclerView.visibility = View.VISIBLE
                                }
                            }
                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "JSON解析エラー: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    runOnUiThread {
                        searchButton.isEnabled = true
                    }
                }
            })
        }
    }
}
