package com.huawei.systemmanager.spacecleanner.utils;

import android.support.v4.view.InputDeviceCompat;
import android.text.TextUtils;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.secpatch.common.ConstValues;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashMap;
import java.util.Locale;

public class HwMediaFile {
    public static final int APK_FILE = 4;
    public static final int AUDIO_FILE = 1;
    public static final int COMPRESS_FILE = 6;
    public static final int DOCUMENT_FILE = 5;
    public static final int FILE_TYPE_3G2 = 14;
    private static final int FILE_TYPE_3GPP = 112;
    private static final int FILE_TYPE_3GPP2 = 114;
    private static final int FILE_TYPE_7Z = 513;
    public static final int FILE_TYPE_AAC = 12;
    public static final int FILE_TYPE_AC3 = 7;
    public static final int FILE_TYPE_AMR = 5;
    public static final int FILE_TYPE_APE = 17;
    private static final int FILE_TYPE_APK = 301;
    public static final int FILE_TYPE_ART = 212;
    private static final int FILE_TYPE_ASF = 116;
    public static final int FILE_TYPE_AU = 10;
    private static final int FILE_TYPE_AVI = 113;
    public static final int FILE_TYPE_AWB = 9;
    private static final int FILE_TYPE_BIN = 512;
    public static final int FILE_TYPE_BM = 213;
    public static final int FILE_TYPE_BMP = 204;
    private static final int FILE_TYPE_BZ2 = 507;
    private static final int FILE_TYPE_CSV = 406;
    private static final int FILE_TYPE_DIVX = 111;
    public static final int FILE_TYPE_DWG = 214;
    public static final int FILE_TYPE_DXF = 215;
    private static final int FILE_TYPE_EXE = 1001;
    private static final int FILE_TYPE_F4V = 110;
    public static final int FILE_TYPE_FLAC = 21;
    private static final int FILE_TYPE_FLV = 109;
    public static final int FILE_TYPE_GIF = 202;
    private static final int FILE_TYPE_GTAR = 511;
    private static final int FILE_TYPE_GZ = 506;
    private static final int FILE_TYPE_HTML = 401;
    public static final int FILE_TYPE_HTTPLIVE = 23;
    public static final int FILE_TYPE_ICO = 216;
    public static final int FILE_TYPE_IMY = 6;
    private static final int FILE_TYPE_INI = 404;
    private static final int FILE_TYPE_JAD = 503;
    private static final int FILE_TYPE_JAR = 504;
    public static final int FILE_TYPE_JPEG = 201;
    private static final int FILE_TYPE_LOG = 403;
    public static final int FILE_TYPE_M3U = 22;
    public static final int FILE_TYPE_M4A = 4;
    private static final int FILE_TYPE_M4V = 103;
    public static final int FILE_TYPE_MCF = 208;
    public static final int FILE_TYPE_MID = 8;
    public static final int FILE_TYPE_MKA = 13;
    private static final int FILE_TYPE_MKV = 104;
    public static final int FILE_TYPE_MMF = 19;
    private static final int FILE_TYPE_MP2TS = 106;
    public static final int FILE_TYPE_MP3 = 1;
    private static final int FILE_TYPE_MP4 = 102;
    private static final int FILE_TYPE_MPG = 105;
    private static final int FILE_TYPE_MPG4 = 101;
    public static final int FILE_TYPE_MPO = 207;
    private static final int FILE_TYPE_MS_EPUB = 412;
    private static final int FILE_TYPE_MS_EXCEL = 410;
    private static final int FILE_TYPE_MS_POWERPOINT = 411;
    private static final int FILE_TYPE_MS_WORD = 409;
    public static final int FILE_TYPE_NAP = 210;
    public static final int FILE_TYPE_NAPLPS = 211;
    public static final int FILE_TYPE_OGG = 16;
    private static final int FILE_TYPE_PDF = 408;
    public static final int FILE_TYPE_PLS = 24;
    public static final int FILE_TYPE_PNG = 203;
    public static final int FILE_TYPE_QCP = 15;
    public static final int FILE_TYPE_RA = 18;
    private static final int FILE_TYPE_RAR = 508;
    private static final int FILE_TYPE_RM = 108;
    public static final int FILE_TYPE_RMF = 11;
    private static final int FILE_TYPE_RV = 107;
    public static final int FILE_TYPE_SMF = 20;
    private static final int FILE_TYPE_SWF = 118;
    private static final int FILE_TYPE_TAR = 509;
    private static final int FILE_TYPE_TEXT = 402;
    private static final int FILE_TYPE_TGZ = 510;
    public static final int FILE_TYPE_TIF = 206;
    private static final int FILE_TYPE_VCF = 405;
    public static final int FILE_TYPE_WAV = 2;
    public static final int FILE_TYPE_WBMP = 205;
    private static final int FILE_TYPE_WEBM = 115;
    public static final int FILE_TYPE_WEBP = 206;
    public static final int FILE_TYPE_WMA = 3;
    private static final int FILE_TYPE_WMV = 117;
    private static final int FILE_TYPE_WPL = 502;
    private static final int FILE_TYPE_XML = 407;
    private static final int FILE_TYPE_Z = 505;
    private static final int FILE_TYPE_ZIP = 501;
    private static final int FIRST_AUDIO_FILE_TYPE = 1;
    private static final int FIRST_COMPRESS_FILE_TYPE = 501;
    private static final int FIRST_DOCUMENT_FILE_TYPE = 401;
    private static final int FIRST_IMAGE_FILE_TYPE = 201;
    private static final int FIRST_VIDEO_FILE_TYPE = 101;
    public static final int IMAGE_FILE = 3;
    private static final int LAST_AUDIO_FILE_TYPE = 24;
    private static final int LAST_COMPRESS_FILE_TYPE = 513;
    private static final int LAST_DOCUMENT_FILE_TYPE = 412;
    private static final int LAST_IMAGE_FILE_TYPE = 216;
    private static final int LAST_VIDEO_FILE_TYPE = 118;
    public static final int OTHER_FILE = 7;
    private static final String TAG = "HwMediaFile";
    public static final int VIDEO_FILE = 2;
    private static final HashMap<String, Integer> mExtensionToIconResMap = new HashMap();
    private static final HashMap<String, MediaFileType> sFileTypeMap = new HashMap();
    private static final HashMap<String, Integer> sMimeTypeMap = new HashMap();

