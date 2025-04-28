package jp.ac.ecc.whisper_3e

import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView

class TimelineActivity : OverflowMenu() {

    // ２－１．画面デザインで定義したオブジェクトを変数として宣言する。
    private lateinit var timelineRecycle: RecyclerView

    // ２－２．グローバル変数のログインユーザーIDを取得。
    private var loginUserId: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_timeline) // あなたのXMLファイル名に合わせてください

        // ２－１．画面デザインで定義したオブジェクトを変数として宣言する。
        timelineRecycle = findViewById(R.id.timelineRecycle)

        // ２－２．グローバル変数のログインユーザーIDを取得。
        loginUserId = GlobalData.loginUserId

        // ２－３．タイムライン情報取得APIをリクエストしてログインユーザが確認できるささやき情報取得を行う
        fetchTimeline()
    }

    private fun fetchTimeline() {
        // API通信はここで行います。ここでは仮の例としてFakeApiを使用します。
        FakeApi.getTimeline(loginUserId ?: "") { result, error ->

            if (error != null) {
                // ２－３－２－１．エラーメッセージをトースト表示する
                Toast.makeText(this, "リクエスト失敗: $error", Toast.LENGTH_SHORT).show()
            } else if (result != null) {
                if (result.hasError) {
                    // ２－３－１－１．JSONデータがエラーの場合、受け取ったエラーメッセージをトースト表示して処理を終了させる
                    Toast.makeText(this, "エラー: ${result.errorMessage}", Toast.LENGTH_SHORT).show()
                } else {
                    // ２－３－１－２．ささやき情報一覧が存在する間、以下の処理を繰り返す
                    val whispersList = result.whispers // ささやき情報をリストに格納

                    // ２－３－１－３．timelineRecycleにささやき情報リストをセットする
                    val adapter = TimelineAdapter(whispersList)
                    timelineRecycle.adapter = adapter
                }
            }
        }
    }
}
