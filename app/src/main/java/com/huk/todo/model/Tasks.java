package com.huk.todo.model;

public class Tasks {

    private int itemID;
    private String itemText;
    private boolean isDone;
    private int parentId;

    public Tasks(int id, String text, boolean done) {
        itemID = id;
        itemText = text;
        isDone = done;
    }

    public Tasks(int id, String text, boolean done, int parent_Id) {
        itemID = id;
        itemText = text;
        isDone = done;
        parentId = parent_Id;
    }

    public String getItemText() {
        return itemText;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setIsCompleted(boolean isCompleted) {
        this.isDone = isCompleted;
    }

    public int getItemID() {
        return itemID;
    }

    public int getParentId() {
        return parentId;
    }

    public void setParentId(int parentId) {
        this.parentId = parentId;
    }
}
