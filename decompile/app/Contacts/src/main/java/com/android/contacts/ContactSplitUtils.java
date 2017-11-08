package com.android.contacts;

import android.animation.Animator;
import android.animation.Animator.AnimatorListener;
import android.animation.AnimatorListenerAdapter;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.graphics.Point;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.util.DisplayMetrics;
import android.view.IWindowManager;
import android.view.IWindowManager.Stub;
import android.view.View;
import android.view.WindowManager;
import com.android.contacts.activities.PeopleActivity;
import com.android.contacts.activities.VoiceSearchResultActivity;
import com.android.contacts.hap.CommonUtilMethods;
import java.math.BigDecimal;

public class ContactSplitUtils {
    private static double mDeviceSize = 0.0d;
    private static boolean mIsAnimRunning = false;
    private static int mLandColumns = 0;
    private static int mPortColumns = 0;

    public static double calculateDeviceSize(Context aContext) {
        if (mDeviceSize > 0.0d || aContext == null) {
            return mDeviceSize;
        }
        IWindowManager iwm = Stub.asInterface(ServiceManager.checkService("window"));
        DisplayMetrics dm = new DisplayMetrics();
        ((WindowManager) aContext.getSystemService("window")).getDefaultDisplay().getRealMetrics(dm);
        if (iwm != null) {
            Point point = new Point();
            try {
                iwm.getInitialDisplaySize(0, point);
                mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) point.x) / dm.xdpi), 2.0d) + Math.pow((double) (((float) point.y) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
                return mDeviceSize;
            } catch (RemoteException e) {
            }
        }
        mDeviceSize = new BigDecimal(Math.sqrt(Math.pow((double) (((float) dm.widthPixels) / dm.xdpi), 2.0d) + Math.pow((double) (((float) dm.heightPixels) / dm.ydpi), 2.0d))).setScale(2, 4).doubleValue();
        return mDeviceSize;
    }

    private static void refreshColumnsNumber(Context aContext, int orientation, int appWidth) {
        double sizeInch = calculateDeviceSize(aContext);
        if (sizeInch < 5.5d && Math.abs(sizeInch - 5.5d) > 0.1d) {
            mPortColumns = 1;
            mLandColumns = 1;
        } else if (sizeInch >= 8.0d) {
            if (orientation != 2) {
                mPortColumns = 2;
            } else if (appWidth >= dip2px(aContext, 592.0f)) {
                mLandColumns = 2;
            } else {
                mLandColumns = 1;
            }
        } else if (orientation != 2) {
            mPortColumns = 1;
        } else if (appWidth >= dip2px(aContext, 592.0f)) {
            mLandColumns = 2;
        } else {
            mLandColumns = 1;
        }
    }

    private static void refreshColumnsNumTwoOrien(Context aContext) {
        int width = ContactDpiAdapter.getScreenSize(aContext, true);
        int height = ContactDpiAdapter.getScreenSize(aContext, false);
        if (2 == aContext.getResources().getConfiguration().orientation) {
            refreshColumnsNumber(aContext, 2, width);
            refreshColumnsNumber(aContext, 1, height);
            return;
        }
        refreshColumnsNumber(aContext, 2, height);
        refreshColumnsNumber(aContext, 1, width);
    }

    public static int getColumnsNumber(Context aContext, int orientation, boolean isInMultiWindow) {
        if (!CommonUtilMethods.calcIfNeedSplitScreen() || isInMultiWindow) {
            return 1;
        }
        if (mLandColumns == 0 || mPortColumns == 0) {
            refreshColumnsNumTwoOrien(aContext);
        }
        if (orientation == 2 && mLandColumns != 0) {
            return mLandColumns;
        }
        if (orientation != 1 || mPortColumns == 0) {
            return 1;
        }
        return mPortColumns;
    }

    public static int getColumnsNumber(Context aContext, boolean isInMultiWindow) {
        return getColumnsNumber(aContext, aContext.getResources().getConfiguration().orientation, isInMultiWindow);
    }

    public static boolean isSpiltTwoColumn(Context aContext, boolean isInMultiWindow) {
        return getColumnsNumber(aContext, isInMultiWindow) == 2;
    }

    public static int dip2px(Context context, float dpValue) {
        return (int) ((dpValue * context.getResources().getDisplayMetrics().density) + 0.5f);
    }

    public static Animator createSplitAnimator(int transit, boolean enter, int nextAnim, final View rootView, int resID, Activity act) {
        if (rootView == null) {
            return null;
        }
        Animator mAnimator = null;
        if (act instanceof PeopleActivity) {
            mAnimator = ((PeopleActivity) act).getAnimator(rootView, transit, enter);
        } else if (act instanceof VoiceSearchResultActivity) {
            mAnimator = ((VoiceSearchResultActivity) act).getAnimator(rootView, transit, enter);
        }
        if (mAnimator == null) {
            return null;
        }
        final int layerType = rootView.getLayerType();
        mAnimator.addListener(new AnimatorListener() {
            public void onAnimationStart(Animator animation) {
                if (2 != layerType) {
                    rootView.setLayerType(2, null);
                }
            }

            public void onAnimationEnd(Animator animation) {
                if (2 != layerType) {
                    rootView.setLayerType(layerType, null);
                }
            }

            public void onAnimationCancel(Animator animation) {
                if (2 != layerType) {
                    rootView.setLayerType(layerType, null);
                }
            }

            public void onAnimationRepeat(Animator animation) {
            }
        });
        return mAnimator;
    }

    public static void startActionbarShowAnimate(final ActionBar bar, View actionbarView) {
        if (actionbarView != null && bar != null && !mIsAnimRunning) {
            ObjectAnimator actionBarAlphaAnim = ObjectAnimator.ofFloat(actionbarView, View.ALPHA, new float[]{1.0f});
            AnimatorSet animatorSet = new AnimatorSet();
            animatorSet.setStartDelay(220);
            animatorSet.play(actionBarAlphaAnim);
            animatorSet.setDuration(30);
            animatorSet.addListener(new AnimatorListenerAdapter() {
                public void onAnimationStart(Animator animation) {
                    ContactSplitUtils.mIsAnimRunning = true;
                }

                public void onAnimationEnd(Animator animator) {
                    ContactSplitUtils.mIsAnimRunning = false;
                    bar.show();
                }

                public void onAnimationCancel(Animator animation) {
                    ContactSplitUtils.mIsAnimRunning = false;
                    bar.show();
                }
            });
            animatorSet.start();
        }
    }
}
