package com.polevpn.application.tools;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.util.DisplayMetrics;
import android.view.WindowManager;

import com.polevpn.application.App;
import com.polevpn.application.BuildConfig;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.security.MessageDigest;
import java.util.Enumeration;
import java.util.Locale;
import java.util.UUID;

public class Utils {
    public static String getIPAddress() {
        Context context = App.getAppContext();

        NetworkInfo info = ((ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE)).getActiveNetworkInfo();
        if (info != null && info.isConnected()) {
            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {//当前使用2G/3G/4G网络

                return getIpAddressFromDevice();

            } else if (info.getType() == ConnectivityManager.TYPE_WIFI) {//当前使用无线网络
                WifiManager wifiManager = (WifiManager) context.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                String ipAddress = intIP2StringIP(wifiInfo.getIpAddress());//得到IPV4地址
                return ipAddress;
            }
        }else{
            return "";
        }
        return "";
    }

    private  static String getIpAddressFromDevice(){
        try {
            for (Enumeration<NetworkInterface> en = NetworkInterface.getNetworkInterfaces(); en.hasMoreElements(); ) {
                NetworkInterface interfaces = en.nextElement();
                for (Enumeration<InetAddress> enumIpInfo = interfaces.getInetAddresses(); enumIpInfo.hasMoreElements(); ) {
                    InetAddress inetAddress = enumIpInfo.nextElement();
                    if (!inetAddress.isLoopbackAddress() && inetAddress instanceof Inet4Address) {
                        return inetAddress.getHostAddress();
                    }
                }
            }
        } catch (SocketException e) {
            e.printStackTrace();
            return "";
        }
        return "";
    }

    /**
     * 将得到的int类型的IP转换为String类型
     *
     * @param ip
     * @return
     */
    private static String intIP2StringIP(int ip) {
        return (ip & 0xFF) + "." +
                ((ip >> 8) & 0xFF) + "." +
                ((ip >> 16) & 0xFF) + "." +
                (ip >> 24 & 0xFF);
    }

    public static String getDeviceID()
    {
        String m_szDevIDShort = "35" + (Build.BOARD.length() % 10) + (Build.BRAND.length() % 10) + (Build.CPU_ABI.length() % 10) + (Build.DEVICE.length() % 10) + (Build.MANUFACTURER.length() % 10) + (Build.MODEL.length() % 10) + (Build.PRODUCT.length() % 10);
        String serial = null;
        try
        {
            serial = android.os.Build.class.getField("SERIAL").get(null).toString();
        }
        catch (Exception e)
        {
            serial = "serial"; // some value
        }
        return md5(m_szDevIDShort+serial);
    }

    public static String md5(String input)  {
        try{
            byte[] bytes = MessageDigest.getInstance("MD5").digest(input.getBytes());
            return printHexBinary(bytes);
        }catch (Exception e){
            e.printStackTrace();
        }
        return "";
    }
    public static String printHexBinary(byte[] data) {
        StringBuilder r = new StringBuilder(data.length * 2);
        for (byte b : data) {
            r.append(String.format("%02X", new Integer(b & 0xFF)));
        }
        return r.toString();
    }
    public static String getScreen(){
        DisplayMetrics dm = App.getAppContext().getResources().getDisplayMetrics();
        return dm.widthPixels+"*"+dm.heightPixels;
    }
    public static String getUserAgent(){
        String sysVersion = Build.VERSION.RELEASE;
        String manufacturer = Build.MANUFACTURER;
        String brand = Build.BRAND;
        String model = Build.MODEL;
        String display = getScreen();
        String lang = Locale.getDefault().getLanguage();
        String softVersion = BuildConfig.VERSION_NAME;
        String deviceId = getDeviceID();
        StringBuilder builder = new StringBuilder();
        builder.append("Platform/").append("Android").append(" ");
        builder.append("SysVersion/").append(sysVersion).append(" ");
        builder.append("MFC/").append(manufacturer).append(" ");
        builder.append("Brand/").append(brand).append(" ");
        builder.append("Model/").append(model).append(" ");
        builder.append("Display/").append(display).append(" ");
        builder.append("Lang/").append(lang).append(" ");
        builder.append("SoftVersion/").append(softVersion).append(" ");
        builder.append("DeviceId/").append(deviceId);
        return builder.toString();
    }
}
