package com.fyusion.sdk.common.ext.util.exif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.util.SparseIntArray;
import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteOrder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

/* compiled from: Unknown */
public class ExifInterface {
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final int TAG_APERTURE_VALUE = defineTag(2, (short) -28158);
    public static final int TAG_ARTIST = defineTag(0, (short) 315);
    public static final int TAG_BITS_PER_SAMPLE = defineTag(0, (short) 258);
    public static final int TAG_BRIGHTNESS_VALUE = defineTag(2, (short) -28157);
    public static final int TAG_CFA_PATTERN = defineTag(2, (short) -23806);
    public static final int TAG_COLOR_SPACE = defineTag(2, (short) -24575);
    public static final int TAG_COMPONENTS_CONFIGURATION = defineTag(2, (short) -28415);
    public static final int TAG_COMPRESSED_BITS_PER_PIXEL = defineTag(2, (short) -28414);
    public static final int TAG_COMPRESSION = defineTag(0, (short) 259);
    public static final int TAG_CONTRAST = defineTag(2, (short) -23544);
    public static final int TAG_COPYRIGHT = defineTag(0, (short) -32104);
    public static final int TAG_CUSTOM_RENDERED = defineTag(2, (short) -23551);
    public static final int TAG_DATE_TIME = defineTag(0, (short) 306);
    public static final int TAG_DATE_TIME_DIGITIZED = defineTag(2, (short) -28668);
    public static final int TAG_DATE_TIME_ORIGINAL = defineTag(2, (short) -28669);
    public static final int TAG_DEVICE_SETTING_DESCRIPTION = defineTag(2, (short) -23541);
    public static final int TAG_DIGITAL_ZOOM_RATIO = defineTag(2, (short) -23548);
    public static final int TAG_EXIF_IFD = defineTag(0, (short) -30871);
    public static final int TAG_EXIF_VERSION = defineTag(2, (short) -28672);
    public static final int TAG_EXPOSURE_BIAS_VALUE = defineTag(2, (short) -28156);
    public static final int TAG_EXPOSURE_INDEX = defineTag(2, (short) -24043);
    public static final int TAG_EXPOSURE_MODE = defineTag(2, (short) -23550);
    public static final int TAG_EXPOSURE_PROGRAM = defineTag(2, (short) -30686);
    public static final int TAG_EXPOSURE_TIME = defineTag(2, (short) -32102);
    public static final int TAG_FILE_SOURCE = defineTag(2, (short) -23808);
    public static final int TAG_FLASH = defineTag(2, (short) -28151);
    public static final int TAG_FLASHPIX_VERSION = defineTag(2, (short) -24576);
    public static final int TAG_FLASH_ENERGY = defineTag(2, (short) -24053);
    public static final int TAG_FOCAL_LENGTH = defineTag(2, (short) -28150);
    public static final int TAG_FOCAL_LENGTH_IN_35_MM_FILE = defineTag(2, (short) -23547);
    public static final int TAG_FOCAL_PLANE_RESOLUTION_UNIT = defineTag(2, (short) -24048);
    public static final int TAG_FOCAL_PLANE_X_RESOLUTION = defineTag(2, (short) -24050);
    public static final int TAG_FOCAL_PLANE_Y_RESOLUTION = defineTag(2, (short) -24049);
    public static final int TAG_F_NUMBER = defineTag(2, (short) -32099);
    public static final int TAG_GAIN_CONTROL = defineTag(2, (short) -23545);
    public static final int TAG_GPS_ALTITUDE = defineTag(4, (short) 6);
    public static final int TAG_GPS_ALTITUDE_REF = defineTag(4, (short) 5);
    public static final int TAG_GPS_AREA_INFORMATION = defineTag(4, (short) 28);
    public static final int TAG_GPS_DATE_STAMP = defineTag(4, (short) 29);
    public static final int TAG_GPS_DEST_BEARING = defineTag(4, (short) 24);
    public static final int TAG_GPS_DEST_BEARING_REF = defineTag(4, (short) 23);
    public static final int TAG_GPS_DEST_DISTANCE = defineTag(4, (short) 26);
    public static final int TAG_GPS_DEST_DISTANCE_REF = defineTag(4, (short) 25);
    public static final int TAG_GPS_DEST_LATITUDE = defineTag(4, (short) 20);
    public static final int TAG_GPS_DEST_LATITUDE_REF = defineTag(4, (short) 19);
    public static final int TAG_GPS_DEST_LONGITUDE = defineTag(4, (short) 22);
    public static final int TAG_GPS_DEST_LONGITUDE_REF = defineTag(4, (short) 21);
    public static final int TAG_GPS_DIFFERENTIAL = defineTag(4, (short) 30);
    public static final int TAG_GPS_DOP = defineTag(4, (short) 11);
    public static final int TAG_GPS_IFD = defineTag(0, (short) -30683);
    public static final int TAG_GPS_IMG_DIRECTION = defineTag(4, (short) 17);
    public static final int TAG_GPS_IMG_DIRECTION_REF = defineTag(4, (short) 16);
    public static final int TAG_GPS_LATITUDE = defineTag(4, (short) 2);
    public static final int TAG_GPS_LATITUDE_REF = defineTag(4, (short) 1);
    public static final int TAG_GPS_LONGITUDE = defineTag(4, (short) 4);
    public static final int TAG_GPS_LONGITUDE_REF = defineTag(4, (short) 3);
    public static final int TAG_GPS_MAP_DATUM = defineTag(4, (short) 18);
    public static final int TAG_GPS_MEASURE_MODE = defineTag(4, (short) 10);
    public static final int TAG_GPS_PROCESSING_METHOD = defineTag(4, (short) 27);
    public static final int TAG_GPS_SATTELLITES = defineTag(4, (short) 8);
    public static final int TAG_GPS_SPEED = defineTag(4, (short) 13);
    public static final int TAG_GPS_SPEED_REF = defineTag(4, (short) 12);
    public static final int TAG_GPS_STATUS = defineTag(4, (short) 9);
    public static final int TAG_GPS_TIME_STAMP = defineTag(4, (short) 7);
    public static final int TAG_GPS_TRACK = defineTag(4, (short) 15);
    public static final int TAG_GPS_TRACK_REF = defineTag(4, (short) 14);
    public static final int TAG_GPS_VERSION_ID = defineTag(4, (short) 0);
    public static final int TAG_IMAGE_DESCRIPTION = defineTag(0, (short) 270);
    public static final int TAG_IMAGE_LENGTH = defineTag(0, (short) 257);
    public static final int TAG_IMAGE_UNIQUE_ID = defineTag(2, (short) -23520);
    public static final int TAG_IMAGE_WIDTH = defineTag(0, (short) 256);
    public static final int TAG_INTEROPERABILITY_IFD = defineTag(2, (short) -24571);
    public static final int TAG_INTEROPERABILITY_INDEX = defineTag(3, (short) 1);
    public static final int TAG_ISO_SPEED_RATINGS = defineTag(2, (short) -30681);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT = defineTag(1, (short) 513);
    public static final int TAG_JPEG_INTERCHANGE_FORMAT_LENGTH = defineTag(1, (short) 514);
    public static final int TAG_LIGHT_SOURCE = defineTag(2, (short) -28152);
    public static final int TAG_MAKE = defineTag(0, (short) 271);
    public static final int TAG_MAKER_NOTE = defineTag(2, (short) -28036);
    public static final int TAG_MAX_APERTURE_VALUE = defineTag(2, (short) -28155);
    public static final int TAG_METERING_MODE = defineTag(2, (short) -28153);
    public static final int TAG_MODEL = defineTag(0, (short) 272);
    public static final int TAG_OECF = defineTag(2, (short) -30680);
    public static final int TAG_ORIENTATION = defineTag(0, (short) 274);
    public static final int TAG_PHOTOMETRIC_INTERPRETATION = defineTag(0, (short) 262);
    public static final int TAG_PIXEL_X_DIMENSION = defineTag(2, (short) -24574);
    public static final int TAG_PIXEL_Y_DIMENSION = defineTag(2, (short) -24573);
    public static final int TAG_PLANAR_CONFIGURATION = defineTag(0, (short) 284);
    public static final int TAG_PRIMARY_CHROMATICITIES = defineTag(0, (short) 319);
    public static final int TAG_REFERENCE_BLACK_WHITE = defineTag(0, (short) 532);
    public static final int TAG_RELATED_SOUND_FILE = defineTag(2, (short) -24572);
    public static final int TAG_RESOLUTION_UNIT = defineTag(0, (short) 296);
    public static final int TAG_ROWS_PER_STRIP = defineTag(0, (short) 278);
    public static final int TAG_SAMPLES_PER_PIXEL = defineTag(0, (short) 277);
    public static final int TAG_SATURATION = defineTag(2, (short) -23543);
    public static final int TAG_SCENE_CAPTURE_TYPE = defineTag(2, (short) -23546);
    public static final int TAG_SCENE_TYPE = defineTag(2, (short) -23807);
    public static final int TAG_SENSING_METHOD = defineTag(2, (short) -24041);
    public static final int TAG_SHARPNESS = defineTag(2, (short) -23542);
    public static final int TAG_SHUTTER_SPEED_VALUE = defineTag(2, (short) -28159);
    public static final int TAG_SOFTWARE = defineTag(0, (short) 305);
    public static final int TAG_SPATIAL_FREQUENCY_RESPONSE = defineTag(2, (short) -24052);
    public static final int TAG_SPECTRAL_SENSITIVITY = defineTag(2, (short) -30684);
    public static final int TAG_STRIP_BYTE_COUNTS = defineTag(0, (short) 279);
    public static final int TAG_STRIP_OFFSETS = defineTag(0, (short) 273);
    public static final int TAG_SUBJECT_AREA = defineTag(2, (short) -28140);
    public static final int TAG_SUBJECT_DISTANCE = defineTag(2, (short) -28154);
    public static final int TAG_SUBJECT_DISTANCE_RANGE = defineTag(2, (short) -23540);
    public static final int TAG_SUBJECT_LOCATION = defineTag(2, (short) -24044);
    public static final int TAG_SUB_SEC_TIME = defineTag(2, (short) -28016);
    public static final int TAG_SUB_SEC_TIME_DIGITIZED = defineTag(2, (short) -28014);
    public static final int TAG_SUB_SEC_TIME_ORIGINAL = defineTag(2, (short) -28015);
    public static final int TAG_TRANSFER_FUNCTION = defineTag(0, (short) 301);
    public static final int TAG_USER_COMMENT = defineTag(2, (short) -28026);
    public static final int TAG_WHITE_BALANCE = defineTag(2, (short) -23549);
    public static final int TAG_WHITE_POINT = defineTag(0, (short) 318);
    public static final int TAG_X_RESOLUTION = defineTag(0, (short) 282);
    public static final int TAG_Y_CB_CR_COEFFICIENTS = defineTag(0, (short) 529);
    public static final int TAG_Y_CB_CR_POSITIONING = defineTag(0, (short) 531);
    public static final int TAG_Y_CB_CR_SUB_SAMPLING = defineTag(0, (short) 530);
    public static final int TAG_Y_RESOLUTION = defineTag(0, (short) 283);
    protected static final HashSet<Short> a = new HashSet(b);
    private static HashSet<Short> b = new HashSet();
    private c c = new c(DEFAULT_BYTE_ORDER);
    private final DateFormat d = new SimpleDateFormat("yyyy:MM:dd kk:mm:ss");
    private final DateFormat e = new SimpleDateFormat("yyyy:MM:dd");
    private final Calendar f = Calendar.getInstance(TimeZone.getTimeZone("UTC"));
    private SparseIntArray g = null;

