package com.polevpn.application.data;

import android.os.Bundle;

import com.polevpn.application.services.PoleVPNManager;
import com.polevpn.application.tools.SharePref;

import polevpnmobile.Polevpnmobile;

public class Node {

    private static boolean loaded;

    public static  void loadAllNodes(boolean force) {

        if (!SharePref.getInstance().getBoolean("login")){
            return;
        }

        if(loaded && !force){
            return;
        }

        String apiHost =  SharePref.getInstance().getString("api_host");
        String header = "{\"X-Token\":\"" + SharePref.getInstance().getString("token") + "\"}";

        Polevpnmobile.api(apiHost,"/api/node/all", header, "", (ret, msg, resp) -> {
            SharePref.getInstance().setString("all_nodes",resp);
            Bundle bundle = new Bundle();
            bundle.putString("type","load_all_nodes");
            PoleVPNManager.getInstance().sendMessage("home",bundle);
            loaded = true;
        });
    }
}
