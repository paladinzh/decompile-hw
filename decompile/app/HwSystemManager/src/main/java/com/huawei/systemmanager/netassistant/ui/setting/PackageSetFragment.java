package com.huawei.systemmanager.netassistant.ui.setting;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceFragment;
import android.widget.ListView;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.SingleFragmentActivity;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.netassistant.ui.setting.subpreference.Util;
import com.huawei.systemmanager.util.HwLog;

public class PackageSetFragment extends PreferenceFragment {
    private static final String TAG = "PackageSetFragment";

    public static class PackageSetActivity extends SingleFragmentActivity {
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setActionBar();
            if (Util.getCardFromActivityIntent(this, PackageSetFragment.TAG) == null) {
                HwLog.i(PackageSetFragment.TAG, "activity oncreate card  is null!");
                finish();
            }
        }

        protected Fragment buildFragment() {
            return new PackageSetFragment();
        }

        private void setActionBar() {
            Intent intent = getIntent();
            if (intent != null && intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0) > 0) {
                setTitle(getString(R.string.title_flow_package_settings, new Object[]{Integer.valueOf(intent.getIntExtra(CommonConstantUtil.KEY_NETASSISTANT_CARD_INDEX, 0))}));
            }
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.traffic_setting_packageset_preference);
        CardItem card = Util.getCardFromActivityIntent(getActivity(), TAG);
        if (card == null) {
            HwLog.e(TAG, "onCreate card is null");
        }
        Util.setCardToPreference(getPreferenceScreen(), card);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ListView lv = (ListView) getActivity().findViewById(16908298);
        if (lv != null) {
            lv.setDivider(null);
        }
    }

    public void onResume() {
        super.onResume();
        Util.refreshPreferenceShow(getPreferenceScreen());
    }
}
