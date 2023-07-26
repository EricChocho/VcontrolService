package com.viewsonic.service1;

import android.app.Application;

public class MyAPP extends Application {

    private static MyAPP instance;

    @Override
    public void onCreate() {
        // TODO Auto-generated method stub
        super.onCreate();

        instance = this;
    }

    public static MyAPP getInstance() {
        // TODO Auto-generated method stub
        return instance;
    }
}