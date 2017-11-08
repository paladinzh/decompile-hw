package com.huawei.mms.crypto.account.ui;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import cn.com.xy.sms.sdk.action.NearbyPoint;
import com.google.android.gms.R;
import com.huawei.cloudservice.CloudAccount;
import com.huawei.cspcommon.MLog;
import com.huawei.hwid.core.helper.handler.ErrorStatus;
import com.huawei.hwid.ui.common.password.FindpwdByHwIdActivity;
import com.huawei.mms.crypto.account.AccountCheckHandler;
import com.huawei.mms.crypto.account.AccountManager;

public class PwdCheckActivity extends Activity implements PwdCheckListener {
    private String mAccountName;
    private CheckFragment mFragment;
    private Handler mMainHandler = new Handler() {
        public void handleMessage(Message msg) {
            MLog.i("PwdCheckActivity", "main handler, handler message id is: " + msg.what);
            switch (msg.what) {
                case 1:
                    PwdCheckActivity.this.finish();
                    break;
                case 2:
                    PwdCheckActivity.this.dismissDialogSafely(1);
                    if (PwdCheckActivity.this.mFragment != null) {
                        PwdCheckActivity.this.mFragment.setDialogCanDismiss(true);
                    }
                    if (msg.obj instanceof ErrorStatus) {
                        PwdCheckActivity.this.showVerifyErrorTip(msg.obj);
                        break;
                    }
                    MLog.e("PwdCheckActivity", "handle MESSAGE_VERIRY_PASSWORD_FAILED message, invalid params");
                    return;
            }
        }
    };

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(1);
        initFragment();
    }

    private void initFragment() {
        Intent intent = getIntent();
        this.mAccountName = intent.getStringExtra("account_name");
        int requestType = intent.getIntExtra("request_type", 1);
        MLog.d("PwdCheckActivity", "initFragment, requestType is: " + requestType);
        if (1 == requestType) {
            this.mFragment = new PwdCheckFragment(this.mAccountName);
            this.mFragment.show(getFragmentManager(), "PwdCheckActivity");
            return;
        }
        this.mFragment = new DeactivatePwdCheckFragment(this.mAccountName);
        this.mFragment.show(getFragmentManager(), "PwdCheckActivity");
    }

    protected Dialog onCreateDialog(int id) {
        if (1 == id) {
            return createVerifyProgessDialog();
        }
        return super.onCreateDialog(id);
    }

    private Dialog createVerifyProgessDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        dialog.setMessage(getString(R.string.CS_verify_waiting_progress_message));
        return dialog;
    }

    private void dismissDialogSafely(int id) {
        try {
            dismissDialog(id);
        } catch (IllegalArgumentException e) {
            MLog.e("PwdCheckActivity", "invalid params when dismissDialog");
        }
    }

    private void showVerifyErrorTip(ErrorStatus errorStatus) {
        if (errorStatus != null) {
            int errorCode = errorStatus.getErrorCode();
            String errorMsg = errorStatus.getErrorReason();
            MLog.d("PwdCheckActivity", "showVerifyErrorTip, the errorCode is: " + errorCode);
            MLog.d("PwdCheckActivity", "showVerifyErrorTip, the errorMsg is: " + errorMsg);
            switch (errorCode) {
                case NearbyPoint.QUERY_RESULT_RECEIVE /*4097*/:
                    createCommonDialog((int) R.string.CS_server_unavailable_title, (int) R.string.CS_server_unavailable_message).show();
                    break;
                case NearbyPoint.QUERY_PARAM_ERROR /*4098*/:
                    createCommonDialog((int) R.string.CS_notification, errorMsg).show();
                    break;
                case NearbyPoint.GET_QUERY_URL_FAILURE /*4099*/:
                    Toast.makeText(this, R.string.CS_account_change, 1).show();
                    break;
                case 70001201:
                case 70002003:
                    if (this.mFragment != null) {
                        this.mFragment.handlePwdError();
                        break;
                    }
                    break;
                case 70002044:
                    createDeviceOverDialog(errorMsg).show();
                    break;
                default:
                    createCommonErrorDialog(errorMsg).show();
                    break;
            }
        }
    }

    private Builder createDeviceOverDialog(String message) {
        View dialogView = View.inflate(this, R.layout.error_weblink_dialog, null);
        ((TextView) dialogView.findViewById(R.id.text)).setText(message);
        return createCommonErrorDialog("").setView(dialogView);
    }

    private Builder createCommonErrorDialog(String message) {
        return createCommonDialog((int) R.string.CS_prompt_dialog_title, message);
    }

    private Builder createCommonDialog(int titleId, int messageId) {
        return createCommonDialog(titleId, getString(messageId));
    }

    private Builder createCommonDialog(int titleId, String message) {
        Builder builder = new Builder(this);
        builder.setMessage(message);
        builder.setTitle(titleId);
        builder.setPositiveButton(17039370, null);
        return builder;
    }

    public void onForgetPwd() {
        MLog.i("PwdCheckActivity", "onForgetPwd");
        Intent intent = new Intent();
        intent.putExtra("userAccount", this.mAccountName);
        intent.setClass(this, FindpwdByHwIdActivity.class);
        startActivity(intent);
    }

    public void onPwdVerifyStart(String pwd) {
        MLog.i("PwdCheckActivity", "onPwdVerifyStart");
        showDialog(1);
        AccountCheckHandler cloudRequestHandler = AccountManager.getInstance().getAccountCheckHandler();
        if (cloudRequestHandler != null) {
            cloudRequestHandler.setMainHandler(this.mMainHandler);
            cloudRequestHandler.setPwd(pwd);
        }
        CloudAccount.checkPassWord(this, this.mAccountName, pwd, null, String.valueOf(32), cloudRequestHandler, new Bundle());
    }

    public void onPwdVerifyStart(String accountName, String pwd) {
        MLog.i("PwdCheckActivity", "onPwdVerifyStart");
        showDialog(1);
        AccountCheckHandler cloudRequestHandler = AccountManager.getInstance().getAccountCheckHandler();
        if (cloudRequestHandler != null) {
            cloudRequestHandler.setMainHandler(this.mMainHandler);
            cloudRequestHandler.setPwd(pwd);
            cloudRequestHandler.setAccountName(accountName);
        }
        CloudAccount.checkPassWord(this, accountName, pwd, null, String.valueOf(32), cloudRequestHandler, new Bundle());
    }

    protected void onStop() {
        super.onStop();
    }
}
