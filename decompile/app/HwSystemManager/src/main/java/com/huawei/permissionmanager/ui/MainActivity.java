package com.huawei.permissionmanager.ui;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageView;
import com.huawei.android.app.ActionBarEx;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.module.ModulePermissionMgr;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.rainbow.client.background.service.RainbowCommonService;
import com.huawei.systemmanager.rainbow.client.base.ClientConstant.CloudActions;
import com.huawei.systemmanager.settingsearch.SettingSearchUtil;
import com.huawei.systemmanager.util.HwLog;

public class MainActivity extends HsmActivity {
    private static final int DELAY_MSG = 100;
    private static final String LOG_TAG = "MainActivity";
    private final int MSG_UPDATE_RECOMMEND_PERMISSION = 1;
    private final OnClickListener mActionBarListener = new OnClickListener() {
        public void onClick(View v) {
            MainActivity.this.onActionBarItemSelected(v.getId());
        }
    };
    private ImageView mActionBarSettingImg = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1:
                    MainActivity.this.updateRecommendPermission();
                    return;
                default:
                    return;
            }
        }
    };
    private Fragment mMainFragment;

    protected void onCreate(Bundle savedInstanceState) {
        checkEnable();
        super.onCreate(savedInstanceState);
        this.mHandler.sendEmptyMessageDelayed(1, 100);
        initActionBar();
        initFragments();
    }

    private void initActionBar() {
        ActionBarEx.setEndIcon(getActionBar(), true, getDrawable(R.drawable.settings_menu_btn_selector), this.mActionBarListener);
        ActionBarEx.setEndContentDescription(getActionBar(), getString(R.string.net_assistant_setting_title));
    }

    private void initFragments() {
        this.mMainFragment = getFragmentManager().findFragmentByTag(MainFragment.class.getSimpleName());
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        if (this.mMainFragment == null) {
            this.mMainFragment = new MainFragment();
            ft.replace(16908290, this.mMainFragment, MainFragment.class.getSimpleName());
        } else {
            ft.attach(this.mMainFragment);
        }
        ft.commit();
    }

    private void onActionBarItemSelected(int itemId) {
        HwLog.d(LOG_TAG, "Permission Manager Setting Button is clicked!");
        startActivity(new Intent(getApplicationContext(), PermissionAndCloudSwitchActivity.class));
    }

    private void updateRecommendPermission() {
        Intent intent = new Intent(CloudActions.INTENT_CLOUD_RECOMMEND_MULTI_APK);
        intent.setClass(getApplicationContext(), RainbowCommonService.class);
        startService(intent);
    }

    protected boolean shouldUpdateActionBarStyle() {
        return false;
    }

    private void checkEnable() {
        if (!new ModulePermissionMgr().entryEnabled(getApplicationContext())) {
            HwLog.w(LOG_TAG, "permission module disabled.");
            finish();
        }
    }

    public String getSelectItemKey() {
        Intent intent = getIntent();
        if (intent == null) {
            return "";
        }
        String key = intent.getStringExtra(SettingSearchUtil.KEY_EXTRA_SETTING);
        if (TextUtils.isEmpty(key)) {
            return "";
        }
        return key;
    }
}
