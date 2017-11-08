package com.android.mms.attachment.datamodel.media;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.os.SystemClock;
import android.util.SparseArray;
import com.android.mms.attachment.Factory;
import com.huawei.cspcommon.MLog;
import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;

public class PoolableImageCache extends MediaCache<ImageResource> {
    private final ReusableImageResourcePool mReusablePoolAccessor;

    public class ReusableImageResourcePool {
        private final SparseArray<LinkedList<ImageResource>> mImageListSparseArray = new SparseArray();

        public Bitmap decodeSampledBitmapFromInputStream(InputStream inputStream, Options optionsTmp, int width, int height) throws IOException {
            if (width <= 0 || height <= 0) {
                MLog.w("PoolableImageCache", "PoolableImageCache: Decoding bitmap with invalid size");
                throw new IOException("Invalid size / corrupted image");
            }
            assignPoolBitmap(optionsTmp, width, height);
            Bitmap b = null;
            try {
                return BitmapFactory.decodeStream(inputStream, null, optionsTmp);
            } catch (IllegalArgumentException e) {
                if (optionsTmp.inBitmap == null) {
                    return b;
                }
                optionsTmp.inBitmap.recycle();
                optionsTmp.inBitmap = null;
                return BitmapFactory.decodeStream(inputStream, null, optionsTmp);
            } catch (OutOfMemoryError e2) {
                MLog.w("PoolableImageCache", "Oom decoding inputStream");
                Factory.get().reclaimMemory();
                return b;
            }
        }

        public Bitmap decodeByteArray(byte[] bytes, Options optionsTmp, int width, int height) throws OutOfMemoryError, IOException {
            if (width <= 0 || height <= 0) {
                MLog.w("PoolableImageCache", "PoolableImageCache: Decoding bitmap with invalid size");
                throw new IOException("Invalid size / corrupted image");
            }
            assignPoolBitmap(optionsTmp, width, height);
            Bitmap b = null;
            try {
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, optionsTmp);
            } catch (IllegalArgumentException e) {
                if (optionsTmp.inBitmap == null) {
                    return b;
                }
                optionsTmp.inBitmap.recycle();
                optionsTmp.inBitmap = null;
                return BitmapFactory.decodeByteArray(bytes, 0, bytes.length, optionsTmp);
            } catch (OutOfMemoryError e2) {
                MLog.w("PoolableImageCache", "Oom decoding inputStream");
                Factory.get().reclaimMemory();
                return b;
            }
        }

        void onResourceEnterCache(ImageResource imageResource) {
            if (getPoolKey(imageResource) != 0) {
                addResourceToPool(imageResource);
            }
        }

        void onResourceLeaveCache(ImageResource imageResource) {
            if (getPoolKey(imageResource) != 0) {
                removeResourceFromPool(imageResource);
            }
        }

        private void addResourceToPool(ImageResource imageResource) {
            synchronized (PoolableImageCache.this) {
                int poolKey = getPoolKey(imageResource);
                LinkedList<ImageResource> imageList = (LinkedList) this.mImageListSparseArray.get(poolKey);
                if (imageList == null) {
                    imageList = new LinkedList();
                    this.mImageListSparseArray.put(poolKey, imageList);
                }
                imageList.addLast(imageResource);
            }
        }

        private void removeResourceFromPool(ImageResource imageResource) {
            synchronized (PoolableImageCache.this) {
                LinkedList<ImageResource> imageList = (LinkedList) this.mImageListSparseArray.get(getPoolKey(imageResource));
                if (imageList != null) {
                    imageList.remove(imageResource);
                }
            }
        }

