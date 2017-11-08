package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public abstract class MapXmlParseHelper {
    private static final String LOG_TAG = "MapXmlParseHelper";
    private Context mContext = null;

    protected abstract String getAssertFileName();

    protected abstract String getColumnKeyName();

    protected abstract String getColumnValueName();

    protected abstract Uri getInsertTableUri();

    public MapXmlParseHelper(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void putXmlInfoIntoDB() {
        Map<String, String> pkgMap = initMapXml(this.mContext, getAssertFileName(), getTagName(), getAttrKeyName(), getAttrValueName());
        if (pkgMap != null && !pkgMap.isEmpty()) {
            dealWithAppMap(pkgMap);
        }
    }

    protected String getTagName() {
        return "package";
    }

    protected String getAttrKeyName() {
        return "name";
    }

    protected String getAttrValueName() {
        return "defaultValue";
    }

    protected void dealWithAppMap(Map<String, String> pkgMap) {
        String columnKeyName = getColumnKeyName();
        String columnValueName = getColumnValueName();
        List<ContentValues> contentValuesList = new ArrayList();
        ContentValues contentValues = new ContentValues();
        try {
            for (Entry<String, String> entry : pkgMap.entrySet()) {
                contentValues.clear();
                contentValues.put(columnKeyName, (String) entry.getKey());
                contentValues.put(columnValueName, Integer.valueOf(Integer.parseInt((String) entry.getValue())));
                contentValuesList.add(new ContentValues(contentValues));
            }
        } catch (NumberFormatException ex) {
            HwLog.e(LOG_TAG, "NumberFormatException is: " + ex.getMessage());
        } catch (Exception e) {
            HwLog.e(LOG_TAG, "Exception is: " + e.getMessage());
        }
        if (!contentValuesList.isEmpty()) {
            this.mContext.getContentResolver().bulkInsert(getInsertTableUri(), (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
        }
    }

    protected Map<String, String> initMapXml(Context context, String assertFileName, String tag, String attrKey, String attrValue) {
        Map<String, String> pkgMapInfo = null;
        try {
            pkgMapInfo = XmlParsers.xmlAttrsToMap(context, null, assertFileName, XmlParsers.getTagAttrMatchPredicate2(tag, attrKey, attrValue), XmlParsers.getRowToAttrValueFunc(attrKey), XmlParsers.getRowToAttrValueFunc(attrValue));
        } catch (XmlParserException XmlEx) {
            HwLog.e(LOG_TAG, XmlEx.getMessage());
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, ex.getMessage());
        }
        if (pkgMapInfo != null) {
            HwLog.i(LOG_TAG, "pkgMapInfo is = " + pkgMapInfo.toString());
        }
        return pkgMapInfo;
    }
}
