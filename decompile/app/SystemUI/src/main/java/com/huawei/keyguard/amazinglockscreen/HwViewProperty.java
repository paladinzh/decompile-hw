package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import android.os.PowerManager;
import com.android.keyguard.R$string;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ImageCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$LayoutCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$MaskCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$TextCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ViewPropertyCallback;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$VisibilityCallback;
import com.huawei.keyguard.HwUnlockConstants$ListenerType;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;
import com.huawei.keyguard.amazinglockscreen.HwPropertyManager.PropertyListener;
import com.huawei.keyguard.amazinglockscreen.data.ExpressParser;
import com.huawei.keyguard.util.HwLog;

public class HwViewProperty {
    private static final /* synthetic */ int[] -com-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues = null;
    private static boolean DEBUG = false;
    private static boolean isUP = true;
    private Context mContext;
    private String mStrValue;
    private HwUnlockConstants$ViewPropertyType mType;
    private Object mValue;
    private HwUnlockInterface$ViewPropertyCallback mViewPropertyCallback;

    private static /* synthetic */ int[] -getcom-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues() {
        if (-com-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues != null) {
            return -com-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues;
        }
        int[] iArr = new int[HwUnlockConstants$ViewPropertyType.values().length];
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_CONDITION.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_DRAWABLE.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_MASK.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_TEXT.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[HwUnlockConstants$ViewPropertyType.TYPE_VISIBILITY.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        -com-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues = iArr;
        return iArr;
    }

    public static boolean isUP() {
        return isUP;
    }

    public static void setUP(boolean isUP) {
        isUP = isUP;
    }

    public HwViewProperty(Context context, String str, HwUnlockConstants$ViewPropertyType type, HwUnlockInterface$ViewPropertyCallback callback) throws IllegalArgumentException {
        this.mStrValue = str;
        this.mContext = context;
        this.mType = type;
        this.mViewPropertyCallback = callback;
        parserProperty();
        registerCallback();
    }

    public Object getValue() {
        if (DEBUG) {
            HwLog.i("HwViewProperty", "HwViewProperty getValue mValue= " + this.mValue);
        }
        if (this.mType == HwUnlockConstants$ViewPropertyType.TYPE_LAYOUT || this.mType == HwUnlockConstants$ViewPropertyType.TYPE_MASK) {
            float scale = AmazingUtils.getScalePara();
            if (scale != 1.0f && (this.mValue instanceof Integer)) {
                return Integer.valueOf((int) (((float) ((Integer) this.mValue).intValue()) * scale));
            }
        }
        return this.mValue;
    }

    private void parserProperty() {
        this.mValue = ExpressParser.getInstance().parse(this.mStrValue);
    }

    private void refreshProperty() {
        this.mValue = ExpressParser.getInstance().parse(this.mStrValue);
        if (this.mValue != null) {
            switch (-getcom-huawei-keyguard-HwUnlockConstants$ViewPropertyTypeSwitchesValues()[this.mType.ordinal()]) {
                case 1:
                    if (isUP) {
                        ((HwUnlockInterface$ConditionCallback) this.mViewPropertyCallback).refreshCondition(this.mStrValue, ((Boolean) this.mValue).booleanValue());
                        return;
                    }
                    return;
                case 2:
                    ((HwUnlockInterface$ImageCallback) this.mViewPropertyCallback).setImageBitmapSrc(this.mValue.toString());
                    return;
                case 3:
                    ((HwUnlockInterface$LayoutCallback) this.mViewPropertyCallback).onLayoutChanged();
                    return;
                case 4:
                    ((HwUnlockInterface$MaskCallback) this.mViewPropertyCallback).onMaskChanged();
                    return;
                case 5:
                    ((HwUnlockInterface$TextCallback) this.mViewPropertyCallback).setTextContent(this.mValue.toString());
                    return;
                case 6:
                    ((HwUnlockInterface$VisibilityCallback) this.mViewPropertyCallback).refreshVisibility(((Boolean) this.mValue).booleanValue());
                    return;
                default:
                    return;
            }
        }
    }

    private void registerCallback() {
        if (this.mStrValue.contains("system.point")) {
            registerCallback(HwUnlockConstants$ListenerType.POSITION);
        }
        if (this.mStrValue.contains("system.time")) {
            registerCallback(HwUnlockConstants$ListenerType.TIME);
        }
        if (this.mStrValue.contains("system.carrier")) {
            registerCallback(HwUnlockConstants$ListenerType.CARRIER);
        }
        if (this.mStrValue.contains("system.carrier2")) {
            registerCallback(HwUnlockConstants$ListenerType.CARRIER2);
        }
        if (this.mStrValue.contains("system.date")) {
            registerCallback(HwUnlockConstants$ListenerType.DATE);
        }
        if (this.mStrValue.contains("system.clockdesc_default")) {
            ExpressParser.getInstance().setSystemValue("clockdesc_default", this.mContext.getString(R$string.kg_dualclock_default));
            registerCallback(HwUnlockConstants$ListenerType.CLOCKDESC_DEFAULT);
        }
        if (this.mStrValue.contains("system.clockdesc_roaming")) {
            ExpressParser.getInstance().setSystemValue("clockdesc_roaming", this.mContext.getString(R$string.kg_duaclock_roaming));
            registerCallback(HwUnlockConstants$ListenerType.CLOCKDESC_ROAMING);
        }
        if (this.mStrValue.contains("system.time_default")) {
            registerCallback(HwUnlockConstants$ListenerType.TIME_DEFAULT);
        }
        if (this.mStrValue.contains("system.time_roaming")) {
            registerCallback(HwUnlockConstants$ListenerType.TIME_ROAMING);
        }
        if (this.mStrValue.contains("system.date_default")) {
            registerCallback(HwUnlockConstants$ListenerType.DATE_DEFAULT);
        }
        if (this.mStrValue.contains("system.date_roaming")) {
            registerCallback(HwUnlockConstants$ListenerType.DATE_ROAMING);
        }
        if (this.mStrValue.contains("system.dualclock")) {
            registerCallback(HwUnlockConstants$ListenerType.DUALCLOCK);
        }
        if (this.mStrValue.contains("system.charge")) {
            registerCallback(HwUnlockConstants$ListenerType.CHARGE);
        }
        if (this.mStrValue.contains("system.press")) {
            registerCallback(HwUnlockConstants$ListenerType.PRESS_STATE);
        }
        if (this.mStrValue.contains("system.call")) {
            registerCallback(HwUnlockConstants$ListenerType.CALL_COUNT);
        }
        if (this.mStrValue.contains("system.email")) {
            registerCallback(HwUnlockConstants$ListenerType.EMAIL_COUNT);
        }
        if (this.mStrValue.contains("system.message")) {
            registerCallback(HwUnlockConstants$ListenerType.MESSAGE_COUNT);
        }
        if (this.mStrValue.contains("system.ownerinfo")) {
            registerCallback(HwUnlockConstants$ListenerType.OWNER_INFO);
        }
        if (this.mStrValue.contains("system.unlocktip")) {
            registerCallback(HwUnlockConstants$ListenerType.UNLOCK_TIP);
        }
        if (this.mStrValue.contains("system.move")) {
            registerCallback(HwUnlockConstants$ListenerType.MOVE);
        }
        if (this.mStrValue.contains("system.start")) {
            registerCallback(HwUnlockConstants$ListenerType.START);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("_state")) {
            registerCallback(HwUnlockConstants$ListenerType.UNLOCK_STATE);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("_moveX")) {
            registerCallback(HwUnlockConstants$ListenerType.UNLOCK_MOVEX);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("_moveY")) {
            registerCallback(HwUnlockConstants$ListenerType.UNLOCK_MOVEY);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_visible")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_VISIBLE);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_state")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_STATE);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_text")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_TEXT);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_prev")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_PREV);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_next")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_NEXT);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_pause")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_PAUSE);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("music_play")) {
            registerCallback(HwUnlockConstants$ListenerType.MUSIC_PLAY);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("time_value")) {
            registerCallback(HwUnlockConstants$ListenerType.TIME_VALUE);
        }
        if (this.mStrValue.contains("system.") && this.mStrValue.contains("week")) {
            registerCallback(HwUnlockConstants$ListenerType.WEEK);
        }
    }

    private void registerCallback(HwUnlockConstants$ListenerType type) {
        HwPropertyManager.getInstance().registerCallback(type, new PropertyListener() {
            public void onChange() {
                if (((PowerManager) HwViewProperty.this.mContext.getSystemService("power")).isScreenOn()) {
                    HwViewProperty.this.refreshProperty();
                }
            }
        });
    }
}
