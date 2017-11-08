package com.android.settings.fingerprint;

import android.app.Activity;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.res.Resources.Theme;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.android.settings.SetupWizardUtils;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupFingerprintEnrollEnrolling extends FingerprintEnrollEnrolling implements NavigationBarListener {

    public static class SkipDialog extends DialogFragment {
        public void show(FragmentManager manager, String tag) {
            if (manager.findFragmentByTag(tag) == null) {
                super.show(manager, tag);
            }
        }

        public Dialog onCreateDialog(Bundle savedInstanceState) {
            Dialog dialog = new Builder(getActivity()).setTitle(2131624672).setMessage(2131624673).setCancelable(false).setPositiveButton(2131625014, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                    Activity activity = SkipDialog.this.getActivity();
                    if (activity != null) {
                        activity.setResult(2);
                        activity.finish();
                    }
                }
            }).setNegativeButton(2131625015, new OnClickListener() {
                public void onClick(DialogInterface dialog, int id) {
                }
            }).create();
            SystemBarHelper.hideSystemBars(dialog);
            return dialog;
        }
    }

    protected Intent getFinishIntent() {
        Intent intent = new Intent(this, SetupFingerprintEnrollFinish.class);
        SetupWizardUtils.copySetupExtras(getIntent(), intent);
        return intent;
    }

    protected void onApplyThemeResource(Theme theme, int resid, boolean first) {
        super.onApplyThemeResource(theme, SetupWizardUtils.getTheme(getIntent()), first);
    }

    protected void initViews() {
        SetupWizardUtils.setImmersiveMode(this);
        View buttonBar = findViewById(2131886327);
        if (buttonBar != null) {
            buttonBar.setVisibility(8);
        }
        NavigationBar navigationBar = getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        navigationBar.getNextButton().setText(2131624550);
        navigationBar.getBackButton().setVisibility(8);
    }

    protected Button getNextButton() {
        return getNavigationBar().getNextButton();
    }

    public void onNavigateBack() {
        onBackPressed();
    }

    public void onNavigateNext() {
        new SkipDialog().show(getFragmentManager(), "dialog");
    }

    protected int getMetricsCategory() {
        return 246;
    }
}
