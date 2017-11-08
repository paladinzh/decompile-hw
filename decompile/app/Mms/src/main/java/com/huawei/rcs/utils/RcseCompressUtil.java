package com.huawei.rcs.utils;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import com.huawei.rcs.utils.videocompress.RcsExtractAndMuxVideo;
import com.huawei.rcs.utils.videocompress.RcsExtractAndMuxVideo.VideoMuxListener;

public class RcseCompressUtil {
    private static volatile boolean isCancelled = false;
    private static volatile boolean isRunning = false;

    private static class MuxListener implements VideoMuxListener {
        private Handler mHandler;

        public MuxListener(Handler mHandler) {
            this.mHandler = mHandler;
        }

        public void onCompressStarted(String targetHint) {
            RcseCompressUtil.isRunning = true;
            Message m = this.mHandler.obtainMessage(0);
            m.obj = targetHint;
            this.mHandler.sendMessage(m);
        }

        public void onCompressProcessing(int progress) {
            Message m = this.mHandler.obtainMessage(1);
            m.arg1 = progress;
            this.mHandler.sendMessage(m);
        }

        public void onCompressCompleted(String target) {
            RcseCompressUtil.isRunning = false;
            Message m = this.mHandler.obtainMessage(2);
            m.obj = target;
            this.mHandler.sendMessage(m);
        }

        public boolean isCanceled() {
            return RcseCompressUtil.isCancelled;
        }
    }

    public static void call4ffmpeg(Handler handler, String filePath, Context context) {
        try {
            isCancelled = false;
            RcsExtractAndMuxVideo.extractDecodeEditEncodeMuxAudioVideo(filePath, new MuxListener(handler), context);
        } catch (RuntimeException e) {
            Log.e("RcseCompressUtil", "Video Compress Error!", e);
        }
    }

    public static boolean isCompressRunning() {
        return isRunning;
    }

    private static void setCancelled() {
        isCancelled = true;
    }

    public static boolean isCancelled() {
        return isCancelled;
    }

    public static void cancelVideoCompress() {
        setCancelled();
    }
}
