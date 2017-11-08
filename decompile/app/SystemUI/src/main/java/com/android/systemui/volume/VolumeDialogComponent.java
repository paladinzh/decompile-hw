package com.android.systemui.volume;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.media.VolumePolicy;
import android.os.Bundle;
import android.os.Handler;
import android.view.ContextThemeWrapper;
import com.android.systemui.SystemUI;
import com.android.systemui.keyguard.HwKeyguardViewMediator;
import com.android.systemui.keyguard.KeyguardViewMediator;
import com.android.systemui.qs.tiles.DndTile;
import com.android.systemui.statusbar.phone.PhoneStatusBar;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.volume.VolumeDialog.Callback;
import java.io.FileDescriptor;
import java.io.PrintWriter;

public class VolumeDialogComponent implements VolumeComponent, Tunable {
    private final Context mContext;
    private final VolumeDialogController mController;
    private final HwVolumeDialog mDialog;
    private final SystemUI mSysui;
    private final Callback mVolumeDialogCallback = new Callback() {
        public void onZenPrioritySettingsClicked() {
            VolumeDialogComponent.this.startSettings(ZenModePanel.ZEN_PRIORITY_SETTINGS);
        }
    };
    private VolumePolicy mVolumePolicy = new VolumePolicy(false, true, true, 400);
    private final ZenModeController mZenModeController;

    public VolumeDialogComponent(SystemUI sysui, Context context, Handler handler, ZenModeController zen) {
        this.mSysui = sysui;
        this.mContext = context;
        this.mController = new VolumeDialogController(context, null) {
            protected void onUserActivityW() {
                VolumeDialogComponent.this.sendUserActivity();
            }
        };
        this.mZenModeController = zen;
        ZenModeController zenModeController = zen;
        this.mDialog = new HwVolumeDialog(new ContextThemeWrapper(this.mContext, this.mContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null)), 2020, this.mController, zenModeController, this.mVolumeDialogCallback);
        this.mController.setSilentViewCallback(this.mDialog.getSilentViewCallback());
        applyConfiguration();
        TunerService.get(this.mContext).addTunable((Tunable) this, "sysui_volume_down_silent", "sysui_volume_up_silent", "sysui_do_not_disturb");
    }

    public void onTuningChanged(String key, String newValue) {
        if ("sysui_volume_down_silent".equals(key)) {
            boolean volumeDownToEnterSilent = newValue != null ? Integer.parseInt(newValue) != 0 : false;
            setVolumePolicy(volumeDownToEnterSilent, this.mVolumePolicy.volumeUpToExitSilent, this.mVolumePolicy.doNotDisturbWhenSilent, this.mVolumePolicy.vibrateToSilentDebounce);
        } else if ("sysui_volume_up_silent".equals(key)) {
            boolean volumeUpToExitSilent = newValue != null ? Integer.parseInt(newValue) != 0 : true;
            setVolumePolicy(this.mVolumePolicy.volumeDownToEnterSilent, volumeUpToExitSilent, this.mVolumePolicy.doNotDisturbWhenSilent, this.mVolumePolicy.vibrateToSilentDebounce);
        } else if ("sysui_do_not_disturb".equals(key)) {
            boolean doNotDisturbWhenSilent = newValue != null ? Integer.parseInt(newValue) != 0 : true;
            setVolumePolicy(this.mVolumePolicy.volumeDownToEnterSilent, this.mVolumePolicy.volumeUpToExitSilent, doNotDisturbWhenSilent, this.mVolumePolicy.vibrateToSilentDebounce);
        }
    }

    private void setVolumePolicy(boolean volumeDownToEnterSilent, boolean volumeUpToExitSilent, boolean doNotDisturbWhenSilent, int vibrateToSilentDebounce) {
        this.mVolumePolicy = new VolumePolicy(volumeDownToEnterSilent, volumeUpToExitSilent, doNotDisturbWhenSilent, vibrateToSilentDebounce);
        this.mController.setVolumePolicy(this.mVolumePolicy);
    }

    private void sendUserActivity() {
        KeyguardViewMediator kvm = (KeyguardViewMediator) this.mSysui.getComponent(HwKeyguardViewMediator.class);
        if (kvm != null) {
            kvm.userActivity();
        }
    }

    private void applyConfiguration() {
        this.mDialog.setStreamImportant(4, true);
        this.mDialog.setStreamImportant(1, false);
        this.mDialog.setShowHeaders(true);
        this.mDialog.setAutomute(true);
        this.mDialog.setSilentMode(false);
        this.mController.setVolumePolicy(this.mVolumePolicy);
        this.mController.showDndTile(true);
    }

    public ZenModeController getZenController() {
        return this.mZenModeController;
    }

    public void onConfigurationChanged(Configuration newConfig) {
    }

    public void dismissNow() {
        this.mController.dismiss();
    }

    public void dispatchDemoCommand(String command, Bundle args) {
    }

    public void register() {
        this.mController.register();
        DndTile.setCombinedIcon(this.mContext, true);
    }

    public void dump(FileDescriptor fd, PrintWriter pw, String[] args) {
        this.mController.dump(fd, pw, args);
        this.mDialog.dump(pw);
    }

    private void startSettings(Intent intent) {
        ((PhoneStatusBar) this.mSysui.getComponent(PhoneStatusBar.class)).startActivityDismissingKeyguard(intent, true, true);
    }
}
