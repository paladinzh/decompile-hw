package com.android.contacts.hap.util;

public class UnsupportedException extends Exception {
    private static final long serialVersionUID = 1;

    public UnsupportedException(Exception exp) {
        super(exp);
    }
}
