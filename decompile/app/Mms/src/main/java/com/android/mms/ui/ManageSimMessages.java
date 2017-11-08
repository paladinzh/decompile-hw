package com.android.mms.ui;

import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.AsyncQueryHandler;
import android.content.BroadcastReceiver;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Build.VERSION;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Telephony.Sms.Inbox;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.TypefaceSpan;
import android.view.ActionMode;
import android.view.ActionMode.Callback2;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MenuItem.OnMenuItemClickListener;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnCreateContextMenuListener;
import android.view.ViewGroup.LayoutParams;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.TextView;
import android.widget.Toast;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.ContactList;
import com.android.mms.transaction.MessagingNotification;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.ErrorMonitor;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.cspcommon.ex.ThreadEx;
import com.huawei.mms.ui.EmuiActionBar;
import com.huawei.mms.ui.EmuiListViewListener;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseActivity;
import com.huawei.mms.ui.MultiModeListView.EditHandler;
import com.huawei.mms.ui.NoMessageView;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.ResEx;
import com.huawei.mms.util.SelectionChangedListener;
import com.huawei.mms.util.SimCursorManager;
import java.text.NumberFormat;
import java.util.Arrays;
import java.util.Date;

public class ManageSimMessages extends HwBaseActivity implements OnCreateContextMenuListener, SelectionChangedListener {
    private static final Uri ICC1_URI = Uri.parse("content://sms/icc1");
    private static final Uri ICC2_URI = Uri.parse("content://sms/icc2");
    private static final Uri ICC_URI = Uri.parse("content://sms/icc");
    EmuiActionBar mActionBar;
    public ActionMode mActionMode = null;
    private EditHandler mAddToContactHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            ManageSimMessages.this.addRecipientsToContact(selectedItems);
            return selectedItems.length;
        }
    };
    protected BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
        public void onReceive(Context context, Intent intent) {
            if ("android.intent.action.SIM_STATE_CHANGED".equals(intent.getAction())) {
                String stateExtra = intent.getStringExtra("ss");
                if (stateExtra != null && ("ABSENT".equals(stateExtra) || "UNKNOWN".equals(stateExtra))) {
                    if (ManageSimMessages.this.mSimList != null) {
                        ManageSimMessages.this.mSimList.exitEditMode();
                    }
                    if (MessageUtils.isMultiSimEnabled()) {
                        int subId = MessageUtils.getSimIdFromIntent(intent, MessageUtils.getDefaultSubscription());
                        MLog.v("ManageSimMessages", "mBroadcastReceiver mSubId: " + ManageSimMessages.this.mSubId + "; subId: " + subId);
                        if (ManageSimMessages.this.mSubId != subId) {
                            return;
                        }
                    }
                    ManageSimMessages.this.updateState(1);
                }
            }
        }
    };
    private ContentResolver mContentResolver;
    private EditHandler mCopyHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            ManageSimMessages.this.updateState(3);
            ManageSimMessages.this.copyMessagesToPhone(selectedItems);
            return selectedItems.length;
        }
    };
    private Cursor mCursor = null;
    private int mDefaultSubId = 0;
    private EditHandler mDeleteHandler = new EditHandler() {
        public int handeleSelecte(Long[] selectedItems, boolean isAllSelected) {
            ManageSimMessages.this.updateState(2);
            ManageSimMessages.this.deleteItemsFromSim(selectedItems);
            return selectedItems.length;
        }
    };
    private View mFooterView = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 1001:
                    if (ManageSimMessages.this.mMenu != null && ManageSimMessages.this.isInLandscape()) {
                        ManageSimMessages.this.mMenu.onPrepareOptionsMenu();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    };
    private long mId;
    private boolean mIsAllSelected = false;
    private boolean mIsCanAddToContactlist = false;
    private boolean mIsCanCopyToPhone = false;
    private boolean mIsCanDelete = false;
    private boolean mIsCanSelectAll = false;
    private SimMessageListAdapter mListAdapter = null;
    private MenuEx mMenu;
    private View mMessageBlockView = null;
    private MessageItem mMsgItem = null;
    private NoMessageView mNoMessage;
    private ProgressDialog mProgressDialog = null;
    private AsyncQueryHandler mQueryHandler = null;
    private int mQueryToken;
    private SimMessageListView mSimList;
    private int mState;
    protected int mSubId = 0;
    private final Handler msgDelete = new Handler() {
        public void handleMessage(Message msg) {
            if (msg.what == 3) {
                if (!MessageUtils.isMultiSimEnabled()) {
                    SimCursorManager.self().clearCursor();
                } else if (ManageSimMessages.this.mSubId == 0) {
                    SimCursorManager.self().clearCursor(1);
                } else {
                    SimCursorManager.self().clearCursor(2);
                }
                ManageSimMessages.this.refreshMessageList();
            } else if (msg.what == 4) {
                Toast.makeText(ManageSimMessages.this, R.string.copy_to_phone_memory_success_Toast, 0).show();
                ManageSimMessages.this.updateState(0);
            } else if (msg.what == 5) {
                Toast.makeText(ManageSimMessages.this, R.string.status_failed_Toast, 0).show();
                ManageSimMessages.this.updateState(0);
            }
            ManageSimMessages.this.registerSimChangeObserver();
            MLog.i("ManageSimMessages", "Register sim change observer!");
        }
    };
    private final ContentObserver simChangeObserver = new ContentObserver(new Handler()) {
        public void onChange(boolean selfUpdate) {
            ManageSimMessages.this.refreshMessageList();
        }
    };

    private class EMUIListViewListener implements EmuiListViewListener {
        private EMUIListViewListener() {
        }

        public void onEnterEditMode() {
            ManageSimMessages.this.mMenu.switchToEdit(true);
        }

        public void onExitEditMode() {
            ManageSimMessages.this.mListAdapter.notifyDataSetChanged();
            ManageSimMessages.this.mMenu.switchToEdit(false);
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

        public EditHandler getHandler(int mode) {
            if (mode == 2) {
                return ManageSimMessages.this.mDeleteHandler;
            }
            if (mode == 4) {
                return ManageSimMessages.this.mCopyHandler;
            }
            return null;
        }
    }

    private class EMUIListViewListenerV3 extends EMUIListViewListener implements SelectionChangedListener, OnItemLongClickListener, OnClickListener {
        private EMUIListViewListenerV3() {
            super();
        }

        public void onClick(View v) {
            ManageSimMessages.this.mSimList.exitEditMode();
        }

        private void updateTitle(int cnt) {
            ManageSimMessages.this.mActionBar.setUseSelecteSize(cnt);
        }

        public void onSelectChange(int selectedSize, int totalSize) {
            boolean z;
            boolean z2 = true;
            ManageSimMessages manageSimMessages = ManageSimMessages.this;
            if (selectedSize != totalSize || selectedSize <= 0) {
                z = false;
            } else {
                z = true;
            }
            manageSimMessages.mIsAllSelected = z;
            ManageSimMessages.this.mMenu.setAllChecked(ManageSimMessages.this.mIsAllSelected, ManageSimMessages.this.isInLandscape());
            MenuEx -get12 = ManageSimMessages.this.mMenu;
            if (selectedSize > 0) {
                z = true;
            } else {
                z = false;
            }
            -get12.setItemEnabled(278925315, z);
            MenuEx -get122 = ManageSimMessages.this.mMenu;
            if (selectedSize <= 0) {
                z2 = false;
            }
            -get122.setItemEnabled(278925336, z2);
            ManageSimMessages.this.mMenu.setItemVisible(278925335, ManageSimMessages.this.canAddtoContact(selectedSize));
            updateTitle(selectedSize);
        }

        public void onEnterEditMode() {
            ManageSimMessages.this.mActionBar.enterEditMode(this);
            ManageSimMessages.this.invalidateOptionsMenu();
        }

        public void onExitEditMode() {
            ManageSimMessages.this.mActionBar.exitEditMode();
            ManageSimMessages.this.setSimMessageTitle();
            ManageSimMessages.this.invalidateOptionsMenu();
            ManageSimMessages.this.updateFooterViewHeight(null);
        }

        public boolean onItemLongClick(AdapterView<?> listView, View view, int position, long id) {
            ManageSimMessages.this.mId = id;
            View v = listView.getChildAt(position - listView.getFirstVisiblePosition());
            if (v == null || !(v instanceof SimMessageListItem)) {
                return true;
            }
            ManageSimMessages.this.mMessageBlockView = ((SimMessageListItem) v).getMessageBlockSuper();
            ManageSimMessages.this.mIsCanCopyToPhone = true;
            ManageSimMessages.this.mIsCanDelete = true;
            ManageSimMessages.this.mIsCanSelectAll = true;
            if (ManageSimMessages.this.canAddtoContactlist(position)) {
                ManageSimMessages.this.mIsCanAddToContactlist = true;
            }
            ManageSimMessages.this.showPopupFloatingToolbar();
            return true;
        }
    }

    private class FloatingCallback2 extends Callback2 {
        private boolean mWasAlreadyClick;

        private FloatingCallback2() {
            this.mWasAlreadyClick = false;
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            mode.setTitle(null);
            mode.setSubtitle(null);
            mode.setTitleOptionalHint(true);
            populateMenuWithItems(menu);
            return true;
        }

        private void populateMenuWithItems(Menu menu) {
            TypefaceSpan span = new TypefaceSpan("default");
            if (ManageSimMessages.this.mIsCanCopyToPhone) {
                SpannableString spanString = new SpannableString(ManageSimMessages.this.getString(R.string.menu_copy_to_phone));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 7, 0, spanString).setShowAsAction(2);
            }
            if (ManageSimMessages.this.mIsCanDelete) {
                spanString = new SpannableString(ManageSimMessages.this.getString(R.string.delete));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 3, 1, spanString).setShowAsAction(2);
            }
            if (ManageSimMessages.this.mIsCanAddToContactlist) {
                spanString = new SpannableString(ManageSimMessages.this.getString(R.string.menu_add_to_contactlist));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 8, 2, spanString).setShowAsAction(2);
            }
            if (ManageSimMessages.this.mIsCanSelectAll) {
                spanString = new SpannableString(ManageSimMessages.this.getString(R.string.menu_add_rcs_more));
                spanString.setSpan(span, 0, spanString.length(), 33);
                menu.add(0, 6, 3, spanString).setShowAsAction(1);
            }
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            if (this.mWasAlreadyClick) {
                return true;
            }
            Long[] selectedItems = new Long[]{Long.valueOf(ManageSimMessages.this.mId)};
            switch (item.getItemId()) {
                case 3:
                    this.mWasAlreadyClick = true;
                    ManageSimMessages.this.confirmDeleteDialogPop(new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ManageSimMessages.this.mSimList.doOperationPop(ManageSimMessages.this.mDeleteHandler, new Long[]{Long.valueOf(ManageSimMessages.this.mId)});
                        }
                    });
                    break;
                case 6:
                    this.mWasAlreadyClick = true;
                    if (MmsConfig.isSmsEnabled(ManageSimMessages.this)) {
                        ManageSimMessages.this.mSimList.enterEditMode(1);
                        ManageSimMessages.this.mSimList.setSeleceted(ManageSimMessages.this.mId, true);
                        ManageSimMessages.this.mListAdapter.notifyDataSetChanged();
                        ManageSimMessages.this.updateFooterViewHeight(null);
                        break;
                    }
                    break;
                case 7:
                    this.mWasAlreadyClick = true;
                    ManageSimMessages.this.mSimList.doOperationPop(ManageSimMessages.this.mCopyHandler, selectedItems);
                    break;
                case 8:
                    this.mWasAlreadyClick = true;
                    ManageSimMessages.this.mSimList.doOperationPop(ManageSimMessages.this.mAddToContactHandler, selectedItems);
                    break;
            }
            if (ManageSimMessages.this.mActionMode != null) {
                ManageSimMessages.this.mActionMode.finish();
                ManageSimMessages.this.mActionMode = null;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            ManageSimMessages.this.mMessageBlockView = null;
            ManageSimMessages.this.mIsCanCopyToPhone = false;
            ManageSimMessages.this.mIsCanDelete = false;
            ManageSimMessages.this.mIsCanAddToContactlist = false;
            ManageSimMessages.this.mIsCanSelectAll = false;
        }

        public void onGetContentRect(ActionMode mode, View view, Rect outRect) {
            if (ManageSimMessages.this.mMessageBlockView != null) {
                outRect.set(0, 0, ManageSimMessages.this.mMessageBlockView.getWidth(), ManageSimMessages.this.mMessageBlockView.getHeight());
            }
        }
    }

    private class MenuEx extends EmuiMenu implements OnCreateContextMenuListener, OnMenuItemClickListener {
        private MessageItem mSelectedItem = null;

        public MenuEx() {
            super(null);
        }

        public boolean onCreateOptionsMenu() {
            return true;
        }

        private void prepareOptionsMenuInEditMode() {
            boolean isLandscape = ManageSimMessages.this.isInLandscape();
            clear();
            addMenuCopyToPhone(isLandscape);
            addMenuDelete(isLandscape);
            addMenuChoice(isLandscape);
            addOverflowMenu(278925335, R.string.menu_add_to_contacts);
        }

        public boolean onPrepareOptionsMenu() {
            boolean isInEditMode;
            if (ManageSimMessages.this.mSimList != null) {
                isInEditMode = ManageSimMessages.this.mSimList.isInEditMode();
            } else {
                isInEditMode = false;
            }
            if (!isInEditMode) {
                return false;
            }
            prepareOptionsMenuInEditMode();
            switchToEdit(isInEditMode);
            ManageSimMessages.this.mSimList.onMenuPrepared();
            return true;
        }

        private void switchToEdit(boolean editable) {
            setItemVisible(278925336, editable);
            setItemVisible(278925315, editable);
            setItemVisible(278925313, editable);
            setItemVisible(278925335, editable ? ManageSimMessages.this.canAddtoContact(1) : false);
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            boolean z = false;
            switch (item.getItemId()) {
                case 16908332:
                    ManageSimMessages.this.onBackPressed();
                    break;
                case 278925312:
                    return false;
                case 278925313:
                    SimMessageListView -get15 = ManageSimMessages.this.mSimList;
                    if (!ManageSimMessages.this.mIsAllSelected) {
                        z = true;
                    }
                    -get15.setAllSelected(z);
                    break;
                case 278925315:
                    if (!ManageSimMessages.this.mSimList.isInEditMode()) {
                        ManageSimMessages.this.mSimList.enterEditMode(2);
                        break;
                    }
                    ManageSimMessages.this.confirmDeleteDialog(new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            ManageSimMessages.this.mSimList.doOperation(ManageSimMessages.this.mDeleteHandler);
                            ManageSimMessages.this.mSimList.exitEditMode();
                        }
                    });
                    break;
                case 278925335:
                    ManageSimMessages.this.mSimList.doOperation(ManageSimMessages.this.mAddToContactHandler);
                    ManageSimMessages.this.mSimList.exitEditMode();
                    break;
                case 278925336:
                    if (!ManageSimMessages.this.mSimList.isInEditMode()) {
                        ManageSimMessages.this.mSimList.enterEditMode(8);
                        break;
                    }
                    ManageSimMessages.this.mSimList.doOperation(ManageSimMessages.this.mCopyHandler);
                    ManageSimMessages.this.mSimList.exitEditMode();
                    break;
            }
            return true;
        }

        public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
            if (v != null && menuInfo != null && menu != null && (v instanceof FavoritesListView)) {
                Cursor cursor = ManageSimMessages.this.mListAdapter.getCursor();
                if (HwBaseActivity.isCurosrValide(cursor)) {
                    String type = cursor.getString(0);
                    long msgId = cursor.getLong(1);
                    this.mSelectedItem = ManageSimMessages.this.mListAdapter.getCachedMessageItem(type, msgId, cursor);
                    if (this.mSelectedItem == null) {
                        MLog.e("ManageSimMessages", "Cannot load message item for type = " + type + ", msgId = " + msgId);
                        return;
                    }
                    menu.setHeaderTitle(R.string.message_options);
                    if (MmsConfig.isSmsEnabled(ManageSimMessages.this.getApplicationContext())) {
                        menu.add(0, 1, 0, R.string.delete_message).setOnMenuItemClickListener(this);
                        menu.add(0, 0, 0, R.string.button_copy_text).setOnMenuItemClickListener(this);
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
            }
            this.mSelectedItem = null;
            return false;
        }
    }

    private class QueryHandler extends AsyncQueryHandlerEx {
        private final ManageSimMessages mParent;

        public QueryHandler(ContentResolver contentResolver, ManageSimMessages parent) {
            super(contentResolver);
            this.mParent = parent;
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            if (ManageSimMessages.this.mQueryToken == token && !ManageSimMessages.this.isFinishing()) {
                ManageSimMessages.this.mCursor = cursor;
                if (ManageSimMessages.this.mCursor != null) {
                    if (!ManageSimMessages.this.mCursor.moveToFirst()) {
                        ManageSimMessages.this.updateState(1);
                    } else if (ManageSimMessages.this.mListAdapter == null) {
                        ManageSimMessages.this.mListAdapter = new SimMessageListAdapter(this.mParent, ManageSimMessages.this.mCursor, ManageSimMessages.this.mSimList, false, null);
                        ManageSimMessages.this.mSimList.setAdapter(ManageSimMessages.this.mListAdapter);
                        ManageSimMessages.this.mSimList.setOnCreateContextMenuListener(this.mParent);
                        ManageSimMessages.this.mSimList.setClickable(true);
                        ManageSimMessages.this.updateState(0);
                    } else {
                        ManageSimMessages.this.mListAdapter.changeCursor(ManageSimMessages.this.mCursor);
                        ManageSimMessages.this.updateState(0);
                    }
                    ManageSimMessages.this.startManagingCursor(ManageSimMessages.this.mCursor);
                } else {
                    ManageSimMessages.this.updateState(1);
                }
            }
        }
    }

    protected void onStop() {
        super.onStop();
    }

    protected void onDestroy() {
        super.onDestroy();
        if (this.mMenu != null) {
            this.mMenu.clear();
        }
        if (this.mListAdapter != null) {
            this.mListAdapter.clearTextSpanCache(true);
        }
    }

    protected void onCreate(Bundle icicle) {
        super.onCreate(icicle);
        this.mContentResolver = getContentResolver();
        this.mQueryHandler = new QueryHandler(this.mContentResolver, this);
        setContentView(R.layout.sim_list);
        this.mSimList = (SimMessageListView) findViewById(R.id.message_list);
        this.mSimList.setFastScrollEnabled(true);
        this.mFooterView = LayoutInflater.from(this).inflate(R.layout.blank_footer_view, this.mSimList, false);
        this.mSimList.setFooterDividersEnabled(false);
        this.mSimList.addFooterView(this.mFooterView, null, false);
        updateFooterViewHeight(null);
        this.mMenu = new MenuEx();
        this.mMenu.setContext(this);
        this.mActionBar = new EmuiActionBar(this);
        this.mNoMessage = (NoMessageView) findViewById(R.id.sim_empty_message);
        this.mNoMessage.setViewType(4);
        this.mSubId = MessageUtils.getSimIdFromIntent(getIntent(), MessageUtils.getDefaultSubscription());
        EMUIListViewListenerV3 listener = new EMUIListViewListenerV3();
        this.mSimList.setListViewListener(listener);
        this.mSimList.setSelectionChangeLisenter(listener);
        this.mSimList.setOnItemLongClickListener(listener);
        this.mProgressDialog = new ProgressDialog(this);
        this.mProgressDialog.setCancelable(false);
        init();
    }

    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        setIntent(intent);
        init();
    }

    private void init() {
        MessagingNotification.cancelNotification(getApplicationContext(), 234);
        getActionBar().setDisplayHomeAsUpEnabled(true);
        updateState(2);
        startQuery();
    }

    private void startQuery() {
        try {
            this.mQueryToken = (int) new Date().getTime();
            if (!MessageUtils.isMultiSimEnabled()) {
                this.mQueryHandler.startQuery(this.mQueryToken, null, ICC_URI, null, null, null, null);
            } else if (this.mSubId == 0) {
                this.mQueryHandler.startQuery(this.mQueryToken, null, ICC1_URI, null, null, null, null);
            } else {
                this.mQueryHandler.startQuery(this.mQueryToken, null, ICC2_URI, null, null, null, null);
            }
        } catch (SQLiteException e) {
            ErrorMonitor.reportErrorInfo(8, getClass() + " startQuery", e);
        }
    }

    private void refreshMessageList() {
        updateState(2);
        if (this.mCursor != null) {
            stopManagingCursor(this.mCursor);
            try {
                if (Integer.parseInt(VERSION.SDK) < 14) {
                    this.mCursor.close();
                }
            } catch (NumberFormatException e) {
                MLog.e("ManageSimMessages", "error:" + e);
            }
        }
        startQuery();
    }

    public void onResume() {
        super.onResume();
        registerSimChangeObserver();
        HwBackgroundLoader.getInst().reloadDataDelayed(2, 300);
    }

    public void onPause() {
        super.onPause();
        unregisterReceiver(this.mBroadcastReceiver);
        this.mContentResolver.unregisterContentObserver(this.simChangeObserver);
        MLog.i("ManageSimMessages", "Unregister sim change observer!");
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
    }

    private void registerSimChangeObserver() {
        MLog.i("ManageSimMessages", "Register sim change observer!");
        if (!MessageUtils.isMultiSimEnabled()) {
            this.mContentResolver.registerContentObserver(ICC_URI, true, this.simChangeObserver);
        } else if (this.mSubId == 0) {
            this.mContentResolver.registerContentObserver(ICC1_URI, true, this.simChangeObserver);
        } else {
            this.mContentResolver.registerContentObserver(ICC2_URI, true, this.simChangeObserver);
        }
        registerReceiver(this.mBroadcastReceiver, new IntentFilter("android.intent.action.SIM_STATE_CHANGED"));
    }

    private boolean copyToPhoneMemory(Cursor cursor) {
        if (cursor == null) {
            return false;
        }
        String address = cursor.getString(cursor.getColumnIndexOrThrow("address"));
        String body = cursor.getString(cursor.getColumnIndexOrThrow("body"));
        Long date = Long.valueOf(cursor.getLong(cursor.getColumnIndexOrThrow("date")));
        if (date.intValue() == 0) {
            date = Long.valueOf(System.currentTimeMillis());
        }
        boolean isCopySuccess = true;
        try {
            if (isIncomingMessage(cursor)) {
                if (MessageUtils.isMultiSimEnabled()) {
                    if (smsInboxAddMessage(this.mContentResolver, Inbox.CONTENT_URI, address, body, null, date, true, this.mSubId) == null) {
                        isCopySuccess = false;
                    }
                } else if (smsInboxAddMessage(this.mContentResolver, Inbox.CONTENT_URI, address, body, null, date, true, this.mDefaultSubId) == null) {
                    isCopySuccess = false;
                }
            } else if (MessageUtils.isMultiSimEnabled()) {
                if (MessageUtils.smsSentAddMessage(this.mContentResolver, address, body, null, date, this.mSubId) == null) {
                    isCopySuccess = false;
                }
            } else if (MessageUtils.smsSentAddMessage(this.mContentResolver, address, body, null, date, this.mDefaultSubId) == null) {
                isCopySuccess = false;
            }
            return isCopySuccess;
        } catch (SQLiteException e) {
            ErrorMonitor.reportErrorInfo(8, getClass() + " Copy message to phone failed.", e);
            return false;
        } catch (Throwable e2) {
            ErrorMonitor.reportErrorInfo(8, getClass() + " Copy message to phone failed.", e2);
            return false;
        }
    }

    public static Uri smsInboxAddMessage(ContentResolver resolver, Uri uri, String address, String body, String subject, Long date, boolean read, int subId) {
        ContentValues values = new ContentValues(8);
        MLog.v("ManageSimMessages", "Telephony addMessageToUri sub id: " + subId);
        if (MessageUtils.isMultiSimEnabled()) {
            values.put("sub_id", Integer.valueOf(subId));
            values.put("network_type", Integer.valueOf(MessageUtils.getNetworkType(subId)));
        }
        values.put("address", address);
        if (date != null) {
            values.put("date", date);
        }
        values.put("read", read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put("seen", read ? Integer.valueOf(1) : Integer.valueOf(0));
        values.put("subject", subject);
        values.put("body", body);
        return resolver.insert(uri, values);
    }

    private boolean isIncomingMessage(Cursor cursor) {
        boolean z = true;
        try {
            int statusOnIcc = cursor.getInt(cursor.getColumnIndexOrThrow("status_on_icc"));
            if (!(statusOnIcc == 1 || statusOnIcc == 3)) {
                z = false;
            }
            return z;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            int messageStatus = cursor.getInt(cursor.getColumnIndexOrThrow("status"));
            if (!(messageStatus == 1 || messageStatus == 3)) {
                z = false;
            }
            return z;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        Menu rightOne;
        if (this.mSimList.isInEditMode()) {
            rightOne = this.mActionBar.getActionMenu();
        } else {
            rightOne = menu;
        }
        if (menu != rightOne) {
            menu.clear();
        }
        this.mMenu.resetOptionMenu(rightOne);
        return this.mMenu.onCreateOptionsMenu();
    }

    public boolean onPrepareOptionsMenu(Menu menu) {
        Menu rightOne;
        if (this.mSimList.isInEditMode()) {
            rightOne = this.mActionBar.getActionMenu();
        } else {
            rightOne = menu;
        }
        if (menu != rightOne) {
            menu.clear();
        }
        this.mMenu.resetOptionMenu(rightOne);
        return this.mMenu.onPrepareOptionsMenu();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (isFinishing()) {
            return true;
        }
        return this.mMenu.onOptionsItemSelected(item);
    }

    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        this.mMenu.onCreateContextMenu(menu, v, menuInfo);
    }

    private void updateState(int state) {
        if (this.mState != state) {
            this.mState = state;
            switch (state) {
                case 0:
                    this.mSimList.setVisibility(0);
                    this.mNoMessage.setVisibility(8, isInMultiWindowMode());
                    setSimMessageTitle();
                    this.mSimList.requestFocus();
                    if (!(this.mProgressDialog == null || !this.mProgressDialog.isShowing() || isFinishing())) {
                        this.mProgressDialog.dismiss();
                        break;
                    }
                case 1:
                    this.mSimList.setVisibility(8);
                    this.mNoMessage.setVisibility(0, isInMultiWindowMode());
                    setSimMessageTitle();
                    if (!(this.mProgressDialog == null || !this.mProgressDialog.isShowing() || isFinishing())) {
                        this.mProgressDialog.dismiss();
                        break;
                    }
                case 2:
                    this.mSimList.setVisibility(8);
                    this.mNoMessage.setVisibility(8);
                    setSimMessageTitle();
                    this.mProgressDialog.setMessage(getResources().getString(R.string.refreshing));
                    this.mProgressDialog.show();
                    break;
                case 3:
                    this.mSimList.setVisibility(8);
                    this.mNoMessage.setVisibility(8, isInMultiWindowMode());
                    setTitle(getString(R.string.wait));
                    this.mProgressDialog.setMessage(getResources().getString(R.string.wait));
                    this.mProgressDialog.show();
                    break;
                default:
                    MLog.e("ManageSimMessages", "Invalid State");
                    break;
            }
            invalidateOptionsMenu();
        }
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        invalidateOptionsMenu();
        updateFooterViewHeight(newConfig);
        if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        }
    }

    private void deleteItemsFromSim(final Long[] selectedItems) {
        Long[] items = selectedItems;
        ThreadEx.execute(new Runnable() {
            public void run() {
                ManageSimMessages.this.mContentResolver.unregisterContentObserver(ManageSimMessages.this.simChangeObserver);
                MLog.i("ManageSimMessages", "Unregister sim change observer!");
                for (Long id : selectedItems) {
                    ManageSimMessages.this.deleteFromSim(ManageSimMessages.this.getApplicationContext(), id);
                }
                ManageSimMessages.this.msgDelete.sendEmptyMessage(3);
            }
        });
    }

    private void deleteFromSim(Context context, Long id) {
        if (!MessageUtils.isMultiSimEnabled()) {
            deleteSimMessage(context, ICC_URI, id.toString());
        } else if (this.mSubId == 0) {
            deleteSimMessage(context, ICC1_URI, id.toString());
        } else {
            deleteSimMessage(context, ICC2_URI, id.toString());
        }
    }

    public static final void deleteSimMessage(Context context, Uri uri, String messageIndexString) {
        try {
            SqliteWrapper.delete(context, context.getContentResolver(), uri.buildUpon().appendPath(messageIndexString).build(), null, null);
        } catch (Exception e) {
            MLog.e("ManageSimMessages", "delete message from SIM error.");
        }
    }

    public void onBackPressed() {
        if (this.mSimList.isInEditMode()) {
            this.mSimList.exitEditMode();
            this.mActionBar.exitEditMode();
        } else if (this.mActionMode != null) {
            this.mActionMode.finish();
            this.mActionMode = null;
        } else {
            super.onBackPressed();
        }
    }

    private void copyMessagesToPhone(final Long[] selectedItems) {
        ThreadEx.execute(new Runnable() {
            public void run() {
                Cursor cursor = ManageSimMessages.this.mListAdapter.getCursor();
                if (cursor == null) {
                    MLog.e("ManageSimMessages", "Cursor is null.");
                } else if (cursor.moveToFirst()) {
                    boolean isCopySuccess = true;
                    ManageSimMessages.this.mContentResolver.unregisterContentObserver(ManageSimMessages.this.simChangeObserver);
                    MLog.i("ManageSimMessages", "Unregister sim change observer!");
                    Arrays.sort(selectedItems);
                    while (!cursor.isAfterLast()) {
                        Long itemsId = Long.valueOf(ManageSimMessages.getSimItemId(cursor));
                        MLog.e("MultiChoiceCopy", " ready to copy seleted:" + itemsId);
                        if (Arrays.binarySearch(selectedItems, itemsId) < 0) {
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } else if (!ManageSimMessages.this.copyToPhoneMemory(cursor)) {
                            isCopySuccess = false;
                            break;
                        } else if (!cursor.moveToNext()) {
                            break;
                        }
                    }
                    if (!isCopySuccess) {
                        MLog.w("ManageSimMessages", "Copy all message to phone failed.");
                        ManageSimMessages.this.msgDelete.sendEmptyMessage(5);
                    } else if (!cursor.isClosed()) {
                        MLog.d("ManageSimMessages", "Copy to phone success.");
                        ManageSimMessages.this.msgDelete.sendEmptyMessage(4);
                    }
                } else {
                    MLog.w("ManageSimMessages", "No first cursor exist.");
                }
            }
        });
    }

    public static long getSimItemId(Cursor cursor) {
        if (cursor != null) {
            return cursor.getLong(cursor.getColumnIndex("index_on_icc"));
        }
        return 0;
    }

    public void onSelectChange(int selectedSize, int totalSize) {
    }

    public void showPopupFloatingToolbar() {
        this.mActionMode = this.mMessageBlockView.startActionMode(new FloatingCallback2(), 1);
        this.mActionMode.hide(0);
    }

    private void confirmDeleteDialog(DialogInterface.OnClickListener l) {
        String message;
        if (this.mIsAllSelected) {
            message = getResources().getString(R.string.whether_delete_all_messages);
        } else {
            NumberFormat.getIntegerInstance().setGroupingUsed(false);
            int size = this.mSimList.getRecorder().size();
            message = getResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, size, new Object[]{format.format((long) size)});
        }
        View contents = View.inflate(this, R.layout.delete_thread_dialog_view, null);
        ((TextView) contents.findViewById(R.id.tv_deleted_message)).setText(message);
        MessageUtils.setButtonTextColor(new Builder(this).setCancelable(true).setView(contents).setPositiveButton(R.string.delete, l).setNegativeButton(R.string.no, null).show(), -1, getResources().getColor(R.color.mms_unread_text_color));
    }

    private void confirmDeleteDialogPop(DialogInterface.OnClickListener l) {
        NumberFormat.getIntegerInstance().setGroupingUsed(false);
        MessageUtils.setButtonTextColor(new Builder(this).setCancelable(true).setTitle(getResources().getQuantityString(R.plurals.whether_delete_selected_messages_2, 1, new Object[]{format.format(1)})).setPositiveButton(R.string.delete, l).setNegativeButton(R.string.no, null).show(), -1, getResources().getColor(R.color.mms_unread_text_color));
    }

    private void setSimMessageTitle() {
        if (this.mActionBar != null) {
            if (!MessageUtils.getMultiSimState()) {
                this.mActionBar.setTitle(getString(R.string.pref_title_manage_sim_messages));
            } else if (this.mSubId == 0) {
                this.mActionBar.setTitle(getString(R.string.pref_title_manage_simuim1_messages_ug));
            } else {
                this.mActionBar.setTitle(getString(R.string.pref_title_manage_simuim2_messages_ug));
            }
        }
    }

    private void addRecipientsToContact(Long[] selectedItems) {
        if (selectedItems.length == 1) {
            String address = getAdrressFromItemId(selectedItems[0].longValue());
            Intent addContactIntent = new Intent("com.android.contacts.action.SHOW_OR_CREATE_CONTACT", Uri.fromParts("tel", address, null));
            addContactIntent.putExtra("phone", address);
            addContactIntent.putExtra("phone_type", 2);
            addContactIntent.setFlags(524288);
            addContactIntent.putExtra("intent_key_is_from_dialpad", true);
            startActivity(addContactIntent);
        }
    }

    private boolean canAddtoContact(int selectSize) {
        if (selectSize != 1) {
            return false;
        }
        Long[] itemsId = this.mSimList.getRecorder().getAllSelectItems();
        if (itemsId.length != 1) {
            return false;
        }
        String address = getAdrressFromItemId(itemsId[0].longValue());
        return (TextUtils.isEmpty(address) || ((Contact) ContactList.getByNumbers(address, false, false).get(0)).existsInDatabase()) ? false : true;
    }

    private boolean canAddtoContactlist(int pos) {
        String address = getAdrressFromItemId(this.mListAdapter.getItemId(pos));
        if (TextUtils.isEmpty(address) || ((Contact) ContactList.getByNumbers(address, false, false).get(0)).existsInDatabase()) {
            return false;
        }
        return true;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private String getAdrressFromItemId(long itemId) {
        Cursor cursor = this.mListAdapter.getCursor();
        if (cursor == null) {
            MLog.e("ManageSimMessages", "Cursor is null.");
            return "";
        } else if (cursor.moveToFirst()) {
            while (!cursor.isAfterLast() && getSimItemId(cursor) != itemId && cursor.moveToNext()) {
            }
            try {
                return cursor.getString(cursor.getColumnIndexOrThrow("address"));
            } catch (Exception e) {
                e.printStackTrace();
                return "";
            }
        } else {
            MLog.w("ManageSimMessages", "No first cursor exist.");
            return "";
        }
    }

    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        if (this.mNoMessage != null) {
            this.mNoMessage.setInMultiWindowMode(isInMultiWindowMode);
        }
    }

    private void updateFooterViewHeight(Configuration newConfig) {
        if (this.mFooterView != null && this.mSimList != null) {
            boolean isLandscape = newConfig == null ? getResources().getConfiguration().orientation == 2 : newConfig.orientation == 2;
            LayoutParams lp = this.mFooterView.getLayoutParams();
            int dimension = (!this.mSimList.isInEditMode() || (isLandscape && !isInMultiWindowMode())) ? 0 : (int) getResources().getDimension(R.dimen.toolbar_footer_height);
            lp.height = dimension;
            this.mFooterView.setLayoutParams(lp);
        }
    }

    public boolean dispatchTouchEvent(MotionEvent event) {
        switch (event.getAction()) {
            case 0:
                if (this.mActionMode != null) {
                    this.mActionMode.finish();
                    this.mActionMode = null;
                    return true;
                }
                break;
        }
        return super.dispatchTouchEvent(event);
    }
}
