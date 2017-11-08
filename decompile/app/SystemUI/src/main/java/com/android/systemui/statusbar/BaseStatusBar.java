package com.android.systemui.statusbar;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.ActivityManager;
import android.app.ActivityManager.RecentTaskInfo;
import android.app.ActivityManagerNative;
import android.app.ActivityOptions;
import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Notification.Action;
import android.app.Notification.Builder;
import android.app.Notification.WearableExtender;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.app.RemoteInput;
import android.app.admin.DevicePolicyManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.IntentSender.SendIntentException;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.UserInfo;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.PowerManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.provider.Settings.Global;
import android.provider.Settings.Secure;
import android.service.dreams.IDreamManager;
import android.service.notification.NotificationListenerService;
import android.service.notification.NotificationListenerService.Ranking;
import android.service.notification.NotificationListenerService.RankingMap;
import android.service.notification.StatusBarNotification;
import android.service.vr.IVrManager;
import android.service.vr.IVrStateCallbacks;
import android.service.vr.IVrStateCallbacks.Stub;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.Slog;
import android.util.SparseArray;
import android.util.SparseBooleanArray;
import android.view.Display;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewAnimationUtils;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.WindowManager;
import android.view.WindowManagerGlobal;
import android.view.accessibility.AccessibilityManager;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.RemoteViews;
import android.widget.RemoteViews.OnClickHandler;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.statusbar.IStatusBarService;
import com.android.internal.statusbar.StatusBarIcon;
import com.android.internal.widget.LockPatternUtils;
import com.android.keyguard.KeyguardHostView.OnDismissAction;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.DejankUtils;
import com.android.systemui.Interpolators;
import com.android.systemui.R;
import com.android.systemui.RecentsComponent;
import com.android.systemui.SwipeHelper.LongPressListener;
import com.android.systemui.SystemUI;
import com.android.systemui.assist.HwAssistManager;
import com.android.systemui.recents.Recents;
import com.android.systemui.statusbar.ActivatableNotificationView.OnActivatedListener;
import com.android.systemui.statusbar.CommandQueue.Callbacks;
import com.android.systemui.statusbar.ExpandableNotificationRow.ExpansionLogger;
import com.android.systemui.statusbar.ExpandableNotificationRow.OnExpandClickListener;
import com.android.systemui.statusbar.HwSwipeHelper.HwLongPressListener;
import com.android.systemui.statusbar.NotificationData.Entry;
import com.android.systemui.statusbar.NotificationData.Environment;
import com.android.systemui.statusbar.NotificationGuts.OnGutsClosedListener;
import com.android.systemui.statusbar.phone.NavigationBarView;
import com.android.systemui.statusbar.phone.NotificationGroupManager;
import com.android.systemui.statusbar.phone.StatusBarKeyguardViewManager;
import com.android.systemui.statusbar.policy.HeadsUpManager;
import com.android.systemui.statusbar.policy.PreviewInflater;
import com.android.systemui.statusbar.policy.RemoteInputView;
import com.android.systemui.statusbar.stack.NotificationStackScrollLayout;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;
import com.huawei.android.app.admin.DeviceRestrictionManager;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public abstract class BaseStatusBar extends SystemUI implements Callbacks, OnActivatedListener, ExpansionLogger, Environment, OnExpandClickListener, OnGutsClosedListener {
    public static final boolean ENABLE_CHILD_NOTIFICATIONS = SystemProperties.getBoolean("debug.child_notifs", true);
    private static boolean ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT = false;
    public static final boolean ENABLE_REMOTE_INPUT = SystemProperties.getBoolean("debug.enable_remote_input", true);
    public static final boolean FORCE_REMOTE_INPUT_HISTORY = SystemProperties.getBoolean("debug.force_remoteinput_history", false);
    public static final DeviceRestrictionManager mDeviceRestrictionManager = new DeviceRestrictionManager();
    protected AccessibilityManager mAccessibilityManager;
    private final BroadcastReceiver mAllUsersReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED".equals(intent.getAction()) && BaseStatusBar.this.isCurrentProfile(getSendingUserId())) {
                BaseStatusBar.this.mUsersAllowingPrivateNotifications.clear();
                BaseStatusBar.this.updateLockscreenNotificationSetting();
                BaseStatusBar.this.updateNotifications();
            }
        }
    };
    protected boolean mAllowLockscreenRemoteInput;
    protected HwAssistManager mAssistManager;
    protected IStatusBarService mBarService;
    protected boolean mBouncerShowing;
    private final BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.USER_SWITCHED".equals(action)) {
                BaseStatusBar.this.mCurrentUserId = intent.getIntExtra("android.intent.extra.user_handle", -1);
                if (BaseStatusBar.this.mCurrentUserId == -1) {
                    BaseStatusBar.this.mCurrentUserId = ActivityManager.getCurrentUser();
                }
                BaseStatusBar.this.updateCurrentProfilesCache();
                Log.d("StatusBar", "userId " + BaseStatusBar.this.mCurrentUserId + " is in the house");
                BaseStatusBar.this.updateLockscreenNotificationSetting();
                BaseStatusBar.this.userSwitched(BaseStatusBar.this.mCurrentUserId);
            } else if ("android.intent.action.USER_ADDED".equals(action)) {
                BaseStatusBar.this.updateCurrentProfilesCache();
            } else if ("android.intent.action.USER_PRESENT".equals(action)) {
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    public boolean runInThread() {
                        List recentTask = null;
                        try {
                            recentTask = ActivityManagerNative.getDefault().getRecentTasks(1, 5, BaseStatusBar.this.mCurrentUserId).getList();
                        } catch (RemoteException e) {
                        }
                        if (recentTask != null && recentTask.size() > 0) {
                            UserInfo user = BaseStatusBar.this.mUserManager.getUserInfo(((RecentTaskInfo) recentTask.get(0)).userId);
                            return user != null && user.isManagedProfile();
                        }
                    }

                    public void runInUI() {
                        Toast toast = Toast.makeText(BaseStatusBar.this.mContext, R.string.managed_profile_foreground_toast, 0);
                        TextView text = (TextView) toast.getView().findViewById(16908299);
                        text.setCompoundDrawablesRelativeWithIntrinsicBounds(R.drawable.stat_sys_managed_profile_status, 0, 0, 0);
                        text.setCompoundDrawablePadding(BaseStatusBar.this.mContext.getResources().getDimensionPixelSize(R.dimen.managed_profile_toast_padding));
                        toast.show();
                    }
                });
            } else if ("com.android.systemui.statusbar.banner_action_cancel".equals(action) || "com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                ((NotificationManager) BaseStatusBar.this.mContext.getSystemService("notification")).cancel(R.id.notification_hidden);
                Secure.putInt(BaseStatusBar.this.mContext.getContentResolver(), "show_note_about_notification_hiding", 0);
                if ("com.android.systemui.statusbar.banner_action_setup".equals(action)) {
                    BaseStatusBar.this.animateCollapsePanels(2, true);
                    BaseStatusBar.this.mContext.startActivity(new Intent("android.settings.ACTION_APP_NOTIFICATION_REDACTION").addFlags(268435456));
                }
            } else if ("com.android.systemui.statusbar.work_challenge_unlocked_notification_action".equals(action)) {
                IntentSender intentSender = (IntentSender) intent.getParcelableExtra("android.intent.extra.INTENT");
                String notificationKey = intent.getStringExtra("android.intent.extra.INDEX");
                if (intentSender != null) {
                    try {
                        BaseStatusBar.this.mContext.startIntentSender(intentSender, null, 0, 0, 0);
                    } catch (SendIntentException e) {
                    }
                }
                if (notificationKey != null) {
                    try {
                        BaseStatusBar.this.mBarService.onNotificationClick(notificationKey);
                    } catch (RemoteException e2) {
                    }
                }
                BaseStatusBar.this.onWorkChallengeUnlocked();
            }
        }
    };
    protected CommandQueue mCommandQueue;
    protected final SparseArray<UserInfo> mCurrentProfiles = new SparseArray();
    protected int mCurrentUserId = 0;
    private int mDensity;
    protected boolean mDeviceInteractive;
    protected DevicePolicyManager mDevicePolicyManager;
    private boolean mDeviceProvisioned = false;
    protected boolean mDisableNotificationAlerts = false;
    protected DismissView mDismissView;
    protected Display mDisplay;
    protected IDreamManager mDreamManager;
    protected EmptyShadeView mEmptyShadeView;
    private float mFontScale;
    protected NotificationGroupManager mGroupManager = new NotificationGroupManager();
    protected H mHandler = createHandler();
    protected ArraySet<Entry> mHeadsUpEntriesToRemoveOnSwitch = new ArraySet();
    protected HeadsUpManager mHeadsUpManager;
    protected boolean mHeadsUpTicker = false;
    protected NotificationOverflowContainer mKeyguardIconOverflowContainer;
    private KeyguardManager mKeyguardManager;
    protected ArraySet<String> mKeysKeptForRemoteInput = new ArraySet();
    protected int mLayoutDirection = -1;
    private Locale mLocale;
    private LockPatternUtils mLockPatternUtils;
    private boolean mLockscreenPublicMode = false;
    private final ContentObserver mLockscreenSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            BaseStatusBar.this.mUsersAllowingPrivateNotifications.clear();
            BaseStatusBar.this.mUsersAllowingNotifications.clear();
            BaseStatusBar.this.updateNotifications();
        }
    };
    protected NavigationBarView mNavigationBarView = null;
    private NotificationClicker mNotificationClicker = new NotificationClicker();
    protected NotificationData mNotificationData;
    private NotificationGuts mNotificationGutsExposed;
    private final NotificationListenerService mNotificationListener = new NotificationListenerService() {
        private Ranking ranking = new Ranking();

        public void onListenerConnected() {
            final StatusBarNotification[] notifications = getActiveNotifications();
            if (notifications == null || notifications.length <= 0) {
                Log.e("StatusBar", "mNotificationListener notifications is empty");
                return;
            }
            final RankingMap currentRanking = getCurrentRanking();
            BaseStatusBar.this.mHandler.post(new Runnable() {
                public void run() {
                    for (StatusBarNotification sbn : notifications) {
                        BaseStatusBar.this.addNotification(sbn, currentRanking, null);
                    }
                }
            });
        }

        public void onNotificationPosted(final StatusBarNotification sbn, final RankingMap rankingMap) {
            if (sbn != null) {
                if (rankingMap != null) {
                    rankingMap.getRanking(sbn.getKey(), this.ranking);
                }
                HwLog.i("StatusBar", "onNotificationPosted: " + sbn + " important=" + this.ranking.getImportance() + ", post=" + sbn.getPostTime() + ", when=" + sbn.getNotification().when + ", vis=" + sbn.getNotification().visibility + ", userid=" + sbn.getUserId());
                BaseStatusBar.this.mHandler.post(new Runnable() {
                    public void run() {
                        BaseStatusBar.this.processForRemoteInput(sbn.getNotification());
                        String key = sbn.getKey();
                        BaseStatusBar.this.mKeysKeptForRemoteInput.remove(key);
                        boolean isUpdate = BaseStatusBar.this.mNotificationData.get(key) != null;
                        if (BaseStatusBar.ENABLE_CHILD_NOTIFICATIONS || !BaseStatusBar.this.mGroupManager.isChildInGroupWithSummary(sbn)) {
                            if (isUpdate) {
                                BaseStatusBar.this.updateNotification(sbn, rankingMap);
                            } else {
                                BaseStatusBar.this.addNotification(sbn, rankingMap, null);
                                BDReporter.e(BaseStatusBar.this.mContext, 344, "pkg:" + sbn.getPackageName());
                            }
                            final StatusBarNotification statusBarNotification = sbn;
                            SystemUIThread.runAsync(new SimpleAsyncTask() {
                                public boolean runInThread() {
                                    if (!(KeyguardUpdateMonitor.getInstance(BaseStatusBar.this.mContext).isDeviceInteractive() || BaseStatusBar.this.mNotificationData.shouldFilterOut(statusBarNotification) || statusBarNotification.isOngoing())) {
                                        SystemUiUtil.wakeScreenOnIfNeeded(BaseStatusBar.this.mContext);
                                    }
                                    return false;
                                }
                            });
                            return;
                        }
                        if (isUpdate) {
                            BaseStatusBar.this.removeNotification(key, rankingMap);
                        } else {
                            BaseStatusBar.this.mNotificationData.updateRanking(rankingMap);
                        }
                    }
                });
            }
        }

        public void onNotificationRemoved(StatusBarNotification sbn, final RankingMap rankingMap) {
            HwLog.i("StatusBar", "onNotificationRemoved: " + sbn);
            if (sbn != null) {
                final String key = sbn.getKey();
                BaseStatusBar.this.mHandler.post(new Runnable() {
                    public void run() {
                        BaseStatusBar.this.removeNotification(key, rankingMap);
                    }
                });
            }
        }

        public void onNotificationRankingUpdate(final RankingMap rankingMap) {
            HwLog.i("StatusBar", "onRankingUpdate");
            if (rankingMap != null) {
                BaseStatusBar.this.mHandler.post(new Runnable() {
                    public void run() {
                        BaseStatusBar.this.updateNotificationRanking(rankingMap);
                    }
                });
            }
        }
    };
    protected HwLongPressListener mNotificationSetting;
    private OnClickHandler mOnClickHandler = new OnClickHandler() {
        public boolean onClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
            HwLog.i("StatusBar", "Notification click handler invoked for intent: " + pendingIntent + ", " + fillInIntent + ", pkg:" + view.getContext().getPackageName());
            if (handleRemoteInput(view, pendingIntent, fillInIntent)) {
                return true;
            }
            BDReporter.e(BaseStatusBar.this.mContext, 349, "pkg:" + view.getContext().getPackageName());
            logActionClick(view);
            try {
                ActivityManagerNative.getDefault().resumeAppSwitches();
            } catch (RemoteException e) {
            }
            if (!pendingIntent.isActivity()) {
                return superOnClickHandler(view, pendingIntent, fillInIntent);
            }
            final boolean keyguardShowing = BaseStatusBar.this.mStatusBarKeyguardViewManager.isShowing();
            final boolean afterKeyguardGone = PreviewInflater.wouldLaunchResolverActivity(BaseStatusBar.this.mContext, pendingIntent.getIntent(), BaseStatusBar.this.mCurrentUserId);
            final View view2 = view;
            final PendingIntent pendingIntent2 = pendingIntent;
            final Intent intent = fillInIntent;
            BaseStatusBar.this.dismissKeyguardThenExecute(new OnDismissAction() {
                public boolean onDismiss() {
                    boolean z;
                    if (keyguardShowing && !afterKeyguardGone) {
                        try {
                            ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                            ActivityManagerNative.getDefault().resumeAppSwitches();
                        } catch (RemoteException e) {
                        }
                    }
                    boolean handled = AnonymousClass4.this.superOnClickHandler(view2, pendingIntent2, intent);
                    BaseStatusBar baseStatusBar = BaseStatusBar.this;
                    if (!keyguardShowing || afterKeyguardGone) {
                        z = false;
                    } else {
                        z = true;
                    }
                    baseStatusBar.overrideActivityPendingAppTransition(z);
                    if (handled) {
                        BaseStatusBar.this.animateCollapsePanels(2, true);
                        BaseStatusBar.this.visibilityChanged(false);
                        BaseStatusBar.this.mAssistManager.hideAssist();
                    }
                    return handled;
                }
            }, afterKeyguardGone);
            return true;
        }

        private void logActionClick(View view) {
            ViewParent parent = view.getParent();
            String key = getNotificationKeyForParent(parent);
            if (key == null) {
                Log.w("StatusBar", "Couldn't determine notification for click.");
                return;
            }
            int index = -1;
            if (view.getId() == 16909211 && parent != null && (parent instanceof ViewGroup)) {
                index = ((ViewGroup) parent).indexOfChild(view);
            }
            try {
                BaseStatusBar.this.mBarService.onNotificationActionClick(key, index);
            } catch (RemoteException e) {
            }
        }

        private String getNotificationKeyForParent(ViewParent parent) {
            while (parent != null) {
                if (parent instanceof ExpandableNotificationRow) {
                    return ((ExpandableNotificationRow) parent).getStatusBarNotification().getKey();
                }
                parent = parent.getParent();
            }
            return null;
        }

        private boolean superOnClickHandler(View view, PendingIntent pendingIntent, Intent fillInIntent) {
            return super.onClickHandler(view, pendingIntent, fillInIntent, 1);
        }

        private boolean handleRemoteInput(View view, PendingIntent pendingIntent, Intent fillInIntent) {
            Object tag = view.getTag(16908380);
            RemoteInput[] inputs = null;
            if (tag instanceof RemoteInput[]) {
                inputs = (RemoteInput[]) tag;
            }
            if (inputs == null) {
                return false;
            }
            RemoteInput input = null;
            for (RemoteInput i : inputs) {
                if (i.getAllowFreeFormInput()) {
                    input = i;
                }
            }
            if (input == null) {
                return false;
            }
            ViewParent p = view.getParent();
            View view2 = null;
            while (p != null) {
                if (p instanceof View) {
                    View pv = (View) p;
                    if (pv.isRootNamespace()) {
                        view2 = (RemoteInputView) pv.findViewWithTag(RemoteInputView.VIEW_TAG);
                        break;
                    }
                }
                p = p.getParent();
            }
            ExpandableNotificationRow row = null;
            while (p != null) {
                if (p instanceof ExpandableNotificationRow) {
                    row = (ExpandableNotificationRow) p;
                    break;
                }
                p = p.getParent();
            }
            if (view2 == null || row == null) {
                return false;
            }
            row.setUserExpanded(true);
            if (!BaseStatusBar.this.mAllowLockscreenRemoteInput) {
                if (BaseStatusBar.this.isLockscreenPublicMode()) {
                    BaseStatusBar.this.onLockedRemoteInput(row, view);
                    return true;
                }
                int userId = pendingIntent.getCreatorUserHandle().getIdentifier();
                if (BaseStatusBar.this.mUserManager.getUserInfo(userId).isManagedProfile() && BaseStatusBar.this.mKeyguardManager.isDeviceLocked(userId)) {
                    BaseStatusBar.this.onLockedWorkRemoteInput(userId, row, view);
                    return true;
                }
            }
            view2.setVisibility(0);
            int cx = view.getLeft() + (view.getWidth() / 2);
            int cy = view.getTop() + (view.getHeight() / 2);
            int w = view2.getWidth();
            int h = view2.getHeight();
            int r = Math.max(Math.max(cx + cy, (h - cy) + cx), Math.max((w - cx) + cy, (w - cx) + (h - cy)));
            if (view2.isAttachedToWindow()) {
                ViewAnimationUtils.createCircularReveal(view2, cx, cy, 0.0f, (float) r).start();
            }
            view2.setPendingIntent(pendingIntent);
            view2.setRemoteInput(inputs, input);
            view2.focus();
            BaseStatusBar.this.onShowRemoteInput(row, view);
            return true;
        }
    };
    protected PowerManager mPowerManager;
    protected RecentsComponent mRecents;
    public OnTouchListener mRecentsPreloadOnTouchListener = new OnTouchListener() {
        public boolean onTouch(View v, MotionEvent event) {
            if (BaseStatusBar.mDeviceRestrictionManager.isTaskButtonDisabled(null)) {
                Log.d("StatusBar", "the task key is disabled! OnTouchListener return false;");
                return false;
            }
            int action = event.getAction() & 255;
            if (action == 0) {
                BaseStatusBar.this.preloadRecents();
            } else if (action == 3) {
                BaseStatusBar.this.cancelPreloadingRecents();
            } else if (action == 1 && !v.isPressed()) {
                BaseStatusBar.this.cancelPreloadingRecents();
            }
            return false;
        }
    };
    protected RemoteInputController mRemoteInputController;
    protected ArraySet<Entry> mRemoteInputEntriesToRemoveOnCollapse = new ArraySet();
    protected final ContentObserver mSettingsObserver = new ContentObserver(this.mHandler) {
        public void onChange(boolean selfChange) {
            boolean provisioned = Global.getInt(BaseStatusBar.this.mContext.getContentResolver(), "device_provisioned", 0) != 0;
            if (provisioned != BaseStatusBar.this.mDeviceProvisioned) {
                BaseStatusBar.this.mDeviceProvisioned = provisioned;
                BaseStatusBar.this.updateNotifications();
            }
            BaseStatusBar.this.setZenMode(Global.getInt(BaseStatusBar.this.mContext.getContentResolver(), "zen_mode", 0));
            BaseStatusBar.this.updateLockscreenNotificationSetting();
        }
    };
    protected boolean mShowLockscreenNotifications;
    protected NotificationStackScrollLayout mStackScroller;
    protected int mState;
    protected StatusBarKeyguardViewManager mStatusBarKeyguardViewManager;
    protected boolean mUseHeadsUp = false;
    private UserManager mUserManager;
    private final SparseBooleanArray mUsersAllowingNotifications = new SparseBooleanArray();
    private final SparseBooleanArray mUsersAllowingPrivateNotifications = new SparseBooleanArray();
    protected boolean mVisible;
    private boolean mVisibleToUser;
    protected boolean mVrMode;
    private final IVrStateCallbacks mVrStateCallbacks = new Stub() {
        public void onVrStateChanged(boolean enabled) {
            BaseStatusBar.this.mVrMode = enabled;
        }
    };
    protected WindowManager mWindowManager;
    protected IWindowManager mWindowManagerService;
    protected int mZenMode;

    protected class H extends Handler {
        protected H() {
        }

        public void handleMessage(Message m) {
            boolean z = true;
            BaseStatusBar baseStatusBar;
            boolean z2;
            switch (m.what) {
                case 1019:
                    baseStatusBar = BaseStatusBar.this;
                    z2 = m.arg1 > 0;
                    if (m.arg2 == 0) {
                        z = false;
                    }
                    baseStatusBar.showRecents(z2, z);
                    return;
                case 1020:
                    baseStatusBar = BaseStatusBar.this;
                    z2 = m.arg1 > 0;
                    if (m.arg2 <= 0) {
                        z = false;
                    }
                    baseStatusBar.hideRecents(z2, z);
                    return;
                case 1021:
                    BaseStatusBar.this.toggleRecents();
                    return;
                case 1022:
                    BaseStatusBar.this.preloadRecents();
                    return;
                case 1023:
                    BaseStatusBar.this.cancelPreloadingRecents();
                    return;
                case 1024:
                    BaseStatusBar.this.showRecentsNextAffiliatedTask();
                    return;
                case 1025:
                    BaseStatusBar.this.showRecentsPreviousAffiliatedTask();
                    return;
                case 1026:
                    BaseStatusBar.this.toggleKeyboardShortcuts(m.arg1);
                    return;
                case 1027:
                    BaseStatusBar.this.dismissKeyboardShortcuts();
                    return;
                default:
                    return;
            }
        }
    }

    private final class NotificationClicker implements OnClickListener {
        private NotificationClicker() {
        }

        public void onClick(View v) {
            if (v instanceof ExpandableNotificationRow) {
                final ExpandableNotificationRow row = (ExpandableNotificationRow) v;
                final StatusBarNotification sbn = row.getStatusBarNotification();
                if (sbn == null) {
                    Log.e("StatusBar", "NotificationClicker called on an unclickable notification,");
                    return;
                } else if (row.getSettingsRow() == null || !row.getSettingsRow().isVisible()) {
                    PendingIntent intent;
                    boolean afterKeyguardGone;
                    Notification notification = sbn.getNotification();
                    if (notification.contentIntent != null) {
                        intent = notification.contentIntent;
                    } else {
                        intent = notification.fullScreenIntent;
                    }
                    final String notificationKey = sbn.getKey();
                    BDReporter.e(BaseStatusBar.this.mContext, 341, "pkg:" + sbn.getPackageName());
                    HwLog.i("StatusBar", "click notification: key=" + sbn.getKey() + ", isActivity=" + (intent != null ? intent.isActivity() : false));
                    row.setJustClicked(true);
                    DejankUtils.postAfterTraversal(new Runnable() {
                        public void run() {
                            row.setJustClicked(false);
                        }
                    });
                    BaseStatusBar.this.hideNotificationToastIfShowing();
                    final boolean keyguardShowing = BaseStatusBar.this.mStatusBarKeyguardViewManager.isShowing();
                    if (intent == null || !intent.isActivity()) {
                        afterKeyguardGone = false;
                    } else {
                        afterKeyguardGone = PreviewInflater.wouldLaunchResolverActivity(BaseStatusBar.this.mContext, intent.getIntent(), BaseStatusBar.this.mCurrentUserId);
                    }
                    OnDismissAction action = new OnDismissAction() {
                        public boolean onDismiss() {
                            if (BaseStatusBar.this.mHeadsUpManager != null && BaseStatusBar.this.mHeadsUpManager.isHeadsUp(notificationKey)) {
                                if (BaseStatusBar.this.isPanelFullyCollapsed()) {
                                    HeadsUpManager.setIsClickedNotification(row, true);
                                }
                                BaseStatusBar.this.mHeadsUpManager.releaseImmediately(notificationKey);
                            }
                            StatusBarNotification parentToCancel = null;
                            if (NotificationClicker.this.shouldAutoCancel(sbn) && BaseStatusBar.this.mGroupManager.isOnlyChildInGroup(sbn)) {
                                if (BaseStatusBar.this.mGroupManager.getLogicalGroupSummary(sbn) == null || BaseStatusBar.this.mGroupManager.getLogicalGroupSummary(sbn).getStatusBarNotification() == null) {
                                    HwLog.e("StatusBar", "some null point exception happened, but go on  ");
                                } else {
                                    StatusBarNotification summarySbn = BaseStatusBar.this.mGroupManager.getLogicalGroupSummary(sbn).getStatusBarNotification();
                                    if (summarySbn != null && NotificationClicker.this.shouldAutoCancel(summarySbn)) {
                                        parentToCancel = summarySbn;
                                    }
                                }
                            }
                            final StatusBarNotification parentToCancelFinal = parentToCancel;
                            final boolean z = keyguardShowing;
                            final boolean z2 = afterKeyguardGone;
                            final ExpandableNotificationRow expandableNotificationRow = row;
                            final PendingIntent pendingIntent = intent;
                            final String str = notificationKey;
                            new Thread() {
                                public void run() {
                                    try {
                                        if (z && !z2) {
                                            if (HwExpandableNotificationRowHelper.needDimissKeyguard(expandableNotificationRow)) {
                                                ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                                            }
                                        }
                                        ActivityManagerNative.getDefault().resumeAppSwitches();
                                    } catch (RemoteException e) {
                                    }
                                    if (pendingIntent != null) {
                                        if (pendingIntent.isActivity() && pendingIntent.getCreatorUserHandle() != null) {
                                            int userId = pendingIntent.getCreatorUserHandle().getIdentifier();
                                            if (BaseStatusBar.this.mLockPatternUtils.isSeparateProfileChallengeEnabled(userId) && BaseStatusBar.this.mKeyguardManager.isDeviceLocked(userId) && BaseStatusBar.this.startWorkChallengeIfNecessary(userId, pendingIntent.getIntentSender(), str)) {
                                                return;
                                            }
                                        }
                                        try {
                                            Bundle extras = BaseStatusBar.this.getActivityOptions();
                                            extras.putBoolean("fromSystemUI", true);
                                            pendingIntent.send(null, 0, null, null, null, null, extras);
                                        } catch (CanceledException e2) {
                                            Log.w("StatusBar", "Sending contentIntent failed: " + e2);
                                        }
                                        if (pendingIntent.isActivity()) {
                                            BaseStatusBar.this.mAssistManager.hideAssist();
                                            BaseStatusBar baseStatusBar = BaseStatusBar.this;
                                            boolean z = z ? !z2 : false;
                                            baseStatusBar.overrideActivityPendingAppTransition(z);
                                        }
                                    }
                                    try {
                                        BaseStatusBar.this.mBarService.onNotificationClick(str);
                                    } catch (RemoteException e3) {
                                    }
                                    if (parentToCancelFinal != null) {
                                        H h = BaseStatusBar.this.mHandler;
                                        final StatusBarNotification statusBarNotification = parentToCancelFinal;
                                        h.post(new Runnable() {
                                            public void run() {
                                                final StatusBarNotification statusBarNotification = statusBarNotification;
                                                Runnable removeRunnable = new Runnable() {
                                                    public void run() {
                                                        BaseStatusBar.this.performRemoveNotification(statusBarNotification, true);
                                                    }
                                                };
                                                if (BaseStatusBar.this.isCollapsing()) {
                                                    BaseStatusBar.this.addPostCollapseAction(removeRunnable);
                                                } else {
                                                    removeRunnable.run();
                                                }
                                            }
                                        });
                                    }
                                }
                            }.start();
                            if (!keyguardShowing || HwExpandableNotificationRowHelper.needDimissKeyguard(row)) {
                                BaseStatusBar.this.animateCollapsePanels(2, true, true);
                                BaseStatusBar.this.visibilityChanged(false);
                            }
                            return true;
                        }
                    };
                    if (HwExpandableNotificationRowHelper.needDimissKeyguard(row)) {
                        BaseStatusBar.this.dismissKeyguardThenExecute(action, afterKeyguardGone);
                    } else {
                        HwLog.i("StatusBar", "do not dismiss");
                        action.onDismiss();
                    }
                    return;
                } else {
                    row.animateTranslateNotification(0.0f);
                    return;
                }
            }
            Log.e("StatusBar", "NotificationClicker called on a view that is not a notification row.");
        }

        private boolean shouldAutoCancel(StatusBarNotification sbn) {
            int flags = sbn.getNotification().flags;
            if ((flags & 16) == 16 && (flags & 64) == 0) {
                return true;
            }
            return false;
        }

        public void register(ExpandableNotificationRow row, StatusBarNotification sbn) {
            Notification notification = sbn.getNotification();
            if (notification.contentIntent == null && notification.fullScreenIntent == null) {
                HwLog.i("StatusBar", "no click listener: " + sbn.getKey());
                row.setOnClickListener(null);
                return;
            }
            row.setOnClickListener(this);
        }
    }

    public abstract void addNotification(StatusBarNotification statusBarNotification, RankingMap rankingMap, Entry entry);

    protected abstract void createAndAddWindows();

    protected abstract int getMaxKeyguardNotifications(boolean z);

    public abstract void hideNotificationToastIfShowing();

    public abstract boolean isPanelFullyCollapsed();

    protected abstract boolean isSnoozedPackage(StatusBarNotification statusBarNotification);

    public abstract void maybeEscalateHeadsUp();

    protected abstract void refreshLayout(int i);

    public abstract void removeNotification(String str, RankingMap rankingMap);

    protected abstract void setAreThereNotifications();

    protected abstract void setHeadsUpUser(int i);

    protected abstract void toggleSplitScreenMode(int i, int i2);

    protected abstract void updateHeadsUp(String str, Entry entry, boolean z, boolean z2);

    protected abstract void updateNotificationRanking(RankingMap rankingMap);

    protected abstract void updateNotifications();

    public boolean isDeviceProvisioned() {
        return this.mDeviceProvisioned;
    }

    public boolean isDeviceInVrMode() {
        return this.mVrMode;
    }

    private void updateCurrentProfilesCache() {
        synchronized (this.mCurrentProfiles) {
            this.mCurrentProfiles.clear();
            if (this.mUserManager != null) {
                for (UserInfo user : this.mUserManager.getProfiles(this.mCurrentUserId)) {
                    this.mCurrentProfiles.put(user.id, user);
                }
            }
            HwLog.i("StatusBar", "mCurrentUserId=" + this.mCurrentUserId + ", mCurrentProfiles=" + this.mCurrentProfiles);
        }
        NotificationUserManager.getInstance(this.mContext).updateCurrentProfilesCache(this.mCurrentProfiles);
    }

    public void start() {
        HwSwipeHelper hwSwipeHelper = new HwSwipeHelper(this.mContext);
        hwSwipeHelper.getClass();
        this.mNotificationSetting = new HwLongPressListener();
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mWindowManagerService = WindowManagerGlobal.getWindowManagerService();
        this.mDisplay = this.mWindowManager.getDefaultDisplay();
        this.mDevicePolicyManager = (DevicePolicyManager) this.mContext.getSystemService("device_policy");
        this.mNotificationData = new NotificationData(this);
        this.mAccessibilityManager = (AccessibilityManager) this.mContext.getSystemService("accessibility");
        this.mDreamManager = IDreamManager.Stub.asInterface(ServiceManager.checkService("dreams"));
        this.mPowerManager = (PowerManager) this.mContext.getSystemService("power");
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("device_provisioned"), true, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("zen_mode"), false, this.mSettingsObserver);
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("lock_screen_show_notifications"), false, this.mSettingsObserver, -1);
        if (ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("lock_screen_allow_remote_input"), false, this.mSettingsObserver, -1);
        }
        this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor("lock_screen_allow_private_notifications"), true, this.mLockscreenSettingsObserver, -1);
        this.mBarService = IStatusBarService.Stub.asInterface(ServiceManager.getService("statusbar"));
        this.mRecents = (RecentsComponent) getComponent(Recents.class);
        Configuration currentConfig = this.mContext.getResources().getConfiguration();
        this.mLocale = currentConfig.locale;
        this.mLayoutDirection = TextUtils.getLayoutDirectionFromLocale(this.mLocale);
        this.mFontScale = currentConfig.fontScale;
        this.mDensity = currentConfig.densityDpi;
        this.mUserManager = (UserManager) this.mContext.getSystemService("user");
        this.mKeyguardManager = (KeyguardManager) this.mContext.getSystemService("keyguard");
        this.mLockPatternUtils = new LockPatternUtils(this.mContext);
        this.mCommandQueue = new HwCommandQueue(this);
        int[] switches = new int[9];
        ArrayList<IBinder> binders = new ArrayList();
        ArrayList<String> iconSlots = new ArrayList();
        ArrayList<StatusBarIcon> icons = new ArrayList();
        Rect fullscreenStackBounds = new Rect();
        Rect dockedStackBounds = new Rect();
        try {
            this.mBarService.registerStatusBar(this.mCommandQueue, iconSlots, icons, switches, binders, fullscreenStackBounds, dockedStackBounds);
        } catch (RemoteException e) {
        }
        createAndAddWindows();
        this.mSettingsObserver.onChange(false);
        disable(switches[0], switches[6], false);
        setSystemUiVisibility(switches[1], switches[7], switches[8], -1, fullscreenStackBounds, dockedStackBounds);
        topAppWindowChanged(switches[2] != 0);
        setImeWindowStatus((IBinder) binders.get(0), switches[3], switches[4], switches[5] != 0);
        int N = iconSlots.size();
        for (int i = 0; i < N; i++) {
            setIcon((String) iconSlots.get(i), (StatusBarIcon) icons.get(i));
        }
        try {
            this.mNotificationListener.registerAsSystemService(this.mContext, new ComponentName(this.mContext.getPackageName(), getClass().getCanonicalName()), -1);
        } catch (Throwable e2) {
            Log.e("StatusBar", "Unable to register notification listener", e2);
        }
        this.mCurrentUserId = UserSwitchUtils.getCurrentUser();
        setHeadsUpUser(this.mCurrentUserId);
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.USER_SWITCHED");
        filter.addAction("android.intent.action.USER_ADDED");
        filter.addAction("android.intent.action.USER_PRESENT");
        this.mContext.registerReceiver(this.mBroadcastReceiver, filter);
        IntentFilter internalFilter = new IntentFilter();
        internalFilter.addAction("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        internalFilter.addAction("com.android.systemui.statusbar.banner_action_cancel");
        internalFilter.addAction("com.android.systemui.statusbar.banner_action_setup");
        this.mContext.registerReceiver(this.mBroadcastReceiver, internalFilter, "com.android.systemui.permission.SELF", null);
        IntentFilter allUsersFilter = new IntentFilter();
        allUsersFilter.addAction("android.app.action.DEVICE_POLICY_MANAGER_STATE_CHANGED");
        this.mContext.registerReceiverAsUser(this.mAllUsersReceiver, UserHandle.ALL, allUsersFilter, null, null);
        updateCurrentProfilesCache();
        IVrManager vrManager = IVrManager.Stub.asInterface(ServiceManager.getService("vrmanager"));
        if (vrManager != null) {
            try {
                vrManager.registerListener(this.mVrStateCallbacks);
            } catch (RemoteException e3) {
                Slog.e("StatusBar", "Failed to register VR mode state listener: " + e3);
            }
        }
    }

    protected void notifyUserAboutHiddenNotifications() {
    }

    public void userSwitched(int newUserId) {
        setHeadsUpUser(newUserId);
    }

    public boolean isNotificationForCurrentProfiles(StatusBarNotification n) {
        int thisUserId = this.mCurrentUserId;
        return isCurrentProfile(n.getUserId());
    }

    protected void setNotificationShown(StatusBarNotification n) {
        setNotificationsShown(new String[]{n.getKey()});
    }

    protected void setNotificationsShown(final String[] keys) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                try {
                    BaseStatusBar.this.mNotificationListener.setNotificationsShown(keys);
                } catch (RuntimeException e) {
                    Log.d("StatusBar", "failed setNotificationsShown: ", e);
                }
                return super.runInThread();
            }
        });
    }

    protected boolean isCurrentProfile(int userId) {
        boolean z = true;
        synchronized (this.mCurrentProfiles) {
            if (userId != -1) {
                if (this.mCurrentProfiles.get(userId) == null) {
                    z = false;
                }
            }
        }
        return z;
    }

    public NotificationGroupManager getGroupManager() {
        return this.mGroupManager;
    }

    protected void dismissKeyguardThenExecute(OnDismissAction action, boolean afterKeyguardGone) {
        action.onDismiss();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        Locale locale = this.mContext.getResources().getConfiguration().locale;
        int ld = TextUtils.getLayoutDirectionFromLocale(locale);
        float fontScale = newConfig.fontScale;
        int density = newConfig.densityDpi;
        if (!(density == this.mDensity && this.mFontScale == fontScale)) {
            onDensityOrFontScaleChanged();
            this.mDensity = density;
            this.mFontScale = fontScale;
        }
        if (!(locale.equals(this.mLocale) && ld == this.mLayoutDirection)) {
            this.mLocale = locale;
            this.mLayoutDirection = ld;
            refreshLayout(ld);
        }
        HwLog.i("StatusBar", "density=" + this.mDensity + ", fontScale=" + this.mFontScale + ", locale=" + this.mLocale + ", ld=" + this.mLayoutDirection);
    }

    protected void onDensityOrFontScaleChanged() {
        ArrayList<Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        for (int i = 0; i < activeNotifications.size(); i++) {
            Entry entry = (Entry) activeNotifications.get(i);
            boolean exposedGuts = entry.row.getGuts() == this.mNotificationGutsExposed;
            entry.row.reInflateViews();
            if (exposedGuts) {
                this.mNotificationGutsExposed = entry.row.getGuts();
                bindGuts(entry.row);
            }
            entry.cacheContentViews(this.mContext, null);
            inflateViews(entry, this.mStackScroller);
        }
    }

    protected View bindVetoButtonClickListener(View row, final StatusBarNotification n) {
        View vetoButton = row.findViewById(R.id.veto);
        vetoButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                HwLog.i("StatusBar", "click to delete: key=" + n.getKey());
                v.announceForAccessibility(BaseStatusBar.this.mContext.getString(R.string.accessibility_notification_dismissed));
                BDReporter.e(BaseStatusBar.this.mContext, 346, "pkg:" + n.getPackageName());
                BaseStatusBar.this.performRemoveNotification(n, false);
            }
        });
        vetoButton.setImportantForAccessibility(2);
        return vetoButton;
    }

    protected void performRemoveNotification(StatusBarNotification n, boolean removeView) {
        HwLog.i("StatusBar", "performRemoveNotification: key=" + n.getKey() + ", removeView=" + removeView);
        try {
            this.mBarService.onNotificationClear(n.getPackageName(), n.getTag(), n.getId(), n.getUserId());
            if (FORCE_REMOTE_INPUT_HISTORY && this.mKeysKeptForRemoteInput.contains(n.getKey())) {
                this.mKeysKeptForRemoteInput.remove(n.getKey());
                removeView = true;
            }
            if (this.mRemoteInputEntriesToRemoveOnCollapse.remove(this.mNotificationData.get(n.getKey()))) {
                removeView = true;
            }
            if (removeView) {
                removeNotification(n.getKey(), null);
            }
        } catch (RemoteException e) {
        }
    }

    protected void applyColorsAndBackgrounds(StatusBarNotification sbn, Entry entry) {
        boolean z = true;
        if (entry.getContentView() != null && entry.getContentView().getId() != 16909230 && entry.targetSdk >= 9 && entry.targetSdk < 21) {
            entry.row.setShowingLegacyBackground(true);
            entry.legacy = true;
        }
        if (entry.icon != null) {
            StatusBarIconView statusBarIconView = entry.icon;
            if (entry.targetSdk >= 21) {
                z = false;
            }
            statusBarIconView.setTag(R.id.icon_is_pre_L, Boolean.valueOf(z));
        }
    }

    public boolean isMediaNotification(Entry entry) {
        if (entry.getExpandedContentView() == null || entry.getExpandedContentView().findViewById(16909232) == null) {
            return false;
        }
        return true;
    }

    private void bindGuts(final ExpandableNotificationRow row) {
        row.inflateGuts();
        final StatusBarNotification sbn = row.getStatusBarNotification();
        PackageManager pmUser = getPackageManagerForUser(this.mContext, sbn.getUser().getIdentifier());
        row.setTag(sbn.getPackageName());
        final NotificationGuts guts = row.getGuts();
        guts.setClosedListener(this);
        final String pkg = sbn.getPackageName();
        String appname = pkg;
        Drawable drawable = null;
        int appUid = -1;
        try {
            ApplicationInfo info = pmUser.getApplicationInfo(pkg, 8704);
            if (info != null) {
                appname = String.valueOf(pmUser.getApplicationLabel(info));
                drawable = pmUser.getApplicationIcon(info);
                appUid = info.uid;
            }
        } catch (NameNotFoundException e) {
            drawable = pmUser.getDefaultActivityIcon();
        }
        ((ImageView) guts.findViewById(R.id.app_icon)).setImageDrawable(drawable);
        ((TextView) guts.findViewById(R.id.pkgname)).setText(appname);
        TextView settingsButton = (TextView) guts.findViewById(R.id.more_settings);
        if (appUid >= 0) {
            settingsButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    MetricsLogger.action(BaseStatusBar.this.mContext, 205);
                    guts.resetFalsingCheck();
                    BaseStatusBar.this.mNotificationSetting.goToNotificationSetting(pkg);
                }
            });
            settingsButton.setText(R.string.hw_more_setting);
        } else {
            settingsButton.setVisibility(8);
        }
        guts.bindImportance(pmUser, sbn, this.mNotificationData.getImportance(sbn.getKey()));
        TextView doneButton = (TextView) guts.findViewById(R.id.done);
        doneButton.setText(R.string.hw_done);
        doneButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                if (guts.hasImportanceChanged() && BaseStatusBar.this.isLockscreenPublicMode() && (BaseStatusBar.this.mState == 1 || BaseStatusBar.this.mState == 2)) {
                    final StatusBarNotification statusBarNotification = sbn;
                    final ExpandableNotificationRow expandableNotificationRow = row;
                    final NotificationGuts notificationGuts = guts;
                    final View view = v;
                    BaseStatusBar.this.onLockedNotificationImportanceChange(new OnDismissAction() {
                        public boolean onDismiss() {
                            BaseStatusBar.this.saveImportanceCloseControls(statusBarNotification, expandableNotificationRow, notificationGuts, view);
                            return true;
                        }
                    });
                    return;
                }
                BaseStatusBar.this.saveImportanceCloseControls(sbn, row, guts, v);
            }
        });
    }

    private void saveImportanceCloseControls(StatusBarNotification sbn, ExpandableNotificationRow row, NotificationGuts guts, View done) {
        guts.resetFalsingCheck();
        guts.saveImportance(sbn);
        int[] rowLocation = new int[2];
        int[] doneLocation = new int[2];
        row.getLocationOnScreen(rowLocation);
        done.getLocationOnScreen(doneLocation);
        dismissPopups((doneLocation[0] - rowLocation[0]) + (done.getWidth() / 2), (doneLocation[1] - rowLocation[1]) + (done.getHeight() / 2));
    }

    protected LongPressListener getNotificationLongClicker() {
        return new LongPressListener() {
            public boolean onLongPress(View v, int x, int y) {
                if (!(v instanceof ExpandableNotificationRow)) {
                    return false;
                }
                if (v.getWindowToken() == null) {
                    Log.e("StatusBar", "Trying to show notification guts, but not attached to window");
                    return false;
                }
                final ExpandableNotificationRow row = (ExpandableNotificationRow) v;
                if (SystemUiUtil.isMarketPlaceSbn(row.getStatusBarNotification())) {
                    return true;
                }
                BaseStatusBar.this.bindGuts(row);
                final NotificationGuts guts = row.getGuts();
                if (guts == null) {
                    return false;
                }
                if (guts.getVisibility() == 0) {
                    BaseStatusBar.this.dismissPopups(x, y);
                    return false;
                }
                MetricsLogger.action(BaseStatusBar.this.mContext, 204);
                guts.setVisibility(4);
                final int i = x;
                final int i2 = y;
                guts.post(new Runnable() {
                    public void run() {
                        boolean z = false;
                        BaseStatusBar.this.dismissPopups(-1, -1, false, false);
                        guts.setVisibility(0);
                        if (guts.isAttachedToWindow()) {
                            Animator a = ViewAnimationUtils.createCircularReveal(guts, i, i2, 0.0f, (float) Math.hypot((double) Math.max(guts.getWidth() - i, i), (double) Math.max(guts.getHeight() - i2, i2)));
                            a.setDuration(360);
                            a.setInterpolator(Interpolators.LINEAR_OUT_SLOW_IN);
                            final ExpandableNotificationRow expandableNotificationRow = row;
                            a.addListener(new AnimatorListenerAdapter() {
                                public void onAnimationEnd(Animator animation) {
                                    super.onAnimationEnd(animation);
                                    expandableNotificationRow.resetTranslation();
                                }
                            });
                            a.start();
                        }
                        NotificationGuts notificationGuts = guts;
                        if (BaseStatusBar.this.mState == 1) {
                            z = true;
                        }
                        notificationGuts.setExposed(true, z);
                        row.closeRemoteInput();
                        BaseStatusBar.this.mStackScroller.onHeightChanged(null, true);
                        BaseStatusBar.this.mNotificationGutsExposed = guts;
                    }
                });
                return true;
            }
        };
    }

    public NotificationGuts getExposedGuts() {
        return this.mNotificationGutsExposed;
    }

    public void dismissPopups() {
        dismissPopups(-1, -1, true, false);
    }

    private void dismissPopups(int x, int y) {
        dismissPopups(x, y, true, false);
    }

    public void dismissPopups(int x, int y, boolean resetGear, boolean animate) {
        if (this.mNotificationGutsExposed != null) {
            this.mNotificationGutsExposed.closeControls(x, y, true);
        }
        if (resetGear) {
            this.mStackScroller.resetExposedGearView(animate, true);
        }
    }

    public void onGutsClosed(NotificationGuts guts) {
        this.mStackScroller.onHeightChanged(null, true);
        this.mNotificationGutsExposed = null;
    }

    public void showRecentApps(boolean triggeredFromAltTab, boolean fromHome) {
        int i;
        int i2 = 1;
        this.mHandler.removeMessages(1019);
        H h = this.mHandler;
        if (triggeredFromAltTab) {
            i = 1;
        } else {
            i = 0;
        }
        if (!fromHome) {
            i2 = 0;
        }
        h.obtainMessage(1019, i, i2).sendToTarget();
    }

    public void hideRecentApps(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        int i;
        int i2 = 1;
        this.mHandler.removeMessages(1020);
        H h = this.mHandler;
        if (triggeredFromAltTab) {
            i = 1;
        } else {
            i = 0;
        }
        if (!triggeredFromHomeKey) {
            i2 = 0;
        }
        h.obtainMessage(1020, i, i2).sendToTarget();
    }

    public void toggleRecentApps() {
        toggleRecents();
    }

    public void toggleSplitScreen() {
        toggleSplitScreenMode(-1, -1);
    }

    public void preloadRecentApps() {
        this.mHandler.removeMessages(1022);
        this.mHandler.sendEmptyMessage(1022);
    }

    public void cancelPreloadRecentApps() {
        this.mHandler.removeMessages(1023);
        this.mHandler.sendEmptyMessage(1023);
    }

    public void dismissKeyboardShortcutsMenu() {
        this.mHandler.removeMessages(1027);
        this.mHandler.sendEmptyMessage(1027);
    }

    public void toggleKeyboardShortcutsMenu(int deviceId) {
        this.mHandler.removeMessages(1026);
        this.mHandler.obtainMessage(1026, deviceId, 0).sendToTarget();
    }

    protected H createHandler() {
        return new H();
    }

    protected void sendCloseSystemWindows(String reason) {
        if (ActivityManagerNative.isSystemReady()) {
            try {
                ActivityManagerNative.getDefault().closeSystemDialogs(reason);
            } catch (RemoteException e) {
            }
        }
    }

    protected void showRecents(boolean triggeredFromAltTab, boolean fromHome) {
        if (this.mRecents != null) {
            sendCloseSystemWindows("recentapps");
            this.mRecents.showRecents(triggeredFromAltTab, fromHome);
        }
    }

    protected void hideRecents(boolean triggeredFromAltTab, boolean triggeredFromHomeKey) {
        if (this.mRecents != null) {
            this.mRecents.hideRecents(triggeredFromAltTab, triggeredFromHomeKey);
        }
    }

    protected void toggleRecents() {
        if (this.mRecents != null) {
            this.mRecents.toggleRecents(this.mDisplay);
        }
    }

    protected void preloadRecents() {
        if (this.mRecents != null) {
            this.mRecents.preloadRecents();
        }
    }

    protected void toggleKeyboardShortcuts(int deviceId) {
        KeyboardShortcuts.toggle(this.mContext, deviceId);
    }

    protected void dismissKeyboardShortcuts() {
        KeyboardShortcuts.dismiss();
    }

    protected void cancelPreloadingRecents() {
        if (this.mRecents != null) {
            this.mRecents.cancelPreloadingRecents();
        }
    }

    protected void showRecentsNextAffiliatedTask() {
        if (this.mRecents != null) {
            this.mRecents.showNextAffiliatedTask();
        }
    }

    protected void showRecentsPreviousAffiliatedTask() {
        if (this.mRecents != null) {
            this.mRecents.showPrevAffiliatedTask();
        }
    }

    public void setLockscreenPublicMode(boolean publicMode) {
        this.mLockscreenPublicMode = publicMode;
    }

    public boolean isLockscreenPublicMode() {
        return this.mLockscreenPublicMode;
    }

    protected void onWorkChallengeUnlocked() {
    }

    public boolean userAllowsNotificationsInPublic(int userHandle) {
        if (userHandle == -1) {
            return true;
        }
        if (this.mUsersAllowingNotifications.indexOfKey(userHandle) >= 0) {
            return this.mUsersAllowingNotifications.get(userHandle);
        }
        boolean allowed = Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 0, userHandle) != 0;
        this.mUsersAllowingNotifications.append(userHandle, allowed);
        return allowed;
    }

    public boolean userAllowsPrivateNotificationsInPublic(int userHandle) {
        if (userHandle == -1) {
            return true;
        }
        if (this.mUsersAllowingPrivateNotifications.indexOfKey(userHandle) >= 0) {
            return this.mUsersAllowingPrivateNotifications.get(userHandle);
        }
        boolean adminAllowsUnredactedNotifications = Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_private_notifications", 0, userHandle) != 0 ? adminAllowsUnredactedNotifications(userHandle) : false;
        this.mUsersAllowingPrivateNotifications.append(userHandle, adminAllowsUnredactedNotifications);
        return adminAllowsUnredactedNotifications;
    }

    private boolean adminAllowsUnredactedNotifications(int userHandle) {
        boolean z = true;
        if (userHandle == -1) {
            return true;
        }
        if ((this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, userHandle) & 8) != 0) {
            z = false;
        }
        return z;
    }

    public boolean shouldHideNotifications(int userid) {
        return (this.mStatusBarKeyguardViewManager == null || !this.mStatusBarKeyguardViewManager.isShowing() || userAllowsNotificationsInPublic(userid)) ? false : true;
    }

    public boolean shouldHideNotifications(String key) {
        if (this.mStatusBarKeyguardViewManager != null && this.mStatusBarKeyguardViewManager.isShowing() && this.mNotificationData.getVisibilityOverride(key) == -1) {
            return true;
        }
        return false;
    }

    public boolean onSecureLockScreen() {
        return this.mStatusBarKeyguardViewManager != null ? this.mStatusBarKeyguardViewManager.isShowing() : false;
    }

    public void onPanelLaidOut() {
        if (this.mState == 1 && getMaxKeyguardNotifications(false) != getMaxKeyguardNotifications(true)) {
            updateRowStates();
        }
    }

    protected void onLockedNotificationImportanceChange(OnDismissAction dismissAction) {
    }

    protected void onLockedRemoteInput(ExpandableNotificationRow row, View clickedView) {
    }

    protected void onLockedWorkRemoteInput(int userId, ExpandableNotificationRow row, View clicked) {
    }

    public void onExpandClicked(Entry clickedEntry, boolean nowExpanded) {
    }

    protected void workAroundBadLayerDrawableOpacity(View v) {
    }

    protected boolean inflateViews(Entry entry, ViewGroup parent) {
        PackageManager pmUser = getPackageManagerForUser(this.mContext, entry.notification.getUser().getIdentifier());
        StatusBarNotification sbn = entry.notification;
        entry.cacheContentViews(this.mContext, null);
        RemoteViews contentView = entry.cachedContentView;
        RemoteViews bigContentView = entry.cachedBigContentView;
        RemoteViews headsUpContentView = entry.cachedHeadsUpContentView;
        RemoteViews publicContentView = entry.cachedPublicContentView;
        if (contentView == null) {
            Log.v("StatusBar", "no contentView for: " + sbn.getNotification());
            return false;
        }
        View row;
        boolean hasUserChangedExpansion = false;
        boolean userExpanded = false;
        boolean userLocked = false;
        if (entry.row != null) {
            row = entry.row;
            hasUserChangedExpansion = row.hasUserChangedExpansion();
            userExpanded = row.isUserExpanded();
            userLocked = row.isUserLocked();
            entry.reset();
            if (hasUserChangedExpansion) {
                row.setUserExpanded(userExpanded);
            }
        } else {
            ExpandableNotificationRow row2 = (ExpandableNotificationRow) ((LayoutInflater) this.mContext.getSystemService("layout_inflater")).inflate(R.layout.status_bar_notification_row, parent, false);
            row2.setExpansionLogger(this, entry.notification.getKey());
            row2.setGroupManager(this.mGroupManager);
            row2.setHeadsUpManager(this.mHeadsUpManager);
            row2.setRemoteInputController(this.mRemoteInputController);
            row2.setOnExpandClickListener(this);
            String pkg = sbn.getPackageName();
            String appname = pkg;
            try {
                ApplicationInfo info = pmUser.getApplicationInfo(pkg, 8704);
                if (info != null) {
                    appname = String.valueOf(pmUser.getApplicationLabel(info));
                }
            } catch (NameNotFoundException e) {
            }
            row2.setAppName(appname);
        }
        workAroundBadLayerDrawableOpacity(row);
        bindVetoButtonClickListener(row, sbn).setContentDescription(this.mContext.getString(R.string.accessibility_remove_notification));
        NotificationContentView contentContainer = row.getPrivateLayout();
        NotificationContentView contentContainerPublic = row.getPublicLayout();
        row.setDescendantFocusability(393216);
        if (ENABLE_REMOTE_INPUT) {
            row.setDescendantFocusability(131072);
        }
        this.mNotificationClicker.register(row, sbn);
        View bigContentViewLocal = null;
        View headsUpContentViewLocal = null;
        View publicViewLocal = null;
        try {
            View contentViewLocal = contentView.apply(sbn.getPackageContext(this.mContext), contentContainer, this.mOnClickHandler);
            if (bigContentView != null) {
                bigContentViewLocal = bigContentView.apply(sbn.getPackageContext(this.mContext), contentContainer, this.mOnClickHandler);
            }
            if (headsUpContentView != null) {
                headsUpContentViewLocal = headsUpContentView.apply(sbn.getPackageContext(this.mContext), contentContainer, this.mOnClickHandler);
            }
            if (publicContentView != null) {
                publicViewLocal = publicContentView.apply(sbn.getPackageContext(this.mContext), contentContainerPublic, this.mOnClickHandler);
            }
            if (contentViewLocal != null) {
                try {
                    contentViewLocal.setIsRootNamespace(true);
                    contentContainer.setContractedChild(contentViewLocal);
                } catch (RuntimeException e2) {
                    Log.e("StatusBar", "couldn't init view for notification " + (sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId())), e2);
                    return false;
                }
            }
            if (bigContentViewLocal != null) {
                bigContentViewLocal.setIsRootNamespace(true);
                contentContainer.setExpandedChild(bigContentViewLocal);
            }
            if (headsUpContentViewLocal != null) {
                headsUpContentViewLocal.setIsRootNamespace(true);
                contentContainer.setHeadsUpChild(headsUpContentViewLocal);
            }
            if (publicViewLocal != null) {
                publicViewLocal.setIsRootNamespace(true);
                contentContainerPublic.setContractedChild(publicViewLocal);
            }
            try {
                entry.targetSdk = pmUser.getApplicationInfo(sbn.getPackageName(), 0).targetSdkVersion;
            } catch (NameNotFoundException ex) {
                Log.e("StatusBar", "Failed looking up ApplicationInfo for " + sbn.getPackageName(), ex);
            }
            entry.autoRedacted = entry.notification.getNotification().publicVersion == null;
            entry.row = row;
            entry.row.setOnActivatedListener(this);
            entry.row.setExpandable(bigContentViewLocal != null);
            applyColorsAndBackgrounds(sbn, entry);
            if (hasUserChangedExpansion) {
                row.setUserExpanded(userExpanded);
            }
            row.setUserLocked(userLocked);
            row.onNotificationUpdated(entry);
            return true;
        } catch (RuntimeException e22) {
            Log.e("StatusBar", "couldn't inflate view for notification " + (sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId())), e22);
            return false;
        }
    }

    private void processForRemoteInput(Notification n) {
        if (ENABLE_REMOTE_INPUT && n.extras != null && n.extras.containsKey("android.wearable.EXTENSIONS") && (n.actions == null || n.actions.length == 0)) {
            Action viableAction = null;
            List<Action> actions = new WearableExtender(n).getActions();
            int numActions = actions.size();
            for (int i = 0; i < numActions; i++) {
                Action action = (Action) actions.get(i);
                if (action != null) {
                    RemoteInput[] remoteInputs = action.getRemoteInputs();
                    if (remoteInputs != null) {
                        for (RemoteInput ri : remoteInputs) {
                            if (ri.getAllowFreeFormInput()) {
                                viableAction = action;
                                break;
                            }
                        }
                        if (viableAction != null) {
                            break;
                        }
                    } else {
                        continue;
                    }
                }
            }
            if (viableAction != null) {
                try {
                    Builder rebuilder = Builder.recoverBuilder(this.mContext, n);
                    rebuilder.setActions(new Action[]{viableAction});
                    rebuilder.build();
                } catch (Exception e) {
                    HwLog.e("StatusBar", "Builder.recoverBuilder", e);
                }
            }
        }
    }

    public void startPendingIntentDismissingKeyguard(final PendingIntent intent) {
        if (isDeviceProvisioned()) {
            boolean wouldLaunchResolverActivity;
            final boolean keyguardShowing = this.mStatusBarKeyguardViewManager.isShowing();
            if (intent.isActivity()) {
                wouldLaunchResolverActivity = PreviewInflater.wouldLaunchResolverActivity(this.mContext, intent.getIntent(), this.mCurrentUserId);
            } else {
                wouldLaunchResolverActivity = false;
            }
            dismissKeyguardThenExecute(new OnDismissAction() {
                public boolean onDismiss() {
                    final boolean z = keyguardShowing;
                    final boolean z2 = wouldLaunchResolverActivity;
                    final PendingIntent pendingIntent = intent;
                    new Thread() {
                        public void run() {
                            try {
                                if (z && !z2) {
                                    ActivityManagerNative.getDefault().keyguardWaitingForActivityDrawn();
                                }
                                ActivityManagerNative.getDefault().resumeAppSwitches();
                            } catch (RemoteException e) {
                            }
                            try {
                                pendingIntent.send(null, 0, null, null, null, null, BaseStatusBar.this.getActivityOptions());
                            } catch (CanceledException e2) {
                                Log.w("StatusBar", "Sending intent failed: " + e2);
                            }
                            if (pendingIntent.isActivity()) {
                                BaseStatusBar.this.mAssistManager.hideAssist();
                                BaseStatusBar baseStatusBar = BaseStatusBar.this;
                                boolean z = z ? !z2 : false;
                                baseStatusBar.overrideActivityPendingAppTransition(z);
                            }
                        }
                    }.start();
                    BaseStatusBar.this.animateCollapsePanels(2, true, true);
                    BaseStatusBar.this.visibilityChanged(false);
                    return true;
                }
            }, wouldLaunchResolverActivity);
        }
    }

    public void addPostCollapseAction(Runnable r) {
    }

    public boolean isCollapsing() {
        return false;
    }

    public void animateCollapsePanels(int flags, boolean force) {
    }

    public void animateCollapsePanels(int flags, boolean force, boolean delayed) {
    }

    public void overrideActivityPendingAppTransition(boolean keyguardShowing) {
        if (keyguardShowing) {
            try {
                this.mWindowManagerService.overridePendingAppTransition(null, 0, 0, null);
            } catch (RemoteException e) {
                Log.w("StatusBar", "Error overriding app transition: " + e);
            }
        }
    }

    protected boolean startWorkChallengeIfNecessary(int userId, IntentSender intendSender, String notificationKey) {
        Intent newIntent = this.mKeyguardManager.createConfirmDeviceCredentialIntent(null, null, userId);
        if (newIntent == null) {
            return false;
        }
        Intent callBackIntent = new Intent("com.android.systemui.statusbar.work_challenge_unlocked_notification_action");
        callBackIntent.putExtra("android.intent.extra.INTENT", intendSender);
        callBackIntent.putExtra("android.intent.extra.INDEX", notificationKey);
        callBackIntent.setPackage(this.mContext.getPackageName());
        newIntent.putExtra("android.intent.extra.INTENT", PendingIntent.getBroadcast(this.mContext, 0, callBackIntent, 1409286144).getIntentSender());
        try {
            ActivityManagerNative.getDefault().startConfirmDeviceCredentialIntent(newIntent);
        } catch (RemoteException e) {
        }
        return true;
    }

    protected Bundle getActivityOptions() {
        ActivityOptions options = ActivityOptions.makeBasic();
        options.setLaunchStackId(1);
        return options.toBundle();
    }

    protected void visibilityChanged(boolean visible) {
        if (this.mVisible != visible) {
            this.mVisible = visible;
            if (!visible) {
                dismissPopups();
            }
        }
        updateVisibleToUser();
    }

    protected void updateVisibleToUser() {
        boolean oldVisibleToUser = this.mVisibleToUser;
        this.mVisibleToUser = this.mVisible ? this.mDeviceInteractive : false;
        if (oldVisibleToUser != this.mVisibleToUser) {
            handleVisibleToUserChanged(this.mVisibleToUser);
        }
    }

    protected void handleVisibleToUserChanged(final boolean visibleToUser) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                try {
                    if (visibleToUser) {
                        boolean pinnedHeadsUp = BaseStatusBar.this.mHeadsUpManager.hasPinnedHeadsUp();
                        boolean clearNotificationEffects = !BaseStatusBar.this.isPanelFullyCollapsed() ? BaseStatusBar.this.mState == 0 || BaseStatusBar.this.mState == 2 : false;
                        int notificationLoad = BaseStatusBar.this.mNotificationData.getActiveNotifications().size();
                        if (pinnedHeadsUp && BaseStatusBar.this.isPanelFullyCollapsed()) {
                            notificationLoad = 1;
                        } else {
                            MetricsLogger.histogram(BaseStatusBar.this.mContext, "note_load", notificationLoad);
                        }
                        BaseStatusBar.this.mBarService.onPanelRevealed(clearNotificationEffects, notificationLoad);
                    } else {
                        BaseStatusBar.this.mBarService.onPanelHidden();
                    }
                } catch (RemoteException e) {
                }
                return false;
            }
        });
    }

    public void clearNotificationEffects() {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                try {
                    BaseStatusBar.this.mBarService.clearNotificationEffects();
                } catch (RemoteException e) {
                    HwLog.e("StatusBar", "clearNotificationEffects failed, RemoteException=" + e.getMessage());
                }
                return super.runInThread();
            }
        });
    }

    void handleNotificationError(StatusBarNotification n, String message) {
        removeNotification(n.getKey(), null);
        try {
            this.mBarService.onNotificationError(n.getPackageName(), n.getTag(), n.getId(), n.getUid(), n.getInitialPid(), message, n.getUserId());
        } catch (RemoteException e) {
        }
    }

    protected StatusBarNotification removeNotificationViews(String key, RankingMap ranking) {
        HwLog.i("StatusBar", "removeNotificationViews:" + key);
        Entry entry = this.mNotificationData.remove(key, ranking);
        if (entry == null) {
            Log.w("StatusBar", "removeNotification for unknown key: " + key);
            return null;
        }
        updateNotifications();
        return entry.notification;
    }

    protected Entry createNotificationViews(StatusBarNotification sbn) {
        StatusBarIconView iconView = createIcon(sbn);
        if (iconView == null) {
            return null;
        }
        Entry entry = new Entry(sbn, iconView);
        if (inflateViews(entry, this.mStackScroller)) {
            return entry;
        }
        handleNotificationError(sbn, "Couldn't expand RemoteViews for: " + sbn);
        return null;
    }

    public StatusBarIconView createIcon(StatusBarNotification sbn) {
        Notification n = sbn.getNotification();
        StatusBarIconView iconView = new StatusBarIconView(this.mContext, sbn.getPackageName() + "/0x" + Integer.toHexString(sbn.getId()), n);
        iconView.setScaleType(ScaleType.CENTER_INSIDE);
        iconView.setInitialPid(sbn.getInitialPid());
        iconView.setTag(sbn.getKey());
        iconView.setPackageName(sbn.getPackageName());
        iconView.setCloneIcon();
        Icon smallIcon = n.getSmallIcon();
        if (smallIcon == null) {
            handleNotificationError(sbn, "No small icon in notification from " + sbn.getPackageName());
            return null;
        }
        StatusBarIcon ic = new StatusBarIcon(sbn.getUser(), sbn.getPackageName(), smallIcon, n.iconLevel, n.number, StatusBarIconView.contentDescForNotification(this.mContext, n));
        if (iconView.set(ic)) {
            return iconView;
        }
        handleNotificationError(sbn, "Couldn't create icon: " + ic);
        return null;
    }

    protected void addNotificationViews(Entry entry, RankingMap ranking) {
        if (entry != null) {
            this.mNotificationData.add(entry, ranking);
            this.mGroupManager.updateGroupSummuryTime(entry);
            updateNotifications();
        }
    }

    protected void updateRowStates() {
        boolean shouldExpandNotification;
        this.mKeyguardIconOverflowContainer.getIconsView().removeAllViews();
        ArrayList<Entry> activeNotifications = this.mNotificationData.getActiveNotifications();
        int visibleNotifications = 0;
        boolean onKeyguard = this.mState == 1;
        int maxNotifications = 0;
        if (onKeyguard) {
            maxNotifications = getMaxKeyguardNotifications(true);
        }
        for (int i = 0; i < activeNotifications.size(); i++) {
            Entry entry = (Entry) activeNotifications.get(i);
            boolean childNotification = this.mGroupManager.isChildInGroupWithSummary(entry.notification);
            if (onKeyguard) {
                entry.row.setOnKeyguard(true);
            } else {
                entry.row.setOnKeyguard(false);
                ExpandableNotificationRow expandableNotificationRow = entry.row;
                if (visibleNotifications != 0 || childNotification) {
                    shouldExpandNotification = shouldExpandNotification(entry);
                } else {
                    shouldExpandNotification = true;
                }
                expandableNotificationRow.setSystemExpanded(shouldExpandNotification);
            }
            boolean suppressedSummary = this.mGroupManager.isSummaryOfSuppressedGroup(entry.notification) ? !entry.row.isRemoved() : false;
            boolean childWithVisibleSummary = childNotification ? this.mGroupManager.getGroupSummary(entry.notification).getVisibility() == 0 : false;
            boolean showOnKeyguard = shouldShowOnKeyguard(entry.notification);
            if (suppressedSummary || ((isLockscreenPublicMode() && !this.mShowLockscreenNotifications) || (onKeyguard && !childWithVisibleSummary && (visibleNotifications >= maxNotifications || !showOnKeyguard)))) {
                entry.row.setVisibility(8);
                if (onKeyguard && showOnKeyguard && !childNotification && !suppressedSummary) {
                    this.mKeyguardIconOverflowContainer.getIconsView().addNotification(entry);
                }
            } else {
                boolean wasGone = entry.row.getVisibility() == 8;
                entry.row.setVisibility(0);
                if (!(childNotification || entry.row.isRemoved())) {
                    if (wasGone) {
                        this.mStackScroller.generateAddAnimation(entry.row, !showOnKeyguard);
                    }
                    visibleNotifications++;
                }
            }
        }
        NotificationStackScrollLayout notificationStackScrollLayout = this.mStackScroller;
        shouldExpandNotification = onKeyguard ? this.mKeyguardIconOverflowContainer.getIconsView().getChildCount() > 0 : false;
        notificationStackScrollLayout.updateOverflowContainerVisibility(shouldExpandNotification);
        this.mStackScroller.changeViewPosition(this.mDismissView, this.mStackScroller.getChildCount() - 1);
        this.mStackScroller.changeViewPosition(this.mEmptyShadeView, this.mStackScroller.getChildCount() - 2);
        this.mStackScroller.changeViewPosition(this.mKeyguardIconOverflowContainer, this.mStackScroller.getChildCount() - 3);
    }

    private boolean shouldExpandNotification(Entry entry) {
        return (entry == null || entry.notification == null) ? false : false;
    }

    public boolean shouldShowOnKeyguard(StatusBarNotification sbn) {
        return this.mShowLockscreenNotifications && !this.mNotificationData.isAmbient(sbn.getKey());
    }

    protected void setZenMode(int mode) {
        if (isDeviceProvisioned()) {
            this.mZenMode = mode;
            updateNotifications();
        }
    }

    protected void setShowLockscreenNotifications(boolean show) {
        this.mShowLockscreenNotifications = show;
    }

    protected void setLockScreenAllowRemoteInput(boolean allowLockscreenRemoteInput) {
        this.mAllowLockscreenRemoteInput = allowLockscreenRemoteInput;
    }

    private void updateLockscreenNotificationSetting() {
        boolean show = Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_show_notifications", 1, this.mCurrentUserId) != 0;
        int dpmFlags = this.mDevicePolicyManager.getKeyguardDisabledFeatures(null, this.mCurrentUserId);
        boolean allowedByDpm = (dpmFlags & 4) == 0;
        if (!show) {
            allowedByDpm = false;
        }
        setShowLockscreenNotifications(allowedByDpm);
        if (ENABLE_LOCK_SCREEN_ALLOW_REMOTE_INPUT) {
            boolean remoteInput = Secure.getIntForUser(this.mContext.getContentResolver(), "lock_screen_allow_remote_input", 0, this.mCurrentUserId) != 0;
            boolean remoteInputDpm = (dpmFlags & 64) == 0;
            if (!remoteInput) {
                remoteInputDpm = false;
            }
            setLockScreenAllowRemoteInput(remoteInputDpm);
            return;
        }
        setLockScreenAllowRemoteInput(false);
    }

    public void updateNotification(StatusBarNotification notification, RankingMap ranking) {
        Log.d("StatusBar", "updateNotification(" + notification + ")");
        String key = notification.getKey();
        Entry entry = this.mNotificationData.get(key);
        if (entry != null) {
            StatusBarIcon ic;
            this.mHeadsUpEntriesToRemoveOnSwitch.remove(entry);
            this.mRemoteInputEntriesToRemoveOnCollapse.remove(entry);
            Notification n = notification.getNotification();
            this.mNotificationData.updateRanking(ranking);
            boolean applyInPlace = entry.cacheContentViews(this.mContext, notification.getNotification());
            boolean shouldPeek = shouldPeek(entry, notification);
            boolean alertAgain = alertAgain(entry, n);
            Log.d("StatusBar", "applyInPlace=" + applyInPlace + " shouldPeek=" + shouldPeek + " alertAgain=" + alertAgain);
            StatusBarNotification oldNotification = entry.notification;
            entry.notification = notification;
            this.mGroupManager.onEntryUpdated(entry, oldNotification);
            boolean updateSuccessful = false;
            if (applyInPlace) {
                Log.d("StatusBar", "reusing notification for key: " + key);
                try {
                    if (entry.icon != null) {
                        ic = new StatusBarIcon(notification.getUser(), notification.getPackageName(), n.getSmallIcon(), n.iconLevel, n.number, StatusBarIconView.contentDescForNotification(this.mContext, n));
                        entry.icon.setNotification(n);
                        if (!entry.icon.set(ic)) {
                            handleNotificationError(notification, "Couldn't update icon: " + ic);
                            return;
                        }
                    }
                    updateNotificationViews(entry, notification);
                    updateSuccessful = true;
                } catch (RuntimeException e) {
                    Log.w("StatusBar", "Couldn't reapply views for package " + notification.getPackageName(), e);
                }
            }
            if (!updateSuccessful) {
                Log.d("StatusBar", "not reusing notification for key: " + key);
                ic = new StatusBarIcon(notification.getUser(), notification.getPackageName(), n.getSmallIcon(), n.iconLevel, n.number, StatusBarIconView.contentDescForNotification(this.mContext, n));
                entry.icon.setNotification(n);
                entry.icon.set(ic);
                inflateViews(entry, this.mStackScroller);
            }
            updateHeadsUp(key, entry, shouldPeek, alertAgain);
            updateNotifications();
            bindVetoButtonClickListener(entry.row, notification);
            setAreThereNotifications();
        }
    }

    private void updateNotificationViews(Entry entry, StatusBarNotification sbn) {
        RemoteViews contentView = entry.cachedContentView;
        RemoteViews bigContentView = entry.cachedBigContentView;
        RemoteViews headsUpContentView = entry.cachedHeadsUpContentView;
        RemoteViews publicContentView = entry.cachedPublicContentView;
        if (entry.getContentView() != null) {
            contentView.reapply(this.mContext, entry.getContentView(), this.mOnClickHandler);
        } else {
            HwLog.w("StatusBar", "updateNotificationViews::getContentView is null!");
        }
        if (!(bigContentView == null || entry.getExpandedContentView() == null)) {
            bigContentView.reapply(sbn.getPackageContext(this.mContext), entry.getExpandedContentView(), this.mOnClickHandler);
        }
        View headsUpChild = entry.getHeadsUpContentView();
        if (!(headsUpContentView == null || headsUpChild == null)) {
            headsUpContentView.reapply(sbn.getPackageContext(this.mContext), headsUpChild, this.mOnClickHandler);
        }
        if (!(publicContentView == null || entry.getPublicContentView() == null)) {
            publicContentView.reapply(sbn.getPackageContext(this.mContext), entry.getPublicContentView(), this.mOnClickHandler);
        }
        this.mNotificationClicker.register(entry.row, sbn);
        entry.row.onNotificationUpdated(entry);
        entry.row.resetHeight();
    }

    protected void updatePublicContentView(Entry entry, StatusBarNotification sbn) {
        RemoteViews publicContentView = entry.cachedPublicContentView;
        View inflatedView = entry.getPublicContentView();
        if (entry.autoRedacted && publicContentView != null && inflatedView != null) {
            int i;
            boolean disabledByPolicy = !adminAllowsUnredactedNotifications(entry.notification.getUserId());
            Context context = this.mContext;
            if (disabledByPolicy) {
                i = 17039679;
            } else {
                i = 17039678;
            }
            String notificationHiddenText = context.getString(i);
            TextView titleView = (TextView) inflatedView.findViewById(16908310);
            if (titleView != null && !titleView.getText().toString().equals(notificationHiddenText)) {
                publicContentView.setTextViewText(16908310, notificationHiddenText);
                publicContentView.reapply(sbn.getPackageContext(this.mContext), inflatedView, this.mOnClickHandler);
                entry.row.onNotificationUpdated(entry);
            }
        }
    }

    protected void notifyHeadsUpScreenOff() {
        maybeEscalateHeadsUp();
    }

    private boolean alertAgain(Entry oldEntry, Notification newNotification) {
        return oldEntry == null || !oldEntry.hasInterrupted() || (newNotification.flags & 8) == 0;
    }

    protected boolean shouldPeek(Entry entry) {
        return shouldPeek(entry, entry.notification);
    }

    protected boolean shouldPeek(Entry entry, StatusBarNotification sbn) {
        if (!this.mUseHeadsUp || isDeviceInVrMode()) {
            return false;
        }
        if (this.mNotificationData.shouldFilterOut(sbn)) {
            Log.d("StatusBar", "No peeking: filtered notification: " + sbn.getKey() + ", mCurrentUserId=" + this.mCurrentUserId + ", mCurrentProfiles=" + this.mCurrentProfiles + ", isLockscreenPublicMode=" + isLockscreenPublicMode() + ", shouldHideNotifications by key=" + shouldHideNotifications(sbn.getKey()) + ", shouldHideNotifications by userId:=" + shouldHideNotifications(sbn.getUserId()) + ", mDeviceProvisioned=" + this.mDeviceProvisioned);
            return false;
        }
        boolean inUse = (!this.mStatusBarKeyguardViewManager.isShowing() || this.mStatusBarKeyguardViewManager.isOccluded()) ? !this.mStatusBarKeyguardViewManager.isInputRestricted() : false;
        if (inUse) {
            try {
                if (!this.mDreamManager.isDreaming()) {
                    inUse = true;
                    if (!inUse) {
                        Log.d("StatusBar", "No peeking: not in use: " + sbn.getKey());
                        return false;
                    } else if (this.mNotificationData.shouldSuppressScreenOn(sbn.getKey())) {
                        Log.d("StatusBar", "No peeking: suppressed by DND: " + sbn.getKey());
                        return false;
                    } else if (entry.hasJustLaunchedFullScreenIntent()) {
                        Log.d("StatusBar", "No peeking: recent fullscreen: " + sbn.getKey());
                        return false;
                    } else if (isSnoozedPackage(sbn)) {
                        Log.d("StatusBar", "No peeking: snoozed package: " + sbn.getKey());
                        return false;
                    } else if (HwExpandableNotificationRowHelper.alwaysShowPeek(sbn)) {
                        return true;
                    } else {
                        if (this.mNotificationData.getImportance(sbn.getKey()) < 4) {
                            Log.d("StatusBar", "No peeking: unimportant notification: " + sbn.getKey());
                            return false;
                        } else if (sbn.getNotification().fullScreenIntent != null) {
                            return true;
                        } else {
                            if (this.mNotificationData.getImportance(sbn.getKey()) >= 5) {
                                HwLog.i("StatusBar", "No peeking: max important notification: " + sbn.getKey());
                                return false;
                            } else if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                                return true;
                            } else {
                                Log.d("StatusBar", "No peeking: accessible fullscreen: " + sbn.getKey());
                                return false;
                            }
                        }
                    }
                }
            } catch (RemoteException e) {
                Log.d("StatusBar", "failed to query dream manager", e);
            }
        }
        inUse = false;
        if (!inUse) {
            Log.d("StatusBar", "No peeking: not in use: " + sbn.getKey());
            return false;
        } else if (this.mNotificationData.shouldSuppressScreenOn(sbn.getKey())) {
            Log.d("StatusBar", "No peeking: suppressed by DND: " + sbn.getKey());
            return false;
        } else if (entry.hasJustLaunchedFullScreenIntent()) {
            Log.d("StatusBar", "No peeking: recent fullscreen: " + sbn.getKey());
            return false;
        } else if (isSnoozedPackage(sbn)) {
            Log.d("StatusBar", "No peeking: snoozed package: " + sbn.getKey());
            return false;
        } else if (HwExpandableNotificationRowHelper.alwaysShowPeek(sbn)) {
            return true;
        } else {
            if (this.mNotificationData.getImportance(sbn.getKey()) < 4) {
                Log.d("StatusBar", "No peeking: unimportant notification: " + sbn.getKey());
                return false;
            } else if (sbn.getNotification().fullScreenIntent != null) {
                return true;
            } else {
                if (this.mNotificationData.getImportance(sbn.getKey()) >= 5) {
                    HwLog.i("StatusBar", "No peeking: max important notification: " + sbn.getKey());
                    return false;
                } else if (this.mAccessibilityManager.isTouchExplorationEnabled()) {
                    return true;
                } else {
                    Log.d("StatusBar", "No peeking: accessible fullscreen: " + sbn.getKey());
                    return false;
                }
            }
        }
    }

    public void setBouncerShowing(boolean bouncerShowing) {
        this.mBouncerShowing = bouncerShowing;
    }

    public boolean isBouncerShowing() {
        return this.mBouncerShowing;
    }

    public void destroy() {
        this.mContext.unregisterReceiver(this.mBroadcastReceiver);
        try {
            this.mNotificationListener.unregisterAsSystemService();
        } catch (RemoteException e) {
        }
    }

    public static PackageManager getPackageManagerForUser(Context context, int userId) {
        Context contextForUser = context;
        if (userId >= 0) {
            try {
                contextForUser = context.createPackageContextAsUser(context.getPackageName(), 4, new UserHandle(userId));
            } catch (NameNotFoundException e) {
            }
        }
        return contextForUser.getPackageManager();
    }

    public void logNotificationExpansion(final String key, final boolean userAction, final boolean expanded) {
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            public boolean runInThread() {
                try {
                    BaseStatusBar.this.mBarService.onNotificationExpansionChanged(key, userAction, expanded);
                } catch (RemoteException e) {
                }
                return false;
            }
        });
    }

    public boolean isKeyguardSecure() {
        if (this.mStatusBarKeyguardViewManager != null) {
            return this.mStatusBarKeyguardViewManager.isSecure();
        }
        Slog.w("StatusBar", "isKeyguardSecure() called before startKeyguard(), returning false", new Throwable());
        return false;
    }

    public void showAssistDisclosure() {
        if (this.mAssistManager != null) {
            this.mAssistManager.showDisclosure();
        }
    }

    public void startAssist(Bundle args) {
        if (this.mAssistManager != null) {
            this.mAssistManager.startAssist(args, false);
        }
    }

    public void onShowRemoteInput(ExpandableNotificationRow row, View clickedView) {
    }

    public boolean isNotificationPanelExpanded() {
        return false;
    }
}
