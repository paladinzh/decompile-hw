package com.android.mms.util;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.FileProvider;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import com.android.mms.attachment.utils.ContentType;
import com.google.android.gms.R;
import com.google.android.gms.location.places.Place;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class ShareUtils {
    public static void shareMessage(Context context, Uri messageUri, String messageType, String mssageText) {
        Intent shareIntent = new Intent();
        shareIntent.setAction("android.intent.action.SEND");
        if (messageUri != null) {
            shareIntent.putExtra("android.intent.extra.STREAM", messageUri);
            messageType = context.getContentResolver().getType(messageUri);
        }
        if (!TextUtils.isEmpty(mssageText)) {
            shareIntent.putExtra("android.intent.extra.TEXT", mssageText);
        }
        shareIntent.setType(messageType);
        context.startActivity(Intent.createChooser(shareIntent, context.getString(R.string.button_share)));
    }

    public static Uri copyFile(Context context, File fromfile) {
        IOException e;
        Throwable th;
        if (fromfile == null || !fromfile.exists()) {
            return null;
        }
        File toFile = new File(context.getExternalCacheDir(), fromfile.getName());
        InputStream inputStream = null;
        FileOutputStream fosto = null;
        try {
            if (!toFile.exists() && toFile.createNewFile()) {
                MLog.e("ShareUtils", "createNewFile success");
            }
            InputStream fosfrom = new FileInputStream(fromfile);
            try {
                FileOutputStream fosto2 = new FileOutputStream(toFile);
                try {
                    byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                    while (true) {
                        int length = fosfrom.read(buffer);
                        if (length <= 0) {
                            break;
                        }
                        fosto2.write(buffer, 0, length);
                    }
                    if (fosfrom != null) {
                        try {
                            fosfrom.close();
                        } catch (IOException e2) {
                            e2.printStackTrace();
                        }
                    }
                    if (fosto2 != null) {
                        try {
                            fosto2.close();
                        } catch (IOException e22) {
                            e22.printStackTrace();
                        }
                    }
                    fosto = fosto2;
                } catch (IOException e3) {
                    e22 = e3;
                    fosto = fosto2;
                    inputStream = fosfrom;
                    try {
                        e22.printStackTrace();
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222) {
                                e222.printStackTrace();
                            }
                        }
                        if (fosto != null) {
                            try {
                                fosto.close();
                            } catch (IOException e2222) {
                                e2222.printStackTrace();
                            }
                        }
                        if (toFile.exists()) {
                            return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
                        }
                        return null;
                    } catch (Throwable th2) {
                        th = th2;
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e22222) {
                                e22222.printStackTrace();
                            }
                        }
                        if (fosto != null) {
                            try {
                                fosto.close();
                            } catch (IOException e222222) {
                                e222222.printStackTrace();
                            }
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    fosto = fosto2;
                    inputStream = fosfrom;
                    if (inputStream != null) {
                        inputStream.close();
                    }
                    if (fosto != null) {
                        fosto.close();
                    }
                    throw th;
                }
            } catch (IOException e4) {
                e222222 = e4;
                inputStream = fosfrom;
                e222222.printStackTrace();
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fosto != null) {
                    fosto.close();
                }
                if (toFile.exists()) {
                    return null;
                }
                return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
            } catch (Throwable th4) {
                th = th4;
                inputStream = fosfrom;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fosto != null) {
                    fosto.close();
                }
                throw th;
            }
        } catch (IOException e5) {
            e222222 = e5;
            e222222.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            if (fosto != null) {
                fosto.close();
            }
            if (toFile.exists()) {
                return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
            }
            return null;
        }
        if (toFile.exists()) {
            return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
        }
        return null;
    }

    public static Uri copyFile(Context context, Uri uri, String name) {
        IOException e;
        Throwable th;
        if (uri == null || TextUtils.isEmpty(name)) {
            return null;
        }
        File toFile = new File(context.getExternalCacheDir(), name);
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            if (!toFile.exists() && toFile.createNewFile()) {
                MLog.e("ShareUtils", "createNewFile success");
            }
            inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream fosto = new FileOutputStream(toFile);
            try {
                byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (true) {
                    int length = inputStream.read(buffer);
                    if (length <= 0) {
                        break;
                    }
                    fosto.write(buffer, 0, length);
                }
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                }
                if (fosto != null) {
                    try {
                        fosto.close();
                    } catch (IOException e22) {
                        e22.printStackTrace();
                    }
                }
            } catch (IOException e3) {
                e22 = e3;
                fileOutputStream = fosto;
                try {
                    e22.printStackTrace();
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e222) {
                            e222.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e2222) {
                            e2222.printStackTrace();
                        }
                    }
                    if (toFile.exists()) {
                        return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
                    }
                    return null;
                } catch (Throwable th2) {
                    th = th2;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e22222) {
                            e22222.printStackTrace();
                        }
                    }
                    if (fileOutputStream != null) {
                        try {
                            fileOutputStream.close();
                        } catch (IOException e222222) {
                            e222222.printStackTrace();
                        }
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                fileOutputStream = fosto;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                throw th;
            }
        } catch (IOException e4) {
            e222222 = e4;
            e222222.printStackTrace();
            if (inputStream != null) {
                inputStream.close();
            }
            if (fileOutputStream != null) {
                fileOutputStream.close();
            }
            if (toFile.exists()) {
                return null;
            }
            return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
        }
        if (toFile.exists()) {
            return FileProvider.getUriForFile(context, "com.android.mms.fileprovider", toFile);
        }
        return null;
    }

    public static File fileProvideUriCopy(Context context, Uri fileProviderUri, boolean isRcs) {
        IOException ioException;
        FileOutputStream fileOutputStream;
        Throwable th;
        File file = null;
        ParcelFileDescriptor parcelFileDescriptor = null;
        FileInputStream fileInputStream = null;
        FileOutputStream fileOutputStream2 = null;
        try {
            parcelFileDescriptor = context.getContentResolver().openFileDescriptor(fileProviderUri, "r");
            if (parcelFileDescriptor == null) {
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException ioException2) {
                        MLog.e("ShareUtils", ioException2.getMessage());
                    }
                }
                return null;
            }
            FileInputStream fileInputStream2 = new FileInputStream(parcelFileDescriptor.getFileDescriptor());
            try {
                String fileName = System.currentTimeMillis() + "";
                String contentType = getFileProviderType(fileProviderUri.toString());
                String outPath = RcsUtility.getCacheDirPath(true) + "/" + fileName + "_tmp" + (TextUtils.isEmpty(contentType) ? "shared_image_file.png" : "shared_image_file." + contentType);
                if (isRcs) {
                    file = new File(outPath);
                } else {
                    file = new File(context.getCacheDir(), "shared_image_file");
                }
                if (file.exists()) {
                    MLog.i("ShareUtils", "delete shared image file result " + file.delete());
                }
                fileOutputStream = new FileOutputStream(file);
            } catch (IOException e) {
                ioException2 = e;
                fileInputStream = fileInputStream2;
                try {
                    MLog.e("ShareUtils", "ShareUtils ioException.getMessage() " + ioException2.getMessage());
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException ioException22) {
                            MLog.e("ShareUtils", ioException22.getMessage());
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (IOException ioException222) {
                                    MLog.e("ShareUtils", ioException222.getMessage());
                                }
                            }
                        } catch (Throwable th2) {
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (IOException ioException2222) {
                                    MLog.e("ShareUtils", ioException2222.getMessage());
                                }
                            }
                        }
                    }
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (IOException ioException22222) {
                            MLog.e("ShareUtils", ioException22222.getMessage());
                        }
                    }
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException ioException222222) {
                            MLog.e("ShareUtils", ioException222222.getMessage());
                        }
                    }
                    return file;
                } catch (Throwable th3) {
                    th = th3;
                    if (fileInputStream != null) {
                        try {
                            fileInputStream.close();
                        } catch (IOException ioException2222222) {
                            MLog.e("ShareUtils", ioException2222222.getMessage());
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (IOException ioException22222222) {
                                    MLog.e("ShareUtils", ioException22222222.getMessage());
                                }
                            }
                        } catch (Throwable th4) {
                            if (fileOutputStream2 != null) {
                                try {
                                    fileOutputStream2.close();
                                } catch (IOException ioException222222222) {
                                    MLog.e("ShareUtils", ioException222222222.getMessage());
                                }
                            }
                        }
                    }
                    if (fileOutputStream2 != null) {
                        try {
                            fileOutputStream2.close();
                        } catch (IOException ioException2222222222) {
                            MLog.e("ShareUtils", ioException2222222222.getMessage());
                        }
                    }
                    if (parcelFileDescriptor != null) {
                        try {
                            parcelFileDescriptor.close();
                        } catch (IOException ioException22222222222) {
                            MLog.e("ShareUtils", ioException22222222222.getMessage());
                        }
                    }
                    throw th;
                }
            } catch (Throwable th5) {
                th = th5;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
                throw th;
            }
            try {
                MLog.i("ShareUtils", "ShareUtils outputFile.getAbsolutePath() " + file.getAbsolutePath());
                byte[] buffer = new byte[Place.TYPE_SUBLOCALITY_LEVEL_2];
                while (true) {
                    int length = fileInputStream2.read(buffer);
                    if (-1 == length) {
                        break;
                    }
                    fileOutputStream.write(buffer, 0, length);
                }
                fileOutputStream.flush();
                if (fileInputStream2 != null) {
                    try {
                        fileInputStream2.close();
                    } catch (IOException ioException222222222222) {
                        MLog.e("ShareUtils", ioException222222222222.getMessage());
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException ioException2222222222222) {
                                MLog.e("ShareUtils", ioException2222222222222.getMessage());
                            }
                        }
                    } catch (Throwable th6) {
                        if (fileOutputStream != null) {
                            try {
                                fileOutputStream.close();
                            } catch (IOException ioException22222222222222) {
                                MLog.e("ShareUtils", ioException22222222222222.getMessage());
                            }
                        }
                    }
                }
                if (fileOutputStream != null) {
                    try {
                        fileOutputStream.close();
                    } catch (IOException ioException222222222222222) {
                        MLog.e("ShareUtils", ioException222222222222222.getMessage());
                    }
                }
                if (parcelFileDescriptor != null) {
                    try {
                        parcelFileDescriptor.close();
                    } catch (IOException ioException2222222222222222) {
                        MLog.e("ShareUtils", ioException2222222222222222.getMessage());
                    }
                }
            } catch (IOException e2) {
                ioException2222222222222222 = e2;
                fileOutputStream2 = fileOutputStream;
                fileInputStream = fileInputStream2;
                MLog.e("ShareUtils", "ShareUtils ioException.getMessage() " + ioException2222222222222222.getMessage());
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
                return file;
            } catch (Throwable th7) {
                th = th7;
                fileOutputStream2 = fileOutputStream;
                fileInputStream = fileInputStream2;
                if (fileInputStream != null) {
                    fileInputStream.close();
                }
                if (fileOutputStream2 != null) {
                    fileOutputStream2.close();
                }
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
                throw th;
            }
            return file;
        } catch (IOException e3) {
            ioException2222222222222222 = e3;
            MLog.e("ShareUtils", "ShareUtils ioException.getMessage() " + ioException2222222222222222.getMessage());
            if (fileInputStream != null) {
                fileInputStream.close();
            }
            if (fileOutputStream2 != null) {
                fileOutputStream2.close();
            }
            if (parcelFileDescriptor != null) {
                parcelFileDescriptor.close();
            }
            return file;
        }
    }

    public static boolean isFileProviderImageType(String uriString) {
        String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uriString));
        MLog.d("ShareUtils", " isFileProviderImageType contentType:" + contentType);
        return contentType != null ? ContentType.isImageType(contentType) : false;
    }

    public static String getFileProviderType(String uriString) {
        if (TextUtils.isEmpty(uriString)) {
            return null;
        }
        String contentType = MimeTypeMap.getSingleton().getMimeTypeFromExtension(MimeTypeMap.getFileExtensionFromUrl(uriString));
        if (TextUtils.isEmpty(contentType)) {
            return null;
        }
        if (!contentType.contains("/") || contentType.split("/").length <= 1) {
            return contentType;
        }
        return contentType.split("/")[1];
    }
}
