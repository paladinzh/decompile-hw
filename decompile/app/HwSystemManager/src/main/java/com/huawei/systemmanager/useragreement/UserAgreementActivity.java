package com.huawei.systemmanager.useragreement;

import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.internal.view.SupportMenu;
import android.text.SpannableString;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.method.ScrollingMovementMethod;
import android.text.style.ClickableSpan;
import android.view.View;
import android.view.WindowManager.LayoutParams;
import android.widget.CheckBox;
import android.widget.TextView;
import com.android.internal.app.AlertActivity;
import com.android.internal.app.AlertController.AlertParams;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.hsmstat.base.HsmStatConst;
import com.huawei.systemmanager.push.CustomTaskHandler;
import com.huawei.systemmanager.rainbow.CloudSwitchHelper;
import com.huawei.systemmanager.util.HSMConst;
import com.huawei.systemmanager.util.HwLog;

public class UserAgreementActivity extends AlertActivity implements OnClickListener {
    public static final int REQUEST_CODE_USERAGREEMRNT = (UserAgreementActivity.class.hashCode() & SupportMenu.USER_MASK);
    private static final String TAG = UserAgreementActivity.class.getSimpleName();
    public static final int USERAGREEMENT_VERSION = 2;
    private Context context;
    private CheckBox mNotRemind = null;

    private class PrivacyPolicySpan extends ClickableSpan {
        private final int mLinkColor;

        private PrivacyPolicySpan() {
            this.mLinkColor = UserAgreementActivity.this.context.getResources().getColor(R.color.emui5_theme);
        }

        public void onClick(View widget) {
            UserAgreementActivity.this.jumpToOobePage();
        }

        public void updateDrawState(TextPaint ds) {
            super.updateDrawState(ds);
            ds.setColor(this.mLinkColor);
            ds.setUnderlineText(false);
        }
    }

    protected void onCreate(Bundle savedInstanceState) {
        int i;
        super.onCreate(savedInstanceState);
        String dialogTitle = getString(R.string.app_name);
        AlertParams p = this.mAlertParams;
        LayoutParams l = getWindow().getAttributes();
        this.context = GlobalContext.getContext();
        l.y = (int) (HSMConst.DEVICE_SIZE_80 * this.context.getResources().getDisplayMetrics().density);
        if (1 == this.context.getResources().getConfiguration().orientation) {
            i = 80;
        } else {
            i = 17;
        }
        l.gravity = i;
        p.mIconAttrId = 16843605;
        p.mTitle = dialogTitle;
        p.mView = createView();
        p.mPositiveButtonText = getString(R.string.authority_info_confirm_button);
        p.mPositiveButtonListener = this;
        p.mNegativeButtonText = getString(R.string.alert_dialog_cancel);
        p.mNegativeButtonListener = this;
        onAgree(true);
        setResult(-1);
        finish();
        overridePendingTransition(0, 0);
    }

    private View createView() {
        View view = getLayoutInflater().inflate(R.layout.useragreement_layout, null);
        TextView tv = (TextView) view.findViewById(R.id.text1);
        String privacyPolicy = getString(R.string.hw_privacy);
        String policydesr = getString(R.string.private_policy, new Object[]{privacyPolicy});
        int start = policydesr.indexOf(privacyPolicy);
        int end = start + privacyPolicy.length();
        SpannableString sString = new SpannableString(policydesr);
        if (start >= 0 && end <= sString.length()) {
            sString.setSpan(new PrivacyPolicySpan(), start, end, 33);
        }
        TextView tvPolicy = (TextView) view.findViewById(R.id.app_privacypolicy);
        tvPolicy.setText(sString);
        tvPolicy.setMovementMethod(LinkMovementMethod.getInstance());
        tv.setMovementMethod(ScrollingMovementMethod.getInstance());
        if (Utility.isWifiOnlyMode()) {
            tv.setText(getString(R.string.user_agreement2_wifionly));
        } else if (Utility.isDataOnlyMode()) {
            tv.setText(getString(R.string.user_agreement2_dataonly));
        }
        this.mNotRemind = (CheckBox) view.findViewById(R.id.checkbox_notremind);
        return view;
    }

    private void jumpToOobePage() {
        Intent intent = new Intent("com.android.settings.HuaweiPrivacyPolicyActivity");
        intent.setPackage(HsmStatConst.SETTING_PACKAGE_NAME);
        try {
            this.context.startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void onClick(DialogInterface dialog, int which) {
        HwLog.i(TAG, "onClick: which = " + which);
        switch (which) {
            case -2:
                HwLog.i(TAG, "Disagree user agreement");
                onDisagree();
                setResult(0);
                finish();
                overridePendingTransition(0, 0);
                return;
            case -1:
                boolean notRemindAgain = this.mNotRemind.isChecked();
                HwLog.i(TAG, "Agree user agreement , don't remind flag = " + notRemindAgain);
                onAgree(notRemindAgain);
                setResult(-1);
                finish();
                overridePendingTransition(0, 0);
                return;
            default:
                return;
        }
    }

    private void onAgree(boolean notRemindAgain) {
        Context context = GlobalContext.getContext();
        UserAgreementHelper.setUserAgreementBatch(context, true, 2, notRemindAgain);
        UserAgreementHelper.setAgreedOnThisStart();
        UserAgreementHelper.turnOnNetSettings();
        if (CloudSwitchHelper.isCloudEnabled() && !Utility.isTokenRegistered(context)) {
            CustomTaskHandler.getInstance(context).removeMessages(1);
            CustomTaskHandler.getInstance(context).sendEmptyMessageDelayed(1, 60000);
        }
    }

    private void onDisagree() {
        UserAgreementHelper.setUserAgreementState(GlobalContext.getContext(), false);
        UserAgreementHelper.turnOffNetSettings();
    }
}
