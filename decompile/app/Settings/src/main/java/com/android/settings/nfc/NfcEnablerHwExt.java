package com.android.settings.nfc;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.util.Log;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Switch;
import com.android.settings.ItemUseStat;

public class NfcEnablerHwExt {
    private boolean mBeamDisallowed;
    private Switch mBeamSwitch;
    private final Context mContext;
    private final IntentFilter mIntentFilter;
    private OnCheckedChangeListener mListener;
    private final NfcAdapter mNfcAdapter;
    private final BroadcastReceiver mReceiver;
    private Switch mSwitchPreference;

    /* renamed from: com.android.settings.nfc.NfcEnablerHwExt$1 */
    class AnonymousClass1 extends BroadcastReceiver {
        final /* synthetic */ NfcEnablerHwExt this$0;

        public void onReceive(Context context, Intent intent) {
            if ("android.nfc.action.ADAPTER_STATE_CHANGED".equals(intent.getAction())) {
                this.this$0.handleNfcStateChanged(intent.getIntExtra("android.nfc.extra.ADAPTER_STATE", 1));
            }
        }
    }

    /* renamed from: com.android.settings.nfc.NfcEnablerHwExt$2 */
    class AnonymousClass2 implements OnCheckedChangeListener {
        final /* synthetic */ NfcEnablerHwExt this$0;

        public void onCheckedChanged(CompoundButton switch_, boolean value) {
            if (this.this$0.mNfcAdapter != null && value != this.this$0.mNfcAdapter.isEnabled()) {
                ItemUseStat.getInstance().handleClick(this.this$0.mContext, 2, "nfc_switch");
                this.this$0.mSwitchPreference.setEnabled(false);
                if (value) {
                    this.this$0.mNfcAdapter.enable();
                } else {
                    this.this$0.mNfcAdapter.disable();
                }
            }
        }
    }

    public void resume() {
        if (this.mNfcAdapter != null) {
            handleNfcStateChanged(this.mNfcAdapter.getAdapterState());
            this.mContext.registerReceiver(this.mReceiver, this.mIntentFilter);
            this.mSwitchPreference.setOnCheckedChangeListener(this.mListener);
        }
    }

    public void pause() {
        if (this.mNfcAdapter != null) {
            this.mContext.unregisterReceiver(this.mReceiver);
            this.mSwitchPreference.setOnCheckedChangeListener(null);
        }
    }

    private void handleNfcStateChanged(int newState) {
        boolean z = false;
        switch (newState) {
            case 1:
                this.mSwitchPreference.setChecked(false);
                this.mSwitchPreference.setEnabled(true);
                this.mBeamSwitch.setEnabled(false);
                return;
            case 2:
                this.mSwitchPreference.setChecked(true);
                this.mSwitchPreference.setEnabled(false);
                this.mBeamSwitch.setEnabled(false);
                return;
            case 3:
                this.mSwitchPreference.setChecked(true);
                this.mSwitchPreference.setEnabled(true);
                Switch switchR = this.mBeamSwitch;
                if (!this.mBeamDisallowed) {
                    z = true;
                }
                switchR.setEnabled(z);
                return;
            case 4:
                this.mSwitchPreference.setChecked(false);
                this.mSwitchPreference.setEnabled(false);
                this.mBeamSwitch.setEnabled(false);
                return;
            default:
                Log.d("NfcEnablerHwExt", "received unknow event, just return");
                return;
        }
    }
}
