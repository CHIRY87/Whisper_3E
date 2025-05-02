package jp.ac.ecc.whisper_3e

import android.app.Application

class MyApplication : Application() {

    var loginUserId: String = ""
    var apiUrl: String = "https://" //  API　cua nhom

    companion object {
        private lateinit var instance: MyApplication

        fun getInstance(): MyApplication {
            return instance
        }
    }

    override fun onCreate() {
        super.onCreate()
        instance = this
    }
}
