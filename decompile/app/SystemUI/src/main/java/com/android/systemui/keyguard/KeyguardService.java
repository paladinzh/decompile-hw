package com.android.systemui.keyguard;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Bundle;
import android.os.Debug;
import android.os.IBinder;
import android.util.Log;
import com.android.internal.policy.IKeyguardDrawnCallback;
import com.android.internal.policy.IKeyguardExitCallback;
import com.android.internal.policy.IKeyguardService.Stub;
import com.android.internal.policy.IKeyguardStateCallback;
import com.huawei.systemui.BaseApplication;

public class KeyguardService extends Service {
    private final Stub mBinder = new Stub() {
        public void addStateMonitorCallback(IKeyguardStateCallback callback) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall addStateMonitorCallback.");
            KeyguardService.this.mKeyguardViewMediator.addStateMonitorCallback(callback);
        }

        public void verifyUnlock(IKeyguardExitCallback callback) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall verifyUnlock.");
            KeyguardService.this.mKeyguardViewMediator.verifyUnlock(callback);
        }

        public void keyguardDone(boolean authenticated, boolean wakeup) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall keyguardDone.");
            KeyguardService.this.mKeyguardViewMediator.keyguardDone(authenticated);
        }

        public void setOccluded(boolean isOccluded) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall setOccluded.");
            KeyguardService.this.mKeyguardViewMediator.setOccluded(isOccluded);
        }

        public void dismiss() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall dismiss.");
            KeyguardService.this.mKeyguardViewMediator.dismiss();
        }

        public void onDreamingStarted() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onDreamingStarted.");
            KeyguardService.this.mKeyguardViewMediator.onDreamingStarted();
        }

        public void onDreamingStopped() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onDreamingStopped.");
            KeyguardService.this.mKeyguardViewMediator.onDreamingStopped();
        }

        public void onStartedGoingToSleep(int reason) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onStartedGoingToSleep.");
            KeyguardService.this.mKeyguardViewMediator.onStartedGoingToSleep(reason);
        }

        public void onFinishedGoingToSleep(int reason, boolean cameraGestureTriggered) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onFinishedGoingToSleep.");
            KeyguardService.this.mKeyguardViewMediator.onFinishedGoingToSleep(reason, cameraGestureTriggered);
        }

        public void onStartedWakingUp() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onStartedWakingUp.");
            KeyguardService.this.mKeyguardViewMediator.onStartedWakingUp();
        }

        public void onScreenTurningOn(IKeyguardDrawnCallback callback) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onScreenTurningOn.");
            KeyguardService.this.mKeyguardViewMediator.onScreenTurningOn(callback);
        }

        public void onScreenTurnedOn() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onScreenTurnedOn.");
            KeyguardService.this.mKeyguardViewMediator.onScreenTurnedOn();
        }

        public void onScreenTurnedOff() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onScreenTurnedOff.");
            KeyguardService.this.mKeyguardViewMediator.onScreenTurnedOff();
        }

        public void setKeyguardEnabled(boolean enabled) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall setKeyguardEnabled.");
            KeyguardService.this.mKeyguardViewMediator.setKeyguardEnabled(enabled);
        }

        public void onSystemReady() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onSystemReady.");
            KeyguardService.this.mKeyguardViewMediator.onSystemReady();
        }

        public void doKeyguardTimeout(Bundle options) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall doKeyguardTimeout.");
            KeyguardService.this.mKeyguardViewMediator.doKeyguardTimeout(options);
        }

        public void setCurrentUser(int userId) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall setCurrentUser.");
            KeyguardService.this.mKeyguardViewMediator.setCurrentUser(userId);
        }

        public void onBootCompleted() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onBootCompleted.");
            KeyguardService.this.mKeyguardViewMediator.onBootCompleted();
        }

        public void startKeyguardExitAnimation(long startTime, long fadeoutDuration) {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall startKeyguardExitAnimation.");
            KeyguardService.this.mKeyguardViewMediator.startKeyguardExitAnimation(startTime, fadeoutDuration);
        }

        public void onActivityDrawn() {
            KeyguardService.this.checkPermission();
            Log.d("KeyguardService", "KGSvcCall onActivityDrawn.");
            KeyguardService.this.mKeyguardViewMediator.onActivityDrawn();
        }
    };
    private KeyguardViewMediator mKeyguardViewMediator;

    public void onCreate() {
        ((BaseApplication) getApplication()).startServicesIfNeeded();
        this.mKeyguardViewMediator = (KeyguardViewMediator) ((BaseApplication) getApplication()).getComponent(HwKeyguardViewMediator.class);
    }

    public void onDestroy() {
        super.onDestroy();
        this.mKeyguardViewMediator.onServiceDestroyed();
    }

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    void checkPermission() {
        if (Binder.getCallingUid() == 1000) {
            Log.d("KeyguardService", "Caller checkPermission fail");
        } else if (getBaseContext().checkCallingOrSelfPermission("android.permission.CONTROL_KEYGUARD") != 0) {
            Log.w("KeyguardService", "Caller needs permission 'android.permission.CONTROL_KEYGUARD' to call " + Debug.getCaller());
            throw new SecurityException("Access denied to process: " + Binder.getCallingPid() + ", must have permission " + "android.permission.CONTROL_KEYGUARD");
        }
    }
}
