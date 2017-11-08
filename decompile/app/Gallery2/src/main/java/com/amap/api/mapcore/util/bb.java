package com.amap.api.mapcore.util;

import android.content.Context;
import android.os.Handler;
import com.amap.api.maps.offlinemap.OfflineMapCity;
import com.amap.api.maps.offlinemap.OfflineMapProvince;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/* compiled from: OfflineMapDownloadList */
public class bb {
    public ArrayList<OfflineMapProvince> a = new ArrayList();
    private bm b;
    private Context c;
    private Handler d;

    public bb(Context context, Handler handler) {
        this.c = context;
        this.d = handler;
        this.b = bm.a(context);
    }

    private void a(bh bhVar) {
        if (this.b != null && bhVar != null) {
            this.b.a(bhVar);
        }
    }

    private void b(bh bhVar) {
        if (this.b != null) {
            this.b.b(bhVar);
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
        if (this.a.size() <= 0) {
            for (OfflineMapProvince add : list) {
                OfflineMapProvince add2;
                this.a.add(add2);
            }
            return;
        }
        for (int i = 0; i < this.a.size(); i++) {
            add2 = (OfflineMapProvince) this.a.get(i);
            for (OfflineMapProvince offlineMapProvince : list) {
                if (add2.getProvinceCode().equals(offlineMapProvince.getProvinceCode())) {
                    break;
                }
            }
            OfflineMapProvince offlineMapProvince2 = null;
            if (offlineMapProvince2 != null) {
                a(add2, offlineMapProvince2);
                ArrayList cityList = add2.getCityList();
                ArrayList cityList2 = offlineMapProvince2.getCityList();
                for (int i2 = 0; i2 < cityList.size(); i2++) {
                    OfflineMapCity offlineMapCity;
                    OfflineMapCity offlineMapCity2 = (OfflineMapCity) cityList.get(i2);
                    Iterator it = cityList2.iterator();
                    while (it.hasNext()) {
                        offlineMapCity = (OfflineMapCity) it.next();
                        if (offlineMapCity2.getPinyin().equals(offlineMapCity.getPinyin())) {
                            break;
                        }
                    }
                    offlineMapCity = null;
                    if (offlineMapCity != null) {
                        a(offlineMapCity2, offlineMapCity);
                    }
                }
            }
        }
    }

    private void a(OfflineMapCity offlineMapCity, OfflineMapCity offlineMapCity2) {
        offlineMapCity.setUrl(offlineMapCity2.getUrl());
        offlineMapCity.setVersion(offlineMapCity2.getVersion());
        offlineMapCity.setSize(offlineMapCity2.getSize());
        offlineMapCity.setCode(offlineMapCity2.getCode());
        offlineMapCity.setPinyin(offlineMapCity2.getPinyin());
        offlineMapCity.setJianpin(offlineMapCity2.getJianpin());
    }

    private void a(OfflineMapProvince offlineMapProvince, OfflineMapProvince offlineMapProvince2) {
        offlineMapProvince.setUrl(offlineMapProvince2.getUrl());
        offlineMapProvince.setVersion(offlineMapProvince2.getVersion());
        offlineMapProvince.setSize(offlineMapProvince2.getSize());
        offlineMapProvince.setPinyin(offlineMapProvince2.getPinyin());
        offlineMapProvince.setJianpin(offlineMapProvince2.getJianpin());
    }

    public ArrayList<OfflineMapCity> c() {
        ArrayList<OfflineMapCity> arrayList;
        synchronized (this.a) {
            arrayList = new ArrayList();
            Iterator it = this.a.iterator();
            while (it.hasNext()) {
                for (OfflineMapCity offlineMapCity : ((OfflineMapProvince) it.next()).getCityList()) {
                    if (offlineMapCity.getState() != 4) {
                        if (offlineMapCity.getState() != 7) {
                        }
                    }
                    arrayList.add(offlineMapCity);
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
                if (offlineMapProvince.getState() != 4) {
                    if (offlineMapProvince.getState() != 7) {
                    }
                }
                arrayList.add(offlineMapProvince);
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
        return i == 0 || i == 2 || i == 3 || i == 1 || i == 102 || i == 101 || i == OfflineMapStatus.EXCEPTION_SDCARD || i == -1;
    }

    public void a(aw awVar) {
        String pinyin = awVar.getPinyin();
        synchronized (this.a) {
            Iterator it = this.a.iterator();
            loop0:
            while (it.hasNext()) {
                OfflineMapProvince offlineMapProvince = (OfflineMapProvince) it.next();
                for (OfflineMapCity offlineMapCity : offlineMapProvince.getCityList()) {
                    if (offlineMapCity.getPinyin().trim().equals(pinyin.trim())) {
                        a(awVar, offlineMapCity);
                        a(awVar, offlineMapProvince);
                        break loop0;
                    }
                }
            }
        }
    }

    private void a(aw awVar, OfflineMapCity offlineMapCity) {
        int b = awVar.c().b();
        if (awVar.c().equals(awVar.a)) {
            b(awVar.x());
        } else {
            if (awVar.c().equals(awVar.f)) {
                bu.a("saveJSONObjectToFile  CITY " + awVar.getCity());
                b(awVar);
                awVar.x().c();
            }
            if (a(awVar.getcompleteCode(), awVar.c().b())) {
                a(awVar.x());
            }
        }
        offlineMapCity.setState(b);
        offlineMapCity.setCompleteCode(awVar.getcompleteCode());
    }

    private void b(aw awVar) {
        File[] listFiles = new File(eh.b(this.c)).listFiles();
        if (listFiles != null) {
            for (File file : listFiles) {
                if (file.isFile() && file.exists() && file.getName().contains(awVar.getAdcode()) && file.getName().endsWith(".zip.tmp.dt")) {
                    file.delete();
                }
            }
        }
    }

    private void a(aw awVar, OfflineMapProvince offlineMapProvince) {
        int b = awVar.c().b();
        if (b == 6) {
            offlineMapProvince.setState(b);
            offlineMapProvince.setCompleteCode(0);
            b(new bh(offlineMapProvince, this.c));
            try {
                bu.b(offlineMapProvince.getProvinceCode(), this.c);
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e2) {
                e2.printStackTrace();
            }
        } else if (b(b) && a(offlineMapProvince)) {
            bh bhVar;
            if (awVar.getPinyin().equals(offlineMapProvince.getPinyin())) {
                offlineMapProvince.setState(b);
                offlineMapProvince.setCompleteCode(awVar.getcompleteCode());
                offlineMapProvince.setVersion(awVar.getVersion());
                offlineMapProvince.setUrl(awVar.getUrl());
                bhVar = new bh(offlineMapProvince, this.c);
                bhVar.a(awVar.a());
                bhVar.d(awVar.getCode());
            } else {
                offlineMapProvince.setState(b);
                offlineMapProvince.setCompleteCode(100);
                bhVar = new bh(offlineMapProvince, this.c);
            }
            bhVar.c();
            a(bhVar);
            bu.a("saveJSONObjectToFile  province " + bhVar.d());
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

    public int i() {
        if (this.a == null) {
            return 0;
        }
        return this.a.size();
    }
}
