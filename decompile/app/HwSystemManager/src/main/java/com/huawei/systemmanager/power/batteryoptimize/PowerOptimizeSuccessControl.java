package com.huawei.systemmanager.power.batteryoptimize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class PowerOptimizeSuccessControl extends PowerOptimizeControl {
    private PowerOptimizeSuccessContainer mContainer;

    private static class PowerOptimizeSuccessContainer {
        private View mContainer = ((ViewStub) this.view.findViewById(R.id.power_checkall_success_result)).inflate();
        private ViewGroup mContentLayout = ((ViewGroup) this.mContainer.findViewById(R.id.optimize_content_layout));
        private ViewGroup mImgLayout = ((ViewGroup) this.mContainer.findViewById(R.id.optimize_img_layout));
        private TextView mManualTv = ((TextView) this.mContainer.findViewById(R.id.optimize_success_manual_itemNum));
        private View view;

        public PowerOptimizeSuccessContainer(LayoutInflater inflater) {
            this.view = inflater.inflate(R.layout.power_optimize_upperlayout_success, null);
        }

        public ViewGroup getImgLayout() {
            return this.mImgLayout;
        }

        public ViewGroup getContentLayout() {
            return this.mContentLayout;
        }

        public View getLayoutView() {
            return this.view;
        }

        public TextView getManualOptimizeView() {
            return this.mManualTv;
        }
    }

    public PowerOptimizeSuccessControl(LayoutInflater inflater) {
        this.mContainer = new PowerOptimizeSuccessContainer(inflater);
    }

    public TextView getManualItemNumView() {
        return this.mContainer.getManualOptimizeView();
    }

    public ViewGroup getContentLayout() {
        return this.mContainer.getContentLayout();
    }

    public ViewGroup getHeadLayout() {
        return this.mContainer.getImgLayout();
    }

    public View newView() {
        return this.mContainer.getLayoutView();
    }

    public static PowerOptimizeSuccessControl create(LayoutInflater inflater, ViewGroup parent) {
        PowerOptimizeSuccessControl control = new PowerOptimizeSuccessControl(inflater);
        parent.addView(control.newView());
        return control;
    }
}
