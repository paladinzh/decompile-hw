package com.huawei.gallery.editor.pipeline;

public class SharedPreset {
    private volatile ImagePreset mConsumerPreset = null;
    private volatile boolean mHasNewContent = false;
    private volatile ImagePreset mProducerPreset = null;

    public synchronized void enqueuePreset(ImagePreset preset) {
        this.mProducerPreset = new ImagePreset(preset);
        this.mHasNewContent = true;
    }

    public synchronized ImagePreset dequeuePreset() {
        if (this.mHasNewContent) {
            this.mConsumerPreset = this.mProducerPreset;
            this.mHasNewContent = false;
            return this.mConsumerPreset;
        }
        return this.mConsumerPreset;
    }

    public synchronized void clear() {
        this.mProducerPreset = null;
        this.mConsumerPreset = null;
    }
}
