package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserInfoActivity : OverflowMenuActivity() {

    private lateinit var userImage: ImageView
    private lateinit var userNameText: TextView
    private lateinit var profileText: TextView
    private lateinit var followCntText: TextView
    private lateinit var followerCntText: TextView
    private lateinit var followButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var recyclerView: RecyclerView

    private lateinit var whisperAdapter: WhisperAdapter
    private lateinit var goodAdapter: GoodAdapter

    private var currentUserId: String? = null
    private var isFollowing = false
    private var showingWhispers = true

    private var displayUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        userImage = findViewById(R.id.userImage)
        userNameText = findViewById(R.id.userNameText)
        profileText = findViewById(R.id.profileText)
        followCntText = findViewById(R.id.followCntText)
        followerCntText = findViewById(R.id.followerCntText)
        followButton = findViewById(R.id.followButton)
        radioGroup = findViewById(R.id.radioGroup)
        recyclerView = findViewById(R.id.userRecycle)

        recyclerView.layoutManager = LinearLayoutManager(this)
        whisperAdapter = WhisperAdapter(mutableListOf(), this)
        goodAdapter = GoodAdapter(this, mutableListOf())
        recyclerView.adapter = whisperAdapter

        val userId = intent.getStringExtra("USER_ID") ?: ""
        Log.d("DEBUG_USERINFO", "Received USER_ID=$userId")

        if (userId.isNullOrEmpty()) {
            Toast.makeText(this, "ユーザーIDが取得できませんでした", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        displayUserId = userId

        currentUserId = GlobalData.loginUserId ?: ""
        radioGroup.check(R.id.whisperRadio)
        showingWhispers = true

        fetchUserData(displayUserId!!)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            showingWhispers = (checkedId == R.id.whisperRadio)
            recyclerView.adapter = if (showingWhispers) whisperAdapter else goodAdapter
            fetchUserData(displayUserId!!)
        }

        followCntText.setOnClickListener { startFollowListActivity("follow") }
        followerCntText.setOnClickListener { startFollowListActivity("follower") }

        followButton.setOnClickListener {
            displayUserId?.let { toggleFollow(it, !isFollowing) }
        }
    }

    private fun startFollowListActivity(type: String) {
        val intent = Intent(this, FollowListActivity::class.java).apply {
            putExtra("userId", displayUserId)
            putExtra("type", type)
        }
        startActivity(intent)
    }

    private fun fetchUserData(userId: String) {
        val client = OkHttpClient()
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userWhisperInfo.php"
        val json = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", currentUserId)
        }
        Log.d("DEBUG_USERINFO", "Request JSON: ${json.toString()}")
        Log.d("DEBUG_RESPONSE", "UserInfoAPI response: $json")

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Log.e("UserInfoActivity", "JSON parsing error", e)
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "ユーザー情報の取得に失敗しました。", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyStr = it.body?.string()
                    if (bodyStr.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@UserInfoActivity, "サーバーからの応答がありません。", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    try {
                        val json = JSONObject(bodyStr)
                        Log.d("DEBUG_RESPONSE", "UserInfoAPI response: $json")
                        if (json.optString("result") != "success") {
                            runOnUiThread {
                                Toast.makeText(this@UserInfoActivity, json.optString("errMsg"), Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val userName = json.optString("userName")
                        val profile = json.optString("profile")
                        val followCnt = json.optInt("followCount")
                        val followerCnt = json.optInt("followerCount")
                        val followFlag = json.optBoolean("userFollowFlg", false)
                        val imageUrl = json.optString("image")

                        val whispers = mutableListOf<WhisperRowData>()
                        val goods = mutableListOf<GoodRowData>()

                        if (showingWhispers) {
                            val whisperList = json.optJSONArray("whisperList") ?: JSONArray()
                            for (i in 0 until whisperList.length()) {
                                val obj = whisperList.getJSONObject(i)
                                val whisperNo = obj.optInt("whisperNo")
                                val userId = obj.optString("userId")
                                val userName = obj.optString("userName")
                                val postDate = obj.optString("postDate")
                                val content = obj.optString("content")
                                val goodFlg = obj.optInt("goodFlg") == 1

                                whispers.add(WhisperRowData(whisperNo, userId, userName, postDate, content, goodFlg))
                            }
                        } else {
                            val goodList = json.optJSONArray("goodList") ?: JSONArray()
                            for (i in 0 until goodList.length()) {
                                val obj = goodList.getJSONObject(i)
                                val whisperNo = obj.optInt("whisperNo")
                                val userId = obj.optString("userId")
                                val userName = obj.optString("userName")
                                val postDate = obj.optString("postDate")
                                val content = obj.optString("content")
                                val goodFlg = obj.optInt("goodFlg") == 1

                                goods.add(GoodRowData(whisperNo, userId, userName, postDate, content, goodFlg))
                            }
                        }

                        runOnUiThread {
                            userNameText.text = userName
                            profileText.text = profile
                            followCntText.text = ": $followCnt"
                            followerCntText.text = ": $followerCnt"
                            isFollowing = followFlag

                            if (imageUrl.isNotBlank()) {
                                Glide.with(this@UserInfoActivity).load(imageUrl).into(userImage)
                            } else {
                                userImage.setImageResource(R.drawable.kirito)
                            }

                            followButton.visibility = if (currentUserId == displayUserId) View.GONE else View.VISIBLE
                            followButton.text = if (isFollowing) "フォロー解除" else "フォローする"

                            if (showingWhispers) whisperAdapter.updateWhispers(whispers)
                            else goodAdapter.updateGood(goods)
                        }
                    } catch (e: Exception) {
                        Log.e("UserInfoActivity", "JSON parsing error", e)
                        runOnUiThread {
                            Toast.makeText(this@UserInfoActivity, "データ処理エラーが発生しました。", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }


    private fun toggleFollow(followUserId: String, followFlg: Boolean) {
        val client = OkHttpClient()
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/followCtl.php"
        val json = JSONObject().apply {
            put("loginUserId", currentUserId)
            put("followUserId", followUserId)
            put("followFlg", followFlg)
        }
        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)
        val request = Request.Builder().url(url).post(requestBody).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "フォロー操作に失敗しました。", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    val bodyStr = it.body?.string()
                    if (bodyStr.isNullOrEmpty()) {
                        runOnUiThread {
                            Toast.makeText(this@UserInfoActivity, "サーバーからの応答がありません。", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }
                    try {
                        val json = JSONObject(bodyStr)
                        if (json.optString("result") != "success") {
                            runOnUiThread {
                                Toast.makeText(this@UserInfoActivity, json.optString("errMsg"), Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        isFollowing = followFlg
                        runOnUiThread {
                            followButton.text = if (isFollowing) "フォロー解除" else "フォローする"
                            fetchUserData(followUserId)
                        }
                    } catch (e: Exception) {
                        runOnUiThread {
                            Toast.makeText(this@UserInfoActivity, "データ処理エラーが発生しました。", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        })
    }
}
