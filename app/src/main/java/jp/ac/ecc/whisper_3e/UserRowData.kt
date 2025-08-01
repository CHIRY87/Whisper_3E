package jp.ac.ecc.whisper_3e

data class UserRowData(
    val userId: String,
    val userName: String,
    val profile: String,
    val userFollowFlg: Boolean,
    val followCount: Int,
    val followerCount: Int,
    val whisperList: List<WhisperRowData>,
    val goodList: List<GoodRowData>
)
