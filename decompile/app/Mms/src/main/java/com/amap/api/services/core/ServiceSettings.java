package com.amap.api.services.core;

public class ServiceSettings {
    public static final String CHINESE = "zh-CN";
    public static final String ENGLISH = "en";
    public static final int HTTP = 1;
    public static final int HTTPS = 2;
    private static ServiceSettings c;
    private String a = "zh-CN";
    private int b = 1;
    private int d = 20000;
    private int e = 20000;

    public int getConnectionTimeOut() {
        return this.d;
    }

    public int getSoTimeOut() {
        return this.e;
    }

    public void setConnectionTimeOut(int i) {
        if (i < 5000) {
            this.d = 5000;
        } else if (i <= 30000) {
            this.d = i;
        } else {
            this.d = 30000;
        }
    }

    public void setSoTimeOut(int i) {
        if (i < 5000) {
            this.e = 5000;
        } else if (i <= 30000) {
            this.e = i;
        } else {
            this.e = 30000;
        }
    }

    private ServiceSettings() {
    }

    public static ServiceSettings getInstance() {
        if (c == null) {
            c = new ServiceSettings();
        }
        return c;
    }

    public void setLanguage(String str) {
        if ("en".equals(str) || "zh-CN".equals(str)) {
            this.a = str;
        }
    }

    public void setProtocol(int i) {
        this.b = i;
    }

    public String getLanguage() {
        return this.a;
    }

    public int getProtocol() {
        return this.b;
    }

    public void setApiKey(String str) {
        ak.a(str);
    }
}
