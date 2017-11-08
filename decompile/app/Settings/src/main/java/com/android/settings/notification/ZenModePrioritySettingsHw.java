package com.android.settings.notification;

import android.app.NotificationManager;
import android.app.NotificationManager.Policy;
import android.content.Context;
import android.os.Bundle;
import android.provider.Settings.Secure;
import android.service.notification.ZenModeConfig;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import android.view.View;
import com.android.internal.logging.MetricsLogger;
import com.android.settings.ItemUseStat;
import com.android.settings.Utils;
import com.android.settings.search.Indexable;

public class ZenModePrioritySettingsHw extends ZenModeSettingsBase implements Indexable {
    private static final String[] ENTRY_VALUES_SOURCE = new String[]{"SOURCE_ANYONE", "SOURCE_CONTACT", "SOURCE_STAR", "SOURCE_NONE"};
    private static final String[] ENTRY_VALUES_SOURCE_WITH_WHITELIST = new String[]{"SOURCE_ANYONE", "SOURCE_CONTACT", "SOURCE_WHITE_LIST", "SOURCE_STAR", "SOURCE_NONE"};
    private ListPreference mCalls;
    private boolean mDisableListeners;
    private boolean mIsWhiteListFeatureSupported = false;
    private ListPreference mMessages;
    private Policy mPolicy;
    private SwitchPreference mRepeatCallers;
    private PreferenceScreen mWhiteListScreen;

