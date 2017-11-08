package com.android.mms;

public final class ExceedMessageSizeException extends ContentRestrictionException {
    private static final long serialVersionUID = 6647713416796190850L;

    public ExceedMessageSizeException(String msg) {
        super(msg);
    }
}
