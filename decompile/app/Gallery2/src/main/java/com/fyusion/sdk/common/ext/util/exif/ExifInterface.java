package com.fyusion.sdk.common.ext.util.exif;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;
import android.util.SparseIntArray;
import com.android.gallery3d.settings.HicloudAccountReceiver;
import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.channels.FileChannel.MapMode;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.TimeZone;

/* compiled from: Unknown */
public class ExifInterface {
    public static final ByteOrder DEFAULT_BYTE_ORDER = ByteOrder.BIG_ENDIAN;
    public static final int DEFINITION_NULL = 0;
    public static final int IFD_NULL = -1;
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
    public static final int TAG_NULL = -1;
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

    /* compiled from: Unknown */
    public interface ColorSpace {
        public static final short SRGB = (short) 1;
        public static final short UNCALIBRATED = (short) -1;
    }

    /* compiled from: Unknown */
    public interface ComponentsConfiguration {
        public static final short B = (short) 6;
        public static final short CB = (short) 2;
        public static final short CR = (short) 3;
        public static final short G = (short) 5;
        public static final short NOT_EXIST = (short) 0;
        public static final short R = (short) 4;
        public static final short Y = (short) 1;
    }

    /* compiled from: Unknown */
    public interface Compression {
        public static final short JPEG = (short) 6;
        public static final short UNCOMPRESSION = (short) 1;
    }

    /* compiled from: Unknown */
    public interface Contrast {
        public static final short HARD = (short) 2;
        public static final short NORMAL = (short) 0;
        public static final short SOFT = (short) 1;
    }

    /* compiled from: Unknown */
    public interface ExposureMode {
        public static final short AUTO_BRACKET = (short) 2;
        public static final short AUTO_EXPOSURE = (short) 0;
        public static final short MANUAL_EXPOSURE = (short) 1;
    }

    /* compiled from: Unknown */
    public interface ExposureProgram {
        public static final short ACTION_PROGRAM = (short) 6;
        public static final short APERTURE_PRIORITY = (short) 3;
        public static final short CREATIVE_PROGRAM = (short) 5;
        public static final short LANDSCAPE_MODE = (short) 8;
        public static final short MANUAL = (short) 1;
        public static final short NORMAL_PROGRAM = (short) 2;
        public static final short NOT_DEFINED = (short) 0;
        public static final short PROTRAIT_MODE = (short) 7;
        public static final short SHUTTER_PRIORITY = (short) 4;
    }

    /* compiled from: Unknown */
    public interface FileSource {
        public static final short DSC = (short) 3;
    }

    /* compiled from: Unknown */
    public interface Flash {
        public static final short DID_NOT_FIRED = (short) 0;
        public static final short FIRED = (short) 1;
        public static final short FUNCTION_NO_FUNCTION = (short) 32;
        public static final short FUNCTION_PRESENT = (short) 0;
        public static final short MODE_AUTO_MODE = (short) 24;
        public static final short MODE_COMPULSORY_FLASH_FIRING = (short) 8;
        public static final short MODE_COMPULSORY_FLASH_SUPPRESSION = (short) 16;
        public static final short MODE_UNKNOWN = (short) 0;
        public static final short RED_EYE_REDUCTION_NO_OR_UNKNOWN = (short) 0;
        public static final short RED_EYE_REDUCTION_SUPPORT = (short) 64;
        public static final short RETURN_NO_STROBE_RETURN_DETECTION_FUNCTION = (short) 0;
        public static final short RETURN_STROBE_RETURN_LIGHT_DETECTED = (short) 6;
        public static final short RETURN_STROBE_RETURN_LIGHT_NOT_DETECTED = (short) 4;
    }

    /* compiled from: Unknown */
    public interface GainControl {
        public static final short HIGH_DOWN = (short) 4;
        public static final short HIGH_UP = (short) 2;
        public static final short LOW_DOWN = (short) 3;
        public static final short LOW_UP = (short) 1;
        public static final short NONE = (short) 0;
    }

    /* compiled from: Unknown */
    public interface GpsAltitudeRef {
        public static final short SEA_LEVEL = (short) 0;
        public static final short SEA_LEVEL_NEGATIVE = (short) 1;
    }

