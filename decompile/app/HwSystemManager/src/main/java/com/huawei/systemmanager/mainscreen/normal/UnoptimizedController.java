package com.huawei.systemmanager.mainscreen.normal;

import android.text.Html;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.mainscreen.detector.item.DetectItem;

public class UnoptimizedController {
    private static final int VIEW_TYPE_CLICK = 2;
    private static final int VIEW_TYPE_JUMP = 1;
    private ViewHolder mHolder = null;
    private LayoutInflater mInflater;
    private View mMainView;
    private OnClickListener mOptimizeListener;

    private static class ViewHolder {
        Button btn;
        ImageView completeicon;
        TextView description;
        ImageView jumpicon;
        TextView jumpinfo;
        TextView title;
        int viewType;

        public ViewHolder(View view, int type) {
            this.title = (TextView) view.findViewById(R.id.title);
            this.description = (TextView) view.findViewById(R.id.description);
            if (type == 2) {
                this.btn = (Button) view.findViewById(R.id.btn);
            } else {
                this.jumpinfo = (TextView) view.findViewById(R.id.jumpinfo);
                this.jumpicon = (ImageView) view.findViewById(R.id.jumpicon);
            }
            this.completeicon = (ImageView) view.findViewById(R.id.complete_icon);
            this.viewType = type;
        }
    }

    public UnoptimizedController(LayoutInflater inflater, DetectItem item, OnClickListener clicker) {
        this.mOptimizeListener = clicker;
        this.mInflater = inflater;
        this.mMainView = null;
    }

    protected View newView(DetectItem item) {
        View view = null;
        int viewType = getItemViewType(item);
        String des;
        switch (viewType) {
            case 1:
                view = this.mInflater.inflate(R.layout.main_screen_opitmize_result_jump, null);
                this.mHolder = new ViewHolder(view, viewType);
                this.mHolder.title.setText(item.getTitle(view.getContext()));
                if (this.mHolder.description != null) {
                    des = item.getDescription(view.getContext());
                    if (TextUtils.isEmpty(des) || item.isOptimized()) {
                        this.mHolder.description.setVisibility(8);
                    } else {
                        this.mHolder.description.setText(des);
                        this.mHolder.description.setVisibility(0);
                    }
                }
                if (this.mHolder.jumpinfo != null) {
                    if (TextUtils.isEmpty(item.getOptimizeActionName()) || item.isOptimized()) {
                        this.mHolder.jumpinfo.setVisibility(8);
                    } else {
                        this.mHolder.jumpinfo.setText(Html.fromHtml(item.getOptimizeActionName()));
                        this.mHolder.jumpinfo.setVisibility(0);
                    }
                }
                if (this.mHolder.jumpicon != null) {
                    if (item.isOptimized()) {
                        this.mHolder.jumpinfo.setVisibility(8);
                    } else {
                        this.mHolder.jumpinfo.setVisibility(0);
                    }
                }
                view.setTag(item);
                view.setOnClickListener(this.mOptimizeListener);
                break;
            case 2:
                view = this.mInflater.inflate(R.layout.main_screen_optimize_result_click, null);
                this.mHolder = new ViewHolder(view, viewType);
                this.mHolder.title.setText(item.getTitle(view.getContext()));
                if (this.mHolder.btn != null) {
                    if (item.isOptimized()) {
                        this.mHolder.btn.setVisibility(8);
                    } else {
                        this.mHolder.btn.setTag(item);
                        this.mHolder.btn.setText(Html.fromHtml(item.getOptimizeActionName()));
                        this.mHolder.btn.setVisibility(0);
                        this.mHolder.btn.setOnClickListener(this.mOptimizeListener);
                    }
                }
                if (this.mHolder.description != null) {
                    des = item.getDescription(view.getContext());
                    if (!TextUtils.isEmpty(des) && !item.isOptimized()) {
                        this.mHolder.description.setText(des);
                        this.mHolder.description.setVisibility(0);
                        break;
                    }
                    this.mHolder.description.setVisibility(8);
                    break;
                }
                break;
        }
        this.mMainView = view;
        return view;
    }

    public int getItemViewType(DetectItem itemval) {
        DetectItem item = itemval;
        if (itemval.getOptimizeActionType() == 3) {
            return 1;
        }
        return 2;
    }

    public void update(DetectItem optitem) {
        ViewHolder holder = this.mHolder;
        switch (holder.viewType) {
            case 1:
                if (holder.completeicon != null) {
                    if (optitem == null || optitem.isOptimized()) {
                        holder.completeicon.setVisibility(0);
                    } else {
                        holder.completeicon.setVisibility(8);
                    }
                }
                if (holder.jumpinfo != null) {
                    if (optitem == null || optitem.isOptimized()) {
                        holder.jumpinfo.setVisibility(8);
                    } else {
                        holder.jumpinfo.setVisibility(0);
                    }
                }
                if (holder.jumpicon != null) {
                    if (optitem == null || optitem.isOptimized()) {
                        holder.jumpicon.setVisibility(8);
                    } else {
                        holder.jumpicon.setVisibility(0);
                    }
                }
                if (optitem != null && optitem.isOptimized() && this.mMainView != null) {
                    this.mMainView.setOnClickListener(null);
                    return;
                }
                return;
            case 2:
                if (!(holder.title == null || optitem == null)) {
                    holder.title.setText(optitem.getTitle(this.mMainView.getContext()));
                }
                if (holder.completeicon != null) {
                    if (optitem == null || optitem.isOptimized()) {
                        holder.completeicon.setVisibility(0);
                    } else {
                        holder.completeicon.setVisibility(8);
                    }
                }
                if (holder.btn == null) {
                    return;
                }
                if (optitem == null || optitem.isOptimized()) {
                    holder.btn.setVisibility(8);
                    return;
                } else {
                    holder.btn.setVisibility(0);
                    return;
                }
            default:
                return;
        }
    }

    public static UnoptimizedController create(LayoutInflater inflater, ViewGroup parent, OnClickListener clicker, DetectItem optitem) {
        UnoptimizedController controller = new UnoptimizedController(inflater, optitem, clicker);
        parent.addView(controller.newView(optitem));
        return controller;
    }
}
