package com.android.rcs.ui;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;
import com.android.mms.ui.FavoritesFragment;
import com.android.mms.ui.FavoritesFragment.MenuEx;
import com.android.mms.ui.FavoritesListAdapter;
import com.android.mms.ui.FavoritesListView;
import com.android.mms.ui.HwCustComposeMessageImpl;
import com.android.mms.ui.MessageItem;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.utils.RcsProfileUtils;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import java.io.File;

public class RcsFavoritesFragment {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private RcsChatMessageForwarder mChatForwarder;
    private Context mContext;
    RcsFileTransMessageForwarder mForwarder;
    private FavoritesFragment mFragment;

    public RcsFavoritesFragment(Fragment fragment) {
        this.mFragment = (FavoritesFragment) fragment;
        this.mContext = this.mFragment.getContext();
        if (RcsCommonConfig.isRCSSwitchOn()) {
            this.mChatForwarder = new RcsChatMessageForwarder();
            this.mForwarder = new RcsFileTransMessageForwarder();
        }
    }

    public void onOptionsItemSelected(FavoritesListView ListView, FavoritesListAdapter listAdapter, MenuItem item) {
        if (this.isRcsOn) {
            Integer[] selection = ListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
            MessageItem msgItem = null;
            if (selection != null && 1 == selection.length) {
                listAdapter.setInFavorites(true);
                if (listAdapter.getRcsMessageListAdapter() != null) {
                    msgItem = listAdapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(selection[0].intValue(), listAdapter.getCursor());
                }
            }
            switch (item.getItemId()) {
                case 278925326:
                    if (msgItem != null) {
                        RcsMessageItem custMessageItem = msgItem.getRcsMessageItem();
                        if (custMessageItem.getFileItem() != null) {
                            if (isSetCopyToSDCardVisible(msgItem)) {
                                RcsMediaFileUtils.saveFileToDownLoad(this.mContext, custMessageItem.getFileItem().mImAttachmentPath);
                                break;
                            }
                        }
                        MLog.i("RcsFavoritesFragment", "msgItem or msgItem.mFileItem is null");
                        break;
                    }
                    break;
            }
        }
    }

    public boolean saveFileToPhone(MessageItem msgItem) {
        if (msgItem == null) {
            MLog.w("RcsFavoritesFragment", "msgItem is null!");
            return false;
        }
        RcsMessageItem custMessageItem = msgItem.getRcsMessageItem();
        if (custMessageItem.getFileItem() == null) {
            MLog.i("RcsFavoritesFragment", "msgItem or msgItem.mFileItem is null");
            return false;
        }
        if (isSetCopyToSDCardVisible(msgItem)) {
            RcsMediaFileUtils.saveFileToDownLoad(this.mContext, custMessageItem.getFileItem().mImAttachmentPath);
        } else {
            Toast.makeText(this.mContext, R.string.text_file_not_exist, 0).show();
        }
        return true;
    }

    public void onSelectChange(MenuEx mMenuEx, FavoritesListView ListView, FavoritesListAdapter listAdapter, Menu optionMenu) {
        if (this.isRcsOn) {
            mMenuEx.setItemVisible(278925326, false);
            Integer[] selection = ListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
            if (selection == null || (selection.length <= 1 && selection.length != 0)) {
                if (selection != null && 1 == selection.length) {
                    listAdapter.setInFavorites(true);
                    MessageItem msgItem = listAdapter.getRcsMessageListAdapter().getMessageItemWithIdAssigned(selection[0].intValue(), listAdapter.getCursor());
                    boolean isLoc = RcsMapLoader.isLocItem(listAdapter.getCursor(), selection[0].intValue());
                    if (msgItem == null) {
                        MLog.i("RcsFavoritesFragment", "msgItem is null");
                        return;
                    }
                    RcsMessageItem custMessageItem = msgItem.getRcsMessageItem();
                    if (custMessageItem.getFileItem() == null) {
                        if (isLoc) {
                            mMenuEx.setItemEnabled(278925319, false);
                            mMenuEx.setItemEnabled(278925343, false);
                            mMenuEx.setItemEnabled(278927472, false);
                            mMenuEx.setItemEnabled(278925316, true);
                        }
                        MLog.i("RcsFavoritesFragment", "msgItem or msgItem.mFileItem is null");
                        return;
                    }
                    if (isSetCopyToSDCardVisible(msgItem)) {
                        mMenuEx.setItemVisible(278925343, false);
                        mMenuEx.setItemVisible(278925326, true);
                        if (custMessageItem.getFileItem().isVCardFileTypeMsg()) {
                            mMenuEx.setItemEnabled(278925326, false);
                        }
                    }
                    if (!(custMessageItem.getFileItem().mImAttachmentPath == null || new File(custMessageItem.getFileItem().mImAttachmentPath).exists())) {
                        mMenuEx.setItemEnabled(278925326, false);
                        mMenuEx.setItemEnabled(278925316, false);
                    }
                }
                switchEditMenuPlus(optionMenu, mMenuEx);
                return;
            }
            mMenuEx.setItemVisible(278925343, false);
            mMenuEx.setItemVisible(278925326, false);
            switchEditMenuPlus(optionMenu, mMenuEx);
        }
    }