    /* compiled from: Unknown */
    public interface GpsDifferential {
        public static final short DIFFERENTIAL_CORRECTION_APPLIED = (short) 1;
        public static final short WITHOUT_DIFFERENTIAL_CORRECTION = (short) 0;
    }

    /* compiled from: Unknown */
    public interface GpsLatitudeRef {
        public static final String NORTH = "N";
        public static final String SOUTH = "S";
    }

    /* compiled from: Unknown */
    public interface GpsLongitudeRef {
        public static final String EAST = "E";
        public static final String WEST = "W";
    }

    /* compiled from: Unknown */
    public interface GpsMeasureMode {
        public static final String MODE_2_DIMENSIONAL = "2";
        public static final String MODE_3_DIMENSIONAL = "3";
    }

    /* compiled from: Unknown */
    public interface GpsSpeedRef {
        public static final String KILOMETERS = "K";
        public static final String KNOTS = "N";
        public static final String MILES = "M";
    }

    /* compiled from: Unknown */
    public interface GpsStatus {
        public static final String INTEROPERABILITY = "V";
        public static final String IN_PROGRESS = "A";
    }

    /* compiled from: Unknown */
    public interface GpsTrackRef {
        public static final String MAGNETIC_DIRECTION = "M";
        public static final String TRUE_DIRECTION = "T";
    }

    /* compiled from: Unknown */
    public interface LightSource {
        public static final short CLOUDY_WEATHER = (short) 10;
        public static final short COOL_WHITE_FLUORESCENT = (short) 14;
        public static final short D50 = (short) 23;
        public static final short D55 = (short) 20;
        public static final short D65 = (short) 21;
        public static final short D75 = (short) 22;
        public static final short DAYLIGHT = (short) 1;
        public static final short DAYLIGHT_FLUORESCENT = (short) 12;
        public static final short DAY_WHITE_FLUORESCENT = (short) 13;
        public static final short FINE_WEATHER = (short) 9;
        public static final short FLASH = (short) 4;
        public static final short FLUORESCENT = (short) 2;
        public static final short ISO_STUDIO_TUNGSTEN = (short) 24;
        public static final short OTHER = (short) 255;
        public static final short SHADE = (short) 11;
        public static final short STANDARD_LIGHT_A = (short) 17;
        public static final short STANDARD_LIGHT_B = (short) 18;
        public static final short STANDARD_LIGHT_C = (short) 19;
        public static final short TUNGSTEN = (short) 3;
        public static final short UNKNOWN = (short) 0;
        public static final short WHITE_FLUORESCENT = (short) 15;
    }

    /* compiled from: Unknown */
    public interface MeteringMode {
        public static final short AVERAGE = (short) 1;
        public static final short CENTER_WEIGHTED_AVERAGE = (short) 2;
        public static final short MULTISPOT = (short) 4;
        public static final short OTHER = (short) 255;
        public static final short PARTAIL = (short) 6;
        public static final short PATTERN = (short) 5;
        public static final short SPOT = (short) 3;
        public static final short UNKNOWN = (short) 0;
    }

    /* compiled from: Unknown */
    public interface Orientation {
        public static final short BOTTOM_LEFT = (short) 3;
        public static final short BOTTOM_RIGHT = (short) 4;
        public static final short LEFT_BOTTOM = (short) 7;
        public static final short LEFT_TOP = (short) 5;
        public static final short RIGHT_BOTTOM = (short) 8;
        public static final short RIGHT_TOP = (short) 6;
        public static final short TOP_LEFT = (short) 1;
        public static final short TOP_RIGHT = (short) 2;
    }

    /* compiled from: Unknown */
    public interface PhotometricInterpretation {
        public static final short RGB = (short) 2;
        public static final short YCBCR = (short) 6;
    }

    /* compiled from: Unknown */
    public interface PlanarConfiguration {
        public static final short CHUNKY = (short) 1;
        public static final short PLANAR = (short) 2;
    }

    /* compiled from: Unknown */
    public interface ResolutionUnit {
        public static final short CENTIMETERS = (short) 3;
        public static final short INCHES = (short) 2;
    }

    /* compiled from: Unknown */
    public interface Saturation {
        public static final short HIGH = (short) 2;
        public static final short LOW = (short) 1;
        public static final short NORMAL = (short) 0;
    }

