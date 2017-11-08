package com.huawei.systemmanager.mainscreen.normal;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.HomeWatcherReceiver;
import com.huawei.systemmanager.comm.component.IBackFromAgreementListener;
import com.huawei.systemmanager.comm.component.IHomePressListener;
import com.huawei.systemmanager.comm.component.IWindowFocusChangedListener;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;

public class NormalFragment extends Fragment implements IWindowFocusChangedListener, IHomePressListener, IBackFromAgreementListener {
    private static final String TAG = "NormalFragment";
    private RelativeLayout mCircleLayout = null;
    private RelativeLayout mEntryLayout = null;
    private boolean mIsSupportOrientation;
    private RelativeLayout mSettingLayout = null;
    private MsStateMachine mStateMachine;
    private RelativeLayout mUpperLayout = null;

    public boolean onHomePressed() {
        if (this.mStateMachine == null) {
            HwLog.i(TAG, "onHomePressed: statemachine is null");
            return false;
        }
        HwLog.i(TAG, "onHomePressed");
        HsmStat.statE(10);
        return true;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        HwLog.i(TAG, getClass().getSimpleName() + " create!");
        this.mIsSupportOrientation = Utility.isSupportOrientation();
        View view = inflater.inflate(R.layout.main_screen_main_view, container, false);
        this.mUpperLayout = (RelativeLayout) view.findViewById(R.id.upper_layout);
        this.mCircleLayout = (RelativeLayout) view.findViewById(R.id.circle_layout);
        this.mEntryLayout = (RelativeLayout) view.findViewById(R.id.main_screen_entry);
        this.mSettingLayout = (RelativeLayout) view.findViewById(R.id.setting_menu);
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        Activity activity = getActivity();
        if (activity == null) {
            HwLog.e(TAG, "onViewCreated, activity is null!!");
            return;
        }
        this.mStateMachine = new MsStateMachine(this, view, Looper.getMainLooper());
        this.mStateMachine.start();
        if (((HsmActivity) activity).isShowAgreement()) {
            HwLog.i(TAG, "activity is show agreement, need not start detect, wait agreement finish");
        } else {
            HwLog.i(TAG, "activity need not show agreement, just start detect");
            this.mStateMachine.startDetect();
        }
        if (this.mIsSupportOrientation) {
            initScreenOrientation(getResources().getConfiguration());
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (this.mIsSupportOrientation) {
            initScreenOrientation(newConfig);
        }
    }

    private void initScreenOrientation(Configuration newConfig) {
        int i = -1;
        if (this.mUpperLayout != null && this.mCircleLayout != null && this.mEntryLayout != null && this.mSettingLayout != null) {
            int nuoyiLeftWidth;
            boolean isLand = 2 == newConfig.orientation;
            LayoutParams mUpperLayoutParams = (LayoutParams) this.mUpperLayout.getLayoutParams();
            if (isLand) {
                nuoyiLeftWidth = HSMConst.getNuoyiLeftWidth();
            } else {
                nuoyiLeftWidth = -1;
            }
            mUpperLayoutParams.width = nuoyiLeftWidth;
            mUpperLayoutParams.height = -1;
            this.mUpperLayout.setLayoutParams(mUpperLayoutParams);
            LayoutParams circleLayoutParams = (LayoutParams) this.mCircleLayout.getLayoutParams();
            circleLayoutParams.setMarginStart(HSMConst.getDimensionPixelSize(R.dimen.main_screen_circle_startmargin));
            this.mCircleLayout.setLayoutParams(circleLayoutParams);
            this.mCircleLayout.setPadding(0, HSMConst.getDimensionPixelSize(R.dimen.main_screen_circle_topmargin), 0, 0);
            LayoutParams ep = (LayoutParams) this.mEntryLayout.getLayoutParams();
            if (isLand) {
                nuoyiLeftWidth = this.mUpperLayout.getId();
            } else {
                nuoyiLeftWidth = -1;
            }
            ep.addRule(17, nuoyiLeftWidth);
            if (!isLand) {
                i = this.mUpperLayout.getId();
            }
            ep.addRule(3, i);
            this.mEntryLayout.setLayoutParams(ep);
            LayoutParams tp = (LayoutParams) this.mSettingLayout.getLayoutParams();
            if (isLand) {
                tp.removeRule(21);
                tp.addRule(20);
            } else {
                tp.removeRule(20);
                tp.addRule(21);
            }
            this.mSettingLayout.setLayoutParams(tp);
            if (this.mStateMachine != null) {
                this.mStateMachine.refreshScreenOrientation(newConfig);
            }
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mStateMachine != null) {
            this.mStateMachine.quit();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mStateMachine != null) {
            this.mStateMachine.sendMessage(5);
        }
        if (getActivity() == null) {
            HwLog.e(TAG, "onResume, but activity is null!");
        } else {
            HomeWatcherReceiver.register(getActivity(), this);
        }
    }

    public void onPause() {
        if (this.mStateMachine != null) {
            this.mStateMachine.sendMessage(6);
        }
        HomeWatcherReceiver.unregister(getActivity());
        super.onPause();
    }

    public void onWindowFocusChanged(boolean hasFocus) {
        HwLog.i(TAG, "onWindowFocusChanged, hasFocus:" + hasFocus);
        if (this.mStateMachine != null && hasFocus) {
            this.mStateMachine.sendMessage(10, (Object) Boolean.valueOf(hasFocus));
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (this.mStateMachine != null) {
            this.mStateMachine.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onBackFromAgreement(boolean agree) {
        if (agree) {
            HwLog.i(TAG, "onBackFromAgreement, agree, start detect");
            if (this.mStateMachine != null) {
                this.mStateMachine.startDetect();
                return;
            }
            return;
        }
        HwLog.i(TAG, "onBackFromAgreement, not agree,do not start detect");
    }
}
