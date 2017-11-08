package com.huawei.systemmanager.util.numberlocation;

import android.text.TextUtils;

public class NumberLocationInfo {
    private String mLocation;
    private String mOperator;

    public NumberLocationInfo() {
        this.mLocation = "";
        this.mOperator = "";
        this.mLocation = "";
        this.mOperator = "";
    }

    public NumberLocationInfo(String location, String operator) {
        this.mLocation = "";
        this.mOperator = "";
        this.mLocation = location;
        if (this.mLocation == null) {
            this.mLocation = "";
        }
        this.mOperator = operator;
        if (this.mOperator == null) {
            this.mOperator = "";
        }
    }

    public String getGeoLocation() {
        String geoLocation = "";
        if (!TextUtils.isEmpty(this.mLocation)) {
            geoLocation = geoLocation + this.mLocation;
        }
        if (TextUtils.isEmpty(this.mOperator)) {
            return geoLocation;
        }
        if (!TextUtils.isEmpty(geoLocation)) {
            geoLocation = geoLocation + " ";
        }
        return geoLocation + this.mOperator;
    }

    public String getLocation() {
        return this.mLocation;
    }

    public void setLocation(String location) {
        this.mLocation = location;
        if (this.mLocation == null) {
            this.mLocation = "";
        }
    }

    public String getOperator() {
        return this.mOperator;
    }

    public void setOperator(String operator) {
        this.mOperator = operator;
        if (this.mOperator == null) {
            this.mOperator = "";
        }
    }
}
