package com.android.gallery3d.ui;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;

public abstract class AbstractGifScreenNail implements ScreenNail {
    private BitmapTexture mGifTexture;
    private Entry mHead = null;

    private static class Entry {
        Bitmap mBitmap;
        Entry mNext;
        BitmapTexture mTexture;
        int state = 1;

        Entry(int width, int height) {
            ensureBitmap(width, height);
        }

        void ensureBitmap(int width, int height) {
            if (this.mBitmap == null || this.mBitmap.isRecycled()) {
                this.mBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                this.mTexture = new BitmapTexture(this.mBitmap);
            }
        }

        void recycle() {
            if (this.mTexture != null) {
                this.mTexture.recycle();
                this.mTexture = null;
            }
            if (this.mBitmap != null && !this.mBitmap.isRecycled()) {
                this.mBitmap.recycle();
                this.mBitmap = null;
            }
        }
    }

    public boolean drawGifIfNecessary(GLCanvas canvas, int x, int y, int width, int height) {
        acquire();
        BitmapTexture texture = this.mGifTexture;
        if (texture == null) {
            return false;
        }
        texture.draw(canvas, x, y, width, height);
        return true;
    }

    public Bitmap getGifBitmap() {
        if (this.mHead == null) {
            return null;
        }
        acquire();
        return this.mHead.mBitmap;
    }

    public Bitmap dequeue(int width, int height) {
        if (this.mHead == null) {
            this.mHead = new Entry(width, height);
            Entry tem = new Entry(width, height);
            tem.mNext = this.mHead;
            this.mHead.mNext = tem;
        }
        Entry entry = this.mHead;
        for (int i = 0; i < 2; i++) {
            if (entry.state == 1) {
                entry.state = 2;
                entry.ensureBitmap(width, height);
                return entry.mBitmap;
            }
            entry = entry.mNext;
        }
        return null;
    }

    public boolean enqueue(Bitmap bitmap) {
        Entry entry = this.mHead;
        if (entry == null) {
            return false;
        }
        do {
            if (entry.mBitmap == bitmap && entry.state == 2) {
                entry.state = 4;
                break;
            }
            entry = entry.mNext;
        } while (entry != this.mHead);
        return true;
    }

    private void acquire() {
        Entry entry = this.mHead;
        if (entry != null) {
            while (entry.state != 4) {
                entry = entry.mNext;
                if (entry != null) {
                    if (entry == this.mHead) {
                        break;
                    }
                }
                break;
            }
            entry.state = 8;
            BitmapTexture texture = this.mGifTexture;
            Entry head = this.mHead;
            this.mGifTexture = entry.mTexture;
            this.mHead = entry;
            if (texture != null) {
                texture.recycle();
            }
            if (this.mHead != head) {
                head.state = 1;
            }
        }
    }

    public void recycle() {
        Entry entry = this.mHead;
        this.mGifTexture = null;
        if (entry != null) {
            do {
                entry.recycle();
                entry = entry.mNext;
                if (entry == null) {
                    break;
                }
            } while (entry != this.mHead);
            this.mHead = null;
        }
    }
}
