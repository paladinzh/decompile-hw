package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.os.Bundle;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.CommonHandler.MessageHandler;
import com.huawei.systemmanager.spacecleanner.statistics.SpaceStatsUtils;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.deepscan.DownloadAppTrashItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.DownloadItem;
import com.huawei.systemmanager.spacecleanner.view.ListItemAlertDialog;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.List;

public class ListDownloadFragment extends ListCommonFragment implements MessageHandler {
    private static final int MSG_CLICK_ORDER_SELECT = 1000;
    private static final int MSG_ORDER_STATE_CANCLE_TYPE = 1003;
    private static final int MSG_ORDER_STATE_FILES_FROM = 1001;
    private static final int MSG_ORDER_STATE_FILES_TYPE = 1002;
    private static final int ORDER_STATE_FILES_CANCLE = 2;
    private static final int ORDER_STATE_FILES_FROM = 0;
    private static final int ORDER_STATE_FILES_TYPE = 1;
    private static final String TAG = "ListDownloadFragment";
    private MenuItem mAllCheckMenuItem;
    private MenuItem mCleanMenuItem;
    private int mCurrentOrderState = 0;
    private OnClickListener mItemClick = new OnClickListener() {
        public void onClick(DialogInterface dialog, int which) {
            switch (which) {
                case 0:
                    ListDownloadFragment.this.mHandle.obtainMessage(1001).sendToTarget();
                    break;
                case 1:
                    ListDownloadFragment.this.mHandle.obtainMessage(1002).sendToTarget();
                    break;
                default:
                    ListDownloadFragment.this.mHandle.obtainMessage(1003).sendToTarget();
                    break;
            }
            dialog.dismiss();
        }
    };
    private ListItemAlertDialog mListItemAlertDialog;
    private MenuItem mOrderMenuItem;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        resetState(0, false);
    }

    private void resetState(int state, boolean refresh) {
        this.mCurrentOrderState = state;
        if (refresh) {
            initAdapter();
        }
    }

    public void initAdapter() {
        List totalList = null;
        switch (this.mCurrentOrderState) {
            case 0:
                totalList = DownloadItem.covert(this.mScanHandler, 0);
                break;
            case 1:
                totalList = DownloadItem.covert(this.mScanHandler, 1);
                break;
        }
        initTotalList(totalList);
        this.mAdapter.setData(totalList);
        updateSelectState();
    }

    public void recordDeleteOp() {
        List<ITrashItem> checkedList = getCheckedList();
        HashMap<String, Integer> typeAndCount = new HashMap();
        Integer count;
        switch (this.mCurrentOrderState) {
            case 0:
                for (ITrashItem trashItem : checkedList) {
                    if (trashItem instanceof DownloadAppTrashItem) {
                        String pkgName = ((DownloadAppTrashItem) trashItem).getFileFromPkg();
                        if (TextUtils.isEmpty(pkgName)) {
                            HwLog.e(TAG, "DownloadAppTrashItem's pkg name is empty ");
                        } else {
                            count = (Integer) typeAndCount.get(pkgName);
                            if (count == null) {
                                count = Integer.valueOf(0);
                            }
                            typeAndCount.put(pkgName, Integer.valueOf(count.intValue() + 1));
                        }
                    }
                }
                break;
            case 1:
                for (ITrashItem trashItem2 : checkedList) {
                    if (trashItem2 instanceof DownloadAppTrashItem) {
                        String fileType = String.valueOf(((DownloadAppTrashItem) trashItem2).getFileType());
                        if (TextUtils.isEmpty(fileType)) {
                            HwLog.e(TAG, "DownloadAppTrashItem's fileType name is empty ");
                        } else {
                            count = (Integer) typeAndCount.get(fileType);
                            if (count == null) {
                                count = Integer.valueOf(0);
                            }
                            typeAndCount.put(fileType, Integer.valueOf(count.intValue() + 1));
                        }
                    }
                }
                break;
            default:
                HwLog.e(TAG, "mCurrentOrderState is not invalidate! ");
                break;
        }
        SpaceStatsUtils.reportDownloadDeleteFileOp(this.mCurrentOrderState, typeAndCount);
    }

    public void handleMessage(Message msg) {
        super.handleMessage(msg);
        switch (msg.what) {
            case 1000:
                showOrderStateDialog();
                return;
            case 1001:
                resetState(0, true);
                SpaceStatsUtils.reportDownloadSortDialogClick(0);
                return;
            case 1002:
                resetState(1, true);
                SpaceStatsUtils.reportDownloadSortDialogClick(1);
                return;
            case 1003:
                SpaceStatsUtils.reportDownloadSortDialogClick(2);
                return;
            default:
                return;
        }
    }

    private void showOrderStateDialog() {
        dimissShowingDialog();
        this.mListItemAlertDialog = new ListItemAlertDialog(this.mItemClick);
        FragmentTransaction ft = getFragmentManager().beginTransaction();
        Bundle bundle = new Bundle();
        bundle.putInt(ListItemAlertDialog.ARG_TRASH_SORT, this.mCurrentOrderState);
        bundle.putInt(ListItemAlertDialog.ARG_ARRAY_SPINNER, R.array.space_clean_downlad_spinner_textarray);
        this.mListItemAlertDialog.setArguments(bundle);
        if (!this.mListItemAlertDialog.isVisible()) {
            this.mListItemAlertDialog.show(ft, "normal trash_sort_dialog");
        }
    }

    public void handleOpenDialog(ITrashItem item) {
        if (item != null) {
            showDetailDialogFragment(item);
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                this.mHandle.obtainMessage(8, item).sendToTarget();
                break;
            case R.id.select_all:
                this.mHandle.obtainMessage(7, item).sendToTarget();
                break;
            case R.id.order_all:
                this.mHandle.obtainMessage(1000, item).sendToTarget();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.download_clean_menu, menu);
        this.mAllCheckMenuItem = menu.findItem(R.id.select_all);
        this.mCleanMenuItem = menu.findItem(R.id.cache_clean_menu_item);
        this.mOrderMenuItem = menu.findItem(R.id.order_all);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateSelectState();
    }

    public void updateMenu(long checkSize, int allNum, int checkNum, boolean allChecked) {
        if (this.mAllCheckMenuItem != null && this.mCleanMenuItem != null && this.mOrderMenuItem != null) {
            updateMenu(this.mAllCheckMenuItem, this.mCleanMenuItem, this.mOrderMenuItem, allNum, checkNum, allChecked);
            if (this.mOperationTitleID == 0) {
                this.mCleanMenuItem.setTitle(getApplicationContext().getString(R.string.common_delete));
            } else {
                this.mCleanMenuItem.setTitle(getApplicationContext().getString(this.mOperationTitleID));
            }
        }
    }

    private void updateMenu(MenuItem mAllCheckMenuItem, MenuItem mCleanMenuItem, MenuItem mOrderMenuItem, int totalNum, int checkNum, boolean allChecked) {
        boolean newMenuState;
        boolean z;
        boolean z2 = true;
        boolean preMenuState = mAllCheckMenuItem.isVisible();
        if (totalNum > 0) {
            newMenuState = true;
        } else {
            newMenuState = false;
        }
        if ((preMenuState ^ newMenuState) != 0) {
            mAllCheckMenuItem.setVisible(newMenuState);
            mCleanMenuItem.setVisible(newMenuState);
            mOrderMenuItem.setVisible(newMenuState);
            if (newMenuState) {
                Activity ac = getActivity();
                if (ac != null) {
                    ac.invalidateOptionsMenu();
                }
            }
        }
        if (allChecked) {
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_pressed);
            mAllCheckMenuItem.setTitle(R.string.unselect_all);
            mAllCheckMenuItem.setChecked(true);
        } else {
            mAllCheckMenuItem.setIcon(R.drawable.menu_check_status);
            mAllCheckMenuItem.setTitle(R.string.select_all);
            mAllCheckMenuItem.setChecked(false);
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

    public void dimissShowingDialog() {
        if (this.mDetailDialog != null && this.mDetailDialog.isAdded()) {
            this.mDetailDialog.dismiss();
        }
        if (this.mTrashDetailDialogFragment != null && this.mTrashDetailDialogFragment.isAdded()) {
            this.mTrashDetailDialogFragment.dismiss();
        }
        if (this.deleteFragment != null && this.deleteFragment.isAdded()) {
            this.deleteFragment.dismiss();
        }
        if (this.mListItemAlertDialog != null && this.mListItemAlertDialog.isAdded()) {
            this.mListItemAlertDialog.dismiss();
        }
    }
}
