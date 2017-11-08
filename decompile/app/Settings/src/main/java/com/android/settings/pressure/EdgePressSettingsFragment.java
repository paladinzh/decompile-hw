package com.android.settings.pressure;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.SearchIndexableResource;
import android.provider.Settings.System;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsActivity;
import com.android.settings.Utils;
import com.android.settings.pressure.util.Logger;
import com.android.settings.pressure.util.PressureUtil;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.views.pagerHelper.PagerHelperPreferenceFragment;
import java.util.ArrayList;
import java.util.List;

public class EdgePressSettingsFragment extends PagerHelperPreferenceFragment implements OnPreferenceChangeListener, OnPreferenceClickListener, Indexable {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableResource> getXmlResourcesToIndex(Context context, boolean enabled) {
            if (!PressureUtil.isSupportPressureHabit(context)) {
                return null;
            }
            ArrayList<SearchIndexableResource> result = new ArrayList();
            SearchIndexableResource sir = new SearchIndexableResource(context);
            sir.xmlResId = 2131230783;
            result.add(sir);
            return result;
        }
    };
    private int mCornerTipIndex = -1;
    private Handler mHander = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    EdgePressSettingsFragment.this.popUpCornerTip(EdgePressSettingsFragment.this.mCornerTipIndex);
                    EdgePressSettingsFragment.this.mCornerTipIndex = -1;
                    return;
                default:
                    return;
            }
        }
    };
    private SwitchPreference mVirtualNotificationKey;

    public int[] getDrawables() {
        return new int[]{2130838624};
    }

    public int[] getSummaries() {
        return new int[]{2131628474};
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        addPreferencesFromResource(2131230783);
        setHasOptionsMenu(true);
    }

    protected int getMetricsCategory() {
        return 100000;
    }

    public void onResume() {
        super.onResume();
        updateSwitchState();
        if (this.mCornerTipIndex == 1 || this.mCornerTipIndex == 2) {
            this.mHander.sendMessageDelayed(this.mHander.obtainMessage(0), 500);
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.add(0, 1, 0, 2131628260).setIcon(2130838282).setEnabled(true).setVisible(true).setShowAsAction(1);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() != 1) {
            return super.onOptionsItemSelected(item);
        }
        showHelpTipView(1);
        ItemUseStat.getInstance().handleClick(getActivity(), 2, "edge_press_settings_help");
        return true;
    }

    private SwitchPreference findPreferenceAndSetListener(String key) {
        SwitchPreference pref = (SwitchPreference) findPreference(key);
        if (pref != null) {
            pref.setOnPreferenceChangeListener(this);
        }
        return pref;
    }

    private void updateSwitchState() {
        boolean z = true;
        SwitchPreference virtualKey = findPreferenceAndSetListener("virtual_key");
        if (!Utils.isChinaArea() || virtualKey == null) {
            PreferenceGroup virtualKeyCat = (PreferenceGroup) findPreference("virtual_key_category");
            if (!(virtualKeyCat == null || virtualKey == null)) {
                virtualKeyCat.removePreference(virtualKey);
            }
            virtualKey = null;
        } else {
            boolean z2;
            if (System.getInt(getContentResolver(), "hide_virtual_key", 0) > 0) {
                z2 = true;
            } else {
                z2 = false;
            }
            virtualKey.setChecked(z2);
        }
        this.mVirtualNotificationKey = findPreferenceAndSetListener("virtual_notification_key_toggle");
        if (this.mVirtualNotificationKey != null) {
            SwitchPreference switchPreference = this.mVirtualNotificationKey;
            if (System.getInt(getContentResolver(), "virtual_notification_key_type", 0) != 1) {
                z = false;
            }
            switchPreference.setChecked(z);
            if (Utils.isChinaArea() && r4 != null) {
                this.mVirtualNotificationKey.setDependency("virtual_key");
            }
        }
        Preference leftEdgePressPreference = findPreference("left_edge_press");
        String leftClassInfo = System.getString(getContentResolver(), "pressure_launch_app_left");
        if (leftClassInfo == null || leftClassInfo.length() == 0) {
            leftClassInfo = "none_app";
        }
        leftEdgePressPreference.setSummary(getAppNameByClassInfo(leftClassInfo));
        leftEdgePressPreference.setOnPreferenceClickListener(this);
        Preference rightEdgePressPreference = findPreference("right_edge_press");
        String rightClassInfo = System.getString(getContentResolver(), "pressure_launch_app_right");
        if (rightClassInfo == null || rightClassInfo.length() == 0) {
            rightClassInfo = "none_app";
        }
        rightEdgePressPreference.setSummary(getAppNameByClassInfo(rightClassInfo));
        rightEdgePressPreference.setOnPreferenceClickListener(this);
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        String key = preference.getKey();
        if ("virtual_notification_key_toggle".equals(key)) {
            System.putInt(getContentResolver(), "virtual_notification_key_type", ((Boolean) newValue).booleanValue() ? 1 : 0);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        } else if ("virtual_key".equals(key)) {
            boolean isChecked = ((Boolean) newValue).booleanValue();
            System.putInt(getContentResolver(), "hide_virtual_key", isChecked ? 1 : 0);
            notifyVirtualNotificationDependent(isChecked);
            ItemUseStat.getInstance().handleTwoStatePreferenceClick(getActivity(), preference, newValue);
        }
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        String key = preference.getKey();
        Bundle bundle;
        if ("left_edge_press".equals(key)) {
            bundle = new Bundle();
            bundle.putInt("pressure_which_edge", 1);
            ((SettingsActivity) getActivity()).startPreferencePanel(ApplicationListFragment.class.getCanonicalName(), bundle, 2131628254, null, this, 1);
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            return true;
        } else if (!"right_edge_press".equals(key)) {
            return false;
        } else {
            bundle = new Bundle();
            bundle.putInt("pressure_which_edge", 2);
            ((SettingsActivity) getActivity()).startPreferencePanel(ApplicationListFragment.class.getCanonicalName(), bundle, 2131628255, null, this, 2);
            ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(getActivity(), preference);
            return true;
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (-1 != resultCode) {
            return;
        }
        if (1 == requestCode || 2 == requestCode) {
            this.mCornerTipIndex = requestCode;
        }
    }

    private void popUpCornerTip(int corner) {
        if (needToPopUpCornerTip(corner)) {
            int i;
            if (corner == 1) {
                i = 2;
            } else {
                i = 3;
            }
            showHelpTipView(i);
            dontPopUpTipAnymore(corner);
        }
    }

    private boolean needToPopUpCornerTip(int corner) {
        SharedPreferences pref = getActivity().getPreferences(0);
        if (pref != null) {
            if (1 == corner) {
                return pref.getBoolean("left_edge_press", true);
            }
            if (2 == corner) {
                return pref.getBoolean("right_edge_press", true);
            }
        }
        return false;
    }

    private void dontPopUpTipAnymore(int corner) {
        SharedPreferences pref = getActivity().getPreferences(0);
        if (pref != null && pref.edit() != null) {
            if (1 == corner) {
                pref.edit().putBoolean("left_edge_press", false).commit();
            } else if (2 == corner) {
                pref.edit().putBoolean("right_edge_press", false).commit();
            }
        }
    }

    private String getAppNameByClassInfo(String classInfo) {
        if ("none_app".equals(classInfo)) {
            return getResources().getString(2131628259);
        }
        String name = "";
        String[] info = classInfo.split(";");
        if (info.length == 2) {
            name = getAppNameByPackage(info[0], info[1]);
        }
        return name;
    }

    private String getAppNameByPackage(String packageName, String className) {
        String appName = "";
        if (TextUtils.isEmpty(packageName) || TextUtils.isEmpty(className)) {
            Logger.w("EdgePressSettingsFragment", "packageName or className is null");
            return appName;
        }
        PackageManager pm = getPackageManager();
        if (pm == null) {
            Logger.w("EdgePressSettingsFragment", "cannot getPackageManager");
            return appName;
        }
        ComponentName cn = new ComponentName(packageName, className);
        Intent intent = new Intent();
        intent.setComponent(cn);
        List<ResolveInfo> resolveInfos = pm.queryIntentActivities(intent, 0);
        if (!(resolveInfos == null || resolveInfos.size() <= 0 || resolveInfos.get(0) == null)) {
            appName = ((ResolveInfo) resolveInfos.get(0)).loadLabel(pm).toString();
        }
        return appName;
    }

    private void showHelpTipView(int extra) {
        Intent intent = new Intent();
        intent.setAction("com.android.settings.pressure.PressureHelpActivity");
        intent.putExtra("help_type_extra", extra);
        startActivity(intent);
    }

    private void notifyVirtualNotificationDependent(boolean isChecked) {
        Logger.d("EdgePressSettingsFragment", "notifyVirtualNotificationDependent isChecked " + isChecked);
        if (this.mVirtualNotificationKey != null && !isChecked) {
            this.mVirtualNotificationKey.setChecked(isChecked);
            System.putInt(getContentResolver(), "virtual_notification_key_type", 0);
            ItemUseStat.getInstance().handleClick(getActivity(), 2, "virtual_notification_key_type", 0);
        }
    }
}
