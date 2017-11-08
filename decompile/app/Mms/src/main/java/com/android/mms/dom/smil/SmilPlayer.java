package com.android.mms.dom.smil;

import android.util.Log;
import android.util.MathUtils;
import com.huawei.cspcommon.ex.SafeRunnable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import org.w3c.dom.NodeList;
import org.w3c.dom.events.DocumentEvent;
import org.w3c.dom.events.Event;
import org.w3c.dom.events.EventTarget;
import org.w3c.dom.smil.ElementParallelTimeContainer;
import org.w3c.dom.smil.ElementSequentialTimeContainer;
import org.w3c.dom.smil.ElementTime;
import org.w3c.dom.smil.Time;
import org.w3c.dom.smil.TimeList;

public class SmilPlayer extends SafeRunnable {
    private static SmilPlayer sPlayer;
    private static final Comparator<TimelineEntry> sTimelineEntryComparator = new Comparator<TimelineEntry>() {
        public int compare(TimelineEntry o1, TimelineEntry o2) {
            return Double.compare(o1.getOffsetTime(), o2.getOffsetTime());
        }
    };
    private SmilPlayerAction mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
    private ArrayList<ElementTime> mActiveElements;
    private ArrayList<TimelineEntry> mAllEntries;
    private int mCurrentElement;
    private int mCurrentSlide;
    private long mCurrentTime;
    private Event mMediaTimeUpdatedEvent;
    private Thread mPlayerThread;
    private ElementTime mRoot;
    private SmilPlayerState mState = SmilPlayerState.INITIALIZED;

    private enum SmilPlayerAction {
        NO_ACTIVE_ACTION,
        RELOAD,
        STOP,
        PAUSE,
        START,
        NEXT,
        PREV
    }

    private enum SmilPlayerState {
        INITIALIZED,
        PLAYING,
        PLAYED,
        PAUSED,
        STOPPED
    }

    private static final class TimelineEntry {
        private final int mAction;
        private final ElementTime mElement;
        private final double mOffsetTime;

        public TimelineEntry(double offsetTime, ElementTime element, int action) {
            this.mOffsetTime = offsetTime;
            this.mElement = element;
            this.mAction = action;
        }

        public double getOffsetTime() {
            return this.mOffsetTime;
        }

        public ElementTime getElement() {
            return this.mElement;
        }

        public int getAction() {
            return this.mAction;
        }

        public String toString() {
            return "Type = " + this.mElement + " offset = " + getOffsetTime() + " action = " + getAction();
        }
    }

    private static ArrayList<TimelineEntry> getParTimeline(ElementParallelTimeContainer par, double offset, double maxOffset) {
        ArrayList<TimelineEntry> timeline = new ArrayList();
        double beginOffset = par.getBegin().item(0).getResolvedOffset() + offset;
        if (beginOffset > maxOffset) {
            return timeline;
        }
        int i;
        timeline.add(new TimelineEntry(beginOffset, par, 0));
        double endOffset = par.getEnd().item(0).getResolvedOffset() + offset;
        if (endOffset > maxOffset) {
            endOffset = maxOffset;
        }
        TimelineEntry timelineEntry = new TimelineEntry(endOffset, par, 1);
        maxOffset = endOffset;
        NodeList children = par.getTimeChildren();
        for (i = 0; i < children.getLength(); i++) {
            timeline.addAll(getTimeline((ElementTime) children.item(i), offset, maxOffset));
        }
        Collections.sort(timeline, sTimelineEntryComparator);
        NodeList activeChildrenAtEnd = par.getActiveChildrenAt(((float) (endOffset - offset)) * 1000.0f);
        for (i = 0; i < activeChildrenAtEnd.getLength(); i++) {
            timeline.add(new TimelineEntry(endOffset, (ElementTime) activeChildrenAtEnd.item(i), 1));
        }
        timeline.add(timelineEntry);
        return timeline;
    }

