package com.polevpn.application;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Intent;
import android.net.VpnService;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.JavascriptInterface;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.Toast;

import com.polevpn.application.services.PoleVPNManager;
import com.polevpn.application.services.PoleVPNService;

import org.json.JSONObject;

import polevpnmobile.PoleVPN;
import polevpnmobile.PoleVPNEventHandler;
import polevpnmobile.Polevpnmobile;
import polevpnmobile.*;


public class MainActivity extends AppCompatActivity {

    private WebView webView;
    private PoleVPNService myVpnService;
    private PoleVPN polevpn;
    private String dns;
    private String ip;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWindow();
        initDbAndLog();

        webView = (WebView) findViewById(R.id.webview);
        webView.setWebChromeClient(new WebChromeClient());
        WebSettings webSettings = webView.getSettings();
        initWebView(webSettings);
        webView.addJavascriptInterface(this, "ext");
        webView.loadUrl("file:///android_asset/index.html");

        myVpnService = new PoleVPNService();
        polevpn = PoleVPNManager.getInstance().getPoleVPN();
        polevpn.setEventHandler(poleVPNEventHandler);

        if (polevpn.getState() == Polevpnmobile.POLEVPN_MOBILE_STARTED) {

            Log.i("main","-----------------refresh ui-----------------");
            new Handler(Looper.getMainLooper()).postDelayed(()->{
                try{
                    JSONObject msg = new JSONObject();
                    msg.put("event","refresh");
                    JSONObject data = new JSONObject();
                    data.put("ip",polevpn.getLocalIP());
                    data.put("remoteIp",polevpn.getRemoteIP());
                    msg.put("data",data);
                    webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            },1000);
        }
    }

    private void initDbAndLog(){
        try {
            String path = getApplicationContext().getFilesDir().getAbsolutePath();
            Polevpnmobile.initDB(path+"/config.db");
            Polevpnmobile.setLogPath(path);

        }catch (Exception e){
            e.printStackTrace();
        }
    }

    private void initWindow(){
        Window window = this.getWindow();
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        window.setStatusBarColor(0xFFFFFF);
        window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR);

