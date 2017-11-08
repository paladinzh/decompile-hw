package com.android.mms.data;

import com.android.mms.HwCustMmsConfigImpl;

public class HwCustWorkingMessageImpl extends HwCustWorkingMessage {
    public boolean supportSendToEmail() {
        return HwCustMmsConfigImpl.allowSendSmsToEmail();
    }

    public static String getLongestEmailRecipient(WorkingMessage workingMessage) {
        int longest = 0;
        String str = null;
        String[] mWorkingRecipients = null;
        String Recipients = workingMessage.getWorkingRecipients();
        if (Recipients != null) {
            mWorkingRecipients = Recipients.split(";");
        }
        Conversation mConversation = workingMessage.getConversation();
        int i;
        int length;
        int emailLength;
        if (mWorkingRecipients != null) {
            for (String anElementOfRecipients : mWorkingRecipients) {
                if (Contact.isEmailAddress(anElementOfRecipients)) {
                    emailLength = anElementOfRecipients.length();
                    if (longest < emailLength) {
                        longest = emailLength;
                        str = anElementOfRecipients;
                    }
                }
            }
        } else if (!(mConversation == null || mConversation.getRecipients() == null)) {
            String[] dests = mConversation.getRecipients().getNumbers();
            length = dests.length;
            for (i = 0; i < length; i++) {
                if (Contact.isEmailAddress(dests[i])) {
                    emailLength = dests[i].length();
                    if (longest < emailLength) {
                        longest = emailLength;
                        str = dests[i];
                    }
                }
            }
        }
        return str;
    }
}
