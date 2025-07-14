package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

//３．アダプター作成（クラス名：GoodAdapter［引数：MutableList<GoodRowData>、Context ］）
//３－１．RecyclerView.Adapterクラスを継承する。
class GoodAdapter(
    private val items: MutableList<GoodRowData>,
    private val context: Context
) : RecyclerView.Adapter<GoodAdapter.GoodViewHolder>() {
    //３－２．ビューホルダー（内部クラス）
    inner class GoodViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //３－２－１．画面デザインで定義したオブジェクトを変数として宣言する。
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        var goodCountText: TextView = itemView.findViewById(R.id.goodCountText)
    }
    //３－３．ビューホルダー生成時（onCreateViewHolder処理）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoodViewHolder {
        //３－３－１．いいね行情報の画面デザイン（good_recycle_row）をViewHolderに設定し、戻り値にセットする。
        val view = LayoutInflater.from(context)
            .inflate(R.layout.good_recycle_row, parent, false)
        return GoodViewHolder(view)
    }

    //３－４．ビューホルダーバインド時（onBindViewHolder処理）
    override fun onBindViewHolder(holder: GoodViewHolder, position: Int) {
        //３－４－１．ビューホルダーのオブジェクトに対象行のデータ（ユーザ名、ささやき内容、いいね数）をセットする
        val currentItem = items[position]
        holder.userNameText.text = currentItem.userName
        holder.whisperText.text = currentItem.content
        holder.goodCountText.text = currentItem.goodCount.toString()

        //３－４－２．userImageのクリックイベントリスナーを生成する
        holder.userImage.setOnClickListener {
            //３－４－２－１．Adapterから画面遷移することになるので、インテントに新しいタスクで起動する為のフラグを追加する。
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //３－４－２－２．インテントに対象行のユーザIDをセットする
            intent.putExtra("USER_ID", currentItem.userId)
            //３－４－２－３．ユーザ情報画面に遷移する
            context.startActivity(intent)
        }
    }

    //３－５．行数取得時（getItemCount処理）
    //３－５－１．行リストの件数（データセットのサイズ）を戻り値にセットする
    override fun getItemCount(): Int = items.size
}