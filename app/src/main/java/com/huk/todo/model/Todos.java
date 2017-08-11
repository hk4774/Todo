package com.huk.todo.model;

/**
 * Created by User on 8/10/2017.
 */

public class Todos {

    private int itemID;

    private String itemText;
    private boolean isDone;
    private String pub_date;

    public Todos(){

    }

    public Todos(int itemID, String itemText, boolean isDone, String pub_date) {
        this.itemID = itemID;
        this.itemText = itemText;
        this.isDone = isDone;
        this.pub_date = pub_date;
    }

    public int getItemID() {
        return itemID;
    }

    public void setItemID(int itemID) {
        this.itemID = itemID;
    }

    public String getItemText() {
        return itemText;
    }

    public void setItemText(String itemText) {
        this.itemText = itemText;
    }

    public boolean isDone() {
        return isDone;
    }

    public void setDone(boolean done) {
        isDone = done;
    }

    public String getPub_date() {
        return pub_date;
    }

    public void setPub_date(String pub_date) {
        this.pub_date = pub_date;
    }
}