    private static ArrayList<TimelineEntry> getSeqTimeline(ElementSequentialTimeContainer seq, double offset, double maxOffset) {
        ArrayList<TimelineEntry> timeline = new ArrayList();
        double orgOffset = offset;
        double beginOffset = seq.getBegin().item(0).getResolvedOffset() + offset;
        if (beginOffset > maxOffset) {
            return timeline;
        }
        int i;
        timeline.add(new TimelineEntry(beginOffset, seq, 0));
        double endOffset = seq.getEnd().item(0).getResolvedOffset() + offset;
        if (endOffset > maxOffset) {
            endOffset = maxOffset;
        }
        TimelineEntry timelineEntry = new TimelineEntry(endOffset, seq, 1);
        maxOffset = endOffset;
        NodeList children = seq.getTimeChildren();
        for (i = 0; i < children.getLength(); i++) {
            ArrayList<TimelineEntry> childTimeline = getTimeline((ElementTime) children.item(i), offset, maxOffset);
            timeline.addAll(childTimeline);
            offset = ((TimelineEntry) childTimeline.get(childTimeline.size() - 1)).getOffsetTime();
        }
        NodeList activeChildrenAtEnd = seq.getActiveChildrenAt((float) (endOffset - orgOffset));
        for (i = 0; i < activeChildrenAtEnd.getLength(); i++) {
            timeline.add(new TimelineEntry(endOffset, (ElementTime) activeChildrenAtEnd.item(i), 1));
        }
        timeline.add(timelineEntry);
        return timeline;
    }

    private static ArrayList<TimelineEntry> getTimeline(ElementTime element, double offset, double maxOffset) {
        if (element instanceof ElementParallelTimeContainer) {
            return getParTimeline((ElementParallelTimeContainer) element, offset, maxOffset);
        }
        if (element instanceof ElementSequentialTimeContainer) {
            return getSeqTimeline((ElementSequentialTimeContainer) element, offset, maxOffset);
        }
        int i;
        ArrayList<TimelineEntry> timeline = new ArrayList();
        TimeList beginList = element.getBegin();
        for (i = 0; i < beginList.getLength(); i++) {
            Time begin = beginList.item(i);
            if (begin.getResolved()) {
                double beginOffset = begin.getResolvedOffset() + offset;
                if (beginOffset <= maxOffset) {
                    timeline.add(new TimelineEntry(beginOffset, element, 0));
                }
            }
        }
        TimeList endList = element.getEnd();
        for (i = 0; i < endList.getLength(); i++) {
            Time end = endList.item(i);
            if (end.getResolved()) {
                double endOffset = end.getResolvedOffset() + offset;
                if (endOffset <= maxOffset) {
                    timeline.add(new TimelineEntry(endOffset, element, 1));
                }
            }
        }
        Collections.sort(timeline, sTimelineEntryComparator);
        return timeline;
    }

    private SmilPlayer() {
    }

    public static synchronized SmilPlayer getPlayer() {
        SmilPlayer smilPlayer;
        synchronized (SmilPlayer.class) {
            if (sPlayer == null) {
                sPlayer = new SmilPlayer();
            }
            smilPlayer = sPlayer;
        }
        return smilPlayer;
    }

    public synchronized boolean isPlayingState() {
        return this.mState == SmilPlayerState.PLAYING;
    }

    public synchronized boolean isPlayedState() {
        return this.mState == SmilPlayerState.PLAYED;
    }

    public synchronized boolean isPausedState() {
        return this.mState == SmilPlayerState.PAUSED;
    }

    public synchronized boolean isStoppedState() {
        return this.mState == SmilPlayerState.STOPPED;
    }

    private synchronized boolean isPauseAction() {
        return this.mAction == SmilPlayerAction.PAUSE;
    }

    private synchronized boolean isStartAction() {
        return this.mAction == SmilPlayerAction.START;
    }

    private synchronized boolean isStopAction() {
        return this.mAction == SmilPlayerAction.STOP;
    }

    private synchronized boolean isReloadAction() {
        return this.mAction == SmilPlayerAction.RELOAD;
    }

    private synchronized boolean isNextAction() {
        return this.mAction == SmilPlayerAction.NEXT;
    }

    private synchronized boolean isPrevAction() {
        return this.mAction == SmilPlayerAction.PREV;
    }

    public synchronized void init(ElementTime root) {
        this.mRoot = root;
        this.mAllEntries = getTimeline(this.mRoot, 0.0d, 9.223372036854776E18d);
        if (this.mRoot instanceof DocumentEvent) {
            this.mMediaTimeUpdatedEvent = ((DocumentEvent) this.mRoot).createEvent("Event");
            this.mMediaTimeUpdatedEvent.initEvent("mediaTimeUpdated", false, false);
        }
        this.mActiveElements = new ArrayList();
    }

