package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;

public class PredicateManager {
    public static Predicate<ProcessAppItem> getSleepingSavePredicate(Context ctx) {
        new NonProtectedListAppPredicate(ctx).executeTask();
        new DefaultSmsPredicate(ctx).executeTask();
        LauncherPredicate launcherPre = new LauncherPredicate(ctx, true);
        InputMethodPredicate inputPre = new InputMethodPredicate(ctx, true);
        WallPaperPredicate wallPre = new WallPaperPredicate(ctx, true);
        new ClockAppPredicate(ctx).executeTask();
        new MusicPlayingPredicate(ctx).executeTask();
        new HighLocationAppPredicate(ctx).executeTask();
        return Predicates.and(nonProtectPre, dftSms, launcherPre, inputPre, wallPre, clockAppPre, musicPredicate, highLocationAppPredicate);
    }
}
