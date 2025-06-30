package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FollowListActivity : OverflowMenuActivity() {

    private lateinit var followListText: TextView
    private lateinit var followRecycle: RecyclerView
    private lateinit var followListAdapter: FollowListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        // 2-1: Declare views
        followListText = findViewById(R.id.followListText)
        followRecycle = findViewById(R.id.followRecycle)

        // 2-2: Get userId and category from Intent
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""

        // 2-3: Change text based on category
        followListText.text = if (category == "follow") {
            "Following List"
        } else {
            "Followers List"
        }

        // 2-4: Setup RecyclerView
        followRecycle.layoutManager = LinearLayoutManager(this)
        followListAdapter = FollowListAdapter(this, emptyList())
        followRecycle.adapter = followListAdapter

        // 2-4: Fetch follow/follower data
        fetchFollowList(userId, category)
    }

    private fun fetchFollowList(userId: String, category: String) {
        val client = OkHttpClient()

        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/followCtl.php"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    if (responseBody == null) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, "Empty response from server", Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    // Check for error message in JSON
                    val errorMessage = parseErrorMessage(responseBody)
                    if (errorMessage != null) {
                        runOnUiThread {
                            Toast.makeText(applicationContext, errorMessage, Toast.LENGTH_SHORT).show()
                        }
                        return
                    }

                    // Parse follow list
                    val followList = parseFollowList(responseBody)

                    runOnUiThread {
                        if (followList.isNotEmpty()) {
                            followListAdapter.updateData(followList)
                        } else {
                            Toast.makeText(applicationContext, "No followers/following found", Toast.LENGTH_SHORT).show()
                        }
                        
                    }
                } else {
                    runOnUiThread {
                        Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    /**
     * Parses JSON response for an error message if present.
     * Returns the error message string, or null if none found.
     */
    private fun parseErrorMessage(responseBody: String): String? {
        return try {
            val json = JSONObject(responseBody)
            if (json.has("error")) {
                json.getString("error")
            } else {
                null
            }
        } catch (e: Exception) {
            // Not a JSONObject or no error key, assume no error message
            null
        }
    }

    private fun parseFollowList(responseBody: String): List<JSONObject> {
        val followList = mutableListOf<JSONObject>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                followList.add(jsonArray.getJSONObject(i))
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return followList
    }
}
