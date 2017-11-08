package com.android.mms.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.messaging.util.OsUtil;
import com.android.mms.MmsConfig;
import com.android.mms.ui.GeneralPreferenceFragment.ActivityCallback;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwPreferenceActivity;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.StatisticalHelper;

public class MessagingPreferenceActivity extends HwPreferenceActivity implements ActivityCallback {
    private AdvancedPreferenceFragment mAdvancedSettingsFragment;
    private FragmentManager mFragmentManager;
    private GeneralPreferenceFragment mGeneralSettingsFragment;
    private MenuEx mMenu = null;
    private AlertDialog mRestoreDefaultDialog;
    private OnClickListener mRestoreDefineDialogListener = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case -1:
                    MessagingPreferenceActivity.this.mGeneralSettingsFragment.restoreDefaultPreferences();
                    MessagingPreferenceActivity.this.mAdvancedSettingsFragment.refreshFragmentView();
                    StatisticalHelper.incrementReportCount(MessagingPreferenceActivity.this.getApplicationContext(), 2075);
                    return;
                default:
                    return;
            }
        }
    };

    private class MenuEx extends EmuiMenu {
        public MenuEx() {
            super(null);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onCreateOptionsMenu() {
            addMenuRestore(MessagingPreferenceActivity.this.getResources().getConfiguration().orientation == 2);
            return true;
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 16908332:
                    MessagingPreferenceActivity.this.onBackPressed();
                    break;
                case 278925339:
                    MessagingPreferenceActivity.this.showRestoreDefaultDialog();
                    break;
            }
            return true;
        }

        void resetOptionsMenu(boolean isInLandscape) {
            EmuiMenu.resetMenu(this.mOptionMenu, 278925339, R.string.restore_default, ResEx.self().getStateListDrawable(MessagingPreferenceActivity.this.getApplicationContext(), getDrawableId(278925339, isInLandscape)));
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (OsUtil.isOwner()) {
            setContentView(R.layout.mms_settings);
            getActionBar().setDisplayHomeAsUpEnabled(true);
            createFragments();
            this.mMenu = new MenuEx();
            this.mMenu.setContext(this);
            return;
        }
        Toast.makeText(this, R.string.mms_settings_forbidden_in_secondary_user, 1).show();
        finish();
    }

    protected void onResume() {
        super.onResume();
        if (!OsUtil.isOwner()) {
            MLog.w("MessagingPreferenceActivity", "MessagingPreferenceActivity exit as not in owner user");
            finish();
        }
    }

    protected void onPause() {
        super.onPause();
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mMenu != null) {
            this.mMenu.clear();
        }
        if (this.mRestoreDefaultDialog != null) {
            this.mRestoreDefaultDialog.dismiss();
            this.mRestoreDefaultDialog = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        if (this.mMenu != null) {
            this.mMenu.setOptionMenu(menu).onCreateOptionsMenu();
            this.mMenu.setItemEnabled(278925339, MmsConfig.isSmsEnabled(this));
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.mMenu.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        MLog.d("MessagingPreferenceActivity", "onActivityResltrequestcode:" + requestCode + "resultcode:" + resultCode + "data:" + data);
        if (this.mGeneralSettingsFragment != null) {
            this.mGeneralSettingsFragment.onActivityResult(requestCode, resultCode, data);
        }
    }

    private void createFragments() {
        this.mFragmentManager = getFragmentManager();
        FragmentTransaction ft = this.mFragmentManager.beginTransaction();
        Fragment fragment = this.mFragmentManager.findFragmentByTag("com.android.mms.GeneralPreferenceFragment");
        if (fragment != null) {
            MLog.d("MessagingPreferenceActivity", "fragment is not null");
            ft.remove(fragment);
        }
        fragment = this.mFragmentManager.findFragmentByTag("com.android.mms.AdvancedPreferenceFragment");
        if (fragment != null) {
            MLog.d("MessagingPreferenceActivity", "fragment is not null");
            ft.remove(fragment);
        }
        this.mGeneralSettingsFragment = new GeneralPreferenceFragment();
        this.mGeneralSettingsFragment.setIsFirstEnter(true);
        ft.add(R.id.mms_setting, this.mGeneralSettingsFragment, "com.android.mms.GeneralPreferenceFragment");
        this.mAdvancedSettingsFragment = new AdvancedPreferenceFragment();
        ft.add(R.id.mms_setting, this.mAdvancedSettingsFragment, "com.android.mms.AdvancedPreferenceFragment");
        ft.hide(this.mAdvancedSettingsFragment);
        ft.commitAllowingStateLoss();
    }

    public void onCallBack() {
        if (this.mMenu != null) {
            this.mMenu.setItemEnabled(278925339, true);
        }
    }

    private void showRestoreDefaultDialog() {
        if (this.mRestoreDefaultDialog == null) {
            this.mRestoreDefaultDialog = new Builder(this).setIcon(17301543).setTitle(R.string.mms_restore_default_notify_info_2).setCancelable(true).setPositiveButton(R.string.restore, this.mRestoreDefineDialogListener).setNegativeButton(R.string.no, this.mRestoreDefineDialogListener).create();
        }
        if (!this.mRestoreDefaultDialog.isShowing()) {
            this.mRestoreDefaultDialog.show();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        boolean isInLandscape = newConfig.orientation == 2;
        if (this.mMenu != null) {
            this.mMenu.resetOptionsMenu(isInLandscape);
        }
    }

    public void switchFragment(int index) {
        FragmentTransaction ft = this.mFragmentManager.beginTransaction();
        if (index == 1) {
            ft.show(this.mAdvancedSettingsFragment);
            ft.hide(this.mGeneralSettingsFragment);
            setTitle(R.string.advanced_settings);
            ft.commitAllowingStateLoss();
            return;
        }
        ft.show(this.mGeneralSettingsFragment);
        ft.hide(this.mAdvancedSettingsFragment);
        setTitle(R.string.preferences_title);
        ft.commitAllowingStateLoss();
    }

    public void onBackPressed() {
        if (this.mGeneralSettingsFragment.isHidden()) {
            switchFragment(0);
        } else {
            super.onBackPressed();
        }
    }
}
