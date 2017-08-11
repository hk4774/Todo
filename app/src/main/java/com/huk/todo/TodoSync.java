package com.huk.todo;

import android.content.Context;
import android.util.Log;

import com.huk.todo.callbacks.todoitemlistcallbacks.TaskCreatedCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskDeleteCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskListCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCreateCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListDeleteCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListUpdateCallback;
import com.huk.todo.database.TaskDBHelper;
import com.huk.todo.database.TodoListDatabaseHelper;
import com.huk.todo.helper.Constants;
import com.huk.todo.helper.SharedPrefsUtils;
import com.huk.todo.model.Tasks;
import com.huk.todo.model.Todos;
import com.huk.todo.network.NetworkUtils;
import com.huk.todo.network.synclib.TimeSync;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by User on 8/11/2017.
 */

public class TodoSync extends TimeSync {
    @Override
    protected void onCreate(Context context) {
        super.onCreate(context);
    }

    @Override
    public void onSync(final Context context) throws Exception {
        if (SharedPrefsUtils.getBooleanPreference(context, Constants.IS_LOGIN, false)) {
            final List<Todos> todosList = new ArrayList<>();
            final TodoListDatabaseHelper todoListDatabaseHelper = new TodoListDatabaseHelper(context);
            todoListDatabaseHelper.getTodoListForSync(todosList, new TodoListCallback() {
                @Override
                public void onDataFetched() {
                    if (todosList.size() > 0) {
                        for (final Todos todo : todosList) {
                            NetworkUtils.getInstance().doCreateTodo(todo.getItemText(), new TodoListCreateCallback() {
                                @Override
                                public void onTodoCreated() {
                                    Log.d("todo", " created");
                                    todoListDatabaseHelper.removeTodo(todo, new TodoListDeleteCallback() {
                                        @Override
                                        public void onTodoDeleted() {
                                            Log.d("todo", " removed");
                                        }
                                    });
                                    final List<Tasks> tasksList = new ArrayList<>();
                                    final TaskDBHelper taskDBHelper = new TaskDBHelper(context);
                                    taskDBHelper.getTaskListForSync(todo.getItemID(), (ArrayList<Tasks>) tasksList, new TaskListCallback() {
                                        @Override
                                        public void onDataFetched() {
                                            if (tasksList.size() > 0) {
                                                for (Tasks task : tasksList) {
                                                    NetworkUtils.getInstance().doCreateTask(todo.getItemID(), task.getItemText(), new TaskCreatedCallback() {
                                                        @Override
                                                        public void onTaskCreated() {
                                                            Log.d("task", "created");
                                                        }

                                                        @Override
                                                        public void onTaskCreated(int mParentId, String s) {

                                                        }
                                                    });
                                                    taskDBHelper.deleteItemForTodo(todo.getItemID(), task, new TaskDeleteCallback() {
                                                        @Override
                                                        public void onTaskDeleted() {
                                                            Log.d("task", "deleted");
                                                        }
                                                    });
                                                }
                                            }
                                        }
                                    });
                                }

                                @Override
                                public void onTodoCreated(String todoName) {

                                }
                            });
                        }
                    }
                }
            });

            todosList.clear();
            todoListDatabaseHelper.getTodoListForSync(todosList, new TodoListCallback() {
                @Override
                public void onDataFetched() {
                    if (todosList.size() > 0) {
                        for (final Todos todo : todosList) {
                            NetworkUtils.getInstance().doUpdateTodo(todo.getItemID(), todo, new TodoListUpdateCallback() {
                                @Override
                                public void onTodoUpdate() {
                                    Log.d("todo", "updated");
                                }
                            });
                        }
                    }
                }
            });
        }
    }
}
