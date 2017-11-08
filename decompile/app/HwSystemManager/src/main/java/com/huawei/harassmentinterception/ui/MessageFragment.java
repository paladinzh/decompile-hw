package com.huawei.harassmentinterception.ui;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.text.format.DateUtils;
import android.text.method.ScrollingMovementMethod;
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
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import com.huawei.harassmentinterception.blackwhitelist.AddWhiteListManager;
import com.huawei.harassmentinterception.blackwhitelist.ResultContext;
import com.huawei.harassmentinterception.callback.CheckRestoreSMSCallBack;
import com.huawei.harassmentinterception.callback.HandleListCallBack;
import com.huawei.harassmentinterception.common.CommonObject.ContactInfo;
import com.huawei.harassmentinterception.common.CommonObject.MessageInfo;
import com.huawei.harassmentinterception.db.DBAdapter;
import com.huawei.harassmentinterception.util.BlockReasonDescrp;
import com.huawei.harassmentinterception.util.CommonHelper;
import com.huawei.harassmentinterception.util.dlg.DialogUtil;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.TimeUtil;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.HsmStat;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst.Events;
import com.huawei.systemmanager.netassistant.ui.Item.CardItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class MessageFragment extends Fragment {
    private static final int MSG_ADD_WHITELIST = 2;
    private static final int MSG_CHECK_ADD_WHITELIST = 1;
    private static final int MSG_REFREASH_LIST = 3;
    private static final String TAG = "MessageFragment";
    private HandleListCallBack mAddListCallBack = new HandleListCallBack() {
        public void onAfterCheckListExist(int result, boolean isExist, List<ContactInfo> list) {
            ResultContext context = new ResultContext(result, isExist);
            Message message = MessageFragment.this.mHandler.obtainMessage();
            message.what = 1;
            message.obj = context;
            message.sendToTarget();
        }

        public void onProcessHandleList(Object obj) {
            Message message = MessageFragment.this.mHandler.obtainMessage();
            message.what = 2;
            message.obj = obj;
            message.sendToTarget();
        }

        public void onCompleteHandleList(int result) {
        }
    };
    private long mAddListId = 0;
    private AddWhiteListManager mAddListManager = null;
    private int mClickPosition = 0;
    private MessageInfo mClickedInfo = null;
    private AlertDialog mCreateContactDlg = null;
    private DataLoadingTask mDataLoadingTask = null;
    private Handler mHandler = new Handler() {
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case 1:
                    HwLog.d(MessageFragment.TAG, "SEND MSG_CHECK_ADD_WHITELIST MESSAGE");
                    MessageFragment.this.showConfirmAddWhiteDlg(msg.obj);
                    return;
                case 2:
                    MessageFragment.this.delMsgItems(msg.obj);
                    MessageFragment.this.dissmissOptionDlg();
                    MessageFragment.this.dissmissCreateContactDlg();
                    return;
                case 3:
                    HwLog.d(MessageFragment.TAG, "SEND MSG_REFREASH_LIST MESSAGE");
                    MessageFragment.this.dissmissOptionDlg();
                    MessageFragment.this.refreshMsgList();
                    MessageFragment.this.updateUnReadCountInTab();
                    return;
                default:
                    return;
            }
        }
    };
    private MenuItem mMenuRemoveAll = null;
    private List<MessageInfo> mMsgList = new ArrayList();
    private MsgListAdapter mMsgListAdapter = null;
    private RelativeLayout mMsgListLayout = null;
    private View mNoMsgLayout = null;
    private AlertDialog mOptionDlg = null;
    CheckRestoreSMSCallBack mRestoreSMSCallBack = new CheckRestoreSMSCallBack() {
        public void onCheckRestoreSMSButton(boolean isChecked) {
            MessageFragment.this.addSingleWhiteList(isChecked);
        }
    };
    private Boolean mShowDualCardIcon;
    private long mTodayStartTime = TimeUtil.getTodayStartTime();
    private ProgressDialog mWaitingDialog = null;

    private class DataLoadingTask extends AsyncTask<Void, Void, List<MessageInfo>> {
        private DataLoadingTask() {
        }

        protected List<MessageInfo> doInBackground(Void... arg0) {
            List<MessageInfo> messageList = Collections.emptyList();
            try {
                HwLog.i(MessageFragment.TAG, "DataLoadingTask-doInBackground:getActivity = " + MessageFragment.this.getActivity());
                messageList = DBAdapter.getInterceptedMsgs(GlobalContext.getContext());
            } catch (Exception e) {
                HwLog.e(MessageFragment.TAG, "DataLoadingTask-doInBackground: Exception", e);
            }
            return messageList;
        }

        protected void onCancelled(List<MessageInfo> result) {
            MessageFragment.this.mDataLoadingTask = null;
            HwLog.i(MessageFragment.TAG, "DataLoadingTask-onCancelled: Canceled");
            super.onCancelled(result);
        }

        protected void onPostExecute(List<MessageInfo> msgList) {
            MessageFragment.this.initShowDualcardIcon();
            MessageFragment.this.mDataLoadingTask = null;
            if (isCancelled()) {
                HwLog.i(MessageFragment.TAG, "DataLoadingTask-onPostExecute: Task is canceled");
            } else {
                MessageFragment.this.refreshListView(msgList);
            }
        }
    }

    class MsgListAdapter extends BaseAdapter {
        MsgListAdapter() {
        }

        public int getCount() {
            return MessageFragment.this.mMsgList.size();
        }

        public Object getItem(int arg0) {
            return null;
        }

        public long getItemId(int arg0) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup viewgroup) {
            if (convertView == null) {
                convertView = MessageFragment.this.newView(position, viewgroup);
            }
            MessageFragment.this.fillViewHolderWithData((ViewHolder) convertView.getTag(), (MessageInfo) MessageFragment.this.mMsgList.get(position), position);
            return convertView;
        }

        public void notifyDataSetChanged() {
            MessageFragment.this.mTodayStartTime = TimeUtil.getTodayStartTime();
            super.notifyDataSetChanged();
        }
    }

    private class OptionDlgListener implements OnClickListener, OnCancelListener {
        private OptionDlgListener() {
        }

        public void onClick(View view) {
            String statOp = "";
            switch (view.getId()) {
                case R.id.btn_create_new:
                    MessageFragment.this.addToNewContact();
                    statOp = "2";
                    break;
                case R.id.btn_save_exist:
                    MessageFragment.this.addToExistContact();
                    statOp = "2";
                    break;
                case R.id.btn_delete:
                    MessageFragment.this.confirmDeleteMsg();
                    break;
                case R.id.btn_add_whitelist:
                    MessageFragment.this.addWhiteList();
                    statOp = "1";
                    break;
                case R.id.btn_add_contact:
                    MessageFragment.this.confirmAddContact();
                    break;
                case R.id.btn_restore_msg:
                    MessageFragment.this.confirmRestoreMsg();
                    statOp = "0";
                    break;
            }
            if (view.getId() != R.id.btn_add_whitelist) {
                MessageFragment.this.dissmissOptionDlg();
                MessageFragment.this.dissmissCreateContactDlg();
            }
            if (!TextUtils.isEmpty(statOp)) {
                String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, statOp);
                if (1 == MessageFragment.this.mClickedInfo.getMsgType()) {
                    HsmStat.statE((int) Events.E_ANTISPAM_HANDLE_MMS, statParam);
                    return;
                }
                HsmStat.statE(63, statParam);
            }
        }

        public void onCancel(DialogInterface arg0) {
            MessageFragment.this.dissmissOptionDlg();
            MessageFragment.this.dissmissCreateContactDlg();
        }
    }

    private static class ViewHolder {
        TextView _body;
        TextView _contactInfo;
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
        View fragmentView = inflater.inflate(R.layout.interception_fragment_message, container, false);
        initFragmentView(fragmentView);
        return fragmentView;
    }

    private void delMsgItems(Object object) {
        if (this.mAddListManager.isRestoreMSG()) {
            List<MessageInfo> tobeDelMsgs = getMsgsByPhone(((MessageInfo) ((ArrayList) object).get(0)).getMatchedNumber());
            HwLog.d(TAG, "restore msg size" + tobeDelMsgs.size());
            this.mMsgList.removeAll(tobeDelMsgs);
            this.mMsgListAdapter.notifyDataSetChanged();
            updateUi();
            updateUnReadCountInTab();
        }
        hideWaitingDialog();
    }

    public void delMsgItemByPhone(String phone) {
        List<MessageInfo> toBeDelMsgs = getMsgsByPhone(phone);
        HwLog.d(TAG, "restore sms size" + toBeDelMsgs.size());
        this.mMsgList.removeAll(toBeDelMsgs);
        if (this.mMsgListAdapter != null) {
            this.mMsgListAdapter.notifyDataSetChanged();
            updateUi();
            updateUnReadCountInTab();
        }
    }

    private List<MessageInfo> getMsgsByPhone(String phone) {
        ArrayList<MessageInfo> msgs = new ArrayList();
        for (MessageInfo msg : this.mMsgList) {
            if (phone.equals(msg.getMatchedNumber())) {
                msgs.add(msg);
            }
        }
        return msgs;
    }

    private void initListManager() {
        this.mAddListManager = new AddWhiteListManager();
        this.mAddListManager.registerCallBack(this.mAddListCallBack);
    }

    private void addSingleWhiteList(boolean isChecked) {
        if (this.mAddListManager.isRunning()) {
            HwLog.i(TAG, "addWhitelistManager is busy");
            return;
        }
        showWaitingDialog();
        this.mAddListManager.handleList(GlobalContext.getContext(), this.mAddListId, Boolean.valueOf(isChecked));
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
        final MessageInfo msg = (MessageInfo) this.mMsgList.get(this.mClickPosition);
        if (isExist) {
            DialogUtil.createAddSingleWhiteListDlgFromMSG(getActivity(), msg.getContactInfo(getActivity()), this.mRestoreSMSCallBack);
        } else {
            new Thread("HarassIntercept_restoreSMS") {
                public void run() {
                    MessageFragment.this.restoreSMS(msg);
                    Message message = MessageFragment.this.mHandler.obtainMessage();
                    message.what = 3;
                    message.sendToTarget();
                }
            }.start();
            addSingleWhiteList(false);
        }
    }

    private void addWhiteList() {
        if (this.mAddListManager.isRunning()) {
            HwLog.d(TAG, "addWhiteList is running");
            return;
        }
        MessageInfo msg = (MessageInfo) this.mMsgList.get(this.mClickPosition);
        ArrayList<ContactInfo> phoneList = new ArrayList();
        phoneList.add(msg);
        this.mAddListId = this.mAddListManager.checkListExist(GlobalContext.getContext(), phoneList, null);
    }

    private void restoreSMS(MessageInfo msg) {
        for (MessageInfo message : DBAdapter.getInterceptedMsgsByFuzzyPhone(GlobalContext.getContext(), msg.getPhone())) {
            if (DBAdapter.addMsgToSystemInbox(GlobalContext.getContext(), message)) {
                DBAdapter.deleteInterceptedMsg(GlobalContext.getContext(), message);
            } else {
                HwLog.i(TAG, "restoreSMS addMsgToSystemInbox fail");
                return;
            }
        }
    }

    private void initFragmentView(View fragmentView) {
        this.mMsgListLayout = (RelativeLayout) fragmentView.findViewById(R.id.message_list_layout);
        this.mNoMsgLayout = fragmentView.findViewById(R.id.no_message_view);
        initEmptyView(GlobalContext.getContext(), this.mNoMsgLayout);
        ListView msgListView = (ListView) fragmentView.findViewById(R.id.message_list_view);
        this.mMsgListAdapter = new MsgListAdapter();
        msgListView.setAdapter(this.mMsgListAdapter);
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
        inflater.inflate(R.menu.interception_msgfragment_menu, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_remove_all:
                confirmRemoveAllMsgs();
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onPrepareOptionsMenu(Menu menu) {
        this.mMenuRemoveAll = menu.findItem(R.id.menu_remove_all);
        if (this.mMenuRemoveAll != null) {
            if (Utility.isNullOrEmptyList(this.mMsgList)) {
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
        refreshMsgList();
        super.onResume();
    }

    public void refreshMsgList() {
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
        dissmissCreateContactDlg();
        dissmissOptionDlg();
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

    private void onClickMsgInfo(int nPosition) {
        if (Utility.isNullOrEmptyList(this.mMsgList) || nPosition >= this.mMsgList.size()) {
            HwLog.w(TAG, "onClickMsgInfo: Invalid status or click position, " + nPosition);
        } else if (this.mOptionDlg == null || !this.mOptionDlg.isShowing()) {
            View layout = getLayoutInflater(null).inflate(R.layout.interception_message_option_dlg, null);
            this.mOptionDlg = new Builder(getActivity()).create();
            this.mOptionDlg.setView(layout);
            this.mClickPosition = nPosition;
            this.mClickedInfo = (MessageInfo) this.mMsgList.get(nPosition);
            initOptionDlgData(this.mOptionDlg, layout, nPosition);
            this.mOptionDlg.show();
            if (1 == this.mClickedInfo.getMsgType()) {
                HsmStat.statE(Events.E_ANTISPAM_VIEW_MMS);
            } else {
                HsmStat.statE(62);
            }
        } else {
            HwLog.w(TAG, "onClickMsgInfo: Previous dlg is not dissmissed");
        }
    }

    private void initOptionDlgData(AlertDialog optionDlg, View layout, int nPosition) {
        MessageInfo msg = (MessageInfo) this.mMsgList.get(nPosition);
        HwLog.i(TAG, "MSG TYPE=" + msg.getMsgType());
        optionDlg.setTitle(msg.getContactInfo(getActivity()) + " " + msg.getGeoLocation());
        LinearLayout smmaryView = (LinearLayout) layout.findViewById(R.id.msg_summary_layout);
        TextView sizeView = (TextView) layout.findViewById(R.id.textview_size);
        TextView expdateView = (TextView) layout.findViewById(R.id.textview_expdate);
        TextView bodyView = (TextView) layout.findViewById(R.id.msg_body);
        bodyView.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (msg.getMsgType() == 0) {
            smmaryView.setVisibility(8);
            bodyView.setText(msg.getBody());
        } else {
            smmaryView.setVisibility(0);
            sizeView.setText(getString(R.string.harassment_mms_size, new Object[]{Integer.valueOf((int) Math.ceil(((double) msg.getSize()) / 1024.0d))}));
            expdateView.setText(getString(R.string.harassment_mms_expiredate, new Object[]{CommonHelper.getSystemDateStyle(getActivity(), msg.getExpDate() * 1000)}));
            bodyView.setText(getString(R.string.harassment_mms_content_restore_to_inbox_message));
        }
        if (DBAdapter.getRcs() != null && msg.getMsgType() == 0) {
            DBAdapter.getRcs().setRcsFtLayout(smmaryView, sizeView, expdateView, bodyView, getActivity().getApplicationContext(), MessageInfo.translateFromMessageInfo(msg));
        }
        MessageFragment messageFragment = this;
        OptionDlgListener clickListener = new OptionDlgListener();
        ((Button) layout.findViewById(R.id.btn_restore_msg)).setOnClickListener(clickListener);
        Button btnAddWhitelist = (Button) layout.findViewById(R.id.btn_add_whitelist);
        if (DBAdapter.isWhitelisted(getActivity(), msg.getPhone()) || !CommonHelper.isValidDigtalPhoneNumber(msg.getPhone())) {
            btnAddWhitelist.setVisibility(8);
        } else {
            btnAddWhitelist.setOnClickListener(clickListener);
        }
        ((Button) layout.findViewById(R.id.btn_delete)).setOnClickListener(clickListener);
        optionDlg.setCanceledOnTouchOutside(false);
        optionDlg.setOnCancelListener(clickListener);
    }

    private void confirmRestoreMsg() {
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_COUNT, "1");
        if (1 == this.mClickedInfo.getMsgType()) {
            HsmStat.statE(Events.E_ANTISPAM_HANDLE_MMS_RESTORE);
        } else {
            HsmStat.statE((int) Events.E_HARASSMENT_RESTORE_MESSAGE, statParam);
        }
        restoreToSystemMsgInbox();
    }

    private void restoreToSystemMsgInbox() {
        if (isClickedInfoValid()) {
            MessageInfo msg = (MessageInfo) this.mMsgList.remove(this.mClickPosition);
            if (DBAdapter.addMsgToSystemInbox(getActivity(), msg)) {
                DBAdapter.deleteInterceptedMsg(getActivity(), msg);
                updateUnReadCountInTab();
            }
            refreshMsgList();
            this.mMsgListAdapter.notifyDataSetChanged();
            return;
        }
        HwLog.w(TAG, "restoreToSystemMsgInbox: Data changed or Invalid message ,position = " + this.mClickPosition);
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
            MessageInfo msg = (MessageInfo) this.mMsgList.get(this.mClickPosition);
            CommonHelper.addToNewContact(getActivity(), msg.getName(), msg.getPhone());
            return;
        }
        HwLog.w(TAG, "addToNewContact: Data changed or Invalid message ,position = " + this.mClickPosition);
    }

    private void addToExistContact() {
        if (isClickedInfoValid()) {
            CommonHelper.addToExistContact(getActivity(), ((MessageInfo) this.mMsgList.get(this.mClickPosition)).getPhone());
            return;
        }
        HwLog.w(TAG, "addToExistContact: Data changed or Invalid message ,position = " + this.mClickPosition);
    }

    private void confirmDeleteMsg() {
        deleteMessage();
        String statParam = HsmStatConst.constructJsonParams(HsmStatConst.PARAM_OP, "3");
        if (1 == this.mClickedInfo.getMsgType()) {
            HsmStat.statE(Events.E_ANTISPAM_HANDLE_MMS_DEL);
            return;
        }
        HsmStat.statE(63, statParam);
    }

    private void deleteMessage() {
        if (isClickedInfoValid()) {
            DBAdapter.deleteInterceptedMsg(getActivity(), (MessageInfo) this.mMsgList.remove(this.mClickPosition));
            this.mMsgListAdapter.notifyDataSetChanged();
            updateUi();
            updateUnReadCountInTab();
            return;
        }
        HwLog.w(TAG, "deleteMessage: Data changed or Invalid message ,position = " + this.mClickPosition);
    }

    private void confirmRemoveAllMsgs() {
        View layout = getLayoutInflater(null).inflate(R.layout.interception_removeall_dialog, null);
        ((TextView) layout.findViewById(R.id.remove_all_dialog_message)).setText(R.string.Dialog_ClearAll_Directions);
        Builder dlgBuilder = new Builder(getActivity());
        dlgBuilder.setView(layout);
        dlgBuilder.setNegativeButton(R.string.harassmentInterception_cancel, null);
        dlgBuilder.setPositiveButton(R.string.harassmentInterception_clear, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int arg1) {
                MessageFragment.this.removeAllMsgs();
                HsmStat.statE(61);
            }
        });
        dlgBuilder.show().getButton(-1).setTextColor(getResources().getColor(R.color.hsm_forbidden));
    }

    private void removeAllMsgs() {
        if (DBAdapter.deleteAllInterceptedMsg(getActivity()) > 0) {
            DBAdapter.resetInterceptedMsgCount(getActivity());
            refreshMsgList();
            updateUnReadCountInTab();
        }
    }

    private View newView(int position, ViewGroup viewgroup) {
        View view = getLayoutInflater(null).inflate(R.layout.interception_intercept_message_list_item, viewgroup, false);
        ViewHolder holder = new ViewHolder();
        holder._contactInfo = (TextView) view.findViewById(R.id.message_contactInfo);
        holder._body = (TextView) view.findViewById(R.id.message_body);
        holder._time = (TextView) view.findViewById(R.id.time);
        holder._reason = (TextView) view.findViewById(R.id.blockreason);
        holder._subId = (ImageView) view.findViewById(R.id.sub_id);
        view.setClickable(true);
        view.setTag(holder);
        view.setOnClickListener(new OnClickListener() {
            public void onClick(View view) {
                MessageFragment.this.onClickMsgInfo(((ViewHolder) view.getTag())._position);
            }
        });
        return view;
    }

    private void fillViewHolderWithData(ViewHolder holder, MessageInfo msgInfo, int nPosition) {
        holder._contactInfo.setText(msgInfo.getContactInfo(getActivity()));
        holder._body.setText(msgInfo.getBodyEx(getActivity()));
        if (DBAdapter.getRcs() != null && msgInfo.getMsgType() == 0) {
            DBAdapter.getRcs().setFileRcsBodyText(getActivity().getApplicationContext(), MessageInfo.translateFromMessageInfo(msgInfo), holder._body);
        }
        holder._position = nPosition;
        Context ctx = GlobalContext.getContext();
        String blockReason = BlockReasonDescrp.getMessageBlockreasonStr(ctx, msgInfo.getBlockReason());
        if (TextUtils.isEmpty(blockReason)) {
            holder._reason.setVisibility(8);
        } else {
            holder._reason.setVisibility(0);
            holder._reason.setText(blockReason);
        }
        String timeDes = "";
        long recordTime = msgInfo.getDate();
        if (recordTime >= this.mTodayStartTime) {
            timeDes = DateUtils.formatDateTime(ctx, recordTime, 1);
        } else {
            timeDes = DateUtils.formatDateTime(ctx, recordTime, 16);
        }
        holder._time.setText(timeDes);
        if (this.mShowDualCardIcon == null || !this.mShowDualCardIcon.booleanValue()) {
            holder._subId.setVisibility(8);
            return;
        }
        holder._subId.setVisibility(0);
        if (msgInfo.getSubId() == 1) {
            holder._subId.setImageResource(R.drawable.ic_phone_card2);
        } else {
            holder._subId.setImageResource(R.drawable.ic_phone_card1);
        }
    }

    private void initShowDualcardIcon() {
        if (CardItem.getCardItems().size() >= 2) {
            this.mShowDualCardIcon = Boolean.TRUE;
        } else {
            this.mShowDualCardIcon = Boolean.FALSE;
        }
    }

    private void refreshListView(List<MessageInfo> msgList) {
        if (this.mMsgListLayout == null) {
            HwLog.w(TAG, "refreshListView: Fragment is not initialized");
            return;
        }
        if (this.mMsgList != null) {
            this.mMsgList.clear();
            this.mMsgList.addAll(msgList);
        }
        this.mMsgListAdapter.notifyDataSetChanged();
        updateUi();
    }

    private void updateUi() {
        if (Utility.isNullOrEmptyList(this.mMsgList)) {
            this.mNoMsgLayout.setVisibility(0);
            this.mMsgListLayout.setVisibility(8);
            if (this.mMenuRemoveAll != null) {
                this.mMenuRemoveAll.setVisible(false);
                this.mMenuRemoveAll.setEnabled(false);
                return;
            }
            return;
        }
        this.mNoMsgLayout.setVisibility(8);
        this.mMsgListLayout.setVisibility(0);
        if (this.mMenuRemoveAll != null) {
            this.mMenuRemoveAll.setVisible(true);
            this.mMenuRemoveAll.setEnabled(true);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private boolean isClickedInfoValid() {
        if (this.mClickPosition < 0 || this.mMsgList == null || this.mClickPosition >= this.mMsgList.size()) {
            return false;
        }
        MessageInfo clickedInfo = (MessageInfo) this.mMsgList.get(this.mClickPosition);
        if (this.mClickedInfo == null || clickedInfo == null || clickedInfo.getId() != this.mClickedInfo.getId()) {
            return false;
        }
        return true;
    }

    private void updateUnReadCountInTab() {
        InterceptionActivity activity = (InterceptionActivity) getActivity();
        if (activity != null) {
            activity.updateUnReadCountInTab(0);
        }
    }
}
