package com.android.systemui.lazymode;

import android.content.Context;
import android.graphics.Rect;
import android.os.SystemClock;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.VelocityTracker;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.analyze.BDReporter;
import fyusion.vislib.BuildConfig;

public class SlideTouchEvent {
    private Context mContext;
    private float[] mDownPoint = new float[2];
    private boolean mFlag = false;
    private boolean mIsSupport = true;
    private int mMaximumFlingVelocity;
    private int mMinimumFlingVelocity;
    private OnChangeListener mOnChangeListener = new OnChangeListener() {
        public void onChange(Object value) {
            SlideTouchEvent.this.mScreenZoomEnabled = ((Boolean) SystemUIObserver.get(6)).booleanValue();
            SlideTouchEvent.this.mZoomGestureEnabled = ((Boolean) SystemUIObserver.get(7)).booleanValue();
            if (!SlideTouchEvent.this.mScreenZoomEnabled) {
                SlideTouchEvent.this.quitLazyMode();
            } else if (SlideTouchEvent.this.mZoomGestureEnabled) {
                SlideTouchEvent.this.quitLazyMode();
            }
        }
    };
    private boolean mScreenZoomEnabled = true;
    private long mStartLazyModeTime = 0;
    private float mTriggerLazyMode;
    private VelocityTracker mVelocityTracker;
    private float mVerticalProhibit;
    private boolean mZoomGestureEnabled = false;

