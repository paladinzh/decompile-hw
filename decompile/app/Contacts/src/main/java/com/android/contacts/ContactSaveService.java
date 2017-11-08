package com.android.contacts;

import android.app.Activity;
import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteFullException;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.RemoteException;
import android.provider.ContactsContract.AggregationExceptions;
import android.provider.ContactsContract.Contacts;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.Groups;
import android.provider.ContactsContract.Profile;
import android.provider.ContactsContract.RawContacts;
import android.provider.ContactsContract.RawContactsEntity;
import android.text.TextUtils;
import android.view.ContextThemeWrapper;
import android.widget.Toast;
import com.amap.api.services.core.AMapException;
import com.android.contacts.ContactsUtils.PredefinedNumbers;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.calllog.ContactInfo;
import com.android.contacts.calllog.ContactInfoHelper;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.fragment.HwBaseFragment;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.hap.camcard.groups.CamcardGroup;
import com.android.contacts.hap.delete.ExtendedContactSaveService;
import com.android.contacts.hap.delete.ExtendedContactSaveService.EfidIndexObject;
import com.android.contacts.hap.hotline.HLUtils;
import com.android.contacts.hap.sim.IIccPhoneBookAdapter;
import com.android.contacts.hap.sim.SimFactory;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.hap.sim.SimUtility;
import com.android.contacts.hap.util.GroupMemberEditHelper;
import com.android.contacts.hap.util.GroupMemberEditHelper.GroupInfo;
import com.android.contacts.hap.util.IntentServiceWithWakeLock;
import com.android.contacts.model.AccountTypeManager;
import com.android.contacts.model.RawContactDelta;
import com.android.contacts.model.RawContactDeltaList;
import com.android.contacts.model.RawContactModifier;
import com.android.contacts.model.ValuesDelta;
import com.android.contacts.model.account.AccountWithDataSet;
import com.android.contacts.statistical.ContactEditorReport;
import com.android.contacts.statistical.StatisticalHelper;
import com.android.contacts.util.CallerInfoCacheUtils;
import com.android.contacts.util.ContactPhotoUtils;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.UriUtils;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.R;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import huawei.cust.HwCustUtils;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

public class ContactSaveService extends IntentServiceWithWakeLock {
    private static final HashSet<String> ALLOWED_DATA_COLUMNS = Sets.newHashSet("mimetype", "is_primary", "data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10", "data11", "data12", "data13", "data14", "data15");
    private static final boolean DEBUG = HwLog.HWDBG;
    private static final CopyOnWriteArrayList<Listener> sListeners = new CopyOnWriteArrayList();
    HwCustContactSaveService mCust = null;
    protected IIccPhoneBookAdapter mIccPhoneBookAdapter;
    private Handler mMainHandler;

    private interface ContactEntityQuery {
        public static final String[] PROJECTION = new String[]{"data_id", "contact_id", "is_super_primary"};
    }

    private interface JoinContactQuery {
        public static final String[] PROJECTION = new String[]{"_id", "contact_id", "display_name_source"};
    }

    public interface Listener {
        void onServiceCompleted(Intent intent);
    }

    public ContactSaveService() {
        super("ContactSaveService");
        setIntentRedelivery(true);
        this.mMainHandler = new Handler(Looper.getMainLooper());
        if (EmuiFeatureManager.isProductCustFeatureEnable()) {
            this.mCust = (HwCustContactSaveService) HwCustUtils.createObj(HwCustContactSaveService.class, new Object[0]);
        }
    }

    public static void registerListener(Listener listener) {
        if (!(listener instanceof Activity) && !(listener instanceof HwBaseFragment)) {
            throw new ClassCastException("Only activities can be registered to receive callback from " + ContactSaveService.class.getName());
        } else if (!sListeners.contains(listener)) {
            sListeners.add(0, listener);
        }
    }

    public static void unregisterListener(Listener listener) {
        sListeners.remove(listener);
    }

    public Object getSystemService(String name) {
        Object service = super.getSystemService(name);
        if (service != null) {
            return service;
        }
        return getApplicationContext().getSystemService(name);
    }

