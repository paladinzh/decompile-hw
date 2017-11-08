package com.android.mms.model;

import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.net.Uri;
import android.text.TextUtils;
import cn.com.xy.sms.sdk.ui.popu.util.ContentUtil;
import com.android.mms.ContentRestrictionException;
import com.android.mms.MmsConfig;
import com.android.mms.dom.smil.parser.SmilXmlSerializer;
import com.android.mms.layout.LayoutManager;
import com.google.android.mms.ContentType;
import com.google.android.mms.MmsException;
import com.google.android.mms.pdu.GenericPdu;
import com.google.android.mms.pdu.MultimediaMessagePdu;
import com.google.android.mms.pdu.PduBody;
import com.google.android.mms.pdu.PduPart;
import com.google.android.mms.pdu.PduPersister;
import com.huawei.cspcommon.MLog;
import com.huawei.cspcommon.ex.SqliteWrapper;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.SMILDocument;
import org.w3c.dom.smil.SMILLayoutElement;
import org.w3c.dom.smil.SMILMediaElement;
import org.w3c.dom.smil.SMILParElement;
import org.w3c.dom.smil.SMILRegionElement;
import org.w3c.dom.smil.SMILRootLayoutElement;

public class SlideshowModel extends Model implements List<SlideModel>, IModelChangedObserver {
    private Context mContext;
    private int mCurrentMessageSize;
    private SMILDocument mDocumentCache;
    private ArrayList<String> mImageSlideSources;
    private final LayoutModel mLayout;
    private PduBody mPduBodyCache;
    private final ArrayList<SlideModel> mSlides;
    private int mTotalMessageSize;

    private SlideshowModel(Context context) {
        this.mLayout = new LayoutModel();
        this.mSlides = new ArrayList();
        this.mImageSlideSources = new ArrayList();
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
    }

    public String toString() {
        return "slides: " + this.mSlides + " layoutmodel: " + this.mLayout + " context:" + this.mContext;
    }

    private SlideshowModel(LayoutModel layouts, ArrayList<SlideModel> slides, SMILDocument documentCache, PduBody pbCache, Context context) {
        this.mLayout = layouts;
        this.mSlides = slides;
        if (context != null) {
            this.mContext = context.getApplicationContext();
        }
        this.mImageSlideSources = new ArrayList();
        this.mDocumentCache = documentCache;
        this.mPduBodyCache = pbCache;
        for (SlideModel slide : this.mSlides) {
            increaseMessageSize(slide.getSlideSize());
            slide.setParent(this);
        }
    }

    public static SlideshowModel createNew(Context context) {
        return new SlideshowModel(context);
    }

    public static SlideshowModel createFromMessageUri(Context context, Uri uri) throws MmsException {
        return createFromPduBody(context, getPduBody(context, uri));
    }

    public static SlideshowModel createFromPduBody(Context context, PduBody pb) throws MmsException {
        return createFromPduBody(context, pb, false);
    }

