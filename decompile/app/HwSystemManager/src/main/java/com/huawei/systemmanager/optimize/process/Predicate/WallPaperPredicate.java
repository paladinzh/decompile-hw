package com.huawei.systemmanager.optimize.process.Predicate;

import android.app.WallpaperInfo;
import android.app.WallpaperManager;
import android.content.Context;
import android.text.TextUtils;
import com.google.common.base.Predicate;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.Objects;

public class WallPaperPredicate implements Predicate<ProcessAppItem> {
    private static final String TAG = "WallPaperPredicate";
    private final WallPaperStringPredicate mPredicate;
    private final boolean mShouldFilter;

    public static class WallPaperStringPredicate implements Predicate<String> {
        private final String wallPaper;

        public WallPaperStringPredicate(Context context) {
            this.wallPaper = WallPaperPredicate.getCurrentWallPaper(context);
        }

        public boolean apply(String input) {
            if (TextUtils.isEmpty(input) || !Objects.equals(this.wallPaper, input)) {
                return false;
            }
            HwLog.i(WallPaperPredicate.TAG, "WallPaperStringPredicate :: name = " + input);
            return true;
        }
    }

    public WallPaperPredicate(Context ctx, boolean filter) {
        this.mPredicate = new WallPaperStringPredicate(ctx);
        this.mShouldFilter = filter;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        String pkg = input.getPackageName();
        if (this.mPredicate.apply(pkg)) {
            input.setKeyTask(true);
            if (this.mShouldFilter) {
                HwLog.i(TAG, "WallPaperPredicate :: name = " + input.getName() + "; getPackageName = " + pkg);
                return false;
            }
        }
        return true;
    }

    private static String getCurrentWallPaper(Context ctx) {
        String wallPagerPackageName = "";
        WallpaperManager wpm = WallpaperManager.getInstance(ctx);
        if (wpm == null) {
            HwLog.e(TAG, "getCurrentWallPaper WallpaperManager wpm is NULL");
            return wallPagerPackageName;
        }
        WallpaperInfo wallpaperInfo = wpm.getWallpaperInfo();
        if (wallpaperInfo != null) {
            wallPagerPackageName = wallpaperInfo.getPackageName();
        }
        return wallPagerPackageName;
    }
}
