package com.example.pococ;

import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat; // 导入这个用于获取颜色
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {

    private static final int TYPE_TASK = 0;
    private static final int TYPE_HEADER = 1;

    private List<Object> displayList;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskDelete(Task task);
        void onTaskStatusChanged(Task task);
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Object> displayList, OnTaskActionListener listener) {
        this.displayList = displayList;
        this.listener = listener;
    }

    public void updateList(List<Object> newList) {
        this.displayList = newList;
        notifyDataSetChanged();
    }

    public List<Object> getDisplayList() {
        return displayList;
    }

    public List<Task> getData() {
        ArrayList<Task> tasks = new ArrayList<>();
        for (Object obj : displayList) {
            if (obj instanceof Task) {
                tasks.add((Task) obj);
            }
        }
        return tasks;
    }

    public void onItemMove(int fromPosition, int toPosition) {
        if (displayList.get(fromPosition) instanceof Task && displayList.get(toPosition) instanceof Task) {
            if (fromPosition < toPosition) {
                for (int i = fromPosition; i < toPosition; i++) {
                    Collections.swap(displayList, i, i + 1);
                }
            } else {
                for (int i = fromPosition; i > toPosition; i--) {
                    Collections.swap(displayList, i, i - 1);
                }
            }
            notifyItemMoved(fromPosition, toPosition);
        }
    }

    @Override
    public int getItemViewType(int position) {
        if (displayList.get(position) instanceof String) {
            return TYPE_HEADER;
        } else {
            return TYPE_TASK;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_HEADER) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(32, 48, 32, 16);
            tv.setTextSize(18);
            tv.setTypeface(null, android.graphics.Typeface.BOLD);
            tv.setLayoutParams(new ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT));
            return new HeaderViewHolder(tv);
        } else {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
            return new TaskViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (holder instanceof HeaderViewHolder) {
            String title = (String) displayList.get(position);
            ((HeaderViewHolder) holder).tvHeader.setText(title);
        } else if (holder instanceof TaskViewHolder) {
            TaskViewHolder taskHolder = (TaskViewHolder) holder;
            Task task = (Task) displayList.get(position);

            taskHolder.tvTitle.setText(task.getTitle());

            if (task.getDateTime() != null && !task.getDateTime().isEmpty()) {
                if (task.getTaskType() == 1) { // 月计划简化日期显示
                    try {
                        String rawDate = task.getDateTime();
                        if (rawDate.length() >= 10) {
                            taskHolder.tvDate.setText(rawDate.substring(5, 10));
                        } else {
                            taskHolder.tvDate.setText(rawDate);
                        }
                    } catch (Exception e) {
                        taskHolder.tvDate.setText(task.getDateTime());
                    }
                } else {
                    taskHolder.tvDate.setText(task.getDateTime());
                }
                taskHolder.tvDate.setVisibility(View.VISIBLE);
            } else {
                taskHolder.tvDate.setVisibility(View.GONE);
            }

            taskHolder.checkBox.setOnCheckedChangeListener(null);
            taskHolder.checkBox.setChecked(task.isCompleted());

            if (task.isCompleted()) {
                taskHolder.tvTitle.setPaintFlags(taskHolder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
                taskHolder.tvTitle.setAlpha(0.5f);
            } else {
                taskHolder.tvTitle.setPaintFlags(taskHolder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
                taskHolder.tvTitle.setAlpha(1.0f);

                if (task.getTaskType() == 0 && task.getDateTime() != null && !task.getDateTime().isEmpty()) {
                    try {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                        Date date = sdf.parse(task.getDateTime());
                        if (date != null) {
                            long diff = date.getTime() - System.currentTimeMillis();
                            long diffMinutes = diff / (60 * 1000);

                            if (diff < 0) {
                                taskHolder.tvTitle.setTextColor(Color.RED);
                            } else if (diffMinutes < 30) {
                                taskHolder.tvTitle.setTextColor(Color.parseColor("#FF9800")); // 橙色
                            } else {
                               taskHolder.tvTitle.setTextColor(ContextCompat.getColor(taskHolder.itemView.getContext(), android.R.color.tab_indicator_text));
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

            }

            taskHolder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                task.setCompleted(isChecked);
                listener.onTaskStatusChanged(task);
            });

            taskHolder.ivDelete.setOnClickListener(v -> listener.onTaskDelete(task));
            taskHolder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
        }
    }

    @Override
    public int getItemCount() {
        return displayList.size();
    }

    static class TaskViewHolder extends RecyclerView.ViewHolder {
        TextView tvTitle;
        CheckBox checkBox;
        ImageView ivDelete;
        TextView tvDate;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTaskTitle);
            checkBox = itemView.findViewById(R.id.cbTaskStatus);
            ivDelete = itemView.findViewById(R.id.ivDeleteTask);
            tvDate = itemView.findViewById(R.id.tvTaskDate);
        }
    }

    static class HeaderViewHolder extends RecyclerView.ViewHolder {
        TextView tvHeader;

        public HeaderViewHolder(@NonNull View itemView) {
            super(itemView);
            tvHeader = (TextView) itemView;
        }
    }
}