    public static class MediaFileType {
        public final int fileType;
        public final String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    static {
        addFileType("MP3", 1, "audio/mpeg", R.drawable.list_music_mp3);
        addFileType("WAV", 2, "audio/x-wav", R.drawable.list_music_wav);
        addFileType("WMA", 3, "audio/x-ms-wma", R.drawable.list_music_wma);
        addFileType("M4A", 4, "audio/mp4", R.drawable.list_music_m4a);
        addFileType("AMR", 5, "audio/amr", R.drawable.list_music_amr);
        addFileType("APE", 17, "audio/ffmpeg", R.drawable.list_music_ape);
        addFileType("FLAC", 21, "audio/flac", R.drawable.list_music_flac);
        addFileType("MID", 8, "audio/midi", R.drawable.list_music_mid);
        addFileType("RTTTL", 8, "audio/midi", R.drawable.list_music);
        addFileType("MXMF", 8, "audio/midi", R.drawable.list_music);
        addFileType("XMF", 8, "audio/midi", R.drawable.list_music);
        addFileType("OTA", 8, "audio/midi", R.drawable.list_music);
        addFileType("RTX", 8, "audio/midi", R.drawable.list_music);
        addFileType("MIDI", 8, "audio/midi", R.drawable.list_music);
        addFileType("MPGA", 1, "audio/mpeg", R.drawable.list_music);
        addFileType("IMY", 6, "audio/imelody", R.drawable.list_music);
        addFileType("AC3", 7, "audio/ac3", R.drawable.list_music);
        addFileType("AWB", 9, "audio/amr-wb", R.drawable.list_music);
        addFileType("AU", 10, "audio/basic", R.drawable.list_music);
        addFileType("RMF", 11, "audio/x-rmf", R.drawable.list_music);
        addFileType("AAC", 12, "audio/aac", R.drawable.list_music);
        addFileType("AAC", 12, "audio/aac-adts", R.drawable.list_music);
        addFileType("AAC", 12, "audio/ffmpeg", R.drawable.list_music);
        addFileType("MKA", 13, "audio/x-matroska", R.drawable.list_music);
        addFileType("3G2", 14, "audio/3gpp2", R.drawable.list_music);
        addFileType("QCP", 15, "audio/qcp", R.drawable.list_music);
        addFileType("OGG", 16, "audio/ogg", R.drawable.list_music);
        addFileType("OGG", 16, "application/ogg", R.drawable.list_music);
        addFileType("RA", 18, "audio/x-pn-realaudio", R.drawable.list_music);
        addFileType("MMF", 19, "audio/x-skt-lbs", R.drawable.list_music);
        addFileType("SMF", 20, "audio/sp-midi", R.drawable.list_music);
        addFileType("M3U", 22, "audio/x-mpegurl", R.drawable.list_music);
        addFileType("M3U", 22, "application/x-mpegurl", R.drawable.list_music);
        addFileType("M3U8", 23, "application/vnd.apple.mpegurl", R.drawable.list_music);
        addFileType("M3U8", 23, "audio/mpegurl", R.drawable.list_music);
        addFileType("M3U8", 23, "audio/x-mpegurl", R.drawable.list_music);
        addFileType("PLS", 24, "audio/x-scpls", R.drawable.list_music);
        addFileType("OGA", 16, "application/ogg", R.drawable.list_music);
        addFileType("WMV", FILE_TYPE_WMV, "video/x-ms-wmv", R.drawable.list_video_wmv);
        addFileType("AVI", FILE_TYPE_AVI, "video/avi", R.drawable.list_video_avi);
        addFileType("MOV", 106, "video/quicktime", R.drawable.list_video_mov);
        addFileType("MP4", 102, "video/mp4", R.drawable.list_video_mp4);
        addFileType("MPG", 105, "video/mpeg", R.drawable.list_video_pmg);
        addFileType("RMVB", 106, "video/x-pn-realvideo", R.drawable.list_video_rmvb);
        addFileType("SWF", 118, "application/x-shockwave-flash", R.drawable.list_video_swf);
        addFileType("MPG4", 101, "video/mp4", R.drawable.list_video);
        addFileType("M4V", 103, "video/mp4", R.drawable.list_video);
        addFileType("MKV", 104, "video/x-matroska", R.drawable.list_video);
        addFileType("RV", FILE_TYPE_RV, "video/rv", R.drawable.list_video);
        addFileType("RM", 108, "video/x-pn-realvideo", R.drawable.list_video);
        addFileType("FLV", 109, "video/x-flv", R.drawable.list_video);
        addFileType("FLV", 109, "video/flvff", R.drawable.list_video);
        addFileType("F4V", FILE_TYPE_F4V, "video/x-f4v", R.drawable.list_video);
        addFileType("DIVX", 111, "video/divx", R.drawable.list_video);
        addFileType("TS", 106, "video/x-mpegts", R.drawable.list_video);
        addFileType("3GP", 112, "video/3gpp", R.drawable.list_video);
        addFileType("3GPP", 112, "video/3gpp", R.drawable.list_video);
        addFileType("3G2", FILE_TYPE_3GPP2, "video/3gpp2", R.drawable.list_video);
        addFileType("3GPP2", FILE_TYPE_3GPP2, "video/3gpp2", R.drawable.list_video);
        addFileType("WEBM", 115, "video/webm", R.drawable.list_video);
        addFileType("ASF", 116, "video/x-ms-asf", R.drawable.list_video);
        addFileType("M2TS", 106, "video/x-mpegts", R.drawable.list_video);
        addFileType("PNG", FILE_TYPE_PNG, "image/png", R.drawable.list_pic_png);
        addFileType("BMP", FILE_TYPE_BMP, "image/x-ms-bmp", R.drawable.list_pic_bmp);
        addFileType("GIF", FILE_TYPE_GIF, "image/gif", R.drawable.list_pic_gif);
        addFileType("JPEG", 201, "image/jpeg", R.drawable.list_pic_jpeg);
        addFileType("JPG", 201, "image/jpeg", R.drawable.list_pic_jpg);
        addFileType("JPE", 201, "image/jpeg", R.drawable.list_pic);
        addFileType("WBMP", FILE_TYPE_WBMP, "image/vnd.wap.wbmp", R.drawable.list_pic);
        addFileType("WEBP", 206, "image/webp", R.drawable.list_pic);
        addFileType("TIF", 206, "image/tiff", R.drawable.list_pic);
        addFileType("MPO", FILE_TYPE_MPO, "image/mpo", R.drawable.list_pic);
        addFileType("MCF", FILE_TYPE_MCF, "image/vasa", R.drawable.list_pic);
        addFileType("NAP", 210, "image/naplps", R.drawable.list_pic);
        addFileType("NAPLPS", 211, "image/naplps", R.drawable.list_pic);
        addFileType("ART", FILE_TYPE_ART, "image/x-jg", R.drawable.list_pic);
        addFileType("BM", FILE_TYPE_BM, "image/bmp", R.drawable.list_pic);
        addFileType("DWG", FILE_TYPE_DWG, "image/x-dwg", R.drawable.list_pic);
        addFileType("DXF", FILE_TYPE_DXF, "image/x-dwg", R.drawable.list_pic);
        addFileType("ICO", 216, "image/x-icon", R.drawable.list_pic);
        addFileType("APK", 301, "application/vnd.android", R.drawable.list_apk);
        addFileType("HTM", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("HTML", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("SHTML", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("DHTML", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("PHP", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("JSP", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("ASP", ConstValues.SRV_CODE_AUTH_FAIL, "text/html", R.drawable.list_file_html);
        addFileType("TXT", FILE_TYPE_TEXT, "text/plain", R.drawable.list_file_txt);
        addFileType("LOG", FILE_TYPE_LOG, "text/plain", R.drawable.list_file_txt);
        addFileType("INI", FILE_TYPE_INI, "text/plain", R.drawable.list_file_txt);
        addFileType("VCF", FILE_TYPE_VCF, "text/x-vcard", R.drawable.icon_vcard);
        addFileType("CSV", FILE_TYPE_CSV, "text/comma-separated-values", R.drawable.list_file_csv);
        addFileType("XML", FILE_TYPE_XML, "text/xml", R.drawable.list_file_xml);
        addFileType("PDF", FILE_TYPE_PDF, "application/pdf", R.drawable.list_file_pdf);
        addFileType("DOC", FILE_TYPE_MS_WORD, "application/msword", R.drawable.list_file_doc);
        addFileType("DOCX", FILE_TYPE_MS_WORD, "application/vnd.openxmlformats-officedocument.wordprocessingml.document", R.drawable.list_file_doc);
        addFileType("XLS", FILE_TYPE_MS_EXCEL, "application/vnd.ms-excel", R.drawable.list_file_xls);
        addFileType("XLSX", FILE_TYPE_MS_EXCEL, "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet", R.drawable.list_file_xls);
        addFileType("PPT", FILE_TYPE_MS_POWERPOINT, "application/mspowerpoint", R.drawable.list_file_ppt);
        addFileType("PPTX", FILE_TYPE_MS_POWERPOINT, "application/vnd.openxmlformats-officedocument.presentationml.presentation", R.drawable.list_file_ppt);
        addFileType("EPUB", 412, "application/epub+zip", R.drawable.list_file_epub);
        addFileType("ZIP", 501, "application/zip", R.drawable.list_compressed_zip);
        addFileType("RAR", 508, "application/x-rar-compressed", R.drawable.list_compressed_rar);
        addFileType("7Z", InputDeviceCompat.SOURCE_DPAD, "application/x-compress", R.drawable.list_compressed_7z);
        addFileType("WPL", FILE_TYPE_WPL, "application/vnd.ms-wpl", R.drawable.list_compressed);
        addFileType("JAD", FILE_TYPE_JAD, "text/vnd.sun.j2me.app-descriptor", R.drawable.list_compressed);
        addFileType("JAR", FILE_TYPE_JAR, "text/vnd.sun.j2me.app-descriptor", R.drawable.list_compressed);
        addFileType("Z", FILE_TYPE_Z, "application/x-compress", R.drawable.list_compressed);
        addFileType("GZ", FILE_TYPE_GZ, "application/x-gzip", R.drawable.list_compressed);
        addFileType("BZ2", FILE_TYPE_BZ2, "application/x-bzip2", R.drawable.list_compressed);
        addFileType("TAR", FILE_TYPE_TAR, "application/x-tar", R.drawable.list_compressed);
        addFileType("TGZ", FILE_TYPE_TGZ, "application/x-compressed", R.drawable.list_compressed);
        addFileType("GTAR", FILE_TYPE_GTAR, "application/x-gtar", R.drawable.list_compressed);
        addFileType("BIN", 512, "application/octet-stream", R.drawable.list_compressed);
        addFileType("EXE", 1001, "application/exe", R.drawable.list_file_exe);
    }

    static void addFileType(String extension, int fileType, String mimeType, int resId) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
        mExtensionToIconResMap.put(extension, Integer.valueOf(resId));
    }

    public static boolean isAudioFileType(int fileType) {
        return fileType >= 1 && fileType <= 24;
    }

    public static boolean isVideoFileType(int fileType) {
        return fileType >= 101 && fileType <= 118;
    }

    public static boolean isImageFileType(int fileType) {
        return fileType >= 201 && fileType <= 216;
    }

    public static boolean isAPKFileType(int fileType) {
        return fileType == 301;
    }

    public static boolean isDocumentFileType(int fileType) {
        return fileType >= ConstValues.SRV_CODE_AUTH_FAIL && fileType <= 412;
    }

    public static boolean isCompressFileType(int fileType) {
        return fileType >= 501 && fileType <= InputDeviceCompat.SOURCE_DPAD;
    }

    public static MediaFileType getFileType(String path) {
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "getFileType,path is empty.");
            return null;
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot < 0) {
            return null;
        }
        return (MediaFileType) sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.US));
    }

    public static int getIconResByPath(String path) {
        int i = 0;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "getIconResByPath,path is empty.");
            return 0;
        }
        int lastDot = path.lastIndexOf(46);
        if (lastDot < 0) {
            return 0;
        }
        Integer resId = (Integer) mExtensionToIconResMap.get(path.substring(lastDot + 1).toUpperCase(Locale.US));
        if (resId != null) {
            i = resId.intValue();
        }
        return i;
    }

