package com.huawei.harassmentinterception.service;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class HarassSetIntellState extends CustomCaller {
    private static final String KEY_INTELL_SWITCH = "intell_switch";
    private static final String KEY_INTELL_SWITCH_CARD1 = "intell_switch_card1";
    private static final String KEY_INTELL_SWITCH_CARD2 = "intell_switch_card2";
    private static final String TAG = "HarassSetIntell";

    public String getMethodName() {
        return TAG;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.e(TAG, "called, but param is null!");
            return null;
        }
        String caller = params.getString("pkgName");
        if (TextUtils.isEmpty(caller)) {
            HwLog.e(TAG, "called, but caller pkg is empty!");
            return null;
        }
        Context ctx = GlobalContext.getContext();
        RulesOps.setIfFirstSet(ctx);
        HwLog.i(TAG, "called, caller is:" + caller + ", params:" + params);
        if (params.containsKey(KEY_INTELL_SWITCH)) {
            return doOpByParam(ctx, params.getInt(KEY_INTELL_SWITCH, -1), 1);
        }
        if (params.containsKey(KEY_INTELL_SWITCH_CARD1)) {
            return doOpByParam(ctx, params.getInt(KEY_INTELL_SWITCH_CARD1, -1), 1);
        }
        if (params.containsKey(KEY_INTELL_SWITCH_CARD2)) {
            return doOpByParam(ctx, params.getInt(KEY_INTELL_SWITCH_CARD2, -1), 2);
        }
        return buildResult(false);
    }

    private Bundle doOpByParam(Context ctx, int switchState, int opCard) {
        if (switchState == 1) {
            return buildResult(RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_BLOCK_CALL, true, opCard));
        }
        if (switchState == 0) {
            return buildResult(RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_BLOCK_CALL, false, opCard));
        }
        HwLog.e(TAG, "unknow switchState:" + switchState);
        return buildResult(false);
    }

    private Bundle buildResult(boolean result) {
        Bundle bundle = new Bundle();
        bundle.putBoolean("result", result);
        return bundle;
    }
}
