package com.polevpn.application.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.LinkProperties;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.util.Log;

public class NetworkMonitor {

    private NetworkMonitorCallback callback;

    private ConnectivityManager.NetworkCallback networkCallback = new ConnectivityManager.NetworkCallback() {
        /**
         * 网络可用的回调连接成功
         */
        @Override
        public void onAvailable(Network network) {
            if (callback != null) {
                callback.onChanged("available",network);
            }
            super.onAvailable(network);
        }

        /**
         * 网络不可用时调用和onAvailable成对出现
         */
        @Override
        public void onLost(Network network) {
            if (callback != null) {
                callback.onChanged("lost",network);
            }
            super.onLost(network);
        }

        /**
         * 在网络连接正常的情况下，丢失数据会有回调 即将断开时
         */
        @Override
        public void onLosing(Network network, int maxMsToLive) {
            if (callback != null) {
                callback.onChanged("losing",network);
            }
            super.onLosing(network, maxMsToLive);
        }

        /**
         * 网络功能更改 满足需求时调用
         * @param network
         * @param networkCapabilities
         */
        @Override
        public void onCapabilitiesChanged(Network network, NetworkCapabilities networkCapabilities) {
            super.onCapabilitiesChanged(network, networkCapabilities);
            if (callback != null) {
                callback.onChanged("cap-changed",network);
            }
        }

        /**
         * 网络连接属性修改时调用
         * @param network
         * @param linkProperties
         */
        @Override
        public void onLinkPropertiesChanged(Network network, LinkProperties linkProperties) {
            super.onLinkPropertiesChanged(network, linkProperties);
            if (callback != null) {
                callback.onChanged("prop-changed",network);
            }
        }

        /**
         * 网络缺失network时调用
         */
        @Override
        public void onUnavailable() {
            super.onUnavailable();
            if (callback != null) {
                callback.onChanged("unavailable",null);
            }
        }
    };

    public NetworkMonitor(){

    }

    public void registerNetworkCallback(Context context,NetworkMonitorCallback callback){
         ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkRequest request = new NetworkRequest.Builder()
                .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
                .addCapability(NetworkCapabilities.NET_CAPABILITY_NOT_RESTRICTED)
                .build();
         manager.registerNetworkCallback(request,networkCallback);
         this.callback = callback;

    }

    public void unregisterNetworkCallback(Context context){
        ConnectivityManager manager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        manager.unregisterNetworkCallback(networkCallback);
    }

    public static interface NetworkMonitorCallback {
        public void onChanged(String state,Network network);
    }

}
