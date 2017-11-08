package com.huawei.gallery.video;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.os.SystemProperties;
import com.android.gallery3d.common.BitmapUtils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.MyPrinter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;

public class VideoConvertor {
    private static final MyPrinter LOG = new MyPrinter("VideoConvertor");
    private static final boolean VERBOSE = SystemProperties.getBoolean("debug-switch", false);
    private static File mSaveDir = VideoUtils.SAVE_TO;
    private String mAudioFile;
    private int mBitRate = -1;
    private Bitmap mFrameData;
    private int mHeight = -1;
    private String mImageFile;
    private ImageToVideoListener mListener;
    private String mOutputFile;
    private int mRotation;
    private int mWidth = -1;

    public interface ImageToVideoListener {
        void onProgress(String str, int i);
    }

    private static class VideoEditWrapper implements Runnable {
        private VideoConvertor mTest;
        private Throwable mThrowable;

        private VideoEditWrapper(VideoConvertor test) {
            this.mTest = test;
        }

        public void run() {
            try {
                this.mTest.generateVideoFile();
            } catch (Throwable th) {
                GalleryLog.w("VideoConvertor", "VideoConvertor.generateVideoFile() failed." + th.getMessage());
                this.mThrowable = th;
            }
        }

        public static void runWork(VideoConvertor obj) throws Throwable {
            VideoEditWrapper wrapper = new VideoEditWrapper(obj);
            Thread th = new Thread(wrapper, "codec test");
            th.start();
            th.join();
            if (wrapper.mThrowable != null) {
                throw wrapper.mThrowable;
            }
        }
    }

    public void setAudioFile(String fileName) {
        this.mAudioFile = fileName;
    }

    public void setImageFile(String fileName, int rotation) {
        this.mImageFile = fileName;
        Options options = new Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(this.mImageFile, options);
        options.inSampleSize = BitmapUtils.computeSampleSizeLarger(480.0f / ((float) Math.min(options.outWidth, options.outHeight)));
        options.inJustDecodeBounds = false;
        this.mFrameData = BitmapFactory.decodeFile(this.mImageFile, options);
        setParameters(this.mFrameData.getWidth(), this.mFrameData.getHeight(), 524288);
        this.mRotation = rotation;
    }

    public void exportVideo(String fileName, ImageToVideoListener listener) throws Throwable {
        this.mOutputFile = fileName;
        this.mListener = listener;
        VideoEditWrapper.runWork(this);
    }

    private void setParameters(int width, int height, int bitRate) {
        if (!(width % 16 == 0 && height % 16 == 0)) {
            GalleryLog.w("VideoConvertor", "WARNING: width or height not multiple of 16");
        }
        this.mWidth = chooseWidth(width, height) & -16;
        this.mHeight = 480;
        LOG.d("target size is ( " + this.mWidth + " x " + this.mHeight + ")");
        this.mBitRate = bitRate;
    }

    private int chooseWidth(int width, int height) {
        float ratio = ((float) width) / ((float) height);
        if (ratio <= 1.3833333f) {
            return 640;
        }
        if (ratio <= 1.55f) {
            return 720;
        }
        return 853;
    }

