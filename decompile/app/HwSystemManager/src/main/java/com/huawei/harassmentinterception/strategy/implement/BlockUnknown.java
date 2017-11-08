package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.MmsInterceptionHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.systemmanager.util.HwLog;

public class BlockUnknown extends AbsStrategy {
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            boolean open = false;
            if (opKind == 1) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_UNKONW_CALL, opCard);
            }
            if (open) {
                return new BlockUnknown(ctx);
            }
            return null;
        }
    };
    public String TAG = "BlockUnknown";

    public BlockUnknown(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "BU";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        SmsMsgInfo smsMsg = smsIntentWrapper.getSmsMsgInfo();
        if (!CommonHelper.isInvalidPhoneNumber(smsMsg.getPhone())) {
            return super.handleSms(smsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleSms: Invalid number , should be blocked . " + smsMsg.getPhone());
        if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsMsg, 2)) {
            return 1;
        }
        HwLog.w(this.TAG, "handleSms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        MessageInfo mmsMsg = mmsIntentWrapper.getMsgInfo();
        if (!CommonHelper.isInvalidPhoneNumber(mmsMsg.getPhone())) {
            return super.handleMms(mmsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleMms: Invalid number , should be blocked.");
        if (MmsInterceptionHelper.addToInterceptRecord(this.mContext, mmsMsg, 2)) {
            return 1;
        }
        HwLog.w(this.TAG, "handleMms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        if (!CommonHelper.isInvalidPhoneNumber(inCommingCall.getNumber(), inCommingCall.getPersentation())) {
            return super.handleIncomingCall(inCommingCall);
        }
        HwLog.i(this.TAG, "BlockUnknown, handleIncomingCall, should be blocked.");
        inCommingCall.setReason(new BlockReason(2, 1));
        AbsStrategy.putLatestBlockCall(inCommingCall);
        return 1;
    }

    public int getKey() {
        return 9;
    }
}
