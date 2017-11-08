package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.net.Uri;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.data.LocalMediaAlbum.LocalGroupData;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;
import java.util.ArrayList;

public class TimeGroupAlbumHelper {
    private ITimeLatLng mTimeLatLng;

    public TimeGroupAlbumHelper(ITimeLatLng timeLatLng) {
        this.mTimeLatLng = timeLatLng;
        GalleryLog.d("LocalGroupAlbumHelper", "helper created for " + this.mTimeLatLng);
    }

    public String[] buildGroupDataProjection(TimeBucketPageViewMode mode, String columnName) {
        StringBuffer sb = new StringBuffer();
        sb.append(mode.getNormalizedDateFormat());
        sb.append(",");
        sb.append(" MIN(strftime('%%Y%%m%%d', %s / 1000, 'unixepoch', 'localtime')), MAX(strftime('%%Y%%m%%d', %s / 1000, 'unixepoch', 'localtime')),MIN(%s), MAX(%s)");
        r1 = new String[3];
        r1[0] = String.format(sb.toString(), new Object[]{columnName, columnName, columnName, columnName, columnName});
        r1[1] = "COUNT(_id)";
        r1[2] = "SUM((CASE WHEN media_type=3 THEN 1 ELSE 0 END))";
        return r1;
    }

    protected int getMediaItemCount(ArrayList<LocalGroupData> groupDataList) {
        int sum = 0;
        for (LocalGroupData data : groupDataList) {
            sum += data.count;
        }
        return sum;
    }

    protected int getVideoItemCount(ArrayList<LocalGroupData> groupDataList) {
        int sum = 0;
        for (LocalGroupData data : groupDataList) {
            sum += data.videoCount;
        }
        return sum;
    }

    public ArrayList<LocalGroupData> genGroupData(ContentResolver resolver, Uri uri, String[] projection, String whereClause, String[] whereArgs, String orderBy) {
        long startTime = System.currentTimeMillis();
        ArrayList<LocalGroupData> groupDatas = new ArrayList();
        Closeable closeable = null;
        try {
            closeable = resolver.query(uri, projection, whereClause, whereArgs, orderBy);
            if (closeable == null) {
                GalleryLog.w("LocalGroupAlbumHelper", "query fail");
                return groupDatas;
            }
            while (closeable.moveToNext()) {
                LocalGroupData data = new LocalGroupData();
                data.startDate = closeable.getString(1);
                data.endDate = closeable.getString(2);
                data.defaultTitle = data.startDate + "-" + data.endDate;
                data.startDatetaken = closeable.getLong(3);
                data.endDatetaken = closeable.getLong(4);
                data.dateTaken = data.endDatetaken;
                data.count = closeable.getInt(5);
                data.videoCount = closeable.getInt(6);
                groupDatas.add(data);
            }
            Utils.closeSilently(closeable);
            GalleryLog.d("LocalGroupAlbumHelper", "getGroupData time(ms):" + (System.currentTimeMillis() - startTime));
            return groupDatas;
        } catch (SecurityException e) {
            GalleryLog.w("LocalGroupAlbumHelper", "get group data fail, because of permission." + e.getMessage());
        } catch (Exception e2) {
            GalleryLog.w("LocalGroupAlbumHelper", "get group data fail." + e2.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }
}
