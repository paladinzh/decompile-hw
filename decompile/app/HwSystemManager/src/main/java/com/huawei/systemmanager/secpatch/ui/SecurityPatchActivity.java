package com.huawei.systemmanager.secpatch.ui;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.DialogInterface.OnDismissListener;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.emui.activities.HsmActivity;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.secpatch.adapter.SecurityPatchListAdapter;
import com.huawei.systemmanager.secpatch.adapter.SingleVersionDetailListAdapter;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.secpatch.common.SecPatchCheckResult;
import com.huawei.systemmanager.secpatch.common.SecPatchItem;
import com.huawei.systemmanager.secpatch.common.SecurityPatchInfoBean;
import com.huawei.systemmanager.secpatch.db.DBAdapter;
import com.huawei.systemmanager.secpatch.net.SecPatchRequester;
import com.huawei.systemmanager.secpatch.util.SecPatchHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class SecurityPatchActivity extends HsmActivity {
    private static final String DO_NEWORK_THREAD = "do_network_thread";
    public static final String TAG = "SecurityPatchActivity";
    private static final int UPDATE_UI_WHEN_NETWORK_DB_CHANGE = 100;
    private static final int UPDATE_UI_WHEN_NETWORK_NO_CHANGE = 101;
    private Button mBtnUpdate;
    private Context mContext = null;
    private boolean mFirstTimeOnResume = true;
    private final Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case 100:
                    SecurityPatchActivity.this.hideWaitingDialog();
                    try {
                        if (msg.obj instanceof ArrayList) {
                            List<SecPatchItem> patchList = msg.obj;
                            if (SecurityPatchActivity.this.mUpdateSuccess.get()) {
                                SecurityPatchActivity.this.initInfoList(patchList);
                                SecurityPatchActivity.this.updateUI();
                                break;
                            }
                        }
                    } catch (ClassCastException e) {
                        e.printStackTrace();
                        break;
                    }
                    break;
                case 101:
                    SecurityPatchActivity.this.hideWaitingDialog();
                    break;
            }
            super.handleMessage(msg);
        }
    };
    private ListView mListView;
    private ListView mNeedUpdateListView;
    private RelativeLayout mRelativeLayoutNeedUpdate;
    private ScrollView mSecPatchDetail;
    private SecurityPatchListAdapter mSecPatchListAdapter;
    private SingleVersionDetailListAdapter mSecPatchNeedUpdateListAdapter;
    private TextView mSecPatchUpdate;
    private List<SecurityPatchInfoBean> mSecPatchUpdatedInfoList = new ArrayList();
    private LinearLayout mSecPatchUpdatedNodata;
    private List<SecPatchItem> mSecPatchUpdatingInfoList = new ArrayList();
    private TextView mSecPatchUpdatingNodata;
    private Object mThreadSync = new Object();
    private AtomicBoolean mUpdateSuccess = new AtomicBoolean(false);
    private Dialog mWaitingDialog;

    private class AsynctaskGetPatchsFromDB extends AsyncTask<Void, Void, List<SecPatchItem>> {
        private AsynctaskGetPatchsFromDB() {
        }

        protected List<SecPatchItem> doInBackground(Void... params) {
            return DBAdapter.getSecPatchList(SecurityPatchActivity.this.mContext);
        }

        protected void onPostExecute(List<SecPatchItem> result) {
            super.onPostExecute(result);
            SecurityPatchActivity.this.initInfoList(result);
            SecurityPatchActivity.this.updateUI();
            SecurityPatchActivity.this.getPatchInfoFromNetwork();
        }
    }

    private class DialogListener implements OnClickListener, OnDismissListener {
        private DialogListener() {
        }

        public void onClick(DialogInterface dialog, int which) {
            SecurityPatchActivity.this.startActivity(new Intent(ConstValues.INTENT_ACTION_SETTINGS));
        }

        public void onDismiss(DialogInterface dialog) {
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Utility.isOwnerUser()) {
            this.mContext = getApplicationContext();
            setContentView(R.layout.security_patch_main);
            SecPatchRequester.initUpdateStatus(false, false);
            initView();
            initData();
            setListener();
            refreshListFromDB();
            return;
        }
        finish();
    }

    protected void onResume() {
        super.onResume();
        if (this.mFirstTimeOnResume) {
            this.mFirstTimeOnResume = false;
        } else if (!this.mUpdateSuccess.get()) {
            getPatchInfoFromNetwork();
        }
    }

    private void initView() {
        this.mNeedUpdateListView = (ListView) findViewById(R.id.listview_patch_need_update);
        this.mListView = (ListView) findViewById(R.id.ListView_patch_updated);
        this.mBtnUpdate = (Button) findViewById(R.id.btn_patch_update);
        this.mSecPatchUpdatingNodata = (TextView) findViewById(R.id.patch_updating_no_data);
        this.mSecPatchUpdatedNodata = (LinearLayout) findViewById(R.id.security_patch_no_item);
        this.mSecPatchUpdate = (TextView) findViewById(R.id.txt_patch_update);
        this.mRelativeLayoutNeedUpdate = (RelativeLayout) findViewById(R.id.security_patch_need_update);
        this.mSecPatchDetail = (ScrollView) findViewById(R.id.security_patch_detail);
    }

    private void initData() {
        if (!SecPatchHelper.isNetworkAvaialble(this.mContext)) {
            showNetworkUnavailableDialog();
        }
        this.mRelativeLayoutNeedUpdate.setVisibility(8);
        this.mSecPatchUpdate.setVisibility(8);
        this.mSecPatchDetail.setVisibility(8);
    }

    private void refreshListFromDB() {
        showWaitingDialog();
        new AsynctaskGetPatchsFromDB().execute(new Void[0]);
    }

    private void initInfoList(List<SecPatchItem> secpatchlist) {
        if (Utility.isNullOrEmptyList(secpatchlist)) {
            this.mSecPatchUpdatingInfoList.clear();
            this.mSecPatchUpdatedInfoList.clear();
            return;
        }
        String secpatchPver = "";
        Collections.sort(secpatchlist, new SecPatchItem());
        SecurityPatchInfoBean secPatchInfo = new SecurityPatchInfoBean();
        List<SecurityPatchInfoBean> secPatchUpdatingList = new ArrayList();
        List<SecurityPatchInfoBean> secPatchUpdatedList = new ArrayList();
        List<String> udpateVersionNameList = DBAdapter.getNeedUpdateVersionList(this.mContext);
        int size = secpatchlist.size();
        for (int i = 0; i < size; i++) {
            String pver = ((SecPatchItem) secpatchlist.get(i)).mPver;
            if (TextUtils.isEmpty(secpatchPver)) {
                secpatchPver = pver;
                secPatchInfo.setSecPatchPver(pver);
                secPatchInfo.addSecPatchList((SecPatchItem) secpatchlist.get(i));
            } else if (secpatchPver.equals(pver)) {
                secPatchInfo.addSecPatchList((SecPatchItem) secpatchlist.get(i));
            } else {
                if (udpateVersionNameList.contains(secpatchPver)) {
                    secPatchUpdatingList.add(secPatchInfo);
                } else {
                    secPatchUpdatedList.add(secPatchInfo);
                }
                secpatchPver = pver;
                secPatchInfo = new SecurityPatchInfoBean();
                secPatchInfo.setSecPatchPver(pver);
                secPatchInfo.addSecPatchList((SecPatchItem) secpatchlist.get(i));
            }
        }
        if (udpateVersionNameList.contains(secpatchPver)) {
            secPatchUpdatingList.add(secPatchInfo);
        } else {
            secPatchUpdatedList.add(secPatchInfo);
        }
        List<SecPatchItem> secPatches = new ArrayList();
        for (SecurityPatchInfoBean patchBean : secPatchUpdatingList) {
            if (!patchBean.getSecPatchList().isEmpty()) {
                secPatches.addAll(patchBean.getSecPatchList());
            }
        }
        this.mSecPatchUpdatingInfoList = secPatches;
        this.mSecPatchUpdatedInfoList = secPatchUpdatedList;
    }

    private void initListAdapter() {
        if (this.mSecPatchNeedUpdateListAdapter == null) {
            this.mSecPatchNeedUpdateListAdapter = new SingleVersionDetailListAdapter(this.mContext, this.mSecPatchUpdatingInfoList);
            this.mNeedUpdateListView.setAdapter(this.mSecPatchNeedUpdateListAdapter);
        } else {
            this.mSecPatchNeedUpdateListAdapter.setSingleVersionDetailInfo(this.mSecPatchUpdatingInfoList);
        }
        if (this.mSecPatchListAdapter == null) {
            this.mSecPatchListAdapter = new SecurityPatchListAdapter(this.mContext, this.mSecPatchUpdatedInfoList);
            this.mListView.setAdapter(this.mSecPatchListAdapter);
            return;
        }
        this.mSecPatchListAdapter.setSecPatchInfo(this.mSecPatchUpdatedInfoList);
    }

    private void isHavingData() {
        if (Utility.isNullOrEmptyList(this.mSecPatchUpdatingInfoList)) {
            this.mRelativeLayoutNeedUpdate.setVisibility(8);
            this.mSecPatchUpdatingNodata.setVisibility(8);
            this.mNeedUpdateListView.setVisibility(8);
            this.mSecPatchUpdate.setVisibility(0);
            this.mBtnUpdate.setVisibility(8);
            if (SecPatchHelper.isNetworkAvaialble(this.mContext) && this.mUpdateSuccess.get()) {
                this.mSecPatchUpdate.setText(this.mContext.getString(R.string.Security_Patch_Newest_Tip));
            } else {
                this.mSecPatchUpdate.setText(this.mContext.getString(R.string.Security_Patch_No_Patches_Tip));
            }
        } else {
            this.mRelativeLayoutNeedUpdate.setVisibility(0);
            this.mSecPatchUpdatingNodata.setVisibility(8);
            this.mNeedUpdateListView.setVisibility(0);
            this.mSecPatchUpdate.setVisibility(0);
            this.mBtnUpdate.setVisibility(0);
            this.mSecPatchUpdate.setText(this.mContext.getString(R.string.Security_Patch_Risk_Alarm_Tip));
        }
        if (Utility.isNullOrEmptyList(this.mSecPatchUpdatedInfoList)) {
            this.mSecPatchUpdatedNodata.setVisibility(0);
            this.mListView.setVisibility(8);
            return;
        }
        this.mSecPatchUpdatedNodata.setVisibility(8);
        this.mListView.setVisibility(0);
    }

    private void showNetworkUnavailableDialog() {
        new Builder(this).setTitle(R.string.Security_Patch_Dailog_Title).setMessage(R.string.Security_Patch_Network_Error).setNegativeButton(R.string.Security_Patch_Cancel, null).setPositiveButton(R.string.Security_Patch_Settings, new DialogListener()).create().show();
    }

    private void setListener() {
        this.mNeedUpdateListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                SecPatchItem detailItem = (SecPatchItem) SecurityPatchActivity.this.mSecPatchUpdatingInfoList.get(position);
                Intent intent = new Intent(SecurityPatchActivity.this, SecurityPatchDetailActivity.class);
                intent.putExtra(ConstValues.INTENT_DETAIL_SID, detailItem.mSid);
                intent.putExtra(ConstValues.INTENT_DETAIL_OCID, detailItem.mOcid);
                intent.putExtra(ConstValues.INTENT_DETAIL_SRC, detailItem.mSrc);
                intent.putExtra(ConstValues.INTENT_DETAIL_CHN, detailItem.mDigest);
                intent.putExtra(ConstValues.INTENT_DETAIL_ENG, detailItem.mDigest_en);
                SecurityPatchActivity.this.startActivity(intent);
            }
        });
        this.mListView.setOnItemClickListener(new OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View arg1, int position, long arg3) {
                Intent intent = new Intent(SecurityPatchActivity.this, SingleVersionPatchDetailActivity.class);
                intent.putExtra(ConstValues.INTENT_SINGLE_VERSION, ((SecurityPatchInfoBean) SecurityPatchActivity.this.mSecPatchUpdatedInfoList.get(position)).getSecPatchPver());
                SecurityPatchActivity.this.startActivity(intent);
            }
        });
        this.mBtnUpdate.setOnClickListener(new View.OnClickListener() {
            public void onClick(View arg0) {
                HsmStat.statE(Events.E_SECURITY_PATCH_UPDATE);
                SecurityPatchActivity.this.startActivity(new Intent(ConstValues.INTENT_ACTION_UPDATE_SYSTEM));
            }
        });
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.search_menu, menu);
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_search:
                HsmStat.statE(Events.E_SECURITY_PATCH_SEARCH);
                startActivity(new Intent(this, SecurityPatchSearchActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(this, "", getResources().getString(R.string.Security_Patch_Wait_Load), true, true);
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

    private void updateUI() {
        isHavingData();
        initListAdapter();
        this.mSecPatchDetail.setVisibility(0);
    }

    private void getPatchInfoFromNetwork() {
        new Thread(DO_NEWORK_THREAD) {
            public void run() {
                try {
                    synchronized (SecurityPatchActivity.this.mThreadSync) {
                        if (SecPatchHelper.isNetworkAvaialble(SecurityPatchActivity.this.mContext)) {
                            SecPatchCheckResult checkResult = SecPatchRequester.queryCheckVersion(SecurityPatchActivity.this.mContext);
                            if (!SecPatchHelper.isRomVersionChange(SecurityPatchActivity.this.mContext)) {
                                HwLog.d(SecurityPatchActivity.TAG, "Rom version not change!");
                                if (!checkResult.getResponseCodeValidStatus()) {
                                    HwLog.e(SecurityPatchActivity.TAG, "checkResult is invalid");
                                    SecurityPatchActivity.this.sendNetworkNoChangeMessage();
                                    return;
                                }
                            }
                            checkResult.printVersionInfoToLog(SecurityPatchActivity.TAG);
                            SecurityPatchActivity.this.mUpdateSuccess.set(false);
                            boolean getAllStatus = SecPatchRequester.queryAllPatch(SecurityPatchActivity.this.mContext, checkResult.getCheckAllVersion());
                            boolean getUpdateStatus = SecPatchRequester.queryUpdatePatch(SecurityPatchActivity.this.mContext, checkResult.getCheckAvaVersion());
                            if (getAllStatus || getUpdateStatus) {
                                SecurityPatchActivity.this.mUpdateSuccess.set(true);
                                List<SecPatchItem> patchList = DBAdapter.getSecPatchList(SecurityPatchActivity.this.mContext);
                                Message msg = new Message();
                                msg.what = 100;
                                msg.obj = patchList;
                                SecurityPatchActivity.this.mHandler.sendMessage(msg);
                            } else {
                                SecurityPatchActivity.this.sendNetworkNoChangeMessage();
                            }
                        } else {
                            SecurityPatchActivity.this.sendNetworkNoChangeMessage();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }.start();
    }

    private void sendNetworkNoChangeMessage() {
        Message msg = new Message();
        msg.what = 101;
        this.mHandler.sendMessage(msg);
    }
}