    public synchronized void play() {
        if (isPlayingState()) {
            Log.w("Mms/smil", "Error State: Playback is playing!");
        } else {
            this.mCurrentTime = 0;
            this.mCurrentElement = 0;
            this.mCurrentSlide = 0;
            this.mPlayerThread = new Thread(this, "SmilPlayer thread");
            this.mState = SmilPlayerState.PLAYING;
            this.mPlayerThread.start();
        }
    }

    public synchronized void pause() {
        if (isPlayingState()) {
            this.mAction = SmilPlayerAction.PAUSE;
            notifyAll();
        } else {
            Log.w("Mms/smil", "Error State: Playback is not playing!");
        }
    }

    public synchronized void start() {
        if (isPausedState()) {
            resumeActiveElements();
            this.mAction = SmilPlayerAction.START;
            notifyAll();
        } else if (isPlayedState()) {
            play();
        } else {
            Log.w("Mms/smil", "Error State: Playback can not be started!");
        }
    }

    public synchronized void stop() {
        if (isPlayingState() || isPausedState()) {
            this.mAction = SmilPlayerAction.STOP;
            notifyAll();
        } else if (isPlayedState()) {
            actionStop();
        }
    }

    public synchronized void stopWhenReload() {
        endActiveElements();
    }

    public synchronized void reload() {
        if (isPlayingState() || isPausedState()) {
            this.mAction = SmilPlayerAction.RELOAD;
            notifyAll();
        } else if (isPlayedState()) {
            actionReload();
        }
    }

    public synchronized void next() {
        if (isPlayingState() || isPausedState()) {
            this.mAction = SmilPlayerAction.NEXT;
            notifyAll();
        }
    }

    public synchronized void prev() {
        if (isPlayingState() || isPausedState()) {
            this.mAction = SmilPlayerAction.PREV;
            notifyAll();
        }
    }

    private synchronized boolean isBeginOfSlide(TimelineEntry entry) {
        boolean z = false;
        synchronized (this) {
            if (entry.getAction() == 0) {
                z = entry.getElement() instanceof SmilParElementImpl;
            }
        }
        return z;
    }

    private synchronized void reloadActiveSlide() {
        this.mActiveElements.clear();
        beginSmilDocument();
        int i = this.mCurrentSlide;
        while (i < this.mCurrentElement && i < this.mAllEntries.size()) {
            actionEntry((TimelineEntry) this.mAllEntries.get(i));
            i++;
        }
        seekActiveMedia();
    }

    private synchronized void beginSmilDocument() {
        actionEntry((TimelineEntry) this.mAllEntries.get(0));
    }

    private synchronized double getOffsetTime(ElementTime element) {
        if (!(this.mAllEntries == null || this.mAllEntries.isEmpty())) {
            int i = this.mCurrentSlide;
            while (i < this.mCurrentElement && i < this.mAllEntries.size()) {
                TimelineEntry entry = (TimelineEntry) this.mAllEntries.get(i);
                if (element.equals(entry.getElement())) {
                    return entry.getOffsetTime() * 1000.0d;
                }
                i++;
            }
        }
        return -1.0d;
    }

