package com.android.settings.applications;

import android.content.Context;
import android.os.RemoteException;
import android.os.ServiceManager;
import com.android.internal.telephony.ISms;
import com.android.internal.telephony.ISms.Stub;
import com.android.settings.applications.AppStateBaseBridge.Callback;
import com.android.settingslib.applications.ApplicationsState;
import com.android.settingslib.applications.ApplicationsState.AppEntry;
import com.android.settingslib.applications.ApplicationsState.AppFilter;
import java.util.ArrayList;

public class AppStateSmsPremBridge extends AppStateBaseBridge {
    public static final AppFilter FILTER_APP_PREMIUM_SMS = new AppFilter() {
        public void init() {
        }

        public boolean filterApp(AppEntry info) {
            return (info.extraInfo instanceof SmsState) && ((SmsState) info.extraInfo).smsState != 0;
        }
    };
    private final Context mContext;
    private final ISms mSmsManager = Stub.asInterface(ServiceManager.getService("isms"));

    public static class SmsState {
        public int smsState;
    }

    public AppStateSmsPremBridge(Context context, ApplicationsState appState, Callback callback) {
        super(appState, callback);
        this.mContext = context;
    }

    protected void loadAllExtraInfo() {
        ArrayList<AppEntry> apps = this.mAppSession.getAllApps();
        int N = apps.size();
        for (int i = 0; i < N; i++) {
            AppEntry app = (AppEntry) apps.get(i);
            updateExtraInfo(app, app.info.packageName, app.info.uid);
        }
    }

    protected void updateExtraInfo(AppEntry app, String pkg, int uid) {
        app.extraInfo = getState(pkg);
    }

    public SmsState getState(String pkg) {
        SmsState state = new SmsState();
        state.smsState = getSmsState(pkg);
        return state;
    }

    private int getSmsState(String pkg) {
        try {
            return this.mSmsManager.getPremiumSmsPermission(pkg);
        } catch (RemoteException e) {
            return 0;
        }
    }

    public void setSmsState(String pkg, int state) {
        try {
            this.mSmsManager.setPremiumSmsPermission(pkg, state);
        } catch (RemoteException e) {
        }
    }
}
