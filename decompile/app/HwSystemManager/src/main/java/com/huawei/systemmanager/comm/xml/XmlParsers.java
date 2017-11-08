package com.huawei.systemmanager.comm.xml;

import android.content.Context;
import com.google.android.collect.Maps;
import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import com.huawei.systemmanager.comm.xml.filter.AttrValueValidMatch;
import com.huawei.systemmanager.comm.xml.filter.TagNameMatch;
import com.huawei.systemmanager.comm.xml.func.ExtractStringAttrValueFunction;
import com.huawei.systemmanager.util.HwLog;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Element;
import org.xmlpull.v1.XmlPullParser;

public class XmlParsers {
    private static final String TAG = XmlParsers.class.getSimpleName();

    public static Element assetXmlRootElement(Context context, String assetFile) throws XmlParserException {
        return xmlRootElement(ParserAssistUtils.assetInputStream(context, assetFile));
    }

    public static Element diskXmlRootElement(String diskFile) throws XmlParserException {
        return xmlRootElement(ParserAssistUtils.diskInputStream(diskFile));
    }

    public static Element rawResXmlRootElement(Context context, int rawResId) throws XmlParserException {
        return xmlRootElement(ParserAssistUtils.rawResInputStream(context, rawResId));
    }

    public static List<SimpleXmlRow> assetSimpleXmlRows(Context context, String assetFile) throws XmlParserException {
        List<SimpleXmlRow> result = SimpleXmlParser.parseSimpleAssetXml(context, assetFile);
        if (result != null) {
            return result;
        }
        throw new XmlParserException("assetSimpleXmlRows parse failed:" + assetFile);
    }

    public static List<SimpleXmlRow> resSimpleXmlRows(Context context, int xmlResId) throws XmlParserException {
        return simpleXmlRows(ParserAssistUtils.resXmlPullParser(context, xmlResId));
    }

    public static List<SimpleXmlRow> diskSimpleXmlRows(String diskFile) throws XmlParserException {
        List<SimpleXmlRow> result = SimpleXmlParser.parseSimpleXml(diskFile);
        if (result != null) {
            return result;
        }
        throw new XmlParserException("diskSimpleXmlRows parse failed:" + diskFile);
    }

