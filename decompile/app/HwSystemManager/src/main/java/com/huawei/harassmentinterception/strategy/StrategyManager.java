package com.huawei.harassmentinterception.strategy;

import android.content.ContentValues;
import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.CommonObject.ImIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.InCommingCall;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.common.CommonObject.MsgIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsIntentWrapper;
import com.huawei.harassmentinterception.common.CommonObject.SmsMsgInfo;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.strategy.implement.AbsStrategy;
import com.huawei.harassmentinterception.strategy.implement.BlockAll;
import com.huawei.harassmentinterception.strategy.implement.BlockBlackList;
import com.huawei.harassmentinterception.strategy.implement.BlockIntelligent;
import com.huawei.harassmentinterception.strategy.implement.BlockStranger;
import com.huawei.harassmentinterception.strategy.implement.BlockUnknown;
import com.huawei.harassmentinterception.strategy.implement.PassAll;
import com.huawei.harassmentinterception.strategy.implement.PassWhiteList;
import com.huawei.harassmentinterception.strategy.implement.StrategyFactory;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.Collections;
import java.util.List;

public class StrategyManager {
    private static final String TAG = "StrategyManager";
    private static StrategyManager sInstance = null;
    private List<StrategyFactory> callStrategyFactories = Lists.newArrayList(PassAll.sFactory, BlockAll.sFactory, PassWhiteList.sFactory, BlockBlackList.sFactory, BlockIntelligent.sFactory, BlockStranger.sFactory, BlockUnknown.sFactory);
    private AssembleStrategy mAssembleStrategy;
    private Context mContext = null;
    private List<StrategyFactory> smsStrategyFactories = Lists.newArrayList(PassAll.sFactory, BlockAll.sFactory, PassWhiteList.sFactory, BlockBlackList.sFactory, BlockIntelligent.sFactory, BlockStranger.sFactory);

    private static class AssembleStrategy extends AbsStrategy {
        private AbsStrategy mCard1CallRules;
        private AbsStrategy mCard1SmsRules;
        private AbsStrategy mCard2CallRules;
        private AbsStrategy mCard2SmsRules;

        public AssembleStrategy(Context context) {
            super(context);
        }

        public String getPrintfFlag() {
            return "AssembleStrategy";
        }

        public int getKey() {
            return 0;
        }

        public boolean hasSmsIntell() {
            return !this.mCard1SmsRules.hasStrategy(20) ? this.mCard2SmsRules.hasStrategy(20) : true;
        }

        public int handleIncomingCall(InCommingCall inCommingCall) {
            int subId = inCommingCall.getSubId();
            HwLog.i(StrategyManager.TAG, "handleCall, subId:" + subId);
            if (subId == 2) {
                return this.mCard2CallRules.handleIncomingCall(inCommingCall);
            }
            return this.mCard1CallRules.handleIncomingCall(inCommingCall);
        }

        public int handleIm(ImIntentWrapper imIntentWrapper) {
            HwLog.i(StrategyManager.TAG, "handleIm handleIm called");
            return this.mCard1SmsRules.handleIm(imIntentWrapper);
        }

        public int handleMms(MsgIntentWrapper mmsIntentWrapper) {
            MessageInfo info = mmsIntentWrapper.getMsgInfo();
            HwLog.i(StrategyManager.TAG, "handleMms, subId:" + info.getSubId());
            if (info.getSubId() == 1) {
                return this.mCard2SmsRules.handleMms(mmsIntentWrapper);
            }
            return this.mCard1SmsRules.handleMms(mmsIntentWrapper);
        }

        public int handleSms(SmsIntentWrapper smsIntentWrapper) {
            SmsMsgInfo info = smsIntentWrapper.getSmsMsgInfo();
            HwLog.i(StrategyManager.TAG, "handleSms, subId:" + info.getSubId());
            if (info.getSubId() == 1) {
                return this.mCard2SmsRules.handleSms(smsIntentWrapper);
            }
            return this.mCard1SmsRules.handleSms(smsIntentWrapper);
        }

        public void printf(StringBuilder stringBuilder) {
            if (this.mCard1CallRules == this.mCard2CallRules) {
                stringBuilder.append("DualcardcardSet[");
                this.mCard1CallRules.printf(stringBuilder);
                stringBuilder.append("],");
            } else {
                stringBuilder.append("c1call[");
                this.mCard1CallRules.printf(stringBuilder);
                stringBuilder.append("],");
                stringBuilder.append("c2call[");
                this.mCard2CallRules.printf(stringBuilder);
                stringBuilder.append("],");
            }
            stringBuilder.append("    ");
            if (this.mCard1SmsRules == this.mCard2SmsRules) {
                stringBuilder.append("DualcardcardSet[");
                this.mCard1SmsRules.printf(stringBuilder);
                stringBuilder.append("],");
                return;
            }
            stringBuilder.append("c1sms[");
            this.mCard1SmsRules.printf(stringBuilder);
            stringBuilder.append("],");
            stringBuilder.append("c2sms[");
            this.mCard2SmsRules.printf(stringBuilder);
            stringBuilder.append("],");
        }
    }

