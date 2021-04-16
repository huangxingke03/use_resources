package com.example.resources;

import android.app.Application;
import com.example.resources.util.MyLogUtils;

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        MyLogUtils.init(true);
    }
}
