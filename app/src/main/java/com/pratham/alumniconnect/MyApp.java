package com.pratham.alumniconnect;

import android.app.Application;

import com.cloudinary.android.MediaManager;

import java.util.HashMap;
import java.util.Map;

//public class MyApp extends Application {
//
//    @Override
//    public void onCreate() {
//        super.onCreate();
//
//        Map<String, String> config = new HashMap<>();
//        config.put("cloud_name", "dvt0ac7op"); // ✅ Only cloud name needed for unsigned
//        MediaManager.init(this, config);
//    }
//}

public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        Map<String, String> config = new HashMap<>();
        config.put("cloud_name", "dvt0ac7op");
        config.put("api_key", "566366747551622");     // ← Use your signed key
        config.put("api_secret", "N4Y7A4oxR0A6aDh2RdqoFoBRzwA"); // ← Use your signed secret
        config.put("secure", "true");

        MediaManager.init(this, config);
    }
}
