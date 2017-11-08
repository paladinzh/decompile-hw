package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.util.MmsInterceptionHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.util.HwLog;

public class BlockAll extends AbsStrategy {
    public static final String TAG = "BlockAll";
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            boolean open = false;
            if (opKind == 1) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_ALL_CALL, opCard);
            }
            if (open) {
                return new BlockAll(ctx);
            }
            return null;
        }
    };

    public BlockAll(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "BA";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        HwLog.d(TAG, "handleSms, BlockAll");
        if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsIntentWrapper.getSmsMsgInfo(), 100)) {
            return 1;
        }
        HwLog.w(TAG, "handleSms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        HwLog.d(TAG, "handleMms, BlockAll");
        if (MmsInterceptionHelper.addToInterceptRecord(this.mContext, mmsIntentWrapper.getMsgInfo(), 100)) {
            return 1;
        }
        HwLog.w(TAG, "handleMms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        HwLog.d(TAG, "handleIm, BlockAll");
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return super.handleIm(imIntentWrapper);
        }
        if (DBAdapter.getRcs().addRcsToInterceptRecord(this.mContext, imIntentWrapper.getImMsgId(), imIntentWrapper.getImMsgType(), 100)) {
            return 1;
        }
        HwLog.w(TAG, "handleIm: Fail to addRcsToInterceptRecord ,skip");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        HwLog.d(TAG, "handleIncomingCall, BlockAll  presentation: " + inCommingCall.getPersentation());
        inCommingCall.setReason(new BlockReason(100, 1));
        AbsStrategy.putLatestBlockCall(inCommingCall);
        return 1;
    }

    public int getKey() {
        return 3;
    }
}
