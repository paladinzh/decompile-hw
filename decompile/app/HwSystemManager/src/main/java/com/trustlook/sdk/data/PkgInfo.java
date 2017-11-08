package com.trustlook.sdk.data;

import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import org.json.JSONException;
import org.json.JSONObject;

public class PkgInfo {
    String a;
    String b;
    String c;
    long d;
    String e;

    public String getPkgName() {
        return this.a;
    }

    public void setPkgName(String str) {
        this.a = str;
    }

    public String getPkgPath() {
        return this.b;
    }

    public void setPkgPath(String str) {
        this.b = str;
    }

    public String getMd5() {
        return this.c;
    }

    public void setMd5(String str) {
        this.c = str;
    }

    public long getPkgSize() {
        return this.d;
    }

    public void setPkgSize(long j) {
        this.d = j;
    }

    public String getPkgSource() {
        return this.e;
    }

    public void setPkgSource(String str) {
        this.e = str;
    }

    public PkgInfo(String str) {
        this.a = str;
    }

    public JSONObject toJSON() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("packageName", this.a);
            jSONObject.put("MD5", this.c);
            jSONObject.put("size", this.d);
            jSONObject.put(SecurityThreatsConst.CHECK_UNINSTALL_PKG_SOURCE, this.e);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jSONObject;
    }
}
