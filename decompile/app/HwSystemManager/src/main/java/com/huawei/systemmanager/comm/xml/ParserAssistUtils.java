package com.huawei.systemmanager.comm.xml;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import com.google.common.base.Preconditions;
import com.huawei.systemmanager.comm.wrapper.DiskFile;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import org.xmlpull.v1.XmlPullParser;

class ParserAssistUtils {
    private static String TAG = ParserAssistUtils.class.getSimpleName();

    ParserAssistUtils() {
    }

    public static void close(InputStream is) {
        if (is == null) {
            HwLog.d(TAG, "close inputsteam is null!");
            return;
        }
        try {
            is.close();
        } catch (IOException ex) {
            HwLog.e(TAG, "close catch IOException");
            ex.printStackTrace();
        }
    }

    public static boolean diskFileExist(String diskPath) {
        return DiskFile.fileExist(diskPath);
    }

    public static InputStream diskInputStream(String diskFile) {
        try {
            File file = new File(diskFile);
            if (file.exists()) {
                return new FileInputStream(file);
            }
        } catch (FileNotFoundException ex) {
            HwLog.e(TAG, "diskInputStream file not found:" + diskFile);
            ex.printStackTrace();
        }
        return null;
    }

    public static InputStream assetInputStream(Context context, String assetFile) {
        try {
            Preconditions.checkArgument(context != null, "context can't be null");
            return context.getAssets().open(assetFile);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "assetInputStream catch IllegalArgumentException:" + assetFile);
            ex.printStackTrace();
            return null;
        } catch (IOException ex2) {
            HwLog.e(TAG, "assetInputStream catch IO exception:" + assetFile);
            ex2.printStackTrace();
            return null;
        }
    }

    public static InputStream rawResInputStream(Context context, int rawResId) {
        try {
            Preconditions.checkArgument(context != null, "context can't be null");
            return context.getResources().openRawResource(rawResId);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "rawResInputStream catch IllegalArgumentException");
            ex.printStackTrace();
            return null;
        } catch (NotFoundException ex2) {
            HwLog.e(TAG, "rawResInputStream catch NotFoundException");
            ex2.printStackTrace();
            return null;
        }
    }

    public static XmlPullParser resXmlPullParser(Context context, int xmlResId) {
        try {
            Preconditions.checkArgument(context != null, "context can't be null");
            return context.getResources().getXml(xmlResId);
        } catch (IllegalArgumentException ex) {
            HwLog.e(TAG, "resXmlPullParser catch IllegalArgumentException");
            ex.printStackTrace();
            return null;
        } catch (NotFoundException ex2) {
            HwLog.e(TAG, "resXmlPullParser catch NotFoundException");
            ex2.printStackTrace();
            return null;
        }
    }
}
