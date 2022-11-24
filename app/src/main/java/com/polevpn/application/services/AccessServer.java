package com.polevpn.application.services;

import java.util.List;

public class AccessServer {
    public String endpoint;
    public String user;
    public String password;
    public String sni;
    public boolean useRemoteRouteRules;
    public boolean skipSSLVerify;
    public List<String> localRouteRules;
}
