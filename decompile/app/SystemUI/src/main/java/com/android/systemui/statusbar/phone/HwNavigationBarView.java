package com.android.systemui.statusbar.phone;

import android.app.ActivityManager;
import android.app.StatusBarManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.provider.Settings.Global;
import android.provider.Settings.System;
import android.util.AttributeSet;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.IWindowManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.view.WindowManager.LayoutParams;
import android.view.WindowManagerGlobal;
import android.view.animation.TranslateAnimation;
import com.android.keyguard.KeyguardUpdateMonitor;
import com.android.systemui.HwSystemUIApplication;
import com.android.systemui.R;
import com.android.systemui.observer.ObserverItem.OnChangeListener;
import com.android.systemui.observer.SystemUIObserver;
import com.android.systemui.stackdivider.Divider;
import com.android.systemui.statusbar.policy.HwSplitScreenArrowView;
import com.android.systemui.tint.TintManager;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SecurityCodeCheck;
import com.android.systemui.utils.SystemUIThread;
import com.android.systemui.utils.SystemUIThread.SimpleAsyncTask;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.UserSwitchUtils;
import com.android.systemui.utils.analyze.BDReporter;
import com.android.systemui.utils.analyze.MonitorReporter;
import com.android.systemui.utils.analyze.PerfDebugUtils;
import java.lang.reflect.Array;
import java.util.HashMap;

