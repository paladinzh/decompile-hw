package com.huawei.systemmanager.securitythreats.ui;

import android.text.TextUtils;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.util.HwLog;
import com.huawei.systemmanager.util.app.HsmPkgInfo;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;

public class VirusPkg {
    private static final char[] HEX_CHAR = new char[]{'0', '1', '2', '3', '4', '5', '6', '7', '8', '9', 'a', 'b', 'c', 'd', 'e', 'f'};
    private static final String TAG = "VirusPkg";
    private final String mDescription;
    private final String mPackageName;
    private final List<Version> mVersionList = Lists.newArrayList();
    private final String mVirus;

    public static class Version {
        private String mSha256;
        private String mVersionCode;

        public String getCode() {
            return this.mVersionCode;
        }

        public void setCode(String versionCode) {
            this.mVersionCode = versionCode;
        }

        public String getSha256() {
            return this.mSha256;
        }

        public void setSha256(String sha256) {
            this.mSha256 = sha256;
        }
    }

    public VirusPkg(String packageName, String virus, String description) {
        this.mPackageName = packageName;
        this.mVirus = virus;
        this.mDescription = description;
    }

    public String getPackageName() {
        return this.mPackageName;
    }

    public String getVirus() {
        return this.mVirus;
    }

    public String getDescription() {
        return this.mDescription;
    }

    public void addVerison(Version version) {
        this.mVersionList.add(version);
    }

    public boolean match(HsmPkgInfo info) {
        if (info == null) {
            HwLog.i(TAG, "match info is null");
            return false;
        } else if (!TextUtils.equals(this.mPackageName, info.getPackageName())) {
            HwLog.i(TAG, "match package name not equals");
            return false;
        } else if (this.mVersionList.isEmpty()) {
            HwLog.i(TAG, "match all versions");
            return true;
        } else {
            String versionCode = String.valueOf(info.getVersionCode());
            for (Version version : this.mVersionList) {
                if (TextUtils.equals(versionCode, version.getCode())) {
                    if (TextUtils.isEmpty(version.getSha256())) {
                        HwLog.i(TAG, "match version=" + versionCode);
                        return true;
                    }
                    boolean sha256Equals = TextUtils.equals(getSha256ContentsDigest(info.getPath(), info.mFileName), version.getSha256());
                    HwLog.i(TAG, "match version=" + versionCode + ", sha256Equals=" + sha256Equals);
                    return sha256Equals;
                }
            }
            HwLog.i(TAG, "match no version");
            return false;
        }
    }

    public static String getSha256ContentsDigest(String sourceDir, String sourceName) {
        IOException e;
        Throwable th;
        File file = new File(sourceDir, sourceName);
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] buf = new byte[8192];
            InputStream inputStream = null;
            try {
                InputStream in = new BufferedInputStream(new FileInputStream(file), buf.length);
                try {
                    int chunkSize = in.read(buf);
                    if (-1 == chunkSize) {
                        if (in != null) {
                            try {
                                in.close();
                            } catch (IOException e2) {
                                HwLog.e(TAG, "stream closed failed", e2);
                            }
                        }
                        return null;
                    }
                    digest.update(buf, 0, chunkSize);
                    while (true) {
                        chunkSize = in.read(buf);
                        if (chunkSize == -1) {
                            break;
                        }
                        digest.update(buf, 0, chunkSize);
                    }
                    if (in != null) {
                        try {
                            in.close();
                        } catch (IOException e22) {
                            HwLog.e(TAG, "stream closed failed", e22);
                        }
                    }
                    return toHexString(digest.digest());
                } catch (IOException e3) {
                    e22 = e3;
                    inputStream = in;
                    try {
                        HwLog.e(TAG, "getSha256ContentsDigest IOException", e22);
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222) {
                                HwLog.e(TAG, "stream closed failed", e222);
                            }
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e2222) {
                                HwLog.e(TAG, "stream closed failed", e2222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    inputStream = in;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e2222 = e4;
                HwLog.e(TAG, "getSha256ContentsDigest IOException", e2222);
                if (inputStream != null) {
                    inputStream.close();
                }
                return null;
            }
        } catch (NoSuchAlgorithmException e5) {
            HwLog.e(TAG, "SHA-256 not available {}", e5);
            return null;
        }
    }

    private static String toHexString(byte[] b) {
        StringBuilder sb = new StringBuilder(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            sb.append(HEX_CHAR[(b[i] & 240) >>> 4]);
            sb.append(HEX_CHAR[b[i] & 15]);
        }
        return sb.toString();
    }
}
