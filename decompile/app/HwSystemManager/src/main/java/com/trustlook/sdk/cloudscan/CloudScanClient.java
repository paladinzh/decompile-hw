package com.trustlook.sdk.cloudscan;

import android.content.Context;
import android.util.Log;
import com.huawei.systemmanager.push.PushResponse;
import com.trustlook.sdk.Constants;
import com.trustlook.sdk.data.DataUtils;
import com.trustlook.sdk.data.PkgInfo;
import com.trustlook.sdk.data.Region;
import com.trustlook.sdk.database.DBManager;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.json.JSONArray;
import org.json.JSONException;

public class CloudScanClient {
    public static final int PUA = 7;
    public static final int RISKY = 8;
    public static final int UNKNOWN = -1;
    private static final Map<Region, String> i;
    public static String version = "2.0.0";
    int a;
    int b;
    private Context c;
    private Region d;
    private String e;
    private String f;
    private List<PkgInfo> g;
    private String h;

    public static class Builder {
        int a = 3000;
        int b = 5000;
        private Context c;
        private Region d;
        private String e;

        public Builder setContext(Context context) {
            this.c = context;
            return this;
        }

        public Builder setRegion(Region region) {
            this.d = region;
            return this;
        }

        public Builder setToken(String str) {
            this.e = str;
            return this;
        }

        public Builder setConnectionTimeout(int i) {
            this.a = i;
            return this;
        }

        public Builder setSocketTimeout(int i) {
            this.b = i;
            return this;
        }

        public CloudScanClient build() {
            return new CloudScanClient();
        }
    }

    static {
        Map hashMap = new HashMap();
        i = hashMap;
        hashMap.put(Region.INTL, "https://sla-intl.trustlook.com/v2/query");
        i.put(Region.CHN, "http://sla-cn.trustlook.com/v2/query");
    }

    private CloudScanClient(Builder builder) {
        this.c = builder.c;
        this.d = builder.d;
        this.e = (String) i.get(this.d);
        this.f = builder.e;
        this.h = DataUtils.getAndroidId(this.c);
        this.a = builder.a;
        this.b = builder.b;
        DataUtils.saveRegionValue(builder.c, builder.d);
        DataUtils.saveStringValue(builder.c, Constants.CLIENT_TOKEN, builder.e);
        DataUtils.saveIntValue(builder.c, Constants.CLIENT_CONNECTION_TIMEOUT, builder.a);
        DataUtils.saveIntValue(builder.c, Constants.CLIENT_SOCKET_TIMEOUT, builder.b);
    }

    public void setCacheLimit(int i) {
        DBManager.getInstance(this.c).getAppInfoDataSource().setAppInfoLimit((long) i);
    }

    public void clearAppInfoCache() {
        DBManager.getInstance(this.c).getAppInfoDataSource().clearAppInfoCache();
    }

    public ScanResult cacheCheck(List<PkgInfo> list) {
        ScanResult scanResult = new ScanResult();
        List arrayList = new ArrayList();
        if (this.f != null) {
            for (PkgInfo appInfoFromMD5 : list) {
                arrayList.add(DBManager.getInstance(this.c).getAppInfoDataSource().getAppInfoFromMD5(appInfoFromMD5));
            }
            scanResult.setList(arrayList);
            scanResult.setIsSuccess(true);
            scanResult.setError(0);
            Log.w(Constants.TAG, "Scan result from local");
            return scanResult;
        }
        scanResult.setIsSuccess(false);
        scanResult.setError(7);
        return scanResult;
    }

