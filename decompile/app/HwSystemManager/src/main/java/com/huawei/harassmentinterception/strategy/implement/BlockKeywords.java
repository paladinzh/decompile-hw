package com.huawei.harassmentinterception.strategy.implement;

import android.content.Context;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.rcs.db.RcsDBAdapter;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.util.HwLog;

public class BlockKeywords extends AbsStrategy {
    private static final String TAG = "BlockKeywords";

    public BlockKeywords(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return TAG;
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        if (DBAdapter.isContact(this.mContext, smsIntentWrapper.getSmsMsgInfo().getPhone())) {
            HwLog.i(TAG, "handleSms: Passed by contacts");
            return 0;
        }
        String msgInfo = smsIntentWrapper.getSmsMsgInfo().getBody();
        if (TextUtils.isEmpty(msgInfo)) {
            HwLog.i(TAG, "handleSms: Empty msg info");
            return 0;
        }
        for (KeywordsInfo keywordInfo : DBAdapter.getKeywordsList(this.mContext)) {
            if (keywordInfo.isMatch(msgInfo)) {
                if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsIntentWrapper.getSmsMsgInfo(), 5)) {
                    HwLog.i(TAG, "handleSms: Should block");
                    return 1;
                }
                HwLog.w(TAG, "handleSms: Fail to addToInterceptRecord ,skip");
                return 0;
            }
        }
        return super.handleSms(smsIntentWrapper);
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        HwLog.i(TAG, "block keywords handleIm");
        if (!HwRcsFeatureEnabler.isRcsEnabled()) {
            return super.handleIm(imIntentWrapper);
        }
        String phone = imIntentWrapper.getMmsgPeerNum();
        long msgId = imIntentWrapper.getImMsgId();
        if (DBAdapter.isContact(this.mContext, phone)) {
            HwLog.i(TAG, "block keywords handleIm: Passed by contacts");
            return 0;
        } else if (DBAdapter.getRcs().isFileTypeFromMsgType(imIntentWrapper.getImMsgType())) {
            HwLog.i(TAG, "block keywords handleIm:Ft not block");
            return 0;
        } else {
            String msgInfo = RcsDBAdapter.querySingleImMsgInfo(this.mContext, msgId);
            if (TextUtils.isEmpty(msgInfo)) {
                HwLog.i(TAG, "handleIm: Empty msg info");
                return 0;
            }
            for (KeywordsInfo keywordInfo : DBAdapter.getKeywordsList(this.mContext)) {
                if (keywordInfo.isMatch(msgInfo)) {
                    if (DBAdapter.getRcs().addRcsToInterceptRecord(this.mContext, msgId, imIntentWrapper.getImMsgType(), 5)) {
                        HwLog.i(TAG, "handleIm: Should block");
                        return 1;
                    }
                    HwLog.w(TAG, "handleIm: Fail to addRcsToInterceptRecord ,skip");
                    return 0;
                }
            }
            return super.handleIm(imIntentWrapper);
        }
    }

    public int getKey() {
        return 0;
    }
}
