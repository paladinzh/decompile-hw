package com.android.settings;

import android.os.Bundle;
import android.os.Environment;
import android.os.FileUtils;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.security.KeyStore;
import android.util.Log;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Stack;

public class HwCustCredentialStorageImpl extends HwCustCredentialStorage {
    private static final int HISI_WAPI = 0;
    private static final int INVALID_WAPI = -1;
    private static final int QUALCOMM_WAPI = 1;
    private static final String TAG = "HwCustCredentialStorageImpl";
    private static final String WAPI_TYPE = "wapi_type";
    private final String DEFAULT_CERTIFICATE_PATH = (Environment.getDataDirectory().getPath() + "/wapi_certificate");
    private CredentialStorage mCredentialStorage;
    private int mWapiType;

    public HwCustCredentialStorageImpl(CredentialStorage credentialStorage) {
        super(credentialStorage);
        this.mCredentialStorage = credentialStorage;
        this.mWapiType = Global.getInt(this.mCredentialStorage.getContentResolver(), WAPI_TYPE, INVALID_WAPI);
    }

    public boolean installIfAvailable(Bundle bundle, KeyStore mKeyStore, int uid, int flags) {
        String caListName;
        byte[] caListData;
        if (bundle.containsKey("wapi_ca_certificates_name")) {
            caListName = bundle.getString("wapi_ca_certificates_name");
            caListData = bundle.getByteArray("wapi_ca_certificates_data");
            Log.e(TAG, "Credentials.EXTRA_WAPI_AS_CERTIFICATES_DATA ==> mKeyStore.importKey :" + caListName);
            if (!mKeyStore.put(caListName, caListData, uid, flags)) {
                Log.e(TAG, "############ Failed to install " + caListName);
                return true;
            } else if (1 == this.mWapiType && !copyFile("as.cer", caListName, caListData)) {
                Log.e(TAG, "############ Failed to copyFile " + caListName);
                return true;
            }
        }
        Log.e(TAG, " not not not Credentials.EXTRA_WAPI_AS_CERTIFICATES_NAME\n");
        if (bundle.containsKey("wapi_user_certificate_name")) {
            caListName = bundle.getString("wapi_user_certificate_name");
            caListData = bundle.getByteArray("wapi_user_certificate_data");
            Log.e(TAG, "Credentials.EXTRA_WAPI_USER_CERTIFICATES_DATA ==> mKeyStore.importKey :" + caListName);
            if (!mKeyStore.put(caListName, caListData, uid, flags)) {
                Log.e(TAG, "@@@@@@@@@@@@@@@@@ Failed to install " + caListName);
                return true;
            } else if (1 == this.mWapiType && !copyFile("user.cer", caListName, caListData)) {
                Log.e(TAG, "@@@@@@@@@@@@@@@@@ Failed to copyFile " + caListName);
                return true;
            }
        }
        Log.e(TAG, " not not not Credentials.EXTRA_WAPI_USER_CERTIFICATES_NAME\n");
        return false;
    }

    public void resetKeyStore(KeyStore mKeyStore) {
        if ("true".equals(SystemProperties.get("ro.config.qcom_wapi", "false"))) {
            File file = new File(this.DEFAULT_CERTIFICATE_PATH);
            if (file.exists()) {
                delFile(file);
            }
        }
    }

    private boolean delFile(File file) {
        boolean ret = true;
        if (file == null || !file.exists()) {
            return true;
        }
        Stack<File> tmpFileStack = new Stack();
        tmpFileStack.push(file);
        while (!tmpFileStack.isEmpty()) {
            try {
                File curFile = (File) tmpFileStack.pop();
                if (curFile != null) {
                    if (!curFile.isFile()) {
                        File[] tmpSubFileList = curFile.listFiles();
                        if (tmpSubFileList != null && tmpSubFileList.length != 0) {
                            tmpFileStack.push(curFile);
                            for (File item : tmpSubFileList) {
                                tmpFileStack.push(item);
                            }
                        } else if (!curFile.delete()) {
                            ret = false;
                        }
                    } else if (!curFile.delete()) {
                        ret = false;
                    }
                }
            } catch (SecurityException e) {
                ret = false;
            } catch (Exception e2) {
                ret = false;
            }
        }
        return ret;
    }

    public boolean copyFile(String cert, String alias, byte[] data) {
        Throwable th;
        OutputStream outputStream = null;
        try {
            String toFileDir = this.DEFAULT_CERTIFICATE_PATH + "/" + alias + "/";
            File toFile = new File(toFileDir);
            if (!toFile.exists()) {
                toFile.mkdirs();
            }
            FileUtils.setPermissions(this.DEFAULT_CERTIFICATE_PATH, 511, INVALID_WAPI, INVALID_WAPI);
            FileUtils.setPermissions(toFileDir, 511, INVALID_WAPI, INVALID_WAPI);
            String toFilePath = toFileDir + cert;
            OutputStream fosto = new FileOutputStream(new File(toFilePath));
            try {
                FileUtils.setPermissions(toFilePath, 292, INVALID_WAPI, INVALID_WAPI);
                fosto.write(data, 0, data.length);
                if (fosto != null) {
                    try {
                        fosto.close();
                    } catch (IOException e) {
                    }
                }
                return true;
            } catch (Exception e2) {
                outputStream = fosto;
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e3) {
                    }
                }
                return false;
            } catch (Throwable th2) {
                th = th2;
                outputStream = fosto;
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e4) {
                    }
                }
                throw th;
            }
        } catch (Exception e5) {
            if (outputStream != null) {
                outputStream.close();
            }
            return false;
        } catch (Throwable th3) {
            th = th3;
            if (outputStream != null) {
                outputStream.close();
            }
            throw th;
        }
    }
}
