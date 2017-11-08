package com.amap.api.trace;

import android.content.Context;
import com.amap.api.mapcore.util.eh;
import com.amap.api.mapcore.util.ev;
import com.amap.api.mapcore.util.gj;
import java.util.List;

public class LBSTraceClient implements LBSTraceBase {
    public static final int TYPE_AMAP = 1;
    public static final int TYPE_BAIDU = 3;
    public static final int TYPE_GPS = 2;
    private LBSTraceBase a;

    public LBSTraceClient(Context context) {
        if (context != null) {
            try {
                this.a = (LBSTraceBase) gj.a(context.getApplicationContext(), eh.e(), "com.amap.api.wrapper.LBSTraceClientWrapper", ev.class, new Class[]{Context.class}, new Object[]{context.getApplicationContext()});
            } catch (Throwable th) {
                this.a = new ev(context.getApplicationContext());
            }
        }
    }

    public void queryProcessedTrace(int i, List<TraceLocation> list, int i2, TraceListener traceListener) {
        if (this.a != null) {
            this.a.queryProcessedTrace(i, list, i2, traceListener);
        }
    }
}
