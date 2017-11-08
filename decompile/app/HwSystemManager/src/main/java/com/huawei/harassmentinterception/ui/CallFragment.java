package com.huawei.harassmentinterception.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.harassmentinterception.blackwhitelist.AddWhiteListManager;
import com.huawei.harassmentinterception.blackwhitelist.ResultContext;
import com.huawei.harassmentinterception.callback.CheckRestoreSMSCallBack;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.common.CommonObject.CallInfo;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.BlockReasonDescrp;
import com.huawei.harassmentinterception.util.BlockReasonDescrp.CallBlockReasonStr;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.dlg.DialogUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class CallFragment extends Fragment {
    private static final int MSG_ADD_WHITELIST = 2;
    private static final int MSG_CHECK_ADD_WHITELIST = 1;
    private static final String TAG = "CallFragment";
    private HandleListCallBack mAddListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> list) {
            ResultContext context = new ResultContext(result, isExist);
            Message message = CallFragment.this.mHandler.obtainMessage();
            message.what = 1;
            message.obj = context;
            message.sendToTarget();
        }

        public void onProcessHandleList(Object obj) {
            Message message = CallFragment.this.mHandler.obtainMessage();
            message.what = 2;
            message.obj = obj;
            message.sendToTarget();
        }

        public void onCompleteHandleList(int result) {
        }
    };
    private long mAddListId = 0;
    private AddWhiteListManager mAddListManager = null;
    private List<CallInfo> mCallList = new ArrayList();
    private CallListAdapter mCallListAdapter = null;
    private RelativeLayout mCallListLayout = null;
    private int mClickPosition = 0;
    private CallInfo mClickedInfo = null;
    private AlertDialog mCreateContactDlg = null;
    private DataLoadingTask mDataLoadingTask = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    HwLog.d(CallFragment.TAG, "SEND MSG_CHECK_ADD_WHITELIST MESSAGE");
                    CallFragment.this.showConfirmAddWhiteDlg(msg.obj);
                    return;
                case 2:
                    CallFragment.this.delMessageByPhone(msg.obj);
                    CallFragment.this.dissmissOptionDlg();
                    CallFragment.this.dissmissCreateContactDlg();
                    return;
                default:
                    return;
            }
        }
    };
    private MenuItem mMenuRemoveAll = null;
    private View mNoCallLayout = null;
    private AlertDialog mOptionDlg = null;
    CheckRestoreSMSCallBack mRestoreSMSCallBack = new CheckRestoreSMSCallBack() {
        public void onCheckRestoreSMSButton(boolean isChecked) {
            CallFragment.this.addSingleWhiteList(isChecked);
        }
    };
    private Boolean mShowDualCardIcon;
    private long mTodayStartTime = TimeUtil.getTodayStartTime();
    private ProgressDialog mWaitingDialog = null;

    class CallListAdapter extends BaseAdapter {
        CallListAdapter() {
        }

        public int getCount() {
            return CallFragment.this.mCallList.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            if (convertView == null) {
                convertView = CallFragment.this.newView(position, viewgroup);
            }
            CallFragment.this.fillViewHolderWithData((ViewHolder) convertView.getTag(), (CallInfo) CallFragment.this.mCallList.get(position), position);
            return convertView;
        }

        public void notifyDataSetChanged() {
            CallFragment.this.mTodayStartTime = TimeUtil.getTodayStartTime();
            super.notifyDataSetChanged();
        }
    }

    private class DataLoadingTask extends AsyncTask<Void, Void, List<CallInfo>> {
        private DataLoadingTask() {
        }

        protected List<CallInfo> doInBackground(Void... arg0) {
            HwLog.d(CallFragment.TAG, "DataLoadingTask-doInBackground: Starts");
            List<CallInfo> callList = Collections.emptyList();
            try {
                callList = DBAdapter.getInterceptedCalls(GlobalContext.getContext());
            } catch (Exception e) {
                HwLog.e(CallFragment.TAG, "DataLoadingTask-doInBackground: Exception", e);
            }
            return callList;
        }

        protected void onCancelled(List<CallInfo> result) {
            CallFragment.this.mDataLoadingTask = null;
            HwLog.i(CallFragment.TAG, "DataLoadingTask-onCancelled: Canceled");
            super.onCancelled(result);
        }

        protected void onPostExecute(List<CallInfo> callList) {
            CallFragment.this.initShowDualcardIcon();
            HwLog.d(CallFragment.TAG, "DataLoadingTask-onPostExecute: callList.size = " + callList.size());
            CallFragment.this.mDataLoadingTask = null;
            if (isCancelled()) {
                HwLog.i(CallFragment.TAG, "DataLoadingTask-onPostExecute: Canceled");
            } else {
                CallFragment.this.refreshListView(callList);
            }
        }
    }

    class OptionDlgListener implements OnClickListener, OnCancelListener {
        OptionDlgListener() {
        }

        public void onClick(View view) {
            String statOp = "";
            switch (view.getId()) {
                case R.id.btn_create_new:
                    CallFragment.this.addToNewContact();
                    statOp = "2";
                    break;
                case R.id.btn_save_exist:
                    CallFragment.this.addToExistContact();
                    statOp = "2";
                    break;
                case R.id.btn_delete:
                    CallFragment.this.confirmDeleteCall();
                    statOp = "3";
                    break;
                case R.id.btn_call:
                    CallFragment.this.call();
                    break;
                case R.id.btn_add_whitelist:
                    CallFragment.this.addWhiteList();
                    dissmissOptionDlgWhenUnkownNumber();
                    statOp = "1";
                    break;
                case R.id.btn_add_contact:
                    CallFragment.this.confirmAddContact();
                    break;
            }
            if (view.getId() != R.id.btn_add_whitelist) {
                CallFragment.this.dissmissOptionDlg();
            }
            if (view.getId() != R.id.btn_add_contact) {
                CallFragment.this.dissmissCreateContactDlg();
            }
            if (!TextUtils.isEmpty(statOp)) {
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, statOp);
                HsmStat.statE(67, statParam);
            }
        }

        private void dissmissOptionDlgWhenUnkownNumber() {
            if (TextUtils.isEmpty(((CallInfo) CallFragment.this.mCallList.get(CallFragment.this.mClickPosition)).getPhone())) {
                HwLog.i(CallFragment.TAG, "dissmissOptionDlgWhenUnkownNumber");
                CallFragment.this.dissmissOptionDlg();
            }
        }

        public void onCancel(DialogInterface arg0) {
            CallFragment.this.dissmissOptionDlg();
            CallFragment.this.dissmissCreateContactDlg();
        }
    }

    private static class ViewHolder {
        TextView _contactInfo;
        TextView _location;
        TextView _mark;
        int _position;
        TextView _reason;
        ImageView _subId;
        TextView _time;

        private ViewHolder() {
        }
    }

    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
        initListManager();
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.interception_fragment_call, container, false);
        initFragmentView(fragmentView);
        return fragmentView;
    }

    private void initListManager() {
        this.mAddListManager = new AddWhiteListManager();
        this.mAddListManager.registerCallBack(this.mAddListCallBack);
    }

    private void addSingleWhiteList(boolean isChecked) {
        if (!this.mAddListManager.isRunning()) {
            showWaitingDialog();
            this.mAddListManager.handleList(getActivity(), this.mAddListId, Boolean.valueOf(isChecked));
        }
    }

    private void showWaitingDialog() {
        if (this.mWaitingDialog == null) {
            this.mWaitingDialog = ProgressDialog.show(getActivity(), "", getResources().getString(R.string.harassmentInterception_wait), true, true);
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

    private void showConfirmAddWhiteDlg(Object object) {
        if (object.getClass() != ResultContext.class) {
            HwLog.d(TAG, "object.getClass() != ResultContext.class");
            return;
        }
        boolean isExist = ((ResultContext) object).isExist();
        HwLog.d(TAG, "showConfirmAddWhiteDlg" + isExist);
        if (isExist) {
            DialogUtil.createAddSingleWhiteListDlgFromMSG(getActivity(), ((CallInfo) this.mCallList.get(this.mClickPosition)).getContactInfo(getActivity()), this.mRestoreSMSCallBack);
        } else {
            addSingleWhiteList(false);
        }
    }

    private void delMessageByPhone(Object object) {
        if (this.mAddListManager.isRestoreMSG()) {
            String phone = ((ContactInfo) ((List) object).get(0)).getMatchedNumber();
            InterceptionActivity activity = (InterceptionActivity) getActivity();
            if (activity != null) {
                activity.delMessageByPhone(phone);
            }
        }
        hideWaitingDialog();
    }

    private void call() {
        String phonenumber = ((CallInfo) this.mCallList.get(this.mClickPosition)).getPhone();
        if (!TextUtils.isEmpty(phonenumber)) {
            startActivity(new Intent("android.intent.action.DIAL", Uri.parse("tel:" + phonenumber)));
        }
    }

    private void addWhiteList() {
        if (!this.mAddListManager.isRunning()) {
            CallInfo callInfo = (CallInfo) this.mCallList.get(this.mClickPosition);
            if (!TextUtils.isEmpty(callInfo.getPhone())) {
                ArrayList<ContactInfo> phoneList = new ArrayList();
                phoneList.add(callInfo);
                this.mAddListId = this.mAddListManager.checkListExist(GlobalContext.getContext(), phoneList, null);
            }
        }
    }

    private void initFragmentView(View fragmentView) {
        this.mNoCallLayout = fragmentView.findViewById(R.id.no_call_view);
        initEmptyView(GlobalContext.getContext(), this.mNoCallLayout);
        this.mCallListLayout = (RelativeLayout) fragmentView.findViewById(R.id.call_list_layout);
        ListView callListView = (ListView) fragmentView.findViewById(R.id.call_list_view);
        this.mCallListAdapter = new CallListAdapter();
        callListView.setAdapter(this.mCallListAdapter);
    }

    private void initEmptyView(Context ctx, View emptyView) {
        if (emptyView != null) {
            LayoutParams params = (LayoutParams) emptyView.getLayoutParams();
            int screenHeight = Utility.getScreenLongHeight(ctx);
            int statusBarHeight = ctx.getResources().getDimensionPixelSize(R.dimen.statusbar_height);
            int actionBarHeight = ctx.getResources().getDimensionPixelSize(R.dimen.actionbar_height);
            int marginTop = ((((screenHeight * 3) / 10) - statusBarHeight) - actionBarHeight) - ctx.getResources().getDimensionPixelSize(R.dimen.widgetTab_height);
            if (params.topMargin == 0) {
                params.topMargin = marginTop;
            }
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.interception_callfragment_menu, menu);
        this.mMenuRemoveAll = menu.findItem(R.id.menu_remove_all);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove_all:
                confirmRemoveAllCalls();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mMenuRemoveAll = menu.findItem(R.id.menu_remove_all);
        if (this.mMenuRemoveAll != null) {
            if (Utility.isNullOrEmptyList(this.mCallList)) {
                this.mMenuRemoveAll.setVisible(false);
                this.mMenuRemoveAll.setEnabled(false);
            } else {
                this.mMenuRemoveAll.setVisible(true);
                this.mMenuRemoveAll.setEnabled(true);
            }
        }
        super.onPrepareOptionsMenu(menu);
    }

    public void onResume() {
        refreshCallList();
        super.onResume();
    }

    public void refreshCallList() {
        if (this.mDataLoadingTask == null) {
            this.mDataLoadingTask = new DataLoadingTask();
            this.mDataLoadingTask.execute(new Void[0]);
        }
    }

    public void onDestroy() {
        if (this.mDataLoadingTask != null) {
            this.mDataLoadingTask.cancel(false);
            this.mDataLoadingTask = null;
        }
        if (this.mAddListManager != null) {
            this.mAddListManager.unregisterCallBack();
            this.mAddListManager.stop();
            this.mAddListManager = null;
        }
        dissmissOptionDlg();
        dissmissCreateContactDlg();
        super.onDestroy();
    }

    private void dissmissOptionDlg() {
        if (this.mOptionDlg != null) {
            this.mOptionDlg.dismiss();
            this.mOptionDlg = null;
        }
    }

    private void dissmissCreateContactDlg() {
        if (this.mCreateContactDlg != null) {
            this.mCreateContactDlg.dismiss();
            this.mCreateContactDlg = null;
        }
    }

    private View newView(int position, ViewGroup viewgroup) {
        View interView = getLayoutInflater(null).inflate(R.layout.interception_intercept_call_list_item, viewgroup, false);
        ViewHolder holder = new ViewHolder();
        holder._contactInfo = (TextView) interView.findViewById(R.id.call_contactInfo);
        holder._subId = (ImageView) interView.findViewById(R.id.sub_id);
        holder._location = (TextView) interView.findViewById(R.id.call_location);
        holder._time = (TextView) interView.findViewById(R.id.time);
        holder._reason = (TextView) interView.findViewById(R.id.blockreason);
        holder._mark = (TextView) interView.findViewById(R.id.mark_number);
        interView.setClickable(true);
        interView.setTag(holder);
        interView.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                CallFragment.this.onClickCallInfo(((ViewHolder) view.getTag())._position);
            }
        });
        return interView;
    }

    private void fillViewHolderWithData(ViewHolder holder, CallInfo callInfo, int position) {
        holder._contactInfo.setText(callInfo.getContactInfo(getActivity()));
        if (this.mShowDualCardIcon == null || !this.mShowDualCardIcon.booleanValue()) {
            holder._subId.setVisibility(8);
        } else if (callInfo.getSubId() == 1) {
            holder._subId.setVisibility(0);
            holder._subId.setImageResource(R.drawable.ic_phone_card1);
        } else if (callInfo.getSubId() == 2) {
            holder._subId.setVisibility(0);
            holder._subId.setImageResource(R.drawable.ic_phone_card2);
        } else {
            holder._subId.setVisibility(8);
        }
        String location = callInfo.getGeoLocation();
        if (TextUtils.isEmpty(location)) {
            holder._location.setText("");
            holder._location.setVisibility(8);
        } else {
            holder._location.setText(location);
            holder._location.setVisibility(0);
        }
        holder._position = position;
        Context ctx = GlobalContext.getContext();
        CallBlockReasonStr blockStr = BlockReasonDescrp.getCallBlockreasonStr(ctx, callInfo.getReason());
        if (TextUtils.isEmpty(blockStr.getReasonStr())) {
            holder._reason.setVisibility(8);
            holder._mark.setVisibility(8);
        } else {
            holder._reason.setVisibility(0);
            holder._reason.setText(blockStr.getReasonStr());
            holder._mark.setVisibility(0);
            holder._mark.setText(blockStr.getMarkStr());
        }
        String timeDes = "";
        long recordTime = callInfo.getDate();
        if (recordTime >= this.mTodayStartTime) {
            timeDes = DateUtils.formatDateTime(ctx, recordTime, 1);
        } else {
            timeDes = DateUtils.formatDateTime(ctx, recordTime, 16);
        }
        holder._time.setText(timeDes);
    }

    private void onClickCallInfo(int nPosition) {
        if (Utility.isNullOrEmptyList(this.mCallList) || nPosition >= this.mCallList.size()) {
            HwLog.w(TAG, "onClickMsgInfo: Invalid status or click position, " + nPosition);
        } else if (this.mOptionDlg == null || !this.mOptionDlg.isShowing()) {
            View layout = getLayoutInflater(null).inflate(R.layout.interception_call_option_dlg, null);
            this.mOptionDlg = new Builder(getActivity()).create();
            this.mOptionDlg.setView(layout);
            this.mClickPosition = nPosition;
            this.mClickedInfo = (CallInfo) this.mCallList.get(nPosition);
            initOptionDlgData(this.mOptionDlg, layout, nPosition);
            this.mOptionDlg.show();
            HsmStat.statE(66);
        } else {
            HwLog.w(TAG, "onClickCallInfo: Previous dlg is not dissmissed");
        }
    }

    private void initOptionDlgData(AlertDialog optionDlg, View layout, int nPosition) {
        CallInfo callInfo = (CallInfo) this.mCallList.get(nPosition);
        optionDlg.setTitle(callInfo.getContactInfo(getActivity()) + " " + callInfo.getGeoLocation());
        OptionDlgListener clickListener = new OptionDlgListener();
        Button btnCall = (Button) layout.findViewById(R.id.btn_call);
        if (TextUtils.isEmpty(callInfo.getPhone())) {
            btnCall.setVisibility(8);
        } else {
            btnCall.setOnClickListener(clickListener);
        }
        Button btnAddWhitelist = (Button) layout.findViewById(R.id.btn_add_whitelist);
        if (DBAdapter.isWhitelisted(getActivity(), callInfo.getPhone()) || TextUtils.isEmpty(callInfo.getPhone())) {
            btnAddWhitelist.setVisibility(8);
        } else {
            btnAddWhitelist.setOnClickListener(clickListener);
        }
        boolean isContact = DBAdapter.isContact(getActivity(), callInfo.getPhone());
        HwLog.d(TAG, "initMsgDetailData: isContact = " + isContact);
        Button btnAddContact = (Button) layout.findViewById(R.id.btn_add_contact);
        if (isContact || TextUtils.isEmpty(callInfo.getPhone())) {
            btnAddContact.setVisibility(8);
        } else {
            btnAddContact.setOnClickListener(clickListener);
        }
        ((Button) layout.findViewById(R.id.btn_delete)).setOnClickListener(clickListener);
        optionDlg.setCanceledOnTouchOutside(false);
        optionDlg.setOnCancelListener(clickListener);
    }

    private void confirmDeleteCall() {
        Builder dlgBuilder = new Builder(getActivity());
        dlgBuilder.setTitle(R.string.harassmentInterceptionCallDeleteOrNot_new);
        dlgBuilder.setNegativeButton(R.string.cancel, null);
        dlgBuilder.setPositiveButton(R.string.harassmentInterceptionMenuDelete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                CallFragment.this.deleteCall();
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "3");
                HsmStat.statE(67, statParam);
            }
        });
        dlgBuilder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void deleteCall() {
        if (isClickedInfoValid()) {
            DBAdapter.deleteInterceptedCall(getActivity(), (CallInfo) this.mCallList.remove(this.mClickPosition));
            this.mCallListAdapter.notifyDataSetChanged();
            updateUi();
            updateUnReadCountInTab();
            return;
        }
        HwLog.w(TAG, "deleteCall: Data list changed ,stop operation ,position = " + this.mClickPosition);
    }

    private void confirmRemoveAllCalls() {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_removeall_dialog, null);
        ((TextView) layout.findViewById(R.id.remove_all_dialog_message)).setText(R.string.Dialog_ClearAll_Directions02);
        Builder dlgBuilder = new Builder(getActivity());
        dlgBuilder.setView(layout);
        dlgBuilder.setNegativeButton(R.string.harassmentInterception_cancel, null);
        dlgBuilder.setPositiveButton(R.string.harassmentInterception_clear, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                CallFragment.this.removeAllCalls();
                HsmStat.statE(65);
            }
        });
        dlgBuilder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void removeAllCalls() {
        if (DBAdapter.deleteAllInterceptedCall(getActivity()) > 0) {
            DBAdapter.resetInterceptedCallCount(getActivity());
            refreshCallList();
            updateUnReadCountInTab();
        }
    }

    private void confirmAddContact() {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_add_contact_dlg, null);
        Builder builder = new Builder(getActivity());
        builder.setTitle(R.string.Button_SpamMessage_AddToContact);
        this.mCreateContactDlg = builder.create();
        this.mCreateContactDlg.setView(layout);
        OptionDlgListener clickListener = new OptionDlgListener();
        ((Button) layout.findViewById(R.id.btn_create_new)).setOnClickListener(clickListener);
        ((Button) layout.findViewById(R.id.btn_save_exist)).setOnClickListener(clickListener);
        this.mCreateContactDlg.setCanceledOnTouchOutside(false);
        this.mCreateContactDlg.setOnCancelListener(clickListener);
        this.mCreateContactDlg.show();
    }

    private void addToNewContact() {
        if (isClickedInfoValid()) {
            CallInfo callInfo = (CallInfo) this.mCallList.get(this.mClickPosition);
            CommonHelper.addToNewContact(getActivity(), callInfo.getName(), callInfo.getPhone());
            return;
        }
        HwLog.w(TAG, "addToNewContact: Data list changed ,stop operation, position = " + this.mClickPosition);
    }

    private void addToExistContact() {
        if (isClickedInfoValid()) {
            CommonHelper.addToExistContact(getActivity(), ((CallInfo) this.mCallList.get(this.mClickPosition)).getPhone());
            return;
        }
        HwLog.w(TAG, "addToExistContact: Data list changed ,stop operation, position = " + this.mClickPosition);
    }

    private void refreshListView(List<CallInfo> callList) {
        if (this.mCallListLayout == null) {
            HwLog.w(TAG, "refreshListView: Fragment is not initialized");
            return;
        }
        if (this.mCallList != null) {
            this.mCallList.clear();
            this.mCallList.addAll(callList);
        }
        this.mCallListAdapter.notifyDataSetChanged();
        updateUi();
    }

    private void updateUi() {
        if (Utility.isNullOrEmptyList(this.mCallList)) {
            this.mCallListLayout.setVisibility(8);
            this.mNoCallLayout.setVisibility(0);
            if (this.mMenuRemoveAll != null) {
                this.mMenuRemoveAll.setVisible(false);
                this.mMenuRemoveAll.setEnabled(false);
                return;
            }
            return;
        }
        this.mCallListLayout.setVisibility(0);
        this.mNoCallLayout.setVisibility(8);
        if (this.mMenuRemoveAll != null) {
            this.mMenuRemoveAll.setVisible(true);
            this.mMenuRemoveAll.setEnabled(true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isClickedInfoValid() {
        if (this.mClickPosition < 0 || this.mCallList == null || this.mClickPosition >= this.mCallList.size()) {
            return false;
        }
        CallInfo clickedInfo = (CallInfo) this.mCallList.get(this.mClickPosition);
        if (this.mClickedInfo == null || clickedInfo == null || clickedInfo.getId() != this.mClickedInfo.getId()) {
            return false;
        }
        return true;
    }

    private void updateUnReadCountInTab() {
        InterceptionActivity activity = (InterceptionActivity) getActivity();
        if (activity != null) {
            activity.updateUnReadCountInTab(1);
        }
    }

    private void initShowDualcardIcon() {
        if (CardItem.getCardItems().size() >= 2) {
            this.mShowDualCardIcon = Boolean.TRUE;
        } else {
            this.mShowDualCardIcon = Boolean.FALSE;
        }
    }
}
