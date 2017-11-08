package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.nearby.UploadInfo;

/* compiled from: NearbyUpdateHandler */
public class w extends b<UploadInfo, Integer> {
    private Context h;
    private UploadInfo i;

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public w(Context context, UploadInfo uploadInfo) {
        super(context, uploadInfo);
        this.h = context;
        this.i = uploadInfo;
    }

    protected String e() {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append("key=").append(aj.f(this.h));
        stringBuffer.append("&userid=").append(this.i.getUserID());
        LatLonPoint point = this.i.getPoint();
        stringBuffer.append("&location=").append(((float) ((int) (point.getLongitude() * 1000000.0d))) / 1000000.0f).append(",").append(((float) ((int) (point.getLatitude() * 1000000.0d))) / 1000000.0f);
        stringBuffer.append("&coordtype=").append(this.i.getCoordType());
        return stringBuffer.toString();
    }

    protected Integer d(String str) throws AMapException {
        return Integer.valueOf(0);
    }

    public String g() {
        return h.b() + "/nearby/data/create";
    }
}
