package com.amap.api.services.cloud;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.core.f;
import com.amap.api.services.core.g;
import com.amap.api.services.core.i;
import com.amap.api.services.core.t;
import com.amap.api.services.core.t.c;
import com.amap.api.services.core.t.d;
import com.amap.api.services.core.y;
import com.amap.api.services.core.z;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;

public class CloudSearch {
    private Context a;
    private OnCloudSearchListener b;
    private Query c;
    private int d;
    private HashMap<Integer, CloudResult> e;
    private Handler f = t.a();

    public interface OnCloudSearchListener {
        void onCloudItemDetailSearched(CloudItemDetail cloudItemDetail, int i);

        void onCloudSearched(CloudResult cloudResult, int i);
    }

    public static class Query implements Cloneable {
        private String a;
        private int b = 0;
        private int c = 20;
        private String d;
        private SearchBound e;
        private Sortingrules f;
        private ArrayList<y> g = new ArrayList();
        private HashMap<String, String> h = new HashMap();

        public Query(String str, String str2, SearchBound searchBound) throws AMapException {
            if (i.a(str) || searchBound == null) {
                throw new AMapException("无效的参数 - IllegalArgumentException");
            }
            this.d = str;
            this.a = str2;
            this.e = searchBound;
        }

        private Query() {
        }

        public String getQueryString() {
            return this.a;
        }

        public void setTableID(String str) {
            this.d = str;
        }

        public String getTableID() {
            return this.d;
        }

        public int getPageNum() {
            return this.b;
        }

        public void setPageNum(int i) {
            this.b = i;
        }

        public void setPageSize(int i) {
            if (i <= 0) {
                this.c = 20;
            } else if (i <= 100) {
                this.c = i;
            } else {
                this.c = 100;
            }
        }

        public int getPageSize() {
            return this.c;
        }

        public void setBound(SearchBound searchBound) {
            this.e = searchBound;
        }

        public SearchBound getBound() {
            return this.e;
        }

        public void addFilterString(String str, String str2) {
            this.h.put(str, str2);
        }

