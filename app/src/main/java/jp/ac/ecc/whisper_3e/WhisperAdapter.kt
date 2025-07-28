package jp.ac.ecc.whisper_3e

import android.content.Context
import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity.MODE_PRIVATE
import androidx.recyclerview.widget.RecyclerView
import okhttp3.Call
import okhttp3.Callback
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import org.json.JSONObject
import java.io.IOException

//３．アダプター作成（クラス名：WhisperAdapter［引数：MutableList<WhisperRowData>、Context ］）
//３－１．RecyclerView.Adapterクラスを継承する。
class WhisperAdapter(
    private val whisperList: MutableList<WhisperRowData>,
    private val context: Context,
) : RecyclerView.Adapter<WhisperAdapter.WhisperViewHolder>() {

    //３－２．ビューホルダー（内部クラス）
    inner class WhisperViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //３－２－１．画面デザインで定義したオブジェクトを変数として宣言する。
        val userImage: ImageView = itemView.findViewById(R.id.userImage)
        val userNameText: TextView = itemView.findViewById(R.id.userNameText)
        val whisperText: TextView = itemView.findViewById(R.id.whisperText)
        val goodImage: ImageView = itemView.findViewById(R.id.goodImage)
    }

    //３－３．ビューホルダー生成時（onCreateViewHolder処理）
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WhisperViewHolder {
        //３－３－１．ささやき行情報の画面デザイン（whisper_recycle_row）をViewHolderに設定し、戻り値にセットする。
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.whisper_recycle_row, parent, false)
        return WhisperViewHolder(view)
    }

    //３－４．ビューホルダーバインド時（onBindViewHolder処理）
    override fun onBindViewHolder(holder: WhisperViewHolder, position: Int) {
        //３－４－１．ビューホルダーのオブジェクトに対象行のデータ（ユーザ名、ささやき）をセットする
        val item = whisperList[position]
        holder.userNameText.text = item.userName
        holder.whisperText.text = item.content

        //３－４－２．イイねフラグに併せて、いいね画像を切り替える。
        holder.goodImage.setImageResource(
            if (item.goodFlg) android.R.drawable.btn_star_big_on
            else android.R.drawable.btn_star_big_off
        )

        //３－４－３．userImageのクリックイベントリスナーを生成する
        holder.userImage.setOnClickListener {
            //３－４－３－１．Adapterから画面遷移することになるので、インテントに新しいタスクで起動する為のフラグを追加する。
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            //３－４－３－２．インテントに対象行のユーザIDをセットする
            intent.putExtra("USER_ID", item.userId)
            //３－４－３－３．ユーザ情報画面に遷移する
            context.startActivity(intent)
        }
        holder.itemView.setOnClickListener {
            val intent = Intent(context, UserInfoActivity::class.java)
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            intent.putExtra("USER_ID", item.userId)
            context.startActivity(intent)
        }

        //３－４－４．goodImageのクリックイベントリスナーを生成する
        holder.goodImage.setOnClickListener {
            val currentPosition = holder.adapterPosition
            //３－４－４－１．イイね管理処理APIをリクエストして入力した対象行のささやきのイイねの登録・解除を行う
            val client = OkHttpClient()
            val currentUserId = GlobalData.loginUserId ?: ""
            Log.d("currentUserId", "currentUserId: $currentUserId")
            val json = JSONObject().apply {
                put("userId", currentUserId)
                put("whisperNo", item.whisperNo)
                put("goodFlg", !item.goodFlg)
            }

            println(json)

            val mediaType = "application/json; charset=utf-8".toMediaTypeOrNull()
            val requestBody = json.toString().toRequestBody(mediaType)
            val request = Request.Builder()
                .url("https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/goodCtl.php")
                .post(requestBody)
                .build()

            client.newCall(request).enqueue(object : Callback {
                //３－４－４－１－１．正常にレスポンスを受け取った時(コールバック処理)
                override fun onResponse(call: Call, response: Response) {
                    val responseBody = response.body?.string()

                    //３－４－４－１－１－１．JSONデータがエラーの場合、受け取ったエラーメッセージをトースト表示して処理を終了させる
                    if (!response.isSuccessful || responseBody == null) {
                        Handler(Looper.getMainLooper()).post {
                            Toast.makeText(
                                context,
                                "サーバーからの応答が不正です。",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                        return
                    }

                    //３－４－４－１－１－２．対象行のいいねのレイアウトを切り替えるため、いいねフラグの変更を通知する。
                    whisperList[currentPosition].goodFlg = !item.goodFlg
                    Handler(Looper.getMainLooper()).post {
                        notifyItemChanged(currentPosition)
                    }
                }

                //３－４－４－１－２．リクエストが失敗した時(コールバック処理)
                override fun onFailure(call: Call, e: IOException) {
                    e.printStackTrace()
                    //３－４－４－１－２－１．エラーメッセージをトースト表示する
                    Handler(Looper.getMainLooper()).post {
                        Toast.makeText(context, "通信エラー: ${e.message}", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            })
        }
    }
    //３－５．行数取得時（getItemCount処理）
    //３－５－１．行リストの件数（データセットのサイズ）を戻り値にセットする
    override fun getItemCount(): Int = whisperList.size
}







