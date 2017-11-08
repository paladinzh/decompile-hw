package com.android.contacts.hap.camcard.bcr;

import android.app.IntentService;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract.Contacts;
import com.android.contacts.ContactSaveService;
import com.android.contacts.editor.ContactEditorUtils;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContact;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountType;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.util.HwLog;
import java.io.File;

public class CCSaveService extends IntentService {
    private static final String TAG = CCSaveService.class.getSimpleName();

    private void multiRecoSave(Intent intent) {
        Context context = this;
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this);
        ContactEditorUtils editorUtils = ContactEditorUtils.getInstance(this);
        editorUtils.setExcludeSim(true);
        editorUtils.setExcludeSim1(true);
        editorUtils.setExcludeSim2(true);
        AccountWithDataSet defaultAccount = null;
        if (!editorUtils.shouldShowAccountChangedNotification()) {
            defaultAccount = editorUtils.getDefaultAccount();
        }
        if (defaultAccount == null) {
            defaultAccount = accountTypes.getAccountWithDataSet(0);
        }
        if (defaultAccount != null) {
            AccountType accountType = accountTypes.getAccountType(defaultAccount.type, defaultAccount.dataSet);
            ContentValues values = new ContentValues();
            values.put("is_camcard", Integer.valueOf(2));
            RawContact rawContact = new RawContact(values);
            rawContact.setAccount(defaultAccount);
            RawContactDelta rawContactDelta = new RawContactDelta(ValuesDelta.fromAfter(rawContact.getValues()));
            Bundle extra = intent.getExtras();
            String path = intent.getStringExtra("photo_path");
            RawContactModifier.parseExtras(this, accountType, rawContactDelta, extra);
            RawContactDeltaList mState = RawContactDeltaList.fromSingle(rawContactDelta);
            Bundle updatePhoto = new Bundle();
            if (path != null) {
                updatePhoto.putParcelable(String.valueOf(-1), CCardPhotoUtils.generatePhotoUri(this, new File(path)));
            }
            Intent serviceIntent = ContactSaveService.createSaveContactIntent(this, mState, "saveMode", 0, false, null, null, updatePhoto);
            serviceIntent.putExtra("key_from_camcard", true);
            startService(serviceIntent);
            if (HwLog.HWDBG) {
                HwLog.d(TAG, "start ContactSaveService");
            }
        }
    }

    public CCSaveService() {
        super(TAG);
    }

    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            if ("updateCard".equals(intent.getAction())) {
                update();
            } else if ("groupUpdate".equals(intent.getAction())) {
                CamcardGroup.updateGroupSync2Title(this);
            } else if ("multirecognize_save".equals(intent.getAction())) {
                multiRecoSave(intent);
            }
        }
    }

    private void update() {
        Uri contactUri = Contacts.CONTENT_URI;
        ContentValues values = new ContentValues();
        values.put("is_camcard", Integer.valueOf(1));
        int count = getContentResolver().update(contactUri, values, "is_camcard=2", null);
        if (HwLog.HWDBG) {
            HwLog.d(TAG, "update count " + count);
        }
    }

    public static Intent createMultiRecognizeIntent(Context context) {
        Intent intent = new Intent();
        intent.setPackage("com.huawei.contactscamcard");
        intent.setClassName("com.huawei.contactscamcard", "com.huawei.contactscamcard.bcr.CamCardRecognizeService");
        return intent;
    }

    public static Intent createMultiUpdateIntent(Context context) {
        Intent intent = new Intent(context, CCSaveService.class);
        intent.setAction("updateCard");
        return intent;
    }

    public static Intent createGroupUpdateIntent(Context context) {
        Intent intent = new Intent(context, CCSaveService.class);
        intent.setAction("groupUpdate");
        return intent;
    }
}
