package com.android.settings.datausage;

import com.android.settings.applications.AppStateBaseBridge;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import java.util.ArrayList;

public class AppStateDataUsageBridge extends AppStateBaseBridge {
    private final DataSaverBackend mDataSaverBackend;

    public static class DataUsageState {
        public boolean isDataSaverBlacklisted;
        public boolean isDataSaverWhitelisted;

        public DataUsageState(boolean isDataSaverWhitelisted, boolean isDataSaverBlacklisted) {
            this.isDataSaverWhitelisted = isDataSaverWhitelisted;
            this.isDataSaverBlacklisted = isDataSaverBlacklisted;
        }
    }

    public AppStateDataUsageBridge(ApplicationsState appState, Callback callback, DataSaverBackend backend) {
        super(appState, callback);
        this.mDataSaverBackend = backend;
    }

    protected void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry app = (AppEntry) apps.get(i);
            app.extraInfo = new DataUsageState(this.mDataSaverBackend.isWhitelisted(app.info.uid), this.mDataSaverBackend.isBlacklisted(app.info.uid));
        }
    }

    protected void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = new DataUsageState(this.mDataSaverBackend.isWhitelisted(uid), this.mDataSaverBackend.isBlacklisted(uid));
    }
}
