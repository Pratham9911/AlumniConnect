package com.pratham.alumniconnect;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

public class MyApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dvt0ac7op"); // ✅ Only cloud name needed for unsigned
        MediaManager.init(this, config);
    }
}