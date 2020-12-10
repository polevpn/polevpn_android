package com.polevpn.application;

import android.Manifest;
import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.orhanobut.logger.AndroidLogAdapter;
import com.orhanobut.logger.DiskLogAdapter;
import com.orhanobut.logger.FormatStrategy;
import com.orhanobut.logger.Logger;
import com.orhanobut.logger.PrettyFormatStrategy;
import com.polevpn.application.data.Node;
import com.polevpn.application.tools.SharePref;
import com.tencent.bugly.crashreport.CrashReport;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

import polevpnmobile.PoleVPNLogHandler;
import polevpnmobile.Polevpnmobile;


public class App extends Application {

    private static Context context;

    @Override
    public void onCreate() {
        super.onCreate();
        context = getApplicationContext();

        Polevpnmobile.setLogLevel("INFO");
        getSystemConfig();
        FormatStrategy formatStrategy = PrettyFormatStrategy.newBuilder()
                .showThreadInfo(false)  // (Optional) Whether to show thread info or not. Default true
                .methodCount(0)         // (Optional) How many method line to show. Default 2
                .methodOffset(7)        // (Optional) Hides internal method calls up to offset. Default 5
                .tag("PoleVPNLogger")   // (Optional) Global tag for every log. Default PRETTY_LOGGER
                .build();

        if(BuildConfig.DEBUG){
            Logger.addLogAdapter(new AndroidLogAdapter(formatStrategy));
        }
        CrashReport.initCrashReport(getApplicationContext(), "97c0733afd", false);
    }


    public static void requestPermission(Activity activity) {

        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(activity, new String[]{
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE}, 100);
        }
    }


    public static void getSystemConfig(){
        Polevpnmobile.getSystemConfig((ret,msg,resp)->{
            if(ret!= Polevpnmobile.HTTP_OK){
                Logger.e("get system config fail");
                return;
            }
            try{
                JSONObject obj = new JSONObject(resp);
                String apiHost = obj.getString("api_host");
                SharePref.getInstance().setString("api_host",apiHost);

            }catch (JSONException e){
                Logger.e(e,e.getMessage());
            }

        });
    }

    public static Context getAppContext() {
        return context;
    }

}
