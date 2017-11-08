package com.huawei.harassmentinterception.engine;

import android.content.Context;
import com.huawei.harassmentinterception.engine.tencent.TmEngine;
import com.huawei.systemmanager.util.HwLog;

public class HwEngineManager {
    private static final /* synthetic */ int[] -com-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues = null;
    public static final String TAG = "EngineManager";
    private static HwEngine mEngine = null;

    public enum EngineId {
        INVALID,
        TENCENT_TMS,
        DIANXIN_DX,
        COOTEK_TOUCHPAL
    }

    private static /* synthetic */ int[] -getcom-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues() {
        if (-com-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues != null) {
            return -com-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues;
        }
        int[] iArr = new int[EngineId.values().length];
        try {
            iArr[EngineId.COOTEK_TOUCHPAL.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[EngineId.DIANXIN_DX.ordinal()] = 3;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[EngineId.INVALID.ordinal()] = 4;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[EngineId.TENCENT_TMS.ordinal()] = 2;
        } catch (NoSuchFieldError e4) {
        }
        -com-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues = iArr;
        return iArr;
    }

    public static synchronized HwEngine getEngine(Context context) {
        HwEngine hwEngine;
        synchronized (HwEngineManager.class) {
            if (mEngine != null) {
                HwLog.d(TAG, "EngineId = " + mEngine.getEngineId());
            } else {
                mEngine = generateEngine(context);
            }
            hwEngine = mEngine;
        }
        return hwEngine;
    }

    private static HwEngine generateEngine(Context context, EngineId engineId) {
        switch (-getcom-huawei-harassmentinterception-engine-HwEngineManager$EngineIdSwitchesValues()[engineId.ordinal()]) {
            case 2:
                return new TmEngine(context);
            default:
                HwLog.w(TAG, "generateEngine: Not supported, engineId = " + engineId);
                return null;
        }
    }

    private static HwEngine generateEngine(Context context) {
        return generateEngine(context, getEngineId(context));
    }

    private static final EngineId getEngineId(Context context) {
        return EngineId.TENCENT_TMS;
    }

    public static synchronized HwEngine switchEngine(Context context) {
        synchronized (HwEngineManager.class) {
            HwEngine hwEngine;
            EngineId engineId = getEngineId(context);
            if (mEngine != null) {
                if (engineId == mEngine.getEngineId()) {
                    HwLog.w(TAG, "switchEngine: Switch to the same engine. engineId = " + engineId);
                    hwEngine = mEngine;
                    return hwEngine;
                }
                mEngine.destroyEngine(0);
                mEngine = null;
            }
            hwEngine = generateEngine(context, engineId);
            return hwEngine;
        }
    }

    public static synchronized void destroyEngine(int nFlag) {
        synchronized (HwEngineManager.class) {
            if (mEngine == null) {
                HwLog.w(TAG, "destroyEngine: No active engine");
            } else {
                mEngine.destroyEngine(nFlag);
                mEngine = null;
                HwLog.d(TAG, "destroyEngine");
            }
        }
    }
}
