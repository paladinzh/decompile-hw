package android.content.pm;

import android.app.ActivityManagerNative;
import android.content.pm.PackageParser.Activity;
import android.content.pm.PackageParser.PackageParserException;
import android.os.Parcel;
import android.os.RemoteException;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.util.Log;
import android.util.Xml;
import com.android.internal.util.XmlUtils;
import com.huawei.hsm.permission.StubController;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class HwPackageParser implements IHwPackageParser {
    private static final String APP_NAME = "app";
    private static final String ATTR_NAME = "name";
    private static final String CUST_FILE_DIR = "system/etc";
    private static final String CUST_FILE_NAME = "benchmar_app.xml";
    private static final boolean FASTBOOT_UNLOCK = SystemProperties.getBoolean("ro.fastboot.unlock", false);
    private static final boolean HIDE_PRODUCT_INFO = SystemProperties.getBoolean("ro.build.hide", false);
    private static final boolean IS_32BIT_SYSTEM = "zygote32".equals(SystemProperties.get("ro.zygote", "zygote64_32"));
    private static final boolean IS_FILTER_LARGEHEAP = SystemProperties.getBoolean("ro.config.hw_filter_largeheap", false);
    public static final boolean IS_SUPPORT_CLONE_APP = SystemProperties.getBoolean("ro.config.hw_support_clone_app", false);
    private static final int MAX_NUM = 500;
    private static final String TAG = "BENCHMAR_APP";
    private static final String TAG_ARRAY = "array";
    private static final String TAG_ARRAYITEM = "value";
    private static final String TAG_DEVICE = "device";
    private static final String TAG_HWPP = "HwPackageParser";
    private static final String TAG_ITEM = "item";
    private static Set<String> mBenchmarkApp;
    private static HwPackageParser mInstance = null;
    private static final Object mInstanceLock = new Object();
    private static final Object mLock = new Object();
    private static final HashMap<String, Object> sAppMap = new HashMap();
    private static ArrayList<String> sDisableLargeHeapAppList = new ArrayList();

    static {
        sDisableLargeHeapAppList.add("com.tencent.mm");
    }

    protected void HwPackageParser() {
    }

    public static HwPackageParser getDefault() {
        if (mInstance == null) {
            synchronized (mInstanceLock) {
                if (mInstance == null) {
                    mInstance = new HwPackageParser();
                }
            }
        }
        return mInstance;
    }

    public void initMetaData(Activity a) {
        String navigationHide = a.metaData.getString("hwc-navi");
        if (navigationHide == null) {
            return;
        }
        if (navigationHide.startsWith("ro.config")) {
            a.info.navigationHide = SystemProperties.getBoolean(navigationHide, false);
            return;
        }
        a.info.navigationHide = true;
    }

    private boolean readBenchmarkAppFromXml(HashMap<String, Object> sMap, String fileDir, String fileName) {
        Throwable th;
        File mFile = new File(fileDir, fileName);
        InputStream inputStream = null;
        if (!mFile.exists()) {
            return false;
        }
        if (mFile.canRead()) {
            try {
                InputStream inputStream2 = new FileInputStream(mFile);
                try {
                    XmlPullParser parser = Xml.newPullParser();
                    parser.setInput(inputStream2, null);
                    boolean parsingArray = false;
                    ArrayList<String> array = new ArrayList();
                    Object arrayName = null;
                    int i = 0;
                    while (true) {
                        int i2 = i + 1;
                        if (i <= MAX_NUM) {
                            XmlUtils.nextElement(parser);
                            String element = parser.getName();
                            if (element == null) {
                                break;
                            }
                            if (parsingArray && !element.equals("value")) {
                                sMap.put(arrayName, array.toArray(new String[array.size()]));
                                parsingArray = false;
                            }
                            if (element.equals(TAG_ARRAY)) {
                                parsingArray = true;
                                array.clear();
                                arrayName = parser.getAttributeValue(null, ATTR_NAME);
                            } else if (element.equals(TAG_ITEM) || element.equals("value")) {
                                Object name = null;
                                if (!parsingArray) {
                                    name = parser.getAttributeValue(null, ATTR_NAME);
                                }
                                if (parser.next() == 4) {
                                    String value = parser.getText();
                                    if (element.equals(TAG_ITEM)) {
                                        sMap.put(name, value);
                                    } else if (parsingArray) {
                                        array.add(value);
                                    } else {
                                        continue;
                                    }
                                } else {
                                    continue;
                                }
                            }
                            i = i2;
                        } else {
                            break;
                        }
                    }
                    if (parsingArray) {
                        sMap.put(arrayName, array.toArray(new String[array.size()]));
                    }
                    if (inputStream2 != null) {
                        try {
                            inputStream2.close();
                        } catch (IOException e) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    inputStream = inputStream2;
                } catch (XmlPullParserException e2) {
                    inputStream = inputStream2;
                } catch (IOException e3) {
                    inputStream = inputStream2;
                } catch (Throwable th2) {
                    th = th2;
                    inputStream = inputStream2;
                }
            } catch (XmlPullParserException e4) {
                try {
                    Log.w(TAG, "readBenchmarkAppFromXml  XmlPullParserException");
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e5) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                    return true;
                } catch (Throwable th3) {
                    th = th3;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e6) {
                            Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                        }
                    }
                    throw th;
                }
            } catch (IOException e7) {
                Log.w(TAG, "readBenchmarkAppFromXml  IOException");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e8) {
                        Log.w(TAG, "readBenchmarkAppFromXml  inputStream close IOException");
                    }
                }
                Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
                return true;
            }
            Log.w(TAG, fileDir + "/" + CUST_FILE_NAME + " be read ! ");
            return true;
        }
        Log.w(TAG, "benchmar_app.xml not found! name maybe not right!");
        return false;
    }

    public void needStopApp(String packageName, File packageDir) throws PackageParserException {
        if (HIDE_PRODUCT_INFO || FASTBOOT_UNLOCK) {
            synchronized (mLock) {
                if (mBenchmarkApp == null) {
                    mBenchmarkApp = new HashSet();
                    readBenchmarkAppFromXml(sAppMap, CUST_FILE_DIR, CUST_FILE_NAME);
                    String[] BenchmarkApp = (String[]) sAppMap.get(APP_NAME);
                    if (BenchmarkApp != null) {
                        for (Object add : BenchmarkApp) {
                            mBenchmarkApp.add(add);
                        }
                    }
                }
                for (String appName : mBenchmarkApp) {
                    if (packageName.contains(appName)) {
                        throw new PackageParserException(-2, "Inconsistent package " + packageName + " in " + packageDir);
                    }
                }
            }
        }
    }

    public void changeApplicationEuidIfNeeded(ApplicationInfo ai, int flags) {
        if (IS_SUPPORT_CLONE_APP && (StubController.PERMISSION_MOBILEDATE & flags) != 0 && isPackageCloned(ai.packageName, UserHandle.getUserId(ai.uid))) {
            Log.i(TAG, "generateApplicationInfo for cloned app: " + ai.packageName);
            ai.euid = 2147383647;
            String str = ai.deviceProtectedDataDir + File.separator + "_hwclone";
            ai.deviceProtectedDataDir = str;
            ai.deviceEncryptedDataDir = str;
            str = ai.credentialProtectedDataDir + File.separator + "_hwclone";
            ai.credentialProtectedDataDir = str;
            ai.credentialEncryptedDataDir = str;
            ai.dataDir += File.separator + "_hwclone";
        }
    }

    private static boolean isPackageCloned(String packageName, int userId) {
        boolean res = false;
        try {
            Parcel data = Parcel.obtain();
            Parcel reply = Parcel.obtain();
            data.writeInterfaceToken("android.app.IActivityManager");
            data.writeString(packageName);
            data.writeInt(userId);
            ActivityManagerNative.getDefault().asBinder().transact(505, data, reply, 0);
            reply.readException();
            res = reply.readInt() != 0;
            data.recycle();
            reply.recycle();
        } catch (RemoteException e) {
            Log.e(TAG, "isPackageCloned", e);
        }
        return res;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean isEnableLargeHeapApp(String pkgName) {
        if (!IS_FILTER_LARGEHEAP || pkgName == null || !IS_32BIT_SYSTEM || !sDisableLargeHeapAppList.contains(pkgName)) {
            return true;
        }
        Log.i(TAG_HWPP, "Disable Large heap for package: " + pkgName);
        return false;
    }
}
