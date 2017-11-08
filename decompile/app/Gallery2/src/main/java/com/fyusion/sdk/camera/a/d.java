package com.fyusion.sdk.camera.a;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import android.util.Log;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.j;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/* compiled from: Unknown */
public class d {
    private static final int a = j.j;
    private static final int[] b = new int[]{8000000, 5000000};
    private static boolean l = false;
    private MediaCodec c = null;
    private boolean d = false;
    private ByteBuffer[] e;
    private ByteBuffer[] f;
    private BufferInfo g;
    private int h;
    private MediaMuxer i;
    private int j;
    private boolean k = false;
    private int m = 0;
    private boolean n = true;
    private int o = -1;
    private int p = -1;
    private int q = -1;
    private boolean r = false;
    private final Semaphore s = new Semaphore(0);

    public d(int i, int i2, String str) {
        int i3 = 1;
        if ((l && i % 16 != 0) || i2 % 16 != 0) {
            DLog.w("MPEGEncoder", "WARNING: width or height not multiple of 16");
        }
        this.o = i;
        this.p = i2;
        int[] iArr = b;
        if (i2 >= 1080) {
            i3 = 0;
        }
        this.q = iArr[i3];
        b(str);
    }

    private static int a(MediaCodecInfo mediaCodecInfo, String str) {
        try {
            CodecCapabilities capabilitiesForType = mediaCodecInfo.getCapabilitiesForType(str);
            for (int i : capabilitiesForType.colorFormats) {
                if (i == 21) {
                    return 21;
                }
            }
            for (int i2 : capabilitiesForType.colorFormats) {
                if (a(i2)) {
                    return i2;
                }
            }
            return 0;
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            Log.w("MPEGEncoder", "selectColorFormat: Defaulting to COLOR_FormatYUV420SemiPlanar");
            return 21;
        }
    }

