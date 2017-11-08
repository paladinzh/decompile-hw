package com.huawei.rcs.service;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.strategy.StrategyManager;
import com.huawei.rcs.util.HwRcsFeatureEnabler;

public class RcsHarassmentInterceptionService {
    private static final String TAG = "RcsHarassmentInterceptionService";
    private Context mContext;

    public RcsHarassmentInterceptionService(Context context) {
        this.mContext = context;
    }

    public boolean isImIntentEmpty(Bundle bundle) {
        boolean z = true;
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return true;
        }
        if (((Intent) bundle.getParcelable(ConstValues.HANDLE_KEY_AIDL_IMINTENT)) != null) {
            z = false;
        }
        return z;
    }

    public int getImIntentResult(Bundle bundle) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return -1;
        }
        Intent intent = (Intent) bundle.getParcelable(ConstValues.HANDLE_KEY_AIDL_IMINTENT);
        if (intent == null) {
            return -1;
        }
        long msgId = intent.getLongExtra("msg_id", -1);
        int msgType = intent.getIntExtra("msg_type", 0);
        String msgPeerNum = intent.getStringExtra("msg_peer_num");
        if (-1 == msgId || msgType == 0) {
            return -1;
        }
        return StrategyManager.getInstance(this.mContext).allpyStrategyForIm(new ImIntentWrapper(msgId, msgType, msgPeerNum, intent, intent.getStringExtra("msg_local_num")));
    }
}
