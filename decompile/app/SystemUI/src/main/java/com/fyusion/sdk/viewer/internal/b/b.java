package com.fyusion.sdk.viewer.internal.b;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.util.Log;
import com.fyusion.sdk.core.util.d;
import java.io.IOException;
import java.nio.ByteBuffer;

/* compiled from: Unknown */
public class b {
    private volatile boolean a;

    /* compiled from: Unknown */
    public interface a {
        void a(int i);

        void a(int i, com.fyusion.sdk.core.a.b bVar);
    }

    private int a(MediaExtractor mediaExtractor) {
        int trackCount = mediaExtractor.getTrackCount();
        for (int i = 0; i < trackCount; i++) {
            MediaFormat trackFormat = mediaExtractor.getTrackFormat(i);
            String string = trackFormat.getString("mime");
            if (string.startsWith("video/")) {
                Log.d("FyuseDecoder", "Extractor selected track " + i + " (" + string + "): " + trackFormat);
                return i;
            }
        }
        return -1;
    }

    private void a(MediaExtractor mediaExtractor, MediaCodec mediaCodec, com.fyusion.sdk.core.a.a aVar, a aVar2) {
        ByteBuffer[] inputBuffers = mediaCodec.getInputBuffers();
        BufferInfo bufferInfo = new BufferInfo();
        int i = 0;
        int i2 = 0;
        Object obj = null;
        Object obj2 = null;
        long a = d.a();
        while (obj == null && !this.a) {
            Object obj3;
            if (obj2 == null) {
                int dequeueInputBuffer = mediaCodec.dequeueInputBuffer(10000);
                if (dequeueInputBuffer < 0) {
                    Log.d("FyuseDecoder", "input buffer not available");
                } else {
                    int readSampleData = mediaExtractor.readSampleData(inputBuffers[dequeueInputBuffer], 0);
                    if (readSampleData >= 0) {
                        mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, readSampleData, mediaExtractor.getSampleTime(), 0);
                        if (Log.isLoggable("FyuseDecoder", 3)) {
                            Log.d("FyuseDecoder", "submitted frame " + i + " to dec, size=" + readSampleData);
                        }
                        int i3 = i + 1;
                        mediaExtractor.advance();
                        dequeueInputBuffer = i3;
                        obj3 = obj2;
                    } else {
                        mediaCodec.queueInputBuffer(dequeueInputBuffer, 0, 0, 0, 4);
                        obj3 = 1;
                        if (Log.isLoggable("FyuseDecoder", 3)) {
                            Log.d("FyuseDecoder", "sent input EOS");
                        }
                        dequeueInputBuffer = i;
                    }
                    obj2 = obj3;
                    i = dequeueInputBuffer;
                }
            }
            if (obj == null) {
                int dequeueOutputBuffer = mediaCodec.dequeueOutputBuffer(bufferInfo, 10000);
                if (dequeueOutputBuffer != -1) {
                    if (dequeueOutputBuffer != -3) {
                        if (dequeueOutputBuffer == -2) {
                            MediaFormat outputFormat = mediaCodec.getOutputFormat();
                            if (Log.isLoggable("FyuseDecoder", 3)) {
                                Log.d("FyuseDecoder", "decoder output format changed: " + outputFormat);
                            }
                        } else if (dequeueOutputBuffer >= 0) {
                            if (Log.isLoggable("FyuseDecoder", 3)) {
                                Log.d("FyuseDecoder", "surface decoder given buffer " + dequeueOutputBuffer + " (size=" + bufferInfo.size + ")");
                            }
                            if ((bufferInfo.flags & 4) == 0) {
                                obj3 = obj;
                            } else {
                                if (Log.isLoggable("FyuseDecoder", 3)) {
                                    Log.d("FyuseDecoder", "output EOS");
                                }
                                obj3 = 1;
                            }
                            boolean z = bufferInfo.size != 0;
                            mediaCodec.releaseOutputBuffer(dequeueOutputBuffer, z);
                            if (z) {
                                if (Log.isLoggable("FyuseDecoder", 3)) {
                                    Log.d("FyuseDecoder", "awaiting decode of frame " + i2);
                                }
                                aVar.c();
                                aVar.a(true);
                                aVar2.a(i2, aVar.d());
                                obj = obj3;
                                i3 = i2 + 1;
                            } else {
                                obj = obj3;
                                i3 = i2;
                            }
                        } else {
                            Log.e("FyuseDecoder", "unexpected result from decoder.dequeueOutputBuffer: " + dequeueOutputBuffer);
                        }
                    } else if (Log.isLoggable("FyuseDecoder", 3)) {
                        Log.d("FyuseDecoder", "decoder output buffers changed");
                    }
                    i3 = i2;
                } else {
                    if (Log.isLoggable("FyuseDecoder", 3)) {
                        Log.d("FyuseDecoder", "no output from decoder available");
                    }
                    i3 = i2;
                }
                i2 = i3;
            }
        }
        Log.d("FyuseDecoder", "complete decode due to isCancelled:" + this.a + " in " + d.a(a));
        aVar2.a(i2);
    }

    public void a() {
        this.a = true;
    }

    public void a(String str, int i, int i2, a aVar) {
        MediaExtractor mediaExtractor;
        com.fyusion.sdk.core.a.a aVar2;
        Throwable e;
        MediaCodec mediaCodec;
        com.fyusion.sdk.core.a.a aVar3;
        MediaCodec mediaCodec2;
        Object obj;
        com.fyusion.sdk.core.a.a aVar4 = null;
        try {
            MediaFormat trackFormat;
            mediaExtractor = new MediaExtractor();
            try {
                mediaExtractor.setDataSource(str);
                int a = a(mediaExtractor);
                if (a < 0) {
                    Log.e("FyuseDecoder", "no video track found");
                }
                mediaExtractor.selectTrack(a);
                trackFormat = mediaExtractor.getTrackFormat(a);
                aVar2 = new com.fyusion.sdk.core.a.c.a(i, i2);
            } catch (IOException e2) {
                e = e2;
                mediaCodec = aVar4;
                try {
                    Log.e("FyuseDecoder", "decode failed: ", e);
                    aVar3 = aVar4;
                    mediaCodec2 = mediaCodec;
                    aVar2 = aVar3;
                    if (aVar2 != null) {
                        aVar2.a();
                    }
                    if (mediaCodec2 != null) {
                        mediaCodec2.stop();
                        mediaCodec2.release();
                    }
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                } catch (Throwable th) {
                    e = th;
                    if (aVar4 != null) {
                        aVar4.a();
                    }
                    if (mediaCodec != null) {
                        mediaCodec.stop();
                        mediaCodec.release();
                    }
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                    throw e;
                }
            } catch (Throwable th2) {
                e = th2;
                obj = aVar4;
                if (aVar4 != null) {
                    aVar4.a();
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                }
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
                throw e;
            }
            try {
                mediaCodec2 = MediaCodec.createDecoderByType(trackFormat.getString("mime"));
                try {
                    com.fyusion.sdk.common.util.b.a(mediaCodec2, trackFormat, aVar2.b(), 0);
                    mediaCodec2.start();
                    a(mediaExtractor, mediaCodec2, aVar2, aVar);
                } catch (IOException e3) {
                    e = e3;
                    aVar3 = aVar2;
                    mediaCodec = mediaCodec2;
                    aVar4 = aVar3;
                    Log.e("FyuseDecoder", "decode failed: ", e);
                    aVar3 = aVar4;
                    mediaCodec2 = mediaCodec;
                    aVar2 = aVar3;
                    if (aVar2 != null) {
                        aVar2.a();
                    }
                    if (mediaCodec2 != null) {
                        mediaCodec2.stop();
                        mediaCodec2.release();
                    }
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                } catch (Throwable th3) {
                    e = th3;
                    aVar3 = aVar2;
                    mediaCodec = mediaCodec2;
                    aVar4 = aVar3;
                    if (aVar4 != null) {
                        aVar4.a();
                    }
                    if (mediaCodec != null) {
                        mediaCodec.stop();
                        mediaCodec.release();
                    }
                    if (mediaExtractor != null) {
                        mediaExtractor.release();
                    }
                    throw e;
                }
            } catch (IOException e4) {
                e = e4;
                aVar3 = aVar2;
                obj = aVar4;
                aVar4 = aVar3;
                Log.e("FyuseDecoder", "decode failed: ", e);
                aVar3 = aVar4;
                mediaCodec2 = mediaCodec;
                aVar2 = aVar3;
                if (aVar2 != null) {
                    aVar2.a();
                }
                if (mediaCodec2 != null) {
                    mediaCodec2.stop();
                    mediaCodec2.release();
                }
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
            } catch (Throwable th4) {
                e = th4;
                aVar3 = aVar2;
                obj = aVar4;
                aVar4 = aVar3;
                if (aVar4 != null) {
                    aVar4.a();
                }
                if (mediaCodec != null) {
                    mediaCodec.stop();
                    mediaCodec.release();
                }
                if (mediaExtractor != null) {
                    mediaExtractor.release();
                }
                throw e;
            }
        } catch (IOException e5) {
            e = e5;
            Object obj2 = aVar4;
            obj = aVar4;
            Log.e("FyuseDecoder", "decode failed: ", e);
            aVar3 = aVar4;
            mediaCodec2 = mediaCodec;
            aVar2 = aVar3;
            if (aVar2 != null) {
                aVar2.a();
            }
            if (mediaCodec2 != null) {
                mediaCodec2.stop();
                mediaCodec2.release();
            }
            if (mediaExtractor != null) {
                mediaExtractor.release();
            }
        } catch (Throwable th5) {
            e = th5;
            mediaExtractor = aVar4;
            mediaCodec = aVar4;
            if (aVar4 != null) {
                aVar4.a();
            }
            if (mediaCodec != null) {
                mediaCodec.stop();
                mediaCodec.release();
            }
            if (mediaExtractor != null) {
                mediaExtractor.release();
            }
            throw e;
        }
        if (aVar2 != null) {
            aVar2.a();
        }
        if (mediaCodec2 != null) {
            mediaCodec2.stop();
            mediaCodec2.release();
        }
        if (mediaExtractor != null) {
            mediaExtractor.release();
        }
    }

    public void b() {
        this.a = false;
    }
}
