package com.fyusion.sdk.common.util;

import android.annotation.TargetApi;
import android.media.MediaCodec;
import android.media.MediaCodec.CodecException;
import android.media.MediaFormat;
import android.os.Build.VERSION;
import android.view.Surface;
import com.fyusion.sdk.common.h;

/* compiled from: Unknown */
public class b {
    public static void a(MediaCodec mediaCodec, MediaFormat mediaFormat, Surface surface, int i) {
        if (VERSION.SDK_INT < 21) {
            b(mediaCodec, mediaFormat, surface, i);
        } else {
            c(mediaCodec, mediaFormat, surface, i);
        }
    }

    private static void b(MediaCodec mediaCodec, MediaFormat mediaFormat, Surface surface, int i) {
        mediaCodec.configure(mediaFormat, surface, null, i);
    }

    @TargetApi(21)
    private static void c(MediaCodec mediaCodec, MediaFormat mediaFormat, Surface surface, int i) {
        int i2 = 0;
        while (true) {
            int i3 = i2 + 1;
            if (i2 <= 10) {
                try {
                    mediaCodec.configure(mediaFormat, surface, null, i);
                    break;
                } catch (Exception e) {
                    try {
                        if (e instanceof CodecException) {
                            h.d("MediaCodecUtil", "Codec error: " + e.getMessage() + " Attempt: " + i3);
                            Thread.sleep(200);
                        }
                    } catch (InterruptedException e2) {
                    }
                    i2 = i3;
                }
            } else {
                return;
            }
        }
    }
}
