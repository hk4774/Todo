package com.huk.todo.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

import com.androidnetworking.AndroidNetworking;
import com.androidnetworking.common.Priority;
import com.androidnetworking.error.ANError;
import com.androidnetworking.interfaces.JSONObjectRequestListener;
import com.huk.todo.callbacks.LoginCallback;
import com.huk.todo.callbacks.LogoutCallback;
import com.huk.todo.callbacks.SignupCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskCreatedCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskDeleteCallback;
import com.huk.todo.callbacks.todoitemlistcallbacks.TaskUpdateCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListCreateCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListDeleteCallback;
import com.huk.todo.callbacks.todolistcallbacks.TodoListUpdateCallback;
import com.huk.todo.helper.Constants;
import com.huk.todo.model.Tasks;
import com.huk.todo.model.Todos;

import org.json.JSONException;
import org.json.JSONObject;

import static com.huk.todo.helper.Constants.CREATE_TODO_URL;

/**
 * Created by User on 8/10/2017.
 */

public class NetworkUtils {

    private static final String TAG = NetworkUtils.class.getSimpleName();
    private static NetworkUtils mInstance;
    private static Context mContext;

    public static NetworkUtils getInstance() {
        if (mInstance == null) {
            synchronized (NetworkUtils.class) {
                mInstance = new NetworkUtils();
            }
        }
        return mInstance;
    }

    public static NetworkUtils getInstance(Context context) {
        if (mInstance == null) {
            synchronized (NetworkUtils.class) {
                mInstance = new NetworkUtils();
                mContext = context;
            }
        }
        return mInstance;
    }

    public boolean isNetworkAvailable() {
        if (mContext == null)
            return false;
        ConnectivityManager cm =
                (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    public void doLogin(String userName, String password, final LoginCallback loginCallback) {
        AndroidNetworking.post(Constants.LOGIN_URL)
                .addHeaders("username", userName)
                .addHeaders("password", password)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getInt("status") == 1) {
                                loginCallback.onDoLogin();
                            } else {
                                loginCallback.onLoginFailed(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            loginCallback.onLoginFailed();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        loginCallback.onLoginFailed();
                    }
                });
    }

    public void doLogout(final LogoutCallback logoutCallback) {
        AndroidNetworking.post(Constants.LOGUT_URL)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, response.toString());
                        try {
                            if (response.getInt("status") == 1 || response.getInt("status") == 2) {
                                logoutCallback.onLogoutComplete();
                            } else {
                                logoutCallback.onLogoutFailed(response.getString("message"));
                            }
                        } catch (JSONException e) {
                            logoutCallback.onLogoutFailed();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        logoutCallback.onLogoutFailed();
                        Log.d("Login", anError.getErrorBody());
                    }
                });
    }

    public void doSignup(String userName, String password, final SignupCallback signupCallback) {
        AndroidNetworking.post(Constants.SIGN_UP_URL)
                .addHeaders("username", userName)
                .addHeaders("password", password)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                signupCallback.onDoSignup();
                            } else {
                                Log.d("signup", response.toString());
                                signupCallback.onSignupFailed("Username already exists");
                            }
                        } catch (JSONException e) {
                            signupCallback.onSignupFailed();
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        signupCallback.onSignupFailed();
                        Log.d("signup", anError.getErrorBody());
                    }
                });
    }

    public void doCreateTodo(String title, final TodoListCreateCallback todoListCreateCallback) {

        AndroidNetworking.post(CREATE_TODO_URL)
                .addHeaders("title", title)
                .setPriority(Priority.MEDIUM)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d("todo_response", response.toString());
                        try {
                            if (response.getInt("status") == 1) {
                                todoListCreateCallback.onTodoCreated();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("todo_response", anError.getResponse().toString());
                    }
                });
    }

    public void doCreateTask(int parentId, String taskName, final TaskCreatedCallback taskCreatedCallback) {
        AndroidNetworking.post(Constants.CREATE_TASK_URL + "{todo_id}")
                .addPathParameter("todo_id", String.valueOf(parentId))
                .addHeaders("text", taskName)
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                taskCreatedCallback.onTaskCreated();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                        Log.d("Networkutils", anError.getResponse().toString());

                    }
                });
    }

    public void doDeleteTodo(final int todoId, final TodoListDeleteCallback todoListDeleteCallback) {
        AndroidNetworking.post(Constants.DELETE_TODO_URL + "{todo_id}")
                .addPathParameter("todo_id", String.valueOf(todoId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                todoListDeleteCallback.onTodoDeleted();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }

    public void doDeleteTask(final int taskId, final TaskDeleteCallback taskDeleteCallback) {
        AndroidNetworking.post(Constants.UPDATE_TASK_URL + "{todo_id}")
                .addPathParameter("todo_id", String.valueOf(taskId))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                taskDeleteCallback.onTaskDeleted();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }


    public void doUpdateTodo(final int todoId, Todos todos, final TodoListUpdateCallback todoListUpdateCallback) {
        AndroidNetworking.post(Constants.UPDATE_TODO_URL + "{todo_id}")
                .addPathParameter("todo_id", String.valueOf(todoId))
                .addHeaders("text", todos.getItemText())
                .addHeaders("complete", String.valueOf(todos.isDone()))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                todoListUpdateCallback.onTodoUpdate();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }

    public void doUpdateTask(final int todoId, final Tasks task, final TaskUpdateCallback taskUpdateCallback) {
        AndroidNetworking.post(Constants.UPDATE_TASK_URL + "{todo_id}")
                .addPathParameter("todo_id", String.valueOf(todoId))
                .addHeaders("text", task.getItemText())
                .addHeaders("complete", String.valueOf(task.isDone()))
                .build()
                .getAsJSONObject(new JSONObjectRequestListener() {
                    @Override
                    public void onResponse(JSONObject response) {
                        try {
                            if (response.getInt("status") == 1) {
                                taskUpdateCallback.onTaskUpdate();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }

                    @Override
                    public void onError(ANError anError) {
                    }
                });
    }


}
