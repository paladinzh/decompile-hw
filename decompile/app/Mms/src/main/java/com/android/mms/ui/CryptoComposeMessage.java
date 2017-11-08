package com.android.mms.ui;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnCancelListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewStub;
import android.view.inputmethod.InputMethodManager;
import android.widget.ImageButton;
import android.widget.TextView;
import com.amap.api.services.core.AMapException;
import com.android.mms.MmsConfig;
import com.android.mms.attachment.datamodel.data.AttachmentSelectData;
import com.android.mms.data.ContactList;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageListAdapter.ColumnsMap;
import com.android.mms.util.SignatureUtil;
import com.google.android.gms.R;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.crypto.CryptoMessageServiceProxy;
import com.huawei.mms.crypto.CryptoMessageUtil;
import com.huawei.mms.crypto.account.AccountManager;
import com.huawei.mms.crypto.util.DecryptData;
import com.huawei.mms.ui.HwBaseFragment;
import com.huawei.mms.ui.MsimSmsEncryptSetting;
import com.huawei.mms.ui.SmsEncryptSetting;
import com.huawei.mms.util.StatisticalHelper;
import java.lang.ref.WeakReference;
import java.util.HashSet;

public class CryptoComposeMessage {
    private static final String[] SEND_PROJECTION = new String[]{"_id", "thread_id", "address", "body", "status", "sub_id"};
    private Handler mCMAHandler = null;
    private ICryptoComposeHolder mComposeHolder;
    private Context mContext;
    private int mCryptoFlag;
    private boolean mIsActivityVisible = false;
    public MessageListAdapter mMsgListAdapter;
    private EsmsSharedPreferenceChangeListener mPreferenceChangeListener = null;
    private SharedPreferences mPreferences;
    private String mRecipents;
    private AlertDialog mSaveDraftREmindDlg;
    private ImageButton mSwitchButton;
    private TextView mSwitchHint;
    private String mSwitchOnHint;
    private View mView;
    private boolean mWasSwitchOnWhenHandleSupertext = false;

    public interface ICryptoComposeHolder {
        void addAttachment(int i, AttachmentSelectData attachmentSelectData, boolean z);

        void deleteSmsDraft();

        CharSequence get7BitText();

        ContactList getComposeRecipientsViewRecipients();

        HwBaseFragment getFragment();

        ContactList getRecipients();

        CharSequence getText();

        void goToConversationList();

        boolean hasAttachment();

        boolean hasText();

        boolean isComposeRecipientsViewVisible();

        boolean isContainSignature();

        boolean isDraftWorthSaving();

        boolean isFromWidget();

        boolean isSubjectEditorVisible();

        void judgeAttachSmiley();

        void refreshMediaAttachment(int i);

        boolean requiresMms();

        void setLengthRequiresMms(boolean z, boolean z2);

        void setSendButtonEnabled(int i, boolean z);

        void setText(String str);

        void showInvalidDestinationToast();

        void updateSendButtonView();
    }

