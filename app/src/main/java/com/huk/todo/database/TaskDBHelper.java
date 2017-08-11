package com.huk.todo.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import com.huk.todo.callbacks.todoitemlistcallbacks.TaskDeleteCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskListCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskMarkCompletedCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskRenameCallback;
import com.huk.todo.model.Tasks;
import com.huk.todo.network.NetworkUtils;

import java.util.ArrayList;

/**
 * Created by soumya on 5/19/17.
 */

public class TaskDBHelper {

    private final Context mContext;
    private SQLiteDatabase mTaskDatabase;
    private TodoDatabaseHelper mDatabaseHelper;

    public TaskDBHelper(Context context) {
        this.mContext = context;
        mDatabaseHelper = new TodoDatabaseHelper(context);
    }

    public void getItemsListForTodo(int todoId, ArrayList<Tasks> itemsList, TaskListCallback taskListCallback) {

        if (null == itemsList) {
            return;
        }

        mTaskDatabase = mDatabaseHelper.getReadableDatabase();

        String[] searchColumns = {
                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID,
                TodoDatabaseHelper.TODO_ITEMS_NAME,
                TodoDatabaseHelper.TODO_ITEMS_DONE
        };

        Cursor resultSet = mTaskDatabase.query(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                searchColumns, TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=?",
                new String[]{Integer.toString(todoId)}, null, null, null, null);

        if (resultSet.moveToFirst()) {

            do {

                itemsList.add(new Tasks(
                        resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_ITEM_ID)),
                        resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_NAME)),
                        0 != resultSet.getInt(resultSet.getColumnIndex(
                                TodoDatabaseHelper.TODO_DONE))
                ));

            } while (resultSet.moveToNext());
        }

        resultSet.close();

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskListCallback.onDataFetched();
    }

    public void getAllTasks(ArrayList<Tasks> itemsList, TaskListCallback taskListCallback) {

        if (null == itemsList) {
            return;
        }

        mTaskDatabase = mDatabaseHelper.getReadableDatabase();

        String[] searchColumns = {
                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID,
                TodoDatabaseHelper.TODO_ITEMS_NAME,
                TodoDatabaseHelper.TODO_ITEMS_DONE
        };

        Cursor resultSet = mTaskDatabase.query(
                TodoDatabaseHelper.TABLE_TODO_ITEMS_temp,
                searchColumns, null,
                null, null, null, null, null);

        if (resultSet.moveToFirst()) {

            do {

                itemsList.add(new Tasks(
                        resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_ITEM_ID)),
                        resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_NAME)),
                        0 != resultSet.getInt(resultSet.getColumnIndex(
                                TodoDatabaseHelper.TODO_DONE)), resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_PARENT_ID))));

            } while (resultSet.moveToNext());
        }

        resultSet.close();

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskListCallback.onDataFetched();
    }

    public void getTaskListForSync(int todoId, ArrayList<Tasks> itemsList, TaskListCallback taskListCallback) {

        if (null == itemsList) {
            return;
        }

        mTaskDatabase = mDatabaseHelper.getReadableDatabase();

        String[] searchColumns = {
                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID,
                TodoDatabaseHelper.TODO_ITEMS_NAME,
                TodoDatabaseHelper.TODO_ITEMS_DONE
        };

        Cursor resultSet = mTaskDatabase.query(
                TodoDatabaseHelper.TABLE_TODO_ITEMS_temp,
                searchColumns, TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=?",
                new String[]{Integer.toString(todoId)}, null, null, null, null);

        if (resultSet.moveToFirst()) {

            do {

                itemsList.add(new Tasks(
                        resultSet.getInt(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_ITEM_ID)),
                        resultSet.getString(resultSet.getColumnIndex(TodoDatabaseHelper.TODO_ITEMS_NAME)),
                        0 != resultSet.getInt(resultSet.getColumnIndex(
                                TodoDatabaseHelper.TODO_DONE))
                ));

            } while (resultSet.moveToNext());
        }

        resultSet.close();
        resultSet = null;

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskListCallback.onDataFetched();
    }


    public long createItemForTodo(int todoId, String itemName) {

        if (null == itemName || itemName.isEmpty()) {
            return -1;
        }

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues dataToInsert = new ContentValues();
        dataToInsert.put(TodoDatabaseHelper.TODO_ITEMS_PARENT_ID, todoId);
        dataToInsert.put(TodoDatabaseHelper.TODO_ITEMS_NAME, itemName);
        dataToInsert.put(TodoDatabaseHelper.TODO_ITEMS_DONE, 0);

        long insertId = mTaskDatabase.insert(TodoDatabaseHelper.TABLE_TODO_ITEMS, null, dataToInsert);
        if (!NetworkUtils.getInstance(mContext).isNetworkAvailable())
            mTaskDatabase.insert(TodoDatabaseHelper.TABLE_TODO_ITEMS_temp, null, dataToInsert);

        mTaskDatabase.close();
        mTaskDatabase = null;

        return insertId;
    }

