package com.huawei.rcs.ui;

import android.content.Context;
import android.view.View;
import com.android.mms.ui.ComposeMessageActivity;
import com.android.rcs.RcsCommonConfig;
import com.huawei.mms.ui.MultiModeListView;
import com.huawei.mms.ui.MultiModeListView.CheckableView;
import com.huawei.mms.util.SelectRecorder;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.rcs.util.RcsSelectRecorder.SelectChangeExtListener;

public class RcsMultiModeListView implements SelectChangeExtListener {
    private Context mContext;
    private MultiModeListView mMultiView;
    private SelectRecorder mRecorder;
    private SelectionChangedListener mSelectChangeListener = null;

    public RcsMultiModeListView(Context context) {
        this.mContext = context;
    }

    public boolean isIgnoreClick(View view) {
        boolean z = false;
        if (!RcsCommonConfig.isRCSSwitchOn()) {
            return false;
        }
        if (!(view instanceof CheckableView)) {
            z = true;
        }
        return z;
    }

    public void setSelecetedPosition(int position, boolean selected, SelectRecorder recorder) {
        if (RcsCommonConfig.isRCSSwitchOn() && recorder != null) {
            if (selected) {
                recorder.getRcsSelectRecorder().addPosition(position);
            } else {
                recorder.getRcsSelectRecorder().removePosition(position);
            }
        }
    }

    public boolean isInComposeMessageActivity() {
        if (RcsCommonConfig.isRCSSwitchOn() && (this.mContext instanceof ComposeMessageActivity)) {
            return true;
        }
        return false;
    }

    public void setChangeExtListener(MultiModeListView v) {
        if (RcsCommonConfig.isRCSSwitchOn() && v != null) {
            this.mMultiView = v;
            this.mRecorder = v.getRecorder();
            if (this.mRecorder != null) {
                this.mRecorder.getRcsSelectRecorder().setSelectChangeExtListener(this);
            }
        }
    }

    public void setSelectChangeListener(SelectionChangedListener l) {
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mSelectChangeListener = l;
        }
    }

    public void onItemChangedPosition(int position) {
        if (this.mSelectChangeListener != null && this.mRecorder != null && this.mMultiView != null && this.mMultiView.isInEditMode()) {
            this.mSelectChangeListener.onSelectChange(this.mRecorder.getRcsSelectRecorder().positionSize(), this.mMultiView.getCount());
        }
    }

    public boolean isRcsSwitchOn() {
        return RcsCommonConfig.isRCSSwitchOn();
    }
}
