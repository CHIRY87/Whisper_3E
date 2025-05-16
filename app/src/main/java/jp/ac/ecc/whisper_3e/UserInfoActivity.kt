package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
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
    private lateinit var followCntText: TextView
    private lateinit var followerCntText: TextView
    private lateinit var followButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var recyclerView: RecyclerView

    private lateinit var whisperAdapter: WhisperAdapter
    private lateinit var goodAdapter: GoodAdapter

    private var currentUserId: String? = null
    private var isFollowing: Boolean = false
    private var showingWhispers = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        userImage = findViewById(R.id.userImage)
        userNameText = findViewById(R.id.userNameText)
        followCntText = findViewById(R.id.followCntText)
        followerCntText = findViewById(R.id.followerCntText)
        followButton = findViewById(R.id.followButton)
        radioGroup = findViewById(R.id.radioGroup)
        recyclerView = findViewById(R.id.userRecycle)

        recyclerView.layoutManager = LinearLayoutManager(this)
        whisperAdapter = WhisperAdapter(mutableListOf(), this)
        goodAdapter = GoodAdapter(this, mutableListOf())
        recyclerView.adapter = whisperAdapter // default adapter

        val intentUserId = intent.getStringExtra("userId")
        currentUserId = getSharedPreferences("login", MODE_PRIVATE).getString("userId", null)

        fetchUserInfo(intentUserId)

        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            showingWhispers = (checkedId == R.id.whisperRadio)
            fetchUserInfo(intentUserId)
        }

        followCntText.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", intentUserId)
            intent.putExtra("type", "follow")
            startActivity(intent)
        }

        followerCntText.setOnClickListener {
            val intent = Intent(this, FollowListActivity::class.java)
            intent.putExtra("userId", intentUserId)
            intent.putExtra("type", "follower")
            startActivity(intent)
        }

        followButton.setOnClickListener {
            intentUserId?.let { targetUserId ->
                toggleFollow(targetUserId, !isFollowing)
            }
        }
    }

    private fun fetchUserInfo(userId: String?) {
        val targetUserId = userId ?: currentUserId ?: return
        val client = OkHttpClient()
        val url = "https://your-api-url.com/user_info?userId=$targetUserId&loginUserId=$currentUserId"

        val request = Request.Builder().url(url).build()
        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Failed to load user info", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.body.string().let { body ->
                    val json = JSONObject(body)

                    if (json.optBoolean("error", false)) {
                        val message = json.optString("message", "Unknown error")
                        runOnUiThread {
                            Toast.makeText(this@UserInfoActivity, message, Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    val user = json.getJSONObject("user")
                    val isMe = targetUserId == currentUserId

                    // Select whispers or goodWhispers based on showingWhispers flag
                    val whispersJsonArray = if (showingWhispers) {
                        json.optJSONArray("whispers") ?: JSONArray()
                    } else {
                        json.optJSONArray("goodWhispers") ?: JSONArray()
                    }

                    runOnUiThread {
                        userNameText.text = user.optString("name", "Unknown")
                        followCntText.text = "Follow: ${user.optInt("followCount", 0)}"
                        followerCntText.text = "Follower: ${user.optInt("followerCount", 0)}"

                        // Load profile image using Glide
                        val userImageUrl = user.optString("userImage", "")
                        if (userImageUrl.isNotEmpty()) {
                            Glide.with(this@UserInfoActivity)
                                .load(userImageUrl)
                                .placeholder(R.drawable.kirito)
                                .into(userImage)
                        } else {
                            userImage.setImageResource(R.drawable.kirito)
                        }

                        if (isMe) {
                            followButton.visibility = View.GONE
                        } else {
                            followButton.visibility = View.VISIBLE
                            isFollowing = user.optBoolean("isFollowing", false)
                            followButton.text = if (isFollowing) "Unfollow" else "Follow"
                        }

                        if (showingWhispers) {
                            // Parse whispers into WhisperRowData list
                            val whisperList = mutableListOf<WhisperRowData>()
                            for (i in 0 until whispersJsonArray.length()) {
                                val w = whispersJsonArray.getJSONObject(i)
                                val isLiked = w.optBoolean("isLiked", false)

                                whisperList.add(
                                    WhisperRowData(
                                        whisperId = w.optString("id", ""),
                                        userId = w.optString("userId", ""),
                                        userName = w.optString("userName", "Unknown"),
                                        whisperText = w.optString("text", ""),
                                        userImage = w.optString("userImage", ""),
                                        isLiked = isLiked
                                    )
                                )
                            }
                            whisperAdapter.updateWhispers(whisperList)
                            recyclerView.adapter = whisperAdapter
                        } else {
                            // Parse goodWhispers into GoodRowData list
                            val goodList = mutableListOf<GoodRowData>()
                            for (i in 0 until whispersJsonArray.length()) {
                                val w = whispersJsonArray.getJSONObject(i)

                                goodList.add(
                                    GoodRowData(
                                        userId = w.optString("userId", ""),
                                        userName = w.optString("userName", "Unknown"),
                                        whisper = w.optString("text", ""),
                                        goodCount = w.optInt("goodCount", 0),
                                        userImagePath = w.optString("userImage", "")
                                    )
                                )
                            }
                            goodAdapter.updateWhispers(goodList)
                            recyclerView.adapter = goodAdapter
                        }
                    }
                }
            }
        })
    }

    private fun toggleFollow(targetUserId: String, follow: Boolean) {
        val client = OkHttpClient()
        val json = JSONObject().apply {
            put("loginUserId", currentUserId)
            put("targetUserId", targetUserId)
            put("followFlg", follow)
        }

        val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url("https://your-api-url.com/follow")
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Follow action failed", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    isFollowing = follow
                    runOnUiThread {
                        followButton.text = if (isFollowing) "Unfollow" else "Follow"
                        fetchUserInfo(targetUserId) // Refresh user info to update counts
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Follow action failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }
}
