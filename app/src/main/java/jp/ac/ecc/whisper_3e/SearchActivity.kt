import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import jp.ac.ecc.whisper_3e.OverflowMenuActivity
import jp.ac.ecc.whisper_3e.R
import jp.ac.ecc.whisper_3e.WhisperAdapter
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class SearchActivity : OverflowMenuActivity() {

    // Declare UI components
    private lateinit var searchEdit: EditText
    private lateinit var searchButton: Button
    private lateinit var radioGroup: RadioGroup
    private lateinit var userRadio: RadioButton
    private lateinit var whisperRadio: RadioButton
    private lateinit var recyclerView: RecyclerView

    private val client = OkHttpClient()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_search)  // Ensure your layout file is named activity_search.xml

        // ２－１． Initialize the UI components
        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)
        recyclerView = findViewById(R.id.searchRecycle)

        recyclerView.layoutManager = LinearLayoutManager(this)

        // ２－２． Search button click event listener
        searchButton.setOnClickListener {
            val query = searchEdit.text.toString().trim()

            // ２－２－１． Check if input is empty and show error if true
            if (query.isEmpty()) {
                Toast.makeText(this, "検索内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // ２－２－２． Get the selected radio button's value
            val selectedOption = when (radioGroup.checkedRadioButtonId) {
                R.id.userRadio -> "user"
                R.id.whisperRadio -> "whisper"
                else -> ""
            }

            // ２－２－３． Make API request to get search results
            val request = Request.Builder()
                .url("https://api.example.com/search?type=$selectedOption&query=$query")  // Replace with actual endpoint
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    // ２－２－４． Handle request failure
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "エラーが発生しました: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val json = JSONObject(body.string())

                        // ２－２－３－１． Check if the response contains an error
                        if (json.has("error")) {
                            val errorMsg = json.getString("error")
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val items = mutableListOf<String>()  // Replace String with your data class
                        val results = json.getJSONArray("results")

                        // ２－２－３－１－２． Process results based on selected radio button
                        if (selectedOption == "user") {
                            // ２－２－３－１－２－１． If userRadio is selected
                            for (i in 0 until results.length()) {
                                val user = results.getJSONObject(i)
                                items.add(user.optString("username", "No username"))
                            }

                            // ２－２－３－１－２－２． Set the user data adapter
                            runOnUiThread {
                                val adapter = UserAdapter(items)
                                recyclerView.adapter = adapter
                                recyclerView.visibility = View.VISIBLE
                            }
                        } else if (selectedOption == "whisper") {
                            // ２－２－３－１－２－２． If whisperRadio is selected
                            for (i in 0 until results.length()) {
                                val whisper = results.getJSONObject(i)
                                items.add(whisper.optString("whisper_text", "No whisper"))
                            }

                            // ２－２－３－１－２－３． Set the whisper data adapter
                            runOnUiThread {
                                val adapter = WhisperAdapter(items)
                                recyclerView.adapter = adapter
                                recyclerView.visibility = View.VISIBLE
                            }
                        }
                    }
                }
            })
        }
    }
}
