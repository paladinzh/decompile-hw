package com.amap.api.location;

import com.loc.e;

public class AMapLocationClientOption implements Cloneable {
    private long a = 2000;
    private long b = ((long) e.j);
    private boolean c = false;
    private boolean d = false;
    private boolean e = true;
    private boolean f = true;
    private AMapLocationMode g = AMapLocationMode.Hight_Accuracy;
    private boolean h = false;
    private boolean i = false;
    private boolean j = true;

    public enum AMapLocationMode {
        Battery_Saving,
        Device_Sensors,
        Hight_Accuracy
    }

    private AMapLocationClientOption a(AMapLocationClientOption aMapLocationClientOption) {
        this.a = aMapLocationClientOption.a;
        this.c = aMapLocationClientOption.c;
        this.g = aMapLocationClientOption.g;
        this.d = aMapLocationClientOption.d;
        this.h = aMapLocationClientOption.h;
        this.i = aMapLocationClientOption.i;
        this.e = aMapLocationClientOption.e;
        this.f = aMapLocationClientOption.f;
        this.b = aMapLocationClientOption.b;
        return this;
    }

    public AMapLocationClientOption clone() {
        return new AMapLocationClientOption().a(this);
    }

    public long getHttpTimeOut() {
        return this.b;
    }

    public long getInterval() {
        return this.a;
    }

    public AMapLocationMode getLocationMode() {
        return this.g;
    }

    public boolean isGpsFirst() {
        return this.i;
    }

    public boolean isKillProcess() {
        return this.h;
    }

    public boolean isMockEnable() {
        return this.d;
    }

    public boolean isNeedAddress() {
        return this.e;
    }

    public boolean isOffset() {
        return this.j;
    }

    public boolean isOnceLocation() {
        return this.c;
    }

    public boolean isWifiActiveScan() {
        return this.f;
    }

    public AMapLocationClientOption setGpsFirst(boolean z) {
        this.i = z;
        return this;
    }

    public void setHttpTimeOut(long j) {
        this.b = j;
    }

    public AMapLocationClientOption setInterval(long j) {
        if ((j >= 1000 ? 1 : null) == null) {
            j = 1000;
        }
        this.a = j;
        return this;
    }

    public AMapLocationClientOption setKillProcess(boolean z) {
        this.h = z;
        return this;
    }

    public AMapLocationClientOption setLocationMode(AMapLocationMode aMapLocationMode) {
        this.g = aMapLocationMode;
        return this;
    }

    public void setMockEnable(boolean z) {
        this.d = z;
    }

    public AMapLocationClientOption setNeedAddress(boolean z) {
        this.e = z;
        return this;
    }

    public AMapLocationClientOption setOffset(boolean z) {
        this.j = z;
        return this;
    }

    public AMapLocationClientOption setOnceLocation(boolean z) {
        this.c = z;
        return this;
    }

    public void setWifiActiveScan(boolean z) {
        this.f = z;
    }
}
