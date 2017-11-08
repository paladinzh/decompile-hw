package com.android.settings.fingerprint;

import android.content.Context;
import android.content.Intent;
import android.os.SystemProperties;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class HwCustFingerprintManageFragmentImpl extends HwCustFingerprintManageFragment {
    private static final int FP_RECOGNITION = 1;
    private static final int NUM_FOR_INTENT = 1;
    private String FP_SETTING_START_SHOT_CUT = "ro.config.fp_launch_app";
    private Button mFpDiyButton;

    public HwCustFingerprintManageFragmentImpl(FingerprintManageFragment fingerprintManageFragment) {
        super(fingerprintManageFragment);
    }

    public boolean fingerPrintShotcut() {
        return SystemProperties.getBoolean(this.FP_SETTING_START_SHOT_CUT, false);
    }

    public void initDiyButton(Context mContext, View fragmentView) {
        this.mFpDiyButton = (Button) fragmentView.findViewById(2131886635);
        this.mFpDiyButton.setText(2131629282);
        this.mFpDiyButton.setVisibility(0);
        this.mFpDiyButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                switch (v.getId()) {
                    case 2131886635:
                        Intent intent = new Intent();
                        intent.setClass(HwCustFingerprintManageFragmentImpl.this.mFingerprintManageFragment.getActivity(), FingerprintShortcutActivity.class);
                        intent.putExtra("fp_msg", HwCustFingerprintManageFragmentImpl.this.mFingerprintManageFragment.getArguments());
                        ((FingerprintManagementActivity) HwCustFingerprintManageFragmentImpl.this.mFingerprintManageFragment.getActivity()).setmIsToFinish(false);
                        HwCustFingerprintManageFragmentImpl.this.mFingerprintManageFragment.startActivityForResult(intent, 1);
                        return;
                    default:
                        return;
                }
            }
        });
    }
}
