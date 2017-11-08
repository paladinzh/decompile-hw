package com.android.gallery3d.data;

import android.content.ContentResolver;
import android.graphics.RectF;
import android.net.Uri;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.List;

public interface ITimeLatLng {

    public static class LatitudeLongitude {
        public final double latitude;
        public final double longitude;

        public LatitudeLongitude(double lat, double lng) {
            this.latitude = lat;
            this.longitude = lng;
        }
    }

    public static class TimeLatLng {
        private static final String[] LATITUDE_LONGITUDE_PROJECTION = new String[]{"distinct latitude", "longitude"};
        private static final String[] RECT_PROJECTION = new String[]{"MAX(latitude)", "MIN(latitude)", "MAX(longitude)", "MIN(longitude)"};
        private String mMediaTableName;
        private TimeBucketPageViewMode mMode = TimeBucketPageViewMode.DAY;
        protected String mQueryClauseGroup;
        private String mTimeColumName;
        private ITimeLatLng mTimeLatLng;
        protected String mWhereClauseDeleteSet;
        protected String mWhereClauseSet;
        protected String mWhereClauseSetLatitudeLongitude;
        protected String mWhereClauseSetRect;
        private String whereBetweenTime = "";

        TimeLatLng(ITimeLatLng timeLatLng) {
            this.mTimeLatLng = timeLatLng;
            this.mMediaTableName = this.mTimeLatLng.getMediaTableName();
            this.mTimeColumName = this.mTimeLatLng.getTimeColumnName();
            this.whereBetweenTime = this.mTimeColumName + " >= ? AND " + this.mTimeColumName + " <= ?";
        }

        void initWhereClause(String bucketSet, String excludeBurstNotCoverInSet) {
            this.mWhereClauseSet = this.mTimeColumName + " > ? AND " + bucketSet + " AND " + excludeBurstNotCoverInSet;
            this.mWhereClauseDeleteSet = this.mTimeColumName + " > ? AND " + bucketSet;
            String latingInSetInDatataken = this.whereBetweenTime + " AND " + "latitude IS NOT NULL AND longitude IS NOT NULL AND " + bucketSet;
            this.mWhereClauseSetLatitudeLongitude = latingInSetInDatataken + " AND " + "(latitude IN (select MAX(latitude) FROM " + this.mMediaTableName + " WHERE " + latingInSetInDatataken + ") OR " + "latitude IN(select MIN(latitude) FROM " + this.mMediaTableName + " WHERE " + latingInSetInDatataken + ") OR " + "longitude IN (select MAX(longitude) FROM " + this.mMediaTableName + " WHERE " + latingInSetInDatataken + ") OR " + "longitude IN (select MIN(longitude) FROM " + this.mMediaTableName + " WHERE " + latingInSetInDatataken + ") OR " + this.mTimeColumName + " IN (select MAX(" + this.mTimeColumName + ") FROM " + this.mMediaTableName + " WHERE " + latingInSetInDatataken + "))";
            this.mWhereClauseSetRect = this.whereBetweenTime + " AND " + bucketSet;
            this.mQueryClauseGroup = this.mWhereClauseSet + " AND " + this.whereBetweenTime;
        }

        protected String[] getQueryClauseArgs(long startTakenTime) {
            ArrayList<String> args = new ArrayList();
            args.add(Long.toString(startTakenTime));
            return (String[]) args.toArray(new String[args.size()]);
        }

        protected String[] getQueryClauseGroupArgs(long startTakenTime, long groupStartTime, long groupEndTime) {
            ArrayList<String> args = new ArrayList();
            args.add(Long.toString(startTakenTime));
            args.add(Long.toString(groupStartTime));
            args.add(Long.toString(groupEndTime));
            return (String[]) args.toArray(new String[args.size()]);
        }

