package com.android.systemui.assist;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemProperties;
import android.provider.Settings.Secure;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.statusbar.BaseStatusBar;
import com.android.systemui.statusbar.phone.HwNavigationBarView;
import com.android.systemui.statusbar.phone.HwPhoneStatusBar;
import com.android.systemui.utils.HwLog;
import com.android.systemui.utils.SystemUiUtil;
import com.android.systemui.utils.analyze.PerfDebugUtils;

public class HwAssistManager extends AssistManager {
    private Context mContext;
    private Handler mMainHandler = new Handler();
    private SearchPanelView mSearchPanelView;
    private boolean mUseHwAssist = true;
    private WindowManager mWindowManager;

    public HwAssistManager(BaseStatusBar bar, Context context) {
        super(bar, context);
        this.mContext = context;
        this.mWindowManager = (WindowManager) this.mContext.getSystemService("window");
        this.mUseHwAssist = hasHwAssist();
        if (this.mUseHwAssist) {
            makeSearchPanelView();
        }
    }

    private boolean hasHwAssist() {
        if (!SystemUiUtil.isChina()) {
            return false;
        }
        try {
            this.mContext.getPackageManager().getPackageInfo("com.huawei.vassistant", 128);
            return true;
        } catch (NameNotFoundException e) {
            return false;
        }
    }

    private void makeSearchPanelView() {
        this.mSearchPanelView = (SearchPanelView) LayoutInflater.from(this.mContext).inflate(R.layout.status_bar_search_panel, new LinearLayout(this.mContext), false);
        this.mSearchPanelView.setOnTouchListener(new OnTouchListener() {
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == 4) {
                    HwAssistManager.this.hideSearchPanel();
                }
                return false;
            }
        });
        this.mSearchPanelView.setVisibility(8);
        this.mSearchPanelView.setHorizontal(true);
        SystemUiUtil.addWindowView(this.mWindowManager, this.mSearchPanelView, getSearchLayoutParams(this.mSearchPanelView.getLayoutParams()));
        ((HwNavigationBarView) HwPhoneStatusBar.getInstance().getNavigationBarView()).setDelegateView(this.mSearchPanelView);
    }

    private LayoutParams getSearchLayoutParams(ViewGroup.LayoutParams layoutParams) {
        LayoutParams lp = new LayoutParams(-1, -1, 2024, 8519936, -3);
        if (ActivityManager.isHighEndGfx()) {
            lp.flags |= 16777216;
        }
        lp.gravity = 8388691;
        lp.setTitle("SearchPanel");
        lp.windowAnimations = 16974579;
        lp.softInputMode = 49;
        return lp;
    }

    public void startAssist(Bundle args, boolean internal) {
        HwLog.i("HwAssistManager", "startAssist::mUseHwAssist, " + this.mUseHwAssist + ", internal=" + internal);
        if (SystemProperties.getBoolean("sys.super_power_save", false)) {
            HwLog.i("HwAssistManager", "in super power mode , startAssist return");
        } else if (HwPhoneStatusBar.getInstance() == null || !HwPhoneStatusBar.getInstance().shouldDisableNavbarGestures()) {
            if (!this.mUseHwAssist) {
                super.startAssist(args);
            } else if (internal) {
                this.mMainHandler.post(new Runnable() {
                    public void run() {
                        HwAssistManager.this.showSearchPanel();
                    }
                });
            } else {
                this.mSearchPanelView.startHwAssistActivity();
            }
        } else {
            HwLog.i("HwAssistManager", "in boot wizard interface, startAssist return");
        }
    }

    public void hideAssist() {
        if (this.mUseHwAssist) {
            this.mMainHandler.post(new Runnable() {
                public void run() {
                    HwAssistManager.this.hideSearchPanel();
                }
            });
        } else {
            super.hideAssist();
        }
    }

    public void onConfigurationChanged() {
        if (!this.mUseHwAssist) {
            super.onConfigurationChanged();
        }
    }

    public void onLockscreenShown() {
        if (!this.mUseHwAssist) {
            super.onLockscreenShown();
        }
    }

    private void showSearchPanel() {
        PerfDebugUtils.beginSystraceSection("HwAssistManager_showSearchPanel");
        if (this.mSearchPanelView.getVisibility() != 0) {
            this.mSearchPanelView.show(true, true);
            ((HwNavigationBarView) HwPhoneStatusBar.getInstance().getNavigationBarView()).setTouchMode(true);
        }
        PerfDebugUtils.endSystraceSection();
    }

    private void hideSearchPanel() {
        PerfDebugUtils.beginSystraceSection("HwAssistManager_hideSearchPanel");
        if (this.mSearchPanelView.getVisibility() == 0) {
            this.mSearchPanelView.show(false, true);
            ((HwNavigationBarView) HwPhoneStatusBar.getInstance().getNavigationBarView()).setTouchMode(false);
        }
        PerfDebugUtils.endSystraceSection();
    }

    public boolean hasSearchPanel() {
        if (Secure.getInt(this.mContext.getContentResolver(), "hw_long_home_voice_assistant", 0) != 1) {
            return this.mUseHwAssist;
        }
        return false;
    }
}
