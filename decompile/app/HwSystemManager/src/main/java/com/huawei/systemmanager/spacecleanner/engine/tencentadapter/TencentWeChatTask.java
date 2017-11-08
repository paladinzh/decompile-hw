package com.huawei.systemmanager.spacecleanner.engine.tencentadapter;

import android.content.Context;
import com.huawei.systemmanager.R;
import com.huawei.systemmanager.comm.collections.HsmCollections;
import com.huawei.systemmanager.comm.misc.MimeTypeHelper;
import com.huawei.systemmanager.comm.misc.Utility;
import com.huawei.systemmanager.spacecleanner.engine.ScanParams;
import com.huawei.systemmanager.spacecleanner.engine.base.Task;
import com.huawei.systemmanager.util.HwLog;
import java.util.List;
import tmsdk.fg.creator.ManagerCreatorF;
import tmsdk.fg.module.spacemanager.ISpaceScanListener;
import tmsdk.fg.module.spacemanager.SpaceManager;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;
import tmsdk.fg.module.spacemanager.WeChatFileModel;

public class TencentWeChatTask extends Task {
    private static final String TAG = "TencentWeChatTask";
    private SpaceManager mSpaceManager = ((SpaceManager) ManagerCreatorF.getManager(SpaceManager.class));
    private WeChatScanListener mWeChatScanListener = new WeChatScanListener();

    private class WeChatScanListener implements ISpaceScanListener {
        private static final String TAG = "WeChatScanListener";
        private List<WeChatCacheFiles> mWeChatScanResult;

        private WeChatScanListener() {
        }

        public void onFound(Object obj) {
        }

        public void onFinish(int aErrorCode, Object obj) {
            if (!(obj instanceof List)) {
                HwLog.e(TAG, "TencentWeChat onfinish failed , Error Code = " + aErrorCode);
            } else if (!TencentWeChatTask.this.checkIsCanceled()) {
                this.mWeChatScanResult = (List) obj;
                if (this.mWeChatScanResult.size() > 0 && (this.mWeChatScanResult.get(0) instanceof WeChatCacheFiles)) {
                    changeToHwData();
                }
                HwLog.i(TAG, "onFinish success");
            } else {
                return;
            }
            TencentWeChatTask.this.onPublishEnd();
        }

        public void onProgressChanged(int percent) {
        }

        public void onCancelFinished() {
            HwLog.i(TAG, "onCancelFinished");
        }

        public void onStart() {
            HwLog.i(TAG, "onScanStart");
        }

        private void changeToHwData() {
            for (WeChatCacheFiles files : this.mWeChatScanResult) {
                if (files != null && WeChatTypeCons.isWeChatMediaType(files.mScanType)) {
                    HwLog.i(TAG, "changeToHwData ::    name is:  " + files.mName + " type is:  " + files.mScanType);
                    createTrashGroup(files);
                }
            }
        }

        private void createTrashGroup(WeChatCacheFiles files) {
            WeChatTrashGroup group = new WeChatTrashGroup(files.mScanType, files.mName);
            for (WeChatFileModel fileModel : files.mFileModes) {
                if (!WeChatTypeCons.isWeChatVideo(files.mScanType) || !Utility.isPhoto(MimeTypeHelper.getInstance().getMimeType(fileModel.getFilePath()))) {
                    group.addChild(TecentWeChatTrashFile.creator(fileModel.getFilePath(), 1048576, fileModel.mYear, fileModel.mMonth));
                }
            }
            TencentWeChatTask.this.onPublishItemUpdate(group);
        }
    }

    public TencentWeChatTask(Context ctx) {
        super(ctx);
    }

    public void cancel() {
        if (isEnd()) {
            HwLog.i(TAG, "cancel, task is already end");
            return;
        }
        setCanceled(true);
        this.mSpaceManager.stopWechatScan();
    }

    public String getTaskName() {
        return TAG;
    }

    public int getType() {
        return 54;
    }

    public List<Integer> getSupportTrashType() {
        return HsmCollections.newArrayList(Integer.valueOf(1048576));
    }

    public boolean isNormal() {
        return false;
    }

    protected void startWork(ScanParams params) {
        this.mSpaceManager.wechatScan(this.mWeChatScanListener);
    }

    protected int getWeight() {
        return getContext().getResources().getInteger(R.integer.scan_weight_trash_tencent_wechat_scan);
    }

    private boolean checkIsCanceled() {
        if (!isCanceled()) {
            return false;
        }
        HwLog.i(TAG, "onFinish, but task is canceled");
        onPublishEnd();
        return true;
    }
}
