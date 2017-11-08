package com.huawei.harassmentinterception.util;

import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.util.HwLog;

public class CallIntelligentGuide {
    public static final String KEY_CARD1_STATE = "card1_state";
    public static final String KEY_CARD2_STATE = "card2_state";
    public static final String KEY_DUALCARD = "dual_card";
    public static final String KEY_STATE = "state";
    public static final int STATE_NOT_SHOW = 3;
    public static final int STATE_OFF = 2;
    public static final int STATE_ON = 1;
    private static final String TAG = "CallIntelligentGuide";

    public static Bundle getCallIntellGuideState(Context ctx) {
        Bundle res = new Bundle();
        boolean supportDualcard = HsmSubsciptionManager.isMultiSubs();
        if (!supportDualcard) {
            HwLog.e(TAG, "dont support dualcard");
        }
        boolean z = RulesOps.getDualcardSet(ctx) ? supportDualcard : false;
        res.putBoolean(KEY_DUALCARD, z);
        ContentValues values = RulesOps.getAllRules(ctx);
        if (z) {
            int card1State = getSingleCardState(values, 1);
            res.putInt(KEY_CARD1_STATE, card1State);
            int card2State = getSingleCardState(values, 2);
            res.putInt(KEY_CARD2_STATE, card2State);
            if (card1State == 1 || card2State == 1) {
                res.putInt("state", 1);
            } else if (card1State == 3 && card2State == 3) {
                res.putInt("state", 3);
            } else {
                res.putInt("state", 2);
            }
        } else {
            int state = getSingleCardState(values, 1);
            res.putInt(KEY_CARD1_STATE, state);
            res.putInt("state", state);
        }
        return res;
    }

    private static int getSingleCardState(ContentValues values, int opCard) {
        if (RulesOps.isChecked(values, RulesOps.KEY_INTELL_BLOCK_CALL, opCard)) {
            return 1;
        }
        boolean blockStranger = RulesOps.isChecked(values, RulesOps.KEY_BLOCK_STRANGER_CALL, opCard);
        boolean blockAll = RulesOps.isChecked(values, RulesOps.KEY_BLOCK_ALL_CALL, opCard);
        if (blockStranger || blockAll) {
            return 3;
        }
        return 2;
    }
}
