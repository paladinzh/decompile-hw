package com.huawei.systemmanager.netassistant.traffic.trafficranking;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.os.Message;
import android.support.v4.view.ViewPager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.huawei.netassistant.util.CommonConstantUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.GenericHandler;
import com.huawei.systemmanager.comm.component.GenericHandler.MessageHandler;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.utils.NatConst;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;

public class TrafficRankingListContainerFragment extends Fragment implements MessageHandler {
    private static final int MSG_ADD_SEC_PAGE = 201;
    private static final int[] PEROID = new int[]{2, 3, 1};
    private static final int[] SET_SUBTAB_TITLE_ID = new int[]{R.id.systemmanager_net_assistant_traffic_per_day, R.id.systemmanager_net_assistant_traffic_per_weekly, R.id.systemmanager_net_assistant_traffic_per_month};
    private static final int[] SUBTAB_TITLE_ID = new int[]{R.string.net_assistant_traffic_per_day, R.string.net_assistant_traffic_per_weekly, R.string.net_assistant_traffic_per_month};
    private static final String TAG = TrafficRankingListContainerFragment.class.getSimpleName();
    private GenericHandler mHandler;
    private String mImsi;
    private View mLayout;
    private TrafficSubTabFragmentPagerAdapter mSubTabFragmentPagerAdapter;
    private ViewPager mViewPager;
    private SubTabWidget subTabWidget = null;

    private static class TrafficSubTabFragmentPagerAdapter extends SubTabFragmentPagerAdapter {
        public TrafficSubTabFragmentPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public TrafficSubTabFragmentPagerAdapter(FragmentManager fm, Context context, ViewPager pager, SubTabWidget subTabWidget) {
            super(fm, context, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            super.onPageSelected(position);
            HsmStat.statE(Events.E_NETASSISTANT_TRAFFIC_RANKING_PAGE_CHANGE);
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mHandler = new GenericHandler(this);
        Bundle bundle = getArguments();
        if (bundle != null) {
            int subId = bundle.getInt(NatConst.KEY_SUBID, -1);
            if (subId >= 0) {
                int slotIndex = HsmSubsciptionManager.getSubIndex(subId);
                this.mImsi = HsmSubsciptionManager.getImsi(subId);
                if (HsmSubsciptionManager.isMultiSubs() && slotIndex < 0) {
                    HwLog.w(TAG, "get SimSlotIndex fail");
                }
            }
        }
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = inflater.inflate(R.layout.net_app_list_activity, container, false);
        this.mViewPager = (ViewPager) this.mLayout.findViewById(R.id.pager);
        this.subTabWidget = (SubTabWidget) this.mLayout.findViewById(R.id.subTab_layout);
        initializeSubTabs();
        return this.mLayout;
    }

    private void initializeSubTabs() {
        this.mSubTabFragmentPagerAdapter = new TrafficSubTabFragmentPagerAdapter(getChildFragmentManager(), GlobalContext.getContext(), this.mViewPager, this.subTabWidget);
        addSubTab(0, true);
        this.mHandler.sendEmptyMessageDelayed(201, 100);
    }

    private void addSubTab(int flag, boolean show) {
        SubTab subTab = this.subTabWidget.newSubTab(getString(SUBTAB_TITLE_ID[flag]));
        TrafficRankingFragment frag1 = new TrafficRankingFragment();
        Bundle bundle1 = new Bundle();
        bundle1.putInt(TrafficRankingFragment.EXTRA_TRAFFIC_PERIOD, PEROID[flag]);
        bundle1.putString(CommonConstantUtil.KEY_NETASSISTANT_IMSI, this.mImsi);
        subTab.setSubTabId(SET_SUBTAB_TITLE_ID[flag]);
        this.mSubTabFragmentPagerAdapter.addSubTab(subTab, frag1, bundle1, show);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mHandler.quiteLooper();
    }

    public void onHandleMessage(Message msg) {
        switch (msg.what) {
            case 201:
                addSubTab(1, false);
                addSubTab(2, false);
                return;
            default:
                return;
        }
    }
}
