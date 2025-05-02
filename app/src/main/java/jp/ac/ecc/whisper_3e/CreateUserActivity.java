package jp.ac.ecc.whisper_3e;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class CreateUserActivity extends AppCompatActivity {

    private EditText nicknameEdit, emailEdit, passwordEdit, confirmPasswordEdit;
    private Button createButton, cancelButton;

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

        // ユーザ作成ボタンのクリックイベントリスナー
        createButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 入力項目の検証
                if (nicknameEdit.getText().toString().isEmpty() ||
                        emailEdit.getText().toString().isEmpty() ||
                        passwordEdit.getText().toString().isEmpty() ||
                        confirmPasswordEdit.getText().toString().isEmpty()) {
                    Toast.makeText(CreateUserActivity.this, "全ての項目を入力してください", Toast.LENGTH_SHORT).show();
                    return;
                }

                // パスワード確認
                if (!passwordEdit.getText().toString().equals(confirmPasswordEdit.getText().toString())) {
                    Toast.makeText(CreateUserActivity.this, "パスワードが一致しません", Toast.LENGTH_SHORT).show();
                    return;
                }

                // ユーザ作成処理API呼び出し（仮のAPIリクエスト）
                createUser();
            }
        });

        // キャンセルボタンのクリックイベントリスナー
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // この画面を閉じる
                finish();
            }
        });
    }

    private void createUser() {
        // APIリクエストを行い、ユーザを作成する処理
        // 仮に成功した場合の処理
        // 例えば、レスポンスが正常であれば以下のような処理になります
        String createdUserId = "new_user_id";  // 仮のユーザID
        Toast.makeText(CreateUserActivity.this, "ユーザが作成されました", Toast.LENGTH_SHORT).show();

        // グローバル変数にユーザIDを格納
        // loginUserId = createdUserId;

        // タイムライン画面に遷移
        // Intent intent = new Intent(CreateUserActivity.this, TimelineActivity.class);
        // startActivity(intent);

        // 自分の画面を閉じる
        finish();
    }
}
