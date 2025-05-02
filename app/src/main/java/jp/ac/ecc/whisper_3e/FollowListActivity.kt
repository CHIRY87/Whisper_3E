import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.whisperclient.OverflowMenuActivity
import jp.ac.ecc.whisper_3e.R
import okhttp3.*
import org.json.JSONArray
import org.json.JSONObject
import java.io.IOException

class FollowListActivity : OverflowMenuActivity() {  // Inherit from OverflowMenuActivity

    private lateinit var followListText: TextView
    private lateinit var followRecycle: RecyclerView
    private lateinit var followListAdapter: FollowListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        // Step 2-1: Declare the views
        followListText = findViewById(R.id.followListText)
        followRecycle = findViewById(R.id.followRecycle)

        // Set up RecyclerView
        followRecycle.layoutManager = LinearLayoutManager(this)

        // Initialize the adapter with an empty list
        followListAdapter = FollowListAdapter(emptyList())
        followRecycle.adapter = followListAdapter

        // Step 2-2: Get the user ID and category (follow/follower) from intent
        val userId = intent.getStringExtra("USER_ID") ?: ""
        val category = intent.getStringExtra("CATEGORY") ?: ""

        // Step 2-3: Change the text of followListText based on the category
        followListText.text = if (category == "follow") {
            "Following List"
        } else {
            "Followers List"
        }

        // Step 2-4: Request follow/follower data from the API
        fetchFollowList(userId, category)
    }

    private fun fetchFollowList(userId: String?, category: String?) {
        val client = OkHttpClient()

        // Step 2-4: Prepare the API URL (adjust with the actual endpoint)
        val url = "https://10.108.1.194/getFollowersAndFollowing?userId=$userId&category=$category"

        // Step 2-4: Request setup
        val request = Request.Builder()
            .url(url)
            .build()

        // Send the request
        client.newCall(request).enqueue(object : Callback {
            // Step 2-4-1: If the response is successful
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Step 2-4-2: Parse the response JSON and update the UI
                    val followList = parseFollowList(responseBody)

                    // Update the UI with the fetched data on the main thread
                    runOnUiThread {
                        // Step 2-4-2-4: Set the follow/follower data to the RecyclerView
                        followListAdapter.updateData(followList)
                    }
                } else {
                    runOnUiThread {
                        // Step 2-4-2-1: Show error message if the response is not successful
                        Toast.makeText(applicationContext, "Failed to fetch data", Toast.LENGTH_SHORT).show()
                    }
                }
            }

            // Step 2-4-2: If the request fails
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    // Step 2-4-2-1: Show error message when request fails
                    Toast.makeText(applicationContext, "Request failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    // Step 2-4-2: Parse the response JSON and return the list of users
    private fun parseFollowList(responseBody: String?): List<String> {
        val followList = mutableListOf<String>()
        try {
            val jsonArray = JSONArray(responseBody)
            for (i in 0 until jsonArray.length()) {
                val jsonObject = jsonArray.getJSONObject(i)
                val userName = jsonObject.getString("username")  // Assuming the key for the username is "username"
                followList.add(userName)
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        return followList
    }
}