    public void onItemLongClick(FavoritesListView ListView, int position) {
        if (this.isRcsOn) {
            ListView.getRecorder().getRcsSelectRecorder().addPosition(position);
        }
    }

    private boolean isSetCopyToSDCardVisible(MessageItem msgItem) {
        if (5 == msgItem.getRcsMessageItem().mRcsMsgType || 3 == msgItem.getRcsMessageItem().mRcsMsgType || 6 == msgItem.getRcsMessageItem().mRcsMsgType) {
            return true;
        }
        return false;
    }

    public boolean isSaveFile(MessageItem msgItem) {
        if (5 == msgItem.getRcsMessageItem().mRcsMsgType || 3 == msgItem.getRcsMessageItem().mRcsMsgType || 6 == msgItem.getRcsMessageItem().mRcsMsgType) {
            return true;
        }
        return false;
    }

    private void switchEditMenuPlus(Menu optionMenu, MenuEx mMenuEx) {
        MLog.v("RcsFavoritesFragment", "switchEditMenuPlus start.");
        if (this.mFragment != null) {
            FavoritesListView listView = this.mFragment.getListView();
            FavoritesListAdapter listAdapter = this.mFragment.getListAdapter();
            if (listAdapter != null && listView != null) {
                Cursor cursor = listAdapter.getCursor();
                if (listAdapter.isCursorValid(cursor)) {
                    Integer[] positions = listView.getRecorder().getRcsSelectRecorder().getAllSelectPositions();
                    if (positions != null && positions.length > 0) {
                        boolean hasFtItem = false;
                        boolean hasMoreThanOne = false;
                        boolean hasWithoutFtItem = false;
                        int prePosition = cursor.getPosition();
                        for (Integer intValue : positions) {
                            cursor.moveToPosition(intValue.intValue());
                            if (listAdapter.isCursorValid(cursor)) {
                                boolean hasLocItem = RcsMapLoader.isLocItem(cursor.getString(4));
                                int rcsMsgNormalType = RcsProfileUtils.getRcsMsgType(cursor);
                                if (3 == rcsMsgNormalType || 6 == rcsMsgNormalType || 5 == rcsMsgNormalType || hasLocItem) {
                                    hasFtItem = true;
                                } else {
                                    hasWithoutFtItem = true;
                                }
                                if (positions.length > 1) {
                                    hasMoreThanOne = true;
                                }
                                if (hasWithoutFtItem && hasFtItem) {
                                    break;
                                }
                                MLog.d("RcsFavoritesFragment", "switchEditMenuPlus hasFtItem: " + hasFtItem + ", hasWithoutFtItem = " + hasWithoutFtItem);
                            }
                        }
                        cursor.moveToPosition(prePosition);
                        boolean itemVisible = positions.length > 0 ? hasFtItem ? false : getItemVisible(optionMenu, 278925319) : false;
                        mMenuEx.setItemEnabled(278925319, itemVisible);
                        itemVisible = positions.length == 1 ? hasFtItem ? false : getItemVisible(optionMenu, 278925343) : false;
                        mMenuEx.setItemEnabled(278925343, itemVisible);
                        itemVisible = positions.length > 0 ? (hasFtItem && (hasWithoutFtItem || hasMoreThanOne)) ? false : getItemVisible(optionMenu, 278925316) : false;
                        mMenuEx.setItemEnabled(278925316, itemVisible);
                    }
                }
            } else {
                return;
            }
        }
        MLog.d("RcsFavoritesFragment", "switchEditMenuPlus start.");
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data, FavoritesListAdapter listAdapter) {
        if (this.isRcsOn) {
            switch (requestCode) {
                case HwCustComposeMessageImpl.REQUEST_CODE_MEDIA_COMPRESS_FORWARD /*150*/:
                    RcsTransaction.sendFileForForward(data, this.mContext);
                    break;
                case 160125:
                case 160126:
                    if (data != null) {
                        if (this.mForwarder != null) {
                            this.mForwarder.onForwardResult(data);
                            break;
                        }
                    }
                    MLog.w("RcsFavoritesFragment", "onActivityResult REQUEST_CODE_PICK_CONTACTS_FT_FORWARD data is null.");
                    return;
                    break;
                case 160127:
                    if (this.mChatForwarder != null) {
                        this.mChatForwarder.rcsActivityResult(data);
                        break;
                    }
                    break;
            }
        }
    }

