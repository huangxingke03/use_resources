package com.example.resources;

import android.app.Application;

import com.example.resources.util.MyLogUtils;
import com.example.resources.util.MyLogUtilsKotlin;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyLogUtils.init(true);
        MyLogUtilsKotlin.init(true);
    }
}