    private class CMAHandler extends Handler {
        private void updateSwitchView() {
            if (CryptoComposeMessage.this.mComposeHolder != null && CryptoComposeMessage.this.mView != null) {
                if (AccountManager.getInstance().isCardStateActivated()) {
                    Log.d("CryptoComposeMessage", "updateSwitchView: card state is activated");
                    if (CryptoComposeMessage.this.mView.getVisibility() != 0) {
                        CryptoComposeMessage.this.mView.setVisibility(0);
                        if (CryptoComposeMessage.this.mSwitchButton != null && CryptoComposeMessage.this.mSwitchHint != null) {
                            CryptoComposeMessage.this.setPreferenceChangeListener();
                            if (CryptoMessageUtil.isSmsEncryptionSwitchOn(CryptoComposeMessage.this.mComposeHolder)) {
                                CryptoComposeMessage.this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_on);
                                CryptoComposeMessage.this.mSwitchHint.setText(R.string.encrypted_sms_switch_on);
                                CryptoComposeMessage.this.mSwitchHint.setTextColor(CryptoComposeMessage.this.mContext.getResources().getColor(R.color.timestamp_color));
                            } else {
                                CryptoComposeMessage.this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_off);
                                CryptoComposeMessage.this.mSwitchHint.setText(R.string.encrypted_sms_switch_off);
                                CryptoComposeMessage.this.mSwitchHint.setTextColor(CryptoComposeMessage.this.mContext.getResources().getColor(R.color.text_color));
                            }
                        } else {
                            return;
                        }
                    }
                    return;
                }
                Log.d("CryptoComposeMessage", "updateSwitchView: all cards state are inactivated");
                if (CryptoMessageUtil.isSmsEncryptionSwitchOn(CryptoComposeMessage.this.mComposeHolder)) {
                    CryptoMessageUtil.setSmsEncryptionSwitchState(CryptoComposeMessage.this.mComposeHolder, false);
                }
                if (CryptoComposeMessage.this.mView.getVisibility() == 0) {
                    CryptoComposeMessage.this.mView.setVisibility(8);
                }
            }
        }

        public void handleMessage(Message msg) {
            Log.d("CryptoComposeMessage", "handleMessage: card state changed, msg=" + msg);
            switch (msg.what) {
                case 5:
                    AccountManager.getInstance().bindFingerPrompt(CryptoComposeMessage.this.mComposeHolder.getFragment().getActivity());
                    updateSwitchView();
                    return;
                case AMapException.CODE_AMAP_ID_NOT_EXIST /*2001*/:
                    updateSwitchView();
                    return;
                case 3001:
                    AccountManager accountManager = AccountManager.getInstance();
                    if (accountManager.isNeedReactive(CryptoComposeMessage.this.mContext)) {
                        accountManager.updateStateForNewKeyVersion(CryptoComposeMessage.this.mContext);
                        CryptoComposeMessage.showReactiveDialog(CryptoComposeMessage.this.mContext);
                        updateSwitchView();
                        return;
                    }
                    return;
                default:
                    return;
            }
        }
    }

    private static class DecryptSmsRefreshHandler extends Handler {
        private WeakReference<MessageListAdapter> mAdapter;

        public DecryptSmsRefreshHandler(MessageListAdapter adapter) {
            this.mAdapter = new WeakReference(adapter);
        }

        public void handleMessage(Message msg) {
            MessageListAdapter listAdapter = (MessageListAdapter) this.mAdapter.get();
            if (listAdapter != null) {
                CryptoMessageListAdapter listAdapterCust = listAdapter.getCryptoMessageListAdapter();
                if (listAdapterCust != null && (msg.obj instanceof DecryptData)) {
                    DecryptData data = msg.obj;
                    if (data != null) {
                        String text = data.getMessageContent();
                        if (!TextUtils.isEmpty(text)) {
                            switch (msg.what) {
                                case 1:
                                    listAdapterCust.putIntoEncryptSmsCache(Long.valueOf(data.getMessageId()), text);
                                    listAdapter.notifyDataSetChanged();
                                    return;
                                default:
                                    Log.d("CryptoComposeMessage", "DecryptSmsRefreshHandler#handleMessage: Unknown message=" + msg.what);
                                    return;
                            }
                        }
                    }
                }
            }
        }
    }

    private static class EsmsSharedPreferenceChangeListener implements OnSharedPreferenceChangeListener {
        private ImageButton mButton;
        private ICryptoComposeHolder mComposeHolder;
        private TextView mHint;

        public EsmsSharedPreferenceChangeListener(ICryptoComposeHolder composeHolder, ImageButton button, TextView hint) {
            this.mButton = button;
            this.mHint = hint;
            this.mComposeHolder = composeHolder;
        }

        private void disableSwitch() {
            if (this.mButton != null) {
                this.mButton.setClickable(false);
                this.mButton.setEnabled(false);
                this.mButton.setBackgroundResource(R.drawable.encrypted_sms_lock_unenable);
            }
            if (this.mHint != null) {
                this.mHint.setClickable(false);
                this.mHint.setEnabled(false);
                this.mHint.setTextColor(-7829368);
            }
        }

        private boolean isSubjectEditorVisible() {
            return this.mComposeHolder.isSubjectEditorVisible();
        }

        public void onSharedPreferenceChanged(SharedPreferences sp, String key) {
            String recipients = CryptoMessageUtil.getFormatedRecipients(this.mComposeHolder);
            Log.d("CryptoComposeMessage", "onSharedPreferenceChanged: recipients=" + recipients + ", key=" + key);
            if (recipients.equals(key)) {
                boolean isTextAsMms = false;
                if (sp.getBoolean(key, false)) {
                    StatisticalHelper.incrementReportCount(this.mComposeHolder.getFragment().getContext(), 2194);
                    this.mButton.setBackgroundResource(R.drawable.encrypted_sms_lock_on);
                    this.mHint.setText(R.string.encrypted_sms_switch_on);
                    this.mHint.setTextColor(this.mComposeHolder.getFragment().getContext().getResources().getColor(R.color.timestamp_color));
                    CharSequence smsText = this.mComposeHolder.getText();
                    if (!TextUtils.isEmpty(smsText)) {
                        CharSequence eText = CryptoComposeMessage.getEncryptedText(smsText.toString());
                        if (!TextUtils.isEmpty(eText)) {
                            isTextAsMms = MessageUtils.isMmsText(eText);
                        }
                    }
                    this.mComposeHolder.deleteSmsDraft();
                } else {
                    StatisticalHelper.incrementReportCount(this.mComposeHolder.getFragment().getContext(), 2195);
                    this.mButton.setBackgroundResource(R.drawable.encrypted_sms_lock_off);
                    this.mHint.setText(R.string.encrypted_sms_switch_off);
                    this.mHint.setTextColor(this.mComposeHolder.getFragment().getContext().getResources().getColor(R.color.text_color));
                    isTextAsMms = MessageUtils.isMmsText(this.mComposeHolder.get7BitText());
                    if (this.mComposeHolder.requiresMms() || isSubjectEditorVisible()) {
                        disableSwitch();
                    }
                }
                if (!MmsConfig.getMultipartSmsEnabled()) {
                    this.mComposeHolder.setLengthRequiresMms(isTextAsMms, true);
                }
                this.mComposeHolder.updateSendButtonView();
            }
        }
    }

    public void notifyActivityVisibility(boolean visible, MessageListAdapter adapter) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && adapter != null) {
            if (!visible) {
                adapter.getCryptoMessageListAdapter().setMarkedMessageId(Long.MAX_VALUE);
                CryptoMessageUtil.clearAccountState();
            } else if (isActivityRestoreFromBG()) {
                adapter.notifyDataSetChanged();
            }
            this.mIsActivityVisible = visible;
        }
    }

    public boolean isActivityRestoreFromBG() {
        boolean z = false;
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return false;
        }
        if (!this.mIsActivityVisible) {
            z = true;
        }
        return z;
    }

    public void notifyMessageSent(MessageListAdapter adapter) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mWasSwitchOnWhenHandleSupertext = false;
            if (isSwitchVisible()) {
                boolean requiresMms = this.mComposeHolder.requiresMms();
                Log.d("CryptoComposeMessage", "notifyMessageSent: requiresMms=" + requiresMms);
                updateSwitchDisplayState(requiresMms);
            }
            if (Long.MIN_VALUE == adapter.getCryptoMessageListAdapter().getMarkedMessageId()) {
                Log.d("CryptoComposeMessage", "notifyMessageSent: the marked message id is Long.MIN_VALUE");
            } else if (isSwitchVisible() && CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
                long msgId = Long.MAX_VALUE;
                Uri uri = Uri.parse("content://sms/queued");
                Cursor cursor = SqliteWrapper.query(this.mContext, this.mContext.getContentResolver(), uri, SEND_PROJECTION, null, null, "date ASC");
                if (cursor != null) {
                    Log.d("CryptoComposeMessage", "notifyMessageSent: cursor is Ok");
                    try {
                        if (cursor.moveToFirst()) {
                            msgId = cursor.getLong(0);
                            Log.d("CryptoComposeMessage", "notifyMessageSent: GET message id from database: " + msgId);
                        }
                        cursor.close();
                    } catch (Throwable th) {
                        cursor.close();
                    }
                }
                Log.d("CryptoComposeMessage", "notifyMessageSent: msgId=" + msgId);
                adapter.getCryptoMessageListAdapter().setMarkedMessageId(msgId);
            }
        }
    }

    public void initEncryptSms(MessageListAdapter msgListAdapter, MessageListView msgListView) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mMsgListAdapter = msgListAdapter;
            CryptoMessageListAdapter listAdapterCust = msgListAdapter.getCryptoMessageListAdapter();
            if (listAdapterCust != null) {
                listAdapterCust.setRefreshHandlerAndNewDecryptStack(new DecryptSmsRefreshHandler(msgListAdapter));
                listAdapterCust.setmComposeMessageScrollListener(new ComposeMessageScrollListener(msgListAdapter));
            }
        }
    }

    public void onCreate(final ICryptoComposeHolder composeHolder, final RichMessageEditor richEditor) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mComposeHolder = composeHolder;
            this.mContext = composeHolder.getFragment().getContext();
            this.mSwitchOnHint = this.mContext.getResources().getString(R.string.encrypted_sms_switch_on);
            ContactList emptyRecipient = new ContactList();
            if (this.mComposeHolder.isComposeRecipientsViewVisible()) {
                emptyRecipient = this.mComposeHolder.getComposeRecipientsViewRecipients();
            } else {
                emptyRecipient = this.mComposeHolder.getRecipients();
            }
            if (CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mContext, emptyRecipient)) {
                CryptoMessageUtil.setSmsEncryptionSwitchState(this.mContext, emptyRecipient, true);
            } else {
                CryptoMessageUtil.setSmsEncryptionSwitchState(this.mContext, emptyRecipient, false);
            }
            this.mPreferences = CryptoMessageUtil.getSharedPreferences(this.mContext);
            ViewStub switchStub = (ViewStub) composeHolder.getFragment().getView().findViewById(R.id.encrypted_sms_switch_stub);
            if (switchStub == null) {
                Log.e("CryptoComposeMessage", "onCreate: initialize encrypted sms failed");
                return;
            }
            switchStub.setLayoutResource(R.layout.compose_message_activity_multisim_encrypto_stub);
            this.mView = switchStub.inflate();
            this.mSwitchButton = (ImageButton) this.mView.findViewById(R.id.encrypted_sms_switch);
            this.mSwitchHint = (TextView) this.mView.findViewById(R.id.encrypted_sms_switch_hint);
            this.mPreferenceChangeListener = new EsmsSharedPreferenceChangeListener(this.mComposeHolder, this.mSwitchButton, this.mSwitchHint);
            OnClickListener clickListener = new OnClickListener() {
                public void onClick(View buttonView) {
                    richEditor.requestFocus();
                    if (CryptoMessageUtil.isSmsEncryptionSwitchOn(composeHolder)) {
                        CryptoMessageUtil.setSmsEncryptionSwitchState(composeHolder, false);
                        CryptoComposeMessage.this.mWasSwitchOnWhenHandleSupertext = false;
                    } else if (CryptoComposeMessage.this.mSwitchHint == null || !CryptoComposeMessage.this.mSwitchOnHint.equals(CryptoComposeMessage.this.mSwitchHint.getText().toString())) {
                        CryptoMessageUtil.setSmsEncryptionSwitchState(composeHolder, true);
                        composeHolder.judgeAttachSmiley();
                    }
                }
            };
            this.mSwitchButton.setOnClickListener(clickListener);
            this.mSwitchHint.setOnClickListener(clickListener);
            this.mWasSwitchOnWhenHandleSupertext = false;
        }
    }

    public boolean isSmsEncryptionSwitchOn() {
        boolean z = false;
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return false;
        }
        if (isSwitchVisible()) {
            z = CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder);
        }
        return z;
    }

    public boolean isSmsRelateSignature() {
        String signature = SignatureUtil.getSignature(this.mComposeHolder.getFragment().getContext(), MmsConfig.getDefaultSignatureText());
        CharSequence richEditorText = this.mComposeHolder.getText();
        if (!this.mComposeHolder.isContainSignature()) {
            return false;
        }
        String property = System.getProperty("line.separator", "");
        if (TextUtils.isEmpty(richEditorText) || TextUtils.isEmpty(signature) || richEditorText.length() <= signature.length() + property.length()) {
            return true;
        }
        return false;
    }

    public void onResume(Activity activity, Conversation conversation) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return;
        }
        if (this.mView == null) {
            Log.e("CryptoComposeMessage", "onResume: mView is null");
            return;
        }
        if (this.mCMAHandler == null) {
            this.mCMAHandler = new CMAHandler();
        }
        CryptoMessageServiceProxy.addListener(this.mCMAHandler);
        showSmsEncryptionStatePrompt(this.mComposeHolder.getFragment().getActivity());
        if (AccountManager.getInstance().isCardStateActivated()) {
            if (conversation == null || conversation.isServiceNumber()) {
                this.mView.setVisibility(8);
            } else {
                this.mView.setVisibility(0);
            }
            if (this.mPreferences == null) {
                Log.e("CryptoComposeMessage", "onResume: mPreferences is null");
                return;
            } else if (this.mPreferenceChangeListener == null) {
                Log.e("CryptoComposeMessage", "onResume: mPreferenceChangeListener is null");
                return;
            } else {
                boolean requreMms;
                Log.d("CryptoComposeMessage", "onResume: register shared preference change listener");
                this.mPreferences.registerOnSharedPreferenceChangeListener(this.mPreferenceChangeListener);
                if (CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
                    this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_on);
                    this.mSwitchHint.setText(R.string.encrypted_sms_switch_on);
                    this.mSwitchHint.setTextColor(this.mContext.getResources().getColor(R.color.timestamp_color));
                    if (this.mSaveDraftREmindDlg != null && this.mSaveDraftREmindDlg.isShowing()) {
                        this.mSaveDraftREmindDlg.dismiss();
                    }
                } else {
                    this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_off);
                    this.mSwitchHint.setText(R.string.encrypted_sms_switch_off);
                    this.mSwitchHint.setTextColor(this.mContext.getResources().getColor(R.color.text_color));
                }
                if (this.mComposeHolder.isDraftWorthSaving()) {
                    requreMms = this.mComposeHolder.requiresMms();
                } else {
                    requreMms = false;
                }
                if (requreMms) {
                    CryptoMessageUtil.setSmsEncryptionSwitchState(this.mComposeHolder, false);
                }
                updateSwitchDisplayState(requreMms);
                hideKeyBoard(activity);
                return;
            }
        }
        CryptoMessageUtil.setSmsEncryptionSwitchState(this.mComposeHolder, false);
        this.mView.setVisibility(8);
    }

    private void hideKeyBoard(Activity activity) {
        InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService("input_method");
        if (inputMethodManager != null) {
            View v = activity.getCurrentFocus();
            if (v != null && v.getWindowToken() != null) {
                inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), 0);
            }
        }
    }

    private void showSmsEncryptionStatePrompt(Activity activity) {
        AccountManager accountManager = AccountManager.getInstance();
        if (accountManager.isNeedReactive(this.mContext)) {
            accountManager.updateStateForNewKeyVersion(this.mContext);
            showReactiveDialog(this.mContext);
            accountManager.setShouldBindFingerPrompt(this.mContext, true);
            return;
        }
        accountManager.bindFingerPrompt(activity);
    }

    public void onPause() {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            if (this.mCMAHandler != null) {
                CryptoMessageServiceProxy.removeListener(this.mCMAHandler);
            }
            if (isSwitchVisible()) {
                CryptoMessageListAdapter listAdapterCust = this.mMsgListAdapter.getCryptoMessageListAdapter();
                if (listAdapterCust != null) {
                    listAdapterCust.clearSendIDCache();
                }
                if (this.mPreferences == null) {
                    Log.e("CryptoComposeMessage", "onPause: mPreferences is null");
                } else if (this.mPreferenceChangeListener == null) {
                    Log.e("CryptoComposeMessage", "onPause: mPreferenceChangeListener is null");
                } else {
                    Log.d("CryptoComposeMessage", "onPause: unregister shared preference change listener");
                    this.mPreferences.unregisterOnSharedPreferenceChangeListener(this.mPreferenceChangeListener);
                }
            }
        }
    }

    public void onProtocolChanged(boolean isMms, RichMessageEditor richEditor) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isSwitchVisible()) {
            boolean isVisible = this.mComposeHolder.isSubjectEditorVisible();
            if (isMms) {
                isVisible = true;
            }
            updateSwitchDisplayState(isVisible);
            if (isMms && CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder) && !TextUtils.isEmpty(this.mComposeHolder.getText()) && !this.mComposeHolder.hasAttachment() && this.mComposeHolder.hasText()) {
                new Builder(this.mContext).setTitle(R.string.mms_remind_title).setMessage(R.string.encrypted_esms_notify_supertext).setPositiveButton(R.string.encrypted_esms_user_know, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int buttonId) {
                        CryptoComposeMessage.this.mWasSwitchOnWhenHandleSupertext = true;
                        CryptoMessageUtil.setSmsEncryptionSwitchState(CryptoComposeMessage.this.mComposeHolder, false);
                        dialog.dismiss();
                    }
                }).setCancelable(false).show();
            }
        }
    }

    public void onBackPressed() {
        if (!CryptoMessageUtil.isCryptoSmsEnabled()) {
            return;
        }
        if (this.mComposeHolder.isDraftWorthSaving()) {
            this.mSaveDraftREmindDlg = new Builder(this.mContext).setTitle(R.string.mms_remind_title).setMessage(R.string.discard_message_for_sms_encryption).setPositiveButton(R.string.yes, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    if (CryptoComposeMessage.this.mComposeHolder.isFromWidget()) {
                        CryptoComposeMessage.this.mComposeHolder.goToConversationList();
                    } else {
                        CryptoComposeMessage.this.mComposeHolder.getFragment().finishSelf(false);
                    }
                    CryptoComposeMessage.this.mComposeHolder.setText("");
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    dialog.dismiss();
                }
            }).show();
            return;
        }
        if (this.mComposeHolder.isFromWidget()) {
            this.mComposeHolder.goToConversationList();
        } else {
            this.mComposeHolder.getFragment().finishSelf(false);
        }
    }

    public void onDestroy(Context context) {
        if (this.mCMAHandler != null) {
            CryptoMessageServiceProxy.removeListener(this.mCMAHandler);
            this.mCMAHandler = null;
        }
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            if (!(this.mMsgListAdapter == null || this.mMsgListAdapter.getCryptoMessageListAdapter() == null)) {
                this.mMsgListAdapter.getCryptoMessageListAdapter().cleanEncryptCache();
            }
            ContactList emptyRecipient = new ContactList();
            if (this.mComposeHolder.isComposeRecipientsViewVisible()) {
                emptyRecipient = this.mComposeHolder.getRecipients();
            }
            if (CryptoMessageUtil.isSmsEncryptionSwitchOn(context, emptyRecipient)) {
                CryptoMessageUtil.setSmsEncryptionSwitchState(context, emptyRecipient, false);
            }
            if (this.mSaveDraftREmindDlg != null && this.mSaveDraftREmindDlg.isShowing()) {
                this.mSaveDraftREmindDlg.dismiss();
            }
        }
    }

    public void onPreMessageSent(boolean recipientHasFocus) {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isSwitchVisible()) {
            ContactList emptyRecipients = new ContactList();
            if (recipientHasFocus && this.mSwitchHint != null && this.mSwitchOnHint.equals(this.mSwitchHint.getText().toString())) {
                CryptoMessageUtil.setSmsEncryptionSwitchState(this.mComposeHolder, true);
                CryptoMessageUtil.setSmsEncryptionSwitchState(this.mContext, emptyRecipients, false);
            }
        }
    }

    public boolean needNotifyUser(int type) {
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !isSwitchVisible()) {
            return false;
        }
        if (CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
            boolean needNotifyUser;
            switch (type) {
                case 0:
                    needNotifyUser = false;
                    break;
                case 1:
                    needNotifyUser = true;
                    break;
                case 2:
                    needNotifyUser = true;
                    break;
                case 3:
                    needNotifyUser = true;
                    break;
                case 4:
                    needNotifyUser = true;
                    break;
                case 5:
                    needNotifyUser = true;
                    break;
                case 6:
                    needNotifyUser = true;
                    break;
                case 7:
                    needNotifyUser = false;
                    break;
                case 8:
                    needNotifyUser = false;
                    break;
                case 10:
                    needNotifyUser = false;
                    break;
                case 11:
                    needNotifyUser = false;
                    break;
                case 12:
                    needNotifyUser = true;
                    break;
                case 13:
                    needNotifyUser = true;
                    break;
                case 14:
                    needNotifyUser = true;
                    break;
                case 1001:
                    needNotifyUser = true;
                    break;
                case AMapException.CODE_AMAP_SERVICE_MISSING_REQUIRED_PARAMS /*1201*/:
                    needNotifyUser = true;
                    break;
                case 1211:
                    needNotifyUser = true;
                    break;
                default:
                    needNotifyUser = false;
                    break;
            }
            return needNotifyUser;
        }
        Log.d("CryptoComposeMessage", "needNotifyUser: sms encryption switch in CMA is off");
        return false;
    }

    public void addAttachment(RichMessageEditor richEditor, int type, boolean replace) {
        addAttachment(richEditor, type, replace, null);
    }

    public void addAttachment(final RichMessageEditor richEditor, final int type, final boolean replace, final AttachmentSelectData attachmentData) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            new Builder(this.mContext).setTitle(R.string.mms_remind_title).setMessage(R.string.notice_attachment_esms_switch_on).setPositiveButton(R.string.esms_add, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    CryptoMessageUtil.setSmsEncryptionSwitchState(CryptoComposeMessage.this.mComposeHolder, false);
                    CryptoComposeMessage.this.mComposeHolder.addAttachment(type, attachmentData, replace);
                    dialog.dismiss();
                }
            }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    CryptoComposeMessage.this.mComposeHolder.judgeAttachSmiley();
                    if (attachmentData != null) {
                        CryptoComposeMessage.this.mComposeHolder.refreshMediaAttachment(type);
                    }
                    richEditor.requestFocus();
                    dialog.dismiss();
                }
            }).setOnCancelListener(new OnCancelListener() {
                public void onCancel(DialogInterface arg0) {
                    CryptoComposeMessage.this.mComposeHolder.judgeAttachSmiley();
                    if (attachmentData != null) {
                        CryptoComposeMessage.this.mComposeHolder.refreshMediaAttachment(type);
                    }
                    richEditor.requestFocus();
                }
            }).show();
        }
    }

    public void showVcardMmsTypeDialog(final RichMessageEditor richEditor, final Uri uri) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            Builder builder = new Builder(this.mContext);
            builder.setTitle(R.string.vcard_contact);
            builder.setItems(R.array.mms_vcard_menu, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int which) {
                    switch (which) {
                        case 0:
                            richEditor.insertVcardText(uri);
                            CryptoComposeMessage.this.mComposeHolder.showInvalidDestinationToast();
                            break;
                        case 1:
                            CryptoComposeMessage.this.addContactForSmsEncryption(richEditor, uri);
                            break;
                    }
                    dialog.dismiss();
                }
            });
            builder.show();
        }
    }

    public void updateUIOnRecipientsChanged() {
        updateSendButtonState();
        updateSwitchButtonState();
    }

    public void updateSendButtonState() {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isSwitchVisible() && CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
            if (MessageUtils.isMultiSimEnabled()) {
                if (!AccountManager.getInstance().isCardStateActivated(0)) {
                    this.mComposeHolder.setSendButtonEnabled(R.id.send_button_dual_1, false);
                }
                if (!AccountManager.getInstance().isCardStateActivated(1)) {
                    this.mComposeHolder.setSendButtonEnabled(R.id.send_button_dual_2, false);
                }
            } else if (!AccountManager.getInstance().isCardStateActivated()) {
                this.mComposeHolder.setSendButtonEnabled(R.id.send_button_sms, false);
            }
        }
    }

    public void updateSwitchButtonState() {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isSwitchVisible()) {
            if (CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
                this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_on);
                this.mSwitchHint.setText(R.string.encrypted_sms_switch_on);
                this.mSwitchHint.setTextColor(this.mContext.getResources().getColor(R.color.timestamp_color));
            } else {
                this.mSwitchButton.setBackgroundResource(R.drawable.encrypted_sms_lock_off);
                this.mSwitchHint.setText(R.string.encrypted_sms_switch_off);
                this.mSwitchHint.setTextColor(this.mContext.getResources().getColor(R.color.text_color));
            }
        }
    }

    public void updateSwitchStateLoadDraft(boolean isMms) {
        updateSwitchDisplayState(isMms);
    }

    private void updateSwitchDisplayState(boolean isMms) {
        boolean z;
        boolean z2 = false;
        if (!isMms) {
            CharSequence text = this.mComposeHolder.getText();
            if (!TextUtils.isEmpty(text)) {
                isMms = MessageUtils.isMmsText(getEncryptedText(text.toString()));
            }
        }
        boolean isSwitchOn = CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder);
        Log.d("CryptoComposeMessage", "updateSwitchDisplayState: isMms=" + isMms + ", isSwitchOn=" + isSwitchOn);
        if (this.mSwitchButton != null) {
            this.mSwitchButton.setClickable(!isMms);
            ImageButton imageButton = this.mSwitchButton;
            if (isMms) {
                z = false;
            } else {
                z = true;
            }
            imageButton.setEnabled(z);
            int resId = R.drawable.encrypted_sms_lock_off;
            if (isMms) {
                resId = R.drawable.encrypted_sms_lock_unenable;
                Log.e("CryptoComposeMessage", "resId = R.drawable.encrypted_sms_lock_unenable");
            } else if (isSwitchOn) {
                resId = R.drawable.encrypted_sms_lock_on;
            }
            this.mSwitchButton.setBackgroundResource(resId);
        }
        if (this.mSwitchHint != null) {
            TextView textView = this.mSwitchHint;
            if (isMms) {
                z = false;
            } else {
                z = true;
            }
            textView.setClickable(z);
            TextView textView2 = this.mSwitchHint;
            if (!isMms) {
                z2 = true;
            }
            textView2.setEnabled(z2);
            if (isMms) {
                this.mSwitchHint.setTextColor(-7829368);
                return;
            }
            int color = this.mContext.getResources().getColor(R.color.text_color);
            if (isSwitchOn) {
                color = this.mContext.getResources().getColor(R.color.timestamp_color);
            }
            this.mSwitchHint.setTextColor(color);
        }
    }

    private static CharSequence getEncryptedText(String smsText) {
        return smsText;
    }

    private void addContactForSmsEncryption(final RichMessageEditor richEditor, final Uri uri) {
        new Builder(this.mContext).setTitle(R.string.mms_remind_title).setMessage(R.string.notice_attachment_esms_switch_on).setPositiveButton(R.string.esms_add, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                CryptoMessageUtil.setSmsEncryptionSwitchState(CryptoComposeMessage.this.mComposeHolder, false);
                richEditor.setNewAttachment(uri, 6, false);
                CryptoComposeMessage.this.mComposeHolder.showInvalidDestinationToast();
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.no, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                CryptoComposeMessage.this.mComposeHolder.judgeAttachSmiley();
                richEditor.requestFocus();
                dialog.dismiss();
            }
        }).setCancelable(false).show();
    }

    public void updateActionBarItemMenuStatus(MenuEx menuEx, MessageListAdapter msgListAdapter, Long[] selection) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            if (menuEx != null) {
                menuEx.setItemVisible(278925318, true);
            }
            if (!(selection == null || selection.length <= 0 || this.mMsgListAdapter == null || menuEx == null)) {
                Object obj = null;
                ColumnsMap columnsMap = msgListAdapter.getColumnsMap();
                HashSet<Long> selectionSet = new HashSet();
                for (Long sel : selection) {
                    selectionSet.add(sel);
                }
                Cursor c = this.mMsgListAdapter.getCursor();
                int currentpos = c.getPosition();
                if (c.moveToFirst()) {
                    while (true) {
                        Long msgId = Long.valueOf(c.getLong(columnsMap.mColumnMsgId));
                        if ("sms".equals(c.getString(columnsMap.mColumnMsgType)) && selectionSet.contains(msgId)) {
                            String messageBody = c.getString(columnsMap.mColumnSmsBody);
                            CryptoMessageItem cryptoMessageItem = new CryptoMessageItem();
                            cryptoMessageItem.setEncryptSmsType(messageBody);
                            obj = cryptoMessageItem.getEncryptSmsType() != 0 ? 1 : null;
                            if (obj != null) {
                                break;
                            }
                        }
                        if (!c.moveToNext()) {
                            break;
                        }
                    }
                    c.moveToPosition(currentpos);
                }
                if (obj != null) {
                    menuEx.setItemEnabled(278925316, false);
                    menuEx.setItemEnabled(278925319, false);
                    menuEx.setItemEnabled(278927472, false);
                    if (selection.length > 1) {
                        menuEx.setItemEnabled(278925318, false);
                    }
                }
            }
        }
    }

    public void updateOverflowMenu(MenuEx menuEx, MessageListAdapter msgListAdapter, Long selection) {
        if (!(!CryptoMessageUtil.isCryptoSmsEnabled() || this.mMsgListAdapter == null || menuEx == null)) {
            MessageItem msgItem = getMessageItemByMsgId(msgListAdapter, selection);
            if (msgItem != null && msgItem.getCryptoMessageItem().isEncryptSms(msgItem)) {
                menuEx.setItemVisible(278925318, false);
                menuEx.setItemVisible(278925351, false);
                menuEx.setItemVisible(278925325, false);
                menuEx.setItemVisible(278925323, false);
                menuEx.setItemVisible(278925324, false);
                menuEx.setItemVisible(278925343, false);
                menuEx.setItemVisible(278925322, false);
            }
        }
    }

    private MessageItem getMessageItemByMsgId(MessageListAdapter msgListAdapter, Long msgId) {
        return msgListAdapter.getCachedMessageItem(msgId.longValue() > 0 ? "sms" : "mms", msgId.longValue() > 0 ? msgId.longValue() : -msgId.longValue(), this.mMsgListAdapter.getCursor());
    }

    public void handleInsertSubject() {
        if (CryptoMessageUtil.isCryptoSmsEnabled() && isSwitchVisible()) {
            boolean visible = this.mComposeHolder.isSubjectEditorVisible();
            if (!CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
                updateSwitchDisplayState(visible);
            } else if (visible) {
                CryptoMessageUtil.setSmsEncryptionSwitchState(this.mComposeHolder, false);
            }
        }
    }

    private boolean isSwitchVisible() {
        boolean z = false;
        if (this.mView == null) {
            return false;
        }
        if (this.mView.getVisibility() == 0) {
            z = true;
        }
        return z;
    }

    public void setPreferenceChangeListener() {
        if (this.mPreferences != null && this.mPreferenceChangeListener != null) {
            this.mPreferences.registerOnSharedPreferenceChangeListener(this.mPreferenceChangeListener);
        }
    }

    private static void showReactiveDialog(final Context context) {
        new Builder(context).setTitle(R.string.mms_remind_title).setMessage(R.string.esms_credentials_overdue).setPositiveButton(R.string.esms_switch_on, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int buttonId) {
                if (MessageUtils.isMultiSimEnabled()) {
                    context.startActivity(new Intent(context, MsimSmsEncryptSetting.class));
                } else {
                    context.startActivity(new Intent(context, SmsEncryptSetting.class));
                }
            }
        }).setNegativeButton(R.string.no, null).show();
    }

    public Intent setIntentValue(Intent intent) {
        intent.putExtra("cryptoflag", 1);
        if (!CryptoMessageUtil.isCryptoSmsEnabled() || !isSwitchVisible() || !CryptoMessageUtil.isSmsEncryptionSwitchOn(this.mComposeHolder)) {
            return intent;
        }
        intent.putExtra("recipents", CryptoMessageUtil.formatRecipients(this.mComposeHolder.getRecipients()));
        intent.putExtra("cryptoflag", 0);
        return intent;
    }

    public void processCryptoDtatus(Intent intent) {
        if (CryptoMessageUtil.isCryptoSmsEnabled()) {
            this.mRecipents = intent.getStringExtra("recipents");
            this.mCryptoFlag = intent.getIntExtra("cryptoflag", 1);
        }
    }

    public void updateCryptoStateFullScreen(Activity activity) {
        final Context contextFinal = activity.getBaseContext();
        if (CryptoMessageUtil.isSmsEncryptionSwitchOn(activity.getBaseContext(), this.mRecipents) && this.mCryptoFlag == 0) {
            new Builder(activity).setTitle(R.string.mms_remind_title).setMessage(R.string.encrypted_esms_notify_supertext).setPositiveButton(R.string.encrypted_esms_user_know, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int buttonId) {
                    CryptoComposeMessage.this.mWasSwitchOnWhenHandleSupertext = true;
                    CryptoMessageUtil.setSmsEncryptionSwitchState(contextFinal, CryptoComposeMessage.this.mRecipents, false);
                    dialog.dismiss();
                }
            }).setCancelable(false).show();
        }
    }

    public void setmComposeHolder(ICryptoComposeHolder mComposeHolder) {
        this.mComposeHolder = mComposeHolder;
    }
}
