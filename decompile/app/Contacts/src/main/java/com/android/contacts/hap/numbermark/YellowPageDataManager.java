package com.android.contacts.hap.numbermark;

import android.content.ContentProviderOperation;
import android.content.ContentProviderOperation.Builder;
import android.content.Context;
import android.content.OperationApplicationException;
import android.content.SharedPreferences;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.os.RemoteException;
import android.os.SystemClock;
import android.util.Base64;
import com.android.contacts.hap.provider.ContactsAppDatabaseHelper;
import com.android.contacts.hap.provider.ContactsAppProvider;
import com.android.contacts.util.ExceptionCapture;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.ArrayList;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class YellowPageDataManager {
    private Context mContext;
    private SQLiteDatabase mDatabase = this.mDatabaseHelper.getWritableDatabase();
    private ContactsAppDatabaseHelper mDatabaseHelper = ContactsAppDatabaseHelper.getInstance(this.mContext);

    public YellowPageDataManager(Context context) {
        this.mContext = context;
    }

    public void prepareYellowPageDataAsync(final boolean update) {
        this.mDatabase = this.mDatabaseHelper.getWritableDatabase();
        Thread t = new Thread(new Runnable() {
            public void run() {
                int version;
                int oldVersion = YellowPageDataManager.this.mDatabaseHelper.getProperty(YellowPageDataManager.this.mDatabase, "yellow_page_version", 0);
                if (update) {
                    version = YellowPageDataManager.this.updateYellowPage(oldVersion);
                } else {
                    version = YellowPageDataManager.this.parseYellowPageByAsset(oldVersion);
                }
                SharedPreferences sp = SharePreferenceUtil.getDefaultSp_de(YellowPageDataManager.this.mContext);
                if (version > oldVersion) {
                    YellowPageDataManager.this.mDatabaseHelper.setProperty(YellowPageDataManager.this.mDatabase, "yellow_page_version", version);
                    if (!update) {
                        YellowPageDataManager.this.editYpPreVersion(sp);
                    }
                } else if (!sp.contains("key_ver_4")) {
                    YellowPageDataManager.this.editYpPreVersion(sp);
                }
            }
        });
        t.setPriority(10);
        t.start();
    }

    private void editYpPreVersion(SharedPreferences sp) {
        sp.edit().putLong("key_ver_4", Long.parseLong(this.mContext.getString(R.string.pre_ver_yp))).apply();
    }

    private void deleteYellowPageData() {
        this.mDatabase.delete("yellow_page", null, null);
    }

    private int updateYellowPage(int oldVersion) {
        FileNotFoundException e;
        IOException ie;
        Throwable th;
        InputStreamReader inputStreamReader = null;
        try {
            InputStreamReader reader = new InputStreamReader(new FileInputStream(new File(this.mContext.createDeviceProtectedStorageContext().getFilesDir(), "yellowpage.data")), "UTF-8");
            try {
                int parseYellowPage = parseYellowPage(reader, oldVersion);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                    }
                }
                return parseYellowPage;
            } catch (FileNotFoundException e3) {
                e = e3;
                inputStreamReader = reader;
                HwLog.w("YellowPageDataManager", "file not found.", e);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e4) {
                    }
                }
                return -2;
            } catch (IOException e5) {
                ie = e5;
                inputStreamReader = reader;
                try {
                    HwLog.w("YellowPageDataManager", "read file exception.", ie);
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e6) {
                        }
                    }
                    return -2;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e7) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = reader;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e = e8;
            HwLog.w("YellowPageDataManager", "file not found.", e);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return -2;
        } catch (IOException e9) {
            ie = e9;
            HwLog.w("YellowPageDataManager", "read file exception.", ie);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return -2;
        }
    }

    private int parseYellowPageByAsset(int oldVersion) {
        FileNotFoundException e;
        IOException ie;
        SQLException sqlexc;
        Throwable th;
        InputStreamReader inputStreamReader = null;
        try {
            InputStreamReader reader = new InputStreamReader(this.mContext.getAssets().open("yellowpage.data"), "UTF-8");
            try {
                int parseYellowPage = parseYellowPage(reader, oldVersion);
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e2) {
                    }
                }
                return parseYellowPage;
            } catch (FileNotFoundException e3) {
                e = e3;
                inputStreamReader = reader;
                HwLog.w("YellowPageDataManager", "file not found.", e);
                ExceptionCapture.captureYellowPageException("YellowPageDataManager->parseYellowPageByAsset file not found.", e);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e4) {
                    }
                }
                return -2;
            } catch (IOException e5) {
                ie = e5;
                inputStreamReader = reader;
                HwLog.w("YellowPageDataManager", "read file exception.", ie);
                ExceptionCapture.captureYellowPageException("YellowPageDataManager->parseYellowPageByAsset read file exception.", ie);
                if (inputStreamReader != null) {
                    try {
                        inputStreamReader.close();
                    } catch (IOException e6) {
                    }
                }
                return -2;
            } catch (SQLException e7) {
                sqlexc = e7;
                inputStreamReader = reader;
                try {
                    HwLog.w("YellowPageDataManager", "database exception", sqlexc);
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e8) {
                        }
                    }
                    return -3;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStreamReader != null) {
                        try {
                            inputStreamReader.close();
                        } catch (IOException e9) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                inputStreamReader = reader;
                if (inputStreamReader != null) {
                    inputStreamReader.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
            HwLog.w("YellowPageDataManager", "file not found.", e);
            ExceptionCapture.captureYellowPageException("YellowPageDataManager->parseYellowPageByAsset file not found.", e);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return -2;
        } catch (IOException e11) {
            ie = e11;
            HwLog.w("YellowPageDataManager", "read file exception.", ie);
            ExceptionCapture.captureYellowPageException("YellowPageDataManager->parseYellowPageByAsset read file exception.", ie);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return -2;
        } catch (SQLException e12) {
            sqlexc = e12;
            HwLog.w("YellowPageDataManager", "database exception", sqlexc);
            if (inputStreamReader != null) {
                inputStreamReader.close();
            }
            return -3;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private int parseYellowPage(InputStreamReader reader, int oldVersion) {
        IOException ie;
        JSONException jse;
        Throwable th;
        BufferedReader bufferedReader = null;
        ArrayList<ContentProviderOperation> mOperationList = new ArrayList();
        int version = oldVersion;
        try {
            BufferedReader bufferedReader2 = new BufferedReader(reader);
            int lineNumber = 0;
            while (true) {
                try {
                    String line = bufferedReader2.readLine();
                    if (line == null) {
                        break;
                    } else if (lineNumber == 0) {
                        version = new JSONObject(line).getInt("version");
                        if (version <= oldVersion) {
                            break;
                        }
                        deleteYellowPageData();
                        lineNumber++;
                    } else {
                        parseJsonObject(mOperationList, line);
                        if (mOperationList.size() > VTMCDataCache.MAX_EXPIREDTIME) {
                            applyBatch(mOperationList);
                            mOperationList.clear();
                            SystemClock.sleep(350);
                        }
                        lineNumber++;
                    }
                } catch (IOException e) {
                    ie = e;
                    bufferedReader = bufferedReader2;
                } catch (JSONException e2) {
                    jse = e2;
                    bufferedReader = bufferedReader2;
                } catch (Throwable th2) {
                    th = th2;
                    bufferedReader = bufferedReader2;
                }
            }
            if (mOperationList.size() > 0) {
                applyBatch(mOperationList);
                mOperationList.clear();
            }
            if (bufferedReader2 != null) {
                try {
                    bufferedReader2.close();
                } catch (IOException e3) {
                }
            }
            return version;
            return oldVersion;
        } catch (IOException e4) {
            ie = e4;
            HwLog.w("YellowPageDataManager", "read file exception.", ie);
            if (bufferedReader != null) {
                try {
                    bufferedReader.close();
                } catch (IOException e5) {
                }
            }
            return -1;
        } catch (JSONException e6) {
            jse = e6;
            try {
                HwLog.w("YellowPageDataManager", "json format error", jse);
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e7) {
                    }
                }
                return -1;
            } catch (Throwable th3) {
                th = th3;
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e8) {
                    }
                }
                throw th;
            }
        }
    }

    private void parseJsonObject(ArrayList<ContentProviderOperation> operationList, String line) throws JSONException {
        JSONObject entry = new JSONObject(line);
        int referenceId = operationList.size();
        Builder builder = ContentProviderOperation.newInsert(ContactsAppProvider.YELLOW_PAGE_URI);
        builder.withValue("name", entry.getString("name"));
        builder.withValue("group_name", entry.getString("group"));
        if (entry.has("photo")) {
            String str = entry.getString("photo");
            entry.remove("photo");
            try {
                String filepath = writePhotoToFile(this.mContext, str);
                if (filepath != null) {
                    builder.withValue("photo", filepath);
                }
            } catch (IllegalStateException ise) {
                ise.printStackTrace();
            }
            builder.withValue(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, entry.toString());
        } else {
            builder.withValue(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, line);
        }
        operationList.add(builder.build());
        JSONArray array = entry.getJSONArray("phone");
        for (int i = 0; i < array.length(); i++) {
            JSONObject obj = array.getJSONObject(i);
            builder = ContentProviderOperation.newInsert(ContactsAppProvider.YELLOW_PAGE_DATA_URI);
            builder.withValue("name", obj.getString("name"));
            builder.withValue("dial_map", obj.getString("dial_map"));
            builder.withValue("hot_points", Double.valueOf(obj.getDouble("hot_points")));
            builder.withValue("number", obj.getString("phone"));
            builder.withValueBackReference("ypid", referenceId);
            operationList.add(builder.build());
        }
    }

    private String writePhotoToFile(Context context, String photoData) {
        IOException e;
        FileNotFoundException e2;
        IllegalArgumentException ie;
        Throwable th;
        if (photoData == null) {
            return null;
        }
        String path = getFileName(context);
        File partFile = new File(path);
        if (!partFile.exists()) {
            try {
                if (!partFile.createNewFile()) {
                    throw new IllegalStateException("Unable to create new file: " + path);
                }
            } catch (IOException e3) {
                throw new IllegalStateException("Unable to create new file: " + path);
            }
        }
        OutputStream outputStream = null;
        try {
            OutputStream os = new FileOutputStream(partFile);
            try {
                os.write(Base64.decode(photoData, 0));
                os.flush();
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e4) {
                        e4.printStackTrace();
                    }
                }
                outputStream = os;
            } catch (FileNotFoundException e5) {
                e2 = e5;
                outputStream = os;
                path = null;
                e2.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e42) {
                        e42.printStackTrace();
                    }
                }
                return path;
            } catch (IOException e6) {
                e42 = e6;
                outputStream = os;
                path = null;
                e42.printStackTrace();
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e422) {
                        e422.printStackTrace();
                    }
                }
                return path;
            } catch (IllegalArgumentException e7) {
                ie = e7;
                outputStream = os;
                path = null;
                try {
                    ie.printStackTrace();
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e4222) {
                            e4222.printStackTrace();
                        }
                    }
                    return path;
                } catch (Throwable th2) {
                    th = th2;
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e42222) {
                            e42222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                outputStream = os;
                if (outputStream != null) {
                    outputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e8) {
            e2 = e8;
            path = null;
            e2.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            return path;
        } catch (IOException e9) {
            e42222 = e9;
            path = null;
            e42222.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            return path;
        } catch (IllegalArgumentException e10) {
            ie = e10;
            path = null;
            ie.printStackTrace();
            if (outputStream != null) {
                outputStream.close();
            }
            return path;
        }
        return path;
    }

    private String getFileName(Context context) {
        String path = context.getDir("parts", 0).getPath() + "/PART_";
        long time = System.currentTimeMillis();
        File partFile = new File(path + time);
        int i = 0;
        while (partFile.exists()) {
            partFile = new File(path + time + i);
            i++;
        }
        return partFile.getPath();
    }

    private void applyBatch(ArrayList<ContentProviderOperation> operationList) {
        try {
            this.mContext.getContentResolver().applyBatch("com.android.contacts.app", operationList);
        } catch (RemoteException e) {
            HwLog.e("YellowPageDataManager", String.format("%s: %s", new Object[]{e.toString(), e.getMessage()}));
        } catch (OperationApplicationException e2) {
            HwLog.e("YellowPageDataManager", String.format("%s: %s", new Object[]{e2.toString(), e2.getMessage()}));
        }
    }
}
