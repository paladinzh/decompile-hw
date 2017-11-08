package com.android.contacts.activities;

import android.content.Context;
import android.content.res.Resources;
import android.view.ContextThemeWrapper;
import android.view.LayoutInflater;
import android.view.View;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;

public class ContactDetailLayoutCache {
    private static View detailsView;
    private static float fontScale;
    private static int mLayoutId;
    private static Object mlock = new Object();
    private static int orientation;

    public static synchronized void clearDetailViewCache() {
        synchronized (ContactDetailLayoutCache.class) {
            synchronized (mlock) {
                detailsView = null;
            }
        }
    }

    public static void inflateDetailsViewInBackground(Context context, final int layoutId) {
        if (HwLog.HWDBG) {
            HwLog.d("Optimization", "CACHE: inflateDetailsViewInBackground called");
        }
        final Context lContext = new ContextThemeWrapper(context.getApplicationContext(), context.getApplicationContext().getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
        lContext.getTheme().applyStyle(R.style.PeopleThemeWithListSelector, true);
        Resources res = lContext.getResources();
        final int tempOrientation = res.getConfiguration().orientation;
        final float tempFontScale = res.getConfiguration().fontScale;
        synchronized (mlock) {
            if (detailsView != null && tempOrientation == orientation) {
                if (((double) Math.abs(fontScale - tempFontScale)) <= 0.001d) {
                    if (mLayoutId != layoutId) {
                    }
                }
            }
            new Thread() {
                public void run() {
                    ContactDetailLayoutCache.inflateDetailsViewInSameThread(lContext, tempFontScale, tempOrientation, layoutId);
                }
            }.start();
        }
    }

    private static void inflateDetailsViewInSameThread(Context context, float tempFontScale, int tempOrientation, int layoutId) {
        synchronized (mlock) {
            fontScale = tempFontScale;
            orientation = tempOrientation;
            mLayoutId = layoutId;
            detailsView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(layoutId, null);
            if (HwLog.HWDBG) {
                HwLog.d("Optimization", "CACHE: inflate details layout...........");
            }
        }
    }

    public static View getCachedDetailsView(Context context, int layoutId) {
        View local;
        synchronized (mlock) {
            int tmpOrientation = context.getResources().getConfiguration().orientation;
            if (HwLog.HWFLOW) {
                HwLog.i("Optimization", "CACHE: getCachedDetailsView layout...orientation=" + orientation + "The current screen Orientation=" + tmpOrientation);
            }
            if (!(orientation == tmpOrientation && mLayoutId == layoutId)) {
                detailsView = null;
            }
            if (detailsView == null) {
                if (HwLog.HWFLOW) {
                    HwLog.i("Optimization", "CACHE: getCachedDetailsView orientation changed or null so re inflating");
                }
                detailsView = ((LayoutInflater) context.getSystemService("layout_inflater")).inflate(layoutId, null);
            } else if (HwLog.HWFLOW) {
                HwLog.i("Optimization", "CACHE: getCachedDetailsView returing cached view+++++++");
            }
            local = detailsView;
            detailsView = null;
        }
        return local;
    }
}
