package com.huawei.harassmentinterception.ui;

import android.app.ActionBar;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
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
import com.huawei.harassmentinterception.blackwhitelist.RemoveWhiteListManager;
import com.huawei.harassmentinterception.blackwhitelist.ResultContext;
import com.huawei.harassmentinterception.blackwhitelist.WhiteListDataLoader;
import com.huawei.harassmentinterception.callback.ClickConfirmCallBack;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.callback.LoadDataCallBack;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.WhitelistInfo;
import com.huawei.harassmentinterception.common.ConstValues;
import com.huawei.harassmentinterception.util.CommonObjectHelper;
import com.huawei.harassmentinterception.util.dlg.DialogUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.comm.widget.ViewUtil;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class WhiteListEditActivity extends HsmActivity implements LoadDataCallBack {
    private static final int MSG_CONFIRM_WHITELIST_REMOVE = 3;
    private static final int MSG_WHITELIST_REFRESH = 1;
    private static final int MSG_WHITELIST_REMOVE = 2;
    private static final int MSG_WHITELIST_REMOVE_COMPLETE = 4;
    private static final String TAG = WhiteListEditActivity.class.getSimpleName();
    private String mCheckedNum = "";
    private int mCheckerPosition = 0;
    ClickConfirmCallBack mClickConfirmCallBack = new ClickConfirmCallBack() {
        public void onClickConfirmButton() {
            WhiteListEditActivity.this.removeWhiteList();
            HsmStat.statE(77);
        }
    };
    private WhiteListDataLoader mDataLoadingThread = null;
    private MenuItem mDelBtn;
    private boolean mFirstTimeCheck = true;
    private boolean mFirstTimeFresh = true;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    if (WhiteListEditActivity.this.mFirstTimeFresh) {
                        WhiteListEditActivity.this.mWhitelistView.setSelection(WhiteListEditActivity.this.mCheckerPosition);
                        WhiteListEditActivity.this.mFirstTimeFresh = !WhiteListEditActivity.this.mFirstTimeFresh;
                    }
                    WhiteListEditActivity.this.refreshWhiteList(msg.obj);
                    return;
                case 2:
                    WhiteListEditActivity.this.delWhiteListItems(msg.obj);
                    return;
                case 3:
                    WhiteListEditActivity.this.confirmContactExist(msg.obj);
                    return;
                case 4:
                    WhiteListEditActivity.this.afterDelWhiteList();
                    return;
                default:
                    return;
            }
        }
    };
    private boolean mIsAllChecked = false;
    private Menu mMenu;
    private View mNoWhitelistLayout;
    private TextView mOperatorView;
    private long mRemoveListId = 0;
    private RemoveWhiteListManager mRemoveListManager = null;
    private HandleListCallBack mRemovedListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> contacts) {
            WhiteListEditActivity.this.postMessage(3, new ResultContext(result, isExist, contacts));
        }

        public void onProcessHandleList(Object obj) {
            WhiteListEditActivity.this.postMessage(2, obj);
        }

        public void onCompleteHandleList(int result) {
            WhiteListEditActivity.this.postMessage(4, null);
        }
    };
    private MenuItem mSelAllBtn;
    private TextView mSelectedCountView;
    private ProgressDialog mWaitingDialog = null;
    private List<WhitelistInfo> mWhitelist = new ArrayList();
    private WhitelistAdapter mWhitelistAdapter;
    private ListView mWhitelistView;

    private static class ViewHolder {
        TextView _contactView;
        CheckBox _isSelectView;

        private ViewHolder() {
        }
    }

    class WhitelistAdapter extends BaseAdapter {
        WhitelistAdapter() {
        }

        public int getCount() {
            return WhiteListEditActivity.this.mWhitelist.size();
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
                convertView = WhiteListEditActivity.this.getLayoutInflater().inflate(R.layout.common_list_item_singleline_checkbox, null);
                holder = new ViewHolder();
                holder._contactView = (TextView) convertView.findViewById(ViewUtil.HWID_TEXT_1);
                holder._isSelectView = (CheckBox) convertView.findViewById(R.id.single_line_checkbox);
                holder._isSelectView.setVisibility(0);
                convertView.setTag(holder);
            } else {
                holder = (ViewHolder) convertView.getTag();
            }
            WhitelistInfo whitelistInfo = (WhitelistInfo) WhiteListEditActivity.this.mWhitelist.get(position);
            holder._contactView.setText(whitelistInfo.getContactInfo(WhiteListEditActivity.this));
            if (!TextUtils.isEmpty(WhiteListEditActivity.this.mCheckedNum) && WhiteListEditActivity.this.mFirstTimeCheck && whitelistInfo.getPhone().equals(WhiteListEditActivity.this.mCheckedNum)) {
                whitelistInfo.setSelected(true);
                WhiteListEditActivity.this.mFirstTimeCheck = false;
            }
            holder._isSelectView.setChecked(whitelistInfo.isSelected());
            return convertView;
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState == null) {
            Intent intent = getIntent();
            if (intent != null) {
                this.mCheckedNum = intent.getStringExtra(ConstValues.GET_CLICKED_ITEM_NUM);
                this.mCheckerPosition = intent.getIntExtra(ConstValues.GET_FIRST_ITEM_POSITION, 0);
            }
        }
        setContentView(R.layout.interception_fragment_whitelist);
        getActionBar().setDisplayHomeAsUpEnabled(false);
        initActionBar();
        initView();
        initListManager();
        showWaitingDialog();
        loadWhiteList();
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
                WhiteListEditActivity.this.finish();
            }
        });
        ActionBarEx.setEndIcon(actionBar, false, null, null);
    }

    private void initView() {
        this.mWhitelistView = (ListView) findViewById(R.id.whitelist_view);
        this.mWhitelistAdapter = new WhitelistAdapter();
        this.mWhitelistView.setAdapter(this.mWhitelistAdapter);
        this.mWhitelistView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                if (WhiteListEditActivity.this.mRemoveListManager.isRunning()) {
                    WhiteListEditActivity.this.showWaitingDialog();
                    return;
                }
                boolean z;
                boolean isChecked = CommonObjectHelper.getItemCheckState(WhiteListEditActivity.this.mWhitelist, position);
                WhiteListEditActivity whiteListEditActivity = WhiteListEditActivity.this;
                if (isChecked) {
                    z = false;
                } else {
                    z = true;
                }
                whiteListEditActivity.changeCheckBoxState(z, position);
            }
        });
    }

    public void loadWhiteList() {
        synchronized (this) {
            if (this.mDataLoadingThread == null) {
                this.mDataLoadingThread = new WhiteListDataLoader("HarassIntercept_WhiteListEditAc", this);
                this.mDataLoadingThread.start();
            }
        }
    }

    private void initListManager() {
        this.mRemoveListManager = new RemoveWhiteListManager();
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

    private void removeWhiteList() {
        if (!this.mRemoveListManager.isRunning()) {
            showWaitingDialog();
            this.mRemoveListManager.handleList(this, this.mRemoveListId, Boolean.valueOf(false));
        }
    }

    private void confirmContactExist(Object object) {
        DialogUtil.createRemoveWhitlistDlg(this, this.mClickConfirmCallBack);
    }

    private void refreshListView() {
        if (this.mWhitelistView == null) {
            HwLog.w(TAG, "refreshListView: activity is not initialized");
        } else if (this.mWhitelist == null) {
            HwLog.w(TAG, "refreshListView: null == mWhitelist");
        } else {
            if (this.mWhitelist.size() > 0) {
                this.mWhitelistView.setVisibility(0);
                updateNoDataView(8);
            } else {
                this.mWhitelistView.setVisibility(8);
                updateNoDataView(0);
            }
            hideWaitingDialog();
            this.mWhitelistAdapter.notifyDataSetChanged();
            updateCheckedNumber();
        }
    }

    private void updateNoDataView(int visibility) {
        if (visibility == 0) {
            if (this.mNoWhitelistLayout == null) {
                ViewStub stub = (ViewStub) findViewById(R.id.viewstub_no_whitelist);
                if (stub != null) {
                    stub.inflate();
                }
                this.mNoWhitelistLayout = findViewById(R.id.no_whitelist_view);
            }
            this.mNoWhitelistLayout.setVisibility(0);
        } else if (this.mNoWhitelistLayout != null) {
            this.mNoWhitelistLayout.setVisibility(8);
        }
    }

    private void changeCheckBoxState(boolean enable, int position) {
        if (!CommonObjectHelper.isOverLastData(this.mWhitelist, position)) {
            CommonObjectHelper.setItemCheckState(this.mWhitelist, position, enable);
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
                deleteWhiteList();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void refreshWhiteList(Object obj) {
        this.mDataLoadingThread = null;
        if (!(obj == null || obj.getClass() != ArrayList.class || this.mWhitelist == null)) {
            this.mWhitelist.clear();
            this.mWhitelist.addAll((ArrayList) obj);
            Collections.sort(this.mWhitelist, WhitelistInfo.WHITELIST_ALP_COMPARATOR);
        }
        refreshListView();
    }

    private void afterDelWhiteList() {
        Intent intent = new Intent();
        intent.putExtra(ConstValues.PARAM_DELETE_OPER, 1);
        setResult(-1, intent);
        finish();
    }

    private void updateCheckedNumber() {
        boolean z = true;
        if (this.mMenu != null) {
            int number = CommonObjectHelper.getCheckedItemNum(this.mWhitelist);
            this.mIsAllChecked = CommonObjectHelper.isAllItemChecked(this.mWhitelist, number);
            if (number > 0) {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Select);
                this.mSelectedCountView.setVisibility(0);
                this.mSelectedCountView.setText("" + Utility.getLocaleNumber(number));
                setTitle(getResources().getString(R.string.ActionBar_DoNotDisturb_Select) + Utility.getLocaleNumber(number));
                this.mDelBtn.setEnabled(true);
            } else {
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Unselect);
                this.mSelectedCountView.setVisibility(8);
                this.mOperatorView.setText(R.string.ActionBar_DoNotDisturb_Unselect);
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
            if (this.mWhitelist.size() == 0) {
                z = false;
            }
            menuItem.setEnabled(z);
        }
    }

    private void selectAllContacts() {
        this.mIsAllChecked = !this.mIsAllChecked;
        CommonObjectHelper.doSelect(this.mWhitelist, this.mIsAllChecked);
        refreshListView();
    }

    private void deleteWhiteList() {
        ArrayList<ContactInfo> phoneList = CommonObjectHelper.getCheckedItems(this.mWhitelist);
        if (!this.mRemoveListManager.isRunning()) {
            this.mRemoveListId = this.mRemoveListManager.confirmDelWhiteList(this, phoneList);
        }
    }

    private void delWhiteListItems(Object obj) {
        CommonObjectHelper.deleteItems(this.mWhitelist, obj);
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.harassmentInterception_wait), true, true);
            this.mWaitingDialog.setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface dialog) {
                    WhiteListEditActivity.this.afterDelWhiteList();
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
