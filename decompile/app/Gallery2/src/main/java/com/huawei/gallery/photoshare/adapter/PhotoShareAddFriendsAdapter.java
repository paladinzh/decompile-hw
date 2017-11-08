package com.huawei.gallery.photoshare.adapter;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import com.android.gallery3d.R;
import com.android.gallery3d.util.GalleryLog;
import java.util.List;

public class PhotoShareAddFriendsAdapter {
    private static final ComponentName[] sEmailComponents = new ComponentName[]{new ComponentName("com.android.email", "com.android.email.activity.MessageCompose")};
    private static final ComponentName[] sEmailWelcome = new ComponentName[]{new ComponentName("com.android.email", "com.android.email.activity.Welcome")};
    private static final ComponentName[] sMmsComponents = new ComponentName[]{new ComponentName("jp.emobile.emnet.mail", "jp.emobile.emnet.mail.MmsEditActivity"), new ComponentName("com.huawei.message", "com.huawei.hotalk.MainActivity"), new ComponentName("com.android.mms", "com.android.mms.ui.ComposeMessageActivity"), new ComponentName("com.android.contacts", "com.android.mms.ui.ComposeMessageActivity")};
    private final Context mContext;
    private ComponentName mEmail = null;
    private ComponentName mEmailWelcome = null;
    private ComponentName mMessage = null;
    private String mUri = null;
    private String mUriTitle = null;

    public PhotoShareAddFriendsAdapter(Activity context) {
        this.mContext = context;
        initComponent();
    }

    public void updateUri(String uri) {
        this.mUri = uri;
    }

    public void setUriTitle(String title) {
        this.mUriTitle = title;
    }

    private ComponentName findTargetActivity(ComponentName[] components, List<ResolveInfo> resolveInfo) {
        for (ComponentName component : components) {
            for (ResolveInfo info : resolveInfo) {
                if (component.getPackageName().equalsIgnoreCase(info.activityInfo.packageName)) {
                    return component;
                }
            }
        }
        return null;
    }

    private void initComponent() {
        try {
            PackageManager packageManager = this.mContext.getPackageManager();
            List<ResolveInfo> components = packageManager.queryIntentActivities(new Intent("android.intent.action.SEND").setType("text/*"), 0);
            this.mMessage = findTargetActivity(sMmsComponents, components);
            this.mEmail = findTargetActivity(sEmailComponents, components);
            if (this.mEmail == null) {
                List<ResolveInfo> startEmailComponents = packageManager.queryIntentActivities(new Intent("android.intent.action.MAIN").setComponent(sEmailWelcome[0]), 0);
                if (startEmailComponents != null && !startEmailComponents.isEmpty()) {
                    this.mEmailWelcome = sEmailWelcome[0];
                }
            }
        } catch (ActivityNotFoundException e) {
            GalleryLog.w("PhotoShareAddFriendsAdapter", "initComponent sms or email activity not found");
        }
    }

    public boolean isMessageEnable() {
        return (this.mUri == null || this.mMessage == null) ? false : true;
    }

    public boolean isEmailEnable() {
        return (this.mUri == null || (this.mEmail == null && this.mEmailWelcome == null)) ? false : true;
    }

    public boolean isLinkEnable() {
        return this.mUri != null;
    }

    private void sendUri(ComponentName component, String extra_text, String extra_subject) {
        try {
            Intent intent = new Intent();
            if (component != null) {
                intent.setComponent(component);
            }
            intent.setAction("android.intent.action.SEND");
            intent.setType("text/*");
            if (extra_subject != null) {
                intent.putExtra("android.intent.extra.SUBJECT", extra_subject);
            }
            if (extra_text != null) {
                intent.putExtra("android.intent.extra.TEXT", extra_text);
            }
            if (component == null) {
                intent = Intent.createChooser(intent, this.mContext.getResources().getString(R.string.share));
            }
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            GalleryLog.w("PhotoShareAddFriendsAdapter", "sendUri activity not found");
        }
    }

    public void sendMessage() {
        sendUri(this.mMessage, this.mUri + "\n" + this.mUriTitle, null);
    }

    private void sendEmailWelcome() {
        try {
            Intent intent = new Intent();
            intent.setComponent(this.mEmailWelcome);
            intent.setAction("android.intent.action.MAIN");
            this.mContext.startActivity(intent);
        } catch (ActivityNotFoundException e) {
            GalleryLog.w("PhotoShareAddFriendsAdapter", "sendEmailWelcome activity not found");
        }
    }

    public void sendEmail() {
        if (this.mEmail != null) {
            sendUri(this.mEmail, this.mUri, this.mUriTitle);
        } else if (this.mEmailWelcome != null) {
            sendEmailWelcome();
        } else {
            startLink();
        }
    }

    public void startLink() {
        sendUri(null, this.mUri + "\n" + this.mUriTitle, null);
    }
}
