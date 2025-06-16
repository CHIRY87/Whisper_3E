package jp.ac.ecc.whisper_3e;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import org.json.JSONException;
import org.json.JSONObject;

public class CreateUserActivity extends AppCompatActivity {

    private EditText nicknameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private Button createButton, cancelButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        // 1-1: オブジェクトの取得
        nicknameEdit = findViewById(R.id.userNameEdit);
        emailEdit = findViewById(R.id.userIdEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        confirmPasswordEdit = findViewById(R.id.rePasswordEdit);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        // 1-2: 作成ボタンの処理
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String nickname = nicknameEdit.getText().toString().trim();
                String email = emailEdit.getText().toString().trim();
                String password = passwordEdit.getText().toString().trim();
                String confirmPassword = confirmPasswordEdit.getText().toString().trim();

                // 1-2-1: 入力チェック
                if (nickname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                    Toast.makeText(CreateUserActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1-2-2: パスワード一致チェック
                if (!password.equals(confirmPassword)) {
                    Toast.makeText(CreateUserActivity.this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 1-2-3: ユーザ作成処理の実行
                new CreateUserTask().execute(nickname, email, password);
            }
        });

        // 1-3: キャンセルボタンの処理
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish(); // 自画面を終了
            }
        });
    }

    // 仮のAPI通信（非同期でユーザー作成）
    private class CreateUserTask extends AsyncTask<String, Void, JSONObject> {
        private boolean isSuccess = false;
        private String errorMessage = "通信に失敗しました";

        @Override
        protected JSONObject doInBackground(String... params) {
            try {
                // 仮のレスポンス（サーバー通信の代わり）
                Thread.sleep(1000); // 通信待ちを模倣

                // 仮の成功レスポンス
                JSONObject response = new JSONObject();
                response.put("success", true);
                response.put("userId", "mock_user_123");

                // 成功と判断
                isSuccess = response.getBoolean("success");
                return response;

            } catch (Exception e) {
                errorMessage = "エラー: " + e.getMessage();
                return null;
            }
        }

        @Override
        protected void onPostExecute(JSONObject result) {
            if (isSuccess && result != null) {
                try {
                    // 1-2-3-1-1: サーバーからエラーが返された場合
                    if (!result.getBoolean("success")) {
                        String message = result.optString("error", "ユーザ作成に失敗しました");
                        Toast.makeText(CreateUserActivity.this, message, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // 1-2-3-1-2: ユーザーIDをグローバルに保存
                    String createdUserId = result.getString("userId");
                    GlobalData.loginUserId = createdUserId;

                    // 1-2-3-1-3: タイムライン画面へ遷移
                    Intent intent = new Intent(CreateUserActivity.this, TimelineActivity.class);
                    startActivity(intent);

                    // 1-2-3-1-4: 自画面を閉じる
                    finish();

                } catch (JSONException e) {
                    Toast.makeText(CreateUserActivity.this, "データ解析エラー", Toast.LENGTH_SHORT).show();
                }
            } else {
                // 1-2-3-2-1: 通信失敗時のトースト表示
                Toast.makeText(CreateUserActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
