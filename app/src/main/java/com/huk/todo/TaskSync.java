package com.huk.todo;

import android.content.Context;
import android.util.Log;

import com.huk.todo.callbacks.todoitemlistcallbacks.TaskListCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskUpdateCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.helper.Constants;
import com.huk.todo.helper.SharedPrefsUtils;
import com.huk.todo.model.Tasks;
import com.huk.todo.network.NetworkUtils;
import com.huk.todo.network.synclib.TimeSync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 8/11/2017.
 */

public class TaskSync extends TimeSync {

    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
    }

    @Override
    public void onSync(final Context context) throws Exception {
        if (SharedPrefsUtils.getBooleanPreference(context, Constants.IS_LOGIN, false)) {

            final List<Tasks> tasksList = new ArrayList<>();
            final List<Tasks> taskToSync = new ArrayList<>();
            final TaskDBHelper taskDBHelper = new TaskDBHelper(context);
            taskDBHelper.getAllTasks((ArrayList<Tasks>) tasksList, new TaskListCallback() {
                @Override
                public void onDataFetched() {
                    for (Tasks task : tasksList) {
                        taskToSync.clear();
                        taskDBHelper.getTaskListForSync(task.getParentId(), (ArrayList<Tasks>) taskToSync, new TaskListCallback() {
                                    @Override
                                    public void onDataFetched() {
                                        if (taskToSync.size() > 0) {
                                            for (final Tasks task : taskToSync) {
                                                NetworkUtils.getInstance().doUpdateTask(task.getParentId(), task, new TaskUpdateCallback() {
                                                    @Override
                                                    public void onTaskUpdate() {
                                                        Log.d("task", "updated");
                                                    }
                                                });
                                            }
                                        }
                                    }
                                }
                        );
                    }
                }
            });
        }
    }
}
