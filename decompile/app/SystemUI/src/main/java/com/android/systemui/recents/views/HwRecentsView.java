package com.android.systemui.recents.views;

import android.app.ActivityManager;
import android.content.Context;
import android.content.res.Configuration;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.MeasureSpec;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.widget.FrameLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;
import com.android.internal.util.MemInfoReader;
import com.android.systemui.R;
import com.android.systemui.RunningState;
import com.android.systemui.recents.HwRecentTaskRemove;
import com.android.systemui.recents.HwRecentsHelper;
import com.android.systemui.recents.events.EventBus;
import com.android.systemui.recents.events.activity.DismissRecentsToHomeAnimationStarted;
import com.android.systemui.recents.events.activity.EnterRecentsWindowAnimationCompletedEvent;
import com.android.systemui.recents.events.activity.HideStackActionButtonEvent;
import com.android.systemui.recents.events.activity.LaunchTaskEvent;
import com.android.systemui.recents.events.activity.MultiWindowStateChangedEvent;
import com.android.systemui.recents.events.activity.ShowStackActionButtonEvent;
import com.android.systemui.recents.events.ui.AllTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.DismissAllTaskViewsEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEndedEvent;
import com.android.systemui.recents.events.ui.DraggingInRecentsEvent;
import com.android.systemui.recents.events.ui.HwTaskViewsDismissedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragDropTargetChangedEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragEndEvent;
import com.android.systemui.recents.events.ui.dragndrop.DragStartEvent;
import com.android.systemui.recents.misc.SystemServicesProxy;
import com.android.systemui.recents.model.TaskStack;
import com.android.systemui.recents.views.HwRecentsClearBox.CallBack;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import fyusion.vislib.BuildConfig;

public class HwRecentsView extends RecentsView implements CallBack {
    private static boolean mIsfactory = "factory".equals(SystemProperties.get("ro.runmode", "normal"));
    private int clearBoxAnimateInTime;
    private int clearBoxAnimateOutTime;
    private boolean inMultiWindow;
    private boolean mDoingShowAnimation;
    private Handler mHandler;
    private HandlerThread mHandlerThread;
    private long mInitMemorySize;
    private boolean mIsPhone;
    private int mLastDeviceOrientation;
    private View mMemoryLine;
    private TextView mMemorySubText1;
    private TextView mMemorySubText2;
    private long mOldAvailMemorySize;
    private HwRecentsClearBox mRecentsClearBox;
    private long mRemoveAllClickTime;
    private int mScreenHeight;
    private int mScreenWidth;
    private Handler mSubThreadHandler;