    private synchronized void seekActiveMedia() {
        int i = this.mActiveElements.size() - 1;
        while (i >= 0) {
            ElementTime element = (ElementTime) this.mActiveElements.get(i);
            if (!(element instanceof SmilParElementImpl)) {
                double offset = getOffsetTime(element);
                if (offset >= 0.0d && offset <= ((double) this.mCurrentTime)) {
                    element.seekElement((float) (((double) this.mCurrentTime) - offset));
                }
                i--;
            } else {
                return;
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized void waitForEntry(long interval) throws InterruptedException {
        long overhead = 0;
        while (interval > 0) {
            long startAt = System.currentTimeMillis();
            long sleep = Math.min(interval, 200);
            if (overhead < sleep) {
                wait(sleep - overhead);
                this.mCurrentTime += sleep;
            } else {
                sleep = 0;
                this.mCurrentTime += overhead;
            }
            if (!isStopAction() && !isReloadAction() && !isPauseAction() && !isNextAction() && !isPrevAction()) {
                ((EventTarget) this.mRoot).dispatchEvent(this.mMediaTimeUpdatedEvent);
                interval -= 200;
                overhead = (System.currentTimeMillis() - startAt) - sleep;
            }
        }
    }

    public synchronized int getDuration() {
        if (this.mAllEntries == null || this.mAllEntries.isEmpty()) {
            return 0;
        }
        return ((int) ((TimelineEntry) this.mAllEntries.get(this.mAllEntries.size() - 1)).mOffsetTime) * 1000;
    }

    public synchronized int getCurrentPosition() {
        return (int) this.mCurrentTime;
    }

    private synchronized void endActiveElements() {
        for (int i = this.mActiveElements.size() - 1; i >= 0; i--) {
            ((ElementTime) this.mActiveElements.get(i)).endElement();
        }
    }

    private synchronized void pauseActiveElements() {
        for (int i = this.mActiveElements.size() - 1; i >= 0; i--) {
            ((ElementTime) this.mActiveElements.get(i)).pauseElement();
        }
    }

    private synchronized void resumeActiveElements() {
        int size = this.mActiveElements.size();
        for (int i = 0; i < size; i++) {
            ((ElementTime) this.mActiveElements.get(i)).resumeElement();
        }
    }

    private synchronized void waitForWakeUp() {
        while (true) {
            try {
                boolean z;
                if (isStartAction() || isStopAction() || isReloadAction() || isNextAction()) {
                    z = true;
                } else {
                    z = isPrevAction();
                }
                if (z) {
                    break;
                }
                wait(200);
            } catch (InterruptedException e) {
                Log.e("Mms/smil", "Unexpected InterruptedException.", e);
            }
        }
        if (isStartAction()) {
            this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
            this.mState = SmilPlayerState.PLAYING;
        }
        return;
    }

    private synchronized void actionEntry(TimelineEntry entry) {
        switch (entry.getAction()) {
            case 0:
                entry.getElement().beginElement();
                this.mActiveElements.add(entry.getElement());
                break;
            case 1:
                entry.getElement().endElement();
                this.mActiveElements.remove(entry.getElement());
                break;
        }
    }

    private synchronized TimelineEntry reloadCurrentEntry() {
        if (this.mCurrentElement >= this.mAllEntries.size()) {
            return null;
        }
        return (TimelineEntry) this.mAllEntries.get(this.mCurrentElement);
    }

    private void stopCurrentSlide() {
        HashSet<TimelineEntry> skippedEntries = new HashSet();
        int totalEntries = this.mAllEntries.size();
        for (int i = this.mCurrentElement; i < totalEntries; i++) {
            TimelineEntry entry = (TimelineEntry) this.mAllEntries.get(i);
            int action = entry.getAction();
            if ((entry.getElement() instanceof SmilParElementImpl) && action == 1) {
                actionEntry(entry);
                this.mCurrentElement = i;
                return;
            }
            if (action == 1 && !skippedEntries.contains(entry)) {
                actionEntry(entry);
            } else if (action == 0) {
                skippedEntries.add(entry);
            }
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private synchronized TimelineEntry loadNextSlide() {
        TimelineEntry entry;
        int totalEntries = this.mAllEntries.size();
        for (int i = this.mCurrentElement; i < totalEntries; i++) {
            entry = (TimelineEntry) this.mAllEntries.get(i);
            if (isBeginOfSlide(entry)) {
                this.mCurrentElement = i;
                this.mCurrentSlide = i;
                this.mCurrentTime = (long) (entry.getOffsetTime() * 1000.0d);
                return entry;
            }
        }
        this.mCurrentElement++;
        entry = null;
        if (this.mCurrentElement < totalEntries) {
            entry = (TimelineEntry) this.mAllEntries.get(this.mCurrentElement);
            this.mCurrentTime = (long) (entry.getOffsetTime() * 1000.0d);
        }
    }

    private synchronized TimelineEntry loadPrevSlide() {
        Throwable th;
        int latestBeginEntryIndex = -1;
        try {
            int i = this.mCurrentSlide;
            int skippedSlides = 1;
            while (i >= 0) {
                int skippedSlides2;
                try {
                    TimelineEntry entry = (TimelineEntry) this.mAllEntries.get(i);
                    if (isBeginOfSlide(entry)) {
                        latestBeginEntryIndex = i;
                        skippedSlides2 = skippedSlides - 1;
                        if (skippedSlides == 0) {
                            this.mCurrentElement = i;
                            this.mCurrentSlide = i;
                            this.mCurrentTime = (long) (entry.getOffsetTime() * 1000.0d);
                            return entry;
                        }
                    } else {
                        skippedSlides2 = skippedSlides;
                    }
                    i--;
                    skippedSlides = skippedSlides2;
                } catch (Throwable th2) {
                    th = th2;
                    skippedSlides2 = skippedSlides;
                }
            }
            if (latestBeginEntryIndex == -1) {
                return null;
            }
            this.mCurrentElement = latestBeginEntryIndex;
            this.mCurrentSlide = latestBeginEntryIndex;
            return (TimelineEntry) this.mAllEntries.get(this.mCurrentElement);
        } catch (Throwable th3) {
            th = th3;
            throw th;
        }
    }

    private synchronized TimelineEntry actionNext() {
        stopCurrentSlide();
        return loadNextSlide();
    }

    private synchronized TimelineEntry actionPrev() {
        stopCurrentSlide();
        return loadPrevSlide();
    }

    private synchronized void actionPause() {
        pauseActiveElements();
        this.mState = SmilPlayerState.PAUSED;
        this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
    }

    private synchronized void actionStop() {
        endActiveElements();
        this.mCurrentTime = 0;
        this.mCurrentElement = 0;
        this.mCurrentSlide = 0;
        this.mState = SmilPlayerState.STOPPED;
        this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
    }

    private synchronized void actionReload() {
        reloadActiveSlide();
        this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
    }

    public synchronized void safeRun() {
        if (!isStoppedState()) {
            synchronized (this.mAllEntries) {
                this.mCurrentElement = 0;
                while (this.mCurrentElement < this.mAllEntries.size()) {
                    if (this.mCurrentElement < this.mAllEntries.size()) {
                        TimelineEntry entry = (TimelineEntry) this.mAllEntries.get(this.mCurrentElement);
                        if (isBeginOfSlide(entry)) {
                            this.mCurrentSlide = this.mCurrentElement;
                        }
                        long offset = (long) (entry.getOffsetTime() * 1000.0d);
                        while (offset > this.mCurrentTime) {
                            try {
                                waitForEntry(offset - this.mCurrentTime);
                            } catch (InterruptedException e) {
                                Log.e("Mms/smil", "Unexpected InterruptedException.", e);
                            }
                            while (true) {
                                if (isPauseAction() || isStopAction() || isReloadAction() || isNextAction() || isPrevAction()) {
                                    if (isPauseAction()) {
                                        actionPause();
                                        waitForWakeUp();
                                    }
                                    if (isStopAction()) {
                                        actionStop();
                                        return;
                                    }
                                    if (isReloadAction()) {
                                        actionReload();
                                        entry = reloadCurrentEntry();
                                        if (entry == null) {
                                            return;
                                        } else if (isPausedState()) {
                                            this.mAction = SmilPlayerAction.PAUSE;
                                        }
                                    }
                                    if (isNextAction()) {
                                        TimelineEntry nextEntry = actionNext();
                                        if (nextEntry != null) {
                                            entry = nextEntry;
                                        }
                                        if (this.mState == SmilPlayerState.PAUSED) {
                                            this.mAction = SmilPlayerAction.PAUSE;
                                            actionEntry(entry);
                                        } else {
                                            this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
                                        }
                                        offset = this.mCurrentTime;
                                    }
                                    if (isPrevAction()) {
                                        TimelineEntry prevEntry = actionPrev();
                                        if (prevEntry != null) {
                                            entry = prevEntry;
                                        }
                                        if (this.mState == SmilPlayerState.PAUSED) {
                                            this.mAction = SmilPlayerAction.PAUSE;
                                            actionEntry(entry);
                                        } else {
                                            this.mAction = SmilPlayerAction.NO_ACTIVE_ACTION;
                                        }
                                        offset = this.mCurrentTime;
                                    }
                                }
                            }
                        }
                        this.mCurrentTime = offset;
                        actionEntry(entry);
                    }
                    this.mCurrentElement++;
                }
                this.mState = SmilPlayerState.PLAYED;
            }
        }
    }

    public synchronized int getCurrentSlide() {
        return this.mCurrentSlide;
    }

    public synchronized void replay() {
        this.mCurrentTime = 0;
        reload();
    }

    public synchronized int getCurrentSlideNum() {
        int slideNum;
        slideNum = 0;
        if (!(this.mAllEntries == null || this.mAllEntries.isEmpty())) {
            for (int i = (int) MathUtils.min(this.mCurrentElement, this.mAllEntries.size() - 1); i >= 0; i--) {
                if (isBeginOfSlide((TimelineEntry) this.mAllEntries.get(i))) {
                    slideNum++;
                }
            }
        }
        return slideNum;
    }
}
