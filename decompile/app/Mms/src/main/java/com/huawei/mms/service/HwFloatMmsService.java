package com.huawei.mms.service;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.ui.MessageUtils;
import com.huawei.mms.service.IHwFloatMmsService.Stub;
import com.huawei.mms.util.MccMncConfig;
import com.huawei.mms.util.SmartArchiveSettingUtils;

public class HwFloatMmsService extends Service {
    public final Stub mBinder = new Stub() {
        public boolean is7bitEnable() throws RemoteException {
            return MccMncConfig.is7bitEnable();
        }

        public CharSequence replaceAlphabetForGsm7Bit(CharSequence s, int start, int end) throws RemoteException {
            return MessageUtils.replaceAlphabetFor7Bit(s, start, end);
        }

        public boolean isMultipartSmsEnabled() {
            return MmsConfig.getMultipartSmsEnabled();
        }

        public boolean isAlertLongSmsEnable() {
            return MmsConfig.getMmsBoolConfig("enableAlertLongSms", false);
        }

        public int getSmsToMmsTextThreshhold() {
            return MmsConfig.getMultipartSmsEnabled() ? -1 : MmsConfig.getSmsToMmsTextThreshold();
        }

        public String getHuaweiNameFromSnippet(String snippet) throws RemoteException {
            if (SmartArchiveSettingUtils.isHuaweiArchiveEnabled(MmsApp.getApplication())) {
                return Conversation.getSenderNameFromMessageBody(snippet);
            }
            return "";
        }

        public NameMatchResult getNameMatchedContact(String name) {
            if (SmartArchiveSettingUtils.isHuaweiArchiveEnabled(MmsApp.getApplication())) {
                return Contact.getNameMatchedContact(MmsApp.getApplication(), name);
            }
            return null;
        }
    };

    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    public void onCreate() {
        super.onCreate();
    }

    public void onDestroy() {
        super.onDestroy();
    }

    public boolean onUnbind(Intent intent) {
        return super.onUnbind(intent);
    }

    public void onRebind(Intent intent) {
        super.onRebind(intent);
    }
}