    private boolean isSupportSingleHand() {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x00d9 in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r14 = this;
        r9 = 1;
        r10 = 0;
        r11 = "SlideTouchEvent";
        r12 = "isSupportSingleHand()";
        com.android.systemui.utils.HwLog.i(r11, r12);
        r11 = android.view.WindowManagerGlobal.getWindowManagerService();
        r8 = r11.asBinder();
        r0 = 0;
        r5 = 0;
        r0 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r5 = android.os.Parcel.obtain();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        if (r8 == 0) goto L_0x005b;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
    L_0x001f:
        r11 = "android.view.IWindowManager";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r0.writeInterfaceToken(r11);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = 1991; // 0x7c7 float:2.79E-42 double:9.837E-321;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = 0;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r8.transact(r11, r0, r5, r12);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r5.readException();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r7 = r5.readInt();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = "SlideTouchEvent";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12.<init>();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r13 = "single_hand_switch = ";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = r12.append(r13);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = r12.append(r7);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = r12.toString();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        android.util.Log.i(r11, r12);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        if (r7 != r9) goto L_0x0059;
    L_0x004e:
        if (r0 == 0) goto L_0x0053;
    L_0x0050:
        r0.recycle();
    L_0x0053:
        if (r5 == 0) goto L_0x0058;
    L_0x0055:
        r5.recycle();
    L_0x0058:
        return r9;
    L_0x0059:
        r9 = r10;
        goto L_0x004e;
    L_0x005b:
        if (r0 == 0) goto L_0x0060;
    L_0x005d:
        r0.recycle();
    L_0x0060:
        if (r5 == 0) goto L_0x0065;
    L_0x0062:
        r5.recycle();
    L_0x0065:
        return r9;
    L_0x0066:
        r1 = move-exception;
        r9 = "SlideTouchEvent";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10.<init>();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = "read single_hand_switch exception. message = ";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = r1.getMessage();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.toString();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        android.util.Log.e(r9, r10);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = r14.mContext;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = r9.getResources();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = "single_hand_mode";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = "bool";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r12 = "androidhwext";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r3 = r9.getIdentifier(r10, r11, r12);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r6 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = "Single handle support state read exception: ";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r6.<init>(r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = "androidhwext::id=";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = r6.append(r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9.append(r3);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r4 = 1;
        if (r3 == 0) goto L_0x00b7;
    L_0x00ad:
        r9 = r14.mContext;	 Catch:{ NotFoundException -> 0x00df }
        r9 = r9.getResources();	 Catch:{ NotFoundException -> 0x00df }
        r4 = r9.getBoolean(r3);	 Catch:{ NotFoundException -> 0x00df }
    L_0x00b7:
        r9 = ", isSupport=";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = r6.append(r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9.append(r4);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = r6.toString();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = 0;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = com.android.systemui.utils.analyze.MonitorReporter.createMapInfo(r10, r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = 907033001; // 0x361039a9 float:2.1491235E-6 double:4.481338454E-315;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r9 = com.android.systemui.utils.analyze.MonitorReporter.createInfoIntent(r10, r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        com.android.systemui.utils.analyze.MonitorReporter.doMonitor(r9);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        if (r0 == 0) goto L_0x00d9;
    L_0x00d6:
        r0.recycle();
    L_0x00d9:
        if (r5 == 0) goto L_0x00de;
    L_0x00db:
        r5.recycle();
    L_0x00de:
        return r4;
    L_0x00df:
        r2 = move-exception;
        r9 = "SlideTouchEvent";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10.<init>();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = "read single_hand_switch exception use androidhwext method:";	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r11 = r1.getMessage();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.append(r11);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        r10 = r10.toString();	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        android.util.Log.e(r9, r10);	 Catch:{ RemoteException -> 0x0066, all -> 0x00ff }
        goto L_0x00b7;
    L_0x00ff:
        r9 = move-exception;
        if (r0 == 0) goto L_0x0105;
    L_0x0102:
        r0.recycle();
    L_0x0105:
        if (r5 == 0) goto L_0x010a;
    L_0x0107:
        r5.recycle();
    L_0x010a:
        throw r9;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.lazymode.SlideTouchEvent.isSupportSingleHand():boolean");
    }

    public SlideTouchEvent(Context context) {
        this.mContext = context;
        init();
    }

    private void init() {
        if (this.mContext != null) {
            this.mIsSupport = isSupportSingleHand();
            if (this.mIsSupport) {
                ViewConfiguration configuration = ViewConfiguration.get(this.mContext);
                this.mMinimumFlingVelocity = configuration.getScaledMinimumFlingVelocity();
                this.mMaximumFlingVelocity = configuration.getScaledMaximumFlingVelocity();
                this.mTriggerLazyMode = this.mContext.getResources().getDimension(R.dimen.navbar_lazy_mode_horizontal_threshhold);
                this.mVerticalProhibit = this.mContext.getResources().getDimension(R.dimen.navbar_lazy_mode_vertical_threshhold);
                this.mScreenZoomEnabled = ((Boolean) SystemUIObserver.get(6)).booleanValue();
                this.mZoomGestureEnabled = ((Boolean) SystemUIObserver.get(7)).booleanValue();
            }
        }
    }

    public void register(Context context) {
        SystemUIObserver.getObserver(6).addOnChangeListener(this.mOnChangeListener);
        SystemUIObserver.getObserver(7).addOnChangeListener(this.mOnChangeListener);
    }

    public void unRegister() {
        SystemUIObserver.getObserver(6).removeOnChangeListener(this.mOnChangeListener);
        SystemUIObserver.getObserver(7).removeOnChangeListener(this.mOnChangeListener);
    }

    public void handleTouchEvent(MotionEvent event) {
        HwLog.i("SlideTouchEvent", "handleTouchEvent:" + event);
        if (event != null && this.mIsSupport && this.mScreenZoomEnabled && !this.mZoomGestureEnabled && !"factory".equals(SystemProperties.get("ro.runmode", "normal"))) {
            if (this.mVelocityTracker == null) {
                this.mVelocityTracker = VelocityTracker.obtain();
            }
            this.mVelocityTracker.addMovement(event);
            switch (event.getActionMasked()) {
                case 0:
                    this.mFlag = true;
                    this.mDownPoint[0] = event.getX();
                    this.mDownPoint[1] = event.getY();
                    break;
                case 1:
                    if (this.mFlag) {
                        this.mFlag = false;
                        VelocityTracker velocityTracker = this.mVelocityTracker;
                        velocityTracker.computeCurrentVelocity(1000, (float) this.mMaximumFlingVelocity);
                        float velocityX = velocityTracker.getXVelocity(event.getPointerId(0));
                        HwLog.i("SlideTouchEvent", "vel=" + Math.abs(velocityX) + ", MinimumFlingVelocity=" + this.mMinimumFlingVelocity);
                        if (Math.abs(velocityX) > ((float) this.mMinimumFlingVelocity)) {
                            int historySize = event.getHistorySize();
                            int i = 0;
                            while (i < historySize + 1) {
                                float distanceX = this.mDownPoint[0] - (i < historySize ? event.getHistoricalX(i) : event.getX());
                                float distanceY = this.mDownPoint[1] - (i < historySize ? event.getHistoricalY(i) : event.getY());
                                if (Math.abs(distanceY) > Math.abs(distanceX) || Math.abs(distanceY) > this.mVerticalProhibit) {
                                    Log.i("SlideTouchEvent", "Sliding distanceY > distancex, " + distanceY + ", " + distanceX);
                                    return;
                                }
                                if (Math.abs(distanceX) <= this.mTriggerLazyMode) {
                                    Log.i("SlideTouchEvent", "Sliding distance is too short, can not trigger the lazy mode");
                                } else if (1 == this.mContext.getResources().getConfiguration().orientation) {
                                    startLazyMode(distanceX);
                                }
                                i++;
                            }
                            break;
                        }
                    }
                    break;
                case 6:
                    if (event.getActionIndex() == 0) {
                        this.mFlag = false;
                        break;
                    }
                    break;
            }
        }
    }

    private void startLazyMode(float distanceX) {
        String str = Global.getString(this.mContext.getContentResolver(), "single_hand_mode");
        HwLog.i("SingleHand", "start lazy mode str: " + str + " distanceX: " + distanceX);
        if (distanceX > 0.0f && TextUtils.isEmpty(str)) {
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "left");
            BDReporter.e(this.mContext, 9, "START_LAZY_MODE:0");
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < 0.0f && TextUtils.isEmpty(str)) {
            Global.putString(this.mContext.getContentResolver(), "single_hand_mode", "right");
            BDReporter.e(this.mContext, 9, "START_LAZY_MODE:1");
            this.mStartLazyModeTime = SystemClock.uptimeMillis();
        }
        if (distanceX < 0.0f && str != null && str.contains("left")) {
            quitLazyMode();
            BDReporter.e(this.mContext, 12, "QUIT_LAZY_MODE:0");
            reportLazyModeUsingTime();
        }
        if (distanceX > 0.0f && str != null && str.contains("right")) {
            quitLazyMode();
            BDReporter.e(this.mContext, 12, "QUIT_LAZY_MODE:1");
            reportLazyModeUsingTime();
        }
    }

    private void reportLazyModeUsingTime() {
        if (this.mStartLazyModeTime > 0 && SystemClock.uptimeMillis() - this.mStartLazyModeTime >= 3600000) {
            BDReporter.c(this.mContext, 46);
        }
    }

    public static int getLazyState(Context context) {
        String str = Global.getString(context.getContentResolver(), "single_hand_mode");
        if (TextUtils.isEmpty(str)) {
            return 0;
        }
        if (str.contains("left")) {
            return 1;
        }
        if (str.contains("right")) {
            return 2;
        }
        return 0;
    }

    public static Rect getScreenshotRect(Context context) {
        Display display = ((WindowManager) context.getSystemService("window")).getDefaultDisplay();
        DisplayMetrics displayMetrics = new DisplayMetrics();
        display.getRealMetrics(displayMetrics);
        int state = getLazyState(context);
        if (1 == state) {
            return new Rect(0, (int) (((float) displayMetrics.heightPixels) * 0.25f), (int) (((float) displayMetrics.widthPixels) * 0.75f), displayMetrics.heightPixels);
        }
        if (2 == state) {
            return new Rect((int) (((float) displayMetrics.widthPixels) * 0.25f), (int) (((float) displayMetrics.heightPixels) * 0.25f), displayMetrics.widthPixels, displayMetrics.heightPixels);
        }
        return null;
    }

    public static boolean isLazyMode(Context context) {
        return getLazyState(context) != 0;
    }

    private void quitLazyMode() {
        HwLog.i("SlideTouchEvent", "quitLazyMode");
        Global.putString(this.mContext.getContentResolver(), "single_hand_mode", BuildConfig.FLAVOR);
    }
}
