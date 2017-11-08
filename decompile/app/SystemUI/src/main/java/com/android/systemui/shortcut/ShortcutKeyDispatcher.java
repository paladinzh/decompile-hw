package com.android.systemui.shortcut;

import android.app.ActivityManagerNative;
import android.app.IActivityManager;
import android.os.RemoteException;
import android.util.Log;
import android.view.IWindowManager;
import android.view.WindowManagerGlobal;
import com.android.internal.policy.DividerSnapAlgorithm;
import com.android.internal.policy.DividerSnapAlgorithm.SnapTarget;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.shortcut.ShortcutKeyServiceProxy.Callbacks;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.stackdivider.DividerView;

public class ShortcutKeyDispatcher extends SystemUI implements Callbacks {
    protected final long ALT_MASK = 8589934592L;
    protected final long CTRL_MASK = 17592186044416L;
    protected final long META_MASK = 281474976710656L;
    protected final long SC_DOCK_LEFT = 281474976710727L;
    protected final long SC_DOCK_RIGHT = 281474976710728L;
    protected final long SHIFT_MASK = 4294967296L;
    private IActivityManager mActivityManager = ActivityManagerNative.getDefault();
    private ShortcutKeyServiceProxy mShortcutKeyServiceProxy = new ShortcutKeyServiceProxy(this);
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

    public void registerShortcutKey(long shortcutCode) {
        try {
            this.mWindowManagerService.registerShortcutKey(shortcutCode, this.mShortcutKeyServiceProxy);
        } catch (RemoteException e) {
        }
    }

    public void onShortcutKeyPressed(long shortcutCode) {
        int orientation = this.mContext.getResources().getConfiguration().orientation;
        if ((shortcutCode == 281474976710727L || shortcutCode == 281474976710728L) && orientation == 2) {
            handleDockKey(shortcutCode);
        }
    }

    public void start() {
        registerShortcutKey(281474976710727L);
        registerShortcutKey(281474976710728L);
    }

    private void handleDockKey(long shortcutCode) {
        try {
            if (this.mWindowManagerService.getDockedStackSide() == -1) {
                int dockMode;
                Recents recents = (Recents) getComponent(Recents.class);
                if (shortcutCode == 281474976710727L) {
                    dockMode = 0;
                } else {
                    dockMode = 1;
                }
                recents.dockTopTask(-1, dockMode, null, 352);
                return;
            }
            DividerView dividerView = ((Divider) getComponent(Divider.class)).getView();
            DividerSnapAlgorithm snapAlgorithm = dividerView.getSnapAlgorithm();
            SnapTarget target = snapAlgorithm.cycleNonDismissTarget(snapAlgorithm.calculateNonDismissingSnapTarget(dividerView.getCurrentPosition()), shortcutCode == 281474976710727L ? -1 : 1);
            dividerView.startDragging(true, false);
            dividerView.stopDragging(target.position, 0.0f, true, true);
        } catch (RemoteException e) {
            Log.e("ShortcutKeyDispatcher", "handleDockKey() failed.");
        }
    }
}
