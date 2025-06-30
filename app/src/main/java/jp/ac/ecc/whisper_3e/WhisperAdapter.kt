package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class WhisperAdapter(
    private val whisperList: MutableList<WhisperRowData>,
    private val context: Context
) : RecyclerView.Adapter<WhisperAdapter.WhisperViewHolder>() {

    inner class WhisperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.whisper_recycle_row, parent, false)
        return WhisperViewHolder(view)
    }

    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        val item = whisperList[position]

        holder.userNameText.text = item.userName
        holder.whisperText.text = item.content

        // Set like icon
        holder.goodImage.setImageResource(
            if (item.goodFlg) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        // Navigate to user profile
        val goToUserInfo = {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                putExtra("userId", item.userId)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
            }
            context.startActivity(intent)
        }

        holder.userImage.setOnClickListener { goToUserInfo() }
        holder.itemView.setOnClickListener { goToUserInfo() }

        // Handle like click
        holder.goodImage.setOnClickListener {
            toggleLike(item, position)
        }
    }

    override fun getItemCount(): Int = whisperList.size

    private fun toggleLike(item: WhisperRowData, position: Int) {
        val url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userWhisperInfo.php"
        val client = OkHttpClient()

        val json = JSONObject().apply {
            put("userId", item.userId)
            put("whisperNo", item.whisperNo)
            put("goodFlg", !item.goodFlg)
        }

        val mediaType = "application/json; charset=utf-8".toMediaType()
        val requestBody = json.toString().toRequestBody(mediaType)

        val request = Request.Builder()
            .url(url)
            .post(requestBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, "通信に失敗しました", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                response.use {
                    if (!response.isSuccessful) {
                        showError("サーバーエラー: ${response.code}")
                        return
                    }

                    val jsonResponse = JSONObject(response.body?.string() ?: "{}")
                    if (jsonResponse.optString("result") == "success") {
                        // Since goodFlg is val, we can't change it directly
                        // Workaround: replace the object in the list
                        val updated = item.copy(goodFlg = !item.goodFlg)
                        whisperList[position] = updated
                        Handler(Looper.getMainLooper()).post {
                            notifyItemChanged(position)
                        }
                    } else {
                        showError("いいね処理に失敗しました")
                    }
                }
            }

            private fun showError(msg: String) {
                Handler(Looper.getMainLooper()).post {
                    Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                }
            }
        })
    }

    fun updateWhispers(newList: List<WhisperRowData>) {
        whisperList.clear()
        whisperList.addAll(newList)
        notifyDataSetChanged()
    }
}
