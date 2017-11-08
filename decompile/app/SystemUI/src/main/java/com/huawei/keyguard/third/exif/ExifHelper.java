package com.huawei.keyguard.third.exif;

import android.text.TextUtils;
import com.huawei.keyguard.support.magazine.BigPictureInfo.DescriptionInfo;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import java.io.IOException;

public class ExifHelper {
    public static final int DESCRIPTION = ExifInterface.TAG_IMAGE_DESCRIPTION;

    public static ExifInterface readExif(String path) {
        ExifInterface exifInterface = new ExifInterface();
        try {
            exifInterface.readExif(path);
        } catch (IOException e) {
        }
        return exifInterface;
    }

    public static DescriptionInfo getDescriptionInfoFromPicture(String picPath) {
        DescriptionInfo info = HwFyuseUtils.getDescription(picPath);
        if (info != null) {
            return info;
        }
        ExifInterface exifInterface = readExif(picPath);
        String userComment = exifInterface.getUserComment();
        if (TextUtils.isEmpty(userComment)) {
            return DescriptionInfo.parseDescription(exifInterface.getTagStringValue(DESCRIPTION));
        }
        DescriptionInfo desUserComment = DescriptionInfo.parseDescription(userComment);
        if (desUserComment.isUserCommentDescriptionValid()) {
            return desUserComment;
        }
        return DescriptionInfo.parseDescription(exifInterface.getTagStringValue(DESCRIPTION));
    }
}
