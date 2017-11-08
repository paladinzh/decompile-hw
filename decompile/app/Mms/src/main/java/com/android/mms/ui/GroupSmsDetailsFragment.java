package com.android.mms.ui;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.os.Bundle;
import android.os.Handler;
import android.provider.ContactsContract.Contacts;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ListView;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import com.android.mms.MmsConfig;
import com.android.mms.transaction.SmsReceiver;
import com.android.mms.ui.GroupSmsDetailsListAdapter.OnDataSetChangedListener;
import com.google.android.gms.R;
import com.google.android.mms.MmsException;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.AsyncQueryHandlerEx;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.ui.EmuiMenu;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MmsEmuiActionBar;
import com.huawei.mms.ui.SplitActionBarView;
import com.huawei.mms.ui.SplitActionBarView.OnCustomMenuListener;
import com.huawei.mms.util.HwBackgroundLoader;
import com.huawei.mms.util.HwDualCardNameHelper;
import com.huawei.mms.util.HwMessageUtils;
import java.util.ArrayList;
import rcstelephony.RcsMessagingConstants$Sms;

public class GroupSmsDetailsFragment extends HwBaseFragment implements OnClickListener {
    private MmsEmuiActionBar mActionBar;
    ContentObserver mContactChangeListener = new ContentObserver(new Handler()) {
        public void onChange(boolean updated) {
            HwBackgroundLoader.getUIHandler().removeCallbacks(GroupSmsDetailsFragment.this.mUpdateContactRunner);
            HwBackgroundLoader.getUIHandler().postDelayed(GroupSmsDetailsFragment.this.mUpdateContactRunner, 500);
        }
    };
    private Cursor mCursor = null;
    ArrayList<GroupSmsDetailsItem> mFailListData = new ArrayList();
    private Handler mHandler = new Handler();
    private boolean mIsLastItem;
    private ListView mListView;
    private OnCustomMenuListener mMenuClickListener = new OnCustomMenuListener() {
        public boolean onCustomMenuItemClick(MenuItem menuItem) {
            return GroupSmsDetailsFragment.this.mMenuEx.onOptionsItemSelected(menuItem);
        }
    };
    MenuEx mMenuEx;
    public GroupSmsDetailsListAdapter mMsgListAdapter;
    private DetailMsgListQueryHandler mQueryHandler;
    private SplitActionBarView mSplitActionBar;
    private long mThreadID;
    private long mUID;
    private Runnable mUpdateContactRunner = new Runnable() {
        public void run() {
            if (GroupSmsDetailsFragment.this.mListView != null) {
                GroupSmsDetailsFragment.this.mListView.invalidateViews();
            }
        }
    };

    private final class DetailMsgListQueryHandler extends AsyncQueryHandlerEx implements OnDataSetChangedListener {
        public DetailMsgListQueryHandler(ContentResolver contentResolver) {
            super(contentResolver);
        }

        protected void onQueryComplete(int token, Object cookie, Cursor cursor) {
            switch (token) {
                case 101011000:
                    if (GroupSmsDetailsFragment.this.mUID == ((Long) cookie).longValue()) {
                        if (cursor != null) {
                            GroupSmsDetailsFragment.this.mCursor = cursor;
                            ArrayList<GroupSmsDetailsItem> groupMsgList = GroupSmsDetailsFragment.this.getGroupMsgByCursor(cursor);
                            GroupSmsDetailsFragment.this.updateDetailList(groupMsgList);
                            if (GroupSmsDetailsFragment.this.mMsgListAdapter != null) {
                                GroupSmsDetailsFragment.this.mMsgListAdapter.setData(groupMsgList);
                                GroupSmsDetailsFragment.this.mMsgListAdapter.changeCursor(GroupSmsDetailsFragment.this.mCursor);
                                break;
                            }
                        }
                        MLog.w("Mms_GroupSmsDetails", "the data from query is null");
                        return;
                    }
                    MLog.w("Mms_GroupSmsDetails", "the group_id is not as same as the startListquery/s token");
                    if (cursor != null) {
                        cursor.close();
                    }
                    startMsgListQuery(101011000);
                    return;
                    break;
            }
        }

        protected void onDeleteComplete(int token, Object cookie, int result) {
            super.onDeleteComplete(token, cookie, result);
        }

        public void onContentChanged(GroupSmsDetailsListAdapter adapter) {
            startMsgListQuery(101011000);
        }

        public void onDataSetChanged(GroupSmsDetailsListAdapter adapter) {
        }

        private void startMsgListQuery(int token) {
            cancelOperation(token);
            try {
                StringBuilder lSelection = new StringBuilder("group_id");
                lSelection.append(" = ?");
                MLog.d("Mms_GroupSmsDetails", "startMsgListQuery for sms table");
                startQuery(token, Long.valueOf(GroupSmsDetailsFragment.this.mUID), RcsMessagingConstants$Sms.CONTENT_URI, GroupSmsDetailsListAdapter.DETAILS_PROJECTION, lSelection.toString(), new String[]{String.valueOf(GroupSmsDetailsFragment.this.mUID)}, null);
            } catch (SQLiteException e) {
                MLog.e("Mms_GroupSmsDetails", " startQuery : " + e);
            }
        }
    }

