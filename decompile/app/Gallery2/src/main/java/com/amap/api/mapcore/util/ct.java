package com.amap.api.mapcore.util;

import android.os.RemoteException;
import com.autonavi.amap.mapcore.interfaces.IOverlay;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: IOverlayDelegate */
public interface ct extends IOverlay {
    void a(GL10 gl10) throws RemoteException;

    boolean a();

    boolean b() throws RemoteException;

    boolean c();
}
