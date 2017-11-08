package com.huawei.gallery.app;

import android.text.TextUtils;
import com.android.gallery3d.app.GalleryContext;
import com.android.gallery3d.data.MediaSet;
import com.android.gallery3d.data.PhotoShareTimeBucketAlbum.CloudShareGroupData;
import com.huawei.gallery.data.AbsGroupData;
import java.util.ArrayList;

public class PhotoShareTimeBucketDataLoader extends MediaItemsDataLoader {
    public PhotoShareTimeBucketDataLoader(GalleryContext context, MediaSet mediaSet) {
        super(context, mediaSet);
    }

    protected boolean groupDatasChange(ArrayList<AbsGroupData> groupDatas) {
        synchronized (GROUPCOUNT_LOCK) {
            if (super.groupDatasChange(groupDatas)) {
                return true;
            }
            boolean isOnlyNickNameChanged = isOnlyNickNameChanged(groupDatas);
            return isOnlyNickNameChanged;
        }
    }

    private boolean isOnlyNickNameChanged(ArrayList<AbsGroupData> groupDatas) {
        for (int i = 0; i < this.mGroupDatas.size(); i++) {
            String oldNickName = ((CloudShareGroupData) this.mGroupDatas.get(i)).createrNickName;
            String newNickName = ((CloudShareGroupData) groupDatas.get(i)).createrNickName;
            if (!TextUtils.isEmpty(oldNickName) && !TextUtils.isEmpty(newNickName) && !oldNickName.equals(newNickName)) {
                return true;
            }
        }
        return false;
    }
}
