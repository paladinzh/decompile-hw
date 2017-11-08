package com.huawei.systemmanager.rainbow.client.parsexml;

import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import com.huawei.systemmanager.comm.misc.ProviderUtils;
import com.huawei.systemmanager.comm.xml.XmlParserException;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.rainbow.client.helper.LocalSharedPrefrenceHelper;
import com.huawei.systemmanager.util.HwLog;
import java.util.ArrayList;
import java.util.List;

public abstract class SimpleXmlParseHelper {
    private static final String LOG_TAG = "SimpleXmlParseHelper";
    private Context mContext = null;

    protected abstract String getAssertFileName();

    protected abstract String getFeatureVersionName();

    protected abstract Uri getInsertTableUri();

    public SimpleXmlParseHelper(Context context) {
        this.mContext = context.getApplicationContext();
    }

    public void putXmlInfoIntoDB(boolean updateFlag) {
        if (!updateFlag || needUpdateDBFromXml()) {
            List<String> pkgList = initListXml(this.mContext, getAssertFileName(), getTagName(), getAttrName());
            if (pkgList != null && !pkgList.isEmpty()) {
                String columnName = getColumnName();
                List<ContentValues> contentValuesList = new ArrayList();
                ContentValues contentValues = new ContentValues();
                for (String packageName : pkgList) {
                    contentValues.clear();
                    contentValues.put(columnName, packageName);
                    contentValuesList.add(new ContentValues(contentValues));
                }
                ProviderUtils.deleteAll(this.mContext, getInsertTableUri());
                this.mContext.getContentResolver().bulkInsert(getInsertTableUri(), (ContentValues[]) contentValuesList.toArray(new ContentValues[contentValuesList.size()]));
                return;
            }
            return;
        }
        HwLog.e(LOG_TAG, "No need updateDB from xml!");
    }

    protected String getTagName() {
        return "package";
    }

    protected String getAttrName() {
        return "name";
    }

    protected String getColumnName() {
        return "packageName";
    }

    private List<String> initListXml(Context context, String assertFileName, String tag, String attr) {
        List<String> pkgList = null;
        try {
            pkgList = XmlParsers.xmlAttrValueList(context, null, assertFileName, XmlParsers.getTagAttrMatchPredicate(tag, attr), XmlParsers.getRowToAttrValueFunc(attr));
        } catch (XmlParserException XmlEx) {
            HwLog.e(LOG_TAG, XmlEx.getMessage());
        } catch (Exception ex) {
            HwLog.e(LOG_TAG, ex.getMessage());
        }
        if (pkgList != null) {
            HwLog.i(LOG_TAG, "pkgList is = " + pkgList.toString());
        }
        return pkgList;
    }

    private boolean needUpdateDBFromXml() {
        return new LocalSharedPrefrenceHelper(this.mContext).getBoolean(XmlParseConst.getUpdateFlagForSpfKey(getFeatureVersionName()), false);
    }
}
