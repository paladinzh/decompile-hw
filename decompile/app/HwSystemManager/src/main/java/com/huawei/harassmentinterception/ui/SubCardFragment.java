package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.view.View;
import android.widget.ListView;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.ui.CallIntellBlockFragment.CallIntellBlockActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.preference.TextArrowPreferenceCompat;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class SubCardFragment extends PreferenceFragment implements OnPreferenceChangeListener {
    private static final String KEY_BLOCK_BLACK_LIST_CALL_DEVIDE_LINE = "harass_call_block_blacklist_devideline";
    private static final String KEY_BLOCK_BLACK_LIST_MSG_DEVIDE_LINE = "harass_msg_block_blacklist_divideline";
    private static final String KEY_CALL_RULES_CATEGORY = "harassment_call_rules";
    private static final String KEY_INTELL_BLOCK_CALL_DEVIDE_LINE = "harass_call_block_intell_devideline";
    private static final String KEY_INTELL_BLOCK_MSG_DEVIDE_LINE = "harass_msg_block_intell_devideline";
    private static final String KEY_MESSAGE_RULES_CATEGORY = "harassment_msg_rules";
    private static final String TAG = "SubCardFragment";
    private SwitchPreference mBlackListCallPerfer;
    private SwitchPreference mBlackListMsgPerfer;
    private SwitchPreference mBlockAllCallPrefer;
    private ContentObserver mDataChangedOberser = new ContentObserver(this.mHanlder) {
        public void onChange(boolean selfChange, Uri uri) {
            HwLog.i(SubCardFragment.TAG, "receivie backup end notify, refresh data");
            SubCardFragment.this.updateValue();
        }
    };
    private Handler mHanlder = new Handler();
    private OnPreferenceClickListener mIntellCallClicker = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            HwLog.i(SubCardFragment.TAG, "user click, perfer key:" + preference.getKey());
            Activity ac = SubCardFragment.this.getActivity();
            if (ac == null) {
                return false;
            }
            Intent intent = new Intent(ac, CallIntellBlockActivity.class);
            intent.putExtra(ConstValues.KEY_OP_CARD, SubCardFragment.this.mOpCard);
            SubCardFragment.this.startActivity(intent);
            HsmStat.statE((int) Events.E_HARASSMENT_CLICK_INTELL_HARASS_CALL, HsmStatConst.PARAM_SUB, String.valueOf(SubCardFragment.this.mOpCard));
            return true;
        }
    };
    private TextArrowPreferenceCompat mIntellCallPrefer;
    private SwitchPreference mIntellMsgPrefer;
    private int mOpCard = -1;
    private SwitchPreference mStrangerCallPrefer;
    private SwitchPreference mStrangerMsgPrefer;
    private SwitchPreference mUnknowCallPrefer;

    public void setOpcard(int opCard) {
        this.mOpCard = opCard;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.harassment_subcard_rules_perfer);
        initPreference();
        GlobalContext.getContext().getContentResolver().registerContentObserver(DBAdapter.BACKUP_END_URI, false, this.mDataChangedOberser);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ListView lv = (ListView) getView().findViewById(16908298);
        lv.setOverScrollMode(2);
        lv.setDivider(null);
    }

    public void onResume() {
        super.onResume();
        updateValue();
    }

    public void onDestroy() {
        super.onDestroy();
        GlobalContext.getContext().getContentResolver().unregisterContentObserver(this.mDataChangedOberser);
    }

    private void initPreference() {
        PreferenceCategory callPrefers = (PreferenceCategory) findPreference(KEY_CALL_RULES_CATEGORY);
        this.mIntellCallPrefer = TextArrowPreferenceCompat.createFromPerfer(callPrefers.findPreference(RulesOps.KEY_INTELL_BLOCK_CALL));
        this.mIntellCallPrefer.setOnPreferenceClickListener(this.mIntellCallClicker);
        this.mBlackListCallPerfer = (SwitchPreference) callPrefers.findPreference(RulesOps.KEY_BLOCK_BLACK_LIST_CALL);
        this.mBlackListCallPerfer.setOnPreferenceChangeListener(this);
        this.mUnknowCallPrefer = (SwitchPreference) callPrefers.findPreference(RulesOps.KEY_BLOCK_UNKONW_CALL);
        this.mUnknowCallPrefer.setOnPreferenceChangeListener(this);
        this.mStrangerCallPrefer = (SwitchPreference) callPrefers.findPreference(RulesOps.KEY_BLOCK_STRANGER_CALL);
        this.mStrangerCallPrefer.setOnPreferenceChangeListener(this);
        this.mBlockAllCallPrefer = (SwitchPreference) callPrefers.findPreference(RulesOps.KEY_BLOCK_ALL_CALL);
        this.mBlockAllCallPrefer.setOnPreferenceChangeListener(this);
        PreferenceCategory msgPrefers = (PreferenceCategory) findPreference(KEY_MESSAGE_RULES_CATEGORY);
        this.mIntellMsgPrefer = (SwitchPreference) msgPrefers.findPreference(RulesOps.KEY_INTELL_BLOCK_MSG);
        this.mIntellMsgPrefer.setOnPreferenceChangeListener(this);
        this.mBlackListMsgPerfer = (SwitchPreference) msgPrefers.findPreference(RulesOps.KEY_BLOCK_BLACK_LIST_MSG);
        this.mBlackListMsgPerfer.setOnPreferenceChangeListener(this);
        this.mStrangerMsgPrefer = (SwitchPreference) msgPrefers.findPreference(RulesOps.KEY_BLOCK_STRANGER_MSG);
        this.mStrangerMsgPrefer.setOnPreferenceChangeListener(this);
        if (ConstValues.isSupportNB()) {
            callPrefers.removePreference(this.mBlackListCallPerfer);
            callPrefers.removePreference(callPrefers.findPreference(KEY_BLOCK_BLACK_LIST_CALL_DEVIDE_LINE));
            msgPrefers.removePreference(this.mBlackListMsgPerfer);
            msgPrefers.removePreference(msgPrefers.findPreference(KEY_BLOCK_BLACK_LIST_MSG_DEVIDE_LINE));
        }
        if (!CustomizeWrapper.shouldEnableIntelligentEngine()) {
            callPrefers.removePreference(this.mIntellCallPrefer.getPrference());
            callPrefers.removePreference(callPrefers.findPreference(KEY_INTELL_BLOCK_CALL_DEVIDE_LINE));
            msgPrefers.removePreference(this.mIntellMsgPrefer);
            msgPrefers.removePreference(msgPrefers.findPreference(KEY_INTELL_BLOCK_MSG_DEVIDE_LINE));
        }
    }

    public void updateValue() {
        long start = SystemClock.elapsedRealtime();
        ContentValues configs = RulesOps.getAllRules(GlobalContext.getContext());
        HwLog.i(TAG, "getAllRules cost time:" + (SystemClock.elapsedRealtime() - start));
        updateIntellCallPerfer(configs);
        updateSwitchState(configs, this.mUnknowCallPrefer);
        updateSwitchState(configs, this.mStrangerCallPrefer);
        updateSwitchState(configs, this.mBlockAllCallPrefer);
        updateSwitchState(configs, this.mIntellMsgPrefer);
        updateSwitchState(configs, this.mStrangerMsgPrefer);
        updateBlockAllState(this.mBlockAllCallPrefer.isChecked());
    }

    private void updateBlockAllState(boolean blockAll) {
        boolean enable = !blockAll;
        this.mIntellCallPrefer.setEnabled(enable);
        this.mBlackListCallPerfer.setEnabled(enable);
        this.mUnknowCallPrefer.setEnabled(enable);
        this.mStrangerCallPrefer.setEnabled(enable);
    }

    private void updateSwitchState(ContentValues configs, SwitchPreference perference) {
        perference.setChecked(RulesOps.isChecked(configs, perference.getKey(), this.mOpCard));
    }

    private void updateIntellCallPerfer(ContentValues configs) {
        CharSequence string;
        boolean checked = RulesOps.isChecked(configs, RulesOps.KEY_INTELL_BLOCK_CALL, this.mOpCard);
        TextArrowPreferenceCompat textArrowPreferenceCompat = this.mIntellCallPrefer;
        if (checked) {
            string = GlobalContext.getString(R.string.harassment_intelligent_call_blocking_on);
        } else {
            string = GlobalContext.getString(R.string.harassment_intelligent_call_blocking_off);
        }
        textArrowPreferenceCompat.setDetail(string);
        if (checked) {
            this.mIntellCallPrefer.setNetherSummary(getCallIntellDes(configs));
        } else {
            this.mIntellCallPrefer.setNetherSummary(GlobalContext.getString(R.string.harassment_intelligent_call_blocking_1));
        }
    }

    private String getCallIntellDes(ContentValues configs) {
        Context ctx = GlobalContext.getContext();
        List<String> itemDes = Lists.newArrayList();
        if (RulesOps.isChecked(configs, RulesOps.KEY_INTELL_SCAM_SWITCH, this.mOpCard)) {
            itemDes.add(ctx.getString(R.string.harassment_intell_block_call_des_scam));
        }
        if (RulesOps.isChecked(configs, RulesOps.KEY_INTELL_HARASS_SWITCH, this.mOpCard)) {
            itemDes.add(ctx.getString(R.string.harassment_intell_block_call_des_harassing));
        }
        if (RulesOps.isChecked(configs, RulesOps.KEY_INTELL_ADVER_SWITCH, this.mOpCard)) {
            itemDes.add(ctx.getString(R.string.harassment_intell_block_call_des_advertising));
        }
        if (RulesOps.isChecked(configs, RulesOps.KEY_INTELL_ESTATE_SWITCH, this.mOpCard)) {
            itemDes.add(ctx.getString(R.string.harassment_intell_block_call_des_estate));
        }
        String str = "";
        switch (itemDes.size()) {
            case 1:
                return ctx.getString(R.string.harassment_intell_block_call_on_des_1, new Object[]{itemDes.get(0)});
            case 2:
                return ctx.getString(R.string.harassment_intell_block_call_on_des_2, new Object[]{itemDes.get(0), itemDes.get(1)});
            case 3:
                return ctx.getString(R.string.harassment_intell_block_call_on_des_3, new Object[]{itemDes.get(0), itemDes.get(1), itemDes.get(2)});
            case 4:
                return ctx.getString(R.string.harassment_intell_block_call_on_des_4, new Object[]{itemDes.get(0), itemDes.get(1), itemDes.get(2), itemDes.get(3)});
            default:
                HwLog.e(TAG, "getOnDescrpt error number");
                return str;
        }
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        boolean checked = ((Boolean) newValue).booleanValue();
        HwLog.i(TAG, "user click perfer:" + key + ", checked:" + checked + ", opCard:" + this.mOpCard);
        RulesOps.setSingleRuleChecked(getContext(), key, checked, this.mOpCard);
        if (preference == this.mBlockAllCallPrefer) {
            updateBlockAllState(checked);
        }
        int stateEventId = getStateEventIdByKey(key);
        if (stateEventId > 0) {
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_VAL;
            strArr[1] = checked ? String.valueOf("1") : String.valueOf("0");
            strArr[2] = HsmStatConst.PARAM_SUB;
            strArr[3] = String.valueOf(this.mOpCard);
            HsmStat.statE(stateEventId, strArr);
        }
        return true;
    }

    private int getStateEventIdByKey(String key) {
        if (RulesOps.KEY_BLOCK_STRANGER_CALL.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_STRANGER_CALL;
        }
        if (RulesOps.KEY_BLOCK_UNKONW_CALL.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_UNKNOW_CALL;
        }
        if (RulesOps.KEY_BLOCK_ALL_CALL.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_ALL_CALL;
        }
        if (RulesOps.KEY_INTELL_BLOCK_MSG.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_INTELL_MSG;
        }
        if (RulesOps.KEY_BLOCK_STRANGER_MSG.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_STRANGER_MSG;
        }
        HwLog.e(TAG, "getStateEventIdByKey called, unknow key:" + key);
        return -1;
    }
}
