package com.huk.todo.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by soumya on 5/18/17.
 */

public class TodoDatabaseHelper extends SQLiteOpenHelper {

    public static final String TABLE_TODO = "todos";
    public static final String TABLE_TODO_TEMP = "todos_temp";
    public static final String TODO_COLUMN_ID = "_id";
    public static final String TODO_NAME = "title";
    public static final String TODO_DONE = "done";
    public static final String TODO_PUBLISH_DATE = "pub_date";
    public static final String TODO_ISSYNCED = "is_synced";

    public static final String TABLE_TODO_ITEMS = "todos_items";
    public static final String TABLE_TODO_ITEMS_temp = "todos_items_temp";
    public static final String TODO_ITEMS_PARENT_ID = "parent_id";
    public static final String TODO_ITEMS_ITEM_ID = "todo_items_id";
    public static final String TODO_ITEMS_NAME = "text";
    public static final String TODO_ITEMS_DONE = "done";
    public static final String TODO_ITEMS_ISSYNCED = "is_synced";

    private static final String DATABASE_NAME = "todo.db";
    private static final int DATABASE_VERSION = 1;

    public TodoDatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }



    @Override
    public void onCreate(SQLiteDatabase db) {
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        //
    }

    private void createTables(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_TODO + " (" +
                TODO_COLUMN_ID + " integer primary key autoincrement, " +
                TODO_NAME + " varchar(255), " +
                TODO_DONE + " BOOLEAN, " +
                TODO_PUBLISH_DATE + " DATETIME, " +
                TODO_ISSYNCED + " BOOLEAN, " +
                "CHECK (" + TODO_DONE + " IN (0, 1)), CHECK (" + TODO_ISSYNCED + " IN (0, 1)));"
        );

        db.execSQL("CREATE TABLE " + TABLE_TODO_ITEMS + " (" +
                TODO_ITEMS_ITEM_ID + " integer primary key autoincrement, " +
                TODO_ITEMS_PARENT_ID + " integer, " +
                TODO_ITEMS_NAME + " varchar(2048), " +
                TODO_ITEMS_DONE + " BOOLEAN, " +
                TODO_ITEMS_ISSYNCED + " BOOLEAN, " +
                "CHECK (" + TODO_ITEMS_DONE + " IN (0, 1)), CHECK (" + TODO_ITEMS_ISSYNCED + " IN (0, 1)));"
        );

        db.execSQL("CREATE TABLE " + TABLE_TODO_TEMP + " (" +
                TODO_COLUMN_ID + " integer primary key autoincrement, " +
                TODO_NAME + " varchar(255), " +
                TODO_DONE + " BOOLEAN, " +
                TODO_PUBLISH_DATE + " DATETIME, " +
                TODO_ISSYNCED + " BOOLEAN, " +
                "CHECK (" + TODO_DONE + " IN (0, 1)), CHECK (" + TODO_ISSYNCED + " IN (0, 1)));"
        );

        db.execSQL("CREATE TABLE " + TABLE_TODO_ITEMS_temp + " (" +
                TODO_ITEMS_ITEM_ID + " integer primary key autoincrement, " +
                TODO_ITEMS_PARENT_ID + " integer, " +
                TODO_ITEMS_NAME + " varchar(2048), " +
                TODO_ITEMS_DONE + " BOOLEAN, " +
                TODO_ITEMS_ISSYNCED + " BOOLEAN, " +
                "CHECK (" + TODO_ITEMS_DONE + " IN (0, 1)), CHECK (" + TODO_ITEMS_ISSYNCED + " IN (0, 1)));"
        );

    }
}