    private class SubThreadHandler extends Handler {
        public SubThreadHandler(Looper looper) {
            super(looper);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 8:
                    HwRecentsView.this.updateMemoryInfo();
                    removeMessages(8);
                    return;
                case 32:
                    HwRecentsView.this.mOldAvailMemorySize = HwRecentsHelper.getAvailableMemory(HwRecentsView.this.getContext());
                    return;
                default:
                    return;
            }
        }
    }

    public HwRecentsView(Context context) {
        this(context, null);
    }

    public HwRecentsView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public HwRecentsView(Context context, AttributeSet attrs, int defStyleAttr) {
        this(context, attrs, defStyleAttr, 0);
    }

    public HwRecentsView(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        this.mMemorySubText1 = null;
        this.mMemorySubText2 = null;
        this.mMemoryLine = null;
        this.mInitMemorySize = 0;
        this.mOldAvailMemorySize = 0;
        this.clearBoxAnimateInTime = 200;
        this.clearBoxAnimateOutTime = 300;
        this.mScreenHeight = 1920;
        this.mScreenWidth = 1080;
        this.mLastDeviceOrientation = 1;
        this.mDoingShowAnimation = false;
        this.mHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 7:
                        HwRecentsView.this.setMemoryText(msg.obj.toString());
                        return;
                    case 9:
                        HwRecentsView.this.showMemoryToast(HwRecentsView.this.getContext());
                        return;
                    default:
                        return;
                }
            }
        };
        this.mIsPhone = context.getResources().getBoolean(R.bool.config_task_manager);
        this.mLastDeviceOrientation = getResources().getConfiguration().orientation;
        initClearBox();
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mHandlerThread = new HandlerThread("handle_thread");
        this.mHandlerThread.start();
        this.mSubThreadHandler = new SubThreadHandler(this.mHandlerThread.getLooper());
        updateScreenSize();
    }

    protected void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        updateScreenSize();
        int orientation = this.mScreenHeight > this.mScreenWidth ? 1 : 2;
        if (this.mLastDeviceOrientation != orientation) {
            this.mLastDeviceOrientation = orientation;
            initClearBox();
        }
    }

    protected void initClearBox() {
        if (SystemUiUtil.isLandscape()) {
            this.mLastDeviceOrientation = 2;
        } else {
            this.mLastDeviceOrientation = 1;
        }
        HwLog.i("HwRecentsView", "initClearBox: " + this.mLastDeviceOrientation);
        updateScreenSize();
        this.mDoingShowAnimation = false;
        removeView(this.mRecentsClearBox);
        if (this.mLastDeviceOrientation == 1) {
            this.mRecentsClearBox = (HwRecentsClearBox) LayoutInflater.from(getContext()).inflate(R.layout.hw_status_bar_recent_horizontal_clear_all, this, false);
            addView(this.mRecentsClearBox);
        } else {
            this.mRecentsClearBox = (HwRecentsClearBox) LayoutInflater.from(getContext()).inflate(R.layout.hw_status_bar_recent_vertical_clear_all, this, false);
            addView(this.mRecentsClearBox, new LayoutParams(-2, this.mScreenHeight));
        }
        this.mRecentsClearBox.setCallback(this);
        this.mRecentsClearBox.requestLayout();
        setMemoryText(HwRecentsHelper.getLastMemoryInfo(getContext()));
        if (this.mStack != null && this.mStack.getTaskCount() <= 0) {
            updateClearBox(false, false);
        }
    }

    public void updateScreenSize() {
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRealMetrics(displayMetrics);
        this.mScreenHeight = displayMetrics.heightPixels;
        this.mScreenWidth = displayMetrics.widthPixels;
    }

    public void updateStack(TaskStack stack, boolean setStackViewTasks) {
        super.updateStack(stack, setStackViewTasks);
    }

    public void showEmptyView(int msgResId) {
        super.showEmptyView(msgResId);
        this.mRecentsClearBox.bringToFront();
    }

    public void hideEmptyView() {
        super.hideEmptyView();
        this.mRecentsClearBox.bringToFront();
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mHandlerThread != null) {
            this.mHandlerThread.quit();
        }
    }

    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = MeasureSpec.getSize(heightMeasureSpec);
        if (this.mStack != null && this.mStack.getTaskCount() > 0) {
            measureChild(this.mRecentsClearBox, MeasureSpec.makeMeasureSpec(width, Integer.MIN_VALUE), MeasureSpec.makeMeasureSpec(height, Integer.MIN_VALUE));
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        HwLog.i("HwRecentsView", "onLayout left=" + left + ";top=" + top + ";right=" + right + ";bottom=" + bottom + ";mScreenWidth=" + this.mScreenWidth + ";mScreenHeight=" + this.mScreenHeight);
        boolean isFullScreen = right - left == this.mScreenWidth && bottom - top == this.mScreenHeight;
        if (isFullScreen) {
            int topBottomInsets = this.mSystemInsets.top + this.mSystemInsets.bottom;
            int leftRightInsets = this.mSystemInsets.left + this.mSystemInsets.right;
            int childHeight = this.mRecentsClearBox.getMeasuredHeight();
            int childWidth = this.mRecentsClearBox.getMeasuredWidth();
            int childTop = (this.mSystemInsets.top + top) + Math.max(0, ((bottom - top) - topBottomInsets) - childHeight);
            int childLeft = (this.mSystemInsets.left + left) + Math.max(0, ((right - left) - leftRightInsets) - childWidth);
            HwLog.i("HwRecentsView", "onLayout mLastDeviceOrientation=" + this.mLastDeviceOrientation + ";childTop=" + childTop + ";childLeft=" + childLeft + ";childHeight=" + childHeight + ";childWidth=" + childWidth);
            if (this.mLastDeviceOrientation == 1) {
                this.mRecentsClearBox.layout(0, childTop, this.mScreenWidth, this.mScreenHeight);
            } else {
                this.mRecentsClearBox.layout(childLeft, 0, this.mScreenWidth, this.mScreenHeight);
            }
        }
        super.onLayout(changed, left, top, right, bottom);
    }

    public void removeAll() {
        PerfDebugUtils.beginSystraceSection("HwRecentsView.removeAll");
        this.mRemoveAllClickTime = System.currentTimeMillis();
        EventBus.getDefault().send(new DismissAllTaskViewsEvent());
        HwRecentTaskRemove.getInstance(getContext()).setRemoveTaskCondition(this.mOldAvailMemorySize, this.mInitMemorySize);
        PerfDebugUtils.endSystraceSection();
    }

    public long getRemoveAllClickTime() {
        return this.mRemoveAllClickTime;
    }

    public void updateClearBox(boolean show, boolean animated) {
        int i = 8;
        if (this.mStack != null && this.mStack.getTaskCount() <= 0) {
            show = false;
            animated = false;
        }
        final boolean z = this.inMultiWindow ? false : show;
        if (this.inMultiWindow) {
            animated = false;
        }
        HwLog.i("HwRecentsView", "updateClearBox show=" + z + ",animated=" + animated);
        if (!animated) {
            HwRecentsClearBox hwRecentsClearBox = this.mRecentsClearBox;
            if (z) {
                i = 0;
            }
            hwRecentsClearBox.setVisibility(i);
            this.mDoingShowAnimation = false;
        } else if (this.mDoingShowAnimation) {
            HwLog.i("HwRecentsView", "mRecentsClearBox is showing, ignore animation");
        } else {
            if (z) {
                setMemoryText(HwRecentsHelper.getLastMemoryInfo(getContext()));
                if (this.mIsPhone) {
                    this.mSubThreadHandler.sendEmptyMessageDelayed(8, 150);
                }
                this.mSubThreadHandler.removeMessages(32);
                this.mSubThreadHandler.sendEmptyMessageDelayed(32, 500);
            }
            if (this.mRecentsClearBox.getVisibility() == 0) {
                HwLog.i("HwRecentsView", "mRecentsClearBox is VISIBLE, ignore animation");
                return;
            }
            float f;
            this.mDoingShowAnimation = true;
            if (z) {
                f = 0.0f;
            } else {
                f = 1.0f;
            }
            Animation animation = new AlphaAnimation(f, z ? 1.0f : 0.0f);
            animation.setDuration((long) (z ? this.clearBoxAnimateInTime : this.clearBoxAnimateOutTime));
            animation.setAnimationListener(new AnimationListener() {
                public void onAnimationStart(Animation animation) {
                }

                public void onAnimationEnd(Animation animation) {
                    HwRecentsView.this.mRecentsClearBox.setVisibility(z ? 0 : 8);
                    HwRecentsView.this.mDoingShowAnimation = false;
                }

                public void onAnimationRepeat(Animation animation) {
                }
            });
            this.mRecentsClearBox.startAnimation(animation);
        }
    }

    private void setMemoryText(String text) {
        if (this.mRecentsClearBox != null) {
            this.mMemorySubText1 = (TextView) this.mRecentsClearBox.findViewById(R.id.memory_subtext1);
            this.mMemorySubText2 = (TextView) this.mRecentsClearBox.findViewById(R.id.memory_subtext2);
            this.mMemoryLine = this.mRecentsClearBox.findViewById(R.id.memory_vertical_line);
            if (this.mMemorySubText1 == null || this.mMemorySubText2 == null || this.mMemoryLine == null) {
                Log.e("HwRecentsView", "not find mMemorySubText1/mMemorySubText2/mMemoryLine ");
                return;
            }
            String[] ss = text.split("\\|");
            if (2 != ss.length) {
                this.mMemorySubText1.setText(text);
                this.mMemorySubText2.setText(BuildConfig.FLAVOR);
                this.mMemoryLine.setVisibility(4);
                Log.w("HwRecentsView", "Memory text is not valid,can not split to double text ");
                return;
            }
            this.mMemorySubText1.setText(ss[0]);
            this.mMemorySubText2.setText(ss[1]);
            this.mMemoryLine.setVisibility(0);
        }
    }

    private long updateMemoryInfo() {
        long mMemorySize = getAvaildMemSize(getContext());
        this.mInitMemorySize = mMemorySize;
        double mAvail = ((double) this.mInitMemorySize) / 1048576.0d;
        int memoryId = getMemoryId(mAvail, ((double) HwRecentsHelper.getTotalMemorySize(getContext())) / 1024.0d);
        String memoryText = getContext().getString(memoryId, new Object[]{SystemUiUtil.getMemoryString(mAvail), SystemUiUtil.getMemoryString(mTotal)});
        HwRecentsHelper.saveLastMemoryInfo(getContext(), memoryText);
        this.mHandler.sendMessage(this.mHandler.obtainMessage(7, memoryText));
        return mMemorySize;
    }

    private static long getAvaildMemSize(Context context) {
        try {
            MemInfoReader memInfoReader = new MemInfoReader();
            memInfoReader.readMemInfo();
            RunningState runningtate = new RunningState();
            runningtate.update(context, (ActivityManager) context.getSystemService("activity"));
            return (memInfoReader.getFreeSize() + memInfoReader.getCachedSize()) + runningtate.mBackgroundProcessMemory;
        } catch (Exception e) {
            Log.e("HwRecentsView", e.toString(), e);
            return 0;
        }
    }

    private int getMemoryId(double avail, double total) {
        if (((int) avail) < 1024 && ((int) total) >= 1024) {
            return R.string.TaskManager_Other_MemoryNew;
        }
        if (((int) avail) >= 1024 && ((int) total) >= 1024) {
            return R.string.TaskManager_Other_MemoryNew2;
        }
        if (((int) avail) < 1024 && ((int) total) < 1024) {
            return R.string.TaskManager_Other_MemoryNew1;
        }
        Log.w("HwRecentsView", "memory data is not valid,avail must less than total!");
        return R.string.TaskManager_Other_MemoryNew2;
    }

    public void showMemoryToast(Context context) {
        showMemoryToast(context, this.mOldAvailMemorySize, this.mInitMemorySize);
    }

    public static void showMemoryToast(Context context, long oldAvailMemorySize, long initMemorySize) {
        final Context context2 = context;
        final long j = initMemorySize;
        final long j2 = oldAvailMemorySize;
        SystemUIThread.runAsync(new SimpleAsyncTask() {
            Context appContext = context2.getApplicationContext();
            int availableMemory;
            long mInitMemorySize = j;
            int memoryToastId;
            int totalReleasedMemorySize;

            public boolean runInThread() {
                int i;
                long newAvailMemorySize = HwRecentsHelper.getAvailableMemory(this.appContext);
                Log.d("HwRecentsView", "newAvailMemorySize:" + newAvailMemorySize + ",oldAvailMemorySize:" + j2);
                this.totalReleasedMemorySize = ((int) (newAvailMemorySize - j2)) / 1048576;
                if (this.totalReleasedMemorySize > 0) {
                    i = this.totalReleasedMemorySize;
                } else {
                    i = 0;
                }
                this.totalReleasedMemorySize = i;
                if (this.mInitMemorySize <= 0) {
                    this.mInitMemorySize = HwRecentsView.getAvaildMemSize(this.appContext);
                }
                this.availableMemory = (int) ((this.mInitMemorySize / 1048576) + ((long) this.totalReleasedMemorySize));
                int themeID = this.appContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                if (themeID != 0) {
                    this.appContext.setTheme(themeID);
                }
                if (this.availableMemory > ((int) (HwRecentsHelper.getTotalMemorySize(this.appContext) / 1024))) {
                    Log.i("HwRecentsView", "showMemoryToast:: available memory less than total memory exception!");
                    this.totalReleasedMemorySize = 0;
                }
                Log.d("HwRecentsView", "totalReleasedMemorySize:" + this.totalReleasedMemorySize + ",availableMemory:" + this.availableMemory);
                this.memoryToastId = HwRecentsView.getReleasedMemoryId(this.totalReleasedMemorySize, this.availableMemory);
                return true;
            }

            public void runInUI() {
                if (this.totalReleasedMemorySize == 0) {
                    if (!HwRecentsView.mIsfactory) {
                        Toast.makeText(this.appContext, R.string.status_bar_remove_no_recent_apps, 1).show();
                    }
                } else if (!SystemProperties.get("ro.runmode", "normal").equals("factory")) {
                    Toast.makeText(this.appContext, this.appContext.getString(this.memoryToastId, new Object[]{SystemUiUtil.getMemoryString((double) this.totalReleasedMemorySize), SystemUiUtil.getMemoryString((double) this.availableMemory)}), 1).show();
                }
            }
        });
    }

    private static int getReleasedMemoryId(int totalReleasedMemory, int availableMemory) {
        if (totalReleasedMemory < 1024 && availableMemory < 1024) {
            return R.string.TaskManager_Toast_ClearMemory;
        }
        if (totalReleasedMemory < 1024 && availableMemory >= 1024) {
            return R.string.TaskManager_Toast_ClearMemory1;
        }
        if (totalReleasedMemory >= 1024 && availableMemory >= 1024) {
            return R.string.TaskManager_Toast_ClearMemory2;
        }
        Log.w("HwRecentsView", "memory data is not valid,releasedMemory must less than availableMemory!");
        return R.string.TaskManager_Toast_ClearMemory2;
    }

    public final void onBusEvent(HwTaskViewsDismissedEvent event) {
        if (this.mIsPhone) {
            this.mSubThreadHandler.sendEmptyMessageDelayed(8, 150);
        }
    }

    public final void onBusEvent(EnterRecentsWindowAnimationCompletedEvent event) {
        super.onBusEvent(event);
        HwLog.i("HwRecentsView", "onBusEvent: EnterRecentsWindowAnimationCompletedEvent");
        if (event.launchState == null || !event.launchState.launchedFromApp) {
            this.clearBoxAnimateInTime = 200;
        } else {
            this.clearBoxAnimateInTime = 350;
        }
        updateClearBox(true, true);
    }

    public final void onBusEvent(AllTaskViewsDismissedEvent event) {
        super.onBusEvent(event);
        this.mRecentsClearBox.setVisibility(8);
    }

    public final void onBusEvent(LaunchTaskEvent event) {
        super.onBusEvent(event);
    }

    public void onBusEvent(DismissRecentsToHomeAnimationStarted event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DragStartEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DragDropTargetChangedEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DragEndEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DraggingInRecentsEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DraggingInRecentsEndedEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(DismissAllTaskViewsEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(ShowStackActionButtonEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(HideStackActionButtonEvent event) {
        super.onBusEvent(event);
    }

    public final void onBusEvent(MultiWindowStateChangedEvent event) {
        super.onBusEvent(event);
        HwLog.i("HwRecentsView", "onBusEvent: MultiWindowStateChangedEvent");
        this.inMultiWindow = event.inMultiWindow;
        if (SystemServicesProxy.getInstance(getContext()).isRecentsActivityVisible()) {
            boolean z;
            if (this.inMultiWindow) {
                z = false;
            } else {
                z = true;
            }
            updateClearBox(z, false);
        }
    }
}
