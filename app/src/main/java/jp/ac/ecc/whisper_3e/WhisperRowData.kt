package jp.ac.ecc.whisper_3e

data class WhisperRowData(
    val userId: String,
    val userName: String,
    val whisperId: String,
    val whisperText: String,
    val userImage: String, //
    var isLiked: Boolean
)

