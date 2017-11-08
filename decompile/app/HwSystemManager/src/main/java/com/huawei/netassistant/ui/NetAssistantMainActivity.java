package com.huawei.netassistant.ui;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.UserHandle;
import android.provider.SearchIndexableResource;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import com.google.common.collect.Lists;
import com.huawei.android.app.ActionBarEx;
import com.huawei.netassistant.service.NetAssistantManager;
import com.huawei.netassistant.util.NotificationUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.ToastUtils;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager;
import com.huawei.systemmanager.comm.simcard.HsmSubsciptionManager.HsmSubInfo;
import com.huawei.systemmanager.customize.CustomizeManager;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NATAutoAdjustService;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.NetState;
import com.huawei.systemmanager.netassistant.traffic.trafficcorrection.ShareCfg;
import com.huawei.systemmanager.netassistant.ui.mainpage.MainEmptyFragment;
import com.huawei.systemmanager.netassistant.ui.setting.TrafficSettingFragment.TrafficSettingActivity;
import com.huawei.systemmanager.settingsearch.BaseSearchIndexProvider;
import com.huawei.systemmanager.util.HwLog;
import huawei.android.widget.SubTabWidget;
import huawei.android.widget.SubTabWidget.SubTab;
import huawei.support.v13.app.SubTabFragmentPagerAdapter;
import java.util.ArrayList;
import java.util.List;

