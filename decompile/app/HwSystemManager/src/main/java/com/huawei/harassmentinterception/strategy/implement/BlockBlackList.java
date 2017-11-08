package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.KeywordsInfo;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.util.MmsBlackListHelper;
import com.huawei.harassmentinterception.util.MmsInterceptionHelper;
import com.huawei.harassmentinterception.util.SmsBlackListHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.rcs.db.RcsDBAdapter;
import com.huawei.rcs.util.HwRcsFeatureEnabler;
import com.huawei.systemmanager.util.HwLog;

public class BlockBlackList extends AbsStrategy {
    public static final String TAG = "BlockBlackList";
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            if (ConstValues.isSupportNB()) {
                return new BlockBlackList(ctx);
            }
            boolean open = false;
            if (opKind == 1) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_BLACK_LIST_CALL, opCard);
            } else if (opKind == 2) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_BLOCK_BLACK_LIST_MSG, opCard);
            }
            if (open) {
                return new BlockBlackList(ctx);
            }
            return null;
        }
    };

    public BlockBlackList(Context context) {
        super(context);
    }

    public String getPrintfFlag() {
        return "BB";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        SmsMsgInfo smsInfo = smsIntentWrapper.getSmsMsgInfo();
        int nHandleResult = SmsBlackListHelper.handleSmsByBlackList(this.mContext, smsInfo);
        if (nHandleResult != 0) {
            smsInfo.setName(DBAdapter.getNameFromBlacklist(this.mContext, smsInfo.getPhone()));
            if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsInfo, 1)) {
                return nHandleResult;
            }
            HwLog.w(TAG, "handleSms: Fail to addToInterceptRecord ,skip");
            return 0;
        }
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
        if (HwRcsFeatureEnabler.isRcsEnabled()) {
            int nHandleResult = DBAdapter.getRcs().handleRcsByBlackList(this.mContext, imIntentWrapper.getImMsgId(), imIntentWrapper.getImMsgType(), imIntentWrapper.getMmsgPeerNum());
            HwLog.i(TAG, "blockblacklist handleIm, result is:" + nHandleResult);
            if (nHandleResult == 0 || 1 == nHandleResult) {
                return nHandleResult;
            }
            int keywordsResult = checkImKeywords(imIntentWrapper);
            if (keywordsResult != 0 && 1 != keywordsResult) {
                return super.handleIm(imIntentWrapper);
            }
            HwLog.i(TAG, "handleIm keywords check result is:" + keywordsResult);
            return keywordsResult;
        }
        HwLog.w(TAG, "handleIm  Rcs is not enabled!");
        return super.handleIm(imIntentWrapper);
    }

    private int checkImKeywords(ImIntentWrapper imIntentWrapper) {
        String phone = imIntentWrapper.getMmsgPeerNum();
        long msgId = imIntentWrapper.getImMsgId();
        if (DBAdapter.isContact(this.mContext, phone)) {
            HwLog.i(TAG, "block keywords handleIm: Passed by contacts");
            return -1;
        } else if (DBAdapter.getRcs().isFileTypeFromMsgType(imIntentWrapper.getImMsgType())) {
            HwLog.i(TAG, "block keywords handleIm:Ft not block");
            return -1;
        } else {
            String msgInfo = RcsDBAdapter.querySingleImMsgInfo(this.mContext, msgId);
            if (TextUtils.isEmpty(msgInfo)) {
                HwLog.i(TAG, "handleIm: Empty msg info");
                return -1;
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
            return -1;
        }
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        MessageInfo mmsMsgInfo = mmsIntentWrapper.getMsgInfo();
        HwLog.i(TAG, "handleMms");
        int nHandleResult = MmsBlackListHelper.handleMmsByBlackList(this.mContext, mmsMsgInfo);
        if (nHandleResult == 0) {
            return super.handleMms(mmsIntentWrapper);
        }
        HwLog.i(TAG, "handleMms: start to check this should be really add to record db");
        mmsMsgInfo.setName(DBAdapter.getNameFromBlacklist(this.mContext, mmsMsgInfo.getPhone()));
        if (MmsInterceptionHelper.addToInterceptRecord(this.mContext, mmsIntentWrapper.getMsgInfo(), 1)) {
            return nHandleResult;
        }
        HwLog.w(TAG, "handleMms: Fail to addToInterceptRecord ,skip");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        int nHandleResult = SmsBlackListHelper.handleIncomingCallByBlacklist(this.mContext, inCommingCall.getNumber());
        if (nHandleResult == 0) {
            return super.handleIncomingCall(inCommingCall);
        }
        inCommingCall.setReason(new BlockReason(1, 1));
        AbsStrategy.putLatestBlockCall(inCommingCall);
        return nHandleResult;
    }

    public int getKey() {
        return 11;
    }
}
