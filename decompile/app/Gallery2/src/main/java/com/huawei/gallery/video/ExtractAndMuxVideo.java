package com.huawei.gallery.video;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.support.v4.app.FragmentTransaction;
import android.view.Surface;
import com.android.gallery3d.common.Utils;
import com.android.gallery3d.util.GalleryLog;
import com.huawei.gallery.util.MyPrinter;
import java.io.File;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;
import tmsdk.common.module.intelli_sms.SmsCheckResult;

@TargetApi(18)
public class ExtractAndMuxVideo {
    private static final MyPrinter LOG = new MyPrinter("ExtractAndMuxVideo");
    private static final File OUTPUT_FILENAME_DIR = VideoUtils.SAVE_TO;
    private static Thread mWorker = null;

    public interface VideoMuxListener {
        boolean isCanceled();

        void onCompressDone(String str);

        void onCompressStart(String str);

        void onProgress(int i);
    }

    private static class Worker implements Runnable {
        private long mDuration = 1;
        private int mHeight = -1;
        private String mInputFile;
        private VideoMuxListener mListener;
        private String mOutputFile;
        private int mWidth = -1;

        private Worker(String inputFile, VideoMuxListener listener) {
            this.mInputFile = inputFile;
            this.mListener = listener;
            setOutputFile();
        }

        public void run() {
            try {
                extractDecodeEditEncodeMux();
                GalleryLog.i("ExtractAndMuxVideo", "extract and mux done, cancelled ? " + this.mListener.isCanceled());
                ExtractAndMuxVideo.mWorker = null;
            } catch (Throwable th) {
                GalleryLog.i("ExtractAndMuxVideo", "extractDecodeEditEncodeMux() failed." + th.getMessage());
            }
        }

        public static Thread doJob(String inputFile, VideoMuxListener listener) {
            try {
                if (ExtractAndMuxVideo.mWorker != null) {
                    ExtractAndMuxVideo.mWorker.interrupt();
                    ExtractAndMuxVideo.mWorker.join(500);
                }
            } catch (InterruptedException e) {
                GalleryLog.i("ExtractAndMuxVideo", "Thread.join() failed, reason: InterruptedException.");
            }
            Thread th = new Thread(new Worker(inputFile, listener), "video codec thread");
            th.start();
            return th;
        }

        private void setSize(int width, int height) {
            if (Math.min(width, height) > SmsCheckResult.ESCT_320) {
                float ratio = ((float) width) / ((float) height);
                if (width > height) {
                    height = SmsCheckResult.ESCT_320;
                    width = Math.round(320.0f * ratio);
                } else {
                    width = SmsCheckResult.ESCT_320;
                    height = Math.round(320.0f / ratio);
                }
                ExtractAndMuxVideo.LOG.d("scaled size is ( " + width + " x " + height + ")");
            }
            if (!(width % 16 == 0 && height % 16 == 0)) {
                GalleryLog.w("ExtractAndMuxVideo", "WARNING: width or height not multiple of 16");
            }
            this.mWidth = width & -16;
            this.mHeight = height & -16;
            ExtractAndMuxVideo.LOG.d("target size is ( " + this.mWidth + " x " + this.mHeight + ")");
        }

        private void setOutputFile() {
            File input = new File(this.mInputFile);
            File target = new File(ExtractAndMuxVideo.OUTPUT_FILENAME_DIR, "compressed_" + input.getName());
            int index = 0;
            while (target.exists()) {
                index++;
                target = new File(ExtractAndMuxVideo.OUTPUT_FILENAME_DIR, "compressed_" + index + input.getName());
            }
            GalleryLog.d("ExtractAndMuxVideo", "compressed video output to " + target.getAbsolutePath());
            this.mOutputFile = target.getAbsolutePath();
        }

