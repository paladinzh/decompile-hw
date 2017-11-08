package com.huawei.rcs.media;

import android.content.Context;
import android.content.Intent;
import android.media.DecoderCapabilities;
import android.media.DecoderCapabilities.AudioDecoder;
import android.media.DecoderCapabilities.VideoDecoder;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.text.TextUtils;
import android.widget.Toast;
import cn.com.xy.sms.sdk.SmartSmsSdkUtil;
import cn.com.xy.sms.sdk.service.msgurlservice.MsgUrlService;
import com.autonavi.amap.mapcore.VTMCDataCache;
import com.google.android.gms.R;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsTransaction;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map.Entry;

public class RcsMediaFileUtils {
    private static final boolean HAS_DRM_CONFIG = SystemProperties.get("ro.huawei.cust.oma_drm", "false").equals("true");
    public static final String sFileExtensions;
    private static HashMap<String, MediaFileType> sFileTypeMap = new HashMap();
    private static HashMap<String, Integer> sMimeTypeMap = new HashMap();

    public static class MediaFileType {
        public int fileType;
        public String mimeType;

        MediaFileType(int fileType, String mimeType) {
            this.fileType = fileType;
            this.mimeType = mimeType;
        }
    }

    static {
        addFileType("MP3", 1, "audio/mpeg");
        addFileType("M4A", 2, "audio/mp4");
        addFileType("WAV", 3, "audio/x-wav");
        addFileType("AMR", 4, "audio/amr");
        addFileType("AWB", 5, "audio/amr-wb");
        if (isWMAEnabled()) {
            addFileType("WMA", 6, "audio/x-ms-wma");
        }
        addFileType("OGG", 7, "application/ogg");
        addFileType("OGA", 7, "application/ogg");
        addFileType("AAC", 8, "audio/aac");
        addFileType("AAC", 8, "audio/aac-adts");
        addFileType("3G2", 5, "audio/3gpp2");
        addFileType("3GP", 5, "audio/3gpp");
        addFileType("AC3", 5, "audio/ac3");
        addFileType("ASF", 5, "audio/x-ms-asf");
        addFileType("AVI", 5, "audio/avi");
        addFileType("F4V", 5, "audio/mp4");
        addFileType("FLV", 5, "audio/x-flv");
        addFileType("M2TS", 5, "audio/x-mpegts");
        addFileType("MKA", 5, "audio/x-matroska");
        addFileType("MKV", 5, "audio/x-matroska");
        addFileType("MMF", 5, "audio/x-skt-lbs");
        addFileType("MP4", 5, "audio/mp4");
        addFileType("MOV", 5, "audio/quicktime");
        addFileType("OGG", 5, "audio/ogg");
        addFileType("RA", 5, "audio/x-pn-realaudio");
        addFileType("RM", 5, "audio/x-pn-realaudio");
        addFileType("RMVB", 5, "audio/x-pn-realaudio");
        addFileType("TS", 5, "audio/x-mpegts");
        addFileType("WEBM", 5, "audio/x-matroska");
        addFileType("OGG", 7, "audio/ogg");
        addFileType("MPGA", 1, "audio/mpeg");
        addFileType("RA", 10, "audio/x-pn-realaudio");
        addFileType("APE", 12, "audio/ffmpeg");
        addFileType("AAC", 8, "audio/ffmpeg");
        addFileType("MKA", 9, "audio/x-matroska");
        addFileType("MID", 100, "audio/midi");
        addFileType("MIDI", 100, "audio/midi");
        addFileType("XMF", 100, "audio/midi");
        addFileType("RTTTL", 100, "audio/midi");
        addFileType("SMF", 101, "audio/sp-midi");
        addFileType("IMY", 102, "audio/imelody");
        addFileType("RTX", 100, "audio/midi");
        addFileType("OTA", 100, "audio/midi");
        addFileType("MPEG", SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, "video/mpeg");
        addFileType("MP4", SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, "video/mp4");
        addFileType("M4V", 201, "video/mp4");
        addFileType("3GP", 202, "video/3gpp");
        addFileType("3GPP", 202, "video/3gpp");
        addFileType("3G2", 203, "video/3gpp2");
        addFileType("3GPP2", 203, "video/3gpp2");
        addFileType("MKV", 206, "video/x-matroska");
        addFileType("WEBM", 213, "video/webm");
        addFileType("M2TS", 207, "video/x-mpegts");
        addFileType("MPG", SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE, "video/mpeg");
        addFileType("RMVB", 207, "video/x-pn-realvideo");
        addFileType("MXMF", 100, "audio/midi");
        addFileType("AAC+", 100, "audio/midi");
        addFileType("EAAC+", 100, "audio/midi");
        addFileType("AALC+", 100, "audio/midi");
        addFileType("MP3", 1, "audio/mp3");
        addFileType("M4A", 2, "audio/m4a");
        addFileType("MOV", 207, "video/quicktime");
        addFileType("RM", 207, "video/x-pn-realvideo");
        addFileType("TS", 207, "video/x-mpegts");
        addFileType("AVI", 208, "video/avi");
        addFileType("F4V", 207, "video/mp4");
        addFileType("FLV", 207, "video/x-flv");
        addFileType("RM", 209, "video/x-pn-realvideo");
        addFileType("RV", 209, "video/x-pn-realvideo");
        addFileType("RMVB", 212, "video/x-pn-realvideo");
        if (isWMVEnabled()) {
            addFileType("WMV", 204, "video/x-ms-wmv");
            addFileType("ASF", 205, "video/x-ms-asf");
        }
        addFileType("JPG", VTMCDataCache.MAX_EXPIREDTIME, "image/jpeg");
        addFileType("JPEG", VTMCDataCache.MAX_EXPIREDTIME, "image/jpeg");
        addFileType("GIF", 301, "image/gif");
        addFileType("PNG", 302, "image/png");
        addFileType("BMP", 303, "image/x-ms-bmp");
        addFileType("WBMP", 304, "image/vnd.wap.wbmp");
        addFileType("WEBP", 305, "image/webp");
        addFileType("gif87a", 305, "image/png");
        addFileType("M3U", 400, "audio/x-mpegurl");
        addFileType("M3U", 400, "application/x-mpegurl");
        addFileType("PLS", 401, "audio/x-scpls");
        addFileType("WPL", 402, "application/vnd.ms-wpl");
        addFileType("M3U8", MsgUrlService.RESULT_TOKEN_FLASH, "application/vnd.apple.mpegurl");
        addFileType("M3U8", MsgUrlService.RESULT_TOKEN_FLASH, "audio/mpegurl");
        addFileType("M3U8", MsgUrlService.RESULT_TOKEN_FLASH, "audio/x-mpegurl");
        addFileType("FL", VTMCDataCache.MAXSIZE, "application/x-android-drm-fl");
        addFileType("TXT", 600, "text/html");
        addFileType("HTM", 601, "text/html");
        addFileType("HTML", 601, "text/html");
        addFileType("PDF", 602, "application/pdf");
        addFileType("DOC", 604, "application/msword");
        addFileType("XLS", 605, "application/vnd.ms-excel");
        addFileType("PPT", 606, "application/mspowerpoint");
        addFileType("FLAC", 11, "audio/flac");
        addFileType("ZIP", 607, "application/zip");
        addFileType("LOG", 608, "text/html");
        addFileType("BMP", 303, "image/bmp");
        addFileType("APK", 608, "application/vnd.android.package-archive");
        addFileType("VCF", 610, "vcard/vcf");
        addFileType("VCS", 611, "text/x-vcalendar");
        StringBuilder builder = new StringBuilder();
        for (String append : sFileTypeMap.keySet()) {
            if (builder.length() > 0) {
                builder.append(',');
            }
            builder.append(append);
        }
        sFileExtensions = builder.toString();
    }

