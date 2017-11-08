package com.android.settings.vpn2;

import android.app.Activity;
import android.app.AppOpsManager;
import android.app.AppOpsManager.OpEntry;
import android.app.AppOpsManager.PackageOps;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager.NameNotFoundException;
import android.net.ConnectivityManager;
import android.net.ConnectivityManager.NetworkCallback;
import android.net.IConnectivityManager;
import android.net.IConnectivityManager.Stub;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.NetworkRequest.Builder;
import android.os.Bundle;
import android.os.Handler;
import android.os.Handler.Callback;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.os.UserManager;
import android.security.KeyStore;
import android.support.v7.preference.Preference;
import android.support.v7.preference.Preference.OnPreferenceClickListener;
import android.support.v7.preference.PreferenceGroup;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;
import com.android.internal.net.LegacyVpnInfo;
import com.android.internal.net.VpnConfig;
import com.android.internal.net.VpnProfile;
import com.android.internal.util.ArrayUtils;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsExtUtils;
import com.android.settings.Utils;
import com.android.settings.Utils.ImmersionIcon;
import com.android.settingslib.RestrictedLockUtils;
import com.google.android.collect.Lists;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class VpnSettings extends VpnSettingsHwBase implements Callback, OnPreferenceClickListener {
    private static final NetworkRequest VPN_REQUEST = new Builder().removeCapability(15).removeCapability(13).removeCapability(14).build();
    private Map<AppVpnInfo, AppPreference> mAppPreferences = new ArrayMap();
    private LegacyVpnInfo mConnectedLegacyVpn;
    private ConnectivityManager mConnectivityManager;
    private final IConnectivityManager mConnectivityService = Stub.asInterface(ServiceManager.getService("connectivity"));
    private final KeyStore mKeyStore = KeyStore.getInstance();
    private NetworkCallback mNetworkCallback = new NetworkCallback() {
        public void onAvailable(Network network) {
            if (VpnSettings.this.mUpdater != null) {
                VpnSettings.this.mUpdater.sendEmptyMessage(0);
            }
        }

        public void onLost(Network network) {
            if (VpnSettings.this.mUpdater != null) {
                VpnSettings.this.mUpdater.sendEmptyMessage(0);
            }
        }
    };
    private boolean mUnavailable;
    private Handler mUpdater;
    private UserManager mUserManager;

    public VpnSettings() {
        super("no_config_vpn");
    }

    protected int getMetricsCategory() {
        return 100;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mUserManager = (UserManager) getSystemService("user");
        this.mConnectivityManager = (ConnectivityManager) getSystemService("connectivity");
        this.mUnavailable = isUiRestricted();
        setHasOptionsMenu(!this.mUnavailable);
        addPreferencesFromResource(2131230930);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        if (this.mLegacyVpnPreferences.size() > 0 || this.mAppPreferences.size() > 0) {
            menu.add(0, 1, 0, 2131626395).setTitle(getString(2131626395)).setIcon(SettingsExtUtils.getAlphaStateListDrawable(getResources(), Utils.getImmersionIconId(getActivity(), ImmersionIcon.IMM_ADD))).setShowAsAction(2);
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        for (int i = 0; i < menu.size(); i++) {
            if (isUiRestrictedByOnlyAdmin()) {
                RestrictedLockUtils.setMenuItemAsDisabledByAdmin(getPrefContext(), menu.getItem(i), getRestrictionEnforcedAdmin());
            } else {
                menu.getItem(i).setEnabled(!this.mUnavailable);
            }
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 1:
                ItemUseStat.getInstance().handleClick(getActivity(), 2, "add_vpn_menu");
                createNewVPN();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mUnavailable) {
            if (!isUiRestrictedByOnlyAdmin()) {
                getEmptyTextView().setText(2131624064);
            }
            getPreferenceScreen().removeAll();
            return;
        }
        getActivity().invalidateOptionsMenu();
        this.mConnectivityManager.registerNetworkCallback(VPN_REQUEST, this.mNetworkCallback);
        if (this.mUpdater == null) {
            this.mUpdater = new Handler(this);
        }
        this.mUpdater.sendEmptyMessage(0);
    }

    public void onPause() {
        if (this.mUnavailable) {
            super.onPause();
            return;
        }
        this.mConnectivityManager.unregisterNetworkCallback(this.mNetworkCallback);
        if (this.mUpdater != null) {
            this.mUpdater.removeCallbacksAndMessages(null);
        }
        super.onPause();
    }

    public boolean handleMessage(Message message) {
        this.mUpdater.removeMessages(0);
        int formerLegacyVpnPreferencesSize = this.mLegacyVpnPreferences.size();
        final List<VpnProfile> vpnProfiles = loadVpnProfiles(this.mKeyStore, new int[0]);
        final List<AppVpnInfo> vpnApps = getVpnApps(getActivity(), true);
        final Map<String, LegacyVpnInfo> connectedLegacyVpns = getConnectedLegacyVpns();
        final Set<AppVpnInfo> connectedAppVpns = getConnectedAppVpns();
        final Set<AppVpnInfo> alwaysOnAppVpnInfos = getAlwaysOnAppVpnInfos();
        final String lockdownVpnKey = VpnUtils.getLockdownVpn();
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                if (VpnSettings.this.isAdded()) {
                    Set<Preference> updates = new ArraySet();
                    for (VpnProfile profile : vpnProfiles) {
                        boolean equals;
                        LegacyVpnPreference p = VpnSettings.this.findOrCreatePreference(profile);
                        if (connectedLegacyVpns.containsKey(profile.key)) {
                            p.setState(((LegacyVpnInfo) connectedLegacyVpns.get(profile.key)).state);
                        } else {
                            p.setState(LegacyVpnPreference.STATE_NONE);
                        }
                        if (lockdownVpnKey != null) {
                            equals = lockdownVpnKey.equals(profile.key);
                        } else {
                            equals = false;
                        }
                        p.setAlwaysOn(equals);
                        updates.add(p);
                    }
                    for (AppVpnInfo app : vpnApps) {
                        AppPreference p2 = VpnSettings.this.findOrCreatePreference(app);
                        if (connectedAppVpns.contains(app)) {
                            p2.setState(3);
                        } else {
                            p2.setState(AppPreference.STATE_DISCONNECTED);
                        }
                        p2.setAlwaysOn(alwaysOnAppVpnInfos.contains(app));
                        updates.add(p2);
                    }
                    VpnSettings.this.mLegacyVpnPreferences.values().retainAll(updates);
                    VpnSettings.this.mAppPreferences.values().retainAll(updates);
                    PreferenceGroup vpnGroup = VpnSettings.this.getPreferenceScreen();
                    for (int i = vpnGroup.getPreferenceCount() - 1; i >= 0; i--) {
                        Preference p3 = vpnGroup.getPreference(i);
                        if (updates.contains(p3)) {
                            updates.remove(p3);
                        } else {
                            vpnGroup.removePreference(p3);
                        }
                    }
                    for (Preference pref : updates) {
                        vpnGroup.addPreference(pref);
                    }
                }
            }
        });
        if (formerLegacyVpnPreferencesSize == 0 && this.mLegacyVpnPreferences.size() > 0) {
            Activity activity = getActivity();
            if (activity != null) {
                activity.invalidateOptionsMenu();
            }
        }
        this.mUpdater.sendEmptyMessageDelayed(0, 1000);
        return true;
    }

    public boolean onPreferenceClick(Preference preference) {
        if (preference instanceof LegacyVpnPreference) {
            VpnProfile profile = ((LegacyVpnPreference) preference).getProfile();
            if (this.mConnectedLegacyVpn != null && profile.key.equals(this.mConnectedLegacyVpn.key) && this.mConnectedLegacyVpn.state == 3) {
                try {
                    this.mConnectedLegacyVpn.intent.send();
                    return true;
                } catch (Exception e) {
                    Log.w("VpnSettings", "Starting config intent failed", e);
                }
            }
            editVpn(profile, false);
            return true;
        } else if (!(preference instanceof AppPreference)) {
            return false;
        } else {
            AppPreference pref = (AppPreference) preference;
            boolean connected = pref.getState() == 3;
            if (!connected) {
                try {
                    UserHandle user = UserHandle.of(pref.getUserId());
                    Context userContext = getActivity().createPackageContextAsUser(getActivity().getPackageName(), 0, user);
                    Intent appIntent = userContext.getPackageManager().getLaunchIntentForPackage(pref.getPackageName());
                    if (appIntent != null) {
                        userContext.startActivityAsUser(appIntent, user);
                        return true;
                    }
                } catch (NameNotFoundException nnfe) {
                    Log.w("VpnSettings", "VPN provider does not exist: " + pref.getPackageName(), nnfe);
                }
            }
            AppDialogFragment.show(this, pref.getPackageInfo(), pref.getLabel(), false, connected);
            return true;
        }
    }

    protected int getHelpResource() {
        return 2131626541;
    }

    private LegacyVpnPreference findOrCreatePreference(VpnProfile profile) {
        LegacyVpnPreference pref = (LegacyVpnPreference) this.mLegacyVpnPreferences.get(profile.key);
        if (pref == null) {
            pref = new LegacyVpnPreference(getPrefContext(), this);
            pref.setOnPreferenceClickListener(this);
            this.mLegacyVpnPreferences.put(profile.key, pref);
        }
        pref.setProfile(profile);
        return pref;
    }

    private AppPreference findOrCreatePreference(AppVpnInfo app) {
        AppPreference pref = (AppPreference) this.mAppPreferences.get(app);
        if (pref != null) {
            return pref;
        }
        pref = new AppPreference(getPrefContext(), app.userId, app.packageName);
        pref.setOnPreferenceClickListener(this);
        this.mAppPreferences.put(app, pref);
        return pref;
    }

    private Map<String, LegacyVpnInfo> getConnectedLegacyVpns() {
        try {
            this.mConnectedLegacyVpn = this.mConnectivityService.getLegacyVpnInfo(UserHandle.myUserId());
            if (this.mConnectedLegacyVpn != null) {
                return Collections.singletonMap(this.mConnectedLegacyVpn.key, this.mConnectedLegacyVpn);
            }
        } catch (RemoteException e) {
            Log.e("VpnSettings", "Failure updating VPN list with connected legacy VPNs", e);
        }
        return Collections.emptyMap();
    }

    private Set<AppVpnInfo> getConnectedAppVpns() {
        Set<AppVpnInfo> connections = new ArraySet();
        try {
            for (UserHandle profile : this.mUserManager.getUserProfiles()) {
                VpnConfig config = this.mConnectivityService.getVpnConfig(profile.getIdentifier());
                if (!(config == null || config.legacy)) {
                    connections.add(new AppVpnInfo(profile.getIdentifier(), config.user));
                }
            }
        } catch (RemoteException e) {
            Log.e("VpnSettings", "Failure updating VPN list with connected app VPNs", e);
        }
        return connections;
    }

    private Set<AppVpnInfo> getAlwaysOnAppVpnInfos() {
        Set<AppVpnInfo> result = new ArraySet();
        for (UserHandle profile : this.mUserManager.getUserProfiles()) {
            int profileId = profile.getIdentifier();
            String packageName = this.mConnectivityManager.getAlwaysOnVpnPackageForUser(profileId);
            if (packageName != null) {
                result.add(new AppVpnInfo(profileId, packageName));
            }
        }
        return result;
    }

    static List<AppVpnInfo> getVpnApps(Context context, boolean includeProfiles) {
        List<AppVpnInfo> result = Lists.newArrayList();
        if (context == null) {
            return result;
        }
        Set<Integer> profileIds;
        if (includeProfiles) {
            profileIds = new ArraySet();
            for (UserHandle profile : UserManager.get(context).getUserProfiles()) {
                profileIds.add(Integer.valueOf(profile.getIdentifier()));
            }
        } else {
            profileIds = Collections.singleton(Integer.valueOf(UserHandle.myUserId()));
        }
        List<PackageOps> apps = ((AppOpsManager) context.getSystemService("appops")).getPackagesForOps(new int[]{47});
        if (apps != null) {
            for (PackageOps pkg : apps) {
                int userId = UserHandle.getUserId(pkg.getUid());
                if (profileIds.contains(Integer.valueOf(userId))) {
                    boolean allowed = false;
                    for (OpEntry op : pkg.getOps()) {
                        if (op.getOp() == 47 && op.getMode() == 0) {
                            allowed = true;
                        }
                    }
                    if (allowed) {
                        result.add(new AppVpnInfo(userId, pkg.getPackageName()));
                    }
                }
            }
        }
        Collections.sort(result);
        return result;
    }

    static List<VpnProfile> loadVpnProfiles(KeyStore keyStore, int... excludeTypes) {
        ArrayList<VpnProfile> result = Lists.newArrayList();
        for (String key : keyStore.list("VPN_")) {
            VpnProfile profile = VpnProfile.decode(key, keyStore.get("VPN_" + key));
            if (!(profile == null || ArrayUtils.contains(excludeTypes, profile.type))) {
                result.add(profile);
            }
        }
        return result;
    }

    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenuInfo info) {
        ItemUseStat.getInstance().handleClick(getActivity(), 2, "vpn_press_long");
        Preference preference = (Preference) view.getTag();
        if (preference instanceof LegacyVpnPreference) {
            VpnProfile profile = ((LegacyVpnPreference) preference).getProfile();
            this.mSelectedKey = profile.key;
            menu.setHeaderTitle(profile.name);
            menu.add(0, 2131626396, 0, 2131626396);
            menu.add(0, 2131626397, 0, 2131626397);
        }
    }

    public boolean onContextItemSelected(MenuItem item) {
        LegacyVpnPreference preference = (LegacyVpnPreference) this.mLegacyVpnPreferences.get(this.mSelectedKey);
        if (preference == null) {
            Log.e("VpnSettingsHwBase", "onContextItemSelected() is called but no preference is found");
            return false;
        }
        switch (item.getItemId()) {
            case 2131626396:
                editVpn(preference.getProfile(), true);
                return true;
            case 2131626397:
                disconnect(preference.getProfile());
                getPreferenceScreen().removePreference(preference);
                this.mLegacyVpnPreferences.remove(this.mSelectedKey);
                this.mKeyStore.delete("VPN_" + this.mSelectedKey);
                getActivity().invalidateOptionsMenu();
                return true;
            default:
                return false;
        }
    }

    public void updatePreference(VpnProfile profile, boolean isEditing) {
        if (profile != null) {
            LegacyVpnPreference preference = (LegacyVpnPreference) this.mLegacyVpnPreferences.get(profile.key);
            if (preference != null) {
                preference.setProfile(profile);
                disconnect(profile);
                preference.update();
            } else {
                preference = new LegacyVpnPreference(getPrefContext(), this);
                preference.setProfile(profile);
                preference.setOnPreferenceClickListener(this);
                this.mLegacyVpnPreferences.put(profile.key, preference);
                getPreferenceScreen().addPreference(preference);
            }
            if (!isEditing) {
                try {
                    connect(profile);
                } catch (Exception e) {
                    Log.e("VpnSettingsHwBase", "connect", e);
                }
            }
        }
    }

    private void connect(VpnProfile profile) {
        try {
            this.mService.startLegacyVpn(profile);
        } catch (IllegalStateException e) {
            Toast.makeText(getActivity(), 2131626404, 1).show();
        } catch (Exception e2) {
            Log.e("VpnSettingsHwBase", "connect has exception is" + e2.getMessage());
        }
    }
}
