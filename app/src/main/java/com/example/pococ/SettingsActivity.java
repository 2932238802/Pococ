package com.example.pococ;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchMusic;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        LinearLayout btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> showLogoutDialog());

        LinearLayout btnMusicContainer = findViewById(R.id.btnMusic);
        switchMusic = findViewById(R.id.switchMusic);

        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);
        boolean isMusicEnabled = appPrefs.getBoolean("music_enabled", true);
        switchMusic.setChecked(isMusicEnabled);

        btnMusicContainer.setOnClickListener(v -> switchMusic.toggle());

        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPrefs.edit().putBoolean("music_enabled", isChecked).apply();
            if (isChecked) {
                startMusicService();
                Toast.makeText(this, "背景音乐已开启", Toast.LENGTH_SHORT).show();
            } else {
                stopMusicService();
                Toast.makeText(this, "背景音乐已关闭", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout btnReminderOffset = findViewById(R.id.btnReminderOffset);
        TextView tvReminderValue = findViewById(R.id.tvReminderValue);

        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);

        int savedOffset = userPrefs.getInt("reminder_offset", 0); // 默认0分钟
        updateReminderText(tvReminderValue, savedOffset);

        btnReminderOffset.setOnClickListener(v -> {
            final String[] options = {"准时", "提前 5 分钟", "提前 10 分钟", "提前 30 分钟", "提前 1 小时"};
            final int[] values = {0, 5, 10, 30, 60};

            new AlertDialog.Builder(this)
                    .setTitle("选择提醒时间")
                    .setItems(options, (dialog, which) -> {
                        int selectedMinutes = values[which];

                        userPrefs.edit().putInt("reminder_offset", selectedMinutes).apply();

                        updateReminderText(tvReminderValue, selectedMinutes);
                    })
                    .show();
        });


        LinearLayout adviceBtn = findViewById(R.id.adviceBtn);
        adviceBtn.setOnClickListener(v -> {
            sendFeedbackEmail();
        });

        LinearLayout sourceCodeBtn = findViewById(R.id.sourceCodeBtn);
        sourceCodeBtn.setOnClickListener(v -> {
            openUrl("https://github.com/");
        });

        LinearLayout aboutBtn = findViewById(R.id.aboutBtn);
        aboutBtn.setOnClickListener(v -> {
            Toast.makeText(this, "Poco List\nDesigned for Minimalists.", Toast.LENGTH_SHORT).show();
        });
    }

    private void updateReminderText(TextView textView, int minutes) {
        if (minutes == 0) {
            textView.setText("准时");
        } else if (minutes == 60) {
            textView.setText("提前 1 小时");
        } else {
            textView.setText("提前 " + minutes + " 分钟");
        }
    }

    private void startMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        startService(intent);
    }

    private void stopMusicService() {
        Intent intent = new Intent(this, MusicService.class);
        stopService(intent);
    }

    private void sendFeedbackEmail() {
        String[] recipientEmail = {"19857198709@163.com"};
        Intent intent = new Intent(Intent.ACTION_SENDTO);
        intent.setData(Uri.parse("mailto:"));
        intent.putExtra(Intent.EXTRA_EMAIL, recipientEmail);
        intent.putExtra(Intent.EXTRA_SUBJECT, "有啥建议尽管说");
        intent.putExtra(Intent.EXTRA_TEXT, "说吧您就" );
        try {
            startActivity(Intent.createChooser(intent, "选择邮件客户端发送反馈"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "未找到已安装的邮件客户端", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("登出")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> {
                    performLogout();
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        userPrefs.edit().clear().apply();

        Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        Toast.makeText(this, "已安全登出", Toast.LENGTH_SHORT).show();
    }

    private void openUrl(String url) {
        try {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(url));
            startActivity(intent);
        } catch (Exception e) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }
}
