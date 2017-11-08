package com.huawei.keyguard.view;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.support.v4.view.PagerAdapter;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.widget.ImageView.ScaleType;
import com.android.keyguard.R$layout;
import com.huawei.keyguard.monitor.HwLockScreenReporter;
import com.huawei.keyguard.support.magazine.BigPicture;
import com.huawei.keyguard.support.magazine.BigPictureInfo;
import com.huawei.keyguard.support.magazine.HwFyuseUtils;
import com.huawei.keyguard.support.magazine.KeyguardWallpaper;
import com.huawei.keyguard.support.magazine.MagazineWallpaper;
import com.huawei.keyguard.util.HwLog;
import com.huawei.keyguard.view.effect.bokeh.BokehDrawable;
import com.huawei.openalliance.ad.inter.constant.EventType;
import java.util.ArrayList;
import java.util.List;

public class WallpaperPagerAdapter extends PagerAdapter {
    private static WallpaperPagerAdapter inst = null;
    private static HwBackDropView mBackdrop;
    public final int LOAD_TYPE_DOUBLE_CURRENT_NEXT = 3;
    public final int LOAD_TYPE_DOUBLE_PREV_NEXT = 2;
    public final int LOAD_TYPE_NONE = 0;
    public final int LOAD_TYPE_SINGLE = 1;
    private Context mContext;
    private int mCurrentIndexOfList;
    private int mCurrentPosition = 1073741823;
    private LayoutInflater mInflater;
    private boolean mIsDetailViewLift = false;
    private List<HwMagazineImageView> mListCombinedViews = new ArrayList();
    private boolean mSlideDirectRight = true;

    private class BitmapWorkerTask extends AsyncTask<Integer, Void, BokehDrawable> {
        BigPicture bigPicture;
        BigPictureInfo bigPictureInfo;
        int position;
        int type;

        private BitmapWorkerTask() {
        }

        protected BokehDrawable doInBackground(Integer... params) {
            this.type = params[0].intValue();
            this.position = params[1].intValue();
            MagazineWallpaper magazineWallpaper = MagazineWallpaper.getInst(WallpaperPagerAdapter.this.mContext);
            this.bigPictureInfo = magazineWallpaper.getPictureInfo(this.type);
            this.bigPicture = magazineWallpaper.getBigPicture(this.bigPictureInfo);
            if (this.bigPicture != null) {
                return this.bigPicture.getBokehDrawable(WallpaperPagerAdapter.this.mContext);
            }
            cancel(false);
            return null;
        }

        protected void onPostExecute(BokehDrawable bokehDrawable) {
            if (bokehDrawable == null) {
                HwLog.w("WallpaperPagerAdapter", "onPostExecute bokehDrawable null");
                return;
            }
            HwMagazineImageView view = (HwMagazineImageView) WallpaperPagerAdapter.this.mListCombinedViews.get(this.position);
            view.loadMagazineWallPaper(this.bigPictureInfo, this.bigPicture.getBitmap());
            ImageView imageView = view.getImageView();
            imageView.setVisibility(0);
            imageView.setImageDrawable(bokehDrawable);
            imageView.setContentDescription(this.bigPicture.getPicPath());
            WallpaperPagerAdapter.this.notifyDataSetChanged();
        }

        protected void onCancelled() {
            super.onCancelled();
        }
    }

    public WallpaperPagerAdapter(Context context) {
        this.mContext = context;
        this.mInflater = (LayoutInflater) context.getSystemService("layout_inflater");
        addImage2ListView();
    }

    public static WallpaperPagerAdapter getInst(Context context, HwBackDropView backDropView) {
        WallpaperPagerAdapter wallpaperPagerAdapter;
        synchronized (WallpaperPagerAdapter.class) {
            if (inst == null) {
                inst = new WallpaperPagerAdapter(context);
            }
            if (backDropView != null) {
                mBackdrop = backDropView;
            }
            wallpaperPagerAdapter = inst;
        }
        return wallpaperPagerAdapter;
    }

    public int getCount() {
        return Integer.MAX_VALUE;
    }

    public boolean isViewFromObject(View arg0, Object arg1) {
        return arg0 == arg1;
    }

    public void destroyItem(ViewGroup container, int position, Object object) {
    }

