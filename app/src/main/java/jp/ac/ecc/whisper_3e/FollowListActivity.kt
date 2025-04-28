import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import jp.ac.ecc.whisper_3e.R
import okhttp3.*
import java.io.IOException

class FollowListActivity : AppCompatActivity() {

    private lateinit var followListText: TextView
    private lateinit var followRecycle: RecyclerView
    private lateinit var followListAdapter: FollowListAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_follow_list)

        // Initialize views
        followListText = findViewById(R.id.followListText)
        followRecycle = findViewById(R.id.followRecycle)

        // Set up RecyclerView
        followRecycle.layoutManager = LinearLayoutManager(this)
        followListAdapter = FollowListAdapter()
        followRecycle.adapter = followListAdapter

        // Get the user ID and category (follow/follower) from intent
        val userId = intent.getStringExtra("USER_ID")
        val category = intent.getStringExtra("CATEGORY") // "follow" or "follower"

        // Change text based on the category
        followListText.text = if (category == "follow") {
            "Following List"
        } else {
            "Followers List"
        }

        // Fetch follow/follower data from API
        fetchFollowList(userId, category)
    }

    private fun fetchFollowList(userId: String?, category: String?) {
        val client = OkHttpClient()

        // Prepare the API URL (replace with your actual endpoint)
        val url = "https://10.108.1.194/getFollowersAndFollowing?userId=$userId&category=$category"

        // Request setup
        val request = Request.Builder()
            .url(url)
            .build()

        // Send request
        client.newCall(request).enqueue(object : Callback {
            override fun onResponse(call: Call, response: Response) {
                if (response.isSuccessful) {
                    val responseBody = response.body?.string()
                    // Parse JSON and update UI (Assuming you have a function to parse JSON)
                    val followList = parseFollowList(responseBody)

                    // Update UI with the fetched data on the main thread
                    runOnUiThread {
                        followListAdapter.submitList(followList)
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

    private fun parseFollowList(responseBody: String?): List<String> {
        // Parse the response and return the list of follows/followers
        // This is just an example; adjust as per your API response structure
        return listOf("User1", "User2", "User3") // Replace with actual data parsing logic
    }
}