    static void addFileType(String extension, int fileType, String mimeType) {
        sFileTypeMap.put(extension, new MediaFileType(fileType, mimeType));
        sMimeTypeMap.put(mimeType, Integer.valueOf(fileType));
    }

    private static boolean isWMAEnabled() {
        for (AudioDecoder decoder : DecoderCapabilities.getAudioDecoders()) {
            if (decoder == AudioDecoder.AUDIO_DECODER_WMA) {
                return true;
            }
        }
        return false;
    }

    private static boolean isWMVEnabled() {
        for (VideoDecoder decoder : DecoderCapabilities.getVideoDecoders()) {
            if (decoder == VideoDecoder.VIDEO_DECODER_WMV) {
                return true;
            }
        }
        return false;
    }

    public static boolean isAudioFileType(int fileType) {
        if (fileType >= 1 && fileType <= 13) {
            return true;
        }
        if (fileType < 100) {
            return false;
        }
        if (fileType > 102) {
            return false;
        }
        return true;
    }

    public static boolean isVideoFileType(int fileType) {
        if (fileType < SmartSmsSdkUtil.DUOQU_BUBBLE_DATA_CACHE_SIZE || fileType > 213) {
            return false;
        }
        return true;
    }

    public static boolean isImageFileType(int fileType) {
        if (fileType < VTMCDataCache.MAX_EXPIREDTIME || fileType > 305) {
            return false;
        }
        return true;
    }