    private class MenuEx extends EmuiMenu {
        public MenuEx() {
            super(null);
        }

        private MenuEx setOptionMenu(Menu menu) {
            this.mOptionMenu = menu;
            return this;
        }

        private Menu getMenu() {
            return this.mOptionMenu;
        }

        public void onPrepareOptionsMenu() {
            GroupSmsDetailsFragment.this.initMenu();
            if (GroupSmsDetailsFragment.this.mFailListData == null || GroupSmsDetailsFragment.this.mFailListData.size() <= 0) {
                GroupSmsDetailsFragment.this.mSplitActionBar.setVisibility(8);
                return;
            }
            addMenu(278925341, R.string.detail_msg_resend_all, getDrawableId(278925341, GroupSmsDetailsFragment.this.isInLandscape()));
            enableMenu();
        }

        private void enableMenu() {
            if (GroupSmsDetailsFragment.this.mFailListData != null && GroupSmsDetailsFragment.this.mFailListData.size() > 0) {
                MLog.d("Mms_GroupSmsDetails", "set optionMenu enable or not");
                boolean isSmsEnable = MmsConfig.isSmsEnabled(GroupSmsDetailsFragment.this.getActivity());
                if (GroupSmsDetailsFragment.this.calculateFailListMsgTypeCnt()[1] != 0) {
                    setItemEnabled(278925341, isSmsEnable);
                } else {
                    setItemEnabled(278925341, false);
                }
            }
        }

        private boolean onOptionsItemSelected(MenuItem item) {
            switch (item.getItemId()) {
                case 278925341:
                    GroupSmsDetailsFragment.this.resendAllFailedMsg();
                    break;
            }
            return true;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        this.mMenuEx = new MenuEx();
        this.mMenuEx.setContext(getActivity());
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        Intent intent = getIntent();
        Bundle bundle = null;
        if (intent != null) {
            bundle = intent.getExtras();
        }
        if (bundle != null) {
            this.mUID = bundle.getLong("group_id");
            this.mThreadID = bundle.getLong("thread_id");
            this.mIsLastItem = bundle.getBoolean("is_last_item");
        }
        View view = inflater.inflate(R.layout.group_sms_detail_layout, container, false);
        this.mActionBar = createEmuiActionBar(view);
        this.mActionBar.setStartIcon(true, (int) R.drawable.ic_public_back, (OnClickListener) this);
        this.mActionBar.setTitle(getResources().getString(R.string.msg_detail));
        this.mSplitActionBar = (SplitActionBarView) view.findViewById(R.id.group_sms_detail_bottom);
        this.mListView = (ListView) view.findViewById(16908298);
        this.mListView.setFastScrollEnabled(true);
        registerContactDataChangeObserver();
        return view;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        this.mQueryHandler = new DetailMsgListQueryHandler(getContext().getContentResolver());
        this.mMsgListAdapter = new GroupSmsDetailsListAdapter(getContext(), null);
        this.mMsgListAdapter.setOnDataSetChangedListener(this.mQueryHandler);
        if (this.mListView != null) {
            this.mListView.setAdapter(this.mMsgListAdapter);
            this.mListView.setDivider(null);
        }
        if (MessageUtils.isMultiSimEnabled()) {
            HwDualCardNameHelper.self().initCardName();
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        MLog.d("Mms_GroupSmsDetails", "onSaveInstanceState called ");
        outState.putLong("group_id", this.mUID);
        outState.putLong("thread_id", this.mThreadID);
        outState.putBoolean("is_last_item", this.mIsLastItem);
    }

    public void onStart() {
        super.onStart();
        this.mHandler.postDelayed(new Runnable() {
            public void run() {
                GroupSmsDetailsFragment.this.mQueryHandler.startMsgListQuery(101011000);
            }
        }, 0);
    }

    public void onDestroy() {
        super.onDestroy();
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.setOnDataSetChangedListener(null);
        }
        unregisterContactDataChangeObserver();
        if (this.mMenuEx != null) {
            this.mMenuEx.clear();
        }
    }

    public void onStop() {
        super.onStop();
        if (this.mQueryHandler != null) {
            this.mQueryHandler.cancelOperation(101011000);
        }
    }