    public static MediaCodecInfo a(String str) {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                for (String equalsIgnoreCase : codecInfoAt.getSupportedTypes()) {
                    if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                        return codecInfoAt;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private static boolean a(int i) {
        switch (i) {
            case 19:
            case 20:
            case 21:
            case 39:
            case 2130706688:
                return true;
            default:
                return false;
        }
    }

    private long b(int i) {
        return (((long) i) * 1000000) / ((long) j.j);
    }

    private void b() throws Exception {
        try {
            MediaCodecInfo a = a("video/avc");
            if (a != null) {
                if (l) {
                    DLog.d("MPEGEncoder", "found codec: " + a.getName());
                }
                int a2 = a(a, "video/avc");
                if (l) {
                    DLog.d("MPEGEncoder", "found colorFormat: " + a2);
                }
                MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/avc", this.o, this.p);
                createVideoFormat.setInteger("color-format", a2);
                createVideoFormat.setInteger("bitrate", this.q);
                createVideoFormat.setInteger("frame-rate", a);
                createVideoFormat.setInteger("i-frame-interval", 5);
                createVideoFormat.setInteger("bitrate-mode", 2);
                if (l) {
                    DLog.d("MPEGEncoder", "format: " + createVideoFormat);
                }
                this.c = MediaCodec.createByCodecName(a.getName());
                this.c.configure(createVideoFormat, null, null, 1);
                this.c.start();
                this.d = true;
                return;
            }
            DLog.e("MPEGEncoder", "Unable to find an appropriate codec for video/avc");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void b(String str) {
        try {
            b();
            this.h = 0;
            if (this.c != null) {
                this.e = this.c.getInputBuffers();
                this.f = this.c.getOutputBuffers();
                this.g = new BufferInfo();
                c(str);
                return;
            }
            DLog.e("MPEGEncoder", "Encoder is NULL.");
        } catch (Exception e) {
            e.printStackTrace();
            DLog.e("MPEGEncoder", "Failed to create encoder.");
        }
    }

    private synchronized void c() {
        this.m = 0;
        this.n = true;
        if (l) {
            DLog.d("MPEGEncoder", "mEncoderStarted " + this.d);
        }
        if (this.c != null) {
            if (this.d) {
                try {
                    if (l) {
                        DLog.d("MPEGEncoder", "Stopping encoder!");
                    }
                    synchronized (this) {
                        if (!this.r) {
                            DLog.i("MPEGEncoder", "waiting for encoder to finish!");
                            this.s.acquire();
                        }
                        this.c.stop();
                        this.c.release();
                        this.d = false;
                    }
                } catch (Exception e) {
                    DLog.e("MPEGEncoder", e.getMessage());
                }
            }
        }
        if (this.i != null) {
            try {
                if (this.k) {
                    if (this.h > 0) {
                        this.i.stop();
                        this.i.release();
                    }
                }
                this.k = false;
                this.i = null;
            } catch (Throwable e2) {
                DLog.e("MPEGEncoder", e2.getMessage(), e2);
            }
        }
    }

    private void c(String str) {
        try {
            this.i = null;
            this.i = new MediaMuxer(str, 0);
            if (l) {
                DLog.d("MPEGEncoder", "encoded output will be saved as " + str);
            }
            this.j = -1;
            this.k = false;
        } catch (Throwable e) {
            throw new RuntimeException("MediaMuxer creation failed", e);
        }
    }

    public void a() {
        if (l) {
            DLog.d("MPEGEncoder", "stopping encoder");
        }
        c();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    synchronized void a(byte[] bArr, boolean z) {
        if (l) {
            DLog.i("MPEGEncoder", "encodeFromBuffer called");
        }
        if (this.d) {
            if (l) {
                DLog.i("MPEGEncoder", "passed " + this.g.size + " encoded" + (!z ? "" : " (EOS)"));
            }
            this.n = this.m % (a * 5) == 0;
            Object obj = null;
            while (obj == null) {
                if (l) {
                    DLog.d("MPEGEncoder", "loop");
                }
                int dequeueInputBuffer = this.c.dequeueInputBuffer(10000);
                if (l) {
                    DLog.d("MPEGEncoder", "inputBufIndex=" + dequeueInputBuffer);
                }
                if (dequeueInputBuffer >= 0) {
                    long b = b(this.m);
                    if (!z) {
                        ByteBuffer byteBuffer = this.e[dequeueInputBuffer];
                        byteBuffer.clear();
                        if (bArr == null) {
                            DLog.e("MPEGEncoder", "Error: frameData is NULL!");
                            break;
                        }
                        byteBuffer.put(bArr);
                        if (this.n) {
                            Log.e("mpegEncoder", "frameIndex: " + this.m + " isKeyframe: " + this.n);
                        }
                        this.c.queueInputBuffer(dequeueInputBuffer, 0, bArr.length, b, !this.n ? 0 : 1);
                        if (l) {
                            DLog.d("MPEGEncoder", "submitted frame " + this.m + " to enc with bytes: " + bArr.length);
                        }
                    } else {
                        this.c.queueInputBuffer(dequeueInputBuffer, 0, 0, b, 4);
                        if (l) {
                            DLog.d("MPEGEncoder", "sent input EOS (with zero-length frame)");
                        }
                    }
                    this.m++;
                    obj = 1;
                } else if (l) {
                    DLog.w("MPEGEncoder", "input buffer not available");
                }
            }
            while (true) {
                long a = com.fyusion.sdk.core.util.d.a();
                int dequeueOutputBuffer = this.c.dequeueOutputBuffer(this.g, 10000);
                if (l) {
                    Log.d("MPEGEncoder", "dequeueOutputBuffer took: " + com.fyusion.sdk.core.util.d.a(a));
                }
                if (dequeueOutputBuffer == -1) {
                    Log.d("MPEGEncoder", "no output available yet " + dequeueOutputBuffer);
                    if (!z) {
                        break;
                    }
                    DLog.d("MPEGEncoder", "no output available, spinning to await EOS");
                } else if (dequeueOutputBuffer == -3) {
                    this.f = this.c.getOutputBuffers();
                    if (l) {
                        DLog.d("MPEGEncoder", "encoder output buffers changed");
                    }
                } else if (dequeueOutputBuffer != -2) {
                    if (dequeueOutputBuffer >= 0) {
                        ByteBuffer byteBuffer2 = this.f[dequeueOutputBuffer];
                        if (byteBuffer2 != null) {
                            if (l) {
                                Log.d("MPEGEncoder", "encoding took: " + com.fyusion.sdk.core.util.d.a(a));
                            }
                            byteBuffer2.position(this.g.offset);
                            byteBuffer2.limit(this.g.offset + this.g.size);
                            if ((this.g.flags & 2) != 0) {
                                if (l) {
                                    DLog.d("MPEGEncoder", "ignoring BUFFER_FLAG_CODEC_CONFIG");
                                }
                                this.g.size = 0;
                            }
                            if (this.g.size != 0) {
                                if (!this.k) {
                                    break;
                                }
                                byteBuffer2.position(this.g.offset);
                                byteBuffer2.limit(this.g.offset + this.g.size);
                                this.i.writeSampleData(this.j, byteBuffer2, this.g);
                                if (l) {
                                    DLog.d("MPEGEncoder", "sent " + this.g.size + " bytes to muxer");
                                }
                                this.h++;
                            }
                            this.c.releaseOutputBuffer(dequeueOutputBuffer, false);
                            if ((this.g.flags & 4) != 0) {
                                break;
                            }
                        } else {
                            DLog.e("MPEGEncoder", "encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                            return;
                        }
                    }
                    DLog.e("MPEGEncoder", "unexpected result from encoder.dequeueOutputBuffer: " + dequeueOutputBuffer);
                } else if (this.k) {
                    throw new RuntimeException("format changed twice");
                } else {
                    MediaFormat outputFormat = this.c.getOutputFormat();
                    DLog.d("MPEGEncoder", "encoder output format changed: " + outputFormat);
                    this.j = this.i.addTrack(outputFormat);
                    this.i.start();
                    this.k = true;
                }
            }
            if (!z) {
                DLog.w("MPEGEncoder", "reached end of stream unexpectedly");
            } else if (l) {
                DLog.d("MPEGEncoder", "end of stream reached");
            }
            synchronized (this) {
                this.r = true;
                this.s.release();
            }
            if (z) {
                this.m = 0;
                this.n = true;
                c();
                if (l) {
                    DLog.i("MPEGEncoder", "Total number of frames written: " + this.h);
                }
            }
        } else if (l) {
            DLog.w("MPEGEncoder", "encodeFromBuffer called, but Encoder has not started!");
        }
    }
}
