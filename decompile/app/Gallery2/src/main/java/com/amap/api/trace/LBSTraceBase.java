package com.amap.api.trace;

import java.util.List;

public interface LBSTraceBase {
    void queryProcessedTrace(int i, List<TraceLocation> list, int i2, TraceListener traceListener);
}
