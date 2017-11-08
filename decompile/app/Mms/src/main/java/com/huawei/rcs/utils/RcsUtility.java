package com.huawei.rcs.utils;

import android.app.AlertDialog.Builder;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.Drawable;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.util.Log;
import android.util.LruCache;
import android.view.View;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.Toast;
import com.amap.api.maps.model.WeightedLatLng;
import com.amap.api.maps.offlinemap.OfflineMapStatus;
import com.android.mms.MmsConfig;
import com.android.mms.data.Contact;
import com.android.mms.data.Conversation;
import com.android.mms.data.WorkingMessage;
import com.android.mms.exif.ExifInterface;
import com.android.mms.ui.ComposeMessageFragment;
import com.android.mms.ui.MessageItem;
import com.android.mms.ui.MessageListAdapter;
import com.android.mms.ui.MessageListItem;
import com.android.mms.ui.MessageListView;
import com.android.mms.ui.MessageUtils;
import com.android.mms.ui.UriImage;
import com.android.mms.util.ShareUtils;
import com.android.mms.util.SmileyParser;
import com.android.mms.util.SmileyParser.SMILEY_TYPE;
import com.android.mms.util.VcardMessageHelper;
import com.android.rcs.RcsCommonConfig;
import com.android.vcard.VCardComposer;
import com.autonavi.amap.mapcore.MapTilsCacheAndResManager;
import com.google.android.gms.R;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import com.huawei.mms.util.NumberUtils;
import com.huawei.rcs.media.RcsMediaFileUtils;
import com.huawei.rcs.media.RcsMediaFileUtils.MediaFileType;
import com.huawei.rcs.ui.RcsFileTransMessageItem;
import com.huawei.rcs.ui.RcsFileTransMessageListItem;
import com.huawei.rcs.ui.RcsVCardInfo;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.Charset;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RcsUtility {
    public static final String FT_COMPRESSED_IMAGE_PATH = (FT_COMPRESSED_PARENT_PATH + File.separator + ".images");
    public static final String FT_COMPRESSED_PARENT_PATH = (Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "RCS" + File.separator + ".cache");
    public static final String FT_COMPRESSED_VIDEO_PATH = (FT_COMPRESSED_PARENT_PATH + File.separator + ".videos");
    private static final int MAX_FILE_SEND_SIZE = MmsConfig.getMaxSlides();
    public static final String RCSE_PICTURE_DIR = (Environment.getExternalStorageDirectory() + "/rcse/pictures/");
    private static boolean isRcsOn = RcsCommonConfig.isRCSSwitchOn();
    private static Object mGroupMemberCacheLock = new Object();
    private static LruCache<String, String> mGroupMemberNameCache = null;

    public static class FileInfo {
        private String fileDisplayName;
        private String mimeType;
        private String sendFilePath;
        private long totalSize;

        public String getFileDisplayName() {
            return this.fileDisplayName;
        }

        public void setFileDisplayName(String aFileDisplayName) {
            this.fileDisplayName = aFileDisplayName;
        }

        public String getMimeType() {
            return this.mimeType;
        }

        public void setMimeType(String aMimeType) {
            this.mimeType = aMimeType;
        }

        public String getSendFilePath() {
            return this.sendFilePath;
        }

        public void setSendFilePath(String sendFilePath) {
            this.sendFilePath = sendFilePath;
        }

        public long getTotalSize() {
            return this.totalSize;
        }

        public void setTotalSize(long totalSize) {
            this.totalSize = totalSize;
        }
    }

    public static String formatFileSizeWithoutUnit(long transSize, long totalSize) {
        double div = WeightedLatLng.DEFAULT_INTENSITY;
        int i = 0;
        while (((double) totalSize) / div > 1024.0d && i < 3) {
            div *= 1024.0d;
            i++;
        }
        return new DecimalFormat("0.0").format(((double) transSize) / div);
    }

    public static void compressImage(Context context, Uri imageUri, FileInfo fileInfo) {
        Throwable th;
        String oldPath = fileInfo.getSendFilePath();
        int index = oldPath.lastIndexOf(".");
        String path = getCacheDirPath(true) + "/" + (System.currentTimeMillis() + "") + "_tmp" + oldPath.substring(index);
        MLog.i("RcsUtility FileTrans: ", "Resized image tmp path = " + path);
        File file = new File(path);
        if (!file.exists()) {
            InputStream inputStream = null;
            int mWidth = 0;
            int mHeight = 0;
            try {
                inputStream = context.getContentResolver().openInputStream(imageUri);
                Options opt = new Options();
                opt.inJustDecodeBounds = true;
                BitmapFactory.decodeStream(inputStream, null, opt);
                mWidth = opt.outWidth;
                mHeight = opt.outHeight;
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e) {
                    }
                }
            } catch (FileNotFoundException e2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e3) {
                    }
                }
            } catch (Throwable th2) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
            }
            int widthLimit = MmsConfig.getMaxImageWidth();
            int heightLimit = MmsConfig.getMaxImageHeight();
            if (mHeight > mWidth) {
                int temp = widthLimit;
                widthLimit = heightLimit;
                heightLimit = temp;
            }
            MLog.i("RcsUtility FileTrans: ", "file transfer compressImage mWidth = " + mWidth + ", mHeight = " + mHeight);
            if (mWidth > widthLimit || mHeight > heightLimit) {
                byte[] data = getResizedImageData(mWidth, mHeight, -1, imageUri, context);
                if (data == null) {
                    MLog.i("RcsUtility FileTrans: ", "compressImage but data is null ,so return ");
                    return;
                }
                FileOutputStream fileOutputStream = null;
                try {
                    FileOutputStream fout = new FileOutputStream(path);
                    try {
                        fout.write(data);
                        fout.close();
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e5) {
                                MLog.e("RcsUtility FileTrans: ", "file close error");
                            }
                        }
                    } catch (FileNotFoundException e6) {
                        fileOutputStream = fout;
                        MLog.e("RcsUtility FileTrans: ", "FileNotFoundException error");
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e7) {
                                MLog.e("RcsUtility FileTrans: ", "file close error");
                            }
                        }
                        fileInfo.setFileDisplayName(file.getName());
                        fileInfo.setSendFilePath(file.getAbsolutePath());
                        fileInfo.setTotalSize(file.length());
                    } catch (IOException e8) {
                        fileOutputStream = fout;
                        MLog.e("RcsUtility FileTrans: ", "IOException error");
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e9) {
                                MLog.e("RcsUtility FileTrans: ", "file close error");
                            }
                        }
                        fileInfo.setFileDisplayName(file.getName());
                        fileInfo.setSendFilePath(file.getAbsolutePath());
                        fileInfo.setTotalSize(file.length());
                    } catch (RuntimeException e10) {
                        fileOutputStream = fout;
                        try {
                            MLog.e("RcsUtility FileTrans: ", "FileOutputStream error");
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e11) {
                                    MLog.e("RcsUtility FileTrans: ", "file close error");
                                }
                            }
                            fileInfo.setFileDisplayName(file.getName());
                            fileInfo.setSendFilePath(file.getAbsolutePath());
                            fileInfo.setTotalSize(file.length());
                        } catch (Throwable th3) {
                            th = th3;
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e12) {
                                    MLog.e("RcsUtility FileTrans: ", "file close error");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th4) {
                        th = th4;
                        fileOutputStream = fout;
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                } catch (FileNotFoundException e13) {
                    MLog.e("RcsUtility FileTrans: ", "FileNotFoundException error");
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fileInfo.setFileDisplayName(file.getName());
                    fileInfo.setSendFilePath(file.getAbsolutePath());
                    fileInfo.setTotalSize(file.length());
                } catch (IOException e14) {
                    MLog.e("RcsUtility FileTrans: ", "IOException error");
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fileInfo.setFileDisplayName(file.getName());
                    fileInfo.setSendFilePath(file.getAbsolutePath());
                    fileInfo.setTotalSize(file.length());
                } catch (RuntimeException e15) {
                    MLog.e("RcsUtility FileTrans: ", "FileOutputStream error");
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    fileInfo.setFileDisplayName(file.getName());
                    fileInfo.setSendFilePath(file.getAbsolutePath());
                    fileInfo.setTotalSize(file.length());
                }
            }
            if (!file.delete()) {
                MLog.i("RcsUtility FileTrans: ", "file.delete failed ");
            }
            return;
        }
        fileInfo.setFileDisplayName(file.getName());
        fileInfo.setSendFilePath(file.getAbsolutePath());
        fileInfo.setTotalSize(file.length());
    }

    public static int computeSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int initialSize = computeInitialSampleSize(options, minSideLength, maxNumOfPixels);
        if (initialSize > 8) {
            return ((initialSize + 7) / 8) * 8;
        }
        int roundedSize = 1;
        while (roundedSize < initialSize) {
            roundedSize <<= 1;
        }
        return roundedSize;
    }

    public static int computeInitialSampleSize(Options options, int minSideLength, int maxNumOfPixels) {
        int lowerBound;
        int upperBound;
        double w = (double) options.outWidth;
        double h = (double) options.outHeight;
        if (maxNumOfPixels == -1) {
            lowerBound = 1;
        } else {
            lowerBound = (int) Math.ceil(Math.sqrt((w * h) / ((double) maxNumOfPixels)));
        }
        if (minSideLength == -1) {
            upperBound = 128;
        } else {
            upperBound = (int) Math.min(Math.floor(w / ((double) minSideLength)), Math.floor(h / ((double) minSideLength)));
        }
        if (upperBound < lowerBound) {
            return lowerBound;
        }
        if (maxNumOfPixels == -1 && minSideLength == -1) {
            return 1;
        }
        if (minSideLength == -1) {
            return lowerBound;
        }
        return upperBound;
    }

    public static Bitmap toRoundCorner(Bitmap bitmap, int pixels) {
        if (bitmap == null) {
            return null;
        }
        Bitmap bitmap2 = null;
        try {
            bitmap2 = Bitmap.createBitmap(bitmap.getWidth(), bitmap.getHeight(), Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap2);
            Paint paint = new Paint();
            Rect rect = new Rect(0, 0, bitmap.getWidth(), bitmap.getHeight());
            RectF rectF = new RectF(rect);
            float roundPx = (float) pixels;
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            paint.setColor(-12434878);
            canvas.drawRoundRect(rectF, roundPx, roundPx, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            canvas.drawBitmap(bitmap, rect, rect, paint);
        } catch (OutOfMemoryError e) {
            try {
                Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
            } catch (InvocationTargetException e2) {
                MLog.e("RcsUtility", "InvocationTargetException error");
            } catch (ClassNotFoundException e3) {
                MLog.e("RcsUtility", "ClassNotFoundException error");
            } catch (IllegalAccessException e4) {
                MLog.e("RcsUtility", "IllegalAccessException error");
            } catch (NoSuchMethodException e5) {
                MLog.e("RcsUtility", "NoSuchMethodException error");
            } catch (RuntimeException e6) {
                MLog.e("RcsUtility", "RuntimeException error");
            }
        }
        bitmap.recycle();
        return bitmap2;
    }

    public static Bitmap composeFileTransIcon(Context context, String filePath, Bitmap bitmap, int iconType) {
        return composeFileTransIcon(context, filePath, bitmap, 240, 320, 40960, 3, iconType);
    }

    public static Bitmap composeFileTransIcon(Context context, String filePath, Bitmap bitmap, int width, int height, int maxSize, int pixels, int iconType) {
        if (TextUtils.isEmpty(filePath) && bitmap == null) {
            return null;
        }
        int widthLimit = width;
        int heightLimit = height;
        Bitmap tmp = null;
        if (bitmap == null) {
            if (!TextUtils.isEmpty(filePath)) {
                switch (iconType) {
                    case 4096:
                        try {
                            tmp = ThumbnailUtils.createVideoThumbnail(filePath, 0);
                            break;
                        } catch (OutOfMemoryError e) {
                            try {
                                Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
                                break;
                            } catch (InvocationTargetException e2) {
                                MLog.e("RcsUtility", "composeFileTransIcon occurs InvocationTargetException error");
                                break;
                            } catch (ClassNotFoundException e3) {
                                MLog.e("RcsUtility", "composeFileTransIcon occurs ClassNotFoundException error");
                                break;
                            } catch (IllegalAccessException e4) {
                                MLog.e("RcsUtility", "composeFileTransIcon occurs IllegalAccessException error");
                                break;
                            } catch (NoSuchMethodException e5) {
                                MLog.e("RcsUtility", "composeFileTransIcon occurs NoSuchMethodException error");
                                break;
                            } catch (RuntimeException e6) {
                                MLog.e("RcsUtility", "composeFileTransIcon occurs RuntimeException error");
                                break;
                            }
                        }
                    case 8192:
                        Options options = new Options();
                        options.inJustDecodeBounds = true;
                        options.inPreferredConfig = Config.RGB_565;
                        BitmapFactory.decodeFile(filePath, options);
                        options.inJustDecodeBounds = false;
                        options.inSampleSize = computeSampleSize(options, -1, width * height);
                        options.inDither = false;
                        options.inPurgeable = true;
                        options.inInputShareable = true;
                        tmp = BitmapFactory.decodeFile(filePath, options);
                        break;
                    default:
                        break;
                }
            }
        }
        tmp = bitmap;
        if (tmp == null) {
            return null;
        }
        Bitmap output;
        int decodeWidth = tmp.getWidth();
        int decodeHeight = tmp.getHeight();
        if (decodeHeight < decodeWidth) {
            int tmpLimit = width;
            widthLimit = height;
            heightLimit = width;
        }
        if (decodeHeight > heightLimit || decodeWidth > widthLimit) {
            try {
                int scaleFactor = (int) Math.ceil(Math.sqrt(((double) (decodeHeight * decodeWidth)) / ((double) (widthLimit * heightLimit))));
                Bitmap tempBitmap = tmp;
                tmp = Bitmap.createScaledBitmap(tempBitmap, decodeWidth / scaleFactor, decodeHeight / scaleFactor, false);
                MLog.i("RcsUtility FileTrans: ", "composeFileTransIcon tmp.size : " + tmp.getByteCount() + ", scaleFactor = " + scaleFactor + " widthLimit : " + widthLimit + " heightLimit : " + heightLimit + ", decodeWidth = " + decodeWidth + ", decodeHeight = " + decodeHeight);
                if (tempBitmap != tmp) {
                    tempBitmap.recycle();
                }
            } catch (OutOfMemoryError e7) {
                try {
                    Class.forName(System.class.getName()).getMethod("gc", new Class[0]).invoke(null, null);
                } catch (NoSuchMethodException e8) {
                    MLog.e("RcsUtility", "setExtracting NoSuchMethodException" + e7);
                } catch (IllegalAccessException e9) {
                    MLog.e("RcsUtility", "setExtracting IllegalAccessException" + e7);
                } catch (IllegalArgumentException e10) {
                    MLog.e("RcsUtility", "setExtracting IllegalArgumentException" + e7);
                } catch (InvocationTargetException e11) {
                    MLog.e("RcsUtility", "setExtracting InvocationTargetException" + e7);
                } catch (Exception e12) {
                    MLog.e("RcsUtility", "setExtracting unknow Exception" + e7);
                }
            }
        }
        if (tmp != null) {
            output = fixRotateBitmap(filePath, getCutBitmap(tmp, context));
        } else {
            MLog.w("RcsUtility", "output bitmap is null");
            output = null;
        }
        return output;
    }

    private static Bitmap getCutBitmap(Bitmap compressBitmap, Context context) {
        int w = compressBitmap.getWidth();
        int h = compressBitmap.getHeight();
        Bitmap bitmap = null;
        int viewWidth = MessageUtils.getAttachWidthAndHeight(context);
        int viewHeight = viewWidth;
        float scale = Math.max(((float) viewWidth) / ((float) w), ((float) viewWidth) / ((float) h));
        try {
            bitmap = Bitmap.createBitmap(viewWidth, viewWidth, Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);
            canvas.translate(((float) viewWidth) / 2.0f, ((float) viewWidth) / 2.0f);
            canvas.rotate(0.0f);
            canvas.scale(scale, scale);
            canvas.drawBitmap(compressBitmap, ((float) (-w)) / 2.0f, ((float) (-h)) / 2.0f, new Paint(6));
        } catch (Throwable thow) {
            Log.e("RcsUtility", " compress getCutBitmap cutBitmap err!!!");
            thow.printStackTrace();
        }
        compressBitmap.recycle();
        return bitmap;
    }

    public static Bitmap drawableToBitmap(Drawable drawable) {
        Config config;
        int w = drawable.getIntrinsicWidth();
        int h = drawable.getIntrinsicHeight();
        if (drawable.getOpacity() != -1) {
            config = Config.ARGB_8888;
        } else {
            config = Config.RGB_565;
        }
        Bitmap bitmap = Bitmap.createBitmap(w, h, config);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, w, h);
        drawable.draw(canvas);
        return bitmap;
    }

    public static List<Uri> getUriFromIntent(Intent intent) {
        List<Uri> uriList = new ArrayList();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            if (extras.get("android.intent.extra.STREAM") == null) {
                uriList.add(((Intent) extras.get("BODY")).getData());
            } else {
                String uriString = extras.get("android.intent.extra.STREAM").toString();
                if (uriString.startsWith("[")) {
                    uriString = uriString.substring(1, uriString.length() - 1);
                }
                String[] uriStrs = uriString.split(",");
                for (String trim : uriStrs) {
                    uriList.add(Uri.parse(trim.trim()));
                }
            }
        }
        return uriList;
    }

    public static List<Uri> getUriFromIntent(Context context, Intent intent) {
        List<Uri> uriList = new ArrayList();
        Bundle extras = intent.getExtras();
        if (extras == null) {
            return uriList;
        }
        if (extras.get("android.intent.extra.STREAM") == null) {
            uriList.add(((Intent) extras.get("BODY")).getData());
        } else {
            String uriString = extras.get("android.intent.extra.STREAM").toString();
            if (uriString.startsWith("[")) {
                uriString = uriString.substring(1, uriString.length() - 1);
            }
            String[] uriStrs = uriString.split(",");
            int i;
            if (uriStrs.length > MAX_FILE_SEND_SIZE) {
                Toast.makeText(context, context.getResources().getQuantityString(R.plurals.too_many_files_Toast, MAX_FILE_SEND_SIZE, new Object[]{Integer.valueOf(MAX_FILE_SEND_SIZE)}), 1).show();
                for (i = 0; i < MAX_FILE_SEND_SIZE; i++) {
                    if (ShareUtils.isFileProviderImageType(uriStrs[i].trim())) {
                        uriList.add(ShareUtils.copyFile(context, Uri.parse(uriStrs[i].trim()), "shared_image_file.png"));
                    } else {
                        uriList.add(Uri.parse(uriStrs[i].trim()));
                    }
                }
            } else {
                for (i = 0; i < uriStrs.length; i++) {
                    if (ShareUtils.isFileProviderImageType(uriStrs[i].trim())) {
                        uriList.add(ShareUtils.copyFile(context, Uri.parse(uriStrs[i].trim()), "shared_image_file.png"));
                    } else {
                        uriList.add(Uri.parse(uriStrs[i].trim()));
                    }
                }
            }
        }
        return uriList;
    }

    public static void delExcLimitUri(Context context, ArrayList<Uri> mUriList) {
        int size = mUriList.size();
        if (size > MAX_FILE_SEND_SIZE) {
            Toast.makeText(context, context.getResources().getQuantityString(R.plurals.too_many_files_Toast, MAX_FILE_SEND_SIZE, new Object[]{Integer.valueOf(MAX_FILE_SEND_SIZE)}), 1).show();
            mUriList.subList(MAX_FILE_SEND_SIZE, size).clear();
        }
    }

    public static RcsFileTransMessageListItem getMessageListItemById(long fileTransId, MessageListView mMsgListView) {
        int listSize = mMsgListView.getChildCount();
        for (int i = 0; i < listSize; i++) {
            RcsFileTransMessageListItem rcsFileTransMessageListItem = null;
            if (((MessageListItem) mMsgListView.getChildAt(i)) instanceof RcsFileTransMessageListItem) {
                rcsFileTransMessageListItem = (RcsFileTransMessageListItem) mMsgListView.getChildAt(i);
            }
            if (rcsFileTransMessageListItem != null && rcsFileTransMessageListItem.getFileTransMessageItem().mMsgId == fileTransId) {
                return rcsFileTransMessageListItem;
            }
        }
        return null;
    }

    public static void updateItemAnyStatusWithMsgId(long MsgId, int status, MessageListView mMsgListView, ComposeMessageFragment fragment) {
        RcsFileTransMessageItem aFtMsgItem;
        RcsFileTransMessageListItem view = getMessageListItemById(MsgId, mMsgListView);
        if (view != null) {
            aFtMsgItem = view.getFileTransMessageItem();
        } else {
            aFtMsgItem = getMessageItemWithMsgId(MsgId, fragment);
        }
        if (aFtMsgItem != null) {
            aFtMsgItem.mImAttachmentStatus = status;
        }
    }

    public static RcsFileTransMessageItem getMessageItemWithMsgId(long msgId, ComposeMessageFragment fragment) {
        if (fragment == null || fragment.getRcsComposeMessage() == null) {
            return null;
        }
        return getMessageItem(fragment.getRcsComposeMessage().getComposeMessageListAdapter(), "chat", msgId, true);
    }

    public static RcsFileTransMessageItem getMessageItem(MessageListAdapter mMsgListAdapter, String type, long msgId, boolean createFromCursorIfNotInCache) {
        Cursor cursor;
        if (createFromCursorIfNotInCache) {
            cursor = mMsgListAdapter.getCursor();
        } else {
            cursor = null;
        }
        MessageItem msgItem = mMsgListAdapter.getCachedMessageItem(type, msgId, cursor);
        if (msgItem == null || !(msgItem instanceof RcsFileTransMessageItem)) {
            return null;
        }
        return (RcsFileTransMessageItem) msgItem;
    }

    public static boolean resolutionVerity4Video(String filePath) {
        boolean z = false;
        try {
            MediaMetadataRetriever mmr = new MediaMetadataRetriever();
            mmr.setDataSource(filePath);
            String bitrate = mmr.extractMetadata(20);
            int width = Integer.parseInt(mmr.extractMetadata(18));
            int height = Integer.parseInt(mmr.extractMetadata(19));
            MLog.i("RcsUtility FileTrans: ", "resolutionVerity4Video Bitrate : " + bitrate + " width : " + width + " height : " + height);
            if (width > 640 && height > 480) {
                z = true;
            }
            return z;
        } catch (RuntimeException e) {
            MLog.e("RcsUtility FileTrans: ", "Can't get correct video resolution from filePath : ");
            return false;
        }
    }

    public static List<Uri> divideVideoList(Context context, List<Uri> uriList) {
        List<Uri> videoList = new ArrayList();
        int i = 0;
        while (i < uriList.size()) {
            Uri uri = (Uri) uriList.get(i);
            FileInfo info = RcsTransaction.getFileInfoByData(context, uri);
            if (info != null && info.getMimeType().contains("video")) {
                videoList.add(uri);
                uriList.remove(uri);
                i--;
            }
            i++;
        }
        return videoList;
    }

    public static String getCacheDirPath(boolean isImage) {
        String dirPath = isImage ? FT_COMPRESSED_IMAGE_PATH : FT_COMPRESSED_VIDEO_PATH;
        try {
            File rcsDir = new File(dirPath);
            boolean created = false;
            if (rcsDir.exists() && rcsDir.isDirectory()) {
                created = true;
            } else if (rcsDir.mkdirs()) {
                created = true;
            } else {
                MLog.i("RcsUtility FileTrans: ", "create rcsDir " + dirPath + " failed!");
            }
            if (created) {
                File nomediaFile = new File(dirPath + File.separator + ".nomedia");
                if (!nomediaFile.exists() && nomediaFile.createNewFile()) {
                    MLog.i("RcsUtility FileTrans: ", "create nomediaFile success!");
                }
            }
        } catch (IOException e) {
            MLog.e("RcsUtility FileTrans: ", "create .nomedia file failed. -> " + e.getMessage());
        }
        return dirPath;
    }

    public static File createNewFileByCopyOldFile(File fileIn, Context context) {
        IOException e;
        Throwable th;
        boolean result = false;
        FileOutputStream fileOutputStream = null;
        FileInputStream fin = null;
        String dir = "";
        String msg = "";
        if (fileIn == null) {
            return null;
        }
        String fileName = fileIn.getName();
        String extension = "";
        int index = fileName.lastIndexOf(".");
        dir = Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_PICTURES + "/";
        if (index == -1) {
            extension = "jpg";
        } else {
            extension = fileName.substring(index + 1, fileName.length());
        }
        File fileOut = new File(dir + System.currentTimeMillis() + "." + extension);
        File parentFile = fileOut.getParentFile();
        if (parentFile == null) {
            return null;
        }
        if (!parentFile.exists() && !parentFile.mkdirs()) {
            return null;
        }
        try {
            FileOutputStream fout = new FileOutputStream(fileOut);
            try {
                FileInputStream fin2 = new FileInputStream(fileIn);
                try {
                    byte[] buffer = new byte[8000];
                    while (true) {
                        int size = fin2.read(buffer);
                        if (size == -1) {
                            break;
                        }
                        fout.write(buffer, 0, size);
                    }
                    result = true;
                    context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(fileOut)));
                    if (fout != null) {
                        try {
                            fout.close();
                        } catch (IOException e2) {
                            MLog.e("RcsUtility FileTrans: ", "Exception fout caught while opening or reading stream" + e2);
                            if (fin2 != null) {
                                try {
                                    fin2.close();
                                } catch (IOException e22) {
                                    MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e22);
                                }
                            }
                        } catch (Throwable th2) {
                            if (fin2 != null) {
                                try {
                                    fin2.close();
                                } catch (IOException e222) {
                                    MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e222);
                                }
                            }
                        }
                    }
                    if (fin2 != null) {
                        try {
                            fin2.close();
                        } catch (IOException e2222) {
                            MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e2222);
                        }
                    }
                } catch (IOException e3) {
                    e2222 = e3;
                    fin = fin2;
                    fileOutputStream = fout;
                    try {
                        MLog.e("RcsUtility FileTrans: ", "IOException caught while opening or reading stream " + e2222);
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e22222) {
                                MLog.e("RcsUtility FileTrans: ", "Exception fout caught while opening or reading stream" + e22222);
                                if (fin != null) {
                                    try {
                                        fin.close();
                                    } catch (IOException e222222) {
                                        MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e222222);
                                    }
                                }
                            } catch (Throwable th3) {
                                if (fin != null) {
                                    try {
                                        fin.close();
                                    } catch (IOException e2222222) {
                                        MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e2222222);
                                    }
                                }
                            }
                        }
                        if (fin != null) {
                            try {
                                fin.close();
                            } catch (IOException e22222222) {
                                MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e22222222);
                            }
                        }
                        if (result) {
                            msg = context.getResources().getString(R.string.copy_to_sdcard_success_Toast) + dir;
                        } else {
                            msg = context.getResources().getString(R.string.copy_to_sdcard_fail_Toast);
                        }
                        Toast.makeText(context, msg, 0).show();
                        return fileOut;
                    } catch (Throwable th4) {
                        th = th4;
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException e222222222) {
                                MLog.e("RcsUtility FileTrans: ", "Exception fout caught while opening or reading stream" + e222222222);
                                if (fin != null) {
                                    try {
                                        fin.close();
                                    } catch (IOException e2222222222) {
                                        MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e2222222222);
                                    }
                                }
                            } catch (Throwable th5) {
                                if (fin != null) {
                                    try {
                                        fin.close();
                                    } catch (IOException e22222222222) {
                                        MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e22222222222);
                                    }
                                }
                            }
                        }
                        if (fin != null) {
                            try {
                                fin.close();
                            } catch (IOException e222222222222) {
                                MLog.e("RcsUtility FileTrans: ", "Exception fin caught while opening or reading stream" + e222222222222);
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th6) {
                    th = th6;
                    fin = fin2;
                    fileOutputStream = fout;
                    if (fileOutputStream != null) {
                        fileOutputStream.close();
                    }
                    if (fin != null) {
                        fin.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222222222222 = e4;
                fileOutputStream = fout;
                MLog.e("RcsUtility FileTrans: ", "IOException caught while opening or reading stream " + e222222222222);
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (fin != null) {
                    fin.close();
                }
                if (result) {
                    msg = context.getResources().getString(R.string.copy_to_sdcard_success_Toast) + dir;
                } else {
                    msg = context.getResources().getString(R.string.copy_to_sdcard_fail_Toast);
                }
                Toast.makeText(context, msg, 0).show();
                return fileOut;
            } catch (Throwable th7) {
                th = th7;
                fileOutputStream = fout;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                if (fin != null) {
                    fin.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            e222222222222 = e5;
            MLog.e("RcsUtility FileTrans: ", "IOException caught while opening or reading stream " + e222222222222);
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (fin != null) {
                fin.close();
            }
            if (result) {
                msg = context.getResources().getString(R.string.copy_to_sdcard_fail_Toast);
            } else {
                msg = context.getResources().getString(R.string.copy_to_sdcard_success_Toast) + dir;
            }
            Toast.makeText(context, msg, 0).show();
            return fileOut;
        }
        if (result) {
            msg = context.getResources().getString(R.string.copy_to_sdcard_success_Toast) + dir;
        } else {
            msg = context.getResources().getString(R.string.copy_to_sdcard_fail_Toast);
        }
        Toast.makeText(context, msg, 0).show();
        return fileOut;
    }

    public static int getFileTransType(String mAttachmentPath) {
        if (mAttachmentPath == null) {
            MLog.w("RcsUtility FileTrans: ", "getFileTransType but mAttachmentPath is null,return 0 ");
            return 0;
        }
        MediaFileType fileType = RcsMediaFileUtils.getFileType(mAttachmentPath);
        if (fileType != null) {
            if (RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                return 9;
            }
            if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                return 7;
            }
            if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                return 8;
            }
            if (RcsMediaFileUtils.isVCardFileType(fileType.fileType)) {
                return 10;
            }
        }
        MLog.w("RcsUtility FileTrans: ", "getFileTransType return 0 ");
        return 0;
    }

    public static boolean handleAddVCard(Context context, Intent data, String tmpFileName) {
        IOException e;
        Throwable th;
        boolean result = false;
        if (data == null || data.getData() == null || context == null) {
            return false;
        }
        context.deleteFile(tmpFileName);
        ArrayList<Uri> uriList = data.getParcelableArrayListExtra("SelItemData_KeyValue");
        if (uriList == null) {
            uriList = new ArrayList();
            uriList.add(data.getData());
        }
        VCardComposer vCardComposer = null;
        FileOutputStream outputStream = null;
        try {
            VCardComposer composer = new VCardComposer(context, -1073741824, true);
            try {
                outputStream = context.openFileOutput(tmpFileName, 32768);
                StringBuffer selection = new StringBuffer("_id");
                selection.append(" in (");
                for (Uri uri : uriList) {
                    selection.append(uri.getLastPathSegment()).append(",");
                }
                selection.deleteCharAt(selection.length() - 1).append(")");
                if (!composer.init(selection.toString(), null)) {
                    MLog.i("RcsUtility FileTrans: ", "VCardComposer init failed");
                    if (composer != null) {
                        composer.terminate();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e2) {
                            Log.e("RcsUtility FileTrans: ", Log.getStackTraceString(e2));
                        }
                    }
                    return false;
                } else if (composer.getCount() == 0) {
                    Log.i("RcsUtility FileTrans: ", " VCardComposer.getCount() == 0");
                    if (composer != null) {
                        composer.terminate();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e22) {
                            Log.e("RcsUtility FileTrans: ", Log.getStackTraceString(e22));
                        }
                    }
                    return false;
                } else {
                    while (!composer.isAfterLast()) {
                        outputStream.write(VcardMessageHelper.filterVcardNumbers(composer.createOneEntry()).getBytes(Charset.defaultCharset()));
                    }
                    result = true;
                    if (composer != null) {
                        composer.terminate();
                    }
                    if (outputStream != null) {
                        try {
                            outputStream.close();
                        } catch (IOException e222) {
                            Log.e("RcsUtility FileTrans: ", Log.getStackTraceString(e222));
                        }
                    }
                    return result;
                }
            } catch (IOException e3) {
                e222 = e3;
                vCardComposer = composer;
            } catch (Throwable th2) {
                th = th2;
                vCardComposer = composer;
            }
        } catch (IOException e4) {
            e222 = e4;
            try {
                Log.e("RcsUtility FileTrans: ", "composer.createOneEntry() failed : " + Log.getStackTraceString(e222));
                if (vCardComposer != null) {
                    vCardComposer.terminate();
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e2222) {
                        Log.e("RcsUtility FileTrans: ", Log.getStackTraceString(e2222));
                    }
                }
                return result;
            } catch (Throwable th3) {
                th = th3;
                if (vCardComposer != null) {
                    vCardComposer.terminate();
                }
                if (outputStream != null) {
                    try {
                        outputStream.close();
                    } catch (IOException e22222) {
                        Log.e("RcsUtility FileTrans: ", Log.getStackTraceString(e22222));
                    }
                }
                throw th;
            }
        }
    }

    public static Intent handleVcardForFT(Context context, String tmpFileName) {
        Uri fileuri;
        Intent intent;
        Throwable th;
        String inputPath = context.getFileStreamPath(tmpFileName).getAbsolutePath();
        File outputFile = RcsTransaction.getOutputVcardFile();
        String outputPath = null;
        InputStream inputStream = null;
        FileOutputStream fs = null;
        if (outputFile != null) {
            outputPath = outputFile.getAbsolutePath();
        }
        if (outputPath == null) {
            MLog.w("RcsUtility FileTrans: ", "handleVcardForFT error, outputPath is null");
            return null;
        }
        int bytesum = 0;
        try {
            if (new File(inputPath).exists()) {
                InputStream inStream = new FileInputStream(inputPath);
                try {
                    FileOutputStream fs2 = new FileOutputStream(outputPath);
                    try {
                        byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                        while (true) {
                            int byteread = inStream.read(buffer);
                            if (byteread == -1) {
                                break;
                            }
                            bytesum += byteread;
                            fs2.write(buffer, 0, byteread);
                        }
                        inStream.close();
                        fs2.close();
                        fs = fs2;
                        inputStream = inStream;
                    } catch (IOException e) {
                        fs = fs2;
                        inputStream = inStream;
                        try {
                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT error");
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2) {
                                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close FileStream failed");
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e3) {
                                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                        }
                                    }
                                } catch (Throwable th2) {
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e4) {
                                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                        }
                                    }
                                }
                            }
                            if (fs != null) {
                                try {
                                    fs.close();
                                } catch (IOException e5) {
                                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                }
                            }
                            fileuri = Uri.fromFile(outputFile);
                            MLog.d("RcsUtility FileTrans: ", "handleVcardForFT fileuri = " + fileuri);
                            intent = new Intent();
                            intent.setData(fileuri);
                            return intent;
                        } catch (Throwable th3) {
                            th = th3;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e6) {
                                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close FileStream failed");
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e7) {
                                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                        }
                                    }
                                } catch (Throwable th4) {
                                    if (fs != null) {
                                        try {
                                            fs.close();
                                        } catch (IOException e8) {
                                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                        }
                                    }
                                }
                            }
                            if (fs != null) {
                                try {
                                    fs.close();
                                } catch (IOException e9) {
                                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th5) {
                        th = th5;
                        fs = fs2;
                        inputStream = inStream;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fs != null) {
                            fs.close();
                        }
                        throw th;
                    }
                } catch (IOException e10) {
                    inputStream = inStream;
                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT error");
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fs != null) {
                        fs.close();
                    }
                    fileuri = Uri.fromFile(outputFile);
                    MLog.d("RcsUtility FileTrans: ", "handleVcardForFT fileuri = " + fileuri);
                    intent = new Intent();
                    intent.setData(fileuri);
                    return intent;
                } catch (Throwable th6) {
                    th = th6;
                    inputStream = inStream;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fs != null) {
                        fs.close();
                    }
                    throw th;
                }
            }
            if (inputStream != null) {
                try {
                    inputStream.close();
                } catch (IOException e11) {
                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close FileStream failed");
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e12) {
                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                        }
                    }
                } catch (Throwable th7) {
                    if (fs != null) {
                        try {
                            fs.close();
                        } catch (IOException e13) {
                            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                        }
                    }
                }
            }
            if (fs != null) {
                try {
                    fs.close();
                } catch (IOException e14) {
                    MLog.e("RcsUtility FileTrans: ", "handleVcardForFT close fs failed");
                }
            }
        } catch (IOException e15) {
            MLog.e("RcsUtility FileTrans: ", "handleVcardForFT error");
            if (inputStream != null) {
                inputStream.close();
            }
            if (fs != null) {
                fs.close();
            }
            fileuri = Uri.fromFile(outputFile);
            MLog.d("RcsUtility FileTrans: ", "handleVcardForFT fileuri = " + fileuri);
            intent = new Intent();
            intent.setData(fileuri);
            return intent;
        }
        fileuri = Uri.fromFile(outputFile);
        MLog.d("RcsUtility FileTrans: ", "handleVcardForFT fileuri = " + fileuri);
        intent = new Intent();
        intent.setData(fileuri);
        return intent;
    }

    public static byte[] getResizedImageData(int width, int height, int byteLimit, Uri uri, Context context) {
        Throwable e;
        Throwable e2;
        Throwable e3;
        Throwable th;
        int[] des = RcsScaleUtils.calculateResolution(width, height);
        int outWidth = des[0];
        int outHeight = des[1];
        int quality = RcsScaleUtils.calculateQuality(RcsScaleUtils.calculateSampleSize(width, height, outWidth, outHeight), des);
        int orientation = 0;
        if (!ShareUtils.isFileProviderImageType(uri.toString())) {
            orientation = UriImage.getOrientation(context, uri);
        }
        MLog.i("RcsUtility", "ExifInterface orientation:" + orientation);
        InputStream inputStream = null;
        ByteArrayOutputStream byteArrayOutputStream = null;
        try {
            inputStream = context.getContentResolver().openInputStream(uri);
            Bitmap unscaledBitmap = RcsScaleUtils.decodeResource(inputStream, width, height, outWidth, outHeight);
            if (unscaledBitmap == null) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e4) {
                    }
                }
                return null;
            }
            Bitmap scaledBitmap = RcsScaleUtils.createScaledBitmap(unscaledBitmap, outWidth, outHeight);
            unscaledBitmap.recycle();
            if (scaledBitmap == null) {
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e5) {
                    }
                }
                return null;
            }
            if (orientation != 0) {
                scaledBitmap = UriImage.rotateBitmap(scaledBitmap, orientation);
            }
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            try {
                scaledBitmap.compress(CompressFormat.JPEG, quality, os);
                scaledBitmap.recycle();
                byte[] toByteArray = os.toByteArray();
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e6) {
                    }
                }
                if (os != null) {
                    try {
                        os.close();
                    } catch (IOException e7) {
                    }
                }
                return toByteArray;
            } catch (OutOfMemoryError e8) {
                e = e8;
                byteArrayOutputStream = os;
                MLog.e("RcsUtility FileTrans: ", e.getMessage(), e);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e9) {
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e10) {
                    }
                }
                return null;
            } catch (FileNotFoundException e11) {
                e2 = e11;
                byteArrayOutputStream = os;
                MLog.e("RcsUtility FileTrans: ", e2.getMessage(), e2);
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e12) {
                    }
                }
                if (byteArrayOutputStream != null) {
                    try {
                        byteArrayOutputStream.close();
                    } catch (IOException e13) {
                    }
                }
                return null;
            } catch (IOException e14) {
                e3 = e14;
                byteArrayOutputStream = os;
                try {
                    MLog.e("RcsUtility FileTrans: ", e3.getMessage(), e3);
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e15) {
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e16) {
                        }
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e17) {
                        }
                    }
                    if (byteArrayOutputStream != null) {
                        try {
                            byteArrayOutputStream.close();
                        } catch (IOException e18) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                byteArrayOutputStream = os;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (byteArrayOutputStream != null) {
                    byteArrayOutputStream.close();
                }
                throw th;
            }
        } catch (OutOfMemoryError e19) {
            e = e19;
            MLog.e("RcsUtility FileTrans: ", e.getMessage(), e);
            if (inputStream != null) {
                inputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (FileNotFoundException e20) {
            e2 = e20;
            MLog.e("RcsUtility FileTrans: ", e2.getMessage(), e2);
            if (inputStream != null) {
                inputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        } catch (IOException e21) {
            e3 = e21;
            MLog.e("RcsUtility FileTrans: ", e3.getMessage(), e3);
            if (inputStream != null) {
                inputStream.close();
            }
            if (byteArrayOutputStream != null) {
                byteArrayOutputStream.close();
            }
            return null;
        }
    }

    public static boolean isRCSFileTypeInvalid(Context context, int requestCode, int chatType, Intent data) {
        if (data == null || (!RcseMmsExt.isRcsMode() && chatType == 1)) {
            return false;
        }
        Uri uri = data.getData();
        if (uri == null) {
            MLog.d("RcsUtility FileTrans: ", "checkRCSFileType uri is null ");
            return false;
        }
        FileInfo fileinfo = RcsTransaction.getFileInfoByData(context, uri);
        if (fileinfo == null) {
            MLog.d("RcsUtility FileTrans: ", "checkRCSFileType fileinfo is null ");
            return false;
        }
        String filepath = fileinfo.getSendFilePath();
        if (filepath == null) {
            MLog.d("RcsUtility FileTrans: ", "checkRCSFileType filepath is null ");
            return false;
        }
        MediaFileType fileType = RcsMediaFileUtils.getFileType(filepath);
        if (fileType == null) {
            MLog.d("RcsUtility FileTrans: ", " checkRCSFileType fileType is null ");
            return false;
        }
        switch (requestCode) {
            case 102:
                if (!RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                    Toast.makeText(context, R.string.invalid_file_format_Toast, 1).show();
                    return true;
                }
                break;
            case LocationRequest.PRIORITY_LOW_POWER /*104*/:
                if (!RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                    Toast.makeText(context, R.string.invalid_file_format_Toast, 1).show();
                    return true;
                }
                break;
            default:
                MLog.d("RcsUtility FileTrans: ", "checkRCSFileType default ");
                return false;
        }
        return false;
    }

    public static void scrollWhenMsgFailed(Bundle bundle, final ListView listView, Cursor cursor) {
        if (bundle == null || listView == null || cursor == null || cursor.getCount() <= 0) {
            MLog.d("RcsUtility FileTrans: ", "scrollWhenMsgFailed -> one of the params is null.");
            return;
        }
        long fileTransId = bundle.getLong("ft.msg_id");
        String ft_status = bundle.getString("rcs.ft.status");
        MLog.d("RcsUtility FileTrans: ", "scrollWhenMsgFailed -> fileTransId = " + fileTransId + ", ft_status = " + ft_status);
        if ("send_failed".equals(ft_status)) {
            int firstItemVisible = listView.getFirstVisiblePosition();
            int lastItemVisible = listView.getLastVisiblePosition();
            int posBefore = cursor.getPosition();
            for (int i = 0; i < cursor.getCount(); i++) {
                cursor.moveToPosition(i);
                if (cursor.getLong(cursor.getColumnIndexOrThrow("_id")) == fileTransId) {
                    final int position = cursor.getPosition();
                    MLog.d("RcsUtility FileTrans: ", "scrollWhenMsgFailed -> position = " + position + ", firstItemVisible = " + firstItemVisible + ", lastItemVisible = " + lastItemVisible);
                    if (position <= lastItemVisible && position > firstItemVisible) {
                        listView.post(new Runnable() {
                            public void run() {
                                listView.smoothScrollToPosition(position);
                            }
                        });
                    }
                    cursor.moveToPosition(posBefore);
                }
            }
            cursor.moveToPosition(posBefore);
        }
    }

    public static void addConversationSmileySpans(CharSequence text, SMILEY_TYPE smileyType, SpannableStringBuilder builder, Conversation conversation, SmileyParser sp, Context context, boolean isCannotConvert) {
        builder.clear();
        Object lastAdd = null;
        if (!(conversation == null || conversation.getHwCust() == null)) {
            lastAdd = conversation.getHwCust().getLastMessageFromName(conversation, context);
        }
        String separate = context.getResources().getString(R.string.rcs_group_last_message_from_separate);
        int baseCount = 0;
        if (!(lastAdd == null || separate == null)) {
            builder.append(lastAdd);
            builder.append(separate);
            baseCount = lastAdd.length() + separate.length();
        }
        builder.append(text);
        if (sp.getHwCust() != null) {
            if (isCannotConvert) {
                MLog.d("RcsUtility FileTrans: ", "addConversationSmileySpans isCannotConvert is ture");
                return;
            }
            Pattern mPattern = sp.getHwCust().getPattern();
            HashMap<String, Integer> mSmileyToRes = sp.getHwCust().getSmileyToRes();
            if (mPattern != null && mSmileyToRes != null) {
                Matcher matcher = mPattern.matcher(text);
                float fontScale = context.getResources().getConfiguration().fontScale;
                while (matcher.find()) {
                    String faceText = matcher.group();
                    int resId = ((Integer) mSmileyToRes.get(faceText)).intValue();
                    Object obj = faceText;
                    if (resId > 0) {
                        Object tmp = sp.createImageSpan(resId, smileyType, fontScale);
                        if (tmp != null) {
                            obj = tmp;
                        }
                    }
                    builder.setSpan(obj, matcher.start() + baseCount, matcher.end() + baseCount, 33);
                }
            }
        }
    }

    public static void showUserFtNoNeedVardDialog(List<Uri> uriList, List<String> addrList, Uri uri, Bundle bundle, int chatType, Context context, ComposeMessageFragment fragment, ArrayList<String> addresses, WorkingMessage workingMessage) {
        final SharedPreferences pref = context.getSharedPreferences(MapTilsCacheAndResManager.AUTONAVI_DATA_PATH, 0);
        if (pref.getBoolean("no_need_dialog_for_vcf", false)) {
            confimSendVcf(uriList, addrList, uri, bundle, chatType, context, fragment, addresses, workingMessage);
            return;
        }
        View view = View.inflate(context, R.layout.rcs_ft_send_notice_dialog, null);
        final CheckBox checkbox = (CheckBox) view.findViewById(R.id.vcard_not_ask_me);
        final List<Uri> list = uriList;
        final List<String> list2 = addrList;
        final Uri uri2 = uri;
        final Bundle bundle2 = bundle;
        final int i = chatType;
        final Context context2 = context;
        final ComposeMessageFragment composeMessageFragment = fragment;
        final ArrayList<String> arrayList = addresses;
        final WorkingMessage workingMessage2 = workingMessage;
        final List<Uri> list3 = uriList;
        uri2 = uri;
        final int i2 = chatType;
        final Context context3 = context;
        final ComposeMessageFragment composeMessageFragment2 = fragment;
        new Builder(context).setTitle(R.string.mms_remind_title).setMessage(R.string.rcs_im_send_contacts_note).setView(view).setPositiveButton(R.string.nickname_dialog_confirm, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                if (checkbox.isChecked()) {
                    Editor editor = pref.edit();
                    editor.putBoolean("no_need_dialog_for_vcf", true);
                    editor.commit();
                }
                RcsUtility.confimSendVcf(list, list2, uri2, bundle2, i, context2, composeMessageFragment, arrayList, workingMessage2);
                dialog.dismiss();
            }
        }).setNegativeButton(R.string.nickname_dialog_cancel, new OnClickListener() {
            public void onClick(DialogInterface dialog, int which) {
                RcsUtility.delveteSendVcf(list3, uri2, i2, context3, composeMessageFragment2);
                dialog.dismiss();
            }
        }).create().show();
    }

    private static void confimSendVcf(List<Uri> uriList, List<String> addrList, Uri uri, Bundle bundle, int chatType, Context context, ComposeMessageFragment fragment, ArrayList<String> addresses, WorkingMessage workingMessage) {
        long mThreadID = bundle.getLong("thread_id");
        String mGroupID = bundle.getString("groupId");
        MLog.i("RcsUtility FileTrans: ", "confimSendVcf  " + chatType + " mThreadID = " + mThreadID + "mGroupID " + mGroupID);
        boolean todo = false;
        switch (chatType) {
            case 1:
            case 5:
                RcsTransaction.multiSend(fragment.getContext(), Long.valueOf(mThreadID), uriList, addrList, 120);
                todo = true;
                break;
            case 2:
                RcsTransaction.rcsSendGroupAnyFile(context, uri, mThreadID, mGroupID);
                break;
            case 3:
                fragment.onPreMessageSent();
                RcsTransaction.multiSend(fragment.getContext(), Long.valueOf(mThreadID), uriList, addresses, 0);
                fragment.onMessageSent();
                todo = true;
                break;
        }
        if (todo && workingMessage != null) {
            if (workingMessage.hasMmsDraft() || workingMessage.hasSmsDraft() || workingMessage.getConversation().hasDraft()) {
                MLog.i("RcsUtility FileTrans: ", "confimSendVcf discard draft.");
                workingMessage.discard();
            }
        }
    }

    public static void delveteSendVcf(List<Uri> uriList, Uri uri, int chatType, Context context, ComposeMessageFragment fragment) {
        switch (chatType) {
            case 1:
            case 3:
                delveteFt(RcsTransaction.getFileInfoByData(context, uriList.get(0)));
                return;
            case 2:
                delveteFt(RcsTransaction.getFileInfoByData(context, uri));
                return;
            case 5:
                fragment.finishSelf(false);
                delveteFt(RcsTransaction.getFileInfoByData(context, uriList.get(0)));
                return;
            default:
                return;
        }
    }

    private static void delveteFt(FileInfo info) {
        if (info != null && !new File(info.getSendFilePath()).delete()) {
            MLog.d("RcsUtility FileTrans: ", "delete file  error ");
        }
    }

    public static String getGroupMemberName(Context context, String groupId) {
        if (context == null || groupId == null) {
            return null;
        }
        synchronized (mGroupMemberCacheLock) {
            if (mGroupMemberNameCache == null) {
                mGroupMemberNameCache = new LruCache(10);
            }
            String groupMemberName = (String) mGroupMemberNameCache.get(groupId);
            if (groupMemberName == null) {
                groupMemberName = getGroupMemberNameFromDB(context, groupId);
                if (TextUtils.isEmpty(groupMemberName)) {
                    return null;
                }
                mGroupMemberNameCache.put(groupId, groupMemberName);
                return groupMemberName;
            }
            return groupMemberName;
        }
    }

    private static String getGroupMemberNameFromDB(Context context, String groupId) {
        Context context2 = context;
        Cursor cursor = SqliteWrapper.query(context2, context.getContentResolver(), Uri.parse("content://rcsim/rcs_group_members"), new String[]{"thread_id", "rcs_id"}, "thread_id in (select thread_id from rcs_groups where name = ?)", new String[]{groupId}, null);
        ArrayList<String> contactList = new ArrayList();
        if (cursor != null) {
            try {
                cursor.moveToPosition(-1);
                while (cursor.moveToNext()) {
                    contactList.add(getGroupContactShowName(cursor.getString(1), cursor.getLong(0)));
                }
            } catch (RuntimeException e) {
                MLog.e("RcsUtility", "getGroupMemberNameFromDB cursor == null");
                return join(contactList, context.getResources().getString(R.string.groupchat_member_name_separate));
            } finally {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        if (cursor != null) {
            cursor.close();
        }
        return join(contactList, context.getResources().getString(R.string.groupchat_member_name_separate));
    }

    public static String getGroupContactShowName(String number, long groupThreadId) {
        boolean isNickNameEnable = RcsProfile.isGroupChatNicknameEnabled();
        String address = NumberUtils.normalizeNumber(number);
        Contact contact = Contact.get(address, true);
        String nickname = "";
        if (contact.existsInDatabase()) {
            return contact.getName();
        }
        if (isNickNameEnable) {
            nickname = RcsProfile.getGroupMemberNickname(address, groupThreadId);
            if (!TextUtils.isEmpty(nickname)) {
                return nickname;
            }
        }
        return address;
    }

    private static String join(ArrayList<String> list, String separ) {
        if (list == null || separ == null || list.size() == 0) {
            return null;
        }
        int size = list.size();
        StringBuffer str = new StringBuffer();
        for (int i = 0; i < size - 1; i++) {
            str.append((String) list.get(i));
            str.append(separ);
        }
        str.append((String) list.get(size - 1));
        return str.toString();
    }

    public static void clearGroupNameCache() {
        synchronized (mGroupMemberCacheLock) {
            if (mGroupMemberNameCache != null) {
                mGroupMemberNameCache.evictAll();
            }
        }
    }

    public static boolean isRcsLogin() {
        if (!isRcsOn) {
            return false;
        }
        boolean loginStatus = false;
        try {
            if (RcsProfile.getRcsService() != null) {
                loginStatus = RcsProfile.getRcsService().getLoginState();
            }
        } catch (Exception e) {
            loginStatus = false;
        }
        return loginStatus;
    }

    public static String getStrOfUtf8Bytes(byte[] data) {
        if (data == null) {
            return null;
        }
        int dataLength = data.length;
        int offset = 0;
        int i = dataLength - 1;
        while (i >= dataLength - 7 && i >= 0) {
            int zeroLoc = getZeroLoc(data[i]);
            if (zeroLoc != 0) {
                if (zeroLoc == 1) {
                    offset++;
                    i--;
                } else {
                    offset++;
                    if (zeroLoc == offset) {
                        offset = 0;
                    }
                }
            }
        }
        try {
            return new String(data, 0, data.length - offset, "utf-8");
        } catch (UnsupportedEncodingException e) {
            MLog.e("RcsUtility", "getStrOfUtf8Bytes UnsupportedEncodingException");
            return null;
        }
    }

    private static int getZeroLoc(byte data) {
        int iData = data;
        int loc = 0;
        while (iData != 0 && (iData & 128) != 0) {
            iData <<= 1;
            loc++;
        }
        return loc;
    }

    public static String getCustSnippet(CharSequence text, Conversation conv, Context context) {
        CharSequence messageServiceType = text;
        String VcardText = "";
        String senderName = "";
        SpannableStringBuilder custSnippet = new SpannableStringBuilder();
        int fileType = 0;
        if (conv.getHwCust() != null) {
            fileType = conv.getHwCust().getFileType();
        }
        MLog.d("TAG", "filetype = " + fileType);
        if (fileType != 0) {
            Object messageServiceType2;
            switch (fileType) {
                case 101:
                    messageServiceType = "[" + context.getResources().getString(R.string.attachment_audio) + "]";
                    break;
                case 102:
                    messageServiceType = "[" + context.getResources().getString(R.string.attachment_picture) + "]";
                    break;
                case OfflineMapStatus.EXCEPTION_SDCARD /*103*/:
                    messageServiceType = "[" + context.getResources().getString(R.string.attachment_video) + "]";
                    break;
                case LocationRequest.PRIORITY_NO_POWER /*105*/:
                    messageServiceType = "[" + context.getResources().getString(R.string.attach_anyfile) + "]";
                    break;
                case 106:
                    messageServiceType = "[" + context.getResources().getString(R.string.attach_location) + "]";
                    break;
                case 108:
                case 110:
                    String vcardInfo_send = getVcarfInfo(text.toString(), context);
                    if (vcardInfo_send != null && !TextUtils.isEmpty(vcardInfo_send)) {
                        messageServiceType2 = String.format(context.getResources().getString(R.string.rcs_ft_type_vcard_send), new Object[]{vcardInfo_send, getVcardRecipent(conv)});
                        break;
                    }
                    messageServiceType = "[" + context.getResources().getString(R.string.type_vcard) + "]";
                    break;
                    break;
                case 109:
                    String vcardInfo_recv = getVcarfInfo(text.toString(), context);
                    if (vcardInfo_recv != null && !TextUtils.isEmpty(vcardInfo_recv)) {
                        messageServiceType2 = String.format(context.getResources().getString(R.string.rcs_ft_type_vcard_single_recv), new Object[]{vcardInfo_recv, getVcardRecipent(conv)});
                        break;
                    }
                    messageServiceType = "[" + context.getResources().getString(R.string.type_vcard) + "]";
                    break;
                    break;
                case 111:
                    String vcardInfo_recvGroup = getVcarfInfo(text.toString(), context);
                    if (vcardInfo_recvGroup != null && !TextUtils.isEmpty(vcardInfo_recvGroup)) {
                        messageServiceType2 = String.format(context.getResources().getString(R.string.rcs_ft_type_vcard_single_recv), new Object[]{vcardInfo_recvGroup, ""});
                        break;
                    }
                    messageServiceType = "[" + context.getResources().getString(R.string.type_vcard) + "]";
                    break;
                    break;
            }
        }
        custSnippet.append(messageServiceType);
        return custSnippet.toString();
    }

    private static String getVcarfInfo(String path, Context context) {
        String vcardPath = "";
        MLog.d("RcsUtility", "file path=" + path);
        if (path != null) {
            int firstDataIndex = path.indexOf("storage");
            if (firstDataIndex >= 0) {
                vcardPath = path.substring(firstDataIndex);
                MLog.d("RcsUtility", "file vcard path=" + vcardPath);
            }
        }
        File file = new File(vcardPath);
        if (file.exists()) {
            try {
                return new RcsVCardInfo(context, Uri.fromFile(file)).getVcardMessageHelper().getVcardDetail()[0];
            } catch (Exception e) {
                MLog.e("RcsUtility", "createVcardParsingModule failed");
                return "";
            }
        }
        MLog.d("RcsUtility", "VcardFile file not exist path=" + vcardPath);
        return "";
    }

    private static String getVcardRecipent(Conversation conv) {
        Contact contact = (Contact) conv.getRecipients().get(0);
        if (contact == null) {
            return "";
        }
        contact.checkAndUpdateContact();
        return contact.getName();
    }

    public static int getFileType(String mImAttachmentPath, int chatType, boolean isOutGoing) {
        if (mImAttachmentPath == null) {
            return -1;
        }
        MediaFileType fileType = RcsMediaFileUtils.getFileType(mImAttachmentPath);
        if (fileType != null) {
            if (RcsMediaFileUtils.isAudioFileType(fileType.fileType)) {
                return 101;
            }
            if (RcsMediaFileUtils.isImageFileType(fileType.fileType)) {
                return 102;
            }
            if (RcsMediaFileUtils.isVideoFileType(fileType.fileType)) {
                return OfflineMapStatus.EXCEPTION_SDCARD;
            }
            if (RcsMediaFileUtils.isVCardFileType(fileType.fileType)) {
                if (chatType == 1 && isOutGoing) {
                    return 108;
                }
                if (chatType == 1 && !isOutGoing) {
                    return 109;
                }
                if (chatType == 2 && isOutGoing) {
                    return 110;
                }
                if (chatType == 2 && !isOutGoing) {
                    return 111;
                }
            }
        }
        return LocationRequest.PRIORITY_NO_POWER;
    }

    public static String getBitmapFromMemCacheKey(long msgId, int chatType) {
        StringBuilder builder = new StringBuilder();
        builder.append(msgId).append("-").append(chatType);
        return builder.toString();
    }

    public static Bitmap fixRotateBitmap(String filePath, Bitmap bp) {
        try {
            ExifInterface exif = new ExifInterface();
            exif.readExif(filePath);
            Integer val = exif.getTagIntValue(ExifInterface.TAG_ORIENTATION);
            int orientation = 0;
            if (val != null) {
                orientation = ExifInterface.getRotationForOrientationValue(val.shortValue());
            }
            MLog.i("RcsUtility", "ExifInterface orientation:" + orientation);
            if (orientation != 0) {
                bp = UriImage.rotateBitmap(bp, orientation);
            }
        } catch (Throwable e) {
            MLog.e("RcsUtility", "Failed to read EXIF orientation", e);
        } catch (Throwable e2) {
            MLog.e("RcsUtility", "Failed to read EXIF orientation with: ", e2);
        }
        return bp;
    }

    public static int getAudioLength(int duration, float scale) {
        int minutes = (duration % 3600) / 60;
        int seconds = (duration % 3600) % 60;
        int length = (int) ((59.0f * scale) + 0.5f);
        if (minutes >= 1) {
            return ((int) ((187.33f * scale) + 0.5f)) + (((int) ((3.33f * scale) + 0.5f)) * (minutes - 1));
        }
        if (seconds <= 10) {
            return ((int) ((59.0f * scale) + 0.5f)) + (((int) ((5.0f * scale) + 0.5f)) * (seconds - 1));
        }
        return ((int) ((104.0f * scale) + 0.5f)) + (((int) ((1.67f * scale) + 0.5f)) * (seconds - 10));
    }
}
