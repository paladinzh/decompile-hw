package com.googlecode.mp4parser.authoring.builder;

import com.coremedia.iso.BoxParser;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.IsoTypeWriter;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.CompositionTimeToSample;
import com.coremedia.iso.boxes.CompositionTimeToSample.Entry;
import com.coremedia.iso.boxes.ContainerBox;
import com.coremedia.iso.boxes.DataEntryUrlBox;
import com.coremedia.iso.boxes.DataInformationBox;
import com.coremedia.iso.boxes.DataReferenceBox;
import com.coremedia.iso.boxes.FileTypeBox;
import com.coremedia.iso.boxes.HandlerBox;
import com.coremedia.iso.boxes.MediaBox;
import com.coremedia.iso.boxes.MediaHeaderBox;
import com.coremedia.iso.boxes.MediaInformationBox;
import com.coremedia.iso.boxes.MovieBox;
import com.coremedia.iso.boxes.MovieHeaderBox;
import com.coremedia.iso.boxes.SampleDependencyTypeBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleTableBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.StaticChunkOffsetBox;
import com.coremedia.iso.boxes.SyncSampleBox;
import com.coremedia.iso.boxes.TimeToSampleBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.TrackHeaderBox;
import com.googlecode.mp4parser.authoring.DateHelper;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.util.CastUtils;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class DefaultMp4Builder {
    static final /* synthetic */ boolean -assertionsDisabled;
    private static Logger LOG = Logger.getLogger(DefaultMp4Builder.class.getName());
    public int STEPSIZE = 64;
    Set<StaticChunkOffsetBox> chunkOffsetBoxes = new HashSet();
    private FragmentIntersectionFinder intersectionFinder = new TwoSecondIntersectionFinder();
    HashMap<Track, List<ByteBuffer>> track2Sample = new HashMap();
    HashMap<Track, long[]> track2SampleSizes = new HashMap();

    private class InterleaveChunkMdat implements Box {
        long contentSize;
        ContainerBox parent;
        List<ByteBuffer> samples;
        List<Track> tracks;

        public ContainerBox getParent() {
            return this.parent;
        }

        public void setParent(ContainerBox parent) {
            this.parent = parent;
        }

        public void parse(ReadableByteChannel readableByteChannel, ByteBuffer header, long contentSize, BoxParser boxParser) throws IOException {
        }

        private InterleaveChunkMdat(Movie movie) {
            this.samples = new ArrayList();
            this.contentSize = 0;
            this.tracks = movie.getTracks();
            Map<Track, int[]> chunks = new HashMap();
            for (Track track : movie.getTracks()) {
                chunks.put(track, DefaultMp4Builder.this.getChunkSizes(track, movie));
            }
            for (int i = 0; i < ((int[]) chunks.values().iterator().next()).length; i++) {
                for (Track track2 : this.tracks) {
                    int j;
                    int[] chunkSizes = (int[]) chunks.get(track2);
                    long firstSampleOfChunk = 0;
                    for (j = 0; j < i; j++) {
                        firstSampleOfChunk += (long) chunkSizes[j];
                    }
                    for (j = CastUtils.l2i(firstSampleOfChunk); ((long) j) < ((long) chunkSizes[i]) + firstSampleOfChunk; j++) {
                        ByteBuffer s = (ByteBuffer) ((List) DefaultMp4Builder.this.track2Sample.get(track2)).get(j);
                        this.contentSize += (long) s.limit();
                        this.samples.add((ByteBuffer) s.rewind());
                    }
                }
            }
        }

        public long getDataOffset() {
            long offset = 16;
            for (Box b = this; b.getParent() != null; b = b.getParent()) {
                for (Box box : b.getParent().getBoxes()) {
                    if (b == box) {
                        break;
                    }
                    offset += box.getSize();
                }
            }
            return offset;
        }

        public String getType() {
            return "mdat";
        }

        public long getSize() {
            return this.contentSize + 16;
        }

        private boolean isSmallBox(long contentSize) {
            return 8 + contentSize < 4294967296L;
        }

        public void getBox(WritableByteChannel writableByteChannel) throws IOException {
            ByteBuffer bb = ByteBuffer.allocate(16);
            long size = getSize();
            if (isSmallBox(size)) {
                IsoTypeWriter.writeUInt32(bb, size);
            } else {
                IsoTypeWriter.writeUInt32(bb, 1);
            }
            bb.put(IsoFile.fourCCtoBytes("mdat"));
            if (isSmallBox(size)) {
                bb.put(new byte[8]);
            } else {
                IsoTypeWriter.writeUInt64(bb, size);
            }
            bb.rewind();
            writableByteChannel.write(bb);
            if (writableByteChannel instanceof GatheringByteChannel) {
                List<ByteBuffer> nuSamples = DefaultMp4Builder.this.unifyAdjacentBuffers(this.samples);
                for (int i = 0; ((double) i) < Math.ceil(((double) nuSamples.size()) / ((double) DefaultMp4Builder.this.STEPSIZE)); i++) {
                    List<ByteBuffer> sublist = nuSamples.subList(i * DefaultMp4Builder.this.STEPSIZE, (i + 1) * DefaultMp4Builder.this.STEPSIZE < nuSamples.size() ? (i + 1) * DefaultMp4Builder.this.STEPSIZE : nuSamples.size());
                    ByteBuffer[] sampleArray = (ByteBuffer[]) sublist.toArray(new ByteBuffer[sublist.size()]);
                    do {
                        ((GatheringByteChannel) writableByteChannel).write(sampleArray);
                    } while (sampleArray[sampleArray.length - 1].remaining() > 0);
                }
                return;
            }
            for (ByteBuffer sample : this.samples) {
                sample.rewind();
                writableByteChannel.write(sample);
            }
        }
    }

    static {
        boolean z;
        if (DefaultMp4Builder.class.desiredAssertionStatus()) {
            z = false;
        } else {
            z = true;
        }
        -assertionsDisabled = z;
    }

    public IsoFile build(Movie movie) {
        LOG.fine("Creating movie " + movie);
        for (Track track : movie.getTracks()) {
            int i;
            List<ByteBuffer> samples = track.getSamples();
            putSamples(track, samples);
            long[] sizes = new long[samples.size()];
            for (i = 0; i < sizes.length; i++) {
                sizes[i] = (long) ((ByteBuffer) samples.get(i)).limit();
            }
            putSampleSizes(track, sizes);
        }
        IsoFile isoFile = new IsoFile();
        List<String> minorBrands = new LinkedList();
        minorBrands.add("isom");
        minorBrands.add("iso2");
        minorBrands.add("avc1");
        isoFile.addBox(new FileTypeBox("isom", 0, minorBrands));
        isoFile.addBox(createMovieBox(movie));
        InterleaveChunkMdat mdat = new InterleaveChunkMdat(movie);
        isoFile.addBox(mdat);
        long dataOffset = mdat.getDataOffset();
        for (StaticChunkOffsetBox chunkOffsetBox : this.chunkOffsetBoxes) {
            long[] offsets = chunkOffsetBox.getChunkOffsets();
            for (i = 0; i < offsets.length; i++) {
                offsets[i] = offsets[i] + dataOffset;
            }
        }
        return isoFile;
    }

    protected long[] putSampleSizes(Track track, long[] sizes) {
        return (long[]) this.track2SampleSizes.put(track, sizes);
    }

    protected List<ByteBuffer> putSamples(Track track, List<ByteBuffer> samples) {
        return (List) this.track2Sample.put(track, samples);
    }

    private MovieBox createMovieBox(Movie movie) {
        Box udta;
        MovieBox movieBox = new MovieBox();
        MovieHeaderBox mvhd = new MovieHeaderBox();
        mvhd.setCreationTime(DateHelper.convert(new Date()));
        mvhd.setModificationTime(DateHelper.convert(new Date()));
        long movieTimeScale = getTimescale(movie);
        long duration = 0;
        for (Track track : movie.getTracks()) {
            long tracksDuration = (getDuration(track) * movieTimeScale) / track.getTrackMetaData().getTimescale();
            if (tracksDuration > duration) {
                duration = tracksDuration;
            }
        }
        mvhd.setDuration(duration);
        mvhd.setTimescale(movieTimeScale);
        long nextTrackId = 0;
        for (Track track2 : movie.getTracks()) {
            if (nextTrackId < track2.getTrackMetaData().getTrackId()) {
                nextTrackId = track2.getTrackMetaData().getTrackId();
            }
        }
        mvhd.setNextTrackId(nextTrackId + 1);
        if (mvhd.getCreationTime() < 4294967296L && mvhd.getModificationTime() < 4294967296L) {
            if (mvhd.getDuration() >= 4294967296L) {
            }
            movieBox.addBox(mvhd);
            for (Track track22 : movie.getTracks()) {
                movieBox.addBox(createTrackBox(track22, movie));
            }
            udta = createUdta(movie);
            if (udta != null) {
                movieBox.addBox(udta);
            }
            return movieBox;
        }
        mvhd.setVersion(1);
        movieBox.addBox(mvhd);
        while (track$iterator.hasNext()) {
            movieBox.addBox(createTrackBox(track22, movie));
        }
        udta = createUdta(movie);
        if (udta != null) {
            movieBox.addBox(udta);
        }
        return movieBox;
    }

    protected Box createUdta(Movie movie) {
        return null;
    }

    private TrackBox createTrackBox(Track track, Movie movie) {
        MediaBox mdia;
        MediaHeaderBox mdhd;
        HandlerBox hdlr;
        MediaInformationBox minf;
        DataInformationBox dinf;
        DataReferenceBox dref;
        DataEntryUrlBox url;
        SampleTableBox stbl;
        TimeToSampleBox stts;
        List<Entry> compositionTimeToSampleEntries;
        CompositionTimeToSample ctts;
        long[] syncSamples;
        SyncSampleBox stss;
        SampleDependencyTypeBox sdtp;
        HashMap<Track, int[]> track2ChunkSizes;
        int[] tracksChunkSizes;
        SampleToChunkBox stsc;
        long lastChunkSize;
        int i;
        SampleSizeBox stsz;
        StaticChunkOffsetBox stco;
        long offset;
        long[] chunkOffset;
        int[] chunkSizes;
        long firstSampleOfChunk;
        int j;
        LOG.info("Creating Mp4TrackImpl " + track);
        TrackBox trackBox = new TrackBox();
        TrackHeaderBox tkhd = new TrackHeaderBox();
        int flags = 0;
        if (track.isEnabled()) {
            flags = 1;
        }
        if (track.isInMovie()) {
            flags += 2;
        }
        if (track.isInPreview()) {
            flags += 4;
        }
        if (track.isInPoster()) {
            flags += 8;
        }
        tkhd.setFlags(flags);
        tkhd.setAlternateGroup(track.getTrackMetaData().getGroup());
        tkhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        tkhd.setDuration((getDuration(track) * getTimescale(movie)) / track.getTrackMetaData().getTimescale());
        tkhd.setHeight(track.getTrackMetaData().getHeight());
        tkhd.setWidth(track.getTrackMetaData().getWidth());
        tkhd.setLayer(track.getTrackMetaData().getLayer());
        tkhd.setModificationTime(DateHelper.convert(new Date()));
        tkhd.setTrackId(track.getTrackMetaData().getTrackId());
        tkhd.setVolume(track.getTrackMetaData().getVolume());
        tkhd.setMatrix(track.getTrackMetaData().getMatrix());
        if (tkhd.getCreationTime() < 4294967296L && tkhd.getModificationTime() < 4294967296L) {
            if (tkhd.getDuration() >= 4294967296L) {
            }
            trackBox.addBox(tkhd);
            mdia = new MediaBox();
            trackBox.addBox(mdia);
            mdhd = new MediaHeaderBox();
            mdhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
            mdhd.setDuration(getDuration(track));
            mdhd.setTimescale(track.getTrackMetaData().getTimescale());
            mdhd.setLanguage(track.getTrackMetaData().getLanguage());
            mdia.addBox(mdhd);
            hdlr = new HandlerBox();
            mdia.addBox(hdlr);
            hdlr.setHandlerType(track.getHandler());
            minf = new MediaInformationBox();
            minf.addBox(track.getMediaHeaderBox());
            dinf = new DataInformationBox();
            dref = new DataReferenceBox();
            dinf.addBox(dref);
            url = new DataEntryUrlBox();
            url.setFlags(1);
            dref.addBox(url);
            minf.addBox(dinf);
            stbl = new SampleTableBox();
            stbl.addBox(track.getSampleDescriptionBox());
            if (!(track.getDecodingTimeEntries() == null || track.getDecodingTimeEntries().isEmpty())) {
                stts = new TimeToSampleBox();
                stts.setEntries(track.getDecodingTimeEntries());
                stbl.addBox(stts);
            }
            compositionTimeToSampleEntries = track.getCompositionTimeEntries();
            if (!(compositionTimeToSampleEntries == null || compositionTimeToSampleEntries.isEmpty())) {
                ctts = new CompositionTimeToSample();
                ctts.setEntries(compositionTimeToSampleEntries);
                stbl.addBox(ctts);
            }
            syncSamples = track.getSyncSamples();
            if (syncSamples != null && syncSamples.length > 0) {
                stss = new SyncSampleBox();
                stss.setSampleNumber(syncSamples);
                stbl.addBox(stss);
            }
            if (!(track.getSampleDependencies() == null || track.getSampleDependencies().isEmpty())) {
                sdtp = new SampleDependencyTypeBox();
                sdtp.setEntries(track.getSampleDependencies());
                stbl.addBox(sdtp);
            }
            track2ChunkSizes = new HashMap();
            for (Track current : movie.getTracks()) {
                track2ChunkSizes.put(current, getChunkSizes(current, movie));
            }
            tracksChunkSizes = (int[]) track2ChunkSizes.get(track);
            stsc = new SampleToChunkBox();
            stsc.setEntries(new LinkedList());
            lastChunkSize = -2147483648L;
            for (i = 0; i < tracksChunkSizes.length; i++) {
                if (lastChunkSize != ((long) tracksChunkSizes[i])) {
                    stsc.getEntries().add(new SampleToChunkBox.Entry((long) (i + 1), (long) tracksChunkSizes[i], 1));
                    lastChunkSize = (long) tracksChunkSizes[i];
                }
            }
            stbl.addBox(stsc);
            stsz = new SampleSizeBox();
            stsz.setSampleSizes((long[]) this.track2SampleSizes.get(track));
            stbl.addBox(stsz);
            stco = new StaticChunkOffsetBox();
            this.chunkOffsetBoxes.add(stco);
            offset = 0;
            chunkOffset = new long[tracksChunkSizes.length];
            if (LOG.isLoggable(Level.FINE)) {
                LOG.fine("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId());
            }
            for (i = 0; i < tracksChunkSizes.length; i++) {
                if (LOG.isLoggable(Level.FINER)) {
                    LOG.finer("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId() + " chunk " + i);
                }
                for (Track current2 : movie.getTracks()) {
                    if (LOG.isLoggable(Level.FINEST)) {
                        LOG.finest("Adding offsets of track_" + current2.getTrackMetaData().getTrackId());
                    }
                    chunkSizes = (int[]) track2ChunkSizes.get(current2);
                    firstSampleOfChunk = 0;
                    for (j = 0; j < i; j++) {
                        firstSampleOfChunk += (long) chunkSizes[j];
                    }
                    if (current2 == track) {
                        chunkOffset[i] = offset;
                    }
                    for (j = CastUtils.l2i(firstSampleOfChunk); ((long) j) < ((long) chunkSizes[i]) + firstSampleOfChunk; j++) {
                        offset += ((long[]) this.track2SampleSizes.get(current2))[j];
                    }
                }
            }
            stco.setChunkOffsets(chunkOffset);
            stbl.addBox(stco);
            minf.addBox(stbl);
            mdia.addBox(minf);
            return trackBox;
        }
        tkhd.setVersion(1);
        trackBox.addBox(tkhd);
        mdia = new MediaBox();
        trackBox.addBox(mdia);
        mdhd = new MediaHeaderBox();
        mdhd.setCreationTime(DateHelper.convert(track.getTrackMetaData().getCreationTime()));
        mdhd.setDuration(getDuration(track));
        mdhd.setTimescale(track.getTrackMetaData().getTimescale());
        mdhd.setLanguage(track.getTrackMetaData().getLanguage());
        mdia.addBox(mdhd);
        hdlr = new HandlerBox();
        mdia.addBox(hdlr);
        hdlr.setHandlerType(track.getHandler());
        minf = new MediaInformationBox();
        minf.addBox(track.getMediaHeaderBox());
        dinf = new DataInformationBox();
        dref = new DataReferenceBox();
        dinf.addBox(dref);
        url = new DataEntryUrlBox();
        url.setFlags(1);
        dref.addBox(url);
        minf.addBox(dinf);
        stbl = new SampleTableBox();
        stbl.addBox(track.getSampleDescriptionBox());
        stts = new TimeToSampleBox();
        stts.setEntries(track.getDecodingTimeEntries());
        stbl.addBox(stts);
        compositionTimeToSampleEntries = track.getCompositionTimeEntries();
        ctts = new CompositionTimeToSample();
        ctts.setEntries(compositionTimeToSampleEntries);
        stbl.addBox(ctts);
        syncSamples = track.getSyncSamples();
        stss = new SyncSampleBox();
        stss.setSampleNumber(syncSamples);
        stbl.addBox(stss);
        sdtp = new SampleDependencyTypeBox();
        sdtp.setEntries(track.getSampleDependencies());
        stbl.addBox(sdtp);
        track2ChunkSizes = new HashMap();
        for (Track current22 : movie.getTracks()) {
            track2ChunkSizes.put(current22, getChunkSizes(current22, movie));
        }
        tracksChunkSizes = (int[]) track2ChunkSizes.get(track);
        stsc = new SampleToChunkBox();
        stsc.setEntries(new LinkedList());
        lastChunkSize = -2147483648L;
        for (i = 0; i < tracksChunkSizes.length; i++) {
            if (lastChunkSize != ((long) tracksChunkSizes[i])) {
                stsc.getEntries().add(new SampleToChunkBox.Entry((long) (i + 1), (long) tracksChunkSizes[i], 1));
                lastChunkSize = (long) tracksChunkSizes[i];
            }
        }
        stbl.addBox(stsc);
        stsz = new SampleSizeBox();
        stsz.setSampleSizes((long[]) this.track2SampleSizes.get(track));
        stbl.addBox(stsz);
        stco = new StaticChunkOffsetBox();
        this.chunkOffsetBoxes.add(stco);
        offset = 0;
        chunkOffset = new long[tracksChunkSizes.length];
        if (LOG.isLoggable(Level.FINE)) {
            LOG.fine("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId());
        }
        for (i = 0; i < tracksChunkSizes.length; i++) {
            if (LOG.isLoggable(Level.FINER)) {
                LOG.finer("Calculating chunk offsets for track_" + track.getTrackMetaData().getTrackId() + " chunk " + i);
            }
            for (Track current222 : movie.getTracks()) {
                if (LOG.isLoggable(Level.FINEST)) {
                    LOG.finest("Adding offsets of track_" + current222.getTrackMetaData().getTrackId());
                }
                chunkSizes = (int[]) track2ChunkSizes.get(current222);
                firstSampleOfChunk = 0;
                for (j = 0; j < i; j++) {
                    firstSampleOfChunk += (long) chunkSizes[j];
                }
                if (current222 == track) {
                    chunkOffset[i] = offset;
                }
                for (j = CastUtils.l2i(firstSampleOfChunk); ((long) j) < ((long) chunkSizes[i]) + firstSampleOfChunk; j++) {
                    offset += ((long[]) this.track2SampleSizes.get(current222))[j];
                }
            }
        }
        stco.setChunkOffsets(chunkOffset);
        stbl.addBox(stco);
        minf.addBox(stbl);
        mdia.addBox(minf);
        return trackBox;
    }

    int[] getChunkSizes(Track track, Movie movie) {
        long[] referenceChunkStarts = this.intersectionFinder.sampleNumbers(track, movie);
        int[] chunkSizes = new int[referenceChunkStarts.length];
        for (int i = 0; i < referenceChunkStarts.length; i++) {
            long end;
            long start = referenceChunkStarts[i] - 1;
            if (referenceChunkStarts.length == i + 1) {
                end = (long) track.getSamples().size();
            } else {
                end = referenceChunkStarts[i + 1] - 1;
            }
            chunkSizes[i] = CastUtils.l2i(end - start);
        }
        if (!-assertionsDisabled) {
            if ((((long) ((List) this.track2Sample.get(track)).size()) == sum(chunkSizes) ? 1 : null) == null) {
                throw new AssertionError("The number of samples and the sum of all chunk lengths must be equal");
            }
        }
        return chunkSizes;
    }

    private static long sum(int[] ls) {
        long rc = 0;
        for (int i : ls) {
            rc += (long) i;
        }
        return rc;
    }

    protected static long getDuration(Track track) {
        long duration = 0;
        for (TimeToSampleBox.Entry entry : track.getDecodingTimeEntries()) {
            duration += entry.getCount() * entry.getDelta();
        }
        return duration;
    }

    public long getTimescale(Movie movie) {
        long timescale = ((Track) movie.getTracks().iterator().next()).getTrackMetaData().getTimescale();
        for (Track track : movie.getTracks()) {
            timescale = gcd(track.getTrackMetaData().getTimescale(), timescale);
        }
        return timescale;
    }

    public static long gcd(long a, long b) {
        if (b == 0) {
            return a;
        }
        return gcd(b, a % b);
    }

    public List<ByteBuffer> unifyAdjacentBuffers(List<ByteBuffer> samples) {
        ArrayList<ByteBuffer> nuSamples = new ArrayList(samples.size());
        for (ByteBuffer buffer : samples) {
            int lastIndex = nuSamples.size() - 1;
            if (lastIndex >= 0 && buffer.hasArray() && ((ByteBuffer) nuSamples.get(lastIndex)).hasArray() && buffer.array() == ((ByteBuffer) nuSamples.get(lastIndex)).array()) {
                if (((ByteBuffer) nuSamples.get(lastIndex)).limit() + ((ByteBuffer) nuSamples.get(lastIndex)).arrayOffset() == buffer.arrayOffset()) {
                    ByteBuffer oldBuffer = (ByteBuffer) nuSamples.remove(lastIndex);
                    nuSamples.add(ByteBuffer.wrap(buffer.array(), oldBuffer.arrayOffset(), oldBuffer.limit() + buffer.limit()).slice());
                }
            }
            if (lastIndex >= 0 && (buffer instanceof MappedByteBuffer) && (nuSamples.get(lastIndex) instanceof MappedByteBuffer) && ((ByteBuffer) nuSamples.get(lastIndex)).limit() == ((ByteBuffer) nuSamples.get(lastIndex)).capacity() - buffer.capacity()) {
                oldBuffer = (ByteBuffer) nuSamples.get(lastIndex);
                oldBuffer.limit(buffer.limit() + oldBuffer.limit());
            } else {
                nuSamples.add(buffer);
            }
        }
        return nuSamples;
    }
}