    private boolean generateVideoFile() throws IOException {
        MediaExtractor audioExtractor;
        InputSurface inputSurface;
        Exception e;
        MediaMuxer muxer;
        Throwable th;
        if (VERBOSE) {
            GalleryLog.d("VideoConvertor", "generateVideoFile " + this.mWidth + "x" + this.mHeight);
        }
        MediaCodec encoder = null;
        BitmapTextureRender bitmapTextureRender;
        try {
            MediaCodecInfo codecInfo = selectCodec("video/avc");
            if (codecInfo == null) {
                GalleryLog.e("VideoConvertor", "Unable to find an appropriate codec for video/avc");
                return false;
            }
            MediaFormat audioInputFormat;
            if (VERBOSE) {
                GalleryLog.d("VideoConvertor", "found codec: " + codecInfo.getName());
            }
            audioExtractor = new MediaExtractor();
            try {
                audioExtractor.setDataSource(this.mAudioFile);
                audioInputFormat = findAudioFormat(audioExtractor);
                MediaFormat format = MediaFormat.createVideoFormat("video/avc", this.mWidth, this.mHeight);
                format.setInteger("color-format", 2130708361);
                format.setInteger("bitrate", this.mBitRate);
                format.setInteger("frame-rate", 15);
                format.setInteger("i-frame-interval", 10);
                if (VERBOSE) {
                    GalleryLog.d("VideoConvertor", "format: " + format);
                }
                encoder = MediaCodec.createByCodecName(codecInfo.getName());
                encoder.configure(format, null, null, 1);
                inputSurface = new InputSurface(encoder.createInputSurface());
            } catch (Exception e2) {
                e = e2;
                bitmapTextureRender = null;
                muxer = null;
                inputSurface = null;
                try {
                    GalleryLog.e("VideoConvertor", "exception in generateVideoFile(). " + e.getMessage());
                    if (audioExtractor != null) {
                        audioExtractor.release();
                    }
                    if (encoder != null) {
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "releasing encoder");
                        }
                        encoder.stop();
                        encoder.release();
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "released encoder");
                        }
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (muxer != null) {
                        muxer.stop();
                        muxer.release();
                    }
                    return true;
                } catch (Throwable th2) {
                    th = th2;
                    if (audioExtractor != null) {
                        audioExtractor.release();
                    }
                    if (encoder != null) {
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "releasing encoder");
                        }
                        encoder.stop();
                        encoder.release();
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "released encoder");
                        }
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (muxer != null) {
                        muxer.stop();
                        muxer.release();
                    }
                    throw th;
                }
            } catch (Throwable th3) {
                th = th3;
                bitmapTextureRender = null;
                muxer = null;
                inputSurface = null;
                if (audioExtractor != null) {
                    audioExtractor.release();
                }
                if (encoder != null) {
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "releasing encoder");
                    }
                    encoder.stop();
                    encoder.release();
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "released encoder");
                    }
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
                throw th;
            }
            try {
                inputSurface.makeCurrent();
                encoder.start();
                muxer = new MediaMuxer(this.mOutputFile, 0);
                try {
                    if (this.mRotation != 0) {
                        muxer.setOrientationHint(this.mRotation);
                    }
                    bitmapTextureRender = new BitmapTextureRender(this.mFrameData, this.mWidth, this.mHeight);
                    try {
                        bitmapTextureRender.surfaceCreated();
                        bitmapTextureRender.uploadTexture();
                        generateVideoData(audioExtractor, audioInputFormat, encoder, inputSurface, muxer, bitmapTextureRender);
                        if (audioExtractor != null) {
                            audioExtractor.release();
                        }
                        if (encoder != null) {
                            if (VERBOSE) {
                                GalleryLog.d("VideoConvertor", "releasing encoder");
                            }
                            encoder.stop();
                            encoder.release();
                            if (VERBOSE) {
                                GalleryLog.d("VideoConvertor", "released encoder");
                            }
                        }
                        if (inputSurface != null) {
                            inputSurface.release();
                        }
                        if (muxer != null) {
                            muxer.stop();
                            muxer.release();
                        }
                    } catch (Exception e3) {
                        e = e3;
                        GalleryLog.e("VideoConvertor", "exception in generateVideoFile(). " + e.getMessage());
                        if (audioExtractor != null) {
                            audioExtractor.release();
                        }
                        if (encoder != null) {
                            if (VERBOSE) {
                                GalleryLog.d("VideoConvertor", "releasing encoder");
                            }
                            encoder.stop();
                            encoder.release();
                            if (VERBOSE) {
                                GalleryLog.d("VideoConvertor", "released encoder");
                            }
                        }
                        if (inputSurface != null) {
                            inputSurface.release();
                        }
                        if (muxer != null) {
                            muxer.stop();
                            muxer.release();
                        }
                        return true;
                    }
                } catch (Exception e4) {
                    e = e4;
                    bitmapTextureRender = null;
                    GalleryLog.e("VideoConvertor", "exception in generateVideoFile(). " + e.getMessage());
                    if (audioExtractor != null) {
                        audioExtractor.release();
                    }
                    if (encoder != null) {
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "releasing encoder");
                        }
                        encoder.stop();
                        encoder.release();
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "released encoder");
                        }
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (muxer != null) {
                        muxer.stop();
                        muxer.release();
                    }
                    return true;
                } catch (Throwable th4) {
                    th = th4;
                    bitmapTextureRender = null;
                    if (audioExtractor != null) {
                        audioExtractor.release();
                    }
                    if (encoder != null) {
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "releasing encoder");
                        }
                        encoder.stop();
                        encoder.release();
                        if (VERBOSE) {
                            GalleryLog.d("VideoConvertor", "released encoder");
                        }
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (muxer != null) {
                        muxer.stop();
                        muxer.release();
                    }
                    throw th;
                }
            } catch (Exception e5) {
                e = e5;
                bitmapTextureRender = null;
                muxer = null;
                GalleryLog.e("VideoConvertor", "exception in generateVideoFile(). " + e.getMessage());
                if (audioExtractor != null) {
                    audioExtractor.release();
                }
                if (encoder != null) {
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "releasing encoder");
                    }
                    encoder.stop();
                    encoder.release();
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "released encoder");
                    }
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
                return true;
            } catch (Throwable th5) {
                th = th5;
                bitmapTextureRender = null;
                muxer = null;
                if (audioExtractor != null) {
                    audioExtractor.release();
                }
                if (encoder != null) {
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "releasing encoder");
                    }
                    encoder.stop();
                    encoder.release();
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "released encoder");
                    }
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
                throw th;
            }
            return true;
        } catch (Exception e6) {
            e = e6;
            bitmapTextureRender = null;
            muxer = null;
            inputSurface = null;
            audioExtractor = null;
            GalleryLog.e("VideoConvertor", "exception in generateVideoFile(). " + e.getMessage());
            if (audioExtractor != null) {
                audioExtractor.release();
            }
            if (encoder != null) {
                if (VERBOSE) {
                    GalleryLog.d("VideoConvertor", "releasing encoder");
                }
                encoder.stop();
                encoder.release();
                if (VERBOSE) {
                    GalleryLog.d("VideoConvertor", "released encoder");
                }
            }
            if (inputSurface != null) {
                inputSurface.release();
            }
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
            return true;
        } catch (Throwable th6) {
            th = th6;
            bitmapTextureRender = null;
            muxer = null;
            inputSurface = null;
            audioExtractor = null;
            if (audioExtractor != null) {
                audioExtractor.release();
            }
            if (encoder != null) {
                if (VERBOSE) {
                    GalleryLog.d("VideoConvertor", "releasing encoder");
                }
                encoder.stop();
                encoder.release();
                if (VERBOSE) {
                    GalleryLog.d("VideoConvertor", "released encoder");
                }
            }
            if (inputSurface != null) {
                inputSurface.release();
            }
            if (muxer != null) {
                muxer.stop();
                muxer.release();
            }
            throw th;
        }
    }

    private MediaFormat findAudioFormat(MediaExtractor audioExtractor) {
        int trackCount = audioExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat format = audioExtractor.getTrackFormat(i);
            if (format.getString("mime").startsWith("audio/")) {
                audioExtractor.selectTrack(i);
                return format;
            }
        }
        return null;
    }

    private static MediaCodecInfo selectCodec(String mimeType) {
        int numCodecs = MediaCodecList.getCodecCount();
        for (int i = 0; i < numCodecs; i++) {
            MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
            if (codecInfo.isEncoder()) {
                String[] types = codecInfo.getSupportedTypes();
                for (String equalsIgnoreCase : types) {
                    if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                        GalleryLog.d("VideoConvertor", "find the codecInfo");
                        return codecInfo;
                    }
                }
                continue;
            } else {
                GalleryLog.d("VideoConvertor", "is not Encoder!");
            }
        }
        return null;
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private void generateVideoData(MediaExtractor audioExtractor, MediaFormat audioInputFormat, MediaCodec encoder, InputSurface inputSurface, MediaMuxer muxer, BitmapTextureRender textureRender) {
        ByteBuffer[] encoderOutputBuffers = encoder.getOutputBuffers();
        BufferInfo info = new BufferInfo();
        int audioTrackIndex = 0;
        int videoTrackIndex = 0;
        boolean muxing = false;
        if (audioInputFormat.containsKey("max-input-size")) {
            audioInputFormat.getInteger("max-input-size");
        }
        ByteBuffer audioBuf = ByteBuffer.allocate(1048576);
        BufferInfo bufferInfo = new BufferInfo();
        boolean outputDone = false;
        long duration = audioInputFormat.getLong("durationUs");
        int frameIndex = 0;
        int frameCount = (int) (((double) (15 * duration)) / 1000000.0d);
        GalleryLog.d("VideoConvertor", "frame output begin, progress: 0");
        setProgress(this.mOutputFile, 0);
        float videoProgreStep = 80.0f / ((float) frameCount);
        long presentationTimeStep = (1000 * duration) / ((long) frameCount);
        while (!outputDone) {
            if (VERBOSE) {
                GalleryLog.d("VideoConvertor", "gen loop");
            }
            if (frameIndex < frameCount) {
                textureRender.drawFrame();
                verbose("draw frame, presentationTime: " + (((long) frameIndex) * presentationTimeStep));
                inputSurface.setPresentationTime(((long) frameIndex) * presentationTimeStep);
                verbose("inputSurface swapBuffers");
                inputSurface.swapBuffers();
                setProgress(this.mOutputFile, Math.round(((float) frameIndex) * videoProgreStep));
                frameIndex++;
                if (frameIndex == frameCount) {
                    encoder.signalEndOfInputStream();
                    verbose("frame input done EOS");
                }
            }
            while (true) {
                int encoderStatus = encoder.dequeueOutputBuffer(info, 10000);
                if (encoderStatus == -1) {
                    break;
                } else if (encoderStatus == -3) {
                    encoderOutputBuffers = encoder.getOutputBuffers();
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "encoder output buffers changed");
                    }
                } else if (encoderStatus == -2) {
                    MediaFormat newFormat = encoder.getOutputFormat();
                    if (!muxing) {
                        videoTrackIndex = muxer.addTrack(newFormat);
                        audioTrackIndex = muxer.addTrack(audioInputFormat);
                        muxer.start();
                        muxing = true;
                    }
                    if (VERBOSE) {
                        GalleryLog.d("VideoConvertor", "encoder output format changed: " + newFormat);
                    }
                } else if (encoderStatus < 0) {
                    fail("unexpected result from encoder.dequeueOutputBuffer: " + encoderStatus);
                } else {
                    ByteBuffer encodedData = encoderOutputBuffers[encoderStatus];
                    if (encodedData == null) {
                        fail("encoderOutputBuffer " + encoderStatus + " was null");
                    }
                    if (info.size != 0) {
                        encodedData.position(info.offset);
                        encodedData.limit(info.offset + info.size);
                        verbose("frame size : " + info.size);
                        verbose("frame time : " + info.presentationTimeUs);
                        muxer.writeSampleData(videoTrackIndex, encodedData, info);
                    }
                    encoder.releaseOutputBuffer(encoderStatus, false);
                    if ((info.flags & 4) != 0) {
                        break;
                    }
                }
            }
            if (VERBOSE) {
                GalleryLog.d("VideoConvertor", "no output from encoder available");
            }
        }
        GalleryLog.d("VideoConvertor", "video frame output done, progress: 80");
        setProgress(this.mOutputFile, 80);
        while (muxing) {
            bufferInfo.offset = 0;
            bufferInfo.size = audioExtractor.readSampleData(audioBuf, 0);
            if (bufferInfo.size < 0) {
                GalleryLog.d("VideoConvertor", "Saw audio input EOS.");
                bufferInfo.size = 0;
                break;
            }
            bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
            bufferInfo.flags = audioExtractor.getSampleFlags();
            muxer.writeSampleData(audioTrackIndex, audioBuf, bufferInfo);
            audioExtractor.advance();
        }
        GalleryLog.d("VideoConvertor", "audio output done, progress: 100");
        setProgress(this.mOutputFile, 100);
    }

    private void setProgress(String filename, int progress) {
        verbose("convert video. filename: " + filename + ", progress: " + progress);
        if (this.mListener != null) {
            this.mListener.onProgress(filename, progress);
        }
    }

    private static void verbose(String msg) {
        if (VERBOSE) {
            GalleryLog.d("VideoConvertor", msg);
        }
    }

    private static void fail(String msg) {
        throw new RuntimeException(msg);
    }
}
