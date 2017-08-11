package com.huk.todo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huk.todo.callbacks.todolistcallbacks.TodoListCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListDeleteCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListMarkCompletedCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListRenameCallback;
import com.huk.todo.model.Todos;
import com.huk.todo.network.NetworkUtils;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

/**
 * Created by User on 8/9/2017.
 */

public class TodoListDatabaseHelper {


    private Context mContext;
    private SQLiteDatabase mTodoDatabase;
    private TodoDatabaseHelper mDBHelper;

    public TodoListDatabaseHelper(Context context) {
        mContext = context;
        mDBHelper = new TodoDatabaseHelper(context);
    }


    public long createTodo(String todoName) {

        if (null == todoName || todoName.isEmpty()) {
            return -1;
        }

        mTodoDatabase = mDBHelper.getWritableDatabase();

        ContentValues todoToInsert = new ContentValues();
        todoToInsert.put(TodoDatabaseHelper.TODO_NAME, todoName);
        todoToInsert.put(TodoDatabaseHelper.TODO_DONE, false);
        todoToInsert.put(TodoDatabaseHelper.TODO_PUBLISH_DATE, getCurrentTimeStamp());
        todoToInsert.put(TodoDatabaseHelper.TODO_ISSYNCED, false);

        long insertId = mTodoDatabase.insert(TodoDatabaseHelper.TABLE_TODO, null, todoToInsert);
        if (!NetworkUtils.getInstance(mContext).isNetworkAvailable())
            mTodoDatabase.insert(TodoDatabaseHelper.TABLE_TODO_TEMP, null, todoToInsert);

        mTodoDatabase.close();
        mTodoDatabase = null;

        return insertId;
    }

