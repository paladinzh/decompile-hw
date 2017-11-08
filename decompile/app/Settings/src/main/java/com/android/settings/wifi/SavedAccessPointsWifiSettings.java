package com.android.settings.wifi;

import android.app.Dialog;
import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceScreen;
import android.util.Log;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.search.BaseSearchIndexProvider;
import com.android.settings.search.Index;
import com.android.settings.search.Indexable;
import com.android.settings.search.Indexable.SearchIndexProvider;
import com.android.settings.search.SearchIndexableRaw;
import com.android.settings.wifi.AccessPointPreference.UserBadgeCache;
import com.android.settings.wifi.WifiDialog.WifiDialogListener;
import com.android.settingslib.wifi.AccessPoint;
import com.android.settingslib.wifi.WifiTracker;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class SavedAccessPointsWifiSettings extends SettingsPreferenceFragment implements Indexable, WifiDialogListener {
    public static final SearchIndexProvider SEARCH_INDEX_DATA_PROVIDER = new BaseSearchIndexProvider() {
        public List<SearchIndexableRaw> getRawDataToIndex(Context context, boolean enabled) {
            List<SearchIndexableRaw> result = new ArrayList();
            String title = context.getResources().getString(2131625019);
            SearchIndexableRaw data = new SearchIndexableRaw(context);
            data.title = title;
            data.screenTitle = title;
            List<WifiConfiguration> configs = ((WifiManager) context.getSystemService("wifi")).getConfiguredNetworks();
            if (configs != null && configs.size() > 0) {
                result.add(data);
            }
            List<AccessPoint> accessPoints = WifiTracker.getCurrentAccessPoints(context, true, false, true);
            SavedAccessPointsWifiSettings.removeTempCreatedAccessPoint(accessPoints);
            int accessPointsSize = accessPoints.size();
            for (int i = 0; i < accessPointsSize; i++) {
                data = new SearchIndexableRaw(context);
                data.title = ((AccessPoint) accessPoints.get(i)).getSsidStr();
                data.screenTitle = title;
                result.add(data);
            }
            return result;
        }
    };
    private Bundle mAccessPointSavedState;
    private WifiDialog mDialog;
    private AccessPoint mDlgAccessPoint;
    private AccessPoint mSelectedAccessPoint;
    private UserBadgeCache mUserBadgeCache;
    private WifiManager mWifiManager;

    protected int getMetricsCategory() {
        return 106;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(2131230940);
        this.mUserBadgeCache = new UserBadgeCache(getPackageManager());
    }

    public void onResume() {
        super.onResume();
        initPreferences();
        Index.getInstance(getActivity()).updateFromClassNameResource(SavedAccessPointsWifiSettings.class.getName(), true, true);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mWifiManager = (WifiManager) getSystemService("wifi");
        if (savedInstanceState != null && savedInstanceState.containsKey("wifi_ap_state")) {
            this.mAccessPointSavedState = savedInstanceState.getBundle("wifi_ap_state");
        }
    }

    private void initPreferences() {
        PreferenceScreen preferenceScreen = getPreferenceScreen();
        Context context = getPrefContext();
        List<AccessPoint> accessPoints = WifiTracker.getCurrentAccessPoints(context, true, false, true);
        removeTempCreatedAccessPoint(accessPoints);
        Collections.sort(accessPoints, new Comparator<AccessPoint>() {
            public int compare(AccessPoint ap1, AccessPoint ap2) {
                if (ap1.getConfigName() != null) {
                    return ap1.getConfigName().compareTo(ap2.getConfigName());
                }
                return -1;
            }
        });
        preferenceScreen.removeAll();
        int accessPointsSize = accessPoints.size();
        for (int i = 0; i < accessPointsSize; i++) {
            LongPressAccessPointPreference preference = new LongPressAccessPointPreference((AccessPoint) accessPoints.get(i), context, this.mUserBadgeCache, true, this);
            preference.setIcon(null);
            preferenceScreen.addPreference(preference);
        }
        if (getPreferenceScreen().getPreferenceCount() < 1) {
            Log.w("SavedAccessPointsWifiSettings", "Saved networks activity loaded, but there are no saved networks!");
        }
    }

    private void showDialog(LongPressAccessPointPreference accessPoint, boolean edit) {
        if (this.mDialog != null) {
            removeDialog(1);
            this.mDialog = null;
        }
        this.mDlgAccessPoint = accessPoint.getAccessPoint();
        showDialog(1);
    }

    public Dialog onCreateDialog(int dialogId) {
        switch (dialogId) {
            case 1:
                if (this.mDlgAccessPoint == null) {
                    if (this.mAccessPointSavedState == null) {
                        this.mAccessPointSavedState = new Bundle();
                    }
                    this.mDlgAccessPoint = new AccessPoint(getActivity(), this.mAccessPointSavedState);
                    this.mAccessPointSavedState = null;
                }
                this.mSelectedAccessPoint = this.mDlgAccessPoint;
                this.mDialog = new WifiDialog(getActivity(), this, this.mDlgAccessPoint, 0, true);
                return this.mDialog;
            default:
                return super.onCreateDialog(dialogId);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        if (this.mDialog != null && this.mDialog.isShowing() && this.mDlgAccessPoint != null) {
            this.mAccessPointSavedState = new Bundle();
            this.mDlgAccessPoint.saveWifiState(this.mAccessPointSavedState);
            outState.putBundle("wifi_ap_state", this.mAccessPointSavedState);
        }
    }

    public void onForget(WifiDialog dialog) {
        if (this.mSelectedAccessPoint != null) {
            this.mWifiManager.forget(this.mSelectedAccessPoint.getConfig().networkId, null);
            Preference preference = (Preference) this.mSelectedAccessPoint.getTag();
            if (preference != null) {
                getPreferenceScreen().removePreference(preference);
            }
            this.mSelectedAccessPoint = null;
            if (getPreferenceScreen().getPreferenceCount() < 1) {
                Index.getInstance(getActivity()).updateFromClassNameResource(SavedAccessPointsWifiSettings.class.getName(), true, true);
            }
        }
    }

    public void onSubmit(WifiDialog dialog) {
    }

    public boolean onPreferenceTreeClick(Preference preference) {
        if (!(preference instanceof LongPressAccessPointPreference)) {
            return super.onPreferenceTreeClick(preference);
        }
        showDialog((LongPressAccessPointPreference) preference, false);
        return true;
    }

    public static void removeTempCreatedAccessPoint(List<AccessPoint> list) {
        if (list != null) {
            Iterator<AccessPoint> iterator = list.iterator();
            while (iterator.hasNext()) {
                AccessPoint accessPoint = (AccessPoint) iterator.next();
                if (accessPoint.isTempCreated()) {
                    Log.d("SavedAccessPointsWifiSettings", "remove access point as it is created temporarily by access point evaluation, ssid:" + accessPoint.getSsidStr());
                    iterator.remove();
                }
            }
        }
    }
}
