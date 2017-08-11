package com.huk.todo.helper;

/**
 * Created by User on 8/10/2017.
 */

public interface Constants {

    String BASE_URL = "https://todo-list-huk.herokuapp.com/todos";
    String LOGIN_URL = BASE_URL + "/login";
    String SIGN_UP_URL = BASE_URL + "/register";
    String LOGUT_URL = BASE_URL + "/logout";
    String UPDATE_TODO_URL = BASE_URL + "/update/";
    String UPDATE_TASK_URL = BASE_URL + "/update/item/";
    String DELETE_TODO_URL = BASE_URL + "/update/";
    String DELETE_TASK_URL = BASE_URL + "/update/item/";
    String CREATE_TODO_URL = BASE_URL + "/new";
    String CREATE_TASK_URL = BASE_URL + "/new/";
    String IS_LOGIN = "is_logedin";
    String ALL_PERMISSION_GRANTED = "all_permission_granted";
}
