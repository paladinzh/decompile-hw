package com.android.gallery3d.util;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import com.android.gallery3d.common.Utils;
import com.huawei.gallery.provider.StorageMonitorProvider;
import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.CRC32;

public class StorageMonitorUtil {
    static final String[] TABLE_COLUMNS = new String[]{"_id", "file_path", "creat_time", "crc32_data", "be_reported"};

    public static class CheckResult {
        public String errMsg;
        public boolean reported;
        public int resultCode;
    }

    private static final class RecordUnit {
        public long checksum;
        public long creatTime;
        public long id;
        public String path;
        public boolean reported;

        private RecordUnit() {
        }
    }

    public static CheckResult checkFileStatus(Context context, String path) {
        CheckResult checkRet = new CheckResult();
        if (path == null) {
            GalleryLog.e("StorageMonitorUtil", "checkFileStatus:RESULT_CODE_NO_RECORD");
            checkRet.resultCode = 0;
            return checkRet;
        }
        RecordUnit record = findRecordByPath(context, path);
        if (record == null || record.checksum == 0) {
            GalleryLog.e("StorageMonitorUtil", "checkFileStatus:RESULT_CODE_NO_RECORD");
            checkRet.resultCode = 0;
            return checkRet;
        }
        GalleryLog.v("StorageMonitorUtil", "checkFileStatus: " + record.path + " reported:" + record.reported);
        checkRet.reported = record.reported;
        if (!checkRet.reported) {
            setRecordReported(context, record.id);
        }
        long currentChecksum = getCRC32Checksum(path);
        if (record.checksum == currentChecksum) {
            GalleryLog.w("StorageMonitorUtil", "checkFileStatus:RESULT_CODE_NO_CHANGE");
            checkRet.resultCode = 1;
            checkRet.errMsg = "Path:" + path + " Code:" + checkRet.resultCode + " data:" + record.checksum + ":" + currentChecksum;
        } else {
            long currentLastModTime = getLastModifiedTime(path);
            if (record.creatTime == currentLastModTime) {
                GalleryLog.w("StorageMonitorUtil", "checkFileStatus:RESULT_CODE_BE_DAMAGED");
                checkRet.resultCode = 3;
            } else {
                GalleryLog.w("StorageMonitorUtil", "checkFileStatus:RESULT_CODE_BE_EDITED");
                checkRet.resultCode = 2;
            }
            checkRet.errMsg = "Path:" + path + " Code:" + checkRet.resultCode + " time:" + record.creatTime + ":" + currentLastModTime + " checksum:" + record.checksum + ":" + currentChecksum;
        }
        return checkRet;
    }

    private static long getLastModifiedTime(String path) {
        long ret = 0;
        File file = new File(path);
        if (file.exists() && file.isFile()) {
            ret = file.lastModified();
        }
        GalleryLog.v("StorageMonitorUtil", "getLastModifiedTime:" + ret);
        return ret;
    }

    private static long getCRC32Checksum(String path) {
        if (path == null) {
            GalleryLog.e("StorageMonitorUtil", "getCRC32Checksum:path == null");
            return 0;
        }
        byte[] data = readBytes(path);
        if (data.length == 0) {
            GalleryLog.e("StorageMonitorUtil", "getCRC32Checksum:length == 0");
            return 0;
        }
        long retVal = 0;
        CRC32 crc = new CRC32();
        try {
            crc.update(data);
            retVal = crc.getValue();
        } catch (Exception e) {
            GalleryLog.e("StorageMonitorUtil", "exception error: " + e.getMessage());
        }
        GalleryLog.d("StorageMonitorUtil", "getCRC32Checksum retVal:" + retVal);
        return retVal;
    }

    private static RecordUnit findRecordByPath(Context context, String path) {
        Cursor cursor = null;
        RecordUnit recordUnit = new RecordUnit();
        try {
            cursor = context.getContentResolver().query(StorageMonitorProvider.BASE_URI, TABLE_COLUMNS, "file_path = ? ", new String[]{path}, null);
            if (cursor == null || cursor.getCount() == 0 || !cursor.moveToFirst()) {
                GalleryLog.e("StorageMonitorUtil", "con't find this record !!!");
                recordUnit = null;
            } else {
                boolean z;
                recordUnit.id = cursor.getLong(cursor.getColumnIndex("_id"));
                recordUnit.path = cursor.getString(cursor.getColumnIndex("file_path"));
                recordUnit.creatTime = cursor.getLong(cursor.getColumnIndex("creat_time"));
                recordUnit.checksum = cursor.getLong(cursor.getColumnIndex("crc32_data"));
                if (cursor.getInt(cursor.getColumnIndex("be_reported")) == 0) {
                    z = true;
                } else {
                    z = false;
                }
                recordUnit.reported = z;
                dumpRecordInfo(recordUnit);
            }
            if (cursor != null) {
                cursor.close();
            }
        } catch (Exception e) {
            recordUnit = null;
            GalleryLog.e("StorageMonitorUtil", "query parameter failed !!! ", e);
            if (cursor != null) {
                cursor.close();
            }
        } catch (Throwable th) {
            if (cursor != null) {
                cursor.close();
            }
        }
        return recordUnit;
    }

