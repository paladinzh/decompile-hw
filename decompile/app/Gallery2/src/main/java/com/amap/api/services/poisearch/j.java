package com.amap.api.services.poisearch;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.core.c;
import com.amap.api.services.core.d;
import com.amap.api.services.core.s;
import com.amap.api.services.core.w;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: PoiSearchKeywordsHandler */
class j extends g<s, ArrayList<PoiItem>> {
    private int h = 1;
    private int i = 20;
    private int j = 0;
    private List<String> k = new ArrayList();
    private List<SuggestionCity> l = new ArrayList();

    public /* synthetic */ Object b(String str) throws AMapException {
        return e(str);
    }

    public j(Context context, s sVar) {
        super(context, sVar);
    }

    public void a(int i) {
        this.h = i + 1;
    }

    public void b(int i) {
        int i2;
        if (i <= 30) {
            i2 = i;
        } else {
            i2 = 30;
        }
        if (i2 <= 0) {
            i2 = 30;
        }
        this.i = i2;
    }

    public int f() {
        return this.i;
    }

    public int i() {
        return this.j;
    }

    public Query j() {
        return ((s) this.a).a;
    }

    public SearchBound k() {
        return ((s) this.a).b;
    }

    public List<String> l() {
        return this.k;
    }

    public List<SuggestionCity> m() {
        return this.l;
    }

    public String b() {
        String str = c.a() + "/place";
        if (((s) this.a).b == null) {
            return str + "/text?";
        }
        String str2;
        if (((s) this.a).b.getShape().equals(SearchBound.BOUND_SHAPE)) {
            str2 = str + "/around?";
        } else if (((s) this.a).b.getShape().equals(SearchBound.RECTANGLE_SHAPE) || ((s) this.a).b.getShape().equals(SearchBound.POLYGON_SHAPE)) {
            str2 = str + "/polygon?";
        } else {
            str2 = str;
        }
        return str2;
    }

    public ArrayList<PoiItem> e(String str) throws AMapException {
        ArrayList<PoiItem> arrayList = new ArrayList();
        if (str == null) {
            return arrayList;
        }
        try {
            JSONObject jSONObject = new JSONObject(str);
            this.j = jSONObject.optInt("count");
            arrayList = com.amap.api.services.core.j.c(jSONObject);
            if (!jSONObject.has("suggestion")) {
                return arrayList;
            }
            jSONObject = jSONObject.optJSONObject("suggestion");
            if (jSONObject == null) {
                return arrayList;
            }
            this.l = com.amap.api.services.core.j.a(jSONObject);
            this.k = com.amap.api.services.core.j.b(jSONObject);
            return arrayList;
        } catch (Throwable e) {
            d.a(e, "PoiSearchKeywordHandler", "paseJSONJSONException");
        } catch (Throwable e2) {
            d.a(e2, "PoiSearchKeywordHandler", "paseJSONException");
        }
    }

    protected String a_() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("output=json");
        if (((s) this.a).b != null) {
            double a;
            if (((s) this.a).b.getShape().equals(SearchBound.BOUND_SHAPE)) {
                a = d.a(((s) this.a).b.getCenter().getLongitude());
                stringBuilder.append("&location=").append(a + "," + d.a(((s) this.a).b.getCenter().getLatitude()));
                stringBuilder.append("&radius=").append(((s) this.a).b.getRange());
                stringBuilder.append("&sortrule=").append(n());
            } else if (((s) this.a).b.getShape().equals(SearchBound.RECTANGLE_SHAPE)) {
                LatLonPoint lowerLeft = ((s) this.a).b.getLowerLeft();
                LatLonPoint upperRight = ((s) this.a).b.getUpperRight();
                double a2 = d.a(lowerLeft.getLatitude());
                a = d.a(lowerLeft.getLongitude());
                stringBuilder.append("&polygon=" + a + "," + a2 + ";" + d.a(upperRight.getLongitude()) + "," + d.a(upperRight.getLatitude()));
            } else if (((s) this.a).b.getShape().equals(SearchBound.POLYGON_SHAPE)) {
                List polyGonList = ((s) this.a).b.getPolyGonList();
                if (polyGonList != null && polyGonList.size() > 0) {
                    stringBuilder.append("&polygon=" + d.a(polyGonList));
                }
            }
        }
        String city = ((s) this.a).a.getCity();
        if (!a(city)) {
            stringBuilder.append("&city=").append(c(city));
        }
        if (!d.a(o())) {
            stringBuilder.append(o());
        }
        stringBuilder.append("&keywords=" + c(((s) this.a).a.getQueryString()));
        stringBuilder.append("&language=").append(c.b());
        stringBuilder.append("&offset=" + this.i);
        stringBuilder.append("&page=" + this.h);
        stringBuilder.append("&types=" + c(((s) this.a).a.getCategory()));
        stringBuilder.append("&extensions=all");
        stringBuilder.append("&key=" + w.f(this.d));
        return stringBuilder.toString();
    }

    private String n() {
        if (((s) this.a).b.isDistanceSort()) {
            return "distance";
        }
        return "weight";
    }

    private String o() {
        StringBuffer stringBuffer = new StringBuffer();
        if (((s) this.a).a.hasGroupBuyLimit() && ((s) this.a).a.hasDiscountLimit()) {
            stringBuffer.append("&filter=groupbuy:1|discount:1");
            return stringBuffer.toString();
        }
        if (((s) this.a).a.hasGroupBuyLimit()) {
            stringBuffer.append("&filter=");
            stringBuffer.append("groupbuy:1");
        }
        if (((s) this.a).a.hasDiscountLimit()) {
            stringBuffer.append("&filter=");
            stringBuffer.append("discount:1");
        }
        return stringBuffer.toString();
    }
}
