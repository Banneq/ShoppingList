package com.example.shoppinglist;

import android.app.Application;

import com.backendless.Backendless;
import com.backendless.BackendlessUser;

public class ApplicationClass extends Application {
    public static final String APPLICATION_ID = "D29A2D2D-7954-CA75-FF61-284806176A00";
    public static final String API_KEY = "5B05C294-FBE4-F4C1-FFF1-B15B77895400";
    public static final String SERVER_URL = "https://api.backendless.com";
    public static BackendlessUser user;

    @Override
    public void onCreate() {
        super.onCreate();

        Backendless.setUrl( SERVER_URL );
        Backendless.initApp( getApplicationContext(),
                APPLICATION_ID,
                API_KEY );
    }
}
