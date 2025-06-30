import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.ac.ecc.whisper_3e.GoodRowData
import jp.ac.ecc.whisper_3e.R
import jp.ac.ecc.whisper_3e.UserInfoActivity

class GoodAdapter(
    private val goodList: MutableList<GoodRowData>,
    private val context: Context
) : RecyclerView.Adapter<GoodAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val whisperText: TextView = view.findViewById(R.id.whisperText)
        val goodFlgText: TextView = view.findViewById(R.id.goodFlgText) // Hiển thị trạng thái like
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.good_recycle_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = goodList[position]

        holder.userNameText.text = item.userName
        holder.whisperText.text = item.content
        holder.goodFlgText.text = if (item.goodCount > 0) "いいね済み" else "未いいね"

        if (item.iconPath.isNotEmpty()) {
            Glide.with(context).load(item.iconPath).into(holder.userImage)
        } else {
            holder.userImage.setImageResource(R.drawable.kirito)
        }

        holder.userImage.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                putExtra("userId", item.userId)
            }
            context.startActivity(intent)
        }
    }


    override fun getItemCount(): Int = goodList.size
}
