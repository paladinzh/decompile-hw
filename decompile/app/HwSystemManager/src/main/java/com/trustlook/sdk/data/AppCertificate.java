package com.trustlook.sdk.data;

import com.huawei.systemmanager.comm.database.IDatabaseConst.SqlMarker;
import org.json.JSONException;
import org.json.JSONObject;

@Deprecated
public class AppCertificate {
    private String a;
    private long b;
    private long c;
    private String d;

    public String getPemIssuer() {
        return this.a;
    }

    public void setPemIssuer(String str) {
        this.a = str;
    }

    public long getPemStartDate() {
        return this.b;
    }

    public void setPemStartDate(long j) {
        this.b = j;
    }

    public long getPemExpiredDate() {
        return this.c;
    }

    public void setPemExpiredDate(long j) {
        this.c = j;
    }

    public String getPemSerialNumber() {
        return this.d;
    }

    public void setPemSerialNumber(String str) {
        this.d = str;
    }

    public String toString() {
        return "{\"pem_issuer\":\"" + this.a + "\",\"pem_serial\":" + SqlMarker.QUOTATION + this.d + "\",\"pem_start\":" + this.b + ",\"pem_expire\":" + this.c + "}";
    }

    public JSONObject getJSONObject() {
        JSONObject jSONObject = new JSONObject();
        try {
            jSONObject.put("pem_issuer", this.a);
            jSONObject.put("pem_serial", this.d);
            jSONObject.put("pem_start", this.b);
            jSONObject.put("pem_expire", this.c);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jSONObject;
    }
}