    /* compiled from: Unknown */
    public interface SceneCapture {
        public static final short LANDSCAPE = (short) 1;
        public static final short NIGHT_SCENE = (short) 3;
        public static final short PROTRAIT = (short) 2;
        public static final short STANDARD = (short) 0;
    }

    /* compiled from: Unknown */
    public interface SceneType {
        public static final short DIRECT_PHOTOGRAPHED = (short) 1;
    }

    /* compiled from: Unknown */
    public interface SensingMethod {
        public static final short COLOR_SEQUENTIAL_AREA = (short) 5;
        public static final short COLOR_SEQUENTIAL_LINEAR = (short) 8;
        public static final short NOT_DEFINED = (short) 1;
        public static final short ONE_CHIP_COLOR = (short) 2;
        public static final short THREE_CHIP_COLOR = (short) 4;
        public static final short TRILINEAR = (short) 7;
        public static final short TWO_CHIP_COLOR = (short) 3;
    }

    /* compiled from: Unknown */
    public interface Sharpness {
        public static final short HARD = (short) 2;
        public static final short NORMAL = (short) 0;
        public static final short SOFT = (short) 1;
    }

    /* compiled from: Unknown */
    public interface SubjectDistance {
        public static final short CLOSE_VIEW = (short) 2;
        public static final short DISTANT_VIEW = (short) 3;
        public static final short MACRO = (short) 1;
        public static final short UNKNOWN = (short) 0;
    }

    /* compiled from: Unknown */
    public interface WhiteBalance {
        public static final short AUTO = (short) 0;
        public static final short MANUAL = (short) 1;
    }

    /* compiled from: Unknown */
    public interface YCbCrPositioning {
        public static final short CENTERED = (short) 1;
        public static final short CO_SITED = (short) 2;
    }

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

