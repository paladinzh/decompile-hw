package com.android.systemui.assist;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.app.SearchManager;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Resources.NotFoundException;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.RemoteException;
import android.os.UserHandle;
import android.provider.Settings.Secure;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.ImageView;
import com.android.internal.app.AssistUtils;
import com.android.internal.app.IVoiceInteractionSessionShowCallback;
import com.android.internal.app.IVoiceInteractionSessionShowCallback.Stub;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.BDReporter;

public class AssistManager {
    private final AssistDisclosure mAssistDisclosure;
    private final AssistUtils mAssistUtils;
    private final BaseStatusBar mBar;
    private final Context mContext;
    private Runnable mHideRunnable = new Runnable() {
        public void run() {
            AssistManager.this.mView.removeCallbacks(this);
            AssistManager.this.mView.show(false, true);
        }
    };
    private IVoiceInteractionSessionShowCallback mShowCallback = new Stub() {
        public void onFailed() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
            HwLog.i("AssistManager", "onFailed");
        }

        public void onShown() throws RemoteException {
            AssistManager.this.mView.post(AssistManager.this.mHideRunnable);
            HwLog.i("AssistManager", "onShown");
        }
    };
    private AssistOrbContainer mView;
    private final WindowManager mWindowManager;

    public AssistManager(BaseStatusBar bar, Context context) {
        this.mContext = context;
        this.mBar = bar;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mAssistUtils = new AssistUtils(context);
        this.mAssistDisclosure = new AssistDisclosure(context, new Handler());
    }

    public void onConfigurationChanged() {
        boolean z = false;
        if (this.mView != null) {
            z = this.mView.isShowing();
            this.mWindowManager.removeView(this.mView);
        }
        this.mView = (AssistOrbContainer) LayoutInflater.from(this.mContext).inflate(R.layout.assist_orb, null);
        this.mView.setVisibility(8);
        this.mView.setSystemUiVisibility(1792);
        SystemUiUtil.addWindowView(this.mWindowManager, this.mView, getLayoutParams());
        if (z) {
            this.mView.show(true, false);
        }
    }

    public void startAssist(Bundle args) {
        ComponentName assistComponent = getAssistInfo();
        if (assistComponent == null) {
            HwLog.w("AssistManager", "startAssist::assistComponent is null, return");
            return;
        }
        boolean isService = assistComponent.equals(getVoiceInteractorComponentName());
        boolean isVoiceRunning = isVoiceSessionRunning();
        HwLog.i("AssistManager", "startAssist::isService=" + isService + ", isVoiceSessionRunning=" + isVoiceRunning);
        if (!(isService && isVoiceRunning)) {
            long j;
            showOrb(assistComponent, isService);
            AssistOrbContainer assistOrbContainer = this.mView;
            Runnable runnable = this.mHideRunnable;
            if (isService) {
                j = 2500;
            } else {
                j = 1000;
            }
            assistOrbContainer.postDelayed(runnable, j);
        }
        if (args == null) {
            HwLog.i("AssistManager", "startAssist::args is null, new bundle to avoid it.");
            args = new Bundle();
        }
        startAssistInternal(args, assistComponent, isService);
    }

    public void hideAssist() {
        this.mAssistUtils.hideCurrentSession();
    }

    private LayoutParams getLayoutParams() {
        LayoutParams lp = new LayoutParams(-1, this.mContext.getResources().getDimensionPixelSize(R.dimen.assist_orb_scrim_height), 2033, 280, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.setTitle("AssistPreviewPanel");
        lp.softInputMode = 49;
        return lp;
    }

    private void showOrb(ComponentName assistComponent, boolean isService) {
        maybeSwapSearchIcon(assistComponent, isService);
        this.mView.show(true, true);
    }

    private void startAssistInternal(Bundle args, ComponentName assistComponent, boolean isService) {
        if (isService) {
            startVoiceInteractor(args);
        } else {
            startAssistActivity(args, assistComponent);
        }
    }

    private void startAssistActivity(Bundle args, ComponentName assistComponent) {
        if (this.mBar.isDeviceProvisioned()) {
            this.mBar.animateCollapsePanels(3);
            boolean structureEnabled = Secure.getIntForUser(this.mContext.getContentResolver(), "assist_structure_enabled", 1, -2) != 0;
            final Intent intent = ((SearchManager) this.mContext.getSystemService("search")).getAssistIntent(structureEnabled);
            if (intent == null) {
                HwLog.i("AssistManager", "startAssistActivity::intent is null, return");
                return;
            }
            intent.setComponent(assistComponent);
            intent.putExtras(args);
            if (structureEnabled) {
                showDisclosure();
            }
            try {
                final ActivityOptions opts = ActivityOptions.makeCustomAnimation(this.mContext, R.anim.search_launch_enter, R.anim.search_launch_exit);
                intent.addFlags(268435456);
                AsyncTask.execute(new Runnable() {
                    public void run() {
                        AssistManager.this.mContext.startActivityAsUser(intent, opts.toBundle(), new UserHandle(-2));
                        BDReporter.c(AssistManager.this.mContext, 10);
                    }
                });
            } catch (ActivityNotFoundException e) {
                Log.w("AssistManager", "Activity not found for " + intent.getAction());
            }
            return;
        }
        HwLog.i("AssistManager", "startAssistActivity::bar is in provisioned mode, return");
    }

    private void startVoiceInteractor(Bundle args) {
        this.mAssistUtils.showSessionForActiveService(args, 4, this.mShowCallback, null);
    }

    public void launchVoiceAssistFromKeyguard() {
        this.mAssistUtils.launchVoiceAssistFromKeyguard();
    }

    public boolean canVoiceAssistBeLaunchedFromKeyguard() {
        return this.mAssistUtils.activeServiceSupportsLaunchFromKeyguard();
    }

    public ComponentName getVoiceInteractorComponentName() {
        return this.mAssistUtils.getActiveServiceComponentName();
    }

    private boolean isVoiceSessionRunning() {
        return this.mAssistUtils.isSessionRunning();
    }

    public void destroy() {
        this.mWindowManager.removeViewImmediate(this.mView);
    }

    private void maybeSwapSearchIcon(ComponentName assistComponent, boolean isService) {
        replaceDrawable(this.mView.getOrb().getLogo(), assistComponent, "com.android.systemui.action_assist_icon", isService);
    }

    public void replaceDrawable(ImageView v, ComponentName component, String name, boolean isService) {
        if (component != null) {
            try {
                Bundle metaData;
                PackageManager packageManager = this.mContext.getPackageManager();
                if (isService) {
                    metaData = packageManager.getServiceInfo(component, 128).metaData;
                } else {
                    metaData = packageManager.getActivityInfo(component, 128).metaData;
                }
                if (metaData != null) {
                    int iconResId = metaData.getInt(name);
                    if (iconResId != 0) {
                        v.setImageDrawable(packageManager.getResourcesForApplication(component.getPackageName()).getDrawable(iconResId));
                        return;
                    }
                }
            } catch (NameNotFoundException e) {
                Log.v("AssistManager", "Assistant component " + component.flattenToShortString() + " not found");
            } catch (NotFoundException nfe) {
                Log.w("AssistManager", "Failed to swap drawable from " + component.flattenToShortString(), nfe);
            }
        }
        v.setImageDrawable(null);
    }

    private ComponentName getAssistInfo() {
        return this.mAssistUtils.getAssistComponentForUser(-2);
    }

    public void showDisclosure() {
        this.mAssistDisclosure.postShow();
    }

    public void onLockscreenShown() {
        this.mAssistUtils.onLockscreenShown();
    }
}
