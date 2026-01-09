package com.example.pococ;

import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.animation.AlphaAnimation;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class PocoListActivity extends AppCompatActivity {

    private TextView tvDate;
    private ImageView settingBtn;
    // === 新增：删除按钮 ===
    private ImageView btnDeleteCompleted;

    private TextView titleTextView;
    private TextView tvQuote;
    private TabLayout tabLayout;
    private ViewPager2 viewPager;
    private ViewPagerAdapter viewPagerAdapter;
    private DatabaseHelper dbHelper;

    private final String[] TAB_TITLES = {"日计划", "月计划", "季计划", "年计划"};

    private Handler quoteHandler = new Handler(Looper.getMainLooper());
    private Runnable quoteRunnable;
    private int currentQuoteIndex = 0;

    private final String[] quotes = {
            "曾经的承诺,如今的废纸", "凡有活物，皆为臣属", "爱,是何等的渺茫与空荡", "目之所及,皆为尘土",
            "yxy,你可曾看见,可曾后悔?", "你的背叛，痴如狂潮", "谎言,从一开始就下了,对么?",
            "曾经的点滴,你可曾留恋?", "你的背叛与绝情,将在今日彻底算清", "我的付出,终成笑话",
            "我早已心如槁木", "黑雾由我滚滚而出，宛如大潮", "虽然偶有衰落，但如今，我们将汹涌无前",
            "爱，是何等的渺茫又脆弱啊?", "她是唯一的星光，看顾着我的长路。自她离去，前方只剩黑暗",
            "曾经的爱也是真的,如今你的决绝亦没有假", "如果能重来,你还会选择这么做么?",
            "如果能重来,你能告诉我真话么?", "如果能重来,我选择当初就没有见过你",
            "若是世界将我的至美无情剥夺，毁灭便是它理所应当的结局", "卡玛维亚早已归于尘土。就在那片废墟之中，你我的王座堂皇不朽",
            "恨我咒我，世人请便。一切终将破溃，直到她重归我的怀抱", "你的决绝如果是真的,难道1324张照片都是假的么",
            "我的执拗,今天的陌路人", "这滔天的血海,你可曾看见", "她在黑雾尽头，亭亭而待",
            "我终将再次与她相遇", "任何代价，一概不论", "永失吾爱，举目破败",
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_task_list);

        dbHelper = new DatabaseHelper(this);

        tvDate = findViewById(R.id.tvDate);
        settingBtn = findViewById(R.id.btnSettings);
        // === 新增：绑定视图 ===
        btnDeleteCompleted = findViewById(R.id.btnDeleteCompleted);

        titleTextView = findViewById(R.id.tvTitle);
        tvQuote = findViewById(R.id.tvQuote);
        FloatingActionButton fabAdd = findViewById(R.id.fabAddTask);
        tabLayout = findViewById(R.id.tabLayout);
        viewPager = findViewById(R.id.viewPager);

        settingBtn.setOnClickListener(v -> {
            Intent intent = new Intent(PocoListActivity.this, SettingsActivity.class);
            startActivity(intent);
        });

        if (btnDeleteCompleted != null) {
            btnDeleteCompleted.setOnClickListener(v -> showDeleteConfirmDialog());
        }

        fabAdd.setOnClickListener(v -> showAddTaskDialog());

        setupHeader();
        setupTabs();

        if (tvQuote != null) {
            startQuoteRotation();
        }

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.TIRAMISU) {
            if (androidx.core.content.ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
                    != android.content.pm.PackageManager.PERMISSION_GRANTED) {
                androidx.core.app.ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.POST_NOTIFICATIONS}, 101);
            }
        }
    }

    private void showDeleteConfirmDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("都给我滚~");
        builder.setMessage("确定要删除所有【已完成】的任务吗？此操作不可恢复哦~");

        builder.setPositiveButton("清理", (dialog, which) -> {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            String currentUser = prefs.getString("current_user", "Guest");

            dbHelper.deleteCompletedTasks(currentUser);

            int currentItem = viewPager.getCurrentItem();
            viewPager.setAdapter(viewPagerAdapter);
            viewPager.setCurrentItem(currentItem, false);

            Toast.makeText(this, "已清理所有已完成任务", Toast.LENGTH_SHORT).show();
        });

        builder.setNegativeButton("取消", null);
        showStyledDialog(builder);
    }


    private void setupHeader() {
        String user = getIntent().getStringExtra("USER_NAME");
        if (user == null) {
            SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
            user = prefs.getString("current_user", "My");
        }
        titleTextView.setText(user + "  's Tasks");

        String currentDate = new SimpleDateFormat("MMM dd, yyyy", Locale.US).format(new Date());
        tvDate.setText(currentDate.toUpperCase());
    }

    private void setupTabs() {
        viewPagerAdapter = new ViewPagerAdapter(this);
        viewPager.setAdapter(viewPagerAdapter);

        new TabLayoutMediator(tabLayout, viewPager, (tab, position) ->
                tab.setText(TAB_TITLES[position])
        ).attach();
    }

    private class ViewPagerAdapter extends FragmentStateAdapter {
        public ViewPagerAdapter(@NonNull androidx.fragment.app.FragmentActivity fragmentActivity) {
            super(fragmentActivity);
        }

        @NonNull
        @Override
        public Fragment createFragment(int position) {
            return TaskFragment.newInstance(position);
        }

        @Override
        public int getItemCount() {
            return TAB_TITLES.length;
        }
    }

    private void showAddTaskDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        int currentTab = viewPager.getCurrentItem();
        String typeName = TAB_TITLES[currentTab];
        builder.setTitle("新增 " + typeName);

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etTitle = new EditText(this);
        etTitle.setHint("要干啥 伙计?");
        layout.addView(etTitle);

        final TextView tvTimePick = new TextView(this);
        tvTimePick.setText("ddl  🕑 (点击设置)");
        tvTimePick.setPadding(0, 30, 0, 20);
        tvTimePick.setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
        layout.addView(tvTimePick);

        final StringBuilder finalDateTime = new StringBuilder();

        tvTimePick.setOnClickListener(v -> pickDateTime(tvTimePick, finalDateTime));

        builder.setView(layout);

        builder.setPositiveButton("就这个了", (dialog, which) -> {
            String title = etTitle.getText().toString().trim();
            String dateTime = finalDateTime.toString();

            if (!title.isEmpty()) {
                Task newTask = new Task(title, false, dateTime);
                newTask.setTaskType(currentTab);

                SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
                String currentUser = prefs.getString("current_user", "Guest");
                dbHelper.addTask(newTask, currentUser);

                viewPager.setAdapter(viewPagerAdapter);
                viewPager.setCurrentItem(currentTab, false);

                if (!dateTime.isEmpty()) {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    try {
                        Date date = sdf.parse(dateTime);
                        if (date != null && date.getTime() > System.currentTimeMillis()) {
                            scheduleNotification(title, date.getTime());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        builder.setNegativeButton("算鸟算鸟", (dialog, which) -> dialog.cancel());
        showStyledDialog(builder);
    }

    private void pickDateTime(TextView displayView, StringBuilder outputString) {
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(this, (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(this, (timeView, hourOfDay, minute) -> {

                String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                String result = dateStr + " " + timeStr;

                displayView.setText(result);
                displayView.setTextColor(ContextCompat.getColor(this, android.R.color.black));

                outputString.setLength(0);
                outputString.append(result);

            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStyledDialog(AlertDialog.Builder builder) {
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(this, android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(this, android.R.color.darker_gray));
    }

    private void scheduleNotification(String title, long deadlineInMillis) {
        SharedPreferences prefs = getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        int offsetMinutes = prefs.getInt("reminder_offset", 0);
        long offsetMillis = offsetMinutes * 60 * 1000L;

        long triggerTime = deadlineInMillis - offsetMillis;
        AlarmManager alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (!alarmManager.canScheduleExactAlarms()) {
                Toast.makeText(this, "请授予“闹钟和提醒”权限以接收通知", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);
                intent.setData(Uri.parse("package:" + getPackageName()));
                startActivity(intent);
                return;
            }
        }

        if (triggerTime > System.currentTimeMillis()) {
            Intent intent = new Intent(this, AlarmReceiver.class);
            if (offsetMinutes > 0) {
                intent.putExtra("TASK_TITLE", title + " (还有 " + offsetMinutes + " 分钟)");
            } else {
                intent.putExtra("TASK_TITLE", title);
            }

            int requestCode = (int) System.currentTimeMillis();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(
                    this,
                    requestCode,
                    intent,
                    PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT
            );

            if (alarmManager != null) {
                try {
                    alarmManager.setExact(AlarmManager.RTC_WAKEUP, triggerTime, pendingIntent);
                    String toastMsg = "提醒已设置";
                    if (offsetMinutes > 0) {
                        toastMsg += " (将提前 " + offsetMinutes + " 分钟通知)";
                    }
                    Toast.makeText(this, toastMsg, Toast.LENGTH_SHORT).show();
                } catch (SecurityException e) {
                    Toast.makeText(this, "权限不足，无法设置提醒", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void startQuoteRotation() {
        quoteRunnable = new Runnable() {
            @Override
            public void run() {
                updateQuoteWithAnimation();
                quoteHandler.postDelayed(this, 4000);
            }
        };
        quoteHandler.post(quoteRunnable);
    }

    private void updateQuoteWithAnimation() {
        AlphaAnimation fadeOut = new AlphaAnimation(1.0f, 0.0f);
        fadeOut.setDuration(500);
        fadeOut.setFillAfter(true);

        AlphaAnimation fadeIn = new AlphaAnimation(0.0f, 1.0f);
        fadeIn.setDuration(500);
        fadeIn.setFillAfter(true);

        tvQuote.startAnimation(fadeOut);

        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            currentQuoteIndex = (int) (Math.random() * quotes.length);
            tvQuote.setText(quotes[currentQuoteIndex]);
            tvQuote.startAnimation(fadeIn);
        }, 500);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (quoteHandler != null && quoteRunnable != null) {
            quoteHandler.removeCallbacks(quoteRunnable);
        }
    }
}
