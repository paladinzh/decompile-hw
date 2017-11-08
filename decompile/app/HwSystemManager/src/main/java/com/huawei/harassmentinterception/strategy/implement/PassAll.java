package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.systemmanager.util.HwLog;

public class PassAll extends AbsStrategy {
    public static final String TAG = "PassAll";
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            if (ConstValues.isSupportNB() || CommonHelper.isInterceptionSettingOn(ctx)) {
                return null;
            }
            return new PassAll(ctx);
        }
    };

    public PassAll(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "PA";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        HwLog.i(TAG, "handleSms, PassAll");
        return 0;
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        HwLog.i(TAG, "handleMms, PassAll");
        return 0;
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        HwLog.i(TAG, "handleIncomingCall, PassAll presentation is " + inCommingCall.getPersentation());
        return 0;
    }

    public int getKey() {
        return 1;
    }
}
