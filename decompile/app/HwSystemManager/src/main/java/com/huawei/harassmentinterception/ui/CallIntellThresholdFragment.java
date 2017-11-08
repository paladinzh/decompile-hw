package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.app.Fragment;
import android.content.ContentValues;
import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.db.RulesOps;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.widget.preference.TextArrowPreferenceCompat;
import com.huawei.systemmanager.customize.CustomizeWrapper;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class CallIntellThresholdFragment extends PreferenceFragment {
    private static final String TAG = "CallIntellThresholdFragment";
    private OnPreferenceClickListener mClicker = new OnPreferenceClickListener() {
        public boolean onPreferenceClick(Preference preference) {
            Activity ac = CallIntellThresholdFragment.this.getActivity();
            if (ac == null || !ac.isResumed()) {
                HwLog.e(CallIntellThresholdFragment.TAG, "onPreferenceClick, but ac is not in correct state!");
                return false;
            }
            ThresholdDialog dialogFrg = new ThresholdDialog();
            dialogFrg.setParams(preference.getKey(), CallIntellThresholdFragment.this.mOpcards);
            dialogFrg.show(ac.getFragmentManager(), "thresholdDialog");
            return true;
        }
    };
    private int mOpcards = -1;
    private List<TextArrowPreferenceCompat> mPrefers = HsmCollections.newArrayList();

    public static class CallIntellThresholdActivity extends SingleFragmentActivity {
        private int mOpCard = -1;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            if (CustomizeWrapper.shouldEnableIntelligentEngine()) {
                int opCard = getIntent().getIntExtra(ConstValues.KEY_OP_CARD, -1);
                if (opCard == 1 || opCard == 2) {
                    this.mOpCard = opCard;
                    return;
                }
                HwLog.e(CallIntellThresholdFragment.TAG, "invalidate opcard param:" + opCard);
                finish();
                return;
            }
            HwLog.e(CallIntellThresholdFragment.TAG, "not support intelligent call block!");
            finish();
        }

        public int getOpcard() {
            return this.mOpCard;
        }

        protected Fragment buildFragment() {
            return new CallIntellThresholdFragment();
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.harassment_call_threshold_prefer);
        this.mOpcards = ((CallIntellThresholdActivity) getActivity()).getOpcard();
        HwLog.i(TAG, "CallIntellBlockFragment oncreate, opCard:" + this.mOpcards);
        intPrefer();
    }

    public void onResume() {
        super.onResume();
        refreshData();
    }

    private void intPrefer() {
        this.mPrefers.clear();
        this.mPrefers.add(TextArrowPreferenceCompat.createFromPerfer(findPreference(RulesOps.KEY_INTELL_SCAM_VALUE)));
        this.mPrefers.add(TextArrowPreferenceCompat.createFromPerfer(findPreference(RulesOps.KEY_INTELL_HARASS_VALUE)));
        this.mPrefers.add(TextArrowPreferenceCompat.createFromPerfer(findPreference(RulesOps.KEY_INTELL_ADVER_VALUE)));
        this.mPrefers.add(TextArrowPreferenceCompat.createFromPerfer(findPreference(RulesOps.KEY_INTELL_ESTATE_VALUE)));
        for (TextArrowPreferenceCompat prefer : this.mPrefers) {
            prefer.setOnPreferenceClickListener(this.mClicker);
        }
    }

    public void refreshData() {
        ContentValues configs = RulesOps.getAllRules(GlobalContext.getContext());
        for (TextArrowPreferenceCompat prefer : this.mPrefers) {
            prefer.setDetail(getSummaryStr(RulesOps.getBlockIntValue(configs, prefer.getKey(), this.mOpcards)));
        }
    }

    private String getSummaryStr(int value) {
        Context ctx = GlobalContext.getContext();
        String summary = "";
        if (value == 50) {
            return ctx.getResources().getQuantityString(R.plurals.harassment_blocking_threshold_suggest_value, value, new Object[]{Integer.valueOf(value)});
        }
        return ctx.getResources().getQuantityString(R.plurals.harassment_blocking_threshold_value, value, new Object[]{Integer.valueOf(value)});
    }
}
