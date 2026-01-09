package com.example.pococ;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    private EditText usernameEdit, passwordEdit, confirmPasswordEdit;
    private Button registerBtn;
    private TextView backToLoginTextView;

    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        databaseHelper = new DatabaseHelper(this);

        usernameEdit = findViewById(R.id.etRegUsername);
        passwordEdit = findViewById(R.id.etRegPassword);
        confirmPasswordEdit = findViewById(R.id.etRegConfirmPassword);
        registerBtn = findViewById(R.id.btnRegisterAction);
        backToLoginTextView = findViewById(R.id.tvBackToLogin);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleRegister();
            }
        });

        backToLoginTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
    }

    private void handleRegister() {
        String username = usernameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();
        String confirmPassword = confirmPasswordEdit.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入完整信息", Toast.LENGTH_SHORT).show();
            return;
        }

        if (!password.equals(confirmPassword)) {
            Toast.makeText(this, "两次输入的密码不一致", Toast.LENGTH_SHORT).show();
            return;
        }

        if (databaseHelper.checkUserExist(username)) {
            Toast.makeText(this, "该用户名已被注册", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean isRegistered = databaseHelper.registerUser(username, password);

        if (isRegistered) {
            Toast.makeText(this, "注册成功！请登录", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Toast.makeText(this, "注册失败，请重试", Toast.LENGTH_SHORT).show();
        }
        finish();
    }
}
