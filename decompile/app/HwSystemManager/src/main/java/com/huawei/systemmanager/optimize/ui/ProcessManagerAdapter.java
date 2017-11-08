package com.huawei.systemmanager.optimize.ui;

import android.content.Context;
import android.text.format.Formatter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.widget.CommonAdapter;
import com.huawei.systemmanager.comm.widget.StickyListView.StickyListHeadersAdapter;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.optimize.process.ProcessAppItem.EmptyNormalProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

class ProcessManagerAdapter extends CommonAdapter<ProcessAppItem> implements StickyListHeadersAdapter {
    private static final int INDEX_EMTPY_NORMAL_TASK = 2;
    private static final long INDEX_KEY_TASK = 1;
    private static final long INDEX_NORMAL_TASK = 0;
    private static final String TAG = ProcessManagerAdapter.class.getSimpleName();
    private int mKeyTaskNum = 0;
    private int mNormalTaskNum = 0;

    private static class HeaderViewHolder {
        TextView textView;

        private HeaderViewHolder() {
        }
    }

    private static class KeyViewHolder {
        ImageView appIcon;
        TextView appSize;
        TextView appTitle;
        View bottomDivider;

        private KeyViewHolder() {
        }
    }

    private static class NormalViewHolder {
        ImageView appIconImageView;
        TextView appSizeTextView;
        TextView appTitleTextView;
        View bottomDivider;
        CheckBox checkBox;
        ImageView lockIconImageView;

        private NormalViewHolder() {
        }
    }

    ProcessManagerAdapter(Context context, LayoutInflater inflater) {
        super(context, inflater);
    }

    protected View newView(int position, ViewGroup parent, ProcessAppItem item) {
        int viewType = getItemViewType(position);
        if (viewType == 2) {
            return new View(this.mContex);
        }
        View view;
        if (((long) viewType) == 0) {
            view = getInflater().inflate(R.layout.process_manager_list_item_normal, parent, false);
            NormalViewHolder holder = new NormalViewHolder();
            holder.appIconImageView = (ImageView) view.findViewById(R.id.process_manager_item_icon);
            holder.appTitleTextView = (TextView) view.findViewById(R.id.process_manager_item_name);
            holder.appSizeTextView = (TextView) view.findViewById(R.id.process_manager_item_size);
            holder.lockIconImageView = (ImageView) view.findViewById(R.id.lock_icon);
            holder.checkBox = (CheckBox) view.findViewById(R.id.process_manager_checkbox);
            holder.bottomDivider = view.findViewById(R.id.process_manager_bottom_divider);
            view.setTag(holder);
            return view;
        } else if (((long) viewType) == 1) {
            view = getInflater().inflate(R.layout.process_manager_list_item_key, parent, false);
            KeyViewHolder holder2 = new KeyViewHolder();
            holder2.appIcon = (ImageView) view.findViewById(R.id.process_manager_item_icon);
            holder2.appTitle = (TextView) view.findViewById(R.id.process_manager_item_name);
            holder2.appSize = (TextView) view.findViewById(R.id.process_manager_item_size);
            holder2.bottomDivider = view.findViewById(R.id.process_manager_bottom_divider);
            view.setTag(holder2);
            return view;
        } else {
            HwLog.e(TAG, "newView error,viewType:" + viewType);
            return null;
        }
    }

