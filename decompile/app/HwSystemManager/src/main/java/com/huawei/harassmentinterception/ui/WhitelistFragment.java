package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.harassmentinterception.blackwhitelist.AddWhiteListManager;
import com.huawei.harassmentinterception.blackwhitelist.DataShareManager;
import com.huawei.harassmentinterception.blackwhitelist.RemoveWhiteListManager;
import com.huawei.harassmentinterception.blackwhitelist.ResultContext;
import com.huawei.harassmentinterception.blackwhitelist.WhiteListDataLoader;
import com.huawei.harassmentinterception.callback.CheckRestoreSMSCallBack;
import com.huawei.harassmentinterception.callback.ClickConfirmCallBack;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.callback.LoadDataCallBack;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.ParcelableBlacklistItem;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.common.Tables;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.ui.IDataLoadingWidget.DataChangeObserver;
import com.huawei.harassmentinterception.util.HarassmentUtil;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.harassmentinterception.util.dlg.DialogUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WhitelistFragment extends Fragment implements LoadDataCallBack {
    private static final int DELAY_TIME = 20;
    private static final int MSG_CONFIRM_WHITELIST_REMOVE = 4;
    private static final int MSG_WHITELIST_ADD = 6;
    private static final int MSG_WHITELIST_ADD_COMPLETE = 8;
    private static final int MSG_WHITELIST_APPEND = 2;
    private static final int MSG_WHITELIST_CHECK = 5;
    private static final int MSG_WHITELIST_REFRESH = 1;
    private static final int MSG_WHITELIST_REMOVEITEM = 3;
    private static final int MSG_WHITELIST_REMOVE_COMPLETE = 7;
    private static final int REQUEST_CODE_PICK_CALLLOG = 111;
    private static final int REQUEST_CODE_PICK_EDIT = 112;
    private static final int REQUEST_CODE_PICK_SMS = 110;
    private static final String TAG = WhitelistFragment.class.getSimpleName();
    private long mAddListId = 0;
    private AddWhiteListManager mAddListManager = null;
    private HandleListCallBack mAddWhiteListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> contacts) {
            ResultContext context = new ResultContext(result, isExist, contacts);
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 5;
            message.obj = context;
            message.sendToTarget();
        }

        public void onProcessHandleList(Object obj) {
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 6;
            message.obj = obj;
            message.sendToTarget();
        }

        public void onCompleteHandleList(int result) {
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 8;
            message.obj = Integer.valueOf(result);
            WhitelistFragment.this.mHandler.sendMessageDelayed(message, 20);
        }
    };
    ClickConfirmCallBack mClickConfirmCallBack = new ClickConfirmCallBack() {
        public void onClickConfirmButton() {
            WhitelistFragment.this.removeSingleWhiteList();
            HsmStat.statE(77);
        }
    };
    private AlertDialog mCreateNewDialog = null;
    private WhiteListDataLoader mDataLoadingThread = null;
    private DataChangeObserver mDataObserver = new DataChangeObserver();
    private View mFragmentView = null;
    private Handler mHandler = new MyHandler(this);
    private boolean mIsActivityLoadData = false;
    private long mLastClickTime = 0;
    private View mNoWhitelistLayout;
    private Button mOkButton = null;
    private long mRemoveListId = 0;
    private RemoveWhiteListManager mRemoveListManager = null;
    private HandleListCallBack mRemovedWhiteListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> contacts) {
            ResultContext context = new ResultContext(result, isExist, contacts);
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 4;
            message.obj = context;
            message.sendToTarget();
        }

        public void onProcessHandleList(Object obj) {
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 3;
            message.obj = obj;
            message.sendToTarget();
        }

        public void onCompleteHandleList(int result) {
            Message message = WhitelistFragment.this.mHandler.obtainMessage();
            message.what = 7;
            message.obj = Integer.valueOf(result);
            message.sendToTarget();
        }
    };
    CheckRestoreSMSCallBack mRestoreSMSCallBack = new CheckRestoreSMSCallBack() {
        public void onCheckRestoreSMSButton(boolean isChecked) {
            WhitelistFragment.this.addWhiteList(isChecked);
        }
    };
    private ViewGroup mViewGroup = null;
    private ProgressDialog mWaitingDialog = null;
    private List<WhitelistInfo> mWhitelist = new ArrayList();
    private WhitelistAdapter mWhitelistAdapter;
    private ListView mWhitelistView;
    private Uri whitelist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.WHITELIST_TABLE);

    static class MyHandler extends Handler {
        WeakReference<WhitelistFragment> mFragment;

        MyHandler(WhitelistFragment fragment) {
            this.mFragment = new WeakReference(fragment);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            WhitelistFragment fragment = (WhitelistFragment) this.mFragment.get();
            if (fragment != null) {
                switch (msg.what) {
                    case 1:
                        fragment.afterDataLoad(msg.obj);
                        fragment.mDataObserver.resetDataChangeFlag();
                        break;
                    case 2:
                    case 6:
                        fragment.appendWhiteList(msg.obj);
                        fragment.mDataObserver.resetDataChangeFlag();
                        break;
                    case 3:
                        fragment.delWhiteListItem(msg.obj);
                        break;
                    case 4:
                        fragment.confirmContactExist(msg.obj);
                        break;
                    case 5:
                        fragment.showConfirmAddWhiteDlg(msg.obj);
                        break;
                    case 7:
                        fragment.afterDelWhitelist(msg.obj);
                        fragment.mDataObserver.resetDataChangeFlag();
                        break;
                    case 8:
                        fragment.afterAddWhitelist(msg.obj);
                        break;
                }
            }
        }
    }

    class TextChangedClass implements TextWatcher {
        TextChangedClass() {
        }

        public void onTextChanged(CharSequence s, int start, int end, int count) {
            if (s.toString().trim().length() == 0) {
                WhitelistFragment.this.mOkButton.setEnabled(false);
            } else {
                WhitelistFragment.this.mOkButton.setEnabled(true);
            }
        }

        public void afterTextChanged(Editable arg0) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    private static class ViewHolder {
        TextView _contactView;

        private ViewHolder() {
        }
    }

    class WhitelistAdapter extends BaseAdapter {
        WhitelistAdapter() {
        }

        public int getCount() {
            return WhitelistFragment.this.mWhitelist.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            ViewHolder holder;
            if (convertView == null) {
                convertView = WhitelistFragment.this.getLayoutInflater(null).inflate(R.layout.common_list_item_singleline_checkbox, viewgroup, false);
                holder = new ViewHolder();
                holder._contactView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            holder._contactView.setText(((WhitelistInfo) WhitelistFragment.this.mWhitelist.get(position)).getContactInfo(WhitelistFragment.this.getActivity()));
            return convertView;
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        registerDataObserver();
        initListManager();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mFragmentView = inflater.inflate(R.layout.interception_fragment_whitelist, container, false);
        this.mViewGroup = container;
        initFragment(this.mFragmentView);
        return this.mFragmentView;
    }

    public void onResume() {
        showWaitingDialog();
        refreshWhiteList();
        super.onResume();
    }

    public void onDestroy() {
        if (this.mRemoveListManager != null) {
            this.mRemoveListManager.unregisterCallBack();
            this.mRemoveListManager.stop();
            this.mRemoveListManager = null;
        }
        if (this.mAddListManager != null) {
            this.mAddListManager.unregisterCallBack();
            this.mAddListManager.stop();
            this.mAddListManager = null;
        }
        DataShareManager.destory();
        unregisterDataObserver();
        super.onDestroy();
    }

    private void initListManager() {
        this.mRemoveListManager = new RemoveWhiteListManager();
        this.mRemoveListManager.registerCallBack(this.mRemovedWhiteListCallBack);
        this.mAddListManager = new AddWhiteListManager();
        this.mAddListManager.registerCallBack(this.mAddWhiteListCallBack);
    }

    private void initFragment(View fragmentView) {
        this.mWhitelistView = (ListView) fragmentView.findViewById(R.id.whitelist_view);
        this.mWhitelistAdapter = new WhitelistAdapter();
        this.mWhitelistView.setAdapter(this.mWhitelistAdapter);
        this.mWhitelistView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (!WhitelistFragment.this.isDoubleClick(WhitelistFragment.this.mLastClickTime)) {
                    if (WhitelistFragment.this.mRemoveListManager.isRunning()) {
                        HwLog.d(WhitelistFragment.TAG, "onItemClick is Running");
                        return;
                    }
                    WhitelistFragment.this.mLastClickTime = System.currentTimeMillis();
                    WhitelistInfo info = (WhitelistInfo) WhitelistFragment.this.mWhitelist.get(position);
                    ArrayList<ContactInfo> phoneList = new ArrayList();
                    phoneList.add(info);
                    WhitelistFragment.this.mRemoveListId = WhitelistFragment.this.mRemoveListManager.checkListExist(WhitelistFragment.this.getActivity(), phoneList, null);
                }
            }
        });
        this.mWhitelistView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                bundle.putString(ConstValues.GET_CLICKED_ITEM_NUM, ((WhitelistInfo) WhitelistFragment.this.mWhitelist.get(arg2)).getPhone());
                bundle.putInt(ConstValues.GET_FIRST_ITEM_POSITION, WhitelistFragment.this.getAdjustedPosition(arg2));
                intent.setClass(WhitelistFragment.this.getActivity(), WhiteListEditActivity.class);
                intent.putExtras(bundle);
                WhitelistFragment.this.startActivityForResult(intent, 112);
                return true;
            }
        });
    }

    private int getAdjustedPosition(int position) {
        int firstPosition = this.mWhitelistView.getFirstVisiblePosition();
        if (position > (firstPosition + this.mWhitelistView.getLastVisiblePosition()) / 2) {
            return firstPosition + 1;
        }
        return firstPosition;
    }

    private boolean isDoubleClick(long lastClickTime) {
        if (0 != lastClickTime && System.currentTimeMillis() - lastClickTime <= 500) {
            return true;
        }
        return false;
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.interception_whitelistfragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (((BlackWhiteListActivity) getActivity()).getTabChangeStatus()) {
            return super.onOptionsItemSelected(item);
        }
        switch (item.getItemId()) {
            case R.id.menu_add_manually:
                addFromCreateNew();
                break;
            case R.id.menu_add_from_calllog:
                addFromCalllog();
                break;
            case R.id.menu_add_from_sms:
                addFromMessage();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        ((BlackWhiteListActivity) getActivity()).resetTabChangeStatus();
        super.onPrepareOptionsMenu(menu);
    }

    public void refreshWhiteList() {
        if (this.mDataObserver.isDataChanged()) {
            this.mDataObserver.resetDataChangeFlag();
            startLoadingThread();
        } else {
            if (isLoadingData()) {
                showWaitingDialog();
            }
            if (!this.mIsActivityLoadData) {
                refreshListView();
            }
        }
        this.mIsActivityLoadData = false;
    }

    private void startLoadingThread() {
        synchronized (this) {
            if (this.mDataLoadingThread == null) {
                this.mDataLoadingThread = new WhiteListDataLoader("HarassIntercept_WhiteListFrag", this);
                this.mDataLoadingThread.start();
            }
        }
    }

    private boolean isLoadingData() {
        boolean z;
        synchronized (this) {
            z = this.mDataLoadingThread != null;
        }
        return z;
    }

    private void showWaitingDialog() {
        BlackWhiteListActivity ac = (BlackWhiteListActivity) getActivity();
        if (ac != null && this.mWaitingDialog == null && 1 == ac.getSelectedFragment()) {
            this.mWaitingDialog = ProgressDialog.show(ac, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
            this.mWaitingDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    if (WhitelistFragment.this.isLoadingData()) {
                        Activity ac = WhitelistFragment.this.getActivity();
                        if (ac != null) {
                            HwLog.i(WhitelistFragment.TAG, "dialog canceled, but current is loading, just finish activity");
                            ac.finish();
                        }
                    }
                }
            });
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

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        boolean z;
        if (data != null) {
            z = true;
        } else {
            z = false;
        }
        this.mIsActivityLoadData = z;
        if (-1 != resultCode || data == null) {
            HwLog.w(TAG, "onActivityResult: Invalid result resultCode = " + resultCode);
            return;
        }
        String from = "";
        switch (requestCode) {
            case REQUEST_CODE_PICK_SMS /*110*/:
                from = "0";
                onPickResultFromSms(data);
                break;
            case 111:
                from = "1";
                onPickResultFromCallLog(data);
                break;
            case 112:
                break;
            default:
                HwLog.w(TAG, "onActivityResult: Invalid requestCode = " + requestCode);
                break;
        }
        if (!TextUtils.isEmpty(from)) {
            String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, from);
            HsmStat.statE((int) Events.E_HARASSMENT_ADD_WHITELIST, statParam);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void onPickResultFromSms(Intent data) {
        addToWhitelist(data);
    }

    private void removeSingleWhiteList() {
        if (this.mRemoveListManager.isRunning()) {
            HwLog.d(TAG, "removeSingleWhiteList: is running");
        } else {
            this.mRemoveListManager.handleList(getActivity(), this.mRemoveListId, Boolean.valueOf(false));
        }
    }

    private void confirmContactExist(Object object) {
        if (object.getClass() != ResultContext.class) {
            HwLog.d(TAG, "object.getClass() != ResultContext.class");
        }
        ResultContext context = (ResultContext) object;
        String info = ((WhitelistInfo) context.getContacts().get(0)).getContactInfo(getActivity());
        if (context.isExist()) {
            DialogUtil.createRemoveWhitlistDlgIfContactExist(getActivity(), info, this.mClickConfirmCallBack);
        } else {
            DialogUtil.createRemoveWhitlistDlgIfContactNotExist(getActivity(), info, this.mClickConfirmCallBack);
        }
    }

    private void onPickResultFromCallLog(Intent data) {
        addToWhitelist(data);
    }

    private void addToWhitelist(Intent data) {
        ArrayList<ParcelableBlacklistItem> whitelist = DataShareManager.getInstance().copyWhitelistBuff();
        DataShareManager.getInstance().clearWhitelistBuff();
        if (Utility.isNullOrEmptyList(whitelist)) {
            HwLog.d(TAG, "addToWhitelist: Select none");
        } else if (this.mAddListManager.isRunning()) {
            HwLog.d(TAG, "addToWhitelist is running now");
        } else {
            HwLog.d(TAG, "addToWhitelist: Select count =  " + whitelist.size());
            ArrayList<ContactInfo> phoneList = new ArrayList();
            for (ParcelableBlacklistItem item : whitelist) {
                WhitelistInfo contact;
                String hotlineNumber = HotlineNumberHelper.getHotlineNumber(GlobalContext.getContext(), item.getPhone());
                if (TextUtils.isEmpty(hotlineNumber)) {
                    contact = new WhitelistInfo(item);
                } else {
                    HwLog.i(TAG, "this is hotline number");
                    String phone = hotlineNumber;
                    contact = new WhitelistInfo(0, hotlineNumber, HotlineNumberHelper.getHotlineNumberName(GlobalContext.getContext(), hotlineNumber));
                }
                phoneList.add(contact);
            }
            this.mAddListId = this.mAddListManager.checkListExist(GlobalContext.getContext(), phoneList, null);
        }
    }

    private void addFromCreateNew() {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_create_whitelist_dialog, this.mViewGroup, false);
        final EditText mPhoneEditText = (EditText) layout.findViewById(R.id.interception_create_phone);
        final EditText mNameEditText = (EditText) layout.findViewById(R.id.interception_create_name);
        Button mCancelButton = (Button) layout.findViewById(R.id.interception_create_cancel_button);
        this.mOkButton = (Button) layout.findViewById(R.id.interception_create_ok_button);
        if (mPhoneEditText != null && mNameEditText != null) {
            this.mCreateNewDialog = new Builder(getActivity()).setTitle(getResources().getString(R.string.harassmentInterceptionMenuManuallyAdd)).setView(layout).show();
            mPhoneEditText.requestFocus();
            HarassmentUtil.requestInputMethod(this.mCreateNewDialog);
            mPhoneEditText.addTextChangedListener(new TextChangedClass());
            mCancelButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    WhitelistFragment.this.dismissCreateNewDialog();
                }
            });
            this.mOkButton.setOnClickListener(new OnClickListener() {
                public void onClick(View arg0) {
                    String phone = mPhoneEditText.getText().toString();
                    String name = mNameEditText.getText().toString();
                    if (!TextUtils.isEmpty(phone)) {
                        WhitelistFragment.this.createNewWhitelistItem(phone, name);
                        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "2");
                        HsmStat.statE((int) Events.E_HARASSMENT_ADD_WHITELIST, statParam);
                    }
                    WhitelistFragment.this.dismissCreateNewDialog();
                }
            });
        }
    }

    private void createNewWhitelistItem(String phone, String name) {
        if (this.mAddListManager.isRunning()) {
            HwLog.d(TAG, "createNewWhitelistItem is running now");
        } else if (this.mAddListManager.isWhiteList(getActivity(), phone)) {
            HwLog.d(TAG, "createNewWhitelistItem. this phone is in whitelist.");
        } else {
            String hotlineNumber = HotlineNumberHelper.getHotlineNumber(GlobalContext.getContext(), DBAdapter.formatPhoneNumber(phone));
            if (!TextUtils.isEmpty(hotlineNumber)) {
                HwLog.i(TAG, "this is hotline number");
                phone = hotlineNumber;
                name = HotlineNumberHelper.getHotlineNumberName(GlobalContext.getContext(), hotlineNumber);
            }
            WhitelistInfo info = new WhitelistInfo(0, phone, name);
            ArrayList<ContactInfo> phoneList = new ArrayList();
            phoneList.add(info);
            this.mAddListId = this.mAddListManager.checkListExist(GlobalContext.getContext(), phoneList, null);
        }
    }

    private void dismissCreateNewDialog() {
        if (this.mCreateNewDialog != null) {
            this.mCreateNewDialog.dismiss();
            this.mCreateNewDialog = null;
        }
    }

    protected void registerDataObserver() {
        getActivity().getContentResolver().registerContentObserver(this.whitelist_uri, true, this.mDataObserver);
    }

    protected void unregisterDataObserver() {
        getActivity().getContentResolver().unregisterContentObserver(this.mDataObserver);
    }

    private void afterDataLoad(Object object) {
        synchronized (this) {
            this.mDataLoadingThread = null;
        }
        refreshWhiteList(object);
    }

    private void afterAddWhitelist(Object arg) {
        if (!(this.mWhitelist == null || this.mWhitelist.isEmpty())) {
            Collections.sort(this.mWhitelist, WhitelistInfo.WHITELIST_ALP_COMPARATOR);
        }
        refreshListView();
    }

    private void afterDelWhitelist(Object arg) {
        refreshListView();
    }

    private void showConfirmAddWhiteDlg(Object object) {
        HwLog.d(TAG, "showConfirmAddWhiteDlg");
        hideWaitingDialog();
        refreshListView();
        if (object.getClass() != ResultContext.class) {
            HwLog.d(TAG, "object.getClass() != ResultContext.class");
            return;
        }
        boolean isExist = ((ResultContext) object).isExist();
        HwLog.d(TAG, "showConfirmAddWhiteDlg" + isExist);
        if (isExist) {
            DialogUtil.createAddWhiteListDlg(getActivity(), this.mRestoreSMSCallBack);
        } else {
            addWhiteList(false);
        }
    }

    private void addWhiteList(boolean isChecked) {
        if (this.mAddListManager.isRunning()) {
            HwLog.i(TAG, "addWhitelistManager is busy");
            return;
        }
        showWaitingDialog();
        this.mAddListManager.handleList(GlobalContext.getContext(), this.mAddListId, Boolean.valueOf(isChecked));
    }

    private void refreshWhiteList(Object obj) {
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mWhitelist == null)) {
            this.mWhitelist.clear();
            this.mWhitelist.addAll((ArrayList) obj);
        }
        refreshListView();
    }

    private void delWhiteListItem(Object obj) {
        if (obj != null && obj.getClass() == ArrayList.class) {
            this.mWhitelist.removeAll((ArrayList) obj);
        }
    }

    private void appendWhiteList(Object obj) {
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mWhitelist == null)) {
            ArrayList whiteListCarrier = (ArrayList) obj;
            if (whiteListCarrier.size() > 0) {
                this.mWhitelist.addAll(whiteListCarrier);
                refreshListView();
            }
        }
    }

    private void refreshListView() {
        if (this.mWhitelist == null) {
            HwLog.w(TAG, "refreshListView: null == mWhitelist");
        } else if (this.mWhitelistView == null) {
            HwLog.w(TAG, "refreshListView: Fragment is not initialized");
        } else {
            if (this.mWhitelist.size() > 0) {
                this.mWhitelistView.setVisibility(0);
                updateNoDataView(8);
            } else {
                this.mWhitelistView.setVisibility(8);
                updateNoDataView(0);
            }
            HwLog.d(TAG, "refreshListView");
            hideWaitingDialog();
            this.mWhitelistAdapter.notifyDataSetChanged();
            this.mWhitelistView.invalidate();
        }
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoWhitelistLayout == null) {
                ViewStub stub = (ViewStub) this.mFragmentView.findViewById(R.id.viewstub_no_whitelist);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoWhitelistLayout = this.mFragmentView.findViewById(R.id.no_whitelist_view);
                ViewUtil.initEmptyViewMargin(getActivity(), this.mNoWhitelistLayout);
                if (this.mNoWhitelistLayout != null) {
                    this.mNoWhitelistLayout.setVisibility(0);
                    return;
                }
                return;
            }
            this.mNoWhitelistLayout.setVisibility(0);
        } else if (this.mNoWhitelistLayout != null) {
            this.mNoWhitelistLayout.setVisibility(8);
        }
    }

    private void postWhitelistRefreshMsg(Object obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 1;
        message.obj = obj;
        message.sendToTarget();
    }

    private void addFromMessage() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), HarassMessageListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PICK_SMS);
    }

    private void addFromCalllog() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), HarassCalllogListActivity.class);
        startActivityForResult(intent, 111);
    }

    public void onCompletedDataLoad(Object object) {
        postWhitelistRefreshMsg(object);
    }
}
