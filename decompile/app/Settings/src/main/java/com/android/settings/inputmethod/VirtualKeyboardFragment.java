package com.android.settings.inputmethod;

import android.app.Activity;
import android.app.admin.DevicePolicyManager;
import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import com.android.internal.util.Preconditions;
import com.android.settings.SettingsPreferenceFragment;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public final class VirtualKeyboardFragment extends SettingsPreferenceFragment {
    private static final Drawable NO_ICON = new ColorDrawable(0);
    private Preference mAddVirtualKeyboardScreen;
    private DevicePolicyManager mDpm;
    private InputMethodManager mImm;
    private final ArrayList<InputMethodPreference> mInputMethodPreferenceList = new ArrayList();

    public void onCreatePreferences(Bundle bundle, String s) {
        Activity activity = (Activity) Preconditions.checkNotNull(getActivity());
        addPreferencesFromResource(2131230927);
        this.mImm = (InputMethodManager) Preconditions.checkNotNull((InputMethodManager) activity.getSystemService(InputMethodManager.class));
        this.mDpm = (DevicePolicyManager) Preconditions.checkNotNull((DevicePolicyManager) activity.getSystemService(DevicePolicyManager.class));
        this.mAddVirtualKeyboardScreen = (Preference) Preconditions.checkNotNull(findPreference("add_virtual_keyboard_screen"));
    }

    public void onResume() {
        super.onResume();
        updateInputMethodPreferenceViews();
    }

    protected int getMetricsCategory() {
        return 345;
    }

    private void updateInputMethodPreferenceViews() {
        int i;
        this.mInputMethodPreferenceList.clear();
        List<String> permittedList = this.mDpm.getPermittedInputMethodsForCurrentUser();
        Context context = getPrefContext();
        List<InputMethodInfo> imis = this.mImm.getEnabledInputMethodList();
        int N = imis == null ? 0 : imis.size();
        for (i = 0; i < N; i++) {
            boolean contains;
            Drawable icon;
            InputMethodInfo imi = (InputMethodInfo) imis.get(i);
            if (permittedList != null) {
                contains = permittedList.contains(imi.getPackageName());
            } else {
                contains = true;
            }
            try {
                icon = getActivity().getPackageManager().getApplicationIcon(imi.getPackageName());
            } catch (Exception e) {
                icon = NO_ICON;
            }
            InputMethodPreference pref = new InputMethodPreference(context, imi, false, contains, null);
            pref.setIcon(icon);
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
        this.mAddVirtualKeyboardScreen.setOrder(N);
        getPreferenceScreen().addPreference(this.mAddVirtualKeyboardScreen);
    }
}
