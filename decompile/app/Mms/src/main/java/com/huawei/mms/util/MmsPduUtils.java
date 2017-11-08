package com.huawei.mms.util;

import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.Telephony.Mms;
import android.text.TextUtils;
import android.webkit.MimeTypeMap;
import android.widget.Toast;
import com.android.mms.MmsApp;
import com.android.mms.drm.DrmUtils;
import com.android.mms.model.MediaModel;
import com.android.mms.model.MediaModelFactory;
import com.android.mms.model.SlideshowModel;
import com.android.mms.ui.MessageUtils;
import com.google.android.gms.R;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

public class MmsPduUtils {

    public static class FileSaveResult {
        private boolean mResult = false;
        private String mTargetFile = null;

        public String getTargetPath() {
            return this.mTargetFile;
        }

        public boolean getResult() {
            return this.mResult;
        }
    }

    public static boolean copyMediaAndShowResult(Context context, long msgId) {
        return toastForCopyResults(context, copyMedia(context, msgId));
    }

    public static Collection<FileSaveResult> copyMedia(Context context, long msgId) {
        Collection<FileSaveResult> results = new ArrayList();
        PduBody body = null;
        try {
            body = SlideshowModel.getPduBody(context, ContentUris.withAppendedId(Mms.CONTENT_URI, msgId));
        } catch (MmsException e) {
            MLog.e("MmsPduUtils", "copyMedia can't load pdu body: " + msgId);
        }
        if (body == null) {
            return results;
        }
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            results.add(copyPart(context, body.getPart(i), Long.toHexString(msgId)));
        }
        HwMessageUtils.noticeMediaChanged(context);
        return results;
    }

    public static boolean toastForCopyResult(Context context, FileSaveResult result) {
        Collection<FileSaveResult> results = new ArrayList();
        results.add(result);
        return toastForCopyResults(context, results);
    }

    public static boolean toastForCopyResults(Context context, Collection<FileSaveResult> results) {
        ArrayList<String> failedDirectories = new ArrayList();
        ArrayList<String> succDirectories = new ArrayList();
        HashSet<String> failedSet = new HashSet();
        HashSet<String> succSet = new HashSet();
        String msg = context.getResources().getString(R.string.copy_to_sdcard_fail_Toast);
        for (FileSaveResult result : results) {
            if (!result.getResult()) {
                failedSet.add(result.getTargetPath());
            } else if (!TextUtils.isEmpty(result.getTargetPath())) {
                succSet.add(result.getTargetPath());
            }
        }
        failedDirectories.addAll(failedSet);
        succDirectories.addAll(succSet);
        StringBuffer append;
        if (failedDirectories.size() > 0) {
            if (failedDirectories.size() == 2) {
                msg = context.getString(R.string.copy_path, new Object[]{failedDirectories.get(0), failedDirectories.get(1)});
            } else if (failedDirectories.size() == 1) {
                msg = (String) failedDirectories.get(0);
            }
            append = new StringBuffer().append(context.getResources().getString(R.string.copy_to_sdcard_fail_Toast));
            if (msg == null) {
                msg = "";
            }
            msg = append.append(msg).toString();
        } else if (succDirectories.size() > 0) {
            if (succDirectories.size() == 2) {
                msg = context.getString(R.string.copy_path, new Object[]{succDirectories.get(0), succDirectories.get(1)});
            } else if (succDirectories.size() == 1) {
                msg = (String) succDirectories.get(0);
            }
            if (MessageUtils.isNeedLayoutRtl()) {
                append = new StringBuffer().append("‭");
                if (msg == null) {
                    msg = "";
                }
                msg = append.append(msg).append("‬").toString();
            } else if (msg == null) {
                msg = "";
            }
            msg = context.getResources().getString(R.string.mms_attachment_save_to, new Object[]{msg});
        }
        Toast.makeText(context, msg, 1).show();
        if (failedDirectories.size() == 0) {
            return true;
        }
        return false;
    }

    private static File getUniqueDestination(String base, String extension) {
        File file = new File(base + "." + extension);
        int i = 2;
        while (file.exists()) {
            file = new File(base + "_" + i + "." + extension);
            i++;
        }
        return file;
    }

    public static boolean copyPartAndShowResult(Context context, PduPart part) {
        FileSaveResult result = copyPart(context, part, "default");
        if (result.getTargetPath() != null) {
            toastForCopyResult(context, result);
            HwMessageUtils.noticeMediaChanged(context, result.getTargetPath());
        } else {
            Toast.makeText(context, R.string.copy_to_sdcard_fail_Toast, 0).show();
        }
        return result.getResult();
    }

    public static FileSaveResult copyPart(Context context, PduPart part) {
        return copyPart(context, part, "default");
    }

    public static FileSaveResult copyPart(Context context, PduPart part, String fallback) {
        IOException e;
        Throwable th;
        FileSaveResult result = new FileSaveResult();
        Uri uri = part.getDataUri();
        if (uri == null) {
            result.mResult = true;
            return result;
        }
        String str = new String(part.getContentType(), Charset.defaultCharset());
        String str2 = null;
        if (part.getFilename() != null) {
            str2 = new String(part.getFilename(), Charset.defaultCharset());
        }
        if (DrmUtils.isDrmType(str)) {
            String type = MmsApp.getApplication().getDrmManagerClient().getOriginalMimeType(part.getDataUri());
        }
        if (ContentType.isImageType(type) || ContentType.isVideoType(type) || ContentType.isAudioType(type) || "text/x-vCard".equalsIgnoreCase(type) || "application/ogg".equals(type) || "text/x-vCalendar".equals(type) || (r15 != null && MessageUtils.isEndWithImageExtension(r15) && TextUtils.equals(type, "application/oct-stream"))) {
            InputStream inputStream = null;
            FileOutputStream fileOutputStream = null;
            try {
                inputStream = context.getContentResolver().openInputStream(uri);
                if (inputStream == null || !(inputStream instanceof FileInputStream)) {
                    result.mResult = true;
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e2) {
                            MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e2);
                            result.mResult = false;
                        }
                    }
                    return result;
                }
                String extension;
                String fileName = getFileName(part, fallback);
                int index = fileName.lastIndexOf(".");
                if (index == -1) {
                    extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
                } else {
                    extension = fileName.substring(index + 1, fileName.length());
                    fileName = fileName.substring(0, index);
                }
                if (ContentType.isDrmType(new String(part.getContentType(), Charset.defaultCharset()))) {
                    extension = "dcf";
                }
                File file = getUniqueDestination((Environment.getExternalStorageDirectory() + "/" + (ContentType.isAudioType(type) ? Environment.DIRECTORY_RINGTONES : Environment.DIRECTORY_DOWNLOADS) + "/") + fileName, extension);
                File parentFile = file.getParentFile();
                if (parentFile == null) {
                    if (inputStream != null) {
                        try {
                            inputStream.close();
                        } catch (IOException e22) {
                            MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e22);
                            result.mResult = false;
                        }
                    }
                    return result;
                }
                result.mTargetFile = parentFile.getAbsolutePath();
                if (parentFile.exists() || parentFile.mkdirs()) {
                    FileOutputStream fout = new FileOutputStream(file);
                    try {
                        FileInputStream fin = (FileInputStream) inputStream;
                        byte[] buffer = new byte[8000];
                        while (true) {
                            int size = fin.read(buffer);
                            if (size == -1) {
                                break;
                            }
                            fout.write(buffer, 0, size);
                        }
                        result.mResult = true;
                        context.sendBroadcast(new Intent("android.intent.action.MEDIA_SCANNER_SCAN_FILE", Uri.fromFile(file)));
                        if (inputStream != null) {
                            try {
                                inputStream.close();
                            } catch (IOException e222) {
                                MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e222);
                                result.mResult = false;
                            }
                        }
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e2222) {
                                MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e2222);
                                result.mResult = false;
                            }
                        }
                    } catch (IOException e3) {
                        e2222 = e3;
                        fileOutputStream = fout;
                        try {
                            MLog.e("MmsPduUtils", "IOException caught while opening or reading stream", (Throwable) e2222);
                            result.mResult = false;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e22222) {
                                    MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e22222);
                                    result.mResult = false;
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e222222) {
                                    MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e222222);
                                    result.mResult = false;
                                }
                            }
                            return result;
                        } catch (Throwable th2) {
                            th = th2;
                            if (inputStream != null) {
                                try {
                                    inputStream.close();
                                } catch (IOException e2222222) {
                                    MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e2222222);
                                    result.mResult = false;
                                }
                            }
                            if (fileOutputStream != null) {
                                try {
                                    fileOutputStream.close();
                                } catch (IOException e22222222) {
                                    MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e22222222);
                                    result.mResult = false;
                                }
                            }
                            throw th;
                        }
                    } catch (Throwable th3) {
                        th = th3;
                        fileOutputStream = fout;
                        if (inputStream != null) {
                            inputStream.close();
                        }
                        if (fileOutputStream != null) {
                            fileOutputStream.close();
                        }
                        throw th;
                    }
                    return result;
                }
                MLog.e("MmsPduUtils", "[MMS] copyPart: mkdirs for " + parentFile.getPath() + " failed!");
                if (inputStream != null) {
                    try {
                        inputStream.close();
                    } catch (IOException e222222222) {
                        MLog.e("MmsPduUtils", "IOException caught while closing stream", (Throwable) e222222222);
                        result.mResult = false;
                    }
                }
                return result;
            } catch (IOException e4) {
                e222222222 = e4;
                MLog.e("MmsPduUtils", "IOException caught while opening or reading stream", (Throwable) e222222222);
                result.mResult = false;
                if (inputStream != null) {
                    inputStream.close();
                }
                if (fileOutputStream != null) {
                    fileOutputStream.close();
                }
                return result;
            }
        }
        result.mResult = true;
        return result;
    }

    public static PduPart getPduPartForName(SlideshowModel model, String partName) {
        PduPart pp = null;
        try {
            pp = MediaModelFactory.findPart(model.toPduBody(), partName);
        } catch (IllegalArgumentException e) {
            MLog.d("MmsPduUtils", "No part found for the model.");
        }
        return pp;
    }

    public static Uri getSaveMediaFileUri(SlideshowModel model, MediaModel mediaModel, String fallback) {
        Uri result = null;
        if (model == null || mediaModel == null) {
            try {
                result = Uri.fromFile(getUniqueDestination(fallback, fallback));
            } catch (Exception e) {
                MLog.e("MmsPduUtils", "Exception caught while get uri", (Throwable) e);
            }
            return result;
        }
        PduPart part = getPduPartForName(model, mediaModel.getSrc());
        if (part == null) {
            return result;
        }
        String extension;
        String fileName = getFileName(part, fallback);
        String type = new String(part.getContentType(), Charset.defaultCharset());
        int index = fileName.lastIndexOf(".");
        if (index == -1) {
            extension = MimeTypeMap.getSingleton().getExtensionFromMimeType(type);
        } else {
            extension = fileName.substring(index + 1, fileName.length());
            fileName = fileName.substring(0, index);
        }
        if (ContentType.isDrmType(new String(part.getContentType(), Charset.defaultCharset()))) {
            extension = "dcf";
        }
        try {
            result = Uri.fromFile(getUniqueDestination((Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DOWNLOADS + "/") + fileName, extension));
        } catch (Exception e2) {
            MLog.e("MmsPduUtils", "Exception caught when get uri from file", (Throwable) e2);
        }
        return result;
    }

    private static String getFileName(PduPart part, String fallback) {
        byte[] location = part.getName();
        if (location == null) {
            location = part.getFilename();
        }
        if (location == null) {
            location = part.getContentLocation();
        }
        if (location == null) {
            return fallback;
        }
        return new String(location, Charset.defaultCharset());
    }
}
