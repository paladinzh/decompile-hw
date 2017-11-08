package com.huawei.keyguard.util;

import android.app.PendingIntent;
import android.app.PendingIntent.CanceledException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PorterDuff.Mode;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.net.Uri;
import android.os.Environment;
import android.os.SystemProperties;
import android.os.UserHandle;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.View;
import android.widget.RelativeLayout.LayoutParams;
import com.android.keyguard.R$dimen;
import com.android.keyguard.R$drawable;
import com.huawei.keyguard.HwKeyguardUpdateMonitor;
import com.huawei.keyguard.data.MusicInfo;
import com.huawei.keyguard.monitor.RadarUtil;
import com.huawei.keyguard.support.CustFeature;
import com.huawei.keyguard.support.WaterMarkUtils;
import fyusion.vislib.BuildConfig;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MusicUtils {
    private static final String LYRIC_FILE_PATH = (Environment.getExternalStorageDirectory().getPath() + "/honor.lrc");
    private static PendingIntent mClientIntent;
    private static final Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
    private static int sMusicState = 0;
    private static int sMusicVisible = 8;

    public static void setMusicState(int state) {
        sMusicState = state;
    }

    public static int getMusicState() {
        return sMusicState;
    }

    public static void setMusicVisibleState(int state) {
        sMusicVisible = state;
    }

    public static int getMusicVisibleState() {
        return sMusicVisible;
    }

    public static Bitmap getAlbumBlurBitmap(Context context, Bitmap bitmap) {
        Point realOutSize = WallpaperUtils.getRealScreenPoint(context);
        HwLog.d("MusicUtils", "getAlbumBlurBitmap out size = " + realOutSize);
        return WaterMarkUtils.addWaterMark(getBlurBitmap(context, bitmap, realOutSize.x, realOutSize.y));
    }

    public static Bitmap getAlbumMaskBitmap(Context context, Bitmap input) {
        if (context == null) {
            HwLog.w("MusicUtils", "getAlbumMaskBitmap context is null");
            return input;
        }
        Options opt = new Options();
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        if (input == null) {
            input = BitmapFactory.decodeResource(context.getResources(), R$drawable.hw_music_album_default, opt);
        } else {
            opt.outWidth = context.getResources().getDimensionPixelSize(R$dimen.music_album_default_size);
        }
        int imageViewW = opt.outWidth;
        int bitmapW = input.getWidth();
        int bitmapH = input.getHeight();
        int minEdge = imageViewW > 0 ? imageViewW : bitmapW >= bitmapH ? bitmapH : bitmapW;
        HwLog.i("MusicUtils", "getAlbumMaskBitmap input bitmapW=" + bitmapW + ",bitmapH=" + bitmapH + ",imageViewW=" + imageViewW + ",minEdge=" + minEdge);
        try {
            Bitmap middleBitmap = Bitmap.createBitmap(minEdge, minEdge, Config.ARGB_8888);
            Canvas canvas = new Canvas(middleBitmap);
            int size = minEdge - 4;
            Paint paint = new Paint();
            Rect rect = new Rect(1, 1, minEdge - 2, minEdge - 2);
            paint.setAntiAlias(true);
            canvas.drawARGB(0, 0, 0, 0);
            float radio = ((float) size) / 2.0f;
            canvas.drawCircle(2.0f + radio, 2.0f + radio, radio, paint);
            paint.setXfermode(new PorterDuffXfermode(Mode.SRC_IN));
            int srcSize = Math.min(bitmapH, bitmapW);
            int left = bitmapW > bitmapH ? (bitmapW - bitmapH) / 2 : 0;
            canvas.drawBitmap(input, new Rect(left, 0, srcSize + left, srcSize), rect, paint);
            return middleBitmap;
        } catch (OutOfMemoryError e) {
            RadarUtil.uploadLoadingAlbumImageOOM(context, "getAlbumMaskBitmap: input" + input);
            HwLog.e("MusicUtils", "getAlbumMaskBitmap OutOfMemoryError input = " + input);
            return input;
        }
    }

    public static Bitmap getBlurBitmap(Context context, Bitmap inputbitmap, int width, int height) {
        if (context == null) {
            HwLog.w("MusicUtils", "getBlurBitmap context is null");
            return inputbitmap;
        }
        Options opt = new Options();
        opt.inPurgeable = true;
        opt.inInputShareable = true;
        if (inputbitmap == null) {
            HwLog.i("MusicUtils", "getBlurBitmap inputbitmap is null no album");
            inputbitmap = BitmapFactory.decodeResource(context.getResources(), R$drawable.hw_music_no_album, opt);
        }
        HwLog.w("MusicUtils", "getBlurBitmap context is degree =" + SystemProperties.getInt("blur", 25));
        int bmWidth = inputbitmap.getWidth();
        int bmHeight = inputbitmap.getHeight();
        float bAspect = ((float) bmWidth) / ((float) bmHeight);
        float vAspect = ((float) width) / ((float) height);
        int x = 0;
        int y = 0;
        int w = bmWidth;
        int h = bmHeight;
        if (Float.compare(Math.abs(bAspect - vAspect), 0.0f) > 0) {
            if (bAspect < vAspect) {
                y = (int) ((((float) bmHeight) - (((float) bmWidth) / vAspect)) / 2.0f);
                h = (int) (((float) bmWidth) / vAspect);
            } else {
                x = (int) ((((float) bmWidth) - (((float) bmHeight) * vAspect)) / 2.0f);
                w = (int) (((float) bmHeight) * vAspect);
            }
        }
        HwLog.d("MusicUtils", "getBlurBitmap bmWidth=" + bmWidth + ",bmHeight=" + bmHeight + ",width=" + width + ",height=" + height);
        try {
            Bitmap blurBitmap = Bitmap.createBitmap(w, h, Config.ARGB_8888);
            new Canvas(blurBitmap).drawBitmap(inputbitmap, (float) (-x), (float) (-y), null);
            if (w <= 0 || h <= 0) {
                return blurBitmap;
            }
            Bitmap outputBitmap = Bitmap.createBitmap(w, h, blurBitmap.getConfig());
            ImageUtils.blurImage(context, blurBitmap, outputBitmap, 25.0f);
            return outputBitmap;
        } catch (Exception e) {
            HwLog.w("MusicUtils", "Exception = " + e.getMessage() + ", x = " + x + ", y= " + y + ", w = " + w + ", h = " + h + ",bmWidth = " + bmWidth + ", bmHeight = " + bmHeight + ", width = " + width + ", width = " + width);
            RadarUtil.uploadLoadingAlbumImageUnfittable(context, "getBlurBitmap: inputbitmap" + inputbitmap);
            return inputbitmap;
        }
    }

    public static void sendLyricsBroadcast(Context context, boolean show, int x, int y) {
        if (context == null) {
            HwLog.w("MusicUtils", "sendLyricBroadcast context is null");
        } else if (HwKeyguardUpdateMonitor.getInstance().hasBootCompleted()) {
            HwLog.d("MusicUtils", "sendLyricsBroadcast show=" + show + ",x=" + x + ",y=" + y);
            Intent intent = new Intent("com.android.keyguard.intent.action.ACTION_LYRICS");
            intent.addFlags(536870912);
            intent.addFlags(268435456);
            intent.putExtra("com.android.keyguard.intent.action.FLAG_LYRICS_SHOW", show);
            intent.putExtra("com.android.keyguard.intent.action.FLAG_LYRICS_PX", x);
            intent.putExtra("com.android.keyguard.intent.action.FLAG_LYRICS_PY", checkMultiDpiChanged(y));
            intent.setPackage("com.android.mediacenter");
            OsUtils.sendUserBroadcastWithPermission(context, intent, "com.android.keyguard.permission.SHOW_LYRICS");
        } else {
            HwLog.e("MusicUtils", "sendLyricBroadcast fail as Boot not Completed ? ");
        }
    }

    private static int checkMultiDpiChanged(int oldDimens) {
        int newDimens = oldDimens;
        int srcDpi = SystemProperties.getInt("ro.sf.lcd_density", 0);
        int realDpi = SystemProperties.getInt("persist.sys.dpi", srcDpi);
        if (srcDpi == realDpi || realDpi == 0 || srcDpi == 0) {
            return newDimens;
        }
        return (oldDimens * srcDpi) / realDpi;
    }

    public static void checkMusicViewMultiDpiChanged(View view) {
        if (view != null) {
            LayoutParams params = (LayoutParams) view.getLayoutParams();
            int oldBottomMargin = params.bottomMargin;
            int newBottomMargin = checkMultiDpiChanged(oldBottomMargin);
            params.bottomMargin = newBottomMargin;
            if (newBottomMargin != oldBottomMargin) {
                params.bottomMargin = (int) (((float) newBottomMargin) * 1.5f);
            }
            view.setLayoutParams(params);
        }
    }

    private static PendingIntent getPendingIntent(Context context) {
        PendingIntent pendingIntent;
        synchronized (MusicUtils.class) {
            if (mClientIntent == null) {
                mClientIntent = PendingIntent.getBroadcastAsUser(context, 0, new Intent("android.intent.action.MEDIA_BUTTON"), 0, new UserHandle(-2));
            }
            pendingIntent = mClientIntent;
        }
        return pendingIntent;
    }

    public static void sendMediaButtonClick(Context context, int keyCode) {
        HwLog.i("MusicUtils", "send MediaButton: " + keyCode);
        String currentPlayPackage = MusicInfo.getInst().getPlayerApp();
        HwLog.d("MusicUtils", "HwNewMusic sendMediaButtonClick {" + keyCode + "} " + currentPlayPackage);
        PendingIntent pendingIntent = getPendingIntent(context);
        Intent intent = new Intent();
        intent.setPackage(currentPlayPackage);
        intent.setAction("android.intent.action.MEDIA_BUTTON");
        intent.addFlags(268435456);
        try {
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(0, keyCode));
            pendingIntent.send(context, 0, intent);
            intent.putExtra("android.intent.extra.KEY_EVENT", new KeyEvent(1, keyCode));
            pendingIntent.send(context, 0, intent);
        } catch (CanceledException e) {
            HwLog.w("MusicUtils", "CanceledException Error sending intent for media button down:" + e.toString());
        }
    }

    public static boolean isSupportMusic(Context context, String packageName) {
        List<String> pkgList = MusicXmlParseHelper.getInstance().getSupportMusicList(context);
        if (pkgList == null || !pkgList.contains(packageName)) {
            return CustFeature.isMusicAppSupported(context, packageName);
        }
        return true;
    }

    private static boolean isValidController(Context context, MediaController controller) {
        if (controller == null || controller.getPlaybackState() == null) {
            return false;
        }
        return isSupportMusic(context, controller.getPackageName());
    }

    public static MediaController findCurrentMediaController(Context context, MediaSessionManager sessionManager) {
        int size = 0;
        if (sessionManager == null) {
            HwLog.w("MusicUtils", "skip find MediaPlayPackage");
            return null;
        }
        List<MediaController> sessions = sessionManager.getActiveSessionsForUser(null, -1);
        if (sessions != null) {
            size = sessions.size();
        }
        if (size <= 0) {
            HwLog.w("MusicUtils", "findCurrentMediaPlayPackage sessions size is 0");
            return null;
        }
        MediaController currentController = null;
        for (MediaController controller : sessions) {
            if (isValidController(context, controller)) {
                switch (controller.getPlaybackState().getState()) {
                    case 0:
                    case 1:
                    case 7:
                        break;
                    case 2:
                        currentController = controller;
                        break;
                    case 3:
                        return controller;
                    default:
                        break;
                }
            }
        }
        return currentController;
    }

    public static void parseLyricStringToList(String lyricString, List<String> lyricList, List<Integer> timeList) {
        if (lyricList != null && timeList != null) {
            lyricList.clear();
            timeList.clear();
            if (!TextUtils.isEmpty(lyricString)) {
                readLrcFile(LYRIC_FILE_PATH, lyricList, timeList);
            }
        }
    }

    private static void readLrcFile(String path, List<String> lyricList, List<Integer> timeList) {
        InputStreamReader inputStreamReader;
        FileNotFoundException e;
        IOException e2;
        Throwable th;
        FileInputStream fileInputStream = null;
        InputStreamReader inputStreamReader2 = null;
        BufferedReader bufferedReader = null;
        try {
            FileInputStream fileInputStream2 = new FileInputStream(new File(path));
            try {
                inputStreamReader = new InputStreamReader(fileInputStream2, "UTF-8");
            } catch (FileNotFoundException e3) {
                e = e3;
                fileInputStream = fileInputStream2;
                try {
                    e.printStackTrace();
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                            return;
                        }
                    }
                    if (inputStreamReader2 != null) {
                        inputStreamReader2.close();
                    }
                    if (fileInputStream == null) {
                        fileInputStream.close();
                    }
                } catch (Throwable th2) {
                    th = th2;
                    if (bufferedReader != null) {
                        try {
                            bufferedReader.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                            throw th;
                        }
                    }
                    if (inputStreamReader2 != null) {
                        inputStreamReader2.close();
                    }
                    if (fileInputStream != null) {
                        fileInputStream.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222 = e4;
                fileInputStream = fileInputStream2;
                e222.printStackTrace();
                if (bufferedReader != null) {
                    try {
                        bufferedReader.close();
                    } catch (IOException e2222) {
                        e2222.printStackTrace();
                        return;
                    }
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (Throwable th3) {
                th = th3;
                fileInputStream = fileInputStream2;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
            try {
                BufferedReader bufferedReader2 = new BufferedReader(inputStreamReader);
                try {
                    String str = BuildConfig.FLAVOR;
                    while (true) {
                        str = bufferedReader2.readLine();
                        if (str == null) {
                            break;
                        }
                        try {
                            addTimeToList(str, timeList);
                            if (str.indexOf("[ar:") == -1 && str.indexOf("[ti:") == -1) {
                                if (str.indexOf("[by:") == -1) {
                                    str = str.replace(str.substring(str.indexOf("["), str.indexOf("]") + 1), BuildConfig.FLAVOR);
                                    lyricList.add(str);
                                }
                            }
                            str = str.substring(str.indexOf(":") + 1, str.indexOf("]"));
                            lyricList.add(str);
                        } catch (IndexOutOfBoundsException e5) {
                            HwLog.w("MusicUtils", "readLrcFile with IndexOutOfBoundsException");
                        }
                    }
                    if (bufferedReader2 != null) {
                        try {
                            bufferedReader2.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    if (inputStreamReader != null) {
                        inputStreamReader.close();
                    }
                    if (fileInputStream2 != null) {
                        fileInputStream2.close();
                    }
                } catch (FileNotFoundException e6) {
                    e = e6;
                    bufferedReader = bufferedReader2;
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = fileInputStream2;
                } catch (IOException e7) {
                    e22222 = e7;
                    bufferedReader = bufferedReader2;
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = fileInputStream2;
                } catch (Throwable th4) {
                    th = th4;
                    bufferedReader = bufferedReader2;
                    inputStreamReader2 = inputStreamReader;
                    fileInputStream = fileInputStream2;
                }
            } catch (FileNotFoundException e8) {
                e = e8;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = fileInputStream2;
                e.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (IOException e9) {
                e22222 = e9;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = fileInputStream2;
                e22222.printStackTrace();
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream == null) {
                    fileInputStream.close();
                }
            } catch (Throwable th5) {
                th = th5;
                inputStreamReader2 = inputStreamReader;
                fileInputStream = fileInputStream2;
                if (bufferedReader != null) {
                    bufferedReader.close();
                }
                if (inputStreamReader2 != null) {
                    inputStreamReader2.close();
                }
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e10) {
            e = e10;
            e.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader2 != null) {
                inputStreamReader2.close();
            }
            if (fileInputStream == null) {
                fileInputStream.close();
            }
        } catch (IOException e11) {
            e22222 = e11;
            e22222.printStackTrace();
            if (bufferedReader != null) {
                bufferedReader.close();
            }
            if (inputStreamReader2 != null) {
                inputStreamReader2.close();
            }
            if (fileInputStream == null) {
                fileInputStream.close();
            }
        }
    }

    private static void addTimeToList(String string, List<Integer> timeList) {
        Matcher matcher = Pattern.compile("\\[\\d{1,2}:\\d{1,2}([\\.:]\\d{1,2})?\\]").matcher(string);
        if (matcher.find()) {
            try {
                String str = matcher.group();
                int timer = parseTimeToMillis(str.substring(1, str.length() - 1));
                synchronized (MusicUtils.class) {
                    timeList.add(Integer.valueOf(timer));
                }
            } catch (IllegalStateException e) {
                HwLog.w("MusicUtils", "addTimeToList with IllegalStateException");
            }
        }
    }

    private static int parseTimeToMillis(String string) {
        int currentTime = -1;
        if (TextUtils.isEmpty(string)) {
            return -1;
        }
        String[] timeData = string.replace(".", ":").split(":");
        if (timeData.length < 3) {
            return -1;
        }
        try {
            int minute = Integer.parseInt(timeData[0]);
            currentTime = (((minute * 60) + Integer.parseInt(timeData[1])) * 1000) + (Integer.parseInt(timeData[2]) * 10);
        } catch (NumberFormatException e) {
            HwLog.w("MusicUtils", "timeHandler with NumberFormatException");
        }
        return currentTime;
    }
}
