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
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TaskAdapter extends RecyclerView.Adapter<TaskAdapter.TaskViewHolder> {

    private List<Task> taskList;
    private OnTaskActionListener listener;

    public interface OnTaskActionListener {
        void onTaskDelete(Task task);
        void onTaskStatusChanged(Task task);
        void onTaskClick(Task task);
    }

    public TaskAdapter(List<Task> taskList, OnTaskActionListener listener) {
        this.taskList = taskList;
        this.listener = listener;
    }

    public void updateList(List<Task> newList) {
        this.taskList = newList;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_task, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.tvTitle.setText(task.getTitle());

        if (task.getDateTime() != null && !task.getDateTime().isEmpty()) {
            holder.tvDate.setText(task.getDateTime());
            holder.tvDate.setVisibility(View.VISIBLE);
        } else {
            holder.tvDate.setVisibility(View.GONE);
        }

        holder.checkBox.setOnCheckedChangeListener(null);
        holder.checkBox.setChecked(task.isCompleted());

        if (task.isCompleted()) {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            holder.tvTitle.setTextColor(Color.parseColor("#AAAAAA"));
            holder.tvDate.setTextColor(Color.parseColor("#AAAAAA"));
        }
        else {
            holder.tvTitle.setPaintFlags(holder.tvTitle.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG)); // 取消删除线

            int titleColor = Color.parseColor("#000000");
            int dateColor = Color.parseColor("#888888");

            if (task.getDateTime() != null && !task.getDateTime().isEmpty()) {
                try {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
                    Date date = sdf.parse(task.getDateTime());

                    if (date != null) {
                        long diff = date.getTime() - System.currentTimeMillis();
                        long diffMinutes = diff / (60 * 1000);

                        if (diff < 0) {
                            titleColor = Color.RED;
                            dateColor = Color.RED;
                        } else if (diffMinutes < 3) {
                            titleColor = Color.parseColor("#D84315");
                            dateColor = Color.parseColor("#D84315");
                        } else if (diffMinutes < 10) {
                            titleColor = Color.parseColor("#795548");
                            dateColor = Color.parseColor("#795548");
                        } else if (diffMinutes < 30) {
                            titleColor = Color.parseColor("#FF9800");
                            dateColor = Color.parseColor("#FF9800");
                        } else if (diffMinutes < 60) {
                            titleColor = Color.parseColor("#FFCC80");
                            dateColor = Color.parseColor("#FFCC80");
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            holder.tvTitle.setTextColor(titleColor);
            holder.tvDate.setTextColor(dateColor);
        }

        holder.checkBox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            task.setCompleted(isChecked);
            listener.onTaskStatusChanged(task);
        });

        holder.ivDelete.setOnClickListener(v -> listener.onTaskDelete(task));
        holder.itemView.setOnClickListener(v -> listener.onTaskClick(task));
    }

    @Override
    public int getItemCount() {
        return taskList.size();
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
}
