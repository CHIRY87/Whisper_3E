package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

// １．OverFlowMenuActivityクラスを継承する
class FollowListActivity : OverflowMenuActivity() {

    private lateinit var followListText: TextView
    private lateinit var followRecycle: RecyclerView
    private lateinit var userAdapter: UserAdapter

    // フォローまたはフォロワー情報のリスト
    private val userList = mutableListOf<UserRowData>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        // ２－１．画面デザインで定義したオブジェクトを変数として宣言する。
        followListText = findViewById(R.id.followListText)
        followRecycle = findViewById(R.id.followRecycle)

        // ２－２．インテント(前画面)から対象ユーザIDと区分（フォロー・フォロワー）を取得する
        val userId = intent.getStringExtra("userId") ?: ""
        val category = intent.getStringExtra("type") ?: ""

        // ２－３．区分に併せて、followListTextのテキストを変更する
        followListText.text = if (category == "follow") {
            "Following List"
        } else {
            "Followers List"
        }

        // ２－４－２－４．followRecycleにフォロー情報リストまたはフォロワー情報リストをセットする
        userAdapter = UserAdapter(this, userList)
        followRecycle.layoutManager = LinearLayoutManager(this)
        followRecycle.adapter = userAdapter

        // ２－４．フォロワー情報取得APIをリクエストして対象ユーザのフォロー・フォロワー情報取得処理を行う
        fetchFollowList(userId, category)
    }

    private fun fetchFollowList(userId: String, category: String) {
        val client = OkHttpClient()

        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/followerInfo.php"
        val json = JSONObject().apply {
            put("userId", userId)
            put("type", category)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            // ２－４－２．リクエストが失敗した時(コールバック処理)
            override fun onFailure(call: Call, e: IOException) {
                // ２－４－２－１．エラーメッセージをトースト表示する
                runOnUiThread {
                    Toast.makeText(applicationContext, "通信エラー: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }

            // ２－４－１．正常にレスポンスを受け取った時(コールバック処理)
            override fun onResponse(call: Call, response: Response) {
                val body = response.body?.string() ?: return
                try {
                    val json = JSONObject(body)

                    // ２－４－２－１．JSONデータがエラーの場合、受け取ったエラーメッセージをトースト表示して処理を終了させる
                    if (json.optString("result") != "success") {
                        val msg = json.optString("errMsg", "取得に失敗しました")
                        runOnUiThread {
                            Toast.makeText(applicationContext, msg, Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    // ２－４－２－２．フォロー情報一覧が存在するかチェックする／２－４－２－３．フォロワー情報一覧が存在するかチェックする
                    val list = if (category == "follow") {
                        json.optJSONArray("followList") ?: JSONArray()
                    } else {
                        json.optJSONArray("followerList") ?: JSONArray()
                    }

                    userList.clear()
                    for (i in 0 until list.length()) {
                        val obj = list.getJSONObject(i)

                        // ２－４－２－２－１．フォロー情報／フォロワー情報をリストに格納する
                        userList.add(
                            UserRowData(
                                userId = obj.optString("userId"),
                                userName = obj.optString("userName"),
                                profile = obj.optString("profile"),
                                userFollowFlg = obj.optBoolean("userFollowFlg"),
                                followCount = obj.optInt("followCount"),
                                followerCount = obj.optInt("followerCount"),
                                whisperList = mutableListOf(),
                                goodList = mutableListOf()
                            )
                        )
                    }

                    // ２－４－２－４ー１／２－４－２－４ー２．リストをRecyclerViewにセットする
                    runOnUiThread {
                        userAdapter.notifyDataSetChanged()
                    }

                } catch (e: Exception) {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "JSON解析エラー", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
