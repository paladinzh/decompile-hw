package com.fyusion.sdk.common.ext;

/* compiled from: Unknown */
public enum ProcessError {
    FILE_NOT_FOUND("Invalid location"),
    INSUFFICIENT_ACCESS_PRIVILEGES("Insufficient access privilege"),
    CORRUPT_DATA("Corrupt data"),
    USER_CANCEL_REQUEST("User cancelled request"),
    PROCESSING_IN_PROGRESS("Processing is already in progress"),
    RECORDING_IN_PROGRESS("Fyuse is not ready to process");
    
    private String a;

    private ProcessError(String str) {
        setMessage(str);
    }

    public String getMessage() {
        return this.a;
    }

    public void setMessage(String str) {
        this.a = str;
    }
}
