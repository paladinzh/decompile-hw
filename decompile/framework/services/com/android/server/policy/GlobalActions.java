package com.android.server.policy;

import android.app.ActivityManager;
import android.app.ActivityManagerNative;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.UserInfo;
import android.database.ContentObserver;
import android.graphics.drawable.Drawable;
import android.media.AudioManager;
import android.net.ConnectivityManager;
import android.os.Build;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.os.UserManager;
import android.os.Vibrator;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.service.dreams.IDreamManager;
import android.service.dreams.IDreamManager.Stub;
import android.telephony.PhoneStateListener;
import android.telephony.ServiceState;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.WindowManagerPolicy.WindowManagerFuncs;
import android.view.accessibility.AccessibilityEvent;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import android.widget.ListView;
import android.widget.TextView;
import com.android.internal.app.AlertController;
import com.android.internal.app.AlertController.AlertParams;
import com.android.internal.logging.MetricsLogger;
import com.android.internal.widget.LockPatternUtils;
import com.android.server.audio.AudioService;
import java.util.ArrayList;
import java.util.List;

class GlobalActions implements OnDismissListener, OnClickListener {
    private static final int DIALOG_DISMISS_DELAY = 300;
    private static final String GLOBAL_ACTION_KEY_AIRPLANE = "airplane";
    private static final String GLOBAL_ACTION_KEY_ASSIST = "assist";
    private static final String GLOBAL_ACTION_KEY_BUGREPORT = "bugreport";
    private static final String GLOBAL_ACTION_KEY_LOCKDOWN = "lockdown";
    private static final String GLOBAL_ACTION_KEY_POWER = "power";
    private static final String GLOBAL_ACTION_KEY_REBOOT = "hwrestart";
    private static final String GLOBAL_ACTION_KEY_SETTINGS = "settings";
    private static final String GLOBAL_ACTION_KEY_SILENT = "silent";
    private static final String GLOBAL_ACTION_KEY_USERS = "users";
    private static final String GLOBAL_ACTION_KEY_VOICEASSIST = "voiceassist";
    private static final int MESSAGE_DISMISS = 0;
    private static final int MESSAGE_REFRESH = 1;
    private static final int MESSAGE_SHOW = 2;
    private static final boolean SHOW_SILENT_TOGGLE = true;
    private static final String TAG = "GlobalActions";
    private MyAdapter mAdapter;
    private ContentObserver mAirplaneModeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfChange) {
            GlobalActions.this.onAirplaneModeChanged();
        }
    };
    private ToggleAction mAirplaneModeOn;
    private State mAirplaneState = State.Off;
    private final AudioManager mAudioManager;
    private BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if ("android.intent.action.CLOSE_SYSTEM_DIALOGS".equals(action) || "android.intent.action.SCREEN_OFF".equals(action)) {
                if (!PhoneWindowManager.SYSTEM_DIALOG_REASON_GLOBAL_ACTIONS.equals(intent.getStringExtra(PhoneWindowManager.SYSTEM_DIALOG_REASON_KEY))) {
                    GlobalActions.this.mHandler.sendEmptyMessage(0);
                }
            } else if ("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED".equals(action) && !intent.getBooleanExtra("PHONE_IN_ECM_STATE", false) && GlobalActions.this.mIsWaitingForEcmExit) {
                GlobalActions.this.mIsWaitingForEcmExit = false;
                GlobalActions.this.changeAirplaneModeSystemSetting(GlobalActions.SHOW_SILENT_TOGGLE);
            }
        }
    };
    private final Context mContext;
    private boolean mDeviceProvisioned = false;
    private GlobalActionsDialog mDialog;
    private final IDreamManager mDreamManager;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 0:
                    if (GlobalActions.this.mDialog != null) {
                        GlobalActions.this.mDialog.dismiss();
                        GlobalActions.this.mDialog = null;
                        return;
                    }
                    return;
                case 1:
                    GlobalActions.this.refreshSilentMode();
                    GlobalActions.this.mAdapter.notifyDataSetChanged();
                    return;
                case 2:
                    GlobalActions.this.handleShow();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mHasTelephony;
    private boolean mHasVibrator;
    private boolean mIsWaitingForEcmExit = false;
    private ArrayList<Action> mItems;
    private boolean mKeyguardShowing = false;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onServiceStateChanged(ServiceState serviceState) {
            if (GlobalActions.this.mHasTelephony) {
                GlobalActions.this.mAirplaneState = serviceState.getState() == 3 ? GlobalActions.SHOW_SILENT_TOGGLE : false ? State.On : State.Off;
                GlobalActions.this.mAirplaneModeOn.updateState(GlobalActions.this.mAirplaneState);
                GlobalActions.this.mAdapter.notifyDataSetChanged();
            }
        }
    };
    private BroadcastReceiver mRingerModeReceiver = null;
    private final boolean mShowSilentToggle;
    private Action mSilentModeAction;
    private final WindowManagerFuncs mWindowManagerFuncs;

    public interface Action {
        View create(Context context, View view, ViewGroup viewGroup, LayoutInflater layoutInflater);

        CharSequence getLabelForAccessibility(Context context);

        boolean isEnabled();

        void onPress();

        boolean showBeforeProvisioning();

        boolean showDuringKeyguard();
    }

    private static abstract class SinglePressAction implements Action {
        private final Drawable mIcon;
        private final int mIconResId;
        private final CharSequence mMessage;
        private final int mMessageResId;

        public abstract void onPress();

        protected SinglePressAction(int iconResId, int messageResId) {
            this.mIconResId = iconResId;
            this.mMessageResId = messageResId;
            this.mMessage = null;
            this.mIcon = null;
        }

        protected SinglePressAction(int iconResId, Drawable icon, CharSequence message) {
            this.mIconResId = iconResId;
            this.mMessageResId = 0;
            this.mMessage = message;
            this.mIcon = icon;
        }

        public boolean isEnabled() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public String getStatus() {
            return null;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            if (this.mMessage != null) {
                return this.mMessage;
            }
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909152);
            String status = getStatus();
            if (TextUtils.isEmpty(status)) {
                statusView.setVisibility(8);
            } else {
                statusView.setText(status);
            }
            if (this.mIcon != null) {
                icon.setImageDrawable(this.mIcon);
                icon.setScaleType(ScaleType.CENTER_CROP);
            } else if (this.mIconResId != 0) {
                icon.setImageDrawable(context.getDrawable(this.mIconResId));
            }
            if (this.mMessage != null) {
                messageView.setText(this.mMessage);
            } else {
                messageView.setText(this.mMessageResId);
            }
            return v;
        }
    }

    public static abstract class ToggleAction implements Action {
        protected int mDisabledIconResid;
        protected int mDisabledStatusMessageResId;
        protected int mEnabledIconResId;
        protected int mEnabledStatusMessageResId;
        protected int mMessageResId;
        protected State mState = State.Off;

        enum State {
            Off(false),
            TurningOn(GlobalActions.SHOW_SILENT_TOGGLE),
            TurningOff(GlobalActions.SHOW_SILENT_TOGGLE),
            On(false);
            
            private final boolean inTransition;

            private State(boolean intermediate) {
                this.inTransition = intermediate;
            }

            public boolean inTransition() {
                return this.inTransition;
            }
        }

        abstract void onToggle(boolean z);

        public ToggleAction(int enabledIconResId, int disabledIconResid, int message, int enabledStatusMessageResId, int disabledStatusMessageResId) {
            this.mEnabledIconResId = enabledIconResId;
            this.mDisabledIconResid = disabledIconResid;
            this.mMessageResId = message;
            this.mEnabledStatusMessageResId = enabledStatusMessageResId;
            this.mDisabledStatusMessageResId = disabledStatusMessageResId;
        }

        void willCreate() {
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return context.getString(this.mMessageResId);
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            willCreate();
            View v = inflater.inflate(17367142, parent, false);
            ImageView icon = (ImageView) v.findViewById(16908294);
            TextView messageView = (TextView) v.findViewById(16908299);
            TextView statusView = (TextView) v.findViewById(16909152);
            boolean enabled = isEnabled();
            if (messageView != null) {
                messageView.setText(this.mMessageResId);
                messageView.setEnabled(enabled);
            }
            boolean on = (this.mState == State.On || this.mState == State.TurningOn) ? GlobalActions.SHOW_SILENT_TOGGLE : false;
            if (icon != null) {
                icon.setImageDrawable(context.getDrawable(on ? this.mEnabledIconResId : this.mDisabledIconResid));
                icon.setEnabled(enabled);
            }
            if (statusView != null) {
                statusView.setText(on ? this.mEnabledStatusMessageResId : this.mDisabledStatusMessageResId);
                statusView.setVisibility(0);
                statusView.setEnabled(enabled);
            }
            v.setEnabled(enabled);
            return v;
        }

        public final void onPress() {
            if (this.mState.inTransition()) {
                Log.w(GlobalActions.TAG, "shouldn't be able to toggle when in transition");
                return;
            }
            boolean nowOn = this.mState != State.On ? GlobalActions.SHOW_SILENT_TOGGLE : false;
            onToggle(nowOn);
            changeStateFromPress(nowOn);
        }

        public boolean isEnabled() {
            return this.mState.inTransition() ? false : GlobalActions.SHOW_SILENT_TOGGLE;
        }

        protected void changeStateFromPress(boolean buttonOn) {
            this.mState = buttonOn ? State.On : State.Off;
        }

        public void updateState(State state) {
            this.mState = state;
        }
    }

    private interface LongPressAction extends Action {
        boolean onLongPress();
    }

    private class BugReportAction extends SinglePressAction implements LongPressAction {
        public BugReportAction() {
            super(17302357, 17039660);
        }

        public void onPress() {
            if (!ActivityManager.isUserAMonkey()) {
                GlobalActions.this.mHandler.postDelayed(new Runnable() {
                    public void run() {
                        try {
                            MetricsLogger.action(GlobalActions.this.mContext, 292);
                            ActivityManagerNative.getDefault().requestBugReport(1);
                        } catch (RemoteException e) {
                        }
                    }
                }, 500);
            }
        }

        public boolean onLongPress() {
            if (ActivityManager.isUserAMonkey()) {
                return false;
            }
            try {
                MetricsLogger.action(GlobalActions.this.mContext, 293);
                ActivityManagerNative.getDefault().requestBugReport(0);
            } catch (RemoteException e) {
            }
            return false;
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public String getStatus() {
            return GlobalActions.this.mContext.getString(17039666, new Object[]{VERSION.RELEASE, Build.ID});
        }
    }

    private static final class GlobalActionsDialog extends Dialog implements DialogInterface {
        private final MyAdapter mAdapter;
        private final AlertController mAlert = new AlertController(this.mContext, this, getWindow());
        private boolean mCancelOnUp;
        private final Context mContext = getContext();
        private EnableAccessibilityController mEnableAccessibilityController;
        private boolean mIntercepted;
        private final int mWindowTouchSlop;

        public GlobalActionsDialog(Context context, AlertParams params) {
            super(context, getDialogTheme(context));
            this.mAdapter = (MyAdapter) params.mAdapter;
            this.mWindowTouchSlop = ViewConfiguration.get(context).getScaledWindowTouchSlop();
            params.apply(this.mAlert);
        }

        private static int getDialogTheme(Context context) {
            TypedValue outValue = new TypedValue();
            context.getTheme().resolveAttribute(16843529, outValue, GlobalActions.SHOW_SILENT_TOGGLE);
            return outValue.resourceId;
        }

        protected void onStart() {
            if (EnableAccessibilityController.canEnableAccessibilityViaGesture(this.mContext)) {
                this.mEnableAccessibilityController = new EnableAccessibilityController(this.mContext, new Runnable() {
                    public void run() {
                        GlobalActionsDialog.this.dismiss();
                    }
                });
                super.setCanceledOnTouchOutside(false);
            } else {
                this.mEnableAccessibilityController = null;
                super.setCanceledOnTouchOutside(GlobalActions.SHOW_SILENT_TOGGLE);
            }
            super.onStart();
        }

        protected void onStop() {
            if (this.mEnableAccessibilityController != null) {
                this.mEnableAccessibilityController.onDestroy();
            }
            super.onStop();
        }

        public boolean dispatchTouchEvent(MotionEvent event) {
            if (this.mEnableAccessibilityController != null) {
                int action = event.getActionMasked();
                if (action == 0) {
                    View decor = getWindow().getDecorView();
                    int eventX = (int) event.getX();
                    int eventY = (int) event.getY();
                    if (eventX >= (-this.mWindowTouchSlop) && eventY >= (-this.mWindowTouchSlop) && eventX < decor.getWidth() + this.mWindowTouchSlop) {
                        if (eventY >= decor.getHeight() + this.mWindowTouchSlop) {
                        }
                    }
                    this.mCancelOnUp = GlobalActions.SHOW_SILENT_TOGGLE;
                }
                try {
                    if (this.mIntercepted) {
                        boolean onTouchEvent = this.mEnableAccessibilityController.onTouchEvent(event);
                        if (action == 1) {
                            if (this.mCancelOnUp) {
                                cancel();
                            }
                            this.mCancelOnUp = false;
                            this.mIntercepted = false;
                        }
                        return onTouchEvent;
                    }
                    this.mIntercepted = this.mEnableAccessibilityController.onInterceptTouchEvent(event);
                    if (this.mIntercepted) {
                        long now = SystemClock.uptimeMillis();
                        event = MotionEvent.obtain(now, now, 3, 0.0f, 0.0f, 0);
                        event.setSource(4098);
                        this.mCancelOnUp = GlobalActions.SHOW_SILENT_TOGGLE;
                    }
                    if (action == 1) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                } catch (Throwable th) {
                    if (action == 1) {
                        if (this.mCancelOnUp) {
                            cancel();
                        }
                        this.mCancelOnUp = false;
                        this.mIntercepted = false;
                    }
                }
            }
            return super.dispatchTouchEvent(event);
        }

        public ListView getListView() {
            return this.mAlert.getListView();
        }

        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            this.mAlert.installContent();
        }

        public boolean dispatchPopulateAccessibilityEvent(AccessibilityEvent event) {
            if (event.getEventType() == 32) {
                for (int i = 0; i < this.mAdapter.getCount(); i++) {
                    CharSequence label = this.mAdapter.getItem(i).getLabelForAccessibility(getContext());
                    if (label != null) {
                        event.getText().add(label);
                    }
                }
            }
            return super.dispatchPopulateAccessibilityEvent(event);
        }

        public boolean onKeyDown(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyDown(keyCode, event)) {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
            return super.onKeyDown(keyCode, event);
        }

        public boolean onKeyUp(int keyCode, KeyEvent event) {
            if (this.mAlert.onKeyUp(keyCode, event)) {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
            return super.onKeyUp(keyCode, event);
        }
    }

    private class MyAdapter extends BaseAdapter {
        private MyAdapter() {
        }

        public int getCount() {
            int count = 0;
            for (int i = 0; i < GlobalActions.this.mItems.size(); i++) {
                Action action = (Action) GlobalActions.this.mItems.get(i);
                if ((!GlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) && (GlobalActions.this.mDeviceProvisioned || action.showBeforeProvisioning())) {
                    count++;
                }
            }
            return count;
        }

        public boolean isEnabled(int position) {
            return getItem(position).isEnabled();
        }

        public boolean areAllItemsEnabled() {
            return false;
        }

        public Action getItem(int position) {
            int filteredPos = 0;
            for (int i = 0; i < GlobalActions.this.mItems.size(); i++) {
                Action action = (Action) GlobalActions.this.mItems.get(i);
                if ((!GlobalActions.this.mKeyguardShowing || action.showDuringKeyguard()) && (GlobalActions.this.mDeviceProvisioned || action.showBeforeProvisioning())) {
                    if (filteredPos == position) {
                        return action;
                    }
                    filteredPos++;
                }
            }
            throw new IllegalArgumentException("position " + position + " out of range of showable actions" + ", filtered count=" + getCount() + ", keyguardshowing=" + GlobalActions.this.mKeyguardShowing + ", provisioned=" + GlobalActions.this.mDeviceProvisioned);
        }

        public long getItemId(int position) {
            return (long) position;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            return getItem(position).create(GlobalActions.this.mContext, convertView, parent, LayoutInflater.from(GlobalActions.this.mContext));
        }
    }

    private final class PowerAction extends SinglePressAction implements LongPressAction {
        private PowerAction() {
            super(17301552, 17039658);
        }

        public boolean onLongPress() {
            if (((UserManager) GlobalActions.this.mContext.getSystemService("user")).hasUserRestriction("no_safe_boot")) {
                return false;
            }
            GlobalActions.this.mWindowManagerFuncs.rebootSafeMode(GlobalActions.SHOW_SILENT_TOGGLE);
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public void onPress() {
            GlobalActions.this.mWindowManagerFuncs.shutdown(GlobalActions.SHOW_SILENT_TOGGLE);
        }
    }

    private class SilentModeToggleAction extends ToggleAction {
        public SilentModeToggleAction() {
            super(17302261, 17302260, 17039667, 17039668, 17039669);
        }

        void onToggle(boolean on) {
            if (on) {
                GlobalActions.this.mAudioManager.setRingerMode(0);
            } else {
                GlobalActions.this.mAudioManager.setRingerMode(2);
            }
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }
    }

    private static class SilentModeTriStateAction implements Action, View.OnClickListener {
        private final int[] ITEM_IDS = new int[]{16909153, 16909154, 16909155};
        private final AudioManager mAudioManager;
        private final Context mContext;
        private final Handler mHandler;

        SilentModeTriStateAction(Context context, AudioManager audioManager, Handler handler) {
            this.mAudioManager = audioManager;
            this.mHandler = handler;
            this.mContext = context;
        }

        private int ringerModeToIndex(int ringerMode) {
            return ringerMode;
        }

        private int indexToRingerMode(int index) {
            return index;
        }

        public CharSequence getLabelForAccessibility(Context context) {
            return null;
        }

        public View create(Context context, View convertView, ViewGroup parent, LayoutInflater inflater) {
            View v = inflater.inflate(17367143, parent, false);
            int selectedIndex = ringerModeToIndex(this.mAudioManager.getRingerMode());
            for (int i = 0; i < 3; i++) {
                boolean z;
                View itemView = v.findViewById(this.ITEM_IDS[i]);
                if (selectedIndex == i) {
                    z = GlobalActions.SHOW_SILENT_TOGGLE;
                } else {
                    z = false;
                }
                itemView.setSelected(z);
                itemView.setTag(Integer.valueOf(i));
                itemView.setOnClickListener(this);
            }
            return v;
        }

        public void onPress() {
        }

        public boolean showDuringKeyguard() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        public boolean showBeforeProvisioning() {
            return false;
        }

        public boolean isEnabled() {
            return GlobalActions.SHOW_SILENT_TOGGLE;
        }

        void willCreate() {
        }

        public void onClick(View v) {
            if (v.getTag() instanceof Integer) {
                this.mAudioManager.setRingerMode(indexToRingerMode(((Integer) v.getTag()).intValue()));
                this.mHandler.sendEmptyMessageDelayed(0, 300);
            }
        }
    }

    public GlobalActions(Context context, WindowManagerFuncs windowManagerFuncs) {
        boolean z = false;
        this.mContext = context;
        this.mWindowManagerFuncs = windowManagerFuncs;
        this.mAudioManager = (AudioManager) this.mContext.getSystemService("audio");
        this.mDreamManager = Stub.asInterface(ServiceManager.getService("dreams"));
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.intent.action.CLOSE_SYSTEM_DIALOGS");
        filter.addAction("android.intent.action.SCREEN_OFF");
        filter.addAction("android.intent.action.EMERGENCY_CALLBACK_MODE_CHANGED");
        context.registerReceiver(this.mBroadcastReceiver, filter);
        this.mHasTelephony = ((ConnectivityManager) context.getSystemService("connectivity")).isNetworkSupported(0);
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 1);
        this.mContext.getContentResolver().registerContentObserver(Global.getUriFor("airplane_mode_on"), SHOW_SILENT_TOGGLE, this.mAirplaneModeObserver);
        Vibrator vibrator = (Vibrator) this.mContext.getSystemService("vibrator");
        this.mHasVibrator = vibrator != null ? vibrator.hasVibrator() : false;
        if (!this.mContext.getResources().getBoolean(17956995)) {
            z = SHOW_SILENT_TOGGLE;
        }
        this.mShowSilentToggle = z;
    }

    public void showDialog(boolean keyguardShowing, boolean isDeviceProvisioned) {
        this.mKeyguardShowing = keyguardShowing;
        this.mDeviceProvisioned = isDeviceProvisioned;
        if (this.mDialog != null) {
            this.mDialog.dismiss();
            this.mDialog = null;
            this.mHandler.sendEmptyMessage(2);
            return;
        }
        handleShow();
    }

    private void awakenIfNecessary() {
        if (this.mDreamManager != null) {
            try {
                if (this.mDreamManager.isDreaming()) {
                    this.mDreamManager.awaken();
                }
            } catch (RemoteException e) {
            }
        }
    }

    private void handleShow() {
        awakenIfNecessary();
        this.mDialog = createDialog();
        prepareDialog();
        if (this.mAdapter.getCount() == 1 && (this.mAdapter.getItem(0) instanceof SinglePressAction) && !(this.mAdapter.getItem(0) instanceof LongPressAction)) {
            ((SinglePressAction) this.mAdapter.getItem(0)).onPress();
            return;
        }
        LayoutParams attrs = this.mDialog.getWindow().getAttributes();
        attrs.setTitle(TAG);
        this.mDialog.getWindow().setAttributes(attrs);
        this.mDialog.show();
        this.mDialog.getWindow().getDecorView().setSystemUiVisibility(DumpState.DUMP_INSTALLS);
    }

    private GlobalActionsDialog createDialog() {
        if (this.mHasVibrator) {
            this.mSilentModeAction = new SilentModeTriStateAction(this.mContext, this.mAudioManager, this.mHandler);
        } else {
            this.mSilentModeAction = new SilentModeToggleAction();
        }
        this.mAirplaneModeOn = new ToggleAction(17302353, 17302355, 17039670, 17039671, 17039672) {
            void onToggle(boolean on) {
                if (GlobalActions.this.mHasTelephony && Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    GlobalActions.this.mIsWaitingForEcmExit = GlobalActions.SHOW_SILENT_TOGGLE;
                    Intent ecmDialogIntent = new Intent("android.intent.action.ACTION_SHOW_NOTICE_ECM_BLOCK_OTHERS", null);
                    ecmDialogIntent.addFlags(268435456);
                    GlobalActions.this.mContext.startActivity(ecmDialogIntent);
                    return;
                }
                GlobalActions.this.changeAirplaneModeSystemSetting(on);
            }

            protected void changeStateFromPress(boolean buttonOn) {
                if (GlobalActions.this.mHasTelephony && !Boolean.parseBoolean(SystemProperties.get("ril.cdma.inecmmode"))) {
                    this.mState = buttonOn ? State.TurningOn : State.TurningOff;
                    GlobalActions.this.mAirplaneState = this.mState;
                }
            }

            public boolean showDuringKeyguard() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
        onAirplaneModeChanged();
        this.mItems = new ArrayList();
        String[] defaultActions = this.mContext.getResources().getStringArray(17236030);
        ArraySet<String> addedKeys = new ArraySet();
        for (String actionKey : defaultActions) {
            if (!addedKeys.contains(actionKey)) {
                if (GLOBAL_ACTION_KEY_POWER.equals(actionKey)) {
                    this.mItems.add(new PowerAction());
                } else if (GLOBAL_ACTION_KEY_REBOOT.equals(actionKey)) {
                    HwPolicyFactory.addRebootMenu(this.mItems);
                } else if (GLOBAL_ACTION_KEY_AIRPLANE.equals(actionKey)) {
                    this.mItems.add(this.mAirplaneModeOn);
                } else if (GLOBAL_ACTION_KEY_BUGREPORT.equals(actionKey)) {
                    if (Global.getInt(this.mContext.getContentResolver(), "bugreport_in_power_menu", 0) != 0 && isCurrentUserOwner()) {
                        this.mItems.add(new BugReportAction());
                    }
                } else if (GLOBAL_ACTION_KEY_SILENT.equals(actionKey)) {
                    if (this.mShowSilentToggle) {
                        this.mItems.add(this.mSilentModeAction);
                    }
                } else if ("users".equals(actionKey)) {
                    if (SystemProperties.getBoolean("fw.power_user_switcher", false)) {
                        addUsersToMenu(this.mItems);
                    }
                } else if (GLOBAL_ACTION_KEY_SETTINGS.equals(actionKey)) {
                    this.mItems.add(getSettingsAction());
                } else if (GLOBAL_ACTION_KEY_LOCKDOWN.equals(actionKey)) {
                    this.mItems.add(getLockdownAction());
                } else if (GLOBAL_ACTION_KEY_VOICEASSIST.equals(actionKey)) {
                    this.mItems.add(getVoiceAssistAction());
                } else if ("assist".equals(actionKey)) {
                    this.mItems.add(getAssistAction());
                } else {
                    Log.e(TAG, "Invalid global action key " + actionKey);
                }
                addedKeys.add(actionKey);
            }
        }
        this.mAdapter = new MyAdapter();
        AlertParams params = new AlertParams(this.mContext);
        params.mAdapter = this.mAdapter;
        params.mOnClickListener = this;
        params.mForceInverseBackground = SHOW_SILENT_TOGGLE;
        GlobalActionsDialog dialog = new GlobalActionsDialog(this.mContext, params);
        dialog.setCanceledOnTouchOutside(false);
        dialog.getListView().setItemsCanFocus(SHOW_SILENT_TOGGLE);
        dialog.getListView().setLongClickable(SHOW_SILENT_TOGGLE);
        dialog.getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View view, int position, long id) {
                Action action = GlobalActions.this.mAdapter.getItem(position);
                if (action instanceof LongPressAction) {
                    return ((LongPressAction) action).onLongPress();
                }
                return false;
            }
        });
        dialog.getWindow().setType(2009);
        dialog.setOnDismissListener(this);
        return dialog;
    }

    private Action getSettingsAction() {
        return new SinglePressAction(17302552, 17039673) {
            public void onPress() {
                Intent intent = new Intent("android.settings.SETTINGS");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }

            public boolean showBeforeProvisioning() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
        };
    }

    private Action getAssistAction() {
        return new SinglePressAction(17302246, 17039674) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.ASSIST");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }

            public boolean showBeforeProvisioning() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
        };
    }

    private Action getVoiceAssistAction() {
        return new SinglePressAction(17302573, 17039675) {
            public void onPress() {
                Intent intent = new Intent("android.intent.action.VOICE_ASSIST");
                intent.addFlags(335544320);
                GlobalActions.this.mContext.startActivity(intent);
            }

            public boolean showDuringKeyguard() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }

            public boolean showBeforeProvisioning() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }
        };
    }

    private Action getLockdownAction() {
        return new SinglePressAction(17301551, 17039676) {
            public void onPress() {
                new LockPatternUtils(GlobalActions.this.mContext).requireCredentialEntry(-1);
                try {
                    WindowManagerGlobal.getWindowManagerService().lockNow(null);
                } catch (RemoteException e) {
                    Log.e(GlobalActions.TAG, "Error while trying to lock device.", e);
                }
            }

            public boolean showDuringKeyguard() {
                return GlobalActions.SHOW_SILENT_TOGGLE;
            }

            public boolean showBeforeProvisioning() {
                return false;
            }
        };
    }

    private UserInfo getCurrentUser() {
        try {
            return ActivityManagerNative.getDefault().getCurrentUser();
        } catch (RemoteException e) {
            return null;
        }
    }

    private boolean isCurrentUserOwner() {
        UserInfo currentUser = getCurrentUser();
        return currentUser != null ? currentUser.isPrimary() : SHOW_SILENT_TOGGLE;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void addUsersToMenu(ArrayList<Action> items) {
        UserManager um = (UserManager) this.mContext.getSystemService("user");
        if (um.isUserSwitcherEnabled()) {
            List<UserInfo> users = um.getUsers();
            UserInfo currentUser = getCurrentUser();
            for (final UserInfo user : users) {
                if (user.supportsSwitchToByUser()) {
                    boolean isCurrentUser;
                    Drawable createFromPath;
                    if (currentUser != null) {
                        if (currentUser.id == user.id) {
                        }
                        isCurrentUser = false;
                        if (user.iconPath == null) {
                            createFromPath = Drawable.createFromPath(user.iconPath);
                        } else {
                            createFromPath = null;
                        }
                        items.add(new SinglePressAction(17302445, createFromPath, (user.name == null ? user.name : "Primary") + (isCurrentUser ? " ✔" : "")) {
                            public void onPress() {
                                try {
                                    ActivityManagerNative.getDefault().switchUser(user.id);
                                } catch (RemoteException re) {
                                    Log.e(GlobalActions.TAG, "Couldn't switch user " + re);
                                }
                            }

                            public boolean showDuringKeyguard() {
                                return GlobalActions.SHOW_SILENT_TOGGLE;
                            }

                            public boolean showBeforeProvisioning() {
                                return false;
                            }
                        });
                    }
                    isCurrentUser = SHOW_SILENT_TOGGLE;
                    if (user.iconPath == null) {
                        createFromPath = null;
                    } else {
                        createFromPath = Drawable.createFromPath(user.iconPath);
                    }
                    if (user.name == null) {
                    }
                    if (isCurrentUser) {
                    }
                    items.add(/* anonymous class already generated */);
                }
            }
        }
    }

    private void prepareDialog() {
        boolean airplaneModeOn = SHOW_SILENT_TOGGLE;
        refreshSilentMode();
        if (System.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
            airplaneModeOn = false;
        }
        this.mAirplaneState = airplaneModeOn ? State.On : State.Off;
        this.mAirplaneModeOn.updateState(this.mAirplaneState);
        this.mAdapter.notifyDataSetChanged();
        this.mDialog.getWindow().setType(2009);
        if (this.mShowSilentToggle) {
            IntentFilter filter = new IntentFilter("android.media.RINGER_MODE_CHANGED");
            Context context = this.mContext;
            BroadcastReceiver anonymousClass12 = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    if (action != null && "android.media.RINGER_MODE_CHANGED".equals(action)) {
                        GlobalActions.this.mHandler.sendEmptyMessage(1);
                    }
                }
            };
            this.mRingerModeReceiver = anonymousClass12;
            context.registerReceiver(anonymousClass12, filter);
        }
    }

    private void refreshSilentMode() {
        if (!this.mHasVibrator) {
            ((ToggleAction) this.mSilentModeAction).updateState(this.mAudioManager.getRingerMode() != 2 ? SHOW_SILENT_TOGGLE : false ? State.On : State.Off);
        }
    }

    public void onDismiss(DialogInterface dialog) {
        if (this.mShowSilentToggle) {
            try {
                if (this.mRingerModeReceiver != null) {
                    this.mContext.unregisterReceiver(this.mRingerModeReceiver);
                    this.mRingerModeReceiver = null;
                }
            } catch (IllegalArgumentException ie) {
                Log.w(TAG, ie);
            }
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        if (!(this.mAdapter.getItem(which) instanceof SilentModeTriStateAction)) {
            dialog.dismiss();
        }
        this.mAdapter.getItem(which).onPress();
    }

    private void onAirplaneModeChanged() {
        boolean airplaneModeOn = SHOW_SILENT_TOGGLE;
        if (!this.mHasTelephony) {
            if (Global.getInt(this.mContext.getContentResolver(), "airplane_mode_on", 0) != 1) {
                airplaneModeOn = false;
            }
            this.mAirplaneState = airplaneModeOn ? State.On : State.Off;
            this.mAirplaneModeOn.updateState(this.mAirplaneState);
        }
    }

    private void changeAirplaneModeSystemSetting(boolean on) {
        Global.putInt(this.mContext.getContentResolver(), "airplane_mode_on", on ? 1 : 0);
        Intent intent = new Intent("android.intent.action.AIRPLANE_MODE");
        intent.addFlags(536870912);
        intent.putExtra(AudioService.CONNECT_INTENT_KEY_STATE, on);
        this.mContext.sendBroadcastAsUser(intent, UserHandle.ALL);
        if (!this.mHasTelephony) {
            State state;
            if (on) {
                state = State.On;
            } else {
                state = State.Off;
            }
            this.mAirplaneState = state;
        }
    }
}
