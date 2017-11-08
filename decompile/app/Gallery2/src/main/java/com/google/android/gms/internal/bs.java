package com.google.android.gms.internal;

import android.content.Context;
import android.media.MediaPlayer;
import android.media.MediaPlayer.OnCompletionListener;
import android.media.MediaPlayer.OnErrorListener;
import android.media.MediaPlayer.OnPreparedListener;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.widget.FrameLayout;
import android.widget.FrameLayout.LayoutParams;
import android.widget.MediaController;
import android.widget.VideoView;
import java.lang.ref.WeakReference;
import java.util.HashMap;
import java.util.Map;

/* compiled from: Unknown */
public final class bs extends FrameLayout implements OnCompletionListener, OnErrorListener, OnPreparedListener {
    private final MediaController nB;
    private final a nC = new a(this);
    private final VideoView nD;
    private long nE;
    private String nF;
    private final dd ng;

    /* compiled from: Unknown */
    private static final class a {
        private final Runnable kW;
        private volatile boolean nG = false;

        public a(final bs bsVar) {
            this.kW = new Runnable(this) {
                private final WeakReference<bs> nH = new WeakReference(bsVar);
                final /* synthetic */ a nJ;

                public void run() {
                    bs bsVar = (bs) this.nH.get();
                    if (!this.nJ.nG && bsVar != null) {
                        bsVar.az();
                        this.nJ.aA();
                    }
                }
            };
        }

        public void aA() {
            cz.pT.postDelayed(this.kW, 250);
        }

        public void cancel() {
            this.nG = true;
            cz.pT.removeCallbacks(this.kW);
        }
    }

    public bs(Context context, dd ddVar) {
        super(context);
        this.ng = ddVar;
        this.nD = new VideoView(context);
        addView(this.nD, new LayoutParams(-1, -1, 17));
        this.nB = new MediaController(context);
        this.nC.aA();
        this.nD.setOnCompletionListener(this);
        this.nD.setOnPreparedListener(this);
        this.nD.setOnErrorListener(this);
    }

    private static void a(dd ddVar, String str) {
        a(ddVar, str, new HashMap(1));
    }

    public static void a(dd ddVar, String str, String str2) {
        Object obj = null;
        if (str2 == null) {
            obj = 1;
        }
        Map hashMap = new HashMap(obj == null ? 3 : 2);
        hashMap.put("what", str);
        if (obj == null) {
            hashMap.put("extra", str2);
        }
        a(ddVar, "error", hashMap);
    }

    private static void a(dd ddVar, String str, String str2, String str3) {
        Map hashMap = new HashMap(2);
        hashMap.put(str2, str3);
        a(ddVar, str, hashMap);
    }

    private static void a(dd ddVar, String str, Map<String, String> map) {
        map.put("event", str);
        ddVar.a("onVideoEvent", map);
    }

    public void ay() {
        if (TextUtils.isEmpty(this.nF)) {
            a(this.ng, "no_src", null);
        } else {
            this.nD.setVideoPath(this.nF);
        }
    }

    public void az() {
        long currentPosition = (long) this.nD.getCurrentPosition();
        if (this.nE != currentPosition) {
            a(this.ng, "timeupdate", "time", String.valueOf(((float) currentPosition) / 1000.0f));
            this.nE = currentPosition;
        }
    }

    public void b(MotionEvent motionEvent) {
        this.nD.dispatchTouchEvent(motionEvent);
    }

    public void destroy() {
        this.nC.cancel();
        this.nD.stopPlayback();
    }

    public void i(boolean z) {
        if (z) {
            this.nD.setMediaController(this.nB);
            return;
        }
        this.nB.hide();
        this.nD.setMediaController(null);
    }

    public void o(String str) {
        this.nF = str;
    }

    public void onCompletion(MediaPlayer mediaPlayer) {
        a(this.ng, "ended");
    }

    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {
        a(this.ng, String.valueOf(what), String.valueOf(extra));
        return true;
    }

    public void onPrepared(MediaPlayer mediaPlayer) {
        a(this.ng, "canplaythrough", "duration", String.valueOf(((float) this.nD.getDuration()) / 1000.0f));
    }

    public void pause() {
        this.nD.pause();
    }

    public void play() {
        this.nD.start();
    }

    public void seekTo(int timeInMilliseconds) {
        this.nD.seekTo(timeInMilliseconds);
    }
}
