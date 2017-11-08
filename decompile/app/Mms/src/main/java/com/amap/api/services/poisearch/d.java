package com.amap.api.services.poisearch;

import android.content.Context;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.PoiItem;
import com.amap.api.services.core.SuggestionCity;
import com.amap.api.services.core.aa;
import com.amap.api.services.core.aj;
import com.amap.api.services.core.h;
import com.amap.api.services.core.i;
import com.amap.api.services.core.n;
import com.amap.api.services.poisearch.PoiSearch.Query;
import com.amap.api.services.poisearch.PoiSearch.SearchBound;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONObject;

/* compiled from: PoiSearchKeywordsHandler */
class d extends b<aa, ArrayList<PoiItem>> {
    private int h = 1;
    private int i = 20;
    private int j = 0;
    private List<String> k = new ArrayList();
    private List<SuggestionCity> l = new ArrayList();

    public /* synthetic */ Object a(String str) throws AMapException {
        return e(str);
    }

    public d(Context context, aa aaVar) {
        super(context, aaVar);
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

    public int h() {
        return this.i;
    }

    public int i() {
        return this.j;
    }

    public Query j() {
        return ((aa) this.a).a;
    }

    public SearchBound k() {
        return ((aa) this.a).b;
    }

    public List<String> l() {
        return this.k;
    }

    public List<SuggestionCity> m() {
        return this.l;
    }

    public String g() {
        String str = h.a() + "/place";
        if (((aa) this.a).b == null) {
            return str + "/text?";
        }
        String str2;
        if (((aa) this.a).b.getShape().equals("Bound")) {
            str2 = str + "/around?";
        } else if (((aa) this.a).b.getShape().equals("Rectangle") || ((aa) this.a).b.getShape().equals("Polygon")) {
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
            arrayList = n.c(jSONObject);
            if (!jSONObject.has("suggestion")) {
                return arrayList;
            }
            jSONObject = jSONObject.optJSONObject("suggestion");
            if (jSONObject == null) {
                return arrayList;
            }
            this.l = n.a(jSONObject);
            this.k = n.b(jSONObject);
            return arrayList;
        } catch (Throwable e) {
            i.a(e, "PoiSearchKeywordHandler", "paseJSONJSONException");
        } catch (Throwable e2) {
            i.a(e2, "PoiSearchKeywordHandler", "paseJSONException");
        }
    }

    protected String e() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("output=json");
        if (((aa) this.a).b != null) {
            double a;
            if (((aa) this.a).b.getShape().equals("Bound")) {
                a = i.a(((aa) this.a).b.getCenter().getLongitude());
                stringBuilder.append("&location=").append(a + "," + i.a(((aa) this.a).b.getCenter().getLatitude()));
                stringBuilder.append("&radius=").append(((aa) this.a).b.getRange());
                stringBuilder.append("&sortrule=").append(p());
            } else if (((aa) this.a).b.getShape().equals("Rectangle")) {
                LatLonPoint lowerLeft = ((aa) this.a).b.getLowerLeft();
                LatLonPoint upperRight = ((aa) this.a).b.getUpperRight();
                double a2 = i.a(lowerLeft.getLatitude());
                a = i.a(lowerLeft.getLongitude());
                stringBuilder.append("&polygon=" + a + "," + a2 + ";" + i.a(upperRight.getLongitude()) + "," + i.a(upperRight.getLatitude()));
            } else if (((aa) this.a).b.getShape().equals("Polygon")) {
                List polyGonList = ((aa) this.a).b.getPolyGonList();
                if (polyGonList != null && polyGonList.size() > 0) {
                    stringBuilder.append("&polygon=" + i.a(polyGonList));
                }
            }
        }
        String city = ((aa) this.a).a.getCity();
        if (!d(city)) {
            stringBuilder.append("&city=").append(b(city));
        }
        stringBuilder.append("&keywords=" + b(((aa) this.a).a.getQueryString()));
        stringBuilder.append("&language=").append(h.c());
        stringBuilder.append("&offset=" + this.i);
        stringBuilder.append("&page=" + this.h);
        stringBuilder.append("&types=" + b(((aa) this.a).a.getCategory()));
        stringBuilder.append("&extensions=all");
        stringBuilder.append("&key=" + aj.f(this.d));
        if (((aa) this.a).a.getCityLimit()) {
            stringBuilder.append("&citylimit=true");
        } else {
            stringBuilder.append("&citylimit=false");
        }
        if (((aa) this.a).a.isRequireSubPois()) {
            stringBuilder.append("&children=1");
        } else {
            stringBuilder.append("&children=0");
        }
        return stringBuilder.toString();
    }

    private String p() {
        if (((aa) this.a).b.isDistanceSort()) {
            return "distance";
        }
        return "weight";
    }
}
