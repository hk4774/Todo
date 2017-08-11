package com.huk.todo.helper;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;

import com.huk.todo.R;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskRenameCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListRenameCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.database.TodoListDatabaseHelper;

/**
 * Created by User on 8/9/2017.
 */

public class Utilities {

    private static Utilities mInstance;

    public static Utilities getInstance() {
        if (mInstance == null) {
            mInstance = new Utilities();
        }
        return mInstance;
    }

    public void showRenameDialog(final Context mContext, CharSequence todoTitleText, final int todoId, final TodoListRenameCallback todoListRenameCallback) {
        LayoutInflater li = LayoutInflater.from(mContext);
        View dialogView = li.inflate(R.layout.todo_rename, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Rename Todo");
        builder.setCancelable(true);
        builder.setView(dialogView);
        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.et_input);
        userInput.setText(todoTitleText.toString());
        userInput.post(new Runnable() {
            @Override
            public void run() {
                userInput.setSelection(userInput.length());
                userInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TodoListDatabaseHelper todoListDatabaseHelper = new TodoListDatabaseHelper(mContext);
                todoListDatabaseHelper.renameTodo(todoId, userInput.getText().toString(), todoListRenameCallback);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void showRenameDialog(final Context mContext, CharSequence taskTitleText, final int taskId, final TaskRenameCallback taskRenameCallback) {
        LayoutInflater li = LayoutInflater.from(mContext);
        View dialogView = li.inflate(R.layout.todo_rename, null);
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Rename Todo");
        builder.setCancelable(true);
        builder.setView(dialogView);
        final EditText userInput = (EditText) dialogView
                .findViewById(R.id.et_input);
        userInput.setText(taskTitleText.toString());
        userInput.post(new Runnable() {
            @Override
            public void run() {
                userInput.setSelection(userInput.length());
                userInput.requestFocus();
                InputMethodManager imm = (InputMethodManager) mContext.getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, InputMethodManager.HIDE_IMPLICIT_ONLY);
            }
        });
        builder.setPositiveButton("Set", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TaskDBHelper taskDBHelper = new TaskDBHelper(mContext);
                taskDBHelper.renameTodo(taskId, userInput.getText().toString(), taskRenameCallback);
                dialog.dismiss();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}
