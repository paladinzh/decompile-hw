package com.huawei.harassmentinterception.db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Parcel;
import android.os.Parcelable;
import com.google.common.collect.Lists;
import com.huawei.harassmentinterception.common.RuleStates;
import com.huawei.harassmentinterception.common.Tables.TbInterceptionRules;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.comm.valueprefer.ValuePair;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public class DBVersion9To10 {
    public static final String KEY_ADVERTISE = "advertise";
    public static final String KEY_BLOCK_ALL = "block_all";
    public static final String KEY_BLOCK_BLACKLIST = "block_blacklist";
    public static final String KEY_BLOCK_INTELLIGENT = "block_intelligent";
    public static final String KEY_BLOCK_STRANGER = "block_stranger";
    public static final String KEY_BLOCK_UNKNOW = "block_unknow";
    public static final String KEY_ESTATE = "estate";
    public static final String KEY_HARASS = "harassment";
    public static final String KEY_SCAM = "scam";
    public static final String KEY_SIM_SLOT = "sim_slot";
    public static final String TABLE_CALL_RUES = "interception_call_rules";
    public static final String TABLE_INTELL_CALL_RULES = "interception_call_intelligent_rules";
    public static final String TABLE_SMS_RULES = "interception_message_rules";
    private static final String TAG = "DBVersion9To10";

    public static class DbRulesItem implements Parcelable {
        private static final String TAG = "DbRulesItem";
        private String key;
        private int state;
        private int value1;
        private int value2;

        public DbRulesItem(String key, int state) {
            this(key, state, 0, 0);
        }

        public DbRulesItem(String key, int state, int value1, int value2) {
            this.key = key;
            this.state = state;
            this.value1 = value1;
            this.value2 = value2;
        }

        public String getKey() {
            return this.key;
        }

        public int getState() {
            return this.state;
        }

        public int getValue1() {
            return this.value1;
        }

        public int getValue2() {
            return this.value2;
        }

        public boolean isCard1Open() {
            return RuleStates.isCard1Open(this.state);
        }

        public boolean isCard2Open() {
            return RuleStates.isCard2Open(this.state);
        }

        public boolean isOpen(int cardOp) {
            if (cardOp == 1) {
                return isCard1Open();
            }
            if (cardOp == 2) {
                return isCard2Open();
            }
            HwLog.e(TAG, "isOpen unknown cardop:" + cardOp);
            return false;
        }

        public int getValue(int cardOp) {
            if (cardOp == 1) {
                return getValue1();
            }
            if (cardOp == 2) {
                return getValue2();
            }
            HwLog.e(TAG, "getValue unknown cardop:" + cardOp);
            return -1;
        }

        public int describeContents() {
            return 0;
        }

        public void writeToParcel(Parcel dest, int flags) {
        }
    }

    public static void upgradeFrom9To10(Context ctx, SQLiteDatabase db) {
        inheritDats(ctx, db, false);
    }

    private static void inheritDats(Context ctx, SQLiteDatabase db, boolean temp) {
        HwLog.i(TAG, "inheritDats called, temp:" + temp);
        ArrayList<ValuePair> valuePairs = HsmCollections.newArrayList();
        for (DbRulesItem item : getTableCallRules(db, temp)) {
            String key = item.getKey();
            boolean checked = item.isCard1Open();
            if ("block_intelligent".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_BLOCK_CALL, 1, checked));
            } else if ("block_unknow".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_UNKONW_CALL, 1, checked));
            } else if ("block_stranger".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_STRANGER_CALL, 1, checked));
            } else if ("block_all".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_ALL_CALL, 1, checked));
            }
        }
        for (DbRulesItem item2 : getTableIntellCallRules(db, temp)) {
            key = item2.getKey();
            checked = item2.isCard1Open();
            int value = item2.getValue1();
            if ("scam".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_SCAM_SWITCH, 1, checked));
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_SCAM_VALUE, 1, value));
            } else if ("harassment".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_HARASS_SWITCH, 1, checked));
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_HARASS_VALUE, 1, value));
            } else if ("advertise".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_ADVER_SWITCH, 1, checked));
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_ADVER_VALUE, 1, value));
            } else if ("estate".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_ESTATE_SWITCH, 1, checked));
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_ESTATE_VALUE, 1, value));
            }
        }
        for (DbRulesItem item22 : getMessageRules(db, temp)) {
            key = item22.getKey();
            checked = item22.isCard1Open();
            if ("block_intelligent".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_INTELL_BLOCK_MSG, 1, checked));
            } else if ("block_stranger".equals(key)) {
                valuePairs.add(RulesOps.createValuePair(RulesOps.KEY_BLOCK_STRANGER_MSG, 1, checked));
            }
        }
        RulesOps.setRules(ctx, valuePairs);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<DbRulesItem> getTableCallRules(SQLiteDatabase db, boolean temp) {
        String tableName = TABLE_CALL_RUES + (temp ? "_tmpbak" : "");
        List<DbRulesItem> items = Lists.newArrayList();
        try {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            if (cursor != null) {
                int keyIndex = cursor.getColumnIndex("key");
                int stateIndex = cursor.getColumnIndex("status");
                while (cursor.moveToNext()) {
                    items.add(new DbRulesItem(cursor.getString(keyIndex), cursor.getInt(stateIndex)));
                }
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.e(TAG, "getTableCallRules query data failed!", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return items;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<DbRulesItem> getTableIntellCallRules(SQLiteDatabase db, boolean temp) {
        String tableName = TABLE_INTELL_CALL_RULES + (temp ? "_tmpbak" : "");
        List<DbRulesItem> items = Lists.newArrayList();
        try {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            if (cursor != null) {
                int keyIndex = cursor.getColumnIndex("key");
                int stateIndex = cursor.getColumnIndex("status");
                int value1Index = cursor.getColumnIndex(TbInterceptionRules.VALUE_1);
                int value2Index = cursor.getColumnIndex(TbInterceptionRules.VALUE_2);
                while (cursor.moveToNext()) {
                    items.add(new DbRulesItem(cursor.getString(keyIndex), cursor.getInt(stateIndex), cursor.getInt(value1Index), cursor.getInt(value2Index)));
                }
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.e(TAG, "getTableIntellCallRules  query data failed!", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return items;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<DbRulesItem> getMessageRules(SQLiteDatabase db, boolean temp) {
        String tableName = TABLE_SMS_RULES + (temp ? "_tmpbak" : "");
        List<DbRulesItem> items = Lists.newArrayList();
        try {
            Cursor cursor = db.query(tableName, null, null, null, null, null, null);
            if (cursor != null) {
                int keyIndex = cursor.getColumnIndex("key");
                int stateIndex = cursor.getColumnIndex("status");
                while (cursor.moveToNext()) {
                    items.add(new DbRulesItem(cursor.getString(keyIndex), cursor.getInt(stateIndex)));
                }
            }
            Closeables.close(cursor);
        } catch (Exception e) {
            HwLog.e(TAG, "getMessageRules query data failed!", e);
        } catch (Throwable th) {
            Closeables.close(null);
        }
        return items;
    }
}
