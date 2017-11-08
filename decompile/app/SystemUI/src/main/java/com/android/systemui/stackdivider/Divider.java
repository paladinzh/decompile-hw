package com.android.systemui.stackdivider;

import android.content.res.Configuration;
import android.os.RemoteException;
import android.view.IDockedStackListener.Stub;
import android.view.LayoutInflater;
import com.android.systemui.R;
import com.android.systemui.SystemUI;
import com.android.systemui.recents.Recents;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class Divider extends SystemUI {
    private boolean mAdjustedForIme = false;
    private DividerMenusView mDividerMenusView;
    private final DividerState mDividerState = new DividerState();
    private DockDividerVisibilityListener mDockDividerVisibilityListener;
    int mDockSide;
    private ForcedResizableInfoActivityController mForcedResizableController;
    private boolean mMinimized = false;
    private DividerView mView;
    private boolean mVisible = false;
    private DividerWindowManager mWindowManager;
    private final WindowManagerProxy mWindowManagerProxy = WindowManagerProxy.getInstance();

    class DockDividerVisibilityListener extends Stub {

        final /* synthetic */ class -void_onAdjustedForImeChanged_boolean_adjustedForIme_long_animDuration_LambdaImpl0 implements Runnable {
            private /* synthetic */ boolean val$adjustedForIme;
            private /* synthetic */ long val$animDuration;
            private /* synthetic */ DockDividerVisibilityListener val$this;

            public /* synthetic */ -void_onAdjustedForImeChanged_boolean_adjustedForIme_long_animDuration_LambdaImpl0(DockDividerVisibilityListener dockDividerVisibilityListener, boolean z, long j) {
                this.val$this = dockDividerVisibilityListener;
                this.val$adjustedForIme = z;
                this.val$animDuration = j;
            }

            public void run() {
                this.val$this.-com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$1(this.val$adjustedForIme, this.val$animDuration);
            }
        }

        final /* synthetic */ class -void_onDockSideChanged_int_newDockSide_LambdaImpl0 implements Runnable {
            private /* synthetic */ int val$newDockSide;
            private /* synthetic */ DockDividerVisibilityListener val$this;

            public /* synthetic */ -void_onDockSideChanged_int_newDockSide_LambdaImpl0(DockDividerVisibilityListener dockDividerVisibilityListener, int i) {
                this.val$this = dockDividerVisibilityListener;
                this.val$newDockSide = i;
            }

            public void run() {
                this.val$this.-com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$2(this.val$newDockSide);
            }
        }

        DockDividerVisibilityListener() {
        }

        public void onDividerVisibilityChanged(boolean visible) throws RemoteException {
            Divider.this.updateVisibility(visible);
        }

        public void onDockedStackExistsChanged(boolean exists) throws RemoteException {
            Divider.this.notifyDockedStackExistsChanged(exists);
        }

        public void onDockedStackMinimizedChanged(boolean minimized, long animDuration) throws RemoteException {
            Divider.this.updateMinimizedDockedStack(minimized, animDuration);
        }

        public void onAdjustedForImeChanged(boolean adjustedForIme, long animDuration) throws RemoteException {
            Divider.this.mView.post(new -void_onAdjustedForImeChanged_boolean_adjustedForIme_long_animDuration_LambdaImpl0(this, adjustedForIme, animDuration));
        }

        /* synthetic */ void -com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$1(boolean adjustedForIme, long animDuration) {
            if (Divider.this.mAdjustedForIme != adjustedForIme) {
                Divider.this.mAdjustedForIme = adjustedForIme;
                Divider.this.updateTouchable();
                if (!Divider.this.mMinimized) {
                    if (animDuration > 0) {
                        Divider.this.mView.setAdjustedForIme(adjustedForIme, animDuration);
                    } else {
                        Divider.this.mView.setAdjustedForIme(adjustedForIme);
                    }
                }
            }
        }

        public void onDockSideChanged(int newDockSide) throws RemoteException {
            Divider.this.mDockSide = newDockSide;
            Divider.this.mView.post(new -void_onDockSideChanged_int_newDockSide_LambdaImpl0(this, newDockSide));
        }

        /* synthetic */ void -com_android_systemui_stackdivider_Divider$DockDividerVisibilityListener_lambda$2(int newDockSide) {
            if (newDockSide == 3) {
                Divider.this.update(Divider.this.mContext.getResources().getConfiguration(), newDockSide);
            }
            Divider.this.mView.notifyDockSideChanged(newDockSide);
        }
    }

    public void start() {
        this.mWindowManager = new DividerWindowManager(this.mContext);
        update(this.mContext.getResources().getConfiguration(), this.mWindowManagerProxy.getDockSide());
        putComponent(Divider.class, this);
        this.mDockDividerVisibilityListener = new DockDividerVisibilityListener();
        Recents.getSystemServices().registerDockedStackListener(this.mDockDividerVisibilityListener);
        this.mForcedResizableController = new ForcedResizableInfoActivityController(this.mContext);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        update(newConfig, this.mWindowManagerProxy.getDockSide());
    }

    public DividerView getView() {
        return this.mView;
    }

    private void addDivider(Configuration configuration) {
        boolean landscape;
        this.mView = (DividerView) LayoutInflater.from(this.mContext).inflate(R.layout.docked_stack_divider, null);
        this.mView.setVisibility(this.mVisible ? 0 : 4);
        int size = this.mContext.getResources().getDimensionPixelSize(17104929);
        if (configuration.orientation == 2) {
            landscape = true;
        } else {
            landscape = false;
        }
        int width = landscape ? size : -1;
        int height = landscape ? -1 : size;
        initDivderMenus();
        this.mWindowManager.add(this.mView, width, height);
        this.mView.injectDependencies(this.mWindowManager, this.mDividerState);
    }

    private void removeDivider() {
        if (this.mView != null) {
            this.mView.removeMenusView();
        }
        this.mWindowManager.remove();
    }

    public void initDivderMenus() {
        HwLog.i("Divider", "initDivderMenus: " + this.mDockSide);
        if (this.mDockSide == 3) {
            this.mDividerMenusView = (DividerMenusView) LayoutInflater.from(this.mContext).inflate(R.layout.docked_stack_menu_right, null);
        } else {
            this.mDividerMenusView = (DividerMenusView) LayoutInflater.from(this.mContext).inflate(R.layout.docked_stack_menu, null);
        }
        this.mDividerMenusView.setDividerView(this.mView);
        this.mDividerMenusView.addViewToWindow();
        this.mView.setDividerMenuView(this.mDividerMenusView);
    }

    private void update(Configuration configuration, int dockSide) {
        HwLog.i("Divider", "update: orientation=" + configuration.orientation + ", dockSide=" + dockSide);
        this.mDockSide = dockSide;
        removeDivider();
        addDivider(configuration);
        if (this.mMinimized) {
            this.mView.setMinimizedDockStack(true);
            updateTouchable();
        }
    }

    private void updateVisibility(final boolean visible) {
        HwLog.i("Divider", "updateVisibility: " + visible);
        this.mView.post(new Runnable() {
            public void run() {
                Divider.this.updateAfterDockChanged(visible);
                if (Divider.this.mVisible != visible) {
                    Divider.this.mVisible = visible;
                    Divider.this.mView.setVisibility(visible ? 0 : 4);
                    Divider.this.mView.setMinimizedDockStack(Divider.this.mMinimized);
                }
            }
        });
    }

    public void updateAfterDockChanged(boolean visible) {
        int dockSide = this.mWindowManagerProxy.getDockSide();
        if (visible && this.mDockSide != dockSide) {
            update(this.mContext.getResources().getConfiguration(), dockSide);
        }
    }

    private void updateMinimizedDockedStack(final boolean minimized, final long animDuration) {
        this.mView.post(new Runnable() {
            public void run() {
                if (Divider.this.mMinimized != minimized) {
                    Divider.this.mMinimized = minimized;
                    Divider.this.updateTouchable();
                    if (animDuration > 0) {
                        Divider.this.mView.setMinimizedDockStack(minimized, animDuration);
                    } else {
                        Divider.this.mView.setMinimizedDockStack(minimized);
                    }
                }
            }
        });
    }

    private void notifyDockedStackExistsChanged(final boolean exists) {
        this.mView.post(new Runnable() {
            public void run() {
                TintManager.getInstance().setDockedStackExists(exists);
                TintManager.getInstance().updateBarTint();
                Divider.this.mForcedResizableController.notifyDockedStackExistsChanged(exists);
            }
        });
    }

    private void updateTouchable() {
        boolean result = !this.mAdjustedForIme;
        if (!Recents.getSystemServices().isCurrentHomeActivity()) {
            result = result && !this.mMinimized;
        }
        this.mWindowManager.setTouchable(result);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        pw.print("  mVisible=");
        pw.println(this.mVisible);
        pw.print("  mMinimized=");
        pw.println(this.mMinimized);
        pw.print("  mAdjustedForIme=");
        pw.println(this.mAdjustedForIme);
    }
}
