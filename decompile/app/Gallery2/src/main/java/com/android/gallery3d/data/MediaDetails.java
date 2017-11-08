package com.android.gallery3d.data;

import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.exif.ExifTag;
import com.android.gallery3d.util.GalleryLog;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.TreeMap;

public class MediaDetails implements Iterable<Entry<Integer, Object>> {
    private TreeMap<Integer, Object> mDetails = new TreeMap();
    private HashMap<Integer, Integer> mUnits = new HashMap();

    public static class FlashState {
        private static int FLASH_FIRED_MASK = 1;
        private static int FLASH_FUNCTION_MASK = 32;
        private static int FLASH_MODE_MASK = 24;
        private static int FLASH_RED_EYE_MASK = 64;
        private static int FLASH_RETURN_MASK = 6;
        public int mState;

        public FlashState(int state) {
            this.mState = state;
        }

        public boolean isFlashFired() {
            return (this.mState & FLASH_FIRED_MASK) != 0;
        }
    }

    public interface MediaDetailsListener {
        void onDetailsChange(int i, int i2, String str);
    }

    public void addDetail(int index, Object value) {
        this.mDetails.put(Integer.valueOf(index), value);
    }

    public void deleteDetail(int index) {
        this.mDetails.remove(Integer.valueOf(index));
    }

    public Object getDetail(int index) {
        return this.mDetails.get(Integer.valueOf(index));
    }

    public int size() {
        return this.mDetails.size();
    }

    public Iterator<Entry<Integer, Object>> iterator() {
        return this.mDetails.entrySet().iterator();
    }

    public boolean hasUnit(int index) {
        return this.mUnits.containsKey(Integer.valueOf(index));
    }

    public int getUnit(int index) {
        return ((Integer) this.mUnits.get(Integer.valueOf(index))).intValue();
    }

    private static void setExifData(MediaDetails details, ExifTag tag, int key) {
        if (tag != null) {
            String value;
            int type = tag.getDataType();
            if (type == 5 || type == 10) {
                double rs = tag.getValueAsRational(0).toDouble();
                if (key != 104 || rs < 10.0d) {
                    value = String.valueOf(rs);
                } else {
                    value = String.valueOf((int) rs);
                }
            } else if (type == 2 || key == 112) {
                value = tag.getValueAsString();
            } else {
                value = String.valueOf(tag.forceGetValueAsLong(0));
            }
            if (key == 108) {
                try {
                    details.addDetail(key, new FlashState(Integer.valueOf(value.toString().trim()).intValue()));
                    return;
                } catch (NumberFormatException e) {
                    GalleryLog.e("MediaDetails", "Invalid FlashState value.");
                    return;
                }
            }
            details.addDetail(key, value);
        }
    }

    public static void extractExifInfo(MediaDetails details, String filePath) {
        ExifInterface exif = new ExifInterface();
        try {
            exif.readExif(filePath);
        } catch (FileNotFoundException e) {
            GalleryLog.w("MediaDetails", "Could not find file to read exif: " + filePath + "." + e.getMessage());
        } catch (IOException e2) {
            GalleryLog.w("MediaDetails", "Could not read exif from file: " + filePath + "." + e2.getMessage());
        } catch (Throwable t) {
            GalleryLog.w("MediaDetails", "fail to get exif thumb file:" + filePath + "." + t.getMessage());
        }
        setExifData(details, exif.getTag(ExifInterface.TAG_FLASH), 108);
        setExifData(details, exif.getTag(ExifInterface.TAG_IMAGE_WIDTH), 6);
        setExifData(details, exif.getTag(ExifInterface.TAG_IMAGE_LENGTH), 7);
        setExifData(details, exif.getTag(ExifInterface.TAG_PIXEL_X_DIMENSION), 6);
        setExifData(details, exif.getTag(ExifInterface.TAG_PIXEL_Y_DIMENSION), 7);
        setExifData(details, exif.getTag(ExifInterface.TAG_MAKE), 999);
        setExifData(details, exif.getTag(ExifInterface.TAG_MODEL), 2);
        setExifData(details, exif.getTag(ExifInterface.TAG_APERTURE_VALUE), OfflineMapStatus.EXCEPTION_SDCARD);
        setExifData(details, exif.getTag(ExifInterface.TAG_F_NUMBER), 104);
        setExifData(details, exif.getTag(ExifInterface.TAG_ISO_SPEED_RATINGS), 102);
        setExifData(details, exif.getTag(ExifInterface.TAG_WHITE_BALANCE), 107);
        setExifData(details, exif.getTag(ExifInterface.TAG_EXPOSURE_TIME), 101);
        setExifData(details, exif.getTag(ExifInterface.TAG_METERING_MODE), 110);
        setExifData(details, exif.getTag(ExifInterface.TAG_EXPOSURE_BIAS_VALUE), 109);
        setExifData(details, exif.getTag(ExifInterface.TAG_LIGHT_SOURCE), 111);
        setExifData(details, exif.getTag(ExifInterface.TAG_MAKER_NOTE), 112);
        setExifData(details, exif.getTag(ExifInterface.TAG_COLOR_SPACE), 113);
        ExifTag focalTag = exif.getTag(ExifInterface.TAG_FOCAL_LENGTH_IN_35_MM_FILE);
        int focalLength35 = focalTag != null ? focalTag.getValueAsInt(0) : 0;
        if (focalLength35 != 0) {
            details.addDetail(106, Integer.valueOf(focalLength35));
            return;
        }
        focalTag = exif.getTag(ExifInterface.TAG_FOCAL_LENGTH);
        double focalLength = focalTag != null ? focalTag.getValueAsRational(0).toDouble() : 0.0d;
        if (focalLength > 0.0d) {
            details.addDetail(105, Double.valueOf(focalLength));
        }
    }

    public static void extractDrmInfo(MediaDetails details, LocalMediaItem item) {
        details.addDetail(151, Boolean.valueOf(item.canForward()));
        details.addDetail(152, Integer.valueOf(item.getRightCount()));
        details.addDetail(154, Integer.valueOf(item.getMediaType()));
        details.addDetail(153, Boolean.valueOf(item.hasRight()));
    }
}
