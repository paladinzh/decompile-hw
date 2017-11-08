package com.huawei.keyguard.database;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteException;
import android.net.Uri;
import android.os.Environment;
import android.os.OperationCanceledException;
import android.text.TextUtils;
import android.util.SparseArray;
import com.huawei.keyguard.GlobalContext;
import com.huawei.keyguard.events.EventCenter;
import com.huawei.keyguard.events.EventCenter.IEventListener;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.BigPictureInfo.GalleryInfo;
import com.huawei.keyguard.support.magazine.BigPictureInfo.IdentityInfo;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.MagazineUtils;
import com.huawei.keyguard.util.HwLog;
import fyusion.vislib.BuildConfig;
import java.io.File;
import java.util.Locale;

public class ClientHelper implements IEventListener {
    public static final Uri CONTENT_URI = Uri.parse("content://com.android.huawei.magazineunlock");
    public static final Uri CONTENT_URI_COMMON = Uri.parse(CONTENT_URI + "/" + "common");
    public static final Uri CONTENT_URI_DELETEDIDS = Uri.parse(CONTENT_URI + "/" + "deletedhiads");
    public static final Uri CONTENT_URI_PICTURES = Uri.parse(CONTENT_URI + "/" + "pictures");
    private static int IDX_CHANNEL_ID = -1;
    private static int IDX_CUSTOM = -1;
    private static int IDX_FAVORITE = -1;
    private static int IDX_FORMAT = -1;
    private static int IDX_NAME = -1;
    private static int IDX_PATH = -1;
    private static int IDX_THEME = -1;
    private static int IDX_VERSION = -1;
    private static ClientHelper sInstance;
    Runnable mCheckRunnable = new Runnable() {
        public void run() {
            ClientHelper.this.checkPictureValidity(ClientHelper.this.mContext);
        }
    };
    private Context mContext;
    private boolean mExecuting = false;

    private ClientHelper() {
    }

    public static synchronized ClientHelper getInstance() {
        ClientHelper clientHelper;
        synchronized (ClientHelper.class) {
            if (sInstance == null) {
                sInstance = new ClientHelper();
            }
            EventCenter.getInst().listen(64, sInstance);
            clientHelper = sInstance;
        }
        return clientHelper;
    }

