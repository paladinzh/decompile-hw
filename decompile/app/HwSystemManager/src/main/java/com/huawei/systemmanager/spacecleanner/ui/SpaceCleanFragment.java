package com.huawei.systemmanager.spacecleanner.ui;

import android.app.Fragment;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver.OnGlobalLayoutListener;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.IBackFromAgreementListener;
import com.huawei.systemmanager.comm.component.IBackPressListener;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.util.HwLog;

public class SpaceCleanFragment extends Fragment implements IBackPressListener, IBackFromAgreementListener {
    private static final String TAG = "SpaceCleanFragment";
    private View fragmentView = null;
    private RelativeLayout mBtnContainer;
    private boolean mIsSupportOrientation;
    private OnGlobalLayoutListener mLayoutListener;
    private SpaceCleanStatMachine mStateMachine;
    private View mUpperView;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.fragmentView = inflater.inflate(R.layout.spaceclean_main_layout, container, false);
        this.mIsSupportOrientation = Utility.isSupportOrientation();
        findControllerView();
        return this.fragmentView;
    }

    private void findControllerView() {
        this.mUpperView = this.fragmentView.findViewById(R.id.sliding_layout_upperview);
        this.mBtnContainer = (RelativeLayout) this.fragmentView.findViewById(R.id.btn_container);
        if (this.mIsSupportOrientation && this.mLayoutListener == null) {
            this.mLayoutListener = new OnGlobalLayoutListener() {
                public void onGlobalLayout() {
                    SpaceCleanFragment.this.relayoutBtnContainer();
                }
            };
            if (this.mUpperView != null) {
                this.mUpperView.getViewTreeObserver().addOnGlobalLayoutListener(this.mLayoutListener);
            }
        }
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        HsmActivity ac = (HsmActivity) getActivity();
        if (ac == null) {
            HwLog.w(TAG, "onViewCreated: Unable to getActivity ");
            return;
        }
        this.mStateMachine = new SpaceCleanStatMachine(this, view);
        this.mStateMachine.start();
        if (ac.isShowAgreement()) {
            HwLog.i(TAG, "onViewCreated: Wait for useragreement result");
        } else {
            HwLog.i(TAG, "onViewCreated: No need to show useragreement ,start sacan");
            this.mStateMachine.startNormalScan();
        }
    }

    public void onResume() {
        super.onResume();
        if (this.mStateMachine != null) {
            this.mStateMachine.onResume();
        }
    }

    public boolean onBackPressed() {
        if (this.mStateMachine != null) {
            return this.mStateMachine.clickBack();
        }
        return false;
    }

    public void onBackFromAgreement(boolean agree) {
        if (agree) {
            HwLog.i(TAG, "onBackFromAgreement: agree, start scan");
            if (this.mStateMachine != null) {
                this.mStateMachine.startNormalScan();
                return;
            }
            return;
        }
        HwLog.i(TAG, "onBackFromAgreement: disagree, skip");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        HwLog.i(TAG, "onActivityResult requestCode:" + requestCode + "  resultCode:" + resultCode);
        if (this.mStateMachine != null) {
            this.mStateMachine.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onDestroyView() {
        super.onDestroyView();
        if (this.mStateMachine != null) {
            this.mStateMachine.quit();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
    }

    private void relayoutBtnContainer() {
        if (this.mBtnContainer != null && this.mUpperView != null) {
            LayoutParams btnContainerParams = (LayoutParams) this.mBtnContainer.getLayoutParams();
            btnContainerParams.setMarginStart(getResources().getConfiguration().orientation == 2 ? this.mUpperView.getWidth() : 0);
            this.mBtnContainer.setLayoutParams(btnContainerParams);
        }
    }
}
