package com.android.rcs.ui;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.RemoteException;
import android.text.TextUtils;
import android.util.Log;
import com.android.rcs.RcsCommonConfig;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.commonInterface.IfMsgplus;
import com.huawei.rcs.utils.RcsProfile;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcseMmsExt;
import com.huawei.rcs.utils.RcseMmsExt.SendModeSetListener;

public class RcsMessageFullScreenFragment {
    private boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private Context mContext;
    private String mGroupId = "";
    private IMessageFullScreenHolder mHolder;
    private boolean mIsGroupChat = false;
    private boolean mLoginStatus = false;
    private boolean mPreIsEmpty = false;
    private RcsLoginStatusChangeBroadCastReceiver mRcsLoginStatusChangeBroadCastReceiver;
    private SendModeSetListener mSendModeListener = new SendModeSetListener() {
        public void onSendModeSet(boolean isRcsMode, boolean isSendModeLocked) {
            RcsMessageFullScreenFragment.this.updataTextCountView(isRcsMode);
        }

        public int autoSetSendMode(boolean ignoreCapTimeOut, boolean ignoreLoginStatus) {
            int retVal = 0;
            if (RcsProfile.isRcsServiceEnabledAndUserLogin() || ignoreLoginStatus) {
                boolean isImAvailable = RcsProfile.isImAvailable(RcsMessageFullScreenFragment.this.mSendPhoneNumber, ignoreCapTimeOut);
                MLog.d("RcsMessageFullScreenFragment", "autoSetSendMode isImAvailable:" + isImAvailable);
                if (isImAvailable) {
                    retVal = 1;
                }
            }
            MLog.d("RcsMessageFullScreenFragment", "autoSetSendMode return:" + retVal);
            return retVal;
        }
    };
    private String mSendPhoneNumber;

    public interface IMessageFullScreenHolder {
        void updataMmsTextCountView(int i);

        void updataSmsTextCountView(int i);

        void updateHintText(int i);

        void updateSendButton(boolean z, boolean z2);
    }

