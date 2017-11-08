package com.huawei.systemmanager.optimize.process.Predicate;

import android.content.Context;
import android.hsm.MediaTransactWrapper;
import android.media.AudioManager;
import com.huawei.systemmanager.optimize.process.ProcessAppItem;
import com.huawei.systemmanager.util.HwLog;
import java.util.HashSet;
import java.util.Set;

public class MusicPlayingPredicate extends FutureTaskPredicate<Set<Integer>, ProcessAppItem> {
    private static final String TAG = "MusicPlayingPredicate";
    private Context mCtx;

    public MusicPlayingPredicate(Context ctx) {
        this.mCtx = ctx;
    }

    public boolean apply(ProcessAppItem input) {
        if (input == null) {
            return false;
        }
        if (!((Set) getResult()).contains(Integer.valueOf(input.getUid()))) {
            return true;
        }
        HwLog.i(TAG, "MusicPlayingPredicate " + input.getName() + ", pkg=" + input.getPackageName());
        return false;
    }

    protected Set<Integer> doInbackground() throws Exception {
        Set<Integer> result = new HashSet();
        try {
            AudioManager am = (AudioManager) this.mCtx.getSystemService("audio");
            if (am != null && am.isMusicActive()) {
                result.addAll(MediaTransactWrapper.playingMusicUidSet());
            }
        } catch (Exception ex) {
            HwLog.e(TAG, "doInbackground catch exception: " + ex.getMessage());
        }
        HwLog.d(TAG, "doInbackground result: " + result);
        return result;
    }
}
