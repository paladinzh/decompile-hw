package com.fyusion.sdk.processor;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.opengl.GLES20;
import android.support.annotation.VisibleForTesting;
import android.util.Log;
import com.fyusion.sdk.common.ext.FyuseDescriptor;
import com.fyusion.sdk.common.ext.k;
import com.fyusion.sdk.common.h;
import com.fyusion.sdk.common.util.b;
import java.io.IOException;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class FrameExtractor {
    private static final int MAX_IFRAME_INTERVAL = 100;
    private static final long MICROSEC_MULTIPLER = 1000000;
    final String TAG = "FrameExtractor";
    final boolean VERBOSE = false;
    int currentBufferedFrame = -1;
    MediaCodec decoder = null;
    FyuseDescriptor descriptor;
    int estimatedIFrameDistance = -1;
    MediaExtractor extractor = null;
    long frameDuration = 0;
    int framesPerSecond;
    int nextFrame = 0;
    com.fyusion.sdk.processor.mjpegutils.a outputSurface;
    ByteBuffer pixelBuffer;
    byte[] pixelBufferData;
    long timeSpent = 0;

    /* compiled from: Unknown */
    class a extends Exception {
        final /* synthetic */ FrameExtractor a;

        public a(FrameExtractor frameExtractor, String str) {
            this.a = frameExtractor;
            super(str);
        }
    }

    public FrameExtractor(FyuseDescriptor fyuseDescriptor) {
        this.descriptor = fyuseDescriptor;
        this.framesPerSecond = k.j;
    }

    private byte[] decodeFrame(int i, int i2, boolean z) throws a {
        int dequeueOutputBuffer;
        ByteBuffer[] inputBuffers = this.decoder.getInputBuffers();
        Object obj = null;
        Object obj2 = null;
        BufferInfo bufferInfo = new BufferInfo();
        if (i >= 0) {
            this.decoder.flush();
            while (true) {
                dequeueOutputBuffer = this.decoder.dequeueOutputBuffer(bufferInfo, MICROSEC_MULTIPLER);
                if (dequeueOutputBuffer >= 0) {
                    this.decoder.releaseOutputBuffer(dequeueOutputBuffer, false);
                }
                if (dequeueOutputBuffer < 0) {
                    break;
                }
            }
        }
        if (i >= 0) {
            this.nextFrame = i;
        }
        int i3 = 0;
        int i4 = 0;
        while (true) {
            int i5;
            if (obj != null) {
                i5 = i4;
            } else {
                i5 = this.decoder.dequeueInputBuffer(MICROSEC_MULTIPLER);
                if (i5 < 0) {
                    i5 = i4 + 1;
                } else {
                    Object obj3;
                    dequeueOutputBuffer = this.extractor.readSampleData(inputBuffers[i5], 0);
                    long sampleTime = this.extractor.getSampleTime();
                    if (dequeueOutputBuffer < 0 || !this.extractor.advance()) {
                        int i6 = 1;
                    } else {
                        obj3 = obj;
                    }
                    this.decoder.queueInputBuffer(i5, 0, Math.max(0, dequeueOutputBuffer), sampleTime, obj3 == null ? 0 : 4);
                    obj = obj3;
                    i5 = 0;
                }
                if (i5 > 10) {
                    throw new a(this, "Unable to obtain input buffer");
                }
            }
            int dequeueOutputBuffer2 = this.decoder.dequeueOutputBuffer(bufferInfo, MICROSEC_MULTIPLER);
            if ((bufferInfo.flags & 4) != 0 && bufferInfo.size <= 0) {
                throw new a(this, "Error: Unexpectedly hit EOS. This is due to probably requesting a frame not present in the video file.");
            }
            if (dequeueOutputBuffer2 >= 0) {
                this.nextFrame++;
                if (i2 == this.nextFrame - 1) {
                    dequeueOutputBuffer = 1;
                    i4 = i3 + 1;
                    if (i3 >= 10) {
                        if (dequeueOutputBuffer2 >= 0) {
                            MediaCodec mediaCodec = this.decoder;
                            boolean z2 = z && r3 != null;
                            mediaCodec.releaseOutputBuffer(dequeueOutputBuffer2, z2);
                            i4 = 0;
                        }
                        if (r3 == null) {
                            break;
                        }
                        obj2 = r3;
                        i3 = i4;
                        i4 = i5;
                    } else {
                        h.d("getImageDataForFrame", "Error extracting frame: " + dequeueOutputBuffer2 + ", num attempts: " + i4);
                        throw new a(this, "Error extracting frame, gave it " + i4 + " attempts");
                    }
                }
            }
            Object obj4 = obj2;
            i4 = i3 + 1;
            if (i3 >= 10) {
                h.d("getImageDataForFrame", "Error extracting frame: " + dequeueOutputBuffer2 + ", num attempts: " + i4);
                throw new a(this, "Error extracting frame, gave it " + i4 + " attempts");
            }
            if (dequeueOutputBuffer2 >= 0) {
                MediaCodec mediaCodec2 = this.decoder;
                if (z) {
                    mediaCodec2.releaseOutputBuffer(dequeueOutputBuffer2, z2);
                    i4 = 0;
                }
                mediaCodec2.releaseOutputBuffer(dequeueOutputBuffer2, z2);
                i4 = 0;
            }
            if (obj4 == null) {
                break;
            }
            obj2 = obj4;
            i3 = i4;
            i4 = i5;
        }
        if (z && this.outputSurface.d()) {
            this.outputSurface.a(true);
            i4 = this.descriptor.getMagic().getCameraWidth();
            dequeueOutputBuffer = this.descriptor.getMagic().getCameraHeight();
            if (this.pixelBufferData == null) {
                this.pixelBufferData = new byte[((i4 * dequeueOutputBuffer) * 4)];
                this.pixelBuffer = ByteBuffer.wrap(this.pixelBufferData);
            }
            this.pixelBuffer.rewind();
            GLES20.glFinish();
            GLES20.glReadPixels(0, 0, i4, dequeueOutputBuffer, 6408, 5121, this.pixelBuffer);
            this.pixelBuffer.rewind();
            this.currentBufferedFrame = this.nextFrame - 1;
        }
        return !z ? null : this.pixelBufferData;
    }

    public byte[] getImageDataForFrame(int i) {
        int i2 = -1;
        long nanoTime = System.nanoTime();
        try {
            byte[] bArr;
            if (i != this.currentBufferedFrame) {
                if (this.framesPerSecond > 0) {
                    if (this.estimatedIFrameDistance != -1) {
                        long j = (((long) i) * MICROSEC_MULTIPLER) / ((long) this.framesPerSecond);
                        if (i != this.nextFrame) {
                            if (this.nextFrame / this.estimatedIFrameDistance == i / this.estimatedIFrameDistance && this.nextFrame <= i) {
                                this.extractor.getSampleTime();
                            } else {
                                this.extractor.seekTo(j + (this.frameDuration / 2), 0);
                                i2 = (int) (this.extractor.getSampleTime() / this.frameDuration);
                            }
                            decodeFrame(i2, i, true);
                        } else {
                            decodeFrame(-1, i, true);
                        }
                        bArr = this.pixelBufferData;
                        this.timeSpent = (System.nanoTime() - nanoTime) + this.timeSpent;
                        return bArr;
                    }
                }
                if (this.nextFrame <= i) {
                    decodeFrame(-1, i, true);
                } else {
                    this.extractor.seekTo(0, 0);
                    this.nextFrame = 0;
                    this.currentBufferedFrame = -1;
                    decodeFrame(0, i, true);
                }
                bArr = this.pixelBufferData;
                this.timeSpent = (System.nanoTime() - nanoTime) + this.timeSpent;
                return bArr;
            }
            bArr = this.pixelBufferData;
            this.timeSpent = (System.nanoTime() - nanoTime) + this.timeSpent;
            return bArr;
        } catch (a e) {
            e.printStackTrace();
            this.timeSpent = (System.nanoTime() - nanoTime) + this.timeSpent;
            return null;
        } catch (Throwable th) {
            this.timeSpent = (System.nanoTime() - nanoTime) + this.timeSpent;
        }
    }

    public boolean startExtractor() {
        this.timeSpent = 0;
        this.nextFrame = 0;
        this.currentBufferedFrame = -1;
        this.estimatedIFrameDistance = -1;
        return startExtractor(com.fyusion.sdk.common.ext.h.a().a(this.descriptor.getFyusePath()).getPath());
    }

    @VisibleForTesting
    boolean startExtractor(String str) {
        this.extractor = new MediaExtractor();
        try {
            String str2;
            int i;
            this.extractor.setDataSource(str);
            String str3 = null;
            int i2 = 0;
            while (i2 < this.extractor.getTrackCount()) {
                try {
                    str3 = this.extractor.getTrackFormat(i2).getString("mime");
                    if (str3.startsWith("video/")) {
                        this.extractor.selectTrack(i2);
                        str2 = str3;
                        i = i2;
                        break;
                    }
                    i2++;
                } catch (IllegalArgumentException e) {
                    h.d("FrameExtractor", "unable to get track format");
                    return false;
                }
            }
            i = -1;
            str2 = str3;
            if (i >= 0) {
                int integer;
                boolean z;
                long sampleTime;
                long sampleTime2;
                MediaFormat trackFormat = this.extractor.getTrackFormat(i);
                try {
                    integer = trackFormat.getInteger("frame-rate");
                    i2 = trackFormat.getInteger("i-frame-interval");
                    if (integer > 0) {
                        this.framesPerSecond = integer;
                        i2 *= this.framesPerSecond;
                        z = false;
                        this.outputSurface = new com.fyusion.sdk.processor.mjpegutils.a(this.descriptor.getMagic().getCameraWidth(), this.descriptor.getMagic().getCameraHeight());
                        this.decoder = MediaCodec.createDecoderByType(str2);
                        try {
                            b.a(this.decoder, this.extractor.getTrackFormat(i), this.outputSurface.c(), 0);
                            this.decoder.start();
                            if (z) {
                                sampleTime = this.extractor.getSampleTime();
                                this.extractor.advance();
                                sampleTime2 = this.extractor.getSampleTime();
                                this.extractor.seekTo(0, 0);
                                if (sampleTime2 - sampleTime <= 0) {
                                    Log.e("FrameExtractor", "Something is very wrong with this video: frame0=" + sampleTime + " frame1=" + sampleTime2);
                                    return true;
                                }
                                this.framesPerSecond = (int) Math.round(1000000.0d / ((double) (sampleTime2 - sampleTime)));
                                this.frameDuration = MICROSEC_MULTIPLER / ((long) this.framesPerSecond);
                                i2 = this.framesPerSecond * 2;
                            }
                            for (integer = i2; integer < MAX_IFRAME_INTERVAL; integer *= 2) {
                                this.extractor.seekTo((((long) (integer + 2)) * MICROSEC_MULTIPLER) / ((long) this.framesPerSecond), 0);
                                sampleTime = this.extractor.getSampleTime();
                                if (!(sampleTime > this.frameDuration)) {
                                    this.estimatedIFrameDistance = (int) Math.round(((double) (sampleTime * ((long) this.framesPerSecond))) / 1000000.0d);
                                    break;
                                }
                            }
                            this.extractor.seekTo(0, 0);
                            return true;
                        } catch (Exception e2) {
                            h.d("startExtractor", "caught general exception from decoder.configure with output surface: " + e2.getMessage());
                            if (this.outputSurface != null) {
                                this.outputSurface.a();
                            }
                            return false;
                        }
                    }
                } catch (NullPointerException e3) {
                }
                i2 = 0;
                z = true;
                this.outputSurface = new com.fyusion.sdk.processor.mjpegutils.a(this.descriptor.getMagic().getCameraWidth(), this.descriptor.getMagic().getCameraHeight());
                try {
                    this.decoder = MediaCodec.createDecoderByType(str2);
                    b.a(this.decoder, this.extractor.getTrackFormat(i), this.outputSurface.c(), 0);
                    this.decoder.start();
                    if (z) {
                        sampleTime = this.extractor.getSampleTime();
                        this.extractor.advance();
                        sampleTime2 = this.extractor.getSampleTime();
                        this.extractor.seekTo(0, 0);
                        if (sampleTime2 - sampleTime <= 0) {
                        }
                        if (sampleTime2 - sampleTime <= 0) {
                            this.framesPerSecond = (int) Math.round(1000000.0d / ((double) (sampleTime2 - sampleTime)));
                            this.frameDuration = MICROSEC_MULTIPLER / ((long) this.framesPerSecond);
                            i2 = this.framesPerSecond * 2;
                        } else {
                            Log.e("FrameExtractor", "Something is very wrong with this video: frame0=" + sampleTime + " frame1=" + sampleTime2);
                            return true;
                        }
                    }
                    for (integer = i2; integer < MAX_IFRAME_INTERVAL; integer *= 2) {
                        this.extractor.seekTo((((long) (integer + 2)) * MICROSEC_MULTIPLER) / ((long) this.framesPerSecond), 0);
                        sampleTime = this.extractor.getSampleTime();
                        if (sampleTime > this.frameDuration) {
                        }
                        if (!(sampleTime > this.frameDuration)) {
                            this.estimatedIFrameDistance = (int) Math.round(((double) (sampleTime * ((long) this.framesPerSecond))) / 1000000.0d);
                            break;
                        }
                    }
                    this.extractor.seekTo(0, 0);
                    return true;
                } catch (IOException e4) {
                    h.d("startExtractor", "unable to create decoder for mime ");
                    return false;
                }
            }
            h.d("FrameExtractor", "unable to find a video track");
            return false;
        } catch (IOException e5) {
            h.d("FrameExtractor", "could not set data source");
            return false;
        }
    }

    public boolean stopExtractor() {
        this.pixelBufferData = null;
        this.pixelBuffer = null;
        if (this.outputSurface != null) {
            this.outputSurface.a();
        }
        this.outputSurface = null;
        if (this.extractor != null) {
            this.extractor.release();
        }
        this.extractor = null;
        if (this.decoder != null) {
            try {
                this.decoder.stop();
            } catch (IllegalStateException e) {
                h.d("FrameExtractor", "Stopping decoder, but already released.");
            }
            this.decoder.release();
        }
        this.decoder = null;
        return true;
    }
}
