package com.amap.api.mapcore;

import android.graphics.Rect;
import android.os.RemoteException;
import com.amap.api.maps.model.BitmapDescriptor;
import com.amap.api.maps.model.LatLng;
import com.autonavi.amap.mapcore.FPoint;
import com.autonavi.amap.mapcore.IPoint;
import java.util.ArrayList;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: IMarkerDelegate */
public interface ah {
    boolean A();

    int B();

    int C();

    int D();

    int E();

    boolean F();

    float G();

    boolean H();

    IPoint I();

    void J();

    void a(float f) throws RemoteException;

    void a(float f, float f2) throws RemoteException;

    void a(int i) throws RemoteException;

    void a(int i, int i2);

    void a(BitmapDescriptor bitmapDescriptor) throws RemoteException;

    void a(LatLng latLng) throws RemoteException;

    void a(IPoint iPoint);

    void a(Object obj);

    void a(String str) throws RemoteException;

    void a(ArrayList<BitmapDescriptor> arrayList) throws RemoteException;

    void a(GL10 gl10, ab abVar);

    void a(boolean z) throws RemoteException;

    boolean a(ah ahVar) throws RemoteException;

    void b(float f);

    void b(String str) throws RemoteException;

    void b(boolean z);

    boolean b() throws RemoteException;

    void c(boolean z) throws RemoteException;

    boolean c();

    Rect d();

    void d(boolean z) throws RemoteException;

    LatLng e() throws RemoteException;

    void e(boolean z) throws RemoteException;

    FPoint f();

    LatLng g();

    String h() throws RemoteException;

    String i() throws RemoteException;

    String j() throws RemoteException;

    boolean k();

    void l() throws RemoteException;

    void m() throws RemoteException;

    boolean n();

    boolean o() throws RemoteException;

    void p();

    int q();

    boolean r();

    Object s();

    boolean t() throws RemoteException;

    float u();

    int v() throws RemoteException;

    ArrayList<BitmapDescriptor> w() throws RemoteException;

    boolean x();

    void y();

    void z() throws RemoteException;
}