    public static int getFileTypeForMimeType(String mimeType) {
        Integer value = (Integer) sMimeTypeMap.get(mimeType);
        return value == null ? 0 : value.intValue();
    }

    public static String getMimeTypeForFile(String path) {
        String str = null;
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "getMimeTypeForFile,path is empty.");
            return null;
        }
        MediaFileType mediaFileType = getFileType(path);
        if (mediaFileType != null) {
            str = mediaFileType.mimeType;
        }
        return str;
    }

    public static int getFileBigTypeByPath(String path) {
        if (TextUtils.isEmpty(path)) {
            HwLog.e(TAG, "getFileTypeByPath,path is empty.");
            return 7;
        }
        MediaFileType type = getFileType(path);
        if (type == null) {
            HwLog.e(TAG, "getFileTypeByPath,type is null. ");
            return 7;
        }
        int fileType = type.fileType;
        if (isAudioFileType(fileType)) {
            return 1;
        }
        if (isVideoFileType(fileType)) {
            return 2;
        }
        if (isImageFileType(fileType)) {
            return 3;
        }
        if (isAPKFileType(fileType)) {
            return 4;
        }
        if (isDocumentFileType(fileType)) {
            return 5;
        }
        if (isCompressFileType(fileType)) {
            return 6;
        }
        return 7;
    }
}
