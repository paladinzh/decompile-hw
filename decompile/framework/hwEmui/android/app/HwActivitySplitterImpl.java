package android.app;

import android.app.ActivityManager.RunningTaskInfo;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.FreezeScreenScene;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Parcel;
import android.os.Parcelable;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.Log;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.view.WindowManager;
import huawei.com.android.internal.widget.HwFragmentMenuItemView;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

public class HwActivitySplitterImpl implements IHwActivitySplitterImpl {
    private static final String ACTION_CROP_WALLPAPER = "com.android.camera.action.CROP_WALLPAPER";
    private static final int ENSURE_CONTENT_SHOWING_DELAY = 300;
    public static final String EXTRA_JUMPED_ACTIVITY = "huawei.intent.extra.JUMPED_ACTIVITY";
    private static String EXTRA_SPLIT_BASE_PACKAGE = "huawei.extra.split.pkg";
    private static final String EXTRA_SPLIT_MODE = "huawei.extra.splitmode";
    private static final int EXTRA_VALUE_BASE_ACTIVITY = 4;
    private static final int EXTRA_VALUE_EXIT_ALONE = 2;
    private static final int EXTRA_VALUE_FORCE_SPLIT = 8;
    private static final int EXTRA_VALUE_SEC_ACTIVITY = 1;
    private static final int EXTRA_VALUE_SUBINTENT_ONE = 16;
    private static final int MSG_ADD_TO_ENTRY_STACK = 2;
    private static final int MSG_CLEAR_ENTRY_STACK = 1;
    private static final int MSG_SET_ACTIONBAR = 3;
    private static final int MSG_SET_FIRST_DONE = 4;
    private static final int MSG_SET_RESUMED = 5;
    private static final String TAG = "HwActivitySplitterImpl";
    private static final String TYPE_FILEMANAGER = "filemanager.dir";
    private static boolean mControllerShowing = false;
    private static boolean mFirstTimeStart = true;
    private static HashMap<Integer, HwActivitySplitterImpl> mInstanceMap = new HashMap();
    private Activity mActivity;
    private boolean mAllContentGone = false;
    private boolean mAllSubFinished = false;
    private boolean mBackPressed = false;
    private String mBasePackageName = null;
    private View mContentIndexView;
    private float mContentIndexViewWeight;
    private float mContentWindowWeight;
    private boolean mIsJumpActivity = false;
    private boolean mIsSecondStageActivity = false;
    private boolean mIsSplitBaseActivity = false;
    private boolean mRecycled = false;
    private boolean mRestart = false;
    private boolean mResumed = true;
    private boolean mSplit = false;
    private Handler mSplitHandler;
    private IBinder mToken;
    private int mTransCodeAddEntry = 0;
    private int mTransCodeClearEntry = 0;
    private int mTransCodeGetLast = 0;
    private int mTransCodeIsTop = 0;
    private int mTransCodeRemoveEntry = 0;
    private int mTransCodeSetLast = 0;
    private int mUpButtonVisibility = -1;
    private Point mWinSize = null;
    private IWindowManager mWindowManager;

    public void setActivityInfo(Activity a, IBinder token) {
        this.mActivity = a;
        this.mToken = token;
        this.mRecycled = false;
    }

    private int getSplitModeValue(Intent intent) {
        if (intent == null) {
            return 0;
        }
        return intent.getIntExtra(EXTRA_SPLIT_MODE, 0);
    }

