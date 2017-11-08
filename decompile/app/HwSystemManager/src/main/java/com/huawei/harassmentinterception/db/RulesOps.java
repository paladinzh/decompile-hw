package com.huawei.harassmentinterception.db;

import android.content.ContentValues;
import android.content.Context;
import android.text.TextUtils;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.valueprefer.ValuePair;
import com.huawei.systemmanager.comm.valueprefer.ValuePrefer;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Set;

public class RulesOps {
    public static final int DEFAULT_BLOCK_VALUE = 50;
    private static final boolean DEFUAL_DUALCARD_SET = false;
    private static final boolean DEFUAL_IF_FIRST_SET = false;
    public static final String IF_FIRST_SET = "harass_intellcallensuredialog_showed";
    public static final String KEY_BLOCK_ALL_CALL = "harass_call_block_all";
    public static final String KEY_BLOCK_BLACK_LIST_CALL = "harass_call_block_blacklist";
    public static final String KEY_BLOCK_BLACK_LIST_MSG = "harass_msg_block_blacklist";
    public static final String KEY_BLOCK_STRANGER_CALL = "harass_call_block_stranger";
    public static final String KEY_BLOCK_STRANGER_MSG = "harass_msg_block_stranger";
    public static final String KEY_BLOCK_UNKONW_CALL = "harass_call_block_unknown";
    public static final String KEY_CARD1_SUFFIX = "_card_1";
    public static final String KEY_CARD2_SUFFIX = "_card_2";
    public static final String KEY_DUALCARD_SET = "harass_dual_card_set";
    public static final String KEY_INTELL_ADVER_SWITCH = "harass_call_adver_switch";
    public static final String KEY_INTELL_ADVER_VALUE = "harass_call_adver_value";
    public static final String KEY_INTELL_BLOCK_CALL = "harass_call_block_intell";
    public static final String KEY_INTELL_BLOCK_MSG = "harass_msg_block_intell";
    public static final String KEY_INTELL_ESTATE_SWITCH = "harass_call_estate_switch";
    public static final String KEY_INTELL_ESTATE_VALUE = "harass_call_estate_value";
    public static final String KEY_INTELL_HARASS_SWITCH = "harass_call_harass_switch";
    public static final String KEY_INTELL_HARASS_VALUE = "harass_call_harass_value";
    public static final String KEY_INTELL_SCAM_SWITCH = "harass_call_scam_swith";
    public static final String KEY_INTELL_SCAM_VALUE = "harass_call_scam_value";
    public static final int MAX_BLOCK_VALUE = 200;
    public static final int MIN_BLOCK_VALUE = 1;
    private static final String TAG = "RulesOps";
    private static final Set<String> sCallIntellValuesKeys = HsmCollections.newArraySet();

    static {
        sCallIntellValuesKeys.add(KEY_INTELL_SCAM_VALUE);
        sCallIntellValuesKeys.add(KEY_INTELL_HARASS_VALUE);
        sCallIntellValuesKeys.add(KEY_INTELL_ADVER_VALUE);
        sCallIntellValuesKeys.add(KEY_INTELL_ESTATE_VALUE);
    }

    static void initRules(Context ctx) {
        HwLog.i(TAG, "initRules called");
        if (!ValuePrefer.putValueBulk(ctx, getAllRulesPairs())) {
            HwLog.e(TAG, "initRules failed!");
        }
        notifyRulesChanged(ctx);
    }

    public static boolean setDualcardSet(Context ctx, boolean dualcardSet) {
        boolean res = ValuePrefer.putValueString(ctx, KEY_DUALCARD_SET, Boolean.toString(dualcardSet));
        notifyRulesChanged(ctx);
        return res;
    }

    public static boolean getDualcardSet(Context ctx) {
        return Boolean.valueOf(ValuePrefer.getValueString(ctx, KEY_DUALCARD_SET, Boolean.toString(false))).booleanValue();
    }

    public static boolean getIfFirstSet(Context ctx) {
        return Boolean.valueOf(ValuePrefer.getValueString(ctx, IF_FIRST_SET, Boolean.toString(false))).booleanValue();
    }

    public static void setIfFirstSet(Context ctx) {
        if (!ValuePrefer.getValueBoolean(ctx, IF_FIRST_SET, false)) {
            HwLog.i(TAG, "putValueBooleantrue");
            ValuePrefer.putValueBoolean(ctx, IF_FIRST_SET, true);
        }
    }

    public static boolean setSingleRuleChecked(Context ctx, String key, boolean checked, int opCard) {
        boolean res = ValuePrefer.putValueString(ctx, getPerferKey(key, opCard), String.valueOf(checked));
        notifyRulesChanged(ctx);
        return res;
    }

