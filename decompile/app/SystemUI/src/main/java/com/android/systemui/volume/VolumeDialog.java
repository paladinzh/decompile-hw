package com.android.systemui.volume;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.animation.LayoutTransition;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.KeyguardManager;
import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.media.AudioManager;
import android.media.AudioSystem;
import android.os.Debug;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.util.SparseBooleanArray;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.AccessibilityDelegate;
import android.view.View.OnAttachStateChangeListener;
import android.view.View.OnClickListener;
import android.view.View.OnHoverListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityManager;
import android.view.accessibility.AccessibilityManager.AccessibilityStateChangeListener;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import com.android.systemui.R;
import com.android.systemui.compat.ActivityInfoWrapper;
import com.android.systemui.statusbar.policy.ZenModeController;
import com.android.systemui.tuner.TunerService;
import com.android.systemui.tuner.TunerService.Tunable;
import com.android.systemui.tuner.TunerZenModePanel;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.UserSwitchUtils.UserSwitchedListener;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.volume.HwVolumeSilentView.HwSilentViewCallback;
import com.android.systemui.volume.VolumeDialogController.Callbacks;
import com.android.systemui.volume.VolumeDialogController.State;
import com.android.systemui.volume.VolumeDialogController.StreamState;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public abstract class VolumeDialog implements Tunable, HwSilentViewCallback, UserSwitchedListener {
    private static final String TAG = Util.logTag(VolumeDialog.class);
    private boolean isUserChanged = false;
    private final Accessibility mAccessibility = new Accessibility();
    private final AccessibilityManager mAccessibilityMgr;
    private final ColorStateList mActiveSliderTint;
    private int mActiveStream;
    protected final AudioManager mAudioManager;
    private boolean mAutomute = true;
    private Callback mCallback;
    private final OnClickListener mClickExpand = new OnClickListener() {
        public void onClick(View v) {
            Events.writeEvent(VolumeDialog.this.mContext, 3, Boolean.valueOf(!VolumeDialog.this.mExpanded));
            VolumeDialog.this.setExpandedH(newExpand);
            BDReporter.c(VolumeDialog.this.mContext, 44);
        }
    };
    private long mCollapseTime;
    private final Context mContext;
    private final VolumeDialogController mController;
    private final Callbacks mControllerCallbackH = new Callbacks() {
        public void onShowRequested(int reason) {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onShowRequested renson = " + reason);
            VolumeDialog.this.showH(reason);
        }

        public void onDismissRequested(int reason) {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onDismissRequested reason = " + reason);
            VolumeDialog.this.dismissH(reason);
        }

        public void onScreenOff() {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onScreenOff");
            VolumeDialog.this.dismissH(4);
        }

        public void onStateChanged(State state) {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onStateChanged");
            VolumeDialog.this.onStateChangedH(state);
        }

        public void onLayoutDirectionChanged(int layoutDirection) {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onLayoutDirectionChanged layoutDirection = " + layoutDirection);
            VolumeDialog.this.mDialogView.setLayoutDirection(layoutDirection);
        }

        public void onConfigurationChanged() {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onConfigurationChanged");
            Configuration newConfig = VolumeDialog.this.mContext.getResources().getConfiguration();
            int density = newConfig.densityDpi;
            int configChanges = VolumeDialog.this.mLastConfig.updateFrom(newConfig);
            if (density != VolumeDialog.this.mDensity || ActivityInfoWrapper.isThemeChanged(configChanges)) {
                HwLog.i(VolumeDialog.TAG, "onConfigurationChanged:theme changed or density changed");
                VolumeDialog.this.mController.notifyVisible(false);
                VolumeDialog.this.mDialog.dismiss();
                VolumeDialog.this.mZenFooter.cleanup();
                VolumeDialog.this.initDialog();
            }
            VolumeDialog.this.updateWindowWidthH();
            VolumeDialog.this.mSpTexts.update();
            VolumeDialog.this.mZenFooter.onConfigurationChanged();
        }

        public void onShowVibrateHint() {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onShowVibrateHint");
            if (VolumeDialog.this.mSilentMode) {
                VolumeDialog.this.mController.setRingerMode(0, false);
            }
        }

        public void onShowSilentHint() {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onShowSilentHint");
            if (VolumeDialog.this.mSilentMode) {
                VolumeDialog.this.mController.setRingerMode(2, false);
            }
        }

        public void onShowSafetyWarning(int flags) {
            HwLog.i(VolumeDialog.TAG, "VolumeDialogController.Callbacks onShowSafetyWarning flags = " + flags);
            VolumeDialog.this.showSafetyWarningH(flags);
        }
    };
    private int mDensity;
    protected CustomDialog mDialog;
    private ViewGroup mDialogContentView;
    private ViewGroup mDialogView;
    private final SparseBooleanArray mDynamic = new SparseBooleanArray();
    protected ImageView mExpandButton;
    private int mExpandButtonAnimationDuration;
    private boolean mExpandButtonAnimationRunning;
    protected boolean mExpanded;
    protected final H mHandler = new H();
    private boolean mHovering = false;
    private final ColorStateList mInactiveSliderTint;
    private final KeyguardManager mKeyguard;
    private Configuration mLastConfig = new Configuration();
    private LayoutTransition mLayoutTransition;
    private VolumeDialogMotion mMotion;
    private boolean mPendingRecheckAll;
    private boolean mPendingStateChanged;
    private final List<VolumeRow> mRows = new ArrayList();
    private SafetyWarningDialog mSafetyWarning;
    private final Object mSafetyWarningLock = new Object();
    private boolean mShowFullZen;
    private boolean mShowHeaders = true;
    private boolean mShowing;
    private boolean mSilentMode = true;
    private SpTexts mSpTexts;
    private State mState;
    private final int mWindowType;
    private ZenFooter mZenFooter;
    private final ZenModeController mZenModeController;
    private TunerZenModePanel mZenPanel;
    private final com.android.systemui.volume.ZenModePanel.Callback mZenPanelCallback = new com.android.systemui.volume.ZenModePanel.Callback() {
        public void onPrioritySettings() {
            VolumeDialog.this.mCallback.onZenPrioritySettingsClicked();
        }

        public void onInteraction() {
            VolumeDialog.this.mHandler.sendEmptyMessage(6);
        }

        public void onExpanded(boolean expanded) {
        }
    };

    private final class Accessibility extends AccessibilityDelegate {
        private boolean mFeedbackEnabled;

        private Accessibility() {
        }

        public void init() {
            VolumeDialog.this.mDialogView.addOnAttachStateChangeListener(new OnAttachStateChangeListener() {
                public void onViewDetachedFromWindow(View v) {
                    if (D.BUG) {
                        Log.d(VolumeDialog.TAG, "onViewDetachedFromWindow");
                    }
                }

                public void onViewAttachedToWindow(View v) {
                    if (D.BUG) {
                        Log.d(VolumeDialog.TAG, "onViewAttachedToWindow");
                    }
                    Accessibility.this.updateFeedbackEnabled();
                }
            });
            VolumeDialog.this.mDialogView.setAccessibilityDelegate(this);
            VolumeDialog.this.mAccessibilityMgr.addAccessibilityStateChangeListener(new AccessibilityStateChangeListener() {
                public void onAccessibilityStateChanged(boolean enabled) {
                    Accessibility.this.updateFeedbackEnabled();
                }
            });
            updateFeedbackEnabled();
        }

        public boolean onRequestSendAccessibilityEvent(ViewGroup host, View child, AccessibilityEvent event) {
            VolumeDialog.this.rescheduleTimeoutH();
            return super.onRequestSendAccessibilityEvent(host, child, event);
        }

        private void updateFeedbackEnabled() {
            this.mFeedbackEnabled = computeFeedbackEnabled();
        }

        private boolean computeFeedbackEnabled() {
            for (AccessibilityServiceInfo asi : VolumeDialog.this.mAccessibilityMgr.getEnabledAccessibilityServiceList(-1)) {
                if (asi.feedbackType != 0 && asi.feedbackType != 16) {
                    return true;
                }
            }
            return false;
        }
    }

    public interface Callback {
        void onZenPrioritySettingsClicked();
    }

    protected class CustomDialog extends Dialog {
        public CustomDialog(Context context) {
            super(context, context.getResources().getIdentifier("androidhwext:style/SystemUI.VolumeDialog", null, null));
        }

        public boolean dispatchTouchEvent(MotionEvent ev) {
            VolumeDialog.this.rescheduleTimeoutH();
            return super.dispatchTouchEvent(ev);
        }

        protected void onStop() {
            super.onStop();
            boolean animating = VolumeDialog.this.mMotion.isAnimating();
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStop animating=" + animating);
            }
            if (animating) {
                VolumeDialog.this.mPendingRecheckAll = true;
            } else {
                VolumeDialog.this.mHandler.sendEmptyMessage(4);
            }
        }

        public boolean onTouchEvent(MotionEvent event) {
            if (!isShowing() || event.getAction() != 4) {
                return false;
            }
            VolumeDialog.this.dismissH(1);
            return true;
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            event.setClassName(getClass().getSuperclass().getName());
            event.setPackageName(VolumeDialog.this.mContext.getPackageName());
            LayoutParams params = getWindow().getAttributes();
            boolean isFullScreen = params.width == -1 ? params.height == -1 : false;
            event.setFullScreen(isFullScreen);
            if (event.getEventType() != 32 || !VolumeDialog.this.mShowing) {
                return false;
            }
            event.getText().add(VolumeDialog.this.mContext.getString(R.string.volume_dialog_accessibility_shown_message, new Object[]{VolumeDialog.this.getActiveRow().ss.name}));
            return true;
        }
    }

    protected final class H extends Handler {
        public H() {
            super(Looper.getMainLooper());
        }

        public void handleMessage(Message msg) {
            boolean z = false;
            switch (msg.what) {
                case 1:
                    VolumeDialog.this.showH(msg.arg1);
                    return;
                case 2:
                    VolumeDialog.this.dismissH(msg.arg1);
                    return;
                case 3:
                    VolumeDialog.this.recheckH((VolumeRow) msg.obj);
                    return;
                case 4:
                    VolumeDialog.this.recheckH(null);
                    return;
                case 5:
                    VolumeDialog volumeDialog = VolumeDialog.this;
                    int i = msg.arg1;
                    if (msg.arg2 != 0) {
                        z = true;
                    }
                    volumeDialog.setStreamImportantH(i, z);
                    return;
                case 6:
                    VolumeDialog.this.rescheduleTimeoutH();
                    return;
                case 7:
                    VolumeDialog.this.onStateChangedH(VolumeDialog.this.mState);
                    return;
                case 8:
                    VolumeDialog.this.updateDialogBottomMarginH();
                    return;
                case 9:
                    VolumeDialog.this.updateFooterH();
                    return;
                default:
                    return;
            }
        }
    }

    private static class VolumeRow {
        private int cachedIconRes;
        private boolean cachedShowHeaders;
        private TextView header;
        private ImageView icon;
        private int iconMuteRes;
        private int iconRes;
        private int iconState;
        private boolean important;
        private int lastAudibleLevel;
        private int requestedLevel;
        private SeekBar slider;
        private StreamState ss;
        private int stream;
        private boolean tracking;
        private long userAttempt;
        private View view;

        private VolumeRow() {
            this.requestedLevel = -1;
            this.cachedShowHeaders = false;
            this.lastAudibleLevel = 1;
        }
    }

    private final class VolumeSeekBarChangeListener implements OnSeekBarChangeListener {
        private final VolumeRow mRow;

        private VolumeSeekBarChangeListener(VolumeRow row) {
            this.mRow = row;
        }

        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (this.mRow.ss != null) {
                if (D.BUG) {
                    Log.d(VolumeDialog.TAG, AudioSystem.streamToString(this.mRow.stream) + " onProgressChanged " + progress + " fromUser=" + fromUser);
                }
                if (fromUser) {
                    if (this.mRow.ss.levelMin > 0) {
                        int minProgress = this.mRow.ss.levelMin * 100;
                        if (progress < minProgress) {
                            seekBar.setProgress(minProgress);
                            progress = minProgress;
                        }
                    }
                    int userLevel = VolumeDialog.getImpliedLevel(seekBar, progress);
                    if (this.mRow.ss.level != userLevel || (this.mRow.ss.muted && userLevel > 0)) {
                        this.mRow.userAttempt = SystemClock.uptimeMillis();
                        if (this.mRow.requestedLevel != userLevel) {
                            VolumeDialog.this.mController.setStreamVolume(this.mRow.stream, userLevel);
                            this.mRow.requestedLevel = userLevel;
                            Events.writeEvent(VolumeDialog.this.mContext, 9, Integer.valueOf(this.mRow.stream), Integer.valueOf(userLevel));
                        }
                    }
                }
            }
        }

        public void onStartTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStartTrackingTouch " + this.mRow.stream);
            }
            VolumeDialog.this.mController.setActiveStream(this.mRow.stream);
            this.mRow.tracking = true;
        }

        public void onStopTrackingTouch(SeekBar seekBar) {
            if (D.BUG) {
                Log.d(VolumeDialog.TAG, "onStopTrackingTouch " + this.mRow.stream);
            }
            this.mRow.tracking = false;
            this.mRow.userAttempt = SystemClock.uptimeMillis();
            int userLevel = VolumeDialog.getImpliedLevel(seekBar, seekBar.getProgress());
            Events.writeEvent(VolumeDialog.this.mContext, 16, Integer.valueOf(this.mRow.stream), Integer.valueOf(userLevel));
            if (this.mRow.ss.level != userLevel) {
                VolumeDialog.this.mHandler.sendMessageDelayed(VolumeDialog.this.mHandler.obtainMessage(3, this.mRow), 1000);
            }
            BDReporter.e(VolumeDialog.this.mContext, 45, "type:" + this.mRow.stream + ",progress:" + userLevel);
        }
    }

    public VolumeDialog(Context context, int windowType, VolumeDialogController controller, ZenModeController zenModeController, Callback callback) {
        this.mContext = context;
        this.mController = controller;
        this.mCallback = callback;
        this.mWindowType = windowType;
        this.mZenModeController = zenModeController;
        this.mKeyguard = (KeyguardManager) context.getSystemService("keyguard");
        this.mAudioManager = (AudioManager) context.getSystemService("audio");
        this.mAccessibilityMgr = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mActiveSliderTint = loadColorStateList(R.color.system_accent_color);
        this.mInactiveSliderTint = loadColorStateList(R.color.volume_slider_inactive);
        initDialog();
        this.mAccessibility.init();
        UserSwitchUtils.addListener(this);
        controller.addCallback(this.mControllerCallbackH, this.mHandler);
        controller.getState();
        TunerService.get(this.mContext).addTunable((Tunable) this, "sysui_show_full_zen");
        this.mDensity = this.mContext.getResources().getConfiguration().densityDpi;
    }

    private void initDialog() {
        this.mDialog = new CustomDialog(this.mContext);
        this.mSpTexts = new SpTexts(this.mContext);
        this.mLayoutTransition = new LayoutTransition();
        this.mLayoutTransition.setDuration(new ValueAnimator().getDuration() / 2);
        this.mHovering = false;
        this.mShowing = false;
        Window window = this.mDialog.getWindow();
        window.requestFeature(1);
        window.setBackgroundDrawable(new ColorDrawable(0));
        window.clearFlags(2);
        window.addFlags(17563944);
        this.mDialog.setCanceledOnTouchOutside(true);
        Resources res = this.mContext.getResources();
        WindowManager.LayoutParams lp = window.getAttributes();
        lp.type = this.mWindowType;
        lp.format = -3;
        lp.setTitle(VolumeDialog.class.getSimpleName());
        lp.gravity = 49;
        lp.y = res.getDimensionPixelSize(R.dimen.volume_offset_top);
        if (window.getDecorView() != null) {
            window.getDecorView().setLayoutParams(new LayoutParams(-2, -2));
        }
        lp.windowAnimations = -1;
        window.setAttributes(lp);
        window.setSoftInputMode(48);
        this.mDialog.setContentView(R.layout.volume_dialog);
        lp.blurRoundx = 45;
        lp.blurRoundy = 45;
        lp.blurBlankLeft = 24;
        lp.blurBlankTop = 48;
        lp.blurBlankRight = 24;
        lp.blurBlankBottom = 48;
        window.setAttributes(lp);
        this.mDialogView = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog);
        this.mDialogView.setOnHoverListener(new OnHoverListener() {
            public boolean onHover(View v, MotionEvent event) {
                int action = event.getActionMasked();
                VolumeDialog volumeDialog = VolumeDialog.this;
                boolean z = action != 9 ? action == 7 : true;
                volumeDialog.mHovering = z;
                VolumeDialog.this.rescheduleTimeoutH();
                return true;
            }
        });
        this.mDialogContentView = (ViewGroup) this.mDialog.findViewById(R.id.volume_dialog_content);
        initView();
        this.mExpanded = false;
        this.mExpandButton = (ImageView) this.mDialogView.findViewById(R.id.volume_expand_button);
        this.mExpandButton.setOnClickListener(this.mClickExpand);
        updateWindowWidthH();
        updateExpandButtonH();
        this.mMotion = new VolumeDialogMotion(this.mDialog, this.mDialogView, this.mDialogContentView, this.mExpandButton, new com.android.systemui.volume.VolumeDialogMotion.Callback() {
            public void onAnimatingChanged(boolean animating) {
                if (!animating) {
                    if (VolumeDialog.this.mPendingStateChanged) {
                        VolumeDialog.this.mHandler.sendEmptyMessage(7);
                        VolumeDialog.this.mPendingStateChanged = false;
                    }
                    if (VolumeDialog.this.mPendingRecheckAll) {
                        VolumeDialog.this.mHandler.sendEmptyMessage(4);
                        VolumeDialog.this.mPendingRecheckAll = false;
                    }
                }
            }
        });
        if (this.mRows.isEmpty()) {
            addRow(2, R.drawable.ic_volume_ringer, R.drawable.ic_volume_ringer_mute, true);
            addRow(3, R.drawable.ic_volume_media, R.drawable.ic_volume_media_mute, true);
            addRow(4, R.drawable.ic_volume_alarm, R.drawable.ic_volume_alarm_mute, true);
            addRow(0, R.drawable.ic_volume_voice, R.drawable.ic_volume_voice, true);
            addRow(6, R.drawable.ic_settings_bluetooth_call, R.drawable.ic_settings_bluetooth_call, false);
            addRow(1, R.drawable.ic_volume_system, R.drawable.ic_volume_system_mute, false);
        } else {
            addExistingRows();
        }
        this.mExpandButtonAnimationDuration = res.getInteger(R.integer.volume_expand_animation_duration);
        this.mZenFooter = (ZenFooter) this.mDialog.findViewById(R.id.volume_zen_footer);
        this.mZenFooter.init(this.mZenModeController);
        this.mZenPanel = (TunerZenModePanel) this.mDialog.findViewById(R.id.tuner_zen_mode_panel);
        this.mZenPanel.init(this.mZenModeController);
        this.mZenPanel.setCallback(this.mZenPanelCallback);
    }

    public void onTuningChanged(String key, String newValue) {
        boolean z = false;
        if ("sysui_show_full_zen".equals(key)) {
            if (!(newValue == null || Integer.parseInt(newValue) == 0)) {
                z = true;
            }
            this.mShowFullZen = z;
        }
    }

    private ColorStateList loadColorStateList(int colorResId) {
        return ColorStateList.valueOf(this.mContext.getColor(colorResId));
    }

    private void updateWindowWidthH() {
        LayoutParams lp = this.mDialogView.getLayoutParams();
        DisplayMetrics dm = this.mContext.getResources().getDisplayMetrics();
        if (D.BUG) {
            Log.d(TAG, "updateWindowWidth dm.w=" + dm.widthPixels);
        }
        int w = dm.widthPixels;
        int max = this.mContext.getResources().getDimensionPixelSize(R.dimen.volume_dialog_panel_width);
        if (w > max) {
            w = max;
        }
        lp.width = w;
        Window window = this.mDialog.getWindow();
        WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.width = w;
        window.setAttributes(wlp);
        this.mDialogView.setLayoutParams(lp);
    }

    public void setStreamImportant(int stream, boolean important) {
        this.mHandler.obtainMessage(5, stream, important ? 1 : 0).sendToTarget();
    }

    public void setShowHeaders(boolean showHeaders) {
        if (showHeaders != this.mShowHeaders) {
            this.mShowHeaders = showHeaders;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    public void setAutomute(boolean automute) {
        if (this.mAutomute != automute) {
            this.mAutomute = automute;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    public void setSilentMode(boolean silentMode) {
        if (this.mSilentMode != silentMode) {
            this.mSilentMode = silentMode;
            this.mHandler.sendEmptyMessage(4);
        }
    }

    private void addRow(int stream, int iconRes, int iconMuteRes, boolean important) {
        VolumeRow row = new VolumeRow();
        initRow(row, stream, iconRes, iconMuteRes, important);
        this.mDialogContentView.addView(row.view, this.mDialogContentView.getChildCount() - 2);
        this.mRows.add(row);
    }

    private void addExistingRows() {
        int N = this.mRows.size();
        for (int i = 0; i < N; i++) {
            VolumeRow row = (VolumeRow) this.mRows.get(i);
            initRow(row, row.stream, row.iconRes, row.iconMuteRes, row.important);
            this.mDialogContentView.addView(row.view, this.mDialogContentView.getChildCount() - 2);
        }
    }

    private VolumeRow getActiveRow() {
        for (VolumeRow row : this.mRows) {
            if (row.stream == this.mActiveStream) {
                return row;
            }
        }
        return (VolumeRow) this.mRows.get(0);
    }

    private VolumeRow findRow(int stream) {
        for (VolumeRow row : this.mRows) {
            if (row.stream == stream) {
                return row;
            }
        }
        return null;
    }

    public void dump(PrintWriter writer) {
        writer.println(VolumeDialog.class.getSimpleName() + " state:");
        writer.print("  mShowing: ");
        writer.println(this.mShowing);
        writer.print("  mExpanded: ");
        writer.println(this.mExpanded);
        writer.print("  mExpandButtonAnimationRunning: ");
        writer.println(this.mExpandButtonAnimationRunning);
        writer.print("  mActiveStream: ");
        writer.println(this.mActiveStream);
        writer.print("  mDynamic: ");
        writer.println(this.mDynamic);
        writer.print("  mShowHeaders: ");
        writer.println(this.mShowHeaders);
        writer.print("  mAutomute: ");
        writer.println(this.mAutomute);
        writer.print("  mSilentMode: ");
        writer.println(this.mSilentMode);
        writer.print("  mCollapseTime: ");
        writer.println(this.mCollapseTime);
        writer.print("  mAccessibility.mFeedbackEnabled: ");
        writer.println(this.mAccessibility.mFeedbackEnabled);
    }

    private static int getImpliedLevel(SeekBar seekBar, int progress) {
        int m = seekBar.getMax();
        int n = (m / 100) - 1;
        if (progress == 0) {
            return 0;
        }
        return progress == m ? m / 100 : ((int) ((((float) progress) / ((float) m)) * ((float) n))) + 1;
    }

    @SuppressLint({"InflateParams"})
    private void initRow(final VolumeRow row, final int stream, int iconRes, int iconMuteRes, boolean important) {
        row.stream = stream;
        row.iconRes = iconRes;
        row.iconMuteRes = iconMuteRes;
        row.important = important;
        row.view = this.mDialog.getLayoutInflater().inflate(R.layout.volume_dialog_row, null);
        row.view.setTag(row);
        row.header = (TextView) row.view.findViewById(R.id.volume_row_header);
        this.mSpTexts.add(row.header);
        row.slider = (SeekBar) row.view.findViewById(R.id.volume_row_slider);
        row.slider.setPadding(0, row.slider.getPaddingTop(), 0, row.slider.getPaddingBottom());
        row.slider.setOnSeekBarChangeListener(new VolumeSeekBarChangeListener(row));
        row.icon = (ImageView) row.view.findViewById(R.id.volume_row_icon);
        row.icon.setImageResource(iconRes);
        row.icon.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                int i = 0;
                Events.writeEvent(VolumeDialog.this.mContext, 7, Integer.valueOf(row.stream), Integer.valueOf(row.iconState));
                VolumeDialog.this.mController.setActiveStream(row.stream);
                int ringMode = -1;
                if (row.stream == 2) {
                    boolean hasVibrator = VolumeDialog.this.mController.hasVibrator();
                    if (VolumeDialog.this.mState.ringerModeInternal != 2) {
                        VolumeDialog.this.mController.setRingerMode(2, false);
                        if (row.ss.level == 0) {
                            VolumeDialog.this.mController.setStreamVolume(stream, 1);
                        } else {
                            ringMode = 2;
                        }
                    } else if (hasVibrator) {
                        VolumeDialog.this.mController.setRingerMode(1, false);
                        ringMode = 1;
                    } else {
                        boolean wasZero = row.ss.level == 0;
                        VolumeDialogController -get4 = VolumeDialog.this.mController;
                        int i2 = stream;
                        if (wasZero) {
                            i = row.lastAudibleLevel;
                        }
                        -get4.setStreamVolume(i2, i);
                    }
                } else {
                    VolumeDialog.this.mController.setStreamVolume(stream, row.ss.level == row.ss.levelMin ? row.lastAudibleLevel : row.ss.levelMin);
                }
                row.userAttempt = 0;
                if (ringMode > -1) {
                    VolumeDialog.this.updateStreamVolume(row.stream, ringMode);
                }
            }
        });
    }

    private void showH(int reason) {
        if (D.BUG) {
            Log.d(TAG, "showH r=" + Events.DISMISS_REASONS[reason]);
        }
        this.mHandler.removeMessages(1);
        this.mHandler.removeMessages(2);
        rescheduleTimeoutH();
        if (!this.mShowing) {
            this.mShowing = true;
            this.mMotion.startShow();
            Events.writeEvent(this.mContext, 0, Integer.valueOf(reason), Boolean.valueOf(this.mKeyguard.isKeyguardLocked()));
            this.mController.notifyVisible(true);
        }
    }

    protected void rescheduleTimeoutH() {
        this.mHandler.removeMessages(2);
        int timeout = computeTimeoutH();
        this.mHandler.sendMessageDelayed(this.mHandler.obtainMessage(2, 3, 0), (long) timeout);
        if (D.BUG) {
            Log.d(TAG, "rescheduleTimeout " + timeout + " " + Debug.getCaller());
        }
        this.mController.userActivity();
    }

    private int computeTimeoutH() {
        if (this.mAccessibility.mFeedbackEnabled) {
            return 20000;
        }
        if (this.mHovering) {
            return 16000;
        }
        if (this.mSafetyWarning != null || this.mExpanded || this.mExpandButtonAnimationRunning) {
            return 5000;
        }
        if (this.mActiveStream == 3) {
            return 1500;
        }
        return 3000;
    }

    protected void dismissH(int reason) {
        if (!this.mMotion.isAnimating()) {
            this.mHandler.removeMessages(2);
            this.mHandler.removeMessages(1);
            if (this.mShowing) {
                this.mShowing = false;
                this.mMotion.startDismiss(new Runnable() {
                    public void run() {
                        VolumeDialog.this.setExpandedH(false);
                    }
                });
                if (this.mAccessibilityMgr.isEnabled()) {
                    AccessibilityEvent event = AccessibilityEvent.obtain(32);
                    event.setPackageName(this.mContext.getPackageName());
                    event.setClassName(CustomDialog.class.getSuperclass().getName());
                    event.getText().add(this.mContext.getString(R.string.volume_dialog_accessibility_dismissed_message));
                    this.mAccessibilityMgr.sendAccessibilityEvent(event);
                }
                Events.writeEvent(this.mContext, 1, Integer.valueOf(reason));
                this.mController.notifyVisible(false);
                synchronized (this.mSafetyWarningLock) {
                    if (this.mSafetyWarning != null) {
                        if (D.BUG) {
                            Log.d(TAG, "SafetyWarning dismissed");
                        }
                        this.mSafetyWarning.dismiss();
                    }
                }
            }
        }
    }

    private void updateDialogBottomMarginH() {
        int bottomMargin;
        boolean collapsing = this.mCollapseTime != 0 && System.currentTimeMillis() - this.mCollapseTime < getConservativeCollapseDuration();
        MarginLayoutParams mlp = (MarginLayoutParams) this.mDialogView.getLayoutParams();
        if (collapsing) {
            bottomMargin = this.mDialogContentView.getHeight();
        } else {
            bottomMargin = this.mContext.getResources().getDimensionPixelSize(R.dimen.volume_dialog_margin_bottom);
        }
        if (bottomMargin != mlp.bottomMargin) {
            if (D.BUG) {
                Log.d(TAG, "bottomMargin " + mlp.bottomMargin + " -> " + bottomMargin);
            }
            mlp.bottomMargin = bottomMargin;
            this.mDialogView.setLayoutParams(mlp);
        }
    }

    private long getConservativeCollapseDuration() {
        return (long) (this.mExpandButtonAnimationDuration * 3);
    }

    private void prepareForCollapse() {
    }

    private void setExpandedH(boolean expanded) {
        if (this.mExpanded != expanded) {
            this.mExpanded = expanded;
            if (this.mExpanded) {
                updateVisibility(false);
            } else {
                VolumeRow row = getActiveRow();
                if (row != null) {
                    updateStreamVolume(row.stream, -1);
                }
            }
            if (D.BUG) {
                Log.d(TAG, "setExpandedH " + expanded);
            }
            if (!this.mExpanded) {
                prepareForCollapse();
            }
            updateRowsH();
            rescheduleTimeoutH();
        }
    }

    private void updateExpandButtonH() {
        if (D.BUG) {
            Log.d(TAG, "updateExpandButtonH");
        }
        int res = this.mExpanded ? R.drawable.volume_arrow_up : R.drawable.volume_arrow_down;
        if (hasTouchFeature()) {
            this.mExpandButton.setImageResource(res);
        } else {
            this.mExpandButton.setImageResource(R.drawable.ic_volume_ringer);
            this.mExpandButton.setBackgroundResource(0);
        }
        this.mExpandButton.setContentDescription(this.mContext.getString(this.mExpanded ? R.string.accessibility_volume_collapse : R.string.accessibility_volume_expand));
    }

    private boolean isVisibleH(VolumeRow row, boolean isActive) {
        if ((this.mExpanded && row.view.getVisibility() == 0) || (this.mExpanded && (row.important || isActive))) {
            return true;
        }
        if (this.mExpanded) {
            return false;
        }
        return isActive;
    }

    private void updateRowsH() {
        if (D.BUG) {
            Log.d(TAG, "updateRowsH");
        }
        VolumeRow activeRow = getActiveRow();
        updateFooterH();
        updateExpandButtonH();
        if (!this.mShowing) {
            trimObsoleteH();
        }
        for (VolumeRow row : this.mRows) {
            boolean isActive = row == activeRow;
            boolean visible = isVisibleH(row, isActive);
            Util.setVisOrGone(row.view, visible);
            updateVolumeRowHeaderVisibleH(row);
            updateVolumeRowSliderTintH(row, isActive);
            if (isActive && visible) {
                updateStreamVolume(row.stream, -1);
            }
        }
        updateExpandButton();
    }

    private void trimObsoleteH() {
        if (D.BUG) {
            Log.d(TAG, "trimObsoleteH");
        }
        for (int i = this.mRows.size() - 1; i >= 0; i--) {
            VolumeRow row = (VolumeRow) this.mRows.get(i);
            if (!(row.ss == null || !row.ss.dynamic || this.mDynamic.get(row.stream))) {
                this.mRows.remove(i);
                this.mDialogContentView.removeView(row.view);
            }
        }
    }

    private void onStateChangedH(State state) {
        boolean animating = this.mMotion.isAnimating();
        if (D.BUG) {
            Log.d(TAG, "onStateChangedH animating=" + animating);
        }
        this.mState = state;
        if (animating) {
            this.mPendingStateChanged = true;
            return;
        }
        this.mDynamic.clear();
        for (int i = 0; i < state.states.size(); i++) {
            int stream = state.states.keyAt(i);
            if (((StreamState) state.states.valueAt(i)).dynamic) {
                this.mDynamic.put(stream, true);
                if (findRow(stream) == null) {
                    addRow(stream, R.drawable.ic_volume_remote, R.drawable.ic_volume_remote_mute, true);
                }
            }
        }
        if (this.mActiveStream != state.activeStream) {
            this.mActiveStream = state.activeStream;
            updateRowsH();
            rescheduleTimeoutH();
        }
        for (VolumeRow row : this.mRows) {
            updateVolumeRowH(row);
        }
        updateFooterH();
    }

    public void onUserChanged() {
        HwLog.i(TAG, "onUserChanged::user has changed!");
        this.isUserChanged = true;
    }

    private void updateFooterH() {
        boolean fullVisible = false;
        if (D.BUG) {
            Log.d(TAG, "updateFooterH");
        }
        boolean wasVisible = this.mZenFooter.getVisibility() == 0;
        boolean visible = (this.mState.zenMode == 0 || !(this.mAudioManager.isStreamAffectedByRingerMode(this.mActiveStream) || this.mExpanded)) ? false : !this.mZenPanel.isEditing();
        if (!(wasVisible == visible || visible)) {
            prepareForCollapse();
        }
        Util.setVisOrGone(this.mZenFooter, false);
        this.mZenFooter.update();
        boolean fullWasVisible = this.mZenPanel.getVisibility() == 0;
        if (this.mShowFullZen && !visible) {
            fullVisible = true;
        }
        if (!(fullWasVisible == fullVisible || fullVisible)) {
            prepareForCollapse();
        }
        Util.setVisOrGone(this.mZenPanel, fullVisible);
        if (fullVisible) {
            this.mZenPanel.setZenState(this.mState.zenMode);
            this.mZenPanel.setDoneListener(new OnClickListener() {
                public void onClick(View v) {
                    VolumeDialog.this.prepareForCollapse();
                    VolumeDialog.this.mHandler.sendEmptyMessage(9);
                }
            });
        }
    }

    private void updateVolumeRowH(VolumeRow row) {
        if (D.BUG) {
            Log.d(TAG, "updateVolumeRowH s=" + row.stream);
        }
        if (this.mState != null) {
            StreamState ss = (StreamState) this.mState.states.get(row.stream);
            if (ss != null) {
                int i;
                int vlevel;
                row.ss = ss;
                if (ss.level > 0) {
                    row.lastAudibleLevel = ss.level;
                }
                if (ss.level == row.requestedLevel) {
                    row.requestedLevel = -1;
                }
                boolean isRingStream = row.stream == 2;
                boolean isSystemStream = row.stream == 1;
                boolean isAlarmStream = row.stream == 4;
                boolean isMusicStream = row.stream == 3;
                boolean isCallsStream = row.stream == 0;
                boolean isBluetoothCallsStream = row.stream == 6;
                boolean isRingVibrate = isRingStream ? this.mState.ringerModeInternal == 1 : false;
                boolean isRingSilent = isRingStream ? this.mState.ringerModeInternal == 0 : false;
                boolean isZenNone = this.mState.zenMode == 2;
                boolean isZenPriority = this.mState.zenMode == 1;
                boolean z = (isRingStream || isSystemStream) ? isZenNone : false;
                boolean z2 = isRingStream ? isZenPriority : false;
                int max = ss.levelMax * 100;
                if (max != row.slider.getMax()) {
                    row.slider.setMax(max);
                }
                updateVolumeRowHeaderVisibleH(row);
                String text = ss.name;
                if (this.mShowHeaders) {
                    if (isRingStream) {
                        text = this.mContext.getString(R.string.systemui_volume_ringtone);
                    } else if (isMusicStream) {
                        text = this.mContext.getString(R.string.systemui_volume_media);
                    } else if (isAlarmStream) {
                        text = this.mContext.getString(R.string.systemui_volume_alarms);
                    } else if (isCallsStream) {
                        text = this.mContext.getString(R.string.systemui_volume_calls);
                    } else if (isBluetoothCallsStream) {
                        text = this.mContext.getString(R.string.systemui_volume_bluetooth_calls);
                    } else if (z) {
                        text = this.mContext.getString(R.string.volume_stream_muted_dnd, new Object[]{ss.name});
                    } else if (isRingVibrate && z2) {
                        text = this.mContext.getString(R.string.volume_stream_vibrate_dnd, new Object[]{ss.name});
                    } else if (isRingVibrate) {
                        text = this.mContext.getString(R.string.volume_stream_vibrate, new Object[]{ss.name});
                    } else if (ss.muted || (this.mAutomute && ss.level == 0)) {
                        text = this.mContext.getString(R.string.volume_stream_muted, new Object[]{ss.name});
                    } else if (z2) {
                        text = this.mContext.getString(R.string.volume_stream_limited_dnd, new Object[]{ss.name});
                    }
                }
                Util.setText(row.header, text);
                boolean iconEnabled = this.mAutomute || ss.muteSupported;
                row.icon.setEnabled(iconEnabled);
                row.icon.setAlpha(iconEnabled ? 1.0f : 0.5f);
                int iconRes = isRingVibrate ? R.drawable.ic_volume_ringer_vibrate : !isRingSilent ? ss.routedToBluetooth ? (ss.muted || ss.level == 0) ? R.drawable.ic_volume_media_mute : R.drawable.ic_volume_media : (this.mAutomute && ss.level == 0) ? row.iconMuteRes : ss.muted ? row.iconMuteRes : row.iconRes : R.drawable.ic_volume_ringer_mute;
                if (ss.routedToBluetooth && isMusicStream) {
                    iconRes = R.drawable.ic_notification_bluetooth_on;
                }
                if (iconRes != row.cachedIconRes) {
                    if (row.cachedIconRes != 0 && isRingVibrate && this.mShowing) {
                        this.mController.vibrate();
                    }
                    row.cachedIconRes = iconRes;
                    row.icon.setImageResource(iconRes);
                }
                updateAlarmMute(row);
                if (iconRes == R.drawable.ic_volume_ringer_vibrate) {
                    i = 3;
                } else if (iconRes == R.drawable.ic_volume_media_mute || iconRes == row.iconMuteRes || iconRes == R.drawable.ic_volume_ringer_mute) {
                    i = 2;
                } else if (iconRes == R.drawable.ic_volume_media || iconRes == row.iconRes) {
                    i = 1;
                } else {
                    i = 0;
                }
                row.iconState = i;
                if (!iconEnabled) {
                    row.icon.setContentDescription(ss.name);
                } else if (isRingStream) {
                    if (isRingVibrate) {
                        row.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_unmute, new Object[]{ss.name}));
                    } else if (this.mController.hasVibrator()) {
                        row.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_vibrate, new Object[]{ss.name}));
                    } else {
                        row.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_mute, new Object[]{ss.name}));
                    }
                } else if (ss.muted || (this.mAutomute && ss.level == 0)) {
                    row.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_unmute, new Object[]{ss.name}));
                } else {
                    row.icon.setContentDescription(this.mContext.getString(R.string.volume_stream_content_description_mute, new Object[]{ss.name}));
                }
                if (row.ss.muted && (isRingVibrate || isRingSilent || !isRingStream)) {
                    vlevel = 0;
                } else {
                    vlevel = row.ss.level;
                }
                updateVolumeRowSliderH(row, true, vlevel);
            }
        }
    }

    private void updateAlarmMute(VolumeRow row) {
        if (this.isUserChanged && row.stream == 4 && row.ss.muted && row.ss.level != 0) {
            row.ss.muted = false;
            this.mController.updateStreamMute(4, false);
            this.isUserChanged = false;
        }
    }

    private void updateVolumeRowHeaderVisibleH(VolumeRow row) {
        boolean z = !this.mShowHeaders ? this.mExpanded ? row.ss != null ? row.ss.dynamic : false : false : true;
        if (row.cachedShowHeaders != z) {
            row.cachedShowHeaders = z;
            Util.setVisOrGone(row.header, z);
        }
    }

    private void updateVolumeRowSliderTintH(VolumeRow row, boolean isActive) {
        if (isActive && this.mExpanded) {
            row.slider.requestFocus();
        }
    }

    private void updateVolumeRowSliderH(VolumeRow row, boolean enable, int vlevel) {
        row.slider.setEnabled(enable);
        updateVolumeRowSliderTintH(row, row.stream == this.mActiveStream);
        if (!row.tracking) {
            int progress = row.slider.getProgress();
            int level = getImpliedLevel(row.slider, progress);
            boolean rowVisible = row.view.getVisibility() == 0;
            boolean inGracePeriod = SystemClock.uptimeMillis() - row.userAttempt < 1000;
            this.mHandler.removeMessages(3, row);
            if (this.mShowing && rowVisible && inGracePeriod) {
                if (D.BUG) {
                    Log.d(TAG, "inGracePeriod");
                }
                this.mHandler.sendMessageAtTime(this.mHandler.obtainMessage(3, row), row.userAttempt + 1000);
            } else if (vlevel != level || !this.mShowing || !rowVisible) {
                int newProgress = vlevel * 100;
                if (progress != newProgress) {
                    row.slider.setProgress(newProgress, false);
                }
            }
        }
    }

    private void recheckH(VolumeRow row) {
        if (row == null) {
            if (D.BUG) {
                Log.d(TAG, "recheckH ALL");
            }
            trimObsoleteH();
            for (VolumeRow r : this.mRows) {
                updateVolumeRowH(r);
            }
            return;
        }
        if (D.BUG) {
            Log.d(TAG, "recheckH " + row.stream);
        }
        updateVolumeRowH(row);
    }

    private void setStreamImportantH(int stream, boolean important) {
        for (VolumeRow row : this.mRows) {
            if (row.stream == stream) {
                row.important = important;
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void showSafetyWarningH(int flags) {
        if ((flags & 1025) != 0 || this.mShowing) {
            synchronized (this.mSafetyWarningLock) {
                if (this.mSafetyWarning != null) {
                    return;
                } else {
                    this.mSafetyWarning = new SafetyWarningDialog(this.mContext, this.mController.getAudioManager()) {
                        protected void cleanUp() {
                            synchronized (VolumeDialog.this.mSafetyWarningLock) {
                                VolumeDialog.this.mSafetyWarning = null;
                            }
                            VolumeDialog.this.recheckH(null);
                        }
                    };
                    this.mSafetyWarning.show();
                }
            }
        }
        rescheduleTimeoutH();
    }

    private boolean hasTouchFeature() {
        return this.mContext.getPackageManager().hasSystemFeature("android.hardware.touchscreen");
    }
}