        public String getFilterString() {
            StringBuffer stringBuffer = new StringBuffer();
            try {
                for (Entry entry : this.h.entrySet()) {
                    stringBuffer.append(entry.getKey().toString()).append(":").append(entry.getValue().toString()).append("+");
                }
                if (stringBuffer.length() > 0) {
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return stringBuffer.toString();
        }

        public void addFilterNum(String str, String str2, String str3) {
            this.g.add(new y(str, str2, str3));
        }

        private ArrayList<y> a() {
            if (this.g == null) {
                return null;
            }
            ArrayList<y> arrayList = new ArrayList();
            arrayList.addAll(this.g);
            return arrayList;
        }

        private HashMap<String, String> b() {
            if (this.h == null) {
                return null;
            }
            HashMap<String, String> hashMap = new HashMap();
            hashMap.putAll(this.h);
            return hashMap;
        }

        public String getFilterNumString() {
            StringBuffer stringBuffer = new StringBuffer();
            try {
                Iterator it = this.g.iterator();
                while (it.hasNext()) {
                    y yVar = (y) it.next();
                    stringBuffer.append(yVar.a());
                    stringBuffer.append(":[");
                    stringBuffer.append(yVar.b());
                    stringBuffer.append(",");
                    stringBuffer.append(yVar.c());
                    stringBuffer.append("]");
                    stringBuffer.append("+");
                }
                if (stringBuffer.length() > 0) {
                    stringBuffer.deleteCharAt(stringBuffer.length() - 1);
                }
            } catch (Throwable th) {
                th.printStackTrace();
            }
            return stringBuffer.toString();
        }

        public void setSortingrules(Sortingrules sortingrules) {
            this.f = sortingrules;
        }

        public Sortingrules getSortingrules() {
            return this.f;
        }

        private boolean a(SearchBound searchBound, SearchBound searchBound2) {
            if (searchBound == null && searchBound2 == null) {
                return true;
            }
            if (searchBound == null || searchBound2 == null) {
                return false;
            }
            return searchBound.equals(searchBound2);
        }

        private boolean a(Sortingrules sortingrules, Sortingrules sortingrules2) {
            if (sortingrules == null && sortingrules2 == null) {
                return true;
            }
            if (sortingrules == null || sortingrules2 == null) {
                return false;
            }
            return sortingrules.equals(sortingrules2);
        }

        public boolean queryEquals(Query query) {
            boolean z = true;
            if (query == null) {
                return false;
            }
            if (query == this) {
                return true;
            }
            if (CloudSearch.c(query.a, this.a) && CloudSearch.c(query.getTableID(), getTableID()) && CloudSearch.c(query.getFilterString(), getFilterString()) && CloudSearch.c(query.getFilterNumString(), getFilterNumString()) && query.c == this.c && a(query.getBound(), getBound())) {
                if (!a(query.getSortingrules(), getSortingrules())) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            if (this.g != null) {
                hashCode = this.g.hashCode();
            } else {
                hashCode = 0;
            }
            int i2 = (hashCode + 31) * 31;
            if (this.h != null) {
                hashCode = this.h.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.e != null) {
                hashCode = this.e.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (((((hashCode + i2) * 31) + this.b) * 31) + this.c) * 31;
            if (this.a != null) {
                hashCode = this.a.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.f != null) {
                hashCode = this.f.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (hashCode + i2) * 31;
            if (this.d != null) {
                i = this.d.hashCode();
            }
            return hashCode + i;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == null || !(obj instanceof Query)) {
                return false;
            }
            if (obj == this) {
                return true;
            }
            Query query = (Query) obj;
            if (queryEquals(query)) {
                if (query.b != this.b) {
                }
                return z;
            }
            z = false;
            return z;
        }

        public Query clone() {
            Query query;
            AMapException e;
            try {
                super.clone();
            } catch (CloneNotSupportedException e2) {
                e2.printStackTrace();
            }
            try {
                query = new Query(this.d, this.a, this.e);
                try {
                    query.setPageNum(this.b);
                    query.setPageSize(this.c);
                    query.setSortingrules(getSortingrules());
                    query.g = a();
                    query.h = b();
                } catch (AMapException e3) {
                    e = e3;
                    e.printStackTrace();
                    if (query != null) {
                        return query;
                    }
                    return new Query();
                }
            } catch (AMapException e4) {
                e = e4;
                query = null;
                e.printStackTrace();
                if (query != null) {
                    return new Query();
                }
                return query;
            }
            if (query != null) {
                return query;
            }
            return new Query();
        }
    }

    public static class SearchBound implements Cloneable {
        public static final String BOUND_SHAPE = "Bound";
        public static final String LOCAL_SHAPE = "Local";
        public static final String POLYGON_SHAPE = "Polygon";
        public static final String RECTANGLE_SHAPE = "Rectangle";
        private LatLonPoint a;
        private LatLonPoint b;
        private int c;
        private LatLonPoint d;
        private String e;
        private List<LatLonPoint> f;
        private String g;

        public SearchBound(LatLonPoint latLonPoint, int i) {
            this.e = "Bound";
            this.c = i;
            this.d = latLonPoint;
        }

        public SearchBound(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.e = "Rectangle";
            a(latLonPoint, latLonPoint2);
        }

        public SearchBound(List<LatLonPoint> list) {
            this.e = "Polygon";
            this.f = list;
        }

        public SearchBound(String str) {
            this.e = LOCAL_SHAPE;
            this.g = str;
        }

        private void a(LatLonPoint latLonPoint, LatLonPoint latLonPoint2) {
            this.a = latLonPoint;
            this.b = latLonPoint2;
            if ((this.a.getLatitude() >= this.b.getLatitude() ? 1 : null) != null || this.a.getLongitude() >= this.b.getLongitude()) {
                throw new IllegalArgumentException("invalid rect ");
            }
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

        public int getRange() {
            return this.c;
        }

        public String getShape() {
            return this.e;
        }

        public String getCity() {
            return this.g;
        }

        public List<LatLonPoint> getPolyGonList() {
            return this.f;
        }

        private boolean a(List<LatLonPoint> list, List<LatLonPoint> list2) {
            if (list == null && list2 == null) {
                return true;
            }
            if (list == null || list2 == null || list.size() != list2.size()) {
                return false;
            }
            int size = list.size();
            for (int i = 0; i < size; i++) {
                if (!((LatLonPoint) list.get(i)).equals(list2.get(i))) {
                    return false;
                }
            }
            return true;
        }

        public int hashCode() {
            int hashCode;
            int i = 0;
            if (this.d != null) {
                hashCode = this.d.hashCode();
            } else {
                hashCode = 0;
            }
            int i2 = (hashCode + 31) * 31;
            if (this.a != null) {
                hashCode = this.a.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.b != null) {
                hashCode = this.b.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (hashCode + i2) * 31;
            if (this.f != null) {
                hashCode = this.f.hashCode();
            } else {
                hashCode = 0;
            }
            i2 = (((hashCode + i2) * 31) + this.c) * 31;
            if (this.e != null) {
                hashCode = this.e.hashCode();
            } else {
                hashCode = 0;
            }
            hashCode = (hashCode + i2) * 31;
            if (this.g != null) {
                i = this.g.hashCode();
            }
            return hashCode + i;
        }

        public boolean equals(Object obj) {
            boolean z = true;
            if (obj == null || !(obj instanceof SearchBound)) {
                return false;
            }
            SearchBound searchBound = (SearchBound) obj;
            if (!getShape().equalsIgnoreCase(searchBound.getShape())) {
                return false;
            }
            if (getShape().equals("Bound")) {
                if (searchBound.d.equals(this.d)) {
                    if (searchBound.c != this.c) {
                    }
                    return z;
                }
                z = false;
                return z;
            } else if (getShape().equals("Polygon")) {
                return a(searchBound.f, this.f);
            } else {
                if (getShape().equals(LOCAL_SHAPE)) {
                    return searchBound.g.equals(this.g);
                }
                if (searchBound.a.equals(this.a)) {
                    if (!searchBound.b.equals(this.b)) {
                    }
                    return z;
                }
                z = false;
                return z;
            }
        }

        private List<LatLonPoint> a() {
            if (this.f == null) {
                return null;
            }
            List<LatLonPoint> arrayList = new ArrayList();
            for (LatLonPoint latLonPoint : this.f) {
                arrayList.add(new LatLonPoint(latLonPoint.getLatitude(), latLonPoint.getLongitude()));
            }
            return arrayList;
        }

        public SearchBound clone() {
            try {
                super.clone();
            } catch (CloneNotSupportedException e) {
                e.printStackTrace();
            }
            if (getShape().equals("Bound")) {
                return new SearchBound(this.d, this.c);
            }
            if (getShape().equals("Polygon")) {
                return new SearchBound(a());
            }
            if (getShape().equals(LOCAL_SHAPE)) {
                return new SearchBound(this.g);
            }
            return new SearchBound(this.a, this.b);
        }
    }

    public static class Sortingrules {
        public static final int DISTANCE = 1;
        public static final int WEIGHT = 0;
        private int a = 0;
        private String b;
        private boolean c = true;

        public Sortingrules(String str, boolean z) {
            this.b = str;
            this.c = z;
        }

        public Sortingrules(int i) {
            this.a = i;
        }

        public String toString() {
            String str = "";
            if (i.a(this.b)) {
                if (this.a == 0) {
                    return "_weight";
                }
                if (this.a == 1) {
                    return "_distance";
                }
                return str;
            } else if (this.c) {
                return this.b + ":" + 1;
            } else {
                return this.b + ":" + 0;
            }
        }

        public int hashCode() {
            int i;
            if (this.c) {
                i = 1231;
            } else {
                i = 1237;
            }
            int i2 = (i + 31) * 31;
            if (this.b != null) {
                i = this.b.hashCode();
            } else {
                i = 0;
            }
            return ((i + i2) * 31) + this.a;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            Sortingrules sortingrules = (Sortingrules) obj;
            if (this.c != sortingrules.c) {
                return false;
            }
            if (this.b != null) {
                if (!this.b.equals(sortingrules.b)) {
                    return false;
                }
            } else if (sortingrules.b != null) {
                return false;
            }
            return this.a == sortingrules.a;
        }
    }

    public CloudSearch(Context context) {
        this.a = context.getApplicationContext();
    }

    public void setOnCloudSearchListener(OnCloudSearchListener onCloudSearchListener) {
        this.b = onCloudSearchListener;
    }

    private CloudResult a(Query query) throws AMapException {
        Throwable th;
        CloudResult cloudResult = null;
        try {
            if (!query.queryEquals(this.c)) {
                this.d = 0;
                this.c = query.clone();
                if (this.e != null) {
                    this.e.clear();
                }
            }
            g gVar;
            CloudResult a;
            if (this.d != 0) {
                cloudResult = getPageLocal(query.getPageNum());
                if (cloudResult != null) {
                    return cloudResult;
                }
                gVar = new g(this.a, query);
                gVar.a(query.b);
                gVar.b(query.c);
                a = CloudResult.a(gVar, (ArrayList) gVar.a());
                this.e.put(Integer.valueOf(query.b), a);
                return a;
            }
            gVar = new g(this.a, query);
            gVar.a(query.b);
            gVar.b(query.c);
            a = CloudResult.a(gVar, (ArrayList) gVar.a());
            try {
                a(a, query);
                return a;
            } catch (Throwable th2) {
                Throwable th3 = th2;
                cloudResult = a;
                th = th3;
                if (th instanceof AMapException) {
                    th.printStackTrace();
                    return cloudResult;
                }
                throw ((AMapException) th);
            }
        } catch (Throwable th4) {
            th = th4;
            if (th instanceof AMapException) {
                throw ((AMapException) th);
            }
            th.printStackTrace();
            return cloudResult;
        }
    }

    public void searchCloudAsyn(final Query query) {
        new Thread(this) {
            final /* synthetic */ CloudSearch b;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 12;
                    obtainMessage.what = 700;
                    d dVar = new d();
                    dVar.b = this.b.b;
                    obtainMessage.obj = dVar;
                    dVar.a = this.b.a(query);
                    obtainMessage.arg2 = 1000;
                } catch (AMapException e) {
                    obtainMessage.arg2 = e.getErrorCode();
                } finally {
                    this.b.f.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    private CloudItemDetail b(String str, String str2) throws AMapException {
        if (str == null || str.trim().equals("")) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        } else if (str2 == null || str2.trim().equals("")) {
            throw new AMapException("无效的参数 - IllegalArgumentException");
        } else {
            try {
                return (CloudItemDetail) new f(this.a, new z(str, str2)).a();
            } catch (Throwable th) {
                if (th instanceof AMapException) {
                    AMapException aMapException = (AMapException) th;
                } else {
                    th.printStackTrace();
                    return null;
                }
            }
        }
    }

    public void searchCloudDetailAsyn(final String str, final String str2) {
        new Thread(this) {
            final /* synthetic */ CloudSearch c;

            public void run() {
                Message obtainMessage = t.a().obtainMessage();
                try {
                    obtainMessage.arg1 = 12;
                    obtainMessage.what = 701;
                    c cVar = new c();
                    cVar.b = this.c.b;
                    obtainMessage.obj = cVar;
                    cVar.a = this.c.b(str, str2);
                    obtainMessage.arg2 = 1000;
                } catch (AMapException e) {
                    obtainMessage.arg2 = e.getErrorCode();
                } finally {
                    this.c.f.sendMessage(obtainMessage);
                }
            }
        }.start();
    }

    private static boolean c(String str, String str2) {
        if (str == null && str2 == null) {
            return true;
        }
        if (str == null || str2 == null) {
            return false;
        }
        return str.equals(str2);
    }

    private void a(CloudResult cloudResult, Query query) {
        this.e = new HashMap();
        if (this.d > 0) {
            this.e.put(Integer.valueOf(query.getPageNum()), cloudResult);
        }
    }

    protected CloudResult getPageLocal(int i) {
        if (a(i)) {
            return (CloudResult) this.e.get(Integer.valueOf(i));
        }
        throw new IllegalArgumentException("page out of range");
    }

    private boolean a(int i) {
        return i <= this.d && i > 0;
    }
}
