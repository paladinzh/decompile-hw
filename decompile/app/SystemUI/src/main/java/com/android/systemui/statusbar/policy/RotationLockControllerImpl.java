package com.android.systemui.statusbar.policy;

import android.content.Context;
import com.android.internal.view.RotationPolicy;
import com.android.internal.view.RotationPolicy.RotationPolicyListener;
import com.android.systemui.statusbar.policy.RotationLockController.RotationLockControllerCallback;
import java.util.concurrent.CopyOnWriteArrayList;

public final class RotationLockControllerImpl implements RotationLockController {
    private final CopyOnWriteArrayList<RotationLockControllerCallback> mCallbacks = new CopyOnWriteArrayList();
    private final Context mContext;
    private final RotationPolicyListener mRotationPolicyListener = new RotationPolicyListener() {
        public void onChange() {
            RotationLockControllerImpl.this.notifyChanged();
        }
    };

    public RotationLockControllerImpl(Context context) {
        this.mContext = context;
        setListening(true);
    }

    public void addRotationLockControllerCallback(RotationLockControllerCallback callback) {
        this.mCallbacks.add(callback);
        notifyChanged(callback);
    }

    public void removeRotationLockControllerCallback(RotationLockControllerCallback callback) {
        this.mCallbacks.remove(callback);
    }

    public int getRotationLockOrientation() {
        return RotationPolicy.getRotationLockOrientation(this.mContext);
    }

    public boolean isRotationLocked() {
        return RotationPolicy.isRotationLocked(this.mContext);
    }

    public void setRotationLocked(boolean locked) {
        RotationPolicy.setRotationLock(this.mContext, locked);
    }

    public void setListening(boolean listening) {
        if (listening) {
            RotationPolicy.registerRotationPolicyListener(this.mContext, this.mRotationPolicyListener, -1);
        } else {
            RotationPolicy.unregisterRotationPolicyListener(this.mContext, this.mRotationPolicyListener);
        }
    }

    private void notifyChanged() {
        for (RotationLockControllerCallback callback : this.mCallbacks) {
            notifyChanged(callback);
        }
    }

    private void notifyChanged(RotationLockControllerCallback callback) {
        callback.onRotationLockStateChanged(RotationPolicy.isRotationLocked(this.mContext), RotationPolicy.isRotationLockToggleVisible(this.mContext));
    }
}