        if (getSupportActionBar() != null){
            getSupportActionBar().hide();
        }
    }
    private void initWebView(WebSettings webSettings) {
        webSettings.setJavaScriptEnabled(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setDatabaseEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAppCacheEnabled(true);
        webSettings.setSaveFormData(false);
        webSettings.setJavaScriptCanOpenWindowsAutomatically(true);
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setAllowContentAccess(true);
        webSettings.setAllowFileAccessFromFileURLs(true);
        webSettings.setAllowUniversalAccessFromFileURLs(true);
        webSettings.setSupportMultipleWindows(true);
    }

    private void Toast(String msg){
        new Handler(Looper.getMainLooper()).post(()->{
            Toast.makeText(App.getAppContext(), msg, Toast.LENGTH_SHORT).show();
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        super.onActivityResult(resultCode,resultCode,data);

        if (resultCode == Activity.RESULT_OK) {
            new Handler(Looper.getMainLooper()).post(()->{
                Intent intentStart = new Intent(App.getAppContext(), PoleVPNService.class);
                App.getAppContext().startService(intentStart);
                myVpnService.start(ip,dns,App.getAppContext().getPackageName());
                if (myVpnService.getInterface()!= null){
                    int fd = myVpnService.getInterface().detachFd();
                    polevpn.attach(fd);
                }else{
                    Log.e("main","vpn started fail");
                }

            });
            Log.i("main","vpn started successful");

        }else{
            Log.i("main","user authorize failed");
            Toast("user authorize failed");
            polevpn.stop();
        }
    }

    private PoleVPNEventHandler poleVPNEventHandler = new PoleVPNEventHandler() {
        @Override
        public void onAllocEvent(String ip, String dns) {

            try {
                Log.i("main", "vpn server allocated ip="+ip + ",dns=" + dns);
                MainActivity.this.dns = dns;
                MainActivity.this.ip = ip;
                new Handler(Looper.getMainLooper()).post(()->{
                    Intent intent = VpnService.prepare(App.getAppContext());
                    if (intent != null) {
                        startActivityForResult(intent, 0);
                    } else {
                        onActivityResult(0, Activity.RESULT_OK, null);
                    }

                    try {
                        JSONObject msg = new JSONObject();
                        msg.put("event", "allocated");
                        JSONObject data = new JSONObject();
                        data.put("ip", ip);
                        data.put("remoteIp", polevpn.getRemoteIP());
                        data.put("dns", dns);
                        msg.put("data", data);

                        webView.loadUrl("javascript:onCallback(" + msg.toString() + ")");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                });
            }catch (Throwable e){
                e.printStackTrace();
            }
        }

        @Override
        public void onErrorEvent(String type, String error) {

            new Handler(Looper.getMainLooper()).post(()->{
                try{
                    JSONObject msg = new JSONObject();
                    msg.put("event","error");
                    msg.put("data",new JSONObject().put("error",error));
                    webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onReconnectedEvent() {
            Log.i("main","vpn reconnected");
            new Handler(Looper.getMainLooper()).post(()->{
                try{
                    JSONObject msg = new JSONObject();
                    msg.put("event","reconnected");
                    webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onReconnectingEvent() {
            Log.i("main","vpn reconnecting");
            new Handler(Looper.getMainLooper()).post(()->{
                try{
                    JSONObject msg = new JSONObject();
                    msg.put("event","reconnecting");
                    webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onStartedEvent() {

            Log.i("main","vpn server connected");
            new Handler(Looper.getMainLooper()).post(()->{
                PoleVPNManager.getInstance().registerNetworkCallback();
                try{
                    JSONObject msg = new JSONObject();
                    msg.put("event","started");
                    webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                }catch (Exception e){
                    e.printStackTrace();
                }
            });
        }

        @Override
        public void onStoppedEvent() {
            try{
                Log.i("main","vpn stopped");
                new Handler(Looper.getMainLooper()).post(()->{
                    myVpnService.stop();
                    PoleVPNManager.getInstance().unregisterNetworkCallback();

                    try{
                        JSONObject msg = new JSONObject();
                        msg.put("event","stoped");
                        webView.loadUrl("javascript:onCallback("+msg.toString()+")");
                    }catch (Exception e){
                        e.printStackTrace();
                    }

                });
            }catch (Throwable e){
               e.printStackTrace();
            }
        }
    };

    @JavascriptInterface
    public String GetVersion(String req) {
        return "{\"Version\":\"1.1.1\"}";
    }

    @JavascriptInterface
    public String GetUpDownBytes(String req) {
        new Handler(Looper.getMainLooper()).post(()->{
            try {
                JSONObject msg = new JSONObject();
                msg.put("event", "bytes");
                JSONObject data = new JSONObject();
                data.put("UpBytes", polevpn.getUpBytes());
                data.put("DownBytes", polevpn.getDownBytes());
                msg.put("data", data);
                webView.loadUrl("javascript:onCallback(" + msg.toString() + ")");
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        return "{\"Msg\":\"ok\",\"Code\":0}";

    }

    @JavascriptInterface
    public String GetAllLogs(String req) {

        new Handler(Looper.getMainLooper()).post(()->{
            try {
                JSONObject msg = new JSONObject();
                msg.put("event", "logs");
                JSONObject data = new JSONObject();
                data.put("logs", Polevpnmobile.getAllLogs());
                msg.put("data", data);
                webView.loadUrl("javascript:onCallback(" + msg.toString() + ")");
            }catch (Exception e){
                e.printStackTrace();
            }
        });

        return "{\"Msg\":\"ok\",\"Code\":0}";
    }

    @JavascriptInterface
    public String GetAllAccessServer(String req) {
        return Polevpnmobile.getAllAccessServer(req);
    }

    @JavascriptInterface
    public String DeleteAccessServer(String req) {
        return Polevpnmobile.deleteAccessServer(req);
    }

    @JavascriptInterface
    public String UpdateAccessServer(String req) {
        return Polevpnmobile.updateAccessServer(req);
    }

    @JavascriptInterface
    public String AddAccessServer(String req) {
        return Polevpnmobile.addAccessServer(req);
    }

    @JavascriptInterface
    public String StopAccessServer(String req) {
        polevpn.stop();
        return "{\"Msg\":\"ok\",\"Code\":0}";
    }

    @JavascriptInterface
    public String ConnectAccessServer(String req) {
        try{
            JSONObject obj = new JSONObject(req);
            String endpoint = obj.getString("Endpoint");
            String user = obj.getString("User");
            String password = obj.getString("Password");
            String sni = obj.getString("Sni");

            polevpn.start(endpoint,user,password,sni);

        }catch (Exception e){
            e.printStackTrace();
        }
        return "{\"Msg\":\"ok\",\"Code\":0}";
    }
}