    private ArrayList<GroupSmsDetailsItem> getGroupMsgByCursor(Cursor cursor) {
        ArrayList<GroupSmsDetailsItem> messageList = new ArrayList();
        if (cursor == null) {
            MLog.i("Mms_GroupSmsDetails", "cursor is null");
            return messageList;
        } else if (this.mMsgListAdapter == null) {
            return messageList;
        } else {
            if (cursor.moveToFirst()) {
                MLog.d("Mms_GroupSmsDetails", "getGroupMsgByCursor");
                do {
                    try {
                        messageList.add(new GroupSmsDetailsItem(getContext(), "sms", this.mUID, this.mThreadID, cursor, this.mMsgListAdapter.mColumnsMap));
                    } catch (MmsException e) {
                        MLog.e("Mms_GroupSmsDetails", "MmsException: ", (Throwable) e);
                    }
                } while (cursor.moveToNext());
                return messageList;
            }
            MLog.i("Mms_GroupSmsDetails", "cursor has no content");
            return messageList;
        }
    }

    private void resendAllFailedMsg() {
        if (getListItemNum(this.mFailListData) > 0) {
            Context context = getActivity();
            if (context != null) {
                ContentValues values = new ContentValues(2);
                values.put(NumberInfo.TYPE_KEY, Integer.valueOf(6));
                values.put("date", Long.valueOf(System.currentTimeMillis()));
                StringBuilder selection = new StringBuilder();
                selection.append("group_id");
                selection.append("='").append(this.mUID).append("'");
                selection.append(" and type='5'");
                int count = SqliteWrapper.update(context, context.getContentResolver(), RcsMessagingConstants$Sms.CONTENT_URI, values, selection.toString(), null);
                MLog.d("Mms_GroupSmsDetails", "move failed message to queueBox count: " + count);
                if (count != this.mFailListData.size()) {
                    MLog.e("Mms_GroupSmsDetails", "move failed message to queueBox failed");
                }
                context.sendBroadcast(new Intent("com.android.mms.transaction.SEND_MESSAGE", null, context, SmsReceiver.class));
            }
        }
    }

    private int[] calculateFailListMsgTypeCnt() {
        int[] ret = new int[4];
        for (GroupSmsDetailsItem item : this.mFailListData) {
            switch (item.mType) {
                case 2:
                    ret[0] = ret[0] + 1;
                    break;
                case 4:
                    ret[3] = ret[3] + 1;
                    break;
                case 5:
                    ret[1] = ret[1] + 1;
                    break;
                case 6:
                    ret[2] = ret[2] + 1;
                    break;
                default:
                    break;
            }
        }
        return ret;
    }

    private int getListItemNum(ArrayList<GroupSmsDetailsItem> list) {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    private void updateDetailList(ArrayList<GroupSmsDetailsItem> groupSmsMsgList) {
        this.mFailListData.clear();
        for (GroupSmsDetailsItem item : groupSmsMsgList) {
            if (5 == item.mType) {
                this.mFailListData.add(item);
            }
        }
        invalidateOptionMenu();
    }

    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mActionBar.setActionBarHeight(HwMessageUtils.getSplitActionBarHeight(getContext()));
        initMenu();
        invalidateOptionMenu();
    }

    public void invalidateOptionMenu() {
        Activity act = getActivity();
        if (act != null) {
            act.invalidateOptionsMenu();
            onPrepareOptionsMenu(this.mMenuEx.getMenu());
        }
    }

    public void onPrepareOptionsMenu(Menu menu) {
        if (HwMessageUtils.isSplitOn() || !isInLandscape() || isInMultiWindowMode()) {
            this.mMenuEx.setOptionMenu(this.mSplitActionBar.getMenu()).onPrepareOptionsMenu();
            this.mSplitActionBar.refreshMenu();
            return;
        }
        this.mMenuEx.setOptionMenu(this.mActionBar.getMenu()).onPrepareOptionsMenu();
        this.mActionBar.refreshMenu();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        this.mMenuEx.onOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    protected MmsEmuiActionBar createEmuiActionBar(View fragmentRootView) {
        return new MmsEmuiActionBar(getActivity(), fragmentRootView.findViewById(R.id.compose_message_top), null);
    }

    private void initMenu() {
        boolean showTopMenu = (HwMessageUtils.isSplitOn() || !isInLandscape() || isInMultiWindowMode()) ? false : true;
        this.mSplitActionBar.setVisibility(showTopMenu ? 8 : 0);
        this.mActionBar.showMenu(showTopMenu);
        if (showTopMenu) {
            this.mActionBar.getSplitActionBarView().setOnCustomMenuListener(this.mMenuClickListener);
        } else {
            this.mSplitActionBar.setOnCustomMenuListener(this.mMenuClickListener);
        }
    }

    public void onClick(View v) {
        getActivity().onBackPressed();
    }

    private void registerContactDataChangeObserver() {
        getActivity().getContentResolver().registerContentObserver(Contacts.CONTENT_URI, true, this.mContactChangeListener);
    }

    private void unregisterContactDataChangeObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mContactChangeListener);
    }

    private boolean isInMultiWindowMode() {
        if (getActivity() == null) {
            return false;
        }
        return getActivity().isInMultiWindowMode();
    }
}
