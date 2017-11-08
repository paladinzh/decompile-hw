package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.RoamingOverLimitNotifyPrefer;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util;
import com.huawei.systemmanager.util.HwLog;

public class OverLimitNotifyFragment extends PreferenceFragment {
    private static final String TAG = "OverLimitNotifyFragment";
    private boolean isNetAssistantEnable;

    public static class OverLimitNotifyActivity extends SingleFragmentActivity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setActionBar();
            if (Util.getCardFromActivityIntent(this, OverLimitNotifyFragment.TAG) == null) {
                HwLog.i(OverLimitNotifyFragment.TAG, "activity oncreate card  is null!");
                finish();
            }
        }

        protected Fragment buildFragment() {
            return new OverLimitNotifyFragment();
        }

        private void setActionBar() {
            Intent intent = getIntent();
            if (intent != null && intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0) > 0) {
                setTitle(getString(R.string.net_assistant_flowexcess_notify_title_index, new Object[]{Integer.valueOf(intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0))}));
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.traffic_setting_overlimit_notify_preference);
        this.isNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        if (!this.isNetAssistantEnable) {
            getPreferenceScreen().removePreference(findPreference(RoamingOverLimitNotifyPrefer.TAG));
        }
        CardItem card = Util.getCardFromActivityIntent(getActivity(), TAG);
        if (card == null) {
            HwLog.e(TAG, "onCreate card is null");
        }
        Util.setCardToPreference(getPreferenceScreen(), card);
    }

    public void onResume() {
        super.onResume();
        Util.refreshPreferenceShow(getPreferenceScreen());
    }
}