        /* JADX WARNING: inconsistent code. */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        private Bitmap getReusableBitmapFromPool(int width, int height) {
            synchronized (PoolableImageCache.this) {
                int poolKey = getPoolKey(width, height);
                if (poolKey != 0) {
                    LinkedList<ImageResource> images = (LinkedList) this.mImageListSparseArray.get(poolKey);
                    if (images != null && images.size() > 0) {
                        ImageResource imageResource = null;
                        for (int i = 0; i < images.size(); i++) {
                            ImageResource image = (ImageResource) images.get(i);
                            if (image.getRefCount() == 1) {
                                image.acquireLock();
                                if (image.getRefCount() == 1) {
                                    imageResource = (ImageResource) images.remove(i);
                                    break;
                                }
                                MLog.w("PoolableImageCache", "Image refCount changed from 1 in getReusableBitmapFromPool()");
                                image.releaseLock();
                            }
                        }
                        if (imageResource == null) {
                            return null;
                        }
                        try {
                            imageResource.assertLockHeldByCurrentThread();
                            long timeSinceLastRef = SystemClock.elapsedRealtime() - imageResource.getLastRefAddTimestamp();
                            if (timeSinceLastRef < 5000) {
                                if (MLog.isLoggable("Mms_app", 2)) {
                                    MLog.v("PoolableImageCache", "Not reusing reusing first available bitmap from the pool because it has not been in the pool long enough. timeSinceLastRef=" + timeSinceLastRef);
                                }
                                images.addLast(imageResource);
                                return null;
                            }
                            imageResource.addRef();
                            PoolableImageCache.this.remove(imageResource.getKey());
                            Bitmap reusableBitmap = imageResource.reuseBitmap();
                            imageResource.release();
                            imageResource.releaseLock();
                            return reusableBitmap;
                        } finally {
                            imageResource.releaseLock();
                        }
                    }
                }
            }
        }

        public Bitmap createOrReuseBitmap(int width, int height) {
            return createOrReuseBitmap(width, height, 0);
        }

        public Bitmap createOrReuseBitmap(int width, int height, int backgroundColor) {
            Bitmap retBitmap = null;
            try {
                Bitmap poolBitmap = getReusableBitmapFromPool(width, height);
                if (poolBitmap != null) {
                    retBitmap = poolBitmap;
                } else {
                    retBitmap = Bitmap.createBitmap(width, height, Config.ARGB_8888);
                }
                retBitmap.eraseColor(backgroundColor);
            } catch (OutOfMemoryError e) {
                MLog.w("PoolableImageCache", "PoolableImageCache:try to createOrReuseBitmap");
                Factory.get().reclaimMemory();
            }
            return retBitmap;
        }

        private void assignPoolBitmap(Options optionsTmp, int width, int height) {
            if (!optionsTmp.inJustDecodeBounds) {
                optionsTmp.inBitmap = getReusableBitmapFromPool(width, height);
            }
        }

        private int getPoolKey(int width, int height) {
            if (width > 65535 || height > 65535) {
                return 0;
            }
            return (width << 16) | height;
        }

        private int getPoolKey(ImageResource imageResource) {
            if (imageResource.supportsBitmapReuse()) {
                Bitmap bitmap = imageResource.getBitmap();
                if (bitmap != null && bitmap.isMutable()) {
                    int width = bitmap.getWidth();
                    int height = bitmap.getHeight();
                    if (width > 0 && height > 0) {
                        return getPoolKey(width, height);
                    }
                }
            }
            return 0;
        }
    }

    public PoolableImageCache(int id, String name) {
        this(5120, id, name);
    }

    public PoolableImageCache(int maxSize, int id, String name) {
        super(maxSize, id, name);
        this.mReusablePoolAccessor = new ReusableImageResourcePool();
    }

    public static Options getBitmapOptionsForPool(boolean scaled, int inputDensity, int targetDensity) {
        Options options = new Options();
        options.inScaled = scaled;
        options.inDensity = inputDensity;
        options.inTargetDensity = targetDensity;
        options.inSampleSize = 1;
        options.inJustDecodeBounds = false;
        options.inMutable = true;
        return options;
    }

    public synchronized ImageResource addResourceToCache(String key, ImageResource imageResource) {
        this.mReusablePoolAccessor.onResourceEnterCache(imageResource);
        return (ImageResource) super.addResourceToCache(key, imageResource);
    }

    protected synchronized void entryRemoved(boolean evicted, String key, ImageResource oldValue, ImageResource newValue) {
        this.mReusablePoolAccessor.onResourceLeaveCache(oldValue);
        super.entryRemoved(evicted, key, (RefCountedMediaResource) oldValue, (RefCountedMediaResource) newValue);
    }

    public ReusableImageResourcePool asReusableBitmapPool() {
        return this.mReusablePoolAccessor;
    }
}
