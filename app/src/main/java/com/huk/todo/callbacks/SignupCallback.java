package com.huk.todo.callbacks;

/**
 * Created by User on 8/10/2017.
 */

public interface SignupCallback {
    void onDoSignup();
    void onSignupFailed();


    void onSignupFailed(String s);
}
