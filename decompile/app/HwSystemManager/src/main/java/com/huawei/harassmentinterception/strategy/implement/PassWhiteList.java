package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.util.HwLog;

public class PassWhiteList extends AbsStrategy {
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            return new PassWhiteList(ctx);
        }
    };
    public String TAG = "PassWhiteList";

    public PassWhiteList(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "PW";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        HwLog.d(this.TAG, "handleSms: Implemented");
        if (!isWhiteList(smsIntentWrapper.getSmsMsgInfo().getPhone())) {
            return super.handleSms(smsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleSms: Passed by whitelist");
        return 0;
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        HwLog.d(this.TAG, "handleSms: Implemented");
        if (!isWhiteList(mmsIntentWrapper.getMsgInfo().getPhone())) {
            return super.handleMms(mmsIntentWrapper);
        }
        HwLog.i(this.TAG, "handleSms: Passed by whitelist");
        return 0;
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return super.handleIm(imIntentWrapper);
        }
        if (!isWhiteList(imIntentWrapper.getMmsgPeerNum())) {
            return super.handleIm(imIntentWrapper);
        }
        HwLog.i(this.TAG, "handleIm: Passed by whitelist");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        HwLog.d(this.TAG, "handleIncomingCall: implemented, presentation is " + inCommingCall.getPersentation());
        if (!isWhiteList(inCommingCall.getNumber())) {
            return super.handleIncomingCall(inCommingCall);
        }
        HwLog.i(this.TAG, "handleSms: Passed by whitelist");
        return 0;
    }

    private boolean isWhiteList(String phone) {
        return DBAdapter.isWhitelisted(this.mContext, phone);
    }

    public int getKey() {
        return 5;
    }
}
