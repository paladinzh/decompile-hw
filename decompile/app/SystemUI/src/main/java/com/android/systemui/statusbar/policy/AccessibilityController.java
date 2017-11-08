package com.android.systemui.statusbar.policy;

import android.content.Context;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.view.accessibility.AccessibilityManager.TouchExplorationStateChangeListener;
import java.util.ArrayList;

public class AccessibilityController implements AccessibilityStateChangeListener, TouchExplorationStateChangeListener {
    private boolean mAccessibilityEnabled;
    private final ArrayList<AccessibilityStateChangedCallback> mChangeCallbacks = new ArrayList();
    private boolean mTouchExplorationEnabled;

    public interface AccessibilityStateChangedCallback {
        void onStateChanged(boolean z, boolean z2);
    }

    public AccessibilityController(Context context) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService("accessibility");
        am.addTouchExplorationStateChangeListener(this);
        am.addAccessibilityStateChangeListener(this);
        this.mAccessibilityEnabled = am.isEnabled();
        this.mTouchExplorationEnabled = am.isTouchExplorationEnabled();
    }

    public boolean isAccessibilityEnabled() {
        return this.mAccessibilityEnabled;
    }

    public boolean isTouchExplorationEnabled() {
        return this.mTouchExplorationEnabled;
    }

    public void addStateChangedCallback(AccessibilityStateChangedCallback cb) {
        this.mChangeCallbacks.add(cb);
        cb.onStateChanged(this.mAccessibilityEnabled, this.mTouchExplorationEnabled);
    }

    private void fireChanged() {
        int N = this.mChangeCallbacks.size();
        for (int i = 0; i < N; i++) {
            ((AccessibilityStateChangedCallback) this.mChangeCallbacks.get(i)).onStateChanged(this.mAccessibilityEnabled, this.mTouchExplorationEnabled);
        }
    }

    public void onAccessibilityStateChanged(boolean enabled) {
        this.mAccessibilityEnabled = enabled;
        fireChanged();
    }

    public void onTouchExplorationStateChanged(boolean enabled) {
        this.mTouchExplorationEnabled = enabled;
        fireChanged();
    }
}
