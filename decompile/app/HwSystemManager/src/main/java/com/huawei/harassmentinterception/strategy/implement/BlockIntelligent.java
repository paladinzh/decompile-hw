package com.huawei.harassmentinterception.strategy.implement;

import android.content.ContentValues;
import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.BlockReason;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.preloadrule.PreloadRuelMgr;
import com.huawei.harassmentinterception.strategy.IntellEngineController;
import com.huawei.harassmentinterception.util.CloudNumberHelper;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.harassmentinterception.util.SmsInterceptionHelper;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import java.util.Map;

public class BlockIntelligent extends AbsStrategy {
    public static final String TAG = "BlockIntelligent";
    public static final StrategyFactory sFactory = new StrategyFactory() {
        public AbsStrategy create(Context ctx, ContentValues configs, int opKind, int opCard) {
            if (!CustomizeWrapper.shouldEnableIntelligentEngine()) {
                return null;
            }
            boolean open = false;
            if (opKind == 1) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_INTELL_BLOCK_CALL, opCard);
            } else if (opKind == 2) {
                open = RulesOps.isChecked(configs, RulesOps.KEY_INTELL_BLOCK_MSG, opCard);
            }
            if (open) {
                return new BlockIntelligent(ctx, opKind, configs, opCard);
            }
            return null;
        }
    };
    private Map<Integer, CallBlockType> mTypes = HsmCollections.newArrayMap();

    public static abstract class CallBlockType {
        public final boolean isOpen;
        public final int userMarkCount;

        public abstract String getName();

        public abstract int getNumberMarkType();

        public abstract int getReason();

        public CallBlockType(boolean isOpen, int markCount) {
            this.userMarkCount = markCount;
            this.isOpen = isOpen;
        }
    }

    public class AdvirtiseType extends CallBlockType {
        public AdvirtiseType(boolean isOpen, int markCount) {
            super(isOpen, markCount);
        }

        public int getReason() {
            return 23;
        }

        public int getNumberMarkType() {
            return 53;
        }

        public String getName() {
            return "adv";
        }
    }

    public class EstateType extends CallBlockType {
        public EstateType(boolean isOpen, int markCount) {
            super(isOpen, markCount);
        }

        public int getReason() {
            return 24;
        }

        public int getNumberMarkType() {
            return 51;
        }

        public String getName() {
            return "est";
        }
    }

    public class HarassBlockType extends CallBlockType {
        public HarassBlockType(boolean isOpen, int markCount) {
            super(isOpen, markCount);
        }

        public int getReason() {
            return 21;
        }

        public int getNumberMarkType() {
            return 50;
        }

        public String getName() {
            return "har";
        }
    }

    public class ScamBlockType extends CallBlockType {
        public ScamBlockType(boolean isOpen, int markCount) {
            super(isOpen, markCount);
        }

        public int getReason() {
            return 22;
        }

        public int getNumberMarkType() {
            return 54;
        }

        public String getName() {
            return "scam";
        }
    }

    public BlockIntelligent(Context context, int opKind, ContentValues configs, int opCard) {
        super(context);
        if (opKind == 1) {
            initCallBlockType(context, configs, opCard);
        }
    }

    public String getPrintfFlag() {
        return "BI";
    }

    public int handleSms(SmsIntentWrapper smsIntentWrapper) {
        if (PreloadRuelMgr.getInstance(this.mContext).shouldCheck(smsIntentWrapper)) {
            String phoneNumber = smsIntentWrapper.getSmsMsgInfo().getPhone();
            if (DBAdapter.isSmsContact(this.mContext, phoneNumber) || DBAdapter.isCallContact(this.mContext, phoneNumber)) {
                HwLog.i(TAG, "handleSms: Passed by Sms or call contacts");
                return 0;
            } else if (DBAdapter.isContact(this.mContext, phoneNumber)) {
                HwLog.i(TAG, "handleSms: Passed by contacts");
                return 0;
            } else if (CloudNumberHelper.isCloudNumber(this.mContext, phoneNumber)) {
                HwLog.i(TAG, "handleSms: Passed by system white list");
                return 0;
            } else if (HotlineNumberHelper.isHotlineNumber(this.mContext, phoneNumber)) {
                HwLog.i(TAG, "handleSms: Passed by yellow page");
                return 0;
            } else if (!IntellEngineController.handleSms(this.mContext, smsIntentWrapper)) {
                return super.handleSms(smsIntentWrapper);
            } else {
                if (SmsInterceptionHelper.addToInterceptRecord(this.mContext, smsIntentWrapper.getSmsMsgInfo(), 6)) {
                    HwLog.i(TAG, "handleSms: Spam message ,should be blocked");
                    return 1;
                }
                HwLog.w(TAG, "handleSms: Fail to addToInterceptRecord ,skip");
                return 0;
            }
        }
        HwLog.i(TAG, "handleSms: Passed by preload rules");
        return 0;
    }

    public int handleIncomingCall(InCommingCall inCommingCall) {
        String phoneNumber = inCommingCall.getNumber();
        if (CommonHelper.isInvalidPhoneNumber(phoneNumber)) {
            HwLog.i(TAG, "handleIncomingCall its a invalid phone number");
            return super.handleIncomingCall(inCommingCall);
        } else if (DBAdapter.isContact(this.mContext, phoneNumber)) {
            HwLog.i(TAG, "handleIncomingCall: Passed by contacts");
            return 0;
        } else if (CloudNumberHelper.isCloudNumber(this.mContext, phoneNumber)) {
            HwLog.i(TAG, "handleIncomingCall: Passed by system white list");
            return 0;
        } else if (HotlineNumberHelper.isHotlineNumber(this.mContext, phoneNumber)) {
            HwLog.i(TAG, "handleIncomingCall: Passed by yellow page");
            return 0;
        } else {
            int handleResult = checkNumberMark(inCommingCall);
            if (handleResult == 0 || handleResult == 1) {
                return handleResult;
            }
            return super.handleIncomingCall(inCommingCall);
        }
    }

    public int getKey() {
        return 20;
    }

    private int checkNumberMark(InCommingCall inCommingCall) {
        if (inCommingCall == null) {
            HwLog.i(TAG, "inCommingCall is null");
            return -1;
        }
        int markType = inCommingCall.getMarkType();
        CallBlockType blockType = (CallBlockType) this.mTypes.get(Integer.valueOf(markType));
        if (blockType == null) {
            HwLog.i(TAG, "checkNumberMark: Passed by local custom type, type:" + markType);
            return 0;
        }
        if (!inCommingCall.getIsLocal()) {
            int markCount = inCommingCall.getMarkCount();
            if (markCount < 0) {
                markCount = -markCount;
                HwLog.i(TAG, "adjust markCount:" + inCommingCall.getMarkCount());
            }
            if (blockType.isOpen && markCount >= blockType.userMarkCount) {
                inCommingCall.setReason(new BlockReason(blockType.getReason(), 2, markCount));
                AbsStrategy.putLatestBlockCall(inCommingCall);
                HwLog.i(TAG, "checkNumberMark, block cloud mark, reason:" + blockType.getName() + ", cloud mark count:" + markCount);
                return 1;
            }
        } else if (blockType.isOpen) {
            inCommingCall.setReason(new BlockReason(blockType.getReason(), 3));
            AbsStrategy.putLatestBlockCall(inCommingCall);
            HwLog.i(TAG, "checkNumberMark, block local mark, reason:" + blockType.getName());
            return 1;
        }
        return -1;
    }

    public void printf(StringBuilder stringBuilder) {
        stringBuilder.append(getPrintfFlag()).append(":");
        for (CallBlockType type : this.mTypes.values()) {
            if (type.isOpen) {
                stringBuilder.append(type.getName()).append("-").append(type.userMarkCount).append(" ");
            }
        }
        if (this.mNextStrategy != null) {
            stringBuilder.append(ConstValues.SEPARATOR_KEYWORDS_EN);
            this.mNextStrategy.printf(stringBuilder);
        }
    }

    private void initCallBlockType(Context context, ContentValues configs, int opCard) {
        List<CallBlockType> types = Lists.newArrayList();
        types.add(new ScamBlockType(RulesOps.isChecked(configs, RulesOps.KEY_INTELL_SCAM_SWITCH, opCard), RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_SCAM_VALUE, opCard)));
        types.add(new HarassBlockType(RulesOps.isChecked(configs, RulesOps.KEY_INTELL_HARASS_SWITCH, opCard), RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_HARASS_VALUE, opCard)));
        types.add(new AdvirtiseType(RulesOps.isChecked(configs, RulesOps.KEY_INTELL_ADVER_SWITCH, opCard), RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_ADVER_VALUE, opCard)));
        types.add(new EstateType(RulesOps.isChecked(configs, RulesOps.KEY_INTELL_ESTATE_SWITCH, opCard), RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_ESTATE_VALUE, opCard)));
        for (CallBlockType t : types) {
            this.mTypes.put(Integer.valueOf(t.getNumberMarkType()), t);
        }
    }
}
