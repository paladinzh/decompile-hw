package com.android.dialer.util;

import android.telephony.HwTelephonyManager;
import android.text.TextUtils;
import com.android.contacts.hap.utils.MessageUtils;
import java.util.List;

public class PhoneAccount {
    private String mId;
    private boolean mIsActived;
    private boolean mIsCMOperator;
    private boolean mIsDefault4GSlot;
    private int mSlot;

    public PhoneAccount(String id, int slot, List<String> activedAccounts) {
        boolean isAvalible;
        boolean isCMOperator;
        boolean z = false;
        this.mId = id;
        this.mSlot = slot;
        if (TextUtils.isEmpty(id)) {
            isAvalible = false;
        } else {
            isAvalible = true;
        }
        if (isAvalible) {
            isCMOperator = MessageUtils.isCMOperator(slot);
        } else {
            isCMOperator = false;
        }
        this.mIsCMOperator = isCMOperator;
        this.mIsActived = activedAccounts == null ? false : activedAccounts.contains(this.mId);
        if (HwTelephonyManager.getDefault().getDefault4GSlotId() == this.mSlot) {
            z = true;
        }
        this.mIsDefault4GSlot = z;
    }

    private String getActivedId() {
        if (this.mIsCMOperator && this.mIsActived) {
            return this.mId;
        }
        return null;
    }

    private String getTodoActivedId() {
        if (this.mIsCMOperator && !this.mIsActived && this.mIsDefault4GSlot) {
            return this.mId;
        }
        return null;
    }

    public static String selectActivedId(PhoneAccount account1, PhoneAccount account2) {
        if (account1 != null && account1.getActivedId() != null) {
            return account1.getActivedId();
        }
        if (account2 != null) {
            return account2.getActivedId();
        }
        return null;
    }

    public static String selectTodoActivedId(PhoneAccount account1, PhoneAccount account2) {
        if (account1 != null && account1.getTodoActivedId() != null) {
            return account1.getTodoActivedId();
        }
        if (account2 != null) {
            return account2.getTodoActivedId();
        }
        return null;
    }
}
