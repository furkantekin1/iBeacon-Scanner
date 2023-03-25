package com.furkotek.ibeacon;

public class BeaconItem {
    private String uuid;
    private int major,minor;

    public String getUuid() {
        return uuid;
    }

    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    public int getMajor() {
        return major;
    }

    public void setMajor(int major) {
        this.major = major;
    }

    public int getMinor() {
        return minor;
    }

    public void setMinor(int minor) {
        this.minor = minor;
    }

    public BeaconItem(String uuid, int major, int minor) {
        this.uuid = uuid;
        this.major = major;
        this.minor = minor;

    }


}