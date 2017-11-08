package com.android.contacts.ext.phone;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.ArrayList;

public class HwCustSetupPhoneAccountHelperImpl extends HwCustSetupPhoneAccountHelper {
    private void createPredefinedGroupForCust(Context aContext) {
        boolean addGroup = true;
        long groupId = 0;
        ArrayList<ContentProviderOperation> operation = new ArrayList();
        ContentValues lValues = new ContentValues();
        lValues.put("account_name", "Phone");
        lValues.put("account_type", "com.android.huawei.phone");
        ContentValues contentValues = lValues;
        contentValues.put("title", aContext.getString(R.string.att_other_contacts_group));
        lValues.put("res_package", aContext.getPackageName());
        lValues.put("title_res", Integer.valueOf(R.string.att_other_contacts_group));
        lValues.put("sync1", "PREDEFINED_ATT_OTHER_GROUP");
        if (!isGroupNameExisted(aContext, "com.android.huawei.phone", "Phone", aContext.getString(R.string.att_other_contacts_group))) {
            Uri uri = aContext.getContentResolver().insert(Groups.CONTENT_URI, lValues);
            if (uri != null) {
                groupId = (long) Integer.parseInt(uri.getLastPathSegment());
            }
            if (groupId == 0) {
                addGroup = false;
            }
            if (addGroup) {
                Cursor cursor = aContext.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id"}, "account_type = 'com.android.huawei.phone' AND sync4 = '1'", null, null);
                if (cursor != null && cursor.moveToFirst()) {
                    while (true) {
                        try {
                            Builder lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                            lBuilder.withValue("mimetype", "vnd.android.cursor.item/group_membership");
                            lBuilder.withValue("data1", Long.valueOf(groupId));
                            lBuilder.withValue("raw_contact_id", Long.valueOf(cursor.getLong(0)));
                            operation.add(lBuilder.build());
                            if (!cursor.moveToNext()) {
                                break;
                            }
                        } catch (Exception e) {
                        } finally {
                            cursor.close();
                        }
                    }
                    try {
                        aContext.getContentResolver().applyBatch("com.android.contacts", operation);
                    } catch (RemoteException e2) {
                        e2.printStackTrace();
                    } catch (OperationApplicationException e3) {
                        e3.printStackTrace();
                    }
                }
            }
        }
    }

    public void customizePredefinedContactsAndGroups(Context aContext) {
        if (aContext != null && HwCustContactFeatureUtils.isShowAccServiceGrp()) {
            ContentValues cValues = new ContentValues();
            cValues.put("sync4", CallInterceptDetails.BRANDED_STATE);
            aContext.getContentResolver().update(RawContacts.CONTENT_URI, cValues, "account_type = 'com.android.huawei.phone' AND sync4 = 'PREDEFINED_HUAWEI_CONTACT'", null);
            createPredefinedGroupForCust(aContext);
        }
    }

    private boolean isGroupNameExisted(Context aContext, String accountType, String accountName, String groupName) {
        StringBuilder selection = new StringBuilder();
        ArrayList<String> selectionArgs = new ArrayList();
        boolean isGroupPresent = false;
        Cursor cursor = null;
        try {
            selection.append("(account_name=? AND account_type=?");
            selectionArgs.add(accountName);
            selectionArgs.add(accountType);
            if (groupName != null) {
                selection.append(" AND title=?");
                selectionArgs.add(groupName);
            } else {
                selection.append(" AND title IS NULL");
            }
            selection.append(" AND ").append("deleted").append(" = 0");
            selection.append(")");
            cursor = aContext.getContentResolver().query(Groups.CONTENT_URI, null, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null);
            if (cursor != null && cursor.moveToFirst() && cursor.getCount() > 0) {
                isGroupPresent = true;
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return isGroupPresent;
    }
}