    public static SlideshowModel createFromPduBody(Context context, PduBody pb, boolean retry) throws MmsException {
        SMILDocument document;
        int i;
        MLog.e("Mms/slideshow", "is retry: " + retry);
        if (retry) {
            document = SmilHelper.getDocumentWithoutSmil(pb);
        } else {
            document = SmilHelper.getDocument(pb);
        }
        SMILLayoutElement sle = document.getLayout();
        SMILRootLayoutElement srle = sle.getRootLayout();
        int w = srle.getWidth();
        int h = srle.getHeight();
        if (w == 0 || h == 0) {
            w = LayoutManager.getInstance().getLayoutParameters().getWidth();
            h = LayoutManager.getInstance().getLayoutParameters().getHeight();
            srle.setWidth(w);
            srle.setHeight(h);
        }
        RegionModel rootLayout = new RegionModel(null, 0, 0, w, h);
        ArrayList<RegionModel> regions = new ArrayList();
        NodeList nlRegions = sle.getRegions();
        int regionsNum = nlRegions.getLength();
        for (i = 0; i < regionsNum; i++) {
            SMILRegionElement sre = (SMILRegionElement) nlRegions.item(i);
            regions.add(new RegionModel(sre.getId(), sre.getFit(), sre.getLeft(), sre.getTop(), sre.getWidth(), sre.getHeight(), sre.getBackgroundColor()));
        }
        LayoutModel layouts = new LayoutModel(rootLayout, regions);
        NodeList slideNodes = document.getBody().getChildNodes();
        int slidesNum = slideNodes.getLength();
        ArrayList<SlideModel> slides = new ArrayList(slidesNum);
        ArrayList<String> imageSourceBuilds = new ArrayList();
        int totalMessageSize = 0;
        for (i = 0; i < slidesNum; i++) {
            SMILParElement par = (SMILParElement) slideNodes.item(i);
            NodeList mediaNodes = par.getChildNodes();
            int mediaNum = mediaNodes.getLength();
            ArrayList<MediaModel> arrayList = new ArrayList(mediaNum);
            for (int j = 0; j < mediaNum; j++) {
                SMILMediaElement item = mediaNodes.item(j);
                if (item instanceof SMILMediaElement) {
                    SMILMediaElement sme = item;
                    try {
                        MediaModel media = MediaModelFactory.getMediaModel(context, sme, layouts, pb);
                        if (media != null) {
                            if (!MmsConfig.getSlideDurationEnabled()) {
                                int mediadur = media.getDuration();
                                int dur = (int) par.getDur();
                                if (dur == 0) {
                                    mediadur = MmsConfig.getMinimumSlideElementDuration() * 1000;
                                    media.setDuration(mediadur);
                                }
                                if (mediadur / 1000 != dur) {
                                    String tag = sme.getTagName();
                                    if (!ContentType.isVideoType(media.mContentType)) {
                                        if (!(tag.equals("video") || ContentType.isAudioType(media.mContentType))) {
                                            if (!tag.equals("audio")) {
                                                if (mediadur / 1000 < dur) {
                                                    media.setDuration(dur * 1000);
                                                } else if (dur != 0) {
                                                    media.setDuration(dur * 1000);
                                                } else {
                                                    par.setDur(((float) mediadur) / 1000.0f);
                                                }
                                            }
                                        }
                                    }
                                    par.setDur((((float) mediadur) / 1000.0f) + ContentUtil.FONT_SIZE_NORMAL);
                                }
                            }
                            SmilHelper.addMediaElementEventListeners((EventTarget) sme, media);
                            arrayList.add(media);
                            totalMessageSize += media.getMediaSize();
                        }
                    } catch (Throwable e) {
                        MLog.e("Mms/slideshow", e.getMessage(), e);
                    } catch (Throwable e2) {
                        MLog.e("Mms/slideshow", e2.getMessage(), e2);
                        if (!retry && "No part found for the model.".equals(e2.getMessage())) {
                            return createFromPduBody(context, pb, true);
                        }
                    } catch (Throwable e3) {
                        MLog.e("Mms/slideshow", e3.getMessage(), e3);
                    }
                } else {
                    MLog.e("Mms/slideshow", "node is not a SMILMediaElement");
                }
            }
            SlideModel slideModel = new SlideModel((int) (par.getDur() * 1000.0f), (ArrayList) arrayList);
            slideModel.setFill(par.getFill());
            SmilHelper.addParElementEventListeners((EventTarget) par, slideModel);
            if (slideModel.hasImage()) {
                String imageSourceBuild = slideModel.getImage().getSourceBuild();
                if (!(TextUtils.isEmpty(imageSourceBuild) || imageSourceBuilds.contains(imageSourceBuild))) {
                    imageSourceBuilds.add(imageSourceBuild);
                }
            } else if (slideModel.hasVideo()) {
                String videoSourceBuild = slideModel.getVideo().getSourceBuild();
                if (!(TextUtils.isEmpty(videoSourceBuild) || imageSourceBuilds.contains(videoSourceBuild))) {
                    imageSourceBuilds.add(videoSourceBuild);
                }
            }
            slides.add(slideModel);
        }
        SlideshowModel slideshow = new SlideshowModel(layouts, slides, document, pb, context);
        slideshow.mTotalMessageSize = totalMessageSize;
        slideshow.registerModelChangedObserver(slideshow);
        slideshow.setImageSourceBuilds(imageSourceBuilds);
        return slideshow;
    }

