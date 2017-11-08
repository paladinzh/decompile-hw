package com.huawei.systemmanager.comm.misc;

import android.content.Context;
import android.webkit.MimeTypeMap;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import java.util.Map;

public class MimeTypeHelper {
    private static final String MIME_TYPE_ATTIBUTE_TABLE = "mimetypes.xml";
    private static MimeTypeHelper mInstance;
    private Map<String, String> mMimeTypeMap;

    private static class MiscPredicate<SimpleXmlRow> implements Predicate<SimpleXmlRow> {
        private MiscPredicate() {
        }

        public boolean apply(SimpleXmlRow simpleXmlRow) {
            return true;
        }
    }

    public static synchronized MimeTypeHelper getInstance() {
        MimeTypeHelper mimeTypeHelper;
        synchronized (MimeTypeHelper.class) {
            if (mInstance == null) {
                mInstance = new MimeTypeHelper(GlobalContext.getContext());
            }
            mimeTypeHelper = mInstance;
        }
        return mimeTypeHelper;
    }

    private MimeTypeHelper(Context context) {
        initMimeTypeMap(context);
    }

    private void initMimeTypeMap(Context context) {
        this.mMimeTypeMap = XmlParsers.xmlAttrsToMap(context, null, MIME_TYPE_ATTIBUTE_TABLE, new MiscPredicate(), XmlParsers.getRowToAttrValueFunc("extension"), XmlParsers.getRowToAttrValueFunc("mimetype"));
    }

    public String getMimeType(String filePath) {
        String extension = getExtendsion(filePath);
        String mimetype = readMimeTypeFromApi(extension);
        if (mimetype == null) {
            return getMimeTypeFromMap(extension);
        }
        return mimetype;
    }

    private String readMimeTypeFromApi(String extension) {
        return MimeTypeMap.getSingleton().getMimeTypeFromExtension(extension);
    }

    private String getMimeTypeFromMap(String extension) {
        if (extension == null) {
            return null;
        }
        return (String) this.mMimeTypeMap.get(extension);
    }

    private String getExtendsion(String filePath) {
        return filePath.substring(filePath.lastIndexOf(".") + 1);
    }
}
