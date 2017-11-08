package com.android.systemui.statusbar.phone;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.IStatusBarService.Stub;
import com.android.systemui.R;

public class NavigationBarTransitions extends BarTransitions {
    private final IStatusBarService mBarService;
    private boolean mLightsOut;
    private final OnTouchListener mLightsOutListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent ev) {
            if (ev.getAction() == 0) {
                NavigationBarTransitions.this.applyLightsOut(false, false, false);
                try {
                    NavigationBarTransitions.this.mBarService.setSystemUiVisibility(0, 1, "LightsOutListener");
                } catch (RemoteException e) {
                }
            }
            return false;
        }
    };
    private final NavigationBarView mView;

    public NavigationBarTransitions(NavigationBarView view) {
        super(view, R.drawable.nav_background, true);
        this.mView = view;
        this.mBarService = Stub.asInterface(ServiceManager.getService("statusbar"));
    }

    public void init() {
        applyModeBackground(-1, getMode(), false);
        applyMode(getMode(), false, true);
    }

    protected void onTransition(int oldMode, int newMode, boolean animate) {
        super.onTransition(oldMode, newMode, animate);
        applyMode(newMode, animate, false);
    }

    private void applyMode(int mode, boolean animate, boolean force) {
        applyLightsOut(isLightsOut(mode), animate, force);
    }

    private void applyLightsOut(boolean lightsOut, boolean animate, boolean force) {
        if (force || lightsOut != this.mLightsOut) {
            this.mLightsOut = lightsOut;
            View navButtons = this.mView.getCurrentView().findViewById(R.id.nav_buttons);
            navButtons.animate().cancel();
            float navButtonsAlpha = lightsOut ? 0.5f : 1.0f;
            if (animate) {
                navButtons.animate().alpha(navButtonsAlpha).setDuration((long) (lightsOut ? 750 : 250)).start();
            } else {
                navButtons.setAlpha(navButtonsAlpha);
            }
        }
    }
}
