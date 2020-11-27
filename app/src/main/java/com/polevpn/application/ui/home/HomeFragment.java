package com.polevpn.application.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.polevpn.application.R;
import com.polevpn.application.services.NetworkMonitor;
import com.polevpn.application.services.PoleVPNManager;
import com.polevpn.application.services.PoleVPNService;
import com.polevpn.application.tools.Utils;

import polevpnmobile.PoleVPN;
import polevpnmobile.PoleVPNEventHandler;
import polevpnmobile.Polevpnmobile;


public class HomeFragment extends Fragment {

    private HomeViewModel homeViewModel;
    private PoleVPNService myVpnService;
    private ImageButton btnConnect;
    private PoleVPN polevpn;
    private ImageView imgConnecting;
    private Animation animConnecting;
    private String vpnState;
    private String localIP;
    private String dns;
    private String ip;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        btnConnect = root.findViewById(R.id.btn_connect);
        imgConnecting = root.findViewById(R.id.img_connect);

        imgConnecting.setVisibility(ImageView.INVISIBLE);

        Polevpnmobile.setLogLevel("INFO");
        PoleVPNManager.getInstance().setContext(getContext());

        polevpn = PoleVPNManager.getInstance().getPoleVPN();
        polevpn.setEventHandler(poleVPNEventHandler);

        if (polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED) {
            btnConnect.setImageResource(R.drawable.connected);
        }else{
            btnConnect.setImageResource(R.drawable.connect);
            PoleVPNManager.getInstance().unregisterNetworkCallback();
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED){
                    stopVPN();
                }else {
                     startVPN();
                }
            }
        });
        myVpnService = new PoleVPNService();
        return root;
    }

    private PoleVPNEventHandler poleVPNEventHandler = new PoleVPNEventHandler() {
        @Override
        public void onAllocEvent(String ip, String dns) {

            try {
                Log.i("vpn", "vpn server allocated ip="+ip + ",dns=" + dns);
                HomeFragment.this.dns = dns;
                HomeFragment.this.ip = ip;

                Intent intent = VpnService.prepare(getContext());
                if (intent != null) {
                    startActivityForResult(intent, 0);
                } else {
                    onActivityResult(0, Activity.RESULT_OK, null);
                }
            }catch (Throwable e){
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorEvent(String type, String msg) {

            if(type.equals("login")){
                Log.e("vpn","user or password invalid");
                Toast("user or password invalid");
            }else{
                Toast("vpn error,"+msg);
                Log.e("vpn",msg);
            }
        }

        @Override
        public void onReconnectedEvent() {
            Log.i("vpn","vpn reconnected");
            Toast("vpn reconnected");
        }

        @Override
        public void onReconnectingEvent() {
            polevpn.setLocalIP(Utils.getIPAddress(getContext()));
            Log.i("vpn","vpn reconnecting");
            Toast("vpn reconnecting");
        }

        @Override
        public void onStartedEvent() {

            Log.i("vpn","vpn server connected");
            PoleVPNManager.getInstance().registerNetworkCallback();
        }

        @Override
        public void onStoppedEvent() {
            vpnState = "stopped";
            Log.i("vpn","vpn stopped");
            Toast("vpn stopped");
            new Handler(Looper.getMainLooper()).post(()->{
                btnConnect.setImageResource(R.drawable.connect);
            });
            PoleVPNManager.getInstance().unregisterNetworkCallback();
        }
    };

    private void startVPN(){
        polevpn.setLocalIP( Utils.getIPAddress(getContext()));
        polevpn.start("wss://18.136.205.81/ws","polevpn","123456","www.apple.com");

    }

    private void stopVPN(){
        polevpn.stop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK) {
            Intent intentStart = new Intent(getContext(), PoleVPNService.class);
            getContext().startService(intentStart);
            myVpnService.startService(ip,dns,getContext().getPackageName());
            polevpn.attach(myVpnService.getInterface().getFd());
            btnConnect.setImageResource(R.drawable.connected);
            Log.i("vpn","vpn started successful");
            Toast("vpn started successfully");

        }else{
            Log.i("vpn","user authorize failed");
            Toast("user authorize failed");
            stopVPN();
        }
    }

    private void Toast(String msg){
        new Handler(Looper.getMainLooper()).post(()->{
            Toast.makeText(getContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
    }
}