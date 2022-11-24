package com.polevpn.application;
import android.app.Application;
import android.content.Context;
import com.tencent.bugly.crashreport.CrashReport;
import polevpnmobile.Polevpnmobile;


public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        Polevpnmobile.setLogLevel("INFO");
        CrashReport.initCrashReport(getApplicationContext(), "97c0733afd", false);
    }

    public static Context getAppContext() {
        return context;
    }

}