    private class RcsLoginStatusChangeBroadCastReceiver extends BroadcastReceiver {
        private RcsLoginStatusChangeBroadCastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            if (!(intent == null || intent.getExtras() == null)) {
                if (1 == intent.getExtras().getInt("new_status")) {
                    RcsMessageFullScreenFragment.this.mLoginStatus = true;
                } else {
                    RcsMessageFullScreenFragment.this.mLoginStatus = false;
                }
            }
            RcsMessageFullScreenFragment.this.updateSendButtonForGroupChat();
        }
    }

    public RcsMessageFullScreenFragment(Context context) {
        this.mContext = context;
    }

    public void onCreate(Intent intent) {
        if (this.isRcsOn) {
            this.mSendPhoneNumber = intent.getStringExtra("phonenumber");
            this.mIsGroupChat = intent.getBooleanExtra("is_groupchat", false);
            this.mGroupId = intent.getStringExtra("group_id");
        }
    }

    public void initViews() {
        if (this.isRcsOn) {
            if (this.mIsGroupChat) {
                this.mHolder.updataSmsTextCountView(8);
            } else {
                updataTextCountView(RcseMmsExt.isRcsMode());
            }
        }
    }

    public void onStart() {
        if (this.isRcsOn) {
            if (!this.mIsGroupChat && this.mSendModeListener != null) {
                RcseMmsExt.registerSendModeSetListener(this.mSendModeListener);
            } else if (this.mContext != null) {
                if (RcsCommonConfig.isRCSSwitchOn() && this.mRcsLoginStatusChangeBroadCastReceiver == null) {
                    this.mRcsLoginStatusChangeBroadCastReceiver = new RcsLoginStatusChangeBroadCastReceiver();
                }
                if (this.mRcsLoginStatusChangeBroadCastReceiver != null) {
                    this.mContext.registerReceiver(this.mRcsLoginStatusChangeBroadCastReceiver, new IntentFilter("com.huawei.rcs.loginstatus"), "com.huawei.rcs.RCS_BROADCASTER", null);
                }
            }
        }
    }

    public void onStop() {
        if (this.isRcsOn) {
            if (!this.mIsGroupChat && this.mSendModeListener != null) {
                RcseMmsExt.unRegisterSendModeSetListener(this.mSendModeListener);
            } else if (!(this.mContext == null || this.mRcsLoginStatusChangeBroadCastReceiver == null)) {
                this.mContext.unregisterReceiver(this.mRcsLoginStatusChangeBroadCastReceiver);
            }
        }
    }

    public void onResume() {
        if (this.isRcsOn) {
            setLoginStatus();
            updateSendButtonForGroupChat();
            updataSendStateForRcs();
            updateTextCountForRcs();
        }
    }

    public void setHolder(IMessageFullScreenHolder holder) {
        if (this.isRcsOn) {
            this.mHolder = holder;
        }
    }

    public void onSendClick() {
        if (this.isRcsOn) {
            RcsTransaction.checkValidityTimeAndSendCapRequest(this.mSendPhoneNumber);
        }
    }

    public boolean updataSendStateForRcs() {
        if (!this.isRcsOn || this.mHolder == null) {
            return false;
        }
        if (this.mIsGroupChat) {
            updateSendButtonForGroupChat();
            return true;
        } else if (!RcseMmsExt.isRcsMode()) {
            return false;
        } else {
            this.mHolder.updateSendButton(true, true);
            return true;
        }
    }

    public void onTextChangSendCapRequest(CharSequence s) {
        if (this.isRcsOn) {
            boolean curIsEmpty = TextUtils.isEmpty(s);
            boolean sendOption = this.mPreIsEmpty && !curIsEmpty;
            this.mPreIsEmpty = curIsEmpty;
            if (sendOption) {
                RcsTransaction.checkValidityTimeAndSendCapRequest(this.mSendPhoneNumber);
            }
        }
    }

    public boolean isRcsMessage() {
        if (!this.isRcsOn) {
            return false;
        }
        boolean isRcsMessage = false;
        if (this.mIsGroupChat) {
            isRcsMessage = true;
        } else if (RcseMmsExt.isRcsMode()) {
            isRcsMessage = true;
        }
        if (this.mHolder != null) {
            this.mHolder.updateHintText(isRcsMessage ? R.string.type_to_compose_im_text_enter_to_send_new_rcs : R.string.type_to_compose_text_enter_to_send_new_sms);
        }
        return isRcsMessage;
    }

    public boolean updateTextCountForRcs() {
        if (!this.isRcsOn || this.mHolder == null) {
            return false;
        }
        if (this.mIsGroupChat) {
            this.mHolder.updataSmsTextCountView(8);
            this.mHolder.updataMmsTextCountView(8);
            return true;
        } else if (!RcseMmsExt.isRcsMode()) {
            return false;
        } else {
            this.mHolder.updataSmsTextCountView(8);
            this.mHolder.updataMmsTextCountView(8);
            return true;
        }
    }

    public void configLocalBroadcastIntent(Intent intent) {
        if (this.isRcsOn) {
            intent.putExtra("is_groupchat", this.mIsGroupChat);
        }
    }

    private void setLoginStatus() {
        IfMsgplus aMsgPlus = RcsProfile.getRcsService();
        if (aMsgPlus != null) {
            try {
                this.mLoginStatus = aMsgPlus.getLoginState();
            } catch (RemoteException e) {
                Log.e("RcsMessageFullScreenFragment", "getLoginState fail");
            }
        }
    }

    private void updataTextCountView(boolean isRcsMode) {
        if (this.mHolder != null) {
            if (isRcsMode) {
                this.mHolder.updataSmsTextCountView(8);
                this.mHolder.updateHintText(R.string.type_to_compose_im_text_enter_to_send_new_rcs);
            } else {
                this.mHolder.updataSmsTextCountView(0);
                this.mHolder.updateHintText(R.string.type_to_compose_text_enter_to_send_new_sms);
            }
        }
    }

    private void updateSendButtonForGroupChat() {
        if (this.mHolder != null && this.mIsGroupChat) {
            if (!this.mLoginStatus || RcsGroupChatComposeMessageFragment.isExitRcsGroupEnable()) {
                this.mHolder.updateSendButton(false, false);
            } else if (RcsProfile.canProcessGroupChat(this.mGroupId)) {
                this.mHolder.updateSendButton(true, true);
            } else {
                this.mHolder.updateSendButton(false, false);
            }
        }
    }
}