    public ScanResult cloudScan(List<PkgInfo> list) {
        ScanResult scanResult = new ScanResult();
        if (this.e == null) {
            scanResult.setIsSuccess(false);
            scanResult.setError(3);
            return scanResult;
        } else if (this.f != null) {
            if (list != null) {
                this.g = list;
                JSONArray jSONArray = new JSONArray();
                for (PkgInfo toJSON : list) {
                    jSONArray.put(toJSON.toJSON());
                }
                try {
                    Map hashMap = new HashMap();
                    hashMap.put("apikey", this.f);
                    hashMap.put("aid", this.h);
                    if (this.c != null) {
                        Log.e(Constants.TAG, "Locale = " + this.c.getResources().getConfiguration().locale);
                        Locale locale = this.c.getResources().getConfiguration().locale;
                        if (locale == null) {
                            hashMap.put("locale", Locale.US.toString());
                        } else {
                            hashMap.put("locale", locale.toString());
                        }
                    }
                    hashMap.put(PushResponse.DATA_FIELD, jSONArray.toString());
                    new StringBuilder("Post to ").append(hashMap.toString());
                    new StringBuilder("apikey =  ").append((String) hashMap.get("apikey"));
                    new StringBuilder("aid =  ").append((String) hashMap.get("aid"));
                    new StringBuilder("locale =  ").append((String) hashMap.get("locale"));
                    new StringBuilder("data = ").append((String) hashMap.get(PushResponse.DATA_FIELD));
                    List a = new b(this).a(this.e, b.a(hashMap, "UTF-8").toString().getBytes());
                    if (a != null) {
                        if (a.size() > 0) {
                            scanResult.setIsSuccess(true);
                            scanResult.setList(a);
                            DBManager.getInstance(this.c).getAppInfoDataSource().batchInsertAppInfoList(a);
                            new StringBuilder("AppInfo number :").append(DBManager.getInstance(this.c).getAppInfoDataSource().countAppInfo());
                        }
                    }
                    scanResult.setIsSuccess(false);
                    scanResult.setError(6);
                } catch (c e) {
                    Log.e(Constants.TAG, "========== Server ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(6);
                } catch (a e2) {
                    Log.e(Constants.TAG, "========== Token ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(7);
                } catch (IOException e3) {
                    Log.e(Constants.TAG, "========== NETWORK ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(4);
                    e3.printStackTrace();
                } catch (JSONException e4) {
                    Log.e(Constants.TAG, "========== JSON ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(5);
                } catch (d e5) {
                    Log.e(Constants.TAG, "========== Rate Exceed ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(9);
                } catch (Md5InvalidException e6) {
                    Log.e(Constants.TAG, "========== MD5 INVALID ERROR ========");
                    scanResult.setIsSuccess(false);
                    scanResult.setError(8);
                } catch (Exception e7) {
                    scanResult.setIsSuccess(false);
                    scanResult.setError(1);
                    e7.printStackTrace();
                }
            } else {
                scanResult.setIsSuccess(false);
                scanResult.setError(2);
            }
            return scanResult;
        } else {
            scanResult.setIsSuccess(false);
            scanResult.setError(7);
            return scanResult;
        }
    }

    public PkgInfo populatePkgInfo(String str, String str2) {
        PkgInfo pkgInfo = new PkgInfo(str);
        if (!(str == null || str2 == null)) {
            String mD5FromPkgInfo = DBManager.getInstance(this.c).getAppInfoDataSource().getMD5FromPkgInfo(str, str2);
            File file = new File(str2);
            if (mD5FromPkgInfo == null) {
                mD5FromPkgInfo = a(file, "MD5");
            }
            pkgInfo.setMd5(mD5FromPkgInfo);
            pkgInfo.setPkgSize(file.length());
            pkgInfo.setPkgPath(str2);
            try {
                pkgInfo.setPkgSource(this.c.getPackageManager().getInstallerPackageName(str));
            } catch (Exception e) {
            }
        }
        return pkgInfo;
    }

    private static String a(File file, String str) {
        try {
            MessageDigest instance = MessageDigest.getInstance(str);
            try {
                FileInputStream fileInputStream = new FileInputStream(file);
                byte[] bArr = new byte[8192];
                while (true) {
                    int read = fileInputStream.read(bArr);
                    if (read <= 0) {
                        break;
                    }
                    try {
                        instance.update(bArr, 0, read);
                    } catch (Throwable e) {
                        throw new RuntimeException("Unable to process file for MD5", e);
                    } catch (Throwable th) {
                        try {
                            fileInputStream.close();
                        } catch (IOException e2) {
                            Log.e(Constants.TAG, "Exception on closing MD5 input stream " + e2);
                        }
                    }
                }
                String bigInteger = new BigInteger(1, instance.digest()).toString(16);
                bigInteger = String.format("%32s", new Object[]{bigInteger}).replace(' ', '0').toUpperCase(Locale.US);
                try {
                    fileInputStream.close();
                } catch (IOException e22) {
                    Log.e(Constants.TAG, "Exception on closing MD5 input stream " + e22);
                }
                return bigInteger;
            } catch (Throwable e3) {
                Log.e(Constants.TAG, "Exception while getting FileInputStream", e3);
                return null;
            }
        } catch (Throwable e32) {
            Log.e(Constants.TAG, "Exception while getting Digest", e32);
            return null;
        }
    }

    final PkgInfo a(String str) {
        for (PkgInfo pkgInfo : this.g) {
            if (pkgInfo.getMd5().equalsIgnoreCase(str)) {
                return pkgInfo;
            }
        }
        return null;
    }
}
