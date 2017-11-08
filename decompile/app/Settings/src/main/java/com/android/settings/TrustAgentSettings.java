package com.android.settings;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.UserHandle;
import android.support.v14.preference.SwitchPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceChangeListener;
import android.support.v7.preference.PreferenceGroup;
import android.util.ArrayMap;
import android.util.ArraySet;
import com.android.internal.widget.LockPatternUtils;
import com.android.settingslib.RestrictedLockUtils;
import com.android.settingslib.RestrictedLockUtils.EnforcedAdmin;
import com.android.settingslib.RestrictedSwitchPreference;
import com.huawei.cust.HwCustUtils;
import java.util.List;

public class TrustAgentSettings extends SettingsPreferenceFragment implements OnPreferenceChangeListener {
    private final ArraySet<ComponentName> mActiveAgents = new ArraySet();
    private ArrayMap<ComponentName, AgentInfo> mAvailableAgents;
    private Dialog mDialog;
    private boolean mDialogClicked;
    private DevicePolicyManager mDpm;
    private ComponentName mGoogleSmartLock = new ComponentName("com.google.android.gms", "com.google.android.gms.auth.trustagent.GoogleTrustAgent");
    private SwitchPreference mGoogleSmartLockPreference;
    private HwCustTrustAgentSettings mHwCustTrustAgentSettings;
    private ComponentName mHwSmartLock = new ComponentName("com.huawei.trustagent", "com.huawei.trustagent.ui.HwTrustAgent");
    private SwitchPreference mHwSmartLockPreference;
    private LockPatternUtils mLockPatternUtils;

    public static final class AgentInfo {
        ComponentName component;
        public Drawable icon;
        CharSequence label;
        SwitchPreference preference;

        public boolean equals(Object other) {
            if (other instanceof AgentInfo) {
                return this.component.equals(((AgentInfo) other).component);
            }
            return true;
        }
    }

    protected int getMetricsCategory() {
        return 91;
    }

