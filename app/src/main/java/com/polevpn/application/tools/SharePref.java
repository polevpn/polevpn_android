package com.polevpn.application.tools;
import android.content.Context;
import android.content.SharedPreferences;

import com.polevpn.application.App;
import com.polevpn.application.services.PoleVPNManager;

public class SharePref {
    SharedPreferences sharedPreferences;
    private boolean inited;
    static private SharePref instance;

    private SharePref(){
    }

    synchronized public static SharePref getInstance(){
        if(instance == null){
            instance = new SharePref();
            instance.init(App.getAppContext());
        }
        return instance;
    }

    private void init(Context context) {
        if(inited)
        {
            return;
        }
        sharedPreferences = context.getSharedPreferences("PoleVPN", Context.MODE_PRIVATE);
        inited = true;
    }

    public void setString(String key,String value) {
        sharedPreferences.edit().putString(key, value).commit();
    }

    public void setInt(String key,Integer value) {
        sharedPreferences.edit().putInt(key, value).commit();
    }

    public void setLong(String key,Long value) {
        sharedPreferences.edit().putLong(key, value).commit();
    }

    public void setFloat(String key,Float value) {
        sharedPreferences.edit().putFloat(key, value).commit();
    }

    public void setBoolean(String key,boolean value) {
        sharedPreferences.edit().putBoolean(key, value).commit();
    }

    //获取数据的方法
    public String getString(String key) {
        return sharedPreferences.getString(key, "");
    }

    public boolean getBoolean(String key) {
        return sharedPreferences.getBoolean(key, false);
    }

    public int getInt(String key) {
        return sharedPreferences.getInt(key, 0);
    }

    public long getLong(String key) {
        return sharedPreferences.getLong(key, 0);
    }

    public float getFloat(String key) {
        return sharedPreferences.getFloat(key, 0.0f);
    }

    public void remove(String key) {
         sharedPreferences.edit().remove(key).commit();
    }
    public void clear() {
        sharedPreferences.edit().clear().commit();
    }

}