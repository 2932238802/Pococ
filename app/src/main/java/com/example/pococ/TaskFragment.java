package com.example.pococ;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class TaskFragment extends Fragment {

    private static final String ARG_TASK_TYPE = "task_type";
    private int taskType;

    private RecyclerView recyclerView;
    private TaskAdapter adapter;
    private DatabaseHelper dbHelper;
    private List<Task> taskList = new ArrayList<>();

    // 工厂方法创建 Fragment
    public static TaskFragment newInstance(int type) {
        TaskFragment fragment = new TaskFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_TASK_TYPE, type);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            taskType = getArguments().getInt(ARG_TASK_TYPE);
        }
        dbHelper = new DatabaseHelper(getContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // 注意：这里复用了之前的 fragment 布局，假设你的 fragment_task_list.xml 里只有一个 RecyclerView，ID为 recyclerViewFragment
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerViewFragment);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(taskList, new TaskAdapter.OnTaskActionListener() {
            @Override
            public void onTaskDelete(Task task) {
                showDeleteConfirmDialog(task);
            }

            @Override
            public void onTaskStatusChanged(Task task) {
                dbHelper.updateTask(task);
                loadTasks();
            }

            @Override
            public void onTaskClick(Task task) {
                showEditTaskDialog(task);
            }
        });
        recyclerView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        loadTasks();
    }

    public void loadTasks() {
        if (getContext() == null) return;

        SharedPreferences prefs = getContext().getSharedPreferences("UserPrefs", Context.MODE_PRIVATE);
        String currentUser = prefs.getString("current_user", "Guest");
        taskList = dbHelper.getTasksByTypeAndUser(taskType, currentUser);
        Collections.sort(taskList, (t1, t2) -> {
            boolean b1 = t1.isCompleted();
            boolean b2 = t2.isCompleted();
            if (b1 != b2) return b1 ? 1 : -1;
            String d1 = t1.getDateTime() == null ? "" : t1.getDateTime();
            String d2 = t2.getDateTime() == null ? "" : t2.getDateTime();
            return d1.compareTo(d2);
        });

        if (adapter != null) {
            adapter.updateList(taskList);
        }
    }

    private void showEditTaskDialog(Task task) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("修改计划");

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etTitle = new EditText(getContext());
        etTitle.setText(task.getTitle());
        etTitle.setSelection(task.getTitle().length());
        layout.addView(etTitle);

        final TextView tvTimePick = new TextView(getContext());
        String currentDisplayTime = (task.getDateTime() != null && !task.getDateTime().isEmpty())
                ? task.getDateTime()
                : "重设 ddl 🕑";
        tvTimePick.setText(currentDisplayTime);
        tvTimePick.setPadding(0, 30, 0, 20);
        int colorId = (task.getDateTime() != null && !task.getDateTime().isEmpty())
                ? android.R.color.black
                : android.R.color.darker_gray;
        tvTimePick.setTextColor(ContextCompat.getColor(getContext(), colorId));

        layout.addView(tvTimePick);

        final StringBuilder finalDateTime = new StringBuilder();
        if(task.getDateTime() != null) finalDateTime.append(task.getDateTime());

        tvTimePick.setOnClickListener(v -> pickDateTime(tvTimePick, finalDateTime));

        builder.setView(layout);

        builder.setPositiveButton("保存o 0", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                task.setTitle(newTitle);
                task.setDateTime(finalDateTime.toString());
                dbHelper.updateTask(task);
                loadTasks();
            }
        });

        builder.setNegativeButton("取消0 o", null);
        showStyledDialog(builder);
    }

    private void showDeleteConfirmDialog(Task task) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext())
                .setTitle("算了 删了你吧")
                .setMessage("真的删了么,那这些日子'我在你身边'又算什么呢?")
                .setPositiveButton("DELETE", (dialog, which) -> {
                    dbHelper.deleteTask(task.getId());
                    loadTasks();
                })
                .setNegativeButton("算鸟算鸟", null);

        showStyledDialog(builder);
    }

    private void pickDateTime(TextView displayView, StringBuilder outputString) {
        if (getContext() == null) return;
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            new TimePickerDialog(getContext(), (timeView, hourOfDay, minute) -> {
                String dateStr = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
                String timeStr = String.format(Locale.getDefault(), "%02d:%02d", hourOfDay, minute);
                String result = dateStr + " " + timeStr;

                displayView.setText(result);
                displayView.setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));

                outputString.setLength(0);
                outputString.append(result);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStyledDialog(AlertDialog.Builder builder) {
        if (getContext() == null) return;
        AlertDialog dialog = builder.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setTextColor(ContextCompat.getColor(getContext(), android.R.color.black));
        dialog.getButton(AlertDialog.BUTTON_NEGATIVE).setTextColor(ContextCompat.getColor(getContext(), android.R.color.darker_gray));
    }
}
