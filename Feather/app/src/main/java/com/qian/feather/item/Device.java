package com.qian.feather.item;

public class Device {
    private String name;
    private String ip;
    private PLATFORM platform;
    private boolean isAvailable;
    private boolean latelyUsed;
    public Device() {}
    public Device(String name,String ip) {
        this.name = name;
        this.ip = ip;
    }
    public Device(String name,String ip,PLATFORM platform,boolean isAvailable,boolean latelyUsed) {
        this.name = name;
        this.ip = ip;
        this.platform = platform;
        this.isAvailable = isAvailable;
        this.latelyUsed = latelyUsed;
    }
    enum PLATFORM {
        ANDROID,
        Windows,
        MacOS
    }

    public String getName() {
        return name;
    }

    public String getIp() {
        return ip;
    }
}
