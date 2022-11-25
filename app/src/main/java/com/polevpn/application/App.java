package com.polevpn.application;
import android.app.Application;
import android.content.Context;

import polevpnmobile.Polevpnmobile;


public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();
        Polevpnmobile.setLogLevel("INFO");

    }

    public static Context getAppContext() {
        return context;
    }

}
