package com.huawei.notificationmanager.ui;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import com.huawei.android.app.ActionBarEx;
import com.huawei.notificationmanager.ui.NotificationSettingsFragment.OnChangeLinstener;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;
import huawei.com.android.internal.widget.HwFragmentContainer;

public class NotificationManagmentActivity extends HsmActivity {
    private static final String TAG = "NotificationManagmentActivity";
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            Intent intent = new Intent("android.settings.NOTIFICATION_SETTINGS");
            intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
            try {
                NotificationManagmentActivity.this.startActivity(intent);
            } catch (Exception e) {
                HwLog.e(NotificationManagmentActivity.TAG, "mActionBarListener:Exception", e);
            }
        }
    };
    private OnDataLoadedListener mDataLoadedListener = new OnDataLoadedListener() {
        public void doLoaded(Bundle bundle) {
            NotificationManagmentActivity.this.mNotiSettingsContainerFragment = new NotificationSettingsContainerFragment();
            NotificationManagmentActivity.this.mNotiSettingsContainerFragment.setArguments(bundle);
            NotificationManagmentActivity.this.mNotiSettingsContainerFragment.setOnChangeListener(NotificationManagmentActivity.this.mSettingsChangeLinstener);
            NotificationManagmentActivity.this.mNotiSettingsContainerFragment.setQuitStateListener(NotificationManagmentActivity.this.mQuitStateLinser);
            FragmentTransaction transaction = NotificationManagmentActivity.this.mFM.beginTransaction();
            transaction.replace(NotificationManagmentActivity.this.mFragmentContainer.getRightLayout().getId(), NotificationManagmentActivity.this.mNotiSettingsContainerFragment, NotificationSettingsContainerFragment.class.getName());
            transaction.commitAllowingStateLoss();
            NotificationManagmentActivity.this.mFragmentContainer.refreshFragmentLayout();
        }

        public void refresh(Bundle bundle) {
            if (bundle != null) {
                NotificationManagmentActivity.this.mFragmentContainer.setSelectedContainer(1);
                NotificationManagmentActivity.this.mFragmentContainer.refreshFragmentLayout();
                NotificationManagmentActivity.this.mNotiSettingsContainerFragment.refresh(bundle);
            }
        }
    };
    private FragmentManager mFM;
    private HwFragmentContainer mFragmentContainer;
    private NotificationCenterFragment mNotiCenterFragment;
    private NotificationSettingsContainerFragment mNotiSettingsContainerFragment;
    private OnQuitListener mQuitStateLinser = new OnQuitListener() {
        public void onQuitClicked() {
            NotificationManagmentActivity.this.onBackPressed();
        }
    };
    private OnChangeLinstener mSettingsChangeLinstener = new OnChangeLinstener() {
        public void doChange() {
            NotificationManagmentActivity.this.mNotiCenterFragment.refurbishAppList();
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        HSMConst.managerBundle(savedInstanceState);
        super.onCreate(savedInstanceState);
        getActionBar().setTitle(R.string.notification_manager_title);
        ActionBarEx.setEndIcon(getActionBar(), true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
        this.mFM = getFragmentManager();
        this.mFragmentContainer = new HwFragmentContainer(this, 0.5f, this.mFM);
        setContentView(this.mFragmentContainer.getFragmentLayout());
        this.mNotiCenterFragment = new NotificationCenterFragment();
        this.mNotiCenterFragment.setOnDataLoadedListener(this.mDataLoadedListener);
        FragmentTransaction transaction = this.mFM.beginTransaction();
        transaction.replace(this.mFragmentContainer.getLeftLayout().getId(), this.mNotiCenterFragment, NotificationCenterFragment.class.getName());
        transaction.commitAllowingStateLoss();
        this.mFragmentContainer.setSelectedContainer(0);
        this.mFragmentContainer.refreshFragmentLayout();
    }

    protected void onDestroy() {
        super.onDestroy();
    }

    public void onBackPressed() {
        if (this.mFragmentContainer.getColumnsNumber() == 2) {
            finish();
        } else if (this.mFragmentContainer.getRightLayout().getVisibility() == 0) {
            this.mFragmentContainer.setSelectedContainer(0);
            this.mFragmentContainer.refreshFragmentLayout();
        } else {
            finish();
        }
    }
}
