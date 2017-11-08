package com.android.mms.attachment.datamodel;

import android.content.Context;
import android.net.Uri;
import android.provider.MediaStore.Files;
import com.android.mms.attachment.datamodel.data.GalleryGridItemData;
import com.google.common.base.Joiner;

public class GalleryBoundCursorLoader extends BoundCursorLoader {
    private static final String IMAGE_SELECTION = createSelection(GalleryBoundCursorLoaderParams.ACCEPTABLE_IMAGE_TYPES, new Integer[]{Integer.valueOf(1), Integer.valueOf(3)});
    private static final Uri STORAGE_URI = Files.getContentUri("external");

    private static class GalleryBoundCursorLoaderParams {
        public static final String[] ACCEPTABLE_IMAGE_TYPES = new String[]{"image/jpeg", "image/jpg", "image/png", "image/gif", "video/mp4", "video/3gp", "video/3gpp", "video/h263"};

        private GalleryBoundCursorLoaderParams() {
        }
    }

    public GalleryBoundCursorLoader(String bindingId, Context context) {
        super(bindingId, context, STORAGE_URI, GalleryGridItemData.getImageProjection(), IMAGE_SELECTION, null, "date_added DESC");
    }

    private static String createSelection(String[] mimeTypes, Integer[] mediaTypes) {
        return "mime_type IN ('" + Joiner.on("','").join((Object[]) mimeTypes) + "') AND " + "media_type" + " IN (" + Joiner.on(',').join((Object[]) mediaTypes) + ")";
    }
}
