package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.busline.BusLineQuery;
import com.amap.api.services.busline.BusLineQuery.SearchType;
import com.amap.api.services.busline.BusStationQuery;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: BusSearchServerHandler */
public class d<T> extends b<T, ArrayList<?>> {
    private int h = 0;
    private List<String> i = new ArrayList();
    private List<SuggestionCity> j = new ArrayList();

    protected /* synthetic */ Object a(String str) throws AMapException {
        return d(str);
    }

    public d(Context context, T t) {
        super(context, t);
    }

    public T h() {
        return this.a;
    }

    public int i() {
        return this.h;
    }

    public String g() {
        String str = !(this.a instanceof BusLineQuery) ? "stopname" : ((BusLineQuery) this.a).getCategory() != SearchType.BY_LINE_ID ? ((BusLineQuery) this.a).getCategory() != SearchType.BY_LINE_NAME ? "" : "linename" : "lineid";
        return h.a() + "/bus/" + str + "?";
    }

    protected ArrayList<?> d(String str) throws AMapException {
        try {
            JSONObject jSONObject = new JSONObject(str);
            JSONObject optJSONObject = jSONObject.optJSONObject("suggestion");
            if (optJSONObject != null) {
                this.j = n.a(optJSONObject);
                this.i = n.b(optJSONObject);
            }
            this.h = jSONObject.optInt("count");
            if (this.a instanceof BusLineQuery) {
                return n.i(jSONObject);
            }
            return n.e(jSONObject);
        } catch (Throwable e) {
            i.a(e, "BusSearchServerHandler", "paseJSON");
            return null;
        }
    }

    public List<String> j() {
        return this.i;
    }

    public List<SuggestionCity> k() {
        return this.j;
    }

    protected String e() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("output=json");
        String city;
        if (this.a instanceof BusLineQuery) {
            BusLineQuery busLineQuery = (BusLineQuery) this.a;
            stringBuilder.append("&extensions=all");
            if (busLineQuery.getCategory() != SearchType.BY_LINE_ID) {
                city = busLineQuery.getCity();
                if (!n.i(city)) {
                    stringBuilder.append("&city=").append(b(city));
                }
                stringBuilder.append("&keywords=" + b(busLineQuery.getQueryString()));
                stringBuilder.append("&offset=" + busLineQuery.getPageSize());
                stringBuilder.append("&page=" + (busLineQuery.getPageNumber() + 1));
            } else {
                stringBuilder.append("&id=").append(b(((BusLineQuery) this.a).getQueryString()));
            }
        } else {
            BusStationQuery busStationQuery = (BusStationQuery) this.a;
            city = busStationQuery.getCity();
            if (!n.i(city)) {
                stringBuilder.append("&city=").append(b(city));
            }
            stringBuilder.append("&keywords=" + b(busStationQuery.getQueryString()));
            stringBuilder.append("&offset=" + busStationQuery.getPageSize());
            stringBuilder.append("&page=" + (busStationQuery.getPageNumber() + 1));
        }
        stringBuilder.append("&key=" + aj.f(this.d));
        return stringBuilder.toString();
    }
}
