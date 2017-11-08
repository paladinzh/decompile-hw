package com.huawei.rcs.utils.videocompress;

import android.annotation.TargetApi;
import android.content.Context;
import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaMetadataRetriever;
import android.media.MediaMuxer;
import android.net.Uri;
import android.view.Surface;
import cn.com.xy.sms.sdk.ui.popu.util.ViewPartId;
import com.huawei.cspcommon.MLog;
import com.huawei.rcs.utils.RcsTransaction;
import com.huawei.rcs.utils.RcsUtility;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicReference;

@TargetApi(18)
public class RcsExtractAndMuxVideo {
    private static final File OUTPUT_FILENAME_DIR = new File(RcsUtility.getCacheDirPath(false));
    private static final String TAG = RcsExtractAndMuxVideo.class.getSimpleName();
    private static Thread mWorker = null;

    public interface VideoMuxListener {
        boolean isCanceled();

        void onCompressCompleted(String str);

        void onCompressProcessing(int i);

        void onCompressStarted(String str);
    }

    private static class Worker implements Runnable {
        private Context mContext;
        private long mDuration = 1;
        private long mFileSize = 0;
        private int mHeight = -1;
        private String mInputFile;
        private InputStream mIsForCopyFile;
        private VideoMuxListener mListener;
        private OutputStream mOsForCopyFile;
        private String mOutputFile;
        private int mWidth = -1;

        private Worker(String inputFile, VideoMuxListener listener, Context context) {
            this.mInputFile = inputFile;
            this.mListener = listener;
            this.mContext = context;
            setOutputFile();
        }

        public void run() {
            try {
                extractDecodeEditEncodeMux();
                RcsExtractAndMuxVideo.mWorker = null;
            } catch (Throwable th) {
                th.printStackTrace();
            }
        }