    private int getNewPriorityCategories(boolean r1, int r2) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.DecodeException: Load method exception in method: com.android.settings.notification.ZenModePrioritySettingsHw.getNewPriorityCategories(boolean, int):int
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
        throw new UnsupportedOperationException("Method not decompiled: com.android.settings.notification.ZenModePrioritySettingsHw.getNewPriorityCategories(boolean, int):int");
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230952);
        PreferenceScreen root = getPreferenceScreen();
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        this.mWhiteListScreen = (PreferenceScreen) root.findPreference("white_list_settings");
        if (this.mWhiteListScreen != null) {
            this.mIsWhiteListFeatureSupported = Utils.hasIntentActivity(getPackageManager(), this.mWhiteListScreen.getIntent());
        }
        if (!this.mIsWhiteListFeatureSupported) {
            root.removePreference(this.mWhiteListScreen);
        } else if (this.mWhiteListScreen != null) {
            this.mWhiteListScreen.setOnPreferenceClickListener(new OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    ItemUseStat.getInstance().handleNonTwoStatePreferenceClick(ZenModePrioritySettingsHw.this.getActivity(), preference);
                    return false;
                }
            });
        } else {
            Log.w("ZenModeSettings", "onCreate()-->mWhiteListScreen is null!");
        }
        this.mMessages = (ListPreference) root.findPreference("messages");
        addSources(this.mMessages);
        this.mMessages.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettingsHw.this.mDisableListeners) {
                    return false;
                }
                int allowMessagesFrom;
                CharSequence[] -get1;
                int val = Integer.parseInt((String) newValue);
                MetricsLogger.action(ZenModePrioritySettingsHw.this.mContext, 169, val);
                boolean allowMessages = (val == -1 || val == -2) ? false : true;
                if (-2 == val) {
                    allowMessagesFrom = ZenModePrioritySettingsHw.this.mPolicy.priorityMessageSenders;
                    Secure.putInt(ZenModePrioritySettingsHw.this.getContentResolver(), "zen_message_white_list_enabled", 1);
                } else {
                    allowMessagesFrom = val == -1 ? ZenModePrioritySettingsHw.this.mPolicy.priorityMessageSenders : val;
                    Secure.putInt(ZenModePrioritySettingsHw.this.getContentResolver(), "zen_message_white_list_enabled", 0);
                }
                if (ZenModePrioritySettingsHw.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowMessages=" + allowMessages + " allowMessagesFrom=" + ZenModeConfig.sourceToString(allowMessagesFrom));
                }
                ZenModePrioritySettingsHw.this.savePolicy(ZenModePrioritySettingsHw.this.getNewPriorityCategories(allowMessages, 4), ZenModePrioritySettingsHw.this.mPolicy.priorityCallSenders, allowMessagesFrom, ZenModePrioritySettingsHw.this.mPolicy.suppressedVisualEffects);
                ItemUseStat instance = ItemUseStat.getInstance();
                Context activity = ZenModePrioritySettingsHw.this.getActivity();
                ListPreference -get5 = ZenModePrioritySettingsHw.this.mMessages;
                if (ZenModePrioritySettingsHw.this.mIsWhiteListFeatureSupported) {
                    -get1 = ZenModePrioritySettingsHw.ENTRY_VALUES_SOURCE_WITH_WHITELIST;
                } else {
                    -get1 = ZenModePrioritySettingsHw.ENTRY_VALUES_SOURCE;
                }
                instance.handleClickListPreference(activity, -get5, -get1, String.valueOf(val));
                ZenModePrioritySettingsHw.this.updateControls();
                return true;
            }
        });
        this.mCalls = (ListPreference) root.findPreference("calls");
        addSources(this.mCalls);
        this.mCalls.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettingsHw.this.mDisableListeners) {
                    return false;
                }
                int allowCallsFrom;
                CharSequence[] -get1;
                int val = Integer.parseInt((String) newValue);
                MetricsLogger.action(ZenModePrioritySettingsHw.this.mContext, 170, val);
                boolean allowCalls = (val == -1 || val == -2) ? false : true;
                if (-2 == val) {
                    allowCallsFrom = ZenModePrioritySettingsHw.this.mPolicy.priorityCallSenders;
                    Secure.putInt(ZenModePrioritySettingsHw.this.getContentResolver(), "zen_call_white_list_enabled", 1);
                } else {
                    allowCallsFrom = val == -1 ? ZenModePrioritySettingsHw.this.mPolicy.priorityCallSenders : val;
                    Secure.putInt(ZenModePrioritySettingsHw.this.getContentResolver(), "zen_call_white_list_enabled", 0);
                }
                if (ZenModePrioritySettingsHw.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowCalls=" + allowCalls + " allowCallsFrom=" + ZenModeConfig.sourceToString(allowCallsFrom));
                }
                ZenModePrioritySettingsHw.this.savePolicy(ZenModePrioritySettingsHw.this.getNewPriorityCategories(allowCalls, 8), allowCallsFrom, ZenModePrioritySettingsHw.this.mPolicy.priorityMessageSenders, ZenModePrioritySettingsHw.this.mPolicy.suppressedVisualEffects);
                ItemUseStat instance = ItemUseStat.getInstance();
                Context activity = ZenModePrioritySettingsHw.this.getActivity();
                ListPreference -get2 = ZenModePrioritySettingsHw.this.mCalls;
                if (ZenModePrioritySettingsHw.this.mIsWhiteListFeatureSupported) {
                    -get1 = ZenModePrioritySettingsHw.ENTRY_VALUES_SOURCE_WITH_WHITELIST;
                } else {
                    -get1 = ZenModePrioritySettingsHw.ENTRY_VALUES_SOURCE;
                }
                instance.handleClickListPreference(activity, -get2, -get1, String.valueOf(val));
                ZenModePrioritySettingsHw.this.updateControls();
                return true;
            }
        });
        this.mRepeatCallers = (SwitchPreference) root.findPreference("repeat_callers");
        int minute = this.mContext.getResources().getInteger(17694873);
        this.mRepeatCallers.setSummary(this.mContext.getResources().getQuantityString(2131689505, minute, new Object[]{Integer.valueOf(minute)}));
        this.mRepeatCallers.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                if (ZenModePrioritySettingsHw.this.mDisableListeners) {
                    return true;
                }
                boolean val = ((Boolean) newValue).booleanValue();
                MetricsLogger.action(ZenModePrioritySettingsHw.this.mContext, 171, val);
                if (ZenModePrioritySettingsHw.DEBUG) {
                    Log.d("ZenModeSettings", "onPrefChange allowRepeatCallers=" + val);
                }
                ZenModePrioritySettingsHw.this.savePolicy(ZenModePrioritySettingsHw.this.getNewPriorityCategories(val, 16), ZenModePrioritySettingsHw.this.mPolicy.priorityCallSenders, ZenModePrioritySettingsHw.this.mPolicy.priorityMessageSenders, ZenModePrioritySettingsHw.this.mPolicy.suppressedVisualEffects);
                ItemUseStat.getInstance().handleTwoStatePreferenceClick(ZenModePrioritySettingsHw.this.getActivity(), preference, newValue);
                return true;
            }
        });
        updateControls();
    }

    private void setWhiteScreen(int callV, int msgV) {
        if (this.mWhiteListScreen != null) {
            if (callV == -2 || msgV == -2) {
                this.mWhiteListScreen.setEnabled(true);
            } else {
                this.mWhiteListScreen.setEnabled(false);
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
    }

    protected void onZenModeChanged() {
    }

    protected void onZenModeConfigChanged() {
        this.mPolicy = NotificationManager.from(this.mContext).getNotificationPolicy();
        updateControls();
    }

    private void updateControls() {
        boolean z = true;
        this.mDisableListeners = true;
        int callV = 0;
        int msgV = 0;
        if (this.mCalls != null) {
            int value = 1 == Secure.getInt(getContentResolver(), "zen_call_white_list_enabled", 0) ? -2 : isPriorityCategoryEnabled(8) ? this.mPolicy.priorityCallSenders : -1;
            ZenModeUtils.setSelectedValue(this.mCalls, String.valueOf(value));
            callV = value;
        }
        if (this.mMessages != null) {
            value = 1 == Secure.getInt(getContentResolver(), "zen_message_white_list_enabled", 0) ? -2 : isPriorityCategoryEnabled(4) ? this.mPolicy.priorityMessageSenders : -1;
            ZenModeUtils.setSelectedValue(this.mMessages, String.valueOf(value));
            msgV = value;
        }
        setWhiteScreen(callV, msgV);
        this.mRepeatCallers.setChecked(isPriorityCategoryEnabled(16));
        SwitchPreference switchPreference = this.mRepeatCallers;
        if (isPriorityCategoryEnabled(8) && this.mPolicy.priorityCallSenders == 0) {
            z = false;
        }
        switchPreference.setVisible(z);
        this.mDisableListeners = false;
    }

    protected int getMetricsCategory() {
        return 141;
    }

    private void addSources(ListPreference pref) {
        if (this.mIsWhiteListFeatureSupported) {
            pref.setEntries(new CharSequence[]{getString(2131626818), getString(2131626819), getString(2131628115), getString(2131626820), getString(2131626821)});
            pref.setEntryValues(new CharSequence[]{String.valueOf(0), String.valueOf(1), String.valueOf(-2), String.valueOf(2), String.valueOf(-1)});
            return;
        }
        pref.setEntries(new CharSequence[]{getString(2131626818), getString(2131626819), getString(2131626820), getString(2131626821)});
        pref.setEntryValues(new CharSequence[]{String.valueOf(0), String.valueOf(1), String.valueOf(2), String.valueOf(-1)});
    }

    private boolean isPriorityCategoryEnabled(int categoryType) {
        return (this.mPolicy.priorityCategories & categoryType) != 0;
    }

    private void savePolicy(int priorityCategories, int priorityCallSenders, int priorityMessageSenders, int suppressedVisualEffects) {
        this.mPolicy = new Policy(priorityCategories, priorityCallSenders, priorityMessageSenders, suppressedVisualEffects);
        NotificationManager.from(this.mContext).setNotificationPolicy(this.mPolicy);
    }
}
