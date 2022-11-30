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
        initDbAndLog();

    }

    private void initDbAndLog(){
        try {
            String path = getApplicationContext().getFilesDir().getAbsolutePath();
            Polevpnmobile.initDB(path+"/config.db");
            Polevpnmobile.setLogPath(path);
            Polevpnmobile.setLogLevel("INFO");
        }catch (Exception e){
            Polevpnmobile.log("error",e.getMessage());
            e.printStackTrace();
        }
    }

    public static Context getAppContext() {
        return context;
    }

}
