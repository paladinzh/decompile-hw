package com.android.systemui.statusbar.phone;

import android.app.ActivityManagerNative;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.content.res.Configuration;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.os.UserHandle;
import android.telecom.TelecomManager;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityNodeInfo.AccessibilityAction;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.keyguard.KeyguardUpdateMonitorCallback;
import com.android.systemui.EventLogTags;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.assist.AssistManager;
import com.android.systemui.statusbar.KeyguardAffordanceView;
import com.android.systemui.statusbar.KeyguardIndicationController;
import com.android.systemui.statusbar.phone.ActivityStarter.Callback;
import com.android.systemui.statusbar.phone.UnlockMethodCache.OnUnlockMethodChangedListener;
import com.android.systemui.statusbar.policy.AccessibilityController;
import com.android.systemui.statusbar.policy.AccessibilityController.AccessibilityStateChangedCallback;
import com.android.systemui.statusbar.policy.FlashlightController;
import com.android.systemui.statusbar.policy.PreviewInflater;

public class KeyguardBottomAreaView extends FrameLayout implements OnClickListener, OnUnlockMethodChangedListener, AccessibilityStateChangedCallback, OnLongClickListener {
    public static final Intent INSECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA");
    private static final Intent PHONE_INTENT = new Intent("android.intent.action.DIAL");
    private static final Intent SECURE_CAMERA_INTENT = new Intent("android.media.action.STILL_IMAGE_CAMERA_SECURE").addFlags(8388608);
    private AccessibilityController mAccessibilityController;
    private AccessibilityDelegate mAccessibilityDelegate;
    private ActivityStarter mActivityStarter;
    private AssistManager mAssistManager;
    private KeyguardAffordanceView mCameraImageView;
    private View mCameraPreview;
    private final BroadcastReceiver mDevicePolicyReceiver;
    private FlashlightController mFlashlightController;
    private KeyguardIndicationController mIndicationController;
    private TextView mIndicationText;
    private KeyguardAffordanceView mLeftAffordanceView;
    private boolean mLeftIsVoiceAssist;
    private View mLeftPreview;
    private LockIcon mLockIcon;
    private LockPatternUtils mLockPatternUtils;
    private PhoneStatusBar mPhoneStatusBar;
    private ViewGroup mPreviewContainer;
    private PreviewInflater mPreviewInflater;
    private boolean mPrewarmBound;
    private final ServiceConnection mPrewarmConnection;
    private Messenger mPrewarmMessenger;
    private UnlockMethodCache mUnlockMethodCache;
    private final KeyguardUpdateMonitorCallback mUpdateMonitorCallback;
    private boolean mUserSetupComplete;

