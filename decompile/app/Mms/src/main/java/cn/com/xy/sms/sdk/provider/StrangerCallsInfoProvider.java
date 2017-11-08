package cn.com.xy.sms.sdk.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import cn.com.xy.sms.sdk.constant.Constant;
import cn.com.xy.sms.sdk.db.entity.IccidInfoManager;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.number.f;
import cn.com.xy.sms.sdk.number.l;
import cn.com.xy.sms.sdk.util.StringUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public class StrangerCallsInfoProvider extends ContentProvider {
    private static final UriMatcher a;
    private static final String b = "cn.com.xy.sms.sdk.provider.strangercall";
    private static final String c = "numberInfo";
    private static final String d = "imgFile";
    private static final String e = "netNumberInfo";
    private static final String f = "netImgFile";
    private static final String g = "hotNumbers";
    private static final int h = 1;
    private static final int i = 2;
    private static final int j = 3;

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        a = uriMatcher;
        uriMatcher.addURI(b, c, 1);
        a.addURI(b, e, 2);
        a.addURI(b, g, 3);
    }

    private Cursor a(String str, Uri uri, JSONObject jSONObject) {
        boolean z = false;
        if (jSONObject == null) {
            return null;
        }
        Cursor matrixCursor = new MatrixCursor(new String[]{IccidInfoManager.NUM, "result"});
        Object[] objArr = new Object[2];
        objArr[0] = str;
        String str2 = NumberInfo.LOCAL_LOGO_KEY;
        String optString = jSONObject.optString(NumberInfo.LOGO_KEY);
        if (!StringUtils.isNull(optString) && new File(new StringBuilder(String.valueOf(Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR))).append(optString).toString()).exists()) {
            z = true;
        }
        jSONObject.put(str2, z);
        objArr[1] = jSONObject.toString();
        matrixCursor.addRow(objArr);
        matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return matrixCursor;
    }

    private static boolean a(String str) {
        return !StringUtils.isNull(str) && new File(new StringBuilder(String.valueOf(Constant.getPath(Constant.DUOQU_PUBLIC_LOGO_DIR))).append(str).toString()).exists();
    }

    public int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public String getType(Uri uri) {
        return null;
    }

    public Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public boolean onCreate() {
        return true;
    }

    public ParcelFileDescriptor openFile(Uri uri, String str) {
        List pathSegments = uri.getPathSegments();
        if (pathSegments != null && pathSegments.size() >= 2) {
            File a;
            String str2 = (String) pathSegments.get(0);
            String str3 = (String) pathSegments.get(1);
            if (d.equals(str2)) {
                getContext();
                a = f.a(str3);
            } else if (f.equals(str2)) {
                Map hashMap = new HashMap();
                hashMap.put("DownloadInThreadPool", Boolean.FALSE.toString());
                a = f.a(getContext(), str3, hashMap);
            } else {
                a = null;
            }
            if (a != null && a.exists()) {
                return ParcelFileDescriptor.open(a, 268435456);
            }
            throw new FileNotFoundException(uri.getPath());
        }
        throw new FileNotFoundException(uri.getPath());
    }

    public Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        try {
            String str3;
            switch (a.match(uri)) {
                case 1:
                    if (strArr2 != null && strArr2.length > 0) {
                        str3 = strArr2[0];
                        return a(str3, uri, l.a(str3));
                    }
                case 2:
                    if (strArr2 != null && strArr2.length > 0) {
                        str3 = strArr2[0];
                        return a(str3, uri, l.a(str3, null));
                    }
                case 3:
                    return l.a();
            }
        } catch (Throwable th) {
        }
        return null;
    }

    public int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }
}
