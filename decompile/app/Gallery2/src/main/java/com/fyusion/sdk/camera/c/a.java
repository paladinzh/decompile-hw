package com.fyusion.sdk.camera.c;

import android.media.MediaCodec;
import android.media.MediaCodec.BufferInfo;
import android.media.MediaCodecInfo;
import android.media.MediaCodecInfo.CodecCapabilities;
import android.media.MediaCodecList;
import android.media.MediaFormat;
import android.media.MediaMuxer;
import com.fyusion.sdk.camera.util.b;
import com.fyusion.sdk.common.DLog;
import com.fyusion.sdk.common.ext.j;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.util.concurrent.Semaphore;

/* compiled from: Unknown */
public class a {
    private static boolean n = false;
    final int a = 10000;
    boolean b = false;
    boolean c = false;
    private MediaCodec d = null;
    private boolean e = false;
    private ByteBuffer[] f;
    private ByteBuffer[] g;
    private BufferInfo h;
    private int i;
    private MediaMuxer j;
    private int k;
    private boolean l = false;
    private String m = null;
    private int o = 0;
    private boolean p = true;
    private boolean q = false;
    private boolean r = false;
    private int s = -1;
    private int t = -1;
    private int u = -1;
    private int v = j.j;
    private byte[] w = null;
    private FileOutputStream x = null;
    private final Semaphore y = new Semaphore(0);

    public a(int i, int i2, int i3, String str, boolean z, int i4) {
        this.r = z;
        if (i4 == -1) {
            i4 = 15;
        }
        this.v = i4;
        a(i, i2, i3, this.v);
        this.w = new byte[(((this.s * this.t) * 3) / 2)];
        b(str);
    }

