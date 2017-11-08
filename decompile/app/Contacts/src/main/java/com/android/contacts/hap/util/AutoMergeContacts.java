package com.android.contacts.hap.util;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.ContentValues;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.SQLException;
import android.os.AsyncTask;
import android.os.RemoteException;
import android.provider.ContactsContract.Data;
import android.provider.ContactsContract.RawContacts;
import com.android.contacts.hap.EmuiFeatureManager;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

public class AutoMergeContacts {
    private static final String[] CARE_MIMETYPE = new String[]{"vnd.android.cursor.item/phone_v2", "vnd.android.cursor.item/nickname", "vnd.android.cursor.item/email_v2", "vnd.android.cursor.item/organization", "vnd.android.cursor.item/im", "vnd.android.cursor.item/sip_address", "vnd.android.cursor.item/postal-address_v2", "vnd.android.cursor.item/website"};
    private static final String[] DATA_KEYS = new String[]{"raw_contact_id", "mimetype", "data1", "data2", "data3", "data4", "data5", "data6", "data7", "data8", "data9", "data10", "data11", "data12", "data13", "data14", "data15"};
    private static final boolean DBG = HwLog.HWDBG;

    private static class ProgressAsyncTask extends AsyncTask<Void, Void, Void> {
        Context context;

        public ProgressAsyncTask(Context context) {
            this.context = context;
        }

