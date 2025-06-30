package jp.ac.ecc.whisper_3e

data class GoodRowData(
    val whisperNo: Int,
    val userId: String,
    val userName: String,
    val postDate: String,
    val content: String,
    val goodFlg: Boolean
)