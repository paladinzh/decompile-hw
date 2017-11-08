package com.huawei.systemmanager.comm.component;

import android.app.Activity;
import android.app.ProgressDialog;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.component.Item.CheckItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.LinkedList;
import java.util.List;

public abstract class SelectListFragment<T extends CheckItem> extends BaseListFragment<T> {
    public static final String TAG = SelectListFragment.class.getSimpleName();
    private boolean mAllChecked = false;
    private int mCheckedNum;
    private ProgressDialog mProgressDlg;

    public List<T> getCheckedList() {
        List<T> result = new LinkedList();
        if (this.mAdapter == null) {
            HwLog.i(TAG, "getCheckedList ,adapter is null");
            return result;
        }
        for (T item : this.mAdapter.getData()) {
            if (item.isChecked()) {
                result.add(item);
            }
        }
        return result;
    }

    public void updateSelectState() {
        List<T> list = this.mAdapter.getData();
        int checkableNum = 0;
        int checkNum = 0;
        for (T item : list) {
            if (item.isCheckable()) {
                checkableNum++;
                if (item.isChecked()) {
                    checkNum++;
                }
            }
        }
        if (checkNum != checkableNum || checkNum == 0) {
            this.mAllChecked = false;
        } else {
            this.mAllChecked = true;
        }
        this.mAdapter.notifyDataSetChanged();
        this.mCheckedNum = checkNum;
        onCheckNumChanged(list.size(), checkableNum, checkNum, this.mAllChecked);
    }

    public int getCheckNum() {
        return this.mCheckedNum;
    }

    protected void onListItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        ((CheckItem) getAdapter().getItem(position)).toggle();
        updateSelectState();
    }

    protected void onAdapterDataChange() {
        updateSelectState();
    }

    @Deprecated
    protected void onCheckNumChanged(int allNum, int checkNum, boolean allChecked) {
    }

    protected void onCheckNumChanged(int allNum, int checkedAbleNum, int checkedNum, boolean allChecked) {
        onCheckNumChanged(allNum, checkedNum, allChecked);
    }

    public void clickAllSelect(boolean checked) {
        if (checked != this.mAllChecked) {
            if (this.mAdapter == null) {
                HwLog.i(TAG, "onAllSelectButtonClick adapte is null");
                return;
            }
            for (CheckItem item : this.mAdapter.getData()) {
                item.setChecked(checked);
            }
            updateSelectState();
        }
    }

    public void clickAllSelect() {
        clickAllSelect(!this.mAllChecked);
    }

    public boolean isAllChecked() {
        return this.mAllChecked;
    }

    public void showProgressDialog() {
        if (!(this.mProgressDlg == null || !this.mProgressDlg.isShowing() || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed())) {
            this.mProgressDlg.dismiss();
        }
        Activity ac = getActivity();
        if (ac != null) {
            this.mProgressDlg = ProgressDialog.show(ac, null, getStringEx(R.string.space_common_msg_cleaning), true, false);
        }
    }

    public void dismissProgressDialog() {
        if (this.mProgressDlg != null && this.mProgressDlg.isShowing() && getActivity() != null && !getActivity().isFinishing() && !getActivity().isDestroyed()) {
            this.mProgressDlg.dismiss();
            this.mProgressDlg = null;
        }
    }

    public void updateMenu(MenuItem mAllCheckMenuItem, MenuItem mCleanMenuItem, int totalNum, int checkNum, boolean allChecked) {
        boolean newMenuState;
        boolean z;
        boolean z2 = true;
        boolean preMenuState = mAllCheckMenuItem.isVisible();
        if (!isDataLoadFinished() || totalNum <= 0) {
            newMenuState = false;
        } else {
            newMenuState = true;
        }
        if ((preMenuState ^ newMenuState) != 0) {
            mAllCheckMenuItem.setVisible(newMenuState);
            mCleanMenuItem.setVisible(newMenuState);
            if (newMenuState) {
                Activity ac = getActivity();
                if (ac != null) {
                    ac.invalidateOptionsMenu();
                }
            }
        }
        if (allChecked) {
            mAllCheckMenuItem.setChecked(true);
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_pressed);
            mAllCheckMenuItem.setTitle(R.string.unselect_all);
        } else {
            mAllCheckMenuItem.setChecked(false);
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_status);
            mAllCheckMenuItem.setTitle(R.string.select_all);
        }
        if (totalNum > 0) {
            z = true;
        } else {
            z = false;
        }
        mAllCheckMenuItem.setEnabled(z);
        if (checkNum <= 0) {
            z2 = false;
        }
        mCleanMenuItem.setEnabled(z2);
    }
}
