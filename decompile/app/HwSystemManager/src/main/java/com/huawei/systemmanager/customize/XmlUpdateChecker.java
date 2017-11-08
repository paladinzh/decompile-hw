package com.huawei.systemmanager.customize;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class XmlUpdateChecker {
    public static final int CREATED = 1;
    public static final int DELETED = 2;
    public static final int INITED = 0;
    public static final int NOCHANGE = 4;
    private static final int NOT_EXIST = 0;
    private static final int NOT_INITED = -1;
    private static final String PREF_FILENAME = "customize_last_file_time";
    private static final String TAG = "xml";
    public static final int UPDATED = 3;
    private Context mContext;
    private SharedPreferences mLastTimePref = this.mContext.getSharedPreferences(PREF_FILENAME, 4);

    public XmlUpdateChecker(Context context) {
        this.mContext = context;
    }

    public int checkConfigFileChange(String file) {
        long recordedSign = getRecordedSign(file);
        long currentSign = getCurrentSign(file);
        HwLog.i(TAG, "Recorded sign:" + recordedSign + ", current sign:" + currentSign + ":" + file);
        if (recordedSign == -1) {
            return 0;
        }
        if (recordedSign == currentSign) {
            return 4;
        }
        if (recordedSign != 0 && currentSign == 0) {
            return 2;
        }
        if (recordedSign != 0 || currentSign == 0) {
            return 3;
        }
        return 1;
    }

    private long getCurrentSign(String fileName) {
        if (new File(fileName).exists()) {
            return computeSha256Digest(fileName);
        }
        return 0;
    }

    private long computeSha256Digest(String path) {
        IOException e;
        FileNotFoundException e2;
        Throwable th;
        FileInputStream fileInputStream = null;
        try {
            FileInputStream fis = new FileInputStream(path);
            try {
                Hasher hasher = Hashing.sha256().newHasher();
                byte[] data = new byte[1024];
                while (true) {
                    int read = fis.read(data);
                    if (read == -1) {
                        break;
                    }
                    hasher.putBytes(data, 0, read);
                }
                long asLong = hasher.hash().asLong();
                if (fis != null) {
                    try {
                        fis.close();
                    } catch (IOException e3) {
                        HwLog.w(TAG, "Got execption close fileinputstream.", e3);
                    }
                }
                return asLong;
            } catch (FileNotFoundException e4) {
                e2 = e4;
                fileInputStream = fis;
                try {
                    HwLog.w(TAG, "Got execption FileNotFound.", e2);
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e32) {
                            HwLog.w(TAG, "Got execption close fileinputstream.", e32);
                        }
                    }
                    return 0;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e322) {
                            HwLog.w(TAG, "Got execption close fileinputstream.", e322);
                        }
                    }
                    throw th;
                }
            } catch (IOException e5) {
                e322 = e5;
                fileInputStream = fis;
                HwLog.w(TAG, "Got execption IOException.", e322);
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e3222) {
                        HwLog.w(TAG, "Got execption close fileinputstream.", e3222);
                    }
                }
                return 0;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e6) {
            e2 = e6;
            HwLog.w(TAG, "Got execption FileNotFound.", e2);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return 0;
        } catch (IOException e7) {
            e3222 = e7;
            HwLog.w(TAG, "Got execption IOException.", e3222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            return 0;
        }
    }

    private long getRecordedSign(String fileName) {
        return this.mLastTimePref.getLong(fileName, -1);
    }

    private void setLastTimeToPref(String fileName, long realLastTime) {
        Editor editor = this.mLastTimePref.edit();
        editor.putLong(fileName, realLastTime);
        HwLog.i(TAG, "Save to pref:" + realLastTime + ", fileName:" + fileName);
        editor.commit();
    }

    public void finishConfigFileChange(String fileName) {
        setLastTimeToPref(fileName, getCurrentSign(fileName));
    }
}
