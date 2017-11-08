package com.android.systemui.statusbar.phone;

import android.app.IWallpaperManager;
import android.app.IWallpaperManagerCallback.Stub;
import android.app.WallpaperManager;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Rect;
import android.graphics.Xfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.DrawableWrapper;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.ParcelFileDescriptor;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.UserHandle;
import android.util.Log;
import com.android.systemui.utils.UserSwitchUtils;
import com.huawei.keyguard.inf.HwKeyguardPolicy;
import libcore.io.IoUtils;

public class LockscreenWallpaper extends Stub implements Runnable {
    private final PhoneStatusBar mBar;
    private Bitmap mCache;
    private boolean mCached;
    private final Context mContext;
    private int mCurrentUserId = UserSwitchUtils.getCurrentUser();
    private final Handler mH;
    private AsyncTask<Void, Void, LoaderResult> mLoader;
    private UserHandle mSelectedUser;
    private final WallpaperManager mWallpaperManager;

    private static class LoaderResult {
        public final Bitmap bitmap;
        public final boolean success;

        LoaderResult(boolean success, Bitmap bitmap) {
            this.success = success;
            this.bitmap = bitmap;
        }

        static LoaderResult success(Bitmap b) {
            return new LoaderResult(true, b);
        }

        static LoaderResult fail() {
            return new LoaderResult(false, null);
        }
    }

    public static class WallpaperDrawable extends DrawableWrapper {
        private final ConstantState mState;
        private final Rect mTmpRect;

        static class ConstantState extends android.graphics.drawable.Drawable.ConstantState {
            private final Bitmap mBackground;

            ConstantState(Bitmap background) {
                this.mBackground = background;
            }

            public Drawable newDrawable() {
                return newDrawable(null);
            }

            public Drawable newDrawable(Resources res) {
                return new WallpaperDrawable(res, this);
            }

            public int getChangingConfigurations() {
                return 0;
            }
        }

        public WallpaperDrawable(Resources r, Bitmap b) {
            this(r, new ConstantState(b));
        }

        private WallpaperDrawable(Resources r, ConstantState state) {
            super(new BitmapDrawable(r, state.mBackground));
            this.mTmpRect = new Rect();
            this.mState = state;
        }

        public void setXfermode(Xfermode mode) {
            getDrawable().setXfermode(mode);
        }

        public int getIntrinsicWidth() {
            return -1;
        }

        public int getIntrinsicHeight() {
            return -1;
        }

        protected void onBoundsChange(Rect bounds) {
            float scale;
            int vwidth = getBounds().width();
            int vheight = getBounds().height();
            int dwidth = this.mState.mBackground.getWidth();
            int dheight = this.mState.mBackground.getHeight();
            if (dwidth * vheight > vwidth * dheight) {
                scale = ((float) vheight) / ((float) dheight);
            } else {
                scale = ((float) vwidth) / ((float) dwidth);
            }
            if (scale <= 1.0f) {
                scale = 1.0f;
            }
            float dy = (((float) vheight) - (((float) dheight) * scale)) * 0.5f;
            this.mTmpRect.set(bounds.left, bounds.top + Math.round(dy), bounds.left + Math.round(((float) dwidth) * scale), bounds.top + Math.round((((float) dheight) * scale) + dy));
            super.onBoundsChange(this.mTmpRect);
        }

        public ConstantState getConstantState() {
            return this.mState;
        }
    }

    public LockscreenWallpaper(Context ctx, PhoneStatusBar bar, Handler h) {
        this.mContext = ctx;
        this.mBar = bar;
        this.mH = h;
        this.mWallpaperManager = (WallpaperManager) ctx.getSystemService("wallpaper");
        try {
            IWallpaperManager.Stub.asInterface(ServiceManager.getService("wallpaper")).setLockWallpaperCallback(this);
        } catch (RemoteException e) {
            Log.e("LockscreenWallpaper", "System dead?" + e);
        }
    }

    public Bitmap getBitmap() {
        if (this.mCached) {
            return this.mCache;
        }
        if (this.mWallpaperManager.isWallpaperSupported()) {
            LoaderResult result = loadBitmap(this.mCurrentUserId, this.mSelectedUser);
            if (result.success) {
                this.mCached = true;
                this.mCache = result.bitmap;
            }
            return this.mCache;
        }
        this.mCached = true;
        this.mCache = null;
        return null;
    }

    public LoaderResult loadBitmap(int currentUserId, UserHandle selectedUser) {
        ParcelFileDescriptor fd = this.mWallpaperManager.getWallpaperFile(2, selectedUser != null ? selectedUser.getIdentifier() : currentUserId);
        if (fd != null) {
            LoaderResult success;
            try {
                success = LoaderResult.success(BitmapFactory.decodeFileDescriptor(fd.getFileDescriptor(), null, new Options()));
                return success;
            } catch (OutOfMemoryError e) {
                Log.w("LockscreenWallpaper", "Can't decode file", e);
                success = LoaderResult.fail();
                return success;
            } finally {
                IoUtils.closeQuietly(fd);
            }
        } else if (selectedUser == null || selectedUser.getIdentifier() == currentUserId) {
            return LoaderResult.success(null);
        } else {
            return LoaderResult.success(this.mWallpaperManager.getBitmapAsUser(selectedUser.getIdentifier()));
        }
    }

    public void setCurrentUser(int user) {
        if (user != this.mCurrentUserId) {
            this.mCached = false;
            this.mCurrentUserId = user;
        }
    }

    public void onWallpaperChanged() {
        Log.i("LockscreenWallpaper", "Lockscrenn WallpaperChanged");
        postUpdateWallpaper();
    }

    public void onBlurWallpaperChanged() {
    }

    private void postUpdateWallpaper() {
        this.mH.removeCallbacks(this);
        this.mH.post(this);
    }

    public void run() {
        if (this.mLoader != null) {
            this.mLoader.cancel(false);
        }
        final int currentUser = this.mCurrentUserId;
        final UserHandle selectedUser = this.mSelectedUser;
        this.mLoader = new AsyncTask<Void, Void, LoaderResult>() {
            protected LoaderResult doInBackground(Void... params) {
                return LockscreenWallpaper.this.loadBitmap(currentUser, selectedUser);
            }

            protected void onPostExecute(LoaderResult result) {
                super.onPostExecute(result);
                if (!isCancelled()) {
                    if (result.success) {
                        LockscreenWallpaper.this.mCached = true;
                        LockscreenWallpaper.this.mCache = result.bitmap;
                        LockscreenWallpaper.this.mBar.updateMediaMetaData(true, true);
                        HwKeyguardPolicy.getInst().onLockscreenWallpaperChanged(LockscreenWallpaper.this.mContext);
                    }
                    LockscreenWallpaper.this.mLoader = null;
                }
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR, new Void[0]);
    }
}
