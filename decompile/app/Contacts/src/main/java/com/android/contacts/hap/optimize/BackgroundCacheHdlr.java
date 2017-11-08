package com.android.contacts.hap.optimize;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewStub;
import android.widget.ListView;
import com.android.contacts.hap.CommonUtilMethods;
import com.android.contacts.util.HwLog;
import com.google.android.gms.R;
import com.huawei.cspcommon.util.ViewUtil;
import java.util.List;
import java.util.Vector;

public class BackgroundCacheHdlr {
    private static View mAllListView;
    private static List<View> mCalllogItemViewArray = new Vector();
    private static int mOrientationWhenInflated;
    private static Drawable mSelectAllDrawable;
    private static Drawable mSelectNoneDrawable;
    private static BackgroundCacheHdlr sInstance;
    private Context mContext;
    private LayoutThread mLayoutThread;

    private static class LayoutThread implements Runnable {
        Context context;

        public LayoutThread(Context lContext) {
            this.context = lContext.getApplicationContext();
        }

        public void run() {
            try {
                BackgroundCacheHdlr.inflateLayouts(this.context);
                if (HwLog.HWDBG) {
                    HwLog.d("BackgroundCacheHdlr", "BackgroundGenricHandler.init over");
                }
            } catch (InflateException e) {
                HwLog.e("BackgroundCacheHdlr", "BackgroundGenricHandler.init  inflateLayouts exception");
            }
        }
    }

    public static BackgroundCacheHdlr inflateLayoutsInBackground(Context aContext) {
        if (sInstance == null) {
            sInstance = new BackgroundCacheHdlr(aContext);
        } else if (!(aContext == null || aContext.equals(sInstance.mContext))) {
            sInstance = new BackgroundCacheHdlr(aContext);
        }
        sInstance.init();
        return sInstance;
    }

    private BackgroundCacheHdlr(Context aContext) {
        this.mContext = aContext.getApplicationContext();
    }

    private void init() {
        if (HwLog.HWDBG) {
            HwLog.d("BackgroundCacheHdlr", "BackgroundGenricHandler.init start");
        }
        Handler handler = new Handler(Looper.getMainLooper());
        if (this.mLayoutThread != null) {
            handler.removeCallbacks(this.mLayoutThread);
        }
        this.mLayoutThread = new LayoutThread(this.mContext);
        handler.post(this.mLayoutThread);
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized View getAndUpdateAllListLayout(Context context) {
        synchronized (BackgroundCacheHdlr.class) {
            View lAllListView = mAllListView;
            if (lAllListView != null) {
                if (mOrientationWhenInflated == context.getResources().getConfiguration().orientation) {
                    mAllListView = null;
                    return lAllListView;
                }
                mAllListView = null;
            }
        }
    }

    public static synchronized Drawable getSelectAllDrawable(Context context) {
        synchronized (BackgroundCacheHdlr.class) {
            if (mOrientationWhenInflated != context.getResources().getConfiguration().orientation) {
                return null;
            }
            Drawable lSelectAllDrawable = mSelectAllDrawable;
            mSelectAllDrawable = null;
            return lSelectAllDrawable;
        }
    }

    public static synchronized Drawable getSelectNoneDrawable(Context context) {
        synchronized (BackgroundCacheHdlr.class) {
            if (mOrientationWhenInflated != context.getResources().getConfiguration().orientation) {
                return null;
            }
            Drawable lSelectNoneDrawable = mSelectNoneDrawable;
            mSelectNoneDrawable = null;
            return lSelectNoneDrawable;
        }
    }

    /* JADX WARNING: inconsistent code. */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static synchronized void inflateLayouts(Context lContext) {
        synchronized (BackgroundCacheHdlr.class) {
            Context context = new HwContextThemeWrapper(lContext.getApplicationContext(), lContext.getResources().getIdentifier("androidhwext:style/Theme.Emui", null, null));
            context.getTheme().applyStyle(R.style.PeopleTheme, true);
            LayoutInflater inflator = (LayoutInflater) context.getSystemService("layout_inflater");
            if (inflator == null) {
                return;
            }
            switch (context.getApplicationContext().getResources().getConfiguration().orientation) {
                case 1:
                    mOrientationWhenInflated = 1;
                    break;
                case 2:
                    mOrientationWhenInflated = 2;
                    break;
            }
            if (mSelectAllDrawable == null) {
                mSelectAllDrawable = ViewUtil.getSelectAllItemIcon(context);
            }
            if (mSelectNoneDrawable == null) {
                mSelectNoneDrawable = ViewUtil.getSelectNoneItemIcon(context);
            }
            if (mAllListView == null) {
                mAllListView = inflator.inflate(R.layout.contact_list_content, null);
                ListView listView = (ListView) mAllListView.findViewById(16908298);
                if (listView == null) {
                    ViewStub lViewStub = (ViewStub) mAllListView.findViewById(R.id.pinnedHeaderList_stub);
                    if (lViewStub != null) {
                        lViewStub.setLayoutResource(CommonUtilMethods.getPinnedHeaderListViewResId(context.getApplicationContext()));
                        lViewStub.inflate();
                    }
                } else {
                    listView.setFastScrollEnabled(true);
                }
            }
        }
    }

    public static synchronized void clearCallLogBackgroundCache() {
        synchronized (BackgroundCacheHdlr.class) {
            if (mCalllogItemViewArray != null) {
                mCalllogItemViewArray.clear();
            }
        }
    }

    public static synchronized void clearAllListViewCache() {
        synchronized (BackgroundCacheHdlr.class) {
            mAllListView = null;
        }
    }

    public static synchronized boolean haveNotBeenInflate() {
        boolean z;
        synchronized (BackgroundCacheHdlr.class) {
            z = mAllListView == null;
        }
        return z;
    }
}
