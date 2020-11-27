package com.polevpn.application.services;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.net.VpnService;
import android.os.Build;
import android.os.ParcelFileDescriptor;
import android.util.Log;


public class PoleVPNService extends VpnService {

    private ParcelFileDescriptor mInterface;
    public PoleVPNService() {
        super();
    }

    public boolean startService(String ip,String dns,String pkgName) {
        Builder builder = new Builder();
        builder.setSession("PoleVPN")
                .addAddress(ip, 16)
                .addDnsServer(dns)
                .addRoute("0.0.0.0",0)
                .setMtu(1500);

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

    public void stopService() {
        if(mInterface!= null){
            try{
                mInterface.close();
            }catch (Exception e){
                e.printStackTrace();
            }
        }
    }

    @Override
    public boolean setUnderlyingNetworks(Network[] networks) {
        return super.setUnderlyingNetworks(networks);
    }
}
