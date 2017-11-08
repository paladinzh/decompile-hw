package com.huawei.gallery.video;

import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.os.Environment;
import com.android.gallery3d.common.ApiHelper;
import com.android.gallery3d.util.GalleryLog;
import com.coremedia.iso.IsoFile;
import com.coremedia.iso.boxes.TimeToSampleBox.Entry;
import com.googlecode.mp4parser.authoring.Movie;
import com.googlecode.mp4parser.authoring.Track;
import com.googlecode.mp4parser.authoring.builder.DefaultMp4Builder;
import com.googlecode.mp4parser.authoring.container.mp4.MovieCreator;
import com.googlecode.mp4parser.authoring.tracks.CroppedTrack;
import com.huawei.gallery.storage.GalleryStorage;
import com.huawei.gallery.storage.GalleryStorageManager;
import com.huawei.gallery.util.MyPrinter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public class VideoUtils {
    private static final MyPrinter LOG = new MyPrinter("VideoUtils");
    public static final File SAVE_TO;

    public interface VideoSplitListener {
        boolean isCanceled();

        void onEnd();

        void onProgress(int i, String str);

        void onStart(int i);
    }

    static {
        GalleryStorage innerGalleryStorage = GalleryStorageManager.getInstance().getInnerGalleryStorage();
        if (innerGalleryStorage == null) {
            SAVE_TO = new File(Environment.getExternalStorageDirectory().getPath(), "mmcache");
        } else {
            SAVE_TO = new File(innerGalleryStorage.getPath(), "mmcache");
        }
        if (!SAVE_TO.exists()) {
            LOG.d("temfile save directory not exist, make dir result : " + SAVE_TO.mkdirs());
        }
    }

    public static int splitBlockCount(long filesize) {
        return ((((int) filesize) + 18874368) - 1) / 18874368;
    }

    public static void splitVideo(String filepath, long filesize, int duration, VideoSplitListener listener) {
        if (filesize > 20971520 && filesize <= 2147483647L) {
            int num = splitBlockCount((long) ((int) filesize));
            if (listener != null) {
                listener.onStart(num);
            }
            duration *= 1000;
            int blockDuration = duration / num;
            File src = new File(filepath);
            File dir = SAVE_TO;
            String fileName = src.getName();
            int i = 0;
            File dst = null;
            while (i < num) {
                int startMs = i * blockDuration;
                File dst2;
                try {
                    int endMs = Math.min(startMs + blockDuration, duration);
                    dst2 = new File(dir, "split_" + i + fileName);
                    try {
                        LOG.d(String.format("src->%s, dst->%s, startMs->%s, endMs->%s", new Object[]{src, dst2, Integer.valueOf(startMs), Integer.valueOf(endMs)}));
                        if (listener != null && listener.isCanceled()) {
                            break;
                        }
                        startTrim(src, dst2, startMs, endMs);
                        if (listener != null) {
                            listener.onProgress(i + 1, dst2.getAbsolutePath());
                        }
                        i++;
                        dst = dst2;
                    } catch (IOException e) {
                    }
                } catch (IOException e2) {
                    dst2 = dst;
                } catch (Throwable th) {
                    Throwable th2 = th;
                    dst2 = dst;
                }
            }
            if (listener != null) {
                listener.onEnd();
            }
        }
        return;
        try {
            GalleryLog.i("VideoUtils", "startTrim() failed in splitVideo() method, reason: IOException");
            if (listener != null) {
                listener.onEnd();
            }
        } catch (Throwable th3) {
            th2 = th3;
            if (listener != null) {
                listener.onEnd();
            }
            throw th2;
        }
    }

    public static void startTrim(File src, File dst, int startMs, int endMs) throws IOException {
        if (ApiHelper.HAS_MEDIA_MUXER) {
            genVideoUsingMuxer(src.getPath(), dst.getPath(), startMs, endMs, true, true);
        } else {
            trimUsingMp4Parser(src, dst, startMs, endMs);
        }
    }

    private static void writeMovieIntoFile(File dst, Movie movie) throws IOException {
        if (!dst.exists()) {
            GalleryLog.d("VideoUtils", "create new file result " + dst.createNewFile());
        }
        IsoFile out = new DefaultMp4Builder().build(movie);
        FileOutputStream fos = new FileOutputStream(dst);
        FileChannel fc = fos.getChannel();
        out.getBox(fc);
        fc.close();
        fos.close();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static void genVideoUsingMuxer(String srcPath, String dstPath, int startMs, int endMs, boolean useAudio, boolean useVideo) throws IOException {
        MediaExtractor extractor = new MediaExtractor();
        extractor.setDataSource(srcPath);
        int trackCount = extractor.getTrackCount();
        MediaMuxer mediaMuxer = new MediaMuxer(dstPath, 0);
        HashMap<Integer, Integer> indexMap = new HashMap(trackCount);
        int bufferSize = -1;
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = extractor.getTrackFormat(i);
            String mime = format.getString("mime");
            boolean selectCurrentTrack = false;
            if (mime.startsWith("audio/") && useAudio) {
                selectCurrentTrack = true;
            } else if (mime.startsWith("video/") && useVideo) {
                selectCurrentTrack = true;
            }
            if (selectCurrentTrack) {
                extractor.selectTrack(i);
                int dstIndex = mediaMuxer.addTrack(format);
                indexMap.put(Integer.valueOf(i), Integer.valueOf(dstIndex));
                if (format.containsKey("max-input-size")) {
                    int newSize = format.getInteger("max-input-size");
                    if (newSize > bufferSize) {
                        bufferSize = newSize;
                    }
                }
            }
        }
        if (bufferSize < 0) {
            bufferSize = 1048576;
        }
        MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
        retrieverSrc.setDataSource(srcPath);
        String degreesString = retrieverSrc.extractMetadata(24);
        if (degreesString != null) {
            int degrees = Integer.parseInt(degreesString);
            if (degrees >= 0) {
                mediaMuxer.setOrientationHint(degrees);
            }
        }
        if (startMs > 0) {
            extractor.seekTo(((long) startMs) * 1000, 2);
        }
        ByteBuffer dstBuf = ByteBuffer.allocate(bufferSize);
        BufferInfo bufferInfo = new BufferInfo();
        mediaMuxer.start();
        while (true) {
            bufferInfo.offset = 0;
            bufferInfo.size = extractor.readSampleData(dstBuf, 0);
            if (bufferInfo.size < 0) {
                break;
            }
            try {
                bufferInfo.presentationTimeUs = extractor.getSampleTime();
                if (endMs > 0 && bufferInfo.presentationTimeUs > ((long) endMs) * 1000) {
                    break;
                }
                bufferInfo.flags = extractor.getSampleFlags();
                mediaMuxer.writeSampleData(((Integer) indexMap.get(Integer.valueOf(extractor.getSampleTrackIndex()))).intValue(), dstBuf, bufferInfo);
                extractor.advance();
            } catch (IllegalStateException e) {
                GalleryLog.w("VideoUtils", "The source video file is malformed");
            } finally {
                mediaMuxer.release();
            }
            mediaMuxer.stop();
        }
        GalleryLog.d("VideoUtils", "The current sample is over the trim end time.");
        mediaMuxer.stop();
    }

    private static void trimUsingMp4Parser(File src, File dst, int startMs, int endMs) throws FileNotFoundException, IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(src, "r");
        Movie movie = MovieCreator.build(randomAccessFile.getChannel());
        List<Track> tracks = movie.getTracks();
        movie.setTracks(new LinkedList());
        double startTime = ((double) startMs) / 1000.0d;
        double endTime = ((double) endMs) / 1000.0d;
        boolean timeCorrected = false;
        for (Track track : tracks) {
            if (track.getSyncSamples() != null && track.getSyncSamples().length > 0) {
                if (timeCorrected) {
                    throw new RuntimeException("The startTime has already been corrected by another track with SyncSample. Not Supported.");
                }
                startTime = correctTimeToSyncSample(track, startTime, false);
                endTime = correctTimeToSyncSample(track, endTime, true);
                timeCorrected = true;
            }
        }
        for (Track track2 : tracks) {
            long currentSample = 0;
            double currentTime = 0.0d;
            long startSample = -1;
            long endSample = -1;
            for (int i = 0; i < track2.getDecodingTimeEntries().size(); i++) {
                Entry entry = (Entry) track2.getDecodingTimeEntries().get(i);
                for (int j = 0; ((long) j) < entry.getCount(); j++) {
                    if (currentTime <= startTime) {
                        startSample = currentSample;
                    }
                    if (currentTime > endTime) {
                        break;
                    }
                    endSample = currentSample;
                    currentTime += ((double) entry.getDelta()) / ((double) track2.getTrackMetaData().getTimescale());
                    currentSample++;
                }
            }
            movie.addTrack(new CroppedTrack(track2, startSample, endSample));
        }
        writeMovieIntoFile(dst, movie);
        randomAccessFile.close();
    }

    private static double correctTimeToSyncSample(Track track, double cutHere, boolean next) {
        double[] timeOfSyncSamples = new double[track.getSyncSamples().length];
        long currentSample = 0;
        double currentTime = 0.0d;
        for (int i = 0; i < track.getDecodingTimeEntries().size(); i++) {
            Entry entry = (Entry) track.getDecodingTimeEntries().get(i);
            for (int j = 0; ((long) j) < entry.getCount(); j++) {
                if (Arrays.binarySearch(track.getSyncSamples(), 1 + currentSample) >= 0) {
                    timeOfSyncSamples[Arrays.binarySearch(track.getSyncSamples(), 1 + currentSample)] = currentTime;
                }
                currentTime += ((double) entry.getDelta()) / ((double) track.getTrackMetaData().getTimescale());
                currentSample++;
            }
        }
        double previous = 0.0d;
        int i2 = 0;
        int length = timeOfSyncSamples.length;
        while (i2 < length) {
            double timeOfSyncSample = timeOfSyncSamples[i2];
            if (timeOfSyncSample <= cutHere) {
                previous = timeOfSyncSample;
                i2++;
            } else if (next) {
                return timeOfSyncSample;
            } else {
                return previous;
            }
        }
        return timeOfSyncSamples[timeOfSyncSamples.length - 1];
    }
}
