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

    // Add loginUserId from your global or intent source
    private val loginUserId: String = GlobalData.loginUserId ?: ""

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

            searchButton.isEnabled = false

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
                                searchButton.isEnabled = true
                            }
                            return
                        }

                        if (bodyString.trim().startsWith("<!DOCTYPE") || bodyString.trim().startsWith("<html")) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@SearchActivity,
                                    "サーバーから不正な応答がありました。URLが正しいか確認してください。",
                                    Toast.LENGTH_LONG
                                ).show()
                                searchButton.isEnabled = true
                            }
                            android.util.Log.e("SearchActivity", "Invalid response (HTML): $bodyString")
                            return
                        }

                        try {
                            val json = JSONObject(bodyString)

                            if (json.has("error")) {
                                val errorMsg = json.getString("error")
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                    searchButton.isEnabled = true
                                }
                                return
                            }

                            if (section == "1") {
                                val userList = json.optJSONArray("userList") ?: JSONArray()
                                val users = mutableListOf<UserRowData>()
                                for (i in 0 until userList.length()) {
                                    val user = userList.getJSONObject(i)

                                    val whisperArray = user.optJSONArray("whisperList") ?: JSONArray()
                                    val goodArray = user.optJSONArray("goodList") ?: JSONArray()

                                    val whispers = mutableListOf<WhisperRowData>()
                                    for (j in 0 until whisperArray.length()) {
                                        val whisper = whisperArray.getJSONObject(j)
                                        whispers.add(
                                            WhisperRowData(
                                                whisperNo = whisper.optInt("whisperNo"),
                                                userId = whisper.optString("userId"),
                                                userName = whisper.optString("userName"),
                                                postDate = whisper.optString("postDate"),
                                                content = whisper.optString("content"),
                                                goodCount = whisper.optInt("goodCount"),
                                                goodFlg = whisper.optBoolean("goodFlg"),
                                                iconPath = whisper.optString("iconPath")
                                            )
                                        )
                                    }

                                    val goods = mutableListOf<GoodRowData>()
                                    for (j in 0 until goodArray.length()) {
                                        val good = goodArray.getJSONObject(j)
                                        goods.add(
                                            GoodRowData(
                                                whisperNo = good.optInt("whisperNo"),
                                                userId = good.optString("userId"),
                                                userName = good.optString("userName"),
                                                postDate = good.optString("postDate"),
                                                content = good.optString("content"),
                                                goodCount = good.optInt("goodCount")
                                            )
                                        )
                                    }

                                    users.add(
                                        UserRowData(
                                            userId = user.optString("userId"),
                                            userName = user.optString("userName"),
                                            profile = user.optString("profile"),
                                            userFollowFlg = user.optBoolean("userFollowFlg"),
                                            followCount = user.optInt("followCount"),
                                            followerCount = user.optInt("followerCount"),
                                            whisperList = whispers,
                                            goodList = goods
                                        )
                                    )
                                }
                                runOnUiThread {
                                    recyclerView.adapter = UserAdapter(this@SearchActivity, users)
                                    recyclerView.visibility = View.VISIBLE
                                    searchButton.isEnabled = true
                                }

                            } else if (section == "2") {
                                val whisperList = json.optJSONArray("whisperList") ?: JSONArray()
                                val whispers = mutableListOf<WhisperRowData>()
                                for (i in 0 until whisperList.length()) {
                                    val whisper = whisperList.getJSONObject(i)
                                    whispers.add(
                                        WhisperRowData(
                                            whisperNo = whisper.optInt("whisperNo"),
                                            userId = whisper.optString("userId"),
                                            userName = whisper.optString("userName", "No username"),
                                            postDate = whisper.optString("postDate", ""),
                                            content = whisper.optString("content", "No whisper"),
                                            goodCount = whisper.optInt("goodCount", 0),
                                            goodFlg = whisper.optBoolean("goodFlg", false),
                                            iconPath = whisper.optString("iconPath", "")
                                        )
                                    )
                                }

                                runOnUiThread {
                                    recyclerView.adapter = WhisperAdapter(
                                        whispers.toMutableList(),
                                        this@SearchActivity,
                                        loginUserId,
                                        onUserImageClick = { whisper ->
                                            val intent = Intent(this@SearchActivity, UserInfoActivity::class.java).apply {
                                                putExtra("USER_ID", whisper.userId)
                                            }
                                            startActivity(intent)
                                        },
                                        onGoodClick = { whisper, position ->
                                            // Toggle like using immutable copy and notify adapter
                                            val updatedWhisper = whisper.copy(
                                                goodFlg = !whisper.goodFlg,
                                                goodCount = if (!whisper.goodFlg) whisper.goodCount + 1 else maxOf(0, whisper.goodCount - 1)
                                            )
                                            whispers[position] = updatedWhisper
                                            recyclerView.adapter?.notifyItemChanged(position)

                                            // TODO: Add your server update call here if needed
                                        }
                                    )
                                    recyclerView.visibility = View.VISIBLE
                                    searchButton.isEnabled = true
                                }
                            } else {
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity, "不明な検索区分です", Toast.LENGTH_SHORT).show()
                                    searchButton.isEnabled = true
                                }
                            }

                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "JSON解析エラー: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                                searchButton.isEnabled = true
                            }
                            android.util.Log.e("SearchActivity", "JSON parsing error", e)
                        }
                    }
                }
            })
        }
    }
}
