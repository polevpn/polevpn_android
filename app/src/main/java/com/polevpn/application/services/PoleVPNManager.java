package com.polevpn.application.services;

import android.app.Service;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.polevpn.application.App;
import com.polevpn.application.MainActivity;
import com.polevpn.application.tools.Utils;

import java.util.HashMap;
import java.util.Map;

import polevpnmobile.PoleVPN;
import polevpnmobile.Polevpnmobile;

public class PoleVPNManager {

    private  PoleVPN polevpn = Polevpnmobile.newPoleVPN();
    private  Context context;
    private  String localIP;
    private  boolean registered;
    private NetworkMonitor monitor = new NetworkMonitor();
    private static PoleVPNManager instance;
    private Service service;
    private Map<String,Handler> messageHandler;


    private PoleVPNManager(){
        messageHandler = new HashMap<>();
        localIP =  Utils.getIPAddress();
        context = App.getAppContext();
    }

    synchronized public static PoleVPNManager getInstance(){
        if(instance == null){
            instance = new PoleVPNManager();
        }
        return instance;
    }

    public  PoleVPN getPoleVPN(){
        return  polevpn;
    }

    public void setService(Service service){
        this.service = service;
    }

    public Service getService(){
        return this.service;
    }

    public  void registerNetworkCallback(){

        if (registered == true){
            return;
        }

        registered = true;
        monitor.registerNetworkCallback(context,(state,network)->{
            String curLocalIp = Utils.getIPAddress();
            Log.i("vpn","network changed "+state+",old IP="+localIP+",new IP="+curLocalIp);

            if(!curLocalIp.equals(localIP) && polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED){
                Log.i("vpn","network changed,refresh network");
                polevpn.setLocalIP(curLocalIp);
                polevpn.closeConnect(true);
            }
            localIP = curLocalIp;
        });
    }

    public void unregisterNetworkCallback(){

        if(registered == false){
            return;
        }

        monitor.unregisterNetworkCallback(context);
        registered = false;
    }

    public void addMessageHandler(String type,Handler handler){
        this.messageHandler.put(type,handler);
    }
    public void removeMessageHandler(String type){
        this.messageHandler.remove(type);
    }

    public void sendMessage(String type,Bundle msg){
        Handler handler = messageHandler.get(type);
        if(handler!= null){
            Message message = new Message();
            message.setData(msg);
            handler.sendMessage(message);
        }
    }


}