    public static boolean setSingleBlockValue(Context ctx, String key, int value, int opCard) {
        if (!sCallIntellValuesKeys.contains(key)) {
            HwLog.e(TAG, "setSingleRuleInt called, invalidate key:" + key);
            return false;
        } else if (opCard == 1 || opCard == 2) {
            boolean res = ValuePrefer.putValueString(ctx, getPerferKey(key, opCard), String.valueOf(value));
            notifyRulesChanged(ctx);
            return res;
        } else {
            HwLog.e(TAG, "setSingleRuleInt called, invalidate opcard:" + opCard);
            return false;
        }
    }

    static boolean setRules(Context ctx, ArrayList<ValuePair> values) {
        boolean res = ValuePrefer.putValueBulk(ctx, values);
        notifyRulesChanged(ctx);
        return res;
    }

    public static ContentValues getAllRules(Context ctx) {
        return ValuePrefer.geValueBulk(ctx, getAllRulesPairs());
    }

    private static ArrayList<ValuePair> getAllRulesPairs() {
        ArrayList<ValuePair> pairs = HsmCollections.newArrayList();
        pairs.add(new ValuePair(KEY_DUALCARD_SET, Boolean.toString(false)));
        pairs.addAll(getCardRules(1));
        pairs.addAll(getCardRules(2));
        return pairs;
    }

    private static ArrayList<ValuePair> getCardRules(int opCard) {
        ArrayList<ValuePair> pairs = HsmCollections.newArrayList();
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_BLOCK_CALL, opCard), Boolean.toString(false)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_UNKONW_CALL, opCard), Boolean.toString(false)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_STRANGER_CALL, opCard), Boolean.toString(false)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_BLACK_LIST_CALL, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_ALL_CALL, opCard), Boolean.toString(false)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_BLOCK_MSG, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_BLACK_LIST_MSG, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_BLOCK_STRANGER_MSG, opCard), Boolean.toString(false)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_SCAM_SWITCH, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_HARASS_SWITCH, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_ADVER_SWITCH, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_ESTATE_SWITCH, opCard), Boolean.toString(true)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_SCAM_VALUE, opCard), String.valueOf(50)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_HARASS_VALUE, opCard), String.valueOf(50)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_ADVER_VALUE, opCard), String.valueOf(50)));
        pairs.add(new ValuePair(getPerferKey(KEY_INTELL_ESTATE_VALUE, opCard), String.valueOf(50)));
        return pairs;
    }

    public static final String getPerferKey(String key, int opCards) {
        return key + getSuffix(opCards);
    }

    private static final String getSuffix(int opCard) {
        switch (opCard) {
            case 1:
                return KEY_CARD1_SUFFIX;
            case 2:
                return KEY_CARD2_SUFFIX;
            default:
                HwLog.e(TAG, "getSuffix unknown opcard:" + opCard);
                return KEY_CARD1_SUFFIX;
        }
    }

    private static void notifyRulesChanged(Context ctx) {
        CommonHelper.notifyInterceptionSettingChange(ctx);
    }

    public static final boolean isChecked(ContentValues configs, String key, int opCards) {
        String value = configs.getAsString(getPerferKey(key, opCards));
        if (TextUtils.isEmpty(value)) {
            HwLog.e(TAG, "updateSwitchState can not find key:" + key + ", opcard:" + opCards);
        }
        return Boolean.parseBoolean(value);
    }

    public static int getBlockIntValue(ContentValues configs, String key, int opCards) {
        if (sCallIntellValuesKeys.contains(key)) {
            String valueStr = configs.getAsString(getPerferKey(key, opCards));
            int valueInt = -1;
            try {
                valueInt = Integer.parseInt(valueStr);
            } catch (RuntimeException e) {
                HwLog.e(TAG, "getIntValue failed, valueStr:" + valueStr, e);
            }
            if (valueInt < 1) {
                HwLog.e(TAG, "checkAndAjustBlockValue, inputValue:" + valueInt);
                return 50;
            } else if (valueInt <= 200) {
                return valueInt;
            } else {
                HwLog.e(TAG, "checkAndAjustBlockValue, inputValue:" + valueInt);
                return 50;
            }
        }
        HwLog.e(TAG, "getBlockIntValue failed! illegal key:" + key);
        return 50;
    }

    static void recoverFromContentValues(Context ctx, String key, String value) {
        ValuePrefer.putValueString(ctx, key, value);
    }

    static ContentValues getBackupValues(Context ctx) {
        return getAllRules(ctx);
    }

    public static ValuePair createValuePair(String key, int opcard, boolean checked) {
        return new ValuePair(getPerferKey(key, opcard), checked);
    }

    public static ValuePair createValuePair(String key, int opcard, int value) {
        return new ValuePair(getPerferKey(key, opcard), value);
    }
}