        public static Thread doJob(String inputFile, VideoMuxListener listener, Context context) {
            try {
                if (RcsExtractAndMuxVideo.mWorker != null) {
                    RcsExtractAndMuxVideo.mWorker.interrupt();
                    RcsExtractAndMuxVideo.mWorker.join();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            Thread th = new Thread(new Worker(inputFile, listener, context), "video codec thread");
            th.start();
            return th;
        }

        private void setSize(int width, int height) {
            if (Math.min(width, height) > 480) {
                float ratio = ((float) width) / ((float) height);
                if (width > height) {
                    height = 480;
                    width = Math.round(480.0f * ratio);
                } else {
                    width = 480;
                    height = Math.round(480.0f / ratio);
                }
                MLog.d(RcsExtractAndMuxVideo.TAG, "scaled size is ( " + width + " x " + height + ")");
            }
            if (!(width % 16 == 0 && height % 16 == 0)) {
                MLog.w(RcsExtractAndMuxVideo.TAG, "WARNING: width or height not multiple of 16");
            }
            this.mWidth = width & -16;
            this.mHeight = height & -16;
            MLog.d(RcsExtractAndMuxVideo.TAG, "target size is ( " + this.mWidth + " x " + this.mHeight + ")");
        }

        private void setOutputFile() {
            this.mOutputFile = new File(RcsUtility.getCacheDirPath(false), "compressed_" + new File(this.mInputFile).getName()).getAbsolutePath();
        }

        private void extractDecodeEditEncodeMux() throws Exception {
            Throwable exception;
            Throwable th;
            Exception exception2 = null;
            MediaCodecInfo videoCodecInfo = selectCodec("video/avc");
            if (videoCodecInfo == null) {
                MLog.w(RcsExtractAndMuxVideo.TAG, "Unable to find an appropriate codec for video/avc");
                return;
            }
            MediaCodecInfo audioCodecInfo = selectCodec("audio/mp4a-latm");
            if (audioCodecInfo == null) {
                MLog.e(RcsExtractAndMuxVideo.TAG, "Unable to find an appropriate codec for audio/mp4a-latm");
                return;
            }
            MediaExtractor mediaExtractor = null;
            MediaExtractor audioExtractor = null;
            MediaCodec videoDecoder = null;
            MediaCodec mediaCodec = null;
            MediaCodec mediaCodec2 = null;
            MediaCodec audioEncoder = null;
            MediaMuxer muxer = null;
            RcsInputSurface inputSurface;
            RcsOutputSurface outputSurface;
            try {
                MediaMetadataRetriever retrieverSrc = new MediaMetadataRetriever();
                retrieverSrc.setDataSource(this.mInputFile);
                String degreesString = retrieverSrc.extractMetadata(24);
                mediaExtractor = createExtractor();
                int videoInputTrack = getAndSelectVideoTrackIndex(mediaExtractor);
                MLog.d(RcsExtractAndMuxVideo.TAG, "video track in video " + videoInputTrack);
                MediaFormat inputVideoFormat = mediaExtractor.getTrackFormat(videoInputTrack);
                inputVideoFormat.setInteger("rotation-degrees", 0);
                this.mDuration = inputVideoFormat.getLong("durationUs");
                setSize(inputVideoFormat.getInteger("width"), inputVideoFormat.getInteger("height"));
                MLog.d(RcsExtractAndMuxVideo.TAG, "target size is " + this.mWidth + "x" + this.mHeight);
                MediaFormat outputVideoFormat = MediaFormat.createVideoFormat("video/avc", this.mWidth, this.mHeight);
                outputVideoFormat.setInteger("color-format", 2130708361);
                outputVideoFormat.setInteger("bitrate", 786432);
                outputVideoFormat.setInteger("frame-rate", 30);
                outputVideoFormat.setInteger("i-frame-interval", 10);
                AtomicReference<Surface> inputSurfaceReference = new AtomicReference();
                mediaCodec2 = createVideoEncoder(videoCodecInfo, outputVideoFormat, inputSurfaceReference);
                inputSurface = new RcsInputSurface((Surface) inputSurfaceReference.get());
                try {
                    inputSurface.makeCurrent();
                    outputSurface = new RcsOutputSurface();
                    try {
                        videoDecoder = createVideoDecoder(inputVideoFormat, outputSurface.getSurface());
                        audioExtractor = createExtractor();
                        int audioInputTrack = getAndSelectAudioTrackIndex(audioExtractor);
                        MLog.d(RcsExtractAndMuxVideo.TAG, "audio track in video " + audioInputTrack);
                        if (-1 != audioInputTrack) {
                            MediaFormat inputAudioFormat = audioExtractor.getTrackFormat(audioInputTrack);
                            int audioSampleRateHz = getInteger(inputAudioFormat, "sample-rate", 44100);
                            int audioChannelCount = getInteger(inputAudioFormat, "channel-count", 2);
                            int bitRate = getInteger(inputAudioFormat, "bitrate", 65536);
                            MediaFormat outputAudioFormat = MediaFormat.createAudioFormat("audio/mp4a-latm", audioSampleRateHz, audioChannelCount);
                            outputAudioFormat.setInteger("bitrate", bitRate);
                            outputAudioFormat.setInteger("aac-profile", 5);
                            audioEncoder = createAudioEncoder(audioCodecInfo, outputAudioFormat);
                            mediaCodec = createAudioDecoder(inputAudioFormat);
                        }
                        muxer = createMuxer();
                        if (degreesString != null) {
                            int degrees = Integer.parseInt(degreesString);
                            if (degrees >= 0) {
                                muxer.setOrientationHint(degrees);
                            }
                        }
                        if (-1 == audioInputTrack) {
                            doExtractDecodeEditEncodeMux(mediaExtractor, videoDecoder, mediaCodec2, muxer, inputSurface, outputSurface);
                        } else {
                            doExtractDecodeEditEncodeMux(mediaExtractor, audioExtractor, videoDecoder, mediaCodec2, mediaCodec, audioEncoder, muxer, inputSurface, outputSurface);
                        }
                        if (mediaExtractor != null) {
                            try {
                                mediaExtractor.release();
                            } catch (Throwable e) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoExtractor", e);
                                exception = e;
                            }
                        }
                        if (audioExtractor != null) {
                            try {
                                audioExtractor.release();
                            } catch (Throwable e2) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioExtractor", e2);
                                if (exception2 == null) {
                                    exception = e2;
                                }
                            }
                        }
                        if (videoDecoder != null) {
                            try {
                                videoDecoder.stop();
                                videoDecoder.release();
                            } catch (Throwable e22) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoDecoder", e22);
                                if (exception2 == null) {
                                    exception = e22;
                                }
                            }
                        }
                        if (outputSurface != null) {
                            try {
                                outputSurface.release();
                            } catch (Throwable e222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing outputSurface", e222);
                                if (exception2 == null) {
                                    exception = e222;
                                }
                            }
                        }
                        if (mediaCodec2 != null) {
                            try {
                                mediaCodec2.stop();
                                mediaCodec2.release();
                            } catch (Throwable e2222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoEncoder", e2222);
                                if (exception2 == null) {
                                    exception = e2222;
                                }
                            }
                        }
                        if (mediaCodec != null) {
                            try {
                                mediaCodec.stop();
                                mediaCodec.release();
                            } catch (Throwable e22222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioDecoder", e22222);
                                if (exception2 == null) {
                                    exception = e22222;
                                }
                            }
                        }
                        if (audioEncoder != null) {
                            try {
                                audioEncoder.stop();
                                audioEncoder.release();
                            } catch (Throwable e222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioEncoder", e222222);
                                if (exception2 == null) {
                                    exception = e222222;
                                }
                            }
                        }
                        if (muxer != null) {
                            try {
                                muxer.stop();
                                muxer.release();
                            } catch (Throwable e2222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing muxer", e2222222);
                                if (exception2 == null) {
                                    exception = e2222222;
                                }
                            }
                        }
                        if (inputSurface != null) {
                            try {
                                inputSurface.release();
                            } catch (Throwable e22222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing inputSurface", e22222222);
                                if (exception2 == null) {
                                    exception = e22222222;
                                }
                            }
                        }
                        if (this.mListener != null) {
                            this.mListener.onCompressCompleted(this.mOutputFile);
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        if (mediaExtractor != null) {
                            try {
                                mediaExtractor.release();
                            } catch (Throwable e222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoExtractor", e222222222);
                                exception = e222222222;
                            }
                        }
                        if (audioExtractor != null) {
                            try {
                                audioExtractor.release();
                            } catch (Throwable e2222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioExtractor", e2222222222);
                                if (exception2 == null) {
                                    exception = e2222222222;
                                }
                            }
                        }
                        if (videoDecoder != null) {
                            try {
                                videoDecoder.stop();
                                videoDecoder.release();
                            } catch (Throwable e22222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoDecoder", e22222222222);
                                if (exception2 == null) {
                                    exception = e22222222222;
                                }
                            }
                        }
                        if (outputSurface != null) {
                            try {
                                outputSurface.release();
                            } catch (Throwable e222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing outputSurface", e222222222222);
                                if (exception2 == null) {
                                    exception = e222222222222;
                                }
                            }
                        }
                        if (mediaCodec2 != null) {
                            try {
                                mediaCodec2.stop();
                                mediaCodec2.release();
                            } catch (Throwable e2222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing videoEncoder", e2222222222222);
                                if (exception2 == null) {
                                    exception = e2222222222222;
                                }
                            }
                        }
                        if (mediaCodec != null) {
                            try {
                                mediaCodec.stop();
                                mediaCodec.release();
                            } catch (Throwable e22222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioDecoder", e22222222222222);
                                if (exception2 == null) {
                                    exception = e22222222222222;
                                }
                            }
                        }
                        if (audioEncoder != null) {
                            try {
                                audioEncoder.stop();
                                audioEncoder.release();
                            } catch (Throwable e222222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing audioEncoder", e222222222222222);
                                if (exception2 == null) {
                                    exception = e222222222222222;
                                }
                            }
                        }
                        if (muxer != null) {
                            try {
                                muxer.stop();
                                muxer.release();
                            } catch (Throwable e2222222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing muxer", e2222222222222222);
                                if (exception2 == null) {
                                    exception = e2222222222222222;
                                }
                            }
                        }
                        if (inputSurface != null) {
                            try {
                                inputSurface.release();
                            } catch (Throwable e22222222222222222) {
                                MLog.e(RcsExtractAndMuxVideo.TAG, "error while releasing inputSurface", e22222222222222222);
                                if (exception2 == null) {
                                    exception = e22222222222222222;
                                }
                            }
                        }
                        if (this.mListener != null) {
                            this.mListener.onCompressCompleted(this.mOutputFile);
                        }
                        throw th;
                    }
                } catch (Throwable th3) {
                    th = th3;
                    outputSurface = null;
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                    if (audioExtractor != null) {
                        audioExtractor.release();
                    }
                    if (videoDecoder != null) {
                        videoDecoder.stop();
                        videoDecoder.release();
                    }
                    if (outputSurface != null) {
                        outputSurface.release();
                    }
                    if (mediaCodec2 != null) {
                        mediaCodec2.stop();
                        mediaCodec2.release();
                    }
                    if (mediaCodec != null) {
                        mediaCodec.stop();
                        mediaCodec.release();
                    }
                    if (audioEncoder != null) {
                        audioEncoder.stop();
                        audioEncoder.release();
                    }
                    if (muxer != null) {
                        muxer.stop();
                        muxer.release();
                    }
                    if (inputSurface != null) {
                        inputSurface.release();
                    }
                    if (this.mListener != null) {
                        this.mListener.onCompressCompleted(this.mOutputFile);
                    }
                    throw th;
                }
            } catch (Throwable th4) {
                th = th4;
                inputSurface = null;
                outputSurface = null;
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
                if (audioExtractor != null) {
                    audioExtractor.release();
                }
                if (videoDecoder != null) {
                    videoDecoder.stop();
                    videoDecoder.release();
                }
                if (outputSurface != null) {
                    outputSurface.release();
                }
                if (mediaCodec2 != null) {
                    mediaCodec2.stop();
                    mediaCodec2.release();
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                }
                if (audioEncoder != null) {
                    audioEncoder.stop();
                    audioEncoder.release();
                }
                if (muxer != null) {
                    muxer.stop();
                    muxer.release();
                }
                if (inputSurface != null) {
                    inputSurface.release();
                }
                if (this.mListener != null) {
                    this.mListener.onCompressCompleted(this.mOutputFile);
                }
                throw th;
            }
        }

