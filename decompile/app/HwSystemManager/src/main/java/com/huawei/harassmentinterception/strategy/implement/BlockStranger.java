package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.util.MmsInterceptionHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.util.HwLog;

public class BlockStranger extends AbsStrategy {
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            boolean open = false;
            if (opKind == 1) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_STRANGER_CALL, opCard);
            } else if (opKind == 2) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_STRANGER_MSG, opCard);
            }
            if (open) {
                return new BlockStranger(ctx);
            }
            return null;
        }
    };
    public String TAG = "BlockStranger";

    public BlockStranger(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "BS";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        SmsMsgInfo smsMsg = smsIntentWrapper.getSmsMsgInfo();
        if (DBAdapter.isContact(this.mContext, smsMsg.getPhone())) {
            return super.handleSms(smsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleSms: Not contact ,should be blocked");
        if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsMsg, 3)) {
            return 1;
        }
        HwLog.w(this.TAG, "handleSms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        MessageInfo mmsMsg = mmsIntentWrapper.getMsgInfo();
        if (DBAdapter.isContact(this.mContext, mmsMsg.getPhone())) {
            return super.handleMms(mmsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleMms: Not contact ,should be blocked");
        if (MmsInterceptionHelper.addToInterceptRecord(this.mContext, mmsMsg, 3)) {
            return 1;
        }
        HwLog.w(this.TAG, "handleMms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        if (DBAdapter.isContact(this.mContext, inCommingCall.getNumber())) {
            return super.handleIncomingCall(inCommingCall);
        }
        HwLog.i(this.TAG, "handleIncomingCall: Not contact,   should be blocked");
        inCommingCall.setReason(new BlockReason(3, 1));
        AbsStrategy.putLatestBlockCall(inCommingCall);
        return 1;
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return super.handleIm(imIntentWrapper);
        }
        int nHandleResult = DBAdapter.getRcs().handleRcsByBlockStranger(this.mContext, imIntentWrapper.getImMsgId(), imIntentWrapper.getImMsgType(), imIntentWrapper.getMmsgPeerNum());
        if (nHandleResult == 0) {
            return super.handleIm(imIntentWrapper);
        }
        HwLog.i(this.TAG, "handleIm, did block stranger");
        return nHandleResult;
    }

    public int getKey() {
        return 13;
    }
}
