package com.amap.api.mapcore.util;

import android.graphics.Rect;
import com.autonavi.amap.mapcore.interfaces.IMarkerAction;
import com.autonavi.amap.mapcore.interfaces.IOverlayImage;
import javax.microedition.khronos.opengles.GL10;

/* compiled from: IOverlayImageDelegate */
public interface cu extends IOverlayImage {
    void a(GL10 gl10, l lVar);

    IMarkerAction getIMarkerAction();

    Rect h();

    boolean i();

    boolean isInfoWindowShown();

    boolean j();

    boolean k();

    void l();

    boolean m();
}
