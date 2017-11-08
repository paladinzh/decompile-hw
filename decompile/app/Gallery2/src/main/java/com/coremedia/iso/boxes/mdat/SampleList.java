package com.coremedia.iso.boxes.mdat;

import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.Box;
import com.coremedia.iso.boxes.ChunkOffsetBox;
import com.coremedia.iso.boxes.SampleSizeBox;
import com.coremedia.iso.boxes.SampleToChunkBox;
import com.coremedia.iso.boxes.TrackBox;
import com.coremedia.iso.boxes.fragment.MovieExtendsBox;
import com.coremedia.iso.boxes.fragment.MovieFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackExtendsBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentBox;
import com.coremedia.iso.boxes.fragment.TrackFragmentHeaderBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox;
import com.coremedia.iso.boxes.fragment.TrackRunBox.Entry;
import com.googlecode.mp4parser.util.CastUtils;
import java.nio.ByteBuffer;
import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SampleList extends AbstractList<ByteBuffer> {
    IsoFile isoFile;
    HashMap<MediaDataBox, Long> mdatEndCache = new HashMap();
    HashMap<MediaDataBox, Long> mdatStartCache = new HashMap();
    MediaDataBox[] mdats;
    long[] offsets;
    long[] sizes;

    public SampleList(TrackBox trackBox) {
        initIsoFile(trackBox.getIsoFile());
        SampleSizeBox sampleSizeBox = trackBox.getSampleTableBox().getSampleSizeBox();
        ChunkOffsetBox chunkOffsetBox = trackBox.getSampleTableBox().getChunkOffsetBox();
        SampleToChunkBox sampleToChunkBox = trackBox.getSampleTableBox().getSampleToChunkBox();
        long[] chunkOffsets = chunkOffsetBox != null ? chunkOffsetBox.getChunkOffsets() : new long[0];
        if (sampleToChunkBox != null && sampleToChunkBox.getEntries().size() > 0 && chunkOffsets.length > 0 && sampleSizeBox != null && sampleSizeBox.getSampleCount() > 0) {
            long[] numberOfSamplesInChunk = sampleToChunkBox.blowup(chunkOffsets.length);
            int sampleIndex = 0;
            if (sampleSizeBox.getSampleSize() > 0) {
                this.sizes = new long[CastUtils.l2i(sampleSizeBox.getSampleCount())];
                Arrays.fill(this.sizes, sampleSizeBox.getSampleSize());
            } else {
                this.sizes = sampleSizeBox.getSampleSizes();
            }
            this.offsets = new long[this.sizes.length];
            for (int i = 0; i < numberOfSamplesInChunk.length; i++) {
                long thisChunksNumberOfSamples = numberOfSamplesInChunk[i];
                long sampleOffset = chunkOffsets[i];
                for (int j = 0; ((long) j) < thisChunksNumberOfSamples; j++) {
                    long sampleSize = this.sizes[sampleIndex];
                    this.offsets[sampleIndex] = sampleOffset;
                    sampleOffset += sampleSize;
                    sampleIndex++;
                }
            }
        }
        List<MovieExtendsBox> movieExtendsBoxes = trackBox.getParent().getBoxes(MovieExtendsBox.class);
        if (movieExtendsBoxes.size() > 0) {
            Map<Long, Long> offsets2Sizes = new HashMap();
            for (TrackExtendsBox trackExtendsBox : ((MovieExtendsBox) movieExtendsBoxes.get(0)).getBoxes(TrackExtendsBox.class)) {
                if (trackExtendsBox.getTrackId() == trackBox.getTrackHeaderBox().getTrackId()) {
                    for (MovieFragmentBox movieFragmentBox : trackBox.getIsoFile().getBoxes(MovieFragmentBox.class)) {
                        offsets2Sizes.putAll(getOffsets(movieFragmentBox, trackBox.getTrackHeaderBox().getTrackId(), trackExtendsBox));
                    }
                }
            }
            if (this.sizes == null || this.offsets == null) {
                this.sizes = new long[0];
                this.offsets = new long[0];
            }
            splitToArrays(offsets2Sizes);
        }
    }

    private void splitToArrays(Map<Long, Long> offsets2Sizes) {
        List<Long> keys = new ArrayList(offsets2Sizes.keySet());
        Collections.sort(keys);
        long[] nuSizes = new long[(this.sizes.length + keys.size())];
        System.arraycopy(this.sizes, 0, nuSizes, 0, this.sizes.length);
        long[] nuOffsets = new long[(this.offsets.length + keys.size())];
        System.arraycopy(this.offsets, 0, nuOffsets, 0, this.offsets.length);
        for (int i = 0; i < keys.size(); i++) {
            nuOffsets[i + this.offsets.length] = ((Long) keys.get(i)).longValue();
            nuSizes[i + this.sizes.length] = ((Long) offsets2Sizes.get(keys.get(i))).longValue();
        }
        this.sizes = nuSizes;
        this.offsets = nuOffsets;
    }

    private void initIsoFile(IsoFile isoFile) {
        this.isoFile = isoFile;
        long currentOffset = 0;
        LinkedList<MediaDataBox> mdats = new LinkedList();
        for (Box b : this.isoFile.getBoxes()) {
            long currentSize = b.getSize();
            if ("mdat".equals(b.getType())) {
                if (b instanceof MediaDataBox) {
                    long contentOffset = currentOffset + ((long) ((MediaDataBox) b).getHeader().limit());
                    this.mdatStartCache.put((MediaDataBox) b, Long.valueOf(contentOffset));
                    this.mdatEndCache.put((MediaDataBox) b, Long.valueOf(contentOffset + currentSize));
                    mdats.add((MediaDataBox) b);
                } else {
                    throw new RuntimeException("Sample need to be in mdats and mdats need to be instanceof MediaDataBox");
                }
            }
            currentOffset += currentSize;
        }
        this.mdats = (MediaDataBox[]) mdats.toArray(new MediaDataBox[mdats.size()]);
    }

    public int size() {
        return this.sizes.length;
    }

    public ByteBuffer get(int index) {
        long offset = this.offsets[index];
        int sampleSize = CastUtils.l2i(this.sizes[index]);
        for (MediaDataBox mediaDataBox : this.mdats) {
            long start = ((Long) this.mdatStartCache.get(mediaDataBox)).longValue();
            long end = ((Long) this.mdatEndCache.get(mediaDataBox)).longValue();
            if (start <= offset && ((long) sampleSize) + offset <= end) {
                return mediaDataBox.getContent(offset - start, sampleSize);
            }
        }
        throw new RuntimeException("The sample with offset " + offset + " and size " + sampleSize + " is NOT located within an mdat");
    }

    Map<Long, Long> getOffsets(MovieFragmentBox moof, long trackId, TrackExtendsBox trex) {
        Map<Long, Long> offsets2Sizes = new HashMap();
        for (TrackFragmentBox trackFragmentBox : moof.getBoxes(TrackFragmentBox.class)) {
            if (trackFragmentBox.getTrackFragmentHeaderBox().getTrackId() == trackId) {
                long baseDataOffset;
                if (trackFragmentBox.getTrackFragmentHeaderBox().hasBaseDataOffset()) {
                    baseDataOffset = trackFragmentBox.getTrackFragmentHeaderBox().getBaseDataOffset();
                } else {
                    baseDataOffset = moof.getOffset();
                }
                for (TrackRunBox trun : trackFragmentBox.getBoxes(TrackRunBox.class)) {
                    long sampleBaseOffset = baseDataOffset + ((long) trun.getDataOffset());
                    TrackFragmentHeaderBox tfhd = ((TrackFragmentBox) trun.getParent()).getTrackFragmentHeaderBox();
                    long offset = 0;
                    for (Entry entry : trun.getEntries()) {
                        long sampleSize;
                        if (trun.isSampleSizePresent()) {
                            sampleSize = entry.getSampleSize();
                            offsets2Sizes.put(Long.valueOf(offset + sampleBaseOffset), Long.valueOf(sampleSize));
                            offset += sampleSize;
                        } else if (tfhd.hasDefaultSampleSize()) {
                            sampleSize = tfhd.getDefaultSampleSize();
                            offsets2Sizes.put(Long.valueOf(offset + sampleBaseOffset), Long.valueOf(sampleSize));
                            offset += sampleSize;
                        } else if (trex == null) {
                            throw new RuntimeException("File doesn't contain trex box but track fragments aren't fully self contained. Cannot determine sample size.");
                        } else {
                            sampleSize = trex.getDefaultSampleSize();
                            offsets2Sizes.put(Long.valueOf(offset + sampleBaseOffset), Long.valueOf(sampleSize));
                            offset += sampleSize;
                        }
                    }
                }
                continue;
            }
        }
        return offsets2Sizes;
    }
}
