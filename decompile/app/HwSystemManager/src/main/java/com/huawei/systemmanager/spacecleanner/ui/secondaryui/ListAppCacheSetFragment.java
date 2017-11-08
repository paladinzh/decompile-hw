package com.huawei.systemmanager.spacecleanner.ui.secondaryui;

import android.app.Activity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.ITrashItem;
import com.huawei.systemmanager.spacecleanner.ui.commonitem.TrashItemGroup;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.AppDataDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.LargeFileDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.VideoDeepItem;
import com.huawei.systemmanager.spacecleanner.ui.spacemanager.item.WeChatDeepItem;
import com.huawei.systemmanager.spacecleanner.utils.VedioCacheUtils;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;

public class ListAppCacheSetFragment extends ListCommonFragment {
    private static final String TAG = "ListAppCacheSetFragment";
    private MenuItem mAllCheckMenuItem;
    private MenuItem mCleanMenuItem;

    public void onDestroy() {
        setVedioTrashSize();
        super.onDestroy();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.cache_clean_menu, menu);
        this.mAllCheckMenuItem = menu.findItem(R.id.select_all);
        this.mCleanMenuItem = menu.findItem(R.id.cache_clean_menu_item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        updateSelectState();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.cache_clean_menu_item:
                this.mHandle.obtainMessage(8, item).sendToTarget();
                break;
            case R.id.select_all:
                this.mHandle.obtainMessage(7, item).sendToTarget();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void updateMenu(long checkSize, int allNum, int checkNum, boolean allChecked) {
        if (this.mAllCheckMenuItem != null && this.mCleanMenuItem != null) {
            updateMenu(this.mAllCheckMenuItem, this.mCleanMenuItem, allNum, checkNum, allChecked);
            if (this.mOperationTitleID == 0) {
                this.mCleanMenuItem.setTitle(getApplicationContext().getString(R.string.common_delete));
            } else {
                this.mCleanMenuItem.setTitle(getApplicationContext().getString(this.mOperationTitleID));
            }
        }
    }

    public void initAdapter() {
        List mTotalList = null;
        switch (this.mListAppType) {
            case 4:
                mTotalList = LargeFileDeepItem.convert(this.mScanHandler);
                initTotalList(mTotalList);
                break;
            case VideoDeepItem.TRASH_TYPE /*65792*/:
                mTotalList = VideoDeepItem.convert(this.mScanHandler);
                initTotalList(mTotalList);
                itemOperation(mTotalList, 65536, 4, Boolean.valueOf(true));
                if (!VedioCacheUtils.isRedPoint()) {
                    this.mNeedChangeCkeckPoint = false;
                    break;
                }
                this.mNeedChangeCkeckPoint = true;
                VedioCacheUtils.saveRedPoint(false);
                break;
            case 1048576:
                mTotalList = WeChatDeepItem.getExpandListSource(this.mScanHandler, this.mIndex);
                break;
            case AppDataDeepItem.TRASH_TYPE /*2189369*/:
                mTotalList = AppDataDeepItem.covert(this.mScanHandler);
                initTotalList(mTotalList);
                itemOperation(mTotalList, 1, 2, Boolean.valueOf(true));
                break;
            default:
                HwLog.e(TAG, "The type of trash is not support! trashType:" + this.mListAppType);
                break;
        }
        this.mAdapter.setData(mTotalList);
        if (this.mListAppType == 1048576) {
            this.mAdapter.collapseGroup();
        }
    }

    public void recordDeleteOp() {
    }

    private void updateMenu(MenuItem mAllCheckMenuItem, MenuItem mCleanMenuItem, int totalNum, int checkNum, boolean allChecked) {
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

    private void setVedioTrashSize() {
        if (!this.mNeedChangeCkeckPoint && !this.mIsCleanded) {
            return;
        }
        if (this.mAdapter == null) {
            HwLog.e(TAG, "setVedioTrashSize called, but adapter is null!");
        } else if (this.mListAppType != VideoDeepItem.TRASH_TYPE) {
            HwLog.d(TAG, "setVedioTrashSize called, but is not VideoDeep type!");
        } else {
            long totalSize = 0;
            for (TrashItemGroup<ITrashItem> groupIt : this.mAdapter.getData()) {
                for (ITrashItem item : groupIt) {
                    if (item.getTrashType() == 65536) {
                        totalSize += item.getTrashSizeCleaned(false);
                    }
                }
            }
            VedioCacheUtils.saveSizeKey(totalSize);
        }
    }

    public void dimissShowingDialog() {
        if (!(this.mDetailDialog == null || !this.mDetailDialog.isAdded() || !this.mDetailDialog.isVisible() || getActivity() == null || getActivity().isFinishing() || getActivity().isDestroyed())) {
            this.mDetailDialog.dismiss();
        }
        if (this.mTrashDetailDialogFragment != null && this.mTrashDetailDialogFragment.isAdded()) {
            this.mTrashDetailDialogFragment.dismiss();
        }
        if (this.deleteFragment != null && this.deleteFragment.isAdded()) {
            this.deleteFragment.dismiss();
        }
    }
}
