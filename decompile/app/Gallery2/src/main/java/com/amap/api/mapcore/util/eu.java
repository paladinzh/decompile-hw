package com.amap.api.mapcore.util;

import android.content.Context;
import com.amap.api.maps.model.LatLng;
import com.amap.api.trace.TraceLocation;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: TraceHandler */
public class eu extends es<List<TraceLocation>, List<LatLng>> {
    private List<TraceLocation> i;

    protected /* synthetic */ Object a(String str) throws eq {
        return b(str);
    }

    public eu(Context context, List<TraceLocation> list, int i) {
        super(context, list);
        this.i = list;
    }

    protected String f() {
        JSONArray jSONArray = new JSONArray();
        long j = 0;
        for (int i = 0; i < this.i.size(); i++) {
            TraceLocation traceLocation = (TraceLocation) this.i.get(i);
            JSONObject jSONObject = new JSONObject();
            try {
                jSONObject.put("x", traceLocation.getLongitude());
                jSONObject.put("y", traceLocation.getLatitude());
                jSONObject.put("ag", (int) traceLocation.getBearing());
                long time = traceLocation.getTime();
                if (i != 0) {
                    if (time != 0) {
                        if ((time - j > 0 ? 1 : null) != null) {
                            jSONObject.put("tm", (time - j) / 1000);
                            j = time;
                        }
                    }
                    jSONObject.put("tm", 1);
                    j = time;
                } else {
                    if (time == 0) {
                        time = (System.currentTimeMillis() - 10000) / 1000;
                    }
                    jSONObject.put("tm", time / 1000);
                    j = time;
                }
                jSONObject.put("sp", (int) traceLocation.getSpeed());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            jSONArray.put(jSONObject);
        }
        this.e = c() + "&" + jSONArray.toString();
        return jSONArray.toString();
    }

    protected List<LatLng> b(String str) throws eq {
        List<LatLng> arrayList = new ArrayList();
        try {
            JSONArray optJSONArray = new JSONObject(str).optJSONArray("points");
            if (optJSONArray == null || optJSONArray.length() == 0) {
                return arrayList;
            }
            for (int i = 0; i < optJSONArray.length(); i++) {
                JSONObject optJSONObject = optJSONArray.optJSONObject(i);
                arrayList.add(new LatLng(Double.parseDouble(optJSONObject.optString("y")), Double.parseDouble(optJSONObject.optString("x"))));
            }
            return arrayList;
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (Throwable th) {
            th.printStackTrace();
        }
    }
}