    private Intent setToSecondaryStage(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 1);
    }

    private Intent setToExitAlone(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 2);
    }

    private Intent setToExitTogether(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) & -3);
    }

    private Intent markBaseActivity(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 4);
    }

    public Intent setToSubIntentOne(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 16);
    }

    public Intent setToForceSplit(Intent intent) {
        if (intent == null) {
            return null;
        }
        return intent.putExtra(EXTRA_SPLIT_MODE, getSplitModeValue(intent) | 8);
    }

    public HwActivitySplitterImpl(Activity a, boolean isBase) {
        this.mActivity = a;
        if (isBase) {
            removeCancelFlag(a.getIntent());
        }
    }

    public static HwActivitySplitterImpl getDefault(Activity a) {
        return getDefault(a, false);
    }

    public static HwActivitySplitterImpl getDefault(Activity a, boolean isBase) {
        if (a == null) {
            return null;
        }
        HwActivitySplitterImpl instance = (HwActivitySplitterImpl) mInstanceMap.get(Integer.valueOf(a.hashCode()));
        if (instance != null) {
            return instance;
        }
        if (!isBase && (a.getIntent() == null || (a.getIntent().getHwFlags() & 4) == 0)) {
            return null;
        }
        instance = new HwActivitySplitterImpl(a, isBase);
        mInstanceMap.put(Integer.valueOf(a.hashCode()), instance);
        return instance;
    }

    public boolean isSplitMode() {
        return this.mSplit;
    }

    public boolean isSplitBaseActivity() {
        return this.mIsSplitBaseActivity;
    }

    public boolean isSplitSecondActivity() {
        return this.mIsSecondStageActivity;
    }

    public void setSplit(View contentIndexView, float weight) {
        setBaseActivity();
        this.mContentIndexView = contentIndexView;
        this.mContentIndexViewWeight = weight;
        this.mContentWindowWeight = HwFragmentMenuItemView.ALPHA_NORMAL - this.mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(View contentIndexView) {
        setBaseActivity();
        this.mContentIndexView = contentIndexView;
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        this.mContentIndexViewWeight = (((float) leftWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + this.mActivity.getResources().getInteger(17694889)));
        this.mContentWindowWeight = HwFragmentMenuItemView.ALPHA_NORMAL - this.mContentIndexViewWeight;
        adjustContentIndexView();
    }

    public void setSplit(float contentWeight) {
        setBaseActivity();
        this.mContentWindowWeight = contentWeight;
    }

    public void setSplit() {
        setBaseActivity();
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        int rightWeight = this.mActivity.getResources().getInteger(17694889);
        this.mContentWindowWeight = (((float) rightWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + rightWeight));
    }

    public void setControllerShowing(boolean showing) {
        if (Log.HWLog) {
            Log.i(TAG, "set controller showing : " + showing + ", " + this.mActivity);
        }
        setControllerShowingValue(showing);
    }

    private static void setControllerShowingValue(boolean showing) {
        mControllerShowing = showing;
    }

    public boolean isControllerShowing() {
        return mControllerShowing;
    }

    public void setFirstIntent(Intent intent) {
        if (this.mIsSplitBaseActivity && intent != null && getCurrentSubIntent() == null) {
            setSecondStageIntent(intent);
            setSubIntent(intent);
        }
    }

    public void setTargetIntent(Intent intent) {
        setToSecondaryStage(intent);
        setSubIntent(intent);
        deliverExtraInfo(intent);
    }

    private void setSubIntent(Intent intent) {
        if (intent == null || (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & 16) == 0) {
            if (!isJumpedActivity()) {
                setCurrentSubIntent(intent);
            }
            return;
        }
        setCurrentSubIntent(null);
    }

    private void setLastSplittableActivity(Intent intent) {
        try {
            setIntentInfo(intent, null, getIntentBundle(intent), true);
        } catch (RemoteException e) {
            Log.e(TAG, "Record activity info fail!");
        }
    }

    private void setCurrentSubIntent(Intent intent) {
        try {
            setIntentInfo(intent, this.mActivity.getPackageName(), null, false);
        } catch (RemoteException e) {
            Log.e(TAG, "Record sub-intent info fail!");
        }
    }

    private void adjustContentIndexView() {
        if (this.mContentIndexView != null && this.mContentIndexViewWeight > 0.0f && reachSplitSize()) {
            LayoutParams lp = this.mContentIndexView.getLayoutParams();
            lp.width = (int) ((this.mContentIndexViewWeight * ((float) getCurrentWindowWidth())) + 0.5f);
            if (Log.HWLog) {
                Log.i(TAG, "Adjust content index view, set width to " + lp.width);
            }
            this.mContentIndexView.setLayoutParams(lp);
            adjustActionBar();
        }
    }

    public View getActionBarView() {
        View v = this.mActivity.getWindow().getDecorView();
        View actionBar = v.findViewById(this.mActivity.getResources().getIdentifier("action_bar", "id", "android"));
        if (actionBar == null) {
            return v.findViewById(this.mActivity.getResources().getIdentifier("action_bar", "id", this.mActivity.getPackageName()));
        }
        return actionBar;
    }

    private void adjustActionBar() {
        View abLayout = getActionBarView();
        if (abLayout == null || this.mContentIndexViewWeight <= 0.0f) {
            Log.w(TAG, "Can not get actionbar layout.");
            return;
        }
        LayoutParams lp = abLayout.getLayoutParams();
        lp.width = (int) ((this.mContentIndexViewWeight * ((float) getCurrentWindowWidth())) + 0.5f);
        abLayout.setLayoutParams(lp);
    }

    public void reduceIndexView() {
        if (this.mContentIndexView != null) {
            LayoutParams lp = this.mContentIndexView.getLayoutParams();
            lp.width = -1;
            this.mContentIndexView.setLayoutParams(lp);
            reduceActionBar();
        }
    }

    private void reduceActionBar() {
        View abLayout = getActionBarView();
        if (abLayout != null) {
            LayoutParams lp = abLayout.getLayoutParams();
            lp.width = -1;
            abLayout.setLayoutParams(lp);
        }
    }

    public boolean needSplitActivity() {
        if (this.mActivity == null) {
            return false;
        }
        return needSplitActivity(this.mActivity.getIntent());
    }

    public boolean needSplitActivity(Intent intent) {
        boolean split = false;
        if (intent != null) {
            split = (intent.getHwFlags() & 4) != 0 ? needCancelSplit(intent) ? (intent.getIntExtra(EXTRA_SPLIT_MODE, 0) & 8) != 0 : true : false;
            checkSecondStage(intent);
            if (!split) {
                disposeFakeActivity(intent);
            }
            if (this.mIsSecondStageActivity) {
                if (split) {
                    setSubIntent(intent);
                } else {
                    setExitWhenContentGone(false);
                }
            }
        }
        return split;
    }

    private void disposeFakeActivity(Intent intent) {
        if (this.mIsSecondStageActivity) {
            this.mIsSecondStageActivity = false;
            clearSplittableInfo();
        }
    }

    public void removeCancelFlag(Intent intent) {
        if (intent != null) {
            intent.setHwFlags(intent.getHwFlags() & -9);
        }
    }

    private boolean needCancelSplit(Intent intent) {
        boolean z = true;
        if (intent == null) {
            return false;
        }
        if ((intent.getHwFlags() & 8) == 0 && !isInUnsplittableList(intent) && (intent.getFlags() & 268435456) == 0) {
            z = false;
        }
        return z;
    }

    public static boolean needSplit(Intent intent) {
        boolean z = false;
        if (intent == null) {
            return false;
        }
        if ((intent.getHwFlags() & 4) != 0) {
            z = true;
        }
        return z;
    }

    public boolean notSupportSplit() {
        return false;
    }

    public void splitActivityIfNeeded() {
        if (reachSplitSize()) {
            splitActivity();
        }
        handleSplitActivityStack(this.mActivity.getIntent());
    }

    private void checkSecondStage(Intent intent) {
        if (intent != null) {
            boolean z;
            if ((getSplitModeValue(intent) & 1) != 0) {
                z = true;
            } else {
                z = false;
            }
            this.mIsSecondStageActivity = z;
            if (!this.mIsSecondStageActivity) {
                this.mIsSecondStageActivity = intent.getBooleanExtra(EXTRA_JUMPED_ACTIVITY, false);
            }
            if (Log.HWLog) {
                Log.i(TAG, "Is second stage ? " + this.mIsSecondStageActivity);
            }
        }
    }

    private void setActionBarButton(boolean enable) {
        if (isBaseActivityNeedExit()) {
            View abLayout = getActionBarView();
            if (abLayout != null) {
                View up = abLayout.findViewById(16908363);
                if (up != null) {
                    if (this.mUpButtonVisibility < 0) {
                        this.mUpButtonVisibility = up.getVisibility();
                    }
                    if (!enable) {
                        this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(false);
                    } else if (this.mUpButtonVisibility == 0) {
                        this.mActivity.getActionBar().setDisplayHomeAsUpEnabled(true);
                    }
                }
            }
        }
    }

    private void handleSplitActivityStack(Intent intent) {
        if (intent != null) {
            if (this.mIsSecondStageActivity) {
                pushSplitActivityToStack();
                if (this.mSplit) {
                    finishOtherSplitActivity();
                }
                return;
            }
            if (needSplitActivity(intent) && !this.mIsSplitBaseActivity) {
                pushSplitActivityToStack();
            }
        }
    }

    private String getCompName(Intent intent) {
        if (intent == null) {
            return null;
        }
        String compName = intent.getStringExtra(":settings:show_fragment");
        if (compName == null) {
            compName = intent.getComponent() == null ? null : intent.getComponent().getClassName();
        } else if (Log.HWLog) {
            Log.i(TAG, "get fragment name " + compName);
        }
        return compName;
    }

    private void pushSplitActivityToStack() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessage(2);
    }

    private void finishOtherSplitActivity() {
        checkSplitHandler();
        this.mSplitHandler.sendEmptyMessageDelayed(1, (long) getDelayTime());
    }

    private int getDelayTime() {
        try {
            int duration = this.mActivity.getResources().getInteger(this.mActivity.getResources().getIdentifier("activity_close_enter_duration", "integer", "androidhwext"));
            if (this.mWindowManager == null) {
                this.mWindowManager = Stub.asInterface(ServiceManager.getService(FreezeScreenScene.WINDOW_PARAM));
            }
            return (int) (((float) duration) * this.mWindowManager.getAnimationScale(1));
        } catch (RemoteException e) {
            Log.i(TAG, "throws RemoteException");
            return 0;
        }
    }

    private void checkSplitHandler() {
        if (this.mSplitHandler == null) {
            this.mSplitHandler = new Handler() {
                public void handleMessage(Message msg) {
                    if (msg.what == 1) {
                        boolean isTop = HwActivitySplitterImpl.this.isTopSplitActivity();
                        if (Log.HWLog) {
                            Log.i(HwActivitySplitterImpl.TAG, "Is top ? " + isTop + ", activity is " + HwActivitySplitterImpl.this.mActivity);
                        }
                        if (!isTop) {
                            return;
                        }
                        if (HwActivitySplitterImpl.this.mIsSecondStageActivity && !HwActivitySplitterImpl.this.isJumpedActivity()) {
                            if (Log.HWLog) {
                                Log.w(HwActivitySplitterImpl.TAG, "Try to clear entry stack.");
                            }
                            HwActivitySplitterImpl.this.clearEntryStack(false);
                        }
                    } else if (msg.what == 2) {
                        try {
                            HwActivitySplitterImpl.this.addToEntryStack(HwActivitySplitterImpl.this.mToken, 0, null);
                        } catch (RemoteException e) {
                            Log.e(HwActivitySplitterImpl.TAG, "addToEntryStack FAIL!");
                        }
                    } else if (msg.what == 5) {
                        Intent subIntent = HwActivitySplitterImpl.this.getCurrentSubIntent();
                        if (!(subIntent == null || HwActivitySplitterImpl.this.mAllSubFinished || !HwActivitySplitterImpl.this.isBaseActivityNeedExit())) {
                            HwActivitySplitterImpl.this.clearSplittableInfo();
                            try {
                                HwActivitySplitterImpl.this.mActivity.startActivity(subIntent);
                            } catch (Exception e2) {
                                Log.e(HwActivitySplitterImpl.TAG, "launch activity fail!");
                            }
                        }
                    } else if (msg.what == 3) {
                        HwActivitySplitterImpl.this.setActionBarButton(false);
                    }
                    super.handleMessage(msg);
                }
            };
        }
    }

    public void onSplitActivityRestart() {
        this.mRestart = true;
    }

    public boolean reachSplitSize() {
        return !this.mActivity.isInMultiWindowMode() ? HwSplitUtils.isNeedSplit(this.mActivity) : false;
    }

    public void hideAllContent() {
        clearEntryStack(true);
        clearSplittableInfo();
    }

    private void restartLastContentIfNeeded(boolean fromConfigChange) {
        if (this.mIsSplitBaseActivity) {
            Intent subIntent = getCurrentSubIntent();
            if (subIntent != null && (this.mResumed || this.mActivity.isInMultiWindowMode())) {
                boolean start = fromConfigChange || ((mFirstTimeStart || this.mRestart) && !this.mAllSubFinished);
                if (Log.HWLog) {
                    Log.i(TAG, "fromConfigChange ? " + fromConfigChange + ", FirstTime start ? " + mFirstTimeStart + ", mRestart ?" + this.mRestart);
                }
                this.mRestart = false;
                mFirstTimeStart = false;
                if (start) {
                    setSecondStageIntent(subIntent);
                    if (reachSplitSize()) {
                        clearSplittableInfo();
                        this.mActivity.startActivity(subIntent);
                    }
                }
                return;
            }
        }
        if (Log.HWLog) {
            Log.i(TAG, "Try to start subintent, return 1");
        }
    }

    public void restartLastContentIfNeeded() {
        restartLastContentIfNeeded(false);
    }

    private void splitActivity() {
        if (Log.HWLog) {
            Log.i(TAG, "Try to split Activity");
        }
        if (reachSplitSize()) {
            this.mSplit = true;
            if (Log.HWLog) {
                Log.d(TAG, "Begin to split activity.");
            }
            WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
            Parcelable p = this.mActivity.getIntent().getParcelableExtra("huawei.intent.extra.SPLIT_REGION");
            Rect r = null;
            if (p instanceof Rect) {
                r = (Rect) p;
            }
            if (r != null) {
                lp.gravity = 8388659;
                lp.x = r.left;
                lp.y = r.top;
                lp.width = Math.abs(r.right - r.left);
                lp.height = Math.abs(r.bottom - r.top);
                if (Log.HWLog) {
                    Log.d(TAG, "Rect left " + r.left + ", right " + r.right + ", top " + r.top + ", bottom " + r.bottom);
                }
            } else {
                if (this.mContentWindowWeight <= 0.0f) {
                    this.mContentWindowWeight = getContentWeight();
                }
                lp.gravity = 8388611;
                int winWidth = getCurrentWindowWidth();
                int contentWidth = (int) ((this.mContentWindowWeight * ((float) winWidth)) + 0.5f);
                if (isRtlLocale()) {
                    lp.x = 0;
                } else {
                    lp.x = winWidth - contentWidth;
                }
                lp.width = contentWidth;
            }
            this.mActivity.getWindow().setAttributes(lp);
            this.mActivity.getWindow().addFlags(32);
            return;
        }
        Log.w(TAG, "Not support split, return");
    }

    private float getContentWeight() {
        if (this.mContentWindowWeight > 0.0f) {
            return this.mContentWindowWeight;
        }
        int leftWeight = this.mActivity.getResources().getInteger(17694888);
        int rightWeight = this.mActivity.getResources().getInteger(17694889);
        this.mContentWindowWeight = (((float) rightWeight) * HwFragmentMenuItemView.ALPHA_NORMAL) / ((float) (leftWeight + rightWeight));
        return this.mContentWindowWeight;
    }

    public void adjustWindow(int width) {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.x = getCurrentWindowWidth() - width;
        lp.width = width;
        if (Log.HWLog) {
            Log.i(TAG, "Adjust content view, set width to " + lp.width);
        }
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustWindow(Rect rect) {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.gravity = 8388659;
        lp.x = rect.left;
        lp.y = rect.top;
        lp.width = Math.abs(rect.right - rect.left);
        lp.height = Math.abs(rect.bottom - rect.top);
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustToFullScreen() {
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.width = -1;
        lp.height = -1;
        this.mActivity.getWindow().setAttributes(lp);
    }

    public void adjustToSplitScreen() {
        splitActivity();
    }

    private int getCurrentWindowWidth() {
        if (this.mWinSize == null) {
            this.mWinSize = new Point();
        }
        this.mActivity.getWindowManager().getDefaultDisplay().getSize(this.mWinSize);
        if (Log.HWLog) {
            Log.i(TAG, "Get current window width is " + this.mWinSize.x);
        }
        return this.mWinSize.x;
    }

    public void reduceActivity() {
        this.mSplit = false;
        WindowManager.LayoutParams lp = this.mActivity.getWindow().getAttributes();
        lp.gravity = 17;
        lp.height = -1;
        lp.width = -1;
        this.mActivity.getWindow().setAttributes(lp);
        if (this.mIsSecondStageActivity) {
            setActionBarButton(true);
        }
    }

    public boolean checkAllContentGone() {
        if (!this.mIsSplitBaseActivity) {
            return false;
        }
        if (this.mAllContentGone && !this.mAllSubFinished && isBaseActivityNeedExit() && reachSplitSize()) {
            if (Log.HWLog) {
                Log.i(TAG, "AllContentGone.");
            }
            Intent subIntent = getCurrentSubIntent();
            if (subIntent != null) {
                this.mActivity.startActivity(subIntent);
            }
            return true;
        }
        this.mAllContentGone = true;
        clearSplittableInfo();
        return false;
    }

    public void onSplitActivityNewIntent(Intent intent) {
        if (this.mAllContentGone) {
            this.mAllContentGone = false;
        }
        if (!this.mSplit || needSplitActivity(intent)) {
            if (needSplitActivity(intent) && !this.mSplit) {
                splitActivity();
            }
        } else if (needReduceActivity(intent)) {
            reduceActivity();
        }
    }

    private boolean needReduceActivity(Intent intent) {
        if (intent == null) {
            return false;
        }
        Intent curIntent = getCurrentSubIntent();
        if (curIntent == null) {
            return true;
        }
        String comp = getCompName(intent);
        if (comp != null && comp.equals(getCompName(curIntent))) {
            return false;
        }
        String action = intent.getAction();
        return action == null || !action.equals(curIntent.getAction());
    }

    private void redirectIntent(Intent intent) {
        if (isSplitMode() && "android.settings.SETTINGS".equals(intent.getAction())) {
            intent.setAction("android.settings.WIFI_SETTINGS");
        }
    }

    public boolean isDuplicateSplittableActivity(Intent intent) {
        if (intent == null) {
            return false;
        }
        redirectIntent(intent);
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            markJumpedActivity(intent);
        }
        if (this.mIsSplitBaseActivity && reachSplitSize()) {
            return isLastIntent(intent);
        }
        return false;
    }

    private Bundle getIntentBundle(Intent intent) {
        if (intent != null) {
            return intent.getExtras();
        }
        return null;
    }

    private boolean isLastIntent(Intent intent) {
        if (intent == null) {
            return false;
        }
        Intent lastIntent = null;
        try {
            lastIntent = getIntentInfo(this.mActivity.getPackageName(), true);
        } catch (Exception e) {
            Log.e(TAG, "LastSplittableActivity FAIL!");
        }
        if (!intent.filterEquals(lastIntent)) {
            return false;
        }
        return String.valueOf(getIntentBundle(intent)).equals(String.valueOf(getIntentBundle(lastIntent)));
    }

    public void clearIllegalSplitActivity() {
    }

    public void onSplitActivityConfigurationChanged(Configuration newConfig) {
        if (Log.HWLog) {
            Log.i(TAG, "In onConfigurationChanged.");
        }
        if (this.mIsSplitBaseActivity) {
            if (reachSplitSize()) {
                adjustContentIndexView();
                if (Log.HWLog) {
                    Log.i(TAG, "Try to start subintent from onConfigChanged");
                }
                restartLastContentIfNeeded(true);
                return;
            }
            if (this.mSplitHandler != null && this.mSplitHandler.hasMessages(5)) {
                this.mSplitHandler.removeMessages(5);
            }
            reduceIndexView();
        } else if (!needSplitActivity()) {
        } else {
            if (reachSplitSize()) {
                splitActivity();
                return;
            }
            if (this.mSplitHandler != null) {
                this.mSplitHandler.removeMessages(1);
                this.mSplitHandler.sendEmptyMessage(1);
            }
            reduceActivity();
        }
    }

    public void setExitWhenContentGone(boolean exit) {
        Intent intent;
        if (exit) {
            intent = setToExitTogether(this.mActivity.getIntent());
        } else {
            intent = setToExitAlone(this.mActivity.getIntent());
        }
        if (intent != null) {
            this.mActivity.setIntent(intent);
        }
    }

    private boolean isBaseActivityNeedExit() {
        return isBaseActivityNeedExit(this.mActivity.getIntent());
    }

    private boolean isBaseActivityNeedExit(Intent intent) {
        boolean isBaseNotExit;
        if (intent == null || (getSplitModeValue(intent) & 2) == 0) {
            isBaseNotExit = false;
        } else {
            isBaseNotExit = true;
        }
        return !isBaseNotExit;
    }

    private boolean isJumpedActivity() {
        if (this.mActivity.getIntent() != null) {
            return this.mActivity.getIntent().getBooleanExtra(EXTRA_JUMPED_ACTIVITY, false);
        }
        return false;
    }

    public void handleBackPressed() {
        this.mBackPressed = true;
    }

    private boolean isInUnsplittableList(Intent intent) {
        if (!isUnsplittableAction(intent) && !isUnsplittableCategory(intent) && !isUnsplittablePackage(this.mActivity.getPackageName())) {
            return false;
        }
        if (Log.HWLog) {
            Log.i(TAG, "Unsplittable intent " + intent);
        }
        return true;
    }

    private boolean isUnsplittableAction(Intent intent) {
        String action = intent.getAction();
        if (action == null) {
            return false;
        }
        boolean isUnsplittable = false;
        if (action.equals("android.credentials.INSTALL") || action.equals("android.credentials.INSTALL_AS_USER") || action.equals("android.intent.action.OPEN_DOCUMENT") || action.equals(ACTION_CROP_WALLPAPER) || action.equals("android.media.action.STILL_IMAGE_CAMERA") || action.equals("android.media.action.STILL_IMAGE_CAMERA_SECURE") || action.equals("android.media.action.VIDEO_CAMERA") || action.equals("android.media.action.IMAGE_CAPTURE") || action.equals("android.media.action.IMAGE_CAPTURE_SECURE") || action.equals("android.media.action.VIDEO_CAPTURE")) {
            isUnsplittable = true;
        } else if (action.equals("android.intent.action.VIEW")) {
            if (intent.getType() != null) {
                isUnsplittable = intent.getType().startsWith(TYPE_FILEMANAGER);
            } else {
                isUnsplittable = false;
            }
        }
        return isUnsplittable;
    }

    private boolean isUnsplittablePackage(String pkgName) {
        if ("com.huawei.hwid".equals(pkgName) || "com.android.browser".equals(pkgName)) {
            return true;
        }
        return false;
    }

    private boolean isUnsplittableCategory(Intent intent) {
        if (intent.hasCategory("android.intent.category.HOME") || intent.hasCategory("android.intent.category.APP_BROWSER")) {
            return true;
        }
        return false;
    }

    public static void setNotSplit(Intent intent) {
        if (intent != null) {
            intent.addHwFlags(8);
        }
    }

    public static void setAsJumpActivity(Intent intent) {
        if (intent != null) {
            intent.putExtra(EXTRA_JUMPED_ACTIVITY, true);
        }
    }

    public void handleJumpActivity(Intent intent) {
        this.mIsJumpActivity = true;
        setAsJumpActivity(intent);
    }

    private void markJumpedActivity(Intent intent) {
        if (HwSplitUtils.isJumpedActivity(this.mActivity.getClass().getName(), getCompName(intent))) {
            handleJumpActivity(intent);
        }
    }

    public void setSplittableIfNeeded(Intent intent) {
        if (intent == null) {
            Log.e(TAG, "Intent null when set splittable." + this.mActivity);
            return;
        }
        if (Log.HWLog) {
            Log.i(TAG, "Set splittable if needed.");
        }
        this.mAllSubFinished = false;
        if (this.mIsSplitBaseActivity) {
            if (Log.HWLog) {
                Log.i(TAG, "Need split for intent " + intent);
            }
            setSecondStageIntent(intent);
            intent.putExtra(EXTRA_SPLIT_BASE_PACKAGE, this.mActivity.getPackageName());
        } else if ((this.mContentWindowWeight > 0.0f || needSplitActivity()) && !needCancelSplit(intent)) {
            if (Log.HWLog) {
                Log.i(TAG, "Set splittable for intent : " + intent);
            }
            deliverExtraInfo(intent);
        }
    }

    private String getBasePackage() {
        if (this.mBasePackageName != null) {
            return this.mBasePackageName;
        }
        if (this.mActivity.getIntent() != null) {
            this.mBasePackageName = this.mActivity.getIntent().getStringExtra(EXTRA_SPLIT_BASE_PACKAGE);
        }
        if (this.mBasePackageName == null) {
            this.mBasePackageName = this.mActivity.getPackageName();
        }
        return this.mBasePackageName;
    }

    private void deliverExtraInfo(Intent intent) {
        if (intent != null) {
            intent.addHwFlags(4);
            if (this.mActivity.getIntent() != null) {
                if (this.mActivity.getIntent().hasExtra(EXTRA_SPLIT_BASE_PACKAGE)) {
                    intent.putExtra(EXTRA_SPLIT_BASE_PACKAGE, this.mActivity.getIntent().getStringExtra(EXTRA_SPLIT_BASE_PACKAGE));
                }
                if (this.mActivity.getIntent().hasExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE)) {
                    intent.putExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE, this.mActivity.getIntent().getDoubleArrayExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE));
                }
            }
        }
    }

    private void setSecondStageIntent(Intent intent) {
        if (this.mIsSplitBaseActivity && reachSplitSize() && !needCancelSplit(intent)) {
            setLastSplittableActivity(intent);
            setToSecondaryStage(intent);
            if (!isBaseActivityNeedExit()) {
                setToExitAlone(intent);
            }
            deliverExtraInfo(intent);
        }
    }

    private void setBaseActivity() {
        this.mIsSplitBaseActivity = true;
        markBaseActivity(this.mActivity.getIntent());
    }

    private void clearSplittableInfo() {
        setLastSplittableActivity(null);
    }

    public void setSplitActivityOrientation(int requestedOrientation) {
    }

    public Intent getCurrentSubIntent() {
        try {
            return getIntentInfo(this.mActivity.getPackageName(), false);
        } catch (Exception e) {
            Log.e(TAG, "LastSplittableActivity FAIL!");
            return null;
        }
    }

    public void cancelSplit(Intent intent) {
        intent.addHwFlags(8);
    }

    public void setAnimForSplitActivity() {
    }

    public void setResumed(boolean resumed) {
        if (this.mIsSplitBaseActivity || this.mIsSecondStageActivity) {
            if (Log.HWLog) {
                Log.i(TAG, "Set resumed to " + resumed + " for " + this.mActivity);
            }
            this.mResumed = resumed;
            if (this.mIsSplitBaseActivity) {
                if (Log.HWLog) {
                    Log.i(TAG, "setResumed, Base window splitted? " + this.mAllSubFinished);
                }
                if (resumed && !this.mAllSubFinished && reachSplitSize()) {
                    checkSplitHandler();
                    this.mSplitHandler.sendEmptyMessageDelayed(5, 300);
                } else if (this.mSplitHandler != null && this.mSplitHandler.hasMessages(5)) {
                    this.mSplitHandler.removeMessages(5);
                }
            }
            if (this.mIsSecondStageActivity && isSplitMode() && isBaseActivityNeedExit() && this.mSplitHandler != null) {
                this.mSplitHandler.sendEmptyMessage(3);
            }
        }
    }

    public void splitActivityFinish() {
        if (this.mIsSecondStageActivity && isSplitMode() && isBaseActivityNeedExit() && (isTopSplitActivity() || this.mBackPressed)) {
            if (isTaskRoot() || isJumpedActivity()) {
                this.mActivity.moveTaskToBack(true);
            } else {
                this.mActivity.finishAffinity();
            }
        }
        if (this.mAllContentGone || !(!this.mIsSecondStageActivity || isBaseActivityNeedExit() || this.mIsJumpActivity || isJumpedActivity())) {
            if (Log.HWLog) {
                Log.e(TAG, "Clear LastActivity.");
            }
            clearSplittableInfo();
        }
        releaseIfNeed();
        if (this.mIsSplitBaseActivity) {
            finishAllSubActivities();
        }
    }

    private boolean isTaskRoot() {
        List<RunningTaskInfo> tasks = ((ActivityManager) this.mActivity.getSystemService(FreezeScreenScene.ACTIVITY_PARAM)).getRunningTasks(1);
        if (tasks == null || tasks.isEmpty()) {
            return false;
        }
        RunningTaskInfo task = (RunningTaskInfo) tasks.get(0);
        return task.baseActivity != null && task.numActivities <= 1;
    }

    public void finishAllSubActivities() {
        if (this.mIsSplitBaseActivity) {
            clearEntryStack(true);
            clearSplittableInfo();
            this.mAllSubFinished = true;
        }
    }

    public void onSplitActivityDestroy() {
        releaseIfNeed();
    }

    private void releaseIfNeed() {
        if (!this.mRecycled) {
            this.mRecycled = true;
            if (this.mIsSplitBaseActivity) {
                mInstanceMap.clear();
                setCurrentSubIntent(null);
                setLastSplittableActivity(null);
            } else {
                mInstanceMap.remove(Integer.valueOf(this.mActivity.hashCode()));
            }
            if (this.mIsSplitBaseActivity && !isControllerShowing()) {
                resetFirsetTimeStart();
            }
        }
    }

    private static void resetFirsetTimeStart() {
        mFirstTimeStart = true;
    }

    public boolean isRtlLocale() {
        String currentLang = Locale.getDefault().getLanguage();
        if (Log.HWLog) {
            Log.i(TAG, "CurrentLang is " + currentLang);
        }
        if (currentLang.contains("ar") || currentLang.contains("fa") || currentLang.contains("iw")) {
            return true;
        }
        return currentLang.contains("ur");
    }

    private boolean isTopSplitActivity() {
        try {
            if (isTopSplitActivity(this.mToken)) {
                return true;
            }
        } catch (RemoteException e) {
            Log.e(TAG, "Get top activity FAIL!");
        }
        return false;
    }

    private void clearEntryStack(boolean includeSelf) {
        if (includeSelf) {
            try {
                clearEntryStack(null);
                return;
            } catch (RemoteException e) {
                Log.e(TAG, "clearEntryStack FAIL!");
                return;
            }
        }
        clearEntryStack(this.mToken);
    }

    public void setSplitDeviceSize(double landSplitLimit, double portSplitLimit) {
        if (this.mActivity.getIntent() != null) {
            this.mActivity.getIntent().putExtra(HwSplitUtils.EXTRAS_HWSPLIT_SIZE, new double[]{landSplitLimit, portSplitLimit});
        }
    }

    private int getTransCode(String name) {
        int transCode = 0;
        try {
            transCode = Class.forName("com.huawei.android.os.HwTransCodeEx").getField(name).getInt(null);
        } catch (ClassNotFoundException e) {
            Log.e(TAG, "getTransCode : ClassNotFoundException");
        } catch (NoSuchFieldException e2) {
            Log.e(TAG, "getTransCode : NoSuchFieldException");
        } catch (IllegalAccessException e3) {
            Log.e(TAG, "getTransCode : IllegalAccessException");
        } catch (IllegalArgumentException e4) {
            Log.e(TAG, "getTransCode : IllegalArgumentException");
        }
        return transCode;
    }

    private void setIntentInfo(Intent intent, String pkgName, Bundle bundle, boolean forLast) throws RemoteException {
        if (this.mTransCodeSetLast == 0) {
            this.mTransCodeSetLast = getTransCode("SET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeParcelable(intent, 0);
        data.writeString(pkgName);
        data.writeBundle(bundle);
        data.writeInt(forLast ? 1 : 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeSetLast, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private Intent getIntentInfo(String pkgName, boolean forLast) throws Exception {
        int i;
        if (this.mTransCodeGetLast == 0) {
            this.mTransCodeGetLast = getTransCode("GET_LAST_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeString(pkgName);
        if (forLast) {
            i = 1;
        } else {
            i = 0;
        }
        data.writeInt(i);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeGetLast, data, reply, 0);
        reply.readException();
        Parcelable[] p = reply.readParcelableArray(null);
        Intent intent = p[0];
        Bundle bundle = p[1];
        if (!(intent == null || bundle == null)) {
            intent.putExtras(bundle);
        }
        data.recycle();
        reply.recycle();
        return intent;
    }

    private void addToEntryStack(IBinder token, int resultCode, Intent resultData) throws RemoteException {
        if (this.mTransCodeAddEntry == 0) {
            this.mTransCodeAddEntry = getTransCode("ADD_TO_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeString(getBasePackage());
        data.writeStrongBinder(token);
        data.writeInt(resultCode);
        data.writeParcelable(resultData, 0);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeAddEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private void clearEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeClearEntry == 0) {
            this.mTransCodeClearEntry = getTransCode("CLEAR_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeString(getBasePackage());
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeClearEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private void removeFromEntryStack(IBinder token) throws RemoteException {
        if (this.mTransCodeRemoveEntry == 0) {
            this.mTransCodeRemoveEntry = getTransCode("REMOVE_FROM_ENTRY_STACK_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeString(getBasePackage());
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeRemoveEntry, data, reply, 0);
        reply.readException();
        data.recycle();
        reply.recycle();
    }

    private boolean isTopSplitActivity(IBinder token) throws RemoteException {
        if (this.mTransCodeIsTop == 0) {
            this.mTransCodeIsTop = getTransCode("IS_TOP_SPLIT_ACTIVITY_TRANSACTION");
        }
        Parcel data = Parcel.obtain();
        Parcel reply = Parcel.obtain();
        data.writeInterfaceToken("android.app.IActivityManager");
        data.writeString(getBasePackage());
        data.writeStrongBinder(token);
        ActivityManagerNative.getDefault().asBinder().transact(this.mTransCodeIsTop, data, reply, 0);
        reply.readException();
        boolean ret = reply.readInt() > 0;
        data.recycle();
        reply.recycle();
        return ret;
    }
}
