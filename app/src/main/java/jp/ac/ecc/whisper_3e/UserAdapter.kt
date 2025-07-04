package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide

class UserAdapter(
    private val context: Context,
    private val userList: MutableList<UserRowData>
) : RecyclerView.Adapter<UserAdapter.UserViewHolder>() {

    // ３－２．ビューホルダー（内部クラス）
    // ３－２－１．画面デザインで定義したオブジェクトを変数として宣言する。
    class UserViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val userImage: ImageView = view.findViewById(R.id.userImage)
        val userNameText: TextView = view.findViewById(R.id.userNameText)
        val followCntText: TextView = view.findViewById(R.id.followCntText)
        val followerCntText: TextView = view.findViewById(R.id.followerCntText)
    }

    // ３－３．ビューホルダー生成時（onCreateViewHolder処理）
    // ３－３－１．ユーザ行情報の画面デザイン（user_recycle_row）をViewHolderに設定し、戻り値にセットする。
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.user_recycle_row, parent, false)
        return UserViewHolder(view)
    }

    // ３－４．ビューホルダーバインド時（onBindViewHolder処理）
    override fun onBindViewHolder(holder: UserViewHolder, position: Int) {
        val user = userList[position]

        // ３－４－１．ビューホルダーのオブジェクトに対象行のデータ（ユーザ名、フォロー数、フォロワー数）をセットする
        holder.userNameText.text = user.userName
        holder.followCntText.text = user.followCount.toString()
        holder.followerCntText.text = user.followerCount.toString()

        // ３－４－２．userImageのクリックイベントリスナーを生成する
        holder.userImage.setOnClickListener {
            // ３－４－２－１．Adapterから画面遷移することになるので、インテントに新しいタスクで起動する為のフラグを追加する。
            // ３－４－２－２．インテントに対象行のユーザIDをセットする
            // ３－４－２－３．ユーザ情報画面に遷移する
            Log.d("UserAdapter", "Clicked userId: ${user.userId}")
            if (user.userId.isNullOrBlank()) {
                Toast.makeText(context, "User ID is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("UserAdapter", "Clicked userId: ${user.userId}")
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra("USER_ID", user.userId) // ← this is missing in your case!
            context.startActivity(intent)
            Log.d("UserAdapter", "Final userId sent: '${user.userId}'")
        }

        // ここで行全体（itemView）にもクリックリスナーを設定し、
        // クリックされたらユーザ情報画面に遷移させる
        holder.itemView.setOnClickListener {
            if (user.userId.isNullOrBlank()) {
                Toast.makeText(context, "User ID is missing", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            Log.d("UserAdapter", "Clicked userId: ${user.userId}")
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.putExtra("USER_ID", user.userId) // ← this is missing in your case!
            context.startActivity(intent)
            Log.d("UserAdapter", "Final userId sent: '${user.userId}'")
        }
    }

    // ３－５．行数取得時（getItemCount処理）
    // ３－５－１．行リストの件数（データセットのサイズ）を戻り値にセットする
    override fun getItemCount(): Int = userList.size
}