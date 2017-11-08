package com.android.mms.util;

import android.content.ContentResolver;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import com.android.mms.data.Contact;
import com.huawei.mms.ui.EmuiAvatarImage;

public class HwCustEcidLookup {
    public void init(Context aContext) {
    }

    public boolean getNameIdFeatureEnable() {
        return false;
    }

    public void setContactAsStale(Contact aContact, String aNumber) {
    }

    public void setEcidProfilePicture(Context aContext, String aNumber, EmuiAvatarImage aAvatarImage) {
    }

    public void setEcidContactName(Context aContext, String aNumber, Contact aContact) {
    }

    public Bitmap getEcidNotificationAvatar(Context aContext, String aNumber, Bitmap aAvatar) {
        return aAvatar;
    }

    public void addSender(Context aContext, Uri aUri) {
    }

    public void addSender(String number) {
    }

    public String getEcidName(ContentResolver aCr, String aNumber, String aTitle) {
        return aTitle;
    }

    public boolean delayedNotification(Context context, long threadId, boolean isStatusMessage, String addr) {
        return false;
    }

    public boolean delayedNotification(Context context, long threadId, boolean isStatusMessage, Uri uri) {
        return false;
    }

    public Drawable getEcidDrawableIfExists(Context context, String number, Drawable aDrawable) {
        return aDrawable;
    }
}
