package com.amap.api.mapcore;

import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import com.amap.api.mapcore.util.ce;

/* compiled from: AMapDelegateImp */
class e extends Handler {
    final /* synthetic */ AMapDelegateImp a;

    e(AMapDelegateImp aMapDelegateImp) {
        this.a = aMapDelegateImp;
    }

    public void handleMessage(Message message) {
        super.handleMessage(message);
        try {
            if (this.a.ac != null) {
                this.a.ac.onTouch((MotionEvent) message.obj);
            }
        } catch (Throwable th) {
            ce.a(th, "AMapDelegateImpGLSurfaceView", "onTouchHandler");
            th.printStackTrace();
        }
    }
}
