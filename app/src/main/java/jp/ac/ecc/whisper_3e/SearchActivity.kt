package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
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

            val selectedOption = when (radioGroup.checkedRadioButtonId) {
                R.id.userRadio -> "user"
                R.id.whisperRadio -> "whisper"
                else -> ""
            }

            val request = Request.Builder()
                .url("https://api.example.com/search?type=$selectedOption&query=$query")
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "エラーが発生しました: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body.let { body ->
                        val json = JSONObject(body.string())

                        if (json.has("error")) {
                            val errorMsg = json.getString("error")
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val results = json.getJSONArray("results")

                        if (selectedOption == "user") {
                            val users = mutableListOf<UserRowData>()
                            for (i in 0 until results.length()) {
                                val user = results.getJSONObject(i)
                                users.add(
                                    UserRowData(
                                        userId = user.optString("user_id", ""),
                                        userName = user.optString("username", "No username"),
                                        followCount = user.optInt("follow_count", 0),
                                        followerCount = user.optInt("follower_count", 0),
                                        imagePath = user.optString("image_path", "")
                                    )
                                )
                            }
                            runOnUiThread {
                                val adapter = UserAdapter(this@SearchActivity, users)
                                recyclerView.adapter = adapter
                                recyclerView.visibility = View.VISIBLE
                            }

                        } else if (selectedOption == "whisper") {
                            val whispers = mutableListOf<WhisperRowData>()
                            for (i in 0 until results.length()) {
                                val whisper = results.getJSONObject(i)
                                whispers.add(
                                    WhisperRowData(
                                        whisperId = whisper.optString("whisper_id", ""),
                                        userId = whisper.optString("user_id", ""),
                                        userName = whisper.optString("username", "No username"),
                                        whisperText = whisper.optString("whisper_text", "No whisper"),
                                        userImage = whisper.optString("image_path", ""),
                                        isLiked = whisper.optBoolean("is_liked", false)
                                    )
                                )
                            }
                            runOnUiThread {
                                val adapter = WhisperAdapter(whispers, this@SearchActivity)
                                recyclerView.adapter = adapter
                                recyclerView.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
        }
    }
}
