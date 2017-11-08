package com.huawei.gallery.story.utils;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.text.TextUtils;
import com.android.gallery3d.R;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.ReverseGeocoder;
import com.huawei.android.cg.vo.FileInfo;
import com.huawei.gallery.media.GeoKnowledge;
import com.huawei.gallery.media.StoryAlbum;
import com.huawei.gallery.media.StoryAlbumFile;
import com.huawei.gallery.photoshare.utils.PhotoShareUtils;
import com.huawei.gallery.recycle.utils.RecycleUtils;
import com.huawei.gallery.story.utils.LocationUtils.AddressInfo;
import com.huawei.gallery.story.utils.LocationUtils.LatlngData;
import com.huawei.gallery.story.utils.StoryAlbumDateUtils.DateTaken;
import com.huawei.gallery.util.MyPrinter;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import tmsdk.fg.module.spacemanager.PhotoScanResult.PhotoItem;

public class StoryAlbumUtils {
    private static final MyPrinter LOG = new MyPrinter("Clustering_StoryAlbumUtils");

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<StoryAlbum> queryStoryAlbums(ContentResolver contentResolver, boolean generated) {
        List<StoryAlbum> clusterAlbumList = new ArrayList();
        String whereClause = "story_id != ?";
        if (generated) {
            whereClause = whereClause.concat(" AND name != ''");
        }
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbum.URI, StoryAlbum.getProjection(), whereClause, new String[]{""}, "min_datetaken DESC");
            if (c != null) {
                while (c.moveToNext()) {
                    clusterAlbumList.add(new StoryAlbum(c));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query story albums failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return clusterAlbumList;
    }

    public static StoryAlbum queryStoryAlbumInfo(String clusterCode, ContentResolver contentResolver) {
        Closeable closeable = null;
        StoryAlbum storyAlbum;
        try {
            closeable = contentResolver.query(StoryAlbum.URI, StoryAlbum.getProjection(), queryClusterCodeWhereCluase(), new String[]{clusterCode}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            storyAlbum = new StoryAlbum(closeable);
            return storyAlbum;
        } catch (RuntimeException e) {
            storyAlbum = LOG;
            storyAlbum.w("query story album info failed. " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static String genStoryAlbumName(long minDateTaken, long maxDateTaken, String address, Context context) {
        String albumName = "";
        if (context == null) {
            return albumName;
        }
        String holidayString = StoryAlbumDateUtils.getStoryAlbumHolidayDateString(minDateTaken, maxDateTaken, context);
        int dayCount = StoryAlbumDateUtils.getDayCount(minDateTaken, maxDateTaken);
        if (dayCount >= 3) {
            albumName = context.getResources().getQuantityString(R.plurals.story_album_title_several_day_tour, dayCount, new Object[]{Integer.valueOf(dayCount), address});
        } else if (TextUtils.isEmpty(holidayString)) {
            albumName = address;
        } else {
            albumName = String.format(context.getResources().getString(R.string.story_album_title_in_somewhere), new Object[]{holidayString, address});
        }
        return albumName;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<StoryAlbumFile> queryTodoStoryAlbumFiles(ContentResolver contentResolver) {
        List<StoryAlbumFile> ret = new ArrayList();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbumFile.URI, StoryAlbumFile.getProjection(), "story_cluster_state=? AND substr(_display_name, 1, length(_display_name) - length('000.JPG')) NOT IN (SELECT substr(_display_name, 1, length(_display_name) - length('000_COVER.JPG')) FROM gallery_media WHERE _data LIKE '%BURST____COVER.JPG' ) AND (_data NOT LIKE '%Pre-loaded/Pictures%') AND bucket_display_name != 'MagazineUnlock' AND (mime_type = 'image/jpeg' AND (latitude != '0.0' AND longitude != '0.0') OR mime_type = 'video/mp4') AND (_size > 0)", new String[]{"todo"}, "showDatetoken DESC");
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(new StoryAlbumFile(c));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query cluster todo file failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<String> queryClusterCodeInGalleryMedia(ContentResolver contentResolver) {
        List<String> ret = new ArrayList();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbumFile.URI, new String[]{"story_id"}, "story_id != ? ) GROUP BY (story_id", new String[]{""}, null);
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(c.getString(0));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query cluster code in gallery media failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        LOG.d("cluster code in gallery media are:" + ret);
        return ret;
    }

    public static DateTaken queryStoryAlbumDateTaken(String clusterCode, ContentResolver contentResolver) {
        Closeable closeable = null;
        DateTaken dateTaken;
        try {
            closeable = contentResolver.query(StoryAlbumFile.URI, StoryAlbumFile.getMaxMinDatetakenProjection(), queryClusterCodeWhereCluase(), new String[]{clusterCode}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            dateTaken = new DateTaken(closeable);
            return dateTaken;
        } catch (RuntimeException e) {
            dateTaken = LOG;
            dateTaken.w("query dateTaken range failed. " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static LinkedHashMap<LatlngData, Uri> queryStoryAlbumLocationData(String clusterCode, ContentResolver contentResolver) {
        LinkedHashMap<LatlngData, Uri> latlngMap = new LinkedHashMap();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbumFile.URI, new String[]{"latitude", "longitude", "_data"}, queryClusterCodeWhereCluase() + " AND (latitude != ? AND longitude != ?)", new String[]{clusterCode, "0.0", "0.0"}, "showDatetoken ASC");
            if (c != null) {
                while (c.moveToNext()) {
                    latlngMap.put(new LatlngData(c), Uri.parse("file://" + c.getString(2)));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query lat lng failed. " + clusterCode + ": " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return latlngMap;
    }

    public static AddressInfo queryStoryAlbumAddressInfo(LatlngData latlngData, ContentResolver contentResolver, String language) {
        Closeable closeable = null;
        AddressInfo addressInfo;
        try {
            long locationKey = ReverseGeocoder.genLocationKey(latlngData.latitude, latlngData.longitude);
            ContentResolver contentResolver2 = contentResolver;
            closeable = contentResolver2.query(GeoKnowledge.URI, new String[]{"admin_area", "locality", "sub_locality"}, "location_key = ? AND language = ?", new String[]{String.valueOf(locationKey), language}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return null;
            }
            addressInfo = new AddressInfo((Cursor) closeable);
            return addressInfo;
        } catch (RuntimeException e) {
            addressInfo = LOG;
            addressInfo.w("query address from knowledge fail . " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<FileInfo> queryStoryAlbumUnReadyFileInfo(String clusterCode, int fileType, ContentResolver contentResolver) {
        List<FileInfo> unReadyData = new ArrayList();
        try {
            String unReadyWhereCluase = queryClusterCodeWhereCluase();
            switch (fileType) {
                case 1:
                    unReadyWhereCluase = unReadyWhereCluase + " AND  (local_media_id = '-1' AND (localBigThumbPath IS NULL OR localBigThumbPath ='')) ";
                    break;
                case 3:
                    unReadyWhereCluase = unReadyWhereCluase + " AND ( (local_media_id = '-1' AND (localBigThumbPath IS NULL OR localBigThumbPath =''))  OR  (local_media_id = '-1' AND mime_type = 'video/mp4') )";
                    break;
            }
            Closeable c = contentResolver.query(StoryAlbumFile.URI, StoryAlbumFile.getCloudFileProjection(), unReadyWhereCluase, new String[]{clusterCode}, null);
            if (c != null) {
                while (c.moveToNext()) {
                    FileInfo file = new FileInfo();
                    file.setFileId(Integer.toString(c.getInt(0)));
                    file.setAlbumId(c.getString(1));
                    file.setHash(c.getString(2));
                    file.setFileName(c.getString(3));
                    file.setFileUrl(c.getString(4));
                    file.setFileType(c.getInt(5));
                    file.setVideoThumbId(c.getString(6));
                    if (RecycleUtils.supportRecycle() && PhotoShareUtils.isGUIDSupport()) {
                        file.setUniqueId(c.getString(7));
                    }
                    unReadyData.add(file);
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query unready file info failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        if (unReadyData.size() > 0) {
            LOG.d("album " + clusterCode + " unready data size is " + unReadyData.size());
        } else {
            LOG.d("album " + clusterCode + " all data ready");
        }
        return unReadyData;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static int queryStoryAlbumFileCount(String clusterCode, ContentResolver contentResolver) {
        int storyFileCount = 0;
        try {
            Closeable c = contentResolver.query(StoryAlbumFile.URI, new String[]{"_id", "story_cluster_state"}, queryClusterCodeWhereCluase(), new String[]{clusterCode}, null);
            if (c != null) {
                while (c.moveToNext()) {
                    int id = c.getInt(0);
                    if ("done".equalsIgnoreCase(c.getString(1))) {
                        storyFileCount++;
                    } else {
                        StoryAlbumFile.clearStoryId(id, contentResolver);
                    }
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query cluster files count failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return storyFileCount;
    }

    public static void removeStoryAlbum(String clusterCode, ContentResolver contentResolver) {
        StoryAlbum.removeStoryAlbum(clusterCode, contentResolver);
        for (Integer id : queryStoryAlbumFilesId(clusterCode, contentResolver)) {
            StoryAlbumFile.removeStoryAlbumFile(id.intValue(), contentResolver);
        }
    }

    public static void clearStoryAlbumFiles(String clusterCode, ContentResolver contentResolver) {
        for (Integer id : queryStoryAlbumFilesId(clusterCode, contentResolver)) {
            StoryAlbumFile.clearStoryAlbumFile(id.intValue(), contentResolver);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static List<Integer> queryStoryAlbumFilesId(String clusterCode, ContentResolver contentResolver) {
        List<Integer> ret = new ArrayList();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbumFile.URI, new String[]{"_id"}, queryClusterCodeWhereCluase(), new String[]{clusterCode}, "showDatetoken ASC");
            if (c != null) {
                while (c.moveToNext()) {
                    ret.add(Integer.valueOf(c.getInt(0)));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query cluster files id failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return ret;
    }

    public static boolean queryAndDownloadUnReadyFiles(String clusterCode, int fileType, ContentResolver contentResolver) {
        List<FileInfo> unReadyFiles = queryStoryAlbumUnReadyFileInfo(clusterCode, fileType, contentResolver);
        if (unReadyFiles.size() == 0) {
            return false;
        }
        return StoryAlbumFileDownLoader.downloadUnReadyFiles(clusterCode, unReadyFiles, fileType);
    }

    public static void removeStoryAlbumFile(int id, String clusterCode, ContentResolver contentResolver) {
        StoryAlbumFile.removeStoryAlbumFile(id, contentResolver);
        StoryAlbum album = queryStoryAlbumInfo(clusterCode, contentResolver);
        if (album != null && album.isCoverId(id)) {
            updateStoryAlbumCoverId(album.getStoryId(), contentResolver);
        }
    }

    public static int updateStoryAlbumCoverId(String storyId, ContentResolver contentResolver) {
        int coverId = getStoryAlbumCoverId(storyId, contentResolver);
        StoryAlbum.setStoryAlbumCoverId(storyId, String.valueOf(coverId), contentResolver);
        return coverId;
    }

    public static int getStoryAlbumCoverId(String clusterCode, ContentResolver contentResolver) {
        List<Integer> filesId = queryStoryAlbumFilesId(clusterCode, contentResolver);
        if (filesId.size() == 0) {
            return 0;
        }
        return ((Integer) filesId.get(filesId.size() / 2)).intValue();
    }

    public static boolean isStoryAlbumFilesAvailable(int id, ContentResolver contentResolver) {
        Closeable closeable = null;
        try {
            ContentResolver contentResolver2 = contentResolver;
            closeable = contentResolver2.query(StoryAlbumFile.URI, new String[]{"COUNT(1)"}, "_id = ? AND story_cluster_state = ?", new String[]{String.valueOf(id), "done"}, null);
            if (closeable != null && closeable.moveToNext() && closeable.getInt(0) > 0) {
                return true;
            }
            Utils.closeSilently(closeable);
            return false;
        } catch (RuntimeException e) {
            LOG.w("query cluster files available failed. " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    public static int queryStoryAlbumItemCount(String storyId, ContentResolver contentResolver) {
        int i;
        Closeable closeable = null;
        try {
            ContentResolver contentResolver2 = contentResolver;
            closeable = contentResolver2.query(StoryAlbum.URI, new String[]{"COUNT(1)"}, "story_id = ?", new String[]{storyId}, null);
            if (closeable == null || !closeable.moveToNext()) {
                Utils.closeSilently(closeable);
                return -1;
            }
            i = closeable.getInt(0);
            return i;
        } catch (RuntimeException e) {
            i = LOG;
            i.w("query story album item count failed. " + e.getMessage());
        } finally {
            Utils.closeSilently(closeable);
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static void removeSimilarTimeFiles(String clusterCode, ContentResolver contentResolver) {
        Map<Integer, Long> dateTakenMap = new LinkedHashMap();
        try {
            ContentResolver contentResolver2 = contentResolver;
            Closeable c = contentResolver2.query(StoryAlbumFile.URI, new String[]{"_id", "showDatetoken"}, queryClusterCodeWhereCluase(), new String[]{clusterCode}, "showDatetoken DESC");
            if (c != null) {
                while (c.moveToNext()) {
                    dateTakenMap.put(Integer.valueOf(c.getInt(0)), Long.valueOf(c.getLong(1)));
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query cluster files count failed. " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        Long lastTime = Long.valueOf(0);
        List<Integer> similarTimeList = new ArrayList();
        for (Entry entry : dateTakenMap.entrySet()) {
            if (Math.abs(((Long) entry.getValue()).longValue() - lastTime.longValue()) < 10000) {
                similarTimeList.add((Integer) entry.getKey());
            } else {
                lastTime = (Long) entry.getValue();
            }
        }
        StoryAlbumFile.setStoryAlbumFileDuplicated(similarTimeList, contentResolver);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static ArrayList<PhotoItem> queryStoryAlbumPhotoItem(String storyId, ContentResolver contentResolver) {
        ArrayList<PhotoItem> itemList = new ArrayList();
        try {
            Closeable c = contentResolver.query(StoryAlbumFile.URI, StoryAlbumFile.getSimilarProcessProjection(), queryClusterCodeWhereCluase() + "AND mime_type != 'video/mp4'", new String[]{storyId}, null);
            if (c != null) {
                while (c.moveToNext()) {
                    PhotoItem item = new PhotoItem();
                    item.mTime = c.getLong(0);
                    item.mSize = c.getLong(1);
                    item.mPath = c.getString(2);
                    item.mDbId = c.getLong(3);
                    item.mIsOut = false;
                    item.mIsScreenShot = false;
                    itemList.add(item);
                }
            }
            Utils.closeSilently(c);
        } catch (RuntimeException e) {
            LOG.w("query album photo item fail. " + storyId + " " + e.getMessage());
        } catch (Throwable th) {
            Utils.closeSilently(null);
        }
        return itemList;
    }

    public static String queryClusterCodeWhereCluase() {
        return "story_id = ?";
    }
}
