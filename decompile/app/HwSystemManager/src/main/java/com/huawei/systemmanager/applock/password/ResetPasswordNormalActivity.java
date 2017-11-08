package com.huawei.systemmanager.applock.password;

public class ResetPasswordNormalActivity extends ResetPasswordActivityBase {
    public void onPostFinish() {
        finish();
    }

    protected void subProcessOnBackPressed() {
        finish();
    }

    protected void subProcessOnPause() {
        finish();
    }
}
