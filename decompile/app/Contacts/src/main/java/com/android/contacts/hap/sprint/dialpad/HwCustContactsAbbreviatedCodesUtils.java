package com.android.contacts.hap.sprint.dialpad;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.os.RemoteException;
import android.provider.SettingsEx.Systemex;
import android.text.TextUtils;
import android.util.Log;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.util.LogConfig;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.huawei.sprint.chameleon.provider.ChameleonContract;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.concurrent.atomic.AtomicBoolean;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HwCustContactsAbbreviatedCodesUtils {
    public static final String ADC_CONTACT = "contacts_adc";
    public static final boolean DEBUG = true;
    private static final int[] ENGLISH_DIALPAD_MAP = new int[]{2, 2, 2, 3, 3, 3, 4, 4, 4, 5, 5, 5, 6, 6, 6, 7, 7, 7, 7, 8, 8, 8, 9, 9, 9, 9};
    public static final String SYSTEM_PRELOAD_ADC = "contacts_adc_version";
    private static final String TAG = "ADCData";
    private static AtomicBoolean isRunning = new AtomicBoolean();
    private HashMap<String, String> mCompleteAdcNodesMap = new HashMap();
    private Context mContext;
    private SQLiteDatabase mDatabase;
    private ContactsAppDatabaseHelper mDatabaseHelper;

    public HwCustContactsAbbreviatedCodesUtils(Context context) {
        this.mContext = context;
        this.mDatabaseHelper = ContactsAppDatabaseHelper.getInstance(context);
        this.mDatabase = this.mDatabaseHelper.getWritableDatabase();
    }

    public void prepareADCDataAsync() {
        Thread backgroundThread = new Thread(new Runnable() {
            public void run() {
                if (HwCustContactsAbbreviatedCodesUtils.this.mContext == null || HwCustContactsAbbreviatedCodesUtils.isRunning.get()) {
                    HwCustContactsAbbreviatedCodesUtils.this.log("Either Context is null or thread is running.");
                    return;
                }
                HwCustContactsAbbreviatedCodesUtils.isRunning.set(true);
                long dbVersion = HwCustContactsAbbreviatedCodesUtils.this.getDBVersion();
                long prefVersion = HwCustContactsAbbreviatedCodesUtils.this.getPrefVersion();
                HwCustContactsAbbreviatedCodesUtils.this.log("Chameleaon version = " + dbVersion + " last saved version: " + prefVersion);
                if (dbVersion != prefVersion) {
                    HwCustContactsAbbreviatedCodesUtils.this.deleteADCPageData();
                    HwCustContactsAbbreviatedCodesUtils.this.log("old data has been cleared");
                    HwCustContactsAbbreviatedCodesUtils.this.reloadADCNodesFromDB();
                    HwCustContactsAbbreviatedCodesUtils.this.insertDataToDatabase();
                    HwCustContactsAbbreviatedCodesUtils.this.saveToPref(dbVersion);
                }
                HwCustContactsAbbreviatedCodesUtils.isRunning.set(false);
            }
        });
        backgroundThread.setPriority(10);
        backgroundThread.start();
    }

    private void reloadADCNodesFromDB() {
        reloadBaseAbbreviatedN11Numbers();
        queryChameleonProviderForContactsADCNode();
    }

    private long getDBVersion() {
        long dbVersion = -1;
        Cursor cursor = null;
        try {
            cursor = this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI_VERSION, new String[]{"category", "version"}, "category='contacts_adc'", null, null);
            if (cursor == null) {
                if (cursor != null) {
                    cursor.close();
                }
                return -1;
            }
            if (cursor.moveToFirst()) {
                do {
                    if (ADC_CONTACT.equals(cursor.getString(0))) {
                        dbVersion = cursor.getLong(1);
                    }
                } while (cursor.moveToNext());
            }
            if (cursor != null) {
                cursor.close();
            }
            return dbVersion;
        } catch (SQLiteException e) {
            Log.w(TAG, "=====Error is thrown-- Stop querying the db=====" + e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private long getPrefVersion() {
        return Systemex.getLong(this.mContext.getContentResolver(), SYSTEM_PRELOAD_ADC, -1);
    }

    private void saveToPref(long dbVersion) {
        Systemex.putLong(this.mContext.getContentResolver(), SYSTEM_PRELOAD_ADC, dbVersion);
    }

    private void queryChameleonProviderForContactsADCNode() {
        Cursor adcCursor = this.mContext.getContentResolver().query(ChameleonContract.CONTENT_URI, new String[]{"value"}, "category='contacts_adc'", null, null);
        if (adcCursor != null) {
            try {
                adcCursor.moveToFirst();
                while (true) {
                    String adcInfoValue = adcCursor.getString(0);
                    if (!TextUtils.isEmpty(adcInfoValue)) {
                        putAbbreviatedDialingCodesInfo(adcInfoValue);
                    }
                    if (!adcCursor.moveToNext()) {
                        break;
                    }
                }
            } catch (SQLiteException e) {
                Log.w(TAG, "=====Error is thrown-- Stop querying the db=====");
            } finally {
                adcCursor.close();
            }
        }
    }

    private void reloadBaseAbbreviatedN11Numbers() {
        HashMap<String, String> mN11NumberPair = new HashMap();
        mN11NumberPair.put(CallInterceptDetails.BRANDED_STATE, "Voicemail");
        mN11NumberPair.put("211", "Community Information");
        mN11NumberPair.put("311", "Non-Emergency Services");
        mN11NumberPair.put("411", "Directory Assistance");
        mN11NumberPair.put("511", "Traffic Information");
        mN11NumberPair.put("611", "Carrier Service Repair");
        mN11NumberPair.put("711", "TRS Relay for TTY");
        mN11NumberPair.put("811", "Call Before You Dig");
        this.mCompleteAdcNodesMap.putAll(mN11NumberPair);
    }

    private void putAbbreviatedDialingCodesInfo(String adcMainToken) {
        if (!TextUtils.isEmpty(adcMainToken) && adcMainToken.contains(",")) {
            String[] adcTokenArray = adcMainToken.split(",");
            if (2 == adcTokenArray.length) {
                this.mCompleteAdcNodesMap.put(adcTokenArray[0].trim(), adcTokenArray[1].trim());
            }
        }
    }

    private void insertDataToDatabase() {
        if (!this.mCompleteAdcNodesMap.isEmpty()) {
            log("insert data to database. " + this.mCompleteAdcNodesMap.size());
            ArrayList<ContentProviderOperation> optList = new ArrayList();
            for (Entry<String, String> entry : this.mCompleteAdcNodesMap.entrySet()) {
                String number = (String) entry.getKey();
                String name = (String) entry.getValue();
                if (!(TextUtils.isEmpty(number) || TextUtils.isEmpty(name))) {
                    makeBuilderOperation(optList, number, name);
                }
            }
            applyBatch(optList);
            optList.clear();
        }
    }

    private void makeBuilderOperation(ArrayList<ContentProviderOperation> operationList, String number, String name) {
        int referenceId = operationList.size();
        Builder builder = ContentProviderOperation.newInsert(ContactsAppProvider.YELLOW_PAGE_URI);
        builder.withValue("name", name);
        builder.withValue("group_name", TAG);
        String dummySB = createDataJsonObject(number, name).toString();
        log("dummySB " + dummySB);
        builder.withValue(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, dummySB);
        operationList.add(builder.build());
        Builder data = ContentProviderOperation.newInsert(ContactsAppProvider.YELLOW_PAGE_DATA_URI);
        data.withValue("name", name);
        String dialMap = convertToDialMap(name);
        log(dialMap);
        data.withValue("dial_map", dialMap);
        data.withValue("hot_points", "0");
        data.withValue("number", number);
        data.withValueBackReference("ypid", referenceId);
        operationList.add(data.build());
    }

    public JSONObject createDataJsonObject(String number, String name) {
        JSONObject jobject = new JSONObject();
        try {
            jobject.put("group", "ADC");
            jobject.put("name", name);
            String dialMap = convertToDialMap(name);
            JSONObject jinnerobject = new JSONObject();
            jinnerobject.put("hot_points", "0");
            jinnerobject.put("dial_map", dialMap);
            jinnerobject.put("phone", number);
            jinnerobject.put("name", name);
            JSONArray array = new JSONArray();
            array.put(jinnerobject);
            jobject.put("phone", array);
        } catch (JSONException e) {
            Log.e(TAG, "Exception while creating json object" + e);
        }
        return jobject;
    }

    private static String convertToDialMap(String aInput) {
        String upper = aInput.toUpperCase();
        StringBuffer sb = new StringBuffer();
        for (char c : upper.toCharArray()) {
            if (c <= '@' || c >= '[') {
                sb.append(c);
            } else {
                sb.append(ENGLISH_DIALPAD_MAP[c - 65]);
            }
        }
        return sb.toString();
    }

    private void deleteADCPageData() {
        this.mDatabase.delete("yellow_page", null, null);
    }

    private void log(String msg) {
        if (LogConfig.HWDBG) {
            Log.i(TAG, msg);
        }
    }

    private void applyBatch(ArrayList<ContentProviderOperation> operationList) {
        try {
            this.mContext.getContentResolver().applyBatch("com.android.contacts.app", operationList);
        } catch (RemoteException e) {
            Log.e(TAG, String.format("%s: %s", new Object[]{e.toString(), e.getMessage()}));
        } catch (OperationApplicationException e2) {
            Log.e(TAG, String.format("%s: %s", new Object[]{e2.toString(), e2.getMessage()}));
        }
    }
}
