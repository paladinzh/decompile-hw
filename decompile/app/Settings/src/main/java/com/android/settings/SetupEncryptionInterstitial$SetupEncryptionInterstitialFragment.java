package com.android.settings;

import android.app.Activity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import com.android.settings.EncryptionInterstitial.EncryptionInterstitialFragment;
import com.android.setupwizardlib.R$dimen;
import com.android.setupwizardlib.SetupWizardPreferenceLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupEncryptionInterstitial$SetupEncryptionInterstitialFragment extends EncryptionInterstitialFragment implements NavigationBarListener {
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SetupWizardPreferenceLayout layout = (SetupWizardPreferenceLayout) view;
        layout.setDividerInset(getContext().getResources().getDimensionPixelSize(R$dimen.suw_items_icon_divider_inset));
        layout.setIllustration(2130838643, 2130838642);
        NavigationBar navigationBar = layout.getNavigationBar();
        navigationBar.setNavigationBarListener(this);
        Button nextButton = navigationBar.getNextButton();
        nextButton.setText(null);
        nextButton.setEnabled(false);
        layout.setHeaderText(2131626868);
        Activity activity = getActivity();
        if (activity != null) {
            SetupWizardUtils.setImmersiveMode(activity);
        }
        setDivider(null);
    }

    protected TextView createHeaderView() {
        return (TextView) LayoutInflater.from(getActivity()).inflate(2130969116, null, false);
    }

    public RecyclerView onCreateRecyclerView(LayoutInflater inflater, ViewGroup parent, Bundle savedInstanceState) {
        return ((SetupWizardPreferenceLayout) parent).onCreateRecyclerView(inflater, parent, savedInstanceState);
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
