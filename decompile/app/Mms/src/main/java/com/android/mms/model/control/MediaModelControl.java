package com.android.mms.model.control;

import android.content.Context;
import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.android.mms.model.AudioModel;
import com.android.mms.model.ImageModel;
import com.android.mms.model.MediaModel;
import com.android.mms.model.RegionModel;
import com.android.mms.model.TextModel;
import com.android.mms.model.VCalendarModel;
import com.android.mms.model.VcardModel;
import com.android.mms.model.VideoModel;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.map.abs.RcsMapLoaderFactory;
import java.io.IOException;
import java.util.HashMap;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public class MediaModelControl {
    public static MediaModel createEmptyTextModel(Context context, RegionModel regionModel) throws IOException {
        return new TextModel(context, "text/plain", null, regionModel);
    }

    public static MediaModel createMediaModelByPart(Context context, String tag, String src, String contentType, SMILMediaElement sme, PduPart part, RegionModel regionModel) throws IOException, IllegalArgumentException, MmsException {
        if (context == null || TextUtils.isEmpty(tag) || TextUtils.isEmpty(contentType) || sme == null || part == null) {
            MLog.e("MediaModelControl", "[MediaModelControl] createMediaModelByPart params error");
            return null;
        }
        MediaModel media;
        if (tag.equals("text")) {
            media = new TextModel(context, contentType, src, part.getCharset(), part.getData(), regionModel);
        } else if (tag.equals("img")) {
            media = new ImageModel(context, contentType, src, part.getDataUri(), regionModel);
            hashMap = MediaModelWrapper.getImageLocationSourceBuild(context, part);
            if (Boolean.parseBoolean((String) hashMap.get("islocation"))) {
                media.setLocation(true);
                media.setLocationSource(hashMap);
            }
            media.setBuildSource((String) hashMap.get("datapath"));
        } else if (tag.equals("video")) {
            media = new VideoModel(context, contentType, src, part.getDataUri(), regionModel);
            media.setBuildSource(MediaModelWrapper.getImageSourceBuild(context, part));
        } else if (tag.equals("audio")) {
            media = new AudioModel(context, contentType, src, part.getDataUri());
        } else if (tag.equals("vcard")) {
            media = new VcardModel(context, contentType, part.getDataUri());
        } else if (tag.equals("vcalendar")) {
            media = new VCalendarModel(context, tag, contentType, src, part.getDataUri());
        } else if (!tag.equals("ref")) {
            throw new IllegalArgumentException("Unsupported TAG: " + tag);
        } else if (ContentType.isTextType(contentType)) {
            media = new TextModel(context, contentType, src, part.getCharset(), part.getData(), regionModel);
        } else if (ContentType.isImageType(contentType)) {
            media = new ImageModel(context, contentType, src, part.getDataUri(), regionModel);
            hashMap = MediaModelWrapper.getImageLocationSourceBuild(context, part);
            if (Boolean.parseBoolean((String) hashMap.get("islocation"))) {
                media.setLocation(true);
                media.setLocationSource(hashMap);
            }
            media.setBuildSource((String) hashMap.get("datapath"));
        } else if (ContentType.isVideoType(contentType)) {
            media = new VideoModel(context, contentType, src, part.getDataUri(), regionModel);
            media.setBuildSource(MediaModelWrapper.getImageSourceBuild(context, part));
        } else if (ContentType.isAudioType(contentType)) {
            media = new AudioModel(context, contentType, src, part.getDataUri());
        } else {
            MLog.d("MediaModelControl", "[MediaModelControl] getGenericMediaModel Unsupported Content-Type: " + contentType);
            media = createEmptyTextModel(context, regionModel);
        }
        int begin = 0;
        TimeList tl = sme.getBegin();
        if (tl != null && tl.getLength() > 0) {
            begin = (int) (tl.item(0).getResolvedOffset() * 1000.0d);
        }
        media.setBegin(begin);
        int duration = (int) (sme.getDur() * 1000.0f);
        if (duration <= 0) {
            tl = sme.getEnd();
            if (tl != null && tl.getLength() > 0) {
                Time t = tl.item(0);
                if (t.getTimeType() != (short) 0) {
                    duration = ((int) (t.getResolvedOffset() * 1000.0d)) - begin;
                    if (duration == 0 && ((media instanceof AudioModel) || (media instanceof VideoModel))) {
                        duration = MmsConfig.getMinimumSlideElementDuration();
                        if (MLog.isLoggable("Mms_app", 2)) {
                            MLog.d("MediaModelControl", "[MediaModelControl] compute new duration for " + tag + ", duration=" + duration);
                        }
                    }
                }
            }
        }
        media.setDuration(duration);
        if (MmsConfig.getSlideDurationEnabled()) {
            media.setFill(sme.getFill());
        } else {
            media.setFill((short) 1);
        }
        return media;
    }

    public static void setMediaModelLocationMap(MediaModel mediaModel, HashMap<String, String> locationMap) {
        if (mediaModel != null) {
            mediaModel.setLocationSource(locationMap);
        }
    }

    public static void setBuildResource(MediaModel mediaModel, String buildResource) {
        if (mediaModel != null) {
            mediaModel.setBuildSource(buildResource);
        }
    }

    public static void viewLocationMediaModel(Context context, MediaModel mediaModel) {
        if (context == null || mediaModel == null || !mediaModel.isLocation()) {
            MLog.e("MediaModelControl", "viewLocationMediaModel params error.");
            return;
        }
        HashMap<String, String> locInfo = mediaModel.getLocationSource();
        if (locInfo != null) {
            RcsMapLoaderFactory.getMapLoader(context).loadMap(context, locInfo);
        }
    }
}
