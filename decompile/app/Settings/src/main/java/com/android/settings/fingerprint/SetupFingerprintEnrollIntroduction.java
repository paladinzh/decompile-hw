package com.android.settings.fingerprint;

import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.UserHandle;
import android.widget.Button;
import com.android.internal.widget.LockPatternUtils;
import com.android.settings.SetupChooseLockGeneric;
import com.android.settings.SetupWizardUtils;
import com.android.setupwizardlib.R$dimen;
import com.android.setupwizardlib.SetupWizardRecyclerLayout;
import com.android.setupwizardlib.items.Item;
import com.android.setupwizardlib.items.RecyclerItemAdapter;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupFingerprintEnrollIntroduction extends FingerprintEnrollIntroduction implements NavigationBarListener {
    protected Intent getChooseLockIntent() {
        Intent intent = new Intent(this, SetupChooseLockGeneric.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    protected Intent getFindSensorIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFindSensor.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    protected void initViews() {
        SetupWizardRecyclerLayout layout = (SetupWizardRecyclerLayout) findViewById(2131886616);
        RecyclerItemAdapter adapter = (RecyclerItemAdapter) layout.getAdapter();
        ((Item) adapter.findItemById(2131886371)).setTitle(getText(2131624653));
        ((Item) adapter.findItemById(2131886370)).setTitle(getText(2131624652));
        SetupWizardUtils.setImmersiveMode(this);
        getNavigationBar().setNavigationBarListener(this);
        Button nextButton = getNavigationBar().getNextButton();
        nextButton.setText(null);
        nextButton.setEnabled(false);
        layout.setDividerInset(getResources().getDimensionPixelSize(R$dimen.suw_items_icon_divider_inset));
    }

    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 2) {
            if (data == null) {
                data = new Intent();
            }
            data.putExtra(":settings:password_quality", new LockPatternUtils(this).getKeyguardStoredPasswordQuality(UserHandle.myUserId()));
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    protected void onCancelButtonClick() {
        SetupSkipDialog.newInstance(getIntent().getBooleanExtra(":settings:frp_supported", false)).show(getFragmentManager());
    }

    public void onNavigateBack() {
        onBackPressed();
    }

    public void onNavigateNext() {
    }

    protected int getMetricsCategory() {
        return 249;
    }
}
