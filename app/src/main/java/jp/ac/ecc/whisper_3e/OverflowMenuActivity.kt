
package jp.ac.ecc.whisper_3e

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts

open class OverflowMenuActivity : AppCompatActivity() {
    protected open fun onUserEdited() {
        // 子クラスでオーバーライドされる
    }

    protected lateinit var userEditLauncher: ActivityResultLauncher<Intent>


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        userEditLauncher = registerForActivityResult(
            ActivityResultContracts.StartActivityForResult()
        ) { result ->
            if (result.resultCode == RESULT_OK) {
                onUserEdited()
            }
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.whisper_recycle_row, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        val currentUserId = GlobalData.loginUserId
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
                startActivity(Intent(this, WhisperActivity::class.java))
                return true
            }

            R.id.myprofile -> {
                if (currentUserId.isNullOrEmpty()) {
                    Toast.makeText(this, "ユーザーIDが取得できませんでした", Toast.LENGTH_SHORT).show()
                    return true
                }
                val intent = Intent(this, UserInfoActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                Log.d("DEBUG_OVERFLOW", "Starting UserInfoActivity with USER_ID=$currentUserId")

                startActivity(intent)
                return true
            }

            R.id.profileedit -> {
                if (currentUserId.isNullOrEmpty()) {
                    Toast.makeText(this, "ユーザーIDが取得できませんでした", Toast.LENGTH_SHORT).show()
                    return true
                }
                val intent = Intent(this, UserEditActivity::class.java)
                intent.putExtra("USER_ID", currentUserId)
                userEditLauncher.launch(intent)
                return true
            }

            R.id.logout -> {
                GlobalData.loginUserId = null
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
