package com.android.contacts.hap;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.res.XmlResourceParser;
import android.net.ParseException;
import android.os.UserManager;
import android.util.Xml;
import com.android.contacts.ContactsApplication;
import com.android.contacts.activities.DialtactsActivity;
import com.android.contacts.compatibility.QueryUtil;
import com.android.contacts.hap.optimize.BackgroundCacheHdlr;
import com.android.contacts.hap.sim.SimFactoryManager;
import com.android.contacts.preference.UpdateUtil;
import com.android.contacts.util.ContactsThreadPool;
import com.android.contacts.util.HwLog;
import com.android.contacts.util.SharePreferenceUtil;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class PreBootReceiver extends BroadcastReceiver {
    private static String cust = "/data/cust/xml/predefined_contacts_cust.xml";
    private static String etc = "/system/etc/xml/predefined_contacts_cust.xml";

    public void onReceive(Context context, Intent intent) {
        if (!(intent == null || intent.getAction() == null)) {
            if ("android.intent.action.BOOT_COMPLETED".equals(intent.getAction())) {
                QueryUtil.reInit(context.getApplicationContext());
                SimFactoryManager.reHandleSimState(context);
                migrateDataToDeviceProtectedStrorage(context);
                parseForClearCallLog(context, etc);
                parseForClearCallLog(context, cust);
                if (context.getApplicationContext() instanceof ContactsApplication) {
                    ((ContactsApplication) context.getApplicationContext()).reInitSync();
                }
            } else if ("android.intent.action.USER_INITIALIZE".equals(intent.getAction())) {
                UserManager um = (UserManager) context.getSystemService("user");
                if (um.getUserInfo(um.getUserHandle()).isManagedProfile()) {
                    context.getPackageManager().setComponentEnabledSetting(new ComponentName(context, DialtactsActivity.class), 2, 1);
                }
                return;
            }
        }
        Editor editor = SharePreferenceUtil.getDefaultSp_de(context).edit();
        editor.putBoolean("contact_boot_key", true);
        editor.commit();
        if (BackgroundCacheHdlr.haveNotBeenInflate()) {
            BackgroundCacheHdlr.inflateLayoutsInBackground(context);
        }
    }

    private void migrateDataToDeviceProtectedStrorage(final Context context) {
        ContactsThreadPool.getInstance().execute(new Runnable() {
            public void run() {
                HwLog.i("PreBootReceiver", "PreBootReceiver,migrateDataToDeviceProtectedStrorage begin");
                Context deviceContext = context.createDeviceProtectedStorageContext();
                new SharedPreferencesTransfer(SharePreferenceUtil.getDefaultSp_ce(context), SharePreferenceUtil.getDefaultSp_de(context)).moveSharedPreferences();
                HwLog.i("PreBootReceiver", "transfer.moveSharedPreferences");
                PreBootReceiver.this.processMoveFile(new File(context.getFilesDir() + File.separator + "numberlocation.dat"), new File(deviceContext.getFilesDir() + File.separator + "numberlocation.dat"));
                PreBootReceiver.this.processMoveFile(new File(context.getFilesDir() + File.separator + "yellowpage.data"), new File(deviceContext.getFilesDir() + File.separator + "yellowpage.data"));
                PreBootReceiver.this.processMoveFile(new File(context.getFilesDir().getAbsolutePath().replace("com.android.contacts", "com.huawei.contactscamcard") + File.separator + "IS_BCRAllTemplete.dat"), new File(deviceContext.getFilesDir().getAbsolutePath().replace("com.android.contacts", "com.huawei.contactscamcard") + File.separator + "IS_BCRAllTemplete.dat"));
                PreBootReceiver.this.moveSharePreference(context);
                HwLog.i("PreBootReceiver", "PreBootReceiver,migrateDataToDeviceProtectedStrorage end");
            }
        });
    }

    protected void processMoveFile(File srcFile, File destFile) {
        try {
            if (srcFile.exists()) {
                if (destFile.exists() && !destFile.delete()) {
                    HwLog.i("PreBootReceiver", "delete dest file in DE failed");
                }
                if (!srcFile.renameTo(destFile)) {
                    moveFile(srcFile, destFile);
                }
            }
        } catch (SecurityException e) {
            HwLog.e("PreBootReceiver", "SecurityException when move file:numberlocation.dat");
        }
    }

    private void moveFile(File srcFile, File destFile) {
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        int readSum = 0;
        byte[] buffer = new byte[10000];
        FileOutputStream fileOutputStream = null;
        FileInputStream fileInputStream = null;
        BufferedInputStream bufferedInputStream = null;
        BufferedOutputStream bufferedOutputStream = null;
        try {
            FileInputStream fis = new FileInputStream(srcFile);
            try {
                BufferedInputStream bis = new BufferedInputStream(fis);
                try {
                    FileOutputStream fos = new FileOutputStream(destFile);
                    try {
                        BufferedOutputStream bos = new BufferedOutputStream(fos);
                        while (true) {
                            try {
                                int readLen = bis.read(buffer);
                                if (readLen == -1) {
                                    break;
                                }
                                bos.write(buffer, 0, readLen);
                                readSum += readLen;
                            } catch (FileNotFoundException e3) {
                                e = e3;
                                bufferedOutputStream = bos;
                                bufferedInputStream = bis;
                                fileInputStream = fis;
                                fileOutputStream = fos;
                            } catch (IOException e4) {
                                e2 = e4;
                                bufferedOutputStream = bos;
                                bufferedInputStream = bis;
                                fileInputStream = fis;
                                fileOutputStream = fos;
                            } catch (Throwable th2) {
                                th = th2;
                                bufferedOutputStream = bos;
                                bufferedInputStream = bis;
                                fileInputStream = fis;
                                fileOutputStream = fos;
                            }
                        }
                        bos.flush();
                        HwLog.i("PreBootReceiver", "moveFile coplete,size=" + readSum);
                        if (fis != null) {
                            try {
                                fis.close();
                            } catch (IOException e22) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22);
                            }
                        }
                        if (bis != null) {
                            try {
                                bis.close();
                            } catch (IOException e222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e222);
                            }
                        }
                        if (fos != null) {
                            try {
                                fos.close();
                            } catch (IOException e2222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e2222);
                            }
                        }
                        if (bos != null) {
                            try {
                                bos.close();
                            } catch (IOException e22222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22222);
                            }
                        }
                        if (true) {
                            try {
                                if (!srcFile.delete()) {
                                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                                }
                            } catch (SecurityException e5) {
                                HwLog.e("PreBootReceiver", "movefile,SecurityException when delete:" + e5);
                            }
                        }
                        fileOutputStream = fos;
                    } catch (FileNotFoundException e6) {
                        e = e6;
                        bufferedInputStream = bis;
                        fileInputStream = fis;
                        fileOutputStream = fos;
                        HwLog.e("PreBootReceiver", "movefile,FileNotFoundException:" + e);
                        if (fileInputStream != null) {
                            try {
                                fileInputStream.close();
                            } catch (IOException e222222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e222222);
                            }
                        }
                        if (bufferedInputStream != null) {
                            try {
                                bufferedInputStream.close();
                            } catch (IOException e2222222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e2222222);
                            }
                        }
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e22222222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22222222);
                            }
                        }
                        if (bufferedOutputStream != null) {
                            try {
                                bufferedOutputStream.close();
                            } catch (IOException e222222222) {
                                HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e222222222);
                            }
                        }
                        if (null != null) {
                            try {
                                if (srcFile.delete()) {
                                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                                }
                            } catch (SecurityException e52) {
                                HwLog.e("PreBootReceiver", "movefile,SecurityException when delete:" + e52);
                            }
                        }
                    } catch (IOException e7) {
                        e222222222 = e7;
                        bufferedInputStream = bis;
                        fileInputStream = fis;
                        fileOutputStream = fos;
                        try {
                            HwLog.e("PreBootReceiver", "movefile,IOException:" + e222222222);
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e2222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e2222222222);
                                }
                            }
                            if (bufferedInputStream != null) {
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e22222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22222222222);
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e222222222222);
                                }
                            }
                            if (bufferedOutputStream != null) {
                                try {
                                    bufferedOutputStream.close();
                                } catch (IOException e2222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e2222222222222);
                                }
                            }
                            if (null != null) {
                                try {
                                    if (srcFile.delete()) {
                                        HwLog.e("PreBootReceiver", "delete src file in CE failed");
                                    }
                                } catch (SecurityException e522) {
                                    HwLog.e("PreBootReceiver", "movefile,SecurityException when delete:" + e522);
                                }
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            if (fileInputStream != null) {
                                try {
                                    fileInputStream.close();
                                } catch (IOException e22222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22222222222222);
                                }
                            }
                            if (bufferedInputStream != null) {
                                try {
                                    bufferedInputStream.close();
                                } catch (IOException e222222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e222222222222222);
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e2222222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e2222222222222222);
                                }
                            }
                            if (bufferedOutputStream != null) {
                                try {
                                    bufferedOutputStream.close();
                                } catch (IOException e22222222222222222) {
                                    HwLog.e("PreBootReceiver", "movefile,IOException when close:" + e22222222222222222);
                                }
                            }
                            if (null != null) {
                                try {
                                    if (!srcFile.delete()) {
                                        HwLog.e("PreBootReceiver", "delete src file in CE failed");
                                    }
                                } catch (SecurityException e5222) {
                                    HwLog.e("PreBootReceiver", "movefile,SecurityException when delete:" + e5222);
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        bufferedInputStream = bis;
                        fileInputStream = fis;
                        fileOutputStream = fos;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (bufferedInputStream != null) {
                            bufferedInputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        if (bufferedOutputStream != null) {
                            bufferedOutputStream.close();
                        }
                        if (null != null) {
                            if (srcFile.delete()) {
                                HwLog.e("PreBootReceiver", "delete src file in CE failed");
                            }
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e8) {
                    e = e8;
                    bufferedInputStream = bis;
                    fileInputStream = fis;
                    HwLog.e("PreBootReceiver", "movefile,FileNotFoundException:" + e);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (null != null) {
                        if (srcFile.delete()) {
                            HwLog.e("PreBootReceiver", "delete src file in CE failed");
                        }
                    }
                } catch (IOException e9) {
                    e22222222222222222 = e9;
                    bufferedInputStream = bis;
                    fileInputStream = fis;
                    HwLog.e("PreBootReceiver", "movefile,IOException:" + e22222222222222222);
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (null != null) {
                        if (srcFile.delete()) {
                            HwLog.e("PreBootReceiver", "delete src file in CE failed");
                        }
                    }
                } catch (Throwable th5) {
                    th = th5;
                    bufferedInputStream = bis;
                    fileInputStream = fis;
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    if (bufferedInputStream != null) {
                        bufferedInputStream.close();
                    }
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (bufferedOutputStream != null) {
                        bufferedOutputStream.close();
                    }
                    if (null != null) {
                        if (srcFile.delete()) {
                            HwLog.e("PreBootReceiver", "delete src file in CE failed");
                        }
                    }
                    throw th;
                }
            } catch (FileNotFoundException e10) {
                e = e10;
                fileInputStream = fis;
                HwLog.e("PreBootReceiver", "movefile,FileNotFoundException:" + e);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (null != null) {
                }
                if (srcFile.delete()) {
                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                }
            } catch (IOException e11) {
                e22222222222222222 = e11;
                fileInputStream = fis;
                HwLog.e("PreBootReceiver", "movefile,IOException:" + e22222222222222222);
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (null != null) {
                }
                if (srcFile.delete()) {
                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                }
            } catch (Throwable th6) {
                th = th6;
                fileInputStream = fis;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (bufferedInputStream != null) {
                    bufferedInputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (bufferedOutputStream != null) {
                    bufferedOutputStream.close();
                }
                if (null != null) {
                    if (srcFile.delete()) {
                        HwLog.e("PreBootReceiver", "delete src file in CE failed");
                    }
                }
                throw th;
            }
        } catch (FileNotFoundException e12) {
            e = e12;
            HwLog.e("PreBootReceiver", "movefile,FileNotFoundException:" + e);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (null != null) {
                if (srcFile.delete()) {
                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                }
            }
        } catch (IOException e13) {
            e22222222222222222 = e13;
            HwLog.e("PreBootReceiver", "movefile,IOException:" + e22222222222222222);
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (bufferedInputStream != null) {
                bufferedInputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (bufferedOutputStream != null) {
                bufferedOutputStream.close();
            }
            if (null != null) {
                if (srcFile.delete()) {
                    HwLog.e("PreBootReceiver", "delete src file in CE failed");
                }
            }
        }
    }

    private void moveSharePreference(Context context) {
        SharedPreferences ce = SharePreferenceUtil.getDefaultSp_ce(context);
        SharedPreferences de = SharePreferenceUtil.getDefaultSp_de(context);
        String key = UpdateUtil.getSummaryKey(1);
        long currentVersion = ce.getLong(key, 0);
        if (currentVersion != 0) {
            de.edit().putLong(key, currentVersion).apply();
            ce.edit().remove(key).apply();
            HwLog.i("PreBootReceiver", "moveSharePreference complete");
        }
    }

    private static void parseForClearCallLog(Context context, String path) {
        IOException ie;
        XmlPullParserException e;
        Throwable th;
        String PATH = path;
        InputStream inputStream = null;
        XmlPullParser xmlPullParser = null;
        try {
            InputStream lInput = new FileInputStream(path);
            try {
                xmlPullParser = Xml.newPullParser();
                if (xmlPullParser == null) {
                    if (lInput != null) {
                        try {
                            lInput.close();
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                    }
                    if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                        try {
                            xmlPullParser.setInput(null);
                        } catch (Exception e2) {
                            e2.printStackTrace();
                        }
                    }
                    return;
                }
                xmlPullParser.setInput(lInput, null);
                int eventType = xmlPullParser.getEventType();
                while (1 != eventType) {
                    while (true) {
                        if (2 == eventType) {
                            String lAttribute;
                            String lTag = xmlPullParser.getName();
                            if ("bool".equals(lTag)) {
                                lAttribute = xmlPullParser.getAttributeValue(null, "name");
                                boolean boolValue = false;
                                try {
                                    boolValue = Boolean.parseBoolean(xmlPullParser.nextText());
                                } catch (ParseException e3) {
                                    HwLog.e("PreBootReceiver", "read boolean value faild from configure file for " + lAttribute);
                                }
                                SharePreferenceUtil.getDefaultSp_de(context).edit().putBoolean(lAttribute, boolValue).commit();
                            }
                            if ("int".equals(lTag)) {
                                lAttribute = xmlPullParser.getAttributeValue(null, "name");
                                int intValue = 0;
                                try {
                                    intValue = Integer.parseInt(xmlPullParser.getAttributeValue(null, "value"));
                                } catch (ParseException e4) {
                                }
                                SharePreferenceUtil.getDefaultSp_de(context).edit().putInt(lAttribute, intValue).commit();
                            }
                            if ("string".equals(lTag)) {
                                lAttribute = xmlPullParser.getAttributeValue(null, "name");
                                String str = null;
                                try {
                                    str = xmlPullParser.nextText();
                                } catch (ParseException e5) {
                                }
                                SharePreferenceUtil.getDefaultSp_de(context).edit().putString(lAttribute, str).commit();
                            } else {
                                continue;
                            }
                        } else if (3 == eventType) {
                            break;
                        }
                        eventType = xmlPullParser.next();
                    }
                    eventType = xmlPullParser.next();
                }
                context.getPackageManager().setComponentEnabledSetting(new ComponentName("com.android.contacts", "com.android.contacts.hap.PreBootReceiver"), 2, 1);
                if (lInput != null) {
                    try {
                        lInput.close();
                    } catch (IOException e12) {
                        e12.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (IOException e6) {
                ie = e6;
                inputStream = lInput;
            } catch (XmlPullParserException e7) {
                e = e7;
                inputStream = lInput;
            } catch (Throwable th2) {
                th = th2;
                inputStream = lInput;
            }
        } catch (IOException e8) {
            ie = e8;
            try {
                ie.printStackTrace();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e122) {
                        e122.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e222) {
                        e222.printStackTrace();
                    }
                }
            } catch (Throwable th3) {
                th = th3;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e1222) {
                        e1222.printStackTrace();
                    }
                }
                if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                    try {
                        xmlPullParser.setInput(null);
                    } catch (Exception e2222) {
                        e2222.printStackTrace();
                    }
                }
                throw th;
            }
        } catch (XmlPullParserException e9) {
            e = e9;
            e.printStackTrace();
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e12222) {
                    e12222.printStackTrace();
                }
            }
            if (!(xmlPullParser == null || XmlResourceParser.class.isInstance(xmlPullParser))) {
                try {
                    xmlPullParser.setInput(null);
                } catch (Exception e22222) {
                    e22222.printStackTrace();
                }
            }
        }
    }
}
