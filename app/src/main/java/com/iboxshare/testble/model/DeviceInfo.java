package com.iboxshare.testble.model;

/**
 * Created by KN on 16/8/8.
 */
public class DeviceInfo {
    private String name,mac;
    private int signal;

    public DeviceInfo(){
        //...
    }
    public DeviceInfo(String name,String mac,int signal){
        this.name = name;
        this.mac = mac;
        this.signal = signal;
    }
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMac() {
        return mac;
    }

    public void setMac(String mac) {
        this.mac = mac;
    }

    public int getSignal() {
        return signal;
    }

    public void setSignal(int signal) {
        this.signal = signal;
    }
}
