import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.appcompat.app.AppCompatActivity
import com.example.whisperclient.OverflowMenuActivity
import jp.ac.ecc.whisper_3e.R
import org.json.JSONObject
import java.util.*
import okhttp3.*
import java.io.IOException

class SearchActivity : OverflowMenuActivity() {

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

        searchEdit = findViewById(R.id.searchEdit)
        searchButton = findViewById(R.id.searchButton)
        radioGroup = findViewById(R.id.radioGroup)
        userRadio = findViewById(R.id.userRadio)
        whisperRadio = findViewById(R.id.whisperRadio)
        recyclerView = findViewById(R.id.searchRecycle)

        recyclerView.layoutManager = LinearLayoutManager(this)

        searchButton.setOnClickListener {
            val query = searchEdit.text.toString().trim()

            if (query.isEmpty()) {
                Toast.makeText(this, "検索内容を入力してください", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedOption = when (radioGroup.checkedRadioButtonId) {
                R.id.userRadio -> "user"
                R.id.whisperRadio -> "whisper"
                else -> ""
            }

            val request = Request.Builder()
                .url("https://api.example.com/search?type=$selectedOption&query=$query")  // Replace with actual endpoint
                .build()

            client.newCall(request).enqueue(object : Callback {
                override fun onFailure(call: Call, e: IOException) {
                    runOnUiThread {
                        Toast.makeText(this@SearchActivity, "エラーが発生しました: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onResponse(call: Call, response: Response) {
                    response.body?.let { body ->
                        val json = JSONObject(body.string())

                        if (json.has("error")) {
                            val errorMsg = json.getString("error")
                            runOnUiThread {
                                Toast.makeText(this@SearchActivity, errorMsg, Toast.LENGTH_SHORT).show()
                            }
                            return
                        }

                        val items = mutableListOf<String>()  // Replace String with your data class
                        val results = json.getJSONArray("results")

                        for (i in 0 until results.length()) {
                            val item = results.getJSONObject(i)
                            items.add(item.toString())  // Replace with actual parsing
                        }

                        runOnUiThread {
                            val adapter = if (selectedOption == "user") {
                                UserAdapter(items)
                            } else {
                                WhisperAdapter(items)
                            }

                            recyclerView.adapter = adapter
                            recyclerView.visibility = View.VISIBLE
                        }
                    }
                }
            })
        }
    }
}