        private void extractDecodeEditEncodeMux() throws Exception {
            OutputSurface outputSurface;
            Exception e;
            Throwable th;
            Exception exception = null;
            MediaCodecInfo videoCodecInfo = selectCodec("video/avc");
            if (videoCodecInfo == null) {
                GalleryLog.e("ExtractAndMuxVideo", "Unable to find an appropriate codec for video/avc");
                return;
            }
            MediaExtractor mediaExtractor = null;
            MediaExtractor mediaExtractor2 = null;
            MediaCodec mediaCodec = null;
            MediaCodec mediaCodec2 = null;
            MediaMuxer mediaMuxer = null;
            InputSurface inputSurface;
            try {
                MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
                retrieverSrc.setDataSource(this.mInputFile);
                String degreesString = retrieverSrc.extractMetadata(24);
                mediaExtractor = createExtractor();
                int videoInputTrack = getAndSelectVideoTrackIndex(mediaExtractor);
                ExtractAndMuxVideo.LOG.d("video track in video " + videoInputTrack);
                MediaFormat inputVideoFormat = mediaExtractor.getTrackFormat(videoInputTrack);
                inputVideoFormat.setInteger("rotation-degrees", 0);
                this.mDuration = inputVideoFormat.getLong("durationUs");
                setSize(inputVideoFormat.getInteger("width"), inputVideoFormat.getInteger("height"));
                ExtractAndMuxVideo.LOG.d("target size is " + this.mWidth + "x" + this.mHeight);
                MediaFormat outputVideoFormat = MediaFormat.createVideoFormat("video/avc", this.mWidth, this.mHeight);
                outputVideoFormat.setInteger("color-format", 2130708361);
                outputVideoFormat.setInteger("bitrate", 786432);
                outputVideoFormat.setInteger("frame-rate", 30);
                outputVideoFormat.setInteger("i-frame-interval", 10);
                AtomicReference<Surface> inputSurfaceReference = new AtomicReference();
                mediaCodec2 = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
                inputSurface = new InputSurface((Surface) inputSurfaceReference.get());
                try {
                    inputSurface.makeCurrent();
                    outputSurface = new OutputSurface();
                    try {
                        mediaCodec = createVideoDecoder(inputVideoFormat, outputSurface.getSurface());
                        mediaExtractor2 = createExtractor();
                        int audioInputTrack = getAndSelectAudioTrackIndex(mediaExtractor2);
                        ExtractAndMuxVideo.LOG.d("audio track in video " + audioInputTrack);
                        MediaFormat inputAudioFormat = null;
                        if (audioInputTrack >= 0) {
                            inputAudioFormat = mediaExtractor2.getTrackFormat(audioInputTrack);
                        }
                        mediaMuxer = createMuxer();
                        ExtractAndMuxVideo.LOG.d("degreesString is " + degreesString);
                        if (degreesString != null) {
                            int degrees = Integer.parseInt(degreesString);
                            if (degrees >= 0) {
                                mediaMuxer.setOrientationHint(degrees);
                            }
                        }
                        long start = System.currentTimeMillis();
                        doExtractDecodeEditEncodeMux(mediaExtractor, mediaExtractor2, mediaCodec, mediaCodec2, inputAudioFormat, mediaMuxer, inputSurface, outputSurface);
                        ExtractAndMuxVideo.LOG.w("doExtractDecodeEditEncodeMux cost time " + (System.currentTimeMillis() - start));
                        if (mediaExtractor != null) {
                            try {
                                mediaExtractor.release();
                            } catch (Exception e2) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoExtractor." + e2.getMessage());
                                exception = e2;
                            }
                        }
                        if (mediaExtractor2 != null) {
                            try {
                                mediaExtractor2.release();
                            } catch (Exception e22) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing audioExtractor." + e22.getMessage());
                                if (exception == null) {
                                    exception = e22;
                                }
                            }
                        }
                        if (mediaCodec != null) {
                            try {
                                mediaCodec.stop();
                                mediaCodec.release();
                            } catch (Exception e222) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoDecoder." + e222.getMessage());
                                if (exception == null) {
                                    exception = e222;
                                }
                            }
                        }
                        if (outputSurface != null) {
                            try {
                                outputSurface.release();
                            } catch (Exception e2222) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing outputSurface." + e2222.getMessage());
                                if (exception == null) {
                                    exception = e2222;
                                }
                            }
                        }
                        if (mediaCodec2 != null) {
                            try {
                                mediaCodec2.stop();
                                mediaCodec2.release();
                            } catch (Exception e22222) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoEncoder." + e22222.getMessage());
                                if (exception == null) {
                                    exception = e22222;
                                }
                            }
                        }
                        if (mediaMuxer != null) {
                            try {
                                mediaMuxer.stop();
                                mediaMuxer.release();
                            } catch (Exception e222222) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing muxer." + e222222.getMessage());
                                if (exception == null) {
                                    exception = e222222;
                                }
                            }
                        }
                        if (inputSurface != null) {
                            try {
                                inputSurface.release();
                            } catch (Exception e2222222) {
                                GalleryLog.e("ExtractAndMuxVideo", "error while releasing inputSurface." + e2222222.getMessage());
                                if (exception == null) {
                                    exception = e2222222;
                                }
                            }
                        }
                        if (this.mListener != null) {
                            this.mListener.onCompressDone(this.mOutputFile);
                        }
                    } catch (RuntimeException e3) {
                        e = e3;
                        try {
                            if (this.mListener != null) {
                                this.mListener.onCompressDone(null);
                            }
                            ExtractAndMuxVideo.LOG.w("doExtractDecodeEditEncodeMux failed !! ", e);
                            if (mediaExtractor != null) {
                                try {
                                    mediaExtractor.release();
                                } catch (Exception e22222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoExtractor." + e22222222.getMessage());
                                    exception = e22222222;
                                }
                            }
                            if (mediaExtractor2 != null) {
                                try {
                                    mediaExtractor2.release();
                                } catch (Exception e222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing audioExtractor." + e222222222.getMessage());
                                    if (exception == null) {
                                        exception = e222222222;
                                    }
                                }
                            }
                            if (mediaCodec != null) {
                                try {
                                    mediaCodec.stop();
                                    mediaCodec.release();
                                } catch (Exception e2222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoDecoder." + e2222222222.getMessage());
                                    if (exception == null) {
                                        exception = e2222222222;
                                    }
                                }
                            }
                            if (outputSurface != null) {
                                try {
                                    outputSurface.release();
                                } catch (Exception e22222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing outputSurface." + e22222222222.getMessage());
                                    if (exception == null) {
                                        exception = e22222222222;
                                    }
                                }
                            }
                            if (mediaCodec2 != null) {
                                try {
                                    mediaCodec2.stop();
                                    mediaCodec2.release();
                                } catch (Exception e222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoEncoder." + e222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e222222222222;
                                    }
                                }
                            }
                            if (mediaMuxer != null) {
                                try {
                                    mediaMuxer.stop();
                                    mediaMuxer.release();
                                } catch (Exception e2222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing muxer." + e2222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e2222222222222;
                                    }
                                }
                            }
                            if (inputSurface != null) {
                                try {
                                    inputSurface.release();
                                } catch (Exception e22222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing inputSurface." + e22222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e22222222222222;
                                    }
                                }
                            }
                            if (this.mListener != null) {
                                this.mListener.onCompressDone(this.mOutputFile);
                            }
                            if (exception == null) {
                                throw exception;
                            }
                        } catch (Throwable th2) {
                            th = th2;
                            if (mediaExtractor != null) {
                                try {
                                    mediaExtractor.release();
                                } catch (Exception e222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoExtractor." + e222222222222222.getMessage());
                                    exception = e222222222222222;
                                }
                            }
                            if (mediaExtractor2 != null) {
                                try {
                                    mediaExtractor2.release();
                                } catch (Exception e2222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing audioExtractor." + e2222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e2222222222222222;
                                    }
                                }
                            }
                            if (mediaCodec != null) {
                                try {
                                    mediaCodec.stop();
                                    mediaCodec.release();
                                } catch (Exception e22222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoDecoder." + e22222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e22222222222222222;
                                    }
                                }
                            }
                            if (outputSurface != null) {
                                try {
                                    outputSurface.release();
                                } catch (Exception e222222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing outputSurface." + e222222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e222222222222222222;
                                    }
                                }
                            }
                            if (mediaCodec2 != null) {
                                try {
                                    mediaCodec2.stop();
                                    mediaCodec2.release();
                                } catch (Exception e2222222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing videoEncoder." + e2222222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e2222222222222222222;
                                    }
                                }
                            }
                            if (mediaMuxer != null) {
                                try {
                                    mediaMuxer.stop();
                                    mediaMuxer.release();
                                } catch (Exception e22222222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing muxer." + e22222222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e22222222222222222222;
                                    }
                                }
                            }
                            if (inputSurface != null) {
                                try {
                                    inputSurface.release();
                                } catch (Exception e222222222222222222222) {
                                    GalleryLog.e("ExtractAndMuxVideo", "error while releasing inputSurface." + e222222222222222222222.getMessage());
                                    if (exception == null) {
                                        exception = e222222222222222222222;
                                    }
                                }
                            }
                            if (this.mListener != null) {
                                this.mListener.onCompressDone(this.mOutputFile);
                            }
                            throw th;
                        }
                    }
                } catch (RuntimeException e4) {
                    e = e4;
                    outputSurface = null;
                    if (this.mListener != null) {
                        this.mListener.onCompressDone(null);
                    }
                    ExtractAndMuxVideo.LOG.w("doExtractDecodeEditEncodeMux failed !! ", e);
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                    if (mediaExtractor2 != null) {
                        mediaExtractor2.release();
                    }
                    if (mediaCodec != null) {
                        mediaCodec.stop();
                        mediaCodec.release();
                    }
                    if (outputSurface != null) {
                        outputSurface.release();
                    }
                    if (mediaCodec2 != null) {
                        mediaCodec2.stop();
                        mediaCodec2.release();
                    }
                    if (mediaMuxer != null) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (this.mListener != null) {
                        this.mListener.onCompressDone(this.mOutputFile);
                    }
                    if (exception == null) {
                        throw exception;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outputSurface = null;
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                    if (mediaExtractor2 != null) {
                        mediaExtractor2.release();
                    }
                    if (mediaCodec != null) {
                        mediaCodec.stop();
                        mediaCodec.release();
                    }
                    if (outputSurface != null) {
                        outputSurface.release();
                    }
                    if (mediaCodec2 != null) {
                        mediaCodec2.stop();
                        mediaCodec2.release();
                    }
                    if (mediaMuxer != null) {
                        mediaMuxer.stop();
                        mediaMuxer.release();
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (this.mListener != null) {
                        this.mListener.onCompressDone(this.mOutputFile);
                    }
                    throw th;
                }
            } catch (RuntimeException e5) {
                e = e5;
                inputSurface = null;
                outputSurface = null;
                if (this.mListener != null) {
                    this.mListener.onCompressDone(null);
                }
                ExtractAndMuxVideo.LOG.w("doExtractDecodeEditEncodeMux failed !! ", e);
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
                if (mediaExtractor2 != null) {
                    mediaExtractor2.release();
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                }
                if (outputSurface != null) {
                    outputSurface.release();
                }
                if (mediaCodec2 != null) {
                    mediaCodec2.stop();
                    mediaCodec2.release();
                }
                if (mediaMuxer != null) {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (this.mListener != null) {
                    this.mListener.onCompressDone(this.mOutputFile);
                }
                if (exception == null) {
                    throw exception;
                }
            } catch (Throwable th4) {
                th = th4;
                inputSurface = null;
                outputSurface = null;
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
                if (mediaExtractor2 != null) {
                    mediaExtractor2.release();
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                }
                if (outputSurface != null) {
                    outputSurface.release();
                }
                if (mediaCodec2 != null) {
                    mediaCodec2.stop();
                    mediaCodec2.release();
                }
                if (mediaMuxer != null) {
                    mediaMuxer.stop();
                    mediaMuxer.release();
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (this.mListener != null) {
                    this.mListener.onCompressDone(this.mOutputFile);
                }
                throw th;
            }
            if (exception == null) {
                throw exception;
            }
        }

        private MediaExtractor createExtractor() throws IOException {
            MediaExtractor extractor = new MediaExtractor();
            extractor.setDataSource(this.mInputFile);
            return extractor;
        }

        private MediaCodec createVideoDecoder(MediaFormat inputFormat, Surface surface) throws IOException {
            MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
            decoder.configure(inputFormat, surface, null, 0);
            decoder.start();
            return decoder;
        }

        private MediaCodec createVideoEncoder(MediaCodecInfo codecInfo, MediaFormat format, AtomicReference<Surface> surfaceReference) throws IOException {
            MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, 1);
            surfaceReference.set(encoder.createInputSurface());
            encoder.start();
            return encoder;
        }

        private MediaMuxer createMuxer() throws IOException {
            return new MediaMuxer(this.mOutputFile, 0);
        }

        private int getAndSelectVideoTrackIndex(MediaExtractor extractor) {
            for (int index = 0; index < extractor.getTrackCount(); index++) {
                if (isVideoFormat(extractor.getTrackFormat(index))) {
                    extractor.selectTrack(index);
                    return index;
                }
            }
            return -1;
        }

        private int getAndSelectAudioTrackIndex(MediaExtractor extractor) {
            for (int index = 0; index < extractor.getTrackCount(); index++) {
                if (isAudioFormat(extractor.getTrackFormat(index))) {
                    extractor.selectTrack(index);
                    return index;
                }
            }
            return -1;
        }

        private void doExtractDecodeEditEncodeMux(MediaExtractor videoExtractor, MediaExtractor audioExtractor, MediaCodec videoDecoder, MediaCodec videoEncoder, MediaFormat inputAudioFormat, MediaMuxer muxer, InputSurface inputSurface, OutputSurface outputSurface) {
            if (this.mListener != null) {
                this.mListener.onCompressStart(this.mOutputFile);
            }
            ByteBuffer[] videoDecoderInputBuffers = videoDecoder.getInputBuffers();
            ByteBuffer[] videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
            BufferInfo videoDecoderOutputBufferInfo = new BufferInfo();
            BufferInfo videoEncoderOutputBufferInfo = new BufferInfo();
            int bufferSize = 0;
            if (inputAudioFormat != null) {
                if (inputAudioFormat.containsKey("max-input-size")) {
                    bufferSize = inputAudioFormat.getInteger("max-input-size");
                }
            }
            if (bufferSize <= 0) {
                bufferSize = FragmentTransaction.TRANSIT_ENTER_MASK;
            }
            ByteBuffer audioBuf = ByteBuffer.allocate(bufferSize);
            BufferInfo bufferInfo = new BufferInfo();
            MediaFormat encoderOutputVideoFormat = null;
            int outputVideoTrack = -1;
            int outputAudioTrack = -1;
            boolean videoExtractorDone = false;
            boolean videoDecoderDone = false;
            boolean videoEncoderDone = false;
            boolean audioExtractorDone = false;
            boolean muxing = false;
            while (true) {
                if (!videoEncoderDone || !audioExtractorDone) {
                    if (!videoExtractorDone && (r26 == null || muxing)) {
                        int decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(10000);
                        if (decoderInputBufferIndex == -1) {
                            verbose("no video decoder input buffer");
                        } else {
                            verbose("video decoder: returned input buffer: " + decoderInputBufferIndex);
                            int size = videoExtractor.readSampleData(videoDecoderInputBuffers[decoderInputBufferIndex], 0);
                            long presentationTime = videoExtractor.getSampleTime();
                            if (size >= 0) {
                                videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, videoExtractor.getSampleFlags());
                            }
                            videoExtractorDone = !videoExtractor.advance();
                            if (videoExtractorDone) {
                                verbose("video extractor: EOS");
                                videoDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, 4);
                            }
                        }
                    }
                    if (!videoDecoderDone && (r26 == null || muxing)) {
                        int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, 10000);
                        if (decoderOutputBufferIndex == -1) {
                            verbose("no video decoder output buffer");
                        } else if (decoderOutputBufferIndex == -3) {
                            verbose("video decoder: output buffers changed");
                        } else if (decoderOutputBufferIndex == -2) {
                            verbose("video decoder: output format changed: " + videoDecoder.getOutputFormat());
                        } else {
                            verbose("video decoder: returned output buffer: " + decoderOutputBufferIndex);
                            verbose("video decoder: returned buffer of size " + videoDecoderOutputBufferInfo.size);
                            if ((videoDecoderOutputBufferInfo.flags & 2) != 0) {
                                verbose("video decoder: codec config buffer");
                                videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
                            } else {
                                verbose("video decoder: returned buffer for time " + videoDecoderOutputBufferInfo.presentationTimeUs);
                                boolean render = videoDecoderOutputBufferInfo.size != 0;
                                videoDecoder.releaseOutputBuffer(decoderOutputBufferIndex, render);
                                if (render) {
                                    verbose("output surface: await new image");
                                    outputSurface.awaitNewImage();
                                    verbose("output surface: draw image");
                                    outputSurface.drawImage();
                                    inputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
                                    verbose("input surface: swap buffers");
                                    inputSurface.swapBuffers();
                                    verbose("video encoder: notified of new frame");
                                }
                                if ((videoDecoderOutputBufferInfo.flags & 4) != 0) {
                                    verbose("video decoder: EOS");
                                    videoDecoderDone = true;
                                    videoEncoder.signalEndOfInputStream();
                                }
                            }
                        }
                    }
                    if (!videoEncoderDone && (r26 == null || muxing)) {
                        int encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, 10000);
                        if (encoderOutputBufferIndex == -1) {
                            verbose("no video encoder output buffer");
                        } else if (encoderOutputBufferIndex == -3) {
                            verbose("video encoder: output buffers changed");
                            videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
                        } else if (encoderOutputBufferIndex == -2) {
                            verbose("video encoder: output format changed");
                            if (outputVideoTrack >= 0) {
                                verbose("video encoder changed its output format again?");
                            }
                            encoderOutputVideoFormat = videoEncoder.getOutputFormat();
                        } else {
                            verbose("should have added track before processing output " + muxing);
                            verbose("video encoder: returned output buffer: " + encoderOutputBufferIndex);
                            verbose("video encoder: returned buffer of size " + videoEncoderOutputBufferInfo.size);
                            ByteBuffer encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
                            if ((videoEncoderOutputBufferInfo.flags & 2) != 0) {
                                verbose("video encoder: codec config buffer");
                                videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            } else {
                                verbose("video encoder: returned buffer for time " + videoEncoderOutputBufferInfo.presentationTimeUs);
                                verbose("video encoder: returned buffer flags " + videoEncoderOutputBufferInfo.flags);
                                if ((videoEncoderOutputBufferInfo.flags & 4) != 0) {
                                    verbose("video encoder: EOS");
                                    videoEncoderDone = true;
                                } else if (videoEncoderOutputBufferInfo.size != 0) {
                                    if (this.mListener != null) {
                                        this.mListener.onProgress(Utils.clamp((int) ((100 * videoEncoderOutputBufferInfo.presentationTimeUs) / this.mDuration), 0, 100));
                                    }
                                    muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
                                }
                                videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            }
                        }
                    }
                    if (!(audioExtractorDone || outputAudioTrack == -1)) {
                        bufferInfo.offset = 0;
                        bufferInfo.size = audioExtractor.readSampleData(audioBuf, 0);
                        if (bufferInfo.size < 0) {
                            GalleryLog.d("ExtractAndMuxVideo", "Saw audio input EOS.");
                            bufferInfo.size = 0;
                            audioExtractorDone = true;
                        } else {
                            bufferInfo.presentationTimeUs = audioExtractor.getSampleTime();
                            bufferInfo.flags = audioExtractor.getSampleFlags();
                            verbose("audio extractor: returned buffer for time " + bufferInfo.presentationTimeUs);
                            verbose("audio extractor: returned buffer flags " + bufferInfo.flags);
                            muxer.writeSampleData(outputAudioTrack, audioBuf, bufferInfo);
                            audioExtractorDone = !audioExtractor.advance();
                        }
                    }
                    if (this.mListener != null && this.mListener.isCanceled()) {
                        return;
                    }
                    if (!(muxing || encoderOutputVideoFormat == null)) {
                        GalleryLog.d("ExtractAndMuxVideo", "muxer: adding video track.");
                        outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
                        GalleryLog.d("ExtractAndMuxVideo", "muxer: adding audio track.");
                        if (inputAudioFormat != null) {
                            outputAudioTrack = muxer.addTrack(inputAudioFormat);
                        } else {
                            ExtractAndMuxVideo.LOG.d("there is no audio");
                            audioExtractorDone = true;
                        }
                        GalleryLog.d("ExtractAndMuxVideo", "muxer: starting");
                        muxer.start();
                        muxing = true;
                    }
                } else {
                    return;
                }
            }
        }

        private static boolean isVideoFormat(MediaFormat format) {
            return getMimeTypeFor(format).startsWith("video/");
        }

        private static boolean isAudioFormat(MediaFormat format) {
            return getMimeTypeFor(format).startsWith("audio/");
        }

        private static String getMimeTypeFor(MediaFormat format) {
            return format.getString("mime");
        }

        private static MediaCodecInfo selectCodec(String mimeType) {
            int numCodecs = MediaCodecList.getCodecCount();
            for (int i = 0; i < numCodecs; i++) {
                MediaCodecInfo codecInfo = MediaCodecList.getCodecInfoAt(i);
                if (codecInfo.isEncoder()) {
                    String[] types = codecInfo.getSupportedTypes();
                    for (String equalsIgnoreCase : types) {
                        if (equalsIgnoreCase.equalsIgnoreCase(mimeType)) {
                            return codecInfo;
                        }
                    }
                    continue;
                }
            }
            return null;
        }

        private void verbose(String msg) {
        }
    }

    public static void extractDecodeEditEncodeMuxAudioVideo(String inputFile, VideoMuxListener listener) {
        mWorker = Worker.doJob(inputFile, listener);
    }
}
