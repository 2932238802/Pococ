package com.example.pococ;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import java.util.ArrayList;
import java.util.List;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "LosAngelous.db";
    private static final int DATABASE_VERSION = 6;

    private static final String TABLE_USERS = "users";
    private static final String COLUMN_ID = "id";
    private static final String COLUMN_USERNAME = "username";
    private static final String COLUMN_PASSWORD = "password";

    // 任务表
    private static final String TABLE_TASKS = "tasks";
    private static final String COL_TASK_ID = "id";
    private static final String COL_TASK_TITLE = "title";
    private static final String COL_TASK_IS_DONE = "is_done";
    private static final String COL_TASK_DUE_DATE = "due_date";
    private static final String COL_TASK_TYPE = "task_type";
    private static final String COL_TASK_USER = "user_name";
    private static final String COL_TASK_ORDER = "order_index";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String createUserTable = "CREATE TABLE " + TABLE_USERS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_USERNAME + " TEXT, " +
                COLUMN_PASSWORD + " TEXT)";
        db.execSQL(createUserTable);

        String createTaskTable = "CREATE TABLE " + TABLE_TASKS + " (" +
                COL_TASK_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COL_TASK_TITLE + " TEXT, " +
                COL_TASK_IS_DONE + " INTEGER, " +
                COL_TASK_DUE_DATE + " TEXT, " +
                COL_TASK_TYPE + " INTEGER DEFAULT 0, " +
                COL_TASK_USER + " TEXT, " +
                COL_TASK_ORDER + " INTEGER DEFAULT 0)";
        db.execSQL(createTaskTable);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_TASKS);
        onCreate(db);
    }

    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_USERNAME, username);
        values.put(COLUMN_PASSWORD, password);
        long result = db.insert(TABLE_USERS, null, values);
        return result != -1;
    }

    public boolean checkUserExist(String username) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ?", new String[]{username});
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    public boolean checkUserLogin(String username, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COLUMN_USERNAME + " = ? AND " + COLUMN_PASSWORD + " = ?", new String[]{username, password});
        boolean result = cursor.getCount() > 0;
        cursor.close();
        return result;
    }

    public void addTask(Task task, String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_IS_DONE, task.isCompleted() ? 1 : 0);
        values.put(COL_TASK_DUE_DATE, task.getDateTime());
        values.put(COL_TASK_TYPE, task.getTaskType());
        values.put(COL_TASK_USER, username);

        values.put(COL_TASK_ORDER, System.currentTimeMillis());

        db.insert(TABLE_TASKS, null, values);
    }

    public List<Task> getTasksByTypeAndUser(int type, String username) {
        List<Task> taskList = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        String query = "SELECT * FROM " + TABLE_TASKS +
                " WHERE " + COL_TASK_TYPE + " = ? AND " + COL_TASK_USER + " = ?" +
                " ORDER BY " + COL_TASK_ORDER + " ASC"; // <--- 修改：按自定义顺序排序

        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(type), username});

        if (cursor.moveToFirst()) {
            do {
                int idIndex = cursor.getColumnIndex(COL_TASK_ID);
                int titleIndex = cursor.getColumnIndex(COL_TASK_TITLE);
                int isDoneIndex = cursor.getColumnIndex(COL_TASK_IS_DONE);
                int dateIndex = cursor.getColumnIndex(COL_TASK_DUE_DATE);
                int typeIndex = cursor.getColumnIndex(COL_TASK_TYPE);
                int orderIndex = cursor.getColumnIndex(COL_TASK_ORDER); // <--- 新增

                if (idIndex != -1 && titleIndex != -1) {
                    int id = cursor.getInt(idIndex);
                    String title = cursor.getString(titleIndex);
                    int isDoneInt = cursor.getInt(isDoneIndex);
                    String dueDate = cursor.getString(dateIndex);
                    int taskType = (typeIndex != -1) ? cursor.getInt(typeIndex) : 0;
                    long order = (orderIndex != -1) ? cursor.getLong(orderIndex) : 0; // <--- 新增

                    Task t = new Task(id, title, isDoneInt == 1, dueDate, taskType);
                    taskList.add(t);
                }
            } while (cursor.moveToNext());
        }
        cursor.close();
        return taskList;
    }

    public void updateTaskOrder(int taskId, long newOrder) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_ORDER, newOrder);
        db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public List<Task> getAllTasks() {
        return new ArrayList<>();
    }

    public void updateTask(Task task) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_TASK_TITLE, task.getTitle());
        values.put(COL_TASK_IS_DONE, task.isCompleted() ? 1 : 0);
        values.put(COL_TASK_DUE_DATE, task.getDateTime());
        values.put(COL_TASK_TYPE, task.getTaskType());
        db.update(TABLE_TASKS, values, COL_TASK_ID + " = ?", new String[]{String.valueOf(task.getId())});
    }

    public void deleteTask(int taskId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS, COL_TASK_ID + " = ?", new String[]{String.valueOf(taskId)});
    }

    public void deleteCompletedTasks(String username) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_TASKS,
                COL_TASK_IS_DONE + " = ? AND " + COL_TASK_USER + " = ?",
                new String[]{"1", username});
        db.close();
    }
}