    public Object instantiateItem(ViewGroup container, int position) {
        HwLog.w("WallpaperPagerAdapter", "instantiateItem position is " + position);
        int tempPosition = position % this.mListCombinedViews.size();
        if (tempPosition < 0) {
            tempPosition += this.mListCombinedViews.size();
        }
        HwLog.w("WallpaperPagerAdapter", " instantiateItem mListCombinedViews.get(" + tempPosition + ")");
        HwMagazineImageView view = (HwMagazineImageView) this.mListCombinedViews.get(tempPosition);
        ViewParent vp = view.getParent();
        if (vp != null) {
            ((ViewGroup) vp).removeView(view);
        }
        container.addView(view);
        return view;
    }

    public void setLiftState(boolean lift) {
        this.mIsDetailViewLift = lift;
    }

    private HwMagazineImageView addCombindView(Context context, Drawable drawable, String tag) {
        int resID;
        if (HwFyuseUtils.isSupport3DFyuse()) {
            resID = R$layout.magazine_wallpaper_child_layout;
        } else {
            resID = R$layout.magazine_wallpaper_child_no_fyuse_layout;
        }
        HwMagazineImageView view = (HwMagazineImageView) this.mInflater.inflate(resID, null, false);
        view.getImageView().setScaleType(ScaleType.CENTER_CROP);
        view.setTag(tag);
        return view;
    }

    public final void addImage2ListView() {
        if (this.mListCombinedViews != null && this.mListCombinedViews.size() == 0) {
            this.mListCombinedViews.add(addCombindView(this.mContext, null, "fyusetag_0"));
            this.mListCombinedViews.add(addCombindView(this.mContext, null, "fyusetag_1"));
            this.mListCombinedViews.add(addCombindView(this.mContext, null, "fyusetag_2"));
        }
    }

    private int getNextListIndex(int index, int loadType) {
        synchronized (this) {
            int total = this.mListCombinedViews.size();
            if (index >= total || index < 0) {
                HwLog.w("WallpaperPagerAdapter", "Fail get next index - " + index + "/" + total);
                return 0;
            }
            int indexChecked = index;
            if (loadType == 1) {
                indexChecked = index + 1;
                if (indexChecked >= total) {
                    indexChecked = 0;
                }
            } else if (loadType == -1) {
                indexChecked = index - 1;
                if (indexChecked < 0) {
                    indexChecked = total - 1;
                }
            } else if (loadType == -2) {
                indexChecked = index - 2;
                if (indexChecked < 0) {
                    indexChecked += total;
                }
            } else if (loadType == 2) {
                indexChecked = index + 2;
                if (indexChecked >= total) {
                    indexChecked -= total;
                }
            }
            HwLog.w("WallpaperPagerAdapter", "getNextListIndex indexChecked after:" + indexChecked);
            return indexChecked;
        }
    }

    public void disableAllFyuseMotion() {
        HwLog.i("WallpaperPagerAdapter", " disableFyuseMotion");
        disableFyuseMotion(null);
    }

