package com.android.mms.model;

import android.text.TextUtils;
import com.android.mms.MmsConfig;
import com.huawei.cspcommon.MLog;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventListener;

public class SlideModel extends Model implements List<MediaModel>, EventListener {
    private static final int DEFAULT_SLIDE_DURATION = (-1 != MmsConfig.getCustomDefaultSlideDuration() ? MmsConfig.getCustomDefaultSlideDuration() : 5000);
    private MediaModel mAudio;
    private boolean mCanAddAudio;
    private boolean mCanAddImage;
    private boolean mCanAddVCalendar;
    private boolean mCanAddVcard;
    private boolean mCanAddVideo;
    private int mDuration;
    private short mFill;
    private MediaModel mImage;
    private final ArrayList<MediaModel> mMedia;
    private SlideshowModel mParent;
    private int mSlideSize;
    private MediaModel mText;
    private MediaModel mVCalendar;
    private MediaModel mVcard;
    private MediaModel mVideo;
    private boolean mVisible;

    public SlideModel(SlideshowModel slideshow) {
        this(DEFAULT_SLIDE_DURATION, slideshow);
    }

    public SlideModel(int duration, SlideshowModel slideshow) {
        this.mMedia = new ArrayList();
        this.mCanAddImage = true;
        this.mCanAddAudio = true;
        this.mCanAddVideo = true;
        this.mCanAddVcard = true;
        this.mCanAddVCalendar = true;
        this.mVisible = true;
        this.mDuration = duration;
        this.mParent = slideshow;
    }

    public SlideModel(int duration, ArrayList<MediaModel> mediaList) {
        this.mMedia = new ArrayList();
        this.mCanAddImage = true;
        this.mCanAddAudio = true;
        this.mCanAddVideo = true;
        this.mCanAddVcard = true;
        this.mCanAddVCalendar = true;
        this.mVisible = true;
        this.mDuration = duration;
        for (MediaModel media : mediaList) {
            internalAdd(media);
        }
    }

    private void internalAdd(MediaModel media) throws IllegalStateException {
        if (media != null) {
            if (media.isText()) {
                String contentType = media.getContentType();
                if (TextUtils.isEmpty(contentType) || "text/plain".equals(contentType) || "text/html".equals(contentType)) {
                    internalAddOrReplace(this.mText, media);
                    this.mText = media;
                } else {
                    MLog.w("Mms/slideshow", "[SlideModel] content type " + media.getContentType() + " isn't supported (as text)");
                }
            } else if (media.isImage()) {
                if (this.mCanAddImage) {
                    internalAddOrReplace(this.mImage, media);
                    this.mImage = media;
                    this.mCanAddVideo = false;
                    this.mCanAddVcard = false;
                    this.mCanAddVCalendar = false;
                } else {
                    MLog.w("Mms/slideshow", "[SlideModel] content type " + media.getContentType() + " - can't add image in this state");
                }
            } else if (media.isAudio()) {
                if (this.mCanAddAudio) {
                    internalAddOrReplace(this.mAudio, media);
                    this.mAudio = media;
                    this.mCanAddVideo = false;
                    this.mCanAddVcard = false;
                    this.mCanAddVCalendar = false;
                } else {
                    MLog.w("Mms/slideshow", "[SlideModel] content type " + media.getContentType() + " - can't add audio in this state");
                }
            } else if (media.isVideo()) {
                if (this.mCanAddVideo) {
                    internalAddOrReplace(this.mVideo, media);
                    this.mVideo = media;
                    this.mCanAddImage = false;
                    this.mCanAddAudio = false;
                    this.mCanAddVcard = false;
                    this.mCanAddVCalendar = false;
                } else {
                    MLog.w("Mms/slideshow", "[SlideModel] content type " + media.getContentType() + " - can't add video in this state");
                }
            } else if (media.isVcard()) {
                if (this.mCanAddVcard) {
                    internalAddOrReplace(this.mVcard, media);
                    this.mVcard = media;
                    this.mCanAddImage = false;
                    this.mCanAddAudio = false;
                    this.mCanAddVideo = false;
                    this.mCanAddVCalendar = false;
                } else {
                    MLog.e("Mms/slideshow", "[SlideModel] can't add any more vcard: media");
                }
            } else if (media.isVCalendar()) {
                if (this.mCanAddVCalendar) {
                    internalAddOrReplace(this.mVCalendar, media);
                    this.mVCalendar = media;
                    this.mCanAddImage = false;
                    this.mCanAddAudio = false;
                    this.mCanAddVideo = false;
                    this.mCanAddVcard = false;
                } else {
                    MLog.e("Mms/slideshow", "[SlideModel] can't add any more VCalendar: media");
                }
            }
        }
    }

