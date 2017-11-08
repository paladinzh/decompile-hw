package com.android.settings;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class CheckPasswordAndRedirectActivity extends Activity {
    private String mTargetAction;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.mTargetAction = getIntent().getStringExtra("target_action");
        Log.d("CheckPwdAndRedirect", "mTargetAction:" + this.mTargetAction);
        if (!new ChooseLockSettingsHelper(this).launchConfirmationActivity(10125, getString(2131624724))) {
            startTargetActivityAndFinish();
        }
    }

    private void startTargetActivityAndFinish() {
        Intent intent = new Intent(this.mTargetAction);
        if ("huawei.intent.action.TRUST_AGENT_SETTINGS".equals(this.mTargetAction)) {
            intent.setPackage("com.huawei.trustagent");
        }
        if ("huawei.intent.action.ENTER_PARENT_HOMEACTIVITY".equals(this.mTargetAction)) {
            intent.setPackage("com.huawei.parentcontrol");
            Bundle bundle = new Bundle();
            bundle.putString("target_action", "huawei.intent.action.ENTER_PARENT_HOMEACTIVITY");
            intent.putExtras(bundle);
        }
        try {
            startActivity(intent);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            finish();
        }
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 10125 && resultCode == -1) {
            startTargetActivityAndFinish();
        } else {
            finish();
        }
    }
}
