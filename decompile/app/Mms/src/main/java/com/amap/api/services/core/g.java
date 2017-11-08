package com.amap.api.services.core;

import android.content.Context;
import com.amap.api.services.cloud.CloudItem;
import com.amap.api.services.cloud.CloudSearch.Query;
import com.amap.api.services.cloud.CloudSearch.SearchBound;
import java.util.ArrayList;
import java.util.List;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/* compiled from: CloudSearchKeywordsHandler */
public class g extends e<Query, ArrayList<CloudItem>> {
    private int h = 1;
    private int i = 20;
    private int j = 0;

    public /* synthetic */ Object a(String str) throws AMapException {
        return e(str);
    }

    public g(Context context, Query query) {
        super(context, query);
    }

    public void a(int i) {
        this.h = i + 1;
        ((Query) this.a).getBound();
    }

    public void b(int i) {
        if (i <= 0) {
            i = 20;
        }
        this.i = i;
    }

    public int h() {
        return this.i;
    }

    public int i() {
        return this.j;
    }

    public Query j() {
        return (Query) this.a;
    }

    public SearchBound k() {
        return ((Query) this.a).getBound();
    }

    public String g() {
        String str = h.b() + "/datasearch";
        String shape = ((Query) this.a).getBound().getShape();
        if (shape.equals("Bound")) {
            return str + "/around?";
        }
        if (shape.equals("Polygon") || shape.equals("Rectangle")) {
            return str + "/polygon?";
        }
        if (shape.equals(SearchBound.LOCAL_SHAPE)) {
            return str + "/local?";
        }
        return str;
    }

    public ArrayList<CloudItem> e(String str) throws AMapException {
        if (str == null || str.equals("")) {
            return null;
        }
        ArrayList<CloudItem> b;
        try {
            b = b(new JSONObject(str));
        } catch (JSONException e) {
            e.printStackTrace();
            b = null;
        } catch (Exception e2) {
            e2.printStackTrace();
            b = null;
        }
        return b;
    }

    private ArrayList<CloudItem> b(JSONObject jSONObject) throws JSONException {
        ArrayList<CloudItem> arrayList = new ArrayList();
        if (!jSONObject.has("datas")) {
            return arrayList;
        }
        JSONArray optJSONArray = jSONObject.optJSONArray("datas");
        this.j = jSONObject.getInt("count");
        for (int i = 0; i < optJSONArray.length(); i++) {
            JSONObject optJSONObject = optJSONArray.optJSONObject(i);
            CloudItem a = a(optJSONObject);
            a(a, optJSONObject);
            arrayList.add(a);
        }
        return arrayList;
    }

    protected String e() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append("output=json");
        if (((Query) this.a).getBound() != null) {
            double a;
            if (((Query) this.a).getBound().getShape().equals("Bound")) {
                a = i.a(((Query) this.a).getBound().getCenter().getLongitude());
                stringBuilder.append("&center=").append(a + "," + i.a(((Query) this.a).getBound().getCenter().getLatitude()));
                stringBuilder.append("&radius=").append(((Query) this.a).getBound().getRange());
            } else if (((Query) this.a).getBound().getShape().equals("Rectangle")) {
                LatLonPoint lowerLeft = ((Query) this.a).getBound().getLowerLeft();
                LatLonPoint upperRight = ((Query) this.a).getBound().getUpperRight();
                double a2 = i.a(lowerLeft.getLatitude());
                a = i.a(lowerLeft.getLongitude());
                stringBuilder.append("&polygon=" + a + "," + a2 + ";" + i.a(upperRight.getLongitude()) + "," + i.a(upperRight.getLatitude()));
            } else if (((Query) this.a).getBound().getShape().equals("Polygon")) {
                List polyGonList = ((Query) this.a).getBound().getPolyGonList();
                if (polyGonList != null && polyGonList.size() > 0) {
                    stringBuilder.append("&polygon=" + i.a(polyGonList));
                }
            } else if (((Query) this.a).getBound().getShape().equals(SearchBound.LOCAL_SHAPE)) {
                stringBuilder.append("&city=").append(b(((Query) this.a).getBound().getCity()));
            }
        }
        stringBuilder.append("&tableid=" + ((Query) this.a).getTableID());
        if (!i.a(m())) {
            m();
            stringBuilder.append("&filter=").append(b(m()));
        }
        if (!i.a(l())) {
            stringBuilder.append("&sortrule=").append(l());
        }
        String str = "";
        String b = b(((Query) this.a).getQueryString());
        if (((Query) this.a).getQueryString() == null || ((Query) this.a).getQueryString().equals("")) {
            stringBuilder.append("&keywords=");
        } else {
            stringBuilder.append("&keywords=" + b);
        }
        stringBuilder.append("&limit=" + this.i);
        stringBuilder.append("&page=" + this.h);
        stringBuilder.append("&key=" + aj.f(this.d));
        return stringBuilder.toString();
    }

    private String l() {
        if (((Query) this.a).getSortingrules() == null) {
            return "";
        }
        return ((Query) this.a).getSortingrules().toString();
    }

    private String m() {
        StringBuffer stringBuffer = new StringBuffer();
        String filterString = ((Query) this.a).getFilterString();
        String filterNumString = ((Query) this.a).getFilterNumString();
        stringBuffer.append(filterString);
        if (!(i.a(filterString) || i.a(filterNumString))) {
            stringBuffer.append("+");
        }
        stringBuffer.append(filterNumString);
        return stringBuffer.toString();
    }
}
