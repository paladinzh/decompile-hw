package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridItem.BaseListGridItem;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnClickListener;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridListener.OnSizeChangeListener;
import com.huawei.systemmanager.spacecleanner.ui.StatisticalData;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.photomanager.PhotoViewerActivity;
import java.util.ArrayList;
import java.util.List;

public class BaseListGridAdapter extends BaseAdapter implements OnClickListener {
    public static final String TAG = "BaseListGridAdapter";
    public Context mContext;
    private List<BaseListGridItem> mData = new ArrayList();
    public final LayoutInflater mLayoutInflater;
    private OnSizeChangeListener mSizeChangeListener;
    private List<ITrashItem> mSource = new ArrayList();
    private StatisticalData mStatisticalData;

    private static class TitleHolder {
        CheckBox mCheckBox;
        TextView mTimeTextView;
        TextView mTitleTextView;

        private TitleHolder() {
        }
    }

    public BaseListGridAdapter(Context ct, OnSizeChangeListener l) {
        this.mContext = ct;
        this.mLayoutInflater = (LayoutInflater) ct.getSystemService("layout_inflater");
        this.mSizeChangeListener = l;
    }

    public void setStatisticalData(StatisticalData statisticalData) {
        this.mStatisticalData = statisticalData;
    }

    public void swapData(List<ITrashItem> list) {
        this.mData.clear();
        this.mSource.clear();
        if (list != null) {
            this.mSource.addAll(list);
            List<ITrashItem> tempList = new ArrayList();
            ListGridTitleItem titleItem = null;
            int totalCount = this.mSource.size();
            int i = 0;
            while (i < totalCount) {
                ITrashItem t = (ITrashItem) this.mSource.get(i);
                if (i == 0 || t.getMonth() != ((ITrashItem) this.mSource.get(i - 1)).getMonth()) {
                    if (tempList.size() != 0) {
                        if (titleItem != null) {
                            titleItem.setCount(tempList.size());
                            titleItem.setLists(tempList);
                        }
                        this.mData.addAll(ListGridUtils.createContentItem(tempList));
                        tempList.clear();
                    }
                    titleItem = new ListGridTitleItem(t.getMonth(), t.getYear());
                    this.mData.add(titleItem);
                }
                tempList.add(t);
                i++;
            }
            this.mData.addAll(ListGridUtils.createContentItem(tempList));
            if (titleItem != null) {
                titleItem.setCount(tempList.size());
                titleItem.setLists(tempList);
            }
        }
        notifyDataSetChanged();
        checkedIsAllChecked();
    }

    public int getViewTypeCount() {
        return 2;
    }

    public int getItemViewType(int position) {
        return getItem(position).getType();
    }

    public int getCount() {
        return this.mData.size();
    }

    public BaseListGridItem getItem(int i) {
        return (BaseListGridItem) this.mData.get(i);
    }

    public long getItemId(int i) {
        return (long) i;
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (getItemViewType(i) == 0) {
            view = createTitleView(view);
            bindTitleView(i, view);
            return view;
        }
        view = createContentView(view);
        bindContentView(i, view);
        return view;
    }

    protected View createTitleView(View v) {
        if (v != null) {
            return v;
        }
        v = this.mLayoutInflater.inflate(R.layout.list_grid_title_item, null);
        TitleHolder holder = new TitleHolder();
        holder.mTimeTextView = (TextView) v.findViewById(R.id.time_tv);
        holder.mTitleTextView = (TextView) v.findViewById(R.id.title_tv);
        holder.mCheckBox = (CheckBox) v.findViewById(R.id.checkbox);
        holder.mCheckBox.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                ((ListGridTitleItem) view.getTag()).toggle();
                BaseListGridAdapter.this.notifyDataSetChanged();
                BaseListGridAdapter.this.checkedIsAllChecked();
            }
        });
        v.setTag(holder);
        return v;
    }

    protected View createContentView(View v) {
        if (v != null) {
            return v;
        }
        v = this.mLayoutInflater.inflate(R.layout.list_grid_item, null);
        ListGridItemAdapter adapter = new ListGridItemAdapter(this);
        adapter.initHolder(v);
        v.setTag(adapter);
        return v;
    }

    protected void bindTitleView(int i, View view) {
        ListGridTitleItem titleItem = (ListGridTitleItem) getItem(i);
        TitleHolder titleHolder = (TitleHolder) view.getTag();
        titleHolder.mTimeTextView.setText(titleItem.getDes());
        titleHolder.mTitleTextView.setText(titleItem.getTitle());
        titleHolder.mCheckBox.setTag(titleItem);
        titleHolder.mCheckBox.setChecked(titleItem.isChecked());
    }

    protected void bindContentView(int pos, View view) {
        ((ListGridItemAdapter) view.getTag()).bindView((ListGridContentItem) getItem(pos));
    }

    public void onItemCheckBoxClick(View view) {
        ((ITrashItem) view.getTag()).toggle();
        notifyDataSetChanged();
        checkedIsAllChecked();
    }

    public void onItemClick(View view) {
        if (this.mStatisticalData != null) {
            this.mStatisticalData.sendItemPreviewMsg();
        }
        custOnItemClick(view);
    }

    protected void custOnItemClick(View view) {
        PhotoViewerActivity.startPhotoViewer(this.mContext, ((ITrashItem) view.getTag()).getTrashPath());
    }

    private void checkedIsAllChecked() {
        int checkedCount = 0;
        long checkedSize = 0;
        long totalSize = 0;
        for (BaseListGridItem item : this.mData) {
            if (item instanceof ListGridTitleItem) {
                ListGridTitleItem i = (ListGridTitleItem) item;
                i.refreshCheckedData();
                checkedCount += i.getCheckedCount();
                checkedSize += i.getCheckedSize();
                totalSize += i.getTotalSize();
            }
        }
        if (this.mSizeChangeListener != null) {
            boolean z;
            OnSizeChangeListener onSizeChangeListener = this.mSizeChangeListener;
            if (checkedCount == this.mSource.size()) {
                z = true;
            } else {
                z = false;
            }
            onSizeChangeListener.onSizeChanged(checkedSize, totalSize, z, checkedCount);
        }
    }

    public void setAllItemChecked(boolean value) {
        for (BaseListGridItem item : this.mData) {
            if (item instanceof ListGridTitleItem) {
                item.setChecked(value);
            }
        }
        checkedIsAllChecked();
        notifyDataSetChanged();
    }

    public List<ITrashItem> getCheckedList() {
        List<ITrashItem> result = new ArrayList();
        for (ITrashItem it : this.mSource) {
            if (it.isChecked()) {
                result.add(it);
            }
        }
        return result;
    }
}