public class HwNavigationBarView extends NavigationBarView implements OnGestureListener {
    private static final boolean NAVBAR_REMOVABLE = SystemProperties.getBoolean("ro.config.hw_front_fp_navi", false);
    private int[] buttons = new int[]{R.id.back, R.id.home, R.id.recent_apps, R.id.menu, R.id.ime_switcher, R.id.hide, R.id.expand, R.id.close};
    OnClickListener mCloseButtonClick = new OnClickListener() {
        public void onClick(View v) {
            HwNavigationBarView.this.closeStatusBarPanels();
            HwNavigationBarView.this.getCloseButton().setVisibility(8);
            HwNavigationBarView.this.getExpandButton().setVisibility(0);
            BDReporter.e(HwNavigationBarView.this.getContext(), 18, "status:close");
        }
    };
    private View mCurrentViewStub;
    private DelegateViewHelper mDelegateHelper = new DelegateViewHelper(this);
    private GestureDetector mDetector = new GestureDetector(this);
    OnChangeListener mEnableListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNavigationBarView.this.mEnableNavBar = ((Boolean) SystemUIObserver.get(18)).booleanValue();
            if (HwNavigationBarView.NAVBAR_REMOVABLE) {
                HwNavigationBarView.this.updateNavigationBar(!HwNavigationBarView.this.mEnableNavBar);
            }
        }
    };
    private boolean mEnableNavBar = true;
    OnClickListener mExpandButtonClick = new OnClickListener() {
        public void onClick(View v) {
            if (!HwPhoneStatusBar.getInstance().panelsEnabled()) {
                return;
            }
            if (HwPhoneStatusBar.getInstance().isStatusBarHidden()) {
                HwPhoneStatusBar.getInstance().showStatusBar();
                return;
            }
            BDReporter.e(HwNavigationBarView.this.getContext(), 18, "status:expand");
            HwNavigationBarView.this.expandStatusBarPanels();
            HwNavigationBarView.this.getExpandButton().setVisibility(8);
            HwNavigationBarView.this.getCloseButton().setVisibility(0);
        }
    };
    OnChangeListener mFPNavBarTypeListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNavigationBarView.this.mIsInFPThirdKeyType = ((Boolean) SystemUIObserver.get(24)).booleanValue();
            HwLog.i("HwNavigationBarView", "mFPNavBarTypeListener::onChange:mIsInFPThirdKeyType=" + HwNavigationBarView.this.mIsInFPThirdKeyType);
            if (HwNavigationBarView.this.mIsInScreenPinning) {
                HwNavigationBarView.this.updateNavigationBar(HwNavigationBarView.this.mIsInFPThirdKeyType);
            }
        }
    };
    private HashMap<Integer, View> mHashmap = new HashMap();
    OnClickListener mHideButtonClick = new OnClickListener() {
        public void onClick(View v) {
            HwPhoneStatusBar.getInstance().onHideButtonClick();
        }
    };
    private boolean mHideNavibar = false;
    private BroadcastReceiver mHideNavibarReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                String action = intent.getAction();
                if ("com.huawei.mirrorlink.action.ACTION_HIDE_NAVIBAR".equals(action)) {
                    HwNavigationBarView.this.mHideNavibar = intent.getBooleanExtra("HIDE_NAVIGATION_BAR", false);
                    HwNavigationBarView.this.updateNavigationBar(HwNavigationBarView.this.mHideNavibar);
                    HwLog.i("HwNavigationBarView", "mHideNavibarReceiver::onReceive:action=" + action + ", mHideNavibar=" + HwNavigationBarView.this.mHideNavibar);
                }
            }
        }
    };
    OnChangeListener mHideVirtualKeyListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNavigationBarView.this.updateHideButton();
        }
    };
    private Drawable mHomeIcon;
    private Drawable mHomeLandIcon;
    private boolean mIsInFPThirdKeyType = false;
    private boolean mIsInScreenPinning = false;
    private boolean mIsSupportNavigationBarSlide = false;
    private int mOldVirtualKeyType = -1;
    ViewStub[][] mPadViewStubs = ((ViewStub[][]) Array.newInstance(ViewStub.class, new int[]{8, 3}));
    private BroadcastReceiver mScreenPinningReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if (SecurityCodeCheck.isValidIntentAndAction(intent)) {
                if ("com.huawei.android.systemui.screenpinning".equals(intent.getAction())) {
                    boolean enterScreenPinning = intent.getBooleanExtra("screenpinning_state", false);
                    HwLog.i("HwNavigationBarView", "mScreenPinningReceiver::enterScreenPinning=" + enterScreenPinning);
                    HwNavigationBarView.this.handleScreenPinningStateChanged(enterScreenPinning);
                }
                return;
            }
            HwLog.e("HwNavigationBarView", "mScreenPinningReceiver:: intent or action is null");
        }
    };
    private int mSlidingDirection = 2;
    private HwSplitScreenArrowView mSplitScreenArrowViewLand;
    private HwSplitScreenArrowView mSplitScreenArrowViewPort;
    StatusBarManager mStatusBarManager = ((StatusBarManager) getContext().getSystemService("statusbar"));
    private HashMap<Integer, View> mTargetViewMap = new HashMap();
    private ViewStub[] mViewStubs = new ViewStub[8];
    private int mVirtualKeyPosition = 1;
    OnChangeListener mVirtualKeyPositionListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNavigationBarView.this.updateNavButtons();
        }
    };
    private int mVirtualKeyType = 0;
    OnChangeListener mVirtualKeyTypeListener = new OnChangeListener() {
        public void onChange(Object value) {
            HwNavigationBarView.this.updateNavButtons();
        }
    };
    private IWindowManager mWindowManagerService = WindowManagerGlobal.getWindowManagerService();

    private boolean changeNaviBarStatus(boolean r10) {
        /* JADX: method processing error */
/*
Error: jadx.core.utils.exceptions.JadxRuntimeException: Can't find block by offset: 0x007f in list []
	at jadx.core.utils.BlockUtils.getBlockByOffset(BlockUtils.java:43)
	at jadx.core.dex.instructions.IfNode.initBlocks(IfNode.java:60)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.initBlocksInIfNodes(BlockFinish.java:48)
	at jadx.core.dex.visitors.blocksmaker.BlockFinish.visit(BlockFinish.java:33)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:31)
	at jadx.core.dex.visitors.DepthTraversal.visit(DepthTraversal.java:17)
	at jadx.core.ProcessClass.process(ProcessClass.java:37)
	at jadx.core.ProcessClass.processDependencies(ProcessClass.java:59)
	at jadx.core.ProcessClass.process(ProcessClass.java:42)
	at jadx.api.JadxDecompiler.processClass(JadxDecompiler.java:306)
	at jadx.api.JavaClass.decompile(JavaClass.java:62)
	at jadx.api.JadxDecompiler$1.run(JadxDecompiler.java:199)
*/
        /*
        r9 = this;
        r5 = 1;
        r6 = 0;
        r0 = android.os.Parcel.obtain();
        r3 = android.os.Parcel.obtain();
        r4 = "HwNavigationBarView";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7 = new java.lang.StringBuilder;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7.<init>();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r8 = "changeNaviBarStatus:";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7 = r7.append(r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7 = r7.append(r10);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7 = r7.toString();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        com.android.systemui.utils.HwLog.i(r4, r7);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4 = "android.view.IWindowManager";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r0.writeInterfaceToken(r4);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        if (r10 == 0) goto L_0x004b;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
    L_0x002c:
        r4 = r5;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
    L_0x002d:
        r0.writeInt(r4);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4 = r9.mWindowManagerService;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        if (r4 == 0) goto L_0x0040;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
    L_0x0034:
        r4 = r9.mWindowManagerService;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4 = r4.asBinder();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r7 = 204; // 0xcc float:2.86E-43 double:1.01E-321;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r8 = 0;	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4.transact(r7, r0, r3, r8);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
    L_0x0040:
        if (r0 == 0) goto L_0x0045;
    L_0x0042:
        r0.recycle();
    L_0x0045:
        if (r3 == 0) goto L_0x004a;
    L_0x0047:
        r3.recycle();
    L_0x004a:
        return r5;
    L_0x004b:
        r4 = r6;
        goto L_0x002d;
    L_0x004d:
        r2 = move-exception;
        r4 = "HwNavigationBarView";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r5 = "remote exception.";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        android.util.Log.e(r4, r5);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4 = r2.getMessage();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r9.triggerNaviBarShowError(r4, r10);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        if (r0 == 0) goto L_0x0063;
    L_0x0060:
        r0.recycle();
    L_0x0063:
        if (r3 == 0) goto L_0x0068;
    L_0x0065:
        r3.recycle();
    L_0x0068:
        return r6;
    L_0x0069:
        r1 = move-exception;
        r4 = "HwNavigationBarView";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r5 = "remote remoteexception.";	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        android.util.Log.e(r4, r5);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r4 = r1.getMessage();	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        r9.triggerNaviBarShowError(r4, r10);	 Catch:{ RemoteException -> 0x0069, Exception -> 0x004d, all -> 0x0085 }
        if (r0 == 0) goto L_0x007f;
    L_0x007c:
        r0.recycle();
    L_0x007f:
        if (r3 == 0) goto L_0x0084;
    L_0x0081:
        r3.recycle();
    L_0x0084:
        return r6;
    L_0x0085:
        r4 = move-exception;
        if (r0 == 0) goto L_0x008b;
    L_0x0088:
        r0.recycle();
    L_0x008b:
        if (r3 == 0) goto L_0x0090;
    L_0x008d:
        r3.recycle();
    L_0x0090:
        throw r4;
        */
        throw new UnsupportedOperationException("Method not decompiled: com.android.systemui.statusbar.phone.HwNavigationBarView.changeNaviBarStatus(boolean):boolean");
    }

    public HwNavigationBarView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mButtonDisatchers.put(R.id.hide, new ButtonDispatcher(R.id.hide));
        this.mButtonDisatchers.put(R.id.expand, new ButtonDispatcher(R.id.expand));
        this.mButtonDisatchers.put(R.id.close, new ButtonDispatcher(R.id.close));
        updateSplitScreenArrowView();
        this.mIsSupportNavigationBarSlide = SystemUiUtil.isShowNavigationBarFootView();
    }

    public boolean isHideNavibar() {
        return this.mHideNavibar;
    }

    public void showNavigationBarAlignLeftWhenLand() {
        if (getCurrentView() != null) {
            final ViewGroup navButtons = (ViewGroup) getCurrentView().findViewById(R.id.nav_buttons);
            final ViewGroup mirrorLinkButtons = (ViewGroup) getCurrentView().findViewById(R.id.mirrorlink_buttons);
            if (mirrorLinkButtons != null && navButtons != null) {
                SystemUIThread.runAsync(new SimpleAsyncTask() {
                    boolean isNavibarAlignLeftWhenLand = false;

                    public boolean runInThread() {
                        this.isNavibarAlignLeftWhenLand = SystemUiUtil.getNavibarAlignLeftWhenLand(HwNavigationBarView.this.mContext);
                        return super.runInThread();
                    }

                    public void runInUI() {
                        HwLog.i("HwNavigationBarView", "showNavigationBarAlignLeftWhenLand:" + this.isNavibarAlignLeftWhenLand);
                        if (this.isNavibarAlignLeftWhenLand) {
                            navButtons.setVisibility(8);
                            if (HwNavigationBarView.this.mHideNavibar) {
                                mirrorLinkButtons.setVisibility(8);
                            } else {
                                mirrorLinkButtons.setVisibility(0);
                            }
                        } else {
                            navButtons.setVisibility(0);
                            mirrorLinkButtons.setVisibility(8);
                        }
                        super.runInUI();
                    }
                });
            }
        }
    }

    public void reorient() {
        updateVirtualKeyType();
        super.reorient();
        if (this.mIsSupportNavigationBarSlide && this.mSlidingDirection != 2) {
            showScrollAnim(getCurrentViewStub(), Integer.valueOf(this.mSlidingDirection));
            this.mSlidingDirection = 2;
        }
        updateHideButton();
        updateExpandButton();
        updateSplitScreenArrowView();
        getHomeButton().setImageDrawable(this.mVertical ? this.mHomeIcon : this.mHomeLandIcon);
        TintManager.getInstance().setNavigationBarVertical(this.mVertical);
    }

    private void updateSplitScreenArrowView() {
        if (!ActivityManager.supportsMultiWindow()) {
            return;
        }
        if (1 == this.mContext.getResources().getConfiguration().orientation) {
            if (this.mSplitScreenArrowViewLand != null) {
                this.mSplitScreenArrowViewLand.reset();
                this.mSplitScreenArrowViewLand.removeViewToWindow();
            }
            if (this.mSplitScreenArrowViewPort == null) {
                this.mSplitScreenArrowViewPort = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(R.layout.split_screen_arrow_view, null);
            }
            this.mDelegateHelper.setSplitScreenArrowView(this.mSplitScreenArrowViewPort);
            this.mSplitScreenArrowViewPort.addViewToWindow();
            return;
        }
        if (this.mSplitScreenArrowViewPort != null) {
            this.mSplitScreenArrowViewPort.reset();
            this.mSplitScreenArrowViewPort.removeViewToWindow();
        }
        if (this.mSplitScreenArrowViewLand == null) {
            this.mSplitScreenArrowViewLand = (HwSplitScreenArrowView) LayoutInflater.from(this.mContext).inflate(R.layout.split_screen_arrow_view_land, null);
        }
        this.mDelegateHelper.setSplitScreenArrowView(this.mSplitScreenArrowViewLand);
        this.mSplitScreenArrowViewLand.addViewToWindow();
    }

    public void onFinishInflate() {
        this.mRotatedViews[0] = findViewById(R.id.rot0);
        this.mRotatedViews[2] = this.mRotatedViews[0];
        this.mRotatedViews[1] = findViewById(R.id.rot90);
        this.mRotatedViews[3] = this.mRotatedViews[1];
        layoutInflater();
        updateVirtualKeyType();
        super.onFinishInflate();
        updateHideButton();
        updateBackButton();
        updateHomeButton();
        updateRecentButton();
        updateExpandButton();
        this.mEnableNavBar = ((Boolean) SystemUIObserver.get(18)).booleanValue();
        boolean hideVirtualKeyEnable = System.getIntForUser(this.mContext.getContentResolver(), "hide_virtual_key", 0, UserSwitchUtils.getCurrentUser()) != 0;
        boolean hideNavBar = Global.getInt(getContext().getContentResolver(), "navigationbar_is_min", 0) != 0;
        if (NAVBAR_REMOVABLE && !this.mEnableNavBar) {
            hideNavBar = true;
        } else if (!hideVirtualKeyEnable) {
            hideNavBar = false;
        }
        updateNavigationBar(hideNavBar);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        SystemUIObserver.getObserver(4).addOnChangeListener(this.mHideVirtualKeyListener);
        SystemUIObserver.getObserver(5).addOnChangeListener(this.mVirtualKeyTypeListener);
        SystemUIObserver.getObserver(18).addOnChangeListener(this.mEnableListener);
        SystemUIObserver.getObserver(22).addOnChangeListener(this.mVirtualKeyPositionListener);
        SystemUIObserver.getObserver(24).addOnChangeListener(this.mFPNavBarTypeListener);
        getContext().registerReceiverAsUser(this.mScreenPinningReceiver, UserHandle.ALL, new IntentFilter("com.huawei.android.systemui.screenpinning"), null, null);
        this.mIsInFPThirdKeyType = ((Boolean) SystemUIObserver.get(24)).booleanValue();
        getContext().registerReceiverAsUser(this.mHideNavibarReceiver, UserHandle.ALL, new IntentFilter("com.huawei.mirrorlink.action.ACTION_HIDE_NAVIBAR"), null, null);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        SystemUIObserver.getObserver(4).removeOnChangeListener(this.mHideVirtualKeyListener);
        SystemUIObserver.getObserver(5).removeOnChangeListener(this.mVirtualKeyTypeListener);
        SystemUIObserver.getObserver(18).removeOnChangeListener(this.mEnableListener);
        SystemUIObserver.getObserver(22).removeOnChangeListener(this.mVirtualKeyPositionListener);
        SystemUIObserver.getObserver(24).removeOnChangeListener(this.mFPNavBarTypeListener);
        getContext().unregisterReceiver(this.mScreenPinningReceiver);
        getContext().unregisterReceiver(this.mHideNavibarReceiver);
    }

    protected void updateIcons(Context ctx, Configuration oldConfig, Configuration newConfig, boolean isThemeChanged) {
        super.updateIcons(ctx, oldConfig, newConfig, isThemeChanged);
        this.mHomeIcon = ctx.getResources().getDrawable(R.drawable.ic_sysbar_home);
        this.mHomeLandIcon = ctx.getResources().getDrawable(R.drawable.ic_sysbar_home_land);
    }

    public boolean onInterceptTouchEvent(MotionEvent event) {
        try {
            PerfDebugUtils.beginSystraceSection("HwNavigationBarView_onInterceptTouchEvent");
            ((Divider) HwSystemUIApplication.getInstance().getComponent(Divider.class)).getView().hideMenusView();
            boolean onInterceptTouchEvent = this.mDelegateHelper.onInterceptTouchEvent(event);
            return onInterceptTouchEvent;
        } finally {
            PerfDebugUtils.endSystraceSection();
        }
    }

    public boolean onTouchEvent(MotionEvent event) {
        if (event.getActionMasked() == 4) {
            return super.onTouchEvent(event);
        }
        if (event.getActionMasked() != 2) {
            HwLog.i("HwNavigationBarView", "onTouchEvent:" + event.getActionIndex() + ", " + event);
        }
        try {
            PerfDebugUtils.beginSystraceSection("HwNavigationBarView_onTouchEvent");
            handleButton(event);
            if (this.mDelegateHelper.onInterceptTouchEvent(event)) {
                return true;
            }
            boolean onTouchEvent = super.onTouchEvent(event);
            PerfDebugUtils.endSystraceSection();
            return onTouchEvent;
        } finally {
            PerfDebugUtils.endSystraceSection();
        }
    }

    public void handleButton(MotionEvent event) {
        View view;
        int[] location;
        int[] parentLocation;
        float rawX;
        float rawY;
        if (event.getActionMasked() == 5) {
            for (int i = 0; i < this.mButtonDisatchers.size(); i++) {
                ((ButtonDispatcher) this.mButtonDisatchers.valueAt(i)).setCurrentView(this.mCurrentViewStub);
                view = ((ButtonDispatcher) this.mButtonDisatchers.valueAt(i)).getCurrentView();
                if (view == null || view.getVisibility() != 0) {
                    HwLog.i("HwNavigationBarView", "touch pointer up: view == null " + view);
                } else {
                    location = new int[2];
                    view.getLocationOnScreen(location);
                    parentLocation = new int[2];
                    getLocationOnScreen(parentLocation);
                    rawX = event.getX(event.getActionIndex()) + ((float) parentLocation[0]);
                    rawY = event.getY(event.getActionIndex()) + ((float) parentLocation[1]);
                    HwLog.i("HwNavigationBarView", "touch pointer down: x=" + location[0] + ",y=" + location[1] + ", rawX=" + rawX + ", rawY=" + rawY);
                    if (view.pointInView(rawX - ((float) location[0]), rawY - ((float) location[1]), 0.0f)) {
                        HwLog.i("HwNavigationBarView", "touch pointer down: " + view);
                        view.onTouchEvent(event);
                        this.mTargetViewMap.put(Integer.valueOf(event.getActionIndex()), view);
                        break;
                    }
                }
            }
        } else if (event.getActionMasked() == 6) {
            if (event.getActionIndex() == 0) {
                HwLog.i("HwNavigationBarView", "touch pointer up: index == 0");
                return;
            }
            view = (View) this.mTargetViewMap.get(Integer.valueOf(event.getActionIndex()));
            if (view == null) {
                HwLog.i("HwNavigationBarView", "touch pointer up: view == null");
                return;
            }
            location = new int[2];
            view.getLocationOnScreen(location);
            parentLocation = new int[2];
            getLocationOnScreen(parentLocation);
            rawX = event.getX(event.getActionIndex()) + ((float) parentLocation[0]);
            rawY = event.getY(event.getActionIndex()) + ((float) parentLocation[1]);
            HwLog.i("HwNavigationBarView", "touch pointer up: x=" + location[0] + ",y=" + location[1] + ", rawX=" + rawX + ", rawY=" + rawY);
            float x = rawX - ((float) location[0]);
            float y = rawY - ((float) location[1]);
            if (view.pointInView(x, y, 0.0f)) {
                HwLog.i("HwNavigationBarView", "touch pointer up: " + view);
                view.onTouchEvent(event);
            } else {
                HwLog.i("HwNavigationBarView", "touch pointer up, canceled: " + view);
                view.onTouchEvent(MotionEvent.obtain(event.getDownTime(), event.getEventTime(), 3, x, y, event.getMetaState()));
            }
            this.mTargetViewMap.remove(Integer.valueOf(event.getActionIndex()));
        } else if (event.getActionMasked() == 1) {
            for (View view2 : this.mTargetViewMap.values()) {
                location = new int[2];
                view2.getLocationOnScreen(location);
                parentLocation = new int[2];
                getLocationOnScreen(parentLocation);
                rawX = event.getX(event.getActionIndex()) + ((float) parentLocation[0]);
                rawY = event.getY(event.getActionIndex()) + ((float) parentLocation[1]);
                HwLog.i("HwNavigationBarView", "touch up: x=" + location[0] + ",y=" + location[1] + ", rawX=" + rawX + ", rawY=" + rawY);
                if (view2.pointInView(rawX - ((float) location[0]), rawY - ((float) location[1]), 0.0f)) {
                    HwLog.i("HwNavigationBarView", "touch up: " + view2);
                    view2.onTouchEvent(event);
                } else {
                    HwLog.i("HwNavigationBarView", "touch up, canceled: " + view2);
                    view2.onTouchEvent(MotionEvent.obtain(event.getDownTime(), event.getEventTime(), 3, event.getX(), event.getY(), event.getMetaState()));
                }
            }
            this.mTargetViewMap.clear();
        } else if (event.getActionMasked() == 0) {
            HwLog.i("HwNavigationBarView", "touch down");
            this.mTargetViewMap.clear();
        }
    }

    protected void onLayout(boolean changed, int l, int t, int r, int b) {
        super.onLayout(changed, l, t, r, b);
        showNavigationBarAlignLeftWhenLand();
    }

    private View getViewStub(int rot, int virtualKeyType) {
        int layoutId = 0;
        switch (rot) {
            case 0:
            case 2:
                switch (virtualKeyType) {
                    case 0:
                        layoutId = 0;
                        break;
                    case 1:
                        layoutId = 1;
                        break;
                    case 2:
                        layoutId = 2;
                        break;
                    case 3:
                        layoutId = 3;
                        break;
                    default:
                        break;
                }
            case 1:
            case 3:
                switch (virtualKeyType) {
                    case 0:
                        layoutId = 4;
                        break;
                    case 1:
                        layoutId = 5;
                        break;
                    case 2:
                        layoutId = 6;
                        break;
                    case 3:
                        layoutId = 7;
                        break;
                    default:
                        break;
                }
        }
        boolean contains = this.mHashmap.containsKey(Integer.valueOf(layoutId));
        Log.d("HwNavigationBarView", "layoutId: " + layoutId + ", containsKey: " + contains);
        if (contains) {
            return (View) this.mHashmap.get(Integer.valueOf(layoutId));
        }
        View v = this.mViewStubs[layoutId].inflate();
        this.mHashmap.put(Integer.valueOf(layoutId), v);
        return v;
    }

    private void updateVirtualKeyType() {
        this.mVirtualKeyType = ((Integer) SystemUIObserver.get(5)).intValue();
        this.mVirtualKeyPosition = ((Integer) SystemUIObserver.get(22)).intValue();
        HwLog.i("HwNavigationBarView", "updateVirtualKeyType:oldType=" + this.mOldVirtualKeyType + ", newType=" + this.mVirtualKeyType + ", VirtualKeyPosition=" + this.mVirtualKeyPosition);
        if (this.mOldVirtualKeyType != this.mVirtualKeyType || this.mIsSupportNavigationBarSlide) {
            View portViewStub;
            View landViewStub;
            this.mOldVirtualKeyType = this.mVirtualKeyType;
            for (View v : this.mHashmap.values()) {
                v.setVisibility(8);
            }
            if (this.mIsSupportNavigationBarSlide) {
                portViewStub = findViewStubByPositon(0, this.mVirtualKeyType, this.mVirtualKeyPosition);
                landViewStub = findViewStubByPositon(1, this.mVirtualKeyType, this.mVirtualKeyPosition);
            } else {
                portViewStub = getViewStub(0, this.mVirtualKeyType);
                landViewStub = getViewStub(1, this.mVirtualKeyType);
            }
            portViewStub.setVisibility(0);
            landViewStub.setVisibility(0);
            for (int i = 0; i < this.buttons.length; i++) {
                ButtonDispatcher bd = (ButtonDispatcher) this.mButtonDisatchers.get(this.buttons[i]);
                bd.clear();
                View portButton = portViewStub.findViewById(this.buttons[i]);
                if (portButton != null) {
                    bd.addView(portButton);
                }
                View landButton = landViewStub.findViewById(this.buttons[i]);
                if (landButton != null) {
                    bd.addView(landButton);
                }
            }
            if (getResources().getConfiguration().orientation == 1) {
                this.mCurrentViewStub = portViewStub;
            } else {
                this.mCurrentViewStub = landViewStub;
            }
        }
    }

    private void updateHideButton() {
        if (((Boolean) SystemUIObserver.get(4)).booleanValue()) {
            getHideButton().setVisibility(0);
        } else {
            getHideButton().setVisibility(4);
        }
        getHideButton().setOnClickListener(this.mHideButtonClick);
    }

    public void updateNavButtons() {
        updateVirtualKeyType();
        updateBackButton();
        updateHomeButton();
        updateRecentButton();
        updateHideButton();
        updateExpandButton();
        setDisabledFlags(this.mDisabledFlags, true);
    }

    public ButtonDispatcher getHideButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.hide);
    }

    public ButtonDispatcher getExpandButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.expand);
    }

    public ButtonDispatcher getCloseButton() {
        return (ButtonDispatcher) this.mButtonDisatchers.get(R.id.close);
    }

    public void updateBackButton() {
        getBackButton().setLongClickable(true);
        getBackButton().setOnLongClickListener(HwPhoneStatusBar.getInstance().mLongPressBackListener);
    }

    public void updateHomeButton() {
        getHomeButton().setOnTouchListener(HwPhoneStatusBar.getInstance().mHomeActionListener);
        getHomeButton().setOnLongClickListener(HwPhoneStatusBar.getInstance().mLongPressHomeListener);
        getHomeButton().setImageDrawable(this.mVertical ? this.mHomeIcon : this.mHomeLandIcon);
    }

    public void updateRecentButton() {
        getRecentsButton().setOnClickListener(HwPhoneStatusBar.getInstance().mRecentsClickListener);
        getRecentsButton().setOnTouchListener(HwPhoneStatusBar.getInstance().mRecentsPreloadOnTouchListener);
        getRecentsButton().setLongClickable(true);
        getRecentsButton().setOnLongClickListener(HwPhoneStatusBar.getInstance().mRecentsLongClickListener);
    }

    public void updateExpandButton() {
        if (this.mVirtualKeyType == 2 || this.mVirtualKeyType == 3) {
            if (HwPhoneStatusBar.getInstance().isPanelFullExpanded()) {
                getExpandButton().setVisibility(8);
                getCloseButton().setVisibility(0);
            }
            if (HwPhoneStatusBar.getInstance().isPanelFullCollapsed()) {
                getCloseButton().setVisibility(8);
                getExpandButton().setVisibility(0);
            }
            getExpandButton().setOnClickListener(this.mExpandButtonClick);
            getCloseButton().setOnClickListener(this.mCloseButtonClick);
        }
    }

    public void updateNavigationBar(boolean minNaviBar) {
        HwLog.i("HwNavigationBarView", "updateNavigationBar:" + minNaviBar + ", NAVBAR_REMOVABLE=" + NAVBAR_REMOVABLE + ", mEnableNavBar=" + this.mEnableNavBar + ", mIsInScreenPinning=" + this.mIsInScreenPinning);
        if (!minNaviBar && NAVBAR_REMOVABLE && !this.mEnableNavBar && !this.mIsInScreenPinning && !this.mIsInFPThirdKeyType) {
            HwLog.i("HwNavigationBarView", "can not show nav bar");
        } else if (changeNaviBarStatus(minNaviBar)) {
            if (minNaviBar) {
                getRootView().setVisibility(8);
            } else if (!KeyguardUpdateMonitor.getInstance(getContext()).isShowing()) {
                getRootView().setVisibility(0);
            }
        }
    }

    private void triggerNaviBarShowError(String errorMsg, boolean state) {
        MonitorReporter.doMonitor(MonitorReporter.createInfoIntent(907033007, MonitorReporter.createNaviBarStateInfo(errorMsg, state)));
    }

    public void expandStatusBarPanels() {
        this.mStatusBarManager.expandNotificationsPanel();
    }

    public void closeStatusBarPanels() {
        this.mStatusBarManager.collapsePanels();
    }

    public void setDelegateView(View view) {
        this.mDelegateHelper.setDelegateView(view);
    }

    public void setTouchMode(boolean touchMode) {
        LayoutParams lp = (LayoutParams) getLayoutParams();
        if (touchMode) {
            lp.flags &= -33;
        } else {
            lp.flags |= 32;
        }
        SystemUiUtil.updateWindowView(HwPhoneStatusBar.getInstance().getWindowManager(), this, lp);
    }

    public View getCurrentViewStub() {
        return this.mCurrentViewStub;
    }

    private void layoutInflater() {
        if (this.mIsSupportNavigationBarSlide) {
            this.mPadViewStubs[0][1] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port);
            this.mPadViewStubs[1][1] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_swap);
            this.mPadViewStubs[2][1] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port);
            this.mPadViewStubs[3][1] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_swap);
            this.mPadViewStubs[4][1] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land);
            this.mPadViewStubs[5][1] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_swap);
            this.mPadViewStubs[6][1] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land);
            this.mPadViewStubs[7][1] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_swap);
            this.mPadViewStubs[0][0] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_left);
            this.mPadViewStubs[1][0] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_swap_left);
            this.mPadViewStubs[2][0] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_left);
            this.mPadViewStubs[3][0] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_swap_left);
            this.mPadViewStubs[0][2] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_right);
            this.mPadViewStubs[1][2] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_swap_right);
            this.mPadViewStubs[2][2] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_right);
            this.mPadViewStubs[3][2] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_swap_right);
            this.mPadViewStubs[4][0] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_left);
            this.mPadViewStubs[5][0] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_swap_left);
            this.mPadViewStubs[6][0] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_left);
            this.mPadViewStubs[7][0] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_swap_left);
            this.mPadViewStubs[4][2] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_right);
            this.mPadViewStubs[5][2] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_swap_right);
            this.mPadViewStubs[6][2] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_right);
            this.mPadViewStubs[7][2] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_swap_right);
            return;
        }
        this.mViewStubs[0] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port);
        this.mViewStubs[1] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.no_expand_port_swap);
        this.mViewStubs[2] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port);
        this.mViewStubs[3] = (ViewStub) this.mRotatedViews[0].findViewById(R.id.has_expand_port_swap);
        this.mViewStubs[4] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land);
        this.mViewStubs[5] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.no_expand_land_swap);
        this.mViewStubs[6] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land);
        this.mViewStubs[7] = (ViewStub) this.mRotatedViews[1].findViewById(R.id.has_expand_land_swap);
    }

    public boolean onDown(MotionEvent arg0) {
        return false;
    }

    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if (!this.mIsSupportNavigationBarSlide) {
            Log.d("HwNavigationBarView", " navigation bar is not support slide!");
            return false;
        } else if (System.getIntForUser(getContext().getContentResolver(), "virtual_key_gesture_slide_hide", 1, UserSwitchUtils.getCurrentUser()) == 0) {
            Log.d("HwNavigationBarView", " navigation bar slide switch is not open!");
            return false;
        } else {
            float distanceY = e1.getY() - e2.getY();
            int navBarHeight = (int) getContext().getResources().getDimension(17104920);
            if (!(Math.abs(e1.getX() - e2.getX()) > Math.abs(distanceY)) || distanceY >= ((float) navBarHeight)) {
                return false;
            }
            getCurrentViewStub().clearAnimation();
            this.mVirtualKeyPosition = ((Integer) SystemUIObserver.get(22)).intValue();
            int position = this.mVirtualKeyPosition;
            int verticalMinDistance = getCurrentViewStub().getWidth() / 8;
            if (e1.getX() - e2.getX() > ((float) verticalMinDistance) && Math.abs(velocityX) > 0.0f) {
                setNavigationPosition(position, 0);
            } else if (e2.getX() - e1.getX() > ((float) verticalMinDistance) && Math.abs(velocityX) > 0.0f) {
                setNavigationPosition(position, 1);
            }
            if (position != this.mVirtualKeyPosition) {
                System.putInt(getContext().getContentResolver(), "virtual_key_position", this.mVirtualKeyPosition);
            }
            return true;
        }
    }

    public void setNavigationPosition(int currentPosition, int slidingDirection) {
        if (slidingDirection == 0 && currentPosition != 0) {
            getCurrentViewStub().setVisibility(8);
            currentPosition--;
        }
        if (slidingDirection == 1 && currentPosition != 2) {
            getCurrentViewStub().setVisibility(8);
            currentPosition++;
        }
        this.mSlidingDirection = slidingDirection;
        this.mVirtualKeyPosition = currentPosition;
    }

    public void onLongPress(MotionEvent arg0) {
    }

    public boolean onScroll(MotionEvent arg0, MotionEvent arg1, float arg2, float arg3) {
        return false;
    }

    public void onShowPress(MotionEvent arg0) {
    }

    public boolean onSingleTapUp(MotionEvent arg0) {
        return false;
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        if (this.mDetector != null && this.mDetector.onTouchEvent(event)) {
            event.setAction(3);
        }
        return super.dispatchTouchEvent(event);
    }

    private View findViewStubByPositon(int rot, int virtualKeyType, int navigationPositon) {
        int layoutId = 0;
        switch (rot) {
            case 0:
            case 2:
                switch (virtualKeyType) {
                    case 0:
                        layoutId = 0;
                        break;
                    case 1:
                        layoutId = 1;
                        break;
                    case 2:
                        layoutId = 2;
                        break;
                    case 3:
                        layoutId = 3;
                        break;
                    default:
                        break;
                }
            case 1:
            case 3:
                switch (virtualKeyType) {
                    case 0:
                        layoutId = 4;
                        break;
                    case 1:
                        layoutId = 5;
                        break;
                    case 2:
                        layoutId = 6;
                        break;
                    case 3:
                        layoutId = 7;
                        break;
                    default:
                        break;
                }
        }
        if (this.mHashmap.containsKey(Integer.valueOf((layoutId * 10) + navigationPositon))) {
            return (View) this.mHashmap.get(Integer.valueOf((layoutId * 10) + navigationPositon));
        }
        View v = this.mPadViewStubs[layoutId][navigationPositon].inflate();
        this.mHashmap.put(Integer.valueOf((layoutId * 10) + navigationPositon), v);
        return v;
    }

    private void showScrollAnim(View view, Integer derection) {
        float fromXDelta;
        if (derection.intValue() == 0) {
            fromXDelta = 0.3f;
        } else {
            fromXDelta = -0.3f;
        }
        TranslateAnimation showAction = new TranslateAnimation(1, fromXDelta, 1, 0.0f, 1, 0.0f, 1, 0.0f);
        showAction.setDuration(500);
        view.startAnimation(showAction);
    }

    private void handleScreenPinningStateChanged(boolean enterScreenPinning) {
        boolean z = false;
        if (this.mIsInScreenPinning == enterScreenPinning) {
            HwLog.i("HwNavigationBarView", "handleScreenPinningStateChanged::mIsInScreenPinning=" + this.mIsInScreenPinning + " not changed!");
            return;
        }
        this.mIsInScreenPinning = enterScreenPinning;
        if (this.mIsInScreenPinning && getRootView().getVisibility() == 0) {
            HwLog.i("HwNavigationBarView", "handleScreenPinningStateChanged::navigation bar is shown, no need show again");
        } else if (!this.mIsInScreenPinning && 8 == getRootView().getVisibility()) {
            HwLog.i("HwNavigationBarView", "handleScreenPinningStateChanged::navigation bar is gone, no need gone again");
        } else if (this.mIsInFPThirdKeyType || !NAVBAR_REMOVABLE) {
            HwLog.i("HwNavigationBarView", "handleScreenPinningStateChanged::in third key type or virtual key type, no need shown");
        } else if (!this.mIsInScreenPinning && NAVBAR_REMOVABLE && this.mEnableNavBar) {
            HwLog.i("HwNavigationBarView", "handleScreenPinningStateChanged::navigation bar is shown, cannot be gone");
        } else {
            if (!this.mIsInScreenPinning) {
                z = true;
            }
            updateNavigationBar(z);
        }
    }

    public void setVisibility(int visibility) {
        if (getVisibility() != visibility) {
            HwLog.i("HwNavigationBarView", "setVisibility: " + visibility);
        }
        super.setVisibility(visibility);
    }
}
