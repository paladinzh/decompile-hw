package com.amap.api.mapcore;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.amap.api.mapcore.util.bg;
import com.amap.api.mapcore.util.bm;
import com.amap.api.mapcore.util.ce;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.LatLngBounds;
import com.autonavi.amap.mapcore.DPoint;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import com.autonavi.amap.mapcore.MapProjection;

/* compiled from: AMapDelegateImp */
class d extends Handler {
    final /* synthetic */ AMapDelegateImp a;

    d(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public void handleMessage(Message message) {
        if (message != null && !this.a.aR.booleanValue()) {
            this.a.f(false);
            CameraPosition cameraPosition;
            p pVar;
            int b;
            int c;
            switch (message.what) {
                case 2:
                    Log.w("amapsdk", "Key验证失败：[" + bm.b + "]");
                    break;
                case 10:
                    cameraPosition = (CameraPosition) message.obj;
                    if (!(cameraPosition == null || this.a.aa == null)) {
                        this.a.aa.onCameraChange(cameraPosition);
                        break;
                    }
                case 11:
                    if (this.a.aE != null) {
                        try {
                            this.a.a(this.a.aE);
                        } catch (Throwable th) {
                            ce.a(th, "AMapDelegateImp", "onMapLoaded");
                            th.printStackTrace();
                        }
                    }
                    if (this.a.Z != null) {
                        this.a.Z.onMapLoaded();
                        break;
                    }
                    break;
                case 12:
                    pVar = (p) message.obj;
                    if (pVar != null) {
                        this.a.e.a(pVar);
                        break;
                    }
                    break;
                case 13:
                    if (this.a.at != null && this.a.at.h()) {
                        switch (this.a.at.j()) {
                            case 2:
                                pVar = p.a(new IPoint(this.a.at.b(), this.a.at.c()), this.a.at.d(), this.a.at.e(), this.a.at.f());
                                if (this.a.at.a()) {
                                    pVar.p = true;
                                }
                                pVar.n = this.a.at.k();
                                this.a.e.a(pVar);
                                break;
                            default:
                                b = this.a.at.b() - this.a.au;
                                c = this.a.at.c() - this.a.av;
                                this.a.au = this.a.at.b();
                                this.a.av = this.a.at.c();
                                FPoint fPoint = new FPoint((float) (b + (this.a.n() / 2)), (float) (c + (this.a.o() / 2)));
                                FPoint fPoint2 = new FPoint();
                                this.a.J.win2Map((int) fPoint.x, (int) fPoint.y, fPoint2);
                                IPoint iPoint = new IPoint();
                                this.a.J.map2Geo(fPoint2.x, fPoint2.y, iPoint);
                                pVar = p.a(iPoint);
                                if (this.a.at.a()) {
                                    pVar.p = true;
                                }
                                this.a.e.a(pVar);
                                break;
                        }
                    }
                case 14:
                    if (this.a.R != null) {
                        this.a.R.b();
                        break;
                    }
                    return;
                case 16:
                    Bitmap bitmap = (Bitmap) message.obj;
                    c = message.arg1;
                    if (bitmap == null) {
                        if (this.a.aA != null) {
                            this.a.aA.onMapPrint(null);
                        }
                        if (this.a.aB != null) {
                            this.a.aB.onMapScreenShot(null);
                            this.a.aB.onMapScreenShot(null, c);
                        }
                    } else {
                        Canvas canvas = new Canvas(bitmap);
                        if (this.a.P != null) {
                            this.a.P.onDraw(canvas);
                        }
                        if (!(this.a.aj == null || this.a.ak == null)) {
                            Bitmap drawingCache = this.a.aj.getDrawingCache(true);
                            if (drawingCache != null) {
                                canvas.drawBitmap(drawingCache, (float) this.a.aj.getLeft(), (float) this.a.aj.getTop(), new Paint());
                            }
                        }
                        if (this.a.aA != null) {
                            this.a.aA.onMapPrint(new BitmapDrawable(this.a.H.getResources(), bitmap));
                        }
                        if (this.a.aB != null) {
                            this.a.aB.onMapScreenShot(bitmap);
                            this.a.aB.onMapScreenShot(bitmap, c);
                        }
                    }
                    this.a.aA = null;
                    this.a.aB = null;
                    break;
                case 17:
                    if (!(this.a.aj == null || this.a.al == null)) {
                        this.a.aj.setVisibility(0);
                    }
                    try {
                        cameraPosition = this.a.r();
                        if (cameraPosition != null) {
                            if (cameraPosition.zoom < 10.0f || bg.a(cameraPosition.target.latitude, cameraPosition.target.longitude)) {
                                this.a.P.setVisibility(0);
                            } else {
                                this.a.P.setVisibility(8);
                            }
                        }
                        if (this.a.aw == null || !this.a.aO) {
                            this.a.a(true, cameraPosition);
                        }
                        if (this.a.aw != null) {
                            this.a.aP = true;
                            this.a.aw.onFinish();
                            this.a.aP = false;
                        }
                        if (!this.a.aQ) {
                            this.a.aw = null;
                            break;
                        } else {
                            this.a.aQ = false;
                            break;
                        }
                    } catch (Throwable th2) {
                        ce.a(th2, "AMapDelegateImpGLSurfaceView", "CameraUpdateFinish");
                        break;
                    }
                    break;
                case 18:
                    b = this.a.n();
                    int o = this.a.o();
                    if (b <= 0 || o <= 0) {
                        this.a.bq = null;
                        break;
                    }
                    try {
                        CameraPosition r = this.a.r();
                        MapProjection.lonlat2Geo(r.target.longitude, r.target.latitude, new IPoint());
                        MapProjection mapProjection = new MapProjection(this.a.G);
                        mapProjection.setCameraHeaderAngle(r.tilt);
                        mapProjection.setMapAngle(r.bearing);
                        mapProjection.setMapZoomer(r.zoom);
                        mapProjection.recalculate();
                        DPoint dPoint = new DPoint();
                        this.a.a(mapProjection, 0, 0, dPoint);
                        LatLng latLng = new LatLng(dPoint.y, dPoint.x, false);
                        this.a.a(mapProjection, b, 0, dPoint);
                        LatLng latLng2 = new LatLng(dPoint.y, dPoint.x, false);
                        this.a.a(mapProjection, 0, o, dPoint);
                        LatLng latLng3 = new LatLng(dPoint.y, dPoint.x, false);
                        this.a.a(mapProjection, b, o, dPoint);
                        this.a.bq = LatLngBounds.builder().include(latLng3).include(new LatLng(dPoint.y, dPoint.x, false)).include(latLng).include(latLng2).build();
                        mapProjection.recycle();
                        break;
                    } catch (Throwable th3) {
                        break;
                    }
                    break;
                case 20:
                    bo boVar;
                    boolean z;
                    if (!this.a.at.a()) {
                        if (this.a.at.j() != 1) {
                            if (this.a.g == null) {
                            }
                        }
                        if (this.a.g != null) {
                            boVar = this.a.g;
                            if (message.arg1 != 0) {
                                z = false;
                            } else {
                                z = true;
                            }
                            boVar.a(z);
                            break;
                        }
                    }
                    this.a.g.b(false);
                    if (this.a.g != null) {
                        boVar = this.a.g;
                        if (message.arg1 != 0) {
                            z = true;
                        } else {
                            z = false;
                        }
                        boVar.a(z);
                    }
                    break;
                case 21:
                    if (this.a.f != null) {
                        this.a.f.a(this.a.F());
                        break;
                    }
                    break;
                case 22:
                    this.a.ah();
                    break;
            }
            this.a.f(false);
        }
    }
}
