package com.huawei.keyguard.amazinglockscreen;

import android.content.Context;
import com.android.keyguard.hwlockscreen.HwUnlockInterface$ConditionCallback;
import com.huawei.keyguard.HwUnlockConstants$ViewPropertyType;

public class HwUnlockIntent {
    private HwViewProperty mCondition;
    private String mType;

    public HwUnlockIntent(Context context, String condition, String type, HwUnlockInterface$ConditionCallback conditionCallback) {
        this.mType = type;
        this.mCondition = new HwViewProperty(context, condition, HwUnlockConstants$ViewPropertyType.TYPE_CONDITION, conditionCallback);
    }

    public boolean getCondition() {
        return ((Boolean) this.mCondition.getValue()).booleanValue();
    }

    public String getIntentType() {
        return this.mType;
    }
}
