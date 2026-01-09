package com.example.pococ;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    private EditText userNameEdit;
    private EditText passwordEdit;
    private Button loginBtn;
    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        SharedPreferences prefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        // 这样的话 你之前登陆过了 就没事了
        String savedUser = userPrefs.getString("current_user", null);
        if (savedUser != null) {
            goToMainPage(savedUser);
            return;
        }

        // 这个是音乐的开启
        boolean isMusicEnabled = prefs.getBoolean("music_enabled", true); // 默认为 true
        if (isMusicEnabled) {
            Intent intent = new Intent(this, MusicService.class);
            startService(intent);
        }

        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        userNameEdit = findViewById(R.id.etUsername);
        passwordEdit = findViewById(R.id.etPassword);
        loginBtn = findViewById(R.id.btnLogin);
        TextView registerTextView = findViewById(R.id.tvRegister);

        loginBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                handleLogin();
            }
        });

        registerTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(i);
            }
        });
    }

    private void handleLogin() {
        String username = userNameEdit.getText().toString().trim();
        String password = passwordEdit.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "请输入用户名和密码", Toast.LENGTH_SHORT).show();
            return;
        }

        boolean loginSuccess = dbHelper.checkUserLogin(username, password);
        boolean isAdmin = username.equals("admin") && password.equals("123456");

        if (loginSuccess || isAdmin) {
            Toast.makeText(this, "登录成功！", Toast.LENGTH_SHORT).show();

            SharedPreferences userPrefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            userPrefs.edit().putString("current_user", username).apply();

            goToMainPage(username);
        } else {
            Toast.makeText(this, "用户名或密码错误", Toast.LENGTH_SHORT).show();
        }
    }

    private void goToMainPage(String username) {
        Intent intent = new Intent(LoginActivity.this, PocoListActivity.class);
        Toast.makeText(this, "欢迎回来, " + username + "!", Toast.LENGTH_SHORT).show();
        intent.putExtra("USER_NAME", username);
        startActivity(intent);
        finish();
    }
}