    protected void bindView(int position, View view, ProcessAppItem item) {
        int viewType = getItemViewType(position);
        String appSize;
        if (((long) viewType) == 0) {
            int i;
            NormalViewHolder holder = (NormalViewHolder) view.getTag();
            holder.appIconImageView.setImageDrawable(item.getIcon());
            holder.appTitleTextView.setText(item.getName());
            appSize = Formatter.formatFileSize(getContext(), item.getMemoryCost());
            holder.appSizeTextView.setText(getString(R.string.app_memory_size, appSize));
            if (item.isProtect()) {
                holder.lockIconImageView.setVisibility(0);
            } else {
                holder.lockIconImageView.setVisibility(4);
            }
            holder.checkBox.setChecked(item.isChecked());
            boolean shouldShowBottomDivider = checkShouldShowBottomDivider(position);
            View view2 = holder.bottomDivider;
            if (shouldShowBottomDivider) {
                i = 0;
            } else {
                i = 4;
            }
            view2.setVisibility(i);
        } else if (((long) viewType) == 1) {
            KeyViewHolder holder2 = (KeyViewHolder) view.getTag();
            holder2.appIcon.setImageDrawable(item.getIcon());
            holder2.appTitle.setText(item.getName());
            appSize = Formatter.formatFileSize(getContext(), item.getMemoryCost());
            holder2.appSize.setText(getString(R.string.app_memory_size, appSize));
            holder2.bottomDivider.setVisibility(checkShouldShowBottomDivider(position) ? 0 : 4);
        }
    }

    private boolean checkShouldShowBottomDivider(int position) {
        if (position >= this.mList.size() - 1) {
            return true;
        }
        return (getHeaderId(position) == getHeaderId(position + 1) || (((ProcessAppItem) getItem(position)) instanceof EmptyNormalProcessAppItem)) ? false : true;
    }

    public View getHeaderView(int position, View convertView, ViewGroup parent) {
        HeaderViewHolder holder;
        ProcessAppItem item = (ProcessAppItem) getItem(position);
        if (convertView == null) {
            convertView = getInflater().inflate(R.layout.process_manager_list_indicator, parent, false);
            TextView tv = (TextView) convertView.findViewById(R.id.process_manager_indicator_task_info);
            holder = new HeaderViewHolder();
            holder.textView = tv;
            convertView.setTag(holder);
        }
        holder = (HeaderViewHolder) convertView.getTag();
        String text = "";
        if (item instanceof EmptyNormalProcessAppItem) {
            text = getString(R.string.process_manager_no_normal_task);
        } else if (item.isKeyProcess()) {
            text = getString(R.string.process_manager_key_task, Integer.valueOf(this.mKeyTaskNum));
        } else {
            text = getString(R.string.process_manager_normal_task, Integer.valueOf(this.mNormalTaskNum));
        }
        holder.textView.setText(text);
        return convertView;
    }

    public long getHeaderId(int position) {
        if (((ProcessAppItem) getItem(position)).isKeyProcess()) {
            return 1;
        }
        return 0;
    }

    public int getViewTypeCount() {
        return 3;
    }

    public int getItemViewType(int position) {
        ProcessAppItem item = (ProcessAppItem) getItem(position);
        if (item instanceof EmptyNormalProcessAppItem) {
            return 2;
        }
        if (item.isKeyProcess()) {
            return 1;
        }
        return 0;
    }

    public boolean swapData(List<? extends ProcessAppItem> list) {
        if (list == null) {
            HwLog.i(TAG, "swap data, list is null!");
            return false;
        }
        this.mNormalTaskNum = 0;
        this.mKeyTaskNum = 0;
        for (ProcessAppItem item : list) {
            if (item.isKeyProcess()) {
                this.mKeyTaskNum++;
            } else {
                this.mNormalTaskNum++;
            }
        }
        this.mList.clear();
        if (this.mNormalTaskNum == 0) {
            this.mList.add(ProcessAppItem.EMPTY_NORMAL_ITEM);
        }
        this.mList.addAll(list);
        notifyDataSetChanged();
        return true;
    }

    public List<ProcessAppItem> getData() {
        if (this.mList.size() < 1 || !(((ProcessAppItem) this.mList.get(0)) instanceof EmptyNormalProcessAppItem)) {
            return super.getData();
        }
        return this.mList.subList(1, this.mList.size());
    }

    public void clear() {
        super.clear();
    }

    public int getTotalTaskNum() {
        return this.mNormalTaskNum + this.mKeyTaskNum;
    }
}