    private static void setRecordReported(Context context, long id) {
        GalleryLog.d("StorageMonitorUtil", "setRecordReported id:" + id);
        ContentValues values = new ContentValues();
        values.put("be_reported", Boolean.valueOf(true));
        try {
            context.getContentResolver().update(StorageMonitorProvider.BASE_URI, values, "_id = ? ", new String[]{String.valueOf(id)});
        } catch (Exception e) {
            GalleryLog.e("StorageMonitorUtil", "update parameter failed !!! ", e);
        }
    }

    private static byte[] readBytes(String fileName) {
        IOException e;
        Throwable th;
        Closeable closeable = null;
        Closeable bis = null;
        Closeable bos = null;
        try {
            Closeable bis2;
            Closeable fis = new FileInputStream(fileName);
            try {
                bis2 = new BufferedInputStream(fis);
            } catch (IOException e2) {
                e = e2;
                closeable = fis;
                try {
                    GalleryLog.e("StorageMonitorUtil", "read bytes error!" + e.getMessage());
                    Utils.closeSilently(bis);
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(bos);
                    return new byte[0];
                } catch (Throwable th2) {
                    th = th2;
                    Utils.closeSilently(bis);
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(bos);
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                closeable = fis;
                Utils.closeSilently(bis);
                Utils.closeSilently(closeable);
                Utils.closeSilently(bos);
                throw th;
            }
            try {
                Closeable bos2 = new ByteArrayOutputStream(1024);
                try {
                    byte[] buffer = new byte[1024];
                    while (true) {
                        int size = bis2.read(buffer);
                        if (size != -1) {
                            bos2.write(buffer, 0, size);
                        } else {
                            byte[] toByteArray = bos2.toByteArray();
                            Utils.closeSilently(bis2);
                            Utils.closeSilently(fis);
                            Utils.closeSilently(bos2);
                            return toByteArray;
                        }
                    }
                } catch (IOException e3) {
                    e = e3;
                    bos = bos2;
                    bis = bis2;
                    closeable = fis;
                    GalleryLog.e("StorageMonitorUtil", "read bytes error!" + e.getMessage());
                    Utils.closeSilently(bis);
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(bos);
                    return new byte[0];
                } catch (Throwable th4) {
                    th = th4;
                    bos = bos2;
                    bis = bis2;
                    closeable = fis;
                    Utils.closeSilently(bis);
                    Utils.closeSilently(closeable);
                    Utils.closeSilently(bos);
                    throw th;
                }
            } catch (IOException e4) {
                e = e4;
                bis = bis2;
                closeable = fis;
                GalleryLog.e("StorageMonitorUtil", "read bytes error!" + e.getMessage());
                Utils.closeSilently(bis);
                Utils.closeSilently(closeable);
                Utils.closeSilently(bos);
                return new byte[0];
            } catch (Throwable th5) {
                th = th5;
                bis = bis2;
                closeable = fis;
                Utils.closeSilently(bis);
                Utils.closeSilently(closeable);
                Utils.closeSilently(bos);
                throw th;
            }
        } catch (IOException e5) {
            e = e5;
            GalleryLog.e("StorageMonitorUtil", "read bytes error!" + e.getMessage());
            Utils.closeSilently(bis);
            Utils.closeSilently(closeable);
            Utils.closeSilently(bos);
            return new byte[0];
        }
    }

    private static void dumpRecordInfo(RecordUnit recordUnit) {
        GalleryLog.d("StorageMonitorUtil", "dumpRecordInfo id:" + recordUnit.id);
        GalleryLog.d("StorageMonitorUtil", "dumpRecordInfo path:" + recordUnit.path);
        GalleryLog.d("StorageMonitorUtil", "dumpRecordInfo checksum:" + recordUnit.checksum);
        GalleryLog.d("StorageMonitorUtil", "dumpRecordInfo creatTime:" + recordUnit.creatTime);
        GalleryLog.d("StorageMonitorUtil", "dumpRecordInfo reported:" + recordUnit.reported);
    }
}
