package com.huawei.harassmentinterception.strategy.implement;

import android.content.Context;
import com.google.common.base.Objects;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.Comparator;

public abstract class AbsStrategy {
    public static final int KEY_BLOCK_ALL = 3;
    public static final int KEY_BLOCK_BLACKLIST = 11;
    public static final int KEY_BLOCK_INTELLIGENT = 20;
    public static final int KEY_BLOCK_STRANGER = 13;
    public static final int KEY_BLOCK_UNKNOW = 9;
    public static final int KEY_PASS_ALL = 1;
    public static final int KEY_PASS_CONTACT = 7;
    public static final int KEY_PASS_WHITE_LIST = 5;
    private static final String TAG = "AbsStrategy";
    protected static final String TAG_HANDLE_CALL = "handleCall";
    protected static final String TAG_HANDLE_IM = "handleIm";
    protected static final String TAG_HANDLE_MMS = "handleMms";
    protected static final String TAG_HANDLE_SMS = "handleSms";
    public static final Comparator<AbsStrategy> sKeyComparator = new Comparator<AbsStrategy>() {
        public int compare(AbsStrategy lhs, AbsStrategy rhs) {
            return lhs.getKey() - rhs.getKey();
        }
    };
    private static InCommingCall sLastBlockCall;
    public Context mContext = null;
    protected AbsStrategy mNextStrategy = null;

    public abstract int getKey();

    public abstract String getPrintfFlag();

    public AbsStrategy(Context context) {
        this.mContext = context;
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        if (this.mNextStrategy == null) {
            return 0;
        }
        return this.mNextStrategy.handleSms(smsIntentWrapper);
    }

    public int handleIm(ImIntentWrapper imIntentWrapper) {
        if (this.mNextStrategy == null) {
            return 0;
        }
        return this.mNextStrategy.handleIm(imIntentWrapper);
    }

    public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
        if (this.mNextStrategy == null) {
            return 0;
        }
        return this.mNextStrategy.handleMms(mmsIntentWrapper);
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        if (this.mNextStrategy == null) {
            return 0;
        }
        return this.mNextStrategy.handleIncomingCall(inCommingCall);
    }

    public void setNextStrategy(AbsStrategy nextStrategy) {
        this.mNextStrategy = nextStrategy;
    }

    public AbsStrategy getNextStrategy() {
        return this.mNextStrategy;
    }

    public int cancelUpdate() {
        return 0;
    }

    public boolean hasStrategy(int key) {
        if (key == getKey()) {
            return true;
        }
        if (this.mNextStrategy == null) {
            return false;
        }
        return this.mNextStrategy.hasStrategy(key);
    }

    public void printf(StringBuilder stringBuilder) {
        stringBuilder.append(getPrintfFlag());
        if (this.mNextStrategy != null) {
            stringBuilder.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            this.mNextStrategy.printf(stringBuilder);
        }
    }

    public static void putLatestBlockCall(InCommingCall call) {
        synchronized (AbsStrategy.class) {
            sLastBlockCall = call;
        }
    }

    public static BlockReason getLasteBlockCall(String phone) {
        synchronized (AbsStrategy.class) {
            if (sLastBlockCall == null) {
                HwLog.e(TAG, "getLasteBlockCall called, but cache is null!");
                return null;
            }
            if (!Objects.equal(sLastBlockCall.getNumber(), phone)) {
                HwLog.e(TAG, "getLasteBlockCall called, but number not matched!!");
            }
            BlockReason blockReason = sLastBlockCall.getBlockReason();
            return blockReason;
        }
    }
}
