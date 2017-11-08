package com.android.settings.wifi;

import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiConfiguration;
import android.os.Bundle;
import android.preference.PreferenceFrameLayout;
import android.support.v7.appcompat.R$id;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.LinearLayout.LayoutParams;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.SetupWizardUtils;
import com.android.settings.Utils;

public class WifiSettingsForSetupWizard extends WifiSettings {
    private Dialog mDialog;
    private boolean mFRPisLocked = false;
    private View mLayout;

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        getActivity().setTitle(2131624906);
        this.mLayout = Utils.getCustemPreferenceContainer(inflater, 2130969119, container, super.onCreateView(inflater, container, savedInstanceState));
        ((TextView) this.mLayout.findViewById(R$id.title)).setText(2131628933);
        Intent intent = getActivity().getIntent();
        getFRPIsLocked(intent, savedInstanceState);
        setLogoViewHeight(this.mLayout);
        TextView backButton = (TextView) this.mLayout.findViewById(2131886328);
        backButton.setVisibility(0);
        backButton.setOnClickListener(new OnClickListener() {
            public void onClick(View v) {
                WifiSettingsForSetupWizard.this.getActivity().setResult(0);
                WifiSettingsForSetupWizard.this.getActivity().finish();
            }
        });
        TextView customButton = (TextView) this.mLayout.findViewById(2131886329);
        customButton.setVisibility(0);
        if (WifiExtUtils.isWifiConnected(getActivity())) {
            customButton.setText(2131626195);
        } else {
            customButton.setText(2131626194);
        }
        if ((intent == null || !intent.getBooleanExtra("wifi_show_custom_button", false)) && (this.mHwCustWifiSettingsHwBase == null || !this.mHwCustWifiSettingsHwBase.isShowSkipWifiSettingDialog())) {
            customButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (!WifiSettingsForSetupWizard.this.mFRPisLocked || WifiExtUtils.isWifiConnected(WifiSettingsForSetupWizard.this.getActivity())) {
                        WifiSettingsForSetupWizard.this.getActivity().setResult(-1);
                        WifiSettingsForSetupWizard.this.getActivity().finish();
                        return;
                    }
                    WifiSettingsForSetupWizard.this.showDialog();
                }
            });
        } else {
            customButton.setOnClickListener(new OnClickListener() {
                public void onClick(View v) {
                    if (WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase == null || !WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase.dontShowWifiSkipDialog()) {
                        boolean isConnected = false;
                        ConnectivityManager connectivity = (ConnectivityManager) WifiSettingsForSetupWizard.this.getActivity().getSystemService("connectivity");
                        if (connectivity != null) {
                            if (connectivity.getNetworkInfo(1).isConnected()) {
                                WifiSettingsForSetupWizard.this.getActivity().setResult(-1);
                                WifiSettingsForSetupWizard.this.getActivity().finish();
                                return;
                            }
                            NetworkInfo info = connectivity.getActiveNetworkInfo();
                            isConnected = info != null ? info.isConnected() : false;
                        }
                        if (WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase == null || !WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase.isSkipWifiSettingWithNoPrompt()) {
                            if (isConnected || (WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase != null && WifiSettingsForSetupWizard.this.mHwCustWifiSettingsHwBase.isShowDataCostTip())) {
                                WifiSettingsForSetupWizard.this.showDialog(4);
                            } else {
                                WifiSettingsForSetupWizard.this.showDialog(5);
                            }
                            return;
                        }
                        return;
                    }
                    WifiSettingsForSetupWizard.this.getActivity().setResult(1);
                    WifiSettingsForSetupWizard.this.getActivity().finish();
                }
            });
        }
        if (intent != null && intent.getBooleanExtra("wifi_show_wifi_required_info", false)) {
            this.mLayout.findViewById(2131887163).setVisibility(0);
        }
        return this.mLayout;
    }

    private void getFRPIsLocked(Intent intent, Bundle outState) {
        if (outState != null) {
            this.mFRPisLocked = outState.getBoolean("frp_is_lock");
            Log.i("HwStartupGuide", "getFRPIsLocked outState mFRPisLocked is " + this.mFRPisLocked);
            if (outState.getBoolean("dialog_showing")) {
                showDialog();
            }
        }
        if (intent != null) {
            this.mFRPisLocked = intent.getBooleanExtra("frp_is_lock", false);
            Log.i("HwStartupGuide", "getFRPIsLocked intent mFRPisLocked is " + this.mFRPisLocked);
        }
    }

    public void onSaveInstanceState(Bundle outState) {
        outState.putBoolean("frp_is_lock", this.mFRPisLocked);
        Log.i("HwStartupGuide", "onSaveInstanceState outState mFRPisLocked is " + this.mFRPisLocked);
        if (this.mDialog != null && this.mDialog.isShowing()) {
            outState.putBoolean("dialog_showing", true);
        }
        super.onSaveInstanceState(outState);
    }

    private void showDialog() {
        if (this.mDialog == null || !this.mDialog.isShowing()) {
            this.mDialog = createDialog();
            this.mDialog.show();
        }
    }

    private void dismissDialog() {
        if (this.mDialog != null && this.mDialog.isShowing()) {
            this.mDialog.dismiss();
        }
    }

    private Dialog createDialog() {
        String positiveButtonText = getString(2131627945);
        Builder builder = new Builder(getActivity());
        builder.setTitle(2131627350);
        builder.setMessage(2131628957);
        builder.setPositiveButton(positiveButtonText, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                ItemUseStat.getInstance().handleClick(WifiSettingsForSetupWizard.this.getActivity(), 20, "");
                WifiSettingsForSetupWizard.this.dismissDialog();
                WifiSettingsForSetupWizard.this.getActivity().setResult(-1);
                WifiSettingsForSetupWizard.this.getActivity().finish();
            }
        });
        this.mDialog = builder.create();
        this.mDialog.setCanceledOnTouchOutside(false);
        return this.mDialog;
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        if (hasNextButton()) {
            getNextButton().setVisibility(8);
        }
        PreferenceFrameLayout frameLayout = (PreferenceFrameLayout) getActivity().findViewById(16909261);
        if (frameLayout != null) {
            frameLayout.setPadding(0, frameLayout.getPaddingTop(), 0, frameLayout.getPaddingBottom());
        }
    }

    public void onAccessPointsChanged() {
        super.onAccessPointsChanged();
    }

    public void onWifiStateChanged(int state) {
        super.onWifiStateChanged(state);
    }

    public void registerForContextMenu(View view) {
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    }

    public Dialog onCreateDialog(int dialogId) {
        Dialog dialog = super.onCreateDialog(dialogId);
        SetupWizardUtils.applyImmersiveFlags(dialog);
        return dialog;
    }

    protected void connect(WifiConfiguration config) {
        ((WifiSetupActivity) getActivity()).networkSelected();
        super.connect(config);
    }

    protected void connect(int networkId) {
        ((WifiSetupActivity) getActivity()).networkSelected();
        super.connect(networkId);
    }

    public View setPinnedHeaderView(int layoutResId) {
        return null;
    }

    public void setPinnedHeaderView(View pinnedHeader) {
    }

    public void onCreate(Bundle icicle) {
        this.mSetupWizardMode = true;
        super.onCreate(icicle);
    }

    private void setLogoViewHeight(View root) {
        if (root != null) {
            View logoView = root.findViewById(2131886352);
            View logoContainer = root.findViewById(2131886353);
            if (logoView != null && logoContainer != null) {
                DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
                int logoViewHeight;
                if (!Utils.isTablet()) {
                    logoViewHeight = (displayMetrics.widthPixels * 4) / 5;
                    logoView.getLayoutParams().height = logoViewHeight;
                    ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 4) / 25;
                } else if (2 == getResources().getConfiguration().orientation) {
                    logoViewHeight = (displayMetrics.widthPixels * 268) / 1000;
                    logoView.getLayoutParams().height = logoViewHeight;
                    ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 13) / 100;
                } else {
                    logoViewHeight = (displayMetrics.widthPixels * 62) / 100;
                    logoView.getLayoutParams().height = logoViewHeight;
                    ((LayoutParams) logoContainer.getLayoutParams()).topMargin = (logoViewHeight * 38) / 100;
                }
            }
        }
    }
}
