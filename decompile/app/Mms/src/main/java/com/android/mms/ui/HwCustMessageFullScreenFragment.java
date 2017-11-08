package com.android.mms.ui;

import android.content.Context;
import android.content.Intent;
import com.huawei.mms.ui.EditTextWithSmiley;

public class HwCustMessageFullScreenFragment {

    public interface IMessageFullScreenHolder {
        void updataMmsTextCountView(int i);

        void updataSmsTextCountView(int i);

        void updateHintText(int i);

        void updateSendButton(boolean z, boolean z2);
    }

    public HwCustMessageFullScreenFragment(Context context) {
    }

    public void onCreate(Intent intent) {
    }

    public void initViews() {
    }

    public void onStart() {
    }

    public void onResume() {
    }

    public void onStop() {
    }

    public void setHolder(IMessageFullScreenHolder holder) {
    }

    public void onSendClick() {
    }

    public void onTextChangSendCapRequest(int before) {
    }

    public boolean updataSendStateForRcs() {
        return false;
    }

    public boolean updateTextCountForRcs() {
        return false;
    }

    public boolean isRcsMessage() {
        return false;
    }

    public void configLocalBroadcastIntent(Intent intent) {
    }

    public void setOnePageSmsText(CharSequence smsText, EditTextWithSmiley mDataEditor) {
    }
}