    private void internalAddOrReplace(MediaModel old, MediaModel media) {
        int addSize = media.getMediaSize();
        if (old == null) {
            if (this.mParent != null) {
                this.mParent.checkMessageSize(addSize);
            }
            this.mMedia.add(media);
            increaseSlideSize(addSize);
            increaseMessageSize(addSize);
        } else {
            int removeSize = old.getMediaSize();
            if (addSize > removeSize) {
                if (this.mParent != null) {
                    this.mParent.checkMessageSize(addSize - removeSize);
                }
                increaseSlideSize(addSize - removeSize);
                increaseMessageSize(addSize - removeSize);
            } else {
                decreaseSlideSize(removeSize - addSize);
                decreaseMessageSize(removeSize - addSize);
            }
            this.mMedia.set(this.mMedia.indexOf(old), media);
            old.unregisterAllModelChangedObservers();
        }
        for (IModelChangedObserver observer : this.mModelChangedObservers) {
            media.registerModelChangedObserver(observer);
        }
    }

    private boolean internalRemove(Object object) {
        if (!this.mMedia.remove(object)) {
            return false;
        }
        if (object instanceof TextModel) {
            this.mText = null;
        } else if (object instanceof ImageModel) {
            this.mImage = null;
            this.mCanAddVideo = true;
            this.mCanAddVcard = true;
            this.mCanAddVCalendar = true;
        } else if (object instanceof AudioModel) {
            this.mAudio = null;
            this.mCanAddVideo = true;
            this.mCanAddVcard = true;
            this.mCanAddVCalendar = true;
        } else if (object instanceof VideoModel) {
            this.mVideo = null;
            this.mCanAddImage = true;
            this.mCanAddAudio = true;
            this.mCanAddVcard = true;
            this.mCanAddVCalendar = true;
        } else if (object instanceof VcardModel) {
            this.mVcard = null;
            this.mCanAddImage = true;
            this.mCanAddAudio = true;
            this.mCanAddVideo = true;
            this.mCanAddVCalendar = true;
        } else if (object instanceof VCalendarModel) {
            this.mVCalendar = null;
            this.mCanAddImage = true;
            this.mCanAddAudio = true;
            this.mCanAddVideo = true;
            this.mCanAddVcard = true;
        }
        int decreaseSize = ((MediaModel) object).getMediaSize();
        decreaseSlideSize(decreaseSize);
        decreaseMessageSize(decreaseSize);
        ((Model) object).unregisterAllModelChangedObservers();
        return true;
    }

    public int getDuration() {
        return this.mDuration;
    }

    public void setDuration(int duration) {
        this.mDuration = duration;
        notifyModelChanged(true);
    }

    public int getSlideSize() {
        return this.mSlideSize;
    }

    public void increaseSlideSize(int increaseSize) {
        if (increaseSize > 0) {
            this.mSlideSize += increaseSize;
        }
    }

    public void decreaseSlideSize(int decreaseSize) {
        if (decreaseSize > 0) {
            this.mSlideSize -= decreaseSize;
            if (this.mSlideSize < 0) {
                this.mSlideSize = 0;
            }
        }
    }

    public void setParent(SlideshowModel parent) {
        this.mParent = parent;
    }

    public void increaseMessageSize(int increaseSize) {
        if (increaseSize > 0 && this.mParent != null) {
            this.mParent.setCurrentMessageSize(this.mParent.getCurrentMessageSize() + increaseSize);
        }
    }

    public void decreaseMessageSize(int decreaseSize) {
        if (decreaseSize > 0 && this.mParent != null) {
            int size = this.mParent.getCurrentMessageSize() - decreaseSize;
            if (size < 0) {
                size = 0;
            }
            this.mParent.setCurrentMessageSize(size);
        }
    }

    public boolean add(MediaModel object) {
        internalAdd(object);
        notifyModelChanged(true);
        return true;
    }

