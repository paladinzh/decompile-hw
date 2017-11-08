package com.google.android.gms.maps.model;

import android.graphics.Bitmap;
import android.os.RemoteException;
import com.google.android.gms.internal.er;
import com.google.android.gms.maps.model.internal.a;

/* compiled from: Unknown */
public final class BitmapDescriptorFactory {
    private static a PE;

    private BitmapDescriptorFactory() {
    }

    public static void a(a aVar) {
        if (PE == null) {
            PE = (a) er.f(aVar);
        }
    }

    public static BitmapDescriptor fromBitmap(Bitmap image) {
        try {
            return new BitmapDescriptor(hd().b(image));
        } catch (RemoteException e) {
            throw new RuntimeRemoteException(e);
        }
    }

    private static a hd() {
        return (a) er.b(PE, (Object) "IBitmapDescriptorFactory is not initialized");
    }
}
