package jp.ac.ecc.whisper_3e

data class WhisperRowData(
    val whisperNo: Int,
    val userId: String,
    val userName: String,
    val postDate: String,
    val content: String,
    var goodCount: Int,
    var goodFlg: Boolean,
    val iconPath : String,
    val commentCount: Int
)
