package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusStationQuery;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: BusSearchServerHandler */
public class b<T> extends r<T, ArrayList<?>> {
    private int h = 0;
    private List<String> i = new ArrayList();
    private List<SuggestionCity> j = new ArrayList();

    protected /* synthetic */ Object b(String str) throws AMapException {
        return a(str);
    }

    public b(Context context, T t) {
        super(context, t);
    }

    public T c() {
        return this.a;
    }

    public int d() {
        return this.h;
    }

    public String b() {
        String str = !(this.a instanceof BusLineQuery) ? "stopname" : ((BusLineQuery) this.a).getCategory() != SearchType.BY_LINE_ID ? ((BusLineQuery) this.a).getCategory() != SearchType.BY_LINE_NAME ? "" : "linename" : "lineid";
        return c.a() + "/bus/" + str + "?";
    }

    protected ArrayList<?> a(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONObject optJSONObject = jSONObject.optJSONObject("suggestion");
            if (optJSONObject != null) {
                this.j = j.a(optJSONObject);
                this.i = j.b(optJSONObject);
            }
            this.h = jSONObject.optInt("count");
            if (this.a instanceof BusLineQuery) {
                return j.i(jSONObject);
            }
            return j.e(jSONObject);
        } catch (Throwable e) {
            d.a(e, "BusSearchServerHandler", "paseJSON");
            return null;
        }
    }

    public List<String> b_() {
        return this.i;
    }

    public List<SuggestionCity> f() {
        return this.j;
    }

    protected String a_() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("output=json");
        String city;
        if (this.a instanceof BusLineQuery) {
            BusLineQuery busLineQuery = (BusLineQuery) this.a;
            stringBuilder.append("&extensions=all");
            if (busLineQuery.getCategory() != SearchType.BY_LINE_ID) {
                city = busLineQuery.getCity();
                if (!j.h(city)) {
                    stringBuilder.append("&city=").append(c(city));
                }
                stringBuilder.append("&keywords=" + c(busLineQuery.getQueryString()));
                stringBuilder.append("&offset=" + busLineQuery.getPageSize());
                stringBuilder.append("&page=" + (busLineQuery.getPageNumber() + 1));
            } else {
                stringBuilder.append("&id=").append(c(((BusLineQuery) this.a).getQueryString()));
            }
        } else {
            BusStationQuery busStationQuery = (BusStationQuery) this.a;
            city = busStationQuery.getCity();
            if (!j.h(city)) {
                stringBuilder.append("&city=").append(c(city));
            }
            stringBuilder.append("&keywords=" + c(busStationQuery.getQueryString()));
            stringBuilder.append("&offset=" + busStationQuery.getPageSize());
            stringBuilder.append("&page=" + (busStationQuery.getPageNumber() + 1));
        }
        stringBuilder.append("&key=" + w.f(this.d));
        return stringBuilder.toString();
    }
}
