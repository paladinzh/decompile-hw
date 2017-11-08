package com.huawei.systemmanager.securitythreats.ui;

import android.content.Context;
import android.net.Uri;
import android.util.Xml;
import com.huawei.systemmanager.comm.misc.Closeables;
import com.huawei.systemmanager.securitythreats.comm.SecurityThreatsConst;
import com.huawei.systemmanager.securitythreats.ui.VirusPkg.Version;
import com.huawei.systemmanager.util.HwLog;
import java.io.Closeable;
import java.util.HashMap;
import java.util.Map;
import org.xmlpull.v1.XmlPullParser;

public class VirusPkgParser {
    private static final String CODE = "code";
    private static final String DESCRIPTION = "description";
    private static final String NAME = "name";
    private static final String PACKAGE = "package";
    private static final String SHA256 = "sha256";
    private static final String TAG = "VirusPkgParser";
    private static final String VERSION = "version";
    private static final String VIRUS = "virus";
    private final Context mContext;

    public VirusPkgParser(Context context) {
        this.mContext = context;
    }

    public Map<String, VirusPkg> parse() {
        Closeable closeable = null;
        Map<String, VirusPkg> hashMap;
        try {
            closeable = this.mContext.getContentResolver().openInputStream(Uri.parse(SecurityThreatsConst.PUSH_FILE_URI));
            if (closeable == null) {
                HwLog.e(TAG, "parse InputStream is null");
                hashMap = new HashMap();
                return hashMap;
            }
            XmlPullParser xml = Xml.newPullParser();
            xml.setInput(closeable, null);
            hashMap = parseInner(xml);
            Closeables.close(closeable);
            return hashMap;
        } catch (Exception e) {
            hashMap = TAG;
            HwLog.e(hashMap, "parse RuntimeException", e);
            return new HashMap();
        } finally {
            Closeables.close(closeable);
        }
    }

    private Map<String, VirusPkg> parseInner(XmlPullParser xml) throws Exception {
        Map<String, VirusPkg> map = new HashMap();
        while (true) {
            int xmlType = xml.next();
            if (xmlType == 1) {
                return map;
            }
            if (xmlType == 2 && "package".equals(xml.getName())) {
                VirusPkg onePkg = parseOnePkg(xml);
                map.put(onePkg.getPackageName(), onePkg);
            }
        }
    }

    private VirusPkg parseOnePkg(XmlPullParser xml) throws Exception {
        VirusPkg pkg = new VirusPkg(xml.getAttributeValue(null, "name"), xml.getAttributeValue(null, "virus"), xml.getAttributeValue(null, DESCRIPTION));
        HwLog.d(TAG, "parseOnePkg pkg=" + pkg);
        while (true) {
            boolean equals;
            int xmlType = xml.next();
            if (xmlType == 3) {
                equals = "package".equals(xml.getName());
            } else {
                equals = false;
            }
            if (equals) {
                return pkg;
            }
            if (xmlType == 2 && "version".equals(xml.getName())) {
                Version version = new Version();
                version.setCode(xml.getAttributeValue(null, CODE));
                version.setSha256(xml.getAttributeValue(null, SHA256));
                HwLog.d(TAG, "parseOnePkg addVerison=" + version.getCode() + " " + version.getSha256());
                pkg.addVerison(version);
            }
        }
    }
}
