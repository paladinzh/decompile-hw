package com.android.systemui.networkspeed;

import android.content.Context;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.LinearLayout;
import com.android.systemui.R;
import com.android.systemui.networkspeed.NetworkSpeedManagerEx.Callback;
import com.android.systemui.tint.TintTextView;
import com.android.systemui.utils.HwLog;

public class NetworkSpeedView extends LinearLayout implements Callback {
    private TintTextView mNetSpeedText;
    NetworkSpeedManagerEx mNetworkSpeedManagerEx = new NetworkSpeedManagerEx();

    public NetworkSpeedView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    protected void onFinishInflate() {
        this.mNetSpeedText = (TintTextView) findViewById(R.id.speed);
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        HwLog.i("NetworkSpeedView", "onAttachedToWindow parent class:" + getParent().getClass());
        if (this.mNetworkSpeedManagerEx != null) {
            this.mNetworkSpeedManagerEx.init(getContext(), this);
        } else {
            HwLog.e("NetworkSpeedView", "mNetworkSpeedManagerEx is null");
        }
        if (this.mNetSpeedText != null) {
            this.mNetSpeedText.setColorByTintManager();
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        if (this.mNetworkSpeedManagerEx != null) {
            this.mNetworkSpeedManagerEx.unRegister();
            this.mNetworkSpeedManagerEx = null;
        }
        super.onDetachedFromWindow();
    }

    public void updateSpeed(String speed) {
        Log.i("NetworkSpeedView", "/update(), speed=" + speed + " parent class:" + getParent().getClass());
        if (speed != null && this.mNetSpeedText != null) {
            this.mNetSpeedText.setText(speed);
        }
    }

    public void updateVisibility(boolean show) {
        HwLog.i("NetworkSpeedView", "updateVisibility parent class:" + getParent().getClass());
        setVisibility(show ? 0 : 8);
    }
}
