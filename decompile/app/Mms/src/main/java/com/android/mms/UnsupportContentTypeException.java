package com.android.mms;

public final class UnsupportContentTypeException extends ContentRestrictionException {
    private static final long serialVersionUID = 2684128059358484321L;

    public UnsupportContentTypeException(String msg) {
        super(msg);
    }
}
