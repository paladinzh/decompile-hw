package com.amap.api.services.poisearch;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.d;
import com.amap.api.services.core.l;
import com.amap.api.services.core.p;
import com.amap.api.services.core.p.e;
import com.amap.api.services.core.s;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class PoiSearch {
    public static final String CHINESE = "zh-CN";
    public static final String ENGLISH = "en";
    private static HashMap<Integer, PoiResult> i;
    private SearchBound a;
    private Query b;
    private Context c;
    private OnPoiSearchListener d;
    private String e = "zh-CN";
    private Query f;
    private SearchBound g;
    private int h;
    private Handler j = null;

    public interface OnPoiSearchListener {
        void onPoiItemDetailSearched(PoiItemDetail poiItemDetail, int i);

        void onPoiSearched(PoiResult poiResult, int i);
    }

    public static class Query implements Cloneable {
        private String a;
        private String b;
        private String c;
        private int d;
        private int e;
        private boolean f;
        private boolean g;
        private String h;

        public Query(String str, String str2) {
            this(str, str2, null);
        }

        public Query(String str, String str2, String str3) {
            this.d = 0;
            this.e = 20;
            this.h = "zh-CN";
            this.a = str;
            this.b = str2;
            this.c = str3;
        }

        public String getQueryString() {
            return this.a;
        }

        protected void setQueryLanguage(String str) {
            if ("en".equals(str)) {
                this.h = "en";
            } else {
                this.h = "zh-CN";
            }
        }

        protected String getQueryLanguage() {
            return this.h;
        }

        public void setLimitGroupbuy(boolean z) {
            this.f = z;
        }

        public boolean hasGroupBuyLimit() {
            return this.f;
        }

        public void setLimitDiscount(boolean z) {
            this.g = z;
        }

        public boolean hasDiscountLimit() {
            return this.g;
        }

        public String getCategory() {
            if (this.b == null || this.b.equals("00") || this.b.equals("00|")) {
                return a();
            }
            return this.b;
        }

        private String a() {
            return "";
        }

        public String getCity() {
            return this.c;
        }

        public int getPageNum() {
            return this.d;
        }

        public void setPageNum(int i) {
            this.d = i;
        }

        public void setPageSize(int i) {
            this.e = i;
        }

        public int getPageSize() {
            return this.e;
        }

        public boolean queryEquals(Query query) {
            boolean z = true;
            if (query == null) {
                return false;
            }
            if (query == this) {
                return true;
            }
            if (PoiSearch.b(query.a, this.a) && PoiSearch.b(query.b, this.b) && PoiSearch.b(query.h, this.h) && PoiSearch.b(query.c, this.c)) {
                if (query.e != this.e) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 1237;
            int i2 = 0;
            int hashCode2 = ((this.b != null ? this.b.hashCode() : 0) + 31) * 31;
            if (this.c != null) {
                hashCode = this.c.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode2 = (hashCode + hashCode2) * 31;
            if (this.g) {
                hashCode = 1231;
            } else {
                hashCode = 1237;
            }
            hashCode = (hashCode + hashCode2) * 31;
            if (this.f) {
                i = 1231;
            }
            i = (hashCode + i) * 31;
            if (this.h != null) {
                hashCode = this.h.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (((((hashCode + i) * 31) + this.d) * 31) + this.e) * 31;
            if (this.a != null) {
                i2 = this.a.hashCode();
            }
            return hashCode + i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Query query = (Query) obj;
            if (this.b != null) {
                if (!this.b.equals(query.b)) {
                    return false;
                }
            } else if (query.b != null) {
                return false;
            }
            if (this.c != null) {
                if (!this.c.equals(query.c)) {
                    return false;
                }
            } else if (query.c != null) {
                return false;
            }
            if (this.g != query.g || this.f != query.f) {
                return false;
            }
            if (this.h != null) {
                if (!this.h.equals(query.h)) {
                    return false;
                }
            } else if (query.h != null) {
                return false;
            }
            if (this.d != query.d || this.e != query.e) {
                return false;
            }
            if (this.a != null) {
                return this.a.equals(query.a);
            } else {
                if (query.a != null) {
                    return false;
                }
            }
        }

        public Query clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                d.a(e, "PoiSearch", "queryclone");
            }
            Query query = new Query(this.a, this.b, this.c);
            query.setPageNum(this.d);
            query.setPageSize(this.e);
            query.setLimitDiscount(this.g);
            query.setLimitGroupbuy(this.f);
            query.setQueryLanguage(this.h);
            return query;
        }
    }

    public static class SearchBound implements Cloneable {
        public static final String BOUND_SHAPE = "Bound";
        public static final String ELLIPSE_SHAPE = "Ellipse";
        public static final String POLYGON_SHAPE = "Polygon";
        public static final String RECTANGLE_SHAPE = "Rectangle";
        private LatLonPoint a;
        private LatLonPoint b;
        private int c;
        private LatLonPoint d;
        private String e;
        private boolean f;
        private List<LatLonPoint> g;

        public SearchBound(LatLonPoint latLonPoint, int i) {
            this.f = true;
            this.e = BOUND_SHAPE;
            this.c = i;
            this.d = latLonPoint;
            a(latLonPoint, d.a(i), d.a(i));
        }

        public SearchBound(LatLonPoint latLonPoint, int i, boolean z) {
            this.f = true;
            this.e = BOUND_SHAPE;
            this.c = i;
            this.d = latLonPoint;
            a(latLonPoint, d.a(i), d.a(i));
            this.f = z;
        }

        public SearchBound(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.f = true;
            this.e = RECTANGLE_SHAPE;
            a(latLonPoint, latLonPoint2);
        }

        public SearchBound(List<LatLonPoint> list) {
            this.f = true;
            this.e = POLYGON_SHAPE;
            this.g = list;
        }

        private SearchBound(LatLonPoint latLonPoint, LatLonPoint latLonPoint2, int i, LatLonPoint latLonPoint3, String str, List<LatLonPoint> list, boolean z) {
            this.f = true;
            this.a = latLonPoint;
            this.b = latLonPoint2;
            this.c = i;
            this.d = latLonPoint3;
            this.e = str;
            this.g = list;
            this.f = z;
        }

        private void a(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.a = latLonPoint;
            this.b = latLonPoint2;
            if ((this.a.getLatitude() >= this.b.getLatitude() ? 1 : null) != null || this.a.getLongitude() >= this.b.getLongitude()) {
                throw new IllegalArgumentException("invalid rect ");
            }
            this.d = new LatLonPoint((this.a.getLatitude() + this.b.getLatitude()) / 2.0d, (this.a.getLongitude() + this.b.getLongitude()) / 2.0d);
        }

        private void a(LatLonPoint latLonPoint, double d, double d2) {
            double d3 = d / 2.0d;
            double d4 = d2 / 2.0d;
            double latitude = latLonPoint.getLatitude();
            double longitude = latLonPoint.getLongitude();
            a(new LatLonPoint(latitude - d3, longitude - d4), new LatLonPoint(d3 + latitude, d4 + longitude));
        }

        public LatLonPoint getLowerLeft() {
            return this.a;
        }

        public LatLonPoint getUpperRight() {
            return this.b;
        }

        public LatLonPoint getCenter() {
            return this.d;
        }

        public double getLonSpanInMeter() {
            return RECTANGLE_SHAPE.equals(getShape()) ? this.b.getLongitude() - this.a.getLongitude() : 0.0d;
        }

        public double getLatSpanInMeter() {
            return RECTANGLE_SHAPE.equals(getShape()) ? this.b.getLatitude() - this.a.getLatitude() : 0.0d;
        }

        public int getRange() {
            return this.c;
        }

        public String getShape() {
            return this.e;
        }

        public boolean isDistanceSort() {
            return this.f;
        }

        public List<LatLonPoint> getPolyGonList() {
            return this.g;
        }

        public int hashCode() {
            int i;
            int i2 = 0;
            int hashCode = ((this.d != null ? this.d.hashCode() : 0) + 31) * 31;
            if (this.f) {
                i = 1231;
            } else {
                i = 1237;
            }
            hashCode = (i + hashCode) * 31;
            if (this.a != null) {
                i = this.a.hashCode();
            } else {
                i = 0;
            }
            hashCode = (i + hashCode) * 31;
            if (this.b != null) {
                i = this.b.hashCode();
            } else {
                i = 0;
            }
            hashCode = (i + hashCode) * 31;
            if (this.g != null) {
                i = this.g.hashCode();
            } else {
                i = 0;
            }
            i = (((i + hashCode) * 31) + this.c) * 31;
            if (this.e != null) {
                i2 = this.e.hashCode();
            }
            return i + i2;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            SearchBound searchBound = (SearchBound) obj;
            if (this.d != null) {
                if (!this.d.equals(searchBound.d)) {
                    return false;
                }
            } else if (searchBound.d != null) {
                return false;
            }
            if (this.f != searchBound.f) {
                return false;
            }
            if (this.a != null) {
                if (!this.a.equals(searchBound.a)) {
                    return false;
                }
            } else if (searchBound.a != null) {
                return false;
            }
            if (this.b != null) {
                if (!this.b.equals(searchBound.b)) {
                    return false;
                }
            } else if (searchBound.b != null) {
                return false;
            }
            if (this.g != null) {
                if (!this.g.equals(searchBound.g)) {
                    return false;
                }
            } else if (searchBound.g != null) {
                return false;
            }
            if (this.c != searchBound.c) {
                return false;
            }
            if (this.e != null) {
                return this.e.equals(searchBound.e);
            } else {
                if (searchBound.e != null) {
                    return false;
                }
            }
        }

        public SearchBound clone() {
            try {
                super.clone();
            } catch (Throwable e) {
                d.a(e, "PoiSearch", "SearchBoundClone");
            }
            return new SearchBound(this.a, this.b, this.c, this.d, this.e, this.g, this.f);
        }
    }

    public PoiSearch(Context context, Query query) {
        this.c = context.getApplicationContext();
        setQuery(query);
        this.j = p.a();
    }

    public void setOnPoiSearchListener(OnPoiSearchListener onPoiSearchListener) {
        this.d = onPoiSearchListener;
    }

    public void setLanguage(String str) {
        if ("en".equals(str)) {
            this.e = "en";
        } else {
            this.e = "zh-CN";
        }
    }

    public String getLanguage() {
        return this.e;
    }

    private boolean a() {
        return (d.a(this.b.a) && d.a(this.b.b)) ? false : true;
    }

    private boolean b() {
        SearchBound bound = getBound();
        if (bound == null || !bound.getShape().equals(SearchBound.BOUND_SHAPE)) {
            return false;
        }
        return true;
    }

    public PoiResult searchPOI() throws AMapException {
        SearchBound searchBound = null;
        l.a(this.c);
        if (b() || a()) {
            PoiResult pageLocal;
            j jVar;
            PoiResult a;
            this.b.setQueryLanguage(this.e);
            if (this.b.queryEquals(this.f) || this.a != null) {
                if (!this.b.queryEquals(this.f)) {
                    if (this.a.equals(this.g)) {
                    }
                }
                if (this.a != null) {
                    searchBound = this.a.clone();
                }
                if (this.h == 0) {
                    pageLocal = getPageLocal(this.b.getPageNum());
                    if (pageLocal != null) {
                        return pageLocal;
                    }
                    jVar = new j(this.c, new s(this.b.clone(), searchBound));
                    jVar.a(this.b.d);
                    jVar.b(this.b.e);
                    a = PoiResult.a(jVar, (ArrayList) jVar.g());
                    i.put(Integer.valueOf(this.b.d), a);
                    return a;
                }
                jVar = new j(this.c, new s(this.b.clone(), searchBound));
                jVar.a(this.b.d);
                jVar.b(this.b.e);
                a = PoiResult.a(jVar, (ArrayList) jVar.g());
                a(a);
                return a;
            }
            this.h = 0;
            this.f = this.b.clone();
            if (this.a != null) {
                this.g = this.a.clone();
            }
            if (i != null) {
                i.clear();
            }
            if (this.a != null) {
                searchBound = this.a.clone();
            }
            if (this.h == 0) {
                jVar = new j(this.c, new s(this.b.clone(), searchBound));
                jVar.a(this.b.d);
                jVar.b(this.b.e);
                a = PoiResult.a(jVar, (ArrayList) jVar.g());
                a(a);
                return a;
            }
            pageLocal = getPageLocal(this.b.getPageNum());
            if (pageLocal != null) {
                return pageLocal;
            }
            jVar = new j(this.c, new s(this.b.clone(), searchBound));
            jVar.a(this.b.d);
            jVar.b(this.b.e);
            a = PoiResult.a(jVar, (ArrayList) jVar.g());
            i.put(Integer.valueOf(this.b.d), a);
            return a;
        }
        throw new AMapException("无效的参数 - IllegalArgumentException");
    }

    public void searchPOIAsyn() {
        new Thread(this) {
            final /* synthetic */ PoiSearch a;

            {
                this.a = r1;
            }

            public void run() {
                Message obtainMessage = this.a.j.obtainMessage();
                obtainMessage.arg1 = 6;
                obtainMessage.what = 60;
                Bundle bundle = new Bundle();
                PoiResult poiResult = null;
                try {
                    poiResult = this.a.searchPOI();
                    bundle.putInt("errorCode", 0);
                } catch (Throwable e) {
                    d.a(e, "PoiSearch", "searchPOIAsyn");
                    bundle.putInt("errorCode", e.getErrorCode());
                } finally {
                    e eVar = new e();
                    eVar.b = this.a.d;
                    eVar.a = poiResult;
                    obtainMessage.obj = eVar;
                    obtainMessage.setData(bundle);
                    this.a.j.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    public PoiItemDetail searchPOIDetail(String str) throws AMapException {
        l.a(this.c);
        return (PoiItemDetail) new i(this.c, str, this.e).g();
    }

    public void searchPOIDetailAsyn(final String str) {
        new Thread(this) {
            final /* synthetic */ PoiSearch b;

            public void run() {
                Message obtainMessage = p.a().obtainMessage();
                obtainMessage.arg1 = 6;
                obtainMessage.what = 61;
                Bundle bundle = new Bundle();
                PoiItemDetail poiItemDetail = null;
                try {
                    poiItemDetail = this.b.searchPOIDetail(str);
                    bundle.putInt("errorCode", 0);
                } catch (Throwable e) {
                    d.a(e, "PoiSearch", "searchPOIDetailAsyn");
                    bundle.putInt("errorCode", e.getErrorCode());
                } finally {
                    p.d dVar = new p.d();
                    dVar.b = this.b.d;
                    dVar.a = poiItemDetail;
                    obtainMessage.obj = dVar;
                    obtainMessage.setData(bundle);
                    this.b.j.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    public void setQuery(Query query) {
        this.b = query;
    }

    public void setBound(SearchBound searchBound) {
        this.a = searchBound;
    }

    public Query getQuery() {
        return this.b;
    }

    public SearchBound getBound() {
        return this.a;
    }

    private static boolean b(String str, String str2) {
        if (str == null && str2 == null) {
            return true;
        }
        if (str == null || str2 == null) {
            return false;
        }
        return str.equals(str2);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void a(PoiResult poiResult) {
        i = new HashMap();
        if (this.b != null && poiResult != null && this.h > 0 && this.h > this.b.getPageNum()) {
            i.put(Integer.valueOf(this.b.getPageNum()), poiResult);
        }
    }

    protected PoiResult getPageLocal(int i) {
        if (a(i)) {
            return (PoiResult) i.get(Integer.valueOf(i));
        }
        throw new IllegalArgumentException("page out of range");
    }

    private boolean a(int i) {
        return i <= this.h && i >= 0;
    }
}
