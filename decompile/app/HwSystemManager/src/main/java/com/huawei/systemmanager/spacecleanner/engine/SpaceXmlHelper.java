package com.huawei.systemmanager.spacecleanner.engine;

import android.content.Context;
import com.google.common.collect.Lists;
import com.huawei.systemmanager.comm.Storage.PathEntry;
import com.huawei.systemmanager.comm.Storage.PathEntrySet;
import com.huawei.systemmanager.comm.xml.XmlParsers;
import com.huawei.systemmanager.comm.xml.base.SimpleXmlRow;
import java.io.File;
import java.util.Collections;
import java.util.List;

public class SpaceXmlHelper {
    private static final String ATTRIBUTE_DESCRIPTION = "description";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_PACKAGE_NAME = "packagename";
    private static final String ATTRIBUTE_PATH = "path";
    private static final String ATTRIBUTE_POSITION = "position";
    private static final String ATTRIBUTE_SUGGEST_CLEAN = "suggestclean";
    private static final String TAG = "SpaceXmlHelper";
    private static final String TOP_VIDEO_LIST = "space/video_top.xml";

    public static List<String> getIgnorePath(Context ctx, PathEntrySet entrySet) {
        Iterable list = null;
        try {
            list = XmlParsers.assetSimpleXmlRows(ctx, "space/trash_ignore.xml");
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (r5 == null) {
            return Collections.emptyList();
        }
        List<PathEntry> entries = entrySet.getPathEntry();
        List<String> ingorePathes = Lists.newArrayList();
        for (SimpleXmlRow row : r5) {
            String name = row.getAttrValue("path");
            int position = row.getAttrInteger("position");
            for (PathEntry entry : entries) {
                if (position == 1 || position == entry.mPosition) {
                    ingorePathes.add(entry.mPath + File.separator + name);
                }
            }
        }
        return ingorePathes;
    }

    public static List<String> getTopVideoList(Context ctx) {
        Iterable list = null;
        try {
            list = XmlParsers.assetSimpleXmlRows(ctx, TOP_VIDEO_LIST);
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (r1 == null) {
            return Collections.emptyList();
        }
        List<String> topVideoList = Lists.newArrayList();
        for (SimpleXmlRow row : r1) {
            topVideoList.add(row.getAttrValue("name"));
        }
        return topVideoList;
    }
}
