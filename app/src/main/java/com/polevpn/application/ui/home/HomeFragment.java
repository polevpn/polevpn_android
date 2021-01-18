package com.polevpn.application.ui.home;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.net.VpnService;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.orhanobut.logger.Logger;
import com.polevpn.application.App;
import com.polevpn.application.MainActivity;
import com.polevpn.application.R;
import com.polevpn.application.data.Node;
import com.polevpn.application.services.PoleVPNManager;
import com.polevpn.application.services.PoleVPNService;
import com.polevpn.application.tools.SharePref;
import com.polevpn.application.tools.Utils;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import polevpnmobile.PoleVPN;
import polevpnmobile.PoleVPNEventHandler;
import polevpnmobile.Polevpnmobile;



public class HomeFragment extends Fragment {

    private PoleVPNService myVpnService;
    private ImageView btnConnect;
    private Button btnSelect;
    private PoleVPN polevpn;
    private ImageView imgConnecting;
    private Animation animConnecting;
    private TextView textStatus;
    private TextView textMode;
    private AlertDialog alertDialog;
    private String localIP;
    private String dns;
    private String ip;
    private int currentEndpointIndex;
    private boolean changeNode;
    private ArrayList<Map<String, Object>> nodeList = new ArrayList();

    public Handler handler = new Handler(Looper.getMainLooper()){
        @Override
        public void handleMessage(Message msg) {
            String type =  msg.getData().getString("type");
            switch (type){
            case "speed_setting":
                {
                    boolean speed_up_mode =  msg.getData().getBoolean("speed_up_mode");
                    if(speed_up_mode){
                        textMode.setText("加速模式：全局加速");
                    }else{
                        textMode.setText("加速模式：智能分流");
                    }
                    break;
                }
            case "load_all_nodes":
                {
                    parserAllNodes();
                    break;
                }
            }

        }
    };


    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.fragment_home, container, false);
        btnConnect = root.findViewById(R.id.btn_connect);
        btnSelect = root.findViewById(R.id.btn_select);
        imgConnecting = root.findViewById(R.id.img_connecting);
        textStatus = root.findViewById(R.id.text_status);
        textMode = root.findViewById(R.id.text_mode);

        animConnecting = AnimationUtils.loadAnimation(getContext(), R.anim.rotaterepeat);
        animConnecting.setInterpolator(new LinearInterpolator());

        polevpn = PoleVPNManager.getInstance().getPoleVPN();
        polevpn.setEventHandler(poleVPNEventHandler);

        if(SharePref.getInstance().getBoolean("speed_up_mode")){
            textMode.setText("加速模式：全局加速");
        }else{
            textMode.setText("加速模式：智能分流");
        }

        currentEndpointIndex = SharePref.getInstance().getInt("endpoint_index");

        PoleVPNManager.getInstance().addMessageHandler("home",handler);

        if (polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED) {
            btnConnect.setImageResource(R.drawable.connected);
            imgConnecting.setVisibility(ImageView.INVISIBLE);
            textStatus.setVisibility(TextView.VISIBLE);

        }else{
            btnConnect.setImageResource(R.drawable.connect);
            imgConnecting.setVisibility(ImageView.VISIBLE);
            textStatus.setVisibility(TextView.INVISIBLE);
        }

        btnConnect.setOnClickListener(new View.OnClickListener() {
            @SuppressLint("ResourceType")
            @Override
            public void onClick(View view) {
                if(polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED){
                    imgConnecting.setVisibility(ImageView.VISIBLE);
                    imgConnecting.startAnimation(animConnecting);
                    stopVPN();
                }else {
                    String ip = Utils.getIPAddress();
                    if(ip == ""){
                        Toast("Network Not Available");
                        return;
                    }
                    PoleVPNManager.getInstance().registerNetworkCallback();
                    startVPN();

                }
            }
        });

        btnConnect.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {

                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:{
                        if(polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED){
                            btnConnect.setImageResource(R.drawable.connected_ontouch);
                        }else{
                            btnConnect.setImageResource(R.drawable.connect_ontouch);
                        }
                    }
                }

                return false;
            }
        });

        btnSelect.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showList(view);
            }
        });

        myVpnService = new PoleVPNService();

        Node.loadAllNodes(false);
        parserAllNodes();

        return root;
    }

    private PoleVPNEventHandler poleVPNEventHandler = new PoleVPNEventHandler() {
        @Override
        public void onAllocEvent(String ip, String dns) {

            try {
                Logger.i( "vpn server allocated ip="+ip + ",dns=" + dns);
                HomeFragment.this.dns = dns;
                HomeFragment.this.ip = ip;
                new Handler(Looper.getMainLooper()).post(()->{
                    Intent intent = VpnService.prepare(App.getAppContext());
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, Activity.RESULT_OK, null);
                    }
                });
            }catch (Throwable e){
                Logger.e(e,e.getMessage());
            }
        }

        @Override
        public void onErrorEvent(String type, String msg) {

            if(type.equals("login")){
                Log.e("vpn","token invalid");
                Toast("token invalid,please login again");
            }else{
                Toast("vpn error,"+msg);
                Log.e("vpn",msg);
            }
        }

        @Override
        public void onReconnectedEvent() {
            Logger.i("vpn reconnected");
            Toast("vpn reconnected");
        }

        @Override
        public void onReconnectingEvent() {
            Logger.i("vpn reconnecting");
            Toast("vpn reconnecting");
            polevpn.setLocalIP(Utils.getIPAddress());
        }

        @Override
        public void onStartedEvent() {

            Logger.i("vpn server connected");
            new Handler(Looper.getMainLooper()).post(()->{
                PoleVPNManager.getInstance().registerNetworkCallback();
            });
        }

        @Override
        public void onStoppedEvent() {
            try{
                Logger.i("vpn stopped");
                new Handler(Looper.getMainLooper()).post(()->{
                    myVpnService.stop();
                    btnConnect.setImageResource(R.drawable.connect);
                    textStatus.setVisibility(TextView.INVISIBLE);
                    imgConnecting.clearAnimation();
                    PoleVPNManager.getInstance().unregisterNetworkCallback();
                });

                if(changeNode){
                    startVPN();
                    changeNode = false;
                }
            }catch (Throwable e){
                Logger.e(e,e.getMessage());
            }
        }
    };


    private void parserAllNodes(){

        String allNodes = SharePref.getInstance().getString("all_nodes");
        if(allNodes.equals("")){
            return;
        }

        try{

            JSONObject obj = new JSONObject(allNodes);

            JSONArray result = obj.getJSONArray("free");

            nodeList.clear();

            for(int i=0;i<result.length();i++){
                Map<String,Object> item = new HashMap();
                item.put("image", R.drawable.logo);
                item.put("title", result.getJSONObject(i).get("RegionZh"));
                item.put("info",result.getJSONObject(i));
                nodeList.add(item);
            }

            new Handler(Looper.getMainLooper()).post(()->{
                if (currentEndpointIndex < nodeList.size()){
                    btnSelect.setText("当前线路 ("+nodeList.get(currentEndpointIndex).get("title")+")");
                }
            });

        }catch (JSONException e){
            Logger.e(e,e.getMessage());
        }

    }

    private String getEndpoint() {

        try{
            JSONObject info = (JSONObject)nodeList.get(currentEndpointIndex).get("info");

            int len = info.getJSONArray("Nodes").length();
            Random random = new Random();
            int index = random.nextInt(len);
            for(int i=0;i<len;i++){
                if(index == i){
                    return info.getJSONArray("Nodes").getJSONObject(i).get("CdnEndpoint").toString();
                }
            }

        }catch (JSONException e){
            Logger.e(e,e.getMessage());
            return "";
        }
        return "";
    }

    private String getEndpointSni() {

        try{
            JSONObject info = (JSONObject)nodeList.get(currentEndpointIndex).get("info");

            int len = info.getJSONArray("Nodes").length();
            Random random = new Random();
            int index = random.nextInt(len);
            for(int i=0;i<len;i++){
                if(index == i){
                    return info.getJSONArray("Nodes").getJSONObject(i).get("Sni").toString();
                }
            }

        }catch (JSONException e){
            Logger.e(e,e.getMessage());
            return "";
        }
        return "";
    }

    private void startVPN(){
        String ip = Utils.getIPAddress();
        if(ip == ""){
            Toast("Network Not Available");
            return;
        }

        if(nodeList.isEmpty()){
            Toast("没有可用的线路");
            return;
        }

        new Handler(Looper.getMainLooper()).post(()->{
            imgConnecting.setVisibility(ImageView.VISIBLE);
            imgConnecting.startAnimation(animConnecting);
        });

        String endpoint = getEndpoint();
        String sni = getEndpointSni();

        String email = SharePref.getInstance().getString("email");
        String pwd = SharePref.getInstance().getString("token");
        polevpn.setLocalIP(ip);
        polevpn.setRouteMode(SharePref.getInstance().getBoolean("speed_up_mode"));
        polevpn.start(endpoint,email,pwd,sni);

    }

    private void stopVPN(){
        polevpn.stop();
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            new Handler(Looper.getMainLooper()).post(()->{
                Intent intentStart = new Intent(App.getAppContext(), PoleVPNService.class);
                App.getAppContext().startService(intentStart);
                myVpnService.start(ip,dns,App.getAppContext().getPackageName());
                if (myVpnService.getInterface()!= null){
                    int fd = myVpnService.getInterface().detachFd();
                    polevpn.attach(fd);
                    btnConnect.setImageResource(R.drawable.connected);
                    imgConnecting.setVisibility(ImageView.INVISIBLE);
                    imgConnecting.clearAnimation();
                    animConnecting.cancel();
                    textStatus.setVisibility(TextView.VISIBLE);
                }else{
                    Logger.e("vpn started fail");
                }

            });
            Logger.i("vpn started successful");

        }else{
            Logger.i("user authorize failed");
            Toast("user authorize failed");
            stopVPN();
        }
    }

    private void Toast(String msg){
        new Handler(Looper.getMainLooper()).post(()->{
            Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    public void showList(View view){

        SimpleAdapter adapter = new SimpleAdapter(this.getContext(),nodeList,R.layout.area_list_view,
                new String[]{"image","title"},new int[]{R.id.ItemImage,R.id.ItemTitle});

        AlertDialog.Builder alertBuilder = new AlertDialog.Builder(getContext());
        alertBuilder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                if(polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED && currentEndpointIndex!=i){
                    changeNode = true;
                    currentEndpointIndex = i;
                    SharePref.getInstance().setInt("endpoint_index",i);
                    Toast("正在切换线路到"+nodeList.get(i).get("title").toString());
                    stopVPN();
                }else if(polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STOPPED || polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_INIT){
                    currentEndpointIndex = i;
                    SharePref.getInstance().setInt("endpoint_index",i);
                    Toast("正在连接到"+nodeList.get(i).get("title").toString()+"线路");
                    startVPN();
                    changeNode = false;
                }
                btnSelect.setText("当前线路 ("+nodeList.get(i).get("title").toString()+")");
                alertDialog.dismiss();
            }
        });
        alertBuilder.setTitle("选择线路");
        alertDialog = alertBuilder.create();
        alertDialog.show();


        WindowManager m = getActivity().getWindowManager();
        Display d = m.getDefaultDisplay();
        android.view.WindowManager.LayoutParams p = alertDialog.getWindow().getAttributes();
        Point point = new Point();
        d.getRealSize(point);
        p.height = (int) (point.y * 0.6);
        p.width = (int) (point.x * 0.9);
        alertDialog.getWindow().setAttributes(p);
    }


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        PoleVPNManager.getInstance().removeMessageHandler("home");
    }
}