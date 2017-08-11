package com.huk.todo.callbacks.todoitemlistcallbacks;

/**
 * Created by User on 8/10/2017.
 */

public interface TaskCreatedCallback {
    void onTaskCreated();

    void onTaskCreated(int mParentId, String s);
}