        protected String[] getQueryClauseLatLngArgs(long startTakenTime, long endTakenTime) {
            ArrayList<String> args = new ArrayList();
            for (int i = 0; i < 6; i++) {
                args.add(Long.toString(startTakenTime));
                args.add(Long.toString(endTakenTime));
            }
            return (String[]) args.toArray(new String[args.size()]);
        }

        protected String[] getQueryClauseRectArgs(long startTakenTime, long endTakenTime) {
            ArrayList<String> args = new ArrayList();
            args.add(Long.toString(startTakenTime));
            args.add(Long.toString(endTakenTime));
            return (String[]) args.toArray(new String[args.size()]);
        }

        protected String[] getDeleteClauseArgs(long startTakenTime) {
            ArrayList<String> args = new ArrayList();
            args.add(Long.toString(startTakenTime));
            return (String[]) args.toArray(new String[args.size()]);
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        RectF getRectByDatetaken(ContentResolver resolver, Uri uri, long startDate, long endDate) {
            Closeable closeable = null;
            try {
                closeable = resolver.query(uri, RECT_PROJECTION, " (latitude != 0.0 OR longitude != 0.0)  AND " + this.mWhereClauseSetRect, getQueryClauseRectArgs(startDate, endDate), null);
                if (closeable == null || closeable.getCount() == 0) {
                    Utils.closeSilently(closeable);
                    return null;
                } else if (closeable.moveToFirst()) {
                    double west = closeable.getDouble(3);
                    double east = closeable.getDouble(2);
                    double south = closeable.getDouble(1);
                    double north = closeable.getDouble(0);
                    if ((east == 0.0d && west == 0.0d) || (south == 0.0d && north == 0.0d)) {
                        Utils.closeSilently(closeable);
                        return null;
                    }
                    RectF rectF = new RectF((float) west, (float) south, (float) east, (float) north);
                    Utils.closeSilently(closeable);
                    return rectF;
                } else {
                    Utils.closeSilently(closeable);
                    return null;
                }
            } catch (SecurityException e) {
                GalleryLog.w("SqlHelper", "No permission to query!");
                return null;
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
        }

        public List<LatitudeLongitude> getLatLongByDatetaken(ContentResolver resolver, Uri uri, long startDate, long endDate, String orderBy) {
            return getLatLongByDatetaken(resolver, uri, startDate, endDate, orderBy, "1 = 1");
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public List<LatitudeLongitude> getLatLongByDatetaken(ContentResolver resolver, Uri uri, long startDate, long endDate, String orderBy, String extraWhere) {
            Closeable closeable = null;
            ArrayList<LatitudeLongitude> result = new ArrayList();
            try {
                closeable = resolver.query(uri, LATITUDE_LONGITUDE_PROJECTION, extraWhere + " AND " + " (latitude != 0.0 OR longitude != 0.0) " + " AND " + this.mWhereClauseSetLatitudeLongitude, getQueryClauseLatLngArgs(startDate, endDate), orderBy);
                if (closeable == null || closeable.getCount() == 0) {
                    Utils.closeSilently(closeable);
                    return result;
                }
                while (closeable.moveToNext()) {
                    result.add(new LatitudeLongitude(closeable.getDouble(0), closeable.getDouble(1)));
                }
                Utils.closeSilently(closeable);
                return result;
            } catch (SecurityException e) {
                GalleryLog.w("SqlHelper", "No permission to query!");
            } catch (Throwable th) {
                Utils.closeSilently(closeable);
            }
        }

        public boolean updateMode(boolean beBiggerView) {
            TimeBucketPageViewMode updateMode = this.mMode.getUpdateMode(beBiggerView);
            if (updateMode == this.mMode) {
                return false;
            }
            this.mMode = updateMode;
            return true;
        }

        public TimeBucketPageViewMode getMode() {
            return this.mMode;
        }
    }

    List<LatitudeLongitude> getLatLongByDatetaken(long j, long j2);

    String getMediaTableName();

    TimeBucketPageViewMode getMode();

    RectF getRectByDatetaken(long j, long j2);

    String getTimeColumnName();

    boolean updateMode(boolean z);
}
