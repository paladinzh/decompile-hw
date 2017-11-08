package com.android.mms.model;

import android.drm.DrmManagerClient;
import android.text.TextUtils;
import com.android.mms.MmsApp;
import com.android.mms.MmsConfig;
import com.android.mms.dom.smil.SmilDocumentImpl;
import com.android.mms.dom.smil.parser.SmilXmlParser;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.huawei.cspcommon.MLog;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILElement;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.SMILParElement;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRegionMediaElement;
import org.w3c.dom.smil.SMILRootLayoutElement;
import org.xml.sax.SAXException;

public class SmilHelper {
    private static boolean isOctStream = false;

    private SmilHelper() {
    }

    public static void setOctStream(boolean octStream) {
        isOctStream = octStream;
    }

    public static boolean getOctStream() {
        return isOctStream;
    }

    public static SMILDocument getDocument(PduBody pb) {
        PduPart smilPart = findSmilPart(pb);
        SMILDocument document = null;
        if (smilPart != null) {
            document = getSmilDocument(smilPart, pb);
        }
        if (document == null) {
            return createSmilDocument(pb);
        }
        return document;
    }

    public static SMILDocument getDocumentWithoutSmil(PduBody pb) {
        return createSmilDocument(pb);
    }

    public static SMILDocument getDocument(SlideshowModel model) {
        return createSmilDocument(model);
    }

