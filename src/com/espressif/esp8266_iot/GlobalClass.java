/**
 * Created by gerry on 19.07.15.
 */
package com.espressif.esp8266_iot;

import android.app.Application;
import android.content.Context;
import android.net.wifi.WifiManager;

public class GlobalClass extends Application {

    private String apSsid = "";
    private String apPassword = "";
    private String apBssid = "";
    private Boolean isSsidHidden = false;
    private String isSsidHiddenStr = "NO";
    private String taskResultCountStr = "1";
    private Integer taskResultCount = 0;
    private String mqttIpStr = "192.168.178.5";
    private String mqttPortStr = "1883";
    private String mqttUserStr = "";
    private String mqttPassStr = "";
    private Boolean mqttSSLBool = false;

    private int mTargetPort = 18666;
    private int mRemotePort = 18566;

    private String upgradeIpStr = "84.200.150.145";
    private String upgradePortStr = "88";

    private String ImpulsLengthStr = "1000";

    /**
     * *******************************************************************************
     */
    public String get_apSsid() { return apSsid; }
    public void set_apSsid(String Value) { apSsid = Value; }

    /**
     * *******************************************************************************
     */
    public String get_apPassword() {
        return apPassword;
    }
    public void set_apPassword(String Value) {
        apPassword = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_apBssid() {
        return apBssid;
    }
    public void set_apBssid(String Value) {
        apBssid = Value;
    }

    /**
     * *******************************************************************************
     */
    public Boolean get_isSsidHidden() {
        return isSsidHidden;
    }
    public void set_isSsidHidden(Boolean Value) {
        isSsidHidden = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_isSsidHiddenStr() {
        return isSsidHiddenStr;
    }
    public void set_isSsidHiddenStr(String Value) {
        isSsidHiddenStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public Integer get_taskResultCount() {
        return taskResultCount;
    }
    public void set_taskResultCount(Integer Value) {
        taskResultCount = Value;
    }
    /**
     * *******************************************************************************
     */
    public String get_taskResultCountStr() {
        return taskResultCountStr;
    }
    public void set_taskResultCountStr(String Value) {
        taskResultCountStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_mqttIp() {
        return mqttIpStr;
    }
    public void set_mqttIp(String Value) {
        mqttIpStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_mqttPort() {
        return mqttPortStr;
    }
    public void set_mqttPort(String Value) {
        mqttPortStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_mqttUser() {
        return mqttUserStr;
    }
    public void set_mqttUser(String Value) {
        mqttUserStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_mqttPass() { return mqttPassStr; }
    public void set_mqttPass(String Value) { mqttPassStr = Value; }
    /**
     * *******************************************************************************
     */
    public Boolean get_mqttSSL() { return mqttSSLBool; }
    public void set_mqttSSL(Boolean Value) { mqttSSLBool = Value;}

    /**
     * *******************************************************************************
     */
    public int get_TargetPort() { return mTargetPort; }

    /**
     * *******************************************************************************
     */
    public int get_RemotePort() { return mRemotePort; }

    /**
     * *******************************************************************************
     */
    public String get_upgradeIp() {
        return upgradeIpStr;
    }
    public void set_upgradeIp(String Value) {
        upgradeIpStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_upgradePort() {
        return upgradePortStr;
    }
    public void set_upgradePort(String Value) {
        upgradePortStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_ImpulsLengthStr() {
        return ImpulsLengthStr;
    }
    public void set_ImpulsLengthStr(String Value) {
        ImpulsLengthStr = Value;
    }

    /**
     * *******************************************************************************
     */
    public String get_LocalBroadcastAddress(Context context) {
        WifiManager wifi;
        wifi = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
        int ipAddress = wifi.getConnectionInfo().getIpAddress();
        String ipAddress_str = String.format("%d.%d.%d.%d",
                (ipAddress & 0xff),
                (ipAddress >> 8 & 0xff),
                (ipAddress >> 16 & 0xff),
                (255));
        return ipAddress_str;
    }
/***********************************************************************************/

}