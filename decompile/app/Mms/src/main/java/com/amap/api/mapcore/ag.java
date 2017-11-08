package com.amap.api.mapcore;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.amap.api.maps.AMapOptions;

/* compiled from: IMapFragmentDelegate */
public interface ag {
    View a(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) throws RemoteException;

    ab a() throws RemoteException;

    void a(int i);

    void a(Activity activity, AMapOptions aMapOptions, Bundle bundle) throws RemoteException;

    void a(Context context);

    void a(Bundle bundle) throws RemoteException;

    void a(AMapOptions aMapOptions);

    void b() throws RemoteException;

    void b(Bundle bundle) throws RemoteException;

    void c() throws RemoteException;

    void d() throws RemoteException;

    void e() throws RemoteException;

    void f() throws RemoteException;
}
