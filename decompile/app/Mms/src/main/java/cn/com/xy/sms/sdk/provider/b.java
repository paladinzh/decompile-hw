package cn.com.xy.sms.sdk.provider;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import cn.com.xy.sms.sdk.db.entity.NumberInfo;
import cn.com.xy.sms.sdk.util.StringUtils;
import cn.com.xy.sms.util.ParseManager;
import cn.com.xy.sms.util.PublicInfoParseManager;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.json.JSONObject;

/* compiled from: Unknown */
public final class b extends ContentProvider {
    private static final UriMatcher a;
    private static final String b = "cn.com.xy.sms.sdk.provider.custominfo";
    private static final String c = "publicInfo";
    private static final String d = "publicInfoLogo";
    private static final int e = 1;
    private static final String[] f = new String[]{"id", "name", "classifyName", "weiboName", "weiboUrl", "weixin", NumberInfo.LOGO_KEY, "logoc", "website"};

    static {
        UriMatcher uriMatcher = new UriMatcher(-1);
        a = uriMatcher;
        uriMatcher.addURI(b, c, 1);
    }

    private Cursor a(Uri uri, String[] strArr, String str) {
        Cursor matrixCursor = new MatrixCursor(strArr);
        int length = strArr.length;
        Object[] objArr = new Object[length];
        try {
            JSONObject jSONObject = new JSONObject(str);
            for (int i = 0; i < length; i++) {
                objArr[i] = jSONObject.opt(strArr[i]);
            }
        } catch (Throwable th) {
        }
        matrixCursor.addRow(objArr);
        matrixCursor.setNotificationUri(getContext().getContentResolver(), uri);
        return matrixCursor;
    }

    private String a(String[] strArr) {
        String[] strArr2 = new String[1];
        if (strArr != null && strArr.length >= 2) {
            String str = strArr[0];
            String str2 = strArr[1];
            if (!StringUtils.isNull(str)) {
                Map hashMap = new HashMap();
                hashMap.put("SYNC_QUERY", "");
                strArr2[0] = ParseManager.queryPublicInfo(getContext(), str, 1, str2, hashMap, new c(this, strArr2));
            }
        }
        return strArr2[0];
    }

    public final int delete(Uri uri, String str, String[] strArr) {
        return 0;
    }

    public final String getType(Uri uri) {
        return null;
    }

    public final Uri insert(Uri uri, ContentValues contentValues) {
        return null;
    }

    public final boolean onCreate() {
        return true;
    }

    public final ParcelFileDescriptor openFile(Uri uri, String str) {
        List pathSegments = uri.getPathSegments();
        if (pathSegments != null && pathSegments.size() >= 2) {
            File findLogoFile = !d.equals((String) pathSegments.get(0)) ? null : PublicInfoParseManager.findLogoFile((String) pathSegments.get(1));
            if (findLogoFile != null && findLogoFile.exists()) {
                return ParcelFileDescriptor.open(findLogoFile, 268435456);
            }
            throw new FileNotFoundException(uri.getPath());
        }
        throw new FileNotFoundException(uri.getPath());
    }

    public final Cursor query(Uri uri, String[] strArr, String str, String[] strArr2, String str2) {
        String str3;
        String[] strArr3;
        switch (a.match(uri)) {
            case 1:
                String[] strArr4 = f;
                String[] strArr5 = new String[1];
                if (strArr2 != null && strArr2.length >= 2) {
                    str3 = strArr2[0];
                    String str4 = strArr2[1];
                    if (!StringUtils.isNull(str3)) {
                        Map hashMap = new HashMap();
                        hashMap.put("SYNC_QUERY", "");
                        strArr5[0] = ParseManager.queryPublicInfo(getContext(), str3, 1, str4, hashMap, new c(this, strArr5));
                    }
                }
                str3 = strArr5[0];
                strArr3 = strArr4;
                break;
            default:
                strArr3 = null;
                str3 = null;
                break;
        }
        return (StringUtils.isNull(str3) || strArr3 == null) ? null : a(uri, strArr3, str3);
    }

    public final int update(Uri uri, ContentValues contentValues, String str, String[] strArr) {
        return 0;
    }
}