    public PduBody toPduBody() {
        if (this.mPduBodyCache == null) {
            this.mDocumentCache = SmilHelper.getDocument(this);
            this.mPduBodyCache = makePduBody(this.mDocumentCache);
        }
        return this.mPduBodyCache;
    }

    private PduBody makePduBody(SMILDocument document) {
        PduBody pb = new PduBody();
        Charset charset = Charset.defaultCharset();
        for (SlideModel<MediaModel> slide : this.mSlides) {
            for (MediaModel media : slide) {
                String location;
                PduPart part = new PduPart();
                if (media.isText()) {
                    TextModel text = (TextModel) media;
                    if (!TextUtils.isEmpty(text.getText())) {
                        part.setCharset(text.getCharset());
                    }
                }
                part.setContentType(media.getContentType().getBytes(charset));
                String src = media.getSrc();
                if (MmsConfig.getIsRenameAttachmentName()) {
                    src = media.getSmilAndPartName();
                }
                if (src.startsWith("cid:")) {
                    location = src.substring("cid:".length());
                } else {
                    location = src;
                }
                part.setContentLocation(location.getBytes(charset));
                part.setContentId(location.getBytes(charset));
                part.setFilename(location.getBytes(charset));
                if (media.isText()) {
                    part.setData(((TextModel) media).getText().getBytes(charset));
                } else if (media.isImage() || media.isVideo() || media.isAudio() || media.isLocation()) {
                    part.setDataUri(media.getUri());
                } else if (media.isVcard() || media.isVCalendar()) {
                    try {
                        if (media.getData() != null) {
                            part.setData(media.getData());
                        } else {
                            MLog.e("Mms/slideshow", "both vcard data[]  is empty!!!");
                        }
                        if (media.getUri() != null) {
                            part.setDataUri(media.getUri());
                        } else {
                            MLog.e("Mms/slideshow", "both vcard  uri is empty!!!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    MLog.w("Mms/slideshow", "Unsupport media: " + media);
                }
                pb.addPart(part);
            }
        }
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        SmilXmlSerializer.serialize(document, out);
        PduPart smilPart = new PduPart();
        smilPart.setContentId("smil".getBytes(charset));
        smilPart.setContentLocation("smil.xml".getBytes(charset));
        smilPart.setContentType("application/smil".getBytes(charset));
        smilPart.setData(out.toByteArray());
        pb.addPart(0, smilPart);
        return pb;
    }

    public HashMap<Uri, InputStream> openPartFiles(ContentResolver cr) {
        HashMap<Uri, InputStream> openedFiles = null;
        for (SlideModel<MediaModel> slide : this.mSlides) {
            for (MediaModel media : slide) {
                if (!media.isText()) {
                    Uri uri = media.getUri();
                    if (uri != null) {
                        try {
                            InputStream is = cr.openInputStream(uri);
                            if (is != null) {
                                if (openedFiles == null) {
                                    openedFiles = new HashMap();
                                }
                                openedFiles.put(uri, is);
                            }
                        } catch (FileNotFoundException e) {
                            MLog.e("Mms/slideshow", "openPartFiles couldn't open:uri ", (Throwable) e);
                        } catch (SecurityException e2) {
                            MLog.e("Mms/slideshow", "openPartFiles couldn't open:permission denied " + e2);
                        }
                    }
                }
            }
        }
        return openedFiles;
    }

    public PduBody makeCopy() {
        return makePduBody(SmilHelper.getDocument(this));
    }

    public static PduBody getPduBody(Context context, Uri msg) throws MmsException {
        try {
            GenericPdu pdu = PduPersister.getPduPersister(context).load(msg);
            int msgType = pdu.getMessageType();
            if (msgType == 128 || msgType == 132) {
                return ((MultimediaMessagePdu) pdu).getBody();
            }
            throw new MmsException();
        } catch (SecurityException e) {
            MLog.e("Mms/slideshow", "PduPersister load SecurityException: " + e);
            throw new MmsException();
        }
    }

    public void setCurrentMessageSize(int size) {
        this.mCurrentMessageSize = size;
    }

    public int getCurrentMessageSize() {
        return this.mCurrentMessageSize;
    }

    public int getTotalMessageSize() {
        return this.mTotalMessageSize;
    }

    public void increaseMessageSize(int increaseSize) {
        if (increaseSize > 0) {
            this.mCurrentMessageSize += increaseSize;
        }
    }

    public void decreaseMessageSize(int decreaseSize) {
        if (decreaseSize > 0) {
            this.mCurrentMessageSize -= decreaseSize;
        }
        if (this.mCurrentMessageSize < 0) {
            this.mCurrentMessageSize = 0;
        }
    }

    public LayoutModel getLayout() {
        return this.mLayout;
    }

    public boolean add(SlideModel object) {
        int increaseSize = object.getSlideSize();
        checkMessageSize(increaseSize);
        if (!this.mSlides.add(object)) {
            return false;
        }
        increaseMessageSize(increaseSize);
        object.registerModelChangedObserver(this);
        for (IModelChangedObserver observer : this.mModelChangedObservers) {
            object.registerModelChangedObserver(observer);
        }
        addImageSourceBuild(object);
        notifyModelChanged(true);
        return true;
    }

    public boolean addAll(Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public void clear() {
        if (this.mSlides.size() > 0) {
            for (SlideModel slide : this.mSlides) {
                slide.unregisterModelChangedObserver(this);
                for (IModelChangedObserver observer : this.mModelChangedObservers) {
                    slide.unregisterModelChangedObserver(observer);
                }
            }
            this.mCurrentMessageSize = 0;
            this.mSlides.clear();
            this.mImageSlideSources.clear();
            notifyModelChanged(true);
        }
    }

    public boolean contains(Object object) {
        return this.mSlides.contains(object);
    }

    public boolean containsAll(Collection<?> collection) {
        return this.mSlides.containsAll(collection);
    }

    public boolean isEmpty() {
        return this.mSlides.isEmpty();
    }

    public Iterator<SlideModel> iterator() {
        return this.mSlides.iterator();
    }

    public boolean remove(Object object) {
        if (object == null || !this.mSlides.remove(object)) {
            return false;
        }
        SlideModel slide = (SlideModel) object;
        decreaseMessageSize(slide.getSlideSize());
        slide.unregisterAllModelChangedObservers();
        removeImageSourceBuild(slide);
        notifyModelChanged(true);
        return true;
    }

    public boolean removeAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public boolean retainAll(Collection<?> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public int size() {
        return this.mSlides.size();
    }

    public Object[] toArray() {
        return this.mSlides.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return this.mSlides.toArray(array);
    }

    public void add(int location, SlideModel object) {
        if (object != null) {
            int increaseSize = object.getSlideSize();
            checkMessageSize(increaseSize);
            this.mSlides.add(location, object);
            addImageSourceBuild(object);
            increaseMessageSize(increaseSize);
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : this.mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
        }
    }

    public boolean addAll(int location, Collection<? extends SlideModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public SlideModel get(int location) {
        return (location < 0 || location >= this.mSlides.size()) ? null : (SlideModel) this.mSlides.get(location);
    }

    public int indexOf(Object object) {
        return this.mSlides.indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return this.mSlides.lastIndexOf(object);
    }

    public ListIterator<SlideModel> listIterator() {
        return this.mSlides.listIterator();
    }

    public ListIterator<SlideModel> listIterator(int location) {
        return this.mSlides.listIterator(location);
    }

    public SlideModel remove(int location) {
        SlideModel slide = (SlideModel) this.mSlides.remove(location);
        if (slide != null) {
            decreaseMessageSize(slide.getSlideSize());
            slide.unregisterAllModelChangedObservers();
            removeImageSourceBuild(slide);
        }
        return slide;
    }

    public SlideModel set(int location, SlideModel object) {
        SlideModel slide = (SlideModel) this.mSlides.get(location);
        if (object != null) {
            int removeSize = 0;
            int addSize = object.getSlideSize();
            if (slide != null) {
                removeSize = slide.getSlideSize();
            }
            if (addSize > removeSize) {
                checkMessageSize(addSize - removeSize);
                increaseMessageSize(addSize - removeSize);
            } else {
                decreaseMessageSize(removeSize - addSize);
            }
        }
        slide = (SlideModel) this.mSlides.set(location, object);
        if (slide != null) {
            slide.unregisterAllModelChangedObservers();
        }
        if (object != null) {
            object.registerModelChangedObserver(this);
            for (IModelChangedObserver observer : this.mModelChangedObservers) {
                object.registerModelChangedObserver(observer);
            }
        }
        setImageSourceBuild(slide, object);
        notifyModelChanged(true);
        return slide;
    }

    public List<SlideModel> subList(int start, int end) {
        List<SlideModel> resultList = this.mSlides.subList(start, end);
        subImageSourceBuilds(resultList);
        return resultList;
    }

    protected void registerModelChangedObserverInDescendants(IModelChangedObserver observer) {
        this.mLayout.registerModelChangedObserver(observer);
        for (SlideModel slide : this.mSlides) {
            slide.registerModelChangedObserver(observer);
        }
    }

    protected void unregisterModelChangedObserverInDescendants(IModelChangedObserver observer) {
        this.mLayout.unregisterModelChangedObserver(observer);
        for (SlideModel slide : this.mSlides) {
            slide.unregisterModelChangedObserver(observer);
        }
    }

    protected void unregisterAllModelChangedObserversInDescendants() {
        this.mLayout.unregisterAllModelChangedObservers();
        for (SlideModel slide : this.mSlides) {
            slide.unregisterAllModelChangedObservers();
        }
    }

    public void onModelChanged(Model model, boolean dataChanged) {
        if (dataChanged) {
            this.mDocumentCache = null;
            this.mPduBodyCache = null;
        }
    }

    public void clearPduCache() {
        this.mDocumentCache = null;
        this.mPduBodyCache = null;
    }

    public void sync(PduBody pb) {
        for (SlideModel<MediaModel> slide : this.mSlides) {
            for (MediaModel media : slide) {
                PduPart part = pb.getPartByContentLocation(media.getSrc());
                if (MmsConfig.getIsRenameAttachmentName()) {
                    part = pb.getPartByContentLocation(media.getSmilAndPartName());
                }
                if (part != null) {
                    media.setUri(part.getDataUri());
                    if (((media instanceof ImageModel) || (media instanceof VideoModel)) && !TextUtils.isEmpty(media.getSourceBuild())) {
                        if (media.isLocation()) {
                            insertLocationPartSource(this.mContext, part.getDataUri(), Uri.parse(media.getSourceBuild()), media);
                        } else {
                            insertPartSource(this.mContext, part.getDataUri(), Uri.parse(media.getSourceBuild()));
                        }
                    }
                }
            }
        }
    }

    public void insertPartSource(Context context, Uri newUri, Uri oldUri) {
        if (context != null && newUri != null) {
            SqliteWrapper.delete(context, context.getContentResolver(), Uri.parse("content://mms/part_source/" + newUri.getLastPathSegment()), null, null);
            Uri insertUri = Uri.parse("content://mms/part_source");
            ContentValues values = new ContentValues();
            values.put("part_id", Integer.valueOf(Integer.parseInt(newUri.getLastPathSegment())));
            values.put("old_data", oldUri.getPath());
            SqliteWrapper.insert(context, insertUri, values);
        }
    }

    private void insertLocationPartSource(Context context, Uri newUri, Uri oldUri, MediaModel media) {
        if (context != null && newUri != null) {
            SqliteWrapper.delete(context, context.getContentResolver(), Uri.parse("content://mms/part_source/" + newUri.getLastPathSegment()), null, null);
            Uri uri = Uri.parse("content://mms/part_source");
            ContentValues values = new ContentValues();
            values.put("part_id", Integer.valueOf(Integer.parseInt(newUri.getLastPathSegment())));
            values.put("old_data", oldUri.getPath());
            if (media.getLocationSource() != null) {
                values.put("islocation", Integer.valueOf(1));
                values.put("locationtitle", (String) media.getLocationSource().get("title"));
                values.put("locationsub", (String) media.getLocationSource().get("subtitle"));
                values.put("latitude", (String) media.getLocationSource().get("latitude"));
                values.put("longitude", (String) media.getLocationSource().get("longitude"));
            }
            SqliteWrapper.insert(context, uri, values);
        }
    }

    public void checkMessageSize(int increaseSize) throws ContentRestrictionException {
        ContentRestrictionFactory.getContentRestriction().checkMessageSize(this.mCurrentMessageSize, increaseSize, this.mContext.getContentResolver());
    }

    public boolean isSimple() {
        if (size() != 1) {
            return false;
        }
        int hasAudio;
        SlideModel slide = get(0);
        if ((slide.hasImage() ^ slide.hasVideo()) == 0) {
            hasAudio = slide.hasAudio() ^ slide.hasVideo();
        } else {
            hasAudio = 1;
        }
        if (hasAudio == 0) {
            return false;
        }
        return ((slide.hasAudio() && slide.hasImage()) || slide.hasVcard() || slide.hasVCalendar()) ? false : true;
    }

    public void prepareForSend() {
        if (size() == 1) {
            TextModel text = get(0).getText();
            if (text != null) {
                text.cloneText();
            }
        }
    }

    public void setImageSourceBuilds(ArrayList<String> imagesourcebuilds) {
        if (imagesourcebuilds != null && imagesourcebuilds.size() > 0) {
            this.mImageSlideSources.clear();
            this.mImageSlideSources.addAll(imagesourcebuilds);
        }
    }

    private void setImageSourceBuild(SlideModel oldSlide, SlideModel newModel) {
        if (oldSlide != null && newModel != null) {
            removeImageSourceBuild(oldSlide);
            addImageSourceBuild(newModel);
        }
    }

    public ArrayList<String> getImageSourceBuilds() {
        return this.mImageSlideSources;
    }

    public void addImageSourceBuild(SlideModel slide) {
        CharSequence imageSourceBuild = null;
        if (slide != null && slide.hasImage()) {
            imageSourceBuild = slide.getImage().getSourceBuild();
        } else if (slide != null && slide.hasVideo()) {
            imageSourceBuild = slide.getVideo().getSourceBuild();
        }
        if (!TextUtils.isEmpty(imageSourceBuild) && !this.mImageSlideSources.contains(imageSourceBuild)) {
            this.mImageSlideSources.add(imageSourceBuild);
        }
    }

    public boolean removeImageSourceBuild(SlideModel slide) {
        CharSequence imageSourceBuild = null;
        if (slide != null && slide.hasImage()) {
            imageSourceBuild = slide.getImage().getSourceBuild();
        } else if (slide != null && slide.hasVideo()) {
            imageSourceBuild = slide.getVideo().getSourceBuild();
        }
        if (TextUtils.isEmpty(imageSourceBuild) || !this.mImageSlideSources.contains(imageSourceBuild)) {
            return false;
        }
        this.mImageSlideSources.remove(imageSourceBuild);
        return true;
    }

    private void subImageSourceBuilds(List<SlideModel> slides) {
        if (slides != null) {
            for (SlideModel slide : slides) {
                CharSequence imageSourceBuild = null;
                if (slide != null && slide.hasImage()) {
                    imageSourceBuild = slide.getImage().getSourceBuild();
                } else if (slide != null && slide.hasVideo()) {
                    imageSourceBuild = slide.getVideo().getSourceBuild();
                }
                if (!TextUtils.isEmpty(imageSourceBuild) && this.mImageSlideSources.contains(imageSourceBuild)) {
                    this.mImageSlideSources.remove(imageSourceBuild);
                }
            }
        }
    }

    public ArrayList<SlideModel> getSlideModels() {
        return this.mSlides;
    }

    public boolean removeImageSourceBuilds(String path) {
        if (TextUtils.isEmpty(path) || !this.mImageSlideSources.contains(path)) {
            return false;
        }
        return this.mImageSlideSources.remove(path);
    }

    public boolean canShowPreview() {
        if (this.mSlides == null || this.mSlides.size() == 0) {
            return false;
        }
        for (int i = 0; i < this.mSlides.size(); i++) {
            if (!((SlideModel) this.mSlides.get(i)).hasRoomForAttachment()) {
                return true;
            }
        }
        return false;
    }
}
