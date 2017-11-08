package com.android.mms.transaction;

import com.google.android.mms.MmsException;

public interface MessageSender {
    boolean sendMessage(long j) throws MmsException;
}
