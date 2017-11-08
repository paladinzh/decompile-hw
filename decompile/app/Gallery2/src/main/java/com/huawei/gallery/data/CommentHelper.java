package com.huawei.gallery.data;

import android.text.TextUtils;
import android.util.SparseArray;
import com.android.gallery3d.exif.ExifInterface;
import com.android.gallery3d.gadget.XmlUtils;
import com.android.gallery3d.util.GalleryLog;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;

public class CommentHelper {
    private static byte ASCII_QUESTION = (byte) 63;
    private static int MAX_MESSY_NUM = 12;

    public static CommentInfo readComment(String jpegFile) {
        ExifInterface exif = new ExifInterface();
        String description = null;
        boolean hasExif = true;
        try {
            File srcFile = new File(jpegFile);
            if (srcFile.exists() && srcFile.canWrite()) {
                exif.readExif(jpegFile);
                description = exif.getUserComment();
                GalleryLog.d("CommentHelper", "read from userComment comment : " + description);
            } else {
                GalleryLog.d("CommentHelper", "file not exist or can't write. " + jpegFile);
            }
            if (description == null) {
                description = exif.getTagStringValue(ExifInterface.TAG_IMAGE_DESCRIPTION);
            }
            if (description == null) {
                hasExif = exif.getAllTags() != null;
                GalleryLog.d("CommentHelper", "picure has no comment. has exif ? " + hasExif);
            }
        } catch (FileNotFoundException e) {
            GalleryLog.d("CommentHelper", "read exif error [FileNotFoundException] ");
            hasExif = false;
        } catch (IOException e2) {
            GalleryLog.d("CommentHelper", "read exif error [IOException]");
            hasExif = false;
        } catch (Exception e3) {
            GalleryLog.d("CommentHelper", "read exif error. " + e3.getMessage());
            hasExif = false;
        }
        CommentInfo commentInfo = parseComment(description);
        commentInfo.setFilePath(jpegFile);
        commentInfo.supported = hasExif;
        return commentInfo;
    }

    public static void writeComment(CommentInfo comment) {
        writeComment(comment.getFilePath(), toBytes(comment));
    }

    public static void writeComment(String jpegFile, Object content) {
        try {
            ExifInterface out = new ExifInterface();
            out.setTag(out.buildTag(ExifInterface.TAG_USER_COMMENT, content));
            out.forceRewriteExif(jpegFile);
        } catch (Exception e) {
            GalleryLog.d("CommentHelper", "write exif error. " + e.getMessage());
        }
    }

    public static CommentInfo parseComment(String description) {
        CommentInfo commentInfo = new CommentInfo();
        if (TextUtils.isEmpty(description) || isMessyDescription(description)) {
            return commentInfo;
        }
        SparseArray<String> list = new SparseArray();
        list.append(description.indexOf("<mgzn-title>"), "<mgzn-title>");
        list.append(description.indexOf("<mgzn-cpname>"), "<mgzn-cpname>");
        list.append(description.indexOf("<mgzn-download>"), "<mgzn-download>");
        list.append(description.indexOf("<mgzn-contenturi>"), "<mgzn-contenturi>");
        list.append(description.indexOf("<mgzn-pkgname>"), "<mgzn-pkgname>");
        list.append(description.indexOf("<mgzn-content>"), "<mgzn-content>");
        list.append(description.indexOf("<mgzn-worksdes>"), "<mgzn-worksdes>");
        list.append(description.indexOf("<mgzn-appver>"), "<mgzn-appver>");
        commentInfo.title = getSubDescription(description, list, "<mgzn-title>");
        commentInfo.cpname = getSubDescription(description, list, "<mgzn-cpname>");
        commentInfo.download = getSubDescription(description, list, "<mgzn-download>");
        commentInfo.contenturi = getSubDescription(description, list, "<mgzn-contenturi>");
        commentInfo.pkgname = getSubDescription(description, list, "<mgzn-pkgname>");
        commentInfo.content = getSubDescription(description, list, "<mgzn-content>");
        commentInfo.worksdes = getSubDescription(description, list, "<mgzn-worksdes>");
        commentInfo.appVer = getSubDescription(description, list, "<mgzn-appver>");
        return commentInfo;
    }

    public static byte[] toBytes(CommentInfo info) {
        try {
            return String.format("%-8s%s", new Object[]{XmlUtils.INPUT_ENCODING, packComment(info)}).getBytes(XmlUtils.INPUT_ENCODING);
        } catch (UnsupportedEncodingException e) {
            Charset charset = Charset.defaultCharset();
            GalleryLog.d("CommentInfo", "UnsupportedEncodingException can't use UTF-8, change to " + charset);
            return String.format("%-8s%s", new Object[]{charset.displayName(), packComment(info)}).getBytes(charset);
        }
    }

    public static String packComment(CommentInfo info) {
        if (info == null) {
            return "";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("<mgzn-title>").append(info.title);
        sb.append("<mgzn-cpname>").append(info.cpname);
        sb.append("<mgzn-download>").append(info.download);
        sb.append("<mgzn-contenturi>").append(info.contenturi);
        sb.append("<mgzn-pkgname>").append(info.pkgname);
        sb.append("<mgzn-content>").append(info.content);
        sb.append("<mgzn-worksdes>").append(info.worksdes);
        sb.append("<mgzn-appver>").append(info.appVer);
        return sb.toString();
    }

    private static String getSubDescription(String description, SparseArray<String> list, String tag) {
        int index = list.indexOfValue(tag);
        int last = list.size() - 1;
        int start;
        if (index >= 0 && index < last) {
            start = list.keyAt(index) + tag.length();
            int end = list.keyAt(index + 1);
            if (start >= end || start - tag.length() < 0 || end >= description.length()) {
                return "";
            }
            return description.substring(start, end);
        } else if (index != last) {
            return "";
        } else {
            start = list.keyAt(index) + tag.length();
            if (start - tag.length() < 0 || start >= description.length()) {
                return "";
            }
            return description.substring(start);
        }
    }

    private static boolean isMessyDescription(String description) {
        if (TextUtils.isEmpty(description)) {
            return false;
        }
        int messyNum = 0;
        for (byte b : description.getBytes(Charset.forName(XmlUtils.INPUT_ENCODING))) {
            if (ASCII_QUESTION == b) {
                messyNum++;
                if (messyNum > MAX_MESSY_NUM) {
                    GalleryLog.i("CommentHelper", "messy description, description = " + description);
                    return true;
                }
            }
        }
        return false;
    }
}
