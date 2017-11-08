package com.android.settings.applications;

import android.content.Context;
import android.content.pm.PackageManager;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settings.notification.NotificationBackend;
import com.android.settings.notification.NotificationBackend.AppRow;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.ArrayList;

public class AppStateNotificationBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_APP_NOTIFICATION_BLOCKED = new AppFilter() {
        public void init() {
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public boolean filterApp(AppEntry info) {
            if (info == null || info.extraInfo == null || !(info.extraInfo instanceof AppRow)) {
                return false;
            }
            return info.extraInfo.banned;
        }
    };
    public static final AppFilter FILTER_APP_NOTIFICATION_HIDE_ALL = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            if (info == null || info.extraInfo == null) {
                return false;
            }
            boolean z = ((AppRow) info.extraInfo).lockScreenSecure ? ((AppRow) info.extraInfo).appVisOverride == -1 : false;
            return z;
        }
    };
    public static final AppFilter FILTER_APP_NOTIFICATION_HIDE_SENSITIVE = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            if (info == null || info.extraInfo == null) {
                return false;
            }
            boolean z = ((AppRow) info.extraInfo).lockScreenSecure ? ((AppRow) info.extraInfo).appVisOverride == 0 : false;
            return z;
        }
    };
    public static final AppFilter FILTER_APP_NOTIFICATION_PRIORITY = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            if (info == null || info.extraInfo == null) {
                return false;
            }
            return ((AppRow) info.extraInfo).appBypassDnd;
        }
    };
    public static final AppFilter FILTER_APP_NOTIFICATION_SILENCED = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            boolean z = false;
            if (info == null || info.extraInfo == null) {
                return false;
            }
            AppRow row = info.extraInfo;
            if (row.appImportance > 0 && row.appImportance < 3) {
                z = true;
            }
            return z;
        }
    };
    private final Context mContext;
    private final NotificationBackend mNotifBackend;
    private final PackageManager mPm = this.mContext.getPackageManager();

    public AppStateNotificationBridge(Context context, ApplicationsState appState, Callback callback, NotificationBackend notifBackend) {
        super(appState, callback);
        this.mContext = context;
        this.mNotifBackend = notifBackend;
    }

    protected void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry app = (AppEntry) apps.get(i);
            app.extraInfo = this.mNotifBackend.loadAppRow(this.mContext, this.mPm, app.info);
        }
    }

    protected void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = this.mNotifBackend.loadAppRow(this.mContext, this.mPm, app.info);
    }
}
