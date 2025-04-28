package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
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

        // Get target user ID from Intent
        targetUserId = intent.getStringExtra("userId")
        loginUserId = GlobalData.loginUserId

        if (targetUserId == null) {
            targetUserId = loginUserId // If target user ID is null, use the logged-in user's ID
        }

        // Fetch user profile and whispers data
        fetchUserProfile(targetUserId)

        // Set up radio group listener
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.whisperRadio -> fetchWhispers(targetUserId)
                R.id.goodInfoRadio -> fetchGoodInfo(targetUserId)
            }
        }

        // Follow button listener
        followButton.setOnClickListener {
            toggleFollowStatus(targetUserId)
        }

        // Follow count text click listener
        followCntText.setOnClickListener {
            navigateToFollowList(targetUserId, "follow")
        }

        // Follower count text click listener
        followerCntText.setOnClickListener {
            navigateToFollowList(targetUserId, "follower")
        }
    }

    private fun fetchUserProfile(userId: String?) {
        val url = "https://example.com/api/getUserProfile?userId=$userId"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Request Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string() // Parse JSON response here
                    runOnUiThread {
                        // Set data to UI views
                        // userNameText.text = parsedUserName
                        // profileText.text = parsedUserProfile
                        // followCntText.text = parsedFollowCount
                        // followerCntText.text = parsedFollowerCount
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchWhispers(userId: String?) {
        val url = "https://example.com/api/getUserWhispers?userId=$userId"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Request Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string() // Parse JSON response here
                    runOnUiThread {
                        // Set whispers data to RecyclerView
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun fetchGoodInfo(userId: String?) {
        val url = "https://example.com/api/getGoodInfo?userId=$userId"
        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(this@UserInfoActivity, "Request Failed: $e", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val jsonResponse = response.body?.string() // Parse JSON response here
                    runOnUiThread {
                        // Set good info data to RecyclerView
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(this@UserInfoActivity, "Error: ${response.code}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        })
    }

    private fun toggleFollowStatus(userId: String?) {
        // Make network call to toggle follow/unfollow status
    }

    private fun navigateToFollowList(userId: String?, type: String) {
        val intent = Intent(this, FollowListActivity::class.java)
        intent.putExtra("userId", userId)
        intent.putExtra("type", type)
        startActivity(intent)
    }
}