    public void updateFavoriteStatusToPicDB(Context context, BigPictureInfo info) {
        if (context == null || info == null || info.getGalleryInfo() == null) {
            HwLog.w("ClientHelper", " context = " + context + ", info = " + info);
            return;
        }
        String where = "path = '" + (MagazineUtils.MAGAZINEDIR + info.getPicUniqueName()) + "'";
        ContentResolver resolver = context.getContentResolver();
        Cursor cursor = null;
        try {
            cursor = queryValidPictures(context, null, where, null, null);
        } catch (SQLiteException ex) {
            HwLog.w("ClientHelper", "updateFavoriteStatusToPicDB ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.w("ClientHelper", "updateFavoriteStatusToPicDB ex = " + ex2.toString());
        } catch (SecurityException ex3) {
            HwLog.w("ClientHelper", "updateFavoriteStatusToPicDB ex = " + ex3.toString());
        }
        if (cursor != null) {
            if (1 == cursor.getCount()) {
                ContentValues values = new ContentValues();
                values.put("isFavorite", Integer.valueOf(info.getFavoriteInfo() ? 1 : 0));
                resolver.update(CONTENT_URI_PICTURES, values, where, null);
            }
            cursor.close();
        }
    }

    public void deletePicFormDB(Context context, BigPictureInfo info) {
        if (context == null || info == null) {
            HwLog.w("ClientHelper", " context = " + context + ", info = " + info);
            return;
        }
        String where = "_id = '" + info.keyId + "'";
        HwLog.w("ClientHelper", "Delete pic id = " + info.keyId);
        deleteValidPictures(context, where, null);
    }

    public int queryFavoritePictureAmount(Context context) {
        if (context == null) {
            return 0;
        }
        int amount = 0;
        Cursor cursor = null;
        try {
            String where = addValidConditionForWhereClause("isFavorite = '1'");
            cursor = context.getContentResolver().query(CONTENT_URI_PICTURES, new String[]{"path"}, where, null, null);
            if (cursor != null) {
                amount = cursor.getCount();
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (SQLiteException ex) {
            HwLog.w("ClientHelper", "queryFavoritePictureAmount SQLiteException = ", ex);
            if (cursor != null) {
                cursor.close();
            }
        } catch (SecurityException ex2) {
            HwLog.w("ClientHelper", "queryFavoritePictureAmount SecurityException = ", ex2);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception ex3) {
            HwLog.w("ClientHelper", "queryFavoritePictureAmount Exception = ", ex3);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return amount;
    }

    private int getBucketId(Cursor cursor, String path) {
        int bucketId = getInt(cursor, "bucket_id", -1);
        if (bucketId > 0) {
            return bucketId;
        }
        return getBucketId(new File(path).getParent());
    }

    public static int getBucketId(String path) {
        if (TextUtils.isEmpty(path)) {
            return -1;
        }
        return path.toLowerCase(Locale.US).hashCode();
    }

    private String getString(Cursor cursor, int index) {
        return index > -1 ? cursor.getString(index) : BuildConfig.FLAVOR;
    }

    private int getInt(Cursor cursor, int index) {
        return index > -1 ? cursor.getInt(index) : 1;
    }

    public static String getString(Cursor cursor, String column) {
        String rt = BuildConfig.FLAVOR;
        if (cursor == null) {
            return rt;
        }
        int index = cursor.getColumnIndex(column);
        if (index > -1) {
            return cursor.getString(index);
        }
        HwLog.w("ClientHelper", "column = " + column + ", index = " + index);
        return rt;
    }

    public static int getInt(Cursor cursor, String column, int def) {
        int rt = def;
        if (cursor != null) {
            int index = cursor.getColumnIndex(column);
            if (index > -1) {
                return cursor.getInt(index);
            }
            HwLog.w("ClientHelper", "column = " + column + ", index = " + index);
            return rt;
        }
        HwLog.w("ClientHelper", "column = " + column + ", cursor is null");
        return rt;
    }

    private static final boolean checkCollumIndex(Cursor cursor) {
        boolean z = false;
        if (cursor == null) {
            return false;
        }
        if (IDX_THEME < 0) {
            IDX_THEME = cursor.getColumnIndex("theme");
            IDX_VERSION = cursor.getColumnIndex("version");
            IDX_PATH = cursor.getColumnIndex("path");
            IDX_NAME = cursor.getColumnIndex("name");
            IDX_CHANNEL_ID = cursor.getColumnIndex("channelId");
            IDX_CUSTOM = cursor.getColumnIndex("isCustom");
            IDX_FAVORITE = cursor.getColumnIndex("isFavorite");
            IDX_FORMAT = cursor.getColumnIndex("picFormat");
        }
        if (IDX_THEME >= 0 && IDX_PATH >= 0 && IDX_NAME >= 0 && IDX_CHANNEL_ID >= 0 && IDX_CUSTOM >= 0 && IDX_FAVORITE >= 0 && IDX_FORMAT >= 0) {
            z = true;
        }
        return z;
    }

    public String addValidConditionForWhereClause(String where) {
        if (TextUtils.isEmpty(where)) {
            return "status = 0";
        }
        return "(" + where + ")" + " AND " + " (" + "status" + " = " + 0 + ")";
    }

    private Cursor queryValidPictures(Context context, String[] projection, String where, String[] whereArgs, String sortOrder) {
        return context.getContentResolver().query(CONTENT_URI_PICTURES, projection, addValidConditionForWhereClause(where), whereArgs, sortOrder);
    }

    private void deleteValidPictures(Context context, String where, String[] whereArgs) {
        context.getContentResolver().delete(CONTENT_URI_PICTURES, addValidConditionForWhereClause(where), whereArgs);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean checkPictureValidity(Context context) {
        boolean updated = false;
        if (!MagazineUtils.hasTranslateDatas(context)) {
            HwLog.w("ClientHelper", "data not translated ");
            return false;
        } else if (Environment.getExternalStorageState().equals("mounted")) {
            synchronized (this) {
                if (this.mExecuting) {
                    HwLog.w("ClientHelper", "is executing ");
                    return false;
                }
                this.mExecuting = true;
            }
        } else {
            HwLog.e("ClientHelper", "storage not mounted");
            return false;
        }
    }

    public SparseArray<BigPictureInfo> querySelectedPictures(Context context, boolean isPrivacyModeOn) {
        SparseArray<BigPictureInfo> pictureList = new SparseArray();
        if (context == null) {
            return pictureList;
        }
        Cursor cursor = null;
        try {
            cursor = queryValidPictures(context, null, getWhere(context, isPrivacyModeOn), null, "_id");
            if (cursor == null || cursor.getCount() == 0) {
                HwLog.w("ClientHelper", "querySelectedPictures pictures maybe all deleted");
                if (cursor != null) {
                    cursor.close();
                }
                return pictureList;
            } else if (checkCollumIndex(cursor)) {
                while (cursor.moveToNext()) {
                    try {
                        BigPictureInfo bInfo = new BigPictureInfo();
                        String name = getString(cursor, IDX_NAME);
                        String path = getString(cursor, IDX_PATH);
                        if (HwFyuseUtils.isFyuseProcessed(path)) {
                            IdentityInfo iInfo = new IdentityInfo(getString(cursor, IDX_VERSION), getString(cursor, IDX_VERSION), name, name, path, getString(cursor, IDX_CHANNEL_ID), getInt(cursor, IDX_FORMAT));
                            bInfo.keyId = getInt(cursor, "_id", -1);
                            bInfo.bucketId = getBucketId(cursor, path);
                            bInfo.setIdentityInfo(iInfo);
                            bInfo.setGalleryInfo(new GalleryInfo(false, 1 == getInt(cursor, "isCustom", -1), false, false, 1 == getInt(cursor, "isFavorite", -1)));
                            pictureList.put(bInfo.keyId, bInfo);
                        }
                    } catch (NumberFormatException ex) {
                        HwLog.w("ClientHelper", "querySelectedPictures fail at one item. " + ex.toString());
                    } catch (Exception ex2) {
                        HwLog.w("ClientHelper", "querySelectedPictures fail at one item. " + ex2.toString());
                    }
                }
                if (cursor != null) {
                    cursor.close();
                }
                return pictureList;
            } else {
                if (cursor != null) {
                    cursor.close();
                }
                return pictureList;
            }
        } catch (SQLiteException ex3) {
            HwLog.w("ClientHelper", "querySelectedPictures SQLiteException. " + ex3.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (OperationCanceledException ex4) {
            HwLog.w("ClientHelper", "querySelectedPictures OperationCanceledException. " + ex4.toString());
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable e) {
            HwLog.w("ClientHelper", "querySelectedPictures fail . ", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    private String getWhere(Context context, boolean isPrivacyModeOn) {
        String where = "isHidden <> '1'";
        if (isPrivacyModeOn) {
            return where + " AND " + "isPrivate" + " <> '1'";
        }
        return where;
    }

    public boolean hasCheckedNewVersion(Context context) {
        boolean z = false;
        Cursor cursor = null;
        try {
            cursor = context.getContentResolver().query(CONTENT_URI_COMMON, null, "key = 'checked_new_version'", null, "_id");
        } catch (SQLiteException ex) {
            HwLog.w("ClientHelper", "queryhasCheckedNewVersion ex = " + ex.toString());
        } catch (OperationCanceledException ex2) {
            HwLog.w("ClientHelper", "queryhasCheckedNewVersion ex = " + ex2.toString());
        }
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                z = "1".equalsIgnoreCase(getString(cursor, "value"));
            }
            cursor.close();
        }
        return z;
    }

    public boolean onReceive(Context context, Intent intent) {
        this.mContext = context;
        HwLog.i("ClientHelper", "SD card event received action = " + intent.getAction());
        GlobalContext.getBackgroundHandler().removeCallbacks(this.mCheckRunnable);
        GlobalContext.getBackgroundHandler().postDelayed(this.mCheckRunnable, 300);
        return false;
    }

    public void insertDeletedHiAdIds(Context context, String contentId) {
        if (context != null && !TextUtils.isEmpty(contentId)) {
            deleteInvalidIds(context);
            ContentValues values = new ContentValues();
            values.put("deletetime", Long.valueOf(System.currentTimeMillis()));
            values.put("hiadMaterialId", contentId);
            context.getContentResolver().insert(CONTENT_URI_DELETEDIDS, values);
        }
    }

    private void deleteInvalidIds(Context context) {
        context.getContentResolver().delete(CONTENT_URI_DELETEDIDS, "deletetime < " + (System.currentTimeMillis() - 1209600000), null);
    }
}