    public static List<String> assetAttrValueList(Context context, String assetFile, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        try {
            Preconditions.checkArgument(context != null, "Context can't be null");
            return attrValueTransform(assetSimpleXmlRows(context, assetFile), predicate, transformFunc);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "assetAttrValueList catch IllegalArgumentException:" + ex.getMessage());
            throw new XmlParserException("assetAttrValueList parse failed: " + assetFile);
        }
    }

    public static List<String> resAttrValueList(Context context, int xmlResId, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        try {
            Preconditions.checkArgument(context != null, "Context can't be null");
            return attrValueTransform(resSimpleXmlRows(context, xmlResId), predicate, transformFunc);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "resAttrValueList catch IllegalArgumentException:" + ex.getMessage());
            throw new XmlParserException("resAttrValueList parse failed!");
        }
    }

    public static List<String> diskAttrValueList(String diskPath, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        try {
            return attrValueTransform(diskSimpleXmlRows(diskPath), predicate, transformFunc);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "diskAttrValueList catch IllegalArgumentException:" + ex.getMessage());
            throw new XmlParserException("diskAttrValueList parse failed: " + diskPath);
        }
    }

    public static <K, V> Map<K, V> resAttrsToMap(Context context, int xmlResId, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) {
        try {
            return rowsAttrValueTransform(resSimpleXmlRows(context, xmlResId), predicate, transFuncKey, transFuncValue);
        } catch (Exception ex) {
            HwLog.e(TAG, "resAttrsToMap catch Exception:" + ex.getMessage());
            throw new XmlParserException("resAttrsToMap parse failed!");
        }
    }

    public static <K, V> Map<K, V> assetAttrsToMap(Context context, String assetFile, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) {
        try {
            return rowsAttrValueTransform(assetSimpleXmlRows(context, assetFile), predicate, transFuncKey, transFuncValue);
        } catch (Exception ex) {
            HwLog.e(TAG, "assetAttrsToMap catch Exception:" + ex.getMessage());
            throw new XmlParserException("assetAttrsToMap parse failed!");
        }
    }

    public static <K, V> Map<K, V> diskAttrsToMap(String diskPath, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) {
        try {
            return rowsAttrValueTransform(diskSimpleXmlRows(diskPath), predicate, transFuncKey, transFuncValue);
        } catch (Exception ex) {
            HwLog.e(TAG, "diskAttrsToMap catch Exception:" + ex.getMessage());
            throw new XmlParserException("diskAttrsToMap parse failed: " + diskPath);
        }
    }

    public static List<String> xmlAttrValueList(Context context, String diskPath, int xmlResId, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            return diskAttrValueList(diskPath, predicate, transformFunc);
        }
        return resAttrValueList(context, xmlResId, predicate, transformFunc);
    }

    public static List<String> xmlAttrValueListAfterMerged(Context context, String diskPath, int xmlResId, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        List<String> result = new ArrayList();
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            result.addAll(diskAttrValueList(diskPath, predicate, transformFunc));
        }
        List<String> resourceList = resAttrValueList(context, xmlResId, predicate, transformFunc);
        result.removeAll(resourceList);
        result.addAll(resourceList);
        return result;
    }

    public static List<String> xmlAttrValueList(Context context, String diskPath, String assetFile, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            return diskAttrValueList(diskPath, predicate, transformFunc);
        }
        return assetAttrValueList(context, assetFile, predicate, transformFunc);
    }

    public static List<String> xmlAttrValueListAfterMerged(Context context, String diskPath, String assetFile, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) throws XmlParserException {
        List<String> result = new ArrayList();
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            result = diskAttrValueList(diskPath, predicate, transformFunc);
        }
        List<String> resourceList = assetAttrValueList(context, assetFile, predicate, transformFunc);
        result.removeAll(resourceList);
        result.addAll(resourceList);
        return result;
    }

    public static <K, V> Map<K, V> xmlAttrsToMap(Context context, String diskPath, String assetFile, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) throws XmlParserException {
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            return diskAttrsToMap(diskPath, predicate, transFuncKey, transFuncValue);
        }
        return assetAttrsToMap(context, assetFile, predicate, transFuncKey, transFuncValue);
    }

    public static <K, V> Map<K, V> xmlAttrsToMap(Context context, String diskPath, int xmlResId, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) throws XmlParserException {
        if (ParserAssistUtils.diskFileExist(diskPath)) {
            return diskAttrsToMap(diskPath, predicate, transFuncKey, transFuncValue);
        }
        return resAttrsToMap(context, xmlResId, predicate, transFuncKey, transFuncValue);
    }

    public static Function<SimpleXmlRow, String> getRowToAttrValueFunc(String attrName) {
        return new ExtractStringAttrValueFunction(attrName);
    }

    public static Predicate<SimpleXmlRow> getTagAttrMatchPredicate(String tag, String attrName) {
        return Predicates.and(new TagNameMatch(tag), new AttrValueValidMatch(attrName));
    }

    public static Predicate<SimpleXmlRow> getTagAttrMatchPredicate2(String tag, String attrName1, String attrName2) {
        return Predicates.and(getTagAttrMatchPredicate(tag, attrName1), new AttrValueValidMatch(attrName2));
    }

    public static Predicate<SimpleXmlRow> getTagAttrMatchPredicate3(String tag, String attrName1, String attrName2, String attrName3) {
        return Predicates.and(getTagAttrMatchPredicate2(tag, attrName1, attrName2), new AttrValueValidMatch(attrName3));
    }

    public static Predicate<SimpleXmlRow> joinPredicates(Predicate<SimpleXmlRow> first, Predicate<SimpleXmlRow> second) {
        return Predicates.and(first, second);
    }

    private static Element xmlRootElement(InputStream is) throws XmlParserException {
        Element element;
        try {
            Preconditions.checkArgument(is != null, "InputStream parameter can't be null");
            element = (Element) Preconditions.checkNotNull(new DOMXmlParser(is).rootElement(), "result element can't be null");
            return element;
        } catch (IllegalArgumentException ex) {
            element = TAG;
            HwLog.e(element, "xmlRootElement IllegalArgumentException:" + ex.getMessage());
            throw new XmlParserException("xmlRootElement parse failed");
        } catch (NullPointerException ex2) {
            element = TAG;
            HwLog.e(element, "xmlRootElement NullPointerException:" + ex2.getMessage());
            throw new XmlParserException("xmlRootElement parse failed");
        } finally {
            ParserAssistUtils.close(is);
        }
    }

    private static List<SimpleXmlRow> simpleXmlRows(XmlPullParser xpp) throws XmlParserException {
        try {
            Preconditions.checkArgument(xpp != null, "XmlPullParser parameter can't be null");
            return (List) Preconditions.checkNotNull(SimpleXmlParser.parseSimpleXml(xpp), "result rows can't be null!");
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "simpleXmlRows IllegalArgumentException:" + ex.getMessage());
            throw new XmlParserException("simpleXmlRows parse failed");
        } catch (NullPointerException ex2) {
            HwLog.e(TAG, "simpleXmlRows NullPointerException:" + ex2.getMessage());
            throw new XmlParserException("simpleXmlRows parse failed");
        }
    }

    private static List<String> attrValueTransform(List<SimpleXmlRow> rows, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, String> transformFunc) {
        return Lists.newArrayList(Collections2.transform(Collections2.filter(rows, predicate), transformFunc));
    }

    private static <K, V> Map<K, V> rowsAttrValueTransform(List<SimpleXmlRow> rows, Predicate<SimpleXmlRow> predicate, Function<SimpleXmlRow, K> transFuncKey, Function<SimpleXmlRow, V> transFuncValue) {
        Collection<SimpleXmlRow> filterdRow = Collections2.filter(rows, predicate);
        Map<K, V> outMap = Maps.newHashMap();
        for (SimpleXmlRow row : filterdRow) {
            outMap.put(transFuncKey.apply(row), transFuncValue.apply(row));
        }
        return outMap;
    }
}
