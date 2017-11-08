package com.googlecode.mp4parser.authoring;

import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample.Entry;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleDescriptionBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import java.nio.ByteBuffer;
import java.util.List;

public interface Track {
    List<Entry> getCompositionTimeEntries();

    List<TimeToSampleBox.Entry> getDecodingTimeEntries();

    String getHandler();

    Box getMediaHeaderBox();

    List<SampleDependencyTypeBox.Entry> getSampleDependencies();

    SampleDescriptionBox getSampleDescriptionBox();

    List<ByteBuffer> getSamples();

    long[] getSyncSamples();

    TrackMetaData getTrackMetaData();

    boolean isEnabled();

    boolean isInMovie();

    boolean isInPoster();

    boolean isInPreview();
}