    static {
        b.add(Short.valueOf(getTrueTagKey(TAG_GPS_IFD)));
        b.add(Short.valueOf(getTrueTagKey(TAG_EXIF_IFD)));
        b.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT)));
        b.add(Short.valueOf(getTrueTagKey(TAG_INTEROPERABILITY_IFD)));
        b.add(Short.valueOf(getTrueTagKey(TAG_STRIP_OFFSETS)));
        a.add(Short.valueOf(getTrueTagKey(-1)));
        a.add(Short.valueOf(getTrueTagKey(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH)));
        a.add(Short.valueOf(getTrueTagKey(TAG_STRIP_BYTE_COUNTS)));
    }

    public ExifInterface() {
        this.e.setTimeZone(TimeZone.getTimeZone("UTC"));
    }

    protected static int a(int[] iArr) {
        if (iArr == null || iArr.length == 0) {
            return 0;
        }
        int[] a = h.a();
        int i = 0;
        for (int i2 = 0; i2 < 5; i2++) {
            for (int i3 : iArr) {
                if (a[i2] == i3) {
                    i |= 1 << i2;
                    break;
                }
            }
        }
        return i;
    }

    protected static void a(Closeable closeable) {
        if (closeable != null) {
            try {
                closeable.close();
            } catch (Throwable th) {
            }
        }
    }

    protected static boolean a(int i, int i2) {
        int[] a = h.a();
        int b = b(i);
        int i3 = 0;
        while (i3 < a.length) {
            if (i2 == a[i3] && ((b >> i3) & 1) == 1) {
                return true;
            }
            i3++;
        }
        return false;
    }

    protected static boolean a(short s) {
        return b.contains(Short.valueOf(s));
    }

    protected static int b(int i) {
        return i >>> 24;
    }

    private void b() {
        int a = a(new int[]{0, 1}) << 24;
        this.g.put(TAG_MAKE, a | 131072);
        this.g.put(TAG_IMAGE_WIDTH, (a | 262144) | 1);
        this.g.put(TAG_IMAGE_LENGTH, (a | 262144) | 1);
        this.g.put(TAG_BITS_PER_SAMPLE, (a | 196608) | 3);
        this.g.put(TAG_COMPRESSION, (a | 196608) | 1);
        this.g.put(TAG_PHOTOMETRIC_INTERPRETATION, (a | 196608) | 1);
        this.g.put(TAG_ORIENTATION, (a | 196608) | 1);
        this.g.put(TAG_SAMPLES_PER_PIXEL, (a | 196608) | 1);
        this.g.put(TAG_PLANAR_CONFIGURATION, (a | 196608) | 1);
        this.g.put(TAG_Y_CB_CR_SUB_SAMPLING, (a | 196608) | 2);
        this.g.put(TAG_Y_CB_CR_POSITIONING, (a | 196608) | 1);
        this.g.put(TAG_X_RESOLUTION, (a | 327680) | 1);
        this.g.put(TAG_Y_RESOLUTION, (a | 327680) | 1);
        this.g.put(TAG_RESOLUTION_UNIT, (a | 196608) | 1);
        this.g.put(TAG_STRIP_OFFSETS, a | 262144);
        this.g.put(TAG_ROWS_PER_STRIP, (a | 262144) | 1);
        this.g.put(TAG_STRIP_BYTE_COUNTS, a | 262144);
        this.g.put(TAG_TRANSFER_FUNCTION, (a | 196608) | 768);
        this.g.put(TAG_WHITE_POINT, (a | 327680) | 2);
        this.g.put(TAG_PRIMARY_CHROMATICITIES, (a | 327680) | 6);
        this.g.put(TAG_Y_CB_CR_COEFFICIENTS, (a | 327680) | 3);
        this.g.put(TAG_REFERENCE_BLACK_WHITE, (a | 327680) | 6);
        this.g.put(TAG_DATE_TIME, (a | 131072) | 20);
        this.g.put(TAG_IMAGE_DESCRIPTION, a | 131072);
        this.g.put(TAG_MAKE, a | 131072);
        this.g.put(TAG_MODEL, a | 131072);
        this.g.put(TAG_SOFTWARE, a | 131072);
        this.g.put(TAG_ARTIST, a | 131072);
        this.g.put(TAG_COPYRIGHT, a | 131072);
        this.g.put(TAG_EXIF_IFD, (a | 262144) | 1);
        this.g.put(TAG_GPS_IFD, (a | 262144) | 1);
        a = a(new int[]{1}) << 24;
        this.g.put(TAG_JPEG_INTERCHANGE_FORMAT, (a | 262144) | 1);
        this.g.put(TAG_JPEG_INTERCHANGE_FORMAT_LENGTH, (a | 262144) | 1);
        a = a(new int[]{2}) << 24;
        this.g.put(TAG_EXIF_VERSION, (a | 458752) | 4);
        this.g.put(TAG_FLASHPIX_VERSION, (a | 458752) | 4);
        this.g.put(TAG_COLOR_SPACE, (a | 196608) | 1);
        this.g.put(TAG_COMPONENTS_CONFIGURATION, (a | 458752) | 4);
        this.g.put(TAG_COMPRESSED_BITS_PER_PIXEL, (a | 327680) | 1);
        this.g.put(TAG_PIXEL_X_DIMENSION, (a | 262144) | 1);
        this.g.put(TAG_PIXEL_Y_DIMENSION, (a | 262144) | 1);
        this.g.put(TAG_MAKER_NOTE, a | 458752);
        this.g.put(TAG_USER_COMMENT, a | 458752);
        this.g.put(TAG_RELATED_SOUND_FILE, (a | 131072) | 13);
        this.g.put(TAG_DATE_TIME_ORIGINAL, (a | 131072) | 20);
        this.g.put(TAG_DATE_TIME_DIGITIZED, (a | 131072) | 20);
        this.g.put(TAG_SUB_SEC_TIME, a | 131072);
        this.g.put(TAG_SUB_SEC_TIME_ORIGINAL, a | 131072);
        this.g.put(TAG_SUB_SEC_TIME_DIGITIZED, a | 131072);
        this.g.put(TAG_IMAGE_UNIQUE_ID, (a | 131072) | 33);
        this.g.put(TAG_EXPOSURE_TIME, (a | 327680) | 1);
        this.g.put(TAG_F_NUMBER, (a | 327680) | 1);
        this.g.put(TAG_EXPOSURE_PROGRAM, (a | 196608) | 1);
        this.g.put(TAG_SPECTRAL_SENSITIVITY, a | 131072);
        this.g.put(TAG_ISO_SPEED_RATINGS, a | 196608);
        this.g.put(TAG_OECF, a | 458752);
        this.g.put(TAG_SHUTTER_SPEED_VALUE, (655360 | a) | 1);
        this.g.put(TAG_APERTURE_VALUE, (a | 327680) | 1);
        this.g.put(TAG_BRIGHTNESS_VALUE, (655360 | a) | 1);
        this.g.put(TAG_EXPOSURE_BIAS_VALUE, (655360 | a) | 1);
        this.g.put(TAG_MAX_APERTURE_VALUE, (a | 327680) | 1);
        this.g.put(TAG_SUBJECT_DISTANCE, (a | 327680) | 1);
        this.g.put(TAG_METERING_MODE, (a | 196608) | 1);
        this.g.put(TAG_LIGHT_SOURCE, (a | 196608) | 1);
        this.g.put(TAG_FLASH, (a | 196608) | 1);
        this.g.put(TAG_FOCAL_LENGTH, (a | 327680) | 1);
        this.g.put(TAG_SUBJECT_AREA, a | 196608);
        this.g.put(TAG_FLASH_ENERGY, (a | 327680) | 1);
        this.g.put(TAG_SPATIAL_FREQUENCY_RESPONSE, a | 458752);
        this.g.put(TAG_FOCAL_PLANE_X_RESOLUTION, (a | 327680) | 1);
        this.g.put(TAG_FOCAL_PLANE_Y_RESOLUTION, (a | 327680) | 1);
        this.g.put(TAG_FOCAL_PLANE_RESOLUTION_UNIT, (a | 196608) | 1);
        this.g.put(TAG_SUBJECT_LOCATION, (a | 196608) | 2);
        this.g.put(TAG_EXPOSURE_INDEX, (a | 327680) | 1);
        this.g.put(TAG_SENSING_METHOD, (a | 196608) | 1);
        this.g.put(TAG_FILE_SOURCE, (a | 458752) | 1);
        this.g.put(TAG_SCENE_TYPE, (a | 458752) | 1);
        this.g.put(TAG_CFA_PATTERN, a | 458752);
        this.g.put(TAG_CUSTOM_RENDERED, (a | 196608) | 1);
        this.g.put(TAG_EXPOSURE_MODE, (a | 196608) | 1);
        this.g.put(TAG_WHITE_BALANCE, (a | 196608) | 1);
        this.g.put(TAG_DIGITAL_ZOOM_RATIO, (a | 327680) | 1);
        this.g.put(TAG_FOCAL_LENGTH_IN_35_MM_FILE, (a | 196608) | 1);
        this.g.put(TAG_SCENE_CAPTURE_TYPE, (a | 196608) | 1);
        this.g.put(TAG_GAIN_CONTROL, (a | 327680) | 1);
        this.g.put(TAG_CONTRAST, (a | 196608) | 1);
        this.g.put(TAG_SATURATION, (a | 196608) | 1);
        this.g.put(TAG_SHARPNESS, (a | 196608) | 1);
        this.g.put(TAG_DEVICE_SETTING_DESCRIPTION, a | 458752);
        this.g.put(TAG_SUBJECT_DISTANCE_RANGE, (a | 196608) | 1);
        this.g.put(TAG_INTEROPERABILITY_IFD, (a | 262144) | 1);
        a = a(new int[]{4}) << 24;
        this.g.put(TAG_GPS_VERSION_ID, (65536 | a) | 4);
        this.g.put(TAG_GPS_LATITUDE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_LONGITUDE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_LATITUDE, (655360 | a) | 3);
        this.g.put(TAG_GPS_LONGITUDE, (655360 | a) | 3);
        this.g.put(TAG_GPS_ALTITUDE_REF, (65536 | a) | 1);
        this.g.put(TAG_GPS_ALTITUDE, (a | 327680) | 1);
        this.g.put(TAG_GPS_TIME_STAMP, (a | 327680) | 3);
        this.g.put(TAG_GPS_SATTELLITES, a | 131072);
        this.g.put(TAG_GPS_STATUS, (a | 131072) | 2);
        this.g.put(TAG_GPS_MEASURE_MODE, (a | 131072) | 2);
        this.g.put(TAG_GPS_DOP, (a | 327680) | 1);
        this.g.put(TAG_GPS_SPEED_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_SPEED, (a | 327680) | 1);
        this.g.put(TAG_GPS_TRACK_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_TRACK, (a | 327680) | 1);
        this.g.put(TAG_GPS_IMG_DIRECTION_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_IMG_DIRECTION, (a | 327680) | 1);
        this.g.put(TAG_GPS_MAP_DATUM, a | 131072);
        this.g.put(TAG_GPS_DEST_LATITUDE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_DEST_LATITUDE, (a | 327680) | 1);
        this.g.put(TAG_GPS_DEST_BEARING_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_DEST_BEARING, (a | 327680) | 1);
        this.g.put(TAG_GPS_DEST_DISTANCE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_DEST_DISTANCE, (a | 327680) | 1);
        this.g.put(TAG_GPS_PROCESSING_METHOD, a | 458752);
        this.g.put(TAG_GPS_AREA_INFORMATION, a | 458752);
        this.g.put(TAG_GPS_DATE_STAMP, (a | 131072) | 11);
        this.g.put(TAG_GPS_DIFFERENTIAL, (a | 196608) | 11);
        this.g.put(TAG_INTEROPERABILITY_INDEX, (a(new int[]{3}) << 24) | 131072);
    }

    protected static short c(int i) {
        return (short) ((i >> 16) & 255);
    }

    protected static int d(int i) {
        return 65535 & i;
    }

    public static int defineTag(int i, short s) {
        return (65535 & s) | (i << 16);
    }

    public static int getTrueIfd(int i) {
        return i >>> 16;
    }

    public static short getTrueTagKey(int i) {
        return (short) i;
    }

    protected SparseIntArray a() {
        if (this.g == null) {
            this.g = new SparseIntArray();
            b();
        }
        return this.g;
    }

    protected ExifTag a(int i) {
        boolean z = false;
        int i2 = a().get(i);
        if (i2 == 0) {
            return null;
        }
        short c = c(i2);
        int d = d(i2);
        if (d != 0) {
            z = true;
        }
        return new ExifTag(getTrueTagKey(i), c, d, getTrueIfd(i), z);
    }

    public ExifTag buildTag(int i, int i2, Object obj) {
        boolean z = false;
        int i3 = a().get(i);
        if (i3 == 0 || obj == null) {
            return null;
        }
        short c = c(i3);
        int d = d(i3);
        if (d != 0) {
            z = true;
        }
        if (!a(i3, i2)) {
            return null;
        }
        ExifTag exifTag = new ExifTag(getTrueTagKey(i), c, d, i2, z);
        return exifTag.setValue(obj) ? exifTag : null;
    }

    public ExifTag buildTag(int i, Object obj) {
        return buildTag(i, getTrueIfd(i), obj);
    }

    public List<ExifTag> getAllTags() {
        return this.c.h();
    }

    public int getDefinedTagDefaultIfd(int i) {
        return a().get(i) != 0 ? getTrueIfd(i) : -1;
    }

    public OutputStream getExifWriterStream(OutputStream outputStream) {
        if (outputStream != null) {
            OutputStream eVar = new e(outputStream, this);
            eVar.a(this.c);
            return eVar;
        }
        throw new IllegalArgumentException("Argument is null");
    }

    public ExifTag getTag(int i) {
        return getTag(i, getDefinedTagDefaultIfd(i));
    }

    public ExifTag getTag(int i, int i2) {
        return ExifTag.isValidIfd(i2) ? this.c.a(getTrueTagKey(i), i2) : null;
    }

    public String getTagStringValue(int i) {
        return getTagStringValue(i, getDefinedTagDefaultIfd(i));
    }

    public String getTagStringValue(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsString() : null;
    }

    public void readExif(InputStream inputStream) throws IOException {
        if (inputStream != null) {
            try {
                this.c = new g(this).a(inputStream);
                return;
            } catch (ExifInvalidFormatException e) {
                throw new IOException("Invalid exif format : " + e);
            }
        }
        throw new IllegalArgumentException("Argument is null");
    }

    public void readExif(String str) throws FileNotFoundException, IOException {
        IOException e;
        if (str != null) {
            Closeable bufferedInputStream;
            try {
                bufferedInputStream = new BufferedInputStream(new FileInputStream(str));
                try {
                    readExif((InputStream) bufferedInputStream);
                    bufferedInputStream.close();
                    return;
                } catch (IOException e2) {
                    e = e2;
                    a(bufferedInputStream);
                    throw e;
                }
            } catch (IOException e3) {
                e = e3;
                bufferedInputStream = null;
                a(bufferedInputStream);
                throw e;
            }
        }
        throw new IllegalArgumentException("Argument is null");
    }

    public ExifTag setTag(ExifTag exifTag) {
        return this.c.a(exifTag);
    }

    public void setTags(Collection<ExifTag> collection) {
        for (ExifTag tag : collection) {
            setTag(tag);
        }
    }

    public void writeExif(Bitmap bitmap, OutputStream outputStream) throws IOException {
        if (bitmap == null || outputStream == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        OutputStream exifWriterStream = getExifWriterStream(outputStream);
        bitmap.compress(CompressFormat.JPEG, 90, exifWriterStream);
        exifWriterStream.flush();
    }
}
