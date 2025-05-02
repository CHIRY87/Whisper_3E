import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.ac.ecc.whisper_3e.FollowListAdapter
import jp.ac.ecc.whisper_3e.OverflowMenuActivity
import jp.ac.ecc.whisper_3e.R
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

        // Step 2-1: Declare the views
        followListText = findViewById(R.id.followListText)
        followRecycle = findViewById(R.id.followRecycle)

        // Step 2-2: Get the user ID and category from the Intent
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""

        // Step 2-3: Change the text of followListText based on the category
        followListText.text = if (category == "follow") {
            "Following List"
        } else {
            "Followers List"
        }

        // Step 2-4: Set up RecyclerView
        followRecycle.layoutManager = LinearLayoutManager(this)
        followListAdapter = FollowListAdapter(this, emptyList())
        followRecycle.adapter = followListAdapter

        // Step 2-4: Fetch the follow/follower data
        fetchFollowList(userId, category)
    }

    private fun fetchFollowList(userId: String, category: String) {
        val client = OkHttpClient()

        // Step 2-4: Prepare the API URL (adjust with the actual endpoint)
        val url = "https://your-api-endpoint.com/getFollowersAndFollowing?userId=$userId&category=$category"

        val request = Request.Builder()
            .url(url)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    val followList = parseFollowList(responseBody)

                    // Step 2-4-2: Handle JSON data and update RecyclerView
                    runOnUiThread {
                        if (followList.isNotEmpty()) {
                            followListAdapter.updateData(followList)
                        } else {
                            Toast.makeText(applicationContext, "No followers/following found", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    runOnUiThread {
                        // Step 2-4-2-1: Show error if JSON data is invalid
                        Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Step 2-4-2-1: Show error if the request fails
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    private fun parseFollowList(responseBody: String?): List<JSONObject> {
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