    public KeyguardBottomAreaView(Context context) {
        this(context, null);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public KeyguardBottomAreaView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mPrewarmConnection = new ServiceConnection() {
            public void onServiceConnected(ComponentName name, IBinder service) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = new Messenger(service);
            }

            public void onServiceDisconnected(ComponentName name) {
                KeyguardBottomAreaView.this.mPrewarmMessenger = null;
            }
        };
        this.mAccessibilityDelegate = new AccessibilityDelegate() {
            public void onInitializeAccessibilityNodeInfo(View host, AccessibilityNodeInfo info) {
                super.onInitializeAccessibilityNodeInfo(host, info);
                CharSequence label = null;
                if (host == KeyguardBottomAreaView.this.mLockIcon) {
                    label = KeyguardBottomAreaView.this.getResources().getString(R.string.unlock_label);
                } else if (host == KeyguardBottomAreaView.this.mCameraImageView) {
                    label = KeyguardBottomAreaView.this.getResources().getString(R.string.camera_label);
                } else if (host == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                    if (KeyguardBottomAreaView.this.mLeftIsVoiceAssist) {
                        label = KeyguardBottomAreaView.this.getResources().getString(R.string.voice_assist_label);
                    } else {
                        label = KeyguardBottomAreaView.this.getResources().getString(R.string.phone_label);
                    }
                }
                info.addAction(new AccessibilityAction(16, label));
            }

            public boolean performAccessibilityAction(View host, int action, Bundle args) {
                if (action == 16) {
                    if (host == KeyguardBottomAreaView.this.mLockIcon) {
                        KeyguardBottomAreaView.this.mPhoneStatusBar.animateCollapsePanels(2, true);
                        return true;
                    } else if (host == KeyguardBottomAreaView.this.mCameraImageView) {
                        KeyguardBottomAreaView.this.launchCamera("lockscreen_affordance");
                        return true;
                    } else if (host == KeyguardBottomAreaView.this.mLeftAffordanceView) {
                        KeyguardBottomAreaView.this.launchLeftAffordance();
                        return true;
                    }
                }
                return super.performAccessibilityAction(host, action, args);
            }
        };
        this.mDevicePolicyReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                KeyguardBottomAreaView.this.post(new Runnable() {
                    public void run() {
                        KeyguardBottomAreaView.this.updateCameraVisibility();
                    }
                });
            }
        };
        this.mUpdateMonitorCallback = new KeyguardUpdateMonitorCallback() {
            public void onUserSwitchComplete(int userId) {
                KeyguardBottomAreaView.this.updateCameraVisibility();
            }

            public void onStartedWakingUp() {
                KeyguardBottomAreaView.this.mLockIcon.setDeviceInteractive(true);
            }

            public void onFinishedGoingToSleep(int why) {
                KeyguardBottomAreaView.this.mLockIcon.setDeviceInteractive(false);
            }

            public void onScreenTurnedOn() {
                KeyguardBottomAreaView.this.mLockIcon.setScreenOn(true);
            }

            public void onScreenTurnedOff() {
                KeyguardBottomAreaView.this.mLockIcon.setScreenOn(false);
            }

            public void onKeyguardVisibilityChanged(boolean showing) {
                KeyguardBottomAreaView.this.mLockIcon.update();
            }

            public void onFingerprintRunningStateChanged(boolean running) {
                KeyguardBottomAreaView.this.mLockIcon.update();
            }

            public void onStrongAuthStateChanged(int userId) {
                KeyguardBottomAreaView.this.mLockIcon.update();
            }
        };
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mPreviewContainer = (ViewGroup) findViewById(R.id.preview_container);
        this.mCameraImageView = (KeyguardAffordanceView) findViewById(R.id.camera_button);
        this.mLeftAffordanceView = (KeyguardAffordanceView) findViewById(R.id.left_button);
        this.mLockIcon = (LockIcon) findViewById(R.id.lock_icon);
        this.mIndicationText = (TextView) findViewById(R.id.keyguard_indication_text);
        watchForCameraPolicyChanges();
        updateCameraVisibility();
        this.mUnlockMethodCache = UnlockMethodCache.getInstance(getContext());
        this.mUnlockMethodCache.addListener(this);
        this.mLockIcon.update();
        setClipChildren(false);
        setClipToPadding(false);
        this.mPreviewInflater = new PreviewInflater(this.mContext, new LockPatternUtils(this.mContext));
        inflateCameraPreview();
        this.mLockIcon.setOnClickListener(this);
        this.mLockIcon.setOnLongClickListener(this);
        this.mCameraImageView.setOnClickListener(this);
        this.mLeftAffordanceView.setOnClickListener(this);
        initAccessibility();
    }

    private void initAccessibility() {
        this.mLockIcon.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mLeftAffordanceView.setAccessibilityDelegate(this.mAccessibilityDelegate);
        this.mCameraImageView.setAccessibilityDelegate(this.mAccessibilityDelegate);
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        int indicationBottomMargin = getResources().getDimensionPixelSize(R.dimen.keyguard_indication_margin_bottom);
        MarginLayoutParams mlp = (MarginLayoutParams) this.mIndicationText.getLayoutParams();
        if (mlp.bottomMargin != indicationBottomMargin) {
            mlp.bottomMargin = indicationBottomMargin;
            this.mIndicationText.setLayoutParams(mlp);
        }
        this.mIndicationText.setTextSize(0, (float) getResources().getDimensionPixelSize(17105154));
        LayoutParams lp = this.mCameraImageView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        this.mCameraImageView.setLayoutParams(lp);
        this.mCameraImageView.setImageDrawable(this.mContext.getDrawable(R.drawable.ic_camera_alt_24dp));
        lp = this.mLockIcon.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        this.mLockIcon.setLayoutParams(lp);
        this.mLockIcon.update(true);
        lp = this.mLeftAffordanceView.getLayoutParams();
        lp.width = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_width);
        lp.height = getResources().getDimensionPixelSize(R.dimen.keyguard_affordance_height);
        this.mLeftAffordanceView.setLayoutParams(lp);
        updateLeftAffordanceIcon();
    }

    public void setActivityStarter(ActivityStarter activityStarter) {
        this.mActivityStarter = activityStarter;
    }

    public void setFlashlightController(FlashlightController flashlightController) {
        this.mFlashlightController = flashlightController;
    }

    public void setAccessibilityController(AccessibilityController accessibilityController) {
        this.mAccessibilityController = accessibilityController;
        this.mLockIcon.setAccessibilityController(accessibilityController);
        accessibilityController.addStateChangedCallback(this);
    }

    public void setPhoneStatusBar(PhoneStatusBar phoneStatusBar) {
        this.mPhoneStatusBar = phoneStatusBar;
        updateCameraVisibility();
    }

    public void setUserSetupComplete(boolean userSetupComplete) {
        this.mUserSetupComplete = userSetupComplete;
        updateCameraVisibility();
        updateLeftAffordanceIcon();
    }

    private Intent getCameraIntent() {
        return (!this.mLockPatternUtils.isSecure(KeyguardUpdateMonitor.getCurrentUser()) || KeyguardUpdateMonitor.getInstance(this.mContext).getUserCanSkipBouncer(KeyguardUpdateMonitor.getCurrentUser())) ? INSECURE_CAMERA_INTENT : SECURE_CAMERA_INTENT;
    }

    public ResolveInfo resolveCameraIntent() {
        PackageManager pm = this.mContext.getPackageManager();
        if (pm == null) {
            return null;
        }
        return pm.resolveActivityAsUser(getCameraIntent(), 65536, KeyguardUpdateMonitor.getCurrentUser());
    }

    private void updateCameraVisibility() {
        if (this.mCameraImageView != null) {
            boolean visible;
            int i;
            ResolveInfo resolved = resolveCameraIntent();
            if (isCameraDisabledByDpm() || resolved == null || !getResources().getBoolean(R.bool.config_keyguardShowCameraAffordance)) {
                visible = false;
            } else {
                visible = this.mUserSetupComplete;
            }
            KeyguardAffordanceView keyguardAffordanceView = this.mCameraImageView;
            if (visible) {
                i = 0;
            } else {
                i = 8;
            }
            keyguardAffordanceView.setVisibility(i);
        }
    }

    private void updateLeftAffordanceIcon() {
        int drawableId;
        int contentDescription;
        this.mLeftIsVoiceAssist = canLaunchVoiceAssist();
        boolean visible = this.mUserSetupComplete;
        if (this.mLeftIsVoiceAssist) {
            drawableId = R.drawable.ic_mic_26dp;
            contentDescription = R.string.accessibility_voice_assist_button;
        } else {
            visible &= isPhoneVisible();
            drawableId = R.drawable.ic_phone_24dp;
            contentDescription = R.string.accessibility_phone_button;
        }
        this.mLeftAffordanceView.setVisibility(visible ? 0 : 8);
        this.mLeftAffordanceView.setImageDrawable(this.mContext.getDrawable(drawableId));
        this.mLeftAffordanceView.setContentDescription(this.mContext.getString(contentDescription));
    }

    private boolean isPhoneVisible() {
        PackageManager pm = this.mContext.getPackageManager();
        if (!pm.hasSystemFeature("android.hardware.telephony") || pm.resolveActivity(PHONE_INTENT, 0) == null) {
            return false;
        }
        return true;
    }

    private boolean isCameraDisabledByDpm() {
        DevicePolicyManager dpm = (DevicePolicyManager) getContext().getSystemService("device_policy");
        if (!(dpm == null || this.mPhoneStatusBar == null)) {
            try {
                boolean isKeyguardSecure;
                if ((dpm.getKeyguardDisabledFeatures(null, ActivityManagerNative.getDefault().getCurrentUser().id) & 2) != 0) {
                    isKeyguardSecure = this.mPhoneStatusBar.isKeyguardSecure();
                } else {
                    isKeyguardSecure = false;
                }
                if (dpm.getCameraDisabled(null)) {
                    isKeyguardSecure = true;
                }
                return isKeyguardSecure;
            } catch (RemoteException e) {
                Log.e("PhoneStatusBar/KeyguardBottomAreaView", "Can't get userId", e);
            }
        }
        return false;
    }

    private void watchForCameraPolicyChanges() {
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        getContext().registerReceiverAsUser(this.mDevicePolicyReceiver, UserHandle.ALL, filter, null, null);
        KeyguardUpdateMonitor.getInstance(this.mContext).registerCallback(this.mUpdateMonitorCallback);
    }

    public void onStateChanged(boolean accessibilityEnabled, boolean touchExplorationEnabled) {
        this.mCameraImageView.setClickable(touchExplorationEnabled);
        this.mLeftAffordanceView.setClickable(touchExplorationEnabled);
        this.mCameraImageView.setFocusable(accessibilityEnabled);
        this.mLeftAffordanceView.setFocusable(accessibilityEnabled);
        this.mLockIcon.update();
    }

    public void onClick(View v) {
        if (v == this.mCameraImageView) {
            launchCamera("lockscreen_affordance");
        } else if (v == this.mLeftAffordanceView) {
            launchLeftAffordance();
        }
        if (v != this.mLockIcon) {
            return;
        }
        if (this.mAccessibilityController.isAccessibilityEnabled()) {
            this.mPhoneStatusBar.animateCollapsePanels(0, true);
        } else {
            handleTrustCircleClick();
        }
    }

    public boolean onLongClick(View v) {
        handleTrustCircleClick();
        return true;
    }

    private void handleTrustCircleClick() {
        EventLogTags.writeSysuiLockscreenGesture(6, 0, 0);
        this.mIndicationController.showTransientIndication((int) R.string.keyguard_indication_trust_disabled);
        this.mLockPatternUtils.requireCredentialEntry(KeyguardUpdateMonitor.getCurrentUser());
    }

    public void unbindCameraPrewarmService(boolean launched) {
        if (this.mPrewarmBound) {
            if (this.mPrewarmMessenger != null && launched) {
                try {
                    this.mPrewarmMessenger.send(Message.obtain(null, 1));
                } catch (RemoteException e) {
                    Log.w("PhoneStatusBar/KeyguardBottomAreaView", "Error sending camera fired message", e);
                }
            }
            this.mContext.unbindService(this.mPrewarmConnection);
            this.mPrewarmBound = false;
        }
    }

    public void launchCamera(String source) {
        final Intent intent = getCameraIntent();
        intent.putExtra("com.android.systemui.camera_launch_source", source);
        boolean wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(this.mContext, intent, KeyguardUpdateMonitor.getCurrentUser());
        if (intent != SECURE_CAMERA_INTENT || wouldLaunchResolverActivity) {
            this.mActivityStarter.startActivity(intent, false, new Callback() {
                public void onActivityStarted(int resultCode) {
                    KeyguardBottomAreaView.this.unbindCameraPrewarmService(KeyguardBottomAreaView.isSuccessfulLaunch(resultCode));
                }
            });
        } else {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    int result = -6;
                    try {
                        result = ActivityManagerNative.getDefault().startActivityAsUser(null, KeyguardBottomAreaView.this.getContext().getBasePackageName(), intent, intent.resolveTypeIfNeeded(KeyguardBottomAreaView.this.getContext().getContentResolver()), null, null, 0, 268435456, null, null, UserHandle.CURRENT.getIdentifier());
                    } catch (RemoteException e) {
                        Log.w("PhoneStatusBar/KeyguardBottomAreaView", "Unable to start camera activity", e);
                    }
                    KeyguardBottomAreaView.this.mActivityStarter.preventNextAnimation();
                    final boolean launched = KeyguardBottomAreaView.isSuccessfulLaunch(result);
                    KeyguardBottomAreaView.this.post(new Runnable() {
                        public void run() {
                            KeyguardBottomAreaView.this.unbindCameraPrewarmService(launched);
                        }
                    });
                }
            });
        }
    }

    private static boolean isSuccessfulLaunch(int result) {
        if (result == 0 || result == 3 || result == 2) {
            return true;
        }
        return false;
    }

    public void launchLeftAffordance() {
        if (this.mLeftIsVoiceAssist) {
            launchVoiceAssist();
        } else {
            launchPhone();
        }
    }

    private void launchVoiceAssist() {
        Runnable runnable = new Runnable() {
            public void run() {
                KeyguardBottomAreaView.this.mAssistManager.launchVoiceAssistFromKeyguard();
                KeyguardBottomAreaView.this.mActivityStarter.preventNextAnimation();
            }
        };
        if (this.mPhoneStatusBar.isKeyguardCurrentlySecure()) {
            AsyncTask.execute(runnable);
        } else {
            this.mPhoneStatusBar.executeRunnableDismissingKeyguard(runnable, null, false, false, true);
        }
    }

    private boolean canLaunchVoiceAssist() {
        return this.mAssistManager.canVoiceAssistBeLaunchedFromKeyguard();
    }

    private void launchPhone() {
        final TelecomManager tm = TelecomManager.from(this.mContext);
        if (tm.isInCall()) {
            AsyncTask.execute(new Runnable() {
                public void run() {
                    tm.showInCallScreen(false);
                }
            });
        } else {
            this.mActivityStarter.startActivity(PHONE_INTENT, false);
        }
    }

    protected void onVisibilityChanged(View changedView, int visibility) {
        super.onVisibilityChanged(changedView, visibility);
        if (changedView == this && visibility == 0) {
            this.mLockIcon.update();
            updateCameraVisibility();
        }
    }

    public KeyguardAffordanceView getLeftView() {
        return this.mLeftAffordanceView;
    }

    public KeyguardAffordanceView getRightView() {
        return this.mCameraImageView;
    }

    public LockIcon getLockIcon() {
        return this.mLockIcon;
    }

    public View getIndicationView() {
        return this.mIndicationText;
    }

    public boolean hasOverlappingRendering() {
        return false;
    }

    public void onUnlockMethodStateChanged() {
        this.mLockIcon.update();
        updateCameraVisibility();
    }

    private void inflateCameraPreview() {
        this.mCameraPreview = this.mPreviewInflater.inflatePreview(getCameraIntent());
        if (this.mCameraPreview != null) {
            this.mPreviewContainer.addView(this.mCameraPreview);
            this.mCameraPreview.setVisibility(4);
        }
    }

    private void updateLeftPreview() {
        View previewBefore = this.mLeftPreview;
        if (previewBefore != null) {
            this.mPreviewContainer.removeView(previewBefore);
        }
        if (this.mLeftIsVoiceAssist) {
            this.mLeftPreview = this.mPreviewInflater.inflatePreviewFromService(this.mAssistManager.getVoiceInteractorComponentName());
        } else {
            this.mLeftPreview = this.mPreviewInflater.inflatePreview(PHONE_INTENT);
        }
        if (this.mLeftPreview != null) {
            this.mPreviewContainer.addView(this.mLeftPreview);
            this.mLeftPreview.setVisibility(4);
        }
    }

    public void startFinishDozeAnimation() {
        long delay = 0;
        if (this.mLeftAffordanceView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mLeftAffordanceView, 0);
            delay = 48;
        }
        startFinishDozeAnimationElement(this.mLockIcon, delay);
        delay += 48;
        if (this.mCameraImageView.getVisibility() == 0) {
            startFinishDozeAnimationElement(this.mCameraImageView, delay);
        }
        this.mIndicationText.setAlpha(0.0f);
        this.mIndicationText.animate().alpha(1.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setDuration(700);
    }

    private void startFinishDozeAnimationElement(View element, long delay) {
        element.setAlpha(0.0f);
        element.setTranslationY((float) (element.getHeight() / 2));
        element.animate().alpha(1.0f).translationY(0.0f).setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN).setStartDelay(delay).setDuration(250);
    }

    public void setKeyguardIndicationController(KeyguardIndicationController keyguardIndicationController) {
        this.mIndicationController = keyguardIndicationController;
    }

    public void setAssistManager(AssistManager assistManager) {
        this.mAssistManager = assistManager;
        updateLeftAffordance();
    }

    public void updateLeftAffordance() {
        updateLeftAffordanceIcon();
        updateLeftPreview();
    }
}