    public void disableFyuseMotion(HwMagazineImageView currentView) {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            for (HwMagazineImageView view : this.mListCombinedViews) {
                if (currentView != view) {
                    enableFyuseMotion(view, false);
                }
            }
        }
    }

    public void clearViewData(HwMagazineImageView currentView) {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            for (HwMagazineImageView view : this.mListCombinedViews) {
                if (!(currentView == view || view.getFyuseView() == null)) {
                    view.getFyuseView().destroySurface();
                }
            }
            HwLog.i("WallpaperPagerAdapter", "clearViewData");
        }
    }

    private void enableFyuseMotion(HwMagazineImageView view, boolean enable) {
        if (view != null) {
            HwFyuseView fyuseView = view.getFyuseView();
            if (fyuseView != null) {
                fyuseView.enableMotion(enable);
            }
        }
    }

    public void switchPagerPic(int currentPosition) {
        int i;
        int i2;
        int i3 = 1;
        if (currentPosition > this.mCurrentPosition) {
            this.mCurrentPosition = currentPosition;
            this.mSlideDirectRight = true;
        } else {
            this.mCurrentPosition = currentPosition;
            this.mSlideDirectRight = false;
        }
        HwLog.w("WallpaperPagerAdapter", "switchMagazine");
        KeyguardWallpaper inst = KeyguardWallpaper.getInst(this.mContext);
        if (this.mSlideDirectRight) {
            i = 1;
        } else {
            i = -1;
        }
        inst.switchMagazine(i);
        Context context = this.mContext;
        i = this.mSlideDirectRight ? 105 : 104;
        if (this.mSlideDirectRight) {
            i2 = 1;
        } else {
            i2 = -1;
        }
        HwLockScreenReporter.reportMagazinePictureInfo(context, i, i2);
        setBackdropView(currentPosition);
        Context context2 = this.mContext;
        EventType eventType = EventType.IMPRESSION;
        if (!this.mSlideDirectRight) {
            i3 = -1;
        }
        HwLockScreenReporter.reportPicInfoAdEvent(context2, eventType, 1001, i3);
    }

    public void setBackdropView(int currentPosition) {
        if (mBackdrop != null) {
            HwLog.w("WallpaperPagerAdapter", "setBackdropBackImage");
            HwMagazineImageView currentView = getViewFromList(currentPosition, true);
            if (currentView != null) {
                mBackdrop.setBackdropBackImage(currentView);
                return;
            } else {
                HwLog.w("WallpaperPagerAdapter", "ListView is empty !");
                return;
            }
        }
        HwLog.w("WallpaperPagerAdapter", "mBackdrop is null !");
    }

    public int getCurrentPosition() {
        return this.mCurrentPosition;
    }

    public HwMagazineImageView getViewFromList(int position, boolean setCurrent) {
        int size = this.mListCombinedViews.size();
        if (size == 0) {
            return null;
        }
        position %= size;
        if (position < 0) {
            position += this.mListCombinedViews.size();
        }
        if (setCurrent) {
            this.mCurrentIndexOfList = position;
        }
        HwLog.w("WallpaperPagerAdapter", "getViewFromList view (" + position + ")");
        return (HwMagazineImageView) this.mListCombinedViews.get(position);
    }

    private void loadPicViaAsyncTask(int loadType1, int loadType2) {
        loadPicViaAsyncTask(loadType1, loadType2, false);
    }

    private void loadPicViaAsyncTask(int loadType1, int loadType2, boolean forceUpdate) {
        setFyuseViewDefaultDrawable(loadType1, getNextListIndex(this.mCurrentIndexOfList, loadType1), forceUpdate);
        new BitmapWorkerTask().execute(new Integer[]{Integer.valueOf(loadType1), Integer.valueOf(nextIndex)});
        if (loadType2 != 0) {
            setFyuseViewDefaultDrawable(loadType2, getNextListIndex(this.mCurrentIndexOfList, loadType2), forceUpdate);
            new BitmapWorkerTask().execute(new Integer[]{Integer.valueOf(loadType2), Integer.valueOf(nextIndex)});
        }
    }

    private void setFyuseViewDefaultDrawable(int type, int position, boolean forceUpdate) {
        if (HwFyuseUtils.isSupport3DFyuse()) {
            BigPictureInfo bigPictureInfo = MagazineWallpaper.getInst(this.mContext).getPictureInfo(type);
            if (bigPictureInfo != null && bigPictureInfo.isFyuseFormatPic()) {
                HwFyuseView fyuseview = ((HwMagazineImageView) this.mListCombinedViews.get(position)).getFyuseView();
                if (fyuseview != null) {
                    fyuseview.setDynamicFyuseView(bigPictureInfo.getPicPath(), forceUpdate);
                }
            }
        }
    }

    public void loadPagerImageView(int loadType) {
        HwLog.w("WallpaperPagerAdapter", "loadPagerImageView loadType:" + loadType + " mCurrentPosition:" + this.mCurrentPosition);
        switch (loadType) {
            case 1:
                int mLoadType = this.mSlideDirectRight ? 1 : -1;
                HwLog.w("WallpaperPagerAdapter", "1 load  mCurrentIndexOfList:" + this.mCurrentIndexOfList + " mLoadType:" + mLoadType);
                loadPicViaAsyncTask(mLoadType, 0);
                return;
            case 2:
                HwLog.w("WallpaperPagerAdapter", "2 P_N load  mCurrentIndexOfList:" + this.mCurrentIndexOfList);
                setBackdropView(this.mCurrentPosition);
                loadPicViaAsyncTask(1, -1, true);
                return;
            case 3:
                HwLog.w("WallpaperPagerAdapter", "2 C_N load  mCurrentIndexOfList:" + this.mCurrentIndexOfList);
                setBackdropView(this.mCurrentPosition);
                loadPicViaAsyncTask(0, 1);
                return;
            default:
                return;
        }
    }
}
