package com.huawei.systemmanager.netassistant.netapp.ui;

import android.app.Fragment;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class NetAppListContainerFragment extends Fragment implements MessageHandler {
    private static final int MSG_ADD_SEC_PAGE = 201;
    private static final String TAG = NetAppListContainerFragment.class.getSimpleName();
    GenericHandler mHandler;
    private View mLayout;
    SubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    ViewPager mViewPager;
    private SubTabWidget subTabWidget = null;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new GenericHandler(this);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.net_app_list_activity, container, false);
        this.mViewPager = (ViewPager) this.mLayout.findViewById(R.id.pager);
        this.subTabWidget = (SubTabWidget) this.mLayout.findViewById(R.id.subTab_layout);
        initializeSubTabs();
        return this.mLayout;
    }

    private void initializeSubTabs() {
        this.mSubTabFragmentPagerAdapter = new SubTabFragmentPagerAdapter(getChildFragmentManager(), GlobalContext.getContext(), this.mViewPager, this.subTabWidget);
        SubTab subTab = this.subTabWidget.newSubTab(getString(R.string.net_assistant_installed_app_title));
        NetAppListFragment frag1 = new NetAppListFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt("app_type", 0);
        subTab.setSubTabId(R.id.systemmanager_applist_net_assistant_installed_app_title);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, frag1, bundle1, true);
        this.mHandler.sendEmptyMessageDelayed(201, 100);
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 201:
                SubTab subTab = this.subTabWidget.newSubTab(getString(R.string.net_assistant_system_app_title));
                Bundle bundle2 = new Bundle();
                bundle2.putInt("app_type", 1);
                this.mSubTabFragmentPagerAdapter.addSubTab(subTab, new NetAppListFragment(), bundle2, false);
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
