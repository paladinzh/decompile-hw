package tmsdkobf;

import java.util.ArrayList;
import java.util.List;
import tmsdk.fg.module.spacemanager.WeChatCacheFiles;
import tmsdk.fg.module.spacemanager.WeChatFileModel;

/* compiled from: Unknown */
public class rj {
    public int NS;
    public int NT = 0;
    public int NU = 0;
    public int NV = 0;
    public int NW = 0;
    public rl NX;
    public rk NY;
    public int mCleanType;
    public String mClearTip;
    public List<WeChatFileModel> mFileModes = new ArrayList(1);
    public String mName;
    public int mScanType;
    public long mTotalSize;
    public List<String> pN;

    public WeChatCacheFiles jG() {
        WeChatCacheFiles weChatCacheFiles = new WeChatCacheFiles();
        weChatCacheFiles.mName = this.mName;
        weChatCacheFiles.mCleanType = this.mCleanType;
        weChatCacheFiles.mClearTip = this.mClearTip;
        weChatCacheFiles.mScanType = this.mScanType;
        weChatCacheFiles.mTotalSize = this.mTotalSize;
        weChatCacheFiles.mFileModes.addAll(this.mFileModes);
        return weChatCacheFiles;
    }
}
