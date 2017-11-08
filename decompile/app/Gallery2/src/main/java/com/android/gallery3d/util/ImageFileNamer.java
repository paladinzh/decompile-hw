package com.android.gallery3d.util;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

public class ImageFileNamer {
    private static ImageFileNamer sInstance = new ImageFileNamer();
    private SimpleDateFormat mFormat = new SimpleDateFormat("'IMG'_yyyyMMdd_HHmmss");
    private long mLastDate;
    private int mSameSecondCount;

    private ImageFileNamer() {
    }

    public static String generateName() {
        return sInstance.generateInner(System.currentTimeMillis());
    }

    public String generateInner(long dateTaken) {
        Date date = new Date(dateTaken);
        this.mFormat.setTimeZone(TimeZone.getDefault());
        String result = this.mFormat.format(date);
        if (dateTaken / 1000 == this.mLastDate / 1000) {
            this.mSameSecondCount++;
            return result + "_" + this.mSameSecondCount;
        }
        this.mLastDate = dateTaken;
        this.mSameSecondCount = 0;
        return result;
    }
}
