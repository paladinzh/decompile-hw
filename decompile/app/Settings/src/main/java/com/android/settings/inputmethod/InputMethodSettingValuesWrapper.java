package com.android.settings.inputmethod;

import android.app.ActivityManagerNative;
import android.content.Context;
import android.os.RemoteException;
import android.util.Log;
import android.util.Slog;
import android.view.inputmethod.InputMethodInfo;
import android.view.inputmethod.InputMethodManager;
import android.view.inputmethod.InputMethodSubtype;
import com.android.internal.inputmethod.InputMethodUtils;
import com.android.internal.inputmethod.InputMethodUtils.InputMethodSettings;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;

class InputMethodSettingValuesWrapper {
    private static final String TAG = InputMethodSettingValuesWrapper.class.getSimpleName();
    private static volatile InputMethodSettingValuesWrapper sInstance;
    private final HashSet<InputMethodInfo> mAsciiCapableEnabledImis = new HashSet();
    private final InputMethodManager mImm;
    private final ArrayList<InputMethodInfo> mMethodList = new ArrayList();
    private final HashMap<String, InputMethodInfo> mMethodMap = new HashMap();
    private final InputMethodSettings mSettings;

    static InputMethodSettingValuesWrapper getInstance(Context context) {
        if (sInstance == null) {
            synchronized (TAG) {
                if (sInstance == null) {
                    sInstance = new InputMethodSettingValuesWrapper(context);
                }
            }
        }
        return sInstance;
    }

    private static int getDefaultCurrentUserId() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser().id;
        } catch (RemoteException e) {
            Slog.w(TAG, "Couldn't get current user ID; guessing it's 0", e);
            return 0;
        }
    }

    private InputMethodSettingValuesWrapper(Context context) {
        this.mSettings = new InputMethodSettings(context.getResources(), context.getContentResolver(), this.mMethodMap, this.mMethodList, getDefaultCurrentUserId(), false);
        this.mImm = (InputMethodManager) context.getSystemService("input_method");
        refreshAllInputMethodAndSubtypes();
    }

    void refreshAllInputMethodAndSubtypes() {
        synchronized (this.mMethodMap) {
            this.mMethodList.clear();
            this.mMethodMap.clear();
            List<InputMethodInfo> imms = this.mImm.getInputMethodList();
            this.mMethodList.addAll(imms);
            for (InputMethodInfo imi : imms) {
                this.mMethodMap.put(imi.getId(), imi);
            }
            updateAsciiCapableEnabledImis();
        }
    }

    private void updateAsciiCapableEnabledImis() {
        synchronized (this.mMethodMap) {
            this.mAsciiCapableEnabledImis.clear();
            for (InputMethodInfo imi : this.mSettings.getEnabledInputMethodListLocked()) {
                int subtypeCount = imi.getSubtypeCount();
                for (int i = 0; i < subtypeCount; i++) {
                    InputMethodSubtype subtype = imi.getSubtypeAt(i);
                    if ("keyboard".equalsIgnoreCase(subtype.getMode()) && subtype.isAsciiCapable()) {
                        this.mAsciiCapableEnabledImis.add(imi);
                        break;
                    }
                }
            }
        }
    }

    List<InputMethodInfo> getInputMethodList() {
        List list;
        synchronized (this.mMethodMap) {
            list = this.mMethodList;
        }
        return list;
    }

    CharSequence getCurrentInputMethodName(Context context) {
        synchronized (this.mMethodMap) {
            InputMethodInfo imi = (InputMethodInfo) this.mMethodMap.get(this.mSettings.getSelectedInputMethod());
            if (imi == null) {
                Log.w(TAG, "Invalid selected imi: " + this.mSettings.getSelectedInputMethod());
                CharSequence charSequence = "";
                return charSequence;
            }
            charSequence = InputMethodUtils.getImeAndSubtypeDisplayName(context, imi, this.mImm.getCurrentInputMethodSubtype());
            return charSequence;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    boolean isAlwaysCheckedIme(InputMethodInfo imi, Context context) {
        boolean isEnabled = isEnabledImi(imi);
        synchronized (this.mMethodMap) {
            if (this.mSettings.getEnabledInputMethodListLocked().size() > 1 || !isEnabled) {
            } else {
                return true;
            }
        }
    }

    private int getEnabledValidSystemNonAuxAsciiCapableImeCount(Context context) {
        int count = 0;
        synchronized (this.mMethodMap) {
            List<InputMethodInfo> enabledImis = this.mSettings.getEnabledInputMethodListLocked();
        }
        for (InputMethodInfo imi : enabledImis) {
            if (isValidSystemNonAuxAsciiCapableIme(imi, context)) {
                count++;
            }
        }
        if (count == 0) {
            Log.w(TAG, "No \"enabledValidSystemNonAuxAsciiCapableIme\"s found.");
        }
        return count;
    }

    boolean isEnabledImi(InputMethodInfo imi) {
        synchronized (this.mMethodMap) {
            List<InputMethodInfo> enabledImis = this.mSettings.getEnabledInputMethodListLocked();
        }
        for (InputMethodInfo tempImi : enabledImis) {
            if (tempImi.getId().equals(imi.getId())) {
                return true;
            }
        }
        return false;
    }

    boolean isValidSystemNonAuxAsciiCapableIme(InputMethodInfo imi, Context context) {
        if (imi.isAuxiliaryIme()) {
            return false;
        }
        if (InputMethodUtils.isSystemImeThatHasSubtypeOf(imi, context, true, context.getResources().getConfiguration().locale, false, InputMethodUtils.SUBTYPE_MODE_ANY)) {
            return true;
        }
        if (!this.mAsciiCapableEnabledImis.isEmpty()) {
            return this.mAsciiCapableEnabledImis.contains(imi);
        }
        Log.w(TAG, "ascii capable subtype enabled imi not found. Fall back to English Keyboard subtype.");
        return InputMethodUtils.containsSubtypeOf(imi, Locale.ENGLISH, false, "keyboard");
    }
}
