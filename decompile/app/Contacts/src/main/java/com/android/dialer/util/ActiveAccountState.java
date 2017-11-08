package com.android.dialer.util;

import android.content.Context;
import android.telecom.PhoneAccountHandle;
import android.telecom.TelecomManager;
import android.text.TextUtils;
import java.util.List;

public class ActiveAccountState {
    public String activedId;
    public String todoActiveId;

    public ActiveAccountState(String activedId, String todoActiveId) {
        this.activedId = activedId;
        this.todoActiveId = todoActiveId;
    }

    public static ActiveAccountState getActiveAccountState(List<String> activedAccounts, Context context) {
        String todoActivedId = null;
        String activeId = null;
        if (context == null) {
            return new ActiveAccountState();
        }
        List<PhoneAccountHandle> callCapablePhoneAccounts = ((TelecomManager) context.getSystemService("telecom")).getCallCapablePhoneAccounts();
        if (callCapablePhoneAccounts.size() == 1) {
            String accountId = ((PhoneAccountHandle) callCapablePhoneAccounts.get(0)).getId();
            if (!TextUtils.isEmpty(accountId) && TelecomUtil.isOperatorCM(context)) {
                if (activedAccounts == null || !activedAccounts.contains(accountId)) {
                    todoActivedId = accountId;
                } else {
                    activeId = accountId;
                }
            }
        } else if (callCapablePhoneAccounts.size() == 2) {
            PhoneAccount firstSlot = new PhoneAccount(((PhoneAccountHandle) callCapablePhoneAccounts.get(0)).getId(), 0, activedAccounts);
            PhoneAccount secondSlot = new PhoneAccount(((PhoneAccountHandle) callCapablePhoneAccounts.get(1)).getId(), 1, activedAccounts);
            activeId = PhoneAccount.selectActivedId(firstSlot, secondSlot);
            if (activeId == null) {
                todoActivedId = PhoneAccount.selectTodoActivedId(firstSlot, secondSlot);
            }
        }
        return new ActiveAccountState(activeId, todoActivedId);
    }

    public String toString() {
        return "ActiveAccountState [activedId=" + this.activedId + ", todoActiveId=" + this.todoActiveId + "]";
    }
}
