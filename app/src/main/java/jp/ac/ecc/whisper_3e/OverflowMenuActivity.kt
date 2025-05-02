package com.example.whisperclient

import SearchActivity
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import jp.ac.ecc.whisper_3e.R
import jp.ac.ecc.whisper_3e.TimelineActivity

open class OverflowMenuActivity : AppCompatActivity() {

    companion object {
        var loginUserId: String = "" // グローバル変数としてのログインユーザID
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.whisper_recycle_row, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.timeline -> {
                startActivity(Intent(this, TimelineActivity::class.java))
                return true
            }

            R.id.search -> {
                startActivity(Intent(this, SearchActivity::class.java))
                return true
            }

            R.id.whisper -> {
                startActivity(Intent(this, WhisperPostActivity::class.java))
                return true
            }

            R.id.myprofile -> {
                val intent = Intent(this, ProfileActivity::class.java)
                intent.putExtra("USER_ID", loginUserId)
                startActivity(intent)
                return true
            }

            R.id.profileedit -> {
                val intent = Intent(this, ProfileEditActivity::class.java)
                intent.putExtra("USER_ID", loginUserId)
                startActivity(intent)
                return true
            }

            R.id.logout -> {
                loginUserId = ""
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                return true
            }

            else -> return super.onOptionsItemSelected(item)
        }
    }
}
