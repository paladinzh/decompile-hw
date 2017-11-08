package com.android.settings.fingerprint;

import android.content.Intent;
import android.os.Bundle;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.InstrumentedActivity;
import com.android.setupwizardlib.R$id;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;

public abstract class FingerprintEnrollBase extends InstrumentedActivity implements OnClickListener {
    protected byte[] mToken;
    protected int mUserId;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTheme(2131755570);
        this.mToken = getIntent().getByteArrayExtra("hw_auth_token");
        if (savedInstanceState != null && this.mToken == null) {
            this.mToken = savedInstanceState.getByteArray("hw_auth_token");
        }
        this.mUserId = getIntent().getIntExtra("android.intent.extra.USER_ID", UserHandle.myUserId());
    }

    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putByteArray("hw_auth_token", this.mToken);
    }

    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        initViews();
    }

    protected void initViews() {
    }

    protected NavigationBar getNavigationBar() {
        return (NavigationBar) findViewById(R$id.suw_layout_navigation_bar);
    }

    protected SetupWizardLayout getSetupWizardLayout() {
        return (SetupWizardLayout) findViewById(2131886616);
    }

    protected void setHeaderText(int resId, boolean force) {
        TextView layoutTitle = getSetupWizardLayout().getHeaderTextView();
        CharSequence previousTitle = layoutTitle.getText();
        CharSequence title = getText(resId);
        if (previousTitle != title || force) {
            if (!TextUtils.isEmpty(previousTitle)) {
                layoutTitle.setAccessibilityLiveRegion(1);
            }
            getSetupWizardLayout().setHeaderText(title);
            setTitle(title);
        }
    }

    protected void setHeaderText(int resId) {
        setHeaderText(resId, false);
    }

    protected Button getNextButton() {
        return (Button) findViewById(2131886371);
    }

    public void onClick(View v) {
        if (v == getNextButton()) {
            onNextButtonClick();
        }
    }

    protected void onNextButtonClick() {
    }

    protected Intent getEnrollingIntent() {
        Intent intent = new Intent();
        intent.setClassName("com.android.settings", FingerprintEnrollEnrolling.class.getName());
        intent.putExtra("hw_auth_token", this.mToken);
        if (this.mUserId != -10000) {
            intent.putExtra("android.intent.extra.USER_ID", this.mUserId);
        }
        return intent;
    }
}
