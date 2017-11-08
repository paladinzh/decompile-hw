package com.android.systemui.statusbar.phone;

import android.content.Context;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import java.util.ArrayList;

public class UnlockMethodCache {
    private static UnlockMethodCache sInstance;
    private final KeyguardUpdateMonitorCallback mCallback = new KeyguardUpdateMonitorCallback() {
        public void onUserSwitchComplete(int userId) {
            UnlockMethodCache.this.update(false);
        }

        public void onTrustChanged(int userId) {
            UnlockMethodCache.this.update(false);
        }

        public void onTrustManagedChanged(int userId) {
            UnlockMethodCache.this.update(false);
        }

        public void onStartedWakingUp() {
            UnlockMethodCache.this.update(false);
        }

        public void onFaceUnlockStateChanged(boolean running, int userId) {
            UnlockMethodCache.this.update(false);
        }

        public void onStrongAuthStateChanged(int userId) {
            UnlockMethodCache.this.update(false);
        }
    };
    private boolean mCanSkipBouncer;
    private boolean mFaceUnlockRunning;
    private final HwKeyguardUpdateMonitor mKeyguardUpdateMonitor;
    private final ArrayList<OnUnlockMethodChangedListener> mListeners = new ArrayList();
    private boolean mSecure;
    private boolean mTrustManaged;
    private boolean mTrusted;

    public interface OnUnlockMethodChangedListener {
        void onUnlockMethodStateChanged();
    }

    private UnlockMethodCache(Context ctx) {
        this.mKeyguardUpdateMonitor = HwKeyguardUpdateMonitor.getInstance(ctx);
        KeyguardUpdateMonitor.getInstance(ctx).registerCallback(this.mCallback);
        update(true);
    }

    public static UnlockMethodCache getInstance(Context context) {
        if (sInstance == null) {
            sInstance = new UnlockMethodCache(context);
        }
        return sInstance;
    }

    public boolean isMethodSecure() {
        return this.mSecure;
    }

    public boolean isTrusted() {
        return this.mTrusted;
    }

    public boolean canSkipBouncer() {
        return this.mCanSkipBouncer;
    }

    public void addListener(OnUnlockMethodChangedListener listener) {
        this.mListeners.add(listener);
    }

    private void update(boolean updateAlways) {
        boolean changed = true;
        int user = KeyguardUpdateMonitor.getCurrentUser();
        boolean secure = this.mKeyguardUpdateMonitor.isSecure(user);
        boolean userCanSkipBouncer = secure ? this.mKeyguardUpdateMonitor.getUserCanSkipBouncer(user) : true;
        boolean trustManaged = this.mKeyguardUpdateMonitor.getUserTrustIsManaged(user);
        boolean trusted = this.mKeyguardUpdateMonitor.getUserHasTrust(user);
        boolean z = this.mKeyguardUpdateMonitor.isFaceUnlockRunning(user) ? trustManaged : false;
        if (secure == this.mSecure && userCanSkipBouncer == this.mCanSkipBouncer && trustManaged == this.mTrustManaged && z == this.mFaceUnlockRunning) {
            changed = false;
        }
        if (changed || updateAlways) {
            this.mSecure = secure;
            this.mCanSkipBouncer = userCanSkipBouncer;
            this.mTrusted = trusted;
            this.mTrustManaged = trustManaged;
            this.mFaceUnlockRunning = z;
            notifyListeners();
        }
    }

    private void notifyListeners() {
        for (OnUnlockMethodChangedListener listener : this.mListeners) {
            listener.onUnlockMethodStateChanged();
        }
    }

    public boolean isTrustManaged() {
        return this.mTrustManaged;
    }

    public boolean isFaceUnlockRunning() {
        return this.mFaceUnlockRunning;
    }
}
