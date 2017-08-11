package com.huk.todo;

import android.app.Application;
import android.app.PendingIntent;
import android.content.Intent;

import com.androidnetworking.AndroidNetworking;
import com.facebook.stetho.Stetho;
import com.facebook.stetho.okhttp3.StethoInterceptor;
import com.huk.todo.activities.MainActivity;
import com.huk.todo.helper.SharedPrefsUtils;
import com.huk.todo.network.synclib.TimeSync;

import okhttp3.OkHttpClient;

import static android.content.Intent.FLAG_ACTIVITY_CLEAR_TOP;
import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;
import static android.content.Intent.FLAG_ACTIVITY_RESET_TASK_IF_NEEDED;
import static com.huk.todo.helper.Constants.IS_LOGIN;

/**
 * Created by User on 8/9/2017.
 */

public class TodoApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Stetho.initializeWithDefaults(this);
        OkHttpClient okHttpClient = new OkHttpClient() .newBuilder()
                .addNetworkInterceptor(new StethoInterceptor())
                .build();
        AndroidNetworking.initialize(getApplicationContext(),okHttpClient);
        TimeSync.start(this);
    }
}
