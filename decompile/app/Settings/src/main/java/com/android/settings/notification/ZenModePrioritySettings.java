package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.os.Bundle;
import android.service.notification.ZenModeConfig;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.DropDownPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.search.Indexable;

public class ZenModePrioritySettings extends ZenModeSettingsBase implements Indexable {
    private DropDownPreference mCalls;
    private boolean mDisableListeners;
    private SwitchPreference mEvents;
    private DropDownPreference mMessages;
    private Policy mPolicy;
    private SwitchPreference mReminders;
    private SwitchPreference mRepeatCallers;

    private int getNewPriorityCategories(boolean r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.settings.notification.ZenModePrioritySettings.getNewPriorityCategories(boolean, int):int
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:116)
	at jadx.core.dex.nodes.ClassNode.load(ClassNode.java:249)
	at jadx.core.ProcessClass.process(ProcessClass.java:34)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
Caused by: jadx.core.utils.exceptions.DecodeException: Unknown instruction: not-int
	at jadx.core.dex.instructions.InsnDecoder.decode(InsnDecoder.java:569)
	at jadx.core.dex.instructions.InsnDecoder.process(InsnDecoder.java:56)
	at jadx.core.dex.nodes.MethodNode.load(MethodNode.java:102)
	... 5 more
*/
        /*
        // Can't load method instructions.
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.ZenModePrioritySettings.getNewPriorityCategories(boolean, int):int");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230952);
        PreferenceScreen root = getPreferenceScreen();
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        this.mReminders = (SwitchPreference) root.findPreference("reminders");
        this.mReminders.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettings.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModePrioritySettings.this.mContext, 167, val);
                if (ZenModePrioritySettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowReminders=" + val);
                }
                ZenModePrioritySettings.this.savePolicy(ZenModePrioritySettings.this.getNewPriorityCategories(val, 1), ZenModePrioritySettings.this.mPolicy.priorityCallSenders, ZenModePrioritySettings.this.mPolicy.priorityMessageSenders, ZenModePrioritySettings.this.mPolicy.suppressedVisualEffects);
                return true;
            }
        });
        this.mEvents = (SwitchPreference) root.findPreference("events");
        this.mEvents.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettings.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModePrioritySettings.this.mContext, 168, val);
                if (ZenModePrioritySettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowEvents=" + val);
                }
                ZenModePrioritySettings.this.savePolicy(ZenModePrioritySettings.this.getNewPriorityCategories(val, 2), ZenModePrioritySettings.this.mPolicy.priorityCallSenders, ZenModePrioritySettings.this.mPolicy.priorityMessageSenders, ZenModePrioritySettings.this.mPolicy.suppressedVisualEffects);
                return true;
            }
        });
        this.mMessages = (DropDownPreference) root.findPreference("messages");
        addSources(this.mMessages);
        this.mMessages.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettings.this.mDisableListeners) {
                    return false;
                }
                int val = Integer.parseInt((String) newValue);
                boolean allowMessages = val != -1;
                int allowMessagesFrom = val == -1 ? ZenModePrioritySettings.this.mPolicy.priorityMessageSenders : val;
                MetricsLogger.action(ZenModePrioritySettings.this.mContext, 169, val);
                if (ZenModePrioritySettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowMessages=" + allowMessages + " allowMessagesFrom=" + ZenModeConfig.sourceToString(allowMessagesFrom));
                }
                ZenModePrioritySettings.this.savePolicy(ZenModePrioritySettings.this.getNewPriorityCategories(allowMessages, 4), ZenModePrioritySettings.this.mPolicy.priorityCallSenders, allowMessagesFrom, ZenModePrioritySettings.this.mPolicy.suppressedVisualEffects);
                return true;
            }
        });
        this.mCalls = (DropDownPreference) root.findPreference("calls");
        addSources(this.mCalls);
        this.mCalls.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettings.this.mDisableListeners) {
                    return false;
                }
                int val = Integer.parseInt((String) newValue);
                boolean allowCalls = val != -1;
                int allowCallsFrom = val == -1 ? ZenModePrioritySettings.this.mPolicy.priorityCallSenders : val;
                MetricsLogger.action(ZenModePrioritySettings.this.mContext, 170, val);
                if (ZenModePrioritySettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowCalls=" + allowCalls + " allowCallsFrom=" + ZenModeConfig.sourceToString(allowCallsFrom));
                }
                ZenModePrioritySettings.this.savePolicy(ZenModePrioritySettings.this.getNewPriorityCategories(allowCalls, 8), allowCallsFrom, ZenModePrioritySettings.this.mPolicy.priorityMessageSenders, ZenModePrioritySettings.this.mPolicy.suppressedVisualEffects);
                return true;
            }
        });
        this.mRepeatCallers = (SwitchPreference) root.findPreference("repeat_callers");
        this.mRepeatCallers.setSummary(this.mContext.getString(2131626828, new Object[]{Integer.valueOf(this.mContext.getResources().getInteger(17694873))}));
        this.mRepeatCallers.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettings.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModePrioritySettings.this.mContext, 171, val);
                if (ZenModePrioritySettings.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowRepeatCallers=" + val);
                }
                ZenModePrioritySettings.this.savePolicy(ZenModePrioritySettings.this.getNewPriorityCategories(val, 16), ZenModePrioritySettings.this.mPolicy.priorityCallSenders, ZenModePrioritySettings.this.mPolicy.priorityMessageSenders, ZenModePrioritySettings.this.mPolicy.suppressedVisualEffects);
                return true;
            }
        });
        updateControls();
    }

    protected void onZenModeChanged() {
    }

    protected void onZenModeConfigChanged() {
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        updateControls();
    }

    private void updateControls() {
        boolean z;
        int i = -1;
        this.mDisableListeners = true;
        if (this.mCalls != null) {
            int i2;
            DropDownPreference dropDownPreference = this.mCalls;
            if (isPriorityCategoryEnabled(8)) {
                i2 = this.mPolicy.priorityCallSenders;
            } else {
                i2 = -1;
            }
            dropDownPreference.setValue(Integer.toString(i2));
        }
        DropDownPreference dropDownPreference2 = this.mMessages;
        if (isPriorityCategoryEnabled(4)) {
            i = this.mPolicy.priorityMessageSenders;
        }
        dropDownPreference2.setValue(Integer.toString(i));
        this.mReminders.setChecked(isPriorityCategoryEnabled(1));
        this.mEvents.setChecked(isPriorityCategoryEnabled(2));
        this.mRepeatCallers.setChecked(isPriorityCategoryEnabled(16));
        SwitchPreference switchPreference = this.mRepeatCallers;
        if (!isPriorityCategoryEnabled(8)) {
            z = true;
        } else if (this.mPolicy.priorityCallSenders != 0) {
            z = true;
        } else {
            z = false;
        }
        switchPreference.setVisible(z);
        this.mDisableListeners = false;
    }

    protected int getMetricsCategory() {
        return 141;
    }

    private static void addSources(DropDownPreference pref) {
        pref.setEntries(new CharSequence[]{pref.getContext().getString(2131626818), pref.getContext().getString(2131626819), pref.getContext().getString(2131626820), pref.getContext().getString(2131626821)});
        pref.setEntryValues(new CharSequence[]{Integer.toString(0), Integer.toString(1), Integer.toString(2), Integer.toString(-1)});
    }

    private boolean isPriorityCategoryEnabled(int categoryType) {
        return (this.mPolicy.priorityCategories & categoryType) != 0;
    }

    private void savePolicy(int priorityCategories, int priorityCallSenders, int priorityMessageSenders, int suppressedVisualEffects) {
        this.mPolicy = new Policy(priorityCategories, priorityCallSenders, priorityMessageSenders, suppressedVisualEffects);
        NotificationManager.from(this.mContext).setNotificationPolicy(this.mPolicy);
    }
}