        private int getInteger(MediaFormat format, String name, int defaultValue) {
            try {
                return format.getInteger(name);
            } catch (Throwable e) {
                MLog.d(RcsExtractAndMuxVideo.TAG, " use default value " + name, e);
                return defaultValue;
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

        private MediaCodec createAudioDecoder(MediaFormat inputFormat) throws IOException {
            MediaCodec decoder = MediaCodec.createDecoderByType(getMimeTypeFor(inputFormat));
            decoder.configure(inputFormat, null, null, 0);
            decoder.start();
            return decoder;
        }

        private MediaCodec createAudioEncoder(MediaCodecInfo codecInfo, MediaFormat format) throws IOException {
            MediaCodec encoder = MediaCodec.createByCodecName(codecInfo.getName());
            encoder.configure(format, null, null, 1);
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

        private void getCopyFileStream() {
            Uri outputFileUri = null;
            try {
                if (this.mContext != null) {
                    File file = new File(this.mInputFile);
                    if (file.exists()) {
                        this.mFileSize = file.length();
                    }
                    Uri inputFileUri = Uri.fromFile(new File(this.mInputFile));
                    Uri[] uris = RcsTransaction.getCopyFileBeforeSendSingleUri(this.mContext, inputFileUri);
                    if (uris != null && uris.length == 2) {
                        outputFileUri = uris[0];
                    }
                    if (inputFileUri != null && outputFileUri != null && inputFileUri != outputFileUri) {
                        this.mIsForCopyFile = this.mContext.getContentResolver().openInputStream(inputFileUri);
                        this.mOsForCopyFile = this.mContext.getContentResolver().openOutputStream(outputFileUri);
                    }
                }
            } catch (FileNotFoundException e) {
                MLog.e(RcsExtractAndMuxVideo.TAG, "getCopyFileStream FileNotFoundException" + e.getMessage());
            }
        }

        private long copyFileToRcsDir(long sizeCopied, int progerss) {
            byte[] buf = new byte[ViewPartId.PART_BODY_SIMPLE_CALL_NUMBER];
            if (this.mIsForCopyFile != null && this.mOsForCopyFile != null && this.mFileSize != 0) {
                double copyProgress = ((double) sizeCopied) / ((double) this.mFileSize);
                while (((int) (100.0d * copyProgress)) <= progerss) {
                    try {
                        int nRead = this.mIsForCopyFile.read(buf);
                        if (nRead < 0) {
                            break;
                        }
                        this.mOsForCopyFile.write(buf, 0, nRead);
                        sizeCopied += (long) nRead;
                        copyProgress = ((double) sizeCopied) / ((double) this.mFileSize);
                    } catch (Throwable e) {
                        MLog.e(RcsExtractAndMuxVideo.TAG, "copyFileToRcsDir IOException", e);
                    }
                }
            }
            return sizeCopied;
        }

        private void doExtractDecodeEditEncodeMux(MediaExtractor videoExtractor, MediaCodec videoDecoder, MediaCodec videoEncoder, MediaMuxer muxer, RcsInputSurface inputSurface, RcsOutputSurface outputSurface) {
            if (this.mListener != null) {
                this.mListener.onCompressStarted(this.mOutputFile);
            }
            ByteBuffer[] videoDecoderInputBuffers = videoDecoder.getInputBuffers();
            ByteBuffer[] videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
            BufferInfo videoDecoderOutputBufferInfo = new BufferInfo();
            BufferInfo videoEncoderOutputBufferInfo = new BufferInfo();
            MediaFormat encoderOutputVideoFormat = null;
            int outputVideoTrack = -1;
            boolean videoExtractorDone = false;
            boolean videoDecoderDone = false;
            boolean videoEncoderDone = false;
            boolean muxing = false;
            long sizeCopied = 0;
            getCopyFileStream();
            while (!videoEncoderDone) {
                if (!videoExtractorDone && (r22 == null || muxing)) {
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
                if (!videoDecoderDone && (r22 == null || muxing)) {
                    int decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, 10000);
                    if (decoderOutputBufferIndex == -1) {
                        verbose("no video decoder output buffer");
                    } else if (decoderOutputBufferIndex == -2) {
                        verbose("video decoder: output format changed: " + videoDecoder.getOutputFormat());
                    } else if (decoderOutputBufferIndex == -3) {
                        verbose("video decoder: output buffers changed");
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
                                if (outputSurface != null) {
                                    outputSurface.awaitNewImage();
                                }
                                verbose("output surface: draw image");
                                if (outputSurface != null) {
                                    outputSurface.drawImage();
                                }
                                if (inputSurface != null) {
                                    inputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
                                }
                                verbose("input surface: swap buffers");
                                if (inputSurface != null) {
                                    inputSurface.swapBuffers();
                                }
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
                if (!videoEncoderDone && (r22 == null || muxing)) {
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
                                muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
                                int progress = clamp((int) ((100 * videoEncoderOutputBufferInfo.presentationTimeUs) / this.mDuration), 0, 100);
                                sizeCopied = copyFileToRcsDir(sizeCopied, progress);
                                if (this.mListener != null) {
                                    this.mListener.onCompressProcessing(progress);
                                }
                            }
                            videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                        }
                    }
                }
                if (this.mListener != null && this.mListener.isCanceled()) {
                    break;
                } else if (!(muxing || encoderOutputVideoFormat == null)) {
                    MLog.d(RcsExtractAndMuxVideo.TAG, "muxer: adding video track.");
                    outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
                    muxer.start();
                    muxing = true;
                }
            }
            try {
                copyFileToRcsDir(sizeCopied, 100);
                try {
                    if (this.mOsForCopyFile != null) {
                        this.mOsForCopyFile.close();
                    }
                } catch (Throwable ex) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex);
                }
            } finally {
                try {
                    if (this.mIsForCopyFile != null) {
                        this.mIsForCopyFile.close();
                    }
                } catch (Throwable ex2) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex2);
                }
                try {
                    if (this.mOsForCopyFile != null) {
                        this.mOsForCopyFile.close();
                    }
                } catch (Throwable ex22) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex22);
                }
            }
        }

        private void doExtractDecodeEditEncodeMux(MediaExtractor videoExtractor, MediaExtractor audioExtractor, MediaCodec videoDecoder, MediaCodec videoEncoder, MediaCodec audioDecoder, MediaCodec audioEncoder, MediaMuxer muxer, RcsInputSurface inputSurface, RcsOutputSurface outputSurface) {
            if (this.mListener != null) {
                this.mListener.onCompressStarted(this.mOutputFile);
            }
            ByteBuffer[] videoDecoderInputBuffers = videoDecoder.getInputBuffers();
            ByteBuffer[] videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
            BufferInfo videoDecoderOutputBufferInfo = new BufferInfo();
            BufferInfo videoEncoderOutputBufferInfo = new BufferInfo();
            ByteBuffer[] audioDecoderInputBuffers = audioDecoder.getInputBuffers();
            ByteBuffer[] audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
            ByteBuffer[] audioEncoderInputBuffers = audioEncoder.getInputBuffers();
            ByteBuffer[] audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
            BufferInfo audioDecoderOutputBufferInfo = new BufferInfo();
            BufferInfo audioEncoderOutputBufferInfo = new BufferInfo();
            MediaFormat encoderOutputVideoFormat = null;
            MediaFormat encoderOutputAudioFormat = null;
            int outputVideoTrack = -1;
            int outputAudioTrack = -1;
            boolean videoExtractorDone = false;
            boolean videoDecoderDone = false;
            boolean videoEncoderDone = false;
            boolean audioExtractorDone = false;
            boolean audioDecoderDone = false;
            boolean audioEncoderDone = false;
            int pendingAudioDecoderOutputBufferIndex = -1;
            boolean muxing = false;
            long sizeCopied = 0;
            getCopyFileStream();
            while (true) {
                if (!videoEncoderDone || !audioEncoderDone) {
                    int decoderInputBufferIndex;
                    long presentationTime;
                    int size;
                    int decoderOutputBufferIndex;
                    ByteBuffer decoderOutputBuffer;
                    int encoderOutputBufferIndex;
                    ByteBuffer encoderOutputBuffer;
                    if (!videoExtractorDone && (r35 == null || muxing)) {
                        decoderInputBufferIndex = videoDecoder.dequeueInputBuffer(10000);
                        if (decoderInputBufferIndex == -1) {
                            verbose("no video decoder input buffer");
                        } else {
                            verbose("video decoder: returned input buffer: " + decoderInputBufferIndex);
                            ByteBuffer decoderInputBuffer = videoDecoderInputBuffers[decoderInputBufferIndex];
                            presentationTime = videoExtractor.getSampleTime();
                            size = videoExtractor.readSampleData(decoderInputBuffer, 0);
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
                    if (!audioExtractorDone && (r32 == null || muxing)) {
                        decoderInputBufferIndex = audioDecoder.dequeueInputBuffer(10000);
                        if (decoderInputBufferIndex != -1) {
                            size = audioExtractor.readSampleData(audioDecoderInputBuffers[decoderInputBufferIndex], 0);
                            presentationTime = audioExtractor.getSampleTime();
                            if (size >= 0) {
                                audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, size, presentationTime, audioExtractor.getSampleFlags());
                            }
                            audioExtractorDone = !audioExtractor.advance();
                            if (audioExtractorDone) {
                                verbose("audio extractor: EOS");
                                audioDecoder.queueInputBuffer(decoderInputBufferIndex, 0, 0, 0, 4);
                            }
                        }
                    }
                    if (!videoDecoderDone && (r35 == null || muxing)) {
                        decoderOutputBufferIndex = videoDecoder.dequeueOutputBuffer(videoDecoderOutputBufferInfo, 10000);
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
                                    if (outputSurface != null) {
                                        outputSurface.awaitNewImage();
                                    }
                                    verbose("output surface: draw image");
                                    if (outputSurface != null) {
                                        outputSurface.drawImage();
                                    }
                                    if (inputSurface != null) {
                                        inputSurface.setPresentationTime(videoDecoderOutputBufferInfo.presentationTimeUs * 1000);
                                    }
                                    verbose("input surface: swap buffers");
                                    if (inputSurface != null) {
                                        inputSurface.swapBuffers();
                                    }
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
                    if (!audioDecoderDone && pendingAudioDecoderOutputBufferIndex == -1 && (r32 == null || muxing)) {
                        decoderOutputBufferIndex = audioDecoder.dequeueOutputBuffer(audioDecoderOutputBufferInfo, 10000);
                        if (decoderOutputBufferIndex == -1) {
                            verbose("no audio decoder output buffer");
                        } else if (decoderOutputBufferIndex == -3) {
                            verbose("audio decoder: output buffers changed");
                            audioDecoderOutputBuffers = audioDecoder.getOutputBuffers();
                        } else if (decoderOutputBufferIndex == -2) {
                            verbose("audio decoder: output format changed: " + audioDecoder.getOutputFormat());
                        } else {
                            verbose("audio decoder: returned output buffer: " + decoderOutputBufferIndex);
                            verbose("audio decoder: returned buffer of size " + audioDecoderOutputBufferInfo.size);
                            decoderOutputBuffer = audioDecoderOutputBuffers[decoderOutputBufferIndex];
                            if ((audioDecoderOutputBufferInfo.flags & 2) != 0) {
                                verbose("audio decoder: codec config buffer");
                                audioDecoder.releaseOutputBuffer(decoderOutputBufferIndex, false);
                            } else {
                                verbose("audio decoder: returned buffer for time " + audioDecoderOutputBufferInfo.presentationTimeUs);
                                verbose("audio decoder: output buffer is now pending: " + pendingAudioDecoderOutputBufferIndex);
                                pendingAudioDecoderOutputBufferIndex = decoderOutputBufferIndex;
                            }
                        }
                    }
                    if (pendingAudioDecoderOutputBufferIndex != -1) {
                        verbose("audio decoder: attempting to process pending buffer: " + pendingAudioDecoderOutputBufferIndex);
                        int encoderInputBufferIndex = audioEncoder.dequeueInputBuffer(10000);
                        if (encoderInputBufferIndex == -1) {
                            verbose("no audio encoder input buffer");
                        } else {
                            verbose("audio encoder: returned input buffer: " + encoderInputBufferIndex);
                            ByteBuffer encoderInputBuffer = audioEncoderInputBuffers[encoderInputBufferIndex];
                            size = audioDecoderOutputBufferInfo.size;
                            presentationTime = audioDecoderOutputBufferInfo.presentationTimeUs;
                            verbose("audio decoder: processing pending buffer: " + pendingAudioDecoderOutputBufferIndex);
                            verbose("audio decoder: pending buffer of size " + size);
                            verbose("audio decoder: pending buffer for time " + presentationTime);
                            if (size >= 0) {
                                decoderOutputBuffer = audioDecoderOutputBuffers[pendingAudioDecoderOutputBufferIndex].duplicate();
                                decoderOutputBuffer.position(audioDecoderOutputBufferInfo.offset);
                                decoderOutputBuffer.limit(audioDecoderOutputBufferInfo.offset + size);
                                encoderInputBuffer.position(0);
                                encoderInputBuffer.put(decoderOutputBuffer);
                                audioEncoder.queueInputBuffer(encoderInputBufferIndex, 0, size, presentationTime, audioDecoderOutputBufferInfo.flags);
                            }
                            audioDecoder.releaseOutputBuffer(pendingAudioDecoderOutputBufferIndex, false);
                            pendingAudioDecoderOutputBufferIndex = -1;
                            if ((audioDecoderOutputBufferInfo.flags & 4) != 0) {
                                verbose("audio decoder: EOS");
                                audioDecoderDone = true;
                            }
                        }
                    }
                    if (!videoEncoderDone && (r35 == null || muxing)) {
                        encoderOutputBufferIndex = videoEncoder.dequeueOutputBuffer(videoEncoderOutputBufferInfo, 10000);
                        if (encoderOutputBufferIndex == -1) {
                            verbose("no video encoder output buffer");
                        } else if (encoderOutputBufferIndex == -2) {
                            verbose("video encoder: output format changed");
                            if (outputVideoTrack >= 0) {
                                verbose("video encoder changed its output format again?");
                            }
                            encoderOutputVideoFormat = videoEncoder.getOutputFormat();
                        } else if (encoderOutputBufferIndex == -3) {
                            verbose("video encoder: output buffers changed");
                            videoEncoderOutputBuffers = videoEncoder.getOutputBuffers();
                        } else {
                            verbose("should have added track before processing output " + muxing);
                            verbose("video encoder: returned output buffer: " + encoderOutputBufferIndex);
                            verbose("video encoder: returned buffer of size " + videoEncoderOutputBufferInfo.size);
                            encoderOutputBuffer = videoEncoderOutputBuffers[encoderOutputBufferIndex];
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
                                    muxer.writeSampleData(outputVideoTrack, encoderOutputBuffer, videoEncoderOutputBufferInfo);
                                }
                                videoEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            }
                        }
                    }
                    if (!audioEncoderDone && (r32 == null || muxing)) {
                        encoderOutputBufferIndex = audioEncoder.dequeueOutputBuffer(audioEncoderOutputBufferInfo, 10000);
                        if (encoderOutputBufferIndex == -1) {
                            verbose("no audio encoder output buffer");
                        } else if (encoderOutputBufferIndex == -3) {
                            verbose("audio encoder: output buffers changed");
                            audioEncoderOutputBuffers = audioEncoder.getOutputBuffers();
                        } else if (encoderOutputBufferIndex == -2) {
                            verbose("audio encoder: output format changed");
                            encoderOutputAudioFormat = audioEncoder.getOutputFormat();
                        } else {
                            verbose("should have added track before processing output " + muxing);
                            verbose("audio encoder: returned output buffer: " + encoderOutputBufferIndex);
                            verbose("audio encoder: returned buffer of size " + audioEncoderOutputBufferInfo.size);
                            encoderOutputBuffer = audioEncoderOutputBuffers[encoderOutputBufferIndex];
                            if ((audioEncoderOutputBufferInfo.flags & 2) != 0) {
                                verbose("audio encoder: codec config buffer");
                                audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            } else {
                                verbose("audio encoder: returned buffer for time " + audioEncoderOutputBufferInfo.presentationTimeUs);
                                verbose("audio encoder: returned buffer flags " + audioEncoderOutputBufferInfo.flags);
                                if ((audioEncoderOutputBufferInfo.flags & 4) != 0) {
                                    verbose("audio encoder: EOS");
                                    audioEncoderDone = true;
                                } else if (audioEncoderOutputBufferInfo.size != 0) {
                                    muxer.writeSampleData(outputAudioTrack, encoderOutputBuffer, audioEncoderOutputBufferInfo);
                                    int progress = clamp((int) ((100 * audioEncoderOutputBufferInfo.presentationTimeUs) / this.mDuration), 0, 100);
                                    sizeCopied = copyFileToRcsDir(sizeCopied, progress);
                                    if (this.mListener != null) {
                                        this.mListener.onCompressProcessing(progress);
                                    }
                                }
                                audioEncoder.releaseOutputBuffer(encoderOutputBufferIndex, false);
                            }
                        }
                    }
                    if (this.mListener != null && this.mListener.isCanceled()) {
                        break;
                    } else if (!(muxing || encoderOutputAudioFormat == null || encoderOutputVideoFormat == null)) {
                        MLog.d(RcsExtractAndMuxVideo.TAG, "muxer: adding video track.");
                        outputVideoTrack = muxer.addTrack(encoderOutputVideoFormat);
                        MLog.d(RcsExtractAndMuxVideo.TAG, "muxer: adding audio track.");
                        outputAudioTrack = muxer.addTrack(encoderOutputAudioFormat);
                        MLog.d(RcsExtractAndMuxVideo.TAG, "muxer: starting");
                        muxer.start();
                        muxing = true;
                    }
                } else {
                    break;
                }
            }
            try {
                copyFileToRcsDir(sizeCopied, 100);
                try {
                    if (this.mOsForCopyFile != null) {
                        this.mOsForCopyFile.close();
                    }
                } catch (Throwable ex) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex);
                }
            } finally {
                try {
                    if (this.mIsForCopyFile != null) {
                        this.mIsForCopyFile.close();
                    }
                } catch (Throwable ex2) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex2);
                }
                try {
                    if (this.mOsForCopyFile != null) {
                        this.mOsForCopyFile.close();
                    }
                } catch (Throwable ex22) {
                    MLog.e(RcsExtractAndMuxVideo.TAG, "doExtractDecodeEditEncodeMux IOException", ex22);
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

        private int clamp(int progress, int min, int max) {
            if (progress > max) {
                return max;
            }
            if (progress < min) {
                return min;
            }
            return progress;
        }
    }

    public static void extractDecodeEditEncodeMuxAudioVideo(String inputFile, VideoMuxListener listener, Context context) {
        mWorker = Worker.doJob(inputFile, listener, context);
    }
}
