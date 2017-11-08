package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.content.ContentResolver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.ContentObserver;
import android.database.Cursor;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.ContactsContract.Contacts;
import android.provider.Telephony.Mms.Draft;
import android.text.Selection;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnFocusChangeListener;
import android.view.ViewGroup;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.model.MediaModel;
import com.android.mms.model.VcardModel;
import com.android.mms.transaction.MessagingNotification;
import com.android.mms.ui.MessageListAdapter.OnDataSetChangedListener;
import com.android.mms.util.ShareUtils;
import com.android.rcs.RcsCommonConfig;
import com.android.rcs.ui.RcsFavoritesFragment;
import com.android.rcs.ui.RcsMessageItem;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.EncodedStringValue;
import com.google.android.mms.pdu.PduPersister;
import com.google.android.mms.pdu.SendReq;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.cust.HwCustUtils;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.NoMessageView;
import com.huawei.mms.ui.SpandTextView;
import com.huawei.mms.util.FavoritesUtils;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwMessageUtils;
import com.huawei.mms.util.MmsPduUtils;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.rcs.ui.RcsFileTransGroupMessageItem;
import com.huawei.rcs.utils.map.abs.RcsMapLoader;
import java.io.File;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Pattern;

public class FavoritesFragment extends HwBaseFragment {
    EmuiActionBar mActionBar;
    public ActionMode mActionMode = null;
    private AsyncDialog mAsyncDialog;
    HwCustFavoritesFragment mCustFavoritesFragment;
    private EditHandler mDeleteHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            if (isAllSelected) {
                FavoritesFragment.this.mQueryHandler.startDelete(101010002, null, FavoritesUtils.URI_FAV, null, null);
            } else {
                List<Long> smsSelected = new ArrayList();
                List<Long> mmsSelected = new ArrayList();
                for (Long id : selectedItems) {
                    if (id.longValue() < 0) {
                        mmsSelected.add(Long.valueOf(-id.longValue()));
                    } else {
                        smsSelected.add(id);
                    }
                }
                FavoritesFragment.this.mQueryHandler.startDelete(101010003, null, FavoritesUtils.URI_FAV_SMS, FavoritesUtils.getSelectCondition(smsSelected), null);
                FavoritesFragment.this.mQueryHandler.startDelete(101010002, null, FavoritesUtils.URI_FAV_MMS, FavoritesUtils.getSelectCondition(mmsSelected), null);
            }
            return selectedItems.length;
        }
    };
    private View mFooterView = null;
    private EditHandler mForwardHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            StringBuffer forwardMsgList = new StringBuffer();
            Cursor cursor = FavoritesFragment.this.mListAdapter.getCursor();
            if (cursor == null) {
                return 0;
            }
            if (FavoritesFragment.this.mHwCust != null && FavoritesFragment.this.mHwCust.detectMessageToForwardForFt(FavoritesFragment.this.mListView.getRecorder().getRcsSelectRecorder().getAllSelectPositions(), cursor)) {
                FavoritesFragment.this.mHwCust.toForward(FavoritesFragment.this.getContext());
                return selectedItems.length;
            } else if (FavoritesFragment.this.mHwCust == null || !FavoritesFragment.this.mHwCust.detectMessageToForwardForLoc(FavoritesFragment.this.mListView, cursor)) {
                MessageItem msgItem;
                if (selectedItems.length == 1) {
                    int length = selectedItems.length;
                    int i = 0;
                    while (i < length) {
                        msgItem = FavoritesFragment.this.mListAdapter.getCachedMessageItemWithIdAssigned("sms", selectedItems[i].longValue(), cursor, 1);
                        if (msgItem == null) {
                            i++;
                        } else {
                            Message msg = Message.obtain();
                            msg.what = 1002;
                            msg.obj = msgItem;
                            FavoritesFragment.this.mHandler.sendMessage(msg);
                            return selectedItems.length;
                        }
                    }
                }
                Arrays.sort(selectedItems);
                int count = selectedItems.length;
                StringBuffer cleanForwardMsgList = new StringBuffer();
                String cleanForwardString = "";
                for (int i2 = count; i2 > 0; i2--) {
                    String forwardString = "";
                    msgItem = FavoritesFragment.this.mListAdapter.getCachedMessageItemWithIdAssigned("sms", selectedItems[i2 - 1].longValue(), cursor, 1);
                    if (msgItem != null) {
                        if (PreferenceUtils.getForwardMessageFrom(FavoritesFragment.this.getContext())) {
                            if (!msgItem.isInComingMessage()) {
                                String mSelf = FavoritesFragment.this.getString(R.string.message_sender_from_self);
                                forwardString = FavoritesFragment.this.getString(R.string.forward_from, new Object[]{mSelf});
                            } else if (TextUtils.isEmpty(msgItem.mContact)) {
                                forwardString = FavoritesFragment.this.getString(R.string.forward_from, new Object[]{msgItem.mAddress});
                            } else {
                                forwardString = FavoritesFragment.this.getString(R.string.forward_from, new Object[]{msgItem.mContact});
                            }
                            forwardString = forwardString + System.lineSeparator() + msgItem.mBody;
                        } else {
                            forwardString = msgItem.mBody;
                        }
                        forwardMsgList.append(forwardString + System.lineSeparator() + System.lineSeparator());
                        cleanForwardMsgList.append(msgItem.mBody + System.lineSeparator() + System.lineSeparator());
                    }
                }
                Intent intent = ComposeMessageActivity.createIntent(FavoritesFragment.this.getContext(), 0);
                intent.putExtra("exit_on_sent", true);
                intent.putExtra("forwarded_message", true);
                if (FavoritesFragment.this.mTempThreadId > 0) {
                    intent.putExtra("thread_id", FavoritesFragment.this.mTempThreadId);
                }
                String forwardMsg = MessageUtils.correctForwardMsg(forwardMsgList.toString());
                String cleanForwardMsg = MessageUtils.correctForwardMsg(cleanForwardMsgList.toString());
                if (FavoritesFragment.this.mHwCust == null || !FavoritesFragment.this.mHwCust.isRcsSwitchOn()) {
                    intent.putExtra("sms_body", forwardMsg);
                    intent.setClassName(FavoritesFragment.this.getContext(), "com.android.mms.ui.ForwardMessageActivity");
                    MessageUtils.forwardByChooser(FavoritesFragment.this.getContext(), intent, cleanForwardMsg, FavoritesFragment.this.getAppResources().getString(R.string.forward_message));
                } else {
                    FavoritesFragment.this.mHwCust.prepareFwdMsg(forwardMsg);
                }
                return selectedItems.length;
            } else {
                FavoritesFragment.this.mHwCust.forwardLoc(FavoritesFragment.this.getContext());
                return selectedItems.length;
            }
        }
    };
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    FavoritesFragment.this.mListView.exitEditMode();
                    return;
                case 1002:
                    MessageItem item = msg.obj;
                    if (item != null) {
                        FavoritesFragment.this.forwardMessage(item);
                        return;
                    }
                    return;
                case 1003:
                    if (FavoritesFragment.this.isAdded() && FavoritesFragment.this.mMenuEx != null && FavoritesFragment.this.isInLandscape()) {
                        FavoritesFragment.this.mMenuEx.onPrepareOptionsMenu();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private RcsFavoritesFragment mHwCust;
    private long mId;
    private boolean mIsAllSelected = false;
    private boolean mIsCanCopyText = false;
    private boolean mIsCanDelete = false;
    private boolean mIsCanForward = false;
    private boolean mIsCanSave = false;
    private boolean mIsCanSelectAll = false;
    private boolean mIsCanSeleteText = false;
    private FavoritesListAdapter mListAdapter;
    private FavoritesListView mListView;
    public MenuEx mMenuEx;
    private View mMessageBlockView = null;
    private final Handler mMessageListItemHandler = new Handler() {
        public void handleMessage(Message msg) {
            MessageItem msgItem = msg.obj;
            if (msgItem != null) {
                switch (msg.what) {
                    case 1000102:
                        switch (msgItem.mAttachmentType) {
                            case 0:
                            case 2:
                            case 3:
                            case 4:
                                if (!msgItem.isSms()) {
                                    MessageUtils.viewMmsMessageAttachment(FavoritesFragment.this, msgItem.mMessageUri, msgItem.mSlideshow, FavoritesFragment.this.getAsyncDialog());
                                    break;
                                }
                                break;
                            case 1:
                                if (!msgItem.isSms()) {
                                    MessageUtils.viewSimpleSlideshow(FavoritesFragment.this.getContext(), msgItem.mSlideshow);
                                    break;
                                }
                                break;
                        }
                        break;
                    default:
                        MLog.w("mms/favorites", "Unknown message: " + msg.what);
                        return;
                }
            }
        }
    };
    private MessageItem mMsgItem = null;
    private FavoritesListItem mMsglistItem;
    NoMessageView mNoViewStub = null;
    private int mPosition;
    private FavoritesQueryHandler mQueryHandler;
    private int mSavedScrollPosition = -1;
    private Uri mTempMmsUri = null;
    private long mTempThreadId = 0;
    private TextView mTextView;
    private Runnable mUpdateContactRunner = new Runnable() {
        public void run() {
            if (FavoritesFragment.this.mListAdapter != null) {
                FavoritesFragment.this.mListAdapter.onContactsChange();
            }
            ((FavoritesListView) FavoritesFragment.this.mListView.getListView()).onContactChange();
        }
    };
    private ContentObserver sPresenceObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            FavoritesFragment.this.mMessageListItemHandler.removeCallbacks(FavoritesFragment.this.mUpdateContactRunner);
            FavoritesFragment.this.mMessageListItemHandler.postDelayed(FavoritesFragment.this.mUpdateContactRunner, 500);
        }
    };

    private class DeleteMessageListener implements OnClickListener {
        private final MessageItem mMessageItem;

        public DeleteMessageListener(MessageItem messageItem) {
            this.mMessageItem = messageItem;
        }

        public void onClick(DialogInterface dialog, int whichButton) {
            dialog.dismiss();
            new AsyncTask<Void, Void, Void>() {
                protected Void doInBackground(Void... none) {
                    Cursor cursor;
                    boolean z = true;
                    if (DeleteMessageListener.this.mMessageItem.isMms()) {
                        MmsApp.getApplication().getPduLoaderManager().removePdu(DeleteMessageListener.this.mMessageItem.mMessageUri);
                    }
                    Boolean deletingLastItem = Boolean.valueOf(false);
                    if (FavoritesFragment.this.mListAdapter != null) {
                        cursor = FavoritesFragment.this.mListAdapter.getCursor();
                    } else {
                        cursor = null;
                    }
                    if (cursor != null) {
                        cursor.moveToLast();
                        if (cursor.getLong(1) != DeleteMessageListener.this.mMessageItem.mMsgId) {
                            z = false;
                        }
                        deletingLastItem = Boolean.valueOf(z);
                    }
                    FavoritesFragment.this.mQueryHandler.startDelete(101010002, deletingLastItem, DeleteMessageListener.this.mMessageItem.mMessageUri, null, null);
                    return null;
                }
            }.executeOnExecutor(ThreadEx.getDefaultExecutor(), new Void[0]);
        }
    }

    private class EMUIListViewListener implements EmuiListViewListener {
        private EMUIListViewListener() {
        }

        public void onEnterEditMode() {
            FavoritesFragment.this.mMenuEx.switchToEdit(true);
        }

        public void onExitEditMode() {
            FavoritesFragment.this.mListAdapter.notifyDataSetChanged();
            FavoritesFragment.this.mMenuEx.switchToEdit(false);
        }

        public EditHandler getHandler(int mode) {
            if (mode == 2) {
                return FavoritesFragment.this.mDeleteHandler;
            }
            if (mode == 4) {
                return FavoritesFragment.this.mForwardHandler;
            }
            return null;
        }

        public int getHintColor(int mode, int count) {
            if (count <= 0 || mode != 2) {
                return ResEx.self().getCachedColor(R.color.sms_number_save_disable);
            }
            return ResEx.self().getCachedColor(R.drawable.text_color_red);
        }

        public String getHintText(int mode, int count) {
            if (mode == 2) {
                return ResEx.self().getOperTextDelete(count);
            }
            if (mode == 4) {
                ResEx.self().getOperTextMultiForward(count);
            }
            return "";
        }
    }

    private class EMUIListViewListenerV3 extends EMUIListViewListener implements SelectionChangedListener, OnItemLongClickListener, View.OnClickListener {
        private EMUIListViewListenerV3() {
            super();
        }

        public void onClick(View v) {
            FavoritesFragment.this.mListView.exitEditMode();
        }

        private void updateTitle(int cnt) {
            FavoritesFragment.this.mActionBar.setTitle(FavoritesFragment.this.getAppResources().getString(cnt == 0 ? R.string.no_selected : R.string.has_selected), cnt);
        }

        private boolean hasMmsMsg() {
            for (Long l : FavoritesFragment.this.mListView.getRecorder().getAllSelectItems()) {
                if (l.longValue() < 0) {
                    return true;
                }
            }
            return false;
        }

        private boolean msgHasText() {
            return MessageUtils.msgsHaveText(FavoritesFragment.this.getContext(), FavoritesFragment.this.mListView.getRecorder().getAllSelectItems(), FavoritesFragment.this.mListAdapter, 1);
        }

        private boolean isSlideShow() {
            Long[] selectionItem = FavoritesFragment.this.mListView.getRecorder().getAllSelectItems();
            if (selectionItem.length != 1) {
                MLog.v("mms/favorites", "getSelectedMessageBodies::the select item is not 1, return");
                return false;
            }
            MessageItem msgItem = FavoritesFragment.this.mListAdapter.getCachedMessageItemWithIdAssigned(selectionItem[0].longValue() > 0 ? "sms" : "mms", selectionItem[0].longValue() > 0 ? selectionItem[0].longValue() : -selectionItem[0].longValue(), FavoritesFragment.this.mListAdapter.getCursor(), 1);
            return msgItem != null && 4 == msgItem.mAttachmentType;
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z;
            boolean z2 = false;
            FavoritesFragment favoritesFragment = FavoritesFragment.this;
            if (selectedSize != totalSize || selectedSize <= 0) {
                z = false;
            } else {
                z = true;
            }
            favoritesFragment.mIsAllSelected = z;
            if (FavoritesFragment.this.mHwCust != null) {
                FavoritesFragment.this.mHwCust.onSelectChangeBegin(FavoritesFragment.this.mMenuEx);
            }
            FavoritesFragment.this.mMenuEx.setAllChecked(FavoritesFragment.this.mIsAllSelected, FavoritesFragment.this.isInLandscape());
            FavoritesFragment.this.mMenuEx.setItemEnabled(278925315, selectedSize > 0);
            MenuEx menuEx = FavoritesFragment.this.mMenuEx;
            if (selectedSize == 1) {
                z = true;
            } else if (hasMmsMsg() || selectedSize <= 1 || selectedSize >= MmsConfig.getForwardLimitSize()) {
                z = false;
            } else {
                z = true;
            }
            menuEx.setItemEnabled(278925316, z);
            boolean hasText = msgHasText();
            menuEx = FavoritesFragment.this.mMenuEx;
            if (selectedSize > 0) {
                z = hasText;
            } else {
                z = false;
            }
            menuEx.setItemEnabled(278925319, z);
            MenuEx menuEx2 = FavoritesFragment.this.mMenuEx;
            if (selectedSize != 1) {
                hasText = false;
            }
            menuEx2.setItemVisible(278925343, hasText);
            menuEx = FavoritesFragment.this.mMenuEx;
            if (selectedSize == 1) {
                z = true;
            } else {
                z = false;
            }
            menuEx.setItemVisible(278927472, z);
            menuEx2 = FavoritesFragment.this.mMenuEx;
            if (!isSlideShow()) {
                z2 = true;
            }
            menuEx2.setItemEnabled(278927472, z2);
            updateTitle(selectedSize);
            MLog.v("mms/favorites", "selectedSize = " + selectedSize + ",totalSize = " + totalSize + "mListView = " + FavoritesFragment.this.mListView.getRecorder().getAllSelectItems().length);
            if (FavoritesFragment.this.mHwCust != null) {
                FavoritesFragment.this.mHwCust.onSelectChange(FavoritesFragment.this.mMenuEx, FavoritesFragment.this.mListView, FavoritesFragment.this.mListAdapter, FavoritesFragment.this.mActionBar.getActionMenu());
            }
        }

        public void onEnterEditMode() {
            FavoritesFragment.this.mActionBar.enterEditMode(this);
            FavoritesFragment.this.getActivity().invalidateOptionsMenu();
            FavoritesFragment.this.mActionBar.setStartIconDescription(FavoritesFragment.this.getContext().getString(R.string.no));
        }

        public void onExitEditMode() {
            FavoritesFragment.this.mActionBar.exitEditMode();
            FavoritesFragment.this.mActionBar.setTitle(FavoritesFragment.this.getAppResources().getString(R.string.mms_myfavorite_common));
            if (FavoritesFragment.this.isAdded()) {
                FavoritesFragment.this.getActivity().invalidateOptionsMenu();
                FavoritesFragment.this.updateFooterViewHeight(null);
                FavoritesFragment.this.mActionBar.setStartIconDescription(FavoritesFragment.this.getContext().getString(R.string.up_navigation));
            }
        }

        public void showPopupFloatingToolbar() {
            FavoritesFragment.this.mActionMode = FavoritesFragment.this.mMessageBlockView.startActionMode(new FloatingCallback2(), 1);
            FavoritesFragment.this.mActionMode.hide(0);
        }

        public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
            FavoritesFragment.this.mPosition = position;
            FavoritesFragment.this.mId = id;
            View v = listView.getChildAt(position - listView.getFirstVisiblePosition());
            if (v == null || !(v instanceof FavoritesListItem)) {
                return true;
            }
            FavoritesFragment.this.mMessageBlockView = ((FavoritesListItem) v).getMessageBlockSuper();
            if (FavoritesFragment.this.mMessageBlockView == null) {
                return true;
            }
            if (id <= 0) {
                id = -id;
            }
            Long msgId = Long.valueOf(id);
            FavoritesFragment.this.mMsgItem = FavoritesFragment.this.mListAdapter.getCachedMessageItem(FavoritesFragment.this.mListAdapter.getType(), msgId.longValue(), FavoritesFragment.this.mListAdapter.getCursor());
            if (FavoritesFragment.this.mMsgItem == null) {
                return true;
            }
            FavoritesFragment.this.mIsCanForward = true;
            FavoritesFragment.this.mIsCanDelete = true;
            FavoritesFragment.this.mIsCanSelectAll = true;
            if (MessageUtils.msgHasText(FavoritesFragment.this.getContext(), FavoritesFragment.this.mMsgItem)) {
                FavoritesFragment.this.mIsCanCopyText = true;
                FavoritesFragment.this.mIsCanSeleteText = true;
            }
            switch (FavoritesFragment.this.mMsgItem.mAttachmentType) {
                case -2:
                case 0:
                    break;
                default:
                    FavoritesFragment.this.mIsCanSave = true;
                    break;
            }
            if (FavoritesFragment.this.mHwCust != null && FavoritesFragment.this.mHwCust.isSaveFile(FavoritesFragment.this.mMsgItem)) {
                RcsMessageItem custMessageItem = FavoritesFragment.this.mMsgItem.getRcsMessageItem();
                FavoritesFragment.this.mIsCanCopyText = false;
                FavoritesFragment.this.mIsCanForward = true;
                FavoritesFragment.this.mIsCanSeleteText = false;
                FavoritesFragment.this.mIsCanSave = true;
                if (custMessageItem.getFileItem().isVCardFileTypeMsg()) {
                    FavoritesFragment.this.mIsCanSave = false;
                }
                if (!(custMessageItem.getFileItem().mImAttachmentPath == null || new File(custMessageItem.getFileItem().mImAttachmentPath).exists())) {
                    FavoritesFragment.this.mIsCanForward = false;
                    FavoritesFragment.this.mIsCanSave = false;
                }
            }
            if (FavoritesFragment.this.mHwCust != null && RcsMapLoader.isLocItem(FavoritesFragment.this.mMsgItem.getMessageSummary())) {
                FavoritesFragment.this.mIsCanSelectAll = true;
                FavoritesFragment.this.mIsCanForward = true;
                FavoritesFragment.this.mIsCanCopyText = false;
                FavoritesFragment.this.mIsCanDelete = true;
                FavoritesFragment.this.mIsCanSeleteText = false;
                FavoritesFragment.this.mIsCanSave = false;
            }
            showPopupFloatingToolbar();
            return true;
        }
    }

    private final class FavoritesQueryHandler extends AsyncQueryHandlerEx implements OnDataSetChangedListener {
        public FavoritesQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (cursor != null) {
                switch (token) {
                    case 101010000:
                        FavoritesFragment.this.mListAdapter.changeCursor(cursor);
                        if (FavoritesFragment.this.mSavedScrollPosition != -1) {
                            FavoritesFragment.this.mListView.setSelection(FavoritesFragment.this.mSavedScrollPosition);
                        }
                        int newSelectionPos = -1;
                        long targetMsgId = FavoritesFragment.this.getIntent().getLongExtra("select_id", -1);
                        int witchTable = FavoritesFragment.this.getIntent().getIntExtra("table_to_use", 0);
                        String usedTable;
                        if (witchTable == 8) {
                            usedTable = "sms";
                        } else if (witchTable == 9) {
                            usedTable = "mms";
                        } else {
                            usedTable = "chat";
                        }
                        if (targetMsgId != -1) {
                            cursor.moveToPosition(-1);
                            while (cursor.moveToNext()) {
                                long msgId = cursor.getLong(1);
                                String type = cursor.getString(0);
                                if (msgId == targetMsgId && usedTable.equals(type)) {
                                    newSelectionPos = cursor.getPosition();
                                }
                            }
                        } else if (FavoritesFragment.this.mSavedScrollPosition != -1) {
                            if (FavoritesFragment.this.mSavedScrollPosition == Integer.MAX_VALUE) {
                                int cnt = FavoritesFragment.this.mListAdapter.getCount();
                                if (cnt > 0) {
                                    newSelectionPos = cnt - 1;
                                    FavoritesFragment.this.mSavedScrollPosition = -1;
                                }
                            } else {
                                newSelectionPos = FavoritesFragment.this.mSavedScrollPosition;
                                FavoritesFragment.this.mSavedScrollPosition = -1;
                            }
                        }
                        if (newSelectionPos != -1) {
                            FavoritesFragment.this.mListView.setSelection(newSelectionPos);
                        }
                        FavoritesFragment.this.getActivity().invalidateOptionsMenu();
                        break;
                }
                if (FavoritesFragment.this.mListAdapter.getCount() == 0) {
                    FavoritesFragment.this.mNoViewStub.setVisibility(0, FavoritesFragment.this.getActivity().isInMultiWindowMode());
                    FavoritesFragment.this.mListView.setVisibility(8);
                } else {
                    FavoritesFragment.this.mNoViewStub.setVisibility(8, FavoritesFragment.this.getActivity().isInMultiWindowMode());
                    FavoritesFragment.this.mListView.setVisibility(0);
                }
            }
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            if (token == 101010002) {
                startMsgListQuery(101010000);
            }
            FavoritesFragment.this.mListView.exitEditMode();
        }

        public void onDataSetChanged(MessageListAdapter adapter) {
        }

        public void onContentChanged(MessageListAdapter adapter) {
            startMsgListQuery(101010000);
        }

        private void startMsgListQuery(int token) {
            String selection = getQuerySelectionByToken(token);
            cancelOperation(token);
            startQuery(token, null, FavoritesUtils.URI_FAV, MessageListAdapter.PROJECTION, selection, null, "date DESC ");
        }

        private String getQuerySelectionByToken(int token) {
            if (RcsCommonConfig.isRCSSwitchOn()) {
                return null;
            }
            String selection = null;
            switch (token) {
                case 101010000:
                    selection = "service_center is null or service_center not like \"rcs%\"";
                    break;
            }
            return selection;
        }
    }

    private class FloatingCallback2 extends Callback2 {
        private boolean mWasAlreadyClick;

        private FloatingCallback2() {
            this.mWasAlreadyClick = false;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setSubtitle(null);
            mode.setTitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            return true;
        }

        private void populateMenuWithItems(Menu menu) {
            TypefaceSpan span = new TypefaceSpan("default");
            if (FavoritesFragment.this.mIsCanCopyText) {
                SpannableString spanString = new SpannableString(FavoritesFragment.this.getString(R.string.button_copy_text));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 1, 0, spanString).setShowAsAction(2);
            }
            if (FavoritesFragment.this.mIsCanForward) {
                spanString = new SpannableString(FavoritesFragment.this.getString(R.string.forward_message));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 2, 1, spanString).setShowAsAction(2);
            }
            if (FavoritesFragment.this.mIsCanDelete) {
                spanString = new SpannableString(FavoritesFragment.this.getString(R.string.delete));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 3, 2, spanString).setShowAsAction(2);
            }
            if (FavoritesFragment.this.mIsCanSeleteText) {
                spanString = new SpannableString(FavoritesFragment.this.getString(R.string.mms_select_text_copy));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 4, 3, spanString).setShowAsAction(2);
            }
            if (FavoritesFragment.this.mIsCanSave) {
                spanString = new SpannableString(FavoritesFragment.this.getString(R.string.save));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 5, 4, spanString).setShowAsAction(1);
            }
            if (FavoritesFragment.this.mIsCanSelectAll) {
                spanString = new SpannableString(FavoritesFragment.this.getString(R.string.menu_add_rcs_more));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 6, 5, spanString).setShowAsAction(1);
            }
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (this.mWasAlreadyClick) {
                return true;
            }
            switch (item.getItemId()) {
                case 1:
                    this.mWasAlreadyClick = true;
                    HwMessageUtils.copyToClipboard(FavoritesFragment.this.getContext(), FavoritesFragment.this.mMsgItem.mBody);
                    break;
                case 2:
                    this.mWasAlreadyClick = true;
                    Integer[] selectedItems = new Integer[]{Integer.valueOf(FavoritesFragment.this.mPosition)};
                    if (FavoritesFragment.this.mHwCust == null || !FavoritesFragment.this.mHwCust.detectMessageToForwardForFt(selectedItems, FavoritesFragment.this.mListAdapter.getCursor())) {
                        if (FavoritesFragment.this.mHwCust != null && FavoritesFragment.this.mHwCust.detectMessageToForwardForLoc(selectedItems, FavoritesFragment.this.mListAdapter.getCursor())) {
                            FavoritesFragment.this.mHwCust.forwardLoc(FavoritesFragment.this.getContext());
                            break;
                        }
                        FavoritesFragment.this.forwardMessage(FavoritesFragment.this.mMsgItem);
                        break;
                    }
                    FavoritesFragment.this.mHwCust.toForward(FavoritesFragment.this.getContext());
                    break;
                case 3:
                    this.mWasAlreadyClick = true;
                    FavoritesFragment.this.confirmDeleteDialogPop(new DeleteMessageListener(FavoritesFragment.this.mMsgItem));
                    break;
                case 4:
                    if (FavoritesFragment.this.mMsgItem != null) {
                        this.mWasAlreadyClick = true;
                        MessageUtils.viewText(FavoritesFragment.this.getContext(), FavoritesFragment.this.mMsgItem.mBody);
                        break;
                    }
                    return true;
                case 5:
                    if (FavoritesFragment.this.mMsgItem != null) {
                        this.mWasAlreadyClick = true;
                        if (FavoritesFragment.this.mHwCust != null && FavoritesFragment.this.mHwCust.saveFileToPhone(FavoritesFragment.this.mMsgItem)) {
                            MLog.v("mms/favorites", "onOptionsItemSelected mRcsComposeMessage.saveFileToPhone is true, break");
                            break;
                        }
                        MmsPduUtils.copyMediaAndShowResult(FavoritesFragment.this.getContext(), FavoritesFragment.this.mMsgItem.mFavoritesOrginId);
                        break;
                    }
                    return true;
                case 6:
                    this.mWasAlreadyClick = true;
                    if (MmsConfig.isSmsEnabled(FavoritesFragment.this.getContext())) {
                        if (FavoritesFragment.this.mHwCust == null || !FavoritesFragment.this.mHwCust.isInEditMode(FavoritesFragment.this.mListView)) {
                            FavoritesFragment.this.mListView.enterEditMode(1);
                            FavoritesFragment.this.updateFooterViewHeight(null);
                            FavoritesFragment.this.mListAdapter.notifyDataSetChanged();
                            FavoritesFragment.this.mListView.setSeleceted(FavoritesFragment.this.mId, true);
                            if (FavoritesFragment.this.mHwCust != null) {
                                FavoritesFragment.this.mHwCust.onItemLongClick(FavoritesFragment.this.mListView, FavoritesFragment.this.mPosition);
                                break;
                            }
                        }
                        return true;
                    }
                    break;
            }
            if (FavoritesFragment.this.mActionMode != null) {
                FavoritesFragment.this.mActionMode.finish();
                FavoritesFragment.this.mActionMode = null;
            }
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            FavoritesFragment.this.mMsgItem = null;
            FavoritesFragment.this.mIsCanCopyText = false;
            FavoritesFragment.this.mIsCanForward = false;
            FavoritesFragment.this.mIsCanDelete = false;
            FavoritesFragment.this.mIsCanSeleteText = false;
            FavoritesFragment.this.mIsCanSave = false;
            FavoritesFragment.this.mIsCanSelectAll = false;
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (FavoritesFragment.this.mMessageBlockView != null) {
                outRect.set(0, 0, FavoritesFragment.this.mMessageBlockView.getWidth(), FavoritesFragment.this.mMessageBlockView.getHeight());
            }
        }
    }

    public class MenuEx extends EmuiMenu implements OnCreateContextMenuListener, OnMenuItemClickListener {
        private boolean mEnabled;
        private MessageItem mSelectedItem = null;

        public MenuEx() {
            super(null);
        }

        public void setMenuEnabled(boolean enabled) {
            this.mEnabled = enabled;
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        public boolean onCreateOptionsMenu() {
            if (this.mOptionMenu == null || !this.mEnabled) {
                return true;
            }
            switchToEdit(false);
            return true;
        }

        public boolean onPrepareOptionsMenu() {
            switchToEdit(FavoritesFragment.this.mListView.isInEditMode());
            FavoritesFragment.this.mListView.onMenuPrepared();
            return true;
        }

        public void switchToEdit(boolean editable) {
            if (editable && FavoritesFragment.this.mActionBar.getActionMenu() != null) {
                boolean isLandscape = FavoritesFragment.this.isInLandscape();
                setOptionMenu(FavoritesFragment.this.mActionBar.getActionMenu());
                clear();
                addMenu(278925315, R.string.delete, getDrawableId(278925315, isLandscape));
                addMenu(278925316, R.string.menu_forward, getDrawableId(278925316, isLandscape));
                addMenu(278925319, R.string.button_copy_text, getDrawableId(278925319, isLandscape));
                addMenu(278925313, R.string.menu_select_all, getDrawableId(278925313, isLandscape));
                addOverflowMenu(278925343, R.string.mms_select_text_copy);
                addOverflowMenu(278927472, R.string.button_share);
                if (FavoritesFragment.this.mHwCust != null) {
                    FavoritesFragment.this.mHwCust.switchToEdit(FavoritesFragment.this.mMenuEx);
                }
            } else if (FavoritesFragment.this.mActionBar.getActionMenu() != null) {
                setOptionMenu(FavoritesFragment.this.mActionBar.getActionMenu());
                clear();
            } else if (!editable) {
                clear();
            }
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 16908332:
                    FavoritesFragment.this.onBackPressed();
                    break;
                case 278925312:
                    return false;
                case 278925313:
                    FavoritesFragment.this.mListView.setAllSelected(!FavoritesFragment.this.mIsAllSelected);
                    if (FavoritesFragment.this.mListView.getHwCustFavoritesListView() != null) {
                        FavoritesFragment.this.mListView.getHwCustFavoritesListView().setAllSelectedPosition(FavoritesFragment.this.mIsAllSelected, FavoritesFragment.this.mListView);
                        break;
                    }
                    break;
                case 278925315:
                    if (!FavoritesFragment.this.mListView.isInEditMode()) {
                        FavoritesFragment.this.mListView.enterEditMode(2);
                        break;
                    }
                    FavoritesFragment.this.confirmDeleteDialog(new OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            FavoritesFragment.this.mListView.doOperation(FavoritesFragment.this.mDeleteHandler);
                        }
                    });
                    break;
                case 278925316:
                    if (!FavoritesFragment.this.mListView.isInEditMode()) {
                        FavoritesFragment.this.mListView.enterEditMode(4);
                        break;
                    }
                    FavoritesFragment.this.mListView.doOperation(FavoritesFragment.this.mForwardHandler);
                    FavoritesFragment.this.mListView.exitEditMode();
                    break;
                case 278925319:
                    if (!FavoritesFragment.this.mListView.isInEditMode()) {
                        FavoritesFragment.this.mListView.enterEditMode(8);
                        break;
                    }
                    HwMessageUtils.copyToClipboard(FavoritesFragment.this.getContext(), MessageUtils.getSelectedMessageBodies(FavoritesFragment.this.getContext(), FavoritesFragment.this.mListView.getRecorder().getAllSelectItems(), FavoritesFragment.this.mListAdapter, FavoritesFragment.this.mListView, 1));
                    FavoritesFragment.this.mListView.exitEditMode();
                    break;
                case 278925343:
                    Long[] selectionItems = FavoritesFragment.this.mListView.getRecorder().getAllSelectItems();
                    if (selectionItems.length == 1) {
                        MessageUtils.viewMessageText(FavoritesFragment.this.getContext(), FavoritesFragment.this.mListAdapter.getCachedMessageItemWithIdAssigned(selectionItems[0].longValue() > 0 ? "sms" : "mms", selectionItems[0].longValue() > 0 ? selectionItems[0].longValue() : -selectionItems[0].longValue(), FavoritesFragment.this.mListAdapter.getCursor(), 1));
                        FavoritesFragment.this.mListView.exitEditMode();
                        break;
                    }
                    MLog.v("mms/favorites", "getSelectedMessageBodies::the select item is not 1, return");
                    return false;
                case 278927472:
                    Long[] selectionItem = FavoritesFragment.this.mListView.getRecorder().getAllSelectItems();
                    if (selectionItem.length == 1) {
                        MessageItem messageItem = FavoritesFragment.this.mListAdapter.getCachedMessageItemWithIdAssigned(selectionItem[0].longValue() > 0 ? "sms" : "mms", selectionItem[0].longValue() > 0 ? selectionItem[0].longValue() : -selectionItem[0].longValue(), FavoritesFragment.this.mListAdapter.getCursor(), 1);
                        if (messageItem != null) {
                            shareMessage(messageItem);
                            break;
                        }
                    }
                    MLog.v("mms/favorites", "getSelectedMessageBodies::the select item is not 1, return");
                    return false;
                    break;
                default:
                    if (FavoritesFragment.this.mHwCust != null) {
                        FavoritesFragment.this.mHwCust.onOptionsItemSelected(FavoritesFragment.this.mListView, FavoritesFragment.this.mListAdapter, item);
                        break;
                    }
                    break;
            }
            return true;
        }

        private void shareMessage(MessageItem msgItem) {
            String mssageText = MessageUtils.getSelectedMessageBodies(FavoritesFragment.this.getContext(), FavoritesFragment.this.mListView.getRecorder().getAllSelectItems(), FavoritesFragment.this.mListAdapter, FavoritesFragment.this.mListView, 1);
            String messageType = "text/plain";
            Uri uri = null;
            String messageSrc = null;
            MediaModel mediaModel = null;
            switch (msgItem.mAttachmentType) {
                case 0:
                    messageType = "text/plain";
                    break;
                case 1:
                    mediaModel = msgItem.mSlideshow.get(0).getImage();
                    break;
                case 2:
                    mediaModel = msgItem.mSlideshow.get(0).getVideo();
                    break;
                case 3:
                    mediaModel = msgItem.mSlideshow.get(0).getAudio();
                    break;
                case 5:
                    mediaModel = msgItem.mSlideshow.get(0).getVcard();
                    break;
                case 6:
                    mediaModel = msgItem.mSlideshow.get(0).getVCalendar();
                    break;
            }
            if (mediaModel != null) {
                uri = mediaModel.getUri();
                if (5 == msgItem.mAttachmentType) {
                    messageSrc = ((VcardModel) mediaModel).getName() + "_" + mediaModel.getSrc();
                } else {
                    messageSrc = mediaModel.getSrc();
                }
            }
            uri = ShareUtils.copyFile(FavoritesFragment.this.getContext(), uri, messageSrc);
            RcsMessageItem rcsMsgItem = msgItem.getRcsMessageItem();
            if (rcsMsgItem != null) {
                RcsFileTransGroupMessageItem rcsFileMsgItem = rcsMsgItem.getFileItem();
                if (rcsFileMsgItem != null) {
                    switch (rcsFileMsgItem.mFileTransType) {
                        case 7:
                            mssageText = "";
                            break;
                        case 8:
                            mssageText = "";
                            break;
                        case 9:
                            mssageText = "";
                            break;
                        case 10:
                            mssageText = "";
                            break;
                    }
                    uri = ShareUtils.copyFile(FavoritesFragment.this.getContext(), rcsFileMsgItem.getAttachmentFile());
                }
            }
            ShareUtils.shareMessage(FavoritesFragment.this.getContext(), uri, messageType, mssageText);
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (v != null && menuInfo != null && menu != null && (v instanceof FavoritesListView)) {
                Cursor cursor = FavoritesFragment.this.mListAdapter.getCursor();
                if (HwBaseActivity.isCurosrValide(cursor)) {
                    String type = cursor.getString(0);
                    long msgId = cursor.getLong(1);
                    this.mSelectedItem = FavoritesFragment.this.mListAdapter.getCachedMessageItem(type, msgId, cursor);
                    if (this.mSelectedItem == null) {
                        MLog.e("mms/favorites", "Cannot load message item for type = " + type + ", msgId = " + msgId);
                        return;
                    }
                    menu.setHeaderTitle(R.string.message_options);
                    try {
                        ListView listView = (ListView) v;
                        FavoritesFragment.this.mMsglistItem = (FavoritesListItem) listView.getChildAt(((AdapterContextMenuInfo) menuInfo).position - listView.getFirstVisiblePosition());
                        menu.add(0, 2, 0, R.string.copy_message_text).setOnMenuItemClickListener(this);
                        if (MmsConfig.isSmsEnabled(FavoritesFragment.this.getContext())) {
                            menu.add(0, 1, 0, R.string.delete_message).setOnMenuItemClickListener(this);
                            menu.add(0, 4, 0, R.string.menu_forward).setOnMenuItemClickListener(this);
                        }
                    } catch (ClassCastException e) {
                        MLog.e("mms/favorites", "bad menuInfo");
                    }
                }
            }
        }

        public boolean onMenuItemClick(MenuItem item) {
            int id = item.getItemId();
            if (this.mSelectedItem == null) {
                return false;
            }
            switch (id) {
                case 1:
                    FavoritesFragment.this.confirmDeleteDialog(new DeleteMessageListener(this.mSelectedItem));
                    break;
                case 2:
                    FavoritesFragment.this.copyMessageText();
                    break;
                case 4:
                    FavoritesFragment.this.forwardMessage(this.mSelectedItem);
                    break;
            }
            this.mSelectedItem = null;
            return false;
        }
    }

    public FavoritesListAdapter getListAdapter() {
        return this.mListAdapter;
    }

    public FavoritesListView getListView() {
        return this.mListView;
    }

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.favorites_activity, container, false);
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mCustFavoritesFragment = (HwCustFavoritesFragment) HwCustUtils.createObj(HwCustFavoritesFragment.class, new Object[]{this});
        this.mMenuEx = new MenuEx();
        this.mMenuEx.setContext(getContext());
        this.mMenuEx.setMenuEnabled(true);
        this.mActionBar = new EmuiActionBar(getActivity());
        this.mQueryHandler = new FavoritesQueryHandler(getActivity().getContentResolver());
        initResourceRefs();
        initMessageList();
        ((HwBaseActivity) getActivity()).setSupportScale(this.mListAdapter);
        getActivity().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.sPresenceObserver);
        if (RcsCommonConfig.isRCSSwitchOn() && this.mHwCust == null) {
            this.mHwCust = new RcsFavoritesFragment(this);
        }
    }

    public void onDestroy() {
        super.onDestroy();
        getActivity().getContentResolver().unregisterContentObserver(this.sPresenceObserver);
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
        if (this.mListAdapter != null) {
            this.mListAdapter.clearTextSpanCache(true);
        }
    }

    public void onStart() {
        super.onStart();
        this.mQueryHandler.startMsgListQuery(101010000);
    }

    public void onStop() {
        super.onStop();
        if (this.mListAdapter != null) {
            this.mListAdapter.changeCursor(null);
        }
    }

    public void onResume() {
        super.onResume();
        this.mListView.invalidateViews();
        if (this.mListView.isInEditMode() && !MmsConfig.isSmsEnabled(getContext())) {
            MLog.v("mms/favorites", "onResume:: it is not default sms app, exit multi choice mode");
            this.mListView.exitEditMode();
        }
    }

    AsyncDialog getAsyncDialog() {
        if (this.mAsyncDialog == null) {
            this.mAsyncDialog = new AsyncDialog(getActivity());
        }
        return this.mAsyncDialog;
    }

    private void initResourceRefs() {
        this.mNoViewStub = (NoMessageView) getView().findViewById(R.id.no_favorites);
        this.mNoViewStub.setViewType(2);
        this.mListView = (FavoritesListView) getView().findViewById(R.id.favorites_list);
        this.mListView.setFastScrollEnabled(true);
        this.mFooterView = getView().findViewById(R.id.blank_footer_view);
        updateFooterViewHeight(null);
    }

    public void onPause() {
        super.onPause();
        if (this.mListAdapter == null || this.mListView.getLastVisiblePosition() < this.mListAdapter.getCount() - 1) {
            this.mSavedScrollPosition = this.mListView.getFirstVisiblePosition();
        } else {
            this.mSavedScrollPosition = Integer.MAX_VALUE;
        }
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (isDetached()) {
            MLog.d("mms/favorites", "onConfigurationChanged::activity is finishing, return");
            return;
        }
        this.mMenuEx.onPrepareOptionsMenu();
        updateFooterViewHeight(newConfig);
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
    }

    private void initMessageList() {
        if (this.mListAdapter == null) {
            Pattern pattern;
            String highlightString = getIntent().getStringExtra("highlight");
            if (highlightString == null) {
                pattern = null;
            } else {
                pattern = Pattern.compile(Pattern.quote(highlightString), 2);
            }
            this.mListAdapter = new FavoritesListAdapter(getContext(), null, this.mListView.getListView(), true, pattern);
            this.mListAdapter.setOnDataSetChangedListener(this.mQueryHandler);
            this.mListAdapter.setMsgListItemHandler(this.mMessageListItemHandler);
            this.mListView.setAdapter(this.mListAdapter);
            this.mListView.setVisibility(0);
            this.mListView.setClipChildren(false);
            this.mListView.setClipToPadding(false);
            EMUIListViewListenerV3 listener = new EMUIListViewListenerV3();
            this.mListView.setListViewListener(listener);
            this.mListView.setSelectionChangeLisenter(listener);
            this.mListView.setOnItemLongClickListener(listener);
            this.mListView.setClickable(true);
        }
    }

    public boolean onBackPressed() {
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
            return true;
        } else if (!this.mListView.isInEditMode()) {
            return false;
        } else {
            this.mListView.exitEditMode();
            return true;
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        this.mMenuEx.setOptionMenu(menu).onCreateOptionsMenu();
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (menu != null) {
            this.mMenuEx.setOptionMenu(menu).onPrepareOptionsMenu();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        return this.mMenuEx.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        this.mMenuEx.onCreateContextMenu(menu, v, menuInfo);
    }

    private void confirmDeleteDialog(OnClickListener l) {
        String message;
        if (this.mIsAllSelected) {
            message = getAppResources().getString(R.string.whether_delete_all_messages);
        } else {
            NumberFormat.getIntegerInstance().setGroupingUsed(false);
            int size = this.mListView.getRecorder().size();
            message = getAppResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, size, new Object[]{format.format((long) size)});
        }
        View contents = View.inflate(getActivity(), R.layout.delete_thread_dialog_view, null);
        ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(message);
        MessageUtils.setButtonTextColor(new Builder(getContext()).setView(contents).setCancelable(true).setPositiveButton(R.string.delete, l).setNegativeButton(R.string.no, null).show(), -1, getAppResources().getColor(R.color.mms_unread_text_color));
    }

    private void confirmDeleteDialogPop(OnClickListener l) {
        String message;
        if (this.mIsAllSelected) {
            message = getAppResources().getString(R.string.whether_delete_all_messages);
        } else {
            NumberFormat.getIntegerInstance().setGroupingUsed(false);
            message = getAppResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, 1, new Object[]{format.format(1)});
        }
        View contents = View.inflate(getActivity(), R.layout.delete_thread_dialog_view, null);
        ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(message);
        MessageUtils.setButtonTextColor(new Builder(getContext()).setCancelable(true).setView(contents).setPositiveButton(R.string.delete, l).setNegativeButton(R.string.no, null).show(), -1, getAppResources().getColor(R.color.mms_unread_text_color));
    }

    private void forwardMessage(final MessageItem msgItem) {
        final Context context = getContext();
        final boolean groupMmsEnabled = PreferenceUtils.getIsGroupMmsEnabled(getContext());
        getAsyncDialog().runAsync(new Runnable() {
            public void run() {
                if (msgItem.mType.equals("mms")) {
                    SendReq sendReq = new SendReq();
                    String subject = msgItem.getForwordSubject(R.string.forward_prefix);
                    if (FavoritesFragment.this.mCustFavoritesFragment != null) {
                        subject = FavoritesFragment.this.mCustFavoritesFragment.updateForwardSubject(subject, msgItem.mSubject);
                    }
                    if (subject != null) {
                        sendReq.setSubject(new EncodedStringValue(subject));
                    }
                    if (msgItem.mSlideshow != null) {
                        sendReq.setBody(msgItem.mSlideshow.makeCopy());
                    }
                    FavoritesFragment.this.mTempMmsUri = null;
                    try {
                        FavoritesFragment.this.mTempMmsUri = PduPersister.getPduPersister(context).persist(sendReq, Draft.CONTENT_URI, true, groupMmsEnabled, null);
                        FavoritesFragment.this.mTempThreadId = MessagingNotification.getThreadId(context, FavoritesFragment.this.mTempMmsUri);
                    } catch (MmsException e) {
                        MLog.e("mms/favorites", "Failed to copy message: " + msgItem.mMessageUri);
                        Handler uIHandler = HwBackgroundLoader.getUIHandler();
                        final Context context = context;
                        uIHandler.post(new Runnable() {
                            public void run() {
                                Toast.makeText(context, R.string.cannot_save_message_Toast, 0).show();
                            }
                        });
                    }
                }
            }
        }, new Runnable() {
            public void run() {
                Intent intent = ComposeMessageActivity.createIntent(context, 0);
                intent.putExtra("exit_on_sent", true);
                intent.putExtra("forwarded_message", true);
                if (FavoritesFragment.this.mTempThreadId > 0) {
                    intent.putExtra("thread_id", FavoritesFragment.this.mTempThreadId);
                }
                if (msgItem.mType.equals("sms")) {
                    intent.putExtra("sms_body", msgItem.getForwordMsgBody(R.string.forward_from));
                    if (FavoritesFragment.this.mHwCust != null && FavoritesFragment.this.mHwCust.isRcsSwitchOn()) {
                        FavoritesFragment.this.mHwCust.prepareFwdMsg(msgItem.getForwordMsgBody(R.string.forward_from));
                        return;
                    }
                }
                intent.putExtra("msg_uri", FavoritesFragment.this.mTempMmsUri);
                String subject = msgItem.getForwordSubject(R.string.forward_prefix);
                if (FavoritesFragment.this.mCustFavoritesFragment != null) {
                    subject = FavoritesFragment.this.mCustFavoritesFragment.updateForwardSubject(subject, msgItem.mSubject);
                }
                if (subject != null) {
                    intent.putExtra("subject", subject);
                }
                intent.setClassName(context, "com.android.mms.ui.ForwardMessageActivity");
                if (msgItem.isMms()) {
                    FavoritesFragment.this.startActivityForResult(intent, 112);
                } else {
                    MessageUtils.forwardByChooser(FavoritesFragment.this.getContext(), intent, msgItem.mBody, FavoritesFragment.this.getString(R.string.forward_message), 112);
                }
            }
        }, R.string.building_slideshow_title);
    }

    public void onNewIntent(Intent intent) {
        setIntent(intent);
        this.mQueryHandler.startMsgListQuery(101010000);
    }

    private void copyMessageText() {
        TextView textView = (TextView) this.mMsglistItem.findViewById(R.id.text_view);
        this.mTextView = textView;
        if (textView != null) {
            this.mMsglistItem.setDescendantFocusability(262144);
            this.mListView.setOnCreateContextMenuListener(null);
            textView.setFocusable(true);
            textView.requestFocus();
            ((SpandTextView) textView).switchToSelectionMode();
            textView.performLongClick();
            this.mListView.setOnCreateContextMenuListener(this.mMenuEx);
            this.mListView.postDelayed(new Runnable() {
                public void run() {
                    FavoritesFragment.this.mTextView.setOnFocusChangeListener(new OnFocusChangeListener() {
                        public void onFocusChange(View v, boolean hasFocus) {
                            int selectionStart = Selection.getSelectionStart(FavoritesFragment.this.mTextView.getText());
                            int seletcionEnd = Selection.getSelectionEnd(FavoritesFragment.this.mTextView.getText());
                            if (!hasFocus && selectionStart == seletcionEnd) {
                                FavoritesFragment.this.mTextView.onKeyDown(4, new KeyEvent(0, 4));
                                FavoritesFragment.this.mTextView.setOnFocusChangeListener(null);
                                FavoritesFragment.this.mMsglistItem.setDescendantFocusability(393216);
                            }
                        }
                    });
                }
            }, 300);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (-1 != resultCode) {
            MLog.e("mms/favorites", "onAcitivityResult:resultCode is error");
            return;
        }
        if (this.mHwCust != null) {
            this.mHwCust.onActivityResult(requestCode, resultCode, data, this.mListAdapter);
        }
    }

    private Resources getAppResources() {
        if (isAdded()) {
            return getResources();
        }
        return MmsApp.getApplication().getApplicationContext().getResources();
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mNoViewStub != null) {
            this.mNoViewStub.setInMultiWindowMode(isInMultiWindowMode);
        }
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null && this.mListView != null) {
            boolean isLandscape = newConfig == null ? getResources().getConfiguration().orientation == 2 : newConfig.orientation == 2;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!this.mListView.isInEditMode() || (isLandscape && !isInMultiWindowMode())) ? 0 : (int) getResources().getDimension(R.dimen.toolbar_footer_height);
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
