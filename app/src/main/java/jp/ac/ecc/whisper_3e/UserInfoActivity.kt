package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class UserInfoActivity : AppCompatActivity() {

    private lateinit var userImage: ImageView
    private lateinit var userNameText: TextView
    private lateinit var profileText: TextView
    private lateinit var followCntText: TextView
    private lateinit var followerCntText: TextView
    private lateinit var followButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var whisperRadio: RadioButton
    private lateinit var goodInfoRadio: RadioButton
    private lateinit var userRecycle: RecyclerView

    private lateinit var whisperAdapter: WhisperAdapter
    private lateinit var goodAdapter: GoodAdapter

    private var loginUserId: String? = null
    private var targetUserId: String? = null
    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // Bind views
        userImage = findViewById(R.id.userImage)
        userNameText = findViewById(R.id.userNameText)
        profileText = findViewById(R.id.profileText)
        followCntText = findViewById(R.id.followCntText)
        followerCntText = findViewById(R.id.followerCntText)
        followButton = findViewById(R.id.followButton)
        radioGroup = findViewById(R.id.radioGroup)
        whisperRadio = findViewById(R.id.whisperRadio)
        goodInfoRadio = findViewById(R.id.goodInfoRadio)
        userRecycle = findViewById(R.id.userRecycle)
        userRecycle.layoutManager = LinearLayoutManager(this)

        // Get target user ID from intent
        targetUserId = intent.getStringExtra("userId")
        loginUserId = GlobalData.loginUserId
        if (targetUserId == null) {
            targetUserId = loginUserId
        }

        // Fetch user profile and whispers by default
        fetchUserProfile(targetUserId)
        fetchWhispers(targetUserId)

        // Set up radio button change listener for displaying whispers or good info
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.whisperRadio -> fetchWhispers(targetUserId)
                R.id.goodInfoRadio -> fetchGoodInfo(targetUserId)
            }
        }

        // Follow button click listener
        followButton.setOnClickListener {
            toggleFollowStatus(targetUserId)
        }

        // Follow count click listener
        followCntText.setOnClickListener {
            navigateToFollowList(targetUserId, "follow")
        }

        // Follower count click listener
        followerCntText.setOnClickListener {
            navigateToFollowList(targetUserId, "follower")
        }
    }

    private fun fetchUserProfile(userId: String?) {
        val url = "https://example.com/api/getUserProfile?userId=$userId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Profile Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body.string()
                    val jsonObj = JSONObject(jsonResponse)
                    runOnUiThread {
                        userNameText.text = jsonObj.optString("userName")
                        profileText.text = jsonObj.optString("profile")
                        followCntText.text = jsonObj.optInt("followCount").toString()
                        followerCntText.text = jsonObj.optInt("followerCount").toString()
                        // Uncomment this when Glide setup is available
                        // Glide.with(this@UserInfoActivity).load(jsonObj.optString("userImagePath")).into(userImage)
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Profile Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchWhispers(userId: String?) {
        val url = "https://example.com/api/getUserWhispers?userId=$userId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Whispers Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(response.body.string())
                    val whisperList = mutableListOf<WhisperRowData>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        whisperList.add(
                            WhisperRowData(
                                userId = obj.getString("userId"),
                                userName = obj.getString("userName"),
                                whisperId = obj.getString("whisperId"),
                                whisperText = obj.getString("whisperText"),
                                userIconPath = obj.getString("userIconPath"),
                                isLiked = obj.getBoolean("isLiked")
                            )
                        )
                    }
                    runOnUiThread {
                        whisperAdapter = WhisperAdapter(whisperList, this@UserInfoActivity)
                        userRecycle.adapter = whisperAdapter
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Whispers Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchGoodInfo(userId: String?) {
        val url = "https://example.com/api/getGoodInfo?userId=$userId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Good Info Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonArray = JSONArray(response.body.string())
                    val goodList = mutableListOf<GoodRowData>()
                    for (i in 0 until jsonArray.length()) {
                        val obj = jsonArray.getJSONObject(i)
                        goodList.add(
                            GoodRowData(
                                userId = obj.getString("userId"),
                                userName = obj.getString("userName"),
                                whisper = obj.getString("whisper"),
                                goodCount = obj.getInt("goodCount"),
                                userImagePath = obj.getString("userImagePath")
                            )
                        )
                    }
                    runOnUiThread {
                        goodAdapter = GoodAdapter(this@UserInfoActivity, goodList) // Passing context to adapter
                        userRecycle.adapter = goodAdapter
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Good Info Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun toggleFollowStatus(userId: String?) {
        // Placeholder for follow/unfollow logic
        val followStatus = true // Example value (replace with actual follow status)
        if (followStatus) {
            // Unfollow
            unfollowUser(userId)
        } else {
            // Follow
            followUser(userId)
        }
    }

    private fun followUser(userId: String?) {
        val url = "https://example.com/api/followUser?userId=$userId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Follow Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        followButton.text = "Unfollow"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Follow Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun unfollowUser(userId: String?) {
        val url = "https://example.com/api/unfollowUser?userId=$userId"
        val request = Request.Builder().url(url).build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Unfollow Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    runOnUiThread {
                        followButton.text = "Follow"
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Unfollow Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun navigateToFollowList(userId: String?, type: String) {
        val intent = Intent(this, FollowListActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("type", type)
        startActivity(intent)
    }
}
