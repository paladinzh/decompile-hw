package com.android.contacts.ext;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Profile;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.google.android.collect.Lists;
import com.google.android.gms.R;
import java.util.ArrayList;

public class HwCustProfileSimNumberUpdaterUtil {
    private static final String TAG = HwCustProfileSimNumberUpdaterUtil.class.getSimpleName();

    public static synchronized void checkAndInsertProfile(Context aContext) {
        synchronized (HwCustProfileSimNumberUpdaterUtil.class) {
            if (aContext == null) {
                return;
            }
            Cursor lProfileCursor = aContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
            if (lProfileCursor != null) {
                try {
                    if (lProfileCursor.moveToFirst()) {
                        Log.e(TAG, "Data is already present in the table");
                        if (lProfileCursor != null) {
                            lProfileCursor.close();
                        }
                    }
                } catch (Throwable th) {
                    if (lProfileCursor != null) {
                        lProfileCursor.close();
                    }
                }
            }
            ArrayList<ContentProviderOperation> lOperations = Lists.newArrayList();
            Builder lBuilder = ContentProviderOperation.newInsert(Profile.CONTENT_RAW_CONTACTS_URI);
            lBuilder.withValue("account_name", null);
            lBuilder.withValue("account_type", null);
            lOperations.add(lBuilder.build());
            lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
            lBuilder.withValueBackReference("raw_contact_id", 0);
            lBuilder.withValue("mimetype", "vnd.android.cursor.item/name");
            lBuilder.withValue("data2", aContext.getString(R.string.string_aab_my_info));
            lOperations.add(lBuilder.build());
            try {
                aContext.getContentResolver().applyBatch("com.android.contacts", lOperations);
            } catch (RemoteException e) {
                Log.e(TAG, "RemoteException while inserting profile ");
            } catch (OperationApplicationException aOE) {
                Log.e(TAG, "OperationApplicationException while inserting profile ");
                aOE.printStackTrace();
                if (lProfileCursor != null) {
                    lProfileCursor.close();
                }
                return;
            } finally {
                lOperations.clear();
            }
            if (lProfileCursor != null) {
                lProfileCursor.close();
            }
        }
    }

    public static synchronized void checkAndUpdateProfileNumber(Context aContext) {
        synchronized (HwCustProfileSimNumberUpdaterUtil.class) {
            if (aContext == null) {
                return;
            }
            String lNumber = null;
            TelephonyManager lTeleManager = TelephonyManager.getDefault();
            if (lTeleManager != null) {
                lNumber = lTeleManager.getLine1Number();
            }
            if (TextUtils.isEmpty(lNumber)) {
                lNumber = SimFactoryManager.getSimIccNumber();
            }
            if (!TextUtils.isEmpty(lNumber)) {
                Cursor lProfileCursor;
                lProfileCursor = aContext.getContentResolver().query(Profile.CONTENT_URI, new String[]{"_id"}, null, null, null);
                ContentValues lValues = new ContentValues();
                CharSequence lExistingNumber = null;
                Uri lProfileDataUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "profile/data");
                Cursor cursor = null;
                if (lProfileCursor != null) {
                    try {
                        if (lProfileCursor.moveToFirst()) {
                            long lProfileId = lProfileCursor.getLong(0);
                            String lSelection = "mimetype='vnd.android.cursor.item/phone_v2' AND data15=1";
                            cursor = aContext.getContentResolver().query(lProfileDataUri, new String[]{"data1"}, lSelection, null, null);
                            if (cursor != null && cursor.moveToFirst()) {
                                lExistingNumber = cursor.getString(0);
                            }
                            if (TextUtils.isEmpty(lExistingNumber)) {
                                Uri insertUri = Uri.withAppendedPath(ContactsContract.AUTHORITY_URI, "profile/raw_contacts/" + lProfileId + "/data");
                                lValues.put("raw_contact_id", Long.valueOf(lProfileId));
                                lValues.put("mimetype", "vnd.android.cursor.item/phone_v2");
                                lValues.put("data1", lNumber);
                                lValues.put("data2", Integer.valueOf(2));
                                lValues.put("data15", Integer.valueOf(1));
                                aContext.getContentResolver().insert(insertUri, lValues);
                            } else if (!lNumber.equals(lExistingNumber)) {
                                lValues.put("data1", lNumber);
                                aContext.getContentResolver().update(lProfileDataUri, lValues, lSelection, null);
                            }
                            if (lProfileCursor != null) {
                                lProfileCursor.close();
                            }
                            if (cursor != null) {
                                cursor.close();
                            }
                        }
                    } catch (Throwable th) {
                        if (lProfileCursor != null) {
                            lProfileCursor.close();
                        }
                        if (cursor != null) {
                            cursor.close();
                        }
                    }
                }
                ArrayList<ContentProviderOperation> lOperations = Lists.newArrayList();
                Builder lBuilder = ContentProviderOperation.newInsert(Profile.CONTENT_RAW_CONTACTS_URI);
                lBuilder.withValue("account_name", null);
                lBuilder.withValue("account_type", null);
                lOperations.add(lBuilder.build());
                lBuilder = ContentProviderOperation.newInsert(Data.CONTENT_URI);
                lBuilder.withValueBackReference("raw_contact_id", 0);
                lBuilder.withValue("mimetype", "vnd.android.cursor.item/phone_v2");
                lBuilder.withValue("data1", lNumber);
                lBuilder.withValue("data2", Integer.valueOf(2));
                lBuilder.withValue("data15", Integer.valueOf(1));
                lOperations.add(lBuilder.build());
                try {
                    aContext.getContentResolver().applyBatch("com.android.contacts", lOperations);
                } catch (RemoteException e) {
                    Log.e(TAG, "RemoteException while inserting profile ");
                } catch (OperationApplicationException aOE) {
                    Log.e(TAG, "OperationApplicationException while inserting profile ");
                    aOE.printStackTrace();
                    if (lProfileCursor != null) {
                        lProfileCursor.close();
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                    return;
                } finally {
                    lOperations.clear();
                }
                if (lProfileCursor != null) {
                    lProfileCursor.close();
                }
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }
}
