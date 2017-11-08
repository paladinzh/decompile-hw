package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.widget.ListView;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.harassmentinterception.ui.CallIntellThresholdFragment.CallIntellThresholdActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.valueprefer.ValuePrefer;
import com.huawei.systemmanager.comm.widget.preference.TextArrowPreferenceCompat;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class CallIntellBlockFragment extends PreferenceFragment implements MessageHandler {
    private static final String KEY_ENSURE_DIALOG_TAG = "tag_ensure_dialog";
    private static final int MSG_CHECK_ALL_INTELL_ITEMS_STATE = 1;
    private static final int MSG_REFRESH_DATA = 2;
    private static final String PREFER_KEY_BLOCK_THREASHOLD = "harassment_block_threshold";
    private static final String TAG = "CallIntellBlockFragment";
    private SwitchPreference mAdverPrefer;
    private ContentObserver mDataChangedOberser = new ContentObserver(this.mHanlder) {
        public void onChange(boolean selfChange, Uri uri) {
            HwLog.i(CallIntellBlockFragment.TAG, "receivie backup end notify, refresh data");
            CallIntellBlockFragment.this.refreshData();
        }
    };
    private SwitchPreference mEstatePrefer;
    private Handler mHanlder = new GenericHandler(this);
    private SwitchPreference mHarassPrefer;
    private SwitchPreference mIntellPrefer;
    private OnPreferenceChangeListener mIntellPreferListener = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            String key = preference.getKey();
            Context ctx = GlobalContext.getContext();
            boolean checked = ((Boolean) newValue).booleanValue();
            RulesOps.setSingleRuleChecked(ctx, key, checked, CallIntellBlockFragment.this.mOpcard);
            CallIntellBlockFragment.this.updateSubItemState(checked);
            if (checked && CallIntellBlockFragment.this.checkIfAllSubItemUnchecked()) {
                RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_HARASS_SWITCH, true, CallIntellBlockFragment.this.mOpcard);
                RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_SCAM_SWITCH, true, CallIntellBlockFragment.this.mOpcard);
                RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_ADVER_SWITCH, true, CallIntellBlockFragment.this.mOpcard);
                RulesOps.setSingleRuleChecked(ctx, RulesOps.KEY_INTELL_ESTATE_SWITCH, true, CallIntellBlockFragment.this.mOpcard);
                CallIntellBlockFragment.this.mHanlder.sendEmptyMessage(2);
            }
            if (checked && CallIntellBlockFragment.this.checkAndConsumeEnsureDialogFlag(ctx)) {
                try {
                    new EnsureDialog().show(CallIntellBlockFragment.this.getFragmentManager(), CallIntellBlockFragment.KEY_ENSURE_DIALOG_TAG);
                } catch (Exception e) {
                    HwLog.w(CallIntellBlockFragment.TAG, "show ensure dialog failed!", e);
                }
            }
            HwLog.i(CallIntellBlockFragment.TAG, "user click, key:" + key + ", opCard:" + CallIntellBlockFragment.this.mOpcard + ", checked:" + checked);
            String[] strArr = new String[4];
            strArr[0] = HsmStatConst.PARAM_VAL;
            strArr[1] = checked ? String.valueOf("1") : String.valueOf("0");
            strArr[2] = HsmStatConst.PARAM_SUB;
            strArr[3] = String.valueOf(CallIntellBlockFragment.this.mOpcard);
            HsmStat.statE((int) Events.E_HARASSMENT_SET_BLOCK_INTELL_CALL, strArr);
            return true;
        }
    };
    private int mOpcard = -1;
    private SwitchPreference mScamPrefer;
    private OnPreferenceChangeListener mSubSwitchPreferClick = new OnPreferenceChangeListener() {
        public boolean onPreferenceChange(Preference preference, Object newValue) {
            Context ctx = GlobalContext.getContext();
            boolean checked = ((Boolean) newValue).booleanValue();
            String key = preference.getKey();
            RulesOps.setSingleRuleChecked(ctx, key, checked, CallIntellBlockFragment.this.mOpcard);
            CallIntellBlockFragment.this.mHanlder.sendEmptyMessage(1);
            HwLog.i(CallIntellBlockFragment.TAG, "user click, key:" + key + ", opCard:" + CallIntellBlockFragment.this.mOpcard + ", checked:" + checked);
            int eventId = CallIntellBlockFragment.this.getEventIdByKey(key);
            if (eventId > 0) {
                String[] strArr = new String[4];
                strArr[0] = HsmStatConst.PARAM_VAL;
                strArr[1] = checked ? String.valueOf("1") : String.valueOf("0");
                strArr[2] = HsmStatConst.PARAM_SUB;
                strArr[3] = String.valueOf(CallIntellBlockFragment.this.mOpcard);
                HsmStat.statE(eventId, strArr);
            }
            return true;
        }
    };
    private List<SwitchPreference> mSwitchPrefers = HsmCollections.newArrayList();
    private OnPreferenceClickListener mThresholdClicker = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            HwLog.i(CallIntellBlockFragment.TAG, "user click threshold prefer");
            Activity ac = CallIntellBlockFragment.this.getActivity();
            if (ac == null) {
                return false;
            }
            Intent intent = new Intent(ac, CallIntellThresholdActivity.class);
            intent.putExtra(ConstValues.KEY_OP_CARD, CallIntellBlockFragment.this.mOpcard);
            CallIntellBlockFragment.this.startActivity(intent);
            HsmStat.statE((int) Events.E_HARASSMENT_CLICK_INTELL_CALL_THRESHOLD, HsmStatConst.PARAM_SUB, String.valueOf(CallIntellBlockFragment.this.mOpcard));
            return true;
        }
    };
    private TextArrowPreferenceCompat mThresholdPrefer;

    public static class CallIntellBlockActivity extends SingleFragmentActivity {
        private int mOpCard = -1;

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
                int opCard = getIntent().getIntExtra(ConstValues.KEY_OP_CARD, -1);
                if (opCard == 1 || opCard == 2) {
                    this.mOpCard = opCard;
                    return;
                }
                HwLog.e(CallIntellBlockFragment.TAG, "invalidate opcard param:" + opCard);
                finish();
                return;
            }
            HwLog.e(CallIntellBlockFragment.TAG, "not support intelligent call block!");
            finish();
        }

        public int getOpcard() {
            return this.mOpCard;
        }

        protected Fragment buildFragment() {
            return new CallIntellBlockFragment();
        }
    }

    public static class EnsureDialog extends DialogFragment {
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Builder builder = new Builder(getActivity());
            builder.setTitle(R.string.harassment_intell_call_ensure_dialog_title);
            builder.setMessage(R.string.harassment_intell_call_ensure_dialog_content_1);
            builder.setPositiveButton(R.string.harassment_intell_call_ensure_dialog_ok, null);
            return builder.create();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.harassment_call_intell_block_prefer);
        initPreference();
        GlobalContext.getContext().getContentResolver().registerContentObserver(DBAdapter.BACKUP_END_URI, false, this.mDataChangedOberser);
        this.mOpcard = ((CallIntellBlockActivity) getActivity()).getOpcard();
        HwLog.i(TAG, "CallIntellBlockFragment oncreate, opCard:" + this.mOpcard);
    }

    private void initPreference() {
        this.mSwitchPrefers.clear();
        this.mIntellPrefer = (SwitchPreference) findPreference(RulesOps.KEY_INTELL_BLOCK_CALL);
        this.mIntellPrefer.setOnPreferenceChangeListener(this.mIntellPreferListener);
        this.mSwitchPrefers.add(this.mIntellPrefer);
        this.mScamPrefer = (SwitchPreference) findPreference(RulesOps.KEY_INTELL_SCAM_SWITCH);
        this.mScamPrefer.setOnPreferenceChangeListener(this.mSubSwitchPreferClick);
        this.mSwitchPrefers.add(this.mScamPrefer);
        this.mHarassPrefer = (SwitchPreference) findPreference(RulesOps.KEY_INTELL_HARASS_SWITCH);
        this.mHarassPrefer.setOnPreferenceChangeListener(this.mSubSwitchPreferClick);
        this.mSwitchPrefers.add(this.mHarassPrefer);
        this.mAdverPrefer = (SwitchPreference) findPreference(RulesOps.KEY_INTELL_ADVER_SWITCH);
        this.mAdverPrefer.setOnPreferenceChangeListener(this.mSubSwitchPreferClick);
        this.mSwitchPrefers.add(this.mAdverPrefer);
        this.mEstatePrefer = (SwitchPreference) findPreference(RulesOps.KEY_INTELL_ESTATE_SWITCH);
        this.mEstatePrefer.setOnPreferenceChangeListener(this.mSubSwitchPreferClick);
        this.mSwitchPrefers.add(this.mEstatePrefer);
        this.mThresholdPrefer = TextArrowPreferenceCompat.createFromPerfer(findPreference(PREFER_KEY_BLOCK_THREASHOLD));
        this.mThresholdPrefer.setNetherSummary(GlobalContext.getString(R.string.harassment_blocking_threshold_des));
        this.mThresholdPrefer.setOnPreferenceClickListener(this.mThresholdClicker);
    }

    public void onResume() {
        super.onResume();
        ListView lv = (ListView) getActivity().findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
        refreshData();
    }

    public void refreshData() {
        ContentValues configs = RulesOps.getAllRules(GlobalContext.getContext());
        for (SwitchPreference switchPerfer : this.mSwitchPrefers) {
            switchPerfer.setChecked(RulesOps.isChecked(configs, switchPerfer.getKey(), this.mOpcard));
        }
        updateSubItemState(this.mIntellPrefer.isChecked());
        refreshThresholdPreferShow(configs);
    }

    private void refreshThresholdPreferShow(ContentValues configs) {
        boolean showSuggest = true;
        for (Integer intValue : HsmCollections.newArrayList(Integer.valueOf(RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_SCAM_VALUE, this.mOpcard)), Integer.valueOf(RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_HARASS_VALUE, this.mOpcard)), Integer.valueOf(RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_ADVER_VALUE, this.mOpcard)), Integer.valueOf(RulesOps.getBlockIntValue(configs, RulesOps.KEY_INTELL_ESTATE_VALUE, this.mOpcard)))) {
            if (intValue.intValue() != 50) {
                showSuggest = false;
                break;
            }
        }
        String summary = "";
        if (showSuggest) {
            summary = GlobalContext.getContext().getResources().getQuantityString(R.plurals.harassment_blocking_threshold_suggest_value, 50, new Object[]{Integer.valueOf(50)});
        }
        this.mThresholdPrefer.setDetail(summary);
    }

    private void updateSubItemState(boolean intellPreferChecked) {
        this.mScamPrefer.setEnabled(intellPreferChecked);
        this.mHarassPrefer.setEnabled(intellPreferChecked);
        this.mAdverPrefer.setEnabled(intellPreferChecked);
        this.mEstatePrefer.setEnabled(intellPreferChecked);
        this.mThresholdPrefer.setEnabled(intellPreferChecked);
    }

    public void onDestroy() {
        super.onDestroy();
        GlobalContext.getContext().getContentResolver().unregisterContentObserver(this.mDataChangedOberser);
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 1:
                if (checkIfAllSubItemUnchecked()) {
                    this.mIntellPrefer.setChecked(false);
                    RulesOps.setSingleRuleChecked(GlobalContext.getContext(), RulesOps.KEY_INTELL_BLOCK_CALL, false, this.mOpcard);
                    updateSubItemState(false);
                    return;
                }
                return;
            case 2:
                refreshData();
                return;
            default:
                return;
        }
    }

    private boolean checkAndConsumeEnsureDialogFlag(Context ctx) {
        String ensureDialogKey = RulesOps.IF_FIRST_SET;
        if (ValuePrefer.getValueBoolean(ctx, ensureDialogKey, false)) {
            return false;
        }
        ValuePrefer.putValueBoolean(ctx, ensureDialogKey, true);
        return true;
    }

    private boolean checkIfAllSubItemUnchecked() {
        for (SwitchPreference switchPrefer : this.mSwitchPrefers) {
            if (!RulesOps.KEY_INTELL_BLOCK_CALL.equals(switchPrefer.getKey()) && switchPrefer.isChecked()) {
                return false;
            }
        }
        return true;
    }

    public Context getApplicationContext() {
        return GlobalContext.getContext();
    }

    private int getEventIdByKey(String key) {
        if (RulesOps.KEY_INTELL_HARASS_SWITCH.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_INTELL_HARASS_CALL;
        }
        if (RulesOps.KEY_INTELL_SCAM_SWITCH.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_INTELL_SCAM_CALL;
        }
        if (RulesOps.KEY_INTELL_ADVER_SWITCH.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_INTELL_ADVER_CALL;
        }
        if (RulesOps.KEY_INTELL_ESTATE_SWITCH.equals(key)) {
            return Events.E_HARASSMENT_SET_BLOCK_INTELL_ESTATE_CALL;
        }
        HwLog.e(TAG, "getEventIdByKey unknow key:" + key);
        return -1;
    }
}
