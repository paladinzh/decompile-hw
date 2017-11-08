package com.huawei.systemmanager.customize;

import android.content.Context;
import com.huawei.android.util.NoExtAPIException;
import com.huawei.cust.HwCfgFilePolicy;
import com.huawei.systemmanager.comm.misc.GlobalContext;
import com.huawei.systemmanager.util.HwLog;
import java.io.File;

public class CustomizeManager {
    private static CustomizeManager sInstance;
    private Context mContext = GlobalContext.getContext();
    private CustConifgChecker mCustChecker = new CustConifgChecker();
    private OptChecker mOptChecker = new OptChecker(this.mContext);
    private UpdateCheckRecorder mUpdateRecorder = new UpdateCheckRecorder();

    private CustomizeManager() {
    }

    public static synchronized CustomizeManager getInstance() {
        CustomizeManager customizeManager;
        synchronized (CustomizeManager.class) {
            if (sInstance == null) {
                sInstance = new CustomizeManager();
            }
            customizeManager = sInstance;
        }
        return customizeManager;
    }

    public boolean isFeatureEnabled(int featureCode) {
        return isFeatureEnabled(GlobalContext.getContext(), featureCode);
    }

    public boolean isFeatureEnabled(Context ctx, int featureCode) {
        boolean z = false;
        int custConfig = this.mCustChecker.isFeatureEnabled(featureCode);
        if (custConfig == -1) {
            return this.mOptChecker.isEnableByOpt(ctx, featureCode);
        }
        if (custConfig == 0) {
            z = true;
        }
        return z;
    }

    public int getFeatureIntConfig(Context ctx, String featureName, int featureCode, int dftValue) {
        int type = this.mCustChecker.getFeatureIntConfig(featureName);
        if (type != -1) {
            return type;
        }
        return this.mOptChecker.getInt(ctx, featureCode, dftValue);
    }

    int checkConfigFileChange(String fileName) {
        if (this.mUpdateRecorder.isRecorded(fileName)) {
            return 4;
        }
        this.mUpdateRecorder.record(fileName);
        return new XmlUpdateChecker(this.mContext).checkConfigFileChange(fileName);
    }

    void finishConfigFileChange(String fileName) {
        new XmlUpdateChecker(this.mContext).finishConfigFileChange(fileName);
    }

    public static String composeCustFileName(String baseName) {
        File file = null;
        try {
            file = HwCfgFilePolicy.getCfgFile(baseName, 0);
        } catch (NoExtAPIException e) {
            HwLog.e("Customize", "HwCfgFilePolicy.getCfgFile not supported.");
        } catch (NoClassDefFoundError e2) {
            HwLog.e("Customize", "HwCfgFilePolicy.getCfgFile not supported.");
        } catch (Exception e3) {
            HwLog.e("Customize", "Exception");
        }
        if (file == null) {
            return "/data/cust/" + baseName;
        }
        return file.getAbsolutePath();
    }
}
