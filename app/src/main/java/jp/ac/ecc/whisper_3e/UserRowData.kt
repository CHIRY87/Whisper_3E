package jp.ac.ecc.whisper_3e

data class UserRowData(
    val userId: String,
    val userName: String,
    val followCount: Int,
    val followerCount: Int,

    val imagePath: String
)