//    public boolean checkIfItemCompleted(int itemId) {
//
//        mTaskDatabase = mDatabaseHelper.getReadableDatabase();
//
//        String[] searchColumns = {
//                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID,
//                TodoDatabaseHelper.TODO_ITEMS_DONE
//        };
//
//        Cursor resultSet = mTaskDatabase.query(TodoDatabaseHelper.TABLE_TODO_ITEMS,
//                searchColumns, TodoDatabaseHelper.TODO_ITEMS_ITEM_ID + "=?",
//                new String[]{Integer.toString(itemId)}, null, null, null);
//
//        boolean isCompleted = false;
//
//        if (resultSet.moveToFirst()) {
//
//            isCompleted = 0 != resultSet.getInt(resultSet.getColumnIndex(
//                    TodoDatabaseHelper.TODO_ITEMS_DONE));
//        }
//
//        resultSet.close();
//        resultSet = null;
//
//        mTaskDatabase.close();
//        mTaskDatabase = null;
//
//        return isCompleted;
//    }

    public void markItemAsCompleted(int itemId, boolean completed, TaskMarkCompletedCallback taskMarkCompletedCallback) {

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(TodoDatabaseHelper.TODO_ITEMS_DONE, completed ? 1 : 0);
        dataToUpdate.put(TodoDatabaseHelper.TODO_ITEMS_ISSYNCED, false);

        mTaskDatabase.update(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                dataToUpdate,
                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID + "=?",
                new String[]{Integer.toString(itemId)}
        );

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskMarkCompletedCallback.onTaskMarkCompleted();
    }

    public void renameTodo(int todoId, String newTodoName, TaskRenameCallback taskRenameCallback) {

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues updateTodoListRow = new ContentValues();
        updateTodoListRow.put(TodoDatabaseHelper.TODO_ITEMS_NAME, newTodoName);
        updateTodoListRow.put(TodoDatabaseHelper.TODO_ITEMS_ISSYNCED, false);

        int affectedRows = mTaskDatabase.update(TodoDatabaseHelper.TABLE_TODO_ITEMS,
                updateTodoListRow,
                TodoDatabaseHelper.TODO_ITEMS_ITEM_ID + "=?",
                new String[]{Integer.toString(todoId)});
        mTaskDatabase.close();
        mTaskDatabase = null;
        taskRenameCallback.onTaskRenamed();
    }

    public void deleteAllItemsForTodo(int todoId) {

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        mTaskDatabase.delete(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=?",
                new String[]{Integer.toString(todoId)}
        );

        mTaskDatabase.close();
        mTaskDatabase = null;
    }

    public void deleteItemForTodo(int todoId, int itemId, TaskDeleteCallback taskDeleteCallback) {

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        mTaskDatabase.delete(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=? and " +
                        TodoDatabaseHelper.TODO_ITEMS_ITEM_ID + "=?",
                new String[]{Integer.toString(todoId), Integer.toString(itemId)}
        );

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskDeleteCallback.onTaskDeleted();
    }

    public void deleteItemForTodo(int todoId, Tasks task, TaskDeleteCallback taskDeleteCallback) {

        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        mTaskDatabase.delete(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=? and " +
                        TodoDatabaseHelper.TODO_ITEMS_ITEM_ID + "=?",
                new String[]{Integer.toString(todoId), Integer.toString(task.getItemID())}
        );

        mTaskDatabase.close();
        mTaskDatabase = null;
        taskDeleteCallback.onTaskDeleted();
    }

    public void markAllItemsForTodo(int itemId) {
        mTaskDatabase = mDatabaseHelper.getWritableDatabase();

        ContentValues dataToUpdate = new ContentValues();
        dataToUpdate.put(TodoDatabaseHelper.TODO_DONE, 1);

        mTaskDatabase.update(
                TodoDatabaseHelper.TABLE_TODO_ITEMS,
                dataToUpdate,
                TodoDatabaseHelper.TODO_ITEMS_PARENT_ID + "=?",
                new String[]{Integer.toString(itemId)}
        );

        mTaskDatabase.close();
        mTaskDatabase = null;
    }
}
