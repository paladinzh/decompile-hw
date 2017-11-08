package com.android.contacts.quickcontact;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import android.widget.Toast;
import com.android.contacts.ContactsUtils;
import com.android.contacts.activities.ContactDetailActivity;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.google.android.gms.R;
import java.util.List;

public class QuickContactActivity extends Activity {
    protected void onCreate(Bundle savedInstanceState) {
        getContactExist();
        finish();
        super.onCreate(savedInstanceState);
    }

    private void viewContactDetail(Uri contactLookupUri) {
        Intent intent = new Intent("android.intent.action.VIEW", contactLookupUri);
        intent.setClass(this, ContactDetailActivity.class);
        intent.putExtra("com.android.launcher.intent.extra.shortcut.CHECK_CONTACT_EXIST", true);
        intent.putExtra("fromWhere", "fromLauncher");
        startActivity(intent);
    }

    private void getContactExist() {
        getIntent().setData(getAdjustedUri(getIntent().getData()));
        Uri uri = getIntent().getData();
        Cursor cursor;
        try {
            Uri uriCurrentFormat = ContactsUtils.ensureIsContactUri(getContentResolver(), uri);
            if (uriCurrentFormat != null) {
                cursor = null;
                cursor = getApplicationContext().getContentResolver().query(uriCurrentFormat, null, null, null, null);
                if (cursor == null || !cursor.moveToFirst()) {
                    Toast.makeText(this, R.string.contact_entry_deleted, 0).show();
                    super.finish();
                    if (cursor != null) {
                        if (!cursor.isClosed()) {
                            cursor.close();
                        }
                    }
                    return;
                }
                viewContactDetail(uri);
                if (cursor != null) {
                    if (!cursor.isClosed()) {
                        cursor.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        } catch (Throwable th) {
            if (!(cursor == null || cursor.isClosed())) {
                cursor.close();
            }
        }
    }

    private Uri getAdjustedUri(Uri uri) {
        Uri adjustedUri = uri;
        String sim1AccountHashCode_old = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.sim", SimFactoryManager.getSimCardDisplayLabel("com.android.huawei.sim")));
        String sim2AccountHashCode_old = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.secondsim", SimFactoryManager.getSimCardDisplayLabel("com.android.huawei.secondsim")));
        String sim1AccountHashCode_new = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.sim", SimFactoryManager.getAccountName("com.android.huawei.sim")));
        String sim2AccountHashCode_new = String.valueOf(CommonUtilMethods.getAccountHashCode("com.android.huawei.secondsim", SimFactoryManager.getAccountName("com.android.huawei.secondsim")));
        if (uri == null) {
            return adjustedUri;
        }
        List<String> pathSegments = uri.getPathSegments();
        if (pathSegments.size() != 4) {
            return adjustedUri;
        }
        long contactId = Long.parseLong((String) pathSegments.get(3));
        String oldlookupKey = (String) pathSegments.get(2);
        Object accountHashCode = null;
        int start = oldlookupKey.indexOf("r");
        if (start > 0 && start < oldlookupKey.length()) {
            accountHashCode = oldlookupKey.substring(0, start);
        }
        String newAccountHashCode = null;
        if (sim1AccountHashCode_old.equals(accountHashCode)) {
            newAccountHashCode = sim1AccountHashCode_new;
        } else if (sim2AccountHashCode_old.equals(accountHashCode)) {
            newAccountHashCode = sim2AccountHashCode_new;
        }
        if (newAccountHashCode == null) {
            return adjustedUri;
        }
        StringBuilder newLookupKey = new StringBuilder();
        newLookupKey.append(newAccountHashCode);
        newLookupKey.append(oldlookupKey.substring(start));
        return Contacts.getLookupUri(contactId, newLookupKey.toString());
    }
}