    public static boolean isVCardFileType(int fileType) {
        return 610 == fileType;
    }

    public static boolean isVCalendarFileType(int fileType) {
        return 611 == fileType;
    }

    public static boolean isSpecialFile(String file, String fileformat) {
        int lastDot = file.lastIndexOf(".");
        if (lastDot < 0) {
            return false;
        }
        return fileformat.equals(file.substring(lastDot + 1));
    }

    public static MediaFileType getFileType(String path) {
        if (path == null || TextUtils.isEmpty(path)) {
            MLog.w("RcsMediaFileUtils FileTrans: ", "checkthe path = " + path + " TextUtils.isEmpty(path) =" + TextUtils.isEmpty(path));
            return null;
        }
        int lastDot = path.lastIndexOf(".");
        if (lastDot < 0) {
            return null;
        }
        return (MediaFileType) sFileTypeMap.get(path.substring(lastDot + 1).toUpperCase(Locale.getDefault()));
    }

    public static String getFileMimeType(String path) {
        MediaFileType fileType = getFileType(path);
        if (fileType != null) {
            return fileType.mimeType;
        }
        return "application/*";
    }

    public static String getFileExtensionByMimeType(String mimeType) {
        if (TextUtils.isEmpty(mimeType)) {
            return "";
        }
        for (Entry<String, MediaFileType> entry : sFileTypeMap.entrySet()) {
            if (((MediaFileType) entry.getValue()).mimeType.equals(mimeType)) {
                return ((String) entry.getKey()).toLowerCase(Locale.getDefault());
            }
        }
        return TextUtils.substring(mimeType, mimeType.indexOf("/") + 1, mimeType.length());
    }

