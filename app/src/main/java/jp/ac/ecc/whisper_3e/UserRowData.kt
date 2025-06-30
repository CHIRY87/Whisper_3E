package jp.ac.ecc.whisper_3e

data class UserRowData(
    val userId: String,
    val userName: String,
    val whisperCount: Int,
    val followCount: Int,
    val followerCount: Int,
    val imagePath: String
)
