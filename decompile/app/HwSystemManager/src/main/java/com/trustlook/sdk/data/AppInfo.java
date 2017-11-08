package com.trustlook.sdk.data;

public class AppInfo implements Comparable<AppInfo> {
    private String a;
    private int b;
    private String c;
    private String d;
    private String e;
    private String f;
    private long g;

    public AppInfo(String str) {
        this(str, "");
    }

    public AppInfo(String str, String str2) {
        this.d = str;
        this.a = str2;
    }

    public String getPackageName() {
        return this.d;
    }

    public void setPackageName(String str) {
        this.d = str;
    }

    public String getMd5() {
        return this.a;
    }

    public void setMd5(String str) {
        this.a = str;
    }

    public String getApkPath() {
        return this.c;
    }

    public void setApkPath(String str) {
        this.c = str;
    }

    public long getSizeInBytes() {
        return this.g;
    }

    public void setSizeInBytes(long j) {
        this.g = j;
    }

    public int getScore() {
        return this.b;
    }

    public void setScore(int i) {
        this.b = i;
    }

    public String getVirusNameInCloud() {
        return this.f;
    }

    public void setVirusNameInCloud(String str) {
        this.f = str;
    }

    public String getCategory() {
        return this.e;
    }

    public void setCategory(String str) {
        this.e = str;
    }

    public int compareTo(AppInfo appInfo) {
        return this != appInfo ? appInfo.b - this.b : 0;
    }

    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (!(obj instanceof AppInfo)) {
            return false;
        }
        return this.d.equals(((AppInfo) obj).d);
    }
}
