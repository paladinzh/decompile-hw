package com.android.mms.model;

public class ContentRestrictionFactory {
    private static ContentRestriction sContentRestriction;

    private ContentRestrictionFactory() {
    }

    public static ContentRestriction getContentRestriction() {
        if (sContentRestriction == null) {
            sContentRestriction = new CarrierContentRestriction();
        }
        return sContentRestriction;
    }
}
