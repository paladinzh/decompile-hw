package com.huawei.mms.ui;

import android.content.Context;
import android.view.View;
import com.huawei.mms.util.SelectRecorder;
import com.huawei.mms.util.SelectionChangedListener;

public class HwCustMultiModeListView {
    public HwCustMultiModeListView(Context context) {
    }

    public boolean isIgnoreClick(View view) {
        return false;
    }

    public void setSelecetedPosition(int position, boolean selected, SelectRecorder recorder) {
    }

    public void clearPosition(SelectRecorder recorder) {
    }

    public int getpositionSize(SelectRecorder recorder) {
        return 0;
    }

    public boolean isInComposeMessageActivity() {
        return false;
    }

    public void setChangeExtListener(MultiModeListView v) {
    }

    public void setSelectChangeListener(SelectionChangedListener l) {
    }

    public boolean isRcsSwitchOn() {
        return false;
    }
}
