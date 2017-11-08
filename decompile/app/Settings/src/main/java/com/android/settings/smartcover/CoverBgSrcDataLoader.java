package com.android.settings.smartcover;

import android.content.Context;
import android.os.Bundle;
import java.util.ArrayList;
import java.util.List;

public class CoverBgSrcDataLoader extends AsyncLoader<List<CoverBackgroundSrcInfo>> {
    public CoverBgSrcDataLoader(Context context, Bundle bundle) {
        super(context, bundle);
        this.mBundle = bundle;
    }

    public List<CoverBackgroundSrcInfo> loadInBackground() {
        return getDetailInfos();
    }

    private int[] getCoverBackgroudSrcIdArray() {
        return new int[]{2130837648, 2130837649, 2130837650, 2130837651, 2130837652, 2130837653};
    }

    public List<CoverBackgroundSrcInfo> getDetailInfos() {
        List<CoverBackgroundSrcInfo> srcInfoList = new ArrayList();
        int[] srcIdArray = getCoverBackgroudSrcIdArray();
        if (srcIdArray.length == 0) {
            return srcInfoList;
        }
        int currentBackgroundSrcIndex = -1;
        if (this.mBundle != null) {
            currentBackgroundSrcIndex = this.mBundle.getInt("cover_background_src_index", 0);
        }
        for (int index = 0; index < srcIdArray.length; index++) {
            CoverBackgroundSrcInfo info = new CoverBackgroundSrcInfo();
            info.setImageSrcId(srcIdArray[index]);
            info.setType(-1);
            info.setmImageIndex(index);
            if (currentBackgroundSrcIndex == index) {
                info.setType(8);
            }
            srcInfoList.add(info);
        }
        return srcInfoList;
    }
}
