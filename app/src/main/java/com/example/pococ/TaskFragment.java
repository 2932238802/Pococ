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
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.ItemTouchHelper;
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
    private List<Object> displayList = new ArrayList<>();

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
        View view = inflater.inflate(R.layout.fragment_task_list, container, false);

        recyclerView = view.findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        adapter = new TaskAdapter(displayList, new TaskAdapter.OnTaskActionListener() {
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

        ItemTouchHelper itemTouchHelper = new ItemTouchHelper(new ItemTouchHelper.Callback() {
            @Override
            public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                if (viewHolder.getItemViewType() == 1) {
                    return makeMovementFlags(0, 0);
                }
                int dragFlags = ItemTouchHelper.UP | ItemTouchHelper.DOWN;
                return makeMovementFlags(dragFlags, 0);
            }

            @Override
            public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder target) {
                int fromPos = viewHolder.getAdapterPosition();
                int toPos = target.getAdapterPosition();

                if (viewHolder.getItemViewType() != 0 || target.getItemViewType() != 0) {
                    return false;
                }

                if (taskType == 2) {
                    int headerPosFrom = findHeaderPosition(fromPos);
                    int headerPosTo = findHeaderPosition(toPos);
                    if (headerPosFrom != headerPosTo || headerPosFrom == -1) {
                        return false;
                    }
                }

                adapter.onItemMove(fromPos, toPos);
                return true;
            }

            @Override
            public void onSwiped(@NonNull RecyclerView.ViewHolder viewHolder, int direction) {
            }

            @Override
            public void clearView(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
                super.clearView(recyclerView, viewHolder);
                saveNewOrderToDB();
            }
        });
        itemTouchHelper.attachToRecyclerView(recyclerView);

        return view;
    }

    private int findHeaderPosition(int currentPos) {
        List<Object> list = adapter.getDisplayList();
        for (int i = currentPos; i >= 0; i--) {
            if (list.get(i) instanceof String) {
                return i;
            }
        }
        return -1;
    }

    private void saveNewOrderToDB() {
        List<Task> tasksToUpdate = new ArrayList<>();
        for (Object obj : adapter.getDisplayList()) {
            if (obj instanceof Task) {
                tasksToUpdate.add((Task) obj);
            }
        }
        for (int i = 0; i < tasksToUpdate.size(); i++) {
            Task t = tasksToUpdate.get(i);
            dbHelper.updateTaskOrder(t.getId(), i);
        }
    }

    private void pickDateOnly(TextView displayView, StringBuilder outputString) {
        if (getContext() == null) return;
        Calendar c = Calendar.getInstance();
        new DatePickerDialog(getContext(), (view, year, month, dayOfMonth) -> {
            String result = String.format(Locale.getDefault(), "%d-%02d-%02d", year, (month + 1), dayOfMonth);
            displayView.setText(result);
            // 移除硬编码颜色设置
            outputString.setLength(0);
            outputString.append(result);
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
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
        List<Task> rawTasks = dbHelper.getTasksByTypeAndUser(taskType, currentUser);
        displayList.clear();

        if (taskType == 2) {
            List<Task> spring = new ArrayList<>();
            List<Task> summer = new ArrayList<>();
            List<Task> autumn = new ArrayList<>();
            List<Task> winter = new ArrayList<>();
            List<Task> others = new ArrayList<>();

            for (Task t : rawTasks) {
                String title = t.getTitle();
                if (title.startsWith("[春季]")) spring.add(t);
                else if (title.startsWith("[夏季]")) summer.add(t);
                else if (title.startsWith("[秋季]")) autumn.add(t);
                else if (title.startsWith("[冬季]")) winter.add(t);
                else others.add(t);
            }

            if (!spring.isEmpty()) {
                displayList.add("SPRING (Mar - May)");
                displayList.addAll(spring);
            }
            if (!summer.isEmpty()) {
                displayList.add("SUMMER (Jun - Aug)");
                displayList.addAll(summer);
            }
            if (!autumn.isEmpty()) {
                displayList.add("AUTUMN (Sep - Nov)");
                displayList.addAll(autumn);
            }
            if (!winter.isEmpty()) {
                displayList.add("WINTER (Dec - Feb)");
                displayList.addAll(winter);
            }
            if (!others.isEmpty()) {
                displayList.add("OTHERS");
                displayList.addAll(others);
            }
        } else {
            if (taskType == 0) {
                Collections.sort(rawTasks, (t1, t2) -> {
                    boolean b1 = t1.isCompleted();
                    boolean b2 = t2.isCompleted();
                    if (b1 != b2) return b1 ? 1 : -1;
                    String d1 = t1.getDateTime() == null ? "" : t1.getDateTime();
                    String d2 = t2.getDateTime() == null ? "" : t2.getDateTime();
                    return d1.compareTo(d2);
                });
            }
            displayList.addAll(rawTasks);
        }

        if (adapter != null) {
            adapter.updateList(displayList);
        }
    }

    private void showEditTaskDialog(Task task) {
        if (getContext() == null) return;
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());

        String[] typeNames = {"日计划", "月计划", "季计划", "年计划"};
        String prefix = (taskType >= 0 && taskType < typeNames.length) ? typeNames[taskType] : "计划";
        builder.setTitle("修改 " + prefix);

        LinearLayout layout = new LinearLayout(getContext());
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(60, 40, 60, 20);

        final EditText etTitle = new EditText(getContext());
        etTitle.setText(task.getTitle());
        etTitle.setSelection(task.getTitle().length());
        layout.addView(etTitle);

        final StringBuilder finalDateTime = new StringBuilder();
        if (task.getDateTime() != null) finalDateTime.append(task.getDateTime());

        if (taskType == 0 || taskType == 1) {
            final TextView tvTimePick = new TextView(getContext());
            String currentDisplayTime = (task.getDateTime() != null && !task.getDateTime().isEmpty())
                    ? task.getDateTime()
                    : (taskType == 0 ? "重设 ddl 🕑" : "重设日期 📅");
            tvTimePick.setText(currentDisplayTime);
            tvTimePick.setPadding(0, 30, 0, 20);

            // 移除手动颜色设置，让它默认显示（夜间白色，日间黑色）
            layout.addView(tvTimePick);

            tvTimePick.setOnClickListener(v -> {
                if (taskType == 0) {
                    pickDateTime(tvTimePick, finalDateTime);
                } else {
                    pickDateOnly(tvTimePick, finalDateTime);
                }
            });
        }

        builder.setView(layout);

        builder.setPositiveButton("保存o 0", (dialog, which) -> {
            String newTitle = etTitle.getText().toString().trim();
            if (!newTitle.isEmpty()) {
                task.setTitle(newTitle);
                if (taskType == 0 || taskType == 1) {
                    task.setDateTime(finalDateTime.toString());
                } else {
                    task.setDateTime("");
                }
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
                .setMessage("准备好迎接新的开始了吗？")
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
                // 移除硬编码颜色设置
                outputString.setLength(0);
                outputString.append(result);
            }, c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true).show();
        }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show();
    }

    private void showStyledDialog(AlertDialog.Builder builder) {
        if (getContext() == null) return;
        AlertDialog dialog = builder.create();
        dialog.show();
        // 移除强制设置按钮颜色的代码，让主题自动处理
    }
}
