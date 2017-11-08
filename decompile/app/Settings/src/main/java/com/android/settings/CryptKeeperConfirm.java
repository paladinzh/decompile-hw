package com.android.settings;

import android.app.Activity;
import android.app.StatusBarManager;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.ServiceManager;
import android.os.storage.IMountService;
import android.os.storage.IMountService.Stub;
import android.provider.Settings.System;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import com.android.internal.widget.LockPatternUtils;
import com.huawei.cust.HwCustUtils;
import java.util.Locale;

public class CryptKeeperConfirm extends InstrumentedFragment {
    private View mContentView;
    private Button mFinalButton;
    private OnClickListener mFinalClickListener = new OnClickListener() {
        public void onClick(View v) {
            boolean z = true;
            if (!Utils.isMonkeyRunning()) {
                ItemUseStat.getInstance().handleClick(CryptKeeperConfirm.this.getActivity(), 2, "encrypt_confirm");
                if (CryptKeeperConfirm.this.mHwCustCryptKeeperConfirm != null) {
                    CryptKeeperConfirm.this.mHwCustCryptKeeperConfirm.closeRog();
                }
                LockPatternUtils utils = new LockPatternUtils(CryptKeeperConfirm.this.getActivity());
                utils.setVisiblePatternEnabled(utils.isVisiblePatternEnabled(0), 0);
                if (utils.isOwnerInfoEnabled(0)) {
                    utils.setOwnerInfo(utils.getOwnerInfo(0), 0);
                }
                if (System.getInt(CryptKeeperConfirm.this.getContext().getContentResolver(), "show_password", 1) == 0) {
                    z = false;
                }
                utils.setVisiblePasswordEnabled(z, 0);
                Intent intent = new Intent(CryptKeeperConfirm.this.getActivity(), Blank.class);
                intent.putExtras(CryptKeeperConfirm.this.getArguments());
                CryptKeeperConfirm.this.startActivity(intent);
                try {
                    Stub.asInterface(ServiceManager.getService("mount")).setField("SystemLocale", Locale.getDefault().toLanguageTag());
                } catch (Exception e) {
                    Log.e("CryptKeeperConfirm", "Error storing locale for decryption UI", e);
                }
            }
        }
    };
    private HwCustCryptKeeperConfirm mHwCustCryptKeeperConfirm;

    public static class Blank extends Activity {
        private Handler mHandler = new Handler();

        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(2130968697);
            if (Utils.isMonkeyRunning()) {
                finish();
            }
            ((StatusBarManager) getSystemService("statusbar")).disable(58130432);
            this.mHandler.postDelayed(new Runnable() {
                public void run() {
                    IBinder service = ServiceManager.getService("mount");
                    if (service == null) {
                        Log.e("CryptKeeper", "Failed to find the mount service");
                        Blank.this.finish();
                        return;
                    }
                    IMountService mountService = Stub.asInterface(service);
                    try {
                        Bundle args = Blank.this.getIntent().getExtras();
                        mountService.encryptStorage(args.getInt("type", -1), args.getString("password"));
                    } catch (Exception e) {
                        Log.e("CryptKeeper", "Error while encrypting...", e);
                    }
                }
            }, 700);
        }
    }

    protected int getMetricsCategory() {
        return 33;
    }

    private void establishFinalConfirmationState() {
        this.mFinalButton = (Button) this.mContentView.findViewById(2131886422);
        this.mFinalButton.setOnClickListener(this.mFinalClickListener);
    }

    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        this.mContentView = inflater.inflate(2130968698, container, false);
        this.mContentView.setScrollBarStyle(33554432);
        Utils.prepareCustomPreferencesList(container, this.mContentView, this.mContentView, true);
        establishFinalConfirmationState();
        this.mHwCustCryptKeeperConfirm = (HwCustCryptKeeperConfirm) HwCustUtils.createObj(HwCustCryptKeeperConfirm.class, new Object[]{this});
        return this.mContentView;
    }
}
