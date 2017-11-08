package com.android.mms.model;

import android.content.Context;
import com.android.mms.MmsConfig;
import com.android.mms.model.control.MediaModelControl;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import java.io.IOException;
import java.nio.charset.Charset;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRegionMediaElement;

public class MediaModelFactory {
    public static MediaModel getMediaModel(Context context, SMILMediaElement sme, LayoutModel layouts, PduBody pb) throws IOException, IllegalArgumentException, MmsException {
        String tag = sme.getTagName();
        String src = sme.getSrc();
        PduPart part = findPart(pb, src);
        if (!(sme instanceof SMILRegionMediaElement)) {
            return getGenericMediaModel(context, tag, src, sme, part, null);
        }
        return getRegionMediaModel(context, tag, src, (SMILRegionMediaElement) sme, layouts, part);
    }

    public static PduPart findPart(PduBody pb, String src) {
        PduPart pduPart = null;
        if (src != null) {
            src = unescapeXML(src);
            if (MmsConfig.isMmsGcfTest()) {
                src = src.toUpperCase();
            }
            if (src.startsWith("cid:")) {
                pduPart = pb.getPartByContentId("<" + src.substring("cid:".length()) + ">");
                if (pduPart == null) {
                    pduPart = pb.getPartByContentId(src.substring("cid:".length()));
                }
            } else {
                pduPart = pb.getPartByName(src);
                if (pduPart == null) {
                    pduPart = pb.getPartByFileName(src);
                    if (pduPart == null) {
                        pduPart = pb.getPartByContentLocation(src);
                    }
                }
                if (pduPart == null) {
                    String ncid;
                    if (src.lastIndexOf(".") > 0) {
                        ncid = "<" + src.substring(0, src.lastIndexOf(".")) + ">";
                    } else {
                        ncid = "<" + src + ">";
                    }
                    pduPart = pb.getPartByContentId(ncid);
                }
                if (pduPart == null) {
                    pduPart = pb.getPartByContentId("<" + src + ">");
                }
            }
        }
        if (pduPart != null) {
            return pduPart;
        }
        throw new IllegalArgumentException("No part found for the model.");
    }

    private static String unescapeXML(String str) {
        return str.replaceAll("&lt;", "<").replaceAll("&gt;", ">").replaceAll("&quot;", "\"").replaceAll("&apos;", "'").replaceAll("&amp;", "&");
    }

    private static MediaModel getRegionMediaModel(Context context, String tag, String src, SMILRegionMediaElement srme, LayoutModel layouts, PduPart part) throws IOException, MmsException {
        SMILRegionElement sre = srme.getRegion();
        RegionModel region;
        if (sre != null) {
            region = layouts.findRegionById(sre.getId());
            if (region != null) {
                return getGenericMediaModel(context, tag, src, srme, part, region);
            }
        }
        String rId;
        if (tag.equals("text")) {
            rId = "Text";
        } else {
            rId = "Image";
        }
        region = layouts.findRegionById(rId);
        if (region != null) {
            return getGenericMediaModel(context, tag, src, srme, part, region);
        }
        throw new IllegalArgumentException("Region not found or bad region ID.");
    }

    private static MediaModel getGenericMediaModel(Context context, String tag, String src, SMILMediaElement sme, PduPart part, RegionModel regionModel) throws IOException, MmsException {
        byte[] bytes = part.getContentType();
        if (bytes == null) {
            throw new IllegalArgumentException("Content-Type of the part may not be null.");
        }
        return MediaModelControl.createMediaModelByPart(context, tag, src, new String(bytes, Charset.defaultCharset()), sme, part, regionModel);
    }
}