        protected Void doInBackground(Void... params) {
            if (AutoMergeContacts.DBG) {
                HwLog.d("AutoMergeContacts", "begin to autoMergeRawContacts");
            }
            SharedPreferences contactPref = SharePreferenceUtil.getDefaultSp_de(this.context);
            if (contactPref == null) {
                return null;
            }
            Cursor cursor = null;
            try {
                cursor = this.context.getContentResolver().query(RawContacts.CONTENT_URI, new String[]{"_id", "contact_id", "display_name", "account_type", "account_name"}, "account_type IN  ('com.android.huawei.phone')", null, "display_name COLLATE NOCASE ,_id");
                if (cursor != null && cursor.getCount() > 0 && cursor.moveToFirst()) {
                    String last_name = null;
                    long last_contactid = -1;
                    List<HashMap<String, String>> rawContactList = new ArrayList();
                    List<Long> deleteList = new ArrayList();
                    List<Long> joinList = new ArrayList();
                    do {
                        String name = cursor.getString(2);
                        boolean isMerged = false;
                        if (name != null) {
                            if (!name.equalsIgnoreCase(last_name)) {
                                if (rawContactList.size() > 1) {
                                    AutoMergeContacts.compareAndMerege(this.context, rawContactList, deleteList, joinList);
                                    isMerged = true;
                                }
                                last_name = name;
                                rawContactList.clear();
                            } else if (cursor.getLong(1) != last_contactid) {
                                isMerged = false;
                            }
                            last_contactid = cursor.getLong(1);
                            HashMap<String, String> map = new HashMap();
                            map.put("_id", cursor.getString(0));
                            map.put("contact_id", cursor.getString(1));
                            map.put("account_type", cursor.getString(3));
                            map.put("account_name", cursor.getString(4));
                            map.put("display_name", name);
                            rawContactList.add(map);
                        }
                    } while (cursor.moveToNext());
                    if (rawContactList.size() > 1 && !isMerged) {
                        AutoMergeContacts.compareAndMerege(this.context, rawContactList, deleteList, joinList);
                    }
                    ArrayList<ContentProviderOperation> buildInto = new ArrayList();
                    for (Long rawid : deleteList) {
                        Builder builder = ContentProviderOperation.newDelete(RawContacts.CONTENT_URI);
                        builder.withSelection("_id=?", new String[]{rawid + ""});
                        buildInto.add(builder.build());
                        if (buildInto.size() >= 450) {
                            AutoMergeContacts.deleteContactsBatch(this.context, buildInto);
                        }
                    }
                    if (buildInto.size() > 0) {
                        AutoMergeContacts.deleteContactsBatch(this.context, buildInto);
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                if (AutoMergeContacts.DBG) {
                    HwLog.d("AutoMergeContacts", "autoMergeRawContacts finish");
                }
                contactPref.edit().putBoolean("automergestatus", true).commit();
                return null;
            } catch (SQLException sqlex) {
                sqlex.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
                if (AutoMergeContacts.DBG) {
                    HwLog.d("AutoMergeContacts", "autoMergeRawContacts finish");
                }
                contactPref.edit().putBoolean("automergestatus", true).commit();
                return null;
            } catch (Exception ex) {
                HwLog.w("AutoMergeContacts", "some thing error when auto merge duplicated Contacts");
                ex.printStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
                if (AutoMergeContacts.DBG) {
                    HwLog.d("AutoMergeContacts", "autoMergeRawContacts finish");
                }
                contactPref.edit().putBoolean("automergestatus", true).commit();
                return null;
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
                if (AutoMergeContacts.DBG) {
                    HwLog.d("AutoMergeContacts", "autoMergeRawContacts finish");
                }
                contactPref.edit().putBoolean("automergestatus", true).commit();
                return null;
            }
        }
    }

    static class RawContactEntity {
        private String accountName;
        private String accountType;
        private long contactId;
        private HashMap<String, List<ContentValues>> dataMap;
        private boolean isValid = true;
        private long rawContactId;

        public RawContactEntity(long id, long contactId, String accountType, String accountName) {
            this.rawContactId = id;
            this.accountName = accountName;
            this.accountType = accountType;
            this.contactId = contactId;
            this.dataMap = new HashMap();
        }

        public void addData(ContentValues cv) {
            String mimeType = cv.getAsString("mimetype");
            List<ContentValues> cvList = (List) this.dataMap.get(mimeType);
            if (cvList == null) {
                cvList = new ArrayList();
                this.dataMap.put(mimeType, cvList);
            }
            cvList.add(cv);
        }

        public static int compare(RawContactEntity entity1, RawContactEntity entity2) {
            int paramValid = checkValid(entity1, entity2);
            if (paramValid != 100) {
                return paramValid;
            }
            Set<String> mimeTypeSet1 = entity1.dataMap.keySet();
            Set<String> mimeTypeSet2 = entity2.dataMap.keySet();
            paramValid = checkValid(mimeTypeSet1, mimeTypeSet2);
            if (paramValid != 100) {
                return paramValid;
            }
            if (mimeTypeSet1.containsAll(mimeTypeSet2)) {
                if (mimeTypeSet1.size() == mimeTypeSet2.size()) {
                    return compareDataMap(0, mimeTypeSet2, entity1.dataMap, entity2.dataMap);
                }
                return compareDataMap(1, mimeTypeSet2, entity1.dataMap, entity2.dataMap);
            } else if (mimeTypeSet2.containsAll(mimeTypeSet1)) {
                return compareDataMap(-1, mimeTypeSet1, entity1.dataMap, entity2.dataMap);
            } else {
                return -2;
            }
        }

        private static int checkValid(Object obj1, Object obj2) {
            if (obj1 == null && obj2 == null) {
                return 0;
            }
            if (obj1 == null && obj2 != null) {
                return -1;
            }
            if (obj1 == null || obj2 != null) {
                return 100;
            }
            return 1;
        }

        private static int compareDataMap(int containFlag, Set<String> mimeTypeSet, HashMap<String, List<ContentValues>> dataMap1, HashMap<String, List<ContentValues>> dataMap2) {
            for (String mimeType : mimeTypeSet) {
                List<ContentValues> list1 = (List) dataMap1.get(mimeType);
                List<ContentValues> list2 = (List) dataMap2.get(mimeType);
                if (list1.containsAll(list2)) {
                    if (list1.size() <= list2.size()) {
                        continue;
                    } else if (containFlag == -1) {
                        return -2;
                    } else {
                        containFlag = 1;
                    }
                } else if (!list2.containsAll(list1)) {
                    List<Set<Entry<String, Object>>> listset1 = new ArrayList();
                    for (ContentValues cv1 : list1) {
                        listset1.add(cv1.valueSet());
                    }
                    List<Set<Entry<String, Object>>> listset2 = new ArrayList();
                    for (ContentValues cv2 : list2) {
                        listset2.add(cv2.valueSet());
                    }
                    if (compareData(listset1, listset2)) {
                        if (containFlag == -1) {
                            return -2;
                        }
                        containFlag = 1;
                    } else if (!compareData(listset2, listset1)) {
                        return -2;
                    } else {
                        if (containFlag == 1) {
                            return -2;
                        }
                        containFlag = -1;
                    }
                } else if (containFlag == 1) {
                    return -2;
                } else {
                    containFlag = -1;
                }
            }
            return containFlag;
        }

        private static boolean compareData(List<Set<Entry<String, Object>>> listset1, List<Set<Entry<String, Object>>> listset2) {
            for (Set<Entry<String, Object>> entryset2 : listset2) {
                boolean flag = false;
                for (Set<Entry<String, Object>> entryset1 : listset1) {
                    if (entryset1.containsAll(entryset2)) {
                        flag = true;
                        continue;
                        break;
                    }
                }
                if (!flag) {
                    return false;
                }
            }
            return true;
        }

        public static boolean compareAccount(RawContactEntity entity1, RawContactEntity entity2) {
            if (compareString(entity1.accountType, entity2.accountType)) {
                return compareString(entity1.accountName, entity2.accountName);
            }
            return false;
        }

        private static boolean compareString(String str1, String str2) {
            if (str1 != null) {
                return str1.equals(str2);
            }
            if (str2 == null) {
                return true;
            }
            return false;
        }
    }

    public static void autoMergeRawContacts(Context context) {
        if (context == null) {
            HwLog.w("AutoMergeContacts", "context == null");
        } else if (EmuiFeatureManager.isAutoDeleteContactsEnable()) {
            new ProgressAsyncTask(context).execute(new Void[0]);
        }
    }

    private static void deleteContactsBatch(Context context, ArrayList<ContentProviderOperation> buildInto) {
        try {
            if (DBG) {
                HwLog.d("AutoMergeContacts", "delete buildInto.size = " + buildInto.size());
            }
            context.getContentResolver().applyBatch("com.android.contacts", buildInto);
        } catch (RemoteException e) {
            e.printStackTrace();
        } catch (OperationApplicationException e2) {
            e2.printStackTrace();
        } finally {
            buildInto.clear();
        }
    }

    private static void compareAndMerege(Context context, List<HashMap<String, String>> rawContactList, List<Long> deleteList, List<Long> joinList) {
        if (rawContactList.size() >= 2) {
            List<Integer[]> joinCandidateIndexList = new ArrayList();
            StringBuilder sb = new StringBuilder("raw_contact_id").append(" in(");
            HashMap<String, HashMap<String, String>> accMap = new HashMap();
            for (HashMap<String, String> map : rawContactList) {
                StringBuilder stringBuilder = sb;
                stringBuilder.append((String) map.get("_id")).append(",");
                accMap.put((String) map.get("_id"), map);
            }
            sb.setLength(sb.length() - 1);
            sb.append(")");
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(Data.CONTENT_URI, DATA_KEYS, sb.toString(), null, "raw_contact_id,mimetype");
                List<RawContactEntity> rawContactEntityList = new ArrayList();
                RawContactEntity rawContactData = null;
                if (cursor != null && cursor.moveToFirst()) {
                    long last_raw_contactid = -1;
                    do {
                        Long raw_contactid = Long.valueOf(cursor.getLong(0));
                        String mimeType = cursor.getString(1);
                        if (last_raw_contactid != raw_contactid.longValue()) {
                            last_raw_contactid = raw_contactid.longValue();
                            rawContactData = new RawContactEntity(raw_contactid.longValue(), Long.parseLong((String) ((HashMap) accMap.get(raw_contactid + "")).get("contact_id")), (String) ((HashMap) accMap.get(raw_contactid + "")).get("account_type"), (String) ((HashMap) accMap.get(raw_contactid + "")).get("account_name"));
                            rawContactEntityList.add(rawContactData);
                        }
                        if (!(mimeType == null || rawContactData == null || !isCareMimeType(mimeType))) {
                            if (DBG) {
                                HwLog.d("AutoMergeContacts", "mimeType = " + mimeType);
                            }
                            ContentValues dataValues = new ContentValues();
                            cursorColumnToContentValues(cursor, dataValues, 1);
                            if (mimeType.equals("vnd.android.cursor.item/phone_v2")) {
                                String phoneNum = cursor.getString(2);
                                if (phoneNum != null) {
                                    dataValues.put(DATA_KEYS[2], phoneNum.replaceAll("[^0-9]", ""));
                                }
                            } else {
                                cursorColumnToContentValues(cursor, dataValues, 2);
                            }
                            cursorColumnToContentValues(cursor, dataValues, 4);
                            cursorColumnToContentValues(cursor, dataValues, 5);
                            cursorColumnToContentValues(cursor, dataValues, 6);
                            cursorColumnToContentValues(cursor, dataValues, 7);
                            cursorColumnToContentValues(cursor, dataValues, 8);
                            cursorColumnToContentValues(cursor, dataValues, 9);
                            cursorColumnToContentValues(cursor, dataValues, 10);
                            cursorColumnToContentValues(cursor, dataValues, 11);
                            cursorColumnToContentValues(cursor, dataValues, 12);
                            cursorColumnToContentValues(cursor, dataValues, 13);
                            cursorColumnToContentValues(cursor, dataValues, 14);
                            cursorColumnToContentValues(cursor, dataValues, 15);
                            cursorColumnToContentValues(cursor, dataValues, 16);
                            rawContactData.addData(dataValues);
                        }
                    } while (cursor.moveToNext());
                    int entityListSize = rawContactEntityList.size();
                    for (int i = 0; i < entityListSize; i++) {
                        RawContactEntity entity1 = (RawContactEntity) rawContactEntityList.get(i);
                        if (entity1.isValid) {
                            for (int j = i + 1; j < entityListSize; j++) {
                                RawContactEntity entity2 = (RawContactEntity) rawContactEntityList.get(j);
                                if (entity2.isValid && entity1.isValid) {
                                    int retValue = RawContactEntity.compare(entity1, entity2);
                                    if (DBG) {
                                        HwLog.d("AutoMergeContacts", "retValue = " + retValue);
                                    }
                                    switch (retValue) {
                                        case -1:
                                            if (!RawContactEntity.compareAccount(entity1, entity2)) {
                                                joinCandidateIndexList.add(new Integer[]{Integer.valueOf(j), Integer.valueOf(i)});
                                                break;
                                            }
                                            deleteList.add(Long.valueOf(entity1.rawContactId));
                                            entity1.isValid = false;
                                            break;
                                        case 0:
                                        case 1:
                                            if (!RawContactEntity.compareAccount(entity1, entity2)) {
                                                joinCandidateIndexList.add(new Integer[]{Integer.valueOf(i), Integer.valueOf(j)});
                                                break;
                                            }
                                            deleteList.add(Long.valueOf(entity2.rawContactId));
                                            entity2.isValid = false;
                                            break;
                                        default:
                                            continue;
                                    }
                                }
                            }
                            continue;
                        }
                    }
                    for (Integer[] index : joinCandidateIndexList) {
                        RawContactEntity rce1 = (RawContactEntity) rawContactEntityList.get(index[0].intValue());
                        RawContactEntity rce2 = (RawContactEntity) rawContactEntityList.get(index[1].intValue());
                        if (rce1.isValid && rce2.isValid && rce1.contactId != rce2.contactId) {
                            joinList.add(Long.valueOf(rce1.contactId));
                            joinList.add(Long.valueOf(rce2.contactId));
                            rce2.contactId = rce1.contactId;
                        }
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (SQLException sqlex) {
                sqlex.getStackTrace();
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e) {
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
    }

    private static boolean isCareMimeType(String mimeType) {
        if (mimeType != null) {
            for (String mt : CARE_MIMETYPE) {
                if (mt.equals(mimeType)) {
                    return true;
                }
            }
        }
        return false;
    }

    private static void cursorColumnToContentValues(Cursor cursor, ContentValues values, int index) {
        switch (cursor.getType(index)) {
            case 0:
                return;
            case 1:
                values.put(DATA_KEYS[index], Long.valueOf(cursor.getLong(index)));
                return;
            case 3:
                if (cursor.getString(index).trim().length() > 0) {
                    values.put(DATA_KEYS[index], cursor.getString(index));
                    return;
                }
                return;
            case 4:
                values.put(DATA_KEYS[index], cursor.getBlob(index));
                return;
            default:
                throw new IllegalStateException("Invalid or unhandled data type");
        }
    }
}
