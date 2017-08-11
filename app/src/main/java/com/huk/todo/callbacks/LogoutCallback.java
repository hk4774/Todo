package com.huk.todo.callbacks;

/**
 * Created by User on 8/11/2017.
 */

public interface LogoutCallback {
    void onLogoutComplete();

    void onLogoutFailed(String message);

    void onLogoutFailed();
}
