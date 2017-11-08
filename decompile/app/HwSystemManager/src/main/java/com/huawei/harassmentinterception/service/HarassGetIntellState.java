package com.huawei.harassmentinterception.service;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.service.CustomCaller;
import com.huawei.systemmanager.util.HwLog;

public class HarassGetIntellState extends CustomCaller {
    private static final String BUNDLE_KEY_CARD_1_STATE = "card1_state";
    private static final String BUNDLE_KEY_CARD_2_STATE = "card2_state";
    private static final String BUNDLE_KEY_CARD_STATE = "card_state";
    private static final String BUNDLE_KEY_INTELL_VALUE = "intell_dualcardset";
    private static final String ENSURE_DIALOG_BTN = "ensure_dialog_btn";
    private static final String ENSURE_DIALOG_CONTENT = "ensure_dialog_cotnent";
    private static final String ENSURE_DIALOG_TITLE = "ensure_dialog_title";
    private static final String KEY_INTELL_SET = "has_been_set";
    private static final String KEY_INTELL_SWITCH = "intell_switch";
    private static final String TAG = "HarassGetIntellState";
    private static final String adver = "adver";
    private static final String estate = "estate";
    private static final String harass = "harass";
    private static final String scam = "scam";

    public String getMethodName() {
        return TAG;
    }

    public Bundle call(Bundle params) {
        if (params == null) {
            HwLog.e(TAG, "params is null!");
            return null;
        }
        Bundle res = new Bundle();
        Context ctx = GlobalContext.getContext();
        ContentValues values = RulesOps.getAllRules(ctx);
        boolean useDualCardset = RulesOps.getDualcardSet(ctx);
        boolean hasBeenSet = RulesOps.getIfFirstSet(ctx);
        res.putBoolean(BUNDLE_KEY_INTELL_VALUE, useDualCardset);
        res.putBoolean(KEY_INTELL_SET, hasBeenSet);
        HwLog.i(TAG, "call, params:" + params);
        HwLog.i(TAG, "does intell switch  has Been Set" + hasBeenSet);
        if (useDualCardset) {
            Bundle card1 = new Bundle();
            card1.putInt(KEY_INTELL_SWITCH, RulesOps.isChecked(values, RulesOps.KEY_INTELL_BLOCK_CALL, 1) ? 1 : 0);
            Bundle card2 = new Bundle();
            card2.putInt(KEY_INTELL_SWITCH, RulesOps.isChecked(values, RulesOps.KEY_INTELL_BLOCK_CALL, 2) ? 1 : 0);
            res.putBundle("card1_state", card1);
            res.putBundle("card2_state", card2);
        } else {
            Bundle bundle = new Bundle();
            bundle.putInt(KEY_INTELL_SWITCH, RulesOps.isChecked(values, RulesOps.KEY_INTELL_BLOCK_CALL, 1) ? 1 : 0);
            res.putBundle(BUNDLE_KEY_CARD_STATE, bundle);
        }
        res.putString(ENSURE_DIALOG_TITLE, ctx.getString(R.string.harassment_intell_call_ensure_dialog_title));
        res.putString(ENSURE_DIALOG_CONTENT, ctx.getString(R.string.harassment_intell_call_ensure_dialog_content_1));
        res.putString(ENSURE_DIALOG_BTN, ctx.getString(R.string.harassment_intell_call_ensure_dialog_ok));
        return res;
    }
}
