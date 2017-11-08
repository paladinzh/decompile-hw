package com.android.systemui.statusbar.policy;

import android.os.Handler;
import android.telephony.SmsMessage;
import android.text.Editable;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.Toast;
import com.android.mms.service.MmsService;
import com.android.systemui.R;

public class HwCustRemoteInputViewImpl extends HwCustRemoteInputView {
    public static final int NUM_PER_GROUP = 70;
    private static final int TIME_DELAY_FRESH_COUNTER = 200;
    private Handler mHandler = null;
    private Runnable mToast = new Runnable() {
        public void run() {
            Toast.makeText(HwCustRemoteInputViewImpl.this.mRemoteInputView.getContext(), R.string.sms_too_long, 1).show();
        }
    };

    public HwCustRemoteInputViewImpl(RemoteInputView remoteInputView) {
        super(remoteInputView);
    }

    public void setOnePageSmsText(Editable s, EditText redit) {
        if (getSmsSegmentCount(s) > 1 && MmsService.isAlertLongSmsEnable(this.mRemoteInputView.getContext())) {
            CharSequence charSequence = getSmsInOnePage(s);
            InputMethodManager m = (InputMethodManager) this.mRemoteInputView.getContext().getSystemService("input_method");
            redit.setText(charSequence);
            redit.setSelection(charSequence.length());
            this.mRemoteInputView.updateInputCount();
            m.toggleSoftInput(0, 2);
            postDealyTask(this.mToast, 200);
        }
    }

    private CharSequence getSmsInOnePage(CharSequence csText) {
        CharSequence textOrigin = csText;
        CharSequence text = csText;
        int i = 0;
        while (true) {
            int i2 = i + 1;
            if (i > csText.length() || SmsMessage.calculateLength(text, false)[0] <= 1) {
                return text;
            }
            text = csText.subSequence(0, csText.length() - i2);
            i = i2;
        }
        return text;
    }

    private int getSmsSegmentCount(CharSequence csText) {
        return SmsMessage.calculateLength(csText, false)[0];
    }

    public void postDealyTask(Runnable task, long delay) {
        if (this.mHandler == null) {
            this.mHandler = new Handler();
        }
        this.mHandler.removeCallbacks(task);
        this.mHandler.postDelayed(task, delay);
    }
}