    public static synchronized StrategyManager getInstance(Context context) {
        StrategyManager strategyManager;
        synchronized (StrategyManager.class) {
            if (sInstance == null) {
                sInstance = new StrategyManager(context.getApplicationContext());
            }
            strategyManager = sInstance;
        }
        return strategyManager;
    }

    public void initStrategy() {
        getStrategy();
    }

    public synchronized int applyStrategyForSms(SmsIntentWrapper smsIntentWrapper) {
        return getStrategy().handleSms(smsIntentWrapper);
    }

    public synchronized int applyStrategyForMms(MsgIntentWrapper mmsIntentWrapper) {
        return getStrategy().handleMms(mmsIntentWrapper);
    }

    public synchronized int allpyStrategyForIm(ImIntentWrapper imIntentWrapper) {
        return getStrategy().handleIm(imIntentWrapper);
    }

    public synchronized int applyStrategyForCall(InCommingCall inCommingCall) {
        return getStrategy().handleIncomingCall(inCommingCall);
    }

    private StrategyManager(Context context) {
        this.mContext = context;
    }

    private synchronized AbsStrategy getStrategy() {
        if (this.mAssembleStrategy != null) {
            StringBuilder builder = new StringBuilder().append("getStrategy = ");
            this.mAssembleStrategy.printf(builder);
            HwLog.i(TAG, builder.toString());
            return this.mAssembleStrategy;
        }
        updateStrategyFromDb();
        return this.mAssembleStrategy;
    }

    public void updateStrategyFromDb() {
        Context ctx = this.mContext;
        AssembleStrategy assembleStrategy = new AssembleStrategy(ctx);
        ContentValues configs = RulesOps.getAllRules(ctx);
        boolean useDualCardSet = RulesOps.getDualcardSet(ctx);
        assembleStrategy.mCard1CallRules = createStratey(ctx, configs, 1, 1);
        if (useDualCardSet) {
            assembleStrategy.mCard2CallRules = createStratey(ctx, configs, 1, 2);
        } else {
            assembleStrategy.mCard2CallRules = assembleStrategy.mCard1CallRules;
        }
        assembleStrategy.mCard1SmsRules = createStratey(ctx, configs, 2, 1);
        if (useDualCardSet) {
            assembleStrategy.mCard2SmsRules = createStratey(ctx, configs, 2, 2);
        } else {
            assembleStrategy.mCard2SmsRules = assembleStrategy.mCard1SmsRules;
        }
        updateStrategy(assembleStrategy);
    }

    private AbsStrategy createStratey(Context ctx, ContentValues config, int opKind, int opCard) {
        List<StrategyFactory> strategyFactory = this.callStrategyFactories;
        if (opKind == 2) {
            strategyFactory = this.smsStrategyFactories;
        }
        List<AbsStrategy> strategyList = Lists.newArrayList();
        for (StrategyFactory factory : strategyFactory) {
            AbsStrategy s = factory.create(ctx, config, opKind, opCard);
            if (s != null) {
                strategyList.add(s);
            }
        }
        AbsStrategy head = null;
        AbsStrategy tail = null;
        Collections.sort(strategyList, AbsStrategy.sKeyComparator);
        for (AbsStrategy s2 : strategyList) {
            if (tail == null) {
                head = s2;
                tail = s2;
            } else {
                tail.setNextStrategy(s2);
                tail = s2;
            }
        }
        if (head != null) {
            return head;
        }
        HwLog.e(TAG, "createStratey head is null, something wrong!");
        return new PassAll(ctx);
    }

    private synchronized void updateStrategy(AssembleStrategy strategy) {
        if (strategy == null) {
            HwLog.e(TAG, "updateStrategy called, but strategy is null!");
            return;
        }
        if (this.mAssembleStrategy == null && CustomizeWrapper.shouldEnableIntelligentEngine()) {
            IntellEngineController.openSmsEngine(this.mContext);
        }
        this.mAssembleStrategy = strategy;
        StringBuilder builder = new StringBuilder();
        builder.append("st:");
        this.mAssembleStrategy.printf(builder);
        HwLog.i(TAG, builder.toString());
    }
}
