package com.huawei.systemmanager.power.batteryoptimize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;

public class PowerOptimizeTimeControl extends PowerOptimizeControl {
    private PowerOptimizeTimeContainer mContainer;

    private static class PowerOptimizeTimeContainer {
        private View mContainer = ((ViewStub) this.view.findViewById(R.id.power_checkall_time_result)).inflate();
        private ViewGroup mContentLayout = ((ViewGroup) this.mContainer.findViewById(R.id.result_optimize_time_content));
        private TextView mManualTv = ((TextView) this.mContainer.findViewById(R.id.optimize_manual_itemNum));
        private TextView mSizeTv = ((TextView) this.mContainer.findViewById(R.id.time_size));
        private TextView mSizeUnit = ((TextView) this.mContainer.findViewById(R.id.time_unit));
        private ViewGroup mTimeLayout = ((ViewGroup) this.mContainer.findViewById(R.id.time_size_result_layout));
        private View view;

        public PowerOptimizeTimeContainer(LayoutInflater inflater) {
            this.view = inflater.inflate(R.layout.power_optimize_upperlayout_time, null);
        }

        public ViewGroup getHeadLayout() {
            return this.mTimeLayout;
        }

        public ViewGroup getContentLayout() {
            return this.mContentLayout;
        }

        public TextView getManualOptimizeView() {
            return this.mManualTv;
        }

        public void showOptimizeTimeSize(int timeSize) {
            this.mSizeTv.setText(Utility.getLocaleNumber(timeSize));
        }

        public void showOptimizeTimeSizeOtherLanguages(String str) {
            this.mSizeTv.setText(str);
        }

        public void setTimeUnitGone() {
            this.mSizeUnit.setVisibility(8);
        }

        public View getLayoutView() {
            return this.view;
        }
    }

    public PowerOptimizeTimeControl(LayoutInflater inflater) {
        this.mContainer = new PowerOptimizeTimeContainer(inflater);
    }

    public ViewGroup getContentLayout() {
        return this.mContainer.getContentLayout();
    }

    public TextView getManualItemNumView() {
        return this.mContainer.getManualOptimizeView();
    }

    public void updateOptimizedTime(int timeSize) {
        if (GlobalContext.getContext().getResources().getBoolean(R.bool.spaceclean_percent_small_mode)) {
            this.mContainer.showOptimizeTimeSize(timeSize);
            return;
        }
        this.mContainer.setTimeUnitGone();
        this.mContainer.showOptimizeTimeSizeOtherLanguages(GlobalContext.getContext().getResources().getQuantityString(R.plurals.power_time_min_array, timeSize, new Object[]{Integer.valueOf(timeSize)}));
    }

    public ViewGroup getHeadLayout() {
        return this.mContainer.getHeadLayout();
    }

    public View newView() {
        return this.mContainer.getLayoutView();
    }

    public void onConfigurationChanged() {
        LayoutParams params = (LayoutParams) getContentLayout().getLayoutParams();
        params.bottomMargin = GlobalContext.getContext().getResources().getDimensionPixelSize(R.dimen.hsm_nuoyi_circleinfo_marginbottom);
        getContentLayout().setLayoutParams(params);
    }

    public static PowerOptimizeTimeControl create(LayoutInflater inflater, ViewGroup parent) {
        PowerOptimizeTimeControl control = new PowerOptimizeTimeControl(inflater);
        parent.addView(control.newView());
        return control;
    }
}
