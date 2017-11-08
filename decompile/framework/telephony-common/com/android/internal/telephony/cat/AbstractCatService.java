package com.android.internal.telephony.cat;

import android.os.Handler;
import com.android.internal.telephony.HwTelephonyFactory;

public abstract class AbstractCatService extends Handler implements AppInterface {
    CatServiceReference mReference = HwTelephonyFactory.getHwUiccManager().createHwCatServiceReference();

    public interface CatServiceReference {
        String getLanguageNotificationCode();

        void setLanguageNotificationCode(String str);
    }

    public void onCmdResponse(CatResponseMessage resMsg) {
    }

    public String getLanguageNotificationCode() {
        return this.mReference.getLanguageNotificationCode();
    }

    public void setLanguageNotificationCode(String strLanguageNotificationCode) {
        this.mReference.setLanguageNotificationCode(strLanguageNotificationCode);
    }
}