    private void a(InputStream inputStream, OutputStream outputStream) throws IOException {
        byte[] bArr = new byte[1024];
        int read = inputStream.read(bArr, 0, 1024);
        while (read != -1) {
            outputStream.write(bArr, 0, read);
            read = inputStream.read(bArr, 0, 1024);
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

    private static Rational[] a(double d) {
        double abs = Math.abs(d);
        abs = (abs - ((double) ((int) abs))) * 60.0d;
        int i = (int) ((abs - ((double) ((int) abs))) * 6000.0d);
        return new Rational[]{new Rational((long) r2, 1), new Rational((long) r3, 1), new Rational((long) i, 100)};
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
        this.g.put(TAG_GPS_VERSION_ID, (HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT | a) | 4);
        this.g.put(TAG_GPS_LATITUDE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_LONGITUDE_REF, (a | 131072) | 2);
        this.g.put(TAG_GPS_LATITUDE, (655360 | a) | 3);
        this.g.put(TAG_GPS_LONGITUDE, (655360 | a) | 3);
        this.g.put(TAG_GPS_ALTITUDE_REF, (HicloudAccountReceiver.MSG_HICLOUD_ACCOUNT_LOGOUT | a) | 1);
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

    public static double convertLatOrLongToDouble(Rational[] rationalArr, String str) {
        try {
            double toDouble = (rationalArr[0].toDouble() + (rationalArr[1].toDouble() / 60.0d)) + (rationalArr[2].toDouble() / 3600.0d);
            return (str.equals(GpsLatitudeRef.SOUTH) || str.equals(GpsLongitudeRef.WEST)) ? -toDouble : toDouble;
        } catch (ArrayIndexOutOfBoundsException e) {
            throw new IllegalArgumentException();
        }
    }

    protected static int d(int i) {
        return 65535 & i;
    }

    public static int defineTag(int i, short s) {
        return (65535 & s) | (i << 16);
    }

    public static short getOrientationValueForRotation(int i) {
        int i2 = i % 360;
        if (i2 < 0) {
            i2 += 360;
        }
        return i2 >= 90 ? i2 >= 180 ? i2 >= 270 ? (short) 8 : (short) 3 : (short) 6 : (short) 1;
    }

    public static int getRotationForOrientationValue(short s) {
        switch (s) {
            case (short) 1:
                return 0;
            case (short) 3:
                return 180;
            case (short) 6:
                return 90;
            case (short) 8:
                return 270;
            default:
                return 0;
        }
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

    public boolean addDateTimeStampTag(int i, long j, TimeZone timeZone) {
        if (i != TAG_DATE_TIME && i != TAG_DATE_TIME_DIGITIZED && i != TAG_DATE_TIME_ORIGINAL) {
            return false;
        }
        this.d.setTimeZone(timeZone);
        ExifTag buildTag = buildTag(i, this.d.format(Long.valueOf(j)));
        if (buildTag == null) {
            return false;
        }
        setTag(buildTag);
        return true;
    }

    public boolean addGpsDateTimeStampTag(long j) {
        ExifTag buildTag = buildTag(TAG_GPS_DATE_STAMP, this.e.format(Long.valueOf(j)));
        if (buildTag == null) {
            return false;
        }
        setTag(buildTag);
        this.f.setTimeInMillis(j);
        buildTag = buildTag(TAG_GPS_TIME_STAMP, new Rational[]{new Rational((long) this.f.get(11), 1), new Rational((long) this.f.get(12), 1), new Rational((long) this.f.get(13), 1)});
        if (buildTag == null) {
            return false;
        }
        setTag(buildTag);
        return true;
    }

    public boolean addGpsTags(double d, double d2) {
        ExifTag buildTag = buildTag(TAG_GPS_LATITUDE, a(d));
        ExifTag buildTag2 = buildTag(TAG_GPS_LONGITUDE, a(d2));
        ExifTag buildTag3 = buildTag(TAG_GPS_LATITUDE_REF, d >= 0.0d ? "N" : GpsLatitudeRef.SOUTH);
        ExifTag buildTag4 = buildTag(TAG_GPS_LONGITUDE_REF, d2 >= 0.0d ? GpsLongitudeRef.EAST : GpsLongitudeRef.WEST);
        if (buildTag == null || buildTag2 == null || buildTag3 == null || buildTag4 == null) {
            return false;
        }
        setTag(buildTag);
        setTag(buildTag2);
        setTag(buildTag3);
        setTag(buildTag4);
        return true;
    }

    protected int[] b(short s) {
        int[] a = h.a();
        int[] iArr = new int[a.length];
        SparseIntArray a2 = a();
        int i = 0;
        for (int defineTag : a) {
            int defineTag2;
            int defineTag3 = defineTag(defineTag2, s);
            if (a2.get(defineTag3) != 0) {
                defineTag2 = i + 1;
                iArr[i] = defineTag3;
                i = defineTag2;
            }
        }
        return i != 0 ? Arrays.copyOfRange(iArr, 0, i) : null;
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

    public void clearExif() {
        this.c = new c(DEFAULT_BYTE_ORDER);
    }

    public void deleteTag(int i) {
        deleteTag(i, getDefinedTagDefaultIfd(i));
    }

    public void deleteTag(int i, int i2) {
        this.c.b(getTrueTagKey(i), i2);
    }

    public void forceRewriteExif(String str) throws FileNotFoundException, IOException {
        forceRewriteExif(str, getAllTags());
    }

    public void forceRewriteExif(String str, Collection<ExifTag> collection) throws FileNotFoundException, IOException {
        Closeable fileInputStream;
        IOException e;
        Throwable th;
        if (collection != null && !rewriteExif(str, (Collection) collection)) {
            c cVar = this.c;
            this.c = new c(DEFAULT_BYTE_ORDER);
            try {
                fileInputStream = new FileInputStream(str);
                try {
                    OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
                    a((InputStream) fileInputStream, byteArrayOutputStream);
                    byte[] toByteArray = byteArrayOutputStream.toByteArray();
                    readExif(toByteArray);
                    setTags(collection);
                    writeExif(toByteArray, str);
                    a(fileInputStream);
                    this.c = cVar;
                } catch (IOException e2) {
                    e = e2;
                    try {
                        a(fileInputStream);
                        throw e;
                    } catch (Throwable th2) {
                        th = th2;
                        a(fileInputStream);
                        this.c = cVar;
                        throw th;
                    }
                }
            } catch (IOException e3) {
                e = e3;
                fileInputStream = null;
                a(fileInputStream);
                throw e;
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = null;
                a(fileInputStream);
                this.c = cVar;
                throw th;
            }
        }
    }

    public int getActualTagCount(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getComponentCount() : 0;
    }

    public List<ExifTag> getAllTags() {
        return this.c.h();
    }

    public int getDefinedTagCount(int i) {
        int i2 = a().get(i);
        return i2 != 0 ? d(i2) : 0;
    }

    public int getDefinedTagDefaultIfd(int i) {
        return a().get(i) != 0 ? getTrueIfd(i) : -1;
    }

    public short getDefinedTagType(int i) {
        int i2 = a().get(i);
        return i2 != 0 ? c(i2) : (short) -1;
    }

    public OutputStream getExifWriterStream(OutputStream outputStream) {
        if (outputStream != null) {
            OutputStream eVar = new e(outputStream, this);
            eVar.a(this.c);
            return eVar;
        }
        throw new IllegalArgumentException("Argument is null");
    }

    public OutputStream getExifWriterStream(String str) throws FileNotFoundException {
        if (str != null) {
            try {
                return getExifWriterStream(new FileOutputStream(str));
            } catch (FileNotFoundException e) {
                a(null);
                throw e;
            }
        }
        throw new IllegalArgumentException("Argument is null");
    }

    public double[] getLatLongAsDoubles() {
        Rational[] tagRationalValues = getTagRationalValues(TAG_GPS_LATITUDE);
        String tagStringValue = getTagStringValue(TAG_GPS_LATITUDE_REF);
        Rational[] tagRationalValues2 = getTagRationalValues(TAG_GPS_LONGITUDE);
        String tagStringValue2 = getTagStringValue(TAG_GPS_LONGITUDE_REF);
        if (tagRationalValues == null || tagRationalValues2 == null || tagStringValue == null || tagStringValue2 == null || tagRationalValues.length < 3 || tagRationalValues2.length < 3) {
            return null;
        }
        return new double[]{convertLatOrLongToDouble(tagRationalValues, tagStringValue), convertLatOrLongToDouble(tagRationalValues2, tagStringValue2)};
    }

    public ExifTag getTag(int i) {
        return getTag(i, getDefinedTagDefaultIfd(i));
    }

    public ExifTag getTag(int i, int i2) {
        return ExifTag.isValidIfd(i2) ? this.c.a(getTrueTagKey(i), i2) : null;
    }

    public Byte getTagByteValue(int i) {
        return getTagByteValue(i, getDefinedTagDefaultIfd(i));
    }

    public Byte getTagByteValue(int i, int i2) {
        byte[] tagByteValues = getTagByteValues(i, i2);
        return (tagByteValues != null && tagByteValues.length > 0) ? Byte.valueOf(tagByteValues[0]) : null;
    }

    public byte[] getTagByteValues(int i) {
        return getTagByteValues(i, getDefinedTagDefaultIfd(i));
    }

    public byte[] getTagByteValues(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsBytes() : null;
    }

    public Integer getTagIntValue(int i) {
        return getTagIntValue(i, getDefinedTagDefaultIfd(i));
    }

    public Integer getTagIntValue(int i, int i2) {
        int[] tagIntValues = getTagIntValues(i, i2);
        return (tagIntValues != null && tagIntValues.length > 0) ? Integer.valueOf(tagIntValues[0]) : null;
    }

    public int[] getTagIntValues(int i) {
        return getTagIntValues(i, getDefinedTagDefaultIfd(i));
    }

    public int[] getTagIntValues(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsInts() : null;
    }

    public Long getTagLongValue(int i) {
        return getTagLongValue(i, getDefinedTagDefaultIfd(i));
    }

    public Long getTagLongValue(int i, int i2) {
        long[] tagLongValues = getTagLongValues(i, i2);
        return (tagLongValues != null && tagLongValues.length > 0) ? Long.valueOf(tagLongValues[0]) : null;
    }

    public long[] getTagLongValues(int i) {
        return getTagLongValues(i, getDefinedTagDefaultIfd(i));
    }

    public long[] getTagLongValues(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsLongs() : null;
    }

    public Rational getTagRationalValue(int i) {
        return getTagRationalValue(i, getDefinedTagDefaultIfd(i));
    }

    public Rational getTagRationalValue(int i, int i2) {
        Rational[] tagRationalValues = getTagRationalValues(i, i2);
        return (tagRationalValues == null || tagRationalValues.length == 0) ? null : new Rational(tagRationalValues[0]);
    }

    public Rational[] getTagRationalValues(int i) {
        return getTagRationalValues(i, getDefinedTagDefaultIfd(i));
    }

    public Rational[] getTagRationalValues(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsRationals() : null;
    }

    public String getTagStringValue(int i) {
        return getTagStringValue(i, getDefinedTagDefaultIfd(i));
    }

    public String getTagStringValue(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValueAsString() : null;
    }

    public Object getTagValue(int i) {
        return getTagValue(i, getDefinedTagDefaultIfd(i));
    }

    public Object getTagValue(int i, int i2) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.getValue() : null;
    }

    public List<ExifTag> getTagsForIfdId(int i) {
        return this.c.d(i);
    }

    public List<ExifTag> getTagsForTagId(short s) {
        return this.c.a(s);
    }

    public byte[] getThumbnail() {
        return this.c.a();
    }

    public Bitmap getThumbnailBitmap() {
        if (this.c.b()) {
            byte[] a = this.c.a();
            return BitmapFactory.decodeByteArray(a, 0, a.length);
        }
        if (this.c.d()) {
        }
        return null;
    }

    public byte[] getThumbnailBytes() {
        if (this.c.b()) {
            return this.c.a();
        }
        if (this.c.d()) {
        }
        return null;
    }

    public String getUserComment() {
        return this.c.g();
    }

    public boolean hasThumbnail() {
        return this.c.b();
    }

    public boolean isTagCountDefined(int i) {
        boolean z = false;
        int i2 = a().get(i);
        if (i2 == 0) {
            return false;
        }
        if (d(i2) != 0) {
            z = true;
        }
        return z;
    }

    public boolean isThumbnailCompressed() {
        return this.c.b();
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
        Closeable bufferedInputStream;
        IOException e;
        if (str != null) {
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

    public void readExif(byte[] bArr) throws IOException {
        readExif(new ByteArrayInputStream(bArr));
    }

    public void removeCompressedThumbnail() {
        this.c.a(null);
    }

    public void removeTagDefinition(int i) {
        a().delete(i);
    }

    public void resetTagDefinitions() {
        this.g = null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean rewriteExif(String str, Collection<ExifTag> collection) throws FileNotFoundException, IOException {
        Throwable e;
        IOException e2;
        Closeable closeable = null;
        InputStream bufferedInputStream;
        try {
            File file = new File(str);
            bufferedInputStream = new BufferedInputStream(new FileInputStream(file));
            try {
                long h = (long) f.a(bufferedInputStream, this).h();
                bufferedInputStream.close();
                RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
                if ((randomAccessFile.length() >= h ? 1 : null) == null) {
                    throw new IOException("Filesize changed during operation");
                }
                boolean rewriteExif = rewriteExif(randomAccessFile.getChannel().map(MapMode.READ_WRITE, 0, h), (Collection) collection);
                a(null);
                randomAccessFile.close();
                return rewriteExif;
            } catch (Throwable e3) {
                throw new IOException("Invalid exif format : ", e3);
            } catch (IOException e4) {
                e2 = e4;
                try {
                    a(closeable);
                    throw e2;
                } catch (Throwable th) {
                    e3 = th;
                    closeable = bufferedInputStream;
                    a(closeable);
                    throw e3;
                }
            }
        } catch (IOException e5) {
            e2 = e5;
            bufferedInputStream = null;
            a(closeable);
            throw e2;
        } catch (Throwable th2) {
            e3 = th2;
            a(closeable);
            throw e3;
        }
    }

    public boolean rewriteExif(ByteBuffer byteBuffer, Collection<ExifTag> collection) throws IOException {
        try {
            d dVar = new d(byteBuffer, this);
            for (ExifTag a : collection) {
                dVar.a(a);
            }
            return dVar.b();
        } catch (ExifInvalidFormatException e) {
            throw new IOException("Invalid exif format : " + e);
        }
    }

    public boolean setCompressedThumbnail(Bitmap bitmap) {
        OutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        return bitmap.compress(CompressFormat.JPEG, 90, byteArrayOutputStream) ? setCompressedThumbnail(byteArrayOutputStream.toByteArray()) : false;
    }

    public boolean setCompressedThumbnail(byte[] bArr) {
        this.c.f();
        this.c.a(bArr);
        return true;
    }

    public void setExif(Collection<ExifTag> collection) {
        clearExif();
        setTags(collection);
    }

    public ExifTag setTag(ExifTag exifTag) {
        return this.c.a(exifTag);
    }

    public int setTagDefinition(short s, int i, short s2, short s3, int[] iArr) {
        if (a.contains(Short.valueOf(s)) || !ExifTag.isValidType(s2) || !ExifTag.isValidIfd(i)) {
            return -1;
        }
        int defineTag = defineTag(i, s);
        if (defineTag == -1) {
            return -1;
        }
        int a;
        int[] b = b(s);
        SparseIntArray a2 = a();
        Object obj = null;
        for (int i2 : iArr) {
            if (i == i2) {
                obj = 1;
            }
            if (!ExifTag.isValidIfd(i2)) {
                return -1;
            }
        }
        if (obj == null) {
            return -1;
        }
        a = a(iArr);
        if (b != null) {
            for (int i3 : b) {
                if ((b(a2.get(i3)) & a) != 0) {
                    return -1;
                }
            }
        }
        a().put(defineTag, ((a << 24) | (s2 << 16)) | s3);
        return defineTag;
    }

    public boolean setTagValue(int i, int i2, Object obj) {
        ExifTag tag = getTag(i, i2);
        return tag != null ? tag.setValue(obj) : false;
    }

    public boolean setTagValue(int i, Object obj) {
        return setTagValue(i, getDefinedTagDefaultIfd(i), obj);
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

    public void writeExif(Bitmap bitmap, String str) throws FileNotFoundException, IOException {
        Closeable closeable = null;
        if (bitmap == null || str == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        try {
            closeable = getExifWriterStream(str);
            bitmap.compress(CompressFormat.JPEG, 90, closeable);
            closeable.flush();
            closeable.close();
        } catch (IOException e) {
            a(closeable);
            throw e;
        }
    }

    public void writeExif(InputStream inputStream, OutputStream outputStream) throws IOException {
        if (inputStream == null || outputStream == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        OutputStream exifWriterStream = getExifWriterStream(outputStream);
        a(inputStream, exifWriterStream);
        exifWriterStream.flush();
    }

    public void writeExif(InputStream inputStream, String str) throws FileNotFoundException, IOException {
        Closeable closeable = null;
        if (inputStream == null || str == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        try {
            closeable = getExifWriterStream(str);
            a(inputStream, (OutputStream) closeable);
            closeable.flush();
            closeable.close();
        } catch (IOException e) {
            a(closeable);
            throw e;
        }
    }

    public void writeExif(String str, String str2) throws FileNotFoundException, IOException {
        IOException e;
        if (str == null || str2 == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        Closeable fileInputStream;
        try {
            fileInputStream = new FileInputStream(str);
            try {
                writeExif((InputStream) fileInputStream, str2);
                fileInputStream.close();
            } catch (IOException e2) {
                e = e2;
                a(fileInputStream);
                throw e;
            }
        } catch (IOException e3) {
            e = e3;
            fileInputStream = null;
            a(fileInputStream);
            throw e;
        }
    }

    public void writeExif(byte[] bArr, OutputStream outputStream) throws IOException {
        if (bArr == null || outputStream == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        OutputStream exifWriterStream = getExifWriterStream(outputStream);
        exifWriterStream.write(bArr, 0, bArr.length);
        exifWriterStream.flush();
    }

    public void writeExif(byte[] bArr, String str) throws FileNotFoundException, IOException {
        Closeable closeable = null;
        if (bArr == null || str == null) {
            throw new IllegalArgumentException("Argument is null");
        }
        try {
            closeable = getExifWriterStream(str);
            closeable.write(bArr, 0, bArr.length);
            closeable.flush();
            closeable.close();
        } catch (IOException e) {
            a(closeable);
            throw e;
        }
    }
}
