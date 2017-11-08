package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util;
import com.huawei.systemmanager.util.HwLog;

public class AjustPackageFragment extends PreferenceFragment {
    private static final String TAG = "AjustPackageFragment";

    public static class AdjustPackageActivity extends SingleFragmentActivity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setActionBar();
            if (Util.getCardFromActivityIntent(this, AjustPackageFragment.TAG) == null) {
                HwLog.i(AjustPackageFragment.TAG, "activity oncreate card  is null!");
                finish();
            }
        }

        protected Fragment buildFragment() {
            return new AjustPackageFragment();
        }

        private void setActionBar() {
            Intent intent = getIntent();
            if (intent != null && intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0) > 0) {
                setTitle(getString(R.string.net_assistant_flow_package_adjust_index, new Object[]{Integer.valueOf(intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0))}));
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.traffic_setting_adjust_package_preference);
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
