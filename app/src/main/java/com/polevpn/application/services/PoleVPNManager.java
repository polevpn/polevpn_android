package com.polevpn.application.services;

import android.content.Context;
import android.util.Log;

import com.polevpn.application.tools.Utils;

import polevpnmobile.PoleVPN;
import polevpnmobile.Polevpnmobile;

public class PoleVPNManager {

    private  PoleVPN polevpn = Polevpnmobile.newPoleVPN();
    private  Context context;
    private  String localIP;
    private  boolean registered;
    private NetworkMonitor monitor = new NetworkMonitor();
    private static PoleVPNManager instance;


    private PoleVPNManager(){
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

    public void setContext(Context context){
        this.context = context;
        localIP =  Utils.getIPAddress(context);
    }

    public  void registerNetworkCallback(){

        if (context == null) {
            return;
        }

        if (registered == true){
            return;
        }

        registered = true;
        monitor.registerNetworkCallback(context,(state,network)->{
            String curLocalIp = Utils.getIPAddress(context);
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
        if (context == null) {
            return;
        }
        if(registered == false){
            return;
        }

        monitor.unregisterNetworkCallback(context);
        registered = false;
    }
}
