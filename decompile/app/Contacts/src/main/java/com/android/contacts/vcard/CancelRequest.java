package com.android.contacts.vcard;

public class CancelRequest {
    public final int jobId;

    public CancelRequest(int jobId, String displayName) {
        this.jobId = jobId;
    }
}
