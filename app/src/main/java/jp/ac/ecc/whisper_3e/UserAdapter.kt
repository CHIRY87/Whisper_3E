package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

//class UserAdapter(
//    private val context: Context,
//    private val userList: MutableList<UserRowData>
//) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {
//
//    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
//        val userImage: ImageView = view.findViewById(R.id.userImage)
//        val userNameText: TextView = view.findViewById(R.id.userNameText)
//        val followText: TextView = view.findViewById(R.id.followText)
//        val followCntText: TextView = view.findViewById(R.id.followCntText)
//        val followerText: TextView = view.findViewById(R.id.followerText)
//        val followerCntText: TextView = view.findViewById(R.id.followerCntText)
//    }
//
//    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
//        val view = LayoutInflater.from(parent.context)
//            .inflate(R.layout.user_recycle_row, parent, false)
//        return ViewHolder(view)
//    }
//
//    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
//        val user = userList[position]
//
//        holder.userNameText.text = user.userName
//        holder.followCntText.text = user.followCount.toString()
//        holder.followerCntText.text = user.followerCount.toString()
//
//        // Load ảnh user
//        if (user.imagePath.isNotEmpty()) {
//            Glide.with(context).load(user.imagePath).into(holder.userImage)
//        } else {
//            holder.userImage.setImageResource(R.drawable.ic_launcher_background)
//        }
//
//        // Click vào ảnh → mở UserInfoActivity
//        holder.userImage.setOnClickListener {
//            val intent = Intent(context, UserInfoActivity::class.java).apply {
//                flags = Intent.FLAG_ACTIVITY_NEW_TASK
//                putExtra("userId", user.userId)
//            }
//            context.startActivity(intent)
//        }
//    }
//
//    override fun getItemCount(): Int = userList.size
//} sửa ngày 27/06

class UserAdapter(
    private val context: Context,
    private val userList: List<UserRowData>,
    private val onUserClick: (UserRowData) -> Unit
) : RecyclerView.Adapter<UserAdapter.ViewHolder>() {

    inner class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userNameText: TextView = view.findViewById(R.id.userNameText)

        init {
            view.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onUserClick(userList[position])
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.user_recycle_row, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.userNameText.text = userList[position].userName
    }

    override fun getItemCount(): Int = userList.size
}
