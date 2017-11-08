package com.android.systemui.volume;

import android.content.Context;
import android.os.SystemProperties;
import android.provider.Settings.Global;
import android.util.Log;
import android.view.WindowManager.LayoutParams;
import android.widget.Toast;
import com.android.systemui.R;
import com.android.systemui.utils.ReportTool;

public class HwCustVolumeDialogControllerImpl extends HwCustVolumeDialogController {
    private static final int SREAMTYPE_VOLUME = 4;
    private static final int STATE_TURNING_OFF_ALL_SOUND = 1;
    private static final int STATE_TURNING_ON_ALL_SOUND = 2;
    private static final int STATE_TURN_OFF_ALL_SOUND = 3;
    private static final int STATE_TURN_ON_ALL_SOUND = 0;
    private static final String SYSTEM_TURNOFF_ALL_SOUND = "trun_off_all_sound";
    private static final String TAG = "HwCustVolumeDialogControllerImpl";
    private Toast mAdjustVolToast = null;
    private Context mContext;
    private boolean mIsFuncTurnOffAllSoundValidate;
    private VolumeDialogController mVolumeDialogController;

    public HwCustVolumeDialogControllerImpl(VolumeDialogController volumeDialogController, Context context) {
        super(volumeDialogController, context);
        this.mContext = context;
        this.mVolumeDialogController = volumeDialogController;
        this.mIsFuncTurnOffAllSoundValidate = SystemProperties.getBoolean("ro.show_turn_off_all_sound", false);
    }

    public void saveAfterChangeVolume(int type, int volume) {
        int[] newstate = new int[]{type, volume};
        ReportTool.getInstance(this.mContext).report(4, String.format("{StreamType:%d,Volume:%d}", new Object[]{Integer.valueOf(newstate[0]), Integer.valueOf(newstate[1])}));
        Log.e(TAG, "Type and volume:" + String.format("{StreamType:%d,Volume:%d}", new Object[]{Integer.valueOf(newstate[0]), Integer.valueOf(newstate[1])}));
    }

    public void showToastForShutdown() {
        if (this.mIsFuncTurnOffAllSoundValidate) {
            int state = Global.getInt(this.mContext.getContentResolver(), SYSTEM_TURNOFF_ALL_SOUND, 0);
            if (state == 3 || state == 1) {
                if (this.mAdjustVolToast == null) {
                    this.mAdjustVolToast = Toast.makeText(this.mContext, R.string.cannot_adjuest_volume, 0);
                }
                LayoutParams windowParams = this.mAdjustVolToast.getWindowParams();
                windowParams.privateFlags |= 16;
                this.mAdjustVolToast.show();
            }
        }
    }
}
