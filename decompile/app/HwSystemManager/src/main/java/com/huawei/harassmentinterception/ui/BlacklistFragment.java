package com.huawei.harassmentinterception.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
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
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.blackwhitelist.DataShareManager;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.ParcelableBlacklistItem;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.common.Tables;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.ui.IDataLoadingWidget.DataChangeObserver;
import com.huawei.harassmentinterception.util.HarassmentUtil;
import com.huawei.harassmentinterception.util.HotlineNumberHelper;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.contacts.ContactsHelper;
import com.huawei.systemmanager.util.contacts.ContactsObject.SysContactsObject;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class BlacklistFragment extends Fragment {
    public static final Comparator<CommonItem> BLACK_LIST_COMPARATOR = new Comparator<CommonItem>() {
        public int compare(CommonItem left, CommonItem right) {
            int leftP = left.getPriority();
            int rightP = right.getPriority();
            if (leftP == rightP) {
                if ((left instanceof BlackListItem) && (right instanceof BlackListItem)) {
                    return BlacklistInfo.HARASSMENT_ALP_COMPARATOR.compare(((BlackListItem) left).getInfo(), ((BlackListItem) right).getInfo());
                }
            } else if (leftP < rightP) {
                return -1;
            } else {
                if (leftP > rightP) {
                    return 1;
                }
            }
            return 0;
        }
    };
    private static final int DELAY_TIME = 20;
    private static final int MSG_BLACKLIST_ADD_COMPLETE = 7;
    private static final int MSG_BLACKLIST_APPEND = 2;
    private static final int MSG_BLACKLIST_CONFIRM_DLG = 6;
    private static final int MSG_BLACKLIST_REFRESH = 1;
    private static final int MSG_BLACKLIST_SORTED = 9;
    private static final int MSG_BLACKLIST_UPDATE = 8;
    private static final int MSG_HIDE_WAITING_DLG = 5;
    private static final int MSG_SHOW_WAITING_DLG = 4;
    private static final int ONCE_UPDATE_UI_NUM = 20;
    private static final int REQUEST_CODE_PICK_CALLLOG = 111;
    private static final int REQUEST_CODE_PICK_CONTACTS = 109;
    private static final int REQUEST_CODE_PICK_EDIT = 112;
    private static final int REQUEST_CODE_PICK_SMS = 110;
    private static final String TAG = "BlacklistFragment";
    private static AtomicInteger mAppendingTaskCount = new AtomicInteger(0);
    private Uri blacklist_uri = Uri.withAppendedPath(Uri.parse("content://com.huawei.systemmanager.HarassmentInterceptionDBProvider"), Tables.BLACKLIST_TABLE);
    private List<BlackListItem> mBlacklist = new ArrayList();
    private BlacklistAdapter mBlacklistAdapter = null;
    private ListView mBlacklistView = null;
    private int mClickPosition = 0;
    private Context mContext = null;
    private AlertDialog mCreateNewDialog = null;
    private Thread mDataLoadingThread = null;
    private DataChangeObserver mDataObserver = new DataChangeObserver();
    private View mFragmentView = null;
    private MyHandler mHandler = new MyHandler();
    private AtomicBoolean mIsAbortLoadingTask = new AtomicBoolean(false);
    private boolean mIsActivityLoadData = false;
    private boolean mIsFirstTimeInitData = true;
    private View mNoBlacklistLayout = null;
    private ViewGroup mViewGroup = null;
    private ProgressDialog mWaitingDialog = null;

    private abstract class CommonItem {
        public abstract int getPriority();

        private CommonItem() {
        }
    }

    private abstract class CategoryItem extends CommonItem {
        abstract String getTitle();

        private CategoryItem() {
            super();
        }
    }

    private class BlackDftCategory extends CategoryItem {
        private BlackDftCategory() {
            super();
        }

        public int getPriority() {
            return 3;
        }

        public String getTitle() {
            return BlacklistFragment.this.getString(R.string.harassment_black_list_category);
        }
    }

    private abstract class BlackListItem extends CommonItem {
        abstract BlacklistInfo getInfo();

        private BlackListItem() {
            super();
        }
    }

    private class BlackDftItem extends BlackListItem {
        private final BlacklistInfo blackDftItem;

        public BlackDftItem(BlacklistInfo blackHeader) {
            super();
            this.blackDftItem = blackHeader;
        }

        public BlacklistInfo getInfo() {
            return this.blackDftItem;
        }

        public int getPriority() {
            return 4;
        }
    }

    private class BlackHeaderCategory extends CategoryItem {
        private BlackHeaderCategory() {
            super();
        }

        public int getPriority() {
            return 1;
        }

        public String getTitle() {
            return BlacklistFragment.this.getContext().getString(R.string.harassment_black_header_category);
        }
    }

    private class BlackHeaderItem extends BlackListItem {
        BlacklistInfo blackHeader;

        public BlackHeaderItem(BlacklistInfo blackHeader) {
            super();
            this.blackHeader = blackHeader;
        }

        public BlacklistInfo getInfo() {
            return this.blackHeader;
        }

        public int getPriority() {
            return 2;
        }
    }

    class BlacklistAdapter extends BaseAdapter {
        private int TYPE_CATEGORY = 0;
        private int TYPE_ITEM = 1;
        List<CommonItem> commonItems = Lists.newArrayList();

        public void setList(List<BlackListItem> list) {
            if (list != null) {
                boolean hasDftItem = false;
                boolean hasHeaderItem = false;
                this.commonItems.clear();
                for (BlackListItem item : list) {
                    if (item instanceof BlackDftItem) {
                        hasDftItem = true;
                        break;
                    }
                }
                for (BlackListItem item2 : list) {
                    if (item2 instanceof BlackHeaderItem) {
                        hasHeaderItem = true;
                        break;
                    }
                }
                if (hasDftItem && hasHeaderItem) {
                    this.commonItems.add(new BlackDftCategory());
                    this.commonItems.add(new BlackHeaderCategory());
                }
                this.commonItems.addAll(list);
                Collections.sort(this.commonItems, BlacklistFragment.BLACK_LIST_COMPARATOR);
                notifyDataSetChanged();
            }
        }

        public int getCount() {
            return this.commonItems.size();
        }

        public CommonItem getItem(int arg0) {
            return (CommonItem) this.commonItems.get(arg0);
        }

        public long getItemId(int arg0) {
            return (long) arg0;
        }

        public int getViewTypeCount() {
            return 2;
        }

        public int getItemViewType(int position) {
            if (getItem(position) instanceof BlackListItem) {
                return this.TYPE_ITEM;
            }
            return this.TYPE_CATEGORY;
        }

        public boolean isEnabled(int position) {
            if (getItemViewType(position) == this.TYPE_CATEGORY) {
                return false;
            }
            return true;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            if (getItemViewType(position) == this.TYPE_ITEM) {
                ViewHolder holder;
                if (convertView == null) {
                    convertView = BlacklistFragment.this.getLayoutInflater(null).inflate(R.layout.common_list_item_twolines_checkbox, viewgroup, false);
                    holder = new ViewHolder();
                    holder._contactView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                    holder._countView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
                    holder._divider = convertView.findViewById(R.id.divider);
                    convertView.setTag(holder);
                } else {
                    holder = (ViewHolder) convertView.getTag();
                }
                BlacklistInfo blacklistInfo = ((BlackListItem) getItem(position)).getInfo();
                BlacklistFragment.this.setTextViewMultiLines(holder._countView, holder._contactView);
                holder._contactView.setText(blacklistInfo.getContactInfo(BlacklistFragment.this.getActivity()));
                holder._countView.setText(blacklistInfo.getOptionText(BlacklistFragment.this.getActivity()));
                if (position + 1 < getCount() && getItemViewType(position + 1) == this.TYPE_CATEGORY) {
                    holder._divider.setVisibility(8);
                }
            } else if (getItemViewType(position) == this.TYPE_CATEGORY) {
                CategoryViewHolder holder2;
                if (convertView == null) {
                    convertView = BlacklistFragment.this.getLayoutInflater(null).inflate(R.layout.permission_list_tab_item_tag, viewgroup, false);
                    holder2 = new CategoryViewHolder();
                    holder2.itemTitleView = (TextView) convertView.findViewById(R.id.tvTagName);
                    convertView.setTag(holder2);
                } else {
                    holder2 = (CategoryViewHolder) convertView.getTag();
                }
                holder2.itemTitleView.setText(((CategoryItem) getItem(position)).getTitle());
            }
            return convertView;
        }
    }

    private static class CategoryViewHolder {
        TextView itemTitleView;

        private CategoryViewHolder() {
        }
    }

    static class CheckedChangeListener implements OnCheckedChangeListener {
        private CheckBox mCkCall = null;
        private CheckBox mCkSms = null;
        private Button mOkButton = null;
        private TextView mTextPhone = null;

        public CheckedChangeListener(Button btnOK, CheckBox ckCall, CheckBox ckSms, TextView etPhone) {
            this.mOkButton = btnOK;
            this.mCkCall = ckCall;
            this.mCkSms = ckSms;
            this.mTextPhone = etPhone;
        }

        public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
            if (TextUtils.isEmpty(this.mTextPhone.getText().toString())) {
                this.mOkButton.setEnabled(false);
            } else {
                this.mOkButton.setEnabled(!this.mCkCall.isChecked() ? this.mCkSms.isChecked() : true);
            }
        }
    }

    class DataLoadingThread extends Thread {
        public DataLoadingThread(String name) {
            super(name);
        }

        public void run() {
            try {
                HwLog.d(BlacklistFragment.TAG, "DataLoadingThread: Starts , TaskCount = " + BlacklistFragment.mAppendingTaskCount);
                BlacklistFragment.this.postShowWaitingDlgMsg();
                BlacklistFragment.this.postBlacklistRefreshMsg(BlacklistFragment.this.transInfoToItems(DBAdapter.getBlacklist(BlacklistFragment.this.mContext)));
                synchronized (BlacklistFragment.this) {
                    BlacklistFragment.this.mDataLoadingThread = null;
                }
                BlacklistFragment.this.postHideWaitingDlgMsg();
                HwLog.d(BlacklistFragment.TAG, "DataLoadingThread: Ends");
            } catch (Exception e) {
                HwLog.e(BlacklistFragment.TAG, "DataLoadingThread-run: Exception", e);
                synchronized (BlacklistFragment.this) {
                    BlacklistFragment.this.mDataLoadingThread = null;
                    BlacklistFragment.this.postHideWaitingDlgMsg();
                    HwLog.d(BlacklistFragment.TAG, "DataLoadingThread: Ends");
                }
            } catch (Throwable th) {
                synchronized (BlacklistFragment.this) {
                    BlacklistFragment.this.mDataLoadingThread = null;
                    BlacklistFragment.this.postHideWaitingDlgMsg();
                    HwLog.d(BlacklistFragment.TAG, "DataLoadingThread: Ends");
                }
            }
        }
    }

    static class MyHandler extends Handler {
        WeakReference<BlacklistFragment> mFragment = null;

        MyHandler() {
        }

        public void setAttachedFragment(BlacklistFragment fragment) {
            this.mFragment = new WeakReference(fragment);
        }

        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (this.mFragment != null) {
                BlacklistFragment fragment = (BlacklistFragment) this.mFragment.get();
                if (fragment != null) {
                    switch (msg.what) {
                        case 1:
                            fragment.refreshBlackList(msg.obj);
                            fragment.mDataObserver.resetDataChangeFlag();
                            break;
                        case 2:
                            fragment.hideWaitingDialog();
                            fragment.appendBlackList((ArrayList) msg.obj);
                            fragment.mDataObserver.resetDataChangeFlag();
                            break;
                        case 4:
                            fragment.showWaitingDialog();
                            break;
                        case 5:
                            fragment.hideWaitingDialog();
                            break;
                        case 6:
                            fragment.hideWaitingDialog();
                            fragment.createAddBlackDlg(msg.obj);
                            break;
                        case 7:
                            fragment.sortAndUpdateBlacklistInBkg();
                            fragment.showAddMessage(msg.obj);
                            break;
                        case 8:
                            fragment.updateUIBlacklist((BlacklistInfo) msg.obj);
                            fragment.mDataObserver.resetDataChangeFlag();
                            break;
                        case 9:
                            fragment.refreshBlackListOnSorted((ArrayList) msg.obj, msg.arg1);
                            break;
                    }
                }
            }
        }
    }

    private class OptionDlgListener implements OnClickListener, OnCancelListener {
        private BlacklistInfo mBlacklistInfo;

        public OptionDlgListener(BlacklistInfo blInfo) {
            this.mBlacklistInfo = blInfo;
        }

        public void onClick(View view) {
            BlacklistFragment.this.dissmissOptionDlg();
            switch (view.getId()) {
                case R.id.btn_edit:
                    BlacklistFragment.this.editBlacklist(this.mBlacklistInfo);
                    return;
                case R.id.btn_delete:
                    BlacklistFragment.this.confirmDeleteBlacklist(this.mBlacklistInfo);
                    return;
                default:
                    return;
            }
        }

        public void onCancel(DialogInterface dialog) {
            BlacklistFragment.this.dissmissOptionDlg();
        }
    }

    class RecoverSmsTask extends AsyncTask<BlacklistInfo, Void, Integer> {
        RecoverSmsTask() {
        }

        protected Integer doInBackground(BlacklistInfo... arg0) {
            BlacklistInfo blInfo = arg0[0];
            int nMsgCount = BlacklistFragment.this.recoverCallAndMessage(blInfo.getPhone(), blInfo.getType());
            HwLog.d(BlacklistFragment.TAG, "recoverCallAndMessage count = " + nMsgCount);
            if (nMsgCount > 0) {
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_COUNT, String.valueOf(nMsgCount));
                HsmStat.statE((int) Events.E_HARASSMENT_RESTORE_MESSAGE, statParam);
            }
            return Integer.valueOf(nMsgCount);
        }

        protected void onPostExecute(Integer result) {
            BlacklistFragment.this.hideWaitingDialog();
            if (BlacklistFragment.this.getActivity() != null) {
                BlacklistFragment.this.refreshBlackList();
                super.onPostExecute(result);
            }
        }

        protected void onPreExecute() {
            BlacklistFragment.this.showWaitingDialog();
            super.onPreExecute();
        }
    }

    static class TextWrapper implements TextWatcher {
        private CheckBox mCkCall = null;
        private CheckBox mCkSms = null;
        private Button mOkButton = null;

        public TextWrapper(Button btnOK, CheckBox ckCall, CheckBox ckSms) {
            this.mOkButton = btnOK;
            this.mCkCall = ckCall;
            this.mCkSms = ckSms;
        }

        public void onTextChanged(CharSequence s, int start, int end, int count) {
            if (s.toString().trim().length() == 0) {
                this.mOkButton.setEnabled(false);
            } else if (this.mCkCall.isChecked() || this.mCkSms.isChecked()) {
                this.mOkButton.setEnabled(true);
            } else {
                this.mOkButton.setEnabled(false);
            }
        }

        public void afterTextChanged(Editable arg0) {
        }

        public void beforeTextChanged(CharSequence arg0, int arg1, int arg2, int arg3) {
        }
    }

    private static class ViewHolder {
        TextView _contactView;
        TextView _countView;
        View _divider;

        private ViewHolder() {
        }
    }

    protected void registerDataObserver() {
        this.mContext.getContentResolver().registerContentObserver(this.blacklist_uri, true, this.mDataObserver);
    }

    protected void unregisterDataObserver() {
        this.mContext.getContentResolver().unregisterContentObserver(this.mDataObserver);
    }

    private void showAddMessage(Object object) {
        hideWaitingDialog();
        if (getActivity() == null) {
            HwLog.w(TAG, "showAddMessage: Fragment is detached ,skip");
        }
    }

    private void refreshBlackList(Object obj) {
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mBlacklist == null)) {
            this.mBlacklist.clear();
            this.mBlacklist.addAll((ArrayList) obj);
            this.mBlacklistAdapter.setList(this.mBlacklist);
        }
        refreshListView();
    }

    private void refreshBlackListOnSorted(ArrayList<BlackListItem> obj, int sortHash) {
        if (obj != null && obj.getClass() == ArrayList.class && this.mBlacklist != null) {
            this.mBlacklist.clear();
            ArrayList<BlackListItem> latestBlackList = obj;
            this.mBlacklist.addAll(obj);
            refreshListView();
        }
    }

    private void sortAndUpdateBlacklistInBkg() {
        final List<BlackListItem> blacklist = new ArrayList(this.mBlacklist);
        final int sortTaskHash = this.mBlacklist.hashCode();
        new Thread("HaraIntercept_sortBlackList") {
            public void run() {
                BlacklistFragment.this.postBlacklistSortedMsg(blacklist, sortTaskHash);
            }
        }.start();
    }

    private void appendBlackList(ArrayList<BlackListItem> obj) {
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mBlacklist == null)) {
            ArrayList<BlackListItem> blackListCarrier = obj;
            if (obj.size() > 0) {
                this.mBlacklist.addAll(obj);
                refreshListView();
            }
        }
    }

    private void refreshListView() {
        if (this.mBlacklistView == null) {
            HwLog.w(TAG, "refreshListView: Fragment is not initialized");
            return;
        }
        if (Utility.isNullOrEmptyList(this.mBlacklist)) {
            this.mBlacklistView.setVisibility(8);
            updateNoDataView(0);
        } else {
            this.mBlacklistView.setVisibility(0);
            updateNoDataView(8);
        }
        hideWaitingDialog();
        this.mBlacklistAdapter.setList(this.mBlacklist);
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoBlacklistLayout == null) {
                ViewStub stub = (ViewStub) this.mFragmentView.findViewById(R.id.viewstub_no_blacklist);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoBlacklistLayout = this.mFragmentView.findViewById(R.id.no_blacklist_view);
                ViewUtil.initEmptyViewMargin(getActivity(), this.mNoBlacklistLayout);
                if (this.mNoBlacklistLayout != null) {
                    this.mNoBlacklistLayout.setVisibility(0);
                    return;
                }
                return;
            }
            this.mNoBlacklistLayout.setVisibility(0);
        } else if (this.mNoBlacklistLayout != null) {
            this.mNoBlacklistLayout.setVisibility(8);
        }
    }

    private void postBlacklistRefreshMsg(List<BlackListItem> obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 1;
        message.obj = obj;
        message.sendToTarget();
    }

    private void postBlacklistAppendMsg(List<BlackListItem> obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 2;
        message.obj = obj;
        message.sendToTarget();
    }

    private void postBlacklistUpdateMsg(CommonItem obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 8;
        message.obj = obj;
        message.sendToTarget();
    }

    private void postShowWaitingDlgMsg() {
        this.mHandler.obtainMessage(4).sendToTarget();
    }

    private void postHideWaitingDlgMsg() {
        this.mHandler.obtainMessage(5).sendToTarget();
    }

    private void postShowConfirmAddBlacklistDlg(Object obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 6;
        message.obj = obj;
        message.sendToTarget();
    }

    private void postCompleteAddBlacklist(Object obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = 7;
        message.obj = obj;
        this.mHandler.sendMessageDelayed(message, 20);
    }

    private void postBlacklistSortedMsg(List<BlackListItem> obj, int sortHashCode) {
        Message message = this.mHandler.obtainMessage();
        message.what = 9;
        message.obj = obj;
        message.arg1 = sortHashCode;
        message.sendToTarget();
    }

    public void onCreate(Bundle savedInstanceState) {
        this.mContext = GlobalContext.getContext();
        this.mHandler.setAttachedFragment(this);
        setHasOptionsMenu(true);
        registerDataObserver();
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mFragmentView = inflater.inflate(R.layout.interception_fragment_blacklist, container, false);
        this.mViewGroup = container;
        initFragment(this.mFragmentView);
        return this.mFragmentView;
    }

    private void initFragment(View fragmentView) {
        this.mBlacklistView = (ListView) fragmentView.findViewById(R.id.blacklist_view);
        this.mBlacklistAdapter = new BlacklistAdapter();
        this.mBlacklistView.setAdapter(this.mBlacklistAdapter);
        this.mBlacklistView.setOnItemLongClickListener(new OnItemLongClickListener() {
            public boolean onItemLongClick(AdapterView<?> adapterView, View arg1, int arg2, long arg3) {
                Intent intent = new Intent();
                Bundle bundle = new Bundle();
                CommonItem clickedInfo = BlacklistFragment.this.mBlacklistAdapter.getItem(arg2);
                if (clickedInfo instanceof BlackListItem) {
                    bundle.putInt("id", ((BlackListItem) clickedInfo).getInfo().getId());
                    bundle.putInt(ConstValues.GET_FIRST_ITEM_POSITION, BlacklistFragment.this.getFirstItemPosition(arg2));
                }
                intent.setClass(BlacklistFragment.this.getActivity(), BlackListEditActivity.class);
                intent.putExtras(bundle);
                BlacklistFragment.this.startActivityForResult(intent, 112);
                return true;
            }
        });
    }

    private void editBlacklist(BlacklistInfo blInfo) {
        showAddOrEditBlacklistDlg(blInfo, blInfo.isBlackListHeader());
    }

    private void updateBlacklistoption(int option) {
        if (isClickedInfoValid()) {
            BlacklistInfo blInfo = null;
            CommonItem item = this.mBlacklistAdapter.getItem(this.mClickPosition);
            if (item instanceof BlackListItem) {
                blInfo = ((BlackListItem) item).getInfo();
            }
            if (!(blInfo == null || option == blInfo.getOption() || DBAdapter.updateBlackListOption(getActivity(), blInfo.getId(), option) <= 0)) {
                blInfo.setOption(option);
                this.mBlacklistAdapter.notifyDataSetChanged();
            }
            return;
        }
        HwLog.w(TAG, "updateBlacklistoption: Invalid blacklist info ,skip edit");
    }

    private void confirmDeleteBlacklist(BlacklistInfo blInfo) {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_remove_blacklist_dialog, this.mViewGroup, false);
        final CheckBox checkBox = (CheckBox) layout.findViewById(R.id.recover_alllog_checkbox);
        ((TextView) layout.findViewById(R.id.recover_alllog_msg)).setText(getResources().getString(R.string.harassmentRemoveWhitelist_DlgMsg_InContact));
        final int nClickIndex = this.mClickPosition;
        Builder dlgBuilder = new Builder(getActivity());
        dlgBuilder.setTitle(blInfo.getContactInfo(getActivity()));
        dlgBuilder.setView(layout);
        dlgBuilder.setPositiveButton(getResources().getString(R.string.Remove), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                String str;
                boolean isChecked = checkBox.isChecked();
                BlacklistFragment.this.deleteBlacklist(nClickIndex, isChecked);
                String[] strArr = new String[2];
                strArr[0] = HsmStatConst.PARAM_OP;
                if (isChecked) {
                    str = "1";
                } else {
                    str = "0";
                }
                strArr[1] = str;
                HsmStat.statE(73, HsmStatConst.constructJsonParams(strArr));
            }
        });
        dlgBuilder.setNegativeButton(getResources().getString(R.string.harassmentInterception_cancel), null);
        dlgBuilder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void deleteBlacklist(int position, boolean bRestoreMsg) {
        if (isClickedInfoValid()) {
            BlacklistInfo blacklist = null;
            CommonItem item = this.mBlacklistAdapter.getItem(position);
            if (item instanceof BlackListItem) {
                blacklist = ((BlackListItem) item).getInfo();
            }
            if (blacklist != null) {
                boolean isSuccess;
                if (DBAdapter.deleteBlacklist(getActivity(), blacklist) > 0) {
                    isSuccess = true;
                } else {
                    isSuccess = false;
                }
                if (isSuccess) {
                    this.mBlacklist.remove(blacklist);
                    this.mBlacklistAdapter.setList(this.mBlacklist);
                }
                if (bRestoreMsg) {
                    new RecoverSmsTask().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new BlacklistInfo[]{blacklist});
                }
                refreshListView();
                return;
            }
            return;
        }
        HwLog.w(TAG, "deleteBlacklist: Data changed ,,stop operation, position = " + this.mClickPosition);
    }

    public void onResume() {
        getActivity().invalidateOptionsMenu();
        showWaitingDialog();
        refreshBlackList();
        super.onResume();
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.interception_blacklistfragment_menu, menu);
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
            case R.id.menu_add_header:
                addBlacklistHeader();
                break;
            case R.id.menu_add_from_contacts:
                addFromContacts();
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

    public void onDestroy() {
        unregisterDataObserver();
        dismissCreateNewDialog();
        hideWaitingDialog();
        DataShareManager.destory();
        HwLog.i(TAG, "onDestroy called, set mIsAbortLoadingTask false");
        this.mIsAbortLoadingTask.set(true);
        super.onDestroy();
    }

    private void dismissCreateNewDialog() {
        if (this.mCreateNewDialog != null) {
            this.mCreateNewDialog.dismiss();
            this.mCreateNewDialog = null;
        }
    }

    private int getFirstItemPosition(int position) {
        int type;
        int firstPosition = this.mBlacklistView.getFirstVisiblePosition();
        int modifiedFirstPosition = position > (firstPosition + this.mBlacklistView.getLastVisiblePosition()) / 2 ? firstPosition + 1 : firstPosition;
        CommonItem tempInfo = this.mBlacklistAdapter.getItem(modifiedFirstPosition);
        if (this.mBlacklistAdapter.getItem(0) instanceof BlackListItem) {
            type = 0;
        } else {
            CommonItem clickedInfo;
            if (tempInfo instanceof BlackListItem) {
                clickedInfo = tempInfo;
            } else {
                clickedInfo = this.mBlacklistAdapter.getItem(modifiedFirstPosition + 1);
            }
            type = (-((BlackListItem) clickedInfo).getInfo().getType()) + 2;
        }
        return modifiedFirstPosition - type;
    }

    public void refreshBlackList() {
        if (this.mIsFirstTimeInitData) {
            HwLog.i(TAG, "refreshBlackList FirstTimeInitData");
            this.mIsFirstTimeInitData = false;
            this.mDataObserver.resetDataChangeFlag();
            startLoadingThread();
            return;
        }
        if (!this.mDataObserver.isDataChanged()) {
            HwLog.i(TAG, "refreshBlackList: Data is not changed");
            if (isLoadingData()) {
                HwLog.i(TAG, "refreshBlackList: Loading data");
                postShowWaitingDlgMsg();
            }
            if (!this.mIsActivityLoadData) {
                HwLog.i(TAG, "refreshBlackList: refresh ListView");
                refreshListView();
            }
        } else if (mAppendingTaskCount.get() > 0) {
            HwLog.i(TAG, "refreshBlackList: Data is changed, may be caused by inner appending task ,skip");
            this.mDataObserver.resetDataChangeFlag();
        } else {
            HwLog.i(TAG, "refreshBlackList: Data is changed, reloading");
            this.mDataObserver.resetDataChangeFlag();
            startLoadingThread();
        }
        this.mIsActivityLoadData = false;
    }

    private void startLoadingThread() {
        synchronized (this) {
            if (this.mDataLoadingThread == null) {
                this.mDataLoadingThread = new DataLoadingThread("HarassIntercept_LoadBlackList");
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

    public void setTextViewMultiLines(TextView text1, TextView text2) {
        HwLog.d(TAG, "The text should be multi lines!");
        if (text1 != null) {
            text1.setSingleLine(false);
            text1.setMaxLines(2);
        }
        if (text2 != null) {
            text2.setSingleLine(false);
            text2.setMaxLines(2);
        }
    }

    private int recoverCallAndMessage(String phone, int type) {
        List messageList = DBAdapter.addInterceptedMsgToSystemInBoxByTypeAndFuzzyPhone(this.mContext, phone, type);
        if (messageList == null) {
            return -1;
        }
        if (messageList.size() > 0) {
            return DBAdapter.deleteInterceptedMsg(this.mContext, messageList);
        }
        return 0;
    }

    private void addFromContacts() {
        Intent contactIntent = new Intent("android.intent.action.PICK");
        contactIntent.setType("vnd.android.cursor.dir/phone_v2");
        contactIntent.putExtra("com.huawei.community.action.MULTIPLE_PICK", true);
        contactIntent.putExtra("Launch_BlackList_Multi_Pick", true);
        contactIntent.putExtra("com.huawei.community.action.EXPECT_INTEGER_LIST", true);
        try {
            contactIntent.setComponent(new ComponentName(HsmStatConst.CONTACTS_PACKAGE_NAME, "com.android.contacts.activities.ContactSelectionActivity"));
            startActivityForResult(contactIntent, 109);
        } catch (Exception e) {
            HwLog.e(TAG, "clickAddFromContacts: Exception", e);
            try {
                contactIntent.setComponent(null);
                startActivityForResult(contactIntent, 109);
            } catch (Exception e2) {
                HwLog.e(TAG, "clickAddFromContacts: Exception", e2);
            }
        }
    }

    private void addFromCalllog() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), CalllogListActivity.class);
        startActivityForResult(intent, 111);
    }

    private void addFromMessage() {
        Intent intent = new Intent();
        intent.setClass(getActivity(), MessageListActivity.class);
        startActivityForResult(intent, REQUEST_CODE_PICK_SMS);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (-1 != resultCode || data == null) {
            HwLog.w(TAG, "onActivityResult: Invalid result ， resultCode = " + resultCode);
            return;
        }
        this.mIsActivityLoadData = true;
        String from = "";
        switch (requestCode) {
            case 109:
                from = "2";
                onPickResultFromContacts(data);
                break;
            case REQUEST_CODE_PICK_SMS /*110*/:
                from = "4";
                onPickResultFromSms(data);
                break;
            case 111:
                from = "3";
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
            HsmStat.statE((int) Events.E_HARASSMENT_ADD_BLACKLIST, statParam);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void confirmWhiteList(Intent data, int option) {
        final ArrayList<ParcelableBlacklistItem> parcelableList = DataShareManager.getInstance().copyBlacklistBuff();
        data.putParcelableArrayListExtra(ContactsHelper.KEY_CONTACTS_ID_LIST, parcelableList);
        DataShareManager.getInstance().clearBlacklistBuff();
        if (!Utility.isNullOrEmptyList(parcelableList)) {
            final Intent intent = data;
            final int i = option;
            new Thread("HarassIntercept_confirmIfInWhiteList") {
                public void run() {
                    for (ParcelableBlacklistItem item : parcelableList) {
                        Activity activity = BlacklistFragment.this.getActivity();
                        if (activity != null) {
                            if (DBAdapter.isWhitelisted(activity, item.getPhone())) {
                                BlacklistFragment.this.postShowConfirmAddBlacklistDlg(intent);
                                return;
                            }
                        }
                        return;
                    }
                    BlacklistFragment.this.addToBlacklist(intent, i);
                }
            }.start();
        }
    }

    private void onPickResultFromContacts(final Intent data) {
        new Thread("HarassIntercept_InsertBlackListFromContacts") {
            public void run() {
                BlacklistFragment.mAppendingTaskCount.addAndGet(1);
                BlacklistFragment.this.postShowWaitingDlgMsg();
                List<SysContactsObject> contactList = ContactsHelper.getSelectedContacts(data, BlacklistFragment.this.mContext, BlacklistFragment.this.mIsAbortLoadingTask);
                if (Utility.isNullOrEmptyList(contactList)) {
                    BlacklistFragment.mAppendingTaskCount.decrementAndGet();
                    HwLog.d(BlacklistFragment.TAG, "onPickResultFromContacts: no contact is selected");
                    return;
                }
                HwLog.i(BlacklistFragment.TAG, "onPickResultFromContacts: Selected count = " + contactList.size());
                BlacklistFragment.this.insertContactsPickList(contactList, 3);
                BlacklistFragment.this.postHideWaitingDlgMsg();
                BlacklistFragment.mAppendingTaskCount.decrementAndGet();
            }
        }.start();
    }

    private void onPickResultFromSms(Intent data) {
        confirmWhiteList(data, 3);
    }

    private void onPickResultFromCallLog(Intent data) {
        confirmWhiteList(data, 3);
    }

    private void addToBlacklist(Intent data, final int option) {
        final ArrayList<ParcelableBlacklistItem> blacklist = data.getParcelableArrayListExtra(ContactsHelper.KEY_CONTACTS_ID_LIST);
        if (Utility.isNullOrEmptyList(blacklist)) {
            HwLog.d(TAG, "addToBlacklist: Select none");
            return;
        }
        HwLog.d(TAG, "addToBlacklist: Select count =  " + blacklist.size());
        new Thread("HarassIntercept_addToBlacklist") {
            public void run() {
                BlacklistFragment.mAppendingTaskCount.addAndGet(1);
                BlacklistFragment.this.addToBlacklist(blacklist, option);
                BlacklistFragment.mAppendingTaskCount.decrementAndGet();
            }
        }.start();
    }

    private void addToBlacklist(ArrayList<ParcelableBlacklistItem> blacklist, int option) {
        HwLog.d(TAG, "addToBlacklist: Starts, blacklist count = " + blacklist.size() + ", TaskCount = " + mAppendingTaskCount);
        postShowWaitingDlgMsg();
        List listCarrier = null;
        int nAddedCount = 0;
        boolean isSucessAdd = false;
        for (ParcelableBlacklistItem item : blacklist) {
            if (this.mIsAbortLoadingTask.get()) {
                HwLog.i(TAG, "addToBlacklist: Aborted by user");
                if (listCarrier != null) {
                    postBlacklistAppendMsg(transInfoToItems(listCarrier));
                    isSucessAdd = true;
                }
                postCompleteAddBlacklist(Boolean.valueOf(isSucessAdd));
                HwLog.d(TAG, "addToBlacklist: Ends. Added count = " + nAddedCount);
            }
            String number = item.getPhone();
            String name = item.getName();
            if (DBAdapter.deleteWhitelist(this.mContext, number) >= 0) {
                int[] result = DBAdapter.addBlacklistEx(this.mContext, number, name, option, 0);
                if (result[0] >= 0) {
                    String formatedPhone = DBAdapter.formatPhoneNumber(number);
                    if (!TextUtils.isEmpty(formatedPhone)) {
                        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(GlobalContext.getContext(), formatedPhone);
                        if (!TextUtils.isEmpty(hotlineNumber)) {
                            HwLog.i(TAG, "this is hotline number");
                            number = hotlineNumber;
                            name = HotlineNumberHelper.getHotlineNumberName(GlobalContext.getContext(), hotlineNumber);
                        }
                        BlacklistInfo blacklistInfo = new BlacklistInfo(result[0], number, name, result[1], result[2], option, 0);
                        switch (result[3]) {
                            case 0:
                                if (listCarrier == null) {
                                    listCarrier = new ArrayList();
                                }
                                listCarrier.add(blacklistInfo);
                                nAddedCount++;
                                break;
                            case 1:
                                postBlacklistUpdateMsg(createBlackListItem(blacklistInfo));
                                break;
                        }
                        if (nAddedCount > 0 && nAddedCount % 20 == 0) {
                            postBlacklistAppendMsg(transInfoToItems(listCarrier));
                            listCarrier = null;
                            nAddedCount = 0;
                            isSucessAdd = true;
                        }
                    }
                }
            }
        }
        if (listCarrier != null) {
            postBlacklistAppendMsg(transInfoToItems(listCarrier));
            isSucessAdd = true;
        }
        postCompleteAddBlacklist(Boolean.valueOf(isSucessAdd));
        HwLog.d(TAG, "addToBlacklist: Ends. Added count = " + nAddedCount);
    }

    private void insertContactsPickList(List<SysContactsObject> listContacts, int option) {
        insertContactsPickList(listContacts, false, option);
    }

    private void insertContactsPickList(List<SysContactsObject> listContacts, boolean isConfirmedRmFromWhiteList, int option) {
        if (Utility.isNullOrEmptyList(listContacts)) {
            HwLog.d(TAG, "insertContactsPickList: Empty contact list");
            return;
        }
        HwLog.d(TAG, "insertContactsPickList: Starts, count = " + listContacts.size() + ", isConfirmedRmFromWhiteList = " + isConfirmedRmFromWhiteList);
        List listCarrier = null;
        boolean isSucessAdd = false;
        int contactCount = listContacts.size();
        int nAddedCount = 0;
        int i = 0;
        while (!this.mIsAbortLoadingTask.get() && i < contactCount) {
            SysContactsObject contactsObj = (SysContactsObject) listContacts.get(i);
            String phoneNumber = contactsObj.getNumber();
            String contactName = contactsObj.getName();
            if (isConfirmedRmFromWhiteList) {
                DBAdapter.deleteWhitelist(this.mContext, phoneNumber);
            } else if (DBAdapter.isWhitelisted(this.mContext, phoneNumber)) {
                if (!Utility.isNullOrEmptyList(listCarrier)) {
                    postBlacklistAppendMsg(transInfoToItems(listCarrier));
                }
                postShowConfirmAddBlacklistDlg(new ArrayList(listContacts.subList(i, contactCount)));
                HwLog.d(TAG, "insertContactsPickList: Break. Need confirm removing from whitelist. i = " + i);
                return;
            }
            int[] result = DBAdapter.addBlacklistEx(this.mContext, phoneNumber, contactName, option, 0);
            if (result[0] >= 0) {
                String formatedPhone = DBAdapter.formatPhoneNumber(phoneNumber);
                if (!TextUtils.isEmpty(formatedPhone)) {
                    String hotlineNumber = HotlineNumberHelper.getHotlineNumber(GlobalContext.getContext(), formatedPhone);
                    if (!TextUtils.isEmpty(hotlineNumber)) {
                        HwLog.i(TAG, "this is hotline number");
                        phoneNumber = hotlineNumber;
                        contactName = HotlineNumberHelper.getHotlineNumberName(GlobalContext.getContext(), hotlineNumber);
                    }
                    BlacklistInfo blacklistInfo = new BlacklistInfo(result[0], phoneNumber, contactName, result[1], result[2], option, 0);
                    switch (result[3]) {
                        case 0:
                            if (listCarrier == null) {
                                listCarrier = new ArrayList();
                            }
                            listCarrier.add(blacklistInfo);
                            nAddedCount++;
                            break;
                        case 1:
                            postBlacklistUpdateMsg(createBlackListItem(blacklistInfo));
                            break;
                    }
                    if (nAddedCount > 0 && nAddedCount % 20 == 0) {
                        postBlacklistAppendMsg(transInfoToItems(listCarrier));
                        listCarrier = null;
                        isSucessAdd = true;
                    }
                }
            }
            i++;
        }
        if (!(this.mIsAbortLoadingTask.get() || Utility.isNullOrEmptyList(listCarrier))) {
            postBlacklistAppendMsg(transInfoToItems(listCarrier));
            isSucessAdd = true;
        }
        postCompleteAddBlacklist(Boolean.valueOf(isSucessAdd));
        HwLog.d(TAG, "insertContactsPickList: Ends. Added count = " + nAddedCount);
    }

    private List<BlackListItem> transInfoToItems(List<BlacklistInfo> listCarrier) {
        List<BlackListItem> items = Lists.newArrayList();
        if (listCarrier == null) {
            return items;
        }
        for (BlacklistInfo info : listCarrier) {
            if (info.getType() == 0) {
                items.add(new BlackDftItem(info));
            } else if (info.getType() == 1) {
                items.add(new BlackHeaderItem(info));
            }
        }
        return items;
    }

    private void addFromCreateNew() {
        showAddOrEditBlacklistDlg(null, false);
    }

    private void addBlacklistHeader() {
        showAddOrEditBlacklistDlg(null, true);
    }

    private void showAddOrEditBlacklistDlg(BlacklistInfo blInfo, boolean isBlacklistHeader) {
        int nLayoutId;
        TextView textView;
        LayoutInflater inflater = getLayoutInflater(null);
        final boolean isCreateNew = blInfo == null;
        if (!isCreateNew) {
            nLayoutId = R.layout.interception_create_blacklist_dialog;
        } else if (isBlacklistHeader) {
            nLayoutId = R.layout.interception_create_blacklistheader_dialog;
        } else {
            nLayoutId = R.layout.interception_create_blacklist_dialog;
        }
        View layout = inflater.inflate(nLayoutId, null);
        final TextView editTextPhone = (EditText) layout.findViewById(R.id.edittext_phone);
        final TextView textPhone = (TextView) layout.findViewById(R.id.textview_phone);
        Button btnCancel = (Button) layout.findViewById(R.id.btn_cancel);
        Button btnOK = (Button) layout.findViewById(R.id.btn_ok);
        final CheckBox cbBlockMsg = (CheckBox) layout.findViewById(R.id.checkbox_blockmsg);
        final CheckBox cbBlockCall = (CheckBox) layout.findViewById(R.id.checkbox_blockcall);
        Builder altDlg = new Builder(getActivity());
        altDlg.setView(layout);
        if (isCreateNew) {
            int titleId;
            if (isBlacklistHeader) {
                titleId = R.string.harassmentInterception_BlacklistHeader;
            } else {
                titleId = R.string.harassmentInterceptionMenuManuallyAdd;
            }
            altDlg.setTitle(getResources().getString(titleId));
            editTextPhone.addTextChangedListener(new TextWrapper(btnOK, cbBlockCall, cbBlockMsg));
        } else {
            altDlg.setTitle(getResources().getString(R.string.harassmentInterceptionMenuEdit));
            editTextPhone.setVisibility(8);
            textPhone.setVisibility(0);
            textPhone.setText(blInfo.getPhoneText());
            cbBlockMsg.setChecked(blInfo.isBlockMsg());
            cbBlockCall.setChecked(blInfo.isBlockCall());
            btnOK.setEnabled(true);
        }
        this.mCreateNewDialog = altDlg.show();
        editTextPhone.requestFocus();
        HarassmentUtil.requestInputMethod(this.mCreateNewDialog);
        if (isCreateNew) {
            textView = editTextPhone;
        } else {
            textView = textPhone;
        }
        CheckedChangeListener checkListener = new CheckedChangeListener(btnOK, cbBlockCall, cbBlockMsg, textView);
        cbBlockMsg.setOnCheckedChangeListener(checkListener);
        cbBlockCall.setOnCheckedChangeListener(checkListener);
        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                BlacklistFragment.this.dismissCreateNewDialog();
            }
        });
        final boolean z = isBlacklistHeader;
        btnOK.setOnClickListener(new OnClickListener() {
            public void onClick(View arg0) {
                String phone = "";
                if (isCreateNew) {
                    phone = editTextPhone.getText().toString();
                } else {
                    phone = textPhone.getText().toString();
                }
                if (!TextUtils.isEmpty(phone)) {
                    int i;
                    if (cbBlockMsg.isChecked()) {
                        i = 1;
                    } else {
                        i = 0;
                    }
                    int option = i | 0;
                    if (cbBlockCall.isChecked()) {
                        i = 2;
                    } else {
                        i = 0;
                    }
                    option |= i;
                    if (isCreateNew) {
                        BlacklistFragment blacklistFragment = BlacklistFragment.this;
                        String str = "";
                        if (z) {
                            i = 1;
                        } else {
                            i = 0;
                        }
                        blacklistFragment.createNewBlacklistItem(phone, str, option, i);
                        String from = z ? "1" : "0";
                        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, from);
                        HsmStat.statE((int) Events.E_HARASSMENT_ADD_BLACKLIST, statParam);
                    } else {
                        BlacklistFragment.this.updateBlacklistoption(option);
                    }
                    BlacklistFragment.this.dismissCreateNewDialog();
                }
            }
        });
    }

    private void createNewBlacklistItem(String phone, String name, int option, int type) {
        phone = DBAdapter.formatPhoneNumber(phone);
        BlacklistInfo tmpBlInfo = new BlacklistInfo(-1, phone, name, 0, 0, option, type);
        if (1 == type || !DBAdapter.isWhitelisted(getActivity(), phone)) {
            addBlacklist(tmpBlInfo, option, type);
        } else {
            postShowConfirmAddBlacklistDlg(tmpBlInfo);
        }
    }

    private int createNewBlacklistItem(ContactInfo info, int option, int type) {
        String phone = info.getPhone();
        String name = info.getName();
        if (1 != type && DBAdapter.deleteWhitelist(getActivity(), phone) < 0) {
            return -1;
        }
        int[] result = DBAdapter.addBlacklistEx(getActivity(), phone, name, option, type);
        if (result[0] < 0) {
            return -1;
        }
        this.mDataObserver.resetDataChangeFlag();
        String formatedPhone = DBAdapter.formatPhoneNumber(phone);
        if (TextUtils.isEmpty(formatedPhone)) {
            return -1;
        }
        String hotlineNumber = HotlineNumberHelper.getHotlineNumber(GlobalContext.getContext(), formatedPhone);
        if (!(1 == type || TextUtils.isEmpty(hotlineNumber))) {
            HwLog.i(TAG, "this is hotline number");
            phone = hotlineNumber;
            name = HotlineNumberHelper.getHotlineNumberName(GlobalContext.getContext(), hotlineNumber);
        }
        BlacklistInfo bl = new BlacklistInfo(result[0], phone, name, result[1], result[2], option, type);
        switch (result[3]) {
            case 0:
                this.mBlacklist.add(createBlackListItem(bl));
                sortAndUpdateBlacklistInBkg();
                break;
            case 1:
                updateUIBlacklist(bl);
                refreshListView();
                break;
            default:
                return -1;
        }
        showAddMessage(Boolean.valueOf(true));
        return result[0];
    }

    private void showWaitingDialog() {
        BlackWhiteListActivity ac = (BlackWhiteListActivity) getActivity();
        if (ac != null && this.mWaitingDialog == null && ac.getSelectedFragment() == 0) {
            this.mWaitingDialog = ProgressDialog.show(ac, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setCanceledOnTouchOutside(false);
            this.mWaitingDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    if (BlacklistFragment.this.isLoadingData()) {
                        Activity ac = BlacklistFragment.this.getActivity();
                        if (ac != null) {
                            HwLog.i(BlacklistFragment.TAG, "dialog canceled, but current is loading, just finish activity");
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

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isClickedInfoValid() {
        return (this.mClickPosition < 0 || this.mBlacklist == null || this.mClickPosition >= this.mBlacklist.size()) ? false : false;
    }

    private AlertDialog createAddBlackDlg(Object arg) {
        String message = getResources().getString(R.string.harassmentBlacklist_DlgMsg);
        String title = getResources().getString(R.string.harassmentBlacklist_DlgTitle);
        return createDlg(getActivity(), new String[]{message, title}, arg);
    }

    private AlertDialog createDlg(Context context, String[] uiDesc, final Object arg) {
        Builder alertDialog = new Builder(context);
        alertDialog.setTitle(uiDesc[1]);
        alertDialog.setMessage(uiDesc[0]);
        alertDialog.setNegativeButton(context.getResources().getString(R.string.harassment_cancle), null);
        alertDialog.setPositiveButton(context.getResources().getString(R.string.harassmentInterception_confirm), new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                BlacklistFragment.this.addBlacklist(arg, 3, 0);
            }
        });
        return alertDialog.show();
    }

    private void addBlacklist(final Object arg, final int option, int type) {
        if (arg.getClass() == Intent.class) {
            addToBlacklist((Intent) arg, option);
        } else if (arg.getClass() == ArrayList.class) {
            new Thread("HarassIntercept_addBlacklistOnConfirmed") {
                public void run() {
                    BlacklistFragment.mAppendingTaskCount.addAndGet(1);
                    BlacklistFragment.this.postShowWaitingDlgMsg();
                    BlacklistFragment.this.insertContactsPickList(arg, true, option);
                    BlacklistFragment.this.postHideWaitingDlgMsg();
                    BlacklistFragment.mAppendingTaskCount.decrementAndGet();
                }
            }.start();
        } else if (arg.getClass() == BlacklistInfo.class) {
            BlacklistInfo tmpBlInfo = (BlacklistInfo) arg;
            createNewBlacklistItem(tmpBlInfo, tmpBlInfo.getOption(), tmpBlInfo.getType());
        } else if (arg.getClass() == ContactInfo.class) {
            createNewBlacklistItem((ContactInfo) arg, option, type);
        }
    }

    private void dissmissOptionDlg() {
    }

    void updateUIBlacklist(BlacklistInfo blInfo) {
        if (!Utility.isNullOrEmptyList(this.mBlacklist)) {
            int count = this.mBlacklist.size();
            for (int nIndex = 0; nIndex < count; nIndex++) {
                CommonItem item = (CommonItem) this.mBlacklist.get(nIndex);
                if ((item instanceof BlackListItem) && ((BlackListItem) item).getInfo().isSameId(blInfo)) {
                    this.mBlacklist.set(nIndex, createBlackListItem(blInfo));
                    this.mBlacklistAdapter.setList(this.mBlacklist);
                    return;
                }
            }
        }
    }

    private BlackListItem createBlackListItem(BlacklistInfo bl) {
        if (bl.getType() == 0) {
            return new BlackDftItem(bl);
        }
        return new BlackHeaderItem(bl);
    }
}
