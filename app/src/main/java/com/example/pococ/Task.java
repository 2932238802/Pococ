package com.example.pococ;

public class Task {
    private int id;
    private String title;
    private boolean isCompleted;
    private String dateTime;
    private int taskType;

    private long orderIndex;
    public Task(int id, String title, boolean isCompleted, String dateTime, int taskType) {
        this.id = id;
        this.title = title;
        this.isCompleted = isCompleted;
        this.dateTime = dateTime;
        this.taskType = taskType;
    }

    public Task(String title, boolean isCompleted, String dateTime) {
        this.title = title;
        this.isCompleted = isCompleted;
        this.dateTime = dateTime;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public boolean isCompleted() { return isCompleted; }
    public void setCompleted(boolean completed) { isCompleted = completed; }

    public String getDateTime() { return dateTime; }
    public void setDateTime(String dateTime) { this.dateTime = dateTime; }

    public int getTaskType() { return taskType; }
    public void setTaskType(int taskType) { this.taskType = taskType; }

    // === 新增 Getter/Setter ===
    public long getOrderIndex() { return orderIndex; }
    public void setOrderIndex(long orderIndex) { this.orderIndex = orderIndex; }
}