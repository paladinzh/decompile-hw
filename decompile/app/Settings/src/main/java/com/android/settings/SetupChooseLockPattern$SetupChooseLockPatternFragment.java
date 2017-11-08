package com.android.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.settings.ChooseLockPattern.ChooseLockPatternFragment;
import com.android.setupwizardlib.SetupWizardLayout;
import com.android.setupwizardlib.view.NavigationBar;
import com.android.setupwizardlib.view.NavigationBar.NavigationBarListener;

public class SetupChooseLockPattern$SetupChooseLockPatternFragment extends ChooseLockPatternFragment implements NavigationBarListener {
    private static final /* synthetic */ int[] -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues = null;
    private NavigationBar mNavigationBar;
    private Button mRetryButton;

    private static /* synthetic */ int[] -getcom-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues() {
        if (-com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues != null) {
            return -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues;
        }
        int[] iArr = new int[Stage.values().length];
        try {
            iArr[Stage.ChoiceConfirmed.ordinal()] = 1;
        } catch (NoSuchFieldError e) {
        }
        try {
            iArr[Stage.ChoiceTooShort.ordinal()] = 2;
        } catch (NoSuchFieldError e2) {
        }
        try {
            iArr[Stage.ConfirmWrong.ordinal()] = 3;
        } catch (NoSuchFieldError e3) {
        }
        try {
            iArr[Stage.FirstChoiceValid.ordinal()] = 4;
        } catch (NoSuchFieldError e4) {
        }
        try {
            iArr[Stage.HelpScreen.ordinal()] = 5;
        } catch (NoSuchFieldError e5) {
        }
        try {
            iArr[Stage.Introduction.ordinal()] = 6;
        } catch (NoSuchFieldError e6) {
        }
        try {
            iArr[Stage.NeedToConfirm.ordinal()] = 7;
        } catch (NoSuchFieldError e7) {
        }
        -com-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues = iArr;
        return iArr;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        SetupWizardLayout layout = (SetupWizardLayout) inflater.inflate(2130969113, container, false);
        this.mNavigationBar = layout.getNavigationBar();
        this.mNavigationBar.setNavigationBarListener(this);
        layout.setHeaderText(getActivity().getTitle());
        return layout;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        this.mRetryButton = (Button) view.findViewById(2131887160);
        this.mRetryButton.setOnClickListener(this);
        super.onViewCreated(view, savedInstanceState);
        SetupWizardUtils.setImmersiveMode(getActivity());
    }

    public void onClick(View v) {
        if (v == this.mRetryButton) {
            handleLeftButton();
        } else {
            super.onClick(v);
        }
    }

    protected void setRightButtonEnabled(boolean enabled) {
        this.mNavigationBar.getNextButton().setEnabled(enabled);
    }

    protected void updateStage(Stage stage) {
        boolean z;
        super.updateStage(stage);
        Button button = this.mRetryButton;
        if (stage == Stage.FirstChoiceValid) {
            z = true;
        } else {
            z = false;
        }
        button.setEnabled(z);
        switch (-getcom-android-settings-ChooseLockPattern$ChooseLockPatternFragment$StageSwitchesValues()[stage.ordinal()]) {
            case 1:
            case 3:
            case 7:
                this.mRetryButton.setVisibility(4);
                return;
            case 2:
            case 4:
            case 5:
            case 6:
                this.mRetryButton.setVisibility(0);
                return;
            default:
                return;
        }
    }

    public void onNavigateBack() {
        Activity activity = getActivity();
        if (activity != null) {
            activity.onBackPressed();
        }
    }

    public void onNavigateNext() {
        handleRightButton();
    }
}
