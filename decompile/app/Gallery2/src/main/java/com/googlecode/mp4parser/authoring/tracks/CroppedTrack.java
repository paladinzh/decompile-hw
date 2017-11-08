package com.googlecode.mp4parser.authoring.tracks;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox.Entry;
import com.googlecode.mp4parser.authoring.AbstractTrack;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.TrackMetaData;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

public class CroppedTrack extends AbstractTrack {
    static final /* synthetic */ boolean -assertionsDisabled = (!CroppedTrack.class.desiredAssertionStatus());
    private int fromSample;
    Track origTrack;
    private long[] syncSampleArray;
    private int toSample;

    public CroppedTrack(Track origTrack, long fromSample, long toSample) {
        Object obj = 1;
        this.origTrack = origTrack;
        if (!-assertionsDisabled) {
            if ((fromSample <= 2147483647L ? 1 : null) == null) {
                throw new AssertionError();
            }
        }
        if (!-assertionsDisabled) {
            if (toSample > 2147483647L) {
                obj = null;
            }
            if (obj == null) {
                throw new AssertionError();
            }
        }
        this.fromSample = (int) fromSample;
        this.toSample = (int) toSample;
    }

    public List<ByteBuffer> getSamples() {
        return this.origTrack.getSamples().subList(this.fromSample, this.toSample);
    }

    public SampleDescriptionBox getSampleDescriptionBox() {
        return this.origTrack.getSampleDescriptionBox();
    }

    public List<Entry> getDecodingTimeEntries() {
        if (this.origTrack.getDecodingTimeEntries() == null || this.origTrack.getDecodingTimeEntries().isEmpty()) {
            return null;
        }
        long[] nuDecodingTimes = new long[(this.toSample - this.fromSample)];
        System.arraycopy(TimeToSampleBox.blowupTimeToSamples(this.origTrack.getDecodingTimeEntries()), this.fromSample, nuDecodingTimes, 0, this.toSample - this.fromSample);
        LinkedList<Entry> returnDecodingEntries = new LinkedList();
        for (long nuDecodingTime : nuDecodingTimes) {
            if (returnDecodingEntries.isEmpty() || ((Entry) returnDecodingEntries.getLast()).getDelta() != nuDecodingTime) {
                returnDecodingEntries.add(new Entry(1, nuDecodingTime));
            } else {
                Entry e = (Entry) returnDecodingEntries.getLast();
                e.setCount(e.getCount() + 1);
            }
        }
        return returnDecodingEntries;
    }

    public List<CompositionTimeToSample.Entry> getCompositionTimeEntries() {
        if (this.origTrack.getCompositionTimeEntries() == null || this.origTrack.getCompositionTimeEntries().isEmpty()) {
            return null;
        }
        int[] nuCompositionTimes = new int[(this.toSample - this.fromSample)];
        System.arraycopy(CompositionTimeToSample.blowupCompositionTimes(this.origTrack.getCompositionTimeEntries()), this.fromSample, nuCompositionTimes, 0, this.toSample - this.fromSample);
        LinkedList<CompositionTimeToSample.Entry> returnDecodingEntries = new LinkedList();
        for (int nuDecodingTime : nuCompositionTimes) {
            if (returnDecodingEntries.isEmpty() || ((CompositionTimeToSample.Entry) returnDecodingEntries.getLast()).getOffset() != nuDecodingTime) {
                returnDecodingEntries.add(new CompositionTimeToSample.Entry(1, nuDecodingTime));
            } else {
                CompositionTimeToSample.Entry e = (CompositionTimeToSample.Entry) returnDecodingEntries.getLast();
                e.setCount(e.getCount() + 1);
            }
        }
        return returnDecodingEntries;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized long[] getSyncSamples() {
        synchronized (this) {
            if (this.syncSampleArray != null) {
                return this.syncSampleArray;
            } else if (this.origTrack.getSyncSamples() == null || this.origTrack.getSyncSamples().length <= 0) {
            } else {
                List<Long> syncSamples = new LinkedList();
                for (long l : this.origTrack.getSyncSamples()) {
                    if (l >= ((long) this.fromSample) && l < ((long) this.toSample)) {
                        syncSamples.add(Long.valueOf(l - ((long) this.fromSample)));
                    }
                }
                this.syncSampleArray = new long[syncSamples.size()];
                for (int i = 0; i < this.syncSampleArray.length; i++) {
                    this.syncSampleArray[i] = ((Long) syncSamples.get(i)).longValue();
                }
                return this.syncSampleArray;
            }
        }
    }

    public List<SampleDependencyTypeBox.Entry> getSampleDependencies() {
        if (this.origTrack.getSampleDependencies() == null || this.origTrack.getSampleDependencies().isEmpty()) {
            return null;
        }
        return this.origTrack.getSampleDependencies().subList(this.fromSample, this.toSample);
    }

    public TrackMetaData getTrackMetaData() {
        return this.origTrack.getTrackMetaData();
    }

    public String getHandler() {
        return this.origTrack.getHandler();
    }

    public Box getMediaHeaderBox() {
        return this.origTrack.getMediaHeaderBox();
    }
}
