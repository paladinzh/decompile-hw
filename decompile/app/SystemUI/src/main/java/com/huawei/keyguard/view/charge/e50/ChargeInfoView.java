package com.huawei.keyguard.view.charge.e50;

import android.content.Context;
import android.content.res.Resources;
import android.os.SystemProperties;
import android.util.AttributeSet;
import android.view.OrientationEventListener;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.android.keyguard.R$bool;
import com.android.keyguard.R$drawable;
import com.android.keyguard.R$id;
import com.huawei.keyguard.KeyguardCfg;
import com.huawei.keyguard.data.BatteryStateInfo;
import com.huawei.keyguard.util.HwLog;
import java.text.NumberFormat;

public class ChargeInfoView extends RelativeLayout {
    private int[] mImageFileIDs = new int[]{R$drawable.ic_charge_standard, R$drawable.ic_charge_quick, R$drawable.ic_charge_super, R$drawable.ic_charge_super_v};
    private boolean mKeyguardScreenRotation;
    private OrientationEventListener mOrientationListener;
    private int mRotation = -1;

    public class TabletOrientationEventListener extends OrientationEventListener {
        public TabletOrientationEventListener(Context context, int rate) {
            super(context, rate);
        }

        public void onOrientationChanged(int orientation) {
            if (orientation != -1) {
                int orient;
                if (orientation > 350 || orientation < 10) {
                    orientation = 0;
                } else if (orientation > 80 && orientation < 100) {
                    orientation = 90;
                } else if (orientation > 170 && orientation < 190) {
                    orientation = 180;
                } else if (orientation > 260 && orientation < 280) {
                    orientation = 270;
                } else {
                    return;
                }
                if (KeyguardCfg.isDefaultPortOrientation() || ChargeInfoView.this.mRotation == 0) {
                    orient = 360 - orientation;
                } else if (ChargeInfoView.this.mRotation == 1) {
                    orient = 270 - orientation;
                } else if (ChargeInfoView.this.mRotation == 2) {
                    orient = 180 - orientation;
                } else {
                    orient = 90 - orientation;
                }
                HwLog.v("ChargeInfoView", "Orientation changed to " + orient);
                ChargeInfoView.this.setRotation((float) orient);
            }
        }
    }

    public ChargeInfoView(Context context) {
        super(context);
    }

    public ChargeInfoView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public ChargeInfoView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    private boolean shouldEnableKeyguardScreenRotation() {
        Resources res = getContext().getResources();
        if (SystemProperties.getBoolean("lockscreen.rot_override", false)) {
            return true;
        }
        return res.getBoolean(R$bool.config_enableLockScreenRotation);
    }

    protected void onFinishInflate() {
        super.onFinishInflate();
        this.mKeyguardScreenRotation = shouldEnableKeyguardScreenRotation();
        initialViewViaChargeStatus();
    }

    public void initialViewViaChargeStatus() {
        BatteryStateInfo batteryInfo = BatteryStateInfo.getInst();
        int chargeMode = batteryInfo.getChargingMode();
        ImageView flashIcon = (ImageView) findViewById(R$id.flashIcon);
        TextView content1 = (TextView) findViewById(R$id.chargeInfoContent1);
        TextView content2 = (TextView) findViewById(R$id.chargeInfoContent2);
        ((TextView) findViewById(R$id.superChargingContent)).setVisibility(chargeMode == 2 ? 0 : 8);
        if (KeyguardCfg.isUseVSuperChargeIcon() && chargeMode == 2) {
            flashIcon.setImageResource(this.mImageFileIDs[3]);
        } else {
            flashIcon.setImageResource(this.mImageFileIDs[chargeMode]);
        }
        String percentage = NumberFormat.getPercentInstance().format(((double) batteryInfo.getChargeLevel()) / 100.0d);
        int percentSymbolIndex = percentage.indexOf("%");
        if (percentSymbolIndex != -1) {
            String num = percentage.substring(0, percentSymbolIndex);
            String percentSymbol = percentage.substring(percentSymbolIndex, percentage.length());
            content1.setText(num);
            content2.setText(percentSymbol);
            return;
        }
        content1.setText(percentage);
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.mRotation = ((WindowManager) getContext().getSystemService("window")).getDefaultDisplay().getRotation();
        if (this.mOrientationListener == null) {
            this.mOrientationListener = new TabletOrientationEventListener(getContext(), 3);
            if (this.mOrientationListener.canDetectOrientation() && this.mKeyguardScreenRotation) {
                HwLog.v("ChargeInfoView", "Can detect orientation");
                this.mOrientationListener.enable();
            }
        }
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        if (this.mOrientationListener != null) {
            this.mOrientationListener.disable();
        }
    }
}