    private static int a(MediaCodecInfo mediaCodecInfo, String str) {
        CodecCapabilities codecCapabilities = new CodecCapabilities();
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
            if (n) {
                DLog.e("AvcEncoder", "selectColorFormat: Defaulting to COLOR_FormatYUV420SemiPlanar");
            }
            return 21;
        }
    }

    public static MediaCodecInfo a(String str) {
        int codecCount = MediaCodecList.getCodecCount();
        for (int i = 0; i < codecCount; i++) {
            MediaCodecInfo codecInfoAt = MediaCodecList.getCodecInfoAt(i);
            if (codecInfoAt.isEncoder()) {
                String[] supportedTypes = codecInfoAt.getSupportedTypes();
                for (String equalsIgnoreCase : supportedTypes) {
                    if (equalsIgnoreCase.equalsIgnoreCase(str)) {
                        return codecInfoAt;
                    }
                }
                continue;
            }
        }
        return null;
    }

    private void a(int i, int i2, int i3, int i4) {
        if ((n && i % 16 != 0) || i2 % 16 != 0) {
            DLog.w("AvcEncoder", "WARNING: width or height not multiple of 16");
        }
        this.s = i;
        this.t = i2;
        this.u = i3;
        this.v = i4;
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
        return (((long) i) * 1000000) / ((long) this.v);
    }

    private void b(String str) {
        try {
            c();
            this.i = 0;
            if (this.d != null) {
                this.f = this.d.getInputBuffers();
                this.g = this.d.getOutputBuffers();
                this.h = new BufferInfo();
                c(str);
                return;
            }
            DLog.e("AvcEncoder", "Encoder is NULL.");
        } catch (Exception e) {
            e.printStackTrace();
            DLog.e("AvcEncoder", "Failed to create encoder.");
        }
    }

    private void c() throws Exception {
        try {
            MediaCodecInfo a = a("video/avc");
            if (a != null) {
                if (n) {
                    DLog.d("AvcEncoder", "found codec: " + a.getName());
                }
                int a2 = a(a, "video/avc");
                if (n) {
                    DLog.d("AvcEncoder", "found colorFormat: " + a2);
                }
                MediaFormat createVideoFormat = MediaFormat.createVideoFormat("video/avc", this.s, this.t);
                createVideoFormat.setInteger("color-format", a2);
                createVideoFormat.setInteger("bitrate", this.u);
                createVideoFormat.setInteger("frame-rate", this.v);
                createVideoFormat.setInteger("i-frame-interval", 2);
                DLog.d("AvcEncoder", "specified frame rate of " + this.v);
                if (n) {
                    DLog.d("AvcEncoder", "format: " + createVideoFormat);
                }
                this.d = MediaCodec.createByCodecName(a.getName());
                this.d.configure(createVideoFormat, null, null, 1);
                if (n) {
                    DLog.d("AvcEncoder", "video videoBitRate: " + this.u);
                }
                if (n) {
                    DLog.d("AvcEncoder", "video videoFrameRate: " + this.v);
                }
                if (n) {
                    DLog.d("AvcEncoder", "video videoFrameWidth: " + this.s);
                }
                if (n) {
                    DLog.d("AvcEncoder", "video videoFrameHeight: " + this.t);
                }
                this.d.start();
                this.e = true;
                return;
            }
            DLog.e("AvcEncoder", "Unable to find an appropriate codec for video/avc");
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        } catch (Exception e2) {
            e2.printStackTrace();
        }
    }

    private void c(String str) {
        try {
            this.m = str;
            this.j = null;
            this.j = new MediaMuxer(str, 0);
            if (n) {
                DLog.d("AvcEncoder", "encoded output will be saved as " + str);
            }
            this.k = -1;
            this.l = false;
        } catch (Throwable e) {
            throw new RuntimeException("MediaMuxer creation failed", e);
        }
    }

    private synchronized void d() {
        this.o = 0;
        this.p = true;
        if (n) {
            DLog.d("AvcEncoder", "mEncoderStarted " + this.e);
        }
        if (this.d != null) {
            if (this.e) {
                try {
                    if (n) {
                        DLog.d("AvcEncoder", "Stopping encoder!");
                    }
                    synchronized (this) {
                        if (!this.c) {
                            if (!this.b) {
                                DLog.i("AvcEncoder", "waiting for encoder to finish!");
                                this.y.acquire();
                            }
                        }
                        this.d.stop();
                        this.d.release();
                        this.e = false;
                    }
                } catch (Throwable e) {
                    DLog.w("AvcEncoder", "failed closing debug file");
                    throw new RuntimeException(e);
                } catch (IllegalStateException e2) {
                    e2.printStackTrace();
                } catch (InterruptedException e3) {
                    DLog.e("AvcEncoder", e3.getMessage());
                }
            }
        }
        if (this.j != null) {
            try {
                if (this.l) {
                    if (this.i > 0) {
                        this.j.stop();
                        this.j.release();
                    }
                }
                this.l = false;
                this.j = null;
            } catch (IllegalStateException e22) {
                e22.printStackTrace();
            }
        }
        if (this.x != null) {
            this.x.close();
            if (n) {
                DLog.d("AvcEncoder", "Closing output file.");
            }
        }
        if (this.w != null) {
            this.w = null;
        }
    }

    public synchronized void a() {
        if (n) {
            DLog.d("AvcEncoder", "stopping encoder");
        }
        d();
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void a(byte[] bArr, boolean z) {
        if (n) {
            DLog.i("AvcEncoder", "encodeFromBuffer called");
        }
        if (this.e) {
            if (n) {
                DLog.i("AvcEncoder", "passed " + this.h.size + " encoded" + (!z ? "" : " (EOS)"));
            }
            if (this.o % (this.v * 2) != 0) {
                this.p = false;
            } else {
                this.p = true;
            }
            if (n) {
                DLog.i("AvcEncoder", "inputDone = " + false);
            }
            Object obj = null;
            while (obj == null) {
                if (n) {
                    DLog.d("AvcEncoder", "loop");
                }
                if (obj == null) {
                    int dequeueInputBuffer = this.d.dequeueInputBuffer(10000);
                    if (n) {
                        DLog.d("AvcEncoder", "inputBufIndex=" + dequeueInputBuffer);
                    }
                    if (dequeueInputBuffer >= 0) {
                        long b = b(this.o);
                        DLog.d("AvcEncoder", "Frame " + this.o + " with pt=" + b);
                        if (z) {
                            this.d.queueInputBuffer(dequeueInputBuffer, 0, 0, b, 4);
                            if (n) {
                                DLog.d("AvcEncoder", "sent input EOS (with zero-length frame)");
                            }
                        } else {
                            ByteBuffer byteBuffer = this.f[dequeueInputBuffer];
                            byteBuffer.clear();
                            if (n) {
                                if (byteBuffer == null) {
                                    DLog.e("MPEGEncoder", "Error: InputBuf is NULL!");
                                }
                                if (bArr == null) {
                                    DLog.e("MPEGEncoder", "Error: frameData is NULL!");
                                }
                            }
                            byteBuffer.put(bArr);
                            this.d.queueInputBuffer(dequeueInputBuffer, 0, bArr.length, b, !this.p ? 0 : 1);
                            if (n) {
                                DLog.d("AvcEncoder", "submitted frame " + this.o + " to enc with bytes: " + bArr.length);
                            }
                        }
                        this.o++;
                        obj = 1;
                    } else if (n) {
                        DLog.d("AvcEncoder", "input buffer not available");
                    }
                }
            }
            long j = 0;
            while (true) {
                int dequeueOutputBuffer = this.d.dequeueOutputBuffer(this.h, 10000);
                if (dequeueOutputBuffer != -1) {
                    if (dequeueOutputBuffer == -3) {
                        this.g = this.d.getOutputBuffers();
                        if (n) {
                            DLog.d("AvcEncoder", "encoder output buffers changed");
                        }
                    } else if (dequeueOutputBuffer != -2) {
                        if (dequeueOutputBuffer >= 0) {
                            ByteBuffer byteBuffer2 = this.g[dequeueOutputBuffer];
                            if (byteBuffer2 == null) {
                                DLog.e("AvcEncoder", "encoderOutputBuffer " + dequeueOutputBuffer + " was null");
                            }
                            byteBuffer2.position(this.h.offset);
                            byteBuffer2.limit(this.h.offset + this.h.size);
                            j += (long) this.h.size;
                            if (this.x != null) {
                                byte[] a = b.a().a(this.h.size, b.a);
                                byteBuffer2.get(a);
                                byteBuffer2.position(this.h.offset);
                                try {
                                    this.x.write(a);
                                } catch (Throwable e) {
                                    DLog.w("AvcEncoder", "failed writing debug data to file");
                                    throw new RuntimeException(e);
                                }
                            }
                            if ((this.h.flags & 2) != 0) {
                                if (n) {
                                    DLog.d("AvcEncoder", "ignoring BUFFER_FLAG_CODEC_CONFIG");
                                }
                                this.h.size = 0;
                            }
                            if (this.h.size != 0) {
                                if (!this.l) {
                                    break;
                                }
                                byteBuffer2.position(this.h.offset);
                                byteBuffer2.limit(this.h.offset + this.h.size);
                                this.j.writeSampleData(this.k, byteBuffer2, this.h);
                                if (n) {
                                    DLog.d("AvcEncoder", "sent " + this.h.size + " bytes to muxer");
                                }
                                this.i++;
                            }
                            this.d.releaseOutputBuffer(dequeueOutputBuffer, false);
                            if ((this.h.flags & 4) != 0) {
                                break;
                            }
                        } else {
                            DLog.e("AvcEncoder", "unexpected result from encoder.dequeueOutputBuffer: " + dequeueOutputBuffer);
                        }
                    } else if (this.l) {
                        throw new RuntimeException("format changed twice");
                    } else {
                        MediaFormat outputFormat = this.d.getOutputFormat();
                        if (n) {
                            DLog.d("AvcEncoder", "encoder output format changed: " + outputFormat);
                        }
                        this.k = this.j.addTrack(outputFormat);
                        DLog.d("AvcEncoder", "starting mmuxer " + this.j);
                        this.j.start();
                        this.l = true;
                    }
                } else if (z) {
                    if (n) {
                        DLog.d("AvcEncoder", "no output available, spinning to await EOS");
                    }
                }
                break;
            }
            if (n) {
                if (z) {
                    this.o = 0;
                    this.p = true;
                    d();
                    if (n) {
                        DLog.i("mpegEncoder", "Total number of frames written: " + this.i);
                    }
                }
            } else if (z) {
                this.o = 0;
                this.p = true;
                d();
                if (n) {
                    DLog.i("mpegEncoder", "Total number of frames written: " + this.i);
                }
            }
        } else if (n) {
            DLog.w("AvcEncoder", "encodeFromBuffer called, but Encoder has not started!");
        }
    }

    public synchronized void b() {
        this.b = true;
        d();
    }
}
