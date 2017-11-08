package com.android.settings.applications;

import android.content.Context;
import com.android.settings.Utils;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import com.android.settingslib.applications.ApplicationsState.CompoundFilter;
import java.util.ArrayList;

public class AppStatePowerBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_POWER_WHITELISTED = new CompoundFilter(ApplicationsState.FILTER_WITHOUT_DISABLED_UNTIL_USED, new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return info.extraInfo == Boolean.TRUE;
        }
    });
    private Context mContext;

    public static class HighPowerState {
    }

    public AppStatePowerBridge(ApplicationsState appState, Callback callback) {
        super(appState, callback);
    }

    protected void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry app = (AppEntry) apps.get(i);
            app.extraInfo = Utils.getProtectedAppList(this.mContext).contains(app.info.packageName) ? Boolean.TRUE : Boolean.FALSE;
        }
    }

    protected void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = Utils.getProtectedAppList(this.mContext).contains(pkg) ? Boolean.TRUE : Boolean.FALSE;
    }
}
