package com.example.uber;

import android.app.Application;

import com.parse.Parse;

public class App extends Application {
    @Override
    public void onCreate() {
        Parse.initialize(new Parse.Configuration.Builder(this)
                .applicationId("URoDv6XIOOjZqUVGSbllSW4IyjTbGsBLWczwaLhG")
                // if defined
                .clientKey("g5IvIkuRTTDOYOZboU3Gdbd4tuZbrqXz7im217tX")
                .server("https://parseapi.back4app.com/")
                .build()
        );
        super.onCreate();
    }
}