public class NetAssistantMainActivity extends HsmActivity implements OnClickListener {
    private static final String KEY_4G_TRAFFIC_RANK = "4g_traffic_ranking";
    private static final String KEY_DATA_SAVER_MODE = "key_data_saver_mode";
    private static final String KEY_NET_APP_MANAGEMENT = "net_app_management";
    private static final String KEY_TRAFFIC_RANK_LIST = "traffic_ranking_list";
    public static final BaseSearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = R.xml.netassistant_main_list_preference;
            sir.iconResId = R.drawable.ic_settings_data_usage;
            sir.intentAction = "huawei.intent.action.HSM_NET_ASSISTANT_MAIN_ACTIVITY";
            sir.intentTargetPackage = "com.huawei.systemmanager";
            sir.intentTargetClass = NetAssistantMainActivity.class.getName();
            return Lists.newArrayList(sir);
        }

        public List<String> getNonIndexableKeys(Context context) {
            ArrayList<String> result = new ArrayList();
            if (Utility.isWifiOnlyMode()) {
                result.add(NetAssistantMainActivity.KEY_TRAFFIC_RANK_LIST);
                result.add(NetAssistantMainActivity.KEY_4G_TRAFFIC_RANK);
                result.add("key_data_saver_mode");
                result.add(NetAssistantMainActivity.KEY_NET_APP_MANAGEMENT);
            }
            return result;
        }
    };
    private static final String TAG = "NetAssistantMainActivity";
    private boolean isNetAssistantEnable;
    private boolean mHasMonthlyReset = false;
    private boolean mIsSupportOrientation;
    private ViewPager mPagerView;
    private List<HsmSubInfo> subInfos = Lists.newArrayList();

    private static class FragmentPagerAdapter extends SubTabFragmentPagerAdapter {
        public FragmentPagerAdapter(Activity activity, ViewPager pager, SubTabWidget subTabWidget) {
            super(activity, pager, subTabWidget);
        }

        public void onPageSelected(int position) {
            HsmStat.statE(Events.E_NETASSISTANT_SWITCH_CARD_PAGE);
            super.onPageSelected(position);
        }

        public void onPageScrollStateChanged(int state) {
            super.onPageScrollStateChanged(state);
        }
    }

    @TargetApi(21)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.netassistant_main_activity);
        this.isNetAssistantEnable = CustomizeManager.getInstance().isFeatureEnabled(30);
        this.mIsSupportOrientation = Utility.isSupportOrientation();
        if (this.mIsSupportOrientation) {
            initScreenOrientation(getResources().getConfiguration());
        }
        SubTabWidget subTabWidget = (SubTabWidget) findViewById(R.id.subtab_layout);
        this.mPagerView = (ViewPager) findViewById(R.id.view_pager);
        FragmentPagerAdapter pageAdapter = new FragmentPagerAdapter(this, this.mPagerView, subTabWidget);
        if (this.mPagerView != null) {
            this.mPagerView.setAdapter(pageAdapter);
        }
        if (UserHandle.myUserId() == 0) {
            ActionBarEx.setEndIcon(getActionBar(), true, getDrawable(R.drawable.settings_menu_btn_selector), this);
            ActionBarEx.setEndContentDescription(getActionBar(), getString(R.string.net_assistant_setting_title));
        }
        this.subInfos = HsmSubsciptionManager.createSubInfos();
        if (this.subInfos.size() <= 1) {
            subTabWidget.setVisibility(8);
        } else {
            subTabWidget.setVisibility(0);
        }
        HwLog.i(TAG, "onCreate()-- subInfos.size() = " + this.subInfos.size());
        SubTab tab;
        if (this.subInfos.size() <= 0) {
            tab = subTabWidget.newSubTab("");
            MainEmptyFragment fragment = new MainEmptyFragment();
            tab.setSubTabId(R.id.systemmanager_asistant_main_activity);
            pageAdapter.addSubTab(tab, fragment, null, true);
        } else {
            for (HsmSubInfo subInfo : this.subInfos) {
                tab = subTabWidget.newSubTab(String.format(getString(R.string.harassment_cardtab1_info), new Object[]{subInfo.getOpName()}));
                if (subInfo.getSubId() == 1) {
                    tab = subTabWidget.newSubTab(String.format(getString(R.string.harassment_cardtab2_info), new Object[]{subInfo.getOpName()}));
                    tab.setSubTabId(R.id.systemmanager_harassment_cardtab2_info);
                }
                Bundle bundle = new Bundle();
                bundle.putInt(NetTrafficLineChartFragment.ARG_SUBID, subInfo.getSubId());
                NetTrafficLineChartFragment fragment2 = new NetTrafficLineChartFragment();
                tab.setSubTabId(R.id.systemmanager_harassment_cardtab1_info);
                pageAdapter.addSubTab(tab, fragment2, bundle, subInfo.isActive());
            }
        }
        startNATAutoAdjustService();
    }

    protected void onResume() {
        super.onResume();
        if (!this.mHasMonthlyReset) {
            if (NetState.isCurrentNetActive()) {
                notifyMonthlyResetNotify(getIntent());
            }
            NotificationUtil.cancelNotification(NotificationUtil.NOTIFICATION_ID_MONTYLY_RESET_NOTIFY);
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsSupportOrientation) {
            initScreenOrientation(newConfig);
        }
    }

    private void initScreenOrientation(Configuration newConfig) {
    }

    @FindBugsSuppressWarnings({"SIC_INNER_SHOULD_BE_STATIC_ANON"})
    private void notifyMonthlyResetNotify(Intent intent) {
        if (intent != null) {
            this.mHasMonthlyReset = true;
            boolean isMonthReset = intent.getBooleanExtra(NotificationUtil.KEY_MONTHLY_RESET_NOTIFY, false);
            final String imsi = intent.getStringExtra(NotificationUtil.KEY_IMSI);
            if (isMonthReset) {
                new Builder(this).setTitle(R.string.common_dialog_title_tip).setMessage(R.string.net_assistant_manual_mms_message).setPositiveButton(R.string.alert_dialog_ok, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (TextUtils.isEmpty(imsi)) {
                                HwLog.e(NetAssistantMainActivity.TAG, "no card message");
                                return;
                            }
                            NetAssistantManager.sendAdjustSMS(imsi);
                            ToastUtils.toastShortMsg((int) R.string.net_assistant_toast_manul_send_sms_Toast);
                        } catch (Exception e) {
                            HwLog.e(NetAssistantMainActivity.TAG, "setAdjustItemInfo Exception !");
                            HwLog.e(NetAssistantMainActivity.TAG, e.getMessage());
                        }
                    }
                }).setNegativeButton(R.string.alert_dialog_cancel, null).create().show();
            }
        }
    }

    public void onClick(View v) {
        switch (v.getId()) {
            case 16908296:
                Intent i = new Intent();
                i.setClass(this, TrafficSettingActivity.class);
                startActivity(i);
                return;
            default:
                return;
        }
    }

    protected void backfromAgreement(boolean agree) {
        if (agree) {
            startNATAutoAdjustService();
        }
    }

    private void startNATAutoAdjustService() {
        if (isShowAgreement()) {
            HwLog.i(TAG, "startNATAutoAdjustService,isShowAgreement return");
            return;
        }
        HwLog.i(TAG, "startNATAutoAdjustService");
        Intent intent = new Intent(ShareCfg.REQUEST_PROFIL_ACTION);
        intent.setClass(this, NATAutoAdjustService.class);
        for (HsmSubInfo info : this.subInfos) {
            intent.putExtra(ShareCfg.EXTRA_SEND_SMS_IMSI, info.getImsi());
            startService(intent);
        }
    }

    protected void onNewIntent(Intent intent) {
        HsmStat.checkOnNewIntent(this, intent);
        super.onNewIntent(intent);
    }
}
