package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.settings.CurrentUserTracker;
import com.android.systemui.utils.UserSwitchUtils;
import java.util.ArrayList;

public final class KeyguardMonitor extends KeyguardUpdateMonitorCallback {
    private final ArrayList<Callback> mCallbacks = new ArrayList();
    private boolean mCanSkipBouncer;
    private final Context mContext;
    private int mCurrentUser;
    private final KeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private boolean mListening;
    private boolean mSecure;
    private boolean mShowing;
    private Object mSyncObject = new Object();
    private final CurrentUserTracker mUserTracker;

    public interface Callback {
        void onKeyguardChanged();
    }

    public KeyguardMonitor(Context context) {
        this.mContext = context;
        this.mKeyguardUpdateMonitor = KeyguardUpdateMonitor.getInstance(this.mContext);
        this.mUserTracker = new CurrentUserTracker(this.mContext) {
            public void onUserSwitched(int newUserId) {
                KeyguardMonitor.this.mCurrentUser = newUserId;
                KeyguardMonitor.this.updateCanSkipBouncerState();
            }
        };
    }

    public void addCallback(Callback callback) {
        synchronized (this.mSyncObject) {
            this.mCallbacks.add(callback);
            boolean isEmpty = this.mCallbacks.size() == 0;
        }
        if (!isEmpty && !this.mListening) {
            this.mListening = true;
            this.mCurrentUser = UserSwitchUtils.getCurrentUser();
            updateCanSkipBouncerState();
            this.mKeyguardUpdateMonitor.registerCallback(this);
            this.mUserTracker.startTracking();
        }
    }

    public void removeCallback(Callback callback) {
        synchronized (this.mSyncObject) {
            boolean removeSuccess = this.mCallbacks.remove(callback);
            boolean isEmpty = this.mCallbacks.size() == 0;
        }
        if (removeSuccess && isEmpty && this.mListening) {
            this.mListening = false;
            this.mKeyguardUpdateMonitor.removeCallback(this);
            this.mUserTracker.stopTracking();
        }
    }

    public boolean isShowing() {
        return this.mShowing;
    }

    public boolean isSecure() {
        return this.mSecure;
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public void notifyKeyguardState(boolean showing, boolean secure) {
        if (this.mShowing != showing || this.mSecure != secure) {
            this.mShowing = showing;
            this.mSecure = secure;
            notifyKeyguardChanged();
        }
    }

    public void onTrustChanged(int userId) {
        updateCanSkipBouncerState();
        notifyKeyguardChanged();
    }

    private void updateCanSkipBouncerState() {
        this.mCanSkipBouncer = this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(this.mCurrentUser);
    }

    private void notifyKeyguardChanged() {
        ArrayList<Callback> tempList = new ArrayList();
        synchronized (this.mSyncObject) {
            tempList.addAll(this.mCallbacks);
        }
        for (Callback callback : tempList) {
            callback.onKeyguardChanged();
        }
    }
}