    public void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mDpm = (DevicePolicyManager) getActivity().getSystemService(DevicePolicyManager.class);
        addPreferencesFromResource(2131230915);
        this.mHwCustTrustAgentSettings = (HwCustTrustAgentSettings) HwCustUtils.createObj(HwCustTrustAgentSettings.class, new Object[0]);
    }

    public void onResume() {
        super.onResume();
        removePreference("dummy_preference");
        updateAgents();
    }

    private void updateAgents() {
        Context context = getActivity();
        if (this.mAvailableAgents == null) {
            this.mAvailableAgents = findAvailableTrustAgents();
        }
        if (this.mLockPatternUtils == null) {
            this.mLockPatternUtils = new LockPatternUtils(getActivity());
        }
        loadActiveAgents();
        PreferenceGroup category = (PreferenceGroup) getPreferenceScreen().findPreference("trust_agents");
        category.removeAll();
        EnforcedAdmin admin = RestrictedLockUtils.checkIfKeyguardFeaturesDisabled(context, 16, UserHandle.myUserId());
        int count = this.mAvailableAgents.size();
        for (int i = 0; i < count; i++) {
            AgentInfo agent = (AgentInfo) this.mAvailableAgents.valueAt(i);
            RestrictedSwitchPreference preference = new RestrictedSwitchPreference(getPrefContext());
            preference.useAdminDisabledSummary(true);
            agent.preference = preference;
            preference.setPersistent(false);
            preference.setTitle(agent.label);
            preference.setIcon(agent.icon);
            preference.setPersistent(false);
            preference.setOnPreferenceChangeListener(this);
            preference.setChecked(this.mActiveAgents.contains(agent.component));
            if (admin != null && this.mDpm.getTrustAgentConfiguration(null, agent.component) == null) {
                preference.setChecked(false);
                preference.setDisabledByAdmin(admin);
            }
            category.addPreference(agent.preference);
            if (this.mHwSmartLock.equals(agent.component)) {
                this.mHwSmartLockPreference = preference;
            } else if (this.mGoogleSmartLock.equals(agent.component)) {
                this.mGoogleSmartLockPreference = preference;
                if (this.mHwCustTrustAgentSettings != null) {
                    this.mHwCustTrustAgentSettings.hideGoogleSmartLock(category, this.mGoogleSmartLockPreference);
                }
            }
        }
    }

    private void loadActiveAgents() {
        List<ComponentName> activeTrustAgents = this.mLockPatternUtils.getEnabledTrustAgents(UserHandle.myUserId());
        if (activeTrustAgents != null) {
            this.mActiveAgents.addAll(activeTrustAgents);
        }
    }

    private void saveActiveAgents() {
        this.mLockPatternUtils.setEnabledTrustAgents(this.mActiveAgents, UserHandle.myUserId());
    }

    ArrayMap<ComponentName, AgentInfo> findAvailableTrustAgents() {
        PackageManager pm = getActivity().getPackageManager();
        List<ResolveInfo> resolveInfos = pm.queryIntentServices(new Intent("android.service.trust.TrustAgentService"), 128);
        ArrayMap<ComponentName, AgentInfo> agents = new ArrayMap();
        int count = resolveInfos.size();
        agents.ensureCapacity(count);
        for (int i = 0; i < count; i++) {
            ResolveInfo resolveInfo = (ResolveInfo) resolveInfos.get(i);
            if (resolveInfo.serviceInfo != null && TrustAgentUtils.checkProvidePermission(resolveInfo, pm)) {
                ComponentName name = TrustAgentUtils.getComponentName(resolveInfo);
                AgentInfo agentInfo = new AgentInfo();
                agentInfo.label = resolveInfo.loadLabel(pm);
                agentInfo.icon = resolveInfo.loadIcon(pm);
                agentInfo.component = name;
                agents.put(name, agentInfo);
            }
        }
        return agents;
    }

    public boolean onPreferenceChange(Preference preference, Object newValue) {
        if (preference instanceof SwitchPreference) {
            int count = this.mAvailableAgents.size();
            for (int i = 0; i < count; i++) {
                AgentInfo agent = (AgentInfo) this.mAvailableAgents.valueAt(i);
                if (agent.preference == preference) {
                    final SwitchPreference clickedPreference = (SwitchPreference) preference;
                    final ComponentName clickedComponent = agent.component;
                    this.mDialogClicked = false;
                    if (this.mDialog != null && this.mDialog.isShowing()) {
                        this.mDialog.dismiss();
                    }
                    if (!((Boolean) newValue).booleanValue()) {
                        this.mDialog = new Builder(getActivity()).setMessage(getActivity().getResources().getString(2131627897)).setPositiveButton(17039379, new OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                TrustAgentSettings.this.mDialogClicked = true;
                                TrustAgentSettings.this.mActiveAgents.remove(clickedComponent);
                                TrustAgentSettings.this.saveActiveAgents();
                            }
                        }).setNegativeButton(17039369, null).show();
                        this.mDialog.setOnDismissListener(new OnDismissListener() {
                            public void onDismiss(DialogInterface dialog) {
                                if (!TrustAgentSettings.this.mDialogClicked) {
                                    clickedPreference.setChecked(true);
                                }
                            }
                        });
                    } else if (!this.mActiveAgents.contains(agent.component)) {
                        if (!this.mHwSmartLock.equals(agent.component) && !this.mGoogleSmartLock.equals(agent.component)) {
                            this.mActiveAgents.add(agent.component);
                        } else if (this.mActiveAgents.contains(this.mHwSmartLock) || this.mActiveAgents.contains(this.mGoogleSmartLock)) {
                            this.mDialog = new Builder(getActivity()).setMessage(getActivity().getResources().getString(2131627896)).setPositiveButton(17039379, new OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    TrustAgentSettings.this.mDialogClicked = true;
                                    TrustAgentSettings.this.mActiveAgents.add(clickedComponent);
                                    if (TrustAgentSettings.this.mHwSmartLock.equals(clickedComponent)) {
                                        TrustAgentSettings.this.mActiveAgents.remove(TrustAgentSettings.this.mGoogleSmartLock);
                                        if (TrustAgentSettings.this.mGoogleSmartLockPreference != null) {
                                            TrustAgentSettings.this.mGoogleSmartLockPreference.setOnPreferenceChangeListener(null);
                                            TrustAgentSettings.this.mGoogleSmartLockPreference.setChecked(false);
                                            TrustAgentSettings.this.mGoogleSmartLockPreference.setOnPreferenceChangeListener(TrustAgentSettings.this);
                                        }
                                    } else if (TrustAgentSettings.this.mGoogleSmartLock.equals(clickedComponent)) {
                                        TrustAgentSettings.this.mActiveAgents.remove(TrustAgentSettings.this.mHwSmartLock);
                                        if (TrustAgentSettings.this.mHwSmartLockPreference != null) {
                                            TrustAgentSettings.this.mHwSmartLockPreference.setOnPreferenceChangeListener(null);
                                            TrustAgentSettings.this.mHwSmartLockPreference.setChecked(false);
                                            TrustAgentSettings.this.mHwSmartLockPreference.setOnPreferenceChangeListener(TrustAgentSettings.this);
                                        }
                                    }
                                    TrustAgentSettings.this.saveActiveAgents();
                                }
                            }).setNegativeButton(17039369, null).show();
                            this.mDialog.setOnDismissListener(new OnDismissListener() {
                                public void onDismiss(DialogInterface dialog) {
                                    if (!TrustAgentSettings.this.mDialogClicked) {
                                        clickedPreference.setChecked(false);
                                    }
                                }
                            });
                        } else {
                            this.mActiveAgents.add(agent.component);
                        }
                    }
                    saveActiveAgents();
                    return true;
                }
            }
        }
        return false;
    }
}
