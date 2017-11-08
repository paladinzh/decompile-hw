package com.huawei.systemmanager.applock.view;

import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceFragment;
import android.preference.SwitchPreference;
import android.preference.TextArrowPreference;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.applock.password.PasswordProtectResetActivity;
import com.huawei.systemmanager.applock.password.ResetPasswordNormalActivity;
import com.huawei.systemmanager.applock.taskstack.ActivityStackUtils;
import com.huawei.systemmanager.applock.utils.sp.FunctionSwitchUtils;
import com.huawei.systemmanager.applock.utils.sp.ReloadSwitchUtils;
import com.huawei.systemmanager.applock.view.abs.AppLockRelockBaseActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;

public class AppLockSettingActivity extends AppLockRelockBaseActivity {
    private static final String TAG = "AppLockSettingActivity";
    private AppLockFragment mAppLockFragment = null;

    public static class AppLockFragment extends PreferenceFragment implements OnPreferenceClickListener {
        private static final String APP_LOCK = "app_lock_setting_func_app_lock";
        private static final String CHANGE_PASSWORD = "app_lock_setting_func_change_password";
        private static final String CHANGE_PASSWORD_PROTECT = "app_lock_setting_func_change_password_protect";
        private SwitchPreference appLockPref;
        private Preference changePasswordPref;
        private Preference changePasswordProtectedPref;

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.app_lock_setting);
            initPref();
        }

        public void onResume() {
            super.onResume();
            this.appLockPref.setChecked(FunctionSwitchUtils.getFunctionSwitchStatus(getActivity()));
        }

        private void initPref() {
            initLockPref();
            initChangePasswordPref();
            initChangePasswordProtectedPref();
        }

        private void initLockPref() {
            this.appLockPref = (SwitchPreference) findPreference(APP_LOCK);
            this.appLockPref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    boolean isChecked = ((Boolean) newValue).booleanValue();
                    if (FunctionSwitchUtils.getFunctionSwitchStatus(AppLockFragment.this.getActivity()) != isChecked) {
                        String[] strArr = new String[1];
                        String[] strArr2 = new String[2];
                        strArr2[0] = HsmStatConst.PARAM_OP;
                        strArr2[1] = isChecked ? "1" : "0";
                        strArr[0] = HsmStatConst.constructJsonParams(strArr2);
                        HsmStat.statE((int) Events.E_APPLOCK_SET_GLOBAL_SWITCH, strArr);
                        FunctionSwitchUtils.setFunctionSwitchStatus(AppLockFragment.this.getActivity(), isChecked);
                        ReloadSwitchUtils.setApplicationListNeedReload(AppLockFragment.this.getActivity());
                    }
                    return true;
                }
            });
        }

        private void initChangePasswordPref() {
            this.changePasswordPref = (TextArrowPreference) findPreference(CHANGE_PASSWORD);
            this.changePasswordPref.setOnPreferenceClickListener(this);
        }

        private void initChangePasswordProtectedPref() {
            this.changePasswordProtectedPref = (TextArrowPreference) findPreference(CHANGE_PASSWORD_PROTECT);
            this.changePasswordProtectedPref.setOnPreferenceClickListener(this);
        }

        public boolean onPreferenceClick(Preference preference) {
            String key = preference.getKey();
            if (CHANGE_PASSWORD.equals(key)) {
                startActivity(new Intent(getActivity(), ResetPasswordNormalActivity.class));
                return true;
            } else if (!CHANGE_PASSWORD_PROTECT.equals(key)) {
                return false;
            } else {
                startActivity(new Intent(getActivity(), PasswordProtectResetActivity.class));
                return true;
            }
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_lock_setting_main);
        setTitle(R.string.ActionBar_AddAppSettings_Title);
        if (this.mAppLockFragment == null) {
            this.mAppLockFragment = new AppLockFragment();
        }
        if (!this.mAppLockFragment.isAdded()) {
            getFragmentManager().beginTransaction().replace(R.id.app_lock_setting, this.mAppLockFragment).commit();
        }
        ActivityStackUtils.addActivity(this);
    }

    protected void concreteOnResume() {
    }

    protected String baseClassName() {
        return AppLockSettingActivity.class.getName();
    }

    protected void onDestroy() {
        ActivityStackUtils.removeFromStack(this);
        super.onDestroy();
    }
}
