package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.ViewStub;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.TextView;
import com.huawei.android.app.ActionBarEx;
import com.huawei.harassmentinterception.blackwhitelist.BlackListDataLoader;
import com.huawei.harassmentinterception.blackwhitelist.RemoveBlackListManager;
import com.huawei.harassmentinterception.blackwhitelist.ResultContext;
import com.huawei.harassmentinterception.callback.CheckRestoreSMSCallBack;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.callback.LoadDataCallBack;
import com.huawei.harassmentinterception.common.CommonObject.BlacklistInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.util.CommonObjectHelper;
import com.huawei.harassmentinterception.util.dlg.DialogUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class BlackListEditActivity extends HsmActivity implements LoadDataCallBack {
    private static final int MSG_BLACKLIST_REFRESH = 1;
    private static final int MSG_BLACKLIST_REMOVE = 2;
    private static final int MSG_BLACKLIST_REMOVE_COMPLETE = 4;
    private static final int MSG_CONFIRM_BLACKLIST_REMOVE = 3;
    private static final String TAG = BlackListEditActivity.class.getSimpleName();
    private List<BlacklistInfo> mBlacklist = new ArrayList();
    private BlacklistAdapter mBlacklistAdapter;
    private ListView mBlacklistView;
    private int mCheckedId = -1;
    private int mCheckerPosition = 0;
    private BlackListDataLoader mDataLoadingThread = null;
    private MenuItem mDelBtn;
    private boolean mFirstTimeCheck = true;
    private boolean mFirstTimeFresh = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (BlackListEditActivity.this.mFirstTimeFresh) {
                        BlackListEditActivity.this.mBlacklistView.setSelection(BlackListEditActivity.this.mCheckerPosition);
                        BlackListEditActivity.this.mFirstTimeFresh = !BlackListEditActivity.this.mFirstTimeFresh;
                    }
                    BlackListEditActivity.this.refreshBlackList(msg.obj);
                    return;
                case 2:
                    BlackListEditActivity.this.delBlackListItems(msg.obj);
                    return;
                case 3:
                    BlackListEditActivity.this.confirmContactExist(msg.obj);
                    return;
                case 4:
                    BlackListEditActivity.this.afterDelBlackList();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsAllChecked = false;
    private Menu mMenu;
    private View mNoBlacklistLayout;
    private TextView mOperatorView;
    private long mRemoveListId = 0;
    private RemoveBlackListManager mRemoveListManager = null;
    private HandleListCallBack mRemovedListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> list) {
            BlackListEditActivity.this.postMessage(3, new ResultContext(result, isExist));
        }

        public void onProcessHandleList(Object obj) {
            BlackListEditActivity.this.postMessage(2, obj);
        }

        public void onCompleteHandleList(int result) {
            BlackListEditActivity.this.postMessage(4, null);
        }
    };
    CheckRestoreSMSCallBack mRestoreSMSCallBack = new CheckRestoreSMSCallBack() {
        public void onCheckRestoreSMSButton(boolean isChecked) {
            BlackListEditActivity.this.removeBlackList(isChecked);
            String[] strArr = new String[2];
            strArr[0] = HsmStatConst.PARAM_OP;
            strArr[1] = isChecked ? "1" : "0";
            HsmStat.statE(73, HsmStatConst.constructJsonParams(strArr));
        }
    };
    private MenuItem mSelAllBtn;
    private TextView mSelectedCountView;
    private ProgressDialog mWaitingDialog = null;

    class BlacklistAdapter extends BaseAdapter {
        BlacklistAdapter() {
        }

        public int getCount() {
            return BlackListEditActivity.this.mBlacklist.size();
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
                convertView = BlackListEditActivity.this.getLayoutInflater().inflate(R.layout.common_list_item_twolines_checkbox, null);
                holder = new ViewHolder();
                holder._contactView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                holder._countView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_2);
                holder._isSelectView = (CheckBox) convertView.findViewById(R.id.two_lines_checkbox);
                holder._isSelectView.setVisibility(0);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            BlacklistInfo blacklistInfo = (BlacklistInfo) BlackListEditActivity.this.mBlacklist.get(position);
            holder._contactView.setText(blacklistInfo.getContactInfo(BlackListEditActivity.this));
            holder._countView.setText(String.format(BlackListEditActivity.this.getResources().getString(R.string.harassmentInterceptionRecordsCount), new Object[]{Integer.valueOf(blacklistInfo.getCallCount()), Integer.valueOf(blacklistInfo.getMsgCount())}));
            if (BlackListEditActivity.this.mFirstTimeCheck && blacklistInfo.getId() == BlackListEditActivity.this.mCheckedId) {
                blacklistInfo.setSelected(true);
                BlackListEditActivity.this.mFirstTimeCheck = false;
            }
            holder._isSelectView.setChecked(blacklistInfo.isSelected());
            return convertView;
        }
    }

    private static class ViewHolder {
        TextView _contactView;
        TextView _countView;
        CheckBox _isSelectView;

        private ViewHolder() {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                this.mCheckedId = intent.getIntExtra("id", -1);
                this.mCheckerPosition = intent.getIntExtra(ConstValues.GET_FIRST_ITEM_POSITION, 0);
            }
        }
        setContentView(R.layout.interception_fragment_blacklist);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        initActionBar();
        initView();
        initListManager();
        showWaitingDialog();
        loadBlackList();
    }

    protected void onResume() {
        super.onResume();
    }

    protected void onDestroy() {
        if (this.mRemoveListManager != null) {
            this.mRemoveListManager.unregisterCallBack();
            this.mRemoveListManager.stop();
            this.mRemoveListManager = null;
        }
        if (this.mDataLoadingThread != null) {
            this.mDataLoadingThread.interrupt();
            this.mDataLoadingThread = null;
        }
        super.onDestroy();
    }

    private void initActionBar() {
        View titleBarView = getLayoutInflater().inflate(R.layout.custom_actionbar_selecting, null);
        this.mOperatorView = (TextView) titleBarView.findViewById(R.id.view_title);
        this.mOperatorView.setText(getResources().getString(R.string.ActionBar_DoNotDisturb_Unselect));
        this.mSelectedCountView = (TextView) titleBarView.findViewById(R.id.view_selected_count);
        ActionBar actionBar = getActionBar();
        ActionBarEx.setCustomTitle(actionBar, titleBarView);
        ActionBarEx.setStartIcon(actionBar, true, null, new OnClickListener() {
            public void onClick(View v) {
                BlackListEditActivity.this.finish();
            }
        });
        ActionBarEx.setEndIcon(actionBar, false, null, null);
    }

    private void initView() {
        this.mBlacklistView = (ListView) findViewById(R.id.blacklist_view);
        this.mBlacklistAdapter = new BlacklistAdapter();
        this.mBlacklistView.setAdapter(this.mBlacklistAdapter);
        this.mBlacklistView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (BlackListEditActivity.this.mRemoveListManager.isRunning()) {
                    BlackListEditActivity.this.showWaitingDialog();
                    return;
                }
                boolean z;
                boolean isChecked = CommonObjectHelper.getItemCheckState(BlackListEditActivity.this.mBlacklist, position);
                BlackListEditActivity blackListEditActivity = BlackListEditActivity.this;
                if (isChecked) {
                    z = false;
                } else {
                    z = true;
                }
                blackListEditActivity.changeCheckBoxState(z, position);
            }
        });
    }

    public void loadBlackList() {
        synchronized (this) {
            if (this.mDataLoadingThread == null) {
                this.mDataLoadingThread = new BlackListDataLoader("HarassIntercept_BlackListEditAc", this);
                this.mDataLoadingThread.start();
            }
        }
    }

    private void initListManager() {
        this.mRemoveListManager = new RemoveBlackListManager();
        this.mRemoveListManager.registerCallBack(this.mRemovedListCallBack);
    }

    public void onCompletedDataLoad(Object object) {
        postMessage(1, object);
    }

    private void postMessage(int msg, Object obj) {
        Message message = this.mHandler.obtainMessage();
        message.what = msg;
        message.obj = obj;
        message.sendToTarget();
    }

    private void removeBlackList(boolean isChecked) {
        if (this.mRemoveListManager.isRunning()) {
            HwLog.d(TAG, "removeBlackList is running.");
            return;
        }
        showWaitingDialog();
        this.mRemoveListManager.handleList(this, this.mRemoveListId, Boolean.valueOf(isChecked));
    }

    private void confirmContactExist(Object object) {
        DialogUtil.createRemoveBlacklistDlg(this, this.mRestoreSMSCallBack);
    }

    private void refreshListView() {
        if (this.mBlacklistView == null) {
            HwLog.w(TAG, "refreshListView: activity is not initialized");
        } else if (this.mBlacklist == null) {
            HwLog.w(TAG, "refreshListView: null == mBlacklist");
        } else {
            if (this.mBlacklist.size() > 0) {
                this.mBlacklistView.setVisibility(0);
                updateNoDataView(8);
            } else {
                this.mBlacklistView.setVisibility(8);
                updateNoDataView(0);
            }
            hideWaitingDialog();
            this.mBlacklistAdapter.notifyDataSetChanged();
            updateCheckedNumber();
        }
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoBlacklistLayout == null) {
                ViewStub stub = (ViewStub) findViewById(R.id.viewstub_no_blacklist);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoBlacklistLayout = findViewById(R.id.no_blacklist_view);
            }
            this.mNoBlacklistLayout.setVisibility(0);
        } else if (this.mNoBlacklistLayout != null) {
            this.mNoBlacklistLayout.setVisibility(8);
        }
    }

    private void changeCheckBoxState(boolean enable, int position) {
        if (!CommonObjectHelper.isOverLastData(this.mBlacklist, position)) {
            CommonObjectHelper.setItemCheckState(this.mBlacklist, position, enable);
            refreshListView();
        }
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.preventmode_editcontacts_menu, menu);
        this.mMenu = menu;
        this.mDelBtn = menu.findItem(R.id.deleted_select_contacts);
        this.mSelAllBtn = menu.findItem(R.id.select_all_contacts);
        updateCheckedNumber();
        return super.onCreateOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case 16908332:
                finish();
                break;
            case R.id.select_all_contacts:
                selectAllContacts();
                break;
            case R.id.deleted_select_contacts:
                deleteBlackList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshBlackList(Object obj) {
        this.mDataLoadingThread = null;
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mBlacklist == null)) {
            this.mBlacklist.clear();
            this.mBlacklist.addAll((ArrayList) obj);
        }
        refreshListView();
    }

    private void afterDelBlackList() {
        Intent intent = new Intent();
        intent.putExtra(ConstValues.PARAM_DELETE_OPER, 1);
        setResult(-1, intent);
        finish();
    }

    private void updateCheckedNumber() {
        boolean z = true;
        if (this.mMenu != null) {
            int number = CommonObjectHelper.getCheckedItemNum(this.mBlacklist);
            this.mIsAllChecked = CommonObjectHelper.isAllItemChecked(this.mBlacklist, number);
            if (number > 0) {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Select);
                this.mSelectedCountView.setVisibility(0);
                this.mSelectedCountView.setText("" + Utility.getLocaleNumber(number));
                setTitle(getString(R.string.ActionBar_DoNotDisturb_Select) + Utility.getLocaleNumber(number));
                this.mDelBtn.setEnabled(true);
            } else {
                setTitle(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mSelectedCountView.setVisibility(8);
                this.mDelBtn.setEnabled(false);
            }
            if (number == 0 || !this.mIsAllChecked) {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_status);
                this.mSelAllBtn.setTitle(R.string.select_all);
                this.mSelAllBtn.setChecked(false);
            } else {
                this.mSelAllBtn.setIcon(R.drawable.menu_check_pressed);
                this.mSelAllBtn.setTitle(R.string.unselect_all);
                this.mSelAllBtn.setChecked(true);
            }
            MenuItem menuItem = this.mSelAllBtn;
            if (this.mBlacklist.size() == 0) {
                z = false;
            }
            menuItem.setEnabled(z);
        }
    }

    private void selectAllContacts() {
        this.mIsAllChecked = !this.mIsAllChecked;
        CommonObjectHelper.doSelect(this.mBlacklist, this.mIsAllChecked);
        refreshListView();
    }

    private void deleteBlackList() {
        if (this.mRemoveListManager.isRunning()) {
            HwLog.d(TAG, "deleteBlackList is running");
            return;
        }
        this.mRemoveListId = this.mRemoveListManager.confirmDelBlackList(this, CommonObjectHelper.getCheckedItems(this.mBlacklist));
    }

    private void delBlackListItems(Object obj) {
        CommonObjectHelper.deleteItems(this.mBlacklist, obj);
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    BlackListEditActivity.this.afterDelBlackList();
                }
            });
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
}
