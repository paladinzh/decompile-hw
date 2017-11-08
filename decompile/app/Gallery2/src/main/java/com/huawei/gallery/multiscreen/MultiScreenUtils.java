package com.huawei.gallery.multiscreen;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.net.Uri;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.GalleryLog;
import com.android.gallery3d.util.GalleryUtils;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

public class MultiScreenUtils {
    static File sCacheDir;

    public static void initialize(Context context) {
        sCacheDir = new File(GalleryUtils.ensureExternalCacheDir(context), "multiscreen");
        if (!sCacheDir.exists()) {
            GalleryLog.v("MultiScreen_Utils", "Create MultiScreenCacheDir " + sCacheDir.mkdir());
        }
    }

    public static int timeFromServiceToGallery(String serviceTime) {
        return timeStr2Int(serviceTime) * 1000;
    }

    public static String[] fetchInfo(Context context, Uri uri) {
        String[] result = new String[]{"", ""};
        if (uri == null) {
            return result;
        }
        if ("file".equals(uri.getScheme())) {
            result[0] = Uri.decode(uri.getEncodedPath());
            result[1] = Uri.decode(uri.getLastPathSegment());
        } else if ("content".equals(uri.getScheme())) {
            Cursor cursor = null;
            try {
                cursor = context.getContentResolver().query(uri, new String[]{"title", "_data"}, null, null, null);
                if (cursor != null) {
                    cursor.moveToFirst();
                    result[0] = cursor.getString(cursor.getColumnIndexOrThrow("title"));
                    result[1] = cursor.getString(cursor.getColumnIndexOrThrow("_data"));
                }
                if (cursor != null) {
                    cursor.close();
                }
            } catch (RuntimeException e) {
                GalleryLog.i("MultiScreen_Utils", "Catch a RuntimeException in fetchInfo method.");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Exception e2) {
                GalleryLog.i("MultiScreen_Utils", "Cursor.getColumnIndexOrThrow() failed in fetchInfo() method.");
                if (cursor != null) {
                    cursor.close();
                }
            } catch (Throwable th) {
                if (cursor != null) {
                    cursor.close();
                }
            }
        }
        return result;
    }

    public static String getThumbnailPath(String mediaPath) {
        Throwable th;
        String result = "";
        File pushFile = new File(sCacheDir, mediaPath.hashCode() + ".jpeg");
        if (pushFile.exists()) {
            return pushFile.getAbsolutePath();
        }
        Bitmap bitmap = BitmapUtils.createVideoThumbnail(mediaPath);
        if (bitmap == null) {
            GalleryLog.w("MultiScreen_Utils", "Can not create Video Thumbnail");
            return result;
        }
        FileOutputStream fileOutputStream = null;
        try {
            FileOutputStream fOut = new FileOutputStream(pushFile);
            try {
                bitmap.compress(CompressFormat.JPEG, 50, fOut);
                result = pushFile.getAbsolutePath();
                if (fOut != null) {
                    try {
                        fOut.close();
                    } catch (IOException e) {
                    }
                }
                fileOutputStream = fOut;
            } catch (FileNotFoundException e2) {
                fileOutputStream = fOut;
                try {
                    GalleryLog.i("TAG", "new FileOutputStream() failed in getThumbnailPath() method, reason: FileNotFoundException.");
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e3) {
                        }
                    }
                    return result;
                } catch (Throwable th2) {
                    th = th2;
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e4) {
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fOut;
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (FileNotFoundException e5) {
            GalleryLog.i("TAG", "new FileOutputStream() failed in getThumbnailPath() method, reason: FileNotFoundException.");
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            return result;
        }
        return result;
    }

    public static Uri formatUri(Uri uri) {
        String uriString = "";
        if (uri != null) {
            uriString = Uri.decode(uri.toString());
        }
        return Uri.parse(uriString);
    }

    public static String timeInt2String(int positionInt) {
        String result = "";
        String[] chip = new String[]{"00", "00", "00"};
        for (int i = 0; i <= 2; i++) {
            int remains = positionInt % 60;
            if (remains < 10) {
                chip[i] = "0" + remains;
            } else {
                chip[i] = String.valueOf(remains);
            }
            if (positionInt > remains) {
                positionInt = (positionInt - remains) / 60;
            } else {
                positionInt = 0;
            }
        }
        return chip[2] + ":" + chip[1] + ":" + chip[0];
    }

    public static int timeStr2Int(String postionString) {
        String splitstr = ":";
        if (postionString == null || "".equals(postionString.trim())) {
            return 0;
        }
        int second;
        if (postionString.contains(".")) {
            postionString = postionString.substring(0, postionString.indexOf("."));
        }
        String[] tempstr = postionString.split(splitstr);
        int arraycount = tempstr.length;
        int i = 0;
        while (i < arraycount) {
            if (findFirstNotOf(tempstr[i], "0") == -1) {
                tempstr[i] = "";
            }
            if (!"".equals(tempstr[i].trim()) && findFirstNotOf(tempstr[i], "-") == -1) {
                tempstr[i] = "";
            }
            if ("".equals(tempstr[i].trim())) {
                tempstr[i] = "0";
            }
            i++;
        }
        if (arraycount > 2) {
            second = (Integer.valueOf(tempstr[arraycount - 1]).intValue() + (Integer.valueOf(tempstr[(arraycount - 1) - 1]).intValue() * 60)) + ((Integer.valueOf(tempstr[((arraycount - 1) - 1) - 1]).intValue() * 60) * 60);
        } else if (arraycount == 2) {
            second = Integer.valueOf(tempstr[arraycount - 1]).intValue() + (Integer.valueOf(tempstr[(arraycount - 1) - 1]).intValue() * 60);
        } else if (arraycount == 1) {
            second = Integer.valueOf(tempstr[arraycount - 1]).intValue();
        } else {
            second = 0;
        }
        GalleryLog.i("MultiScreen_Utils", " second = " + second);
        return second;
    }

    public static final int findFirstNotOf(String str, String chars) {
        if (str == null) {
            return -1;
        }
        return findOf(str, chars, 0, str.length() - 1, 1, false);
    }

    public static final int findOf(String str, String chars, int startIdx, int endIdx, int offset, boolean isEqual) {
        if (offset == 0 || chars == null || str == null) {
            return -1;
        }
        int charCnt = chars.length();
        int idx = startIdx;
        while (true) {
            char strc;
            int noEqualCnt;
            int n;
            char charc;
            if (offset > 0) {
                if (endIdx < idx) {
                    break;
                }
                strc = str.charAt(idx);
                noEqualCnt = 0;
                for (n = 0; n < charCnt; n++) {
                    charc = chars.charAt(n);
                    if (isEqual) {
                        if (strc != charc) {
                            noEqualCnt++;
                        }
                        if (noEqualCnt == charCnt) {
                            return idx;
                        }
                    } else if (strc == charc) {
                        return idx;
                    }
                }
                idx += offset;
            } else {
                if (idx < endIdx) {
                    break;
                }
                strc = str.charAt(idx);
                noEqualCnt = 0;
                for (n = 0; n < charCnt; n++) {
                    charc = chars.charAt(n);
                    if (isEqual) {
                        if (strc != charc) {
                            noEqualCnt++;
                        }
                        if (noEqualCnt == charCnt) {
                            return idx;
                        }
                    } else if (strc == charc) {
                        return idx;
                    }
                }
                idx += offset;
            }
        }
        return -1;
    }
}
