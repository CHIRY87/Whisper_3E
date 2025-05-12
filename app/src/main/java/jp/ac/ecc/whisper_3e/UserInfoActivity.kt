package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.widget.TextView
import android.widget.RadioGroup
import android.widget.ImageView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserInfoActivity : AppCompatActivity() {
    private lateinit var userAdapter: UserAdapter
    private val userList = mutableListOf<UserRowData>()

    // UI elements
    private lateinit var userNameText: TextView
    private lateinit var followerCntText: TextView
    private lateinit var followCntText: TextView
    private lateinit var userImage: ImageView
    private lateinit var recyclerView: RecyclerView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_info)

        // Bind views
        userNameText = findViewById(R.id.userNameText)
        followerCntText = findViewById(R.id.followerCntText)
        followCntText = findViewById(R.id.followCntText)
        userImage = findViewById(R.id.userImage)
        recyclerView = findViewById(R.id.userRecycle)

        // Initialize UserAdapter
        userAdapter = UserAdapter(userList) { user: UserRowData ->
            // On user item clicked, update UI with that user's info
            updateUserInfo(user)  // Pass the user object to update the UI
        }


        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = userAdapter

        // Sample data for testing
        loadUserData()

        // Set up radio button listeners
        val radioGroup: RadioGroup = findViewById(R.id.radioGroup)
        radioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.whisperRadio -> {
                    // Handle whisper radio
                }
                R.id.goodInfoRadio -> {
                    // Handle good info radio
                }
            }
        }
    }

    // Sample data loading
    private fun loadUserData() {
        userList.clear()
        // Adding some users to the list for demonstration purposes
        userList.add(UserRowData("1", "Alice", 12,34,"https://example.com/image1.jpg"))
        userList.add(UserRowData("2", "Bob", 45,67,"https://example.com/image2.jpg"))

        userAdapter.notifyDataSetChanged()
    }

    // Function to update UI based on selected user
    private fun updateUserInfo(user: UserRowData) {
        userNameText.text = user.userName
        followCntText.text = user.followCount.toString()
        followerCntText.text = user.followerCount.toString()

        // Here you can use an image loading library like Glide or Picasso to load the user image
        Glide.with(this)
            .load(user.imagePath)
            .into(userImage)
    }
}
