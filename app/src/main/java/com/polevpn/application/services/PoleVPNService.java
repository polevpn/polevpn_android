package com.polevpn.application.services;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;

import androidx.core.app.NotificationCompat;

import com.polevpn.application.MainActivity;
import com.polevpn.application.R;

import java.util.List;

import polevpnmobile.Polevpnmobile;


public class PoleVPNService extends VpnService {

    private ParcelFileDescriptor mInterface;

    private NotificationManager notifyManager;
    private NotificationCompat.Builder notifyBuilder;
    private int NOTIFICATION_ID = 1;

    public PoleVPNService() {
        super();
    }


    @Override
    public void onCreate() {
        super.onCreate();
        PoleVPNManager.getInstance().setService(this);

    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        PoleVPNManager.getInstance().setService(null);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        int  ret = super.onStartCommand(intent, flags, startId);
        if(PoleVPNManager.getInstance().getPoleVPN().getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED){
            PoleVPNManager.getInstance().setService(this);
            showNotification();
        }
        return  ret;
    }

    @Override
    public void onRevoke() {
        super.onRevoke();
        PoleVPNManager.getInstance().getPoleVPN().stop();
    }

    public void showNotification(){

        Log.i("vpn","-----------showNotification--------------");

        notifyManager = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
        notifyBuilder = new NotificationCompat.Builder(getApplicationContext(),getNotificationChannel());

        Intent activityIntent = new Intent(getApplicationContext(), MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(getApplicationContext(), 1, activityIntent, PendingIntent.FLAG_UPDATE_CURRENT);

        notifyBuilder.setContentTitle("PoleVPN")//设置通知栏标题
                .setContentText("PoleVPN is working") //设置通知栏显示内容
                .setContentIntent(pendingIntent) //设置通知栏点击意图
                .setShowWhen(false)
                .setPriority(Notification.PRIORITY_DEFAULT) //设置该通知优先级
                .setOngoing(true)//true，设置他为一个正在进行的通知。他们通常是用来表示一个后台任务
                .setOnlyAlertOnce(true)
                .setSmallIcon(R.drawable.polevpn);//设置通知小ICON

        startForeground(NOTIFICATION_ID, notifyBuilder.build());
    }

    public String getNotificationChannel(){
        String notificationChannelId = "POLE_VPN_CH_ID";

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "PoleVPN Service";
            NotificationChannel notificationChannel = new NotificationChannel(notificationChannelId, channelName, NotificationManager.IMPORTANCE_NONE);
            notificationChannel.setDescription("Channel description");
            if (notifyManager != null) {
                notifyManager.createNotificationChannel(notificationChannel);
            }
        }
        return notificationChannelId;
    }

    public boolean start(String ip, String dns, List<String> routes, String pkgName) {
        Builder builder = new Builder();
        builder.setSession("PoleVPN")
                .addAddress(ip, 32)
                .addDnsServer(dns)
                .setMtu(1500);

        Log.i("main",routes.toString());

        for (String route:routes) {
            String [] arRoute = route.split("/");
            if (arRoute.length == 2){
                builder.addRoute(arRoute[0],Integer.parseInt(arRoute[1]));
            }
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            builder.setMetered(true);
        }

       if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP){
           try{
               builder.addDisallowedApplication(pkgName);
           }catch (Exception e){
               e.printStackTrace();
           }
       }

        mInterface = builder.establish();

        return  true;
    }

    public ParcelFileDescriptor getInterface(){
        return mInterface;
    }

    public void stop() {
        if (mInterface != null) {
            try {
                mInterface.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        stopForeground(true);
    }
}
