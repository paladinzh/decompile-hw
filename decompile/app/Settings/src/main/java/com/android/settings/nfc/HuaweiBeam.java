package com.android.settings.nfc;

import android.app.Activity;
import android.graphics.drawable.AnimationDrawable;
import android.nfc.NfcAdapter;
import android.os.Bundle;
import android.provider.Settings.System;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.Switch;
import android.widget.TextView;
import com.android.settings.ItemUseStat;
import com.android.settings.SettingsPreferenceFragment;
import com.android.settings.Utils;

public class HuaweiBeam extends SettingsPreferenceFragment implements OnCheckedChangeListener {
    private Activity mActivity;
    private Switch mBeamSwitch;
    private AnimationDrawable mFrameAnimation;
    private boolean mIsChecked;
    public boolean mIsFrontChipNfc;
    private NfcAdapter mNfcAdapter;
    private View mView;

    public void onCreate(Bundle savedInstanceState) {
        boolean z = true;
        super.onCreate(savedInstanceState);
        this.mActivity = getActivity();
        this.mNfcAdapter = NfcAdapter.getDefaultAdapter(this.mActivity);
        ItemUseStat.getInstance().handleClick(this.mActivity, 2, "huaweiBeam clicked");
        if (System.getInt(this.mActivity.getContentResolver(), "show_nfc_foreground_tip", 0) != 1) {
            z = false;
        }
        this.mIsFrontChipNfc = z;
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mView = inflater.inflate(2130968830, container, false);
        Utils.prepareCustomPreferencesList(container, this.mView, this.mView, true);
        initView(this.mView);
        return this.mView;
    }

    public void onResume() {
        super.onResume();
        if (this.mFrameAnimation != null) {
            this.mFrameAnimation.start();
        }
    }

    public void onPause() {
        super.onPause();
        if (this.mFrameAnimation != null) {
            this.mFrameAnimation.stop();
        }
        ItemUseStat.getInstance().cacheData(this.mActivity);
    }

    private void initView(View view) {
        this.mBeamSwitch = (Switch) view.findViewById(2131886228);
        if (this.mNfcAdapter != null) {
            this.mBeamSwitch.setChecked(1 == System.getInt(getActivity().getContentResolver(), "androidBeam", 1));
        }
        this.mBeamSwitch.setOnCheckedChangeListener(this);
        ImageView beamImage = (ImageView) view.findViewById(2131886226);
        if (this.mIsFrontChipNfc) {
            ((TextView) view.findViewById(2131886227)).setText(2131627724);
            beamImage.setBackgroundResource(2130838165);
            this.mFrameAnimation = (AnimationDrawable) beamImage.getBackground();
            return;
        }
        beamImage.setImageResource(2130837620);
    }

    public void onCheckedChanged(CompoundButton buttonView, boolean desiredState) {
        if (2131886225 == buttonView.getId()) {
            ItemUseStat.getInstance().handleClick(this.mActivity, 2, "beam_switch");
        }
        this.mIsChecked = desiredState;
        if (this.mNfcAdapter == null) {
            return;
        }
        if (this.mIsChecked) {
            if (this.mNfcAdapter.enableNdefPush()) {
                this.mBeamSwitch.setChecked(desiredState);
                ItemUseStat.getInstance().handleClick(this.mActivity, 3, "Huawe beam status", "on");
                System.putInt(getActivity().getContentResolver(), "androidBeam", 1);
            }
        } else if (this.mNfcAdapter.disableNdefPush()) {
            this.mBeamSwitch.setChecked(desiredState);
            ItemUseStat.getInstance().handleClick(this.mActivity, 3, "Huawe beam status", "off");
            System.putInt(getActivity().getContentResolver(), "androidBeam", 0);
        }
    }

    protected int getMetricsCategory() {
        return 69;
    }
}
