package com.huawei.systemmanager.power.batteryoptimize;

import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;

public class PowerUnOptimizedController {
    private ViewHolder mHolder = null;
    private LayoutInflater mInflater;
    private OnClickListener mOptimizeListener;

    private static class ViewHolder {
        Button optButton;
        ImageView optedImg;
        TextView title;

        public ViewHolder(View view) {
            this.title = (TextView) view.findViewById(R.id.title);
            this.optButton = (Button) view.findViewById(R.id.btn);
            this.optedImg = (ImageView) view.findViewById(R.id.complete_icon);
        }
    }

    public PowerUnOptimizedController(LayoutInflater mInflater, OnClickListener mOptimizeListener) {
        this.mOptimizeListener = mOptimizeListener;
        this.mInflater = mInflater;
    }

    protected View newView(PowerDetectItem item) {
        View view = this.mInflater.inflate(R.layout.power_optimize_result_manual_item_layout, null);
        this.mHolder = new ViewHolder(view);
        int typeItem = item.getItemType();
        if (this.mHolder.optButton != null) {
            if (typeItem == 4) {
                this.mHolder.optButton.setText(R.string.power_check_btn);
            } else {
                this.mHolder.optButton.setText(R.string.power_optimize_btn);
            }
        }
        if (this.mHolder.title != null) {
            this.mHolder.title.setText(item.getTitle());
        }
        if (this.mHolder.optedImg != null) {
            if (item.isOptimized()) {
                this.mHolder.optedImg.setVisibility(0);
            } else {
                this.mHolder.optedImg.setVisibility(8);
            }
        }
        if (this.mHolder.optButton != null) {
            if (item.isOptimized()) {
                this.mHolder.optButton.setVisibility(8);
            } else {
                this.mHolder.optButton.setTag(item);
                this.mHolder.optButton.setVisibility(0);
                this.mHolder.optButton.setOnClickListener(this.mOptimizeListener);
            }
        }
        return view;
    }

    public void update(PowerDetectItem item) {
        this.mHolder.title.setText(item.getTitle());
        if (item.isOptimized()) {
            this.mHolder.optButton.setVisibility(8);
            this.mHolder.optedImg.setVisibility(0);
            return;
        }
        this.mHolder.optButton.setVisibility(0);
        this.mHolder.optedImg.setVisibility(8);
    }

    public static PowerUnOptimizedController create(LayoutInflater inflater, ViewGroup parent, OnClickListener clicker, PowerDetectItem optitem) {
        PowerUnOptimizedController controller = new PowerUnOptimizedController(inflater, clicker);
        parent.addView(controller.newView(optitem));
        return controller;
    }
}
