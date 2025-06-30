package jp.ac.ecc.whisper_3e

import WhisperAdapter
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

// １．OverFlowMenuActivityクラスを継承する
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

        // ２－１．画面デザインで定義したオブジェクトを変数として宣言する。
        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)
        recyclerView = findViewById(R.id.searchRecycle)
        recyclerView.layoutManager = LinearLayoutManager(this)

        // ２－２．searchButtonのクリックイベントリスナーを作成する
        searchButton.setOnClickListener {
            val query = searchEdit.text.toString().trim()

            // ２－２－１．入力項目が空白の時、エラーメッセージをトースト表示して処理を終了させる
            if (query.isEmpty()) {
                Toast.makeText(this, "検索内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ２－２－２．ラジオボタンの選択肢を変数に保持する。
            val section = when (radioGroup.checkedRadioButtonId) {
                R.id.userRadio -> "1"
                R.id.whisperRadio -> "2"
                else -> ""
            }

            if (section.isEmpty()) {
                Toast.makeText(this, "検索区分を選択してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ２－２－３．検索結果取得APIをリクエスト
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
                // ２－２－４．リクエストが失敗した時
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        // ２－２－４－１．エラーメッセージをトースト表示する
                        Toast.makeText(this@SearchActivity, "通信エラー: ${e.message}", Toast.LENGTH_SHORT).show()
                        searchButton.isEnabled = true
                    }
                }

                // ２－２－３－１．正常にレスポンスを受け取った時
                override fun onResponse(call: Call, response: Response) {
                    response.use {
                        val bodyString = response.body?.string()

                        if (bodyString.isNullOrEmpty()) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "サーバー応答が空です", Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        // 不正なHTML応答のチェック
                        if (bodyString.trim().startsWith("<!DOCTYPE") || bodyString.trim().startsWith("<html")) {
                            runOnUiThread {
                                Toast.makeText(
                                    this@SearchActivity,
                                    "サーバーから不正な応答がありました。URLが正しいか確認してください。",
                                    Toast.LENGTH_LONG
                                ).show()
                            }
                            android.util.Log.e("SearchActivity", "Invalid response (HTML): $bodyString")
                            return
                        }

                        try {
                            val json = JSONObject(bodyString)

                            // ２－２－３－１－１．エラーがある場合、トースト表示
                            if (json.has("error")) {
                                val errorMsg = json.getString("error")
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                                }
                                return
                            }

                            // ２－２－３－１－２．ラジオボタンの選択に応じた処理
                            if (section == "1") {
                                // ２－２－３－１－２－１．userRadio選択時
                                val userList = json.optJSONArray("userList") ?: JSONArray()
                                val users = mutableListOf<UserRowData>()
                                for (i in 0 until userList.length()) {
                                    val user = userList.getJSONObject(i)
                                    // ２－２－３－１－２－１－１．ユーザ情報をリストに格納する
                                    users.add(
                                        UserRowData(
                                            userId = user.optString("userId", ""),
                                            userName = user.optString("userName", "No username"),
                                            followCount = user.optInt("followCount", 0),
                                            followerCount = user.optInt("followerCount", 0),
                                            imagePath = user.optString("iconPath", ""), // nếu có ảnh
                                            whisperCount = user.optInt("whisperCount", 0) // 👈 thêm dòng này
                                        )
                                    )

                                }
                                runOnUiThread {
                                    recyclerView.adapter = UserAdapter(this@SearchActivity, users) { user ->
                                        val intent = Intent(this@SearchActivity, UserInfoActivity::class.java)
                                        intent.putExtra("USER_ID", user.userId)
                                        startActivity(intent)
                                    }
                                    recyclerView.visibility = View.VISIBLE
                                }
//
                            } else if (section == "2") {
                                // ２－２－３－１－２－２－１．いいね行情報一覧が存在する間、以下の処理を繰り返す
                                val whisperList = json.optJSONArray("whisperList") ?: JSONArray()
                                val whisperDataList = mutableListOf<WhisperRowData>()

                                for (i in 0 until whisperList.length()) {
                                    val whisper = whisperList.getJSONObject(i)

                                    // ２－２－３－１－２－２－１－１．いいね情報をリストに格納する
                                    whisperDataList.add(
                                        WhisperRowData(
                                            whisperNo = whisper.optInt("whisperNo", 0),
                                            userId = whisper.optString("userId", ""),
                                            userName = whisper.optString("userName", "No username"),
                                            postDate = whisper.optString("postDate", ""),
                                            content = whisper.optString("content", "No whisper"),
                                            goodCount = whisper.optInt("goodCount", 0),
                                            goodFlg = whisper.optInt("goodFlg", 0) == 1,
                                            iconPath = whisper.optString("iconPath", ""),
                                            commentCount = whisper.optInt("commentCount", 0)
                                        )
                                    )
                                }

                                // ２－２－３－１－２－２－２．いいね行情報のアダプターにいいね情報リストをセットする
                                runOnUiThread {
                                    recyclerView.adapter = WhisperAdapter(
                                        whisperDataList,
                                        this@SearchActivity,
                                        onUserImageClick = { whisper ->
                                            val intent = Intent(this@SearchActivity, UserInfoActivity::class.java)
                                            intent.putExtra("USER_ID", whisper.userId)
                                            startActivity(intent)
                                        },
                                        onGoodClick = { whisper, position ->
                                            val client = OkHttpClient()
                                            val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/goodCtl.php"

                                            val goodFlgValue = if (!whisper.goodFlg) 1 else 0

                                            val json = JSONObject().apply {
                                                put("userId", GlobalData.loginUserId)
                                                put("whisperNo", whisper.whisperNo)
                                                put("goodFlg", goodFlgValue)
                                            }

                                            val requestBody = json.toString().toRequestBody("application/json; charset=utf-8".toMediaTypeOrNull())
                                            val request = Request.Builder().url(url).post(requestBody).build()

                                            client.newCall(request).enqueue(object : Callback {
                                                override fun onFailure(call: Call, e: IOException) {
                                                    runOnUiThread {
                                                        Toast.makeText(this@SearchActivity, "通信エラー: ${e.message}", Toast.LENGTH_SHORT).show()
                                                    }
                                                }

                                                override fun onResponse(call: Call, response: Response) {
                                                    response.use {
                                                        val resStr = it.body?.string()
                                                        val jsonRes = JSONObject(resStr ?: "")
                                                        if (jsonRes.optString("result") == "success") {
                                                            runOnUiThread {
                                                                whisper.goodFlg = !whisper.goodFlg
                                                                whisper.goodCount += if (whisper.goodFlg) 1 else -1
                                                                recyclerView.adapter?.notifyItemChanged(position)

                                                                Toast.makeText(this@SearchActivity,
                                                                    "${whisper.userName} のささやきを${if (whisper.goodFlg) "お気に入りにしました" else "お気に入りを解除しました"}",
                                                                    Toast.LENGTH_SHORT).show()
                                                            }
                                                        } else {
                                                            runOnUiThread {
                                                                Toast.makeText(this@SearchActivity, "処理に失敗しました", Toast.LENGTH_SHORT).show()
                                                            }
                                                        }
                                                    }
                                                }
                                            })
                                        }
                                    )

                                    // ２－２－３－１－２－２－３．searchRecycleを表示する
                                    recyclerView.visibility = View.VISIBLE
                                }
                            }
                            else {
                                runOnUiThread {
                                    Toast.makeText(this@SearchActivity, "不明な検索区分です", Toast.LENGTH_SHORT).show()
                                }
                            }

                        } catch (e: Exception) {
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, "JSON解析エラー: ${e.localizedMessage}", Toast.LENGTH_LONG).show()
                            }
                            android.util.Log.e("SearchActivity", "JSON parsing error", e)
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