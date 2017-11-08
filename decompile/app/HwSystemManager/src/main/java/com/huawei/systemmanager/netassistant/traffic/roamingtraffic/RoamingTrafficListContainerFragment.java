package com.huawei.systemmanager.netassistant.traffic.roamingtraffic;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.preference.PreferenceManager;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.netassistant.traffic.datasaver.DataSaverConstants;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class RoamingTrafficListContainerFragment extends Fragment implements MessageHandler {
    private static final int MSG_ADD_SEC_PAGE = 201;
    int mAppType = -1;
    GenericHandler mHandler = null;
    private View mLayout = null;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    ViewPager mViewPager;
    private SubTabWidget subTabWidget = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new GenericHandler(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.net_app_list_activity, container, false);
        dealDataSaver();
        this.mViewPager = (ViewPager) this.mLayout.findViewById(R.id.pager);
        this.subTabWidget = (SubTabWidget) this.mLayout.findViewById(R.id.subTab_layout);
        initializeSubTabs();
        return this.mLayout;
    }

    private void dealDataSaver() {
        Bundle bundle = getArguments();
        if (bundle != null) {
            this.mAppType = bundle.getInt("app_type", -1);
        }
        if (PreferenceManager.getDefaultSharedPreferences(GlobalContext.getContext()).getBoolean(DataSaverConstants.KEY_PREF_DATA_SAVER_SWITCH, false)) {
            LinearLayout dataSaverEnableHint = (LinearLayout) View.inflate(GlobalContext.getContext(), R.layout.data_saver_enable_hint, null);
            LinearLayout container = (LinearLayout) this.mLayout.findViewById(R.id.net_app_data_saver_enable_hint_container);
            container.removeAllViews();
            container.addView(dataSaverEnableHint);
        }
    }

    private void initializeSubTabs() {
        this.mSubTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(getChildFragmentManager(), GlobalContext.getContext(), this.mViewPager, this.subTabWidget);
        SubTab subTab = this.subTabWidget.newSubTab(getString(R.string.net_assistant_installed_app_title));
        Fragment frag1 = RoamingTrafficListFragment.newInstance();
        Bundle bundle1 = new Bundle();
        bundleDataSaverPosition(bundle1);
        bundle1.putBoolean(RoamingTrafficListFragment.ARG_IS_REMOVABLE, true);
        boolean toBeCurrent = this.mAppType == -1 || this.mAppType == 0;
        subTab.setSubTabId(R.id.systemmanager_roamingtrafficlist_net_assistant_installed_app_title);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, frag1, bundle1, toBeCurrent);
        this.mHandler.sendEmptyMessageDelayed(201, 100);
    }

    private void bundleDataSaverPosition(Bundle bundle1) {
        Bundle bundle = getArguments();
        if (bundle != null) {
            String action = bundle.getString("action");
            int uid = bundle.getInt(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID, -1);
            if (action != null && action.equals(DataSaverConstants.ACTION_FROM_DATA_SAVER)) {
                bundle1.putInt(DataSaverConstants.KEY_DATA_SAVER_WHITED_LIST_UID, uid);
            }
        }
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 201:
                SubTab subTab = this.subTabWidget.newSubTab(getString(R.string.net_assistant_system_app_title));
                Bundle bundle2 = new Bundle();
                bundleDataSaverPosition(bundle2);
                bundle2.putBoolean(RoamingTrafficListFragment.ARG_IS_REMOVABLE, false);
                Fragment frag2 = RoamingTrafficListFragment.newInstance();
                boolean toBeCurrent = this.mAppType != -1 && this.mAppType == 1;
                this.mSubTabFragmentPagerAdapter.addSubTab(subTab, frag2, bundle2, toBeCurrent);
                return;
            default:
                return;
        }
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.quiteLooper();
    }
}
