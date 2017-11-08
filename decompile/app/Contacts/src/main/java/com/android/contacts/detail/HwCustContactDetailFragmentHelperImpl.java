package com.android.contacts.detail;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.ContextMenu;
import com.android.contacts.Collapser;
import com.android.contacts.detail.ContactDetailAdapter.DetailViewEntry;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.HwCustCommonConstants;
import com.android.contacts.hap.sprint.preload.HwCustPreloadContacts;
import com.android.contacts.model.Contact;
import com.android.contacts.model.RawContact;
import com.android.contacts.util.HwCustContactFeatureUtils;
import com.google.android.gms.R;
import java.util.ArrayList;
import java.util.Locale;

public class HwCustContactDetailFragmentHelperImpl extends HwCustContactDetailFragmentHelper {
    public static final String TAG = "HwCustContactDetailFragmentHelperImpl";
    private ArrayList<DetailViewEntry> mVibrationEntries = new ArrayList();

    public void customizeContextMenu(ContextMenu aMenu) {
        if (HwCustCommonConstants.IS_AAB_ATT && aMenu.findItem(1001) != null) {
            aMenu.removeItem(1001);
        }
    }

    public void buildCustomEntries(Context context, Contact contactData, String accountType) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && this.mVibrationEntries.size() == 0 && !contactData.isUserProfile() && !contactData.isYellowPage() && EmuiFeatureManager.isSystemVoiceCapable() && contactData.isWritableContact(context)) {
            DetailViewEntry detailViewEntry = new DetailViewEntry();
            detailViewEntry.kind = context.getString(R.string.label_vibration);
            detailViewEntry.id = ((RawContact) contactData.getRawContacts().get(0)).getId().longValue();
            detailViewEntry.mimetype = HwCustCommonConstants.VIBRATION_MIMETYPE;
            detailViewEntry.typeString = context.getString(R.string.phone_vibration);
            detailViewEntry.data = queryVibrationType(context, detailViewEntry.id);
            detailViewEntry.mAccountType = accountType;
            detailViewEntry.uri = ContentUris.withAppendedId(Data.CONTENT_URI, detailViewEntry.id);
            this.mVibrationEntries.add(detailViewEntry);
        }
    }

    private String queryVibrationType(Context context, long rawContactId) {
        Object obj = null;
        Cursor cursor = null;
        if (rawContactId > 0) {
            try {
                cursor = context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"vibration_type"}, "_id=" + rawContactId, null, null);
                if (cursor.moveToFirst()) {
                    obj = cursor.getString(0);
                }
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (SQLiteException e) {
                Log.e(TAG, "Execption during query");
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (!(cursor == null || cursor.isClosed())) {
                    cursor.close();
                }
            }
        }
        if (TextUtils.isEmpty(obj)) {
            return context.getString(R.string.default_option);
        }
        return obj.replace("_", HwCustPreloadContacts.EMPTY_STRING).toUpperCase(Locale.US);
    }

    public void setupCustomFlattenedList(ContactDetailFragment contactDetailFragment) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && this.mVibrationEntries != null && this.mVibrationEntries.size() > 0) {
            Collapser.collapseList(this.mVibrationEntries, false);
            contactDetailFragment.flattenList(this.mVibrationEntries);
        }
    }

    public void setupCustomFlattenedList(ContactDetailFragment contactDetailFragment, Object object) {
        if (HwCustContactFeatureUtils.isVibrationPatternRequired() && this.mVibrationEntries != null && this.mVibrationEntries.size() > 0) {
            Collapser.collapseList(this.mVibrationEntries, false);
            contactDetailFragment.flattenList(this.mVibrationEntries, object);
        }
    }

    public boolean checkAndInitCall(Context aContext, Intent aIntent) {
        if (aIntent == null || aContext == null) {
            return false;
        }
        return HwCustContactFeatureUtils.checkAndInitCall(aContext, PhoneNumberUtils.getNumberFromIntent(aIntent, aContext));
    }
}
