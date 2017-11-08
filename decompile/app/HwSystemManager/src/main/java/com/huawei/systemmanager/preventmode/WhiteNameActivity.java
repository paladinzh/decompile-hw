package com.huawei.systemmanager.preventmode;

import android.app.ActionBar;
import android.app.AlertDialog.Builder;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.ContentProviderOperation;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.AsyncTask.Status;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.ActionMode;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AbsListView.MultiChoiceModeListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.optimize.base.Const;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsHelper;
import com.huawei.systemmanager.util.contacts.ContactsObject.SysContactsObject;
import com.huawei.systemmanager.util.numberlocation.NumberLocationHelper;
import com.huawei.systemmanager.util.phonematch.PhoneMatch;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class WhiteNameActivity extends HsmActivity implements IWhiteNameUpdateListener {
    private static final int DEL_BATCH_SIZE = 100;
    private static final int MSG_HIDE_WAITING_DLG = 6;
    private static final int MSG_SHOW_WAITING_DLG = 4;
    private static final int MSG_WHITELIST_APPEND = 2;
    private static final int MSG_WHITELIST_DELETE = 1;
    private static final int MSG_WHITELIST_UPDATE = 3;
    private static final int ONCE_SHOW_NUM = 20;
    private String TAG = WhiteNameActivity.class.getName();
    private boolean alwaysShowMenu = true;
    private ContactsInfoLoadingTask curLoadingTask = null;
    private boolean isActMode = false;
    private boolean isLoadingData = false;
    private boolean isOnActivityResult = false;
    private AMCallback mCallback = null;
    private MenuItem mCleanMenu;
    private Context mContext;
    private Handler mDelHandler = null;
    private ArrayList<String> mKnownList;
    private LayoutInflater mLayoutInflater;
    private Menu mMenuBar = null;
    private View mNoContactsView = null;
    private PreventConfig mPreventConfig = null;
    private WhiteNameUpdateReceiver mReceiver = null;
    private ProgressDialog mWaitingDialog = null;
    private List<WhiteNameInfo> mWhiteListUseByList = null;
    private ListView mWhiteListView = null;
    private DataListAdapt mWhiteNameListAdapter = null;

    private class AMCallback implements MultiChoiceModeListener {
        private boolean isAllChecked;
        private boolean isForceUnchecked;
        private Button mCancelBtn;
        private MenuItem mDelBtn;
        private OnClickListener mListener;
        private ActionMode mMode;
        private TextView mOperatorView;
        private MenuItem mSelAllBtn;
        private TextView mTitleView;

        private AMCallback() {
            this.mMode = null;
            this.isAllChecked = false;
            this.isForceUnchecked = false;
            this.mListener = new OnClickListener() {
                public void onClick(View v) {
                    AMCallback.this.finish();
                }
            };
        }

        public boolean onCreateActionMode(ActionMode mode, Menu menu) {
            setActionBar(mode);
            WhiteNameActivity.this.getMenuInflater().inflate(R.menu.preventmode_editcontacts_menu, menu);
            this.mDelBtn = menu.findItem(R.id.deleted_select_contacts);
            this.mSelAllBtn = menu.findItem(R.id.select_all_contacts);
            WhiteNameActivity.this.isActMode = true;
            this.isForceUnchecked = false;
            this.mMode = mode;
            WhiteNameActivity.this.doSelect(false);
            onListDataChanged();
            return true;
        }

        public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
            return true;
        }

        public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
            switch (item.getItemId()) {
                case R.id.select_all_contacts:
                    selectAllContacts();
                    break;
                case R.id.deleted_select_contacts:
                    deleteWhiteList();
                    break;
            }
            return true;
        }

        public void onDestroyActionMode(ActionMode mode) {
            WhiteNameActivity.this.isActMode = false;
            this.mMode = null;
        }

        public void onItemCheckedStateChanged(ActionMode mode, int position, long id, boolean checked) {
            boolean z = false;
            if (this.isForceUnchecked) {
                this.isForceUnchecked = false;
                return;
            }
            if (!WhiteNameActivity.this.getItemCheckState(position)) {
                z = true;
            }
            changeCheckBoxState(z, position);
            if (WhiteNameActivity.this.mWhiteListView.getCheckedItemCount() == 0) {
                this.isForceUnchecked = true;
                WhiteNameActivity.this.mWhiteListView.setItemChecked(position, true);
            }
        }

        public void finish() {
            if (this.mMode != null) {
                this.mMode.finish();
            }
        }

        private void setActionBar(ActionMode mode) {
            RelativeLayout titleLayout = (RelativeLayout) LayoutInflater.from(WhiteNameActivity.this).inflate(R.layout.prevent_actionbar, null, false);
            mode.setCustomView(titleLayout);
            this.mOperatorView = (TextView) titleLayout.findViewById(R.id.titleContainer).findViewById(R.id.operator);
            this.mTitleView = (TextView) titleLayout.findViewById(R.id.titleContainer).findViewById(R.id.num);
            this.mCancelBtn = (Button) titleLayout.findViewById(R.id.btn);
            this.mCancelBtn.setOnClickListener(this.mListener);
        }

        private void changeCheckBoxState(boolean enable, int position) {
            if (!WhiteNameActivity.this.isOverLastData(position)) {
                WhiteNameActivity.this.setItemCheckState(position, enable);
                onListDataChanged();
            }
        }

        private void updateCheckedNumber() {
            boolean z = true;
            int number = WhiteNameActivity.this.getCheckedItemNum();
            this.isAllChecked = WhiteNameActivity.this.isAllItemChecked(number);
            if (number > 0) {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Select);
                this.mTitleView.setVisibility(0);
                this.mTitleView.setText(NumberFormat.getInstance().format((long) number));
                this.mDelBtn.setEnabled(true);
            } else {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mTitleView.setVisibility(8);
                this.mDelBtn.setEnabled(false);
            }
            if (number == 0 || !this.isAllChecked) {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_status);
                this.mSelAllBtn.setTitle(R.string.select_all);
                this.mSelAllBtn.setChecked(false);
            } else {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_pressed);
                this.mSelAllBtn.setTitle(R.string.unselect_all);
                this.mSelAllBtn.setChecked(true);
            }
            MenuItem menuItem = this.mSelAllBtn;
            if (WhiteNameActivity.this.mWhiteListUseByList.size() == 0) {
                z = false;
            }
            menuItem.setEnabled(z);
        }

        private void selectAllContacts() {
            if (!WhiteNameActivity.this.handleLoadingData()) {
                this.isAllChecked = !this.isAllChecked;
                WhiteNameActivity.this.doSelect(this.isAllChecked);
                onListDataChanged();
            }
        }

        private void deleteWhiteList() {
            if (!WhiteNameActivity.this.handleLoadingData()) {
                WhiteNameActivity.this.showCleanDialog();
            }
        }

        private void onListDataChanged() {
            WhiteNameActivity.this.notifyAdapter();
            updateCheckedNumber();
        }
    }

    private class ContactsInfoLoadingTask extends AsyncTask<Intent, Void, List<WhiteNameInfo>> {
        private AtomicBoolean mIsAbortLoadingTask;

        private ContactsInfoLoadingTask() {
            this.mIsAbortLoadingTask = new AtomicBoolean(false);
        }

        public void cancelLoading(boolean mayInterruptIfRunning) {
            HwLog.i(WhiteNameActivity.this.TAG, "ContactsInfoLoadingTask-cancelLoading: Canceled");
            cancel(mayInterruptIfRunning);
            if (mayInterruptIfRunning) {
                this.mIsAbortLoadingTask.set(true);
            }
        }

        protected void onPreExecute() {
            WhiteNameActivity.this.showWaitingDialog();
            this.mIsAbortLoadingTask.set(false);
            super.onPreExecute();
        }

        protected List<WhiteNameInfo> doInBackground(Intent... params) {
            WhiteNameActivity.this.isLoadingData = true;
            List<WhiteNameInfo> dataFromContactList = new ArrayList();
            Intent selectedContacts = params[0];
            if (selectedContacts != null) {
                List<SysContactsObject> contactList = ContactsHelper.getSelectedContacts(selectedContacts, WhiteNameActivity.this.mContext, this.mIsAbortLoadingTask);
                if (Utility.isNullOrEmptyList(contactList)) {
                    HwLog.w(WhiteNameActivity.this.TAG, "ContactsInfoLoadingTask-doInBackground: Fail to read contacts data");
                    WhiteNameActivity.this.isLoadingData = false;
                    return dataFromContactList;
                }
                HwLog.d(WhiteNameActivity.this.TAG, "ContactsInfoLoadingTask-doInBackground: Selected count = " + contactList.size());
                WhiteNameActivity.this.mKnownList = WhiteNameActivity.this.getKnownContactsList(WhiteNameActivity.this.mKnownList);
                List<WhiteNameInfo> infoList = new ArrayList();
                int i = 0;
                while (i < contactList.size()) {
                    if (isCancelled()) {
                        HwLog.i(WhiteNameActivity.this.TAG, "ContactsInfoLoadingTask-doInBackground: Canceled. Current index = " + i);
                        break;
                    }
                    SysContactsObject contactsObj = (SysContactsObject) contactList.get(i);
                    if (contactsObj != null) {
                        String phoneNumber = com.huawei.systemmanager.preventmode.util.Utility.reserveData(contactsObj.getNumber());
                        String matchNumber = PhoneMatch.getPhoneNumberMatchInfo(phoneNumber).getPhoneNumber();
                        if (WhiteNameActivity.this.mKnownList.contains(matchNumber)) {
                            HwLog.i(WhiteNameActivity.this.TAG, "this contact already in white list, ignore it.");
                        } else {
                            WhiteNameInfo getFromContact = new WhiteNameInfo(0, contactsObj.getName(), phoneNumber, NumberLocationHelper.queryNumberLocation(WhiteNameActivity.this, phoneNumber), "", PreventConfig.getShortNumber(phoneNumber));
                            long id = WhiteNameActivity.this.insertData(getFromContact);
                            if (id >= 0) {
                                getFromContact.mId = id;
                                dataFromContactList.add(getFromContact);
                                WhiteNameActivity.this.mKnownList.add(matchNumber);
                                infoList.add(getFromContact);
                            }
                            if (i > 0 && i % 20 == 0) {
                                WhiteNameActivity.this.mWhiteListUseByList.addAll(infoList);
                                WhiteNameActivity.this.postUpdateDataMsg(infoList);
                                infoList = new ArrayList();
                            }
                        }
                    }
                    i++;
                }
                if (infoList.size() > 0) {
                    WhiteNameActivity.this.mWhiteListUseByList.addAll(infoList);
                    WhiteNameActivity.this.postUpdateDataMsg(infoList);
                }
                Collections.sort(WhiteNameActivity.this.mWhiteListUseByList, WhiteNameInfo.PREVENT_ALP_COMPARATOR);
            } else {
                dataFromContactList = WhiteNameActivity.this.getHoldWhiteList();
                WhiteNameActivity.this.mWhiteListUseByList.clear();
                WhiteNameActivity.this.mWhiteListUseByList.addAll(dataFromContactList);
                Collections.sort(WhiteNameActivity.this.mWhiteListUseByList, WhiteNameInfo.PREVENT_ALP_COMPARATOR);
            }
            WhiteNameActivity.this.isLoadingData = false;
            return dataFromContactList;
        }

        protected void onPostExecute(List<WhiteNameInfo> DataFromContactList) {
            WhiteNameActivity.this.hideWaitingDialog();
            WhiteNameActivity.this.showAddedContactToast(DataFromContactList);
            WhiteNameActivity.this.refreshUI();
            HwLog.d(WhiteNameActivity.this.TAG, "onPostExecute: loaded count = " + (DataFromContactList == null ? 0 : DataFromContactList.size()));
        }

        protected void onCancelled(List<WhiteNameInfo> DataFromContactList) {
            WhiteNameActivity.this.showAddedContactToast(DataFromContactList);
            WhiteNameActivity.this.hideWaitingDialog();
            HwLog.i(WhiteNameActivity.this.TAG, "onCancelled: loaded count = " + (DataFromContactList == null ? 0 : DataFromContactList.size()));
        }

        protected void onCancelled() {
            WhiteNameActivity.this.hideWaitingDialog();
            HwLog.i(WhiteNameActivity.this.TAG, "onCancelled: Canceled by user");
            super.onCancelled();
        }
    }

    class DataListAdapt extends BaseAdapter {
        private List<WhiteNameInfo> mDataList = new ArrayList();

        DataListAdapt() {
        }

        public int getCount() {
            return this.mDataList.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            int i = 0;
            if (convertView == null) {
                convertView = WhiteNameActivity.this.mLayoutInflater.inflate(R.layout.prevent_mode_white_list_item_edit, null);
            }
            TextView mobileOperatorTextView = (TextView) convertView.findViewById(R.id.prevent_mode_white_item_mobile_operator);
            CheckBox chkBox = (CheckBox) convertView.findViewById(R.id.prevent_mode_white_item_checkbox);
            WhiteNameInfo whiteContact = (WhiteNameInfo) this.mDataList.get(position);
            ((TextView) convertView.findViewById(R.id.prevent_mode_white_item_name)).setText(whiteContact.getContactInfo(WhiteNameActivity.this.mContext));
            String placeInfo = whiteContact.getLocationInfoString();
            if (TextUtils.isEmpty(placeInfo)) {
                mobileOperatorTextView.setVisibility(8);
            } else {
                mobileOperatorTextView.setText(placeInfo);
                mobileOperatorTextView.setVisibility(0);
            }
            chkBox.setChecked(whiteContact.isChecked);
            if (!WhiteNameActivity.this.isActMode) {
                i = 8;
            }
            chkBox.setVisibility(i);
            return convertView;
        }

        public void setData(List<WhiteNameInfo> dataList) {
            this.mDataList.clear();
            this.mDataList.addAll(dataList);
            notifyDataSetChanged();
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.prevent_mode_white_list_activity);
        initView();
        prepareActionBar();
        this.mNoContactsView.setVisibility(8);
        refreshData(null);
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        if (this.mReceiver != null) {
            unregisterReceiver(this.mReceiver);
        }
        hideWaitingDialog();
        super.onDestroy();
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
        }
    }

    private void hideWaitingDialog() {
        if (this.mWaitingDialog != null) {
            if (this.mWaitingDialog.isShowing()) {
                this.mWaitingDialog.dismiss();
            }
            this.mWaitingDialog = null;
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preventmode_addcontacts_menu, menu);
        this.mMenuBar = menu;
        if (this.isLoadingData) {
            showAddMenuItemBtn(this.alwaysShowMenu);
        } else {
            setNoContactsViewShow();
        }
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.add_whitelist_menu:
                sendBrocastToContacts();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PreventConst.REQUEST_CODE_PICK && data != null) {
            this.mNoContactsView.setVisibility(8);
            this.isOnActivityResult = true;
            refreshData(data);
        }
    }

    private ArrayList<String> getKnownContactsList(ArrayList<String> origData) {
        PreventConfig applicationManager = new PreventConfig(this);
        if (origData.size() != 0) {
            return origData;
        }
        origData = new ArrayList();
        Cursor cursor = applicationManager.queryPreventWhiteListDB();
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                do {
                    origData.add(PhoneMatch.getPhoneNumberMatchInfo(com.huawei.systemmanager.preventmode.util.Utility.reserveData(cursor.getString(cursor.getColumnIndex(Const.PREVENT_WHITE_LIST_NUMBER)))).getPhoneNumber());
                } while (cursor.moveToNext());
            }
            cursor.close();
        }
        return origData;
    }

    private void setNoContactsViewShow() {
        boolean z = false;
        boolean isWithout = this.mWhiteListUseByList.size() == 0;
        this.mNoContactsView.setVisibility(isWithout ? 0 : 8);
        if (this.mCleanMenu != null) {
            invalidateOptionsMenu();
            MenuItem menuItem = this.mCleanMenu;
            if (!isWithout) {
                z = true;
            }
            menuItem.setVisible(z);
        }
        showAddMenuItemBtn(this.alwaysShowMenu);
    }

    private void showAddMenuItemBtn(boolean isShow) {
        if (this.mMenuBar != null) {
            invalidateOptionsMenu();
            for (int i = 0; i < this.mMenuBar.size(); i++) {
                this.mMenuBar.getItem(i).setVisible(isShow);
                this.mMenuBar.getItem(i).setEnabled(isShow);
            }
        }
    }

    private void initView() {
        this.mContext = getApplicationContext();
        this.mWhiteListView = (ListView) findViewById(R.id.prevent_mode_white_list);
        this.mNoContactsView = findViewById(R.id.prevent_no_contact_view);
        ViewUtil.initEmptyViewMargin(this.mContext, this.mNoContactsView);
        this.mPreventConfig = new PreventConfig(this.mContext);
        this.mWhiteListUseByList = new ArrayList();
        this.mLayoutInflater = getLayoutInflater();
        this.mWhiteNameListAdapter = new DataListAdapt();
        this.mWhiteListView.setAdapter(this.mWhiteNameListAdapter);
        this.mKnownList = new ArrayList();
        this.isActMode = false;
        this.mCallback = new AMCallback();
        this.mWhiteListView.setChoiceMode(3);
        this.mWhiteListView.setMultiChoiceModeListener(this.mCallback);
        this.mReceiver = new WhiteNameUpdateReceiver(this);
        registerReceiver();
        initDelWhiteNameHandler();
    }

    public void registerReceiver() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(PreventConst.ACTION_PREVENT_MODE_UPDATE_WHITELIST);
        registerReceiver(this.mReceiver, filter, "com.huawei.systemmanager.permission.ACCESS_INTERFACE", null);
    }

    private void prepareActionBar() {
        ActionBar actionBar = getActionBar();
        actionBar.setTitle(R.string.ListViewFirstLine_DoNotDisturb_VIP);
        actionBar.setDisplayHomeAsUpEnabled(true);
    }

    private List<WhiteNameInfo> getHoldWhiteList() {
        Cursor cursor = this.mPreventConfig.queryPreventWhiteListDB();
        List<WhiteNameInfo> dataList = new ArrayList();
        if (cursor != null) {
            cursor.moveToFirst();
            while (!cursor.isAfterLast()) {
                WhiteNameInfo info = new WhiteNameInfo();
                info.parseFrom(cursor);
                dataList.add(info);
                cursor.moveToNext();
            }
            cursor.close();
        }
        return dataList;
    }

    private void sendBrocastToContacts() {
        if (!handleLoadingData()) {
            Intent contactIntent = new Intent("android.intent.action.PICK");
            contactIntent.setType("vnd.android.cursor.dir/phone_v2");
            contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
            contactIntent.putExtra("Launch_WhiteList_Multi_Pick", true);
            contactIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
            try {
                contactIntent.setComponent(new ComponentName(HsmStatConst.CONTACTS_PACKAGE_NAME, "com.android.contacts.activities.ContactSelectionActivity"));
                startActivityForResult(contactIntent, PreventConst.REQUEST_CODE_PICK);
            } catch (Exception e) {
                HwLog.w(this.TAG, "Fail to start default contacts picker");
                try {
                    contactIntent.setComponent(null);
                    startActivityForResult(contactIntent, PreventConst.REQUEST_CODE_PICK);
                } catch (Exception e2) {
                    HwLog.w(this.TAG, "Fail to start com.huawei.community.action.MULTIPLE_PICK");
                }
            }
        }
    }

    private boolean handleLoadingData() {
        if (this.isLoadingData) {
            return true;
        }
        return false;
    }

    private void refreshData(Intent data) {
        ContactsInfoLoadingTask loadingTask = new ContactsInfoLoadingTask();
        loadingTask.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Intent[]{data});
        this.curLoadingTask = loadingTask;
    }

    private void postUpdateDataMsg(List<WhiteNameInfo> obj) {
        Message message = this.mDelHandler.obtainMessage();
        message.what = 2;
        message.obj = obj;
        message.sendToTarget();
    }

    private void appendWhiteList(Object obj) {
        if (obj != null && obj.getClass() == ArrayList.class && this.mWhiteListUseByList != null && ((ArrayList) obj).size() > 0) {
            refreshUI();
        }
    }

    private void updateWhiteList(Object obj) {
        HwLog.d(this.TAG, "updateWhiteList is executed");
        if (obj != null && obj.getClass() == ArrayList.class) {
            ArrayList contacts = (ArrayList) obj;
            if (contacts.size() != 0) {
                updateData(contacts);
                notifyAdapter();
            }
        }
    }

    private void updateData(List<ContactInfo> contacts) {
        for (ContactInfo contact : contacts) {
            for (WhiteNameInfo whitNameInfo : this.mWhiteListUseByList) {
                if (whitNameInfo.updateIfSameContacts(contact)) {
                    break;
                }
            }
        }
    }

    public void sendUpdateNotification(Object obj) {
        Message message = this.mDelHandler.obtainMessage();
        message.what = 3;
        message.obj = obj;
        message.sendToTarget();
    }

    private void showAddedContactToast(List<WhiteNameInfo> DataFromContactList) {
        int addContactNumber = 0;
        if (DataFromContactList != null) {
            addContactNumber = DataFromContactList.size();
        }
        if (addContactNumber > 0 && this.isOnActivityResult) {
            this.isOnActivityResult = false;
        }
    }

    private void refreshUI() {
        setNoContactsViewShow();
        notifyAdapter();
        this.mWhiteListView.invalidate();
    }

    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == 4 && this.curLoadingTask != null && this.curLoadingTask.getStatus() == Status.RUNNING) {
            this.curLoadingTask.cancelLoading(true);
        }
        return super.onKeyDown(keyCode, event);
    }

    private long insertData(WhiteNameInfo info) {
        return this.mPreventConfig.insertPreventWhiteListDB(info.getAsContentValue());
    }

    private void notifyAdapter() {
        if (this.mWhiteNameListAdapter != null) {
            this.mWhiteNameListAdapter.setData(this.mWhiteListUseByList);
        }
    }

    boolean isOverLastData(int pos) {
        return pos >= this.mWhiteListUseByList.size();
    }

    void setItemCheckState(int pos, boolean enable) {
        ((WhiteNameInfo) this.mWhiteListUseByList.get(pos)).isChecked = enable;
    }

    boolean getItemCheckState(int pos) {
        return pos < this.mWhiteListUseByList.size() ? ((WhiteNameInfo) this.mWhiteListUseByList.get(pos)).isChecked : false;
    }

    private int getCheckedItemNum() {
        int num = 0;
        for (WhiteNameInfo contact : this.mWhiteListUseByList) {
            if (contact.isChecked) {
                num++;
            }
        }
        return num;
    }

    private boolean isAllItemChecked(int num) {
        return num == this.mWhiteListUseByList.size();
    }

    private void doSelect(boolean enable) {
        for (WhiteNameInfo contact : this.mWhiteListUseByList) {
            contact.isChecked = enable;
        }
    }

    private List<WhiteNameInfo> getCheckedWhiteList() {
        List<WhiteNameInfo> phoneList = new ArrayList();
        for (WhiteNameInfo info : this.mWhiteListUseByList) {
            if (info.isChecked) {
                phoneList.add(info);
            }
        }
        return phoneList;
    }

    private List<String> getCheckNumber(List<WhiteNameInfo> list) {
        List<String> numList = new ArrayList();
        for (WhiteNameInfo info : list) {
            numList.add(PhoneMatch.getPhoneNumberMatchInfo(info.mNumber).getPhoneNumber());
        }
        return numList;
    }

    private void showCleanDialog() {
        final List<WhiteNameInfo> phoneList = getCheckedWhiteList();
        Builder builder = new Builder(this).setMessage(R.string.prevent_remove_contacts_message).setPositiveButton(getResources().getString(R.string.Toolbar_VIP_Remove), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface arg0, int arg1) {
                final List list = phoneList;
                new Thread("prevent_WhiteNameActivity") {
                    public void run() {
                        WhiteNameActivity.this.mDelHandler.sendEmptyMessage(4);
                        WhiteNameActivity.this.mWhiteListUseByList.removeAll(list);
                        WhiteNameActivity.this.mKnownList.removeAll(WhiteNameActivity.this.getCheckNumber(list));
                        WhiteNameActivity.this.batchDeleteOperation(list, Const.PREVENT_WHITE_LIST);
                        WhiteNameActivity.this.mDelHandler.sendEmptyMessage(6);
                        WhiteNameActivity.this.mDelHandler.sendEmptyMessage(1);
                    }
                }.start();
            }
        }).setNegativeButton(R.string.cancel, null);
        if (phoneList.size() < this.mWhiteListUseByList.size()) {
            builder.setTitle(getResources().getQuantityString(R.plurals.prevent_remove_contacts_confirm_title, phoneList.size(), new Object[]{Integer.valueOf(phoneList.size())}));
        } else {
            builder.setTitle(R.string.prevent_remove_contacts_confirm_all_check);
        }
        builder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void initDelWhiteNameHandler() {
        this.mDelHandler = new Handler() {
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        WhiteNameActivity.this.notifyAdapter();
                        WhiteNameActivity.this.mCallback.finish();
                        WhiteNameActivity.this.setNoContactsViewShow();
                        return;
                    case 2:
                        WhiteNameActivity.this.hideWaitingDialog();
                        WhiteNameActivity.this.appendWhiteList(msg.obj);
                        return;
                    case 3:
                        WhiteNameActivity.this.updateWhiteList(msg.obj);
                        return;
                    case 4:
                        WhiteNameActivity.this.showWaitingDialog();
                        return;
                    case 6:
                        WhiteNameActivity.this.hideWaitingDialog();
                        return;
                    default:
                        return;
                }
            }
        };
    }

    private void batchDeleteOperation(List<WhiteNameInfo> phoneNumList, Uri uri) {
        int listSize = phoneNumList.size();
        HwLog.d(this.TAG, "batchDeleteOperation: Starts, count = " + listSize);
        ArrayList<ContentProviderOperation> ops = new ArrayList(listSize);
        int nCount = 0;
        for (WhiteNameInfo item : phoneNumList) {
            new ContentValues().put("_id", Long.valueOf(item.mId));
            ContentProviderOperation.Builder buider = ContentProviderOperation.newDelete(uri);
            buider.withSelection("_id = ?", new String[]{String.valueOf(item.mId)});
            ops.add(buider.build());
            nCount++;
            if (nCount % 100 == 0) {
                boolean isDeleted = doBatchDelete(ops);
                ops.clear();
                if (!isDeleted) {
                    nCount -= 100;
                    break;
                }
            }
        }
        if (!(ops.isEmpty() || doBatchDelete(ops))) {
            nCount -= nCount % 100;
        }
        HwLog.d(this.TAG, "batchDeleteOperation: Ends, deleted count = " + nCount);
    }

    private boolean doBatchDelete(ArrayList<ContentProviderOperation> ops) {
        try {
            this.mContext.getContentResolver().applyBatch(Const.SMCS_URI_AUTH, ops);
            return true;
        } catch (RemoteException e) {
            HwLog.e(this.TAG, "doBatchDelete: RemoteException", e);
            return false;
        } catch (OperationApplicationException e2) {
            HwLog.e(this.TAG, "doBatchDelete: OperationApplicationException", e2);
            return false;
        }
    }
}
