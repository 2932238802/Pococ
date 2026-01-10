package com.example.pococ;

import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.SwitchCompat;
import androidx.core.content.FileProvider;

import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;

// === 修正 import ===
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends BaseActivity {

    private SwitchCompat switchMusic;
    private SwitchCompat switchNightMode;
    private SwitchCompat switchQuote;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
    }

    private void initViews() {
        ImageView btnBack = findViewById(R.id.btnBack);
        btnBack.setOnClickListener(v -> finish());

        TextView tvUserName = findViewById(R.id.tvUserName);
        SharedPreferences userPrefs = getSharedPreferences("UserPrefs", MODE_PRIVATE);
        String currentUser = userPrefs.getString("current_user", "User");
        tvUserName.setText("Hi, " + currentUser);

        SharedPreferences appPrefs = getSharedPreferences("AppSettings", Context.MODE_PRIVATE);

        LinearLayout btnMusicContainer = findViewById(R.id.btnMusic);
        switchMusic = findViewById(R.id.switchMusic);
        boolean isMusicEnabled = appPrefs.getBoolean("music_enabled", true);
        switchMusic.setChecked(isMusicEnabled);
        if (isMusicEnabled) {
            startMusicService();
        }

        btnMusicContainer.setOnClickListener(v -> switchMusic.toggle());
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPrefs.edit().putBoolean("music_enabled", isChecked).apply();
            if (isChecked) {
                startMusicService();
            } else {
                stopMusicService();
            }
        });

        LinearLayout btnNightModeContainer = findViewById(R.id.btnNightMode);
        switchNightMode = findViewById(R.id.switchNightMode);
        boolean isNightMode = appPrefs.getBoolean("night_mode", false);
        switchNightMode.setChecked(isNightMode);
        btnNightModeContainer.setOnClickListener(v -> switchNightMode.toggle());
        switchNightMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPrefs.edit().putBoolean("night_mode", isChecked).apply();
            if (isChecked) {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
            } else {
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
            }
        });

        LinearLayout btnQuoteContainer = findViewById(R.id.btnQuote);
        switchQuote = findViewById(R.id.switchQuote);
        boolean isQuoteEnabled = appPrefs.getBoolean("quote_enabled", true);
        switchQuote.setChecked(isQuoteEnabled);
        btnQuoteContainer.setOnClickListener(v -> switchQuote.toggle());
        switchQuote.setOnCheckedChangeListener((buttonView, isChecked) -> {
            appPrefs.edit().putBoolean("quote_enabled", isChecked).apply();
            if (!isChecked) {
                Toast.makeText(SettingsActivity.this, "每日一句已关闭，重启应用以生效~", Toast.LENGTH_SHORT).show();
            }
        });

        LinearLayout btnFontSize = findViewById(R.id.btnFontSize);
        TextView tvFontSizeValue = findViewById(R.id.tvFontSizeValue);
        float currentFontScale = appPrefs.getFloat("font_scale", 1.0f);
        updateFontSizeText(tvFontSizeValue, currentFontScale);
        btnFontSize.setOnClickListener(v -> showFontSizeDialog(appPrefs));

        LinearLayout btnReminderOffset = findViewById(R.id.btnReminderOffset);
        TextView tvReminderValue = findViewById(R.id.tvReminderValue);
        int savedOffset = userPrefs.getInt("reminder_offset", 0);
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

        LinearLayout btnClearCache = findViewById(R.id.btnClearCache);
        btnClearCache.setOnClickListener(v -> clearAppCache());

        LinearLayout adviceBtn = findViewById(R.id.adviceBtn);
        adviceBtn.setOnClickListener(v -> sendFeedbackEmail());

        LinearLayout sourceCodeBtn = findViewById(R.id.sourceCodeBtn);
        sourceCodeBtn.setOnClickListener(v -> openUrl("https://github.com/2932238802/Pococ.git"));

        LinearLayout aboutBtn = findViewById(R.id.aboutBtn);
        aboutBtn.setOnClickListener(v -> Toast.makeText(this, "LosAngelous工作室,爱你每一天", Toast.LENGTH_SHORT).show());

        LinearLayout btnLogOut = findViewById(R.id.btnLogOut);
        btnLogOut.setOnClickListener(v -> showLogoutDialog());

        LinearLayout btnCheckUpdate = findViewById(R.id.btnCheckUpdate);
        btnCheckUpdate.setOnClickListener(v -> checkUpdate());
    }

    private void checkUpdate() {
        Toast.makeText(this, "正在检查更新...", Toast.LENGTH_SHORT).show();
        String url = " https://gitee.com/lsjwillwin/Pococ/raw/main/version.json";
//        https://gitee.com/lsjwillwin/Pococ/raw/main/version.json
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "检查更新失败，请稍后再试", Toast.LENGTH_SHORT).show());
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    VersionInfo versionInfo = new Gson().fromJson(json, VersionInfo.class);
                    int currentVersionCode = getCurrentVersionCode();

                    if (versionInfo != null && versionInfo.getVersionCode() > currentVersionCode) {
                        runOnUiThread(() -> showUpdateDialog(versionInfo));
                    } else {
                        runOnUiThread(() -> Toast.makeText(SettingsActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show());
                    }
                } else {
                    onFailure(call, new IOException("Response not successful"));
                }
            }
        });
    }

    private int getCurrentVersionCode() {
        try {
            PackageInfo packageInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void showUpdateDialog(VersionInfo versionInfo) {
        new AlertDialog.Builder(this)
                .setTitle("发现新版本: " + versionInfo.getVersionName())
                .setMessage(versionInfo.getUpdateLog())
                .setPositiveButton("立即更新", (dialog, which) -> downloadAndInstallApk(versionInfo.getApkUrl()))
                .setNegativeButton("稍后", null)
                .show();
    }

    private void downloadAndInstallApk(String url) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                new AlertDialog.Builder(this)
                        .setTitle("需要安装权限")
                        .setMessage("为了安装更新，需要您授予安装未知应用权限")
                        .setPositiveButton("去授权", (dialog, which) -> {
                            Intent intent = new Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:" + getPackageName())
                            );
                            startActivityForResult(intent, 1001);
                        })
                        .setNegativeButton("取消", null)
                        .setCancelable(false)
                        .show();
                return;
            }
        }

        Toast.makeText(this, "开始下载更新包...", Toast.LENGTH_SHORT).show();

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Toast.makeText(this, "系统下载服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
        request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
        request.setTitle("Poco List 更新");
        request.setDescription("正在下载新版本...");

        File downloadDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates");
        if (!downloadDir.exists()) {
            downloadDir.mkdirs();
        }
        File apkFile = new File(downloadDir, "PocoList_update.apk");
        request.setDestinationUri(Uri.fromFile(apkFile));

        long downloadId = downloadManager.enqueue(request);

        BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                if (id == downloadId) {
                    Toast.makeText(context, "下载完成，准备安装...", Toast.LENGTH_SHORT).show();
                    installApk(context, apkFile);

                    try {
                        unregisterReceiver(this);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        };

        registerReceiver(downloadReceiver, new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE));
    }

    private void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) {
            Toast.makeText(context, "安装包不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent installIntent = new Intent(Intent.ACTION_VIEW);
        installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            Uri apkUri = FileProvider.getUriForFile(
                    context,
                    getPackageName() + ".provider",
                    apkFile
            );
            installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        } else {
            installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
        }

        try {
            context.startActivity(installIntent);
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, "安装失败: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == 1001) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    Toast.makeText(this, "权限已授予，请重新点击'立即更新'", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, "未授予权限，无法更新", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }


    // Gson 解析类
    private static class VersionInfo {
        private int versionCode;
        private String versionName;
        private String updateLog;
        private String apkUrl;

        public int getVersionCode() { return versionCode; }
        public String getVersionName() { return versionName; }
        public String getUpdateLog() { return updateLog; }
        public String getApkUrl() { return apkUrl; }
    }


    private void showFontSizeDialog(SharedPreferences appPrefs) {
        final String[] options = {"小", "标准", "大", "特大"};
        final float[] scales = {0.85f, 1.0f, 1.15f, 1.3f};

        float currentScale = appPrefs.getFloat("font_scale", 1.0f);
        int checkedItem = 1;
        for (int i = 0; i < scales.length; i++) {
            if (currentScale == scales[i]) {
                checkedItem = i;
                break;
            }
        }

        new AlertDialog.Builder(this)
                .setTitle("选择字体大小")
                .setSingleChoiceItems(options, checkedItem, (dialog, which) -> {
                    float selectedScale = scales[which];
                    if (currentScale != selectedScale) {
                        SharedPreferences.Editor editor = appPrefs.edit();
                        editor.putFloat("font_scale", selectedScale);
                        editor.putBoolean("needs_recreate", true);
                        editor.apply();
                        dialog.dismiss();
                        recreate();
                    } else {
                        dialog.dismiss();
                    }
                })
                .setNegativeButton("取消", null)
                .show();
    }

    private void updateFontSizeText(TextView textView, float scale) {
        if (scale == 0.85f) textView.setText("小");
        else if (scale == 1.0f) textView.setText("标准");
        else if (scale == 1.15f) textView.setText("大");
        else if (scale == 1.3f) textView.setText("特大");
    }

    private void updateReminderText(TextView textView, int minutes) {
        if (minutes == 0) textView.setText("准时");
        else if (minutes == 60) textView.setText("提前 1 小时");
        else textView.setText("提前 " + minutes + " 分钟");
    }

    private void clearAppCache() {
        try {
            File cacheDir = getCacheDir();
            if (cacheDir == null || !cacheDir.isDirectory()) {
                Toast.makeText(this, "没有找到缓存目录", Toast.LENGTH_SHORT).show();
                return;
            }
            long cacheSize = getDirSize(cacheDir);
            deleteDir(cacheDir);
            String formattedSize = formatSize(cacheSize);

            String message = "缓存已清理完毕 (" + formattedSize + ")";
            Toast.makeText(this, message, Toast.LENGTH_SHORT).show();

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "清理失败", Toast.LENGTH_SHORT).show();
        }
    }

    private long getDirSize(File dir) {
        long size = 0;
        if (dir != null && dir.isDirectory()) {
            File[] files = dir.listFiles();
            if (files != null) {
                for (File file : files) {
                    if (file.isFile()) {
                        size += file.length();
                    } else if (file.isDirectory()) {
                        size += getDirSize(file);
                    }
                }
            }
        }
        return size;
    }

    private String formatSize(long size) {
        if (size <= 0) return "0 B";
        final String[] units = new String[]{"B", "KB", "MB", "GB", "TB"};
        int digitGroups = (int) (Math.log10(size) / Math.log10(1024));
        return String.format(java.util.Locale.getDefault(), "%.2f %s", size / Math.pow(1024, digitGroups), units[digitGroups]);
    }

    private boolean deleteDir(File dir) {
        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            if (children != null) {
                for (String child : children) {
                    if (!deleteDir(new File(dir, child))) return false;
                }
            }
        }
        return dir != null && dir.delete();
    }

    // 开启音乐服务
    private void startMusicService() {
        startService(new Intent(this, MusicService.class));
    }

    private void stopMusicService() {
        stopService(new Intent(this, MusicService.class));
    }

    private void sendFeedbackEmail() {
        Intent intent = new Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:19857198709@163.com"));
        intent.putExtra(Intent.EXTRA_SUBJECT, "Poco Feedback");
        try {
            startActivity(Intent.createChooser(intent, "发送邮件"));
        } catch (android.content.ActivityNotFoundException ex) {
            Toast.makeText(this, "未找到邮件客户端", Toast.LENGTH_SHORT).show();
        }
    }

    private void showLogoutDialog() {
        new AlertDialog.Builder(this)
                .setTitle("登出")
                .setMessage("确定要退出当前账号吗？")
                .setPositiveButton("确定", (dialog, which) -> performLogout())
                .setNegativeButton("取消", null)
                .show();
    }

    private void performLogout() {
        getSharedPreferences("UserPrefs", MODE_PRIVATE).edit().clear().apply();
        Intent intent = new Intent(this, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void openUrl(String url) {
        try {
            startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
        } catch (Exception e) {
            Toast.makeText(this, "无法打开链接", Toast.LENGTH_SHORT).show();
        }
    }
}
