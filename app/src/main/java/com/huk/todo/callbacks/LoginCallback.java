package com.huk.todo.callbacks;

/**
 * Created by User on 8/10/2017.
 */

public interface LoginCallback {
    void onDoLogin();
    void onLoginFailed();

    void onLoginFailed(String s);
}