    public boolean addAll(Collection<? extends MediaModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public void clear() {
        if (this.mMedia.size() > 0) {
            for (MediaModel media : this.mMedia) {
                media.unregisterAllModelChangedObservers();
                int decreaseSize = media.getMediaSize();
                decreaseSlideSize(decreaseSize);
                decreaseMessageSize(decreaseSize);
            }
            this.mMedia.clear();
            this.mText = null;
            this.mImage = null;
            this.mAudio = null;
            this.mVideo = null;
            this.mVcard = null;
            this.mVCalendar = null;
            this.mCanAddImage = true;
            this.mCanAddAudio = true;
            this.mCanAddVideo = true;
            this.mCanAddVcard = true;
            this.mCanAddVCalendar = true;
            notifyModelChanged(true);
        }
    }

    public boolean contains(Object object) {
        return this.mMedia.contains(object);
    }

    public boolean containsAll(Collection<?> collection) {
        return this.mMedia.containsAll(collection);
    }

    public boolean isEmpty() {
        return this.mMedia.isEmpty();
    }

    public boolean hasRoomForAttachment() {
        return (hasVideo() || hasVcard() || hasVCalendar() || hasImage() || hasAudio() || hasLocation()) ? false : true;
    }

    public Iterator<MediaModel> iterator() {
        return this.mMedia.iterator();
    }

    public boolean remove(Object object) {
        if (object == null || !(object instanceof MediaModel) || !internalRemove(object)) {
            return false;
        }
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
        return this.mMedia.size();
    }

    public Object[] toArray() {
        return this.mMedia.toArray();
    }

    public <T> T[] toArray(T[] array) {
        return this.mMedia.toArray(array);
    }

    public void add(int location, MediaModel object) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public boolean addAll(int location, Collection<? extends MediaModel> collection) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public MediaModel get(int location) {
        if (this.mMedia.size() == 0) {
            return null;
        }
        return (MediaModel) this.mMedia.get(location);
    }

    public int indexOf(Object object) {
        return this.mMedia.indexOf(object);
    }

    public int lastIndexOf(Object object) {
        return this.mMedia.lastIndexOf(object);
    }

    public ListIterator<MediaModel> listIterator() {
        return this.mMedia.listIterator();
    }

    public ListIterator<MediaModel> listIterator(int location) {
        return this.mMedia.listIterator(location);
    }

    public MediaModel remove(int location) {
        MediaModel media = (MediaModel) this.mMedia.get(location);
        if (media != null && internalRemove(media)) {
            notifyModelChanged(true);
        }
        return media;
    }

    public MediaModel set(int location, MediaModel object) {
        throw new UnsupportedOperationException("Operation not supported.");
    }

    public List<MediaModel> subList(int start, int end) {
        return this.mMedia.subList(start, end);
    }

    public boolean isVisible() {
        return this.mVisible;
    }

    public void setFill(short fill) {
        this.mFill = fill;
        notifyModelChanged(true);
    }

    protected void registerModelChangedObserverInDescendants(IModelChangedObserver observer) {
        for (MediaModel media : this.mMedia) {
            media.registerModelChangedObserver(observer);
        }
    }

    protected void unregisterModelChangedObserverInDescendants(IModelChangedObserver observer) {
        for (MediaModel media : this.mMedia) {
            media.unregisterModelChangedObserver(observer);
        }
    }

    protected void unregisterAllModelChangedObserversInDescendants() {
        for (MediaModel media : this.mMedia) {
            media.unregisterAllModelChangedObservers();
        }
    }

    public void handleEvent(Event evt) {
        if (evt.getType().equals("SmilSlideStart")) {
            this.mVisible = true;
        } else if (this.mFill != (short) 1) {
            this.mVisible = false;
        }
        notifyModelChanged(false);
    }

    public boolean hasText() {
        return this.mText != null;
    }

    public boolean hasImage() {
        return this.mImage != null;
    }

    public boolean hasLocation() {
        return this.mImage != null ? this.mImage.isLocation() : false;
    }

    public boolean hasAudio() {
        return this.mAudio != null;
    }

    public boolean hasVideo() {
        return this.mVideo != null;
    }

    public boolean hasVcard() {
        return this.mVcard != null;
    }

    public boolean removeVcard() {
        return remove(this.mVcard);
    }

    public boolean hasVCalendar() {
        return this.mVCalendar != null;
    }

    public boolean removeVCalendar() {
        return remove(this.mVCalendar);
    }

    public VCalendarModel getVCalendar() {
        return (VCalendarModel) this.mVCalendar;
    }

    public boolean removeImage() {
        return remove(this.mImage);
    }

    public boolean removeAudio() {
        boolean result = remove(this.mAudio);
        resetDuration();
        return result;
    }

    public boolean removeVideo() {
        boolean result = remove(this.mVideo);
        resetDuration();
        return result;
    }

    public TextModel getText() {
        return (TextModel) this.mText;
    }

    public ImageModel getImage() {
        return (ImageModel) this.mImage;
    }

    public AudioModel getAudio() {
        return (AudioModel) this.mAudio;
    }

    public VideoModel getVideo() {
        return (VideoModel) this.mVideo;
    }

    public VcardModel getVcard() {
        return (VcardModel) this.mVcard;
    }

    public void resetDuration() {
        if (!hasAudio() && !hasVideo()) {
            this.mDuration = DEFAULT_SLIDE_DURATION;
        }
    }

    public void updateDuration(int duration) {
        if (duration > 0) {
            this.mDuration = duration;
        }
    }

    public boolean hasRoomForAttachment(int type) {
        boolean z = true;
        if (isImageWithAudio(type)) {
            return true;
        }
        if (hasVideo() || hasVcard() || hasVCalendar() || hasImage() || hasAudio() || hasLocation()) {
            z = false;
        }
        return z;
    }

    private final boolean isImageWithAudio(int type) {
        if (type == 3 && hasImage() && !hasAudio()) {
            return true;
        }
        if (type == 2 && hasAudio() && !hasImage()) {
            return true;
        }
        return false;
    }
}
