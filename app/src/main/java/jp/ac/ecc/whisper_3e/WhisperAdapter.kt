//import android.content.Context
//import android.view.LayoutInflater
//import android.view.View
//import android.view.ViewGroup
//import android.widget.ImageView
//import android.widget.TextView
//import androidx.recyclerview.widget.RecyclerView
//import com.bumptech.glide.Glide
//import jp.ac.ecc.whisper_3e.R
//import jp.ac.ecc.whisper_3e.WhisperRowData

//class WhisperAdapter(
//    private val whisperList: MutableList<WhisperRowData>,
//    private val context: Context,
//    private val onUserImageClick: (WhisperRowData) -> Unit
//) : RecyclerView.Adapter<WhisperAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val userImage: ImageView = view.findViewById(R.id.userImage)
//        val userNameText: TextView = view.findViewById(R.id.userNameText)
//        val whisperText: TextView = view.findViewById(R.id.whisperText)
//        val goodImage: ImageView = view.findViewById(R.id.goodImage)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.whisper_recycle_row, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val whisper = whisperList[position]
//
//        holder.userNameText.text = whisper.userName
//        holder.whisperText.text = whisper.content
//
//        if (whisper.iconPath.isNotEmpty()) {
//            Glide.with(context).load(whisper.iconPath).into(holder.userImage)
//        } else {
//            holder.userImage.setImageResource(R.drawable.ic_launcher_background)
//        }
//
//        holder.goodImage.setImageResource(
//            if (whisper.goodFlg) R.drawable.btn_star_big_on
//            else R.drawable.btn_star_big_off
//        )
//
//        holder.userImage.setOnClickListener {
//            onUserImageClick(whisper)
//        }
//
//        holder.goodImage.setOnClickListener {
//            val currentPosition = holder.adapterPosition
//            if (currentPosition == RecyclerView.NO_POSITION) return@setOnClickListener
//
//            val isLiked = (whisper.goodFlg)
//            val newFlag = !isLiked
//            whisper.goodFlg = newFlag
//
//            // Giả lập API (bạn thay bằng gọi thật nếu cần)
//            whisper.goodFlg = newFlag
//            notifyItemChanged(currentPosition)
//        }
//    }
//
//    override fun getItemCount(): Int = whisperList.size
//}sửa ngày 27/06

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import jp.ac.ecc.whisper_3e.R
import jp.ac.ecc.whisper_3e.WhisperRowData

class WhisperAdapter(
    private val whisperList: MutableList<WhisperRowData>,
    private val context: Context,
    private val onUserImageClick: (WhisperRowData) -> Unit,
    private val onGoodClick: (WhisperRowData, Int) -> Unit  // truyền position
) : RecyclerView.Adapter<WhisperAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val whisperText: TextView = view.findViewById(R.id.whisperText)
        val goodImage: ImageView = view.findViewById(R.id.goodImage)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.whisper_recycle_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val whisper = whisperList[position]

        holder.userNameText.text = whisper.userName
        holder.whisperText.text = whisper.content


        if (whisper.iconPath.isNotEmpty()) {
            Glide.with(context).load(whisper.iconPath).into(holder.userImage)
        } else {
            holder.userImage.setImageResource(R.drawable.ic_launcher_background)
        }

        holder.goodImage.setImageResource(
            if (whisper.goodFlg) R.drawable.btn_star_big_on else R.drawable.btn_star_big_off
        )

        holder.userImage.setOnClickListener {
            onUserImageClick(whisper)
        }

        holder.goodImage.setOnClickListener {
            val pos = holder.adapterPosition
            if (pos != RecyclerView.NO_POSITION) {
                onGoodClick(whisper, pos)
            }
        }
    }

    override fun getItemCount(): Int = whisperList.size
}