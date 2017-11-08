package com.android.systemui.statusbar.phone;

import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.os.UserHandle;
import android.os.UserManager;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class ManagedProfileController {
    private final List<Callback> mCallbacks = new ArrayList();
    private final Context mContext;
    private int mCurrentUser;
    private boolean mListening;
    private final LinkedList<UserInfo> mProfiles;
    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            ManagedProfileController.this.reloadManagedProfiles();
            for (Callback callback : ManagedProfileController.this.mCallbacks) {
                callback.onManagedProfileChanged();
            }
        }
    };
    private final UserManager mUserManager;

    public interface Callback {
        void onManagedProfileChanged();

        void onManagedProfileRemoved();
    }

    public ManagedProfileController(QSTileHost host) {
        this.mContext = host.getContext();
        this.mUserManager = UserManager.get(this.mContext);
        this.mProfiles = new LinkedList();
    }

    public void addCallback(Callback callback) {
        this.mCallbacks.add(callback);
        if (this.mCallbacks.size() == 1) {
            setListening(true);
        }
        callback.onManagedProfileChanged();
    }

    public void removeCallback(Callback callback) {
        if (this.mCallbacks.remove(callback) && this.mCallbacks.size() == 0) {
            setListening(false);
        }
    }

    public void setWorkModeEnabled(boolean enableWorkMode) {
        synchronized (this.mProfiles) {
            for (UserInfo ui : this.mProfiles) {
                if (!enableWorkMode) {
                    this.mUserManager.setQuietModeEnabled(ui.id, true);
                } else if (!this.mUserManager.trySetQuietModeDisabled(ui.id, null)) {
                    ((StatusBarManager) this.mContext.getSystemService("statusbar")).collapsePanels();
                }
            }
        }
    }

    private void reloadManagedProfiles() {
        synchronized (this.mProfiles) {
            boolean hadProfile = this.mProfiles.size() > 0;
            int user = UserSwitchUtils.getCurrentUser();
            this.mProfiles.clear();
            for (UserInfo ui : this.mUserManager.getEnabledProfiles(user)) {
                if (ui.isManagedProfile()) {
                    this.mProfiles.add(ui);
                }
            }
            if (this.mProfiles.size() == 0 && hadProfile && user == this.mCurrentUser) {
                for (Callback callback : this.mCallbacks) {
                    callback.onManagedProfileRemoved();
                }
            }
            this.mCurrentUser = user;
        }
    }

    public boolean hasActiveProfile() {
        boolean z = false;
        if (!(this.mListening && this.mCurrentUser == UserSwitchUtils.getCurrentUser())) {
            reloadManagedProfiles();
        }
        synchronized (this.mProfiles) {
            if (this.mProfiles.size() > 0) {
                z = true;
            }
        }
        return z;
    }

    public boolean isWorkModeEnabled() {
        if (!this.mListening) {
            reloadManagedProfiles();
        }
        synchronized (this.mProfiles) {
            for (UserInfo ui : this.mProfiles) {
                if (ui.isQuietModeEnabled()) {
                    return false;
                }
            }
            return true;
        }
    }

    private void setListening(boolean listening) {
        this.mListening = listening;
        if (listening) {
            reloadManagedProfiles();
            IntentFilter filter = new IntentFilter();
            filter.addAction("android.intent.action.USER_SWITCHED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_ADDED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_REMOVED");
            filter.addAction("android.intent.action.MANAGED_PROFILE_AVAILABLE");
            filter.addAction("android.intent.action.MANAGED_PROFILE_UNAVAILABLE");
            this.mContext.registerReceiverAsUser(this.mReceiver, UserHandle.ALL, filter, null, null);
            return;
        }
        this.mContext.unregisterReceiver(this.mReceiver);
    }
}
