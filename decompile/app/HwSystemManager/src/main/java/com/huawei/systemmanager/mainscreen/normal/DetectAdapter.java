package com.huawei.systemmanager.mainscreen.normal;

import android.content.Context;
import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;

public class DetectAdapter extends CommonAdapter<DetectItem> {
    private static final int VIEW_TYPE_MANUALLY = 2;
    private static final int VIEW_TYPE_OPTIMIZED = 1;
    private static final int VIEW_TYPE_PROGRESS = 0;
    private OnClickListener mOptimizeBtnClicker;

    private static class ViewHolder {
        final Button btn;
        final TextView description;
        final TextView title;
        final int viewType;

        public ViewHolder(View view, int type) {
            this.title = (TextView) view.findViewById(R.id.title);
            this.description = (TextView) view.findViewById(R.id.description);
            this.btn = (Button) view.findViewById(R.id.btn);
            this.viewType = type;
        }
    }

    public DetectAdapter(Context context, LayoutInflater inflater, OnClickListener optimizeBtnClicker) {
        super(context, inflater);
        this.mOptimizeBtnClicker = optimizeBtnClicker;
    }

    protected View newView(int position, ViewGroup parent, DetectItem item) {
        int resId = R.layout.main_screen_detect_result_item_progress;
        int viewType = getItemViewType(position);
        switch (viewType) {
            case 0:
                resId = R.layout.main_screen_detect_result_item_progress;
                break;
            case 1:
                resId = R.layout.main_screen_detect_result_item_optimized;
                break;
            case 2:
                resId = R.layout.main_screen_detect_result_item_manually;
                break;
        }
        View view = getInflater().inflate(resId, parent, false);
        ViewHolder holder = new ViewHolder(view, viewType);
        Button btn = holder.btn;
        if (btn != null) {
            btn.setOnClickListener(this.mOptimizeBtnClicker);
        }
        view.setTag(holder);
        return view;
    }

    protected void bindView(int position, View view, DetectItem item) {
        ViewHolder holder = (ViewHolder) view.getTag();
        holder.title.setText(item.getTitle(getContext()));
        switch (holder.viewType) {
            case 2:
                if (holder.btn != null) {
                    holder.btn.setTag(item);
                    holder.btn.setText(Html.fromHtml(item.getOptimizeActionName()));
                    break;
                }
                break;
        }
        if (holder.description != null) {
            String des = item.getDescription(getContext());
            if (TextUtils.isEmpty(des) || item.isOptimized()) {
                holder.description.setVisibility(8);
                return;
            }
            holder.description.setText(des);
            holder.description.setVisibility(0);
        }
    }

    public boolean isEnabled(int position) {
        return false;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(int position) {
        DetectItem item = (DetectItem) getItem(position);
        if (item.isOptimized()) {
            return 1;
        }
        if (item.isManulOptimize()) {
            return 2;
        }
        return 0;
    }
}