    public boolean getItemVisible(Menu optionMenu, int itemID) {
        if (optionMenu == null) {
            return false;
        }
        MenuItem menuItem = optionMenu.findItem(itemID);
        if (menuItem != null) {
            return menuItem.isEnabled();
        }
        return false;
    }

    public void toForward(Context context) {
        if (this.isRcsOn) {
            MLog.d("RcsFavoritesFragment", "toForward. begin");
            if (this.mForwarder != null) {
                this.mForwarder.forwardFt();
            }
        }
    }

    public boolean detectMessageToForwardForFt(Integer[] selection, Cursor cursor) {
        if (!this.isRcsOn || this.mForwarder == null) {
            return false;
        }
        this.mForwarder.setFragment(this.mFragment);
        this.mForwarder.setMessageListAdapter(this.mFragment.getListAdapter());
        this.mForwarder.setMessageKind(3);
        return this.mForwarder.detectMessageToForwardForFt(selection, cursor);
    }

    public boolean detectMessageToForwardForLoc(Integer[] selection, Cursor cursor) {
        if (this.isRcsOn && this.mForwarder != null) {
            return this.mForwarder.detectMessageToForwardForLoc(selection, cursor);
        }
        return false;
    }

    public boolean detectMessageToForwardForLoc(FavoritesListView msgListView, Cursor cursor) {
        if (this.isRcsOn && this.mForwarder != null) {
            return this.mForwarder.detectMessageToForwardForLoc(msgListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions(), cursor);
        }
        return false;
    }

    public void forwardLoc(Context context) {
        if (this.isRcsOn && this.mForwarder != null) {
            this.mForwarder.setFragment(this.mFragment);
            this.mForwarder.setMessageListAdapter(this.mFragment.getListAdapter());
            this.mForwarder.setMessageKind(3);
            this.mForwarder.forwardLoc();
        }
    }

    public void switchToEdit(MenuEx menuEx) {
        if (this.isRcsOn) {
            menuEx.addOverflowMenu(278925326, R.string.copy_to_sdcard);
        }
    }

    public boolean isInEditMode(FavoritesListView listView) {
        if (this.isRcsOn) {
            return listView.isInEditMode();
        }
        return false;
    }

    public void onSelectChangeBegin(MenuEx menuEx) {
        if (this.isRcsOn) {
            menuEx.switchToEdit(true);
        }
    }

    public void prepareFwdMsg(String msgBody) {
        if (this.isRcsOn && this.mChatForwarder != null) {
            this.mChatForwarder.setFragment(this.mFragment);
            this.mChatForwarder.launchContactsPicker(160127, msgBody);
        }
    }

    public boolean isRcsSwitchOn() {
        return this.isRcsOn;
    }
}
