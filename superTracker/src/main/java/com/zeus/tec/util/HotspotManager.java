package com.zeus.tec.util;
import android.net.ConnectivityManager;
import android.content.Context;
import android.net.LinkAddress;
import android.net.NetworkCapabilities;

import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.net.LinkProperties;
import android.os.Build;

import java.net.InetAddress;

public class HotspotManager {
    private WifiManager wifiManager;

    public HotspotManager(Context context){
        wifiManager = (WifiManager) context.getApplicationContext().getSystemService(context.WIFI_SERVICE);
    }

    public void setHotspotIp(String ipAddress,String ssid,String password){
        try {
            //创建wificonfigurarion对象
            WifiConfiguration wifiConfiguration = new WifiConfiguration();
            wifiConfiguration.SSID = "\""+ssid+"\"";
            wifiConfiguration.preSharedKey="\""+password+"\"";
            wifiConfiguration.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);

            //关注此热点IP地址
            if (Build.VERSION.SDK_INT>=Build.VERSION_CODES.O){
             //   NetworkCapabilities nc = wifiManager
                if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.Q) {

                    LinkProperties linkProperties = new LinkProperties();
                  //  linkProperties.setLinkAddresses(new LinkAddress(InetAddress.getByName(ipAddress),24));
                }
            }
        }catch (Exception exception){

        }
    }
}
