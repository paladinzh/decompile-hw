package com.android.systemui.statusbar;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.android.systemui.R;
import fyusion.vislib.BuildConfig;

public class HDVoiceView extends FrameLayout {
    private int mCallState;
    private Context mContext;
    private boolean mHDVoiceIntentReceived;
    private TextView mHDVoiceTextView;
    private boolean mIsHDVoice;
    PhoneStateListener mPhoneStateListener = new PhoneStateListener() {
        public void onCallStateChanged(int state, String incomingNumber) {
            HDVoiceView.this.mCallState = state;
            HDVoiceView.this.updateHDVoiceView(state);
        }
    };
    BroadcastReceiver mReceiver = new BroadcastReceiver() {
        private String IS_HD_VOICE = "isHDVoice";

        public void onReceive(Context context, Intent intent) {
            if (intent != null && intent.getAction() != null) {
                Log.d("HDVoiceView", "onReceive , CALL_STATE:" + HDVoiceView.this.mCallState);
                if ("com.huawei.Telephony.HDVoice".equals(intent.getAction()) && HDVoiceView.this.mCallState != 0) {
                    HDVoiceView.this.mIsHDVoice = intent.getBooleanExtra(this.IS_HD_VOICE, false);
                    HDVoiceView.this.mHDVoiceIntentReceived = true;
                    Log.d("HDVoiceView", "onReceive -> action :com.huawei.Telephony.HDVoice ,mIsHDVoice :" + HDVoiceView.this.mIsHDVoice);
                    HDVoiceView.this.updateHDVoiceViewIcon();
                }
            }
        }
    };
    private boolean mRegister;
    private Resources mResources = getResources();

    public HDVoiceView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.mContext = context;
    }

    protected void onFinishInflate() {
        this.mHDVoiceTextView = (TextView) findViewById(R.id.hd_voice_textview);
        super.onFinishInflate();
    }

    protected void onAttachedToWindow() {
        if (!this.mRegister) {
            this.mRegister = true;
            IntentFilter filter = new IntentFilter();
            filter.addAction("com.huawei.Telephony.HDVoice");
            this.mContext.registerReceiver(this.mReceiver, filter);
            registerPhoneStateListener(this.mContext);
        }
        super.onAttachedToWindow();
    }

    protected void onDetachedFromWindow() {
        if (this.mRegister) {
            this.mRegister = false;
            this.mContext.unregisterReceiver(this.mReceiver);
            unregisterPhoneStateListener(this.mContext);
        }
        super.onDetachedFromWindow();
    }

    private void updateHDVoiceViewIcon() {
        if (this.mIsHDVoice) {
            this.mHDVoiceTextView.setText(BuildConfig.FLAVOR);
            this.mHDVoiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.abc_ic_star_half_black_48dp, 0);
            this.mHDVoiceTextView.setContentDescription(this.mResources.getText(R.string.connected_hd));
            this.mHDVoiceTextView.setVisibility(0);
            return;
        }
        this.mHDVoiceTextView.setVisibility(8);
    }

    public void updateHDVoiceView(int state) {
        Log.d("HDVoiceView", "updateHDVoiceView -> CALL_STATE:" + state);
        switch (state) {
            case 0:
                this.mHDVoiceIntentReceived = false;
                this.mHDVoiceTextView.setVisibility(8);
                this.mHDVoiceTextView.setContentDescription(this.mResources.getText(R.string.attempting_hd));
                this.mHDVoiceTextView.setText(R.string.attempting);
                this.mHDVoiceTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.abc_list_focused_holo, 0);
                return;
            case 1:
            case 2:
                if (!this.mHDVoiceIntentReceived) {
                    this.mHDVoiceTextView.setVisibility(0);
                    return;
                }
                return;
            default:
                return;
        }
    }

    private void registerPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 32);
    }

    private void unregisterPhoneStateListener(Context context) {
        ((TelephonyManager) context.getSystemService("phone")).listen(this.mPhoneStateListener, 0);
    }
}