    public void getTodoList(List<Todos> mTodos, TodoListCallback todoListCallback) {

        if (null == mTodos) {
            return;
        }

        mTodoDatabase = mDBHelper.getReadableDatabase();

        String[] columnsToSearch = {
                TodoDatabaseHelper.TODO_COLUMN_ID,
                TodoDatabaseHelper.TODO_NAME,
                TodoDatabaseHelper.TODO_PUBLISH_DATE,
                TodoDatabaseHelper.TODO_DONE
        };

        Cursor resultSet = mTodoDatabase.query(TodoDatabaseHelper.TABLE_TODO,
                columnsToSearch, null, null, null, null, TodoDatabaseHelper.TODO_PUBLISH_DATE + " DESC");

        if (resultSet.moveToFirst()) {
            do {
                Todos todos = new Todos();
                todos.setItemText(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_NAME)));
                todos.setItemID(resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_COLUMN_ID)));
                todos.setPub_date(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_PUBLISH_DATE)));
                todos.setDone(0 != resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_DONE)));
                mTodos.add(todos);
            } while (resultSet.moveToNext());
        }

        todoListCallback.onDataFetched();

        resultSet.close();

        mTodoDatabase.close();
        mTodoDatabase = null;
    }

    public void getTodoListForSync(List<Todos> mTodos, TodoListCallback todoListCallback) {

        if (null == mTodos) {
            return;
        }

        mTodoDatabase = mDBHelper.getReadableDatabase();

        String query = "Select " + TodoDatabaseHelper.TODO_COLUMN_ID + "," + TodoDatabaseHelper.TODO_NAME + "," + TodoDatabaseHelper.TODO_PUBLISH_DATE + "," + TodoDatabaseHelper.TODO_DONE + " WHERE " + TodoDatabaseHelper.TODO_ISSYNCED + " = 0";
        Cursor resultSet = mTodoDatabase.rawQuery(query, null);

        if (resultSet.moveToFirst()) {
            do {
                Todos todos = new Todos();
                todos.setItemText(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_NAME)));
                todos.setItemID(resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_COLUMN_ID)));
                todos.setPub_date(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_PUBLISH_DATE)));
                todos.setDone(0 != resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_DONE)));
                mTodos.add(todos);
            } while (resultSet.moveToNext());
        }

        resultSet.close();

        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListCallback.onDataFetched();

    }

    public void markTodoAsCompleted(int itemId, boolean completed, TodoListMarkCompletedCallback todoListMarkCompletedCallback) {

        mTodoDatabase = mDBHelper.getWritableDatabase();

        if (completed) {
            new TaskDBHelper(mContext).markAllItemsForTodo(itemId);
        }

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(TodoDatabaseHelper.TODO_DONE, completed ? 1 : 0);
        dataToUpdate.put(TodoDatabaseHelper.TODO_ISSYNCED, false);

        mTodoDatabase.update(
                TodoDatabaseHelper.TABLE_TODO,
                dataToUpdate,
                TodoDatabaseHelper.TODO_COLUMN_ID + "=?",
                new String[]{Integer.toString(itemId)}
        );

        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListMarkCompletedCallback.onTodoMarkCompleted();
    }

    public void renameTodo(int todoId, String newTodoName, TodoListRenameCallback todoListRenameCallback) {

        mTodoDatabase = mDBHelper.getWritableDatabase();

        ContentValues updateTodoListRow = new ContentValues();
        updateTodoListRow.put(TodoDatabaseHelper.TODO_NAME, newTodoName);
        updateTodoListRow.put(TodoDatabaseHelper.TODO_ISSYNCED, false);

        int affectedRows = mTodoDatabase.update(TodoDatabaseHelper.TABLE_TODO,
                updateTodoListRow,
                TodoDatabaseHelper.TODO_COLUMN_ID + "=?",
                new String[]{Integer.toString(todoId)});
        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListRenameCallback.onTodoRenamed();
    }

    public void removeTodo(int todoId, TodoListDeleteCallback todoListDeleteCallback) {

        mTodoDatabase = mDBHelper.getWritableDatabase();

        new TaskDBHelper(mContext).deleteAllItemsForTodo(
                todoId
        );

        mTodoDatabase.delete(TodoDatabaseHelper.TABLE_TODO,
                TodoDatabaseHelper.TODO_COLUMN_ID + " =?", new String[]{Integer.toString(todoId)});
        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListDeleteCallback.onTodoDeleted();
    }

    public void getchNotSyncRecord(List<Todos> mTodos, TodoListCallback todoListCallback) {

        if (null == mTodos) {
            return;
        }

        mTodoDatabase = mDBHelper.getReadableDatabase();

        String[] columnsToSearch = {
                TodoDatabaseHelper.TODO_COLUMN_ID,
                TodoDatabaseHelper.TODO_NAME,
                TodoDatabaseHelper.TODO_PUBLISH_DATE,
                TodoDatabaseHelper.TODO_DONE
        };

        Cursor resultSet = mTodoDatabase.query(TodoDatabaseHelper.TABLE_TODO_TEMP,
                columnsToSearch, TodoDatabaseHelper.TODO_ISSYNCED + " = ", new String[]{"0"}, null, null, TodoDatabaseHelper.TODO_PUBLISH_DATE + " DESC");

        if (resultSet.moveToFirst()) {
            do {
                Todos todos = new Todos();
                todos.setItemText(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_NAME)));
                todos.setItemID(resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_COLUMN_ID)));
                todos.setPub_date(resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_PUBLISH_DATE)));
                todos.setDone(0 != resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_DONE)));
                mTodos.add(todos);
            } while (resultSet.moveToNext());
        }

        resultSet.close();

        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListCallback.onDataFetched();
    }

    public void removeTodo(Todos todo, TodoListDeleteCallback todoListDeleteCallback) {

        mTodoDatabase = mDBHelper.getWritableDatabase();

        mTodoDatabase.delete(TodoDatabaseHelper.TABLE_TODO_TEMP,
                TodoDatabaseHelper.TODO_COLUMN_ID + " =?", new String[]{Integer.toString(todo.getItemID())});
        mTodoDatabase.close();
        mTodoDatabase = null;
        todoListDeleteCallback.onTodoDeleted();
    }

    public String getCurrentTimeStamp() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS").format(new Date());
    }


}
