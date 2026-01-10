package com.example.pococ;

import android.annotation.SuppressLint;
import android.app.DownloadManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
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

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class SettingsActivity extends BaseActivity {

    private static final String TAG = "UpdateDebug"; // 专门用于调试更新功能的 TAG
    private static final int REQUEST_INSTALL_PERMISSION = 1001;

    private SwitchCompat switchMusic;
    private SwitchCompat switchNightMode;
    private SwitchCompat switchQuote;

    private BroadcastReceiver downloadReceiver;
    private String pendingApkUrl;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        initViews();
        Log.d(TAG, "onCreate: SettingsActivity 启动");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (downloadReceiver != null) {
            try {
                unregisterReceiver(downloadReceiver);
                Log.d(TAG, "onDestroy: 广播接收器已注销");
            } catch (Exception e) {
                Log.e(TAG, "onDestroy: 注销广播接收器失败", e);
            }
            downloadReceiver = null;
        }
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
        Log.d(TAG, "checkUpdate: 开始检查更新");

        String url = "https://gitee.com/lsjwillwin/Pococ/raw/main/version.json";

        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder().url(url).build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "checkUpdate: 网络请求失败", e);
                runOnUiThread(() ->
                        Toast.makeText(SettingsActivity.this, "检查更新失败，请稍后再试", Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                if (response.isSuccessful() && response.body() != null) {
                    String json = response.body().string();
                    Log.d(TAG, "checkUpdate: 获取到版本信息: " + json);
                    try {
                        VersionInfo versionInfo = new Gson().fromJson(json, VersionInfo.class);
                        int currentVersionCode = getCurrentVersionCode();
                        Log.d(TAG, "当前版本: " + currentVersionCode + ", 远程版本: " + versionInfo.getVersionCode());

                        if (versionInfo != null && versionInfo.getVersionCode() > currentVersionCode) {
                            runOnUiThread(() -> showUpdateDialog(versionInfo));
                        } else {
                            runOnUiThread(() ->
                                    Toast.makeText(SettingsActivity.this, "已是最新版本", Toast.LENGTH_SHORT).show()
                            );
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "checkUpdate: JSON 解析失败", e);
                        runOnUiThread(() ->
                                Toast.makeText(SettingsActivity.this, "解析版本信息失败", Toast.LENGTH_SHORT).show()
                        );
                    }
                } else {
                    Log.e(TAG, "checkUpdate: 服务器响应错误, code=" + response.code());
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
        Log.d(TAG, "showUpdateDialog: 显示更新弹窗");
        new AlertDialog.Builder(this)
                .setTitle("发现新版本: " + versionInfo.getVersionName())
                .setMessage(versionInfo.getUpdateLog())
                .setPositiveButton("立即更新", (dialog, which) -> {
                    Log.d(TAG, "用户点击立即更新");
                    downloadAndInstallApk(versionInfo.getApkUrl());
                })
                .setNegativeButton("稍后", null)
                .setCancelable(false)
                .show();
    }

    private void downloadAndInstallApk(String url) {
        Log.d(TAG, "downloadAndInstallApk: 准备下载 APK, URL=" + url);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                Log.d(TAG, "没有安装权限，请求权限");
                pendingApkUrl = url;
                new AlertDialog.Builder(this)
                        .setTitle("安装权限")
                        .setMessage("需要您开启“安装未知应用”权限，才能更新 App")
                        .setPositiveButton("去设置", (dialog, which) -> {
                            Intent intent = new Intent(
                                    Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                                    Uri.parse("package:" + getPackageName())
                            );
                            startActivityForResult(intent, REQUEST_INSTALL_PERMISSION);
                        })
                        .setNegativeButton("取消", (dialog, which) -> pendingApkUrl = null)
                        .setCancelable(false)
                        .show();
                return;
            }
        }

        startDownload(url);
    }

    private void startDownload(String url) {
        Log.d(TAG, "startDownload: 开始执行下载逻辑");
        Toast.makeText(this, "开始后台下载...", Toast.LENGTH_SHORT).show();

        DownloadManager downloadManager = (DownloadManager) getSystemService(DOWNLOAD_SERVICE);
        if (downloadManager == null) {
            Log.e(TAG, "startDownload: DownloadManager 服务不可用");
            Toast.makeText(this, "下载服务不可用", Toast.LENGTH_SHORT).show();
            return;
        }

        File downloadDir = new File(getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), "updates");
        if (!downloadDir.exists()) {
            boolean created = downloadDir.mkdirs();
            Log.d(TAG, "startDownload: 创建下载目录 " + (created ? "成功" : "失败"));
        }

        File apkFile = new File(downloadDir, "PocoList_update.apk");
        Log.d(TAG, "startDownload: 下载目标路径: " + apkFile.getAbsolutePath());

        if (apkFile.exists()) {
            boolean deleted = apkFile.delete();
            Log.d(TAG, "startDownload: 删除旧文件 " + (deleted ? "成功" : "失败"));
        }

        try {
            DownloadManager.Request request = new DownloadManager.Request(Uri.parse(url));
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
            request.setTitle("Poco List 更新");
            request.setDescription("正在下载新版本...");
            request.setDestinationUri(Uri.fromFile(apkFile));
            request.setAllowedNetworkTypes(DownloadManager.Request.NETWORK_WIFI | DownloadManager.Request.NETWORK_MOBILE);
            request.setAllowedOverMetered(true);
            request.setAllowedOverRoaming(true);

            long downloadId = downloadManager.enqueue(request);
            Log.d(TAG, "startDownload: 下载任务已入队, ID=" + downloadId);

            registerDownloadReceiver(downloadId, apkFile);
        } catch (Exception e) {
            Log.e(TAG, "startDownload: 创建下载请求失败", e);
            Toast.makeText(this, "创建下载任务失败", Toast.LENGTH_SHORT).show();
        }
    }

    @SuppressLint("UnspecifiedRegisterReceiverFlag")
    private void registerDownloadReceiver(long downloadId, File apkFile) {
        if (downloadReceiver != null) {
            try {
                unregisterReceiver(downloadReceiver);
            } catch (Exception e) {
                // ignore
            }
        }

        downloadReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                long id = intent.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1);
                Log.d(TAG, "onReceive: 收到广播, ID=" + id + ", 目标ID=" + downloadId);
                if (id == downloadId) {
                    checkDownloadStatus(context, downloadId, apkFile);
                }
            }
        };

        IntentFilter filter = new IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            registerReceiver(downloadReceiver, filter, Context.RECEIVER_EXPORTED);
            Log.d(TAG, "registerDownloadReceiver: 注册 Receiver (Android 13+)");
        } else {
            registerReceiver(downloadReceiver, filter);
            Log.d(TAG, "registerDownloadReceiver: 注册 Receiver (旧版本)");
        }
    }

    private void checkDownloadStatus(Context context, long downloadId, File apkFile) {
        Log.d(TAG, "checkDownloadStatus: 检查下载状态");
        DownloadManager downloadManager = (DownloadManager) context.getSystemService(DOWNLOAD_SERVICE);
        DownloadManager.Query query = new DownloadManager.Query();
        query.setFilterById(downloadId);

        try (Cursor cursor = downloadManager.query(query)) {
            if (cursor != null && cursor.moveToFirst()) {
                int statusIndex = cursor.getColumnIndex(DownloadManager.COLUMN_STATUS);
                int reasonIndex = cursor.getColumnIndex(DownloadManager.COLUMN_REASON);

                int status = cursor.getInt(statusIndex);
                int reason = cursor.getInt(reasonIndex);

                Log.d(TAG, "checkDownloadStatus: status=" + status + ", reason=" + reason);

                if (status == DownloadManager.STATUS_SUCCESSFUL) {
                    Log.d(TAG, "checkDownloadStatus: 下载成功，准备安装");
                    Toast.makeText(context, "下载成功，准备安装...", Toast.LENGTH_SHORT).show();
                    installApk(context, apkFile);

                    // 成功后注销
                    try {
                        unregisterReceiver(downloadReceiver);
                        downloadReceiver = null;
                        Log.d(TAG, "checkDownloadStatus: Receiver 已注销");
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else if (status == DownloadManager.STATUS_FAILED) {
                    Log.e(TAG, "checkDownloadStatus: 下载失败，原因码: " + reason);
                    Toast.makeText(context, "下载失败，错误码: " + reason, Toast.LENGTH_LONG).show();
                } else {
                    Log.d(TAG, "checkDownloadStatus: 下载仍在进行中或暂停");
                }
            } else {
                Log.e(TAG, "checkDownloadStatus: Cursor 为空或无法移动");
            }
        } catch (Exception e) {
            Log.e(TAG, "checkDownloadStatus: 查询失败", e);
        }
    }

    private void installApk(Context context, File apkFile) {
        if (!apkFile.exists()) {
            Log.e(TAG, "installApk: 文件不存在: " + apkFile.getAbsolutePath());
            Toast.makeText(context, "安装包不存在", Toast.LENGTH_SHORT).show();
            return;
        }

        try {
            Log.d(TAG, "installApk: 开始构建 Intent");
            Intent installIntent = new Intent(Intent.ACTION_VIEW);
            installIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            installIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // ！！！这里的 authority 必须和 AndroidManifest.xml 中的 provider authorities 一致！！！
                String authority = getPackageName() + ".provider";
                Log.d(TAG, "installApk: FileProvider Authority = " + authority);

                Uri apkUri = FileProvider.getUriForFile(context, authority, apkFile);
                Log.d(TAG, "installApk: FileProvider URI = " + apkUri.toString());

                installIntent.setDataAndType(apkUri, "application/vnd.android.package-archive");
            } else {
                installIntent.setDataAndType(Uri.fromFile(apkFile), "application/vnd.android.package-archive");
            }

            Log.d(TAG, "installApk: 启动安装 Intent");
            context.startActivity(installIntent);
        } catch (Exception e) {
            Log.e(TAG, "installApk: 启动安装失败", e);
            Toast.makeText(context, "无法启动安装程序: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_INSTALL_PERMISSION) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                if (getPackageManager().canRequestPackageInstalls()) {
                    Log.d(TAG, "onActivityResult: 权限已授予");
                    if (pendingApkUrl != null) {
                        startDownload(pendingApkUrl);
                        pendingApkUrl = null;
                    }
                } else {
                    Log.d(TAG, "onActivityResult: 权限未授予");
                    Toast.makeText(this, "未授予安装权限，无法更新", Toast.LENGTH_SHORT).show();
                    pendingApkUrl = null;
                }
            }
        }
    }

    // ... 其他无关方法保持不变 ...
    // 版本信息类
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
                        appPrefs.edit().putFloat("font_scale", selectedScale).apply();
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

            Toast.makeText(this, "缓存已清理完毕 (" + formattedSize + ")", Toast.LENGTH_SHORT).show();
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
        return String.format(java.util.Locale.getDefault(), "%.2f %s",
                size / Math.pow(1024, digitGroups), units[digitGroups]);
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
