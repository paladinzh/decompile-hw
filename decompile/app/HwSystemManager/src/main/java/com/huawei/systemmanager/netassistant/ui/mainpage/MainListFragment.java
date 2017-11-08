package com.huawei.systemmanager.netassistant.ui.mainpage;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceScreen;
import android.preference.TextArrowPreference;
import android.text.TextUtils;
import com.huawei.cust.HwCustUtils;
import com.huawei.netassistant.ui.NetAssistant4GInfoActivity;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.netassistant.netapp.ui.NetAppListActivity;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverActivity;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverConstants;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverManager;
import com.huawei.systemmanager.netassistant.traffic.datasaver.IDataSaver.View;
import com.huawei.systemmanager.netassistant.traffic.trafficranking.TrafficRankingListActivity;
import com.huawei.systemmanager.netassistant.utils.NatConst;

public class MainListFragment extends PreferenceFragment implements View {
    private static final String KEY_4G_TRAFFIC_LIST = "4g_traffic_ranking";
    private static final String KEY_NET_APP_LIST = "net_app_management";
    private static final String KEY_TRAFFIC_LIST = "traffic_ranking_list";
    private static final int TRAFFIC_NET_NUM = 4;
    private HwCustMainListFragment mCust;
    private DataSaverManager mDataSaverManager;
    TextArrowPreference mDataSaverPref;
    int mSubId = -1;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.netassistant_main_list_preference);
        this.mCust = (HwCustMainListFragment) HwCustUtils.createObj(HwCustMainListFragment.class, new Object[0]);
        Preference traffic4GListPreference = findPreference(KEY_4G_TRAFFIC_LIST);
        setTraffic4GLocalTitle(traffic4GListPreference);
        if (!(this.mCust == null || HsmSubsciptionManager.isMultiSubs())) {
            this.mCust.updatePreferenceTitle(getActivity(), traffic4GListPreference, -1);
        }
        initDataSaverSummary();
    }

    public void onResume() {
        super.onResume();
        if (this.mDataSaverManager != null) {
            this.mDataSaverManager.registerListener();
        }
    }

    private void initDataSaverSummary() {
        this.mDataSaverPref = (TextArrowPreference) findPreference(DataSaverConstants.KEY_DATA_SAVER_MODE);
        this.mDataSaverManager = new DataSaverManager(getContext(), this);
    }

    public void onPause() {
        super.onPause();
        if (this.mDataSaverManager != null) {
            this.mDataSaverManager.unRegisterListener();
        }
    }

    public void updateCardMessage(int subId) {
        this.mSubId = subId;
        int index = HsmSubsciptionManager.getSubIndex(this.mSubId);
        findPreference(KEY_TRAFFIC_LIST).setTitle(getString(R.string.net_assistant_traffic_list_with_card, new Object[]{Integer.valueOf(index + 1)}));
        Preference traffic4GListPreference = findPreference(KEY_4G_TRAFFIC_LIST);
        traffic4GListPreference.setTitle(getString(R.string.net_assistant_traffic_4G_list_with_card, new Object[]{Integer.valueOf(index + 1)}));
        if (this.mCust != null) {
            this.mCust.updatePreferenceTitle(getActivity(), traffic4GListPreference, subId);
        }
    }

    public boolean onPreferenceTreeClick(PreferenceScreen preferenceScreen, Preference preference) {
        if (TextUtils.equals(preference.getKey(), KEY_TRAFFIC_LIST)) {
            startActivity(TrafficRankingListActivity.class);
        } else if (TextUtils.equals(preference.getKey(), KEY_4G_TRAFFIC_LIST)) {
            startActivity(NetAssistant4GInfoActivity.class);
        } else if (TextUtils.equals(preference.getKey(), KEY_NET_APP_LIST)) {
            if (!Utility.isOwnerUser()) {
                return true;
            }
            startActivity(NetAppListActivity.class);
        } else if (TextUtils.equals(preference.getKey(), DataSaverConstants.KEY_DATA_SAVER_MODE)) {
            if (!Utility.isOwnerUser()) {
                return true;
            }
            startActivity(DataSaverActivity.class);
            HsmStat.statE(3600);
        }
        return super.onPreferenceTreeClick(preferenceScreen, preference);
    }

    private void startActivity(Class clasz) {
        Intent intent = new Intent();
        if (this.mSubId >= 0) {
            intent.putExtra(NatConst.KEY_SUBID, this.mSubId);
        } else {
            intent.putExtra(NatConst.KEY_SUBID, HsmSubsciptionManager.getDataDefaultSubId());
        }
        intent.setClass(getActivity(), clasz);
        startActivity(intent);
    }

    public void onWhiteListedChanged(int uid, boolean whiteListed) {
    }

    public void onBlacklistedChanged(int uid, boolean blackListed) {
    }

    public void onDataSaverStateChanged(boolean enable) {
        if (this.mDataSaverPref != null) {
            if (enable) {
                this.mDataSaverPref.setDetail(getString(R.string.data_saver_on));
            } else {
                this.mDataSaverPref.setDetail(getString(R.string.data_saver_off));
            }
        }
    }

    private void setTraffic4GLocalTitle(Preference traffic4gListPreference) {
        traffic4gListPreference.setTitle(getString(R.string.net_assistant_traffic_4G_list).replace(String.valueOf(4), Utility.getLocaleNumber(4)));
    }
}