    private static PduPart findSmilPart(PduBody body) {
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            PduPart part = body.getPart(i);
            if (Arrays.equals(part.getContentType(), "application/smil".getBytes(Charset.defaultCharset()))) {
                return part;
            }
        }
        return null;
    }

    private static int getMediaPartsType(PduBody body) {
        int partNum = body.getPartsNum();
        for (int i = 0; i < partNum; i++) {
            String contentType = new String(body.getPart(i).getContentType(), Charset.defaultCharset());
            if (contentType.equalsIgnoreCase("text/x-vCard")) {
                return 1;
            }
            if (contentType.equalsIgnoreCase("text/x-vCalendar")) {
                return 2;
            }
            if (i == 0 && !contentType.equalsIgnoreCase("application/smil")) {
                return 3;
            }
        }
        return 0;
    }

    private static SMILDocument validate(SMILDocument in) {
        return in;
    }

    private static SMILDocument getSmilDocument(PduPart smilPart, PduBody pb) {
        try {
            byte[] data = smilPart.getData();
            if (data != null) {
                String sml = new String(data, Charset.defaultCharset());
                Pattern pSeq = Pattern.compile(".+<\\s*seq\\s+.+", 34);
                Pattern pExcl = Pattern.compile(".+<\\s*excl\\s+.+", 34);
                if (pSeq.matcher(sml).matches() || pExcl.matcher(sml).matches()) {
                    MLog.e("Mms/smil", "Smil tag seq or excl found, return null!!");
                    return null;
                }
                int mediaPartsType = getMediaPartsType(pb);
                if (mediaPartsType > 0) {
                    if (!sml.toLowerCase().contains("<par")) {
                        MLog.e("Mms/smil", "Smil tag : <par> no found , return null!!");
                        return null;
                    } else if (!sml.toLowerCase().contains("</par")) {
                        MLog.e("Mms/smil", "Smil tag : <par> has no child , return null!!");
                        return null;
                    } else if (!sml.substring(sml.indexOf("<par") + 1, sml.indexOf("</par")).contains("<")) {
                        MLog.e("Mms/smil", "Smil tag : <par> has no child , return null!!");
                        return null;
                    } else if (mediaPartsType == 1 || mediaPartsType == 2) {
                        return null;
                    }
                }
                return validate(new SmilXmlParser().parse(new ByteArrayInputStream(data)));
            }
        } catch (IOException e) {
            MLog.e("Mms/smil", "Failed to parse SMIL document.", (Throwable) e);
        } catch (SAXException e2) {
            MLog.e("Mms/smil", "Failed to parse SMIL document.", (Throwable) e2);
        } catch (MmsException e3) {
            MLog.e("Mms/smil", "Failed to parse SMIL document.", (Throwable) e3);
        }
        return null;
    }

    public static SMILParElement addPar(SMILDocument document) {
        SMILParElement par = (SMILParElement) document.createElement("par");
        par.setDur(8.0f);
        document.getBody().appendChild(par);
        return par;
    }

    public static SMILMediaElement createMediaElement(String tag, SMILDocument document, String src) {
        SMILMediaElement mediaElement = (SMILMediaElement) document.createElement(tag);
        mediaElement.setSrc(escapeXML(src));
        return mediaElement;
    }

    public static String escapeXML(String str) {
        return str.replaceAll("&", "&amp;").replaceAll("<", "&lt;").replaceAll(">", "&gt;").replaceAll("\"", "&quot;").replaceAll("'", "&apos;");
    }

    private static SMILDocument createSmilDocument(PduBody pb) {
        SMILDocument document = new SmilDocumentImpl();
        SMILElement smil = (SMILElement) document.createElement("smil");
        smil.setAttribute("xmlns", "http://www.w3.org/2001/SMIL20/Language");
        document.appendChild(smil);
        SMILElement head = (SMILElement) document.createElement("head");
        smil.appendChild(head);
        head.appendChild((SMILLayoutElement) document.createElement("layout"));
        smil.appendChild((SMILElement) document.createElement("body"));
        SMILParElement par = addPar(document);
        int partsNum = pb.getPartsNum();
        if (partsNum == 0) {
            return document;
        }
        DrmManagerClient drmManagerClient = MmsApp.getApplication().getDrmManagerClient();
        boolean hasText = false;
        boolean hasMedia = false;
        ArrayList<String> mediaFlagList = new ArrayList();
        int i = 0;
        while (i < partsNum) {
            if (par == null || ((hasMedia && hasText) || isNeedAddNewPar(pb, mediaFlagList, i))) {
                par = addPar(document);
                hasText = false;
                hasMedia = false;
                mediaFlagList.clear();
            }
            PduPart part = pb.getPart(i);
            String contentType = new String(part.getContentType(), Charset.defaultCharset());
            if (ContentType.isDrmType(contentType)) {
                contentType = drmManagerClient.getOriginalMimeType(part.getDataUri());
            }
            if (contentType.equals("text/plain") || contentType.equalsIgnoreCase("application/vnd.wap.xhtml+xml") || contentType.equals("text/html")) {
                par.appendChild(createMediaElement("text", document, part.generateLocation()));
                hasText = true;
                mediaFlagList.add("text");
            } else if (ContentType.isImageType(contentType)) {
                par.appendChild(createMediaElement("img", document, part.generateLocation()));
                hasMedia = true;
                mediaFlagList.add("img");
            } else if (ContentType.isVideoType(contentType)) {
                par.appendChild(createMediaElement("video", document, part.generateLocation()));
                hasMedia = true;
                mediaFlagList.add("video");
            } else if (ContentType.isAudioType(contentType)) {
                par.appendChild(createMediaElement("audio", document, part.generateLocation()));
                hasMedia = true;
                mediaFlagList.add("audio");
            } else if (contentType.equalsIgnoreCase("text/x-vCard")) {
                par.appendChild(createMediaElement("vcard", document, part.generateLocation()));
                hasMedia = true;
                mediaFlagList.add("vcard");
            } else if (contentType.equalsIgnoreCase("text/x-vCalendar")) {
                par.appendChild(createMediaElement("vcalendar", document, part.generateLocation()));
                hasMedia = true;
                mediaFlagList.add("vcalendar");
            } else {
                MLog.w("Mms/smil", "unsupport media type");
            }
            if (contentType.equals("application/oct-stream")) {
                isOctStream = true;
            } else {
                isOctStream = false;
            }
            i++;
        }
        return document;
    }

    private static boolean isNeedAddNewPar(PduBody pb, ArrayList<String> mediaFlagList, int dex) {
        String contentType = new String(pb.getPart(dex).getContentType(), Charset.defaultCharset());
        if (contentType.equalsIgnoreCase("text/plain") || contentType.equalsIgnoreCase("application/vnd.wap.xhtml+xml") || contentType.equalsIgnoreCase("text/html")) {
            return mediaFlagList.contains("text");
        } else {
            if (ContentType.isImageType(contentType)) {
                return mediaFlagList.contains("img") || mediaFlagList.contains("video") || mediaFlagList.contains("vcalendar") || mediaFlagList.contains("vcard");
            } else {
                if (ContentType.isVideoType(contentType)) {
                    return mediaFlagList.contains("img") || mediaFlagList.contains("video") || mediaFlagList.contains("vcalendar") || mediaFlagList.contains("vcard") || mediaFlagList.contains("audio");
                } else {
                    if (ContentType.isAudioType(contentType)) {
                        return mediaFlagList.contains("video") || mediaFlagList.contains("vcard") || mediaFlagList.contains("vcalendar") || mediaFlagList.contains("audio");
                    } else {
                        if (contentType.equalsIgnoreCase("text/x-vCard")) {
                            return mediaFlagList.contains("img") || mediaFlagList.contains("video") || mediaFlagList.contains("vcalendar") || mediaFlagList.contains("vcard") || mediaFlagList.contains("audio");
                        } else {
                            if (contentType.equalsIgnoreCase("text/x-vCalendar")) {
                                return mediaFlagList.contains("img") || mediaFlagList.contains("video") || mediaFlagList.contains("vcalendar") || mediaFlagList.contains("vcard") || mediaFlagList.contains("audio");
                            } else {
                                return false;
                            }
                        }
                    }
                }
            }
        }
    }

    private static SMILDocument createSmilDocument(SlideshowModel slideshow) {
        SMILDocument document = new SmilDocumentImpl();
        SMILElement smilElement = (SMILElement) document.createElement("smil");
        document.appendChild(smilElement);
        SMILElement headElement = (SMILElement) document.createElement("head");
        smilElement.appendChild(headElement);
        SMILLayoutElement layoutElement = (SMILLayoutElement) document.createElement("layout");
        headElement.appendChild(layoutElement);
        SMILRootLayoutElement rootLayoutElement = (SMILRootLayoutElement) document.createElement("root-layout");
        LayoutModel layouts = slideshow.getLayout();
        rootLayoutElement.setWidth(layouts.getLayoutWidth());
        rootLayoutElement.setHeight(layouts.getLayoutHeight());
        String bgColor = layouts.getBackgroundColor();
        if (!TextUtils.isEmpty(bgColor)) {
            rootLayoutElement.setBackgroundColor(bgColor);
        }
        layoutElement.appendChild(rootLayoutElement);
        ArrayList<RegionModel> regions = layouts.getRegions();
        ArrayList<SMILRegionElement> smilRegions = new ArrayList();
        for (RegionModel r : regions) {
            SMILRegionElement smilRegion = (SMILRegionElement) document.createElement("region");
            smilRegion.setId(r.getRegionId());
            smilRegion.setLeft(r.getLeft());
            smilRegion.setTop(r.getTop());
            smilRegion.setWidth(r.getWidth());
            smilRegion.setHeight(r.getHeight());
            smilRegion.setFit(r.getFit());
            smilRegions.add(smilRegion);
        }
        smilElement.appendChild((SMILElement) document.createElement("body"));
        for (SlideModel<MediaModel> slide : slideshow) {
            boolean txtRegionPresentInLayout = false;
            boolean imgRegionPresentInLayout = false;
            SMILParElement par = addPar(document);
            par.setDur(((float) slide.getDuration()) / 1000.0f);
            addParElementEventListeners((EventTarget) par, slide);
            for (MediaModel media : slide) {
                SMILMediaElement sme;
                String src = media.getSrc();
                if (MmsConfig.getIsRenameAttachmentName()) {
                    src = media.getSmilAndPartName();
                }
                if (media instanceof TextModel) {
                    if (!TextUtils.isEmpty(((TextModel) media).getText())) {
                        sme = createMediaElement("text", document, src);
                        txtRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme, smilRegions, layoutElement, "Text", txtRegionPresentInLayout);
                    }
                } else if (media instanceof ImageModel) {
                    sme = createMediaElement("img", document, src);
                    imgRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme, smilRegions, layoutElement, "Image", imgRegionPresentInLayout);
                } else if (media instanceof VideoModel) {
                    sme = createMediaElement("video", document, src);
                    imgRegionPresentInLayout = setRegion((SMILRegionMediaElement) sme, smilRegions, layoutElement, "Image", imgRegionPresentInLayout);
                } else if (media instanceof AudioModel) {
                    sme = createMediaElement("audio", document, src);
                } else if (media instanceof VcardModel) {
                    if (!MmsConfig.getCreationModeEnabled()) {
                        sme = createMediaElement("vcard", document, src);
                    }
                } else if (!(media instanceof VCalendarModel)) {
                    MLog.w("Mms/smil", "Unsupport media: " + media);
                } else if (!MmsConfig.getCreationModeEnabled()) {
                    sme = createMediaElement("vcalendar", document, src);
                }
                int begin = media.getBegin();
                if (begin != 0) {
                    sme.setAttribute("begin", String.valueOf(begin / 1000));
                }
                int duration = media.getDuration();
                if (duration != 0) {
                    sme.setDur(((float) duration) / 1000.0f);
                }
                par.appendChild(sme);
                addMediaElementEventListeners((EventTarget) sme, media);
            }
        }
        return document;
    }

    private static SMILRegionElement findRegionElementById(ArrayList<SMILRegionElement> smilRegions, String rId) {
        for (SMILRegionElement smilRegion : smilRegions) {
            if (smilRegion.getId().equals(rId)) {
                return smilRegion;
            }
        }
        return null;
    }

    private static boolean setRegion(SMILRegionMediaElement srme, ArrayList<SMILRegionElement> smilRegions, SMILLayoutElement smilLayout, String regionId, boolean regionPresentInLayout) {
        SMILRegionElement smilRegion = findRegionElementById(smilRegions, regionId);
        if (regionPresentInLayout || smilRegion == null) {
            return false;
        }
        srme.setRegion(smilRegion);
        smilLayout.appendChild(smilRegion);
        return true;
    }

    static void addMediaElementEventListeners(EventTarget target, MediaModel media) {
        target.addEventListener("SmilMediaStart", media, false);
        target.addEventListener("SmilMediaEnd", media, false);
        target.addEventListener("SmilMediaPause", media, false);
        target.addEventListener("SmilMediaSeek", media, false);
    }

    static void addParElementEventListeners(EventTarget target, SlideModel slide) {
        target.addEventListener("SmilSlideStart", slide, false);
        target.addEventListener("SmilSlideEnd", slide, false);
    }
}
