package com.huawei.keyguard.util;

import android.content.Context;
import android.content.res.Resources.NotFoundException;
import com.android.keyguard.R$array;
import java.util.Arrays;
import java.util.List;

public class MusicXmlParseHelper {
    private static MusicXmlParseHelper sInstance = null;
    private List<String> mPkgList;

    public List<String> getSupportMusicList(Context context) {
        if (this.mPkgList == null || this.mPkgList.isEmpty()) {
            this.mPkgList = parseXmlForMusic(context);
        }
        return this.mPkgList;
    }

    public static MusicXmlParseHelper getInstance() {
        if (sInstance == null) {
            sInstance = new MusicXmlParseHelper();
        }
        return sInstance;
    }

    private MusicXmlParseHelper() {
    }

    private static List<String> parseXmlForMusic(Context context) {
        if (context != null) {
            try {
                if (context.getResources() != null) {
                    return Arrays.asList(context.getResources().getStringArray(R$array.music_package_names));
                }
            } catch (NotFoundException e) {
                HwLog.e("MusicXmlParseHelper", "parseXmlForMusic catch NotFoundException: " + e);
            }
        }
        return null;
    }
}
