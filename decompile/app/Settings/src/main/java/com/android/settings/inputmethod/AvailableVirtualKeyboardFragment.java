package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.PreferenceScreen;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.settings.SettingsPreferenceFragment;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class AvailableVirtualKeyboardFragment extends SettingsPreferenceFragment implements OnSavePreferenceListener {
    private DevicePolicyManager mDpm;
    private InputMethodManager mImm;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList();
    private InputMethodSettingValuesWrapper mInputMethodSettingValues;

    public void onCreatePreferences(Bundle bundle, String s) {
        Activity activity = getActivity();
        PreferenceScreen screen = getPreferenceManager().createPreferenceScreen(activity);
        screen.setTitle(activity.getString(2131625766));
        setPreferenceScreen(screen);
        this.mInputMethodSettingValues = InputMethodSettingValuesWrapper.getInstance(activity);
        this.mImm = (InputMethodManager) activity.getSystemService(InputMethodManager.class);
        this.mDpm = (DevicePolicyManager) activity.getSystemService(DevicePolicyManager.class);
    }

    public void onResume() {
        super.onResume();
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        updateInputMethodPreferenceViews();
    }

    public void onSaveInputMethodPreference(InputMethodPreference pref) {
        if (getActivity() != null) {
            InputMethodAndSubtypeUtil.saveInputMethodSubtypeList(this, getContentResolver(), this.mImm.getInputMethodList(), getResources().getConfiguration().keyboard == 2);
            this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
            for (InputMethodPreference p : this.mInputMethodPreferenceList) {
                p.updatePreferenceViews();
            }
        }
    }

    protected int getMetricsCategory() {
        return 347;
    }

    private static Drawable loadDrawable(PackageManager packageManager, String packageName, int resId, ApplicationInfo applicationInfo) {
        Drawable drawable = null;
        if (resId == 0) {
            return drawable;
        }
        try {
            return packageManager.getDrawable(packageName, resId, applicationInfo);
        } catch (Exception e) {
            return drawable;
        }
    }

    private static Drawable getInputMethodIcon(PackageManager packageManager, InputMethodInfo imi) {
        ServiceInfo si = imi.getServiceInfo();
        ApplicationInfo ai = si.applicationInfo;
        String packageName = imi.getPackageName();
        if (si == null || ai == null || packageName == null) {
            return new ColorDrawable(0);
        }
        Drawable drawable = loadDrawable(packageManager, packageName, si.logo, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, si.icon, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, ai.logo, ai);
        if (drawable != null) {
            return drawable;
        }
        drawable = loadDrawable(packageManager, packageName, ai.icon, ai);
        if (drawable != null) {
            return drawable;
        }
        return new ColorDrawable(0);
    }

    private void updateInputMethodPreferenceViews() {
        int i;
        this.mInputMethodSettingValues.refreshAllInputMethodAndSubtypes();
        this.mInputMethodPreferenceList.clear();
        List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
        Context context = getPrefContext();
        PackageManager packageManager = getActivity().getPackageManager();
        List<InputMethodInfo> imis = this.mInputMethodSettingValues.getInputMethodList();
        int N = imis == null ? 0 : imis.size();
        for (i = 0; i < N; i++) {
            boolean contains;
            InputMethodInfo imi = (InputMethodInfo) imis.get(i);
            if (permittedList != null) {
                contains = permittedList.contains(imi.getPackageName());
            } else {
                contains = true;
            }
            InputMethodPreference pref = new InputMethodPreference(context, imi, true, contains, this);
            pref.setIcon(getInputMethodIcon(packageManager, imi));
            this.mInputMethodPreferenceList.add(pref);
        }
        final Collator collator = Collator.getInstance();
        Collections.sort(this.mInputMethodPreferenceList, new Comparator<InputMethodPreference>() {
            public int compare(InputMethodPreference lhs, InputMethodPreference rhs) {
                return lhs.compareTo(rhs, collator);
            }
        });
        getPreferenceScreen().removeAll();
        for (i = 0; i < N; i++) {
            pref = (InputMethodPreference) this.mInputMethodPreferenceList.get(i);
            pref.setOrder(i);
            getPreferenceScreen().addPreference(pref);
            InputMethodAndSubtypeUtil.removeUnnecessaryNonPersistentPreference(pref);
            pref.updatePreferenceViews();
        }
    }
}