    protected void doWakefulWork(Intent intent) {
        String action = intent.getAction();
        if ("newRawContact".equals(action)) {
            createRawContact(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("saveContact".equals(action)) {
            saveContact(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("createGroup".equals(action)) {
            createGroup(intent);
        } else if ("renameGroup".equals(action)) {
            renameGroup(intent);
        } else if ("deleteGroup".equals(action)) {
            deleteGroup(intent);
        } else if ("updateGroup".equals(action)) {
            updateGroup(intent);
        } else if ("setStarred".equals(action)) {
            setStarred(intent);
        } else if ("setSuperPrimary".equals(action)) {
            setSuperPrimary(intent);
        } else if ("clearPrimary".equals(action)) {
            clearPrimary(intent);
        } else if ("delete".equals(action)) {
            deleteContact(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("joinContacts".equals(action)) {
            joinContacts(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("sendToVoicemail".equals(action)) {
            setSendToVoicemail(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("setRingtone".equals(action)) {
            setRingtone(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("setRingtone_detail".equals(action)) {
            setRingtoneDetail(intent);
            CallerInfoCacheUtils.sendUpdateCallerInfoCacheIntent(this);
        } else if ("deleteMultipleGroup".equals(action)) {
            deleteMultipleGroups(intent);
        }
    }

    public static Intent createNewRawContactIntent(Context context, ArrayList<ContentValues> values, AccountWithDataSet account, Class<?> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("newRawContact");
        if (account != null) {
            serviceIntent.putExtra("accountName", account.name);
            serviceIntent.putExtra("accountType", account.type);
            serviceIntent.putExtra("dataSet", account.dataSet);
        }
        serviceIntent.putParcelableArrayListExtra("contentValues", values);
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void createRawContact(Intent intent) {
        String accountName = intent.getStringExtra("accountName");
        String accountType = intent.getStringExtra("accountType");
        String dataSet = intent.getStringExtra("dataSet");
        List<ContentValues> valueList = intent.getParcelableArrayListExtra("contentValues");
        Intent callbackIntent = (Intent) intent.getParcelableExtra("callbackIntent");
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        operations.add(ContentProviderOperation.newInsert(RawContacts.CONTENT_URI).withValue("account_name", accountName).withValue("account_type", accountType).withValue("data_set", dataSet).build());
        if (valueList != null) {
            int size = valueList.size();
            for (int i = 0; i < size; i++) {
                ContentValues values = (ContentValues) valueList.get(i);
                values.keySet().retainAll(ALLOWED_DATA_COLUMNS);
                operations.add(ContentProviderOperation.newInsert(Data.CONTENT_URI).withValueBackReference("raw_contact_id", 0).withValues(values).build());
            }
        }
        ContentResolver resolver = getContentResolver();
        try {
            callbackIntent.setData(RawContacts.getContactLookupUri(resolver, resolver.applyBatch("com.android.contacts", operations)[0].uri));
            deliverCallback(callbackIntent);
        } catch (Exception e) {
            throw new RuntimeException("Failed to store new contact", e);
        }
    }

    public static Intent createSaveContactIntent(Context context, RawContactDeltaList state, String saveModeExtraKey, int saveMode, boolean isProfile, Class<? extends Activity> callbackActivity, String callbackAction, long rawContactId, Uri updatedPhotoPath) {
        Bundle bundle = new Bundle();
        bundle.putParcelable(String.valueOf(rawContactId), updatedPhotoPath);
        return createSaveContactIntent(context, state, saveModeExtraKey, saveMode, isProfile, callbackActivity, callbackAction, bundle);
    }

    public static Intent createSaveContactIntent(Context context, RawContactDeltaList state, String saveModeExtraKey, int saveMode, boolean isProfile, Class<? extends Activity> callbackActivity, String callbackAction, ArrayList<Long> list, Uri updatedPhotoPath) {
        Bundle bundle = new Bundle();
        if (!(list == null || list.isEmpty())) {
            for (Long rawContactId : list) {
                bundle.putParcelable(String.valueOf(rawContactId), updatedPhotoPath);
            }
        }
        return createSaveContactIntent(context, state, saveModeExtraKey, saveMode, isProfile, callbackActivity, callbackAction, bundle);
    }

    public static Intent createSaveContactIntent(Context context, RawContactDeltaList state, String saveModeExtraKey, int saveMode, boolean isProfile, Class<? extends Activity> callbackActivity, String callbackAction, Bundle updatedPhotos) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("saveContact");
        serviceIntent.putExtra("state", state);
        serviceIntent.putExtra("saveIsProfile", isProfile);
        if (updatedPhotos != null) {
            serviceIntent.putExtra("updatedPhotos", updatedPhotos);
        }
        if (callbackActivity != null) {
            Intent callbackIntent = new Intent(context, callbackActivity);
            callbackIntent.putExtra(saveModeExtraKey, saveMode);
            callbackIntent.setAction(callbackAction);
            serviceIntent.putExtra("callbackIntent", callbackIntent);
        }
        return serviceIntent;
    }

    public static Intent createSaveContactIntentWithToken(Context context, RawContactDeltaList state, String saveModeExtraKey, int saveMode, boolean isProfile, Class<? extends Activity> callbackActivity, String callbackAction, Bundle updatedPhotos, long token) {
        Intent serviceIntent = createSaveContactIntent(context, state, saveModeExtraKey, saveMode, isProfile, callbackActivity, callbackAction, updatedPhotos);
        Intent callbackIntent = (Intent) serviceIntent.getParcelableExtra("callbackIntent");
        if (callbackIntent != null) {
            callbackIntent.putExtra("listenerToken", token);
        }
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void sendDelayedBroadCastToUpdateFavoriteWidget() {
        if (DEBUG) {
            HwLog.d("ContactSaveService", "sendDelayedBroadCastToUpdateFavoriteWidget!!");
        }
        this.mMainHandler.postDelayed(new Runnable() {
            public void run() {
                CommonUtilMethods.updateFavoritesWidget(ContactSaveService.this.getApplicationContext());
            }
        }, 300);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void saveContact(Intent intent) {
        Uri lookupUri;
        Uri lookupUri2;
        Throwable e;
        Exception e2;
        Uri photoUri;
        RawContactDeltaList<RawContactDelta> state = (RawContactDeltaList) intent.getParcelableExtra("state");
        boolean isProfile = intent.getBooleanExtra("saveIsProfile", false);
        Bundle updatedPhotos = (Bundle) intent.getParcelableExtra("updatedPhotos");
        ArrayList<Long> aRawContactIds = new ArrayList();
        AccountTypeManager accountTypes = AccountTypeManager.getInstance(this);
        RawContactModifier.trimEmpty(getApplicationContext(), state, accountTypes, true);
        int starred = 0;
        boolean isSimContact = false;
        Intent callbackIntent = (Intent) intent.getParcelableExtra("callbackIntent");
        if (callbackIntent != null) {
            if (intent.getBooleanExtra("launch_contact_details_before_contact_saved", false)) {
                callbackIntent.putExtra("refresh_details", true);
            }
        }
        StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_SERVICE_MAINTENANCE, ContactEditorReport.checkRawContactDeltaList(state, getApplicationContext()));
        long rawContactId;
        ContentResolver resolver;
        boolean succeeded;
        long insertedRawContactId;
        int tries;
        Cursor c;
        int index;
        int photoSize;
        boolean saveSuccessed;
        if (state != null && !state.isEmpty()) {
            for (RawContactDelta delta : state) {
                ValuesDelta values = delta.getValues();
                if (starred == 0) {
                    starred = values.getAsInteger("starred", Integer.valueOf(0)).intValue();
                }
                String accountType = values.getAsString("account_type");
                if (HwLog.HWFLOW) {
                    HwLog.i("ContactSaveService", "accountTyp in save contact" + accountType);
                }
                int slotId = SimFactoryManager.getSlotIdBasedOnAccountType(accountType);
                if (CommonUtilMethods.isSimAccount(accountType)) {
                    boolean isSimBusy;
                    isSimContact = true;
                    SharedPreferences prefs = SimFactoryManager.getSharedPreferences("SimInfoFile", slotId);
                    if (prefs.getBoolean("sim_delete_progress", false)) {
                        isSimBusy = true;
                    } else {
                        isSimBusy = prefs.getBoolean("sim_copy_contacts_progress", false);
                    }
                    if (isSimBusy) {
                        HwLog.i("ContactSaveService", "Sim is currently busy. Cannot save contact");
                        showToast(String.format(getString(R.string.str_sim_busy), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                        deliverCallback(callbackIntent);
                        return;
                    }
                    SimFactory lSimFactory = SimFactoryManager.getSimFactory(accountType);
                    if (lSimFactory != null) {
                        try {
                            int lAvailableFreeSpace = SimFactoryManager.getSimConfig(accountType).getAvailableFreeSpace();
                            int lTotalSpace = SimFactoryManager.getSimConfig(accountType).getSimCapacity();
                            HwLog.i("ContactSaveService", "lAvailableFreeSpace in contact save service:" + lAvailableFreeSpace);
                            HwLog.i("ContactSaveService", "lTotalSpace in contact save service:" + lTotalSpace);
                            if ((lAvailableFreeSpace == 0 && lTotalSpace == 0) || isSimExt1Full(slotId, values, accountType)) {
                                HwLog.i("ContactSaveService", "Unable to retrieve the available space");
                                showToast(String.format(getString(R.string.sim_save_unknown_error), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                                deliverCallback(callbackIntent);
                                return;
                            } else if (lAvailableFreeSpace == 0 && state.findRawContactId() == -1) {
                                HwLog.i("ContactSaveService", "Sim is full. Cannot save contact.");
                                String mSimCardDisplayLabel = SimFactoryManager.getSimCardDisplayLabel(accountType);
                                showToast(String.format(getString(R.string.sim_full), new Object[]{mSimCardDisplayLabel, mSimCardDisplayLabel}));
                                deliverCallback(callbackIntent);
                                return;
                            } else {
                                int rowsInserted = lSimFactory.getSimPersistanceManager().save(state);
                                rawContactId = state.findRawContactId();
                                if (rawContactId > 0) {
                                    aRawContactIds.add(Long.valueOf(rawContactId));
                                }
                                if (rowsInserted > 0) {
                                    RawContactModifier.trimEmpty((RawContactDeltaList) state, accountTypes);
                                } else {
                                    HwLog.i("ContactSaveService", "Unable to save the contact in SIM ");
                                    if (lSimFactory.getSimConfig().isEmailEnabled()) {
                                        for (RawContactDelta mimeEntries : state) {
                                            ArrayList<ValuesDelta> emailEntries = mimeEntries.getMimeEntries("vnd.android.cursor.item/email_v2");
                                            if (!(emailEntries == null || emailEntries.isEmpty())) {
                                                ValuesDelta lEmailDelta = (ValuesDelta) emailEntries.get(0);
                                                if (lEmailDelta.isDelete()) {
                                                    continue;
                                                } else {
                                                    if (!TextUtils.isEmpty(lEmailDelta.getAsString("data1")) && SimFactoryManager.getSpareEmailCount(slotId) == 0) {
                                                        showToast(String.format(getString(R.string.email_full), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                                                        deliverCallback(callbackIntent);
                                                        return;
                                                    }
                                                }
                                            }
                                        }
                                    }
                                    showToast(String.format(getString(R.string.sim_save_unknown_error), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                                    deliverCallback(callbackIntent);
                                    return;
                                }
                            }
                        } catch (Exception e3) {
                            showToast(String.format(getString(R.string.sim_not_ready), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                            if (1 != null) {
                                ExceptionCapture.captureSimContactSaveException("slotId:" + slotId + " Current SimState --> " + SimFactoryManager.getSimState(slotId) + "; Saving SIM contact to database error: " + e3, e3);
                            }
                            deliverCallback(callbackIntent);
                            return;
                        }
                    }
                    HwLog.i("ContactSaveService", "SIM factory unavailable in ContactSaveService");
                    showToast(String.format(getString(R.string.str_sim_busy), new Object[]{SimFactoryManager.getSimCardDisplayLabel(accountType)}));
                    deliverCallback(callbackIntent);
                    return;
                }
            }
            resolver = getContentResolver();
            succeeded = false;
            insertedRawContactId = -1;
            boolean isCamCard = intent.getBooleanExtra("key_from_camcard", false);
            tries = 0;
            lookupUri = null;
            while (true) {
                int tries2 = tries + 1;
                if (tries >= 3) {
                    break;
                }
                try {
                    ArrayList<ContentProviderOperation> diff = state.buildDiff();
                    if (isCamCard) {
                        diff.add(CamcardGroup.addDiff(this, state));
                    }
                    ContentProviderResult[] contentProviderResultArr = null;
                    if (diff.isEmpty()) {
                        if (updatedPhotos == null) {
                            break;
                        }
                    }
                    try {
                        contentProviderResultArr = resolver.applyBatch("com.android.contacts", diff);
                        if (isSimContact) {
                            ExceptionCapture.checkSimContactSaveResult(contentProviderResultArr, "the returned value is not correct when saving SIM contact to database");
                        } else {
                            ExceptionCapture.checkContactSaveResult(contentProviderResultArr, "the returned value is not correct when saving contact to database");
                        }
                    } catch (SQLiteFullException e4) {
                        showToast(getString(R.string.str_databasefull));
                        if (callbackIntent != null) {
                            deliverCallback(callbackIntent);
                        }
                        return;
                    } catch (Exception e5) {
                        if (isSimContact) {
                            ExceptionCapture.captureSimContactSaveException("saving SIM contact to database error: " + e5, e5);
                        } else {
                            ExceptionCapture.captureContactSaveException("saving contact to database error: " + e5, e5);
                        }
                        if (callbackIntent != null) {
                            deliverCallback(callbackIntent);
                        }
                        return;
                    }
                    rawContactId = getRawContactId(state, diff, contentProviderResultArr);
                    if (rawContactId != -1) {
                        if (this.mCust != null) {
                            this.mCust.updateVibrationPatternIntoDatabase(getApplicationContext(), intent, rawContactId);
                        }
                        insertedRawContactId = getInsertedRawContactId(diff, contentProviderResultArr);
                        if (!isProfile) {
                            Uri rawContactUri = ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId);
                            lookupUri2 = RawContacts.getContactLookupUri(resolver, rawContactUri);
                            if (lookupUri2 != null || rawContactUri == null) {
                                break;
                            }
                            deleteContactsFromSIM(aRawContactIds, this);
                            showToast(getString(R.string.no_contact_details));
                            break;
                        }
                        c = resolver.query(Profile.CONTENT_URI, new String[]{"_id", "lookup"}, null, null, null);
                        if (c == null) {
                            break;
                        }
                        if (c.moveToFirst()) {
                            lookupUri2 = Contacts.getLookupUri(c.getLong(0), c.getString(1));
                        } else {
                            Intent intent2 = new Intent("com.android.huawei.profile_exists");
                            intent2.putExtra("profile_exists", false);
                            sendBroadcast(intent2);
                            lookupUri2 = lookupUri;
                        }
                        try {
                            break;
                        } catch (RemoteException e6) {
                            e = e6;
                        } catch (OperationApplicationException e7) {
                            e2 = e7;
                        }
                    } else {
                        break;
                    }
                } catch (RemoteException e8) {
                    e = e8;
                    lookupUri2 = lookupUri;
                } catch (OperationApplicationException e9) {
                    e2 = e9;
                    lookupUri2 = lookupUri;
                } catch (Throwable th) {
                    c.close();
                }
                if (updatedPhotos != null) {
                    index = 0;
                    photoSize = updatedPhotos.size();
                    for (String key : updatedPhotos.keySet()) {
                        index++;
                        photoUri = (Uri) updatedPhotos.getParcelable(key);
                        rawContactId = Long.parseLong(key);
                        if (rawContactId < 0) {
                            rawContactId = insertedRawContactId;
                            if (insertedRawContactId == -1) {
                            }
                        }
                        if (isSimContact) {
                            getContentResolver().delete(photoUri, null, null);
                        } else {
                            if (index < photoSize) {
                                saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, false);
                            } else {
                                saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, true);
                            }
                            if (saveSuccessed) {
                                succeeded = false;
                            }
                        }
                    }
                }
                if (starred == 0 && lookupUri2 != null) {
                    c = getContentResolver().query(lookupUri2, new String[]{"starred"}, null, null, null);
                    if (c != null) {
                        if (c.moveToFirst()) {
                            starred = c.getInt(0);
                        }
                        c.close();
                    } else {
                        HwLog.i("ContactSaveService", "curosr is null when starred equal 0.");
                    }
                }
                if (starred == 1) {
                    sendDelayedBroadCastToUpdateFavoriteWidget();
                }
                if (succeeded) {
                    updateCallLogCacheDataBackground(lookupUri2);
                }
                if (callbackIntent != null) {
                    if (succeeded) {
                        callbackIntent.putExtra("saveSucceeded", true);
                    }
                    callbackIntent.setData(lookupUri2);
                    deliverCallback(callbackIntent);
                }
                return;
                tries = tries2;
                lookupUri = lookupUri2;
            }
            HwLog.v("ContactSaveService", "Saved contact. New URI: " + lookupUri2);
            if (isSimContact && lookupUri2 != null) {
                long contactId = ContentUris.parseId(lookupUri2);
                if (DEBUG) {
                    HwLog.d("ContactSaveService", "Adding Contactid " + contactId + " IN cache");
                }
            }
            succeeded = true;
            if (updatedPhotos != null) {
                index = 0;
                photoSize = updatedPhotos.size();
                for (String key2 : updatedPhotos.keySet()) {
                    index++;
                    photoUri = (Uri) updatedPhotos.getParcelable(key2);
                    rawContactId = Long.parseLong(key2);
                    if (rawContactId < 0) {
                        rawContactId = insertedRawContactId;
                        if (insertedRawContactId == -1) {
                        }
                    }
                    if (isSimContact) {
                        if (index < photoSize) {
                            saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, true);
                        } else {
                            saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, false);
                        }
                        if (saveSuccessed) {
                            succeeded = false;
                        }
                    } else {
                        getContentResolver().delete(photoUri, null, null);
                    }
                }
            }
            c = getContentResolver().query(lookupUri2, new String[]{"starred"}, null, null, null);
            if (c != null) {
                HwLog.i("ContactSaveService", "curosr is null when starred equal 0.");
            } else {
                if (c.moveToFirst()) {
                    starred = c.getInt(0);
                }
                c.close();
            }
            if (starred == 1) {
                sendDelayedBroadCastToUpdateFavoriteWidget();
            }
            if (succeeded) {
                updateCallLogCacheDataBackground(lookupUri2);
            }
            if (callbackIntent != null) {
                if (succeeded) {
                    callbackIntent.putExtra("saveSucceeded", true);
                }
                callbackIntent.setData(lookupUri2);
                deliverCallback(callbackIntent);
            }
            return;
        }
        return;
        HwLog.e("ContactSaveService", "Problem persisting user edits", e);
        if (isSimContact) {
            ExceptionCapture.captureSimContactSaveException("saving SIM contact to database error: " + e, e);
        } else {
            ExceptionCapture.captureContactSaveException("saving contact to database error: " + e, e);
        }
        if (updatedPhotos != null) {
            index = 0;
            photoSize = updatedPhotos.size();
            for (String key22 : updatedPhotos.keySet()) {
                index++;
                photoUri = (Uri) updatedPhotos.getParcelable(key22);
                rawContactId = Long.parseLong(key22);
                if (rawContactId < 0) {
                    rawContactId = insertedRawContactId;
                    if (insertedRawContactId == -1) {
                    }
                }
                if (isSimContact) {
                    getContentResolver().delete(photoUri, null, null);
                } else {
                    if (index < photoSize) {
                        saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, false);
                    } else {
                        saveSuccessed = saveUpdatedPhoto(rawContactId, photoUri, true);
                    }
                    if (saveSuccessed) {
                        succeeded = false;
                    }
                }
            }
        }
        c = getContentResolver().query(lookupUri2, new String[]{"starred"}, null, null, null);
        if (c != null) {
            if (c.moveToFirst()) {
                starred = c.getInt(0);
            }
            c.close();
        } else {
            HwLog.i("ContactSaveService", "curosr is null when starred equal 0.");
        }
        if (starred == 1) {
            sendDelayedBroadCastToUpdateFavoriteWidget();
        }
        if (succeeded) {
            updateCallLogCacheDataBackground(lookupUri2);
        }
        if (callbackIntent != null) {
            if (succeeded) {
                callbackIntent.putExtra("saveSucceeded", true);
            }
            callbackIntent.setData(lookupUri2);
            deliverCallback(callbackIntent);
        }
        return;
        HwLog.w("ContactSaveService", "Version consistency failed, re-parenting: " + e2.toString());
        if (isSimContact) {
            ExceptionCapture.captureSimContactSaveException("saving SIM contact to database error: " + e2, e2);
        } else {
            ExceptionCapture.captureContactSaveException("saving contact to database error: " + e2, e2);
        }
        StringBuilder stringBuilder = new StringBuilder("_id IN(");
        boolean first = true;
        int count = state.size();
        for (int i = 0; i < count; i++) {
            Long rawContactId2 = state.getRawContactId(i);
            if (!(rawContactId2 == null || rawContactId2.longValue() == -1)) {
                if (!first) {
                    stringBuilder.append(',');
                }
                stringBuilder.append(rawContactId2);
                first = false;
            }
        }
        stringBuilder.append(")");
        if (first) {
            showToast(getString(R.string.contactSavedErrorToast_Toast));
            if (callbackIntent != null) {
                deliverCallback(callbackIntent);
            }
            return;
        }
        Uri uri;
        if (isProfile) {
            uri = RawContactsEntity.PROFILE_CONTENT_URI;
        } else {
            uri = RawContactsEntity.CONTENT_URI;
        }
        state = RawContactDeltaList.mergeAfter(RawContactDeltaList.fromQuery(uri, resolver, stringBuilder.toString(), null, null), state);
        if (isProfile) {
            for (RawContactDelta delta2 : state) {
                delta2.setProfileQueryUri();
            }
        }
        tries = tries2;
        lookupUri = lookupUri2;
    }

    private boolean isSimExt1Full(int slotId, ValuesDelta values, String accountType) {
        if (SimFactoryManager.isDualSim()) {
            this.mIccPhoneBookAdapter = new IIccPhoneBookAdapter(slotId);
        } else {
            this.mIccPhoneBookAdapter = new IIccPhoneBookAdapter();
        }
        if (values == null || values.getPhoneNumber() == null || values.getPhoneNumber().length() <= 20 || this.mIccPhoneBookAdapter.getAvailableSimExt1FreeSpace(slotId) > 0) {
            HwLog.i("ContactSaveService", "EXT Sim is not full.");
            return false;
        }
        HwLog.i("ContactSaveService", "EXT Sim is full. Cannot save contact.");
        return true;
    }

    private void updateCallLogCacheDataBackground(final Uri lookupUri) {
        if (lookupUri != null) {
            ContactsThreadPool.getInstance().execute(new Runnable() {
                public void run() {
                    Context context = ContactSaveService.this.getApplication();
                    Cursor cursor = null;
                    Uri entityUri = Uri.withAppendedPath(lookupUri, "entities");
                    ContentResolver resolver = ContactSaveService.this.getContentResolver();
                    cursor = resolver.query(entityUri, new String[]{"data1"}, "mimetype=?", new String[]{"vnd.android.cursor.item/phone_v2"}, null);
                    if (cursor != null) {
                        while (cursor.moveToNext()) {
                            String newNumber = ContactsUtils.removeDashesAndBlanks(cursor.getString(0));
                            ContentValues values = new ContentValues();
                            Map<String, PredefinedNumbers> specialNumbersMap = ContactsUtils.getPredefinedMap(context);
                            if (specialNumbersMap == null || !specialNumbersMap.containsKey(newNumber)) {
                                try {
                                    String defaultCountryIso = GeoUtil.getCurrentCountryIso(context);
                                    ContactInfo info = new ContactInfoHelper(context, defaultCountryIso).lookupNumber(newNumber, defaultCountryIso);
                                    if (info != null) {
                                        values.put("name", info.name);
                                        values.put("numbertype", Integer.valueOf(info.type));
                                        values.put("numberlabel", info.label);
                                        values.put("lookup_uri", UriUtils.uriToString(info.lookupUri));
                                        values.put("matched_number", info.number);
                                        values.put("normalized_number", info.normalizedNumber);
                                        values.put("photo_id", Long.valueOf(info.photoId));
                                        values.put("formatted_number", info.formattedNumber);
                                        resolver.update(QueryUtil.getCallsContentUri(), values, "number = ?", new String[]{newNumber});
                                    } else {
                                        return;
                                    }
                                } finally {
                                    if (cursor != null) {
                                        cursor.close();
                                    }
                                }
                            } else {
                                if (cursor != null) {
                                    cursor.close();
                                }
                                return;
                            }
                        }
                    }
                    if (cursor != null) {
                        cursor.close();
                    }
                }
            });
        }
    }

    private boolean saveUpdatedPhoto(long rawContactId, Uri photoUri, boolean deleteAfterSave) {
        return ContactPhotoUtils.savePhotoFromUriToUri(this, photoUri, Uri.withAppendedPath(ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactId), "display_photo"), deleteAfterSave);
    }

    private void deleteContactsFromSIM(ArrayList<Long> aRawContactIds, Context aContext) {
        for (EfidIndexObject obj : ExtendedContactSaveService.getEfidAndIndexForSimRawContact(aRawContactIds, aContext)) {
            ExtendedContactSaveService.deleteSingleSimContactFromSim(obj, aContext);
        }
    }

    private int deleteSingleContactFromSIM(long contactId) {
        Cursor cursor = getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"sync1", "sync2", "account_type"}, "contact_id=" + contactId, null, null);
        EfidIndexObject object = new EfidIndexObject();
        int count = 0;
        if (cursor.moveToFirst()) {
            object.efid = cursor.getString(0);
            object.index = cursor.getString(1);
            object.accountType = cursor.getString(2);
            count = ExtendedContactSaveService.deleteSingleSimContactFromSim(object, getApplicationContext());
        }
        cursor.close();
        if (count > 0) {
            HwLog.i("ContactSaveService", "Removing SIM contacts from the contacts cache");
        }
        return count;
    }

    private long getRawContactId(RawContactDeltaList state, ArrayList<ContentProviderOperation> diff, ContentProviderResult[] results) {
        long existingRawContactId = state.findRawContactId();
        if (existingRawContactId != -1) {
            return existingRawContactId;
        }
        return getInsertedRawContactId(diff, results);
    }

    private long getInsertedRawContactId(ArrayList<ContentProviderOperation> diff, ContentProviderResult[] results) {
        int diffSize = diff.size();
        if (results == null || results.length == 0) {
            return -1;
        }
        for (int i = 0; i < diffSize; i++) {
            ContentProviderOperation operation = (ContentProviderOperation) diff.get(i);
            String strEmcodePath = operation.getUri().getEncodedPath();
            if (operation.getType() == 1 && strEmcodePath != null && strEmcodePath.contains(RawContacts.CONTENT_URI.getEncodedPath())) {
                return ContentUris.parseId(results[i].uri);
            }
        }
        return -1;
    }

    public static Intent createNewGroupIntent(Context context, AccountWithDataSet account, String label, long[] rawContactsToAdd, Class<? extends Activity> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("createGroup");
        serviceIntent.putExtra("accountType", account.type);
        serviceIntent.putExtra("accountName", account.name);
        serviceIntent.putExtra("dataSet", account.dataSet);
        serviceIntent.putExtra("groupLabel", label);
        serviceIntent.putExtra("rawContactsToAdd", rawContactsToAdd);
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void createGroup(Intent intent) {
        String accountType = intent.getStringExtra("accountType");
        String accountName = intent.getStringExtra("accountName");
        String dataSet = intent.getStringExtra("dataSet");
        String label = intent.getStringExtra("groupLabel");
        long[] rawContactsToAdd = intent.getLongArrayExtra("rawContactsToAdd");
        boolean isAssertRequired = intent.getBooleanExtra("isFromGroups", false);
        ContentValues values = new ContentValues();
        values.put("account_type", accountType);
        values.put("account_name", accountName);
        values.put("data_set", dataSet);
        values.put("title", label);
        values.put("group_visible", Integer.valueOf(0));
        ContentResolver resolver = getContentResolver();
        if (isGroupNameExisted(accountType, accountName, dataSet, label)) {
            showToast(getString(R.string.contact_group_existed));
            return;
        }
        Uri groupUri = resolver.insert(Groups.CONTENT_URI.buildUpon().appendQueryParameter("DO_VISIBLE_TOUCHED", "0").build(), values);
        if (groupUri == null) {
            HwLog.e("ContactSaveService", "Couldn't create group with label " + label);
            return;
        }
        GroupInfo info = new GroupInfo(ContentUris.parseId(groupUri), 0, isAssertRequired, rawContactsToAdd);
        new GroupMemberEditHelper(getContentResolver()).executeInPararell(info);
        values.clear();
        values.put("mimetype", "vnd.android.cursor.item/group_membership");
        values.put("data1", Long.valueOf(ContentUris.parseId(groupUri)));
        Intent callbackIntent = (Intent) intent.getParcelableExtra("callbackIntent");
        callbackIntent.setData(groupUri);
        callbackIntent.putExtra(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, Lists.newArrayList(values));
        deliverCallback(callbackIntent);
    }

    public static Intent createGroupRenameIntent(Context context, long groupId, AccountWithDataSet account, String newLabel, long[] rawContactsToAdd, long[] contactsToRemove, Class<? extends Activity> callbackActivity, String callbackAction, boolean isPredefined) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("renameGroup");
        serviceIntent.putExtra("groupId", groupId);
        serviceIntent.putExtra("accountType", account.type);
        serviceIntent.putExtra("accountName", account.name);
        serviceIntent.putExtra("dataSet", account.dataSet);
        serviceIntent.putExtra("groupLabel", newLabel);
        serviceIntent.putExtra("rawContactsToAdd", rawContactsToAdd);
        serviceIntent.putExtra("contactsToRemove", contactsToRemove);
        serviceIntent.putExtra("predefinedgroupedited", isPredefined);
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void renameGroup(Intent intent) {
        long groupId = intent.getLongExtra("groupId", -1);
        String label = intent.getStringExtra("groupLabel");
        if (groupId == -1) {
            HwLog.e("ContactSaveService", "Invalid arguments for renameGroup request");
        } else if (isGroupNameExisted(intent.getStringExtra("accountType"), intent.getStringExtra("accountName"), intent.getStringExtra("dataSet"), label)) {
            showToast(getString(R.string.contact_group_existed));
        } else {
            updateGroup(intent);
        }
    }

    public static Intent createGroupDeletionIntent(Context context, long groupId) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("deleteGroup");
        serviceIntent.putExtra("groupId", groupId);
        StatisticalHelper.sendReport((int) AMapException.CODE_AMAP_ENGINE_RETURN_TIMEOUT, 1);
        return serviceIntent;
    }

    private boolean isGroupNameExisted(String accountType, String accountName, String dataSet, String label) {
        StringBuilder selection = new StringBuilder();
        List<String> selectionArgs = new ArrayList();
        int totalCount = 0;
        Cursor cursor = null;
        try {
            selection.append("(account_name=? AND account_type=?");
            selectionArgs.add(accountName);
            selectionArgs.add(accountType);
            if (dataSet != null) {
                selection.append(" AND data_set=?");
                selectionArgs.add(dataSet);
            } else {
                selection.append(" AND data_set IS NULL");
            }
            if (label != null) {
                selection.append(" AND title=?");
                selectionArgs.add(label);
            } else {
                selection.append(" AND title IS NULL");
            }
            selection.append(" AND ").append("deleted").append(" = 0");
            selection.append(")");
            cursor = getContentResolver().query(Groups.CONTENT_URI, new String[]{"_id"}, selection.toString(), (String[]) selectionArgs.toArray(new String[selectionArgs.size()]), null);
            if (cursor != null) {
                totalCount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (IllegalArgumentException e) {
            HwLog.e("ContactSaveService", "IllegalArgumentException found in isGroupNameExisted");
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        if (totalCount > 0) {
            return true;
        }
        return false;
    }

    private void deleteGroup(Intent intent) {
        long groupId = intent.getLongExtra("groupId", -1);
        if (groupId == -1) {
            HwLog.e("ContactSaveService", "Invalid arguments for deleteGroup request");
        } else {
            getContentResolver().delete(ContentUris.withAppendedId(Groups.CONTENT_URI, groupId), null, null);
        }
    }

    public static Intent createMultipleGroupsDeletionIntent(Context context, long[] aGroupIds) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("deleteMultipleGroup");
        serviceIntent.putExtra("groupId", aGroupIds);
        return serviceIntent;
    }

    private void deleteMultipleGroups(Intent intent) {
        long[] lGroupIds = intent.getLongArrayExtra("groupId");
        if (lGroupIds == null || lGroupIds.length == 0) {
            HwLog.e("ContactSaveService", "Invalid arguments for deleteGroup request");
            return;
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        int opCount = 0;
        for (int gpIndex = 0; gpIndex != lGroupIds.length; gpIndex++) {
            if (opCount == 100) {
                applyBatch(operations);
                operations.clear();
                opCount = 0;
            }
            operations.add(ContentProviderOperation.newDelete(ContentUris.withAppendedId(Groups.CONTENT_URI, lGroupIds[gpIndex])).build());
            opCount++;
        }
        applyBatch(operations);
    }

    private void applyBatch(ArrayList<ContentProviderOperation> operations) {
        if (!operations.isEmpty()) {
            try {
                getContentResolver().applyBatch("com.android.contacts", operations);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static Intent createGroupUpdateIntent(Context context, long groupId, String newLabel, long[] rawContactsToAdd, long[] contactsToRemove, Class<? extends Activity> callbackActivity, String callbackAction, boolean isPredefined) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("updateGroup");
        serviceIntent.putExtra("groupId", groupId);
        serviceIntent.putExtra("groupLabel", newLabel);
        serviceIntent.putExtra("rawContactsToAdd", rawContactsToAdd);
        serviceIntent.putExtra("contactsToRemove", contactsToRemove);
        serviceIntent.putExtra("predefinedgroupedited", isPredefined);
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void updateGroup(Intent intent) {
        long groupId = intent.getLongExtra("groupId", -1);
        String label = intent.getStringExtra("groupLabel");
        long[] rawContactsToAdd = intent.getLongArrayExtra("rawContactsToAdd");
        long[] contactsToRemove = intent.getLongArrayExtra("contactsToRemove");
        boolean isPredefinedGroupUpdated = intent.getBooleanExtra("predefinedgroupedited", false);
        if (groupId == -1) {
            HwLog.e("ContactSaveService", "Invalid arguments for updateGroup request");
            return;
        }
        ContentResolver resolver = getContentResolver();
        Uri groupUri = ContentUris.withAppendedId(Groups.CONTENT_URI, groupId);
        if (label != null) {
            ContentValues values = new ContentValues();
            values.put("title", label);
            if (isPredefinedGroupUpdated) {
                values.put("sync4", CallInterceptDetails.BRANDED_STATE);
                values.putNull("title_res");
            }
            resolver.update(groupUri, values, null, null);
        }
        boolean isAssertRequired = intent.getBooleanExtra("isFromGroups", false);
        GroupInfo info = new GroupInfo(ContentUris.parseId(groupUri), 2, isAssertRequired, rawContactsToAdd, contactsToRemove);
        new GroupMemberEditHelper(getContentResolver()).executeInPararell(info);
        Intent callbackIntent = (Intent) intent.getParcelableExtra("callbackIntent");
        if (callbackIntent != null) {
            callbackIntent.setData(groupUri);
            deliverCallback(callbackIntent);
        }
    }

    public static Intent createSetStarredIntent(Context context, Uri contactUri, boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("setStarred");
        serviceIntent.putExtra("contactUri", contactUri);
        serviceIntent.putExtra("starred", value);
        ExceptionCapture.reportScene(value ? 27 : 28);
        return serviceIntent;
    }

    private void setStarred(Intent intent) {
        Uri contactUri = (Uri) intent.getParcelableExtra("contactUri");
        boolean value = intent.getBooleanExtra("starred", false);
        if (contactUri == null) {
            HwLog.e("ContactSaveService", "Invalid arguments for setStarred request");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("starred", Boolean.valueOf(value));
        try {
            getContentResolver().update(contactUri, values, null, null);
        } catch (Exception e) {
            e.printStackTrace();
        }
        CommonUtilMethods.updateFavoritesWidget(getApplicationContext());
    }

    public static Intent createSetSendToVoicemail(Context context, Uri contactUri, boolean value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("sendToVoicemail");
        serviceIntent.putExtra("contactUri", contactUri);
        serviceIntent.putExtra("sendToVoicemailFlag", value);
        return serviceIntent;
    }

    private void setSendToVoicemail(Intent intent) {
        Uri contactUri = (Uri) intent.getParcelableExtra("contactUri");
        boolean value = intent.getBooleanExtra("sendToVoicemailFlag", false);
        if (contactUri == null) {
            HwLog.e("ContactSaveService", "Invalid arguments for setRedirectToVoicemail");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("send_to_voicemail", Boolean.valueOf(value));
        getContentResolver().update(contactUri, values, null, null);
    }

    public static Intent createSetRingtone(Context context, Uri contactUri, String value) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("setRingtone");
        serviceIntent.putExtra("contactUri", contactUri);
        serviceIntent.putExtra("customRingtone", value);
        return serviceIntent;
    }

    private void setRingtoneDetail(Intent aIntent) {
        long lRawContactId = aIntent.getLongExtra("contactId", 0);
        String lRingtoneValue = aIntent.getStringExtra("customRingtone");
        boolean lIsInsert = aIntent.getBooleanExtra("isInsert", true);
        ContentValues lValues = new ContentValues();
        lValues.put("data1", lRingtoneValue);
        if (lIsInsert) {
            lValues.put("is_super_primary", Integer.valueOf(1));
            lValues.put("is_primary", Integer.valueOf(1));
            lValues.put("mimetype", "vnd.android.huawei.cursor.item/ringtone");
            lValues.put("raw_contact_id", Long.valueOf(lRawContactId));
            getContentResolver().insert(Data.CONTENT_URI, lValues);
            return;
        }
        StringBuilder lWhere = new StringBuilder();
        lWhere.append("raw_contact_id = " + lRawContactId + " and " + "mimetype" + " = '" + "vnd.android.huawei.cursor.item/ringtone" + "'");
        lValues.put("is_super_primary", Integer.valueOf(1));
        lValues.put("is_primary", Integer.valueOf(1));
        getContentResolver().update(Data.CONTENT_URI, lValues, lWhere.toString(), null);
    }

    private void setRingtone(Intent intent) {
        Uri contactUri = (Uri) intent.getParcelableExtra("contactUri");
        String value = intent.getStringExtra("customRingtone");
        if (contactUri == null) {
            HwLog.e("ContactSaveService", "Invalid arguments for setRingtone");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("custom_ringtone", value);
        getContentResolver().update(contactUri, values, null, null);
    }

    public static Intent createSetSuperPrimaryIntent(Context context, long dataId, boolean isSmart) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("setSuperPrimary");
        serviceIntent.putExtra("dataId", dataId);
        if (isSmart) {
            serviceIntent.putExtra("is_smart_primary", isSmart);
        }
        return serviceIntent;
    }

    private void setSuperPrimary(Intent intent) {
        long dataId = intent.getLongExtra("dataId", -1);
        if (dataId == -1) {
            HwLog.e("ContactSaveService", "Invalid arguments for setSuperPrimary request");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("is_super_primary", Integer.valueOf(1));
        values.put("is_primary", Integer.valueOf(1));
        if (intent.getBooleanExtra("is_smart_primary", false)) {
            values.put("data5", Integer.valueOf(1));
        } else {
            values.put("data5", Integer.valueOf(0));
        }
        try {
            getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, dataId), values, null, null);
        } catch (SQLiteException e) {
            HwLog.e("ContactSaveService", " setSuperPrimary failed ", e);
        }
    }

    public static Intent createClearPrimaryIntent(Context context, long dataId) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("clearPrimary");
        serviceIntent.putExtra("dataId", dataId);
        return serviceIntent;
    }

    private void clearPrimary(Intent intent) {
        long dataId = intent.getLongExtra("dataId", -1);
        if (dataId == -1) {
            HwLog.e("ContactSaveService", "Invalid arguments for clearPrimary request");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("is_super_primary", Integer.valueOf(0));
        values.put("is_primary", Integer.valueOf(0));
        values.put("data5", Integer.valueOf(0));
        getContentResolver().update(ContentUris.withAppendedId(Data.CONTENT_URI, dataId), values, null, null);
    }

    public static void clearPrimary(String contactIds, ContentResolver contentResolver) {
        if (TextUtils.isEmpty(contactIds) || contentResolver == null) {
            HwLog.e("ContactSaveService", "Invalid arguments for clearPrimary2 request");
            return;
        }
        ContentValues values = new ContentValues(1);
        values.put("is_super_primary", Integer.valueOf(0));
        values.put("is_primary", Integer.valueOf(0));
        contentResolver.update(Data.CONTENT_URI, values, "raw_contact_id in (SELECT _id from view_raw_contacts WHERE contact_id IN(" + contactIds + ")) AND " + "mimetype" + "=?", new String[]{"vnd.android.cursor.item/phone_v2"});
    }

    public static Intent createDeleteContactIntent(Context context, Uri contactUri) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("delete");
        serviceIntent.putExtra("contactUri", contactUri);
        return serviceIntent;
    }

    private void deleteContact(Intent intent) {
        Uri contactUri = (Uri) intent.getParcelableExtra("contactUri");
        if (contactUri == null) {
            HwLog.e("ContactSaveService", "Invalid arguments for deleteContact request");
            return;
        }
        int rowsDeleted = -1;
        long contactId = Long.parseLong(contactUri.getLastPathSegment());
        String accountType = SimFactoryManager.getAccountTypeByContactID(contactId);
        if (!QueryUtil.isHAPProviderInstalled() || (!"com.android.huawei.sim".equals(accountType) && !"com.android.huawei.secondsim".equals(accountType))) {
            try {
                rowsDeleted = getContentResolver().delete(contactUri, null, null);
                if (rowsDeleted <= 0) {
                    ExceptionCapture.captureContactDeleteException("the returned value is " + rowsDeleted + " when deleting contact from database", null);
                }
            } catch (RuntimeException e) {
                ExceptionCapture.captureContactDeleteException("deleting contact from database error: ", e);
            }
        } else if (SimUtility.isSimInBusyState(getApplicationContext(), accountType)) {
            String accountName = SimFactoryManager.getSimCardDisplayLabel(accountType);
            showToast(String.format(getString(R.string.str_sim_busy), new Object[]{accountName}));
            return;
        } else {
            rowsDeleted = deleteSingleContactFromSIM(contactId);
        }
        if (rowsDeleted > 0) {
            new ContactPreRefreshService().delOneContactCallback(getBaseContext(), contactId);
            updateCallLog(contactUri, null, null);
        }
        CommonUtilMethods.updateFavoritesWidget(getApplicationContext());
    }

    private void updateCallLog(Uri contactUri, String newName, Uri newUri) {
        if (contactUri != null) {
            ContentValues values = new ContentValues();
            values.put("name", newName);
            values.put("lookup_uri", UriUtils.uriToString(newUri));
            getContentResolver().update(QueryUtil.getCallsContentUri(), values, "lookup_uri = ?", new String[]{contactUri.toString()});
        }
    }

    public static Intent createJoinContactsIntent(Context context, long contactId1, long contactId2, boolean contactWritable, Class<? extends Activity> callbackActivity, String callbackAction) {
        Intent serviceIntent = new Intent(context, ContactSaveService.class);
        serviceIntent.setAction("joinContacts");
        serviceIntent.putExtra("contactId1", contactId1);
        serviceIntent.putExtra("contactId2", contactId2);
        serviceIntent.putExtra("contactWritable", contactWritable);
        Intent callbackIntent = new Intent(context, callbackActivity);
        callbackIntent.setAction(callbackAction);
        serviceIntent.putExtra("callbackIntent", callbackIntent);
        return serviceIntent;
    }

    private void joinContacts(Intent intent) {
        long contactId1 = intent.getLongExtra("contactId1", -1);
        long contactId2 = intent.getLongExtra("contactId2", -1);
        long[] rawContactIds = getRawContactIdsForAggregation(new long[]{contactId1, contactId2});
        if (rawContactIds.length == 0) {
            HwLog.e("ContactSaveService", "Invalid arguments for joinContacts request");
            return;
        }
        ArrayList<ContentProviderOperation> operations = new ArrayList();
        for (int i = 0; i < rawContactIds.length; i++) {
            for (int j = 0; j < rawContactIds.length; j++) {
                if (i != j) {
                    buildJoinContactDiff(operations, rawContactIds[i], rawContactIds[j]);
                }
            }
        }
        ContentResolver resolver = getContentResolver();
        if (resolver != null) {
            Cursor c = resolver.query(Uri.withAppendedPath(ContentUris.withAppendedId(Contacts.CONTENT_URI, contactId1), "entities"), ContactEntityQuery.PROJECTION, "mimetype = 'vnd.android.cursor.item/name' AND data1=display_name AND data1 IS NOT NULL  AND data1 != '' ", null, null);
            if (c == null) {
                HwLog.e("ContactSaveService", "Unable to open Contacts DB cursor");
                showToast(getString(R.string.contactSavedErrorToast_Toast));
                return;
            }
            long dataIdToAddSuperPrimary = -1;
            try {
                if (c.moveToFirst()) {
                    dataIdToAddSuperPrimary = c.getLong(0);
                }
                c.close();
                if (dataIdToAddSuperPrimary != -1) {
                    Builder builder = ContentProviderOperation.newUpdate(ContentUris.withAppendedId(Data.CONTENT_URI, dataIdToAddSuperPrimary));
                    builder.withValue("is_super_primary", Integer.valueOf(1));
                    builder.withValue("is_primary", Integer.valueOf(1));
                    operations.add(builder.build());
                }
                boolean success = false;
                try {
                    int size = operations.size();
                    HwLog.v("ContactSaveService", "Batch Size : " + size);
                    if (size > 100) {
                        int startIndex = 0;
                        int endIndex = 100;
                        do {
                            resolver.applyBatch("com.android.contacts", new ArrayList(operations.subList(startIndex, endIndex)));
                            startIndex = endIndex;
                            endIndex += 100;
                        } while (endIndex < size);
                        if (startIndex < size) {
                            resolver.applyBatch("com.android.contacts", new ArrayList(operations.subList(startIndex, size)));
                        }
                    } else {
                        resolver.applyBatch("com.android.contacts", operations);
                    }
                    success = true;
                } catch (Throwable e) {
                    HwLog.e("ContactSaveService", "Failed to apply aggregation exception batch", e);
                    showToast(getString(R.string.contactSavedErrorToast_Toast));
                }
                Intent callbackIntent = (Intent) intent.getParcelableExtra("callbackIntent");
                if (success && rawContactIds.length > 0) {
                    initHotNumberLookUpKey();
                    Uri uri = RawContacts.getContactLookupUri(resolver, ContentUris.withAppendedId(RawContacts.CONTENT_URI, rawContactIds[0]));
                    callbackIntent.setData(uri);
                    Cursor lCursor = resolver.query(uri, new String[]{"starred"}, null, null, null);
                    if (lCursor != null) {
                        if (lCursor.moveToFirst() && 1 == lCursor.getInt(0)) {
                            if (DEBUG) {
                                HwLog.d("ContactSaveService", "update favourite widget!!!");
                            }
                            CommonUtilMethods.updateFavoritesWidget(getApplicationContext());
                        }
                        lCursor.close();
                    }
                }
                deliverCallback(callbackIntent);
            } catch (Throwable th) {
                c.close();
            }
        }
    }

    private void initHotNumberLookUpKey() {
        if (HLUtils.isShowHotNumberOnTop) {
            HLUtils.initPredefineContactLookupUri(getApplicationContext());
        }
    }

    private long[] getRawContactIdsForAggregation(long[] contactIds) {
        if (contactIds.length == 0) {
            return new long[0];
        }
        int i;
        ContentResolver resolver = getContentResolver();
        StringBuilder queryBuilder = new StringBuilder();
        String[] stringContactIds = new String[contactIds.length];
        for (i = 0; i < contactIds.length; i++) {
            queryBuilder.append("contact_id=?");
            stringContactIds[i] = String.valueOf(contactIds[i]);
            if (contactIds[i] == -1) {
                return new long[0];
            }
            if (i == contactIds.length - 1) {
                break;
            }
            queryBuilder.append(" OR ");
        }
        Cursor c = resolver.query(RawContacts.CONTENT_URI, JoinContactQuery.PROJECTION, queryBuilder.toString(), stringContactIds, null);
        if (c == null) {
            HwLog.e("ContactSaveService", "Unable to open Contacts DB cursor");
            showToast(getString(R.string.contactSavedErrorToast_Toast));
            return new long[0];
        }
        try {
            if (c.getCount() < 2) {
                HwLog.e("ContactSaveService", "Not enough raw contacts to aggregate together.");
                long[] jArr = new long[0];
                return jArr;
            }
            long[] rawContactIds = new long[c.getCount()];
            for (i = 0; i < rawContactIds.length; i++) {
                c.moveToPosition(i);
                rawContactIds[i] = c.getLong(0);
            }
            c.close();
            return rawContactIds;
        } finally {
            c.close();
        }
    }

    private void buildJoinContactDiff(ArrayList<ContentProviderOperation> operations, long rawContactId1, long rawContactId2) {
        Builder builder = ContentProviderOperation.newUpdate(AggregationExceptions.CONTENT_URI);
        builder.withValue("type", Integer.valueOf(1));
        builder.withValue("raw_contact_id1", Long.valueOf(rawContactId1));
        builder.withValue("raw_contact_id2", Long.valueOf(rawContactId2));
        operations.add(builder.build());
    }

    private void showToast(final String message) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                int themeID = ContactSaveService.this.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null);
                if (themeID == 0) {
                    HwLog.d("Contact", "if case value of themeID is ::::" + themeID);
                    Toast.makeText(ContactSaveService.this, message, 1).show();
                    return;
                }
                HwLog.d("Contact", "else case value of themeID is ::::" + themeID);
                Toast.makeText(new ContextThemeWrapper(ContactSaveService.this, themeID), message, 1).show();
            }
        });
    }

    private void deliverCallback(final Intent callbackIntent) {
        this.mMainHandler.post(new Runnable() {
            public void run() {
                ContactSaveService.this.deliverCallbackOnUiThread(callbackIntent);
            }
        });
    }

    void deliverCallbackOnUiThread(Intent callbackIntent) {
        if (DEBUG) {
            Intent testIntent = new Intent("com.dummyTest.reciverAction");
            testIntent.putExtra("TestCallbackIntent", callbackIntent);
            getApplicationContext().sendBroadcast(testIntent);
            String test_lookupUri = String.valueOf(testIntent.getData());
            HwLog.d("ContactSaveService", "deliverCallbackOnUiThread() called.. test_lookupUri = " + test_lookupUri + " test_Action = : " + callbackIntent.getAction());
        }
        for (Listener listener : sListeners) {
            Intent intent = new Intent();
            if (listener instanceof Activity) {
                intent = ((Activity) listener).getIntent();
            } else if (listener instanceof HwBaseFragment) {
                intent = ((HwBaseFragment) listener).getIntent();
            }
            if (callbackIntent.getComponent().equals(intent.getComponent())) {
                listener.onServiceCompleted(callbackIntent);
                return;
            }
            if (CommonUtilMethods.calcIfNeedSplitScreen()) {
                String callbackIntentName = "com.android.contacts.activities.ContactDetailActivity$TranslucentActivity";
                String IntentName = "com.android.contacts.activities.ContactDetailActivity";
                if (callbackIntent.getComponent() != null && "com.android.contacts.activities.ContactDetailActivity".equals(callbackIntent.getComponent().getClassName()) && intent.getComponent() != null && "com.android.contacts.activities.ContactDetailActivity$TranslucentActivity".equals(intent.getComponent().getClassName())) {
                    listener.onServiceCompleted(callbackIntent);
                    return;
                }
            }
            if ((listener instanceof PeopleActivity) && CommonUtilMethods.calcIfNeedSplitScreen()) {
                String peopleName = "com.android.contacts.activities.PeopleActivity";
                String callName = callbackIntent.getComponent().toString();
                if (callName != null && callName.contains("com.android.contacts.activities.PeopleActivity")) {
                    listener.onServiceCompleted(callbackIntent);
                    return;
                }
            }
        }
    }
}
