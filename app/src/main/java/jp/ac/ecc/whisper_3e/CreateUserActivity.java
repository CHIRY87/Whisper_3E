package jp.ac.ecc.whisper_3e;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.*;

public class CreateUserActivity extends AppCompatActivity {

    private EditText nicknameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private Button createButton, cancelButton;
    private final OkHttpClient client = new OkHttpClient();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);

        nicknameEdit = findViewById(R.id.userNameEdit);
        emailEdit = findViewById(R.id.userIdEdit);
        passwordEdit = findViewById(R.id.passwordEdit);
        confirmPasswordEdit = findViewById(R.id.rePasswordEdit);
        createButton = findViewById(R.id.createButton);
        cancelButton = findViewById(R.id.cancelButton);

        createButton.setOnClickListener(v -> {
            String nickname = nicknameEdit.getText().toString().trim();
            String email = emailEdit.getText().toString().trim();
            String password = passwordEdit.getText().toString().trim();
            String confirmPassword = confirmPasswordEdit.getText().toString().trim();

            if (nickname.isEmpty() || email.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
                Toast.makeText(this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!password.equals(confirmPassword)) {
                Toast.makeText(this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                return;
            }

            sendCreateUserRequest(nickname, email, password);
        });

        cancelButton.setOnClickListener(v -> finish());
    }

    private void sendCreateUserRequest(String nickname, String email, String password) {
        String url = "https://click.ecc.ac.jp/ecc/whisper25_e/PHP_Whisper_3E/userAdd.php";

        JSONObject json = new JSONObject();
        try {
            json.put("userId", email);
            json.put("userName", nickname);
            json.put("password", password);
        } catch (JSONException e) {
            e.printStackTrace();
            return;
        }

        RequestBody body = RequestBody.create(
                json.toString(),
                MediaType.parse("application/json; charset=utf-8")
        );

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(CreateUserActivity.this, "通信エラー: " + e.getMessage(), Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                String resStr = response.body().string();
                runOnUiThread(() -> {
                    try {
                        JSONObject res = new JSONObject(resStr);
                        if ("success".equals(res.optString("result"))) {
                            Toast.makeText(CreateUserActivity.this, "ユーザー登録成功", Toast.LENGTH_SHORT).show();
                            GlobalData.loginUserId = email;
                            startActivity(new Intent(CreateUserActivity.this, TimelineActivity.class));
                            finish();
                        } else {
                            String errMsg = res.optString("errMsg", "登録に失敗しました");
                            Toast.makeText(CreateUserActivity.this, errMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(CreateUserActivity.this, "レスポンス解析エラー", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }
}
