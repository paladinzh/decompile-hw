package com.amap.api.mapcore.util;

import android.content.Context;
import com.autonavi.amap.mapcore.BaseMapCallImplement;
import com.autonavi.amap.mapcore.Convert;
import com.autonavi.amap.mapcore.MapCore;
import com.autonavi.amap.mapcore.MapSourceGridData;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: AMapCallback */
class a extends BaseMapCallImplement {
    private b a;

    public String getMapSvrAddress() {
        return "http://mps.amap.com";
    }

    public a(b bVar) {
        this.a = bVar;
    }

    public void OnMapSurfaceCreate(GL10 gl10, MapCore mapCore) {
        super.OnMapSurfaceCreate(mapCore);
    }

    public void OnMapSurfaceRenderer(GL10 gl10, MapCore mapCore, int i) {
        super.OnMapSurfaceRenderer(gl10, mapCore, i);
        if (i == 3) {
            this.a.j();
            this.a.b.a(gl10, true, this.a.getMapTextZIndex());
            if (this.a.o != null) {
                this.a.o.onDrawFrame(gl10);
            }
        }
    }

    public void OnMapSufaceChanged(GL10 gl10, MapCore mapCore, int i, int i2) {
    }

    public void OnMapProcessEvent(MapCore mapCore) {
    }

    public void OnMapDestory(GL10 gl10, MapCore mapCore) {
        super.OnMapDestory(mapCore);
    }

    public void OnMapReferencechanged(MapCore mapCore, String str, String str2) {
        this.a.s();
    }

    public Context getContext() {
        return this.a.t();
    }

    public boolean isMapEngineValid() {
        if (this.a.a() == null) {
            return false;
        }
        return this.a.a().isMapEngineValid();
    }

    public void OnMapLoaderError(int i) {
    }

    public void requestRender() {
        this.a.setRunLowFrame(false);
    }

    public void onIndoorBuildingActivity(MapCore mapCore, byte[] bArr) {
        aq aqVar = null;
        if (bArr != null) {
            aq aqVar2 = new aq();
            byte b = bArr[0];
            aqVar2.a = new String(bArr, 1, b, "utf-8");
            int i = b + 1;
            int i2 = i + 1;
            b = bArr[i];
            aqVar2.b = new String(bArr, i2, b, "utf-8");
            i = b + i2;
            i2 = i + 1;
            b = bArr[i];
            aqVar2.activeFloorName = new String(bArr, i2, b, "utf-8");
            i = b + i2;
            aqVar2.activeFloorIndex = Convert.getInt(bArr, i);
            i += 4;
            i2 = i + 1;
            b = bArr[i];
            aqVar2.poiid = new String(bArr, i2, b, "utf-8");
            i = b + i2;
            aqVar2.c = Convert.getInt(bArr, i);
            i += 4;
            aqVar2.floor_indexs = new int[aqVar2.c];
            aqVar2.floor_names = new String[aqVar2.c];
            aqVar2.d = new String[aqVar2.c];
            for (int i3 = 0; i3 < aqVar2.c; i3++) {
                aqVar2.floor_indexs[i3] = Convert.getInt(bArr, i);
                i2 = i + 4;
                i = i2 + 1;
                byte b2 = bArr[i2];
                if (b2 <= (byte) 0) {
                    i2 = i;
                } else {
                    aqVar2.floor_names[i3] = new String(bArr, i, b2, "utf-8");
                    i2 = i + b2;
                }
                i = i2 + 1;
                b2 = bArr[i2];
                if (b2 > (byte) 0) {
                    aqVar2.d[i3] = new String(bArr, i, b2, "utf-8");
                    i += b2;
                }
            }
            aqVar2.e = Convert.getInt(bArr, i);
            i += 4;
            if (aqVar2.e <= 0) {
                aqVar = aqVar2;
            } else {
                aqVar2.f = new int[aqVar2.e];
                int i4 = i;
                for (i = 0; i < aqVar2.e; i++) {
                    aqVar2.f[i] = Convert.getInt(bArr, i4);
                    i4 += 4;
                }
                aqVar = aqVar2;
            }
        }
        try {
            this.a.a(aqVar);
        } catch (Throwable th) {
            th.printStackTrace();
            fo.b(th, "AMapCallback", "onIndoorBuildingActivity");
        }
    }

    public void onIndoorDataRequired(MapCore mapCore, int i, String[] strArr, int[] iArr, int[] iArr2) {
        if (strArr != null && strArr.length != 0) {
            ArrayList reqGridList = getReqGridList(i);
            if (reqGridList != null) {
                reqGridList.clear();
                for (int i2 = 0; i2 < strArr.length; i2++) {
                    reqGridList.add(new MapSourceGridData(strArr[i2], i, iArr[i2], iArr2[i2]));
                }
                if (i != 5) {
                    proccessRequiredData(mapCore, reqGridList, i);
                }
            }
        }
    }
}
