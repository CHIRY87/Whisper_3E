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

//１．OverFlowMenuActivityクラスを継承する
class UserInfoActivity : OverflowMenuActivity() {

    private lateinit var userImage: ImageView
    private lateinit var userNameText: TextView
    private lateinit var profileText: TextView
    private lateinit var followCntText: TextView
    private lateinit var followerCntText: TextView
    private lateinit var followButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var recyclerView: RecyclerView
    private lateinit var followText: TextView
    private lateinit var followerText: TextView

    private var whisperAdapter: WhisperAdapter = WhisperAdapter(mutableListOf(), this)
    private var goodAdapter: GoodAdapter = GoodAdapter(mutableListOf(), this)

    private var currentUserId: String? = null
    private var isFollowing = false
    private var showingWhispers = true

    private var displayUserId: String? = null

    //２．画面生成時（onCreate処理）
    override fun onCreate(savedInstanceState: Bundle?) {
        //２－１．画面デザインで定義したオブジェクトを変数として宣言する
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
        followText = findViewById(R.id.followText)
        followerText = findViewById(R.id.followerText)

        recyclerView.layoutManager = LinearLayoutManager(this)

        currentUserId = GlobalData.loginUserId ?: ""

        //２－２．インテント(前画面)から対象ユーザIDを取得する
        val userId = intent.getStringExtra("USER_ID") ?: ""
        Log.d("DEBUG_USERINFO", "Received USER_ID=$userId")

        if (userId.isEmpty()) {
            Toast.makeText(this, "ユーザーIDが取得できませんでした", Toast.LENGTH_SHORT).show()
            finish()
            return
        }
        displayUserId = userId

        radioGroup.check(R.id.whisperRadio)
        showingWhispers = true

        fetchUserData(displayUserId!!, true)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            showingWhispers = (checkedId == R.id.whisperRadio)
            fetchUserData(displayUserId!!, false)
            recyclerView.adapter = if (showingWhispers) whisperAdapter else goodAdapter
        }

        followText.setOnClickListener {
            startFollowListActivity("follow")
        }
        followerText.setOnClickListener {
            startFollowListActivity("follower")
        }
        followCntText.setOnClickListener {
            startFollowListActivity("follow")
        }
        followerCntText.setOnClickListener {
            startFollowListActivity("follower")
        }

        followButton.setOnClickListener {
            displayUserId?.let { userId ->
                Log.d("displayuserid", "$displayUserId")
                Log.d("DEBUG_CLICK", "Before toggle: isFollowing=$isFollowing")
                toggleFollow(userId, !isFollowing)
            }
        }
    }

    private fun startFollowListActivity(type: String) {
        val intent = Intent(this, FollowListActivity::class.java).apply {
            putExtra("userId", displayUserId)
            putExtra("type", type)
        }
        startActivity(intent)
    }

    //２－３．ユーザささやき情報取得API　共通実行メソッドを呼び出す
    private fun fetchUserData(userId: String, initFlg: Boolean) {
        val client = OkHttpClient()
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userWhisperInfo.php"
        val json = JSONObject().apply {
            put("userId", userId)
            put("loginUserId", currentUserId)
        }
        Log.d("DEBUG_USERINFO", "Request JSON: ${json.toString()}")

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
                        val goods = mutableListOf<WhisperRowData>()

                        if (showingWhispers) {
                            val whisperList = json.optJSONArray("whisperList") ?: JSONArray()
                            for (i in 0 until whisperList.length()) {
                                val obj = whisperList.getJSONObject(i)
                                whispers.add(
                                    WhisperRowData(
                                        whisperNo = obj.optInt("whisperNo"),
                                        userId = obj.optString("userId"),
                                        userName = obj.optString("userName"),
                                        postDate = obj.optString("postDate"),
                                        content = obj.optString("content"),
                                        goodCount = obj.optInt("goodCount"),
                                        goodFlg = obj.optInt("goodFlg") == 1,
                                        iconPath = obj.optString("iconPath")
                                    )
                                )
                            }
                        } else {
                            val goodList = json.optJSONArray("goodList") ?: JSONArray()
                            for (i in 0 until goodList.length()) {
                                val obj = goodList.getJSONObject(i)
                                goods.add(
                                    WhisperRowData(
                                        whisperNo = obj.optInt("whisperNo"),
                                        userId = obj.optString("userId"),
                                        userName = obj.optString("userName"),
                                        postDate = obj.optString("postDate"),
                                        content = obj.optString("content"),
                                        goodCount = obj.optInt("goodCount"),
                                        goodFlg = obj.optInt("goodFlg") == 1,
                                        iconPath = obj.optString("iconPath")
                                    )
                                )
                            }
                        }

                        runOnUiThread {
                            userNameText.text = userName
                            profileText.text = profile
                            followCntText.text = ": $followCnt"
                            followerCntText.text = ": $followerCnt"
                            if(initFlg){
                                isFollowing = followFlag
                            }else{
                                isFollowing = !isFollowing
                            }
                            if (isFollowing) {
                                followButton.text = "Unfollow"
                            } else {
                                followButton.text = "Follow"
                            }

                            if (imageUrl.isNotBlank()) {
                                Glide.with(this@UserInfoActivity).load(imageUrl).into(userImage)
                            } else {
                                userImage.setImageResource(R.drawable.chitoge)
                            }

                            followButton.visibility = if (currentUserId == displayUserId) View.GONE else View.VISIBLE

                            if (showingWhispers) {
                                whisperAdapter = WhisperAdapter(whispers, this@UserInfoActivity)
                            } else {
                                whisperAdapter = WhisperAdapter(goods, this@UserInfoActivity)
                            }
                            recyclerView.adapter = whisperAdapter

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
        Log.d("currentuser", "$currentUserId")
        Log.d("followuser", "$followUserId")
        Log.d("followflg", "$followFlg")
        Log.d("UserInfoActivity", "Current isFollowing = $isFollowing")

        val json = JSONObject().apply {
            put("userId", currentUserId)
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
                    Log.d("FOLLOW_API", "Response: $bodyStr")

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
                        runOnUiThread {
                            fetchUserData(followUserId, false)
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
    override fun onUserEdited() {
        displayUserId?.let {
            fetchUserData(it, false)
        }
    }
}
