package com.android.contacts.statistical;

import android.util.SparseArray;
import com.amap.api.services.core.AMapException;

public class MapHelper {
    private static final SparseArray<String> arrayhelper = new SparseArray();

    static {
        arrayhelper.put(1001, "{A:%d,P:%d,S:%d,W:%d,H:%d,STAR:%d}");
        arrayhelper.put(1005, "{DIAL:%d}");
        arrayhelper.put(1006, "{SIM:%d}");
        arrayhelper.put(AMapException.CODE_AMAP_SERVICE_MAINTENANCE, "{NC:%s}");
        arrayhelper.put(AMapException.CODE_AMAP_SERVICE_TABLEID_NOT_EXIST, "{ENC:%s}");
        arrayhelper.put(AMapException.CODE_AMAP_ENGINE_RETURN_TIMEOUT, "{DG:%d}");
        arrayhelper.put(1109, "{UEGC:%d}");
        arrayhelper.put(3000, "{CSIM:%d}");
        arrayhelper.put(4004, "{SCT:%d}");
        arrayhelper.put(4006, "{LUT:%d}");
        arrayhelper.put(1135, "{AT:%d}");
        arrayhelper.put(1140, "{IGT:%d}");
        arrayhelper.put(4019, "{FLAG:%d}");
        arrayhelper.put(4021, "{FLAG:%d}");
        arrayhelper.put(4020, "{FLAG:%d}");
        arrayhelper.put(4026, "{FLAG:%d}");
        arrayhelper.put(4027, "{FLAG:%d}");
        arrayhelper.put(5021, "{ATS:%d}");
        arrayhelper.put(1165, "{COUNTS:%d}");
        arrayhelper.put(1166, "{COUNTS:%d}");
        arrayhelper.put(1167, "{COUNTS:%d}");
        arrayhelper.put(4038, "{UP:%s}");
    }

    protected static String getValue(int id) {
        return (String) arrayhelper.get(id);
    }
}
