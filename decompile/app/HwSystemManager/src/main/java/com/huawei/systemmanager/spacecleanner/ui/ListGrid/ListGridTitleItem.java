package com.huawei.systemmanager.spacecleanner.ui.ListGrid;

import android.text.format.DateUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.spacecleanner.ui.ListGrid.ListGridItem.SimpleListGridItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class ListGridTitleItem extends SimpleListGridItem {
    private static final String TAG = "ListGridTitleItem";
    private boolean isTotalChecked;
    private int mCheckedCount;
    private long mCheckedSize;
    private String mDes;
    private List<ITrashItem> mLists = new ArrayList();
    private String mTitle;
    private int mTotalCount;
    private long mTotalSize;

    public ListGridTitleItem(byte month, short year) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(1, year);
        calendar.set(2, month - 1);
        this.mDes = DateUtils.formatDateTime(GlobalContext.getContext(), calendar.getTime().getTime(), 36);
    }

    public int getType() {
        return 0;
    }

    public String getTitle() {
        return this.mTitle;
    }

    public String getDes() {
        return this.mDes;
    }

    public int getCount() {
        return this.mTotalCount;
    }

    public void setCount(int count) {
        this.mTotalCount = count;
        this.mTitle = GlobalContext.getContext().getResources().getQuantityString(R.plurals.space_clean_files, this.mTotalCount, new Object[]{Integer.valueOf(this.mTotalCount)});
    }

    public void toggle() {
        this.isTotalChecked = !this.isTotalChecked;
        setItemChecked();
    }

    public boolean isChecked() {
        return this.isTotalChecked;
    }

    public void setChecked(boolean value) {
        this.isTotalChecked = value;
        setItemChecked();
    }

    public void setLists(List<ITrashItem> s) {
        if (s != null) {
            this.mLists.clear();
            this.mLists.addAll(s);
        }
        HwLog.i(TAG, "mList size is:  " + this.mLists.size());
    }

    private void setItemChecked() {
        if (!this.mLists.isEmpty()) {
            for (ITrashItem item : this.mLists) {
                item.setChecked(this.isTotalChecked);
            }
        }
    }

    public void refreshCheckedData() {
        boolean z = false;
        this.mCheckedCount = 0;
        this.mCheckedSize = 0;
        this.mTotalSize = 0;
        if (!this.mLists.isEmpty()) {
            for (ITrashItem item : this.mLists) {
                if (item.isChecked()) {
                    this.mCheckedCount++;
                    this.mCheckedSize += item.getTrashSize();
                }
                this.mTotalSize += item.getTrashSize();
            }
        }
        if (this.mCheckedCount == this.mTotalCount) {
            z = true;
        }
        this.isTotalChecked = z;
    }

    public long getCheckedSize() {
        return this.mCheckedSize;
    }

    public long getTotalSize() {
        return this.mTotalSize;
    }

    public int getCheckedCount() {
        return this.mCheckedCount;
    }
}
