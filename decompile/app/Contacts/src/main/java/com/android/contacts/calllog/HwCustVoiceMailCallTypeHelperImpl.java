package com.android.contacts.calllog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.SystemProperties;
import com.android.contacts.DetailActivity;
import com.android.contacts.activities.ContactDetailActivity;

public class HwCustVoiceMailCallTypeHelperImpl extends HwCustVoiceMailCallTypeHelper {
    public boolean voiceMailSupportInCallLogFilter() {
        return SystemProperties.get("ro.config.hw_opta", "").equals("109");
    }

    public boolean isUriEquals(Uri newCallUri, Uri voicemailUri) {
        if (!voiceMailSupportInCallLogFilter() || newCallUri == null || voicemailUri == null || newCallUri.getAuthority() == null || !newCallUri.getAuthority().equals(voicemailUri.getAuthority()) || newCallUri.getScheme() == null || !newCallUri.getScheme().equals(voicemailUri.getScheme()) || newCallUri.getPath() == null || !newCallUri.getPath().equals(voicemailUri.getPath())) {
            return false;
        }
        return true;
    }

    public Intent getIntent(Context context) {
        return new Intent(context, DetailActivity.class);
    }

    public Intent getIntent(Context context, Cursor cursor) {
        if (4 == cursor.getInt(4)) {
            return new Intent(context, DetailActivity.class);
        }
        return new Intent(context, ContactDetailActivity.class);
    }

    public boolean isVVMEnabled(Context context) {
        if (!voiceMailSupportInCallLogFilter()) {
            return false;
        }
        try {
            PackageManager pm = context.getPackageManager();
            if (pm == null) {
                return false;
            }
            int state = pm.getApplicationEnabledSetting("com.orange.vvm");
            if (state == 1 || state == 0) {
                return true;
            }
            return false;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }
}