    public static String saveMediaFile(Context context, File fromFile) {
        IOException e;
        Throwable th;
        String uri = null;
        String suffix = getFileSuffix(fromFile);
        if (suffix == null) {
            return null;
        }
        if (hasSdcard()) {
            File destDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).getAbsolutePath() + "/" + "RCS");
            FileInputStream fileInputStream = null;
            FileOutputStream fosto = null;
            try {
                if (!destDir.exists() && !destDir.mkdirs()) {
                    return null;
                }
                uri = destDir.getAbsolutePath();
                File toFile = new File(destDir.getAbsolutePath() + "/RCS" + getFileName() + suffix);
                if (!toFile.exists()) {
                    FileInputStream fosfrom = new FileInputStream(fromFile);
                    try {
                        FileOutputStream fosto2 = new FileOutputStream(toFile);
                        try {
                            byte[] bt = new byte[4096];
                            while (true) {
                                int c = fosfrom.read(bt);
                                if (c <= 0) {
                                    break;
                                }
                                fosto2.write(bt, 0, c);
                            }
                            context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(toFile)));
                            fosto = fosto2;
                            fileInputStream = fosfrom;
                        } catch (IOException e2) {
                            e = e2;
                            fosto = fosto2;
                            fileInputStream = fosfrom;
                            try {
                                e.printStackTrace();
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e3) {
                                        e3.printStackTrace();
                                    }
                                }
                                if (fosto != null) {
                                    try {
                                        fosto.close();
                                    } catch (IOException e32) {
                                        e32.printStackTrace();
                                    }
                                }
                                return uri;
                            } catch (Throwable th2) {
                                th = th2;
                                if (fileInputStream != null) {
                                    try {
                                        fileInputStream.close();
                                    } catch (IOException e322) {
                                        e322.printStackTrace();
                                    }
                                }
                                if (fosto != null) {
                                    try {
                                        fosto.close();
                                    } catch (IOException e3222) {
                                        e3222.printStackTrace();
                                    }
                                }
                                throw th;
                            }
                        } catch (Throwable th3) {
                            th = th3;
                            fosto = fosto2;
                            fileInputStream = fosfrom;
                            if (fileInputStream != null) {
                                fileInputStream.close();
                            }
                            if (fosto != null) {
                                fosto.close();
                            }
                            throw th;
                        }
                    } catch (IOException e4) {
                        e3222 = e4;
                        fileInputStream = fosfrom;
                        e3222.printStackTrace();
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fosto != null) {
                            fosto.close();
                        }
                        return uri;
                    } catch (Throwable th4) {
                        th = th4;
                        fileInputStream = fosfrom;
                        if (fileInputStream != null) {
                            fileInputStream.close();
                        }
                        if (fosto != null) {
                            fosto.close();
                        }
                        throw th;
                    }
                }
                if (fileInputStream != null) {
                    try {
                        fileInputStream.close();
                    } catch (IOException e32222) {
                        e32222.printStackTrace();
                    }
                }
                if (fosto != null) {
                    try {
                        fosto.close();
                    } catch (IOException e322222) {
                        e322222.printStackTrace();
                    }
                }
            } catch (IOException e5) {
                e322222 = e5;
                e322222.printStackTrace();
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fosto != null) {
                    fosto.close();
                }
                return uri;
            }
        }
        Toast.makeText(context, context.getString(R.string.no_sdcard), 0).show();
        return uri;
    }

    public static String getFileSuffix(File file) {
        if (file == null || !file.exists()) {
            return null;
        }
        int lastDot = file.getAbsolutePath().lastIndexOf(".");
        if (lastDot < 0) {
            return null;
        }
        return file.getAbsolutePath().substring(lastDot).toLowerCase(Locale.getDefault());
    }

    public static boolean hasSdcard() {
        if (Environment.getExternalStorageState().equals("mounted")) {
            return true;
        }
        return false;
    }

    public static String getFileName() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date(System.currentTimeMillis()));
    }

    public static void saveFileToDownLoad(Context context, String filePath) {
        if (filePath == null) {
            RcsTransaction.showFileSaveResult(context, null);
            MLog.w("RcsMediaFileUtils", "saveFileToDownLoad: filePath is null");
        } else if (filePath.lastIndexOf(".") < 0) {
            MLog.w("RcsMediaFileUtils", "saveFileToDownLoad: lastDot < 0");
            RcsTransaction.showFileSaveResult(context, null);
        } else {
            File file = new File(filePath);
            if (!file.exists()) {
                Toast.makeText(context, R.string.text_file_not_exist, 0).show();
            }
            RcsTransaction.showFileSaveResult(context, saveMediaFile(context, file));
        }
    }
}
