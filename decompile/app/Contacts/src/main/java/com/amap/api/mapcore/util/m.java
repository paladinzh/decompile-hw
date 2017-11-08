package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Handler;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: OfflineMapDownloadList */
public class m {
    public ArrayList<OfflineMapProvince> a = new ArrayList();
    private x b;
    private Context c;
    private Handler d;

    public m(Context context, Handler handler) {
        this.c = context;
        this.d = handler;
        this.b = x.a(context);
    }

    private void a(s sVar) {
        if (this.b != null && sVar != null) {
            this.b.a(sVar);
        }
    }

    private void d(String str) {
        if (this.b != null) {
            this.b.c(str);
        }
    }

    private boolean a(int i, int i2) {
        return i2 != 1 || i <= 2 || i >= 98;
    }

    private boolean b(int i) {
        if (i != 4) {
            return false;
        }
        return true;
    }

    private boolean a(OfflineMapProvince offlineMapProvince) {
        if (offlineMapProvince == null) {
            return false;
        }
        Iterator it = offlineMapProvince.getCityList().iterator();
        while (it.hasNext()) {
            if (((OfflineMapCity) it.next()).getState() != 4) {
                return false;
            }
        }
        return true;
    }

    public ArrayList<OfflineMapProvince> a() {
        ArrayList<OfflineMapProvince> arrayList = new ArrayList();
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            arrayList.add((OfflineMapProvince) it.next());
        }
        return arrayList;
    }

    public OfflineMapCity a(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                OfflineMapCity offlineMapCity = (OfflineMapCity) it2.next();
                if (offlineMapCity.getCode().equals(str)) {
                    return offlineMapCity;
                }
            }
        }
        return null;
    }

    public OfflineMapCity b(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                OfflineMapCity offlineMapCity = (OfflineMapCity) it2.next();
                if (offlineMapCity.getCity().trim().equalsIgnoreCase(str.trim())) {
                    return offlineMapCity;
                }
            }
        }
        return null;
    }

    public OfflineMapProvince c(String str) {
        if (str == null || str.equals("")) {
            return null;
        }
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            OfflineMapProvince offlineMapProvince = (OfflineMapProvince) it.next();
            if (offlineMapProvince.getProvinceName().trim().equalsIgnoreCase(str.trim())) {
                return offlineMapProvince;
            }
        }
        return null;
    }

    public ArrayList<OfflineMapCity> b() {
        ArrayList<OfflineMapCity> arrayList = new ArrayList();
        Iterator it = this.a.iterator();
        while (it.hasNext()) {
            Iterator it2 = ((OfflineMapProvince) it.next()).getCityList().iterator();
            while (it2.hasNext()) {
                arrayList.add((OfflineMapCity) it2.next());
            }
        }
        return arrayList;
    }

    public void a(List<OfflineMapProvince> list) {
        OfflineMapProvince offlineMapProvince;
        if (this.a.size() <= 0) {
            for (OfflineMapProvince offlineMapProvince2 : list) {
                this.a.add(offlineMapProvince2);
            }
            return;
        }
        for (int i = 0; i < this.a.size(); i++) {
            offlineMapProvince2 = (OfflineMapProvince) this.a.get(i);
            OfflineMapProvince offlineMapProvince3 = (OfflineMapProvince) list.get(i);
            a(offlineMapProvince2, offlineMapProvince3);
            ArrayList cityList = offlineMapProvince2.getCityList();
            ArrayList cityList2 = offlineMapProvince3.getCityList();
            for (int i2 = 0; i2 < cityList.size(); i2++) {
                a((OfflineMapCity) cityList.get(i2), (OfflineMapCity) cityList2.get(i2));
            }
        }
    }

    private void a(OfflineMapCity offlineMapCity, OfflineMapCity offlineMapCity2) {
        offlineMapCity.setUrl(offlineMapCity2.getUrl());
        offlineMapCity.setVersion(offlineMapCity2.getVersion());
    }

    private void a(OfflineMapProvince offlineMapProvince, OfflineMapProvince offlineMapProvince2) {
        offlineMapProvince.setUrl(offlineMapProvince2.getUrl());
        offlineMapProvince.setVersion(offlineMapProvince2.getVersion());
    }

    public ArrayList<OfflineMapCity> c() {
        ArrayList<OfflineMapCity> arrayList;
        synchronized (this.a) {
            arrayList = new ArrayList();
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                for (OfflineMapCity offlineMapCity : ((OfflineMapProvince) it.next()).getCityList()) {
                    if (offlineMapCity.getState() == 4) {
                        arrayList.add(offlineMapCity);
                    }
                }
            }
        }
        return arrayList;
    }

    public ArrayList<OfflineMapProvince> d() {
        ArrayList<OfflineMapProvince> arrayList;
        synchronized (this.a) {
            arrayList = new ArrayList();
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                OfflineMapProvince offlineMapProvince = (OfflineMapProvince) it.next();
                if (offlineMapProvince.getState() == 4) {
                    arrayList.add(offlineMapProvince);
                }
            }
        }
        return arrayList;
    }

    public ArrayList<OfflineMapCity> e() {
        ArrayList<OfflineMapCity> arrayList;
        synchronized (this.a) {
            arrayList = new ArrayList();
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                for (OfflineMapCity offlineMapCity : ((OfflineMapProvince) it.next()).getCityList()) {
                    if (a(offlineMapCity.getState())) {
                        arrayList.add(offlineMapCity);
                    }
                }
            }
        }
        return arrayList;
    }

    public ArrayList<OfflineMapProvince> f() {
        ArrayList<OfflineMapProvince> arrayList;
        synchronized (this.a) {
            arrayList = new ArrayList();
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                OfflineMapProvince offlineMapProvince = (OfflineMapProvince) it.next();
                if (a(offlineMapProvince.getState())) {
                    arrayList.add(offlineMapProvince);
                }
            }
        }
        return arrayList;
    }

    public boolean a(int i) {
        return i == 0 || i == 2 || i == 3 || i == 1;
    }

    public void a(g gVar) {
        String adcode = gVar.getAdcode();
        synchronized (this.a) {
            Iterator it = this.a.iterator();
            loop0:
            while (it.hasNext()) {
                OfflineMapProvince offlineMapProvince = (OfflineMapProvince) it.next();
                for (OfflineMapCity offlineMapCity : offlineMapProvince.getCityList()) {
                    if (offlineMapCity.getAdcode().trim().equals(adcode.trim())) {
                        a(gVar, offlineMapCity);
                        a(gVar, offlineMapProvince);
                        break loop0;
                    }
                }
            }
        }
    }

    private void a(g gVar, OfflineMapCity offlineMapCity) {
        int b = gVar.c().b();
        if (gVar.c().equals(gVar.a)) {
            d(gVar.getAdcode());
        } else {
            if (gVar.c().equals(gVar.f)) {
                af.a("saveJSONObjectToFile  CITY " + gVar.getCity());
                gVar.w().d();
            }
            if (a(gVar.getcompleteCode(), gVar.c().b())) {
                a(gVar.w());
            }
        }
        offlineMapCity.setState(b);
        offlineMapCity.setCompleteCode(gVar.getcompleteCode());
    }

    private void a(g gVar, OfflineMapProvince offlineMapProvince) {
        int b = gVar.c().b();
        if (b == 6) {
            offlineMapProvince.setState(b);
            offlineMapProvince.setCompleteCode(0);
            d(offlineMapProvince.getProvinceCode());
            try {
                af.a(offlineMapProvince.getProvinceCode(), this.c);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else if (b(b) && a(offlineMapProvince)) {
            s sVar;
            if (gVar.getAdcode().equals(offlineMapProvince.getProvinceCode())) {
                offlineMapProvince.setState(b);
                offlineMapProvince.setCompleteCode(gVar.getcompleteCode());
                offlineMapProvince.setVersion(gVar.getVersion());
                offlineMapProvince.setUrl(gVar.getUrl());
                sVar = new s(offlineMapProvince, this.c);
                sVar.a(gVar.a());
                sVar.c(gVar.getCode());
            } else {
                offlineMapProvince.setState(b);
                offlineMapProvince.setCompleteCode(100);
                sVar = new s(offlineMapProvince, this.c);
            }
            sVar.d();
            a(sVar);
            af.a("saveJSONObjectToFile  province " + sVar.e());
        }
    }

    public void g() {
        h();
        this.d = null;
        this.b = null;
        this.c = null;
    }

    public void h() {
        this.a.clear();
    }
}
