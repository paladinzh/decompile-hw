package com.android.systemui.qs;

import android.content.Context;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings.Secure;
import com.android.systemui.utils.UserSwitchUtils;

public abstract class SecureSetting extends ContentObserver {
    private final Context mContext;
    private boolean mListening;
    private int mObservedValue = 0;
    private final String mSettingName;
    private int mUserId;

    protected abstract void handleValueChanged(int i, boolean z);

    public SecureSetting(Context context, Handler handler, String settingName) {
        super(handler);
        this.mContext = context;
        this.mSettingName = settingName;
        this.mUserId = UserSwitchUtils.getCurrentUser();
    }

    public int getValue() {
        return Secure.getIntForUser(this.mContext.getContentResolver(), this.mSettingName, 0, this.mUserId);
    }

    public void setValue(int value) {
        Secure.putIntForUser(this.mContext.getContentResolver(), this.mSettingName, value, this.mUserId);
    }

    public void setListening(boolean listening) {
        if (listening != this.mListening) {
            this.mListening = listening;
            if (listening) {
                this.mObservedValue = getValue();
                this.mContext.getContentResolver().registerContentObserver(Secure.getUriFor(this.mSettingName), false, this, this.mUserId);
            } else {
                this.mContext.getContentResolver().unregisterContentObserver(this);
                this.mObservedValue = 0;
            }
        }
    }

    public void onChange(boolean selfChange) {
        int value = getValue();
        handleValueChanged(value, value != this.mObservedValue);
        this.mObservedValue = value;
    }

    public void setUserId(int userId) {
        this.mUserId = userId;
        if (this.mListening) {
            setListening(false);
            setListening(true);
        }
    }
}
