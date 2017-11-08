package com.android.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import com.android.settings.ChooseLockPassword.ChooseLockPasswordFragment;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.util.SystemBarHelper;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupChooseLockPassword$SetupChooseLockPasswordFragment extends ChooseLockPasswordFragment implements NavigationBarListener {
    private SetupWizardLayout mLayout;
    private NavigationBar mNavigationBar;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mLayout = (SetupWizardLayout) inflater.inflate(2130969112, container, false);
        this.mNavigationBar = this.mLayout.getNavigationBar();
        this.mNavigationBar.setNavigationBarListener(this);
        return this.mLayout;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SystemBarHelper.setImeInsetView(this.mLayout);
        SetupWizardUtils.setImmersiveMode(getActivity());
        this.mLayout.setHeaderText(getActivity().getTitle());
    }

    protected void setNextEnabled(boolean enabled) {
        this.mNavigationBar.getNextButton().setEnabled(enabled);
    }

    protected void setNextText(int text) {
        this.mNavigationBar.getNextButton().setText(text);
    }

    public void onNavigateBack() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    public void onNavigateNext() {
    }
